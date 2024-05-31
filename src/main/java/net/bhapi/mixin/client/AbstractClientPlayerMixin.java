package net.bhapi.mixin.client;

import net.bhapi.config.BHConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.Session;
import net.minecraft.entity.living.player.AbstractClientPlayer;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends PlayerEntity {
	public AbstractClientPlayerMixin(Level arg) {
		super(arg);
	}
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onClientPlayerInit(Minecraft minecraft, Level arg, Session arg2, int i, CallbackInfo info) {
		if (arg2 != null && arg2.username != null && arg2.username.length() > 0 && BHConfigs.GENERAL.getBool("network.useMinotarSkins", true)) {
			this.skinUrl = "https://minotar.net/skin/" + arg2.username + ".png";
			BHConfigs.GENERAL.save();
		}
	}
}
