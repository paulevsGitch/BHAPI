package net.bhapi.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.bhapi.BHAPI;
import net.bhapi.storage.Resource;
import net.bhapi.storage.Vec3F;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonUtil {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	/**
	 * Read {@link JsonObject} from stream, if there is no any objects or reading fails will return null.
	 * @param stream {@link InputStream} to read object from
	 * @return {@link JsonObject} or null
	 */
	public static JsonObject read(InputStream stream) {
		JsonObject obj = null;
		try {
			InputStreamReader reader = new InputStreamReader(stream);
			obj = GSON.fromJson(reader, JsonObject.class);
			reader.close();
			stream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * Read json from source path specified by {@link Identifier}. If there is no json file will return null.
	 * @param id {@link Identifier} path to load resource
	 * @return {@link JsonObject} or null
	 */
	public static JsonObject readFromSource(Identifier id) {
		String path = "/assets/" + id.getModID() + "/" + id.getName() + ".json";
		return readFromSource(path);
	}
	
	/**
	 * Read json from source path. If there is no json file will return null.
	 * @param path path to load resource
	 * @return {@link JsonObject} or null
	 */
	public static JsonObject readFromSource(String path) {
		JsonObject obj = null;
		Resource resource = ResourceUtil.getResource(path, ".json");
		if (resource != null) {
			obj = read(resource.getStream());
		}
		else {
			BHAPI.warn("Missing json: " + path);
		}
		return obj;
	}
	
	/**
	 * Converts {@link JsonArray} to a vector. Array should have only 3 entries.
	 * @param array {@link JsonArray} to read a vector
	 * @return new {@link Vec3F} instance
	 */
	public static Vec3F vectorFromArray(JsonArray array) {
		return new Vec3F(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
	}
}
