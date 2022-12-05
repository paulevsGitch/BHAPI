package net.bhapi.mixin.common.item;

import net.bhapi.level.BlockStateProvider;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.DoorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorItem.class)
public class DoorItemMixin {
	@Shadow private Material material;
	
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_useOnBlock(ItemStack stack, PlayerBase player, Level level, int x, int y, int z, int facing, CallbackInfoReturnable<Boolean> info) {
		if (facing != 1) {
			info.setReturnValue(false);
			return;
		}
		
		BaseBlock block = this.material == Material.WOOD ? BaseBlock.WOOD_DOOR : BaseBlock.IRON_DOOR;
		System.out.println(block.canPlaceAt(level, x, y + 1, z));
		System.out.println(BlockStateProvider.cast(level).getBlockState(x, y + 1, z));
		if (!block.canPlaceAt(level, x, ++y, z)) {
			info.setReturnValue(false);
			return;
		}
		
		int angle = MathHelper.floor(((player.yaw + 180.0f) * 4.0f / 360.0f) - 0.5) & 3;
		int dx = 0;
		int dz = 0;
		
		if (angle == 0) dz = 1;
		if (angle == 1) dx = -1;
		if (angle == 2) dz = -1;
		if (angle == 3) dx = 1;
		
		int n4 = (level.canSuffocate(x - dx, y, z - dz) ? 1 : 0) + (level.canSuffocate(x - dx, y + 1, z - dz) ? 1 : 0);
		int n5 = (level.canSuffocate(x + dx, y, z + dz) ? 1 : 0) + (level.canSuffocate(x + dx, y + 1, z + dz) ? 1 : 0);
		
		BlockStateProvider provider = BlockStateProvider.cast(level);
		boolean testX = provider.getBlockState(x - dx, y, z - dz).is(block) || provider.getBlockState(x - dx, y + 1, z - dz).is(block);
		boolean testZ = provider.getBlockState(x + dx, y, z + dz).is(block) || provider.getBlockState(x + dx, y + 1, z + dz).is(block);
		
		boolean mirror = false;
		if (testX && !testZ) mirror = true;
		else if (n5 > n4) mirror = true;
		
		if (mirror) {
			angle = angle - 1 & 3;
			angle += 4;
		}
		
		level.stopPhysics = true;
		level.setBlock(x, y, z, block.id, angle);
		level.setBlock(x, y + 1, z, block.id, angle + 8);
		level.stopPhysics = false;
		
		level.updateAdjacentBlocks(x, y, z, block.id);
		level.updateAdjacentBlocks(x, y + 1, z, block.id);
		--stack.count;
		
		info.setReturnValue(true);
	}
}
