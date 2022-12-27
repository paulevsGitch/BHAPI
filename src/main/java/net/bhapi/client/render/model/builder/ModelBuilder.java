package net.bhapi.client.render.model.builder;

import net.bhapi.client.render.model.CustomModel;
import net.bhapi.client.render.model.FaceGroup;
import net.bhapi.client.render.model.ModelQuad;
import net.bhapi.storage.EnumArray;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ModelBuilder {
	protected static final ModelBuilder INSTANCE = new ModelBuilder();
	private final EnumArray<FaceGroup, List<ModelQuad>> groups = new EnumArray<>(FaceGroup.class);
	
	private ModelBuilder() {}
	
	/**
	 * Start model building process
	 */
	public static ModelBuilder start() {
		INSTANCE.groups.forEach(List::clear);
		return INSTANCE;
	}
	
	/**
	 * Adds new quad to model
	 * @param quad {@link ModelQuad} to add
	 * @param group {@link FaceGroup} for culling
	 */
	public ModelBuilder addQuad(ModelQuad quad, FaceGroup group) {
		groups.getOrCreate(group, g -> new ArrayList<>()).add(quad);
		return this;
	}
	
	/**
	 * Start cuboid adding process.
	 * @return {@link ModelCuboidBuilder} instance.
	 */
	public ModelCuboidBuilder cuboid() {
		return ModelCuboidBuilder.start();
	}
	
	/**
	 * Build new {@link CustomModel} from builder data.
	 */
	public CustomModel build() {
		return new CustomModel(buildGroups());
	}
	
	public EnumArray<FaceGroup, ModelQuad[]> buildGroups() {
		EnumArray<FaceGroup, ModelQuad[]> baked = new EnumArray<>(FaceGroup.class);
		for (FaceGroup group: FaceGroup.VALUES) {
			List<ModelQuad> list = groups.get(group);
			if (list != null && !list.isEmpty()) {
				baked.set(group, list.toArray(new ModelQuad[0]));
			}
		}
		return baked;
	}
}
