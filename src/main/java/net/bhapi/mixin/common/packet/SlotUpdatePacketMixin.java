package net.bhapi.mixin.common.packet;

import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.SlotUpdate0x67S2CPacket;
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

@Mixin(SlotUpdate0x67S2CPacket.class)
public abstract class SlotUpdatePacketMixin extends AbstractPacket {
	@Shadow public int containerId;
	@Shadow public int slotIndex;
	@Shadow public ItemStack stack;
	
	@Unique private int bhapi_size;
	
	@Inject(method = "read", at = @At("HEAD"), cancellable = true)
	private void bhapi_read(DataInputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		this.containerId = stream.readByte();
		this.slotIndex = stream.readShort();
		String name = readString(stream, 256);
		if (!name.isEmpty()) {
			BaseItem item = CommonRegistries.ITEM_REGISTRY.get(Identifier.make(name));
			if (item == null) {
				this.stack = null;
				return;
			}
			byte count = stream.readByte();
			short damage = stream.readShort();
			this.stack = new ItemStack(item, count, damage);
		}
		else {
			this.stack = null;
		}
	}
	
	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void bhapi_write(DataOutputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		bhapi_size = stream.size();
		stream.writeByte(this.containerId);
		stream.writeShort(this.slotIndex);
		if (this.stack == null) {
			stream.writeShort(0);
		}
		else {
			Identifier id = CommonRegistries.ITEM_REGISTRY.getID(this.stack.getType());
			if (id == null) {
				stream.writeShort(0);
				return;
			}
			writeString(id.toString(), stream);
			stream.writeByte(this.stack.count);
			stream.writeShort(this.stack.getDamage());
		}
		bhapi_size = stream.size() - bhapi_size;
	}
	
	@Inject(method = "length", at = @At("HEAD"), cancellable = true)
	private void bhapi_length(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(Math.min(16, bhapi_size));
	}
}
