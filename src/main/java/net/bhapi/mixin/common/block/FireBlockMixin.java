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
import net.minecraft.block.FireBlock;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin implements BlockStateContainer, ClientPostInit, BHBlockRender {
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
		BHAPI_SAMPLES[0] = Textures.getAtlas().getSample(Identifier.make("block/fire_0"));
		BHAPI_SAMPLES[1] = Textures.getAtlas().getSample(Identifier.make("block/fire_1"));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		return BHAPI_SAMPLES[textureIndex & 1];
	}
}

