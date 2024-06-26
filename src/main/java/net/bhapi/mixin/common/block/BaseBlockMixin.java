package net.bhapi.mixin.common.block;

import net.bhapi.block.CustomDropProvider;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.vanilla.BlockMarker;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.ItemUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(Block.class)
public abstract class BaseBlockMixin implements BlockStateContainer, BHBlockRender {
	@Shadow protected abstract void drop(Level arg, int i, int j, int k, ItemStack arg2);
	@Shadow public abstract int getDropCount(Random random);
	@Shadow public abstract int getDropId(int i, Random random);
	@Shadow protected abstract int getDropMeta(int i);
	
	@Shadow @Final public int id;
	
	@Unique	private BlockState defaultState;
	
	// Allows to put anything into block ID field
	@ModifyVariable(method = "<init>(ILnet/minecraft/block/material/Material;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static int bhapi_setModBlockID(int id) {
		return BlockMarker.isVanillaInitiated() ? BlockUtil.MOD_BLOCK_ID : id;
	}
	
	// Reset block and all its values to default
	// Allows to register same block multiple times
	@Inject(method = "<init>(ILnet/minecraft/block/material/Material;)V", at = @At("TAIL"))
	private void bhapi_resetBlockEntries(int id, Material material, CallbackInfo info) {
		if (id == BlockUtil.MOD_BLOCK_ID) {
			Block.BY_ID[id] = null;
			Block.FULL_OPAQUE[id] = Block.FULL_OPAQUE[0];
			Block.LIGHT_OPACITY[id] = Block.LIGHT_OPACITY[0];
			Block.NO_AMBIENT_OCCLUSION[id] = Block.NO_AMBIENT_OCCLUSION[0];
			Block.HAS_BLOCK_ENTITY[id] = Block.HAS_BLOCK_ENTITY[0];
		}
	}
	
	@Environment(value= EnvType.CLIENT)
	@Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBrightness(BlockView blockView, int x, int y, int z, CallbackInfoReturnable<Float> info) {
		if (blockView == null) info.setReturnValue(1F);
		else {
			int light = BlockStateProvider.cast(blockView).bhapi_getBlockState(x, y, z).getEmittance();
			info.setReturnValue(blockView.getLight(x, y, z, light));
		}
	}
	
	@Unique
	@Override
	public BlockState bhapi_getDefaultState() {
		if (defaultState == null) {
			bhapi_setDefaultState(BlockState.getDefaultState(Block.class.cast(this)));
		}
		return defaultState;
	}
	
	@Unique
	@Override
	public void bhapi_setDefaultState(BlockState state) {
		defaultState = state;
	}
	
	@Inject(method = "canPlaceAt(Lnet/minecraft/level/Level;III)Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_canPlaceAt(Level level, int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(level).bhapi_getBlockState(x, y, z);
		info.setReturnValue(state.getBlock().material.isReplaceable() || state.is(Block.SNOW));
	}
	
	@Inject(method = "drop(Lnet/minecraft/level/Level;IIIIF)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_drop(Level level, int x, int y, int z, int meta, float f, CallbackInfo info) {
		info.cancel();
		if (level.isRemote) return;
		if (this instanceof CustomDropProvider) {
			List<ItemStack> drop = new ArrayList<>();
			CustomDropProvider.cast(this).getCustomDrop(level, x, y, z, drop);
			drop.forEach(stack -> this.drop(level, x, y, z, stack));
			return;
		}
		BlockState state = BlockUtil.brokenBlock;
		if (state == null) return;
		if (state.getBlock() == Block.class.cast(this)) {
			if (this.id == BlockUtil.MOD_BLOCK_ID) {
				ItemStack stack = ItemUtil.makeStack(bhapi_getDefaultState(), 1);
				if (stack.count > 0) this.drop(level, x, y, z, stack);
			}
			else {
				if (meta == 0) meta = state.getMeta();
				int dropID = this.getDropId(meta, level.random);
				int dropMeta = this.getDropMeta(meta);
				int count = this.getDropCount(level.random);
				ItemStack stack;
				if (dropID < 256) stack = ItemUtil.makeStack(BlockUtil.getLegacyBlock(dropID, dropMeta), count);
				else stack = new ItemStack(dropID, count, dropMeta);
				if (stack.count > 0) this.drop(level, x, y, z, stack);
			}
		}
	}
}
