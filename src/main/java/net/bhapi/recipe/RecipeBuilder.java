package net.bhapi.recipe;

import net.bhapi.mixin.common.recipe.RecipeRegistryAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBuilder {
	private static final RecipeBuilder INSTANCE = new RecipeBuilder();
	
	private Map<Character, ItemStack> shaped = new HashMap<>(9);
	private List<ItemStack> shapeless = new ArrayList<>(9);
	private ItemStack result;
	private String[] shape;
	
	private RecipeBuilder() {}
	
	public static RecipeBuilder start(ItemStack result) {
		INSTANCE.shapeless.clear();
		INSTANCE.result = result;
		INSTANCE.shaped.clear();
		INSTANCE.shape = null;
		return INSTANCE;
	}
	
	public RecipeBuilder setShape(String... shape) {
		this.shape = shape;
		return this;
	}
	
	public RecipeBuilder addIngredient(ItemStack stack) {
		this.shapeless.add(stack);
		return this;
	}
	
	public RecipeBuilder addIngredient(char key, ItemStack stack) {
		this.shaped.put(key, stack);
		return this;
	}
	
	public void build(RecipeRegistry registry) {
		RecipeRegistryAccessor accessor = (RecipeRegistryAccessor) registry;
		if (shape == null) {
			accessor.callAddShapelessRecipe(result, shapeless.toArray());
		}
		else {
			Object[] recipe = new Object[shape.length + (shaped.size() << 1)];
			int[] index = new int[] {0};
			for (String line: shape) {
				recipe[index[0]++] = line;
			}
			shaped.forEach((key, value) -> {
				recipe[index[0]++] = key;
				recipe[index[0]++] = value;
			});
			accessor.callAddShapedRecipe(result, recipe);
		}
	}
}
