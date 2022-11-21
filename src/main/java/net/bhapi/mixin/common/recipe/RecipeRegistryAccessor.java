package net.bhapi.mixin.common.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(RecipeRegistry.class)
public interface RecipeRegistryAccessor {
	@Invoker("addShapedRecipe")
	void callAddShapedRecipe(ItemStack result, Object ... recipe);
	
	@Invoker("addShapelessRecipe")
	void callAddShapelessRecipe(ItemStack result, Object ... recipe);
	
	@Accessor("recipes")
	List<Recipe> getRecipesList();
}
