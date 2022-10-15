package net.bhapi.mixin.client;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin implements LevelHeightProvider {
	@Shadow private Level level;
	
	@ModifyConstant(method = "method_1544(Lnet/minecraft/util/maths/Vec3f;Lnet/minecraft/class_68;F)V", constant = @Constant(intValue = 128))
	private int changeMaxHeight(int value) {
		return getLevelHeight();
	}
	
	@ModifyConstant(method = "method_1544(Lnet/minecraft/util/maths/Vec3f;Lnet/minecraft/class_68;F)V", constant = @Constant(intValue = 127))
	private int changeMaxBlockHeight(int value) {
		return getLevelHeight() - 1;
	}
	
	@ModifyConstant(method = "updateFromOptions()V", constant = @Constant(intValue = 8))
	private int changeSectionCount(int value) {
		return getSectionsCount();
	}
	
	@Unique
	@Override
	public short getLevelHeight() {
		return LevelHeightProvider.cast(this.level).getLevelHeight();
	}
}
