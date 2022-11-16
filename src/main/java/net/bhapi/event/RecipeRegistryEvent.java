package net.bhapi.event;

import net.minecraft.recipe.RecipeRegistry;

public class RecipeRegistryEvent implements BHEvent {
	private final RecipeRegistry registry;
	
	public RecipeRegistryEvent(RecipeRegistry registry) {
		this.registry = registry;
	}
	
	@Override
	public int getPriority() {
		return EventPriorities.RECIPE_REGISTRY;
	}
	
	public RecipeRegistry getRegistry() {
		return registry;
	}
}
