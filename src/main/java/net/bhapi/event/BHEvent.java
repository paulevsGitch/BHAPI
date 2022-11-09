package net.bhapi.event;

import org.jetbrains.annotations.NotNull;

public interface BHEvent extends Comparable<BHEvent> {
	int getPriority();
	
	@Override
	default int compareTo(@NotNull BHEvent event) {
		return Integer.compare(getPriority(), event.getPriority());
	}
}
