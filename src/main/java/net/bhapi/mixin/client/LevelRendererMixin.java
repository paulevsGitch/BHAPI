package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.block.BreakInfo;
import net.bhapi.client.render.level.ClientChunks;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.level.light.ClientLightLevel;
import net.bhapi.registry.CommonRegistries;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.maths.Box;
import net.minecraft.util.maths.BoxCollider;
import net.minecraft.util.maths.MCMath;
import org.lwjgl.opengl.GL11;
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

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements LevelHeightProvider {
	@Shadow private Level level;
	@Shadow private Minecraft minecraft;
	@Shadow public float blockBreakDelta;
	@SuppressWarnings("rawtypes")
	@Shadow private List areaRenderers;
	@Shadow private int sectionCounX;
	@Shadow private int sectionCounY;
	@Shadow private int sectionCounZ;
	
	@Shadow protected abstract void renderBox(Box arg);
	
	@ModifyConstant(
		method = "renderEntitiesFromPos",
		constant = @Constant(intValue = 128)
	)
	private int bhapi_changeMaxHeight(int value) {
		return bhapi_getLevelHeight();
	}
	
	@ModifyConstant(
		method = "renderEntitiesFromPos",
		constant = @Constant(intValue = 127)
	)
	private int bhapi_changeMaxBlockHeight(int value) {
		return bhapi_getLevelHeight() - 1;
	}
	
	@ModifyConstant(method = "updateFromOptions()V", constant = @Constant(intValue = 8))
	private int bhapi_changeSectionCount(int value) {
		return bhapi_getSectionsCount();
	}
	
	@Unique
	@Override
	public short bhapi_getLevelHeight() {
		return LevelHeightProvider.cast(this.level).bhapi_getLevelHeight();
	}
	
	@Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderBlockOutline(PlayerEntity player, HitResult hit, int flag, ItemStack stack, float delta, CallbackInfo info) {
		info.cancel();
		if (flag == 0 && hit.type == HitType.BLOCK) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, 771);
			GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
			GL11.glLineWidth(2.0f);
			GL11.glDisable(3553);
			GL11.glDepthMask(false);
			final float offset = 0.002f;
			BlockState state = BlockStateProvider.cast(this.level).bhapi_getBlockState(hit.x, hit.y, hit.z);
			if (!state.isAir()) {
				Block block = state.getBlock();
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
			GL11.glDisable(GL11.GL_BLEND);
		}
	}
	
	@Inject(method = "playLevelEvent", at = @At("HEAD"), cancellable = true)
	private void bhapi_playLevelEvent(PlayerEntity arg, int i, int x, int y, int z, int data, CallbackInfo info) {
		if (i == 2001) {
			info.cancel();
			BlockState state = CommonRegistries.BLOCKSTATES_MAP.get(data);
			if (state == null) return;
			BlockSounds sounds = state.getSounds();
			this.minecraft.soundHelper.playSound(
				sounds.getBreakSound(),
				x + 0.5f,
				y + 0.5f,
				z + 0.5f,
				(sounds.getVolume() + 1.0f) / 2.0f,
				sounds.getPitch() * 0.8f
			);
			this.minecraft.particleManager.addBlockBreakParticles(x, y, z, state.getID(), 0);
		}
	}
	
	@Inject(method = "renderBlockBreaking", at = @At("HEAD"), cancellable = true)
	public void renderBlockBreak(PlayerEntity player, HitResult hit, int flag, ItemStack stack, float delta, CallbackInfo info) {
		info.cancel();
		
		Tessellator tessellator = Tessellator.INSTANCE;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, (MCMath.sin((float) System.currentTimeMillis() / 100.0f) * 0.2f + 0.4f) * 0.5f);
		
		if (this.blockBreakDelta > 0.0f) {
			GL11.glBlendFunc(774, 768);
			
			int stage = (int) (this.blockBreakDelta * 10.0F);
			int texture = Textures.getBlockBreaking(stage);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			BreakInfo.stage = stage;
			BreakInfo.POS.set(hit.x, hit.y, hit.z);
			
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
			GL11.glPushMatrix();
			
			BlockState state = BlockStateProvider.cast(level).bhapi_getBlockState(hit.x, hit.y, hit.z);
			
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glPolygonOffset(-3.0f, -3.0f);
			GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
			
			double dx = player.prevRenderX + (player.x - player.prevRenderX) * delta;
			double dy = player.prevRenderY + (player.y - player.prevRenderY) * delta;
			double dz = player.prevRenderZ + (player.z - player.prevRenderZ) * delta;
			
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			tessellator.start();
			tessellator.setOffset(-dx, -dy, -dz);
			tessellator.disableColor();
			
			BHBlockRenderer renderer = BHAPIClient.getBlockRenderer();
			renderer.setView(level);
			renderer.renderBlockBreak(state, hit.x, hit.y, hit.z);
			
			tessellator.render();
			tessellator.setOffset(0.0, 0.0, 0.0);
			
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glPolygonOffset(0.0f, 0.0f);
			GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			
			GL11.glDepthMask(true);
			GL11.glPopMatrix();
		}
		else {
			BreakInfo.stage = -1;
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
	}
	
	@Inject(method = "updateAreasAround", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateAreasAround(LivingEntity arg, boolean bl, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(true);
	}
	
	@Inject(method = "updateOcclusion(Lnet/minecraft/entity/living/LivingEntity;ID)I", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderChunks(LivingEntity entity, int layer, double delta, CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(0);
		if (layer != 0) return;
		ClientChunks.render(entity, (float) delta);
	}
	
	@Inject(method = "updateFromOptions", at = @At("TAIL"))
	public void updateFromOptions(CallbackInfo info) {
		ClientChunks.init();
		ClientLightLevel.init();
	}
	
	@Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
	private void bhapi_playSound(String string, double d, double e, double f, float g, float h, CallbackInfo info) {
		if (this.minecraft.viewEntity == null) info.cancel();
	}
	
	@Inject(method = "checkVisibility", at = @At("HEAD"), cancellable = true)
	private void bhapi_disableAreasCheck(BoxCollider arg, float f, CallbackInfo info) {
		info.cancel();
	}
}
