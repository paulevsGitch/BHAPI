package net.bhapi.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.BlockPropertyType;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.interfaces.NBTSerializable;
import net.bhapi.util.BlockUtil;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.EntityRegistry;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;
import net.minecraft.util.maths.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkSection implements NBTSerializable {
	private static final StatesLoader LOADER = new StatesLoader();
	private final Map<BlockPos, BaseBlockEntity> blockEntities = new HashMap();
	private final BlockState[] states = new BlockState[4096];
	private final byte[] light = new byte[4096];
	
	public final List<BaseEntity> entities = new ArrayList<>();
	
	private int getIndex(int x, int y, int z) {
		return x << 8 | y << 4 | z;
	}
	
	public BlockState getBlockState(int x, int y, int z) {
		BlockState state = states[getIndex(x, y, z)];
		return state == null ? BlockUtil.AIR_STATE : state;
	}
	
	public void setBlockState(int x, int y, int z, BlockState state) {
		states[getIndex(x, y, z)] = state;
	}
	
	public int getMeta(int x, int y, int z) {
		BlockState state = getBlockState(x, y, z);
		StateProperty<?> meta = state.getProperty("meta");
		return meta != null && meta.getType() == BlockPropertyType.INTEGER ? (int) state.getValue(meta) : 0;
	}
	
	public void setMeta(int x, int y, int z, int meta) {
		int index = getIndex(x, y, z);
		BlockState state = states[index];
		if (state == null) return;
		StateProperty<?> property = state.getProperty("meta");
		if (property != null && property.getType() == BlockPropertyType.INTEGER) {
			states[index] = state.with(property, meta);
		}
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
	
	public int getMaxLight(int x, int y, int z) {
		int index = getIndex(x, y, z);
		int a = light[index] & 15;
		int b = (light[index] >> 4) & 15;
		return a > b ? a : b;
	}
	
	public int getLight(LightType type, int x, int y, int z) {
		int index = getIndex(x, y, z);
		return type == LightType.SKY ? light[index] & 15 : (light[index] >> 4) & 15;
	}
	
	public void setLight(LightType type, int x, int y, int z, int value) {
		int index = getIndex(x, y, z);
		if (type == LightType.SKY) {
			light[index] = (byte) ((light[index] & 0xF0) | value);
		}
		else {
			light[index] = (byte) ((light[index] & 0x0F) | (value << 4));
		}
	}
	
	@Override
	public void saveToNBT(CompoundTag tag) {
		LOADER.fillFrom(states);
		
		CompoundTag statesTag = new CompoundTag();
		LOADER.saveToNBT(statesTag);
		
		tag.put("states", statesTag);
		tag.put("light", light);
		
		if (!entities.isEmpty()) {
			ListTag entitiesList = new ListTag();
			tag.put("entities", entitiesList);
			entities.forEach(entity -> {
				CompoundTag entityTag = new CompoundTag();
				if (entity.checkAndSave(entityTag)) {
					entitiesList.add(entityTag);
				}
			});
		}
		
		if (!blockEntities.isEmpty()) {
			ListTag blockEntitiesList = new ListTag();
			tag.put("blockEntities", blockEntitiesList);
			blockEntities.values().forEach(entity -> {
				CompoundTag entityTag = new CompoundTag();
				entity.writeIdentifyingData(entityTag);
				blockEntitiesList.add(entityTag);
			});
		}
	}
	
	@Override
	public void loadFromNBT(CompoundTag tag) {
		LOADER.loadFromNBT(tag.getCompoundTag("states"));
		LOADER.fillTo(states);
		
		byte[] data = tag.getByteArray("light");
		if (data.length == light.length) {
			System.arraycopy(data, 0, light, 0, data.length);
		}
	}
	
	public void loadEntities(CompoundTag tag, Level level, int x, int y, int z, boolean canHaveBlockEntities) {
		ListTag listTag = tag.getListTag("blockEntities");
		if (listTag != null) {
			for (int i = 0; i < listTag.size(); ++i) {
				CompoundTag entityTag = (CompoundTag) listTag.get(i);
				BaseBlockEntity blockEntity = BaseBlockEntity.tileEntityFromNBT(entityTag);
				if (blockEntity == null) continue;
				BlockPos pos = new BlockPos(blockEntity.x, blockEntity.y, blockEntity.z);
				blockEntity.level = level;
				blockEntities.put(pos, blockEntity);
			}
		}
		
		if (canHaveBlockEntities) {
			level.addBlockEntities(getBlockEntities());
		}
		
		listTag = tag.getListTag("entities");
		if (listTag == null) return;
		for (int i = 0; i < listTag.size(); ++i) {
			CompoundTag entityTag = (CompoundTag) listTag.get(i);
			BaseEntity entity = EntityRegistry.create(entityTag, level);
			if (entity != null) {
				entity.placedInWorld = true;
				entity.chunkX = x;
				entity.chunkIndex = y;
				entity.chunkZ = z;
				entities.add(entity);
			}
		}
	}
}
