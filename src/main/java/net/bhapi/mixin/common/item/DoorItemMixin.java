package net.bhapi.mixin.common.item;

import net.bhapi.level.BlockStateProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.DoorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MCMath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorItem.class)
public class DoorItemMixin {
	@Shadow private Material material;
	
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_useOnBlock(ItemStack stack, PlayerEntity player, Level level, int x, int y, int z, int facing, CallbackInfoReturnable<Boolean> info) {
		if (facing != 1) {
			info.setReturnValue(false);
			return;
		}
		
		Block block = this.material == Material.WOOD ? Block.WOOD_DOOR : Block.IRON_DOOR;
		if (!block.canPlaceAt(level, x, ++y, z)) {
			info.setReturnValue(false);
			return;
		}
		
		int angle = MCMath.floor(((player.yaw + 180.0f) * 4.0f / 360.0f) - 0.5) & 3;
		int dx = 0;
		int dz = 0;
		
		if (angle == 0) dz = 1;
		if (angle == 1) dx = -1;
		if (angle == 2) dz = -1;
		if (angle == 3) dx = 1;
		
		int n4 = (level.canSuffocate(x - dx, y, z - dz) ? 1 : 0) + (level.canSuffocate(x - dx, y + 1, z - dz) ? 1 : 0);
		int n5 = (level.canSuffocate(x + dx, y, z + dz) ? 1 : 0) + (level.canSuffocate(x + dx, y + 1, z + dz) ? 1 : 0);
		
		BlockStateProvider provider = BlockStateProvider.cast(level);
		boolean testX = provider.bhapi_getBlockState(x - dx, y, z - dz).is(block) || provider.bhapi_getBlockState(x - dx, y + 1, z - dz).is(block);
		boolean testZ = provider.bhapi_getBlockState(x + dx, y, z + dz).is(block) || provider.bhapi_getBlockState(x + dx, y + 1, z + dz).is(block);
		
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
