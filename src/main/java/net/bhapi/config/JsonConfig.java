package net.bhapi.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

public class JsonConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private boolean requireSave;
	private JsonObject root;
	private final File file;
	
	public JsonConfig(String name) {
		file = new File("./config/" + name + ".json");
	}
	
	public void load() {
		if (file.exists()) {
			try {
				FileReader reader = new FileReader(file);
				root = GSON.fromJson(reader, JsonObject.class);
				reader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (root == null) {
			requireSave = true;
			root = new JsonObject();
		}
	}
	
	public void save() {
		if (requireSave) {
			file.getParentFile().mkdirs();
			try {
				FileWriter writer = new FileWriter(file);
				JsonWriter jsonWriter = GSON.newJsonWriter(writer);
				jsonWriter.setIndent("\t");
				GSON.toJson(root, jsonWriter);
				jsonWriter.close();
				writer.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private JsonElement get(String path, Supplier<JsonElement> def) {
		String[] parts = path.split("\\.");
		JsonElement element = root;
		final byte last = (byte) (parts.length - 1);
		for (byte i = 0; i < parts.length; i++) {
			String name = parts[i];
			JsonObject obj = element.getAsJsonObject();
			if (!obj.has(name)) {
				obj.add(name, i == last ? def.get() : new JsonObject());
				requireSave = true;
			}
			element = element.getAsJsonObject().get(name);
		}
		return element;
	}
	
	private void set(String path, JsonElement value) {
		String[] parts = path.split("\\.");
		JsonElement element = root;
		byte last = (byte) (parts.length - 1);
		for (byte i = 0; i < last; i++) {
			String name = parts[i];
			JsonObject obj = element.getAsJsonObject();
			if (!obj.has(name)) {
				obj.add(name, new JsonObject());
			}
			element = element.getAsJsonObject().get(name);
		}
		requireSave = true;
		element.getAsJsonObject().add(parts[last], value);
	}
	
	public int getInt(String path, int def) {
		return get(path, () -> new JsonPrimitive(def)).getAsInt();
	}
	
	public void setInt(String path, int value) {
		set(path, new JsonPrimitive(value));
	}
	
	public boolean getBool(String path, boolean def) {
		return get(path, () -> new JsonPrimitive(def)).getAsBoolean();
	}
	
	public void setBool(String path, boolean value) {
		set(path, new JsonPrimitive(value));
	}
	
	public float getFloat(String path, float def) {
		return get(path, () -> new JsonPrimitive(def)).getAsFloat();
	}
	
	public void setFloat(String path, float value) {
		set(path, new JsonPrimitive(value));
	}
}
