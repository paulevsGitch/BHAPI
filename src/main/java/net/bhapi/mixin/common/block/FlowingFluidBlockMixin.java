package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.BlockPropertyType;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.interfaces.FluidLogic;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.registry.DefaultRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(FlowingFluidBlock.class)
public abstract class FlowingFluidBlockMixin implements BlockStateContainer, FluidLogic {
	@Unique	private BlockState bhapi_staticFluid;
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	/*@Inject(method = "blocksFluid", at = @At("HEAD"), cancellable = true)
	private void bhapi_blocksFluid(Level level, int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		BlockStateProvider provider = BlockStateProvider.cast(level);
		BlockState state = provider.getBlockState(x, y, z);
		info.setReturnValue(stateBlocksFluid(getDefaultState(), state));
	}
	
	@Inject(method = "setBlockWithUpdate", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlockWithUpdate(Level level, int x, int y, int z, CallbackInfo info) {
		info.cancel();
		
		if (bhapi_staticFluid == null) {
			Identifier id = DefaultRegistries.BLOCK_REGISTRY.getID(BaseBlock.class.cast(this));
			String name = id.getName().replaceFirst("flowing", "static");
			id = Identifier.make(id.getModID(), name);
			BaseBlock block = DefaultRegistries.BLOCK_REGISTRY.get(id);
			bhapi_staticFluid = BlockStateContainer.cast(block).getDefaultState();
		}
		
		int meta = level.getBlockMeta(x, y, z);
		StateProperty<?> property = bhapi_staticFluid.getProperty("meta");
		if (property != null && property.getType() == BlockPropertyType.INTEGER) {
			bhapi_staticFluid = bhapi_staticFluid.withCast(property, meta);
		}
		
		BlockStateProvider provider = BlockStateProvider.cast(level);
		provider.setBlockState(x, y, z, bhapi_staticFluid);
		level.callAreaEvents(x, y, z, x, y, z);
		level.updateListenersLight(x, y, z);
	}
	
	@Unique
	@Override
	public boolean stateBlocksFluid(BlockState selfState, BlockState blockingState) {
		if (blockingState.isAir()) {
			return false;
		}
		
		if (blockingState.getBlock() instanceof FluidBlock) {
			int meta1 = selfState.getValue(LegacyProperties.META_16) - 1;
			int meta2 = blockingState.getValue(LegacyProperties.META_16);
			return meta1 < meta2;
		}
		
		if (
			blockingState.is(BaseBlock.WOOD_DOOR) ||
			blockingState.is(BaseBlock.IRON_DOOR) ||
			blockingState.is(BaseBlock.STANDING_SIGN) ||
			blockingState.is(BaseBlock.LADDER) ||
			blockingState.is(BaseBlock.SUGAR_CANES)
		) {
			return true;
		}
		
		return blockingState.getBlock().material.blocksMovement();
	}*/
}