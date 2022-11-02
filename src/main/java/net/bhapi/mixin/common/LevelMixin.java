package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.registry.DefaultRegistries;
import net.minecraft.block.BaseBlock;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.dimension.BaseDimension;
import net.minecraft.level.dimension.DimensionData;
import net.minecraft.level.gen.BiomeSource;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;
import net.minecraft.util.maths.MathHelper;
import net.minecraft.util.maths.Vec2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelHeightProvider, BlockStateProvider {
	@Shadow private Set loadedChunkPositions;
	@Shadow public List players;
	@Shadow private int caveSoundTicks;
	@Shadow protected int randomIndex;
	@Shadow public Random random;
	@Shadow protected int lightingTicks;
	
	@Shadow public abstract Chunk getChunkFromCache(int i, int j);
	@Shadow public abstract int getLightLevel(int i, int j, int k);
	@Shadow public abstract PlayerBase getClosestPlayer(double d, double e, double f, double g);
	@Shadow public abstract int getLight(LightType arg, int i, int j, int k);
	@Shadow public abstract void playSound(double d, double e, double f, String string, float g, float h);
	@Shadow public abstract boolean isRaining();
	@Shadow public abstract boolean isThundering();
	@Shadow public abstract int getHeightIterating(int i, int j);
	@Shadow public abstract boolean canRainAt(int i, int j, int k);
	@Shadow public abstract boolean addEntity(BaseEntity arg);
	@Shadow public abstract BiomeSource getBiomeSource();
	@Shadow public abstract boolean setBlock(int i, int j, int k, int l);
	
	@Shadow @Final public BaseDimension dimension;
	
	@Shadow @Final protected DimensionData dimensionData;
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;Lnet/minecraft/level/dimension/BaseDimension;J)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit1(DimensionData data, String name, BaseDimension dimension, long seed, CallbackInfo info) {
		bhapi_loadBlockStates();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/Level;Lnet/minecraft/level/dimension/BaseDimension;)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit2(Level level, BaseDimension dimension, CallbackInfo info) {
		bhapi_loadBlockStates();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;JLnet/minecraft/level/dimension/BaseDimension;)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit3(DimensionData data, String name, long seed, BaseDimension dimension, CallbackInfo info) {
		bhapi_loadBlockStates();
	}
	
	@Inject(method = "saveLevelData()V", at = @At("HEAD"))
	private void bhapi_onLevelSave(CallbackInfo ci) {
		CompoundTag tag = new CompoundTag();
		boolean requireSave = DefaultRegistries.BLOCKSTATES_MAP.save(tag);
		
		if (requireSave) {
			BHAPI.log("Saving registries");
			try {
				File file = dimensionData.getFile("registries");
				FileOutputStream stream = new FileOutputStream(file);
				NBTIO.writeGzipped(tag, stream);
				stream.close();
			}
			catch (IOException e) {
				BHAPI.warn(e.getMessage());
			}
		}
	}
	
	// TODO optimise this, map this, make readable
	@Inject(method = "processLoadedChunks", at = @At("HEAD"), cancellable = true)
	private void bhapi_processLoadedChunks(CallbackInfo info) {
		info.cancel();
		int px, index, chunkZ, chunkX;
		this.loadedChunkPositions.clear();
		
		for (int i = 0; i < this.players.size(); ++i) {
			PlayerBase player = (PlayerBase) this.players.get(i);
			chunkX = MathHelper.floor(player.x / 16.0);
			chunkZ = MathHelper.floor(player.z / 16.0);
			final int radius = 9;
			for (i = -radius; i <= radius; ++i) {
				for (px = -radius; px <= radius; ++px) {
					this.loadedChunkPositions.add(new Vec2i(i + chunkX, px + chunkZ));
				}
			}
		}
		
		if (this.caveSoundTicks > 0) {
			--this.caveSoundTicks;
		}
		
		for (Object object : this.loadedChunkPositions) {
			int blockID, py, pz;
			chunkX = ((Vec2i)object).x << 4;
			chunkZ = ((Vec2i)object).z << 4;
			Chunk chunk = this.getChunkFromCache(((Vec2i)object).x, ((Vec2i)object).z);
			
			if (this.caveSoundTicks == 0) {
				PlayerBase playerBase;
				this.randomIndex = this.randomIndex * 3 + 1013904223;
				index = this.randomIndex >> 2;
				px = index & 15;
				pz = index >> 8 & 15;
				py = index >> 16 & 127;
				blockID = chunk.getBlockId(px, py, pz);
				if (blockID == 0 && this.getLightLevel(px += chunkX, py, pz += chunkZ) <= this.random.nextInt(8) && this.getLight(LightType.SKY, px, py, pz) <= 0 && (playerBase = this.getClosestPlayer(px + 0.5, py + 0.5, pz + 0.5, 8.0)) != null && playerBase.squaredDistanceTo(px + 0.5, py + 0.5, pz + 0.5) > 4.0) {
					this.playSound(px + 0.5, py + 0.5, pz + 0.5, "ambient.cave.cave", 0.7f, 0.8f + this.random.nextFloat() * 0.2f);
					this.caveSoundTicks = this.random.nextInt(12000) + 6000;
				}
			}
			
			if (this.random.nextInt(100000) == 0 && this.isRaining() && this.isThundering()) {
				this.randomIndex = this.randomIndex * 3 + 1013904223;
				index = this.randomIndex >> 2;
				px = chunkX + (index & 0xF);
				pz = chunkZ + (index >> 8 & 0xF);
				py = this.getHeightIterating(px, pz);
				if (this.canRainAt(px, py, pz)) {
					this.addEntity(new LightningEntity(Level.class.cast(this), px, py, pz));
					this.lightingTicks = 2;
				}
			}
			
			if (this.random.nextInt(16) == 0) {
				this.randomIndex = this.randomIndex * 3 + 1013904223;
				index = this.randomIndex >> 2;
				px = index & 15;
				pz = index >> 8 & 15;
				py = this.getHeightIterating(px | chunkX, pz | chunkZ);
				if (this.getBiomeSource().getBiome(px | chunkX, pz | chunkZ).canSnow() && py >= 0 && py < 128 && chunk.getLight(LightType.BLOCK, px, py, pz) < 10) {
					blockID = chunk.getBlockId(px, py - 1, pz);
					int n9 = chunk.getBlockId(px, py, pz);
					if (this.isRaining() && n9 == 0 && BaseBlock.SNOW.canPlaceAt(Level.class.cast(this), px | chunkX, py, pz | chunkZ) && blockID != 0 && blockID != BaseBlock.ICE.id && BaseBlock.BY_ID[blockID].material.blocksMovement()) {
						this.setBlock(px | chunkX, py, pz | chunkZ, BaseBlock.SNOW.id);
					}
					if (blockID == BaseBlock.STILL_WATER.id && chunk.getMeta(px, py - 1, pz) == 0) {
						this.setBlock(px | chunkX, py - 1, pz | chunkZ, BaseBlock.ICE.id);
					}
				}
			}
			
			for (int k = 0; k < 80; ++k) {
				this.randomIndex = this.randomIndex * 3 + 1013904223;
				index = this.randomIndex >> 2;
				px = index & 15;
				pz = index >> 8 & 15;
				py = index >> 16 & 127;
				blockID = chunk.getBlockId(px, py, pz);
				if (!BaseBlock.TICKS_RANDOMLY[blockID]) continue;
				BaseBlock.BY_ID[blockID].onScheduledTick(Level.class.cast(this), px | chunkX, py, pz | chunkZ, this.random);
			}
		}
	}
	
	@Unique
	private void bhapi_loadBlockStates() {
		BHAPI.log("Loading registries");
		
		File file = this.dimensionData.getFile("registries");
		CompoundTag tag = null;
		
		if (file.exists()) {
			try {
				FileInputStream stream = new FileInputStream(file);
				tag = NBTIO.readGzipped(stream);
				stream.close();
			}
			catch (IOException e) {
				BHAPI.warn(e.getMessage());
			}
		}
		
		if (tag != null) {
			DefaultRegistries.BLOCKSTATES_MAP.load(tag);
		}
		
		if (tag == null) tag = new CompoundTag();
		DefaultRegistries.BLOCKSTATES_MAP.save(tag);
		
		try {
			FileOutputStream stream = new FileOutputStream(file);
			NBTIO.writeGzipped(tag, stream);
			stream.close();
		}
		catch (IOException e) {
			BHAPI.warn(e.getMessage());
		}
	}
	
	@ModifyConstant(method = {
		"getBlockId",
		"isBlockLoaded(III)Z",
		"isAreaLoaded(IIIIII)Z",
		"getLightLevel(III)I",
		"setBlockInChunk(IIII)Z",
		"setBlockInChunk(IIIII)Z",
		"getBlockMeta",
		"setMetaInChunk",
		"isAboveGround",
		"getLight(IIIZ)I",
		"getLight(Lnet/minecraft/level/LightType;III)I",
		"setLight",
		"addEntityWithChecks(Lnet/minecraft/entity/BaseEntity;Z)V"
	}, constant = @Constant(intValue = 128))
	private int bhapi_changeMaxHeight(int value) {
		return getLevelHeight();
	}
	
	@ModifyConstant(method = {
		"getLightLevel(III)I",
		"getLight(IIIZ)I",
		"getLight(Lnet/minecraft/level/LightType;III)I",
		"getHeightIterating"
	}, constant = @Constant(intValue = 127))
	private int bhapi_changeMaxBlockHeight(int value) {
		return getLevelHeight() - 1;
	}
	
	@ModifyConstant(
		method = "getHitResult(Lnet/minecraft/util/maths/Vec3f;Lnet/minecraft/util/maths/Vec3f;ZZ)Lnet/minecraft/util/hit/HitResult;",
		constant = @Constant(intValue = 200)
	)
	private int bhapi_changeMaxEntityCalcHeight(int value) {
		return getLevelHeight() + 64;
	}
	
	@Unique
	@Override
	public short getLevelHeight() {
		return LevelHeightProvider.cast(this.dimension).getLevelHeight();
	}
	
	@Unique
	@Override
	public boolean setBlockState(int x, int y, int z, BlockState state) {
		BlockStateProvider provider = BlockStateProvider.cast(this.getChunkFromCache(x >> 4, z >> 4));
		return provider.setBlockState(x & 15, y, z & 15, state);
	}
	
	@Unique
	@Override
	public BlockState getBlockState(int x, int y, int z) {
		BlockStateProvider provider = BlockStateProvider.cast(this.getChunkFromCache(x >> 4, z >> 4));
		return provider.getBlockState(x & 15, y, z & 15);
	}
}
