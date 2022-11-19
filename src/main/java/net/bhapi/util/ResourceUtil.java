package net.bhapi.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceUtil {
	private static final ImmutableList<File> MODS;
	
	@Nullable
	public static InputStream getStream(String resource) {
		String search = resource.startsWith("/") ? resource.substring(1) : resource;
		for (File mod: MODS) {
			if (mod.isDirectory()) {
				File file = new File(mod, search);
				if (file.exists() && file.isFile()) {
					try {
						return new FileInputStream(file);
					}
					catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				try {
					ZipFile zipFile = new ZipFile(mod);
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					InputStream stream = null;
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						if (entry.getName().equals(search)) {
							stream = zipFile.getInputStream(entry);
							break;
						}
					}
					zipFile.close();
					if (stream != null) {
						return stream;
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ResourceUtil.class.getResourceAsStream(resource);
	}
	
	public static List<String> getResources(String path, String mask) {
		List<String> resources = new ArrayList<>();
		String resource;
		try {
			InputStream stream = getStream(path);
			if (stream != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				while ((resource = reader.readLine()) != null) {
					if (resource.endsWith(mask)) resources.add(path + resource);
				}
				reader.close();
				stream.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return resources;
	}
	
	static {
		Map<String, Integer> order = new HashMap<>();
		Map<String, Path> locations = new HashMap<>();
		Collection<ModContainer> list = FabricLoader.getInstance().getAllMods();
		
		list.forEach(modContainer -> {
			order.put(modContainer.getMetadata().getId(), 0);
			locations.put(modContainer.getMetadata().getId(), modContainer.getOrigin().getPaths().get(0));
		});
		
		list.forEach(modContainer -> modContainer.getMetadata().getDependencies().forEach(modDependency -> {
			String id = modDependency.getModId();
			int priority = order.getOrDefault(id, 0) + 1; // Mods loaded first should be checked last
			order.put(id, priority);
		}));
		
		order.remove("fabricloader");
		
		ImmutableList.Builder<File> builder = ImmutableList.builder();
		builder.addAll(order.keySet().stream().map(
			key -> Pair.of(order.get(key), locations.get(key))
		).sorted(
			Comparator.comparingInt(Pair::first)
		).map(Pair::second).map(Path::toFile).filter(File::exists).toList());
		
		MODS = builder.build();
	}
}
