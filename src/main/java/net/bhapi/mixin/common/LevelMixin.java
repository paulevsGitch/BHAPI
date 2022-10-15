package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateProvider;
import net.bhapi.registry.DefaultRegistries;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.Lightning;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mixin(Level.class)
public abstract class LevelMixin implements BlockStateProvider {
	@Shadow @Final protected DimensionData dimensionData;
	
	@Shadow private Set field_194;
	
	@Shadow public List players;
	
	@Shadow private int field_195;
	
	@Shadow public abstract Chunk getChunkFromCache(int i, int j);
	
	@Shadow protected int field_203;
	
	@Shadow public abstract int getLightLevel(int i, int j, int k);
	
	@Shadow public Random rand;
	
	@Shadow public abstract PlayerBase getClosestPlayer(double d, double e, double f, double g);
	
	@Shadow public abstract int getLight(LightType arg, int i, int j, int k);
	
	@Shadow public abstract void playSound(double d, double e, double f, String string, float g, float h);
	
	@Shadow public abstract boolean isRaining();
	
	@Shadow public abstract boolean isThundering();
	
	@Shadow public abstract int getHeightIterating(int i, int j);
	
	@Shadow public abstract boolean canRainAt(int i, int j, int k);
	
	@Shadow public abstract boolean method_184(BaseEntity arg);
	
	@Shadow protected int field_209;
	
	@Shadow public abstract BiomeSource getBiomeSource();
	
	@Shadow public abstract boolean setBlock(int i, int j, int k, int l);
	
	@Override
	public boolean setBlockState(int x, int y, int z, BlockState state) {
		return false;
	}
	
	@Override
	public BlockState getBlockState(int x, int y, int z) {
		return null;
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;Lnet/minecraft/level/dimension/BaseDimension;J)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit1(DimensionData data, String name, BaseDimension dimension, long seed, CallbackInfo info) {
		loadBlockStates();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/Level;Lnet/minecraft/level/dimension/BaseDimension;)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit2(Level level, BaseDimension dimension, CallbackInfo info) {
		loadBlockStates();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;JLnet/minecraft/level/dimension/BaseDimension;)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit3(DimensionData data, String name, long seed, BaseDimension dimension, CallbackInfo info) {
		loadBlockStates();
	}
	
	@Inject(method = "getMaterial(III)Lnet/minecraft/block/material/Material;", at = @At("HEAD"), cancellable = true)
	private void getMaterial(int x, int y, int z, CallbackInfoReturnable<Material> info) {
		BlockState state = getBlockState(x, y, z);
		info.setReturnValue(state == null ? Material.AIR : state.getBlock().material);
	}
	
	@Inject(method = "saveLevelData()V", at = @At("HEAD"))
	private void bhapi_onLevelSave(CallbackInfo ci) {
		BHAPI.log("Saving registries");
		CompoundTag tag = new CompoundTag();
		
		boolean requireSave = true;
		requireSave &= DefaultRegistries.BLOCKSTATES_MAP.save(tag);
		
		if (requireSave) {
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
	@Inject(method = "method_248", at = @At("HEAD"), cancellable = true)
	private void bhapi_method_248(CallbackInfo info) {
		info.cancel();
		int px;
		int j;
		int chunkZ;
		int chunkX;
		this.field_194.clear();
		
		for (int i = 0; i < this.players.size(); ++i) {
			PlayerBase player = (PlayerBase) this.players.get(i);
			chunkX = MathHelper.floor(player.x / 16.0);
			chunkZ = MathHelper.floor(player.z / 16.0);
			final int radius = 9;
			for (i = -radius; i <= radius; ++i) {
				for (px = -radius; px <= radius; ++px) {
					this.field_194.add(new Vec2i(i + chunkX, px + chunkZ));
				}
			}
		}
		
		if (this.field_195 > 0) {
			--this.field_195;
		}
		
		for (Object object : this.field_194) {
			int n6;
			int py;
			int pz;
			chunkX = ((Vec2i)object).x * 16;
			chunkZ = ((Vec2i)object).z * 16;
			Chunk chunk = this.getChunkFromCache(((Vec2i)object).x, ((Vec2i)object).z);
			
			if (this.field_195 == 0) {
				PlayerBase playerBase;
				this.field_203 = this.field_203 * 3 + 1013904223;
				j = this.field_203 >> 2;
				px = j & 0xF;
				pz = j >> 8 & 0xF;
				py = j >> 16 & 0x7F;
				n6 = chunk.getBlockId(px, py, pz);
				if (n6 == 0 && this.getLightLevel(px += chunkX, py, pz += chunkZ) <= this.rand.nextInt(8) && this.getLight(LightType.SKY, px, py, pz) <= 0 && (playerBase = this.getClosestPlayer(px + 0.5, py + 0.5, pz + 0.5, 8.0)) != null && playerBase.squaredDistanceTo(px + 0.5, py + 0.5, pz + 0.5) > 4.0) {
					this.playSound((double)px + 0.5, (double)py + 0.5, (double)pz + 0.5, "ambient.cave.cave", 0.7f, 0.8f + this.rand.nextFloat() * 0.2f);
					this.field_195 = this.rand.nextInt(12000) + 6000;
				}
			}
			
			if (this.rand.nextInt(100000) == 0 && this.isRaining() && this.isThundering()) {
				this.field_203 = this.field_203 * 3 + 1013904223;
				j = this.field_203 >> 2;
				px = chunkX + (j & 0xF);
				pz = chunkZ + (j >> 8 & 0xF);
				py = this.getHeightIterating(px, pz);
				if (this.canRainAt(px, py, pz)) {
					this.method_184(new Lightning(Level.class.cast(this), px, py, pz));
					this.field_209 = 2;
				}
			}
			
			if (this.rand.nextInt(16) == 0) {
				this.field_203 = this.field_203 * 3 + 1013904223;
				j = this.field_203 >> 2;
				px = j & 0xF;
				pz = j >> 8 & 0xF;
				py = this.getHeightIterating(px + chunkX, pz + chunkZ);
				if (this.getBiomeSource().getBiome(px + chunkX, pz + chunkZ).canSnow() && py >= 0 && py < 128 && chunk.getLight(LightType.BLOCK, px, py, pz) < 10) {
					n6 = chunk.getBlockId(px, py - 1, pz);
					int n9 = chunk.getBlockId(px, py, pz);
					if (this.isRaining() && n9 == 0 && BaseBlock.SNOW.canPlaceAt(Level.class.cast(this), px + chunkX, py, pz + chunkZ) && n6 != 0 && n6 != BaseBlock.ICE.id && BaseBlock.BY_ID[n6].material.blocksMovement()) {
						this.setBlock(px + chunkX, py, pz + chunkZ, BaseBlock.SNOW.id);
					}
					if (n6 == BaseBlock.STILL_WATER.id && chunk.getMeta(px, py - 1, pz) == 0) {
						this.setBlock(px + chunkX, py - 1, pz + chunkZ, BaseBlock.ICE.id);
					}
				}
			}
			
			for (j = 0; j < 80; ++j) {
				this.field_203 = this.field_203 * 3 + 1013904223;
				int index = this.field_203 >> 2;
				px = index & 0xF;
				pz = index >> 8 & 0xF;
				py = index >> 16 & 0x7F;
				int blockID = chunk.getBlockId(px, py, pz);
				if (!BaseBlock.TICKS_RANDOMLY[blockID]) continue;
				BaseBlock.BY_ID[blockID].onScheduledTick(Level.class.cast(this), px + chunkX, py, pz + chunkZ, this.rand);
			}
		}
	}
	
	@Unique
	private void loadBlockStates() {
		BHAPI.log("Loading registries");
		
		CompoundTag tag = null;
		try {
			File file = dimensionData.getFile("registries");
			if (!file.exists()) return;
			FileInputStream stream = new FileInputStream(file);
			tag = NBTIO.readGzipped(stream);
			stream.close();
		}
		catch (IOException e) {
			BHAPI.warn(e.getMessage());
		}
		if (tag != null) {
			DefaultRegistries.BLOCKSTATES_MAP.load(tag);
		}
	}
}
