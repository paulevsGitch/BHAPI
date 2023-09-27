package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.util.BlockUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.MovingPistonBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.technical.PistonDataValues;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PistonBlock.class)
public abstract class PistonBlockMixin extends BaseBlock implements BlockStateContainer, BHBlockRender {
	@Shadow private boolean updateBlock;
	@Shadow private boolean actionByRotation;
	
	@Shadow protected abstract boolean pushByPiston(Level arg, int i, int j, int k, int l);
	
	@Unique private static Level bhapi_level;
	
	protected PistonBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@ModifyConstant(method = {
		"canMoveBlock(Lnet/minecraft/level/Level;IIII)Z"
	}, constant = @Constant(intValue = 127))
	private static int bhapi_changeMaxBlockHeight(int value) {
		return getLevelHeight() - 1;
	}
	
	@Unique
	private static short getLevelHeight() {
		return LevelHeightProvider.cast(bhapi_level).getLevelHeight();
	}
	
	@Inject(method = "onBlockPlaced", at = @At("HEAD"), cancellable = true)
	private void bhapi_onBlockPlaced(Level level, int x, int y, int z, CallbackInfo info) {
		info.cancel();
	}
	
	@Inject(method = "onBlockAction", at = @At("HEAD"), cancellable = true)
	private void bhapi_onBlockAction(Level level, int x, int y, int z, int flag, int meta, CallbackInfo info) {
		info.cancel();
		this.updateBlock = true;
		if (flag == 0) {
			if (this.pushByPiston(level, x, y, z, meta)) {
				level.setBlockMeta(x, y, z, meta | 8);
				level.playSound(x + 0.5, y + 0.5, z + 0.5, "tile.piston.out", 0.5f, level.random.nextFloat() * 0.25f + 0.6f);
			}
		}
		else if (flag == 1) {
			BaseBlockEntity entity = level.getBlockEntity(x + PistonDataValues.OFFSET_X[meta], y + PistonDataValues.OFFSET_Y[meta], z + PistonDataValues.OFFSET_Z[meta]);
			if (entity instanceof PistonBlockEntity) {
				((PistonBlockEntity) entity).resetBlock();
			}
			BlockStateProvider provider = BlockStateProvider.cast(level);
			provider.setBlockState(x, y, z, BlockState.getDefaultState(BaseBlock.MOVING_PISTON).withMeta(meta));
			PistonBlockEntity pistonEntity = (PistonBlockEntity) MovingPistonBlock.createEntity(this.id, meta, meta, false, true);
			BlockStateContainer.cast(pistonEntity).setDefaultState(BlockUtil.getLegacyBlock(this.id, meta));
			level.setBlockEntity(x, y, z, pistonEntity);
			if (this.actionByRotation) {
				BaseBlockEntity entity2;
				int x2 = x + PistonDataValues.OFFSET_X[meta] * 2;
				int y2 = y + PistonDataValues.OFFSET_Y[meta] * 2;
				int z2 = z + PistonDataValues.OFFSET_Z[meta] * 2;
				BlockState state = provider.getBlockState(x2, y2, z2);
				boolean skipUpdate = false;
				if (state.is(BaseBlock.MOVING_PISTON) && (entity2 = level.getBlockEntity(x2, y2, z2)) != null && entity2 instanceof PistonBlockEntity && (pistonEntity = (PistonBlockEntity)entity2).getFacing() == meta && pistonEntity.isExtending()) {
					pistonEntity.resetBlock();
					state = BlockStateContainer.cast(pistonEntity).getDefaultState();
					if (state == null) state = BlockUtil.AIR_STATE;
					skipUpdate = true;
				}
				if (!skipUpdate && !state.isAir() && bhapi_canMoveBlock(state, level, x2, y2, z2, false) && (state.getBlock().getPistonPushMode() == 0 || state.is(BaseBlock.PISTON) || state.is(BaseBlock.STICKY_PISTON))) {
					this.updateBlock = false;
					provider.setBlockState(x2, y2, z2, BlockUtil.AIR_STATE);
					this.updateBlock = true;
					provider.setBlockState(
						x += PistonDataValues.OFFSET_X[meta],
						y += PistonDataValues.OFFSET_Y[meta],
						z += PistonDataValues.OFFSET_Z[meta],
						BlockState.getDefaultState(BaseBlock.MOVING_PISTON).withMeta(meta)
					);
					pistonEntity = (PistonBlockEntity) MovingPistonBlock.createEntity(0, 0, meta, false, false);
					BlockStateContainer.cast(pistonEntity).setDefaultState(state);
					level.setBlockEntity(x, y, z, pistonEntity);
				}
				else if (!skipUpdate) {
					this.updateBlock = false;
					provider.setBlockState(
						x + PistonDataValues.OFFSET_X[meta],
						y + PistonDataValues.OFFSET_Y[meta],
						z + PistonDataValues.OFFSET_Z[meta],
						BlockUtil.AIR_STATE
					);
					this.updateBlock = true;
				}
			}
			else {
				this.updateBlock = false;
				provider.setBlockState(
					x + PistonDataValues.OFFSET_X[meta],
					y + PistonDataValues.OFFSET_Y[meta],
					z + PistonDataValues.OFFSET_Z[meta],
					BlockUtil.AIR_STATE
				);
				this.updateBlock = true;
			}
			level.playSound(x + 0.5, y + 0.5, z + 0.5, "tile.piston.in", 0.5f, level.random.nextFloat() * 0.15f + 0.6f);
		}
		this.updateBlock = false;
	}
	
	@Inject(method = "pushByPiston", at = @At("HEAD"), cancellable = true)
	private void bhapi_pushByPiston(Level level, int x, int y, int z, int meta, CallbackInfoReturnable<Boolean> info) {
		bhapi_level = level;
		int y3;
		int x3;
		int x2 = x + PistonDataValues.OFFSET_X[meta];
		int y2 = y + PistonDataValues.OFFSET_Y[meta];
		int z2 = z + PistonDataValues.OFFSET_Z[meta];
		BlockStateProvider provider = BlockStateProvider.cast(level);
		
		for (x3 = 0; x3 < 13; ++x3) {
			if (y2 <= 0 || y2 >= getLevelHeight()) {
				info.setReturnValue(false);
				return;
			}
			
			BlockState state = provider.getBlockState(x2, y2, z2);
			if (state.isAir()) break;
			
			if (!bhapi_canMoveBlock(state, level, x2, y2, z2, true)) {
				info.setReturnValue(false);
				return;
			}
			
			if (state.getBlock().getPistonPushMode() == 1) {
				state.getBlock().drop(level, x2, y2, z2, state.getMeta());
				level.setBlock(x2, y2, z2, 0);
				break;
			}
			
			if (x3 == 12) {
				info.setReturnValue(false);
				return;
			}
			
			x2 += PistonDataValues.OFFSET_X[meta];
			y2 += PistonDataValues.OFFSET_Y[meta];
			z2 += PistonDataValues.OFFSET_Z[meta];
		}
		
		while (x2 != x || y2 != y || z2 != z) {
			x3 = x2 - PistonDataValues.OFFSET_X[meta];
			y3 = y2 - PistonDataValues.OFFSET_Y[meta];
			int z3 = z2 - PistonDataValues.OFFSET_Z[meta];
			
			BlockState state = provider.getBlockState(x3, y3, z3);
			
			if (state.is(this) && x3 == x && y3 == y && z3 == z) {
				int meta2 = meta | (this.actionByRotation ? 8 : 0);
				BlockState piston = BlockState.getDefaultState(BaseBlock.MOVING_PISTON).withMeta(meta2);
				provider.setBlockState(x2, y2, z2, piston);
				BlockState head = BlockState.getDefaultState(BaseBlock.PISTON_HEAD).withMeta(meta2);
				PistonBlockEntity entity = (PistonBlockEntity) MovingPistonBlock.createEntity(0, 0, meta, true, false);
				BlockStateContainer.cast(entity).setDefaultState(head);
				level.setBlockEntity(x2, y2, z2, entity);
			}
			else {
				BlockState state2 = BlockState.getDefaultState(BaseBlock.MOVING_PISTON).withMeta(state.getMeta());
				provider.setBlockState(x2, y2, z2, state2);
				PistonBlockEntity entity = (PistonBlockEntity) MovingPistonBlock.createEntity(0, 0, meta, true, false);
				BlockStateContainer.cast(entity).setDefaultState(state);
				level.setBlockEntity(x2, y2, z2, entity);
			}
			
			x2 = x3;
			y2 = y3;
			z2 = z3;
		}
		
		info.setReturnValue(true);
	}
	
	@Inject(method = "canMoveBlock(Lnet/minecraft/level/Level;IIII)Z", at = @At("HEAD"), cancellable = true)
	private static void bhapi_canMoveBlock(Level level, int x, int y, int z, int l, CallbackInfoReturnable<Boolean> info) {
		bhapi_level = level;
		int px = x + PistonDataValues.OFFSET_X[l];
		int py = y + PistonDataValues.OFFSET_Y[l];
		int pz = z + PistonDataValues.OFFSET_Z[l];
		short height = LevelHeightProvider.cast(level).getLevelHeight();
		BlockStateProvider provider = BlockStateProvider.cast(level);
		for (int i = 0; i < 13; ++i) {
			if (py <= 0 || py >= height) {
				info.setReturnValue(false);
				return;
			}
			
			BlockState state = provider.getBlockState(px, py, pz);
			if (state.isAir()) break;
			
			if (!bhapi_canMoveBlock(state, level, px, py, pz, true)) {
				info.setReturnValue(false);
				return;
			}
			
			if (state.getBlock().getPistonPushMode() == 1) break;
			if (i == 12) {
				info.setReturnValue(false);
				return;
			}
			
			px += PistonDataValues.OFFSET_X[l];
			py += PistonDataValues.OFFSET_Y[l];
			pz += PistonDataValues.OFFSET_Z[l];
		}
		info.setReturnValue(true);
	}
	
	@Unique
	private static boolean bhapi_canMoveBlock(BlockState state, Level level, int x, int y, int z, boolean flag) {
		if (state.is(BaseBlock.OBSIDIAN)) return false;
		if (state.is(BaseBlock.PISTON) || state.is(BaseBlock.STICKY_PISTON)) {
			return !PistonBlock.isExtendedByMeta(level.getBlockMeta(x, y, z));
		}
		else {
			if (state.getHardness() == -1.0f) return false;
			if (state.getBlock().getPistonPushMode() == 2) return false;
			if (!flag && state.getBlock().getPistonPushMode() == 1) return false;
		}
		return level.getBlockEntity(x, y, z) == null;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		if (view instanceof BlockItemView) {
			BaseBlock block = state.getBlock();
			int texture = block.getTextureForSide(textureIndex, 1);
			TextureSample sample = Textures.getVanillaBlockSample(texture);
			sample.setRotation(0);
			return sample;
		}
		BaseBlock block = state.getBlock();
		int texture = block.getTextureForSide(view, x, y, z, textureIndex);
		return Textures.getVanillaBlockSample(texture);
	}
}

