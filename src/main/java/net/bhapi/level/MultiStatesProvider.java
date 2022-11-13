package net.bhapi.level;

import net.bhapi.blockstate.BlockState;

public interface MultiStatesProvider {
	BlockState[] getStates();
	
	static MultiStatesProvider cast(Object obj) {
		return (MultiStatesProvider) obj;
	}
}
