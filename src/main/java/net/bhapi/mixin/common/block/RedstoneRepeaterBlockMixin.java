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
import net.minecraft.block.RedstoneRepeaterBlock;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RedstoneRepeaterBlock.class)
public abstract class RedstoneRepeaterBlockMixin implements BlockStateContainer, ClientPostInit, BHBlockRender {
	@Shadow @Final private boolean powered;
	@Environment(EnvType.CLIENT)
	private static final TextureSample[] BHAPI_SAMPLES = new TextureSample[4];
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void afterClientInit() {
		if (BHAPI_SAMPLES[0] != null) return;
		BHAPI_SAMPLES[0] = Textures.getAtlas().getSample(Identifier.make("block/redstone_repeater_off"));
		BHAPI_SAMPLES[1] = Textures.getAtlas().getSample(Identifier.make("block/redstone_repeater_on"));
		BHAPI_SAMPLES[2] = Textures.getAtlas().getSample(Identifier.make("block/redstone_torch_off"));
		BHAPI_SAMPLES[3] = Textures.getAtlas().getSample(Identifier.make("block/redstone_torch_on"));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
		int i = index < 6 ? 0 : 2;
		if (this.powered) i ++;
		return BHAPI_SAMPLES[i];
	}
}

