package net.bhapi.mixin.common;

import net.bhapi.blockstate.BlockState;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.ItemProvider;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.block.BaseBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketHandler;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.ItemEntitySpawn0x15S2CPacket;
import net.minecraft.util.maths.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Mixin(ItemEntitySpawn0x15S2CPacket.class)
public abstract class ItemEntitySpawnPacketMixin extends AbstractPacket implements ItemProvider {
	@Shadow public int entityId;
	@Shadow public int count;
	@Shadow public int damage;
	@Shadow public int x;
	@Shadow public int y;
	@Shadow public int z;
	@Shadow public byte velocityX;
	@Shadow public byte velocityY;
	@Shadow public byte velocityZ;
	
	@Unique private BaseItem bhapi_item;
	@Unique private ItemEntity bhapi_entity;
	
	@Inject(method = "<init>(Lnet/minecraft/entity/ItemEntity;)V", at = @At("TAIL"))
	private void bhapi_onPacketInit(ItemEntity entity, CallbackInfo info) {
		setItem(entity.stack.getType());
		bhapi_entity = entity;
	}
	
	@Inject(method = "read", at = @At("HEAD"), cancellable = true)
	private void bhapi_read(DataInputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		this.entityId = stream.readInt();
		String name = readString(stream, 256);
		this.count = stream.readByte();
		this.damage = stream.readShort();
		this.x = stream.readInt();
		this.y = stream.readInt();
		this.z = stream.readInt();
		this.velocityX = stream.readByte();
		this.velocityY = stream.readByte();
		this.velocityZ = stream.readByte();
		bhapi_item = CommonRegistries.ITEM_REGISTRY.get(Identifier.make(name));
	}
	
	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void bhapi_write(DataOutputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		stream.writeInt(this.entityId);
		Identifier id = CommonRegistries.ITEM_REGISTRY.getID(bhapi_item);
		if (id == null && bhapi_item instanceof BHBlockItem) {
			BlockState state = ((BHBlockItem) bhapi_item).getState();
			id = CommonRegistries.BLOCK_REGISTRY.getID(state.getBlock());
			System.out.println(id);
			if (bhapi_entity != null) {
				BaseItem item = CommonRegistries.ITEM_REGISTRY.get(id);
				if (item == null) System.out.println("Null item " + id);
				else bhapi_entity.stack = new ItemStack(item, bhapi_entity.stack.count);
			}
		}
		writeString(id.toString(), stream);
		stream.writeByte(this.count);
		stream.writeShort(this.damage);
		stream.writeInt(this.x);
		stream.writeInt(this.y);
		stream.writeInt(this.z);
		stream.writeByte(this.velocityX);
		stream.writeByte(this.velocityY);
		stream.writeByte(this.velocityZ);
	}
	
	@Inject(method = "length", at = @At("HEAD"), cancellable = true)
	private void bhapi_length(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(32);
	}
	
	@Override
	public BaseItem getItem() {
		return bhapi_item;
	}
	
	@Override
	public void setItem(BaseItem item) {
		bhapi_item = item;
	}
}
