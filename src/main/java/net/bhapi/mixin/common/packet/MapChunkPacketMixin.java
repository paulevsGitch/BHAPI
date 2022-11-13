package net.bhapi.mixin.common.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;
import net.minecraft.packet.play.MapChunk0x33S2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.DataInputStream;

@Mixin(MapChunk0x33S2CPacket.class)
public class MapChunkPacketMixin {
	@Shadow public byte[] chunkData;
	@Shadow public int sizeX;
	@Shadow public int sizeY;
	@Shadow public int sizeZ;
	
	@Environment(value= EnvType.SERVER)
	@Inject(method = "<init>(IIIIIILnet/minecraft/level/Level;)V", at = @At(
		value = "INVOKE",
		target = "Ljava/util/zip/Deflater;deflate([B)I",
		shift = Shift.BEFORE
	))
	private void bhapi_onPacketInit(int x1, int y1, int z1, int dx, int dy, int dz, Level level, CallbackInfo info) {
		this.chunkData = new byte[dx * dy * dz * 5];
	}
	
	@Inject(method = "read", at = @At(
		value = "INVOKE",
		target = "Ljava/util/zip/Inflater;setInput([B)V",
		shift = Shift.BEFORE
	))
	private void bhapi_read(DataInputStream dataInputStream, CallbackInfo info) {
		this.chunkData = new byte[this.sizeX * this.sizeY * this.sizeZ * 5];
	}
}
