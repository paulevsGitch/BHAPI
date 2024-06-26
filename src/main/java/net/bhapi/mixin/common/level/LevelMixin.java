package net.bhapi.mixin.common.level;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.config.BHConfigs;
import net.bhapi.level.BHTimeInfo;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.level.PlaceChecker;
import net.bhapi.level.light.BHLightArea;
import net.bhapi.level.updaters.LevelBlocksUpdater;
import net.bhapi.level.updaters.LevelChunkUpdater;
import net.bhapi.level.updaters.LevelLightUpdater;
import net.bhapi.level.updaters.LevelTicksUpdater;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.BlockUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.level.Level;
import net.minecraft.level.LevelProperties;
import net.minecraft.level.LightType;
import net.minecraft.level.LightUpdateArea;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.dimension.Dimension;
import net.minecraft.level.dimension.DimensionData;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;
import net.minecraft.util.maths.Box;
import net.minecraft.util.maths.MCMath;
import net.minecraft.util.maths.Vec3D;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(Level.class)
@SuppressWarnings("rawtypes")
public abstract class LevelMixin implements LevelHeightProvider, BlockStateProvider, PlaceChecker {
	@Shadow public Random random;
	@Shadow @Final public Dimension dimension;
	@Shadow @Final protected DimensionData dimensionData;
	@Shadow protected LevelProperties properties;
	@Shadow public boolean forceBlockUpdate;
	@Shadow private int lightUpdateTicks;
	@Shadow private List lightingUpdates;
	@Shadow static int areaUpdates;
	@Shadow private ArrayList collidingEntitySearchCache;
	@Shadow public boolean stopPhysics;
	@Shadow public boolean isRemote;
	@Shadow public List blockEntities;
	@Shadow public List entities;
	@Shadow private List entityToRemove;
	@Shadow private List invalidBlockEntities;
	@Shadow public List players;
	@Shadow public List entitiesList;
	
	@Shadow public abstract Chunk getChunkFromCache(int i, int j);
	@Shadow public abstract boolean isAreaLoaded(int i, int j, int k, int l, int m, int n);
	@Shadow public abstract void updateListenersLight(int i, int j, int k);
	@Shadow public abstract void updateAdjacentBlocks(int i, int j, int k, int l);
	@Shadow public abstract Chunk getChunk(int i, int j);
	@Shadow public abstract boolean isBlockLoaded(int i, int j, int k);
	@Shadow public abstract void updateArea(int i, int j, int k, int l, int m, int n);
	@Shadow public abstract int getBlockMeta(int i, int j, int k);
	@Shadow public abstract List getEntities(Entity arg, Box arg2);
	@Shadow public abstract boolean canSuffocate(int i, int j, int k);
	@Shadow public abstract boolean hasInderectPower(int i, int j, int k);
	@Shadow public abstract boolean isAboveGround(int i, int j, int k);
	@Shadow public abstract int getLight(LightType arg, int i, int j, int k);
	@Shadow public abstract void updateLight(LightType arg, int i, int j, int k, int l, int m, int n);
	
	@Unique private LevelBlocksUpdater bhapi_blocksUpdater;
	@Unique private LevelChunkUpdater bhapi_chunksUpdater;
	@Unique private LevelTicksUpdater bhapi_ticksUpdater;
	@Unique private LevelLightUpdater bhapi_lightUpdater;
	@Unique private final Vec3D bhapi_tempPos = Vec3D.make(0, 0, 0);
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;Lnet/minecraft/level/dimension/Dimension;J)V",
		at = @At("TAIL")
	)
	@Environment(value= EnvType.CLIENT)
	private void bhapi_onLevelInit1(DimensionData data, String name, Dimension dimension, long seed, CallbackInfo info) {
		bhapi_loadBlockStates();
		initUpdater();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/Level;Lnet/minecraft/level/dimension/Dimension;)V",
		at = @At("TAIL")
	)
	@Environment(value=EnvType.CLIENT)
	private void bhapi_onLevelInit2(Level level, Dimension dimension, CallbackInfo info) {
		bhapi_loadBlockStates();
		initUpdater();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;JLnet/minecraft/level/dimension/Dimension;)V",
		at = @At("TAIL")
	)
	private void bhapi_onLevelInit3(DimensionData data, String name, long seed, Dimension dimension, CallbackInfo info) {
		bhapi_loadBlockStates();
		initUpdater();
	}
	
	@Inject(method = "saveLevelData()V", at = @At("HEAD"))
	private void bhapi_onLevelSave(CallbackInfo ci) {
		CompoundTag tag = new CompoundTag();
		boolean requireSave = CommonRegistries.BLOCKSTATES_MAP.save(tag);
		
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
		bhapi_chunksUpdater.process();
		bhapi_blocksUpdater.process();
		bhapi_lightUpdater.process();
	}
	
	@SuppressWarnings("unchecked")
	@Inject(method = "scheduleTick", at = @At("HEAD"), cancellable = true)
	private void bhapi_scheduleTick(int x, int y, int z, int id, int time, CallbackInfo ci) {
		ci.cancel();
		
		final int uv = bhapi_chunksUpdater.getUpdatesVertical();
		final int uh = bhapi_chunksUpdater.getUpdatesHorizontal();
		
		BlockState state = bhapi_getBlockState(x, y, z);
		if (state.isAir()) return;
		BHTimeInfo info = new BHTimeInfo(x, y, z, state);
		
		if (!this.forceBlockUpdate) {
			info.setTime(time + this.properties.getTime());
		}
		
		if (this.isAreaLoaded(x - uh, y - uv, z - uh, x + uh, y + uv, z + uh)) {
			bhapi_ticksUpdater.addInfo(info);
		}
	}
	
	@Inject(method = "processBlockTicks", at = @At("HEAD"), cancellable = true)
	private void bhapi_processBlockTicks(boolean flag, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
		bhapi_ticksUpdater.setFlag(flag);
		bhapi_ticksUpdater.process();
	}
	
	@Inject(method = "updateLight()Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateLight1(CallbackInfoReturnable<Boolean> info) {
		Level level = Level.class.cast(this);
		info.setReturnValue(false);
		
		if (this.lightUpdateTicks >= 50) return;
		++this.lightUpdateTicks;
		int n = 500;
		
		synchronized (lightingUpdates) {
			try {
				while (this.lightingUpdates.size() > 0 && --n > 0) {
					LightUpdateArea area = (LightUpdateArea) this.lightingUpdates.remove(this.lightingUpdates.size() - 1);
					if (area != null) area.process(level);
				}
			}
			finally {
				--this.lightUpdateTicks;
			}
		}
	}
	
	@Inject(method = "updateLight(Lnet/minecraft/level/LightType;IIIIIIZ)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateLight2(LightType type, int x1, int y1, int z1, int x2, int y2, int z2, boolean flag, CallbackInfo info) {
		info.cancel();
		
		if (this.dimension.noSkyLight && type == LightType.SKY) return;
		
		int x = (x2 + x1) >> 1;
		int z = (z2 + z1) >> 1;
		if (!this.isBlockLoaded(x, 64, z)) return;
		if (this.getChunk(x, z).isClient()) return;
		
		if (type == LightType.BLOCK) {
			bhapi_lightUpdater.addArea(new BHLightArea(type, x1, y1, z1, x2, y2, z2));
			return;
		}
		
		if (areaUpdates == 50) return;
		
		synchronized (lightingUpdates) {
			++areaUpdates;
			try {
				int count;
				
				int lights = this.lightingUpdates.size();
				
				if (flag) {
					count = 5;
					if (count > lights) {
						count = lights;
					}
					for (int i2 = 0; i2 < count; ++i2) {
						LightUpdateArea lightUpdateArea = (LightUpdateArea) this.lightingUpdates.get(this.lightingUpdates.size() - i2 - 1);
						if (lightUpdateArea == null) continue;
						if (lightUpdateArea.lightType != type || !lightUpdateArea.checkAndUpdate(x1, y1, z1, x2, y2, z2)) continue;
						return;
					}
				}
				
				count = 1000000;
				this.lightingUpdates.add(new LightUpdateArea(type, x1, y1, z1, x2, y2, z2));
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
		short height = bhapi_getLevelHeight();
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
		short height = bhapi_getLevelHeight();
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
				this.updateArea((cx << 4) + px1, py1, (cz << 4) + pz1, (cx << 4) + x1 + px2, py2, (cz << 4) + pz2);
			}
		}
	}
	
	@Inject(
		method = "getHitResult(Lnet/minecraft/util/maths/Vec3D;Lnet/minecraft/util/maths/Vec3D;ZZ)Lnet/minecraft/util/hit/HitResult;",
		at = @At("HEAD"), cancellable = true
	)
	private void getHitResult(Vec3D pos, Vec3D pos2, boolean bl, boolean bl2, CallbackInfoReturnable<HitResult> info) {
		if (Double.isNaN(pos.x) || Double.isNaN(pos.y) || Double.isNaN(pos.z)) {
			info.setReturnValue(null);
			return;
		}
		
		if (Double.isNaN(pos2.x) || Double.isNaN(pos2.y) || Double.isNaN(pos2.z)) {
			info.setReturnValue(null);
			return;
		}
		
		int ix = MCMath.floor(pos2.x);
		int iy = MCMath.floor(pos2.y);
		int iz = MCMath.floor(pos2.z);
		int x = MCMath.floor(pos.x);
		int y = MCMath.floor(pos.y);
		int z = MCMath.floor(pos.z);
		
		Level level = Level.class.cast(this);
		BlockState state = bhapi_getBlockState(x, y, z);
		Block block = state.getBlock();
		int meta = this.getBlockMeta(x, y, z);
		
		HitResult hitResult;
		if ((!bl2 || state.isAir() || block.getCollisionShape(level, x, y, z) != null) && block.isSelectable(meta, bl) && (hitResult = block.getHitResult(level, x, y, z, pos, pos2)) != null) {
			info.setReturnValue(hitResult);
			return;
		}
		
		for (int n7 = 200; n7 >= 0; n7--) {
			if (Double.isNaN(pos.x) || Double.isNaN(pos.y) || Double.isNaN(pos.z)) {
				info.setReturnValue(null);
				return;
			}
			
			if (x == ix && y == iy && z == iz) {
				info.setReturnValue(null);
				return;
			}
			
			meta = 1;
			boolean bl3 = true;
			boolean bl4 = true;
			double d = 999.0;
			double d2 = 999.0;
			double d3 = 999.0;
			
			if (ix > x) {
				d = (double) x + 1.0;
			}
			else if (ix < x) {
				d = (double) x + 0.0;
			}
			else {
				meta = 0;
			}
			
			if (iy > y) {
				d2 = (double) y + 1.0;
			}
			else if (iy < y) {
				d2 = (double) y + 0.0;
			}
			else {
				bl3 = false;
			}
			
			if (iz > z) {
				d3 = (double) z + 1.0;
			}
			else if (iz < z) {
				d3 = (double) z + 0.0;
			}
			else {
				bl4 = false;
			}
			
			double d4 = 999.0;
			double d5 = 999.0;
			double d6 = 999.0;
			double d7 = pos2.x - pos.x;
			double d8 = pos2.y - pos.y;
			double d9 = pos2.z - pos.z;
			
			if (meta != 0) {
				d4 = (d - pos.x) / d7;
			}
			
			if (bl3) {
				d5 = (d2 - pos.y) / d8;
			}
			
			if (bl4) {
				d6 = (d3 - pos.z) / d9;
			}
			
			byte side;
			if (d4 < d5 && d4 < d6) {
				side = ix > x ? (byte) 4 : (byte) 5;
				pos.x = d;
				pos.y += d8 * d4;
				pos.z += d9 * d4;
			}
			else if (d5 < d6) {
				side = iy > y ? (byte) 0 : (byte) 1;
				pos.x += d7 * d5;
				pos.y = d2;
				pos.z += d9 * d5;
			}
			else {
				side = iz > z ? (byte) 2 : (byte) 3;
				pos.x += d7 * d6;
				pos.y += d8 * d6;
				pos.z = d3;
			}
			
			bhapi_tempPos.x = MCMath.floor(pos.x);
			x = (int) bhapi_tempPos.x;
			if (side == 5) {
				--x;
				bhapi_tempPos.x += 1.0;
			}
			
			bhapi_tempPos.y = MCMath.floor(pos.y);
			y = (int)bhapi_tempPos.y;
			if (side == 1) {
				--y;
				bhapi_tempPos.y += 1.0;
			}
			
			bhapi_tempPos.z = MCMath.floor(pos.z);
			z = (int)bhapi_tempPos.z;
			if (side == 3) {
				--z;
				bhapi_tempPos.z += 1.0;
			}
			
			int meta2 = this.getBlockMeta(x, y, z);
			
			state = bhapi_getBlockState(x, y, z);
			block = state.getBlock();
			if (bl2 && !state.isAir() && block.getCollisionShape(level, x, y, z) == null || !block.isSelectable(meta2, bl) || (hitResult = block.getHitResult(level, x, y, z, pos, pos2)) == null) continue;
			
			info.setReturnValue(hitResult);
			return;
		}
		
		info.setReturnValue(null);
	}
	
	@Inject(method = "canPlaceBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_canPlaceBlock(int id, int x, int y, int z, boolean flag, int facing, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockUtil.getLegacyBlock(id, 0);
		info.setReturnValue(canPlaceState(state, x, y, z, flag, facing));
	}
	
	@SuppressWarnings("all")
	@Inject(method = "getCollidingEntities", at = @At("HEAD"), cancellable = true)
	private void bhapi_getCollidingEntities(Entity entity, Box area, CallbackInfoReturnable<List> info) {
		this.collidingEntitySearchCache.clear();
		
		int x1 = MCMath.floor(area.minX);
		int x2 = MCMath.floor(area.maxX + 1.0);
		int y1 = MCMath.floor(area.minY);
		int y2 = MCMath.floor(area.maxY + 1.0);
		int z1 = MCMath.floor(area.minZ);
		int z2 = MCMath.floor(area.maxZ + 1.0);
		
		Level level = Level.class.cast(this);
		for (int x = x1; x < x2; ++x) {
			for (int z = z1; z < z2; ++z) {
				if (!this.isBlockLoaded(x, 64, z)) continue;
				for (int y = y1 - 1; y < y2; ++y) {
					Block baseBlock = bhapi_getBlockState(x, y, z).getBlock();
					if (!baseBlock.isSelectable()) continue;
					baseBlock.doesBoxCollide(level, x, y, z, area, this.collidingEntitySearchCache);
				}
			}
		}
		
		double d = 0.25;
		List list = this.getEntities(entity, area.expandNegative(d, d, d));
		for (int i = 0; i < list.size(); ++i) {
			Box box = ((Entity)list.get(i)).getBoundingBox();
			if (box != null && box.boxIntersects(area)) {
				this.collidingEntitySearchCache.add(box);
			}
			if ((box = entity.getBoundingBox((Entity)list.get(i))) == null || !box.boxIntersects(area)) continue;
			this.collidingEntitySearchCache.add(box);
		}
		
		info.setReturnValue(this.collidingEntitySearchCache);
	}
	
	@Unique
	@Override
	public boolean canPlaceState(BlockState state, int x, int y, int z, boolean flag, int facing) {
		Level level = Level.class.cast(this);
		BlockState levelState = bhapi_getBlockState(x, y, z);
		if (!levelState.isAir() && !levelState.getMaterial().isReplaceable()) return false;
		
		Block placeBlock = state.getBlock();
		Box collider = placeBlock.getCollisionShape(level, x, y, z);
		
		if (flag) {
			collider = null;
		}
		
		if (collider != null && !level.canSpawnEntity(collider)) return false;
		
		return placeBlock.canPlaceAt(level, x, y, z, facing);
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
			CommonRegistries.BLOCKSTATES_MAP.load(tag);
		}
		
		if (tag == null) tag = new CompoundTag();
		BHAPI.log("Saving registries");
		CommonRegistries.BLOCKSTATES_MAP.save(tag);
		
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
		if (bhapi_chunksUpdater == null) {
			Level level = Level.class.cast(this);
			bhapi_chunksUpdater = new LevelChunkUpdater(level);
			bhapi_ticksUpdater = new LevelTicksUpdater(level, this.properties);
			bhapi_blocksUpdater = new LevelBlocksUpdater(level);
			bhapi_lightUpdater = new LevelLightUpdater(level);
		}
		if (BHConfigs.GENERAL.getBool("multithreading.useThreads", true)) {
			this.blockEntities = Collections.synchronizedList(new ArrayList<BlockEntity>());
			this.entities = Collections.synchronizedList(new ArrayList<Entity>());
			this.entityToRemove = Collections.synchronizedList(new ArrayList<Entity>());
			this.invalidBlockEntities = Collections.synchronizedList(new ArrayList<BlockEntity>());
			this.players = Collections.synchronizedList(new ArrayList<PlayerEntity>());
			this.entitiesList = Collections.synchronizedList(new ArrayList<Entity>());
		}
	}
	
	@Inject(method = "getHeightIterating", at = @At("HEAD"), cancellable = true)
	private void bhapi_getHeightIterating(int x, int z, CallbackInfoReturnable<Integer> info) {
		BlockStateProvider provider = BlockStateProvider.cast(this.getChunk(x, z));
		x &= 15;
		z &= 15;
		for (int y = bhapi_getLevelHeight(); y > 0; --y) {
			Material material = provider.bhapi_getBlockState(x, y, z).getMaterial();
			if (!material.blocksMovement() && !material.isLiquid()) continue;
			info.setReturnValue(y + 1);
			return;
		}
		info.setReturnValue(-1);
	}
	
	@Inject(method = "getMaterial(III)Lnet/minecraft/block/material/Material;", at = @At("HEAD"), cancellable = true)
	private void bhapi_getMaterial(int x, int y, int z, CallbackInfoReturnable<Material> info) {
		BlockState state = bhapi_getBlockState(x, y, z);
		info.setReturnValue(state.getMaterial());
	}
	
	@Inject(method = "isFullOpaque", at = @At("HEAD"), cancellable = true)
	private void bhapi_isFullOpaque(int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		BlockState state = bhapi_getBlockState(x, y, z);
		info.setReturnValue(state.isFullOpaque());
	}
	
	@Inject(method = "canSuffocate", at = @At("HEAD"), cancellable = true)
	private void bhapi_canSuffocate(int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		BlockState state = bhapi_getBlockState(x, y, z);
		info.setReturnValue(state.getMaterial().hasNoSuffocation() && state.getBlock().isFullCube());
	}
	
	@Inject(method = "updateAdjacentBlocks", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateAdjacentBlocks(int x, int y, int z, int id, CallbackInfo info) {
		info.cancel();
		if (this.stopPhysics || this.isRemote) return;
		for (BlockDirection dir: BlockDirection.VALUES) {
			bhapi_blocksUpdater.add(new Vec3I(x, y, z).move(dir), dir.invert());
		}
	}
	
	@Inject(method = "hasRedstonePower", at = @At("HEAD"), cancellable = true)
	private void bhapi_hasRedstonePower(int x, int y, int z, int facing, CallbackInfoReturnable<Boolean> info) {
		if (this.canSuffocate(x, y, z)) {
			info.setReturnValue(this.hasInderectPower(x, y, z));
		}
		else {
			Level level = Level.class.cast(this);
			info.setReturnValue(bhapi_getBlockState(x, y, z).isPowered(level, x, y, z, BlockDirection.getFromFacing(facing)));
		}
	}
	
	@Inject(method = "updateLightIfNecessary", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateLightIfNecessary(LightType type, int x, int y, int z, int light, CallbackInfo info) {
		info.cancel();
		
		if (type != LightType.SKY) return;
		if (this.dimension.noSkyLight && type == LightType.SKY) return;
		if (!this.isBlockLoaded(x, y, z)) return;
		
		if (type == LightType.SKY) {
			if (this.isAboveGround(x, y, z)) light = 15;
		}
		else if (type == LightType.BLOCK) {
			BlockState state = bhapi_getBlockState(x, y, z);
			int blockLight = state.getEmittance();
			if (blockLight > light) {
				light = blockLight;
			}
		}
		
		if (this.getLight(type, x, y, z) != light) {
			this.updateLight(type, x, y, z, x, y, z);
		}
	}
	
	@Inject(method = "updateListenersLight", at = @At("HEAD"), cancellable = true)
	public void updateListenersLight(int x, int y, int z, CallbackInfo info) {
		info.cancel();
		bhapi_lightUpdater.addArea(new BHLightArea(LightType.BLOCK, x, y, z, x, y, z));
	}
	
	@ModifyConstant(method = {
		"getBlockID(III)I",
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
		"addEntityWithChecks(Lnet/minecraft/entity/Entity;Z)V"
	}, constant = @Constant(intValue = 128))
	private int bhapi_changeMaxHeight(int value) {
		return bhapi_getLevelHeight();
	}
	
	@ModifyConstant(method = {
		"getLightLevel(III)I",
		"getLight(IIIZ)I",
		"getLight(Lnet/minecraft/level/LightType;III)I"
	}, constant = @Constant(intValue = 127))
	private int bhapi_changeMaxBlockHeight(int value) {
		return bhapi_getLevelHeight() - 1;
	}
	
	@ModifyConstant(
		method = "getHitResult(Lnet/minecraft/util/maths/Vec3D;Lnet/minecraft/util/maths/Vec3D;ZZ)Lnet/minecraft/util/hit/HitResult;",
		constant = @Constant(intValue = 200)
	)
	private int bhapi_changeMaxEntityCalcHeight(int value) {
		return bhapi_getLevelHeight() + 64;
	}
	
	@Unique
	@Override
	public short bhapi_getLevelHeight() {
		return LevelHeightProvider.cast(this.dimension).bhapi_getLevelHeight();
	}
	
	@Unique
	@Override
	public boolean bhapi_setBlockState(int x, int y, int z, BlockState state, boolean update) {
		BlockStateProvider provider = BlockStateProvider.cast(this.getChunkFromCache(x >> 4, z >> 4));
		boolean result = provider.bhapi_setBlockState(x & 15, y, z & 15, state, update);
		if (update && result) {
			this.updateListenersLight(x, y, z);
			this.updateAdjacentBlocks(x, y, z, state.getBlock().id);
		}
		return result;
	}
	
	@Unique
	@Override
	public BlockState bhapi_getBlockState(int x, int y, int z) {
		if (y < 0 || y >= bhapi_getLevelHeight()) return BlockUtil.AIR_STATE;
		BlockStateProvider provider = BlockStateProvider.cast(this.getChunkFromCache(x >> 4, z >> 4));
		return provider.bhapi_getBlockState(x & 15, y, z & 15);
	}
}
