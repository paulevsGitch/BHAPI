package net.bhapi.mixin.client;

import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerBaseMixin extends LivingEntity {
	public PlayerBaseMixin(Level arg) {
		super(arg);
	}
	
	@Inject(method = "initCloak", at = @At("HEAD"), cancellable = true)
	public void initCloak(CallbackInfo info) {
		this.cloakUrl = null; // Didn't find a good API fpr capes
		info.cancel();
	}
}
