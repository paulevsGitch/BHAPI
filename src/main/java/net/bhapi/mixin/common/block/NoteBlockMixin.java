package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.util.BlockDirection;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.NoteblockBlockEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin implements BlockStateContainer {
	@Override
	public void bhapi_onNeighbourBlockUpdate(Level level, int x, int y, int z, BlockDirection facing, BlockState state, BlockState neighbour) {
		if (!neighbour.isAir() && neighbour.emitsPower()) {
			boolean hasPower = level.hasInderectPower(x, y, z);
			NoteblockBlockEntity entity = (NoteblockBlockEntity) level.getBlockEntity(x, y, z);
			if (entity.isActive != hasPower) {
				if (hasPower) {
					entity.playNote(level, x, y, z);
				}
				entity.isActive = hasPower;
			}
		}
	}
}
