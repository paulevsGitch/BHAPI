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
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

@Environment(EnvType.CLIENT)
public class ClientChunks {
	private static final Queue<Vec3I> UPDATE_QUEUE = new PriorityBlockingQueue<>(8192, (p1, p2) -> {
		int l1 = p1.distanceSqr(ClientChunks.CAMERA_POS);
		int l2 = p2.distanceSqr(ClientChunks.CAMERA_POS);
		return Integer.compare(l1, l2);
	});;//new ArrayBlockingQueue<>(8192);
	private static final Set<Vec3I> UPDATE_REQUESTS = new HashSet<>(8192);
	private static final FrustumCulling FRUSTUM_CULLING = new FrustumCulling();
	private static final Vec3I CAMERA_POS = new Vec3I(0, Integer.MIN_VALUE, 0);
	private static final Vec3I CENTER = new Vec3I();
	
	private static WorldCache<ClientChunk> chunks;
	private static RunnableThread[] buildingThreads;
	private static RenderLayer layer;
	private static double px, py, pz;
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
		
		sort = true;
		
		if (buildingThreads == null) {
			int count = BHConfigs.GENERAL.getInt("multithreading.meshBuildersCount", 8);
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
				chunks.forEach(ClientChunk::dispose);
			}
			chunks = new WorldCache<>(width, height, ClientChunk::new);
			if (level != null) {
				int deltaXZ = (width >> 1);
				int deltaY = (height >> 1);
				for (int x = -deltaXZ; x <= deltaXZ; x++) {
					for (int z = -deltaXZ; z <= deltaXZ; z++) {
						for (int y = -deltaY; y <= deltaY; y++) {
							UPDATE_REQUESTS.add(new Vec3I(x, y, z).add(CENTER));
						}
					}
				}
			}
		}
	}
	
	public static void onExit() {
		if (chunks != null) {
			chunks.forEach(ClientChunk::dispose);
		}
	}
	
	public static void update(Vec3I pos) {
		synchronized (UPDATE_REQUESTS) {
			UPDATE_REQUESTS.add(pos);
		}
	}
	
	public static void render(LivingEntity entity, float delta) {
		if (chunks == null) return;
		CENTER.set(entity.chunkX, (int) entity.y >> 4, entity.chunkZ);
		chunks.setCenter(CENTER.x, CENTER.y, CENTER.z);
		
		Level clientLevel = BHAPIClient.getMinecraft().level;
		if (clientLevel == null) {
			UPDATE_QUEUE.clear();
			UPDATE_REQUESTS.clear();
			level = null;
			return;
		}
		else if (clientLevel != level) {
			level = clientLevel;
			chunks.forEach(ClientChunk::markEmpty);
			return;
		}
		
		if (!level.dimension.noSkyLight) {
			int light = level.getEnvironmentLight(delta);
			if (oldLight != light) {
				oldLight = light;
				chunks.forEach((pos, chunk) -> UPDATE_REQUESTS.add(pos.clone()));
			}
		}
		
		if (UPDATE_QUEUE.size() == 0) {
			synchronized (UPDATE_REQUESTS) {
				UPDATE_REQUESTS.forEach(pos -> {
					if (chunks.isInside(pos)) {
						UPDATE_QUEUE.add(pos);
					}
				});
				UPDATE_REQUESTS.clear();
			}
		}
		
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
		if (ix != CAMERA_POS.x || iy != CAMERA_POS.y || iz != CAMERA_POS.z) {
			CAMERA_POS.set(ix, iy, iz);
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
	
	private static void renderChunk(Vec3I pos, ClientChunk chunk) {
		if (!chunk.visible) return;
		
		VBO vbo = chunk.data.get(layer);
		if (vbo.isEmpty()) return;
		
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
		if (!chunk.visible) return;
		List<Pair<BaseBlockEntity, BlockEntityRenderer>> blockEntities = chunk.blockEntities;
		if (blockEntities == null) return;
		if (BlockEntityRenderDispatcher.INSTANCE.textureManager == null) return;
		
		blockEntities.forEach(pair -> {
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
	
	private static void checkVisibility(Vec3I pos, ClientChunk chunk) {
		if (!pos.equals(chunk.pos)) {
			chunk.visible = false;
			return;
		}
		chunk.renderPos.x = (float) ((chunk.pos.x << 4) - px + 8);
		chunk.renderPos.y = (float) ((chunk.pos.y << 4) - py + 8);
		chunk.renderPos.z = (float) ((chunk.pos.z << 4) - pz + 8);
		chunk.visible = !FRUSTUM_CULLING.isOutside(chunk.renderPos, 16);
	}
	
	private static void buildMeshes(BHBlockRenderer renderer) {
		if (level == null) return;
		
		Vec3I pos = UPDATE_QUEUE.poll();
		if (pos == null) return;
		if (!chunks.isInside(pos)) return;
		
		short sections = LevelHeightProvider.cast(level).getSectionsCount();
		if (pos.y < 0 || pos.y >= sections) return;
		if (!level.isBlockLoaded(pos.x << 4, 0, pos.z << 4)) return;
		
		renderer.setView(level);
		renderer.startArea(pos.x << 4, pos.y << 4, pos.z << 4);
		
		ChunkSectionProvider provider = ChunkSectionProvider.cast(level.getChunkFromCache(pos.x, pos.z));
		ChunkSection section = provider.getChunkSection(pos.y);
		
		ClientChunk chunk = chunks.getOrCreate(pos);
		
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
			if (layer == RenderLayer.TRANSLUCENT) sort = true;
		}
		
		List<Pair<BaseBlockEntity, BlockEntityRenderer>> blockEntities = new ArrayList<>();
		section.getBlockEntities().forEach(entity -> {
			BlockEntityRenderer customRenderer = BlockEntityRenderDispatcher.INSTANCE.getCustomRenderer(entity);
			if (customRenderer != null) {
				blockEntities.add(Pair.of(entity, customRenderer));
			}
		});
		
		chunk.blockEntities = blockEntities.isEmpty() ? null : blockEntities;
		chunk.pos.set(pos);
	}
	
	private static class ClientChunk {
		List<Pair<BaseBlockEntity, BlockEntityRenderer>> blockEntities;
		final EnumArray<RenderLayer, VBO> data;
		final Vec3F renderPos;
		final Vec3I pos;
		boolean visible;
		
		ClientChunk() {
			renderPos = new Vec3F(0, Integer.MIN_VALUE, 0);
			pos = new Vec3I(0, Integer.MIN_VALUE, 0);
			data = new EnumArray<>(RenderLayer.class);
			data.set(RenderLayer.SOLID, new VBO());
			data.set(RenderLayer.TRANSPARENT, new VBO());
			data.set(RenderLayer.TRANSLUCENT, new IndexedVBO());
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
		return UPDATE_QUEUE.size();
	}
}
