package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateProvider;
import net.bhapi.registry.DefaultRegistries;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import net.minecraft.level.dimension.Dimension;
import net.minecraft.level.dimension.DimensionData;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;
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

@Mixin(Level.class)
public class LevelMixin implements BlockStateProvider {
	@Shadow @Final protected DimensionData dimensionData;
	
	@Override
	public boolean setBlockState(int x, int y, int z, BlockState state) {
		return false;
	}
	
	@Override
	public BlockState getBlockState(int x, int y, int z) {
		return null;
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;Lnet/minecraft/level/dimension/Dimension;J)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit1(DimensionData data, String name, Dimension dimension, long seed, CallbackInfo info) {
		loadBlockStates();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/Level;Lnet/minecraft/level/dimension/Dimension;)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit2(Level level, Dimension dimension, CallbackInfo info) {
		loadBlockStates();
	}
	
	@Inject(
		method = "<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;JLnet/minecraft/level/dimension/Dimension;)V",
		at = @At("TAIL")
	)
	private void bhapi_onWorldInit3(DimensionData data, String name, long seed, Dimension dimension, CallbackInfo info) {
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
