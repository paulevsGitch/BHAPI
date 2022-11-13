package net.bhapi.event;

public class AfterBlockAndItemsEvent implements BHEvent {
	@Override
	public int getPriority() {
		return EventPriorities.AFTER_BLOCK_AND_ITEMS;
	}
}
