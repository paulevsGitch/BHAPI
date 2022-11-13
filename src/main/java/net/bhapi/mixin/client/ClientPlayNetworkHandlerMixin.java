package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.item.ItemProvider;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.MultiStatesProvider;
import net.bhapi.registry.CommonRegistries;
import net.minecraft.client.level.ClientLevel;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.network.ClientPlayNetworkHandler;
import net.minecraft.packet.play.BlockChangePacket;
import net.minecraft.packet.play.ItemEntitySpawnPacket;
import net.minecraft.packet.play.MultiBlockChangePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow private ClientLevel level;
	
	@Inject(method = "onItemEntitySpawn", at = @At("HEAD"), cancellable = true)
	private void bhapi_onItemEntitySpawn(ItemEntitySpawnPacket packet, CallbackInfo info) {
		info.cancel();
		double x = (double) packet.x / 32.0;
		double y = (double) packet.y / 32.0;
		double z = (double) packet.z / 32.0;
		BaseItem item = ItemProvider.cast(packet).getItem();
		ItemEntity itemEntity = new ItemEntity(this.level, x, y, z, new ItemStack(item, packet.count, packet.damage));
		itemEntity.velocityX = (double) packet.velocityX / 128.0;
		itemEntity.velocityY = (double) packet.velocityY / 128.0;
		itemEntity.velocityZ = (double) packet.velocityZ / 128.0;
		itemEntity.clientX = packet.x;
		itemEntity.clientY = packet.y;
		itemEntity.clientZ = packet.z;
		this.level.method_1495(packet.entityId, itemEntity);
	}
	
	@Inject(method = "onMultiBlockChange", at = @At("HEAD"), cancellable = true)
	private void bhapi_onMultiBlockChange(MultiBlockChangePacket packet, CallbackInfo info) {
		info.cancel();
		Chunk chunk = this.level.getChunkFromCache(packet.chunkX, packet.chunkZ);
		int cx = packet.chunkX * 16;
		int cz = packet.chunkZ * 16;
		BlockState[] states = MultiStatesProvider.cast(packet).getStates();
		BlockStateProvider provider = BlockStateProvider.cast(chunk);
		for (int i = 0; i < packet.arraySize; ++i) {
			short s = packet.coordinateArray[i];
			int x = s >> 12 & 0xF;
			int z = s >> 8 & 0xF;
			int y = s & 0xFF;
			provider.setBlockState(x, y, z, states[i]);
			this.level.method_1498(x | cx, y, z | cz, x | cx, y, z | cz);
			this.level.callAreaEvents(x | cx, y, z | cz, x | cx, y, z | cz);
		}
	}
	
	@Inject(method = "onBlockChange", at = @At("HEAD"), cancellable = true)
	private void bhapi_onBlockChange(BlockChangePacket packet, CallbackInfo info) {
		info.cancel();
		BlockState state = CommonRegistries.BLOCKSTATES_MAP.get(packet.blockId);
		BlockStateProvider.cast(this.level).setBlockState(packet.x, packet.y, packet.z, state);
	}
}
