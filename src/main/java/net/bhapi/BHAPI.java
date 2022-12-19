package net.bhapi;

import com.google.gson.JsonObject;
import net.bhapi.config.BHConfigs;
import net.bhapi.event.BHEvent;
import net.bhapi.event.EventListener;
import net.bhapi.event.EventRegistrationEvent;
import net.bhapi.mixin.common.TranslationStorageAccessor;
import net.bhapi.mixin.common.packet.AbstractPackerAccessor;
import net.bhapi.packet.BlockStatesPacket;
import net.bhapi.recipe.RecipeSorter;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.storage.Resource;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.ItemUtil;
import net.bhapi.util.JSONUtil;
import net.bhapi.util.ResourceUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.recipe.RecipeRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

public class BHAPI implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	private static BHAPI instance;
	
	@Override
	public void onInitialize() {
		log("Init BHAPI");
		instance = this;
		
		BHConfigs.load();
		
		// Betacraft proxy (tests)
		if (BHConfigs.GENERAL.getBool("network.useBetacraftProxy", true)) {
			System.setProperty("http.proxyHost", "betacraft.uk");
			System.setProperty("http.proxyPort", "11705");
		}
		AbstractPackerAccessor.callRegister(132, true, false, BlockStatesPacket.class);
		
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			System.setProperty(
				"net.java.games.input.librarypath",
				new File("../.gradle/loom-cache/natives/b1.7.3").getAbsolutePath()
			);
		}
		
		CommonRegistries.init();
		BlockUtil.init();
		ItemUtil.init();
		handleEvents();
		ItemUtil.setFrozen(true);
		ItemUtil.postProcessStacks();
		
		CommonRegistries.initRecipes();
		RecipeSorter.sort(RecipeRegistry.getInstance());
		loadTranslations();
		
		BHConfigs.save();
	}
	
	public static BHAPI getInstance() {
		return instance;
	}
	
	public static void log(Object obj) {
		LOGGER.log(Level.INFO, obj == null ? "null" : obj.toString());
	}
	
	public static void warn(String message) {
		LOGGER.warn("[WARN] " + message);
	}
	
	public static boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}
	
	public static boolean isServer() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
	}
	
	private void handleEvents() {
		processEntryPoints("bhapi:common_events", CommonRegistries.EVENT_REGISTRY);
		if (isServer()) {
			processEntryPoints("bhapi:server_events", CommonRegistries.EVENT_REGISTRY);
		}
	}
	
	private void loadTranslations() {
		List<Resource> translations = new ArrayList<>();
		FabricLoader.getInstance().getAllMods().forEach(container -> {
			String modID = container.getMetadata().getId();
			List<Resource> list = ResourceUtil.getResources("/assets/" + modID + "/lang/", "en_us.json");
			translations.addAll(list);
		});
		
		TranslationStorageAccessor accessor = (TranslationStorageAccessor) TranslationStorage.getInstance();
		Properties properties = accessor.getProperties();
		
		translations.forEach(resource -> {
			try {
				JsonObject json = JSONUtil.read(resource.getStream());
				resource.close();
				json.keySet().forEach(key -> {
					String value = json.get(key).getAsString();
					properties.put(key, value);
				});
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static void processEntryPoints(String path, Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> eventRegistry) {
		Map<Class<? extends BHEvent>, List<Pair<Object, Method>>> events = new HashMap<>();
		FabricLoader.getInstance().getEntrypointContainers(path, Object.class).forEach(entrypointContainer -> {
			Object entrypoint = entrypointContainer.getEntrypoint();
			Arrays.stream(entrypoint.getClass().getDeclaredMethods())
				  .filter(method -> method.isAnnotationPresent(EventListener.class))
				  .forEach(method -> {
					  method.setAccessible(true);
					  Class<?>[] parameters = method.getParameterTypes();
					  if (parameters.length == 1 && BHEvent.class.isAssignableFrom(parameters[0])) {
						  Class<? extends BHEvent> event = (Class<? extends BHEvent>) parameters[0];
						  if (!eventRegistry.containsKey(event)) return;
						  List<Pair<Object, Method>> pairs = events.computeIfAbsent(event, i -> new ArrayList<>());
						  pairs.add(Pair.of(entrypoint, method));
					  }
				  });
		});
		
		List<Pair<Object, Method>> registerEvents = events.get(EventRegistrationEvent.class);
		if (registerEvents != null && !registerEvents.isEmpty()) {
			EventRegistrationEvent event = new EventRegistrationEvent(eventRegistry);
			registerEvents.forEach(pair -> {
				Object entrypoint = pair.first();
				Method method = pair.second();
				try {
					method.invoke(entrypoint, event);
				}
				catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			});
		}
		events.remove(EventRegistrationEvent.class);
		
		events.keySet()
			  .stream()
			  .map(eventRegistry::get)
			  .filter(Objects::nonNull)
			  .map(Supplier::get)
			  .sorted()
			  .forEach(event -> {
				  String name = event.getClass().getName();
				  name = name.substring(name.lastIndexOf('.') + 1);
				  log("[EVENT] " + name);
				  events.get(event.getClass())
						.stream()
						.sorted(Comparator.comparingInt(p -> p.second().getAnnotation(EventListener.class).priority()))
						.forEach(pair -> {
							Object entrypoint = pair.first();
							Method method = pair.second();
							try {
								method.invoke(entrypoint, event);
							}
							catch (IllegalAccessException | InvocationTargetException e) {
								e.printStackTrace();
							}
						});
			  });
	}
}
