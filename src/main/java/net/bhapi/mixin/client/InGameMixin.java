package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.gui.DebugAllItems;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.event.TestEvent;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.storage.Vec2F;
import net.bhapi.util.BlockUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.InGame;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.Locale;

@Mixin(InGame.class)
public abstract class InGameMixin extends DrawableHelper {
	@Shadow private Minecraft minecraft;
	
	@Unique private TextureSample bhapi_portalSample;
	
	@Inject(method = "renderHud(FZII)V", at = @At("HEAD"))
	private void bhapi_openItemsGUI(float bl, boolean i, int j, int par4, CallbackInfo ci) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		if (Keyboard.getEventKey() == Keyboard.KEY_G && this.minecraft.currentScreen == null) {
			DebugAllItems inventory = new DebugAllItems(9 * 4);
			ItemStack[] items = inventory.getItems();
			final int[] index = new int[] {0};
			TestEvent.BLOCKS.values().forEach(block -> {
				items[index[0]++] = new ItemStack(block, 64);
			});
			items[index[0]++] = new ItemStack(BaseBlock.CACTUS, 64);
			this.minecraft.player.openChestScreen(inventory);
		}
	}
	
	@Inject(
		method = "renderHud(FZII)V",
		at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;maxMemory()J", shift = Shift.BEFORE),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void bhapi_renderAdditionalInfo(float bl, boolean i, int j, int par4, CallbackInfo info, ScreenScaler scaler, int px, int py, TextRenderer renderer) {
		HitResult hit = minecraft.hitResult;
		int offset = 22;
		String text;
		
		text = "\u00A7eLight";
		drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, offset += 10, 16777215);
		
		int x = MathHelper.floor(minecraft.player.x);
		int y = MathHelper.floor(minecraft.player.y - minecraft.player.standingEyeHeight);
		int z = MathHelper.floor(minecraft.player.z);
		Level level = minecraft.level;
		
		text = "\u00A76Block:\u00A7r " + String.format(Locale.ROOT, "%2d", level.getLight(LightType.BLOCK, x, y, z));
		text += " \u00A7bSky:\u00A7r " + String.format(Locale.ROOT, "%2d", level.getLight(LightType.SKY, x, y, z));
		drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, offset += 10, 16777215);
		offset += 10;
		
		text = "\u00A7dInside";
		drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, offset += 10, 16777215);
		
		offset = bhapi_blockstateInfo(x, y, z, renderer, px, offset);
		
		if (hit != null && hit.type == HitType.BLOCK) {
			text = "\u00A7dHit";
			drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, offset += 20, 16777215);
			bhapi_blockstateInfo(hit.x, hit.y, hit.z, renderer, px, offset);
		}
		
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		
		final int w = 96;
		final int h = 96;
		px = scaler.getScaledWidth() - w;
		py = scaler.getScaledHeight() - h;
		Textures.getAtlas().bind();
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.start();
		tessellator.vertex(px, py + w, this.zOffset, 0, 1);
		tessellator.vertex(px + h, py + w, this.zOffset, 1, 1);
		tessellator.vertex(px + h, py, this.zOffset, 1, 0);
		tessellator.vertex(px, py, this.zOffset, 0, 0);
		tessellator.draw();
	}
	
	@Unique
	private int bhapi_blockstateInfo(int x, int y, int z, TextRenderer renderer, int px, int py) {
		BlockState state = BlockStateProvider.cast(minecraft.level).getBlockState(x, y, z);
		BaseBlockEntity entity = minecraft.level.getBlockEntity(x, y, z);
		
		String text = "\u00A7bBlock:\u00A7r ";
		if (state.is(BlockUtil.AIR_BLOCK)) text += "Air";
		else text += state.getBlock().getTranslatedName();
		drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, py += 10, 16777215);
		
		text = "\u00A7bID:\u00A7r " + CommonRegistries.BLOCK_REGISTRY.getID(state.getBlock());
		drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, py += 10, 16777215);
		
		Collection<StateProperty<?>> properties = state.getProperties();
		if (properties.size() > 0) {
			text = "\u00A7aProperties:";
			drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, py += 10, 16777215);
			
			for (StateProperty<?> property : properties) {
				text = "\u00A7a" + property.getName() + ":\u00A7r " + state.getValue(property);
				drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, py += 10, 14737632);
			}
		}
		
		if (entity != null) {
			String className = entity.getClass().getName();
			text = "Block Entity: " + className.substring(className.lastIndexOf('.') + 1);
			drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, py + 20, 16777215);
		}
		
		return py;
	}
	
	@Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderPortalOverlay(float alpha, int x, int y, CallbackInfo info) {
		info.cancel();
		if (alpha < 1.0f) {
			alpha *= alpha;
			alpha *= alpha;
			alpha = alpha * 0.8f + 0.2f;
		}
		GL11.glDisable(3008);
		GL11.glDisable(2929);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
		Textures.getAtlas().bind();
		if (bhapi_portalSample == null) {
			BlockState state = BlockState.getDefaultState(BaseBlock.PORTAL);
			bhapi_portalSample = state.getTextureForIndex(this.minecraft.level, 0, 0, 0, 0, 0);
		}
		Vec2F uv1 = bhapi_portalSample.getUV(0, 0);
		Vec2F uv2 = bhapi_portalSample.getUV(1, 1);
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.start();
		tessellator.vertex(0.0, y, -90.0, uv1.x, uv2.y);
		tessellator.vertex(x, y, -90.0, uv2.x, uv2.y);
		tessellator.vertex(x, 0.0, -90.0, uv2.x, uv1.y);
		tessellator.vertex(0.0, 0.0, -90.0, uv1.x, uv1.y);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(2929);
		GL11.glEnable(3008);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}
}
