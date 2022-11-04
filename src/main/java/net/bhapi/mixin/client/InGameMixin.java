package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.gui.DebugAllItems;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.registry.DefaultRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.InGame;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.inventory.Chest;
import net.minecraft.item.ItemStack;
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
import java.util.Comparator;
import java.util.List;

@Mixin(InGame.class)
public abstract class InGameMixin extends DrawableHelper {
	@Shadow private Minecraft minecraft;
	
	@Inject(method = "renderHud(FZII)V", at = @At("HEAD"))
	private void bhapi_openItemsGUI(float bl, boolean i, int j, int par4, CallbackInfo ci) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		if (Keyboard.getEventKey() == Keyboard.KEY_G && this.minecraft.currentScreen == null) {
			int size = DefaultRegistries.BLOCK_REGISTRY.values().size();
			size = (int) Math.ceil(size / 18F) * 18;
			/*DebugAllItems inventory = new DebugAllItems(size);
			ItemStack[] items = inventory.getItems();
			int[] index = new int[1];
			DefaultRegistries.BLOCK_REGISTRY
				.values()
				.stream()
				.sorted(Comparator.comparing(DefaultRegistries.BLOCK_REGISTRY::getID))
				.forEach(block -> {
					ItemStack stack = new ItemStack(block);
					if (stack.getType() != null) {
						items[index[0]++] = stack;
					}
				});
			this.minecraft.openScreen(new DebugAllItemsScreen(this.minecraft.player.inventory, inventory));*/
			
			DebugAllItems inventory = new DebugAllItems(9 * 3);
			ItemStack[] items = inventory.getItems();
			items[0] = new ItemStack(BaseBlock.STILL_WATER, 64);
			items[1] = new ItemStack(BaseBlock.STONE, 64);
			this.minecraft.player.openChestScreen(inventory);
		}
	}
	
	@Inject(
		method = "renderHud(FZII)V",
		at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;maxMemory()J", shift = Shift.BEFORE),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void bhapi_renderBlockInfo(float bl, boolean i, int j, int par4, CallbackInfo info, ScreenScaler scaler, int px, int py, TextRenderer renderer) {
		HitResult hit = minecraft.hitResult;
		int offset = 22;
		String text;
		
		text = "\u00A7dInside";
		drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, offset += 10, 16777215);
		
		int x = MathHelper.floor(minecraft.player.x);
		int y = MathHelper.floor(minecraft.player.y - minecraft.player.standingEyeHeight);
		int z = MathHelper.floor(minecraft.player.z);
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
		
		String text = "\u00A7bBlock:\u00A7r " + state.getBlock().getTranslatedName();
		drawTextWithShadow(renderer, text, px - renderer.getTextWidth(text) - 2, py += 10, 16777215);
		
		text = "\u00A7bID:\u00A7r " + DefaultRegistries.BLOCK_REGISTRY.getID(state.getBlock());
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
