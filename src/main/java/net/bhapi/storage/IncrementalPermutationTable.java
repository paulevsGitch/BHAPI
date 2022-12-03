package net.bhapi.storage;

public class IncrementalPermutationTable extends PermutationTable {
	private int count;
	
	@Override
	public short nextShort() {
		if (++count >= getSize()) {
			count = 0;
			setIncrement(getIncrement() + 1);
		}
		return super.nextShort();
	}
}
