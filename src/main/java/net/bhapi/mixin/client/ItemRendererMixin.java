package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.BHItemRender;
import net.bhapi.storage.Vec2F;
import net.bhapi.util.BufferUtil;
import net.bhapi.util.ColorUtil;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextRenderer;
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

import java.nio.FloatBuffer;
import java.util.Random;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin extends EntityRenderer {
	@Shadow private Random rand;
	
	@Unique private final BlockItemView bhapi_itemView = new BlockItemView();
	@Unique private final FloatBuffer bhapi_buffer = BufferUtil.createFloatBuffer(4);
	@Unique private ItemStack bhapi_renderingStack;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onInit(CallbackInfo info) {
		bhapi_buffer.put(1.0F);
		bhapi_buffer.put(0.8F);
		bhapi_buffer.put(0.6F);
		bhapi_buffer.position(0);
	}
	
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
			GL11.glTranslatef(x - 2, y + 3, 0.0f);
			GL11.glScalef(10.0f, 10.0f, 10.0f);
			GL11.glTranslatef(1.0f, 0.5f, 1.0f);
			GL11.glScalef(1.0f, 1.0f, -1.0f);
			GL11.glRotatef(210.0f, 1.0f, 0.0f, 0.0f);
			GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
			
			RenderHelper.disableLighting();
			BHBlockRenderer renderer = BHAPIClient.getBlockRenderer();
			bhapi_itemView.setBlockState(state);
			renderer.setView(bhapi_itemView);
			renderer.renderItem(state, 1.0f);
			
			GL11.glPopMatrix();
		}
		else if (texture >= 0) {
			GL11.glDisable(0xb50);
			Textures.getAtlas().bind();
			
			TextureSample sample = BHItemRender.cast(item).getTexture(bhapi_renderingStack);
			int color = sample.getColorMultiplier(bhapi_itemView, 0, 0, 0, null);
			float r = ColorUtil.getRed(color);
			float g = ColorUtil.getGreen(color);
			float b = ColorUtil.getBlue(color);
			GL11.glColor4f(r, g, b, 1.0F);
			
			bhapi_renderRectangle(x, y, sample);
			GL11.glEnable(0xb50);
		}
		GL11.glEnable(0xb44);
	}
	
	@Inject(method = "render(Lnet/minecraft/entity/ItemEntity;DDDFF)V", at = @At("HEAD"), cancellable = true)
	public void bhapi_renderItemEntity(ItemEntity entity, double x, double y, double z, float unused, float delta, CallbackInfo info) {
		info.cancel();
		this.rand.setSeed(187L);
		GL11.glPushMatrix();
		float offset = MathHelper.sin(((float) entity.age + delta) / 10.0f + entity.rotation) * 0.1f + 0.1f;
		float angle = (((float) entity.age + delta) / 20.0f + entity.rotation) * 57.295776f;
		int count = 1;
		if (entity.stack.count > 20) count = 4;
		else if (entity.stack.count > 5) count = 3;
		else if (entity.stack.count > 1) count = 2;
		GL11.glTranslatef((float) x, (float) y + offset, (float) z);
		GL11.glEnable(32826);
		
		BaseItem item = entity.stack.getType();
		if (item instanceof BHBlockItem && !BHBlockItem.cast(item).isFlat()) {
			GL11.glRotatef(angle, 0.0f, 1.0f, 0.0f);
			Textures.getAtlas().bind();
			
			BlockState state = BHBlockItem.cast(item).getState();
			
			GL11.glScalef(0.25f, 0.25f, 0.25f);
			for (int i = 0; i < count; ++i) {
				GL11.glPushMatrix();
				if (i > 0) {
					float dx = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.2f / 0.25f;
					float dy = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.2f / 0.25f;
					float dz = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.2f / 0.25f;
					GL11.glTranslatef(dx, dy, dz);
				}
				
				BHBlockRenderer renderer = BHAPIClient.getBlockRenderer();
				bhapi_itemView.setBlockState(state);
				renderer.setView(bhapi_itemView);
				renderer.renderItem(state, entity.getBrightnessAtEyes(delta));
				
				GL11.glPopMatrix();
			}
		}
		else {
			GL11.glScalef(0.5f, 0.5f, 0.5f);
			Textures.getAtlas().bind();
			
			TextureSample sample = BHItemRender.cast(item).getTexture(entity.stack);
			
			float light = entity.getBrightnessAtEyes(delta);
			float r = light;
			float g = light;
			float b = light;
			int color = sample.getColorMultiplier(bhapi_itemView, 0, 0, 0, null);
			if (color != ColorUtil.WHITE_COLOR) {
				r *= ColorUtil.getRed(color);
				g *= ColorUtil.getGreen(color);
				b *= ColorUtil.getBlue(color);
			}
			GL11.glColor4f(r, g, b, 1.0F);
			
			for (color = 0; color < count; ++color) {
				GL11.glPushMatrix();
				if (color > 0) {
					r = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					g = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					b = (this.rand.nextFloat() * 2.0f - 1.0f) * 0.3f;
					GL11.glTranslatef(r, g, b);
				}
				GL11.glRotatef(180.0f - this.dispatcher.angle, 0.0f, 1.0f, 0.0f);
				
				Vec2F u1v1 = sample.getUV(0, 0);
				Vec2F u1v2 = sample.getUV(0, 1);
				Vec2F u2v1 = sample.getUV(1, 0);
				Vec2F u2v2 = sample.getUV(1, 1);
				
				Tessellator tessellator = Tessellator.INSTANCE;
				tessellator.start();
				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				tessellator.vertex(-0.5F, -0.25F, 0.0, u1v2.x, u1v2.y);
				tessellator.vertex(0.5F, -0.25F, 0.0, u2v2.x, u2v2.y);
				tessellator.vertex(0.5F, 0.75F, 0.0, u2v1.x, u2v1.y);
				tessellator.vertex(-0.5F, 0.75F, 0.0, u1v1.x, u1v1.y);
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
	public void bhapi_renderRectangle(int x, int y, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.start();
		Vec2F uv1 = sample.getUV(0, 0);
		Vec2F uv2 = sample.getUV(1, 1);
		tessellator.vertex(x, y + 16, 0, uv1.x, uv2.y);
		tessellator.vertex(x + 16, y + 16, 0, uv2.x, uv2.y);
		tessellator.vertex(x + 16, y, 0, uv2.x, uv1.y);
		tessellator.vertex(x, y, 0, uv1.x, uv1.y);
		tessellator.draw();
	}
}
