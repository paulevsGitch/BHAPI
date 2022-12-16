package net.bhapi.client.render.level;

import net.bhapi.client.render.texture.RenderLayer;
import net.bhapi.storage.EnumArray;
import net.minecraft.client.render.Tessellator;

public class LayeredMeshBuilder {
	EnumArray<RenderLayer, MeshBuilder> builders = new EnumArray<>(RenderLayer.class);
	
	public LayeredMeshBuilder() {
		for (RenderLayer layer: RenderLayer.VALUES) {
			builders.set(layer, new MeshBuilder());
		}
	}
	
	public MeshBuilder getBuilder(RenderLayer layer) {
		return builders.get(layer);
	}
	
	public void start() {
		for (RenderLayer layer: RenderLayer.VALUES) {
			builders.get(layer).start();
		}
	}
	
	public void start(int x, int y, int z) {
		for (RenderLayer layer: RenderLayer.VALUES) {
			MeshBuilder builder = builders.get(layer);
			builder.start();
			builder.setOffset(x, y, z);
		}
	}
	
	public boolean isEmpty() {
		for (RenderLayer layer: RenderLayer.VALUES) {
			if (!builders.get(layer).isEmpty()) return false;
		}
		return true;
	}
	
	public void build(Tessellator tessellator) {
		if (isEmpty()) return;
		tessellator.start();
		builders.forEach(builder -> {
			if (!builder.isEmpty()) {
				builder.build(tessellator);
			}
		});
		tessellator.draw();
	}
	
	public void build(Tessellator tessellator, RenderLayer layer) {
		MeshBuilder builder = builders.get(layer);
		if (builder.isEmpty()) return;
		tessellator.start();
		builder.build(tessellator);
		tessellator.draw();
	}
}
