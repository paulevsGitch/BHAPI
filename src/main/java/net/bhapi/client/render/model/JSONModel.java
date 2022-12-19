package net.bhapi.client.render.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.bhapi.BHAPI;
import net.bhapi.client.render.model.builder.ModelBuilder;
import net.bhapi.client.render.model.builder.ModelCuboidBuilder;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Vec3F;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.Identifier;
import net.bhapi.util.JSONUtil;

import java.util.HashMap;
import java.util.Map;

public class JSONModel extends CustomModel {
	private static final Map<Identifier, JsonObject> MODEL_CACHE = new HashMap<>();
	private static final Map<String, BlockDirection> FACES = new HashMap<>();
	
	public JSONModel(Identifier path, Map<String, Integer> textureIndexMap) {
		super(loadModel(path, textureIndexMap));
	}
	
	private static JsonObject getModel(Identifier id) {
		JsonObject obj = MODEL_CACHE.get(id);
		if (obj == null) {
			obj = JSONUtil.readFromSource(id);
			if (obj == null) {
				BHAPI.warn("No JSON model with name " + id);
				return null;
			}
			if (obj.has("parent")) {
				String path = obj.get("parent").getAsString();
				int index = path.indexOf(':');
				if (index > 0) path = path.substring(0, index) + "models/" + path.substring(index + 1);
				else path = "models/" + path;
				JsonObject parent = getModel(Identifier.make(path));
				if (parent != null) obj = JSONUtil.merge(obj, parent);
			}
			MODEL_CACHE.put(id, obj);
		}
		return obj;
	}
	
	private static EnumArray<FaceGroup, ModelQuad[]> loadModel(Identifier path, Map<String, Integer> textureIndexMap) {
		JsonObject json = getModel(path);
		if (json == null) return new EnumArray<>(FaceGroup.class);
		
		ModelBuilder builder = ModelBuilder.start();
		json.get("elements").getAsJsonArray().forEach(element -> {
			JsonObject entry = element.getAsJsonObject();
			Vec3F min = JSONUtil.vectorFromArray(entry.getAsJsonArray("from"));
			Vec3F max = JSONUtil.vectorFromArray(entry.getAsJsonArray("to"));
			JsonObject faces = entry.get("faces").getAsJsonObject();
			ModelCuboidBuilder cuboid = builder.cuboid().rescale(true).setMinPos(
				min.x, min.y, min.z
			).setMaxPos(
				max.x, max.y, max.z
			);
			if (entry.has("shade")) cuboid.setShade(entry.get("shade").getAsBoolean());
			if (entry.has("rotation")) {
				entry = entry.getAsJsonObject("rotation");
				Vec3F center = JSONUtil.vectorFromArray(entry.getAsJsonArray("origin"));
				char axis = entry.get("axis").getAsString().charAt(0);
				float angle = (float) Math.toRadians(entry.get("angle").getAsFloat());
				cuboid.setRotation(center.x, center.y, center.z, axis, angle);//cuboid.allFaces();
			}
			faces.keySet().forEach(key -> {
				JsonObject face = faces.getAsJsonObject(key);
				BlockDirection facing = FACES.get(key);
				JsonArray uv = face.getAsJsonArray("uv");
				String textureName = face.get("texture").getAsString();
				if (textureName.startsWith("#")) textureName = textureName.substring(1);
				int texture = textureIndexMap.getOrDefault(textureName, 0);
				cuboid.addFace(facing).setTextureIndex(facing, texture).setUV(
					facing,
					uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
					uv.get(2).getAsFloat(), uv.get(3).getAsFloat()
				);
				if (face.has("cullface")) {
					BlockDirection dir = FACES.get(face.get("cullface").getAsString());
					FaceGroup group = FaceGroup.getFromFacing(dir);
					cuboid.setFaceGroup(dir, group);
				}
			});
			cuboid.build();
		});
		
		return builder.buildGroups();
	}
	
	static {
		FACES.put("up", BlockDirection.POS_Y);
		FACES.put("down", BlockDirection.NEG_Y);
		// Legacy MC have different X and Z coordinates
		FACES.put("north", BlockDirection.NEG_Z);
		FACES.put("south", BlockDirection.POS_Z);
		FACES.put("east", BlockDirection.NEG_X);
		FACES.put("west", BlockDirection.POS_X);
	}
}
