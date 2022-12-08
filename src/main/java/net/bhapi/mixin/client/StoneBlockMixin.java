package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.StoneBlock;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;

// TODO Remove this
@Mixin(StoneBlock.class)
public class StoneBlockMixin implements BHBlockRender {
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
		BaseBlock block = state.getBlock();
		int texture = block.getTextureForSide(view, x, y, z, index);
		TextureSample sample = Textures.getVanillaBlockSample(texture);
		if (!(view instanceof BlockItemView)) sample.setRotation(BHAPIClient.getMinecraft().level.random.nextInt(4));
		return sample;
	}
}
