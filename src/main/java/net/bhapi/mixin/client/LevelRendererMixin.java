package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.registry.CommonRegistries;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.maths.Box;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements LevelHeightProvider {
	@Shadow private Level level;
	
	@Shadow protected abstract void renderBox(Box arg);
	
	@Shadow private Minecraft client;
	
	@ModifyConstant(method = "method_1544(Lnet/minecraft/util/maths/Vec3f;Lnet/minecraft/class_68;F)V", constant = @Constant(intValue = 128))
	private int bhapi_changeMaxHeight(int value) {
		return getLevelHeight();
	}
	
	@ModifyConstant(method = "method_1544(Lnet/minecraft/util/maths/Vec3f;Lnet/minecraft/class_68;F)V", constant = @Constant(intValue = 127))
	private int bhapi_changeMaxBlockHeight(int value) {
		return getLevelHeight() - 1;
	}
	
	@ModifyConstant(method = "updateFromOptions()V", constant = @Constant(intValue = 8))
	private int bhapi_changeSectionCount(int value) {
		return getSectionsCount();
	}
	
	@Unique
	@Override
	public short getLevelHeight() {
		return LevelHeightProvider.cast(this.level).getLevelHeight();
	}
	
	@Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderBlockOutline(PlayerBase player, HitResult hit, int flag, ItemStack stack, float delta, CallbackInfo info) {
		info.cancel();
		if (flag == 0 && hit.type == HitType.BLOCK) {
			GL11.glEnable(3042);
			GL11.glBlendFunc(770, 771);
			GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
			GL11.glLineWidth(2.0f);
			GL11.glDisable(3553);
			GL11.glDepthMask(false);
			final float offset = 0.002f;
			BlockState state = BlockStateProvider.cast(this.level).getBlockState(hit.x, hit.y, hit.z);
			if (!state.isAir()) {
				BaseBlock block = state.getBlock();
				block.updateBoundingBox(this.level, hit.x, hit.y, hit.z);
				double dx = player.prevRenderX + (player.x - player.prevRenderX) * delta;
				double dy = player.prevRenderY + (player.y - player.prevRenderY) * delta;
				double dz = player.prevRenderZ + (player.z - player.prevRenderZ) * delta;
				this.renderBox(
					block.getOutlineShape(this.level, hit.x, hit.y, hit.z)
						 .expandNegative(offset, offset, offset)
						 .expandPositive(-dx, -dy, -dz)
				);
			}
			GL11.glDepthMask(true);
			GL11.glEnable(3553);
			GL11.glDisable(3042);
		}
	}
	
	@Inject(method = "playLevelEvent", at = @At("HEAD"), cancellable = true)
	private void bhapi_playLevelEvent(PlayerBase arg, int i, int x, int y, int z, int data, CallbackInfo info) {
		if (i == 2001) {
			info.cancel();
			BlockState state = CommonRegistries.BLOCKSTATES_MAP.get(data);
			if (state == null) return;
			BlockSounds sounds = state.getSounds();
			this.client.soundHelper.playSound(
				sounds.getBreakSound(),
				x + 0.5f,
				y + 0.5f,
				z + 0.5f,
				(sounds.getVolume() + 1.0f) / 2.0f,
				sounds.getPitch() * 0.8f
			);
			this.client.particleManager.addBlockBreakParticles(x, y, z, state.getID(), 0);
		}
	}
}
