package net.bhapi.client.render.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.bhapi.BHAPI;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Resource;
import net.bhapi.storage.Vec3F;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.Identifier;
import net.bhapi.util.JSONUtil;
import net.bhapi.util.ResourceUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OBJModel extends CustomModel {
	private static final MaterialInfo STARTING_MATERIAL = new MaterialInfo(
		0, new EnumArray<>(BlockDirection.class), false, 0, new Vec3F(0, 1, 0)
	);
	
	public OBJModel(Identifier path) {
		super(loadQuads(path));
	}
	
	private static EnumArray<FaceGroup, ModelQuad[]> loadQuads(Identifier path) {
		EnumArray<FaceGroup, ModelQuad[]> groups = new EnumArray<>(FaceGroup.class);
		
		JsonObject json = JSONUtil.readFromSource(path);
		if (json == null) {
			BHAPI.warn("Model " + path + " don't exists");
			return groups;
		}
		
		Vec3F offset = json.has("offset") ? JSONUtil.vectorFromArray(json.getAsJsonArray("offset")) : new Vec3F();
		String linkedOBJ = json.get("linkedOBJ").getAsString();
		
		JsonObject preMaterials = json.getAsJsonObject("materials");
		Map<String, MaterialInfo> materials = new HashMap<>();
		
		preMaterials.keySet().forEach(name -> {
			JsonObject entry = preMaterials.get(name).getAsJsonObject();
			int textureIndex = entry.get("textureIndex").getAsInt();
			boolean shade = entry.has("shade") && entry.get("shade").getAsBoolean();
			int tintIndex = entry.has("tintIndex") ? entry.get("tintIndex").getAsInt() : -1;
			Vec3F normal = entry.has("normal") ? JSONUtil.vectorFromArray(entry.getAsJsonArray("normal")) : null;
			
			EnumArray<BlockDirection, Boolean> cullingMap = new EnumArray<>(BlockDirection.class);
			for (BlockDirection dir: BlockDirection.VALUES) cullingMap.set(dir, false);
			
			if (entry.has("culling")) {
				JsonElement culling = entry.get("culling");
				if (culling.isJsonPrimitive()) {
					String value = culling.getAsString();
					boolean cull = "auto".equals(value);
					for (BlockDirection dir: BlockDirection.VALUES) cullingMap.set(dir, cull);
				}
				else if (culling.isJsonObject()) {
					JsonObject preCull = culling.getAsJsonObject();
					preCull.keySet().forEach(key -> {
						BlockDirection dir = BlockDirection.getByName(key);
						boolean cull = preCull.get(key).getAsBoolean();
						cullingMap.set(dir, cull);
					});
					for (BlockDirection dir : BlockDirection.VALUES) {
						String dirName = dir.toString();
						cullingMap.set(dir, preCull.has(dirName) && preCull.get(dirName).getAsBoolean());
					}
				}
			}
			
			materials.put(name, new MaterialInfo(textureIndex, cullingMap, shade, tintIndex, normal));
		});
		
		EnumArray<FaceGroup, List<ModelQuad>> quads = new EnumArray<>(FaceGroup.class);
		
		String objPath = "/assets/" + path.getModID() + "/models/" + linkedOBJ + ".obj";
		Resource resource = ResourceUtil.getResource(objPath, ".obj");
		if (resource != null) {
			loadOBJ(resource, quads, materials, offset);
		}
		else {
			BHAPI.warn("Model " + objPath + " is missing");
		}
		
		quads.forEach((dir, group) -> groups.set(dir, group.toArray(new ModelQuad[0])));
		
		return groups;
	}
	
	private static void loadOBJ(Resource resource, EnumArray<FaceGroup, List<ModelQuad>> quads, Map<String, MaterialInfo> materials, Vec3F offset) {
		List<Float> vertexData = new ArrayList<>(12);
		List<Float> uvData = new ArrayList<>(8);
		
		List<Integer> vertexIndex = new ArrayList<>(4);
		List<Integer> uvIndex = new ArrayList<>(4);
		
		try {
			InputStreamReader streamReader = new InputStreamReader(resource.getStream());
			BufferedReader bufferedReader = new BufferedReader(streamReader);
			String string;
			
			MaterialInfo activeMaterial = STARTING_MATERIAL;
			if (materials.size() == 1) activeMaterial = materials.values().stream().findFirst().get();
			while ((string = bufferedReader.readLine()) != null) {
				if (string.startsWith("usemtl ")) {
					String materialName = string.substring(7);
					activeMaterial = materials.get(materialName);
					if (activeMaterial == null) {
						BHAPI.warn("No material with name " + materialName + " in model " + resource.getName());
						activeMaterial = STARTING_MATERIAL;
					}
				}
				else if (string.startsWith("vt ")) {
					String[] uv = string.split(" ");
					uvData.add(Float.parseFloat(uv[1]));
					uvData.add(Float.parseFloat(uv[2]));
				}
				else if (string.startsWith("v ")) {
					String[] vert = string.split(" ");
					for (int i = 1; i < 4; i++) {
						vertexData.add(Float.parseFloat(vert[i]));
					}
				}
				else if (string.startsWith("f ")) {
					String[] members = string.split(" ");
					if (members.length != 5) {
						String type = members.length < 5 ? "triangles" : "n-gons";
						BHAPI.warn("Only quads in OBJ models are supported! Model " + resource.getName() + " has " + type);
						continue;
					}
					vertexIndex.clear();
					uvIndex.clear();
					
					for (int i = 1; i < members.length; i++) {
						String member = members[i];
						
						if (member.contains("/")) {
							String[] sub = member.split("/");
							vertexIndex.add(Integer.parseInt(sub[0]) - 1);
							uvIndex.add(Integer.parseInt(sub[1]) - 1);
						}
						else {
							vertexIndex.add(Integer.parseInt(member) - 1);
						}
					}
					
					ModelQuad quad = new ModelQuad(activeMaterial.textureIndex);
					quad.setTintIndex(activeMaterial.tintIndex);
					quad.setAO(activeMaterial.shade);
					quad.setNormal(activeMaterial.normal);
					
					boolean hasUV = !uvIndex.isEmpty();
					for (byte i = 0; i < 4; i++) {
						int index = vertexIndex.get(i) * 3;
						float x = vertexData.get(index++) + offset.x;
						float y = vertexData.get(index++) + offset.y;
						float z = vertexData.get(index) + offset.z;
						quad.setPosition(i, x, y, z);
						if (hasUV) {
							index = uvIndex.get(i) << 1;
							float u = uvData.get(index++);
							float v = (1.0F - uvData.get(index));
							quad.setUV(i, u, v);
						}
					}
					
					FaceGroup dir = quad.getCullingGroup();
					quads.getOrCreate(dir, key -> new ArrayList<>()).add(quad);
					/*if (dir != null && activeMaterial.culling.get(dir)) {
						quads.getOrCreate(dir, key -> new ArrayList<>()).add(quad);
					}
					else {
						quads.getOrCreate(FaceGroup.NONE, key -> new ArrayList<>()).add(quad);
					}*/
				}
			}
			
			bufferedReader.close();
			streamReader.close();
			resource.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private record MaterialInfo(
		int textureIndex,
		EnumArray<BlockDirection, Boolean> culling,
		boolean shade,
		int tintIndex,
		Vec3F normal
	) {}
	
	static {
		EnumArray<BlockDirection, Boolean> culling = STARTING_MATERIAL.culling;
		for (BlockDirection dir: BlockDirection.VALUES) culling.set(dir, false);
	}
}
