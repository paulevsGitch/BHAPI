package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.BHItemRender;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin extends EntityRenderer {
	@Shadow public boolean coloriseItem;
	@Shadow private BlockRenderer internalBlockRenderer;
	@Shadow private Random rand;
	
	@Unique private BlockItemView bhapi_itemView = new BlockItemView();
	@Unique private ItemStack bhapi_renderingStack;
	
	@Inject(method = "renderItemInGUI", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderItemInGUI(TextRenderer textRenderer, TextureManager manager, int id, int j, int texture, int x, int y, CallbackInfo info) {
		info.cancel();
		if (bhapi_renderingStack == null) return;
		BaseItem item = bhapi_renderingStack.getType();
		if (item == null) return;
		
		if (item instanceof BHBlockItem && !BHBlockItem.cast(item).isFlat()) {
			Textures.getAtlas().bind();
			BlockState state = BHBlockItem.cast(item).getState();
			GL11.glPushMatrix();
			GL11.glTranslatef(x - 2, y + 3, -3.0f);
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
			
			// TODO save only first after all render implementation
			if (BHBlockRenderer.isImplemented(state.getRenderType(bhapi_itemView, 0, 0, 0))) {
				bhapi_itemView.setBlockState(state);
				BHBlockRenderer.setRenderer(bhapi_itemView, this.internalBlockRenderer);
				BHBlockRenderer.renderItem(state, this.coloriseItem, 1.0f);
			}
			else {
				this.internalBlockRenderer.itemColorEnabled = this.coloriseItem;
				this.internalBlockRenderer.renderBlockItem(state.getBlock(), j, 1.0f);
				this.internalBlockRenderer.itemColorEnabled = true;
			}
			
			GL11.glPopMatrix();
		}
		else if (texture >= 0) {
			GL11.glDisable(2896);
			Textures.getAtlas().bind();
			
			if (this.coloriseItem) {
				int color = item.getColorMultiplier(j);
				float r = (float) (color >> 16 & 0xFF) / 255.0F;
				float g = (float) (color >> 8 & 0xFF) / 255.0F;
				float b = (float) (color & 0xFF) / 255.0F;
				GL11.glColor4f(r, g, b, 1.0F);
			}
			
			UVPair uv;
			if (item instanceof BHItemRender) uv = BHItemRender.cast(item).getTexture(bhapi_renderingStack).getUV();
			else uv = Textures.getAtlas().getUV(texture);
			
			bhapi_renderRectangle(x, y, uv);
			GL11.glEnable(2896);
		}
		GL11.glEnable(2884);
	}
	
	@Inject(method = "render(Lnet/minecraft/entity/ItemEntity;DDDFF)V", at = @At("HEAD"), cancellable = true)
	public void bhapi_renderItemEntity(ItemEntity entity, double x, double y, double z, float unused, float delta, CallbackInfo info) {
		info.cancel();
		this.rand.setSeed(187L);
		GL11.glPushMatrix();
		float offset = MathHelper.sin(((float) entity.age + delta) / 10.0f + entity.rotation) * 0.1f + 0.1f;
		float angle = (((float) entity.age + delta) / 20.0f + entity.rotation) * 57.295776f;
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
		if (item instanceof BHBlockItem && !BHBlockItem.cast(item).isFlat()) {
			GL11.glRotatef(angle, 0.0f, 1.0f, 0.0f);
			Textures.getAtlas().bind();
			float scale = 0.25f;
			
			BlockState state = BHBlockItem.cast(item).getState();
			if (!state.getBlock().isFullCube() && !state.is(BaseBlock.STONE_SLAB)) {
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
				
				// TODO save only first after all render implementation
				if (BHBlockRenderer.isImplemented(state.getRenderType(bhapi_itemView, 0, 0, 0))) {
					bhapi_itemView.setBlockState(state);
					BHBlockRenderer.setRenderer(bhapi_itemView, this.internalBlockRenderer);
					BHBlockRenderer.renderItem(state, this.coloriseItem, entity.getBrightnessAtEyes(delta));
				}
				else {
					this.internalBlockRenderer.renderBlockItem(state.getBlock(), entity.stack.getDamage(), entity.getBrightnessAtEyes(delta));
				}
				
				GL11.glPopMatrix();
			}
		}
		else {
			float b, g, r;
			int color;
			GL11.glScalef(0.5f, 0.5f, 0.5f);
			int texture = entity.stack.getTexturePosition();
			Textures.getAtlas().bind();
			
			UVPair uv;
			if (item instanceof BHItemRender) uv = BHItemRender.cast(item).getTexture(entity.stack).getUV();
			else uv = Textures.getAtlas().getUV(texture);
			
			float u1 = uv.getU(0);
			float u2 = uv.getU(1);
			float v1 = uv.getV(0);
			float v2 = uv.getV(1);
			
			if (this.coloriseItem) {
				color = item.getColorMultiplier(entity.stack.getDamage());
				r = (float) (color >> 16 & 0xFF) / 255.0f;
				g = (float) (color >> 8 & 0xFF) / 255.0f;
				b = (float) (color & 0xFF) / 255.0f;
				float light = entity.getBrightnessAtEyes(delta);
				GL11.glColor4f(r * light, g * light, b * light, 1.0f);
			}
			
			for (color = 0; color < count; ++color) {
				GL11.glPushMatrix();
				if (color > 0) {
					r = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					g = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					b = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					GL11.glTranslatef(r, g, b);
				}
				GL11.glRotatef(180.0f - this.dispatcher.angle, 0.0f, 1.0f, 0.0f);
				
				Tessellator tessellator = Tessellator.INSTANCE;
				tessellator.start();
				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				tessellator.vertex(-0.5F, -0.25F, 0.0, u1, v2);
				tessellator.vertex(0.5F, -0.25F, 0.0, u2, v2);
				tessellator.vertex(0.5F, 0.75F, 0.0, u2, v1);
				tessellator.vertex(-0.5F, 0.75F, 0.0, u1, v1);
				tessellator.draw();
				
				GL11.glPopMatrix();
			}
		}
		GL11.glDisable(32826);
		GL11.glPopMatrix();
	}
	
	@Inject(method = "renderStackInGUI", at = @At("HEAD"))
	public void bhapi_renderStackInGUI(TextRenderer textRenderer, TextureManager manager, ItemStack stack, int x, int y, CallbackInfo info) {
		if (stack != null) bhapi_renderingStack = stack;
	}
	
	@Unique
	public void bhapi_renderRectangle(int x, int y, UVPair pair) {
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.start();
		float u1 = pair.getU(0);
		float u2 = pair.getU(1);
		float v1 = pair.getV(0);
		float v2 = pair.getV(1);
		tessellator.vertex(x, y + 16, 0, u1, v2);
		tessellator.vertex(x + 16, y + 16, 0, u2, v2);
		tessellator.vertex(x + 16, y, 0, u2, v1);
		tessellator.vertex(x, y, 0, u1, v1);
		tessellator.draw();
	}
}
