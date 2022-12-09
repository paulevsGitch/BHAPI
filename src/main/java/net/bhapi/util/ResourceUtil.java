package net.bhapi.util;

import net.bhapi.storage.Resource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin.Kind;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceUtil {
	private static final Map<File, ZipFile> ZIP_FILES = new HashMap<>();
	private static final ImmutableList<File> MODS;
	
	public static Resource getResource(String resource, String extension) {
		List<Resource> resources = getResources(resource, extension);
		return resources.size() > 0 ? resources.get(0) : null;
	}
	
	public static List<Resource> getResources(String resource, String extension) {
		List<Resource> result = new ArrayList<>();
		String search = resource.startsWith("/") ? resource.substring(1) : resource;
		for (File mod: MODS) {
			if (mod.isDirectory()) {
				File file = new File(mod, search);
				if (file.exists()) {
					if (file.isFile() && file.getName().endsWith(extension)) {
						try {
							InputStream stream = new FileInputStream(file);
							String path = file.getAbsolutePath();
							result.add(new Resource(stream, path));
						}
						catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
					else if (file.isDirectory()) {
						Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(File::isFile).filter(
							f -> f.getName().endsWith(extension)
						).forEach(f -> {
							try {
								InputStream stream = new FileInputStream(f);
								String path = f.getAbsolutePath();
								result.add(new Resource(stream, path));
							}
							catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						});
					}
				}
			}
			else {
				try {
					ZipFile zipFile = ZIP_FILES.computeIfAbsent(mod, f -> {
						try {
							return new ZipFile(f);
						}
						catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					});
					if (zipFile != null) {
						Enumeration<? extends ZipEntry> entries = zipFile.entries();
						while (entries.hasMoreElements()) {
							ZipEntry entry = entries.nextElement();
							String name = entry.getName();
							if (name.startsWith(search) && (extension == null || name.endsWith(extension))) {
								InputStream stream = zipFile.getInputStream(entry);
								result.add(new Resource(stream, name));
							}
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	static {
		Map<String, Integer> order = new HashMap<>();
		Map<String, Path> locations = new HashMap<>();
		Collection<ModContainer> list = FabricLoader.getInstance().getAllMods().stream().filter(
			modContainer -> modContainer.getOrigin().getKind() == Kind.PATH
		).toList();
		
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
		).filter(
			pair -> pair.first() != null && pair.second() != null
		).sorted(
			Comparator.comparingInt(Pair::first)
		).map(Pair::second).map(Path::toFile).filter(File::exists).toList());
		
		MODS = builder.build();
	}
}
