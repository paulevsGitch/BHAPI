package net.bhapi.level;

import net.bhapi.interfaces.NBTSerializable;
import net.bhapi.util.BlockUtil;
import net.minecraft.util.io.CompoundTag;

import java.nio.ByteBuffer;

public class StatesLoader implements NBTSerializable {
	private static final int[] MASKS = new int[] {
		Byte.MAX_VALUE - Byte.MIN_VALUE,
		Short.MAX_VALUE - Short.MIN_VALUE
	};
	private static final int[] SIZES = new int[] { 4096, 4096 << 1, 4096 << 2 };
	private final ByteBuffer[] buffers = new ByteBuffer[] {
		ByteBuffer.allocate(SIZES[0]),
		ByteBuffer.allocate(SIZES[1]),
		ByteBuffer.allocate(SIZES[2])
	};
	
	private final int[] data = new int[4096];
	private boolean isEmpty;
	private int min, rawID;
	private byte type;
	
	public void fillFrom(int[] states) {
		min = Integer.MAX_VALUE;
		int max = 0;
		int airID = BlockUtil.AIR_STATE.getID();
		short countAir = 0;
		for (short i = 0; i < 4096; i++) {
			rawID = states[i];
			if (rawID == airID) countAir++;
			if (min > rawID) min = rawID;
			if (max < rawID) max = rawID;
			data[i] = rawID;
		}
		isEmpty = countAir == 4096;
		if (isEmpty) return;
		type = getBufferType(max - min);
		buffers[type].rewind();
		for (short i = 0; i < 4096; i++) {
			put(buffers[type], data[i] - min);
		}
		buffers[type].rewind();
	}
	
	public void fillTo(int[] states) {
		if (isEmpty) return;
		for (short i = 0; i < 4096; i++) {
			rawID = get(buffers[type]) + min;
			states[i] = rawID;
		}
	}
	
	private byte getBufferType(int size) {
		for (byte i = 0; i < 2; i++) {
			if (MASKS[i] >= size) return i;
		}
		return (byte) 2;
	}
	
	private byte getArrayBufferType(int size) {
		for (byte i = 0; i < 2; i++) {
			if (SIZES[i] == size) return i;
		}
		return (byte) 2;
	}
	
	private void put(ByteBuffer buffer, int value) {
		switch (type) {
			case 0 -> buffer.put((byte) value);
			case 1 -> buffer.putShort((short) value);
			case 2 -> buffer.putInt(value);
		}
	}
	
	private int get(ByteBuffer buffer) {
		int value = 0;
		switch (type) {
			case 0 -> value = buffer.get() & MASKS[0];
			case 1 -> value = buffer.getShort() & MASKS[1];
			case 2 -> value = buffer.getInt();
		}
		return value;
	}
	
	@Override
	public void saveToNBT(CompoundTag tag) {
		if (isEmpty) return;
		byte[] src = buffers[type].array();
		byte[] res = new byte[src.length];
		System.arraycopy(src, 0, res, 0, src.length);
		tag.put("data", res);
		if (min > 0) tag.put("min", min);
	}
	
	@Override
	public void loadFromNBT(CompoundTag tag) {
		byte[] data = tag.getByteArray("data");
		isEmpty = data == null || data.length == 0;
		if (isEmpty) return;
		type = getArrayBufferType(data.length);
		min = tag.getInt("min");
		buffers[type].rewind();
		buffers[type].put(data);
		buffers[type].rewind();
	}
	
	public boolean isEmpty() {
		return isEmpty;
	}
}
