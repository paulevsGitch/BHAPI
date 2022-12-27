package net.bhapi.client;

import net.bhapi.BHAPI;
import net.bhapi.client.event.AfterTextureLoadedEvent;
import net.bhapi.client.render.model.CustomModel;
import net.bhapi.client.render.model.JSONModel;
import net.bhapi.client.render.model.OBJModel;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.event.EventListener;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

// TODO remove this
@Environment(EnvType.CLIENT)
public class TestClientEvent {
	public static TextureSample[] samples;
	public static TextureSample[] samplesFar;
	public static CustomModel testModel;
	public static CustomModel testModel2;
	public static CustomModel testModel3;
	public static CustomModel testModel4;
	
	@EventListener
	public void testClientEvent(AfterTextureLoadedEvent event) {
		BHAPI.log("Make Sample");
		samples = new TextureSample[] {
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/cobblestone")),
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/unknown_tile_2")),
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/warped_cobble")),
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/lantern")),
		};
		
		samples[0].setColorProvider((view, x, y, z, state) -> Color.MAGENTA.getRGB());
		
		/*EnumArray<FaceGroup, ModelQuad[]> quads = new EnumArray<>(FaceGroup.class);
		ModelQuad[] face = new ModelQuad[1];
		quads.set(FaceGroup.NONE, face);
		face[0] = new ModelQuad(0);
		face[0].setVertex(3, 0, 0.5F, 0, 0, 0);
		face[0].setVertex(2, 1, 0.5F, 0, 0, 1);
		face[0].setVertex(1, 1, 0.5F, 1, 1, 1);
		face[0].setVertex(0, 0, 0.5F, 1, 1, 0);
		testModel = new CustomModel(quads);*/
		
		samplesFar = new TextureSample[] {
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/farlandsSide")),
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/farlandsBottom")),
			Textures.getAtlas().getSample(Identifier.make("bhapi", "block/farlandsTop")),
		};
		
		testModel = new OBJModel(Identifier.make("bhapi", "models/sphere"));
		testModel2 = new OBJModel(Identifier.make("bhapi", "models/farlandsBlock"));
		testModel3 = new OBJModel(Identifier.make("bhapi", "models/sphere_big"));
		
		/*testModel4 = ModelBuilder
			.start()
			//.cuboid().setMinPos(0.25F, 0.25F, 0.25F).setMaxPos(0.75F, 0.75F, 0.75F).allFaces().build()
			//.cuboid().setMinPos(0.125F, 0.375F, 0.375F).setMaxPos(0.875F, 0.625F, 0.625F).allFaces().build()
			//.cuboid().setMinPos(0.375F, 0.125F, 0.375F).setMaxPos(0.625F, 0.875F, 0.625F).allFaces().build()
			//.cuboid().setMinPos(0.375F, 0.375F, 0.125F).setMaxPos(0.625F, 0.625F, 0.875F).allFaces().build()
			.cuboid()
				.setMinPos(0.5F, 0, 0)
				.setMaxPos(0.5F, 1, 1)
				.addFaces(BlockDirection.NEG_X, BlockDirection.POS_X)
				.setRotation(0.5F, 0, 0.5F, 'y', (float) Math.toRadians(45))
				.build()
			.cuboid()
				.setMinPos(0.5F, 0, 0)
				.setMaxPos(0.5F, 1, 1)
				.addFaces(BlockDirection.NEG_X, BlockDirection.POS_X)
				.setRotation(0.5F, 0, 0.5F, 'y', (float) Math.toRadians(-45))
				.build()
			.build();*/
		
		Map<String, Integer> map = new HashMap<>();
		map.put("lantern", 0);
		testModel4 = new JSONModel(Identifier.make("bhapi", "models/template_lantern"), map);
	}
}
