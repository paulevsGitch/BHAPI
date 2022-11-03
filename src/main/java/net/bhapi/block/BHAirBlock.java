package net.bhapi.block;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.util.maths.Box;

import java.util.ArrayList;

public class BHAirBlock extends BHBaseBlock {
	public BHAirBlock() {
		super(Material.AIR);
		disableNotifyOnMetaDataChange();
		disableStat();
	}
	
	@Override
	public boolean isCollidable() {
		return false;
	}
	
	@Override
	public boolean isFullCube() {
		return false;
	}
	
	@Override
	public int getRenderType() {
		return -1;
	}
	
	@Override
	public boolean isFullOpaque() {
		return false;
	}
	
	@Override
	public void doesBoxCollide(Level arg, int i, int j, int k, Box arg2, ArrayList arrayList) {}
	
	@Override
	public Box getCollisionShape(Level arg, int i, int j, int k) {
		return null;
	}
	
	@Override
	public void onAdjacentBlockUpdate(Level arg, int i, int j, int k, int l) {}
	
	@Override
	public void drop(Level arg, int i, int j, int k, int l, float f) {}
	
	@Override
	protected void drop(Level arg, int i, int j, int k, ItemStack arg2) {}
	
	@Override
	public int getPistonPushMode() {
		return 0;
	}
}
