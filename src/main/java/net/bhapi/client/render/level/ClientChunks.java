package net.bhapi.client.render.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.block.BlockBreakingInfo;
import net.bhapi.client.render.block.BreakInfo;
import net.bhapi.client.render.culling.FrustumCulling;
import net.bhapi.client.render.texture.RenderLayer;
import net.bhapi.client.render.vbo.IndexedVBO;
import net.bhapi.client.render.vbo.VBO;
import net.bhapi.config.BHConfigs;
import net.bhapi.level.ChunkSection;
import net.bhapi.level.ChunkSectionProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Vec3F;
import net.bhapi.storage.Vec3I;
import net.bhapi.storage.WorldCache;
import net.bhapi.util.MathUtil;
import net.bhapi.util.ThreadManager;
import net.bhapi.util.ThreadManager.RunnableThread;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.blockentity.BlockEntityRenderer;
import net.minecraft.client.render.entity.BlockEntityRenderDispatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.level.Level;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@Environment(EnvType.CLIENT)
public class ClientChunks {
	private static final Queue<Vec3I> UPDATE_REQUESTS = new ArrayBlockingQueue<>(4096);
	private static final FrustumCulling FRUSTUM_CULLING = new FrustumCulling();
	
	private static WorldCache<ClientChunk> chunks;
	private static RunnableThread[] buildingThreads;
	private static RenderLayer layer;
	private static double px, py, pz;
	private static Vec3I cameraPos;
	private static boolean sort;
	private static int oldLight;
	private static Level level;
	private static float delta;
	
	public static void init() {
		int viewDistance = BHAPIClient.getMinecraft().options.viewDistance;
		int side = 64 << 3 - viewDistance;
		if (side > 512) side = 512;
		int sectionCountXZ = side >> 4 | 1;
		int sectionCountY = sectionCountXZ < 17 ? sectionCountXZ : side >> 5 | 1;
		init(sectionCountXZ, sectionCountY);
		
		if (cameraPos == null) cameraPos = new Vec3I(0, Integer.MIN_VALUE, 0);
		sort = true;
		
		if (buildingThreads == null) {
			int count = BHConfigs.GENERAL.getInt("multithreading.meshBuildersCount", 4);
			count = MathUtil.clamp(count, 1, 16);
			BHConfigs.GENERAL.setInt("multithreading.meshBuildersCount", count);
			BHConfigs.GENERAL.save();
			
			buildingThreads = new RunnableThread[count];
			for (int i = 0; i < count; i++) {
				final BHBlockRenderer renderer = new BHBlockRenderer();
				buildingThreads[i] = ThreadManager.makeThread("chunk_mesh_builder_" + i, () -> buildMeshes(renderer));
				buildingThreads[i].start();
			}
		}
		
		FRUSTUM_CULLING.setViewAngle((float) Math.toRadians(50F));
	}
	
	private static void init(int width, int height) {
		if (chunks == null || chunks.getSizeXZ() != width || chunks.getSizeY() != height) {
			if (chunks != null) {
				chunks.forEach((pos, chunk) -> chunk.dispose());
			}
			chunks = new WorldCache<>(
				width, height,
				ClientChunks::updateChunk,
				ClientChunks::needUpdate,
				ClientChunk::new
			);
			UPDATE_REQUESTS.clear();
		}
	}
	
	public static void onExit() {
		if (chunks != null) {
			chunks.forEach((pos, chunk) -> chunk.dispose());
		}
	}
	
	public static void update(Vec3I pos) {
		ClientChunk chunk = chunks.get(pos);
		if (chunk != null) chunk.needUpdate = true;
	}
	
	public static void updateAll() {
		UPDATE_REQUESTS.clear();
		chunks.forEach((pos, chunk) -> chunk.needUpdate = true);
	}
	
	public static void render(LivingEntity entity, float delta) {
		if (chunks == null) return;
		
		Level clientLevel = BHAPIClient.getMinecraft().level;
		if (clientLevel != level) {
			level = clientLevel;
			UPDATE_REQUESTS.clear();
			chunks.forEach((pos, chunk) -> {
				chunk.needUpdate = true;
				chunk.markEmpty();
			});
			return;
		}
		
		if (!level.dimension.noSkyLight) {
			int light = level.getEnvironmentLight(delta);
			if (oldLight != light) {
				oldLight = light;
				updateAll();
			}
		}
		
		chunks.setCenter(entity.chunkX, (int) entity.y >> 4, entity.chunkZ);
		chunks.update(16);
		
		px = MathUtil.lerp(entity.prevX, entity.x, delta);
		py = MathUtil.lerp(entity.prevY, entity.y, delta);
		pz = MathUtil.lerp(entity.prevZ, entity.z, delta);
		float yaw = (float) Math.toRadians(entity.yaw);
		float pitch = (float) Math.toRadians(entity.pitch);
		FRUSTUM_CULLING.rotate(-yaw, pitch);
		
		ClientChunks.delta = delta;
		chunks.forEach(ClientChunks::checkVisibility);
		
		RenderHelper.disableLighting();
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		layer = RenderLayer.SOLID;
		chunks.forEach(ClientChunks::renderChunk);
		
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		layer = RenderLayer.TRANSPARENT;
		chunks.forEach(ClientChunks::renderChunk);
		
		int ix = (int) px;
		int iy = (int) py;
		int iz = (int) pz;
		if (ix != cameraPos.x || iy != cameraPos.y || iz != cameraPos.z) {
			cameraPos.set(ix, iy, iz);
			sort = true;
		}
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		layer = RenderLayer.TRANSLUCENT;
		chunks.forEach(ClientChunks::renderChunk, true);
		
		GL11.glDisable(GL11.GL_BLEND);
		VBO.unbind();
		sort = false;
		
		RenderHelper.enableLighting();
		chunks.forEach(ClientChunks::renderBlockEntities, true);
	}
	
	private static void updateChunk(Vec3I pos, ClientChunk chunk) {
		if (!chunk.needUpdate) return;
		if (UPDATE_REQUESTS.size() > 4095) return;
		chunk.needUpdate = false;
		UPDATE_REQUESTS.add(pos.clone());
		
		chunk.blockEntities.clear();
		short sections = LevelHeightProvider.cast(level).getSectionsCount();
		if (pos.y < 0 || pos.y >= sections) return;
		
		ChunkSectionProvider provider = ChunkSectionProvider.cast(level.getChunkFromCache(pos.x, pos.z));
		ChunkSection section = provider.getChunkSection(pos.y);
		if (section == null) return;
		
		section.getBlockEntities().forEach(entity -> {
			BlockEntityRenderer customRenderer = BlockEntityRenderDispatcher.INSTANCE.getCustomRenderer(entity);
			if (customRenderer != null) {
				chunk.blockEntities.add(Pair.of(entity, customRenderer));
			}
		});
	}
	
	private static void renderChunk(Vec3I pos, ClientChunk chunk) {
		if (!chunk.visible) return;
		
		VBO vbo = chunk.data.get(layer);
		if (vbo.isEmpty()) return;
		
		if (!chunk.pos.equals(pos)) {
			chunk.pos.set(pos);
			chunk.needUpdate = true;
			chunk.markEmpty();
			return;
		}
		
		if (sort && layer == RenderLayer.TRANSLUCENT) {
			((IndexedVBO) vbo).sort(chunk.renderPos);
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef(
			chunk.renderPos.x - 8,
			chunk.renderPos.y - 8,
			chunk.renderPos.z - 8
		);
		vbo.render();
		GL11.glPopMatrix();
	}
	
	private static void renderBlockEntities(Vec3I pos, ClientChunk chunk) {
		if (!chunk.visible || chunk.blockEntities.isEmpty()) return;
		if (BlockEntityRenderDispatcher.INSTANCE.textureManager == null) return;
		
		chunk.blockEntities.forEach(pair -> {
			BlockEntityRenderer renderer = pair.second();
			BaseBlockEntity entity = pair.first();
			float light = level.getBrightness(entity.x, entity.y, entity.z);
			GL11.glColor3f(light, light, light);
			renderer.render(entity, entity.x - px, entity.y - py, entity.z - pz, delta);
			
			if (BreakInfo.stage == -1) return;
			if (BreakInfo.POS.x != entity.x || BreakInfo.POS.y != entity.y || BreakInfo.POS.z != entity.z) return;
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			
			GL11.glPolygonOffset(-3.0f, -3.0f);
			GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
			
			BlockBreakingInfo.cast(renderer).setBreaking(BreakInfo.stage);
			renderer.render(entity, entity.x - px, entity.y - py, entity.z - pz, delta);
			BlockBreakingInfo.cast(renderer).setBreaking(-1);
			
			GL11.glPolygonOffset(0.0f, 0.0f);
			GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
			
			GL11.glDisable(GL11.GL_BLEND);
		});
	}
	
	private static boolean needUpdate(Vec3I pos, ClientChunk chunk) {
		return chunk.needUpdate;
	}
	
	private static void checkVisibility(Vec3I pos, ClientChunk chunk) {
		chunk.renderPos.x = (float) ((pos.x << 4) - px + 8);
		chunk.renderPos.y = (float) ((pos.y << 4) - py + 8);
		chunk.renderPos.z = (float) ((pos.z << 4) - pz + 8);
		chunk.visible = !FRUSTUM_CULLING.isOutside(chunk.renderPos, 16);
	}
	
	private static void buildMeshes(BHBlockRenderer renderer) {
		if (level == null) return;
		
		Vec3I pos = UPDATE_REQUESTS.poll();
		if (pos == null) return;
		
		short sections = LevelHeightProvider.cast(level).getSectionsCount();
		if (pos.y < 0 || pos.y >= sections) return;
		if (!level.isBlockLoaded(pos.x << 4, 0, pos.z << 4)) return;
		
		renderer.setView(level);
		renderer.startArea(pos.x << 4, pos.y << 4, pos.z << 4);
		
		ChunkSectionProvider provider = ChunkSectionProvider.cast(level.getChunkFromCache(pos.x, pos.z));
		ChunkSection section = provider.getChunkSection(pos.y);
		
		ClientChunk chunk = chunks.get(pos);
		chunk.needUpdate = false;
		
		if (section == null) {
			chunk.markEmpty();
			return;
		}
		
		int wx = pos.x << 4;
		int wy = pos.y << 4;
		int wz = pos.z << 4;
		
		for (short i = 0; i < 4096; i++) {
			int x = i >> 8;
			int y = (i >> 4) & 15;
			int z = i & 15;
			BlockState state = section.getBlockState(x, y, z);
			renderer.render(state, x | wx, y | wy, z | wz);
		}
		
		for (RenderLayer layer: RenderLayer.VALUES) {
			VBO vbo = chunk.data.get(layer);
			boolean empty = renderer.isEmpty(layer);
			if (empty) {
				vbo.setEmpty();
				continue;
			}
			renderer.build(vbo, layer);
			vbo.markToUpdate();
			if (layer == RenderLayer.TRANSLUCENT) sort = true;
		}
	}
	
	private static class ClientChunk {
		final List<Pair<BaseBlockEntity, BlockEntityRenderer>> blockEntities;
		final EnumArray<RenderLayer, VBO> data;
		final Vec3F renderPos;
		final Vec3I pos;
		
		boolean needUpdate;
		boolean visible;
		
		ClientChunk() {
			blockEntities = new ArrayList<>();
			needUpdate = true;
			renderPos = new Vec3F();
			pos = new Vec3I(0, Integer.MIN_VALUE, 0);
			data = new EnumArray<>(RenderLayer.class);
			for (RenderLayer layer: RenderLayer.VALUES) {
				VBO vbo = layer == RenderLayer.TRANSLUCENT ? new IndexedVBO() : new VBO();
				data.set(layer, vbo);
			}
		}
		
		void markEmpty() {
			for (RenderLayer layer: RenderLayer.VALUES) {
				data.get(layer).setEmpty();
			}
		}
		
		void dispose() {
			for (RenderLayer layer: RenderLayer.VALUES) {
				data.get(layer).dispose();
			}
		}
	}
	
	public static int getChunkUpdates() {
		return UPDATE_REQUESTS.size();
	}
}
