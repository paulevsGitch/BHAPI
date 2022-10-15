package net.bhapi.util;

import net.bhapi.blockstate.BlockState;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.EntityRegistry;
import net.minecraft.level.Level;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;
import net.minecraft.util.maths.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkSection implements NBTSerializable {
	private BlockState[] blockStates = new BlockState[4096];
	private byte[] blockID = new byte[4096];
	private byte[] blockMeta = new byte[4096];
	public List<BaseEntity> entities = new ArrayList<>();
	private Map<BlockPos, BaseBlockEntity> blockEntities = new HashMap();
	
	public BlockState getState(int x, int y, int z) {
		BlockState state = blockStates[getIndex(x, y, z)];
		return state == null ? BlockState.AIR_STATE : state;
	}
	
	public void setState(int x, int y, int z, BlockState state) {
		blockStates[getIndex(x, y, z)] = state;
	}
	
	private int getIndex(int x, int y, int z) {
		return x << 8 | y << 4 | z;
	}
	
	public int getID(int x, int y, int z) {
		return blockID[getIndex(x, y, z)];
	}
	
	public void setID(int x, int y, int z, int id) {
		blockID[getIndex(x, y, z)] = (byte) id;
	}
	
	public int getMeta(int x, int y, int z) {
		return blockMeta[getIndex(x, y, z)];
	}
	
	public void setMeta(int x, int y, int z, int meta) {
		blockMeta[getIndex(x, y, z)] = (byte) meta;
	}
	
	public void set(int x, int y, int z, int id, int meta) {
		int index = getIndex(x, y, z);
		blockID[index] = (byte) id;
		blockMeta[index] = (byte) meta;
	}
	
	public BaseBlockEntity getBlockEntity(BlockPos pos) {
		return blockEntities.get(pos);
	}
	
	public void setBlockEntity(BlockPos pos, BaseBlockEntity entity) {
		blockEntities.put(pos, entity);
	}
	
	public void removeBlockEntity(BlockPos pos) {
		blockEntities.remove(pos);
	}
	
	public Collection<BaseBlockEntity> getBlockEntities() {
		return blockEntities.values();
	}
	
	@Override
	public void saveToNBT(CompoundTag tag) {
		tag.put("id", blockID);
		tag.put("meta", blockMeta);
		
		ListTag entitiesList = new ListTag();
		tag.put("entities", entitiesList);
		entities.forEach(entity -> {
			CompoundTag entityTag = new CompoundTag();
			if (entity.checkAndSave(entityTag)) {
				entitiesList.add(entityTag);
			}
		});
		
		ListTag blockEntitiesList = new ListTag();
		tag.put("blockEntities", blockEntitiesList);
		blockEntities.values().forEach(entity -> {
			CompoundTag entityTag = new CompoundTag();
			entity.writeIdentifyingData(entityTag);
			blockEntitiesList.add(entityTag);
		});
	}
	
	@Override
	public void loadFromNBT(CompoundTag tag) {
		byte[] data = tag.getByteArray("id");
		if (data.length == blockID.length) {
			System.arraycopy(data, 0, blockID, 0, data.length);
		}
		
		data = tag.getByteArray("meta");
		if (data.length == blockMeta.length) {
			System.arraycopy(data, 0, blockMeta, 0, data.length);
		}
		
		ListTag listTag = tag.getListTag("blockEntities");
		if (listTag != null) {
			for (int i = 0; i < listTag.size(); ++i) {
				CompoundTag entityTag = (CompoundTag) listTag.get(i);
				BaseBlockEntity blockEntity = BaseBlockEntity.tileEntityFromNBT(entityTag);
				if (blockEntity == null) continue;
				BlockPos pos = new BlockPos(blockEntity.x, blockEntity.y, blockEntity.z);
				blockEntities.put(pos, blockEntity);
			}
		}
	}
	
	public void loadEntities(CompoundTag tag, Level level) {
		ListTag listTag = tag.getListTag("entities");
		if (listTag == null) return;
		for (int i = 0; i < listTag.size(); ++i) {
			CompoundTag entityTag = (CompoundTag) listTag.get(i);
			BaseEntity entity = EntityRegistry.create(entityTag, level);
			if (entity != null) {
				entities.add(entity);
			}
		}
	}
}
