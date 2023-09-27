package net.bhapi.mixin.client;

import net.bhapi.item.BHBlockItem;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.entity.model.BipedModel;
import net.minecraft.client.render.entity.model.EntityModelBase;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer {
	@Shadow private BipedModel playerModel;
	
	public PlayerRendererMixin(EntityModelBase arg, float f) {
		super(arg, f);
	}
	
	@Inject(method = "method_342", at = @At("HEAD"), cancellable = true)
	protected void bhapi_renderPlayerAdditions(PlayerBase player, float delta, CallbackInfo info) {
		info.cancel();
		
		float scale;
		ItemStack itemStack = player.inventory.getArmorItem(3);
		
		if (itemStack != null && itemStack.getType() instanceof BHBlockItem) {
			GL11.glPushMatrix();
			this.playerModel.head.method_1820(0.0625f);
			if (BHBlockItem.cast(itemStack.getType()).isFlat()) {
				float f3 = 0.625f;
				GL11.glTranslatef(0.0f, -0.25f, 0.0f);
				GL11.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
				GL11.glScalef(f3, -f3, f3);
			}
			this.dispatcher.overlayRenderer.renderHand(player, itemStack);
			GL11.glPopMatrix();
		}
		
		if (player.name.equals("deadmau5") && this.tryBindTexture(player.skinUrl, null)) {
			for (int i = 0; i < 2; ++i) {
				scale = player.prevYaw + (player.yaw - player.prevYaw) * delta - (player.field_1013 + (player.field_1012 - player.field_1013) * delta);
				float f4 = player.prevPitch + (player.pitch - player.prevPitch) * delta;
				GL11.glPushMatrix();
				GL11.glRotatef(scale, 0.0f, 1.0f, 0.0f);
				GL11.glRotatef(f4, 1.0f, 0.0f, 0.0f);
				GL11.glTranslatef(0.375f * (float)(i * 2 - 1), 0.0f, 0.0f);
				GL11.glTranslatef(0.0f, -0.375f, 0.0f);
				GL11.glRotatef(-f4, 1.0f, 0.0f, 0.0f);
				GL11.glRotatef(-scale, 0.0f, 1.0f, 0.0f);
				float f5 = 1.3333334f;
				GL11.glScalef(f5, f5, f5);
				this.playerModel.renderDeadMau5Ears(0.0625f);
				GL11.glPopMatrix();
			}
		}
		
		if (this.tryBindTexture(player.playerCloakUrl, null)) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0f, 0.0f, 0.125f);
			double d = player.preX + (player.newX - player.preX) * (double)delta - (player.prevX + (player.x - player.prevX) * (double)delta);
			double d2 = player.preY + (player.newY - player.preY) * (double)delta - (player.prevY + (player.y - player.prevY) * (double)delta);
			double d3 = player.preZ + (player.newZ - player.preZ) * (double)delta - (player.prevZ + (player.z - player.prevZ) * (double)delta);
			float f6 = player.field_1013 + (player.field_1012 - player.field_1013) * delta;
			double d4 = MathHelper.sin(f6 * (float)Math.PI / 180.0f);
			double d5 = -MathHelper.cos(f6 * (float)Math.PI / 180.0f);
			float f7 = (float)d2 * 10.0f;
			if (f7 < -6.0f) {
				f7 = -6.0f;
			}
			if (f7 > 32.0f) {
				f7 = 32.0f;
			}
			float f8 = (float)(d * d4 + d3 * d5) * 100.0f;
			float f9 = (float)(d * d5 - d3 * d4) * 100.0f;
			if (f8 < 0.0f) {
				f8 = 0.0f;
			}
			float f10 = player.field_524 + (player.field_525 - player.field_524) * delta;
			f7 += MathHelper.sin((player.field_1634 + (player.field_1635 - player.field_1634) * delta) * 6.0f) * 32.0f * f10;
			if (player.isChild()) {
				f7 += 25.0f;
			}
			GL11.glRotatef(6.0f + f8 / 2.0f + f7, 1.0f, 0.0f, 0.0f);
			GL11.glRotatef(f9 / 2.0f, 0.0f, 0.0f, 1.0f);
			GL11.glRotatef(-f9 / 2.0f, 0.0f, 1.0f, 0.0f);
			GL11.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
			this.playerModel.renderCloak(0.0625f);
			GL11.glPopMatrix();
		}
		
		ItemStack handItem = player.inventory.getHeldItem();
		if (handItem != null) {
			GL11.glPushMatrix();
			this.playerModel.rightArm.method_1820(0.0625f);
			GL11.glTranslatef(-0.0625f, 0.4375f, 0.0625f);
			if (player.fishHook != null) {
				handItem = new ItemStack(BaseItem.stick);
			}
			BaseItem item = handItem.getType();
			// TODO make 2D item models
			if (item instanceof BHBlockItem) {
				scale = 0.5f;
				GL11.glTranslatef(0.0f, 0.1875f, -0.3125f);
				GL11.glRotatef(20.0f, 1.0f, 0.0f, 0.0f);
				GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
				GL11.glScalef(scale *= 0.75f, -scale, scale);
			}
			else if (item.isRendered3d()) {
				scale = 0.625f;
				if (item.shouldSpinWhenRendering()) {
					GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
					GL11.glTranslatef(0.0f, -0.125f, 0.0f);
				}
				GL11.glTranslatef(0.0f, 0.1875f, 0.0f);
				GL11.glScalef(scale, -scale, scale);
				GL11.glRotatef(-100.0f, 1.0f, 0.0f, 0.0f);
				GL11.glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
			}
			else {
				scale = 0.375f;
				GL11.glTranslatef(0.25f, 0.1875f, -0.1875f);
				GL11.glScalef(scale, scale, scale);
				GL11.glRotatef(60.0f, 0.0f, 0.0f, 1.0f);
				GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
				GL11.glRotatef(20.0f, 0.0f, 0.0f, 1.0f);
			}
			this.dispatcher.overlayRenderer.renderHand(player, handItem);
			GL11.glPopMatrix();
		}
	}
}
