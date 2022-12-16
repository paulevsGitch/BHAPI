package net.bhapi.client.render.level;

import net.bhapi.client.render.texture.RenderLayer;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Vec3I;
import net.minecraft.level.BlockView;

public class ChunkMeshData {
	private static final EnumArray<RenderLayer, MeshBuilder> BUILDERS = new EnumArray<>(RenderLayer.class);
	private final Vec3I pos = new Vec3I();
	
	public void update(BlockView view, int x, int y, int z) {
	
	}
	
	static {
		for (RenderLayer layer: RenderLayer.VALUES) {
			BUILDERS.set(layer, new MeshBuilder());
		}
	}
}
