package net.bhapi.mixin.common.packet;

import net.bhapi.level.BlockStateProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.BlockChangePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Mixin(BlockChangePacket.class)
public abstract class BlockChangePacketMixin extends AbstractPacket {
	@Shadow public int x;
	@Shadow public int y;
	@Shadow public int z;
	@Shadow public int blockId;
	
	@Environment(value= EnvType.SERVER)
	@Inject(method = "<init>(IIILnet/minecraft/level/Level;)V", at = @At("TAIL"))
	private void bhapi_onPacketInit(int x, int y, int z, Level level, CallbackInfo info) {
		this.levelPacket = true;
		this.blockId = BlockStateProvider.cast(level).getBlockState(x, y, z).getID();
	}
	
	@Inject(method = "read", at = @At("HEAD"), cancellable = true)
	private void bhapi_read(DataInputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		this.x = stream.readInt();
		this.y = stream.readInt();
		this.z = stream.readInt();
		this.blockId = stream.readInt();
	}
	
	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void bhapi_write(DataOutputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		stream.writeInt(this.x);
		stream.writeInt(this.y);
		stream.writeInt(this.z);
		stream.writeInt(this.blockId);
	}
	
	@Inject(method = "length", at = @At("HEAD"), cancellable = true)
	private void bhapi_length(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(16);
	}
}
