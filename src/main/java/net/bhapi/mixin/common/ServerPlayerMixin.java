package net.bhapi.mixin.common;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.level.Level;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerBase {
	@Shadow public MinecraftServer server;
	
	public ServerPlayerMixin(Level arg) {
		super(arg);
	}
	
	@ModifyConstant(method = {
		"tick(Z)V"
	}, constant = @Constant(intValue = 128))
	private int bhapi_changeMaxBlockHeight(int value) {
		return LevelHeightProvider.cast(this.server.getLevel(this.dimensionId)).getLevelHeight();
	}
}
