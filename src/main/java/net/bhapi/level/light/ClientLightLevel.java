package net.bhapi.level.light;

import net.bhapi.client.BHAPIClient;
import net.bhapi.level.ChunkSection;
import net.bhapi.level.ChunkSectionProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.storage.Vec3I;
import net.bhapi.storage.WorldCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.chunk.Chunk;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class ClientLightLevel {
	private static WorldCache<BHLightChunk> blockLight;
	private static final Vec3I POS = new Vec3I();
	
	public static void init() {
		int viewDistance = BHAPIClient.getMinecraft().options.viewDistance;
		int side = 64 << 3 - viewDistance;
		if (side > 512) side = 512;
		int sectionCountXZ = side >> 4 | 1;
		int sectionCountY = sectionCountXZ < 17 ? sectionCountXZ : side >> 5 | 1;
		init(sectionCountXZ, sectionCountY);
	}
	
	private static void init(int width, int height) {
		if (blockLight == null || blockLight.getSizeXZ() != width || blockLight.getSizeY() != height) {
			blockLight = new WorldCache<>(width, height, BHLightChunk::new);
		}
	}
	
	private static BHLightChunk getChunk(Level level, int x, int y, int z) {
		BHLightChunk chunk = blockLight.getOrCreate(POS.set(x >> 4, y >> 4, z >> 4));
		if (!chunk.isFilled()) {
			Chunk levelChunk = level.getChunkFromCache(POS.x, POS.z);
			ChunkSection section = ChunkSectionProvider.cast(levelChunk).getChunkSection(POS.y);
			if (section != null) section.fillLightInto(LightType.BLOCK, chunk);
			else Arrays.fill(chunk.getData(), (byte) 0);
			chunk.setFilled(true);
		}
		return chunk;
	}
	
	public static int getLight(int x, int y, int z) {
		Level level = BHAPIClient.getMinecraft().level;
		if (level == null) return 0;
		if (y < 0 || y >= LevelHeightProvider.cast(level).getLevelHeight()) return 0;
		BHLightChunk chunk = getChunk(level, x, y, z);
		return chunk.getLight(x & 15, y & 15, z & 15);
	}
	
	public static void setLight(int x, int y, int z, int light) {
		Level level = BHAPIClient.getMinecraft().level;
		if (level == null) return;
		if (y < 0 || y >= LevelHeightProvider.cast(level).getLevelHeight()) return;
		BHLightChunk chunk = getChunk(level, x, y, z);
		chunk.setLight(x & 15, y & 15, z & 15, light);
	}
	
	public static boolean fillSection(Vec3I pos) {
		Level level = BHAPIClient.getMinecraft().level;
		if (level == null) return false;
		if (pos.y < 0 || pos.y >= LevelHeightProvider.cast(level).getSectionsCount()) return false;
		BHLightChunk chunk = blockLight.get(pos);
		if (chunk == null || !chunk.isFilled()) return false;
		Chunk levelChunk = level.getChunkFromCache(pos.x, pos.z);
		ChunkSection section = ChunkSectionProvider.cast(levelChunk).getChunkSection(pos.y);
		if (section == null) return false;
		section.fillLightFrom(LightType.BLOCK, chunk);
		chunk.setFilled(false);
		return true;
	}
}
