package net.bhapi.blockstate;

import net.bhapi.block.BHAirBlock;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.registry.DefaultRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.block.BaseBlock;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlockState {
	private static final Map<BaseBlock, BlockState[]> POSSIBLE_STATES = new HashMap<>();
	public static final BlockState AIR = new BlockState(BHAirBlock.AIR);
	
	private static int increment;
	private static int index;
	
	private final List<StateProperty<?>> properties = new ArrayList<>();
	private final Map<StateProperty<?>, Object> propertyValues = new HashMap<>();
	private final BlockState[] localCache;
	private final BaseBlock block;
	
	private BlockState(BaseBlock block) {
		this.localCache = POSSIBLE_STATES.computeIfAbsent(block, key -> {
			BlockStateContainer container = BlockStateContainer.cast(block);
			container.appendProperties(properties);
			int size = 1;
			for (StateProperty<?> property: properties) size *= property.getCount();
			container.setDefaultState(this);
			properties.forEach(property -> this.propertyValues.put(property, property.defaultValue()));
			BlockState[] cache = new BlockState[size];
			DefaultRegistries.BLOCKSTATES_MAP.add(this);
			cache[0] = this;
			return cache;
		});
		this.block = block;
	}
	
	public <T> BlockState with(StateProperty<T> property, T value) {
		if (!propertyValues.containsKey(property)) throw new RuntimeException("No property " + property + " in block " + block);
		
		index = 0;
		increment = 1;
		propertyValues.forEach((prop, obj) -> {
			if (prop == property) index += property.getIndex(value) * increment;
			else index += prop.getCastedIndex(obj) * increment;
			increment *= prop.getCount();
		});
		
		BlockState state = localCache[index];
		if (state == null) {
			state = new BlockState(block);
			state.propertyValues.putAll(propertyValues);
			state.propertyValues.put(property, value);
			localCache[index] = state;
			DefaultRegistries.BLOCKSTATES_MAP.add(state);
		}
		
		return state;
	}
	
	private  <T> BlockState withCast(StateProperty<T> property, Object value) {
		return with(property, (T) value);
	}
	
	public BaseBlock getBlock() {
		return block;
	}
	
	public BlockStateContainer getContainer() {
		return BlockStateContainer.cast(block);
	}
	
	/**
	 * Get possible states for a parent block.
	 * @return {@link List} of {@link BlockState}
	 */
	public List<BlockState> getPossibleStates() {
		for (int i = 0; i < localCache.length; i++) {
			if (localCache[i] == null) {
				index = i;
				increment = 1;
				Map<StateProperty<?>, Object> newProperties = new HashMap<>();
				propertyValues.keySet().forEach(prop -> {
					int indexInternal = (index / increment) % prop.getCount();
					newProperties.put(prop, prop.getValues().get(indexInternal));
					increment *= prop.getCount();
				});
				
				BlockState state = localCache[index];
				if (state == null) {
					state = new BlockState(block);
					state.propertyValues.putAll(newProperties);
					localCache[i] = state;
				}
			}
		}
		
		return List.of(localCache);
	}
	
	public String toNBTString() {
		Identifier blockID = DefaultRegistries.BLOCK_REGISTRY.getID(block);
		if (blockID == null) {
			throw new RuntimeException("Block " + block + " is not in registry!");
		}
		StringBuilder builder = new StringBuilder("block=");
		builder.append(blockID);
		index = 0;
		final int max = propertyValues.size();
		if (max > 0) {
			builder.append(",");
			propertyValues.forEach((prop, obj) -> {
				index++;
				builder.append(prop.getName());
				builder.append("=");
				builder.append(obj);
				if (index < max) {
					builder.append(",");
				}
			});
		}
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return toNBTString();
	}
	
	public static BlockState getDefaultState(BaseBlock block) {
		if (POSSIBLE_STATES.containsKey(block)) return POSSIBLE_STATES.get(block)[0];
		return new BlockState(block);
	}
	
	/**
	 * Get property by its name.
	 * @param name a name of a property
	 * @return
	 */
	public StateProperty<?> getProperty(String name) {
		for (StateProperty<?> property: propertyValues.keySet()) {
			if (property.getName().equals(name)) {
				return property;
			}
		}
		return null;
	}
	
	/**
	 * Get {@link List} of available {@link StateProperty} for this state.
	 */
	public List<StateProperty<?>> getProperties() {
		return properties;
	}
	
	/**
	 * Save state into NBT compond tag. Used in serialisation.
	 * @return {@link CompoundTag}
	 */
	public CompoundTag saveToNBT() {
		CompoundTag tag = new CompoundTag();
		tag.put("block", DefaultRegistries.BLOCK_REGISTRY.getID(block).toString());
		if (!propertyValues.isEmpty()) {
			ListTag list = new ListTag();
			tag.put("properties", list);
			propertyValues.forEach((property, value) -> {
				CompoundTag propertyTag = new CompoundTag();
				list.add(propertyTag);
				propertyTag.put("name", property.getName());
				propertyTag.put("index", property.getCastedIndex(value));
			});
		}
		return tag;
	}
	
	/**
	 * Load state from compound tag
	 * @param tag
	 * @return
	 */
	public static BlockState loadFromNBT(CompoundTag tag) {
		Identifier blockID = Identifier.make(tag.getString("block"));
		BaseBlock block = DefaultRegistries.BLOCK_REGISTRY.get(blockID);
		if (block == null) return null;
		
		BlockState state = getDefaultState(block);
		if (!state.propertyValues.isEmpty() && tag.containsKey("properties")) {
			ListTag list = tag.getListTag("properties");
			int size = list.size();
			for (int i = 0; i < size; i++) {
				CompoundTag propertyTag = (CompoundTag) list.get(i);
				String name = propertyTag.getString("name");
				StateProperty<?> property = state.getProperty(name);
				if (property != null) {
					int index = propertyTag.getInt("index");
					List<?> values = property.getValues();
					if (index < 0 || index >= values.size()) continue;
					state = state.withCast(property, values.get(index));
				}
			}
		}
		
		return state;
	}
}
