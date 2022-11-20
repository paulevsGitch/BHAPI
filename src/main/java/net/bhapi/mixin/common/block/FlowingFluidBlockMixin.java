package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.interfaces.ClientPostInit;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(FlowingFluidBlock.class)
public abstract class FlowingFluidBlockMixin implements BlockStateContainer, ClientPostInit, BHBlockRender {
	private static final TextureSample[] BHAPI_SAMPLES = new TextureSample[4];
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
		index = index > 1 ? 1 : 0;
		if (FlowingFluidBlock.class.cast(this).material == Material.LAVA) index += 2;
		return BHAPI_SAMPLES[index];
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void afterClientInit() {
		if (BHAPI_SAMPLES[0] != null) return;
		BHAPI_SAMPLES[0] = Textures.getAtlas().getSample(Identifier.make("block/water_still"));
		BHAPI_SAMPLES[1] = Textures.getAtlas().getSample(Identifier.make("block/water_flow"));
		BHAPI_SAMPLES[2] = Textures.getAtlas().getSample(Identifier.make("block/lava_still"));
		BHAPI_SAMPLES[3] = Textures.getAtlas().getSample(Identifier.make("block/lava_flow"));
	}
}