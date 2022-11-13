package net.bhapi.mixin.common.packet;

import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.InventoryUpdate0x68S2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Mixin(InventoryUpdate0x68S2CPacket.class)
public abstract class InventoryUpdatePacketMixin extends AbstractPacket {
	@Shadow public int containerId;
	@Shadow public ItemStack[] stacks;
	
	@Inject(method = "read", at = @At("HEAD"), cancellable = true)
	private void bhapi_read(DataInputStream dataInputStream, CallbackInfo info) throws IOException {
		info.cancel();
		this.containerId = dataInputStream.readByte();
		short size = dataInputStream.readShort();
		this.stacks = new ItemStack[size];
		for (short i = 0; i < size; ++i) {
			String name;
			try {
				name = readString(dataInputStream, 256);
			}
			catch (Exception e) {
				continue;
			}
			if (name.isEmpty()) continue;
			Identifier id = Identifier.make(name);
			if (id == null) continue;
			byte count = dataInputStream.readByte();
			short damage = dataInputStream.readShort();
			BaseItem item = CommonRegistries.ITEM_REGISTRY.get(id);
			if (item == null) continue;
			this.stacks[i] = new ItemStack(item, count, damage);
		}
	}
	
	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void bhapi_write(DataOutputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		stream.writeByte(this.containerId);
		stream.writeShort(this.stacks.length);
		for (short i = 0; i < this.stacks.length; ++i) {
			if (this.stacks[i] == null) {
				stream.writeShort(0);
				continue;
			}
			
			Identifier id = CommonRegistries.ITEM_REGISTRY.getID(this.stacks[i].getType());
			if (id == null) {
				stream.writeShort(0);
				continue;
			}
			
			writeString(id.toString(), stream);
			stream.writeByte((byte) this.stacks[i].count);
			stream.writeShort((short) this.stacks[i].getDamage());
		}
	}
	
	@Inject(method = "length", at = @At("HEAD"), cancellable = true)
	private void bhapi_length(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(3 + stacks.length * 16);
	}
}
