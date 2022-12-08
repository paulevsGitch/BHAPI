package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.storage.PermutationTable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.StoneBlock;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;

// TODO Remove this
@Mixin(StoneBlock.class)
public class StoneBlockMixin implements BHBlockRender {
	private PermutationTable bh_table = new PermutationTable(0);
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
		BaseBlock block = state.getBlock();
		int texture = block.getTextureForSide(view, x, y, z, index);
		TextureSample sample = Textures.getVanillaBlockSample(texture);
		if (view instanceof BlockItemView) {
			sample.setRotation(0);
			sample.setMirrorU(false);
			sample.setMirrorV(false);
		}
		else {
			int val = bh_table.getInt(x, y, z);
			sample.setRotation((val & 1) << 1);
			sample.setMirrorU(((val >> 1) & 1) == 1);
			sample.setMirrorV(((val >> 2) & 1) == 1);
		}
		return sample;
	}
}
