package net.bhapi;

import net.bhapi.block.LegacyBlockInfo;
import net.bhapi.event.BHEvent;
import net.bhapi.event.EventListener;
import net.bhapi.event.EventRegistrationEvent;
import net.bhapi.mixin.common.AbstractPackerAccessor;
import net.bhapi.packet.BlockStatesPacket;
import net.bhapi.registry.DefaultRegistries;
import net.bhapi.util.BlockUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class BHAPI implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	private static BHAPI instance;
	
	@Override
	public void onInitialize() {
		instance = this;
		
		AbstractPackerAccessor.callRegister(132, true, false, BlockStatesPacket.class);
		
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			System.setProperty(
				"net.java.games.input.librarypath",
				new File("../.gradle/loom-cache/natives/b1.7.3").getAbsolutePath()
			);
		}
		
		LegacyBlockInfo.init();
		DefaultRegistries.initBlocks();
		DefaultRegistries.initEvents();
		BlockUtil.init();
		handleEvents();
	}
	
	public static BHAPI getInstance() {
		return instance;
	}
	
	public static void log(Object obj) {
		LOGGER.log(Level.INFO, obj == null ? "null" : obj.toString());
	}
	
	public static void warn(String message) {
		LOGGER.warn(message);
	}
	
	@SuppressWarnings("unchecked")
	private void handleEvents() {
		Map<Class<? extends BHEvent>, List<Pair<Object, Method>>> events = new HashMap<>();
		FabricLoader.getInstance().getEntrypointContainers("bhapi:events", Object.class).forEach(entrypointContainer -> {
			Object entrypoint = entrypointContainer.getEntrypoint();
			Arrays.stream(entrypoint.getClass().getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(EventListener.class))
				.forEach(method -> {
					Class<?>[] parameters = method.getParameterTypes();
					if (parameters.length == 1 && BHEvent.class.isAssignableFrom(parameters[0])) {
						Class<? extends BHEvent> event = (Class<? extends BHEvent>) parameters[0];
						List<Pair<Object, Method>> pairs = events.computeIfAbsent(event, i -> new ArrayList<>());
						pairs.add(Pair.of(entrypoint, method));
					}
				});
		});
		
		List<Pair<Object, Method>> registerEvents = events.get(EventRegistrationEvent.class);
		if (!registerEvents.isEmpty()) {
			EventRegistrationEvent event = new EventRegistrationEvent(DefaultRegistries.EVENT_REGISTRY);
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
		
		events.keySet()
			.stream()
			.map(DefaultRegistries.EVENT_REGISTRY::get)
			.filter(Objects::nonNull)
			.map(Supplier::get)
			.sorted()
			.forEach(event ->
				events.get(event.getClass()).stream().sorted(
					Comparator.comparingInt(p -> p.second().getAnnotation(EventListener.class).priority())
				).forEach(pair -> {
					Object entrypoint = pair.first();
					Method method = pair.second();
					try {
						method.invoke(entrypoint, event);
					}
					catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
				})
			);
	}
}
