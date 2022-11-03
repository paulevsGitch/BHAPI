package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.gui.DebugAllItems;
import net.bhapi.client.gui.DebugAllItemsScreen;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.registry.DefaultRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.InGame;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;

@Mixin(InGame.class)
public abstract class InGameMixin extends DrawableHelper {
	@Shadow private Minecraft minecraft;
	
	@Inject(method = "renderHud(FZII)V", at = @At("HEAD"))
	private void bhapi_openItemsGUI(float bl, boolean i, int j, int par4, CallbackInfo ci) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		if (Keyboard.getEventKey() == Keyboard.KEY_G && this.minecraft.currentScreen == null) {
			int size = DefaultRegistries.BLOCK_REGISTRY.values().size();
			size = (int) Math.ceil(size / 18F) * 18;
			DebugAllItems inventory = new DebugAllItems(size);
			ItemStack[] items = inventory.getItems();
			int[] index = new int[1];
			DefaultRegistries.BLOCK_REGISTRY.forEach(block -> {
				ItemStack stack = new ItemStack(block);
				if (stack.getType() != null) {
					items[index[0]++] = stack;
				}
			});
			this.minecraft.openScreen(new DebugAllItemsScreen(this.minecraft.player.inventory, inventory));
		}
	}
	
	@Inject(
		method = "renderHud(FZII)V",
		at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;maxMemory()J", shift = Shift.BEFORE),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void bhapi_renderBlockInfo(float bl, boolean i, int j, int par4, CallbackInfo ci, ScreenScaler scaler, int var6, int var7, TextRenderer var8) {
		HitResult hit = minecraft.hitResult;
		int offset = 22;
		if (hit != null && hit.type == HitType.BLOCK) {
			BlockState state = BlockStateProvider.cast(minecraft.level).getBlockState(hit.x, hit.y, hit.z);
			
			String text = "\u00A7bBlock:\u00A7r " + state.getBlock().getTranslatedName();
			drawTextWithShadow(var8, text, var6 - var8.getTextWidth(text) - 2, offset += 10, 16777215);
			
			text = "\u00A7bID:\u00A7r " + DefaultRegistries.BLOCK_REGISTRY.getID(state.getBlock());
			drawTextWithShadow(var8, text, var6 - var8.getTextWidth(text) - 2, offset += 10, 16777215);
			
			Collection<StateProperty<?>> properties = state.getProperties();
			if (properties.size() > 0) {
				text = "\u00A7aProperties:";
				drawTextWithShadow(var8, text, var6 - var8.getTextWidth(text) - 2, offset += 10, 16777215);
				
				for (StateProperty<?> property : properties) {
					text = "\u00A7a" + property.getName() + ":\u00A7r " + state.getValue(property);
					drawTextWithShadow(var8, text, var6 - var8.getTextWidth(text) - 2, offset += 10, 14737632);
				}
			}
			
			if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
				BaseBlockEntity entity = minecraft.level.getBlockEntity(hit.x, hit.y, hit.z);
				if (entity != null) {
					String className = entity.getClass().getName();
					text = "Block Entity: " + className.substring(className.lastIndexOf('.') + 1);
					drawTextWithShadow(var8, text, var6 - var8.getTextWidth(text) - 2, offset + 20, 16777215);
				}
			}
		}
	}
}
