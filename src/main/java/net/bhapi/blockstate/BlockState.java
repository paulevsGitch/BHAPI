package net.bhapi.blockstate;

import net.bhapi.block.BHAirBlock;
import net.bhapi.blockstate.properties.IntegerProperty;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.block.BlockRenderTypes;
import net.bhapi.client.render.model.CustomModel;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.interfaces.IDProvider;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockSounds;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BaseEntity;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class BlockState implements IDProvider {
	private static final Map<BaseBlock, BlockState[]> POSSIBLE_STATES = new HashMap<>();
	private static int incrementalHash = 0;
	
	private final Map<StateProperty<?>, Object> propertyValues = new HashMap<>();
	private final Map<String, StateProperty<?>> properties;
	private final int hash = incrementalHash++;
	private final BlockState[] localCache;
	private final BaseBlock block;
	private int rawID;
	
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
				CommonRegistries.BLOCKSTATES_MAP.add(this);
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
				CommonRegistries.BLOCKSTATES_MAP.add(state);
				state.propertyValues.putAll(propertyValues);
				state.propertyValues.put(property, value);
				localCache[indAndInc[0]] = state;
				CommonRegistries.BLOCKSTATES_MAP.add(state);
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
					CommonRegistries.BLOCKSTATES_MAP.add(state);
					state.propertyValues.putAll(newProperties);
					localCache[i] = state;
				}
			}
		}
		
		return List.of(localCache);
	}
	
	@Override
	public String toString() {
		Identifier blockID = CommonRegistries.BLOCK_REGISTRY.getID(block);
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
		tag.put("block", CommonRegistries.BLOCK_REGISTRY.getID(block).toString());
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
		BaseBlock block = CommonRegistries.BLOCK_REGISTRY.get(blockID);
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
	 * Called after {@link BlockState} is placed in the world.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing Facing index
	 */
	public void onBlockPlaced(Level level, int x, int y, int z, int facing) {
		getContainer().onBlockPlaced(level, x, y, z, facing, this);
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
	
	/**
	 * Called when block neighbour is updated
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing Direction (from target block)
	 * @param neighbour Neighbour {@link BlockState}
	 */
	public void onNeighbourBlockUpdate(Level level, int x, int y, int z, BlockDirection facing, BlockState neighbour) {
		getContainer().onNeighbourBlockUpdate(level, x, y, z, facing, this, neighbour);
	}
	
	/**
	 * Check if specified {@link BlockState} has redstone power.
	 * @param level {@link Level} where block is located
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing {@link BlockDirection}
	 * @return {@code true} if blockstate has redstone power
	 */
	public boolean isPowered(Level level, int x, int y, int z, BlockDirection facing) {
		return getContainer().isPowered(level, x, y, z, facing, this);
	}
	
	public boolean emitsPower() {
		return getBlock().getEmitsRedstonePower();
	}
	
	/**
	 * Get meta value, used for legacy blocks that uses meta properties.
	 * @see net.bhapi.blockstate.properties.LegacyProperties
	 * @return meta value or zero if state don't have meta
	 */
	public int getMeta() {
		StateProperty<?> property = getProperty("meta");
		if (property instanceof IntegerProperty) {
			return (int) getValue(property);
		}
		return 0;
	}
	
	/**
	 * Get state with meta, used for legacy blocks. If state don't have meta will return itself.
	 * @param meta integer meta value
	 * @return {@link BlockState} or self
	 */
	public BlockState withMeta(int meta) {
		StateProperty<?> property = getProperty("meta");
		if (property instanceof IntegerProperty metaProperty) {
			if (metaProperty.isInRange(meta)) {
				return with(metaProperty, meta);
			}
		}
		return this;
	}
	
	@Environment(EnvType.CLIENT)
	public CustomModel getModel(BlockView view, int x, int y, int z) {
		return BHBlockRender.cast(getBlock()).getModel(view, x, y, z, this);
	}
	
	/**
	 * Check if that state will block face rendering from target blockstate or not.
	 * @param blockView {@link BlockView} as a block getter
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing {@link BlockDirection} from target to this block
	 * @param target {@link BlockState} target to check rendering
	 * @return {@code true} if face should be rendered and {@code false} if not
	 */
	@Environment(EnvType.CLIENT)
	public boolean isSideRendered(BlockView blockView, int x, int y, int z, BlockDirection facing, BlockState target) {
		return getContainer().isSideRendered(blockView, x, y, z, facing, this, target);
	}
	
	/**
	 * Check if that state should render block face or not.
	 * @param blockView {@link BlockView} as a block getter
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @param facing {@link BlockDirection} face direction
	 * @return {@code true} if face should be rendered and {@code false} if not
	 */
	@Environment(EnvType.CLIENT)
	public boolean isSideRendered(BlockView blockView, int x, int y, int z, BlockDirection facing) {
		BlockState neighbour = BlockStateProvider.cast(blockView).getBlockState(x, y, z);
		return neighbour.isSideRendered(blockView, x, y, z, facing, this);
	}
	
	/**
	 * Get render type for this block, full cube by default.
	 * @see BlockRenderTypes
	 * @param view {@link BlockView}
	 * @param x X block coordinate
	 * @param y Y block coordinate
	 * @param z Z block coordinate
	 */
	@Environment(EnvType.CLIENT)
	public byte getRenderType(BlockView view, int x, int y, int z) {
		return BHBlockRender.cast(getBlock()).getRenderType(view, x, y, z, this);
	}
	
	/**
	 * Get texture for current model index. Vanilla blocks have indexes equal to quad face directions, custom models
	 * can have any indexes.
	 * @see net.bhapi.util.BlockDirection
	 * @param view {@link BlockView}
	 * @param x X block coordinate
	 * @param y Y block coordinate
	 * @param z Z block coordinate
	 * @param textureIndex current texture index
	 * @param overlayIndex current overlay index
	 * @return {@link TextureSample} or null
	 */
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, int textureIndex, int overlayIndex) {
		return BHBlockRender.cast(getBlock()).getTextureForIndex(view, x, y, z, this, textureIndex, overlayIndex);
	}
	
	/**
	 * Get count of overlay textures for this blockstate.
	 * Each overlay will be rendered as same model, but with different textures.
	 */
	@Environment(EnvType.CLIENT)
	public int getOverlayCount(BlockView view, int x, int y, int z) {
		return BHBlockRender.cast(getBlock()).getOverlayCount(view, x, y, z, this);
	}
	
	@Override
	public int getID() {
		return rawID;
	}
	
	@Override
	public void setID(int id) {
		rawID = id;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof BlockState)) return false;
		BlockState state = (BlockState) obj;
		if (block != state.block) return false;
		if (state.properties.size() != properties.size()) return false;
		for (StateProperty<?> property: propertyValues.keySet()) {
			Object value1 = propertyValues.get(property);
			Object value2 = state.propertyValues.get(property);
			if (value2 == null || !value1.equals(value2)) {
				return false;
			}
		}
		return true;
	}
}
