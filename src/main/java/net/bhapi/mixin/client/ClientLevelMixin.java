package net.bhapi.mixin.client;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.client.level.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements LevelHeightProvider {
	@ModifyConstant(method = "method_1494(IIZ)V", constant = @Constant(intValue = 128))
	private int changeMaxHeight(int value) {
		return getLevelHeight();
	}
}
