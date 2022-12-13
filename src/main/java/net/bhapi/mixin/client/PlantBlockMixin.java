package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.interfaces.ClientPostInit;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.material.Material;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;

// TODO Remove this
@Mixin(PlantBlock.class)
public abstract class PlantBlockMixin extends BaseBlock implements BHBlockRender, ClientPostInit {
	private TextureSample sample;
	
	protected PlantBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Override
	@Environment(value= EnvType.CLIENT)
	public int getOverlayCount(BlockView view, int x, int y, int z, BlockState state) {
		return state.is(BaseBlock.DANDELION) ? 2 : 1;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int textureIndex, int overlayIndex) {
		if (overlayIndex == 0) return Textures.getVanillaBlockSample(this.texture);
		return sample;
	}
	
	@Override
	public void afterClientInit() {
		if (sample == null) {
			sample = Textures.getAtlas().getSample(Identifier.make("bhapi", "block/flower_e"));
			sample.setLight(1);
		}
	}
}
