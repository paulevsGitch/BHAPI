package net.bhapi.client.render.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.texture.RenderLayer;
import net.bhapi.level.ChunkSection;
import net.bhapi.level.ChunkSectionProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Vec3I;
import net.bhapi.storage.WorldCache;
import net.bhapi.util.BufferUtil;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

@Environment(EnvType.CLIENT)
public class ClientChunks {
	private static final EnumArray<RenderLayer, Integer> RENDER_DATA = new EnumArray<>(RenderLayer.class);
	private static final EnumArray<RenderLayer, Integer> INDEX_DATA = new EnumArray<>(RenderLayer.class);
	private static final EnumArray<RenderLayer, IntBuffer> LISTS = new EnumArray<>(RenderLayer.class);
	private static final BHBlockRenderer RENDERER = new BHBlockRenderer();
	private static WorldCache<ClientChunk> chunks;
	private static boolean update;
	private static int listSize;
	
	public static void init(int width, int height) {
		boolean fillChunks = false;
		if (chunks == null || chunks.getSizeXZ() != width || chunks.getSizeY() != height) {
			chunks = new WorldCache<>(
				width, height,
				ClientChunks::updateChunk,
				ClientChunks::renderChunk,
				ClientChunks::needUpdate
			);
			fillChunks = true;
		}
		
		int capacity = chunks.getCapacity();
		update = true;
		
		if (listSize != capacity) {
			if (listSize > 0) deleteLists();
			listSize = capacity;
			genLists();
		}
		
		if (fillChunks) {
			chunks.fill(ClientChunk::new);
		}
	}
	
	public static void render(LivingEntity entity, float delta) {
		if (chunks == null) return;
		chunks.setCenter(entity.chunkX, (int) entity.y >> 4, entity.chunkZ);
		chunks.process(1);
		
		GL11.glPushMatrix();
		double x = MathUtil.lerp(entity.prevX, entity.x, delta);
		double y = MathUtil.lerp(entity.prevY, entity.y, delta);
		double z = MathUtil.lerp(entity.prevZ, entity.z, delta);
		GL11.glTranslated(-x, -y, -z);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glCallLists(LISTS.get(RenderLayer.SOLID));
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glCallLists(LISTS.get(RenderLayer.TRANSPARENT));
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glCallLists(LISTS.get(RenderLayer.TRANSLUCENT));
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}
	
	private static void deleteLists() {
		for (RenderLayer layer: RenderLayer.VALUES) {
			int lists = RENDER_DATA.get(layer);
			GL11.glDeleteLists(lists, listSize);
		}
	}
	
	private static void genLists() {
		for (RenderLayer layer: RenderLayer.VALUES) {
			INDEX_DATA.set(layer, 0);
			RENDER_DATA.set(layer, GL11.glGenLists(listSize));
			LISTS.set(layer, BufferUtil.createIntBuffer(listSize));
		}
	}
	
	private static void updateChunk(Vec3I pos, ClientChunk chunk) {
		Minecraft mc = BHAPIClient.getMinecraft();
		short sections = LevelHeightProvider.cast(mc.level).getSectionsCount();
		if (pos.y < 0 || pos.y >= sections) return;
		
		//System.out.println("Updating " + pos);
		
		update = false;
		chunk.needUpdate = false;
		RENDERER.setView(mc.level);
		RENDERER.startArea(pos.x << 4, pos.y << 4, pos.z << 4);
		
		ChunkSectionProvider provider = ChunkSectionProvider.cast(mc.level.getChunkFromCache(pos.x, pos.z));
		ChunkSection section = provider.getChunkSection(pos.y);
		
		if (section == null) {
			for (RenderLayer layer: RenderLayer.VALUES) {
				int index = chunk.data.get(layer);
				GL11.glNewList(index, GL11.GL_COMPILE);
				GL11.glEndList();
			}
			return;
		}
		
		System.out.println("Building " + pos);
		
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
			int index = chunk.data.get(layer);
			GL11.glNewList(index, GL11.GL_COMPILE);
			GL11.glPushMatrix();
			GL11.glTranslatef(wx, wy, wz);
			tesselator.start();
			RENDERER.build(tesselator, layer);
			tesselator.draw();
			GL11.glPopMatrix();
			GL11.glEndList();
		}
	}
	
	private static void renderChunk(Vec3I pos, ClientChunk chunk) {}
	
	private static boolean needUpdate(Vec3I pos, ClientChunk chunk) {
		return update && chunk.needUpdate;
	}
	
	private static class ClientChunk {
		final EnumArray<RenderLayer, Integer> data;
		boolean needUpdate = true;
		
		ClientChunk() {
			data = new EnumArray<>(RenderLayer.class);
			for (RenderLayer layer: RenderLayer.VALUES) {
				int index = INDEX_DATA.get(layer);
				int list = RENDER_DATA.get(layer) + index;
				LISTS.get(layer).put(index, list);
				data.set(layer, list);
				INDEX_DATA.set(layer, ++index);
			}
		}
	}
}
