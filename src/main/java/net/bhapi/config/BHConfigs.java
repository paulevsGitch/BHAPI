package net.bhapi.config;

import java.util.ArrayList;
import java.util.List;

public class BHConfigs {
	private static final List<JsonConfig> CONFIGS = new ArrayList<>();
	public static final JsonConfig GENERAL = add(new JsonConfig("bhapi/general"));
	
	private static JsonConfig add(JsonConfig config) {
		CONFIGS.add(config);
		return config;
	}
	
	public static void save() {
		CONFIGS.forEach(JsonConfig::save);
	}
	
	public static void load() {
		CONFIGS.forEach(JsonConfig::load);
	}
}
