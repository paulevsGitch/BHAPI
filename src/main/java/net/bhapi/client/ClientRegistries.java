package net.bhapi.client;

import net.bhapi.client.event.AfterTextureLoadedEvent;
import net.bhapi.client.event.TextureLoadingEvent;
import net.bhapi.client.event.TexturesReloadEvent;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.event.BHEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ClientRegistries {
	public static final Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> EVENT_REGISTRY_PRE = new HashMap<>();
	public static final Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> EVENT_REGISTRY_POST = new HashMap<>();
	public static final Map<Class<? extends BHEvent>, Supplier<? extends BHEvent>> EVENT_REGISTRY_RELOAD = new HashMap<>();
	
	public static void init() {
		initEvents();
	}
	
	private static void initEvents() {
		EVENT_REGISTRY_PRE.put(TextureLoadingEvent.class, () -> new TextureLoadingEvent(Textures.LOADED_TEXTURES));
		EVENT_REGISTRY_POST.put(AfterTextureLoadedEvent.class, AfterTextureLoadedEvent::new);
		EVENT_REGISTRY_RELOAD.put(TexturesReloadEvent.class, () -> new TexturesReloadEvent(Textures.LOADED_TEXTURES));
	}
}
