package net.bhapi.client.render.level;

import net.bhapi.client.render.texture.RenderLayer;
import net.bhapi.client.render.vbo.VBO;
import net.bhapi.storage.EnumArray;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Tessellator;

@Environment(EnvType.CLIENT)
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
	
	public boolean isEmpty(RenderLayer layer) {
		return builders.get(layer).isEmpty();
	}
	
	public boolean isEmpty() {
		for (RenderLayer layer: RenderLayer.VALUES) {
			if (!isEmpty(layer)) return false;
		}
		return true;
	}
	
	public void build(Tessellator tessellator) {
		if (isEmpty()) return;
		for (MeshBuilder builder : builders.getValues()) {
			if (builder.isEmpty()) continue;
			builder.build(tessellator);
		}
	}
	
	public void build(Tessellator tessellator, RenderLayer layer) {
		MeshBuilder builder = builders.get(layer);
		if (builder.isEmpty()) return;
		builder.build(tessellator);
	}
	
	public void build(VBO vbo, RenderLayer layer) {
		MeshBuilder builder = builders.get(layer);
		if (builder.isEmpty()) return;
		builder.build(vbo);
	}
}
