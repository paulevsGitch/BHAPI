package net.bhapi.event_old;

import net.bhapi.event_old.context.EventContext;
import org.jetbrains.annotations.NotNull;

public abstract class Event implements Comparable<Event> {
	private final int priority;
	
	public Event(int priority) {
		this.priority = priority;
	}
	
	abstract void execute(EventContext context);
	
	@Override
	public int compareTo(@NotNull Event event) {
		return Integer.compare(priority, event.priority);
	}
}
