package net.bhapi.client;

import net.bhapi.BHAPI;
import net.bhapi.event.BHEvent;
import net.bhapi.event.EventListener;
import net.bhapi.event.EventRegistrationEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.tinyremapper.extension.mixin.common.data.Pair;
import net.minecraft.client.Minecraft;

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

public class BHAPIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BHAPI.log("Init Client");
		ClientRegistries.init();
		handleEvents();
	}
	
	@SuppressWarnings("deprecation")
	public static Minecraft getMinecraft() {
		return (Minecraft) FabricLoader.getInstance().getGameInstance();
	}
	
	@SuppressWarnings("unchecked")
	private void handleEvents() {
		Map<Class<? extends BHEvent>, List<Pair<Object, Method>>> events = new HashMap<>();
		FabricLoader.getInstance().getEntrypointContainers("bhapi:client_events", Object.class).forEach(entrypointContainer -> {
			Object entrypoint = entrypointContainer.getEntrypoint();
			Arrays.stream(entrypoint.getClass().getDeclaredMethods())
				  .filter(method -> method.isAnnotationPresent(EventListener.class))
				  .forEach(method -> {
					  method.setAccessible(true);
					  Class<?>[] parameters = method.getParameterTypes();
					  if (parameters.length == 1 && BHEvent.class.isAssignableFrom(parameters[0])) {
						  Class<? extends BHEvent> event = (Class<? extends BHEvent>) parameters[0];
						  List<Pair<Object, Method>> pairs = events.computeIfAbsent(event, i -> new ArrayList<>());
						  pairs.add(Pair.of(entrypoint, method));
					  }
				  });
		});
		
		List<Pair<Object, Method>> registerEvents = events.get(EventRegistrationEvent.class);
		if (registerEvents != null && !registerEvents.isEmpty()) {
			EventRegistrationEvent event = new EventRegistrationEvent(ClientRegistries.EVENT_REGISTRY);
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
			  .map(ClientRegistries.EVENT_REGISTRY::get)
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
