package net.bhapi.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ResourceUtil {
	private static final ImmutableList<ModContainer> MODS;
	
	@Nullable
	public static InputStream getStream(String resource) {
		for (ModContainer container: MODS) {
			InputStream stream = container.getOrigin().getClass().getResourceAsStream(resource);
			if (stream != null) return stream;
		}
		return null;
	}
	
	static {
		List<ModContainer> list = new ArrayList<>();
		list.addAll(FabricLoader.getInstance().getAllMods());
		Collections.reverse(list);
		ImmutableList.Builder<ModContainer> builder = ImmutableList.builder();
		MODS = builder.addAll(list).build();
	}
}
