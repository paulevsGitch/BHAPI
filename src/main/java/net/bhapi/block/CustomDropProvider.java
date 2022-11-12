package net.bhapi.block;

import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;

import java.util.List;

public interface CustomDropProvider {
	void getCustomDrop(Level level, int x, int y, int z, List<ItemStack> drop);
	
	/*default void drop(Level arg, int i, int j, int k, int l, float f) {
		if (arg.isClientSide) return;
		List<ItemStack> drop = new ArrayList<>();
		getCustomDrop(drop);
		drop.forEach(stack -> );
		int n = this.getDropCount(arg.random);
		for (int i2 = 0; i2 < n; ++i2) {
			int n2;
			if (arg.random.nextFloat() > f || (n2 = this.getDropId(l, arg.random)) <= 0) continue;
			this.drop(arg, i, j, k, new ItemStack(n2, 1, this.getDropMeta(l)));
		}
	}*/
	
	static CustomDropProvider cast(Object obj) {
		return (CustomDropProvider) obj;
	}
}
