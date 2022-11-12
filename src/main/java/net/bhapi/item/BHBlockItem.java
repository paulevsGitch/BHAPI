package net.bhapi.item;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockSounds;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;

public class BHBlockItem extends BHItem {
	private final BlockState state;
	
	public BHBlockItem(BlockState state) {
		this.state = state;
	}
	
	public BHBlockItem(BaseBlock block) {
		this(BlockState.getDefaultState(block));
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
		
		System.out.println(canPlaceBlock(level, worldState, pos, facing));
		
		if (canPlaceBlock(level, worldState, pos, facing)) {
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
	
	protected boolean canPlaceBlock(Level level, BlockState baseBlock2, Vec3I pos, int facing) {
		/*BlockStateProvider provider = BlockStateProvider.cast(level);
		BlockState baseBlock = provider.getBlockState(pos);
		
		Box box = baseBlock2.getBlock().getCollisionShape(level, pos.x, pos.y, pos.z);
		if (box != null && !level.canSpawnEntity(box)) return false;
		if (baseBlock.getBlock() instanceof FluidBlock || baseBlock.is(BaseBlock.FIRE) || baseBlock.is(BaseBlock.SNOW)) {
			baseBlock = null;
		}
		
		return baseBlock == null && baseBlock2.getBlock().canPlaceAt(level, pos.x, pos.y, pos.z, facing);*/
		return true;
	}
	
	public BlockState getState() {
		return state;
	}
	
	@Override
	public int hashCode() {
		return state.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof BHBlockItem)) return false;
		return ((BHBlockItem) obj).state == state;
	}
}
