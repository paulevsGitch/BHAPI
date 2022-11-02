package net.bhapi.interfaces;

import net.minecraft.util.io.CompoundTag;

public interface NBTSerializable {
	void saveToNBT(CompoundTag tag);
	void loadFromNBT(CompoundTag tag);
	
	static NBTSerializable cast(Object obj) {
		return (NBTSerializable) obj;
	}
}
