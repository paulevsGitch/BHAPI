package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.color.VanillaColorProviders;
import net.bhapi.level.BlockStateProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.render.block.FoliageColor;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin implements BlockStateContainer {
	@Override
	public void bhapi_appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16); // Leaves use meta | 8 for updating states.
	}
	
	@Environment(value= EnvType.CLIENT)
	@Inject(method = "getColorMultiplier", at = @At("HEAD"), cancellable = true)
	private void bhapi_getColorMultiplier(BlockView view, int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		BlockState state = BlockStateProvider.cast(view).bhapi_getBlockState(x, y, z);
		int meta = state.getMeta();
		
		if ((meta & 1) == 1) {
			info.setReturnValue(FoliageColor.getSpruceColor());
			return;
		}
		if ((meta & 2) == 2) {
			info.setReturnValue(FoliageColor.getBirchColor());
			return;
		}
		
		info.setReturnValue(VanillaColorProviders.GRASS_BLOCK_COLOR.getColorMultiplier(view, x, y, z, state));
	}
}
