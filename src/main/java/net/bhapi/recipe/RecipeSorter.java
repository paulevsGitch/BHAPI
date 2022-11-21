package net.bhapi.recipe;

import net.bhapi.mixin.common.recipe.RecipeRegistryAccessor;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeRegistry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecipeSorter {
	private static final Comparator<Recipe> COMPARATOR = (r1, r2) -> {
		if (r1 instanceof ShapelessRecipe && r2 instanceof ShapedRecipe) return 1;
		if (r2 instanceof ShapelessRecipe && r1 instanceof ShapedRecipe) return -1;
		if (r2.getIngredientCount() < r1.getIngredientCount()) return -1;
		if (r2.getIngredientCount() > r1.getIngredientCount()) return 1;
		return 0;
	};
	
	public static void sort(RecipeRegistry registry) {
		RecipeRegistryAccessor accessor = (RecipeRegistryAccessor) registry;
		List<Recipe> recipes = accessor.getRecipesList();
		Collections.sort(recipes, COMPARATOR);
	}
}
