package net.bhapi.mixin.client;

import net.bhapi.client.render.block.BlockBreakingInfo;
import net.bhapi.client.render.block.BreakInfo;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.client.render.blockentity.BlockEntityRenderer;
import net.minecraft.client.render.entity.BlockEntityRenderDispatcher;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
	@Inject(method = "renderBlockEntity(Lnet/minecraft/block/entity/BaseBlockEntity;DDDF)V", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/blockentity/BlockEntityRenderer;render(Lnet/minecraft/block/entity/BaseBlockEntity;DDDF)V",
		shift = Shift.AFTER
	), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void bhapi_renderBlockEntity(BaseBlockEntity entity, double x, double y, double z, float delta, CallbackInfo info, BlockEntityRenderer renderer) {
		if (BreakInfo.stage == -1) return;
		if (BreakInfo.POS.x != entity.x || BreakInfo.POS.y != entity.y || BreakInfo.POS.z != entity.z) return;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(774, 768);
		
		GL11.glPolygonOffset(-3.0f, -3.0f);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		
		BlockBreakingInfo.cast(renderer).setBreaking(BreakInfo.stage);
		renderer.render(entity, x, y, z, delta);
		BlockBreakingInfo.cast(renderer).setBreaking(-1);
		
		GL11.glPolygonOffset(0.0f, 0.0f);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		
		GL11.glDisable(GL11.GL_BLEND);
	}
}
