package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.gui.DebugAllItems;
import net.bhapi.event.TestEvent;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.Identifier;
import net.bhapi.util.ItemUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.InGame;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.maths.MathHelper;
import org.lwjgl.input.Keyboard;
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
	
	@Inject(method = "renderHud(FZII)V", at = @At("HEAD"))
	private void bhapi_openItemsGUI(float bl, boolean i, int j, int par4, CallbackInfo ci) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		if (Keyboard.getEventKey() == Keyboard.KEY_G && this.minecraft.currentScreen == null) {
			DebugAllItems inventory = new DebugAllItems(9 * 4);
			ItemStack[] items = inventory.getItems();
			final int[] index = new int[] {0};
			items[index[0]++] = ItemUtil.makeStack(Identifier.make("iron_pickaxe"));
			items[index[0]++] = ItemUtil.makeStack(Identifier.make("iron_shovel"));
			items[index[0]++] = ItemUtil.makeStack(Identifier.make("iron_axe"));
			TestEvent.BLOCKS.keySet().forEach(id -> {
				ItemStack stack = new ItemStack(CommonRegistries.ITEM_REGISTRY.get(id), 64);
				items[index[0]++] = stack;
			});
			for (byte m = 0; m < 3; m++) items[index[0]++] = new ItemStack(BaseBlock.SAPLING, 64, m);
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
}
