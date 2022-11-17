package net.bhapi.item;

import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.interfaces.ClientPostInit;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

public class BHSimpleItem extends BHItem implements ClientPostInit {
	private final Identifier textureID;
	
	@Environment(EnvType.CLIENT)
	private TextureSample sample;
	
	public BHSimpleItem(Identifier textureID) {
		this.textureID = textureID;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTexture(ItemStack stack) {
		return sample;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void afterClientInit() {
		sample = Textures.getAtlas().getSample(textureID);
	}
}
