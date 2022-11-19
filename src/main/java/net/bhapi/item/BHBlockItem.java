package net.bhapi.item;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.level.PlaceChecker;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockSounds;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;

import java.util.HashMap;
import java.util.Map;

public class BHBlockItem extends BHItem {
	private static final Map<BlockState, BHBlockItem> ITEMS = new HashMap<>();
	
	private final BlockState state;
	private final boolean isFlat;
	private static int globalID;
	private final int id = globalID++;
	
	public BHBlockItem(BlockState state, boolean isFlat) {
		this.state = state;
		this.isFlat = isFlat;
		ITEMS.put(state, this);
	}
	
	public BHBlockItem(BaseBlock block, boolean isFlat) {
		this(BlockState.getDefaultState(block), isFlat);
	}
	
	@Override
	public boolean useOnBlock(ItemStack stack, PlayerBase player, Level level, int x, int y, int z, int facing) {
		if (stack.count == 0) return false;
		if (y < 0 || y >= LevelHeightProvider.cast(level).getLevelHeight()) return false;
		
		BlockStateProvider provider = BlockStateProvider.cast(level);
		BlockState worldState = provider.getBlockState(x, y, z);
		if (worldState.is(BaseBlock.SNOW)) facing = 0;
		
		BlockDirection dir = BlockDirection.getFromFacing(facing);
		Vec3I pos = dir.move(new Vec3I(x, y, z));
		
		PlaceChecker checker = PlaceChecker.cast(level);
		if (checker.canPlaceState(state, pos.x, pos.y, pos.z, false, facing)) {
			if (provider.setBlockState(pos, state)) {
				state.onBlockPlaced(level, pos.x, pos.y, pos.z);
				state.getBlock().afterPlaced(level, pos.x, pos.y, pos.z, player);
				BlockSounds sounds = state.getSounds();
				level.playSound(
					pos.x + 0.5F,
					pos.y + 0.5F,
					pos.z + 0.5F,
					sounds.getWalkSound(),
					sounds.getVolume() * 0.5F + 0.5F,
					sounds.getPitch() * 0.8F
				);
				stack.count--;
			}
			return true;
		}
		return false;
	}
	
	@Override
	@Environment(value= EnvType.CLIENT)
	public String getTranslationKey(ItemStack arg) {
		return state.getBlock().getTranslationKey();
	}
	
	@Override
	public String getTranslationKey() {
		return state.getBlock().getTranslationKey();
	}
	
	public BlockState getState() {
		return state;
	}
	
	@Override
	public int hashCode() {
		return id;//state.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof BHBlockItem)) return false;
		return ((BHBlockItem) obj).state == state;
	}
	
	@Environment(EnvType.CLIENT)
	public boolean isFlat() {
		return isFlat;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{class: ");
		builder.append(this.getClass().getName());
		builder.append(", hash: ");
		builder.append(hashCode());
		builder.append(", state: ");
		builder.append(state);
		builder.append("}");
		return builder.toString();
	}
	
	@Override // TODO return sample if item is 2D
	@Environment(EnvType.CLIENT)
	public TextureSample getTexture(ItemStack stack) {
		return null;
	}
	
	public static BHBlockItem get(BlockState state) {
		BHBlockItem item = ITEMS.get(state);
		if (item == null) {
			BHAPI.warn("Missing block item for " + state + ", attempt to get default");
			item = ITEMS.get(BlockState.getDefaultState(state.getBlock()));
		}
		return item;
	}
}
