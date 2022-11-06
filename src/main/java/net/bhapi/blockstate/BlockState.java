package net.bhapi.blockstate;

import net.bhapi.block.BHAirBlock;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.registry.DefaultRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockSounds;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.level.Level;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class BlockState {
	private static final Map<BaseBlock, BlockState[]> POSSIBLE_STATES = new HashMap<>();
	
	private final Map<StateProperty<?>, Object> propertyValues = new HashMap<>();
	private final Map<String, StateProperty<?>> properties;
	private final BlockState[] localCache;
	private final BaseBlock block;
	
	private BlockState(BaseBlock block) {
		this(block, new HashMap<>());
	}
	
	private BlockState(BaseBlock block, Map<String, StateProperty<?>> properties) {
		this.properties = properties;
		synchronized (POSSIBLE_STATES) {
			this.localCache = POSSIBLE_STATES.computeIfAbsent(block, key -> {
				BlockStateContainer container = BlockStateContainer.cast(block);
				ArrayList<StateProperty<?>> rawProperties = new ArrayList<>();
				container.appendProperties(rawProperties);
				int size = 1;
				for (StateProperty<?> property : rawProperties) size *= property.getCount();
				container.setDefaultState(this);
				rawProperties.forEach(property -> {
					this.propertyValues.put(property, property.defaultValue());
					this.properties.put(property.getName(), property);
				});
				BlockState[] cache = new BlockState[size];
				DefaultRegistries.BLOCKSTATES_MAP.add(this);
				cache[0] = this;
				return cache;
			});
		}
		this.block = block;
	}
	
	private <T> BlockState withProperty(StateProperty<T> property, T value) {
		synchronized (propertyValues) {
			int[] indAndInc = new int[] {0, 1};
			if (!propertyValues.containsKey(property)) {
				throw new RuntimeException("No property " + property + " in block " + block);
			}
			propertyValues.forEach((prop, obj) -> {
				if (prop == property) indAndInc[0] += property.getIndex(value) * indAndInc[1];
				else indAndInc[0] += prop.getCastedIndex(obj) * indAndInc[1];
				indAndInc[1] *= prop.getCount();
			});
			BlockState state = localCache[indAndInc[0]];
			if (state == null) {
				state = new BlockState(block, localCache[0].properties);
				DefaultRegistries.BLOCKSTATES_MAP.add(state);
				state.propertyValues.putAll(propertyValues);
				state.propertyValues.put(property, value);
				localCache[indAndInc[0]] = state;
				DefaultRegistries.BLOCKSTATES_MAP.add(state);
			}
			return state;
		}
	}
	
	public <T> BlockState with(StateProperty<T> property, Object value) {
		return withProperty(property, (T) value);
	}
	
	public BaseBlock getBlock() {
		return block;
	}
	
	private BlockStateContainer getContainer() {
		return BlockStateContainer.cast(block);
	}
	
	/**
	 * Get possible states for a parent block.
	 * @return {@link List} of {@link BlockState}
	 */
	public List<BlockState> getPossibleStates() {
		int[] indAndInc = new int[] {0, 1};
		for (int i = 0; i < localCache.length; i++) {
			if (localCache[i] == null) {
				indAndInc[0] = i;
				indAndInc[1] = 1;
				Map<StateProperty<?>, Object> newProperties = new HashMap<>();
				propertyValues.keySet().forEach(prop -> {
					int indexInternal = (indAndInc[0] / indAndInc[1]) % prop.getCount();
					newProperties.put(prop, prop.getValues().get(indexInternal));
					indAndInc[1] *= prop.getCount();
				});
				
				BlockState state = localCache[indAndInc[0]];
				if (state == null) {
					state = new BlockState(block, localCache[0].properties);
					DefaultRegistries.BLOCKSTATES_MAP.add(state);
					state.propertyValues.putAll(newProperties);
					localCache[i] = state;
				}
			}
		}
		
		return List.of(localCache);
	}
	
	@Override
	public String toString() {
		Identifier blockID = DefaultRegistries.BLOCK_REGISTRY.getID(block);
		if (blockID == null) {
			throw new RuntimeException("Block " + block + " is not in registry!");
		}
		StringBuilder builder = new StringBuilder("block=");
		builder.append(blockID);
		int[] index = new int[] {0};
		final int max = propertyValues.size();
		if (max > 0) {
			builder.append(",");
			propertyValues.forEach((prop, obj) -> {
				index[0]++;
				builder.append(prop.getName());
				builder.append("=");
				builder.append(obj);
				if (index[0] < max) {
					builder.append(",");
				}
			});
		}
		return builder.toString();
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
		return properties.get(name);
	}
	
	public <T> T getValue(StateProperty<T> property) {
		return (T) propertyValues.get(property);
	}
	
	/**
	 * Get {@link List} of available {@link StateProperty} for this state.
	 */
	public Collection<StateProperty<?>> getProperties() {
		return properties.values();
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
					state = state.with(property, values.get(index));
				}
			}
		}
		
		return state;
	}
	
	public boolean isAir() {
		return this.block instanceof BHAirBlock;
	}
	
	public boolean is(BaseBlock block) {
		return this.block == block;
	}
	
	/**
	 * Get {@link BlockSounds} for this {@link BlockState}.
	 * @return {@link BlockSounds}
	 */
	public BlockSounds getSounds() {
		return getContainer().getSounds(this);
	}
	
	/**
	 * Check if {@link BlockState} has random ticks.
	 * Example of blocks with random ticks: saplings, crops, grass blocks.
	 * @return {@code true} if state has random ticks and {@code false} if not
	 */
	public boolean hasRandomTicks() {
		return getContainer().hasRandomTicks(this);
	}
	
	/**
	 * Check if {@link BlockState} is full opaque block (example: stone).
	 * @return {@code true} if state is opaque and {@code false} if not
	 */
	public boolean isFullOpaque() {
		return getContainer().isFullOpaque(this);
	}
	
	/**
	 * Check if this {@link BlockState} has {@link net.minecraft.block.entity.BaseBlockEntity} (examples: furnace, sign).
	 * @return {@code true} if state has entity and {@code false} if not
	 */
	public boolean hasBlockEntity() {
		return getContainer().hasBlockEntity(this);
	}
	
	/**
	 * Get {@link BlockState} light opacity, determines how light will be shadowed by block during transition.
	 * Transparent blocks have this value equal to zero, water = 3, leaves = 1, opaque blocks = 255.
	 * @return {@code integer} value of light opacity
	 */
	public int getLightOpacity() {
		return getContainer().getLightOpacity(this);
	}
	
	/**
	 * Checks if {@link BlockState} allows grass blocks to grow under it.
	 * Opaque blocks have this value equal to {@code false}.
	 * If this value is false grass block below current state will be transformed into dirt.
	 * @return {@code true} if state allows grass growing and {@code false} if not
	 */
	public boolean allowsGrasUnder() {
		return getContainer().allowsGrasUnder(this);
	}
	
	/**
	 * Get light value of this {@link BlockState}. 0 is no light and 15 is full-brightness light.
	 * @return {@code integer} value of emittance in [0-15] range
	 */
	public int getEmittance() {
		return getContainer().getEmittance(this);
	}
	
	/**
	 * Get current {@link BlockState} hardness, used in digging time calculations.
	 * @return {@code float} hardness value
	 */
	public float getHardness() {
		return getContainer().getHardness(this);
	}
	
	/**
	 * Get current {@link BlockState} hardness for specific {@link PlayerBase}, used in digging time calculations.
	 * @param player current {@link PlayerBase}
	 * @return {@code float} hardness value
	 */
	public float getHardness(PlayerBase player) {
		return getContainer().getHardness(this, player);
	}
	
	/**
	 * Get {@link BlockState} blast resistance, used in digging explosions calculations.
	 * @param entity current {@link BaseEntity} (explosion cause)
	 * @return {@code float} blast resistance value
	 */
	public float getBlastResistance(BaseEntity entity) {
		return getContainer().getBlastResistance(this, entity);
	}
	
	/**
	 * Called after {@link BlockState} is removed from the world.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param newState {@link BlockState} that will replace this state
	 */
	public void onBlockRemoved(Level level, int x, int y, int z, BlockState newState) {
		getContainer().onBlockRemoved(level, x, y, z, this, newState);
	}
	
	/**
	 * Called after {@link BlockState} is placed in the world.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public void onBlockPlaced(Level level, int x, int y, int z) {
		getContainer().onBlockPlaced(level, x, y, z, this);
	}
	
	/**
	 * Applied on random or scheduled ticks.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param random {@link Random}
	 */
	public void onScheduledTick(Level level, int x, int y, int z, Random random) {
		getContainer().onScheduledTick(level, x, y, z, random, this);
	}
	
	/**
	 * Get block material
	 * @return {@link Material}
	 */
	public Material getMaterial() {
		return getBlock().material;
	}
}
