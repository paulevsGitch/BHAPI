package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.texture.Textures;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.blockentity.PistonRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonRenderer.class)
public class PistonRendererMixin {
	@Unique private BlockState bhapi_headState;
	
	@Inject(method = "render(Lnet/minecraft/block/entity/PistonBlockEntity;DDDF)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_render(PistonBlockEntity entity, double x, double y, double z, float delta, CallbackInfo info) {
		info.cancel();
		BlockState state = BlockStateContainer.cast(entity).getDefaultState();//CommonRegistries.BLOCKSTATES_MAP.get(entity.getBlockID());
		if (state == null) return;
		BaseBlock block = state.getBlock();
		if (entity.getProgress(delta) < 1.0f) {
			Tessellator tessellator = Tessellator.INSTANCE;
			Textures.getAtlas().bind();
			
			RenderHelper.disableLighting();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			
			if (Minecraft.isSmoothLightingEnabled()) {
				GL11.glShadeModel(GL11.GL_SMOOTH);
			}
			else {
				GL11.glShadeModel(GL11.GL_FLAT);
			}
			
			tessellator.start();
			tessellator.setOffset(x - entity.x + entity.getOffsetX(delta), y - entity.y + entity.getOffsetY(delta), z - entity.z + entity.getOffsetZ(delta));
			tessellator.color(1, 1, 1);
			
			BHBlockRenderer renderer = BHAPIClient.getBlockRenderer();
			renderer.setView(entity.level);
			renderer.startArea(0, 0, 0);
			
			if (block == BaseBlock.PISTON_HEAD && entity.getProgress(delta) < 0.5f) {
				renderer.renderPistonHeadAllSides(state, entity.x, entity.y, entity.z, false);
			}
			else if (entity.canRender() && !entity.isExtended() && block instanceof PistonBlock) {
				BaseBlock.PISTON_HEAD.setTexture(((PistonBlock) block).getPistonTexture());
				if (bhapi_headState == null) {
					bhapi_headState = BlockState.getDefaultState(BaseBlock.PISTON_HEAD);
				}
				bhapi_headState = bhapi_headState.withMeta(entity.getFacing());
				renderer.renderPistonHeadAllSides(bhapi_headState, entity.x, entity.y, entity.z, entity.getProgress(delta) < 0.5f);
				BaseBlock.PISTON_HEAD.resetTexture();
				renderer.build(tessellator);
				renderer.startArea(0, 0, 0);
				tessellator.setOffset(x - entity.x, y - entity.y, z - entity.z);
				renderer.renderPistonExtended(state, entity.x, entity.y, entity.z);
			}
			else {
				renderer.renderAllSides(state, entity.x, entity.y, entity.z);
			}
			
			renderer.build(tessellator);
			tessellator.setOffset(0.0, 0.0, 0.0);
			tessellator.draw();
			RenderHelper.enableLighting();
		}
	}
}
