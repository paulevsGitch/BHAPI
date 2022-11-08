package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelChunkUpdater;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.registry.DefaultRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.material.Material;
import net.minecraft.block.technical.TimeInfo;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.level.Level;
import net.minecraft.level.LevelProperties;
import net.minecraft.level.LightType;
import net.minecraft.level.LightUpdateArea;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.dimension.BaseDimension;
import net.minecraft.level.dimension.DimensionData;
import net.minecraft.level.gen.BiomeSource;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelHeightProvider, BlockStateProvider {
	@Shadow private Set loadedChunkPositions;
	@Shadow public List players;
	@Shadow private int caveSoundTicks;
	@Shadow protected int randomIndex;
	@Shadow public Random random;
	@Shadow protected int lightingTicks;
	@Shadow @Final public BaseDimension dimension;
	@Shadow @Final protected DimensionData dimensionData;
	@Shadow protected LevelProperties properties;
	@Shadow private Set tickNextTick;
	@Shadow private TreeSet treeSet;
	@Shadow public boolean forceBlockUpdate;
	
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
	@Shadow public abstract boolean isAreaLoaded(int i, int j, int k, int l, int m, int n);
	@Shadow public abstract void updateListenersLight(int i, int j, int k);
	@Shadow public abstract void updateAdjacentBlocks(int i, int j, int k, int l);
	
	@Shadow public abstract Chunk getChunk(int i, int j);
	
	@Shadow private int lightUpdateTicks;
	@Shadow private List lightingUpdates;
	@Shadow private static int areaUpdates;
	
	@Shadow public abstract boolean isBlockLoaded(int i, int j, int k);
	
	@Shadow public abstract void callAreaEvents(int i, int j, int k, int l, int m, int n);
	
	@Unique private LevelChunkUpdater bhapi_updater;
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;Lnet/minecraft/level/dimension/BaseDimension;J)V",
		at = @At("TAIL")
	)
	@Environment(value= EnvType.CLIENT)
	private void bhapi_onWorldInit1(DimensionData data, String name, BaseDimension dimension, long seed, CallbackInfo info) {
		bhapi_loadBlockStates();
		initUpdater();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/Level;Lnet/minecraft/level/dimension/BaseDimension;)V",
		at = @At("TAIL")
	)
	@Environment(value=EnvType.CLIENT)
	private void bhapi_onWorldInit2(Level level, BaseDimension dimension, CallbackInfo info) {
		bhapi_loadBlockStates();
		initUpdater();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;JLnet/minecraft/level/dimension/BaseDimension;)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit3(DimensionData data, String name, long seed, BaseDimension dimension, CallbackInfo info) {
		bhapi_loadBlockStates();
		initUpdater();
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
	
	@Inject(method = "processLoadedChunks", at = @At("HEAD"), cancellable = true)
	private void bhapi_processLoadedChunks(CallbackInfo info) {
		info.cancel();
		bhapi_updater.process();
	}
	
	@Inject(method = "scheduleTick", at = @At("HEAD"), cancellable = true)
	private void bhapi_scheduleTick(int x, int y, int z, int id, int m, CallbackInfo ci) {
		ci.cancel();
		
		// TODO Make configurable
		final int updatesVertical = 8;
		final int updatesHorizontal = 8;
		
		TimeInfo info = new TimeInfo(x, y, z, id);
		
		if (this.forceBlockUpdate) {
			if (this.isAreaLoaded(
				info.posX - updatesHorizontal,
				info.posY - updatesVertical,
				info.posZ - updatesHorizontal,
				info.posX + updatesHorizontal,
				info.posY + updatesVertical,
				info.posZ + updatesHorizontal)
			) {
				BlockState state = getBlockState(info.posX, info.posY, info.posZ);
				state.onScheduledTick(Level.class.cast(this), info.posX, info.posY, info.posZ, this.random);
			}
		}
		else if (this.isAreaLoaded(
			x - updatesHorizontal,
			y - updatesVertical,
			z - updatesHorizontal,
			x + updatesHorizontal,
			y + updatesVertical,
			z + updatesHorizontal)
		) {
			BlockState state = getBlockState(x, y, z);
			if (!state.isAir()) {
				info.setTime((long) m + this.properties.getTime());
			}
			if (!this.tickNextTick.contains(info)) {
				this.tickNextTick.add(info);
				this.treeSet.add(info);
			}
		}
	}
	
	@Inject(method = "processBlockTicks", at = @At("HEAD"), cancellable = true)
	private void bhapi_processBlockTicks(boolean flag, CallbackInfoReturnable<Boolean> cir) {
		int n = this.treeSet.size();
		if (n != this.tickNextTick.size()) {
			throw new IllegalStateException("TickNextTick list out of synch");
		}
		if (n > 1000) {
			n = 1000;
		}
		final int side = 8;
		for (int i = 0; i < n; ++i) {
			TimeInfo info = (TimeInfo) this.treeSet.first();
			if (!flag && info.time > this.properties.getTime()) break;
			this.treeSet.remove(info);
			this.tickNextTick.remove(info);
			if (!this.isAreaLoaded(info.posX - side, info.posY - side, info.posZ - side, info.posX + side, info.posY + side, info.posZ + side)) continue;
			BlockState state = getBlockState(info.posX, info.posY, info.posZ);
			state.onScheduledTick(Level.class.cast(this), info.posX, info.posY, info.posZ, this.random);
		}
		cir.setReturnValue(this.treeSet.size() != 0);
	}
	
	@Inject(method = "updateLight()Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateLight(CallbackInfoReturnable<Boolean> info) {
		Level level = Level.class.cast(this);
		
		synchronized (lightingUpdates) {
			if (this.lightUpdateTicks >= 50) {
				info.setReturnValue(false);
				return;
			}
			
			++this.lightUpdateTicks;
			try {
				int n = 500;
				
				while (this.lightingUpdates.size() > 0) {
					if (--n <= 0) {
						info.setReturnValue(true);
						return;
					}
					LightUpdateArea area = (LightUpdateArea) this.lightingUpdates.remove(this.lightingUpdates.size() - 1);
					if (area != null) area.process(level);
				}
				info.setReturnValue(false);
				return;
			}
			finally {
				--this.lightUpdateTicks;
			}
		}
	}
	
	@Inject(method = "updateLight(Lnet/minecraft/level/LightType;IIIIIIZ)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateLight(LightType type, int i, int j, int k, int l, int m, int n, boolean bl, CallbackInfo info) {
		info.cancel();
		
		if (this.dimension.noSkyLight && type == LightType.SKY) {
			return;
		}
		
		synchronized (lightingUpdates) {
			++this.areaUpdates;
			try {
				int count;
				
				if (areaUpdates == 50) {
					return;
				}
				
				int x = (l + i) / 2;
				int z = (n + k) / 2;
				
				if (!this.isBlockLoaded(x, 64, z)) {
					return;
				}
				
				if (this.getChunk(x, z).isClient()) {
					return;
				}
				
				int lights = this.lightingUpdates.size();
				
				if (bl) {
					count = 5;
					if (count > lights) {
						count = lights;
					}
					for (int i2 = 0; i2 < count; ++i2) {
						LightUpdateArea lightUpdateArea = (LightUpdateArea) this.lightingUpdates.get(this.lightingUpdates.size() - i2 - 1);
						if (lightUpdateArea == null) continue;
						if (lightUpdateArea.lightType != type || !lightUpdateArea.checkAndUpdate(i, j, k, l, m, n)) continue;
						return;
					}
				}
				
				count = 1000000;
				this.lightingUpdates.add(new LightUpdateArea(type, i, j, k, l, m, n));
				if (this.lightingUpdates.size() > count) {
					BHAPI.warn("More than " + count + " updates, aborting lighting updates");
					this.lightingUpdates.clear();
				}
			}
			finally {
				--areaUpdates;
			}
		}
	}
	
	@Environment(value=EnvType.SERVER)
	@Inject(method = "getServerBlockData", at = @At("HEAD"), cancellable = true)
	private void bhapi_getServerBlockData(int x1, int y1, int z1, int dx, int dy, int dz, CallbackInfoReturnable<byte[]> info) {
		byte[] data = new byte[dx * dy * dz * 5];
		
		int cx1 = x1 >> 4;
		int cz1 = z1 >> 4;
		int cx2 = x1 + dx - 1 >> 4;
		int cz2 = z1 + dz - 1 >> 4;
		int index = 0;
		int py1 = y1;
		int py2 = y1 + dy;
		
		if (py1 < 0) py1 = 0;
		short height = getLevelHeight();
		if (py2 > height) py2 = height;
		
		for (int cx = cx1; cx <= cx2; ++cx) {
			int px1 = x1 - (cx << 4);
			int px2 = x1 + dx - (cx << 4);
			if (px1 < 0) px1 = 0;
			if (px2 > 16) px2 = 16;
			for (int cz = cz1; cz <= cz2; ++cz) {
				int pz1 = z1 - (cz << 4);
				int pz2 = z1 + dz - (cz << 4);
				if (pz1 < 0) {
					pz1 = 0;
				}
				if (pz2 > 16) {
					pz2 = 16;
				}
				index = this.getChunkFromCache(cx, cz).getServerBlockData(data, px1, py1, pz1, px2, py2, pz2, index);
			}
		}
		info.setReturnValue(data);
	}
	
	@Environment(value=EnvType.CLIENT)
	@Inject(method = "setClientBlockData", at = @At("HEAD"), cancellable = true)
	private void bhapi_setClientBlockData(int x1, int y1, int z1, int dx, int dy, int dz, byte[] data, CallbackInfo info) {
		info.cancel();
		
		int cx1 = x1 >> 4;
		int cz1 = z1 >> 4;
		int cx2 = x1 + dx - 1 >> 4;
		int cz2 = z1 + dz - 1 >> 4;
		int index = 0;
		int py1 = y1;
		int py2 = y1 + dy;
		
		if (py1 < 0) py1 = 0;
		short height = getLevelHeight();
		if (py2 > height) py2 = height;
		
		for (int cx = cx1; cx <= cx2; ++cx) {
			int px1 = x1 - (cx << 4);
			int px2 = x1 + dx - (cx << 4);
			if (px1 < 0) px1 = 0;
			if (px2 > 16) px2 = 16;
			for (int cz = cz1; cz <= cz2; ++cz) {
				int pz1 = z1 - (cz << 4);
				int pz2 = z1 + dz - (cz << 4);
				if (pz1 < 0) {
					pz1 = 0;
				}
				if (pz2 > 16) {
					pz2 = 16;
				}
				index = this.getChunkFromCache(cx, cz).setClientBlockData(data, px1, py1, pz1, px2, py2, pz2, index);
				this.callAreaEvents((cx << 4) + px1, py1, (cz << 4) + pz1, (cx << 4) + x1 + px2, py2, (cz << 4) + pz2);
			}
		}
	}
	
	@Unique
	private void bhapi_loadBlockStates() {
		BHAPI.log("Loading registries");
		
		File file = this.dimensionData.getFile("registries");
		if (file == null) return; // When player connects to server
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
		BHAPI.log("Saving registries");
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
	
	@Unique
	private void initUpdater() {
		if (bhapi_updater == null) {
			bhapi_updater = new LevelChunkUpdater(Level.class.cast(this));
		}
	}
	
	@Inject(method = "getHeightIterating", at = @At("HEAD"), cancellable = true)
	private void bhapi_getHeightIterating(int x, int z, CallbackInfoReturnable<Integer> info) {
		BlockStateProvider provider = BlockStateProvider.cast(this.getChunk(x, z));
		x &= 15;
		z &= 15;
		for (int y = getLevelHeight(); y > 0; --y) {
			Material material = provider.getBlockState(x, y, z).getMaterial();
			if (!material.blocksMovement() && !material.isLiquid()) continue;
			info.setReturnValue(y + 1);
			return;
		}
		info.setReturnValue(-1);
	}
	
	@ModifyConstant(method = {
		"getBlockId(III)I",
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
		"getLight(Lnet/minecraft/level/LightType;III)I"
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
	public boolean setBlockState(int x, int y, int z, BlockState state, boolean update) {
		BlockStateProvider provider = BlockStateProvider.cast(this.getChunkFromCache(x >> 4, z >> 4));
		boolean result = provider.setBlockState(x & 15, y, z & 15, state, update);
		if (update && result) {
			this.updateListenersLight(x, y, z);
			this.updateAdjacentBlocks(x, y, z, state.getBlock().id);
		}
		return result;
	}
	
	@Unique
	@Override
	public BlockState getBlockState(int x, int y, int z) {
		BlockStateProvider provider = BlockStateProvider.cast(this.getChunkFromCache(x >> 4, z >> 4));
		return provider.getBlockState(x & 15, y, z & 15);
	}
}
