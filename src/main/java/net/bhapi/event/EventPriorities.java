package net.bhapi.event;

public class EventPriorities {
	// COMMON
	public static final int STARTUP = 0;
	public static final int BLOCK_REGISTRY = 1;
	public static final int ITEM_REGISTRY = 2;
	public static final int AFTER_BLOCK_AND_ITEMS = 3;
	public static final int RECIPE_REGISTRY = 4;
	
	// SERVER
	public static final int COMMAND_REGISTRY = 4;
	
	// CLIENT
	public static final int TEXTURE_LOADING = 0;
	public static final int AFTER_TEXTURE_LOADED = 0;
}
