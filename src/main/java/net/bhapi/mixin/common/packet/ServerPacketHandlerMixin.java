package net.bhapi.mixin.common.packet;

import net.bhapi.packet.BlockStatesPacket;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.packet.login.LoginRequest0x1Packet;
import net.minecraft.server.network.ServerPacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPacketHandler.class)
public class ServerPacketHandlerMixin {
	@Inject(method = "complete", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/server/ServerPlayerConnectionManager;sendPlayerTime(Lnet/minecraft/entity/player/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V",
		shift = Shift.BEFORE
	), locals = LocalCapture.CAPTURE_FAILHARD)
	private void bhapi_sync(LoginRequest0x1Packet packet, CallbackInfo info, ServerPlayer player) {
		player.packetHandler.send(new BlockStatesPacket());
	}
}
