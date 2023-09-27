package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureAtlas;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.BHItemRender;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec2F;
import net.bhapi.util.MathUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.OverlaysRenderer;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.item.MapRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.AbstractClientPlayer;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MapItem;
import net.minecraft.level.storage.MapStorage;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlaysRenderer.class)
public abstract class OverlaysRendererMixin {
	@Shadow private float movementStart;
	@Shadow private float movementEnd;
	@Shadow private Minecraft minecraft;
	@Shadow private ItemStack stack;
	@Shadow private MapRenderer mapRenderer;
	
	@Shadow public abstract void renderHand(LivingEntity arg, ItemStack arg2);
	@Shadow protected abstract void renderUnderwaterOverlay(float f);
	@Shadow protected abstract void renderFireOverlay(float f);
	@Shadow protected abstract void renderSuffocateOverlay(float f, int i);
	
	@Unique private final BlockItemView bhapi_itemView = new BlockItemView();
	
	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderItemInHand(float delta, CallbackInfo info) {
		info.cancel();
		float r, g, b;
		float offset = this.movementStart + (this.movementEnd - this.movementStart) * delta;
		AbstractClientPlayer player = this.minecraft.player;
		float rotation = player.prevPitch + (player.pitch - player.prevPitch) * delta;
		
		GL11.glPushMatrix();
		GL11.glRotatef(rotation, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(
			player.prevYaw + (player.yaw - player.prevYaw) * delta,
			0.0F, 1.0F, 0.0F
		);
		RenderHelper.enableLighting();
		GL11.glPopMatrix();
		
		float light = this.minecraft.level.getBrightness(
			MathHelper.floor(player.x),
			MathHelper.floor(player.y),
			MathHelper.floor(player.z)
		);
		
		if (this.stack != null) {
			BaseItem item = this.stack.getType();
			int color = item.getColorMultiplier(this.stack.getDamage());
			r = (float) (color >> 16 & 0xFF) / 255.0F;
			g = (float) (color >> 8 & 0xFF) / 255.0F;
			b = (float) (color & 0xFF) / 255.0F;
			GL11.glColor4f(light * r, light * g, light * b, 1.0F);
		}
		else {
			GL11.glColor4f(light, light, light, 1.0f);
		}
		
		if (this.stack != null && this.stack.getType() instanceof MapItem) {
			GL11.glPushMatrix();
			float f8 = 0.8f;
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glTranslatef(-b * 0.4f, MathHelper.sin(MathHelper.sqrt(r) * (float) Math.PI * 2.0f) * 0.2f, -g * 0.2f);
			r = 1.0f - rotation / 45.0f + 0.1f;
			if (r < 0.0f) {
				r = 0.0f;
			}
			if (r > 1.0f) {
				r = 1.0f;
			}
			r = -MathHelper.cos(r * (float)Math.PI) * 0.5f + 0.5f;
			GL11.glTranslatef(0.0f, 0.0f * f8 - (1.0f - offset) * 1.2f - r * 0.5f + 0.04f, -0.9f * f8);
			GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(r * -85.0f, 0.0f, 0.0f, 1.0f);
			GL11.glEnable(32826);
			GL11.glBindTexture(3553, this.minecraft.textureManager.getOnlineImageOrDefaultTextureId(this.minecraft.player.skinUrl, this.minecraft.player.getTextured()));
			
			for (int i = 0; i < 2; ++i) {
				int n = i * 2 - 1;
				GL11.glPushMatrix();
				GL11.glTranslatef(-0.0f, -0.6f, 1.1f * (float)n);
				GL11.glRotatef(-45 * n, 1.0f, 0.0f, 0.0f);
				GL11.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
				GL11.glRotatef(59.0f, 0.0f, 0.0f, 1.0f);
				GL11.glRotatef(-65 * n, 0.0f, 1.0f, 0.0f);
				EntityRenderer entityRenderer = EntityRenderDispatcher.INSTANCE.get(this.minecraft.player);
				PlayerRenderer playerRenderer = (PlayerRenderer)entityRenderer;
				float f9 = 1.0f;
				GL11.glScalef(f9, f9, f9);
				playerRenderer.renderHand();
				GL11.glPopMatrix();
			}
			
			float f10 = player.getHandSwing(delta);
			b = MathHelper.sin(f10 * f10 * (float)Math.PI);
			float f11 = MathHelper.sin(MathHelper.sqrt(f10) * (float)Math.PI);
			GL11.glRotatef(-b * 20.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(-f11 * 20.0f, 0.0f, 0.0f, 1.0f);
			GL11.glRotatef(-f11 * 80.0f, 1.0f, 0.0f, 0.0f);
			f10 = 0.38f;
			GL11.glScalef(f10, f10, f10);
			GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
			GL11.glTranslatef(-1.0f, -1.0f, 0.0f);
			b = 0.015625f;
			GL11.glScalef(b, b, b);
			this.minecraft.textureManager.bindTexture(this.minecraft.textureManager.getTextureId("/misc/mapbg.png"));
			Tessellator tessellator = Tessellator.INSTANCE;
			GL11.glNormal3f(0.0f, 0.0f, -1.0f);
			tessellator.start();
			int n = 7;
			tessellator.vertex(0 - n, 128 + n, 0.0, 0.0, 1.0);
			tessellator.vertex(128 + n, 128 + n, 0.0, 1.0, 1.0);
			tessellator.vertex(128 + n, 0 - n, 0.0, 1.0, 0.0);
			tessellator.vertex(0 - n, 0 - n, 0.0, 0.0, 0.0);
			tessellator.draw();
			MapStorage mapStorage = BaseItem.map.getMapStorage(this.stack, this.minecraft.level);
			this.mapRenderer.loadData(this.minecraft.player, this.minecraft.textureManager, mapStorage);
			GL11.glPopMatrix();
		}
		else if (this.stack != null) {
			GL11.glPushMatrix();
			float f12 = 0.8f;
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glTranslatef(-b * 0.4f, MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI * 2.0f) * 0.2f, -g * 0.2f);
			GL11.glTranslatef(0.7f * f12, -0.65f * f12 - (1.0f - offset) * 0.6f, -0.9f * f12);
			GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
			GL11.glEnable(32826);
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glRotatef(-g * 20.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(-b * 20.0f, 0.0f, 0.0f, 1.0f);
			GL11.glRotatef(-b * 80.0f, 1.0f, 0.0f, 0.0f);
			r = 0.4f;
			GL11.glScalef(r, r, r);
			if (this.stack.getType().shouldSpinWhenRendering()) {
				GL11.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
			}
			this.renderHand(player, this.stack);
			GL11.glPopMatrix();
		}
		else {
			GL11.glPushMatrix();
			float f13 = 0.8f;
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glTranslatef(-b * 0.3f, MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI * 2.0f) * 0.4f, -g * 0.4f);
			GL11.glTranslatef(0.8f * f13, -0.75f * f13 - (1.0f - offset) * 0.6f, -0.9f * f13);
			GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
			GL11.glEnable(32826);
			r = player.getHandSwing(delta);
			g = MathHelper.sin(r * r * (float)Math.PI);
			b = MathHelper.sin(MathHelper.sqrt(r) * (float)Math.PI);
			GL11.glRotatef(b * 70.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(-g * 20.0f, 0.0f, 0.0f, 1.0f);
			GL11.glBindTexture(3553, this.minecraft.textureManager.getOnlineImageOrDefaultTextureId(this.minecraft.player.skinUrl, this.minecraft.player.getTextured()));
			GL11.glTranslatef(-1.0f, 3.6f, 3.5f);
			GL11.glRotatef(120.0f, 0.0f, 0.0f, 1.0f);
			GL11.glRotatef(200.0f, 1.0f, 0.0f, 0.0f);
			GL11.glRotatef(-135.0f, 0.0f, 1.0f, 0.0f);
			GL11.glScalef(1.0f, 1.0f, 1.0f);
			GL11.glTranslatef(5.6f, 0.0f, 0.0f);
			EntityRenderer entityRenderer = EntityRenderDispatcher.INSTANCE.get(this.minecraft.player);
			PlayerRenderer playerRenderer = (PlayerRenderer)entityRenderer;
			b = 1.0f;
			GL11.glScalef(b, b, b);
			playerRenderer.renderHand();
			GL11.glPopMatrix();
		}
		GL11.glDisable(32826);
		RenderHelper.disableLighting();
	}
	
	@Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderHand(LivingEntity entity, ItemStack stack, CallbackInfo info) {
		info.cancel();
		GL11.glPushMatrix();
		BaseItem item = stack.getType();
		TextureAtlas atlas = Textures.getAtlas();
		atlas.bind();
		
		if (item instanceof BHBlockItem && !BHBlockItem.cast(item).isFlat()) {
			BlockState state = BHBlockItem.cast(item).getState();
			BHBlockRenderer renderer = BHAPIClient.getBlockRenderer();
			bhapi_itemView.setBlockState(state);
			renderer.setView(bhapi_itemView);
			renderer.renderItem(state, entity.getBrightnessAtEyes(1.0f));
		}
		else {
			float u22;
			float delta;
			int count;
			
			TextureSample sample = BHItemRender.cast(item).getTexture(stack);
			Vec2F uv1 = sample.getUV(0, 0);
			Vec2F uv2 = sample.getUV(1, 1);
			
			int color = item.getColorMultiplier(stack.getDamage());
			if (color != 0xFFFFFF) {
				float light = entity.getBrightnessAtEyes(entity.getEyeHeight());
				float r = ((color >> 16) & 255) / 255F;
				float g = ((color >> 8) & 255) / 255F;
				float b = (color & 255) / 255F;
				GL11.glColor3f(r * light, g * light, b * light);
			}
			
			GL11.glEnable(0x803a);
			GL11.glTranslatef(0.0f, -0.3f, 0.0f);
			GL11.glScalef(1.5f, 1.5f, 1.5f);
			GL11.glRotatef(50.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(335.0f, 0.0f, 0.0f, 1.0f);
			GL11.glTranslatef(-0.9375f, -0.0625f, 0.0f);
			
			Tessellator tessellator = Tessellator.INSTANCE;
			
			tessellator.start();
			tessellator.setNormal(0.0f, 0.0f, 1.0f);
			tessellator.vertex(0.0, 0.0, 0.0, uv2.x, uv2.y);
			tessellator.vertex(1.0, 0.0, 0.0, uv1.x, uv2.y);
			tessellator.vertex(1.0, 1.0, 0.0, uv1.x, uv1.y);
			tessellator.vertex(0.0, 1.0, 0.0, uv2.x, uv1.y);
			
			tessellator.setNormal(0.0f, 0.0f, -1.0f);
			tessellator.vertex(0.0, 1.0, 0.0f - 0.0625f, uv2.x, uv1.y);
			tessellator.vertex(1.0, 1.0, 0.0f - 0.0625f, uv1.x, uv1.y);
			tessellator.vertex(1.0, 0.0, 0.0f - 0.0625f, uv1.x, uv2.y);
			tessellator.vertex(0.0, 0.0, 0.0f - 0.0625f, uv2.x, uv2.y);
			
			tessellator.setNormal(-1.0f, 0.0f, 0.0f);
			for (count = 0; count < sample.getWidth(); ++count) {
				delta = (float) count / sample.getWidth();
				u22 = MathUtil.lerp(uv1.x, uv2.x, 0.999F - delta);
				tessellator.vertex(delta, 0.0, 0.0f - 0.0625f, u22, uv2.y);
				tessellator.vertex(delta, 0.0, 0.0, u22, uv2.y);
				tessellator.vertex(delta, 1.0, 0.0, u22, uv1.y);
				tessellator.vertex(delta, 1.0, 0.0f - 0.0625f, u22, uv1.y);
			}
			
			tessellator.setNormal(0.0f, -1.0f, 0.0f);
			for (count = 0; count < sample.getHeight(); ++count) {
				delta = (float) count / sample.getHeight();
				u22 = MathUtil.lerp(uv1.y, uv2.y, 0.999F - delta);
				tessellator.vertex(1.0, delta, 0.0, uv1.x, u22);
				tessellator.vertex(0.0, delta, 0.0, uv2.x, u22);
				tessellator.vertex(0.0, delta, 0.0f - 0.0625f, uv2.x, u22);
				tessellator.vertex(1.0, delta, 0.0f - 0.0625f, uv1.x, u22);
			}
			
			float offset = 1F / sample.getHeight();
			tessellator.setNormal(0.0f, 1.0f, 0.0f);
			for (count = 0; count < sample.getHeight(); ++count) {
				delta = (float) count / sample.getHeight();
				u22 = MathUtil.lerp(uv1.y, uv2.y, 0.999F - delta);
				float y = delta + offset;
				tessellator.vertex(1.0, y, 0.0f - 0.0625f, uv1.x, u22);
				tessellator.vertex(0.0, y, 0.0f - 0.0625f, uv2.x, u22);
				tessellator.vertex(0.0, y, 0.0, uv2.x, u22);
				tessellator.vertex(1.0, y, 0.0, uv1.x, u22);
			}
			
			tessellator.draw();
			
			GL11.glDisable(32826);
		}
		GL11.glPopMatrix();
	}
	
	@Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
	public void bhapi_renderOverlays(float delta, CallbackInfo info) {
		info.cancel();
		int x, y, z;
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		
		if (this.minecraft.player.isOnFire()) {
			Textures.getAtlas().bind();
			this.renderFireOverlay(delta);
		}
		
		if (this.minecraft.player.isInsideWall()) {
			x = MathHelper.floor(this.minecraft.player.x);
			y = MathHelper.floor(this.minecraft.player.y);
			z = MathHelper.floor(this.minecraft.player.z);
			Textures.getAtlas().bind();
			BlockState state = BlockStateProvider.cast(this.minecraft.level).getBlockState(x, y, z);
			if (this.minecraft.level.canSuffocate(x, y, z)) {
				int texture = state.getTextureForIndex(this.minecraft.level, x, y, z, 2, 0).getTextureID();
				this.renderSuffocateOverlay(delta, texture);
			}
			else {
				for (int i = 0; i < 8; ++i) {
					float fx = (((i) & 1) - 0.5f) * this.minecraft.player.width * 0.9f;
					float fy = (((i >> 1) & 1) - 0.5f) * this.minecraft.player.height * 0.2f;
					float fz = (((i >> 2) & 1) - 0.5f) * this.minecraft.player.width * 0.9f;
					int px = MathHelper.floor(x + fx);
					int py = MathHelper.floor(y + fy);
					int pz = MathHelper.floor(z + fz);
					if (!this.minecraft.level.canSuffocate(px, py, pz)) continue;
					state = BlockStateProvider.cast(this.minecraft.level).getBlockState(px, py, pz);
				}
			}
			if (!state.isAir()) {
				int texture = state.getTextureForIndex(this.minecraft.level, x, y, z, 2, 0).getTextureID();
				this.renderSuffocateOverlay(delta, texture);
			}
		}
		
		if (this.minecraft.player.isInFluid(Material.WATER)) {
			int texture = this.minecraft.textureManager.getTextureId("/misc/water.png");
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			this.renderUnderwaterOverlay(delta);
		}
		
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}
	
	@Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderFireOverlay(float delta, CallbackInfo info) {
		info.cancel();
		Tessellator tessellator = Tessellator.INSTANCE;
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		float f2 = 1.0f;
		for (int i = 0; i < 2; ++i) {
			GL11.glPushMatrix();
			
			TextureSample sample = BlockState.getDefaultState(BaseBlock.FIRE).getTextureForIndex(bhapi_itemView, 0, 0, 0, i, 0);
			Vec2F uv1 = sample.getUV(0, 0);
			Vec2F uv2 = sample.getUV(1, 1);
			
			float x1 = (0.0f - f2) / 2.0f;
			float x2 = x1 + f2;
			float y1 = 0.0f - f2 / 2.0f;
			float y2 = y1 + f2;
			
			GL11.glTranslatef((1 - i * 2) * 0.24f, -0.3f, 0.0f);
			GL11.glRotatef((i * 2 - 1) * 10.0f, 0.0f, 1.0f, 0.0f);
			
			tessellator.start();
			tessellator.vertex(x1, y1, -0.5f, uv2.x, uv2.y);
			tessellator.vertex(x2, y1, -0.5f, uv1.x, uv2.y);
			tessellator.vertex(x2, y2, -0.5f, uv1.x, uv1.y);
			tessellator.vertex(x1, y2, -0.5f, uv2.x, uv1.y);
			tessellator.draw();
			
			GL11.glPopMatrix();
		}
		
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	@Inject(method = "renderSuffocateOverlay", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderSuffocateOverlay(float delta, int index, CallbackInfo info) {
		info.cancel();
		Tessellator tessellator = Tessellator.INSTANCE;
		GL11.glColor4f(0.1f, 0.1f, 0.1f, 0.5f);
		GL11.glPushMatrix();
		
		UVPair uv = Textures.getAtlas().getUV(index);
		
		float u1 = uv.getU(0);
		float u2 = uv.getU(1);
		float v1 = uv.getV(0);
		float v2 = uv.getV(1);
		
		tessellator.start();
		tessellator.vertex(-1.0f, -1.0f, -0.5f, u2, v2);
		tessellator.vertex(1.0f, -1.0f, -0.5f, u1, v2);
		tessellator.vertex(1.0f, 1.0f, -0.5f, u1, v1);
		tessellator.vertex(-1.0f, 1.0f, -0.5f, u2, v1);
		tessellator.draw();
		
		GL11.glPopMatrix();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}
}
