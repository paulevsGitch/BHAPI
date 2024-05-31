package net.bhapi.packet;

import net.bhapi.registry.CommonRegistries;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.PacketHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BlockStatesPacket extends AbstractPacket {
	private byte[] data = CommonRegistries.BLOCKSTATES_MAP.getCompressedData();
	
	@Override
	public void read(DataInputStream dataInputStream) {
		try {
			int length = dataInputStream.readInt();
			data = new byte[length];
			dataInputStream.read(data);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void write(DataOutputStream dataOutputStream) {
		try {
			dataOutputStream.writeInt(data.length);
			dataOutputStream.write(data);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void apply(PacketHandler handler) {
		CommonRegistries.BLOCKSTATES_MAP.readFromData(data);
	}
	
	@Override
	public int length() {
		return data.length + 4;
	}
}
