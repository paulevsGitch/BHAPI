package net.bhapi.level.updaters;

import net.bhapi.blockstate.BlockState;
import net.bhapi.config.BHConfigs;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.ChunkHeightProvider;
import net.bhapi.level.ChunkSection;
import net.bhapi.level.ChunkSectionProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.mixin.common.level.LevelAccessor;
import net.bhapi.storage.ExpandableCache;
import net.bhapi.storage.IncrementalPermutationTable;
import net.bhapi.storage.PermutationTable;
import net.bhapi.storage.Vec2I;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.UnsafeUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.technical.LightningEntity;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.biome.Biome;
import net.minecraft.level.biome.BiomeSource;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.util.maths.MCMath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevelChunkUpdater extends ThreadedUpdater {
	private final List<PlayerEntity> playersList = new ArrayList<>();
	private final ExpandableCache<Vec3I> cache3D = new ExpandableCache<>(Vec3I::new);
	private final ExpandableCache<Vec2I> cache2D = new ExpandableCache<>(Vec2I::new);
	private final Set<Vec3I> loadedSections = new HashSet<>();
	private final Set<Vec2I> loadedChunks = new HashSet<>();
	private final Biome[] biomes = new Biome[1];
	private final PermutationTable random = new IncrementalPermutationTable();
	private final BiomeSource biomeSource;
	private final int height;
	private int caveSoundTicks;
	
	private final int updatesVertical;
	private final int updatesHorizontal;
	
	public LevelChunkUpdater(Level level) {
		super("chunk_updater_", level);
		LevelHeightProvider heightProvider = LevelHeightProvider.cast(level);
		height = heightProvider.bhapi_getSectionsCount();
		caveSoundTicks = random.getInt(12000) + 6000;
		biomeSource = UnsafeUtil.copyObject(level.getBiomeSource());
		updatesVertical = BHConfigs.GENERAL.getInt("updates.verticalChunks", 8);
		updatesHorizontal = BHConfigs.GENERAL.getInt("updates.horizontalChunks", 8);
		BHConfigs.GENERAL.save();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void process() {
		synchronized (playersList) {
			playersList.clear();
			playersList.addAll(level.players);
		}
		super.process();
	}
	
	@Override
	protected void update() {
		cache3D.clear();
		cache2D.clear();
		loadedSections.clear();
		loadedChunks.clear();
		
		synchronized (playersList) {
			playersList.forEach(this::updatePlayer);
		}
		
		this.caveSoundTicks--;
		loadedSections.forEach(this::sectionTick);
		loadedChunks.forEach(this::tickChunk);
	}
	
	private void updatePlayer(PlayerEntity player) {
		int chunkX = MCMath.floor(player.x / 16.0);
		int chunkZ = MCMath.floor(player.z / 16.0);
		int sectionY = MCMath.floor(player.y / 16.0);
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
	}
	
	private void sectionTick(Vec3I pos) {
		Chunk chunk = level.getChunkFromCache(pos.x, pos.z);
		ChunkSection section = ChunkSectionProvider.cast(chunk).getChunkSection(pos.y);
		
		if (section != null) {
			int chunkX = pos.x << 4;
			int chunkY = pos.y << 4;
			int chunkZ = pos.z << 4;
			int px, py, pz;
			
			// Cave sounds in dark areas
			if (this.caveSoundTicks <= 0) {
				px = random.nextInt(16);
				py = random.nextInt(16);
				pz = random.nextInt(16);
				BlockState state = section.getBlockState(px, py, pz);
				if (state.isAir() && section.getMaxLight(px, py, pz) <= random.getInt(8)) {
					px |= chunkX;
					py |= chunkY;
					pz |= chunkZ;
					PlayerEntity playerBase = level.getClosestPlayer(px + 0.5, py + 0.5, pz + 0.5, 8.0);
					if (playerBase != null && playerBase.distanceToSqr(px + 0.5, py + 0.5, pz + 0.5) > 4.0) {
						level.playSound(px + 0.5, py + 0.5, pz + 0.5, "ambient.cave.cave", 0.7F, 0.8F + random.nextFloat() * 0.2F);
						this.caveSoundTicks = random.getInt(12000) + 6000;
					}
				}
			}
			
			// Convert water into ice
			if (biomeSource != null && random.getInt(16) == 0) {
				px = random.nextInt(16);
				pz = random.nextInt(16);
				py = random.nextInt(16);
				Biome[] biome = new Biome[1];
				biomeSource.getBiomes(biome, px | chunkX, pz | chunkZ, 1, 1);
				if (biome[0].canSnow() && section.getLight(LightType.BLOCK, px, py, pz) < 10) {
					BlockStateProvider provider = BlockStateProvider.cast(chunk);
					BlockState block = provider.bhapi_getBlockState(px, py - 1 + chunkY, pz);
					BlockState up = section.getBlockState(px, py, pz);
					if ((up.isAir() || !up.getMaterial().blocksMovement()) && block.is(Block.STILL_WATER) && block.getMeta() == 0) {
						BlockStateProvider levelProvider = BlockStateProvider.cast(level);
						levelProvider.bhapi_setBlockState(px | chunkX, py - 1, pz | chunkZ, BlockState.getDefaultState(Block.ICE));
					}
				}
			}
			
			// Random ticks, 10 per section, vanilla has 80 per chunk and chunk have 8 sections
			for (int k = 0; k < 10; ++k) {
				px = random.nextInt(16);
				py = random.nextInt(16);
				pz = random.nextInt(16);
				BlockState state = section.getBlockState(px, py, pz);
				if (state.hasRandomTicks()) {
					state.onScheduledTick(level, px | chunkX, py | chunkY, pz | chunkZ, level.random);
				}
			}
		}
	}
	
	private void tickChunk(Vec2I pos) {
		int chunkX = pos.x << 4;
		int chunkZ = pos.y << 4;
		Chunk chunk = level.getChunkFromCache(pos.x, pos.y);
		int px, py, pz;
		
		// Lighting during storm
		if (level.isRaining() && level.isThundering() && random.getInt(100000) == 0) {
			px = random.nextInt(16);
			pz = random.nextInt(16);
			py = ChunkHeightProvider.cast(chunk).getHeightmapData(px, pz);
			if (level.canRainAt(px, py, pz)) {
				level.addEntity(new LightningEntity(level, px, py, pz));
				((LevelAccessor) level).setLightingTicks(2);
			}
		}
		
		// Cover areas with snow during rain
		if (level.isRaining() && biomeSource != null && random.getInt(16) == 0) {
			px = random.nextInt(16);
			pz = random.nextInt(16);
			py = ChunkHeightProvider.cast(chunk).getHeightmapData(px, pz);
			biomeSource.getBiomes(biomes, px | chunkX, pz | chunkZ, 1, 1);
			if (biomes[0].canSnow() && chunk.getLight(LightType.BLOCK, px, py, pz) < 10) {
				BlockStateProvider provider = BlockStateProvider.cast(chunk);
				BlockState block = provider.bhapi_getBlockState(px, py - 1, pz);
				BlockState up = provider.bhapi_getBlockState(px, py, pz);
				if (up.isAir() && Block.SNOW.canPlaceAt(level, px | chunkX, py, pz | chunkZ) && !block.is(Block.ICE) && block.getBlock().material.blocksMovement()) {
					BlockStateProvider levelProvider = BlockStateProvider.cast(level);
					levelProvider.bhapi_setBlockState(px | chunkX, py, pz | chunkZ, BlockState.getDefaultState(Block.SNOW));
				}
			}
		}
	}
	
	public int getUpdatesVertical() {
		return updatesVertical;
	}
	
	public int getUpdatesHorizontal() {
		return updatesHorizontal;
	}
}
