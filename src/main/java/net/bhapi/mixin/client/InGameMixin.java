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
import net.minecraft.item.BaseItem;
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

@Mixin(InGame.class)
public abstract class InGameMixin extends DrawableHelper {
	@Shadow private Minecraft minecraft;
	
	@Inject(method = "renderHud(FZII)V", at = @At("HEAD"))
	private void bhapi_openItemsGUI(float bl, boolean i, int j, int par4, CallbackInfo ci) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		if (Keyboard.getEventKey() == Keyboard.KEY_G && this.minecraft.currentScreen == null) {
			DebugAllItems inventory = new DebugAllItems(9 * 4);
			ItemStack[] items = inventory.getItems();
			int index = 0;
			items[index++] = new ItemStack(BaseItem.bed, 64);
			items[index++] = new ItemStack(BaseBlock.BUTTON, 64);
			items[index++] = new ItemStack(BaseItem.cake, 64);
			for (byte m = 0; m < 7; m++) items[index++] = new ItemStack(BaseBlock.CROPS, 64, m);
			items[index++] = new ItemStack(BaseBlock.DISPENSER, 64);
			items[index++] = new ItemStack(BaseItem.woodDoor, 64);
			items[index++] = new ItemStack(BaseItem.ironDoor, 64);
			items[index++] = new ItemStack(BaseBlock.FARMLAND, 64);
			items[index++] = new ItemStack(BaseBlock.FURNACE, 64);
			items[index++] = new ItemStack(BaseBlock.LADDER, 64);
			items[index++] = new ItemStack(BaseBlock.WOODEN_PRESSURE_PLATE, 64);
			items[index++] = new ItemStack(BaseBlock.STONE_PRESSURE_PLATE, 64);
			items[index++] = new ItemStack(BaseBlock.RAIL, 64);
			items[index++] = new ItemStack(BaseBlock.WOOD_STAIRS, 64);
			for (byte m = 0; m < 3; m++) items[index++] = new ItemStack(BaseBlock.TALLGRASS, 64, m);
			for (byte m = 0; m < 7; m++) items[index++] = new ItemStack(BaseBlock.WOOL, 64, m);
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
