package net.bhapi.mixin.common.packet;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.MultiStatesProvider;
import net.bhapi.registry.CommonRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.MultiBlockChange0x34S2CPacket;
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
import java.util.Arrays;

@Mixin(MultiBlockChange0x34S2CPacket.class)
public abstract class MultiBlockChangePacketMixin extends AbstractPacket implements MultiStatesProvider {
	@Shadow public int chunkX;
	@Shadow public int chunkZ;
	@Shadow public int arraySize;
	@Shadow public byte[] typeArray;
	@Shadow public byte[] metadataArray;
	@Shadow public short[] coordinateArray;
	
	@Unique private int[] bhapi_statesArray;
	@Unique private int bhapi_size;
	
	@Environment(value= EnvType.SERVER)
	@Inject(method = "<init>(II[SILnet/minecraft/level/Level;)V", at = @At("TAIL"))
	private void bhapi_onPacketInit(int cx, int xz, short[] ss, int size, Level level, CallbackInfo info) {
		this.typeArray = null;
		this.metadataArray = null;
		bhapi_statesArray = new int[size];
		Chunk chunk = level.getChunkFromCache(cx, xz);
		BlockStateProvider provider = BlockStateProvider.cast(chunk);
		for (int i = 0; i < size; ++i) {
			int x = ss[i] >> 12 & 0xF;
			int z = ss[i] >> 8 & 0xF;
			int y = ss[i] & 0xFF;
			bhapi_statesArray[i] = provider.getBlockState(x, y, z).getID();
		}
	}
	
	@Inject(method = "read", at = @At("HEAD"), cancellable = true)
	private void bhapi_read(DataInputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		this.chunkX = stream.readInt();
		this.chunkZ = stream.readInt();
		this.arraySize = stream.readShort() & 0xFFFF;
		this.coordinateArray = new short[this.arraySize];
		bhapi_statesArray = new int[this.arraySize];
		for (int i = 0; i < this.arraySize; ++i) {
			this.coordinateArray[i] = stream.readShort();
			bhapi_statesArray[i] = stream.readInt();
		}
	}
	
	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void bhapi_write(DataOutputStream stream, CallbackInfo info) throws IOException {
		info.cancel();
		bhapi_size = stream.size();
		stream.writeInt(this.chunkX);
		stream.writeInt(this.chunkZ);
		stream.writeShort((short) this.arraySize);
		for (int i = 0; i < this.arraySize; ++i) {
			stream.writeShort(this.coordinateArray[i]);
			stream.writeInt(bhapi_statesArray[i]);
		}
		bhapi_size = stream.size() - bhapi_size;
	}
	
	@Inject(method = "length", at = @At("HEAD"), cancellable = true)
	private void bhapi_length(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(bhapi_size);
	}
	
	@Unique
	@Override
	public BlockState[] getStates() {
		return Arrays.stream(bhapi_statesArray).mapToObj(CommonRegistries.BLOCKSTATES_MAP::get).toArray(BlockState[]::new);
	}
}
