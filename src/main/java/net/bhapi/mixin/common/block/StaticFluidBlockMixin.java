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
import net.minecraft.block.StaticFluid;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import net.minecraft.util.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StaticFluid.class)
public abstract class StaticFluidBlockMixin extends FluidBlock implements BlockStateContainer {
	@Unique private static final Vec3i[] BHAPI_OFFSETS = new Vec3i[] {
		new Vec3i(-1, 0, 0),
		new Vec3i(1, 0, 0),
		new Vec3i(0, 0, -1),
		new Vec3i(0, 0, 1),
		new Vec3i(0, -1, 0)
	};
	@Unique private BlockState bhapi_flowingFluid;
	
	protected StaticFluidBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	/*@Inject(method = "onAdjacentBlockUpdate", at = @At("HEAD"), cancellable = true)
	private void bhapi_onAdjacentBlockUpdate(Level arg, int i, int j, int k, int l, CallbackInfo info) {
		info.cancel();
		super.onAdjacentBlockUpdate(arg, i, j, k, l);
	}*/
	
	/*@Inject(method = "method_1037", at = @At("HEAD"), cancellable = true)
	private void bhapi_staticToDynamic(Level level, int x, int y, int z, CallbackInfo info) {
		info.cancel();
		
		if (bhapi_flowingFluid == null) {
			Identifier id = DefaultRegistries.BLOCK_REGISTRY.getID(BaseBlock.class.cast(this));
			String name = id.getName().replaceFirst("static", "flowing");
			id = Identifier.make(id.getModID(), name);
			BaseBlock block = DefaultRegistries.BLOCK_REGISTRY.get(id);
			bhapi_flowingFluid = BlockStateContainer.cast(block).getDefaultState();
		}
		
		boolean skipUpdate = true;
		BlockStateProvider provider = BlockStateProvider.cast(level);
		BlockState origin = provider.getBlockState(x, y, z);
		for (Vec3i offset: BHAPI_OFFSETS) {
			BlockState state = provider.getBlockState(x + offset.x, y + offset.y, z + offset.z);
			if (!((FluidLogic) bhapi_flowingFluid.getBlock()).stateBlocksFluid(origin, state)) {
				skipUpdate = false;
				break;
			}
		}
		if (skipUpdate) return;
		
		int meta = level.getBlockMeta(x, y, z);
		StateProperty<?> property = bhapi_flowingFluid.getProperty("meta");
		if (property != null && property.getType() == BlockPropertyType.INTEGER) {
			bhapi_flowingFluid = bhapi_flowingFluid.withCast(property, meta);
		}
		
		level.stopPhysics = true;
		provider.setBlockState(x, y, z, bhapi_flowingFluid, false);
		level.callAreaEvents(x, y, z, x, y, z);
		level.scheduleTick(x, y, z, bhapi_flowingFluid.getBlock().id, this.getTickrate());
		level.stopPhysics = false;
	}*/
}