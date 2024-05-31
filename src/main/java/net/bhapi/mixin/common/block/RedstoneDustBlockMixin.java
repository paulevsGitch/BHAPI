package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.interfaces.ClientPostInit;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.RedstoneDustBlock;
import net.minecraft.block.technical.BedData;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RedstoneDustBlock.class)
public abstract class RedstoneDustBlockMixin implements BlockStateContainer, ClientPostInit, BHBlockRender {
	@Environment(EnvType.CLIENT)
	private static final TextureSample[] BHAPI_SAMPLES = new TextureSample[2];
	
	@Override
	public void bhapi_appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void afterClientInit() {
		if (BHAPI_SAMPLES[0] != null) return;
		BHAPI_SAMPLES[0] = Textures.getAtlas().getSample(Identifier.make("block/redstone_dust_dot"));
		BHAPI_SAMPLES[1] = Textures.getAtlas().getSample(Identifier.make("block/redstone_dust_overlay"));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample bhapi_getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		return BHAPI_SAMPLES[textureIndex & 1];
	}
	
	@Inject(method = "canConnect", at = @At("HEAD"), cancellable = true)
	private static void bhapi_canConnect(BlockView view, int x, int y, int z, int facing, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(view).bhapi_getBlockState(x, y, z);
		
		if (state.isAir()) {
			info.setReturnValue(false);
			return;
		}
		
		if (state.getBlock() instanceof RedstoneDustBlock) {
			info.setReturnValue(true);
			return;
		}
		
		if (state.emitsPower()) {
			info.setReturnValue(true);
			return;
		}
		
		if (state.is(Block.REDSTONE_REPEATER) || state.is(Block.REDSTONE_REPEATER_LIT)) {
			int meta = state.getMeta();
			info.setReturnValue(facing == BedData.sidesVisibilityB[meta & 3]);
			return;
		}
		
		info.setReturnValue(false);
	}
}

