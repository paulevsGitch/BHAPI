package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.RailBlock.RailData;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Mixin(RailBlock.class)
public abstract class RailBlockMixin extends BaseBlock implements BlockStateContainer {
	@Shadow @Final private boolean wrapMeta;
	
	protected RailBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Shadow protected abstract void updateBlock(Level arg, int i, int j, int k, boolean bl);
	
	@Shadow protected abstract boolean canBePowered(Level arg, int i, int j, int k, int l, boolean bl, int m);
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@Inject(method = "onAdjacentBlockUpdate", at = @At("HEAD"), cancellable = true)
	private void bhapi_onAdjacentBlockUpdate(Level level, int x, int y, int z, int l, CallbackInfo info) {
		info.cancel();
		
		int meta = level.getBlockMeta(x, y, z);
		if (level.isClientSide) {
			return;
		}
		
		int wrappedMeta = meta;
		if (this.wrapMeta) {
			wrappedMeta &= 7;
		}
		
		boolean removeBlock = false;
		if (!level.canSuffocate(x, y - 1, z)) removeBlock = true;
		if (wrappedMeta == 2 && !level.canSuffocate(x + 1, y, z)) removeBlock = true;
		if (wrappedMeta == 3 && !level.canSuffocate(x - 1, y, z)) removeBlock = true;
		if (wrappedMeta == 4 && !level.canSuffocate(x, y, z - 1)) removeBlock = true;
		if (wrappedMeta == 5 && !level.canSuffocate(x, y, z + 1)) removeBlock = true;
		
		if (removeBlock) {
			this.drop(level, x, y, z, level.getBlockMeta(x, y, z));
			level.setBlock(x, y, z, 0);
		}
		else if (this.id == BaseBlock.GOLDEN_RAIL.id) {
			boolean power = level.hasRedstonePower(x, y, z) || level.hasRedstonePower(x, y + 1, z);
			power = power || this.canBePowered(level, x, y, z, meta, true, 0) || this.canBePowered(level, x, y, z, meta, false, 0);
			boolean update = false;
			
			if (power && (meta & 8) == 0) {
				level.setBlockMeta(x, y, z, wrappedMeta | 8);
				update = true;
			}
			else if (!power && (meta & 8) != 0) {
				level.setBlockMeta(x, y, z, wrappedMeta);
				update = true;
			}
			
			if (update) {
				level.updateAdjacentBlocks(x, y - 1, z, this.id);
				if (wrappedMeta == 2 || wrappedMeta == 3 || wrappedMeta == 4 || wrappedMeta == 5) {
					level.updateAdjacentBlocks(x, y + 1, z, this.id);
				}
			}
		}
		else if (l > 0 && /*BaseBlock.BY_ID[l].getEmitsRedstonePower() &&*/ !this.wrapMeta && bhapi_countNeighbours(level, x, y, z) == 3) {
			this.updateBlock(level, x, y, z, false); //TODO add neighbours block check using Block Util
		}
	}
	
	private int bhapi_countNeighbours(Level level, int x, int y, int z) {
		try {
			Class<?>[] args = new Class[] {RailBlock.class, Level.class, Integer.class, Integer.class, Integer.class};
			RailBlock block = RailBlock.class.cast(this);
			RailData data = (RailData) RailData.class.getConstructors()[0].newInstance(block, level, x, y, z);
			return data.countNeighbours();
		}
		catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return 0;
	}
}

