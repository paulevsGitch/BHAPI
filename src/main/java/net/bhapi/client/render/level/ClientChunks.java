package net.bhapi.client.render.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.VBO;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.texture.RenderLayer;
import net.bhapi.config.BHConfigs;
import net.bhapi.level.ChunkSection;
import net.bhapi.level.ChunkSectionProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Vec3I;
import net.bhapi.storage.WorldCache;
import net.bhapi.util.MathUtil;
import net.bhapi.util.ThreadManager;
import net.bhapi.util.ThreadManager.RunnableThread;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.level.Level;
import org.lwjgl.opengl.GL11;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@Environment(EnvType.CLIENT)
public class ClientChunks {
	private static final Queue<Vec3I> UPDATE_REQUESTS = new ArrayBlockingQueue<>(4096);
	
	private static WorldCache<ClientChunk> chunks;
	private static RunnableThread[] buildingThreads;
	private static RenderLayer layer;
	private static double px, py, pz;
	private static int oldLight;
	private static Level level;
	
	public static void init() {
		int viewDistance = BHAPIClient.getMinecraft().options.viewDistance;
		int side = 64 << 3 - viewDistance;
		if (side > 512) side = 512;
		int sectionCountXZ = side >> 4 | 1;
		int sectionCountY = sectionCountXZ < 17 ? sectionCountXZ : side >> 5 | 1;
		init(sectionCountXZ, sectionCountY);
		
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
	}
	
	private static void init(int width, int height) {
		if (chunks == null || chunks.getSizeXZ() != width || chunks.getSizeY() != height) {
			if (chunks != null) {
				UPDATE_REQUESTS.clear();
				chunks.forEach((pos, chunk) -> chunk.dispose());
			}
			chunks = new WorldCache<>(
				width, height,
				ClientChunks::updateChunk,
				ClientChunks::needUpdate
			);
			chunks.fill(ClientChunk::new);
		}
	}
	
	public static void update(Vec3I pos) {
		chunks.get(pos).needUpdate = true;
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
		chunks.update(4);
		
		px = MathUtil.lerp(entity.prevX, entity.x, delta);
		py = MathUtil.lerp(entity.prevY, entity.y, delta);
		pz = MathUtil.lerp(entity.prevZ, entity.z, delta);
		
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		layer = RenderLayer.SOLID;
		chunks.forEach(ClientChunks::renderChunk);
		
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		layer = RenderLayer.TRANSPARENT;
		chunks.forEach(ClientChunks::renderChunk);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		layer = RenderLayer.TRANSLUCENT;
		chunks.forEach(ClientChunks::renderChunk);
		
		GL11.glDisable(GL11.GL_BLEND);
		VBO.unbind();
	}
	
	private static void updateChunk(Vec3I pos, ClientChunk chunk) {
		if (UPDATE_REQUESTS.size() > 4095) return;
		chunk.needUpdate = false;
		UPDATE_REQUESTS.add(pos.clone());
	}
	
	private static void renderChunk(Vec3I pos, ClientChunk chunk) {
		VBO vbo = chunk.data.get(layer);
		if (vbo.isEmpty()) return;
		
		if (!chunk.pos.equals(pos)) {
			chunk.pos.set(pos);
			chunk.needUpdate = true;
			chunk.markEmpty();
			return;
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef(
			(float) ((chunk.pos.x << 4) - px),
			(float) ((chunk.pos.y << 4) - py),
			(float) ((chunk.pos.z << 4) - pz)
		);
		vbo.render();
		GL11.glPopMatrix();
	}
	
	private static boolean needUpdate(Vec3I pos, ClientChunk chunk) {
		return chunk.needUpdate;
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
			vbo.setEmpty();
			boolean empty = renderer.isEmpty(layer);
			if (empty) {
				vbo.setEmpty();
				continue;
			}
			renderer.build(vbo, layer);
			vbo.markToUpdate();
		}
	}
	
	private static class ClientChunk {
		final EnumArray<RenderLayer, VBO> data;
		final Vec3I pos;
		
		boolean needUpdate;
		
		ClientChunk() {
			needUpdate = true;
			pos = new Vec3I(0, Integer.MIN_VALUE, 0);
			data = new EnumArray<>(RenderLayer.class);
			for (RenderLayer layer: RenderLayer.VALUES) {
				data.set(layer, new VBO());
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
