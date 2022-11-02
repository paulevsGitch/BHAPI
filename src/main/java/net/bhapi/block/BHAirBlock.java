package net.bhapi.block;

import net.minecraft.block.material.Material;

public class BHAirBlock extends BHBaseBlock {
	public static final BHAirBlock AIR = new BHAirBlock();
	
	public BHAirBlock() {
		super(Material.AIR);
	}
}
