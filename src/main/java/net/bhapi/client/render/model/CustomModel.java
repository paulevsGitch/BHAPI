package net.bhapi.client.render.model;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.storage.CircleCache;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.Vec2F;
import net.bhapi.util.BlockDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.level.BlockView;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class CustomModel {
	private final EnumArray<FaceGroup, ModelQuad[]> groups;
	
	public CustomModel(EnumArray<FaceGroup, ModelQuad[]> groups) {
		this.groups = groups;
		groups.forEach(quads -> Arrays.stream(quads).forEach(ModelQuad::finalise));
	}
	
	public void render(ModelRenderingContext context, CircleCache<Vec2F> uvCache) {
		int ix = (int) context.getX();
		int iy = (int) context.getY();
		int iz = (int) context.getZ();
		ModelQuad[] quads = groups.get(FaceGroup.NONE);
		if (quads != null) renderQuads(context, quads, ix, iy, iz, uvCache);
		for (BlockDirection dir: BlockDirection.VALUES) {
			if (context.renderFace(dir)) {
				quads = groups.get(FaceGroup.getFromFacing(dir));
				if (quads != null) renderQuads(context, quads, ix, iy, iz, uvCache);
			}
		}
	}
	
	private void renderQuads(ModelRenderingContext context, ModelQuad[] quads, int x, int y, int z, CircleCache<Vec2F> uvCache) {
		BlockView view = context.getBlockView();
		BlockState state = context.getState();
		int index = context.getOverlayIndex();
		for (ModelQuad quad: quads) {
			TextureSample sample = state.getTextureForIndex(view, x, y, z, quad.getTextureIndex(), index);
			if (sample != null) quad.render(context, sample, uvCache);
		}
	}
}
