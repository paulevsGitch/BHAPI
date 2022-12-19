package net.bhapi.client.render.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.texture.RenderLayer;
import net.bhapi.level.ChunkSection;
import net.bhapi.level.ChunkSectionProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.ExpandableCache;
import net.bhapi.storage.Vec3I;
import net.bhapi.storage.WorldCache;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.level.Level;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class ClientChunks {
	private static final ExpandableCache<Integer> RENDER_LISTS = new ExpandableCache<>(
		() -> GL11.glGenLists(RenderLayer.VALUES.length)
	);
	private static final BHBlockRenderer RENDERER = new BHBlockRenderer();
	private static WorldCache<ClientChunk> chunks;
	private static RenderLayer layer;
	private static boolean update;
	private static double px, py, pz;
	private static Level level;
	
	public static void init() {
		int viewDistance = BHAPIClient.getMinecraft().options.viewDistance;
		int side = 64 << 3 - viewDistance;
		if (side > 512) side = 512;
		int sectionCounXZ = side >> 4 | 1;
		int sectionCounY = sectionCounXZ < 17 ? sectionCounXZ : side >> 5 | 1;
		init(sectionCounXZ, sectionCounY);
	}
	
	private static void init(int width, int height) {
		update = false;
		level = null;
		if (chunks == null || chunks.getSizeXZ() != width || chunks.getSizeY() != height) {
			if (chunks != null) RENDER_LISTS.clear();
			chunks = new WorldCache<>(
				width, height,
				ClientChunks::updateChunk,
				ClientChunks::needUpdate
			);
			chunks.fill(ClientChunk::new);
		}
		update = true;
	}
	
	public static void update(Vec3I pos) {
		chunks.get(pos).needUpdate = true;
	}
	
	public static void render(LivingEntity entity, float delta) {
		if (chunks == null) return;
		
		Level clientLevel = BHAPIClient.getMinecraft().level;
		if (clientLevel != level) {
			level = clientLevel;
			chunks.forEach((pos, chunk) -> {
				chunk.needUpdate = true;
				for (RenderLayer layer: RenderLayer.VALUES) {
					chunk.empty.set(layer, true);
				}
			});
			return;
		}
		
		chunks.setCenter(entity.chunkX, (int) entity.y >> 4, entity.chunkZ);
		chunks.update(1);
		update = true;
		
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
	}
	
	private static void updateChunk(Vec3I pos, ClientChunk chunk) {
		Minecraft mc = BHAPIClient.getMinecraft();
		short sections = LevelHeightProvider.cast(mc.level).getSectionsCount();
		if (pos.y < 0 || pos.y >= sections) return;
		
		update = false;
		chunk.needUpdate = false;
		RENDERER.setView(mc.level);
		RENDERER.startArea(pos.x << 4, pos.y << 4, pos.z << 4);
		
		ChunkSectionProvider provider = ChunkSectionProvider.cast(mc.level.getChunkFromCache(pos.x, pos.z));
		ChunkSection section = provider.getChunkSection(pos.y);
		
		if (section == null) {
			for (RenderLayer layer: RenderLayer.VALUES) {
				chunk.empty.set(layer, true);
			}
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
			RENDERER.render(state, x | wx, y | wy, z | wz);
		}
		
		Tessellator tesselator = Tessellator.INSTANCE;
		for (RenderLayer layer: RenderLayer.VALUES) {
			boolean empty = RENDERER.isEmpty(layer);
			chunk.empty.set(layer, empty);
			if (empty) continue;
			
			int index = chunk.data.get(layer);
			GL11.glNewList(index, GL11.GL_COMPILE);
			tesselator.start();
			RENDERER.build(tesselator, layer);
			tesselator.draw();
			GL11.glEndList();
		}
		chunk.pos.set(pos);
	}
	
	private static void renderChunk(Vec3I pos, ClientChunk chunk) {
		if (chunk.empty.get(layer)) return;
		if (!chunk.pos.equals(pos)) return;
		
		GL11.glPushMatrix();
		GL11.glTranslatef(
			(float) ((pos.x << 4) - px),
			(float) ((pos.y << 4) - py),
			(float) ((pos.z << 4) - pz)
		);
		GL11.glCallList(chunk.data.get(layer));
		GL11.glPopMatrix();
	}
	
	private static boolean needUpdate(Vec3I pos, ClientChunk chunk) {
		return update && chunk.needUpdate;// && chunk.updateFrames == 0;
	}
	
	private static class ClientChunk {
		final EnumArray<RenderLayer, Boolean> empty;
		final EnumArray<RenderLayer, Integer> data;
		final Vec3I pos;
		
		boolean needUpdate;
		
		ClientChunk() {
			needUpdate = true;
			pos = new Vec3I(0, Integer.MIN_VALUE, 0);
			empty = new EnumArray<>(RenderLayer.class);
			data = new EnumArray<>(RenderLayer.class);
			int list = RENDER_LISTS.get();
			for (RenderLayer layer: RenderLayer.VALUES) {
				empty.set(layer, true);
				data.set(layer, list++);
			}
		}
	}
}
