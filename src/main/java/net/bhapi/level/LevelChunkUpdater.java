package net.bhapi.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.mixin.common.LevelAccessor;
import net.bhapi.storage.ExpandableCache;
import net.bhapi.storage.Vec2I;
import net.bhapi.storage.Vec3I;
import net.minecraft.block.BaseBlock;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.biome.BaseBiome;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.gen.BiomeSource;
import net.minecraft.util.maths.MathHelper;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class LevelChunkUpdater {
	private final ExpandableCache<Vec3I> cache3D = new ExpandableCache<>(Vec3I::new);
	private final ExpandableCache<Vec2I> cache2D = new ExpandableCache<>(Vec2I::new);
	private final Set<Vec3I> loadedSections = new HashSet<>();
	private final Set<Vec2I> loadedChunks = new HashSet<>();
	private final BaseBiome biomes[] = new BaseBiome[1];
	private final Random random = new Random();
	private final Level level;
	private final int height;
	private int caveSoundTicks;
	
	private ReentrantLock lock = new ReentrantLock();
	
	public LevelChunkUpdater(Level level) {
		this.level = level;
		LevelHeightProvider heightProvider = LevelHeightProvider.cast(level);
		height = heightProvider.getSectionsCount();
		caveSoundTicks = random.nextInt(12000) + 6000;
	}
	
	public void process() {
		final int updatesVertical = 8;
		final int updatesHorizontal = 8;
		
		cache3D.clear();
		cache2D.clear();
		loadedSections.clear();
		loadedChunks.clear();
		
		level.players.forEach(obj -> {
			PlayerBase player = (PlayerBase) obj;
			int chunkX = MathHelper.floor(player.x / 16.0);
			int chunkZ = MathHelper.floor(player.z / 16.0);
			int sectionY = MathHelper.floor(player.y / 16.0);
			int minY = sectionY - updatesVertical;
			int maxY = sectionY + updatesVertical;
			if (minY < 0) minY = 0;
			if (maxY > height) maxY = height;
			for (int dx = -updatesHorizontal; dx <= updatesHorizontal; ++dx) {
				int px = chunkX + dx;
				for (int dz = -updatesHorizontal; dz <= updatesHorizontal; ++dz) {
					int pz = chunkZ + dz;
					loadedChunks.add(cache2D.get().set(px, pz));
					for (int py = minY; py < maxY; ++py) {
						loadedSections.add(cache3D.get().set(px, py, pz));
					}
				}
			}
		});
		
		final BlockStateProvider levelProvider = BlockStateProvider.cast(level);
		
		this.caveSoundTicks--;
		loadedSections.forEach(pos -> {
			Chunk chunk = level.getChunkFromCache(pos.x, pos.z);
			ChunkSection section = ChunkSectionProvider.cast(chunk).getChunkSection(pos.y);
			
			if (section != null) {
				int chunkX = pos.x << 4;
				int chunkY = pos.y << 4;
				int chunkZ = pos.z << 4;
				int px, py, pz;
				
				// Cave sounds in dark areas
				if (this.caveSoundTicks <= 0) {
					px = random.nextInt() & 15;
					py = random.nextInt() & 15;
					pz = random.nextInt() & 15;
					BlockState state = section.getBlockState(px, py, pz);
					if (state.isAir() && section.getMaxLight(px, py, pz) <= random.nextInt(8)) {
						px |= chunkX;
						py |= chunkY;
						pz |= chunkZ;
						PlayerBase playerBase = level.getClosestPlayer(px + 0.5, py + 0.5, pz + 0.5, 8.0);
						if (playerBase != null && playerBase.squaredDistanceTo(px + 0.5, py + 0.5, pz + 0.5) > 4.0) {
							level.playSound(px + 0.5, py + 0.5, pz + 0.5, "ambient.cave.cave", 0.7F, 0.8F + random.nextFloat() * 0.2F);
							this.caveSoundTicks = random.nextInt(12000) + 6000;
						}
					}
				}
				
				// Convert water into ice
				if (random.nextInt(16) == 0) {
					px = random.nextInt() & 15;
					pz = random.nextInt() & 15;
					py = random.nextInt() & 15;
					if (level.getBiomeSource().getBiome(px | chunkX, pz | chunkZ).canSnow() && section.getLight(LightType.BLOCK, px, py, pz) < 10) {
						BlockStateProvider provider = BlockStateProvider.cast(chunk);
						BlockState block = provider.getBlockState(px, py - 1 + chunkY, pz);
						BlockState up = section.getBlockState(px, py, pz);
						if ((up.isAir() || !up.getMaterial().blocksMovement()) && block.is(BaseBlock.STILL_WATER) && (int) block.getValue(block.getProperty("meta")) == 0) {
							levelProvider.setBlockState(px | chunkX, py - 1, pz | chunkZ, BlockState.getDefaultState(BaseBlock.ICE));
						}
					}
				}
				
				// Random ticks, 10 per section, vanilla has 80 per chunk and chunk have 8 sections
				for (int k = 0; k < 10; ++k) {
					px = random.nextInt() & 15;
					py = random.nextInt() & 15;
					pz = random.nextInt() & 15;
					BlockState state = section.getBlockState(px, py, pz);
					if (state.hasRandomTicks()) {
						state.onScheduledTick(level, px | chunkX, py | chunkY, pz | chunkZ, random);
					}
				}
			}
		});
		
		loadedChunks.forEach(pos -> {
			int chunkX = pos.x << 4;
			int chunkZ = pos.y << 4;
			Chunk chunk = level.getChunkFromCache(pos.x, pos.y);
			int px, py, pz;
			
			// Lighting during storm
			if (level.isRaining() && level.isThundering() && random.nextInt(100000) == 0) {
				px = random.nextInt() & 15;
				pz = random.nextInt() & 15;
				py = level.getHeightIterating(px | chunkX, pz | chunkZ);
				if (level.canRainAt(px, py, pz)) {
					level.addEntity(new LightningEntity(level, px, py, pz));
					LevelAccessor.class.cast(level).setLightingTicks(2);
				}
			}
			
			// Cover areas with snow during rain
			if (random.nextInt(16) == 0) {
				px = random.nextInt() & 15;
				pz = random.nextInt() & 15;
				py = level.getHeightIterating(px | chunkX, pz | chunkZ);
				BiomeSource source = level.getBiomeSource();
				lock.lock();
				source.getBiomes(biomes, px | chunkX, pz | chunkZ, 1, 1);
				lock.unlock();
				if (biomes[0].canSnow() && chunk.getLight(LightType.BLOCK, px, py, pz) < 10) {
					BlockStateProvider provider = BlockStateProvider.cast(chunk);
					BlockState block = provider.getBlockState(px, py - 1, pz);
					BlockState up = provider.getBlockState(px, py, pz);
					if (level.isRaining() && up.isAir() && BaseBlock.SNOW.canPlaceAt(level, px | chunkX, py, pz | chunkZ) && !block.isAir() && !block.is(BaseBlock.ICE) && block.getBlock().material.blocksMovement()) {
						levelProvider.setBlockState(px | chunkX, py, pz | chunkZ, BlockState.getDefaultState(BaseBlock.SNOW));
					}
				}
			}
		});
	}
}
