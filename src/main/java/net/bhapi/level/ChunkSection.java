package net.bhapi.level;

import net.bhapi.blockstate.BlockState;
import net.bhapi.interfaces.NBTSerializable;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.storage.MultiThreadStorage;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.MathUtil;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.EntityRegistry;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkSection implements NBTSerializable {
	public static final ChunkSection EMPTY = new ChunkSection();
	private static final MultiThreadStorage<StatesLoader> LOADERS = new MultiThreadStorage<>(StatesLoader::new);
	private final Map<Vec3I, BaseBlockEntity> blockEntities = new ConcurrentHashMap<>();
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
		return getBlockState(x, y, z).getMeta();
	}
	
	public void setMeta(int x, int y, int z, int meta) {
		int index = getIndex(x, y, z);
		if (states[index] == null) return;
		states[index] = states[index].withMeta(meta);
	}
	
	public BaseBlockEntity getBlockEntity(Vec3I pos) {
		return blockEntities.get(pos);
	}
	
	public void setBlockEntity(Vec3I pos, BaseBlockEntity entity) {
		blockEntities.put(pos, entity);
	}
	
	public void removeBlockEntity(Vec3I pos) {
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
	
	public int writeData(byte[] data, int x, int y, int z, int index) {
		int secIndex = getIndex(x, y, z);
		
		BlockState state = states[secIndex];
		if (state == null) state = BlockUtil.AIR_STATE;
		int stateID = CommonRegistries.BLOCKSTATES_MAP.getID(state);
		index = MathUtil.writeInt(data, stateID, index);
		
		data[index++] = light[secIndex];
		
		return index;
	}
	
	public int readData(byte[] data, int x, int y, int z, int index) {
		int secIndex = getIndex(x, y, z);
		
		int stateID = MathUtil.readInt(data, index);
		BlockState state = CommonRegistries.BLOCKSTATES_MAP.get(stateID);
		states[secIndex] = state;
		
		index += 4;
		light[secIndex] = data[index++];
		
		return index;
	}
	
	@Override
	public void saveToNBT(CompoundTag tag) {
		StatesLoader loader = LOADERS.get();
		loader.fillFrom(states);
		if (loader.isEmpty()) return;
		
		CompoundTag statesTag = new CompoundTag();
		loader.saveToNBT(statesTag);
		
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
		StatesLoader loader = LOADERS.get();
		loader.loadFromNBT(tag.getCompoundTag("states"));
		if (loader.isEmpty()) return;
		loader.fillTo(states);
		
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
				Vec3I pos = new Vec3I(blockEntity.x & 15, blockEntity.y & 15, blockEntity.z & 15);
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
	
	static {
		for (int i = 0; i < 4096; i++) {
			EMPTY.setLight(LightType.SKY, i & 15, (i >> 4) & 15, i >> 8, 15);
		}
	}
}
