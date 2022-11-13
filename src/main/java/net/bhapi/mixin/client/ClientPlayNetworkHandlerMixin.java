package net.bhapi.mixin.client;

import net.bhapi.item.ItemProvider;
import net.minecraft.client.level.ClientLevel;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientPlayNetworkHandler;
import net.minecraft.packet.play.ItemEntitySpawn0x15S2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow private ClientLevel level;
	
	@Inject(method = "onItemEntitySpawn", at = @At("HEAD"), cancellable = true)
	private void bhapi_onItemEntitySpawn(ItemEntitySpawn0x15S2CPacket arg, CallbackInfo info) {
		info.cancel();
		double x = (double) arg.x / 32.0;
		double y = (double) arg.y / 32.0;
		double z = (double) arg.z / 32.0;
		BaseItem item = ItemProvider.cast(arg).getItem();
		ItemEntity itemEntity = new ItemEntity(this.level, x, y, z, new ItemStack(item, arg.count, arg.damage));
		itemEntity.velocityX = (double) arg.velocityX / 128.0;
		itemEntity.velocityY = (double) arg.velocityY / 128.0;
		itemEntity.velocityZ = (double) arg.velocityZ / 128.0;
		itemEntity.clientX = arg.x;
		itemEntity.clientY = arg.y;
		itemEntity.clientZ = arg.z;
		this.level.method_1495(arg.entityId, itemEntity);
	}
}
