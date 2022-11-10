package net.bhapi.mixin.client;

import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.BaseItem;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
	@Shadow public boolean coloriseItem;
	@Shadow private BlockRenderer internalBlockRenderer;
	@Shadow public abstract void renderRectangle(int i, int j, int k, int l, int m, int n);
	
	@Unique	private BaseItem bhapi_renderItem;
	
	@Inject(method = "renderItemInGUI", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderItemInGUI(TextRenderer textRenderer, TextureManager manager, int id, int j, int k, int l, int m, CallbackInfo info) {
		info.cancel();
		System.out.println(id);
		if (id < 256 /*&& BlockRenderer.isSpecificRenderType(BaseBlock.BY_ID[id].getRenderType())*/) {
			/*int blockID = id;
			manager.bindTexture(manager.getTextureId("/terrain.png"));
			BaseBlock baseBlock = BaseBlock.BY_ID[blockID];
			GL11.glPushMatrix();
			GL11.glTranslatef(l - 2, m + 3, -3.0f);
			GL11.glScalef(10.0f, 10.0f, 10.0f);
			GL11.glTranslatef(1.0f, 0.5f, 1.0f);
			GL11.glScalef(1.0f, 1.0f, -1.0f);
			GL11.glRotatef(210.0f, 1.0f, 0.0f, 0.0f);
			GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);*/
			/*if (this.coloriseItem) {
				int color = BaseItem.byId[id].getColorMultiplier(j);
				float r = (float) (color >> 16 & 0xFF) / 255.0F;
				float g = (float) (color >> 8 & 0xFF) / 255.0F;
				float b = (float) (color & 0xFF) / 255.0F;
				GL11.glColor4f(r, g, b, 1.0F);
			}*/
			/*GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
			this.internalBlockRenderer.itemColorEnabled = this.coloriseItem;
			this.internalBlockRenderer.renderBlockItem(baseBlock, j, 1.0f);
			this.internalBlockRenderer.itemColorEnabled = true;
			GL11.glPopMatrix();*/
		}
		else if (k >= 0) {
			GL11.glDisable(2896);
			if (id < 256) {
				manager.bindTexture(manager.getTextureId("/terrain.png"));
			}
			else {
				manager.bindTexture(manager.getTextureId("/gui/items.png"));
			}
			/*if (this.coloriseItem) {
				int color = BaseItem.byId[id].getColorMultiplier(j);
				float r = (float) (color >> 16 & 0xFF) / 255.0F;
				float g = (float) (color >> 8 & 0xFF) / 255.0F;
				float b = (float) (color & 0xFF) / 255.0F;
				GL11.glColor4f(r, g, b, 1.0F);
			}*/
			this.renderRectangle(l, m, k % 16 * 16, k / 16 * 16, 16, 16);
			GL11.glEnable(2896);
		}
		GL11.glEnable(2884);
	}
}
