package net.bhapi.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonUtil {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
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
}
