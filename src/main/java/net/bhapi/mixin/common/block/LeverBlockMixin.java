package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.interfaces.ClientPostInit;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LeverBlock;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(LeverBlock.class)
public abstract class LeverBlockMixin implements BlockStateContainer, ClientPostInit, BHBlockRender {
	@Environment(EnvType.CLIENT)
	private static final TextureSample[] BHAPI_SAMPLES = new TextureSample[2];
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void afterClientInit() {
		if (BHAPI_SAMPLES[0] != null) return;
		BHAPI_SAMPLES[0] = Textures.getAtlas().getSample(Identifier.make("block/lever"));
		BHAPI_SAMPLES[1] = Textures.getAtlas().getSample(Identifier.make("block/cobblestone"));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
		if (view instanceof BlockItemView) return BHAPI_SAMPLES[0];
		return BHAPI_SAMPLES[index < 6 ? 1 : 0];
	}
}
