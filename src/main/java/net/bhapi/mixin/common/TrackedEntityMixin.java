package net.bhapi.mixin.common;

import net.minecraft.entity.BaseEntity;
import net.minecraft.server.network.TrackedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TrackedEntity.class)
public class TrackedEntityMixin {
	@Shadow public BaseEntity entityToSync;
	
	/*@Inject(method = "sync", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/server/network/TrackedEntity;method_600()Lnet/minecraft/packet/AbstractPacket;",
		shift = Shift.AFTER
	))
	private void bhapi_sync(ServerPlayer player, CallbackInfo info) {
		if (this.entityToSync instanceof ServerPlayer) {
			player.packetHandler.send(new BlockStatesPacket());
			System.out.println("Sync player!");
		}
	}*/
}
