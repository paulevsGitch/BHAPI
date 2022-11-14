package net.bhapi.mixin.client;

import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.minecraft.block.BaseBlock;
import net.minecraft.client.render.block.BlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin {
	private BaseBlock bhapi_block;
	private UVPair bhapi_uv;
	
	@Inject(method = "renderTopFace", at = @At("HEAD"))
	private void bhapi_renderTopFace(BaseBlock block, double e, double f, double i, int id, CallbackInfo info) {
		bhapi_uv = Textures.getAtlas().getUV(id);
		bhapi_block = block;
	}
	
	@ModifyVariable(method = "renderTopFace", at = @At(value = "STORE"), index = 12, ordinal = 3)
	private double bhapi_topFaceU1(double val) { return bhapi_uv.getU((float) bhapi_block.minX); }
	
	@ModifyVariable(method = "renderTopFace", at = @At(value = "STORE"), index = 14, ordinal = 4)
	private double bhapi_topFaceU2(double val) { return bhapi_uv.getU((float) bhapi_block.maxX); }
	
	@ModifyVariable(method = "renderTopFace", at = @At(value = "STORE"), index = 16, ordinal = 5)
	private double bhapi_topFaceV1(double val) { return bhapi_uv.getV((float) bhapi_block.minZ); }
	
	@ModifyVariable(method = "renderTopFace", at = @At(value = "STORE"), index = 18, ordinal = 6)
	private double bhapi_topFaceV2(double val) { return bhapi_uv.getV((float) bhapi_block.maxZ); }
}
