package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.block.BreakInfo;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.ChunkHeightProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.MathUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.maths.Box;
import net.minecraft.util.maths.MathHelper;
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
	@Shadow private Minecraft client;
	@Shadow public float blockBreakDelta;
	
	@Shadow protected abstract void renderBox(Box arg);
	
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
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, 771);
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
			GL11.glDisable(GL11.GL_BLEND);
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
	
	@Inject(method = "renderBlockBreak", at = @At("HEAD"), cancellable = true)
	public void renderBlockBreak(PlayerBase player, HitResult hit, int flag, ItemStack stack, float delta, CallbackInfo info) {
		info.cancel();
		
		Tessellator tessellator = Tessellator.INSTANCE;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, (MathHelper.sin((float) System.currentTimeMillis() / 100.0f) * 0.2f + 0.4f) * 0.5f);
		
		if (this.blockBreakDelta > 0.0f) {
			GL11.glBlendFunc(774, 768);
			
			int stage = (int) (this.blockBreakDelta * 10.0F);
			int texture = Textures.getBlockBreaking(stage);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			BreakInfo.stage = stage;
			BreakInfo.POS.set(hit.x, hit.y, hit.z);
			
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
			GL11.glPushMatrix();
			
			BlockState state = BlockStateProvider.cast(level).getBlockState(hit.x, hit.y, hit.z);
			
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
			
			tessellator.draw();
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
	
	// TODO remove this
	@Inject(method = "renderClouds", at = @At("HEAD"))
	private void debugRenderHeightmap(float delta, CallbackInfo info) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		if (!BHAPIClient.getMinecraft().options.debugHud) return;
		
		LivingEntity entity = this.client.viewEntity;
		double x = MathUtil.lerp(entity.prevX, entity.x, delta);
		double y = MathUtil.lerp(entity.prevY, entity.y, delta);
		double z = MathUtil.lerp(entity.prevZ, entity.z, delta);
		int chunkX = MathHelper.floor(x / 16.0);
		int chunkZ = MathHelper.floor(z / 16.0);
		x = (chunkX << 4) - x;
		z = (chunkZ << 4) - z;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		// Render chunk borders
		/*for (byte dy = -3; dy < 4; dy++) {
			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glColor3f(1F, 0F, 0F);
			GL11.glVertex3f((float) x, dy, 16 + (float) z);
			GL11.glVertex3f(16 + (float) x, dy, 16 + (float) z);
			GL11.glVertex3f(16 + (float) x, dy, (float) z);
			GL11.glVertex3f((float) x, dy, (float) z);
			GL11.glEnd();
		}*/
		
		ChunkHeightProvider provider = ChunkHeightProvider.cast(entity.level.getChunkFromCache(chunkX, chunkZ));
		GL11.glColor3f(1F, 1F, 0F);
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				short height = provider.getHeightmapData(dx, dz);
				float py = (float) (height - y) + 0.1F;
				GL11.glBegin(GL11.GL_LINE_LOOP);
				GL11.glVertex3f(0.1F + (float) x + dx, py, 0.9F + (float) z + dz);
				GL11.glVertex3f(0.9F + (float) x + dx, py, 0.9F + (float) z + dz);
				GL11.glVertex3f(0.9F + (float) x + dx, py, 0.1F + (float) z + dz);
				GL11.glVertex3f(0.1F + (float) x + dx, py, 0.1F + (float) z + dz);
				GL11.glEnd();
			}
		}
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
