package net.bhapi.mixin.common;

import net.minecraft.level.storage.RegionLoader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegionLoader.class)
public class RegionLoaderMixin {
	/*@ModifyVariable(method = "getRegion", at = @At("STORE"), ordinal = 2)
	private static File injected(File file) {
		String name = file.getName();
		return new File(name.substring(0, name.length() - 3) + "bhr");
	}*/
}
