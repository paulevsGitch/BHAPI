package net.bhapi.mixin.common;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.level.Level;
import net.minecraft.level.LevelPopulationRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LevelPopulationRegion.class)
public class LevelPopulationRegionMixin implements LevelHeightProvider {
	@Shadow private Level level;
	
	@ModifyConstant(method = {
		"method_142(IIIZ)I",
		"getBlockId(III)I",
		"getBlockMeta(III)I"
	}, constant = @Constant(intValue = 128))
	private int bhapi_changeMaxHeight(int value) {
		return getLevelHeight();
	}
	
	@Unique
	@Override
	public short getLevelHeight() {
		return LevelHeightProvider.cast(this.level).getLevelHeight();
	}
}
