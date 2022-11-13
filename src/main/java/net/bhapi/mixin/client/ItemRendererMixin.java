package net.bhapi.mixin.client;

import net.bhapi.client.ItemRenderInfo;
import net.bhapi.item.BHBlockItem;
import net.minecraft.block.BaseBlock;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin extends EntityRenderer {
	@Shadow public boolean coloriseItem;
	@Shadow private BlockRenderer internalBlockRenderer;
	@Shadow public abstract void renderRectangle(int i, int j, int k, int l, int m, int n);
	
	@Shadow private Random rand;
	
	@Inject(method = "renderItemInGUI", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderItemInGUI(TextRenderer textRenderer, TextureManager manager, int id, int j, int k, int l, int m, CallbackInfo info) {
		info.cancel();
		BaseItem item = ItemRenderInfo.getRenderingItem();
		if (item == null) return;
		if (item instanceof BHBlockItem /*id < 256 && BlockRenderer.isSpecificRenderType(BaseBlock.BY_ID[id].getRenderType())*/) {
			manager.bindTexture(manager.getTextureId("/terrain.png"));
			BaseBlock block = ((BHBlockItem) item).getState().getBlock();
			GL11.glPushMatrix();
			GL11.glTranslatef(l - 2, m + 3, -3.0f);
			GL11.glScalef(10.0f, 10.0f, 10.0f);
			GL11.glTranslatef(1.0f, 0.5f, 1.0f);
			GL11.glScalef(1.0f, 1.0f, -1.0f);
			GL11.glRotatef(210.0f, 1.0f, 0.0f, 0.0f);
			GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
			if (this.coloriseItem) {
				int color = item.getColorMultiplier(j);
				float r = (float) (color >> 16 & 0xFF) / 255.0F;
				float g = (float) (color >> 8 & 0xFF) / 255.0F;
				float b = (float) (color & 0xFF) / 255.0F;
				GL11.glColor4f(r, g, b, 1.0F);
			}
			GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
			this.internalBlockRenderer.itemColorEnabled = this.coloriseItem;
			this.internalBlockRenderer.renderBlockItem(block, j, 1.0f);
			this.internalBlockRenderer.itemColorEnabled = true;
			GL11.glPopMatrix();
		}
		else if (k >= 0) {
			GL11.glDisable(2896);
			/*if (id < 256) {
				manager.bindTexture(manager.getTextureId("/terrain.png"));
			}
			else {*/
				manager.bindTexture(manager.getTextureId("/gui/items.png"));
			//}
			if (this.coloriseItem) {
				int color = item.getColorMultiplier(j);
				float r = (float) (color >> 16 & 0xFF) / 255.0F;
				float g = (float) (color >> 8 & 0xFF) / 255.0F;
				float b = (float) (color & 0xFF) / 255.0F;
				GL11.glColor4f(r, g, b, 1.0F);
			}
			this.renderRectangle(l, m, k % 16 * 16, k / 16 * 16, 16, 16);
			GL11.glEnable(2896);
		}
		GL11.glEnable(2884);
	}
	
	@Inject(method = "render(Lnet/minecraft/entity/ItemEntity;DDDFF)V", at = @At("HEAD"), cancellable = true)
	public void render(ItemEntity entity, double x, double y, double z, float u1, float delta, CallbackInfo info) {
		info.cancel();
		this.rand.setSeed(187L);
		GL11.glPushMatrix();
		float offset = MathHelper.sin(((float) entity.age + delta) / 10.0f + entity.rotation) * 0.1f + 0.1f;
		float f3 = (((float) entity.age + delta) / 20.0f + entity.rotation) * 57.295776f;
		int count = 1;
		if (entity.stack.count > 1) {
			count = 2;
		}
		if (entity.stack.count > 5) {
			count = 3;
		}
		if (entity.stack.count > 20) {
			count = 4;
		}
		GL11.glTranslatef((float) x, (float) y + offset, (float) z);
		GL11.glEnable(32826);
		BaseItem item = entity.stack.getType();
		//System.out.println(item);
		if (item instanceof BHBlockItem /*entity.stack.itemId < 256 && BlockRenderer.isSpecificRenderType(BaseBlock.BY_ID[entity.stack.itemId].getRenderType())*/) {
			GL11.glRotatef(f3, 0.0f, 1.0f, 0.0f);
			this.bindTexture("/terrain.png");
			float scale = 0.25f;
			BaseBlock block = ((BHBlockItem) item).getState().getBlock();
			if (!block.isFullCube() && block.id != BaseBlock.STONE_SLAB.id && block.getRenderType() != 16) {
				scale = 0.5f;
			}
			GL11.glScalef(scale, scale, scale);
			for (int i = 0; i < count; ++i) {
				GL11.glPushMatrix();
				if (i > 0) {
					float dx = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.2f / scale;
					float dy = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.2f / scale;
					float dz = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.2f / scale;
					GL11.glTranslatef(dx, dy, dz);
				}
				this.internalBlockRenderer.renderBlockItem(block, entity.stack.getDamage(), entity.getBrightnessAtEyes(delta));
				GL11.glPopMatrix();
			}
		}
		else {
			float b, g, r;
			int n2;
			GL11.glScalef(0.5f, 0.5f, 0.5f);
			int texture = entity.stack.getTexturePosition();
			/*if (entity.stack.itemId < 256) {
				this.bindTexture("/terrain.png");
			}
			else {*/
				this.bindTexture("/gui/items.png");
			//}
			Tessellator tessellator = Tessellator.INSTANCE;
			float f11 = (float) (texture % 16 * 16) / 256.0f;
			float f12 = (float) (texture % 16 * 16 + 16) / 256.0f;
			float f13 = (float) (texture / 16 * 16) / 256.0f;
			float f14 = (float) (texture / 16 * 16 + 16) / 256.0f;
			float f15 = 1.0f;
			float f16 = 0.5f;
			float f17 = 0.25f;
			if (this.coloriseItem) {
				n2 = item.getColorMultiplier(entity.stack.getDamage());
				r = (float) (n2 >> 16 & 0xFF) / 255.0f;
				g = (float) (n2 >> 8 & 0xFF) / 255.0f;
				b = (float) (n2 & 0xFF) / 255.0f;
				float light = entity.getBrightnessAtEyes(delta);
				GL11.glColor4f(r * light, g * light, b * light, 1.0f);
			}
			for (n2 = 0; n2 < count; ++n2) {
				GL11.glPushMatrix();
				if (n2 > 0) {
					r = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					g = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					b = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					GL11.glTranslatef(r, g, b);
				}
				GL11.glRotatef(180.0f - this.dispatcher.angle, 0.0f, 1.0f, 0.0f);
				tessellator.start();
				tessellator.setNormal(0.0f, 1.0f, 0.0f);
				tessellator.vertex(0.0f - f16, 0.0f - f17, 0.0, f11, f14);
				tessellator.vertex(f15 - f16, 0.0f - f17, 0.0, f12, f14);
				tessellator.vertex(f15 - f16, 1.0f - f17, 0.0, f12, f13);
				tessellator.vertex(0.0f - f16, 1.0f - f17, 0.0, f11, f13);
				tessellator.draw();
				GL11.glPopMatrix();
			}
		}
		GL11.glDisable(32826);
		GL11.glPopMatrix();
	}
	
	@Inject(method = "renderStackInGUI", at = @At("HEAD"))
	public void bhapi_renderStackInGUI(TextRenderer textRenderer, TextureManager manager, ItemStack stack, int x, int y, CallbackInfo info) {
		if (stack != null) ItemRenderInfo.setItem(stack.getType());
	}
}
