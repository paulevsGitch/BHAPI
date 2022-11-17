package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec3F;
import net.bhapi.util.MathUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.level.BlockView;

public class BHBlockRenderer {
	private static BlockRenderer renderer;
	private static BlockView view;
	
	private static boolean mirrorTexture = false;
	private static boolean renderAllSides = false;
	public static boolean itemColorEnabled = true;
	private static int eastFaceRotation = 0;
	private static int westFaceRotation = 0;
	private static int southFaceRotation = 0;
	private static int northFaceRotation = 0;
	private static int topFaceRotation = 0;
	private static int bottomFaceRotation = 0;
	private static boolean shadeTopFace;
	private static float brightnessMiddle;
	private static float brightnessNorth;
	private static float brightnessBottom;
	private static float brightnessEast;
	private static float brightnessSouth;
	private static float brightnessTop;
	private static float brightnessWest;
	private static float brightnessBottomNorthEast;
	private static float brightnessBottomNorth;
	private static float brightnessBottomNorthWest;
	private static float brightnessBottomEast;
	private static float brightnessBottomWest;
	private static float brightnessBottomSouthEast;
	private static float brightnessBottomSouth;
	private static float brightnessBottomSouthWest;
	private static float brightnessTopNorthEast;
	private static float brightnessTopNorth;
	private static float brightnessTopNorthWest;
	private static float brightnessTopEast;
	private static float brightnessTopSouthEast;
	private static float brightnessTopSouth;
	private static float brightnessTopWest;
	private static float brightnessTopSouthWest;
	private static float brightnessNorthEast;
	private static float brightnessSouthEast;
	private static float brightnessNorthWest;
	private static float brightnessSouthWest;
	private static float colorRed00;
	private static float colorRed01;
	private static float colurRed11;
	private static float colorRed10;
	private static float colorGreen00;
	private static float colorGreen01;
	private static float colorGreen11;
	private static float colorGreen10;
	private static float colorBlue00;
	private static float colorBlue01;
	private static float colorBlue11;
	private static float colorBlue10;
	private static boolean allowsGrassUnderTopEast;
	private static boolean allowsGrassUnderTopSouth;
	private static boolean allowsGrassUnderTopNorth;
	private static boolean allowsGrassUnderTopWest;
	private static boolean allowsGrassUnderNorthEast;
	private static boolean allowsGrassUnderSouthWest;
	private static boolean allowsGrassUnderNorthWest;
	private static boolean allowsGrassUnderSouthEast;
	private static boolean allowsGrassUnderBottomEast;
	private static boolean allowsGrassUnderBottomSouth;
	private static boolean allowsGrassUnderBottomNorth;
	private static boolean allowsGrassUnderBottomWest;
	private static boolean fancyGraphics = true;
	private static boolean breaking = false;
	private static boolean item = false;
	
	private static Vec3F itemColor = new Vec3F();
	
	public static boolean isImplemented(int renderType) {
		return renderType == 0;
	}
	
	public static void setRenderer(BlockView view, BlockRenderer renderer) {
		BHBlockRenderer.renderer = renderer;
		BHBlockRenderer.view = view;
	}
	
	public static void renderBlockBreak(BlockState state, int x, int y, int z) {
		breaking = true;
		render(state, x, y, z);
		breaking = false;
	}
	
	public static void renderAllSides(BlockState state, int x, int y, int z) {
		renderAllSides = true;
		render(state, x, y, z);
		renderAllSides = false;
	}
	
	public static void renderItem(BlockState state, boolean colorizeItem, float light) {
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.start();
		item = true;
		
		itemColor.set(light);
		if (colorizeItem) {
			int color = state.getBlock().getBaseColor(0);
			float r = (float) (color >> 16 & 0xFF) / 255.0F;
			float g = (float) (color >> 8 & 0xFF) / 255.0F;
			float b = (float) (color & 0xFF) / 255.0F;
			//GL11.glColor4f(r * light, g * light, b * light, 1.0f);
			itemColor.multiply(r, g, b);
		}
		
		state.getBlock().updateRenderBounds();
		renderAllSides(state, 0, 0, 0);
		
		item = false;
		tessellator.draw();
	}
	
	public static boolean render(BlockState state, int x, int y, int z) {
		byte type = state.getRenderType(view, x, y, z);
		if (type == BlockRenderTypes.EMPTY) return false;
		if (type == BlockRenderTypes.FULL_CUBE) return renderFullCube(state, x, y, z);
		if (type == BlockRenderTypes.CUSTOM) return true; // TODO make custom rendering
		else if (BlockRenderTypes.isVanilla(type)) {
			return renderer.render(state.getBlock(), x, y, z);
		}
		return false;
	}
	
	private static boolean renderFullCube(BlockState state, int x, int y, int z) {
		float r, g, b;
		if (item) {
			//r = 1.0F; g = 1.0F; b = 1.0F;
			r = itemColor.x; g = itemColor.y; b = itemColor.z;
		}
		else {
			int color = state.getBlock().getColorMultiplier(view, x, y, z);
			r = (float) (color >> 16 & 0xFF) / 255.0F;
			g = (float) (color >> 8 & 0xFF) / 255.0F;
			b = (float) (color & 0xFF) / 255.0F;
		}
		
		if (!item && GameRenderer.anaglyph3d) {
			float nr = (r * 30.0f + g * 59.0f + b * 11.0f) / 100.0F;
			float ng = (r * 30.0f + g * 70.0f) / 100.0F;
			float nb = (r * 30.0f + b * 70.0f) / 100.0F;
			r = nr;
			g = ng;
			b = nb;
		}
		
		if (!item && Minecraft.isSmoothLightingEnabled()) {
			return renderSmooth(state, x, y, z, r, g, b);
		}
		
		return renderFast(state, x, y, z, r, g, b);
	}
	
	private static boolean renderSmooth(BlockState state, int x, int y, int z, float f, float g, float h) {
		BaseBlock block = state.getBlock();
		
		shadeTopFace = true;
		boolean result = false;
		float f2, f3, f4, f5;
		boolean bl2 = true;
		boolean bl3 = true;
		boolean bl4 = true;
		boolean bl5 = true;
		boolean bl6 = true;
		boolean bl7 = true;
		
		brightnessMiddle = block.getBrightness(view, x, y, z);
		brightnessNorth = block.getBrightness(view, x - 1, y, z);
		brightnessBottom = block.getBrightness(view, x, y - 1, z);
		brightnessEast = block.getBrightness(view, x, y, z - 1);
		brightnessSouth = block.getBrightness(view, x + 1, y, z);
		brightnessTop = block.getBrightness(view, x, y + 1, z);
		brightnessWest = block.getBrightness(view, x, y, z + 1);
		
		if (view instanceof BlockStateProvider) {
			BlockStateProvider provider = BlockStateProvider.cast(view);
			allowsGrassUnderTopSouth = provider.getBlockState(x + 1, y + 1, z).isAir();
			allowsGrassUnderBottomSouth = provider.getBlockState(x + 1, y - 1, z).isAir();
			allowsGrassUnderSouthWest = provider.getBlockState(x + 1, y, z + 1).isAir();
			allowsGrassUnderSouthEast = provider.getBlockState(x + 1, y, z - 1).isAir();
			allowsGrassUnderTopNorth = provider.getBlockState(x - 1, y + 1, z).isAir();
			allowsGrassUnderBottomNorth = provider.getBlockState(x - 1, y - 1, z).isAir();
			allowsGrassUnderNorthEast = provider.getBlockState(x - 1, y, z - 1).isAir();
			allowsGrassUnderNorthWest = provider.getBlockState(x - 1, y, z + 1).isAir();
			allowsGrassUnderTopWest = provider.getBlockState(x, y + 1, z + 1).isAir();
			allowsGrassUnderTopEast = provider.getBlockState(x, y + 1, z - 1).isAir();
			allowsGrassUnderBottomWest = provider.getBlockState(x, y - 1, z + 1).isAir();
			allowsGrassUnderBottomEast = provider.getBlockState(x, y - 1, z - 1).isAir();
		}
		
		if (block.texture == 3) {
			bl7 = false;
			bl6 = false;
			bl5 = false;
			bl4 = false;
			bl2 = false;
		}
		
		if (breaking) {
			bl7 = false;
			bl6 = false;
			bl5 = false;
			bl4 = false;
			bl2 = false;
		}
		
		if (renderAllSides || block.isSideRendered(view, x, y - 1, z, 0)) {
			brightnessBottomNorth = block.getBrightness(view, x - 1, --y, z);
			brightnessBottomEast = block.getBrightness(view, x, y, z - 1);
			brightnessBottomWest = block.getBrightness(view, x, y, z + 1);
			brightnessBottomSouth = block.getBrightness(view, x + 1, y, z);
			brightnessBottomNorthEast = allowsGrassUnderBottomEast || allowsGrassUnderBottomNorth ? block.getBrightness(view, x - 1, y, z - 1) : brightnessBottomNorth;
			brightnessBottomNorthWest = allowsGrassUnderBottomWest || allowsGrassUnderBottomNorth ? block.getBrightness(view, x - 1, y, z + 1) : brightnessBottomNorth;
			brightnessBottomSouthEast = allowsGrassUnderBottomEast || allowsGrassUnderBottomSouth ? block.getBrightness(view, x + 1, y, z - 1) : brightnessBottomSouth;
			brightnessBottomSouthWest = allowsGrassUnderBottomWest || allowsGrassUnderBottomSouth ? block.getBrightness(view, x + 1, y, z + 1) : brightnessBottomSouth;
			++y;
			f2 = (brightnessBottomNorthWest + brightnessBottomNorth + brightnessBottomWest + brightnessBottom) / 4.0F;
			f5 = (brightnessBottomWest + brightnessBottom + brightnessBottomSouthWest + brightnessBottomSouth) / 4.0F;
			f4 = (brightnessBottom + brightnessBottomEast + brightnessBottomSouth + brightnessBottomSouthEast) / 4.0F;
			f3 = (brightnessBottomNorth + brightnessBottomNorthEast + brightnessBottom + brightnessBottomEast) / 4.0F;
			colurRed11 = colorRed10 = (bl2 ? f : 1.0f) * 0.5F;
			colorRed01 = colorRed10;
			colorRed00 = colorRed10;
			colorGreen11 = colorGreen10 = (bl2 ? g : 1.0f) * 0.5F;
			colorGreen01 = colorGreen10;
			colorGreen00 = colorGreen10;
			colorBlue11 = colorBlue10 = (bl2 ? h : 1.0f) * 0.5F;
			colorBlue01 = colorBlue10;
			colorBlue00 = colorBlue10;
			colorRed00 *= f2;
			colorGreen00 *= f2;
			colorBlue00 *= f2;
			colorRed01 *= f3;
			colorGreen01 *= f3;
			colorBlue01 *= f3;
			colurRed11 *= f4;
			colorGreen11 *= f4;
			colorBlue11 *= f4;
			colorRed10 *= f5;
			colorGreen10 *= f5;
			colorBlue10 *= f5;
			renderBottomFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 0));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x, y + 1, z, 1)) {
			brightnessTopNorth = block.getBrightness(view, x - 1, ++y, z);
			brightnessTopSouth = block.getBrightness(view, x + 1, y, z);
			brightnessTopEast = block.getBrightness(view, x, y, z - 1);
			brightnessTopWest = block.getBrightness(view, x, y, z + 1);
			brightnessTopNorthEast = allowsGrassUnderTopEast || allowsGrassUnderTopNorth ? block.getBrightness(view, x - 1, y, z - 1) : brightnessTopNorth;
			brightnessTopSouthEast = allowsGrassUnderTopEast || allowsGrassUnderTopSouth ? block.getBrightness(view, x + 1, y, z - 1) : brightnessTopSouth;
			brightnessTopNorthWest = allowsGrassUnderTopWest || allowsGrassUnderTopNorth ? block.getBrightness(view, x - 1, y, z + 1) : brightnessTopNorth;
			brightnessTopSouthWest = allowsGrassUnderTopWest || allowsGrassUnderTopSouth ? block.getBrightness(view, x + 1, y, z + 1) : brightnessTopSouth;
			--y;
			f5 = (brightnessTopNorthWest + brightnessTopNorth + brightnessTopWest + brightnessTop) / 4.0F;
			f2 = (brightnessTopWest + brightnessTop + brightnessTopSouthWest + brightnessTopSouth) / 4.0F;
			f3 = (brightnessTop + brightnessTopEast + brightnessTopSouth + brightnessTopSouthEast) / 4.0F;
			f4 = (brightnessTopNorth + brightnessTopNorthEast + brightnessTop + brightnessTopEast) / 4.0F;
			colorRed10 = bl3 ? f : 1.0F;
			colurRed11 = colorRed10;
			colorRed01 = colorRed10;
			colorRed00 = colorRed10;
			colorGreen10 = bl3 ? g : 1.0F;
			colorGreen11 = colorGreen10;
			colorGreen01 = colorGreen10;
			colorGreen00 = colorGreen10;
			colorBlue10 = bl3 ? h : 1.0F;
			colorBlue11 = colorBlue10;
			colorBlue01 = colorBlue10;
			colorBlue00 = colorBlue10;
			colorRed00 *= f2;
			colorGreen00 *= f2;
			colorBlue00 *= f2;
			colorRed01 *= f3;
			colorGreen01 *= f3;
			colorBlue01 *= f3;
			colurRed11 *= f4;
			colorGreen11 *= f4;
			colorBlue11 *= f4;
			colorRed10 *= f5;
			colorGreen10 *= f5;
			colorBlue10 *= f5;
			renderTopFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 1));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x, y, z - 1, 2)) {
			brightnessNorthEast = block.getBrightness(view, x - 1, y, --z);
			brightnessBottomEast = block.getBrightness(view, x, y - 1, z);
			brightnessTopEast = block.getBrightness(view, x, y + 1, z);
			brightnessSouthEast = block.getBrightness(view, x + 1, y, z);
			brightnessBottomNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderBottomEast ? block.getBrightness(view, x - 1, y - 1, z) : brightnessNorthEast;
			brightnessTopNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderTopEast ? block.getBrightness(view, x - 1, y + 1, z) : brightnessNorthEast;
			brightnessBottomSouthEast = allowsGrassUnderSouthEast || allowsGrassUnderBottomEast ? block.getBrightness(view, x + 1, y - 1, z) : brightnessSouthEast;
			brightnessTopSouthEast = allowsGrassUnderSouthEast || allowsGrassUnderTopEast ? block.getBrightness(view, x + 1, y + 1, z) : brightnessSouthEast;
			++z;
			f2 = (brightnessNorthEast + brightnessTopNorthEast + brightnessEast + brightnessTopEast) / 4.0F;
			f3 = (brightnessEast + brightnessTopEast + brightnessSouthEast + brightnessTopSouthEast) / 4.0F;
			f4 = (brightnessBottomEast + brightnessEast + brightnessBottomSouthEast + brightnessSouthEast) / 4.0F;
			f5 = (brightnessBottomNorthEast + brightnessNorthEast + brightnessBottomEast + brightnessEast) / 4.0F;
			colurRed11 = colorRed10 = (bl4 ? f : 1.0f) * 0.8F;
			colorRed01 = colorRed10;
			colorRed00 = colorRed10;
			colorGreen11 = colorGreen10 = (bl4 ? g : 1.0f) * 0.8F;
			colorGreen01 = colorGreen10;
			colorGreen00 = colorGreen10;
			colorBlue11 = colorBlue10 = (bl4 ? h : 1.0f) * 0.8F;
			colorBlue01 = colorBlue10;
			colorBlue00 = colorBlue10;
			colorRed00 *= f2;
			colorGreen00 *= f2;
			colorBlue00 *= f2;
			colorRed01 *= f3;
			colorGreen01 *= f3;
			colorBlue01 *= f3;
			colurRed11 *= f4;
			colorGreen11 *= f4;
			colorBlue11 *= f4;
			colorRed10 *= f5;
			colorGreen10 *= f5;
			colorBlue10 *= f5;
			renderEastFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 2));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				colorRed00 *= f;
				colorRed01 *= f;
				colurRed11 *= f;
				colorRed10 *= f;
				colorGreen00 *= g;
				colorGreen01 *= g;
				colorGreen11 *= g;
				colorGreen10 *= g;
				colorBlue00 *= h;
				colorBlue01 *= h;
				colorBlue11 *= h;
				colorBlue10 *= h;
				renderEastFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x, y, z + 1, 3)) {
			brightnessNorthWest = block.getBrightness(view, x - 1, y, ++z);
			brightnessSouthWest = block.getBrightness(view, x + 1, y, z);
			brightnessBottomWest = block.getBrightness(view, x, y - 1, z);
			brightnessTopWest = block.getBrightness(view, x, y + 1, z);
			brightnessBottomNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderBottomWest ? block.getBrightness(view, x - 1, y - 1, z) : brightnessNorthWest;
			brightnessTopNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderTopWest ? block.getBrightness(view, x - 1, y + 1, z) : brightnessNorthWest;
			brightnessBottomSouthWest = allowsGrassUnderSouthWest || allowsGrassUnderBottomWest ? block.getBrightness(view, x + 1, y - 1, z) : brightnessSouthWest;
			brightnessTopSouthWest = allowsGrassUnderSouthWest || allowsGrassUnderTopWest ? block.getBrightness(view, x + 1, y + 1, z) : brightnessSouthWest;
			--z;
			f2 = (brightnessNorthWest + brightnessTopNorthWest + brightnessWest + brightnessTopWest) / 4.0F;
			f5 = (brightnessWest + brightnessTopWest + brightnessSouthWest + brightnessTopSouthWest) / 4.0F;
			f4 = (brightnessBottomWest + brightnessWest + brightnessBottomSouthWest + brightnessSouthWest) / 4.0F;
			f3 = (brightnessBottomNorthWest + brightnessNorthWest + brightnessBottomWest + brightnessWest) / 4.0F;
			colurRed11 = colorRed10 = (bl5 ? f : 1.0f) * 0.8F;
			colorRed01 = colorRed10;
			colorRed00 = colorRed10;
			colorGreen11 = colorGreen10 = (bl5 ? g : 1.0f) * 0.8F;
			colorGreen01 = colorGreen10;
			colorGreen00 = colorGreen10;
			colorBlue11 = colorBlue10 = (bl5 ? h : 1.0f) * 0.8F;
			colorBlue01 = colorBlue10;
			colorBlue00 = colorBlue10;
			colorRed00 *= f2;
			colorGreen00 *= f2;
			colorBlue00 *= f2;
			colorRed01 *= f3;
			colorGreen01 *= f3;
			colorBlue01 *= f3;
			colurRed11 *= f4;
			colorGreen11 *= f4;
			colorBlue11 *= f4;
			colorRed10 *= f5;
			colorGreen10 *= f5;
			colorBlue10 *= f5;
			renderWestFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 3));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				colorRed00 *= f;
				colorRed01 *= f;
				colurRed11 *= f;
				colorRed10 *= f;
				colorGreen00 *= g;
				colorGreen01 *= g;
				colorGreen11 *= g;
				colorGreen10 *= g;
				colorBlue00 *= h;
				colorBlue01 *= h;
				colorBlue11 *= h;
				colorBlue10 *= h;
				renderWestFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x - 1, y, z, 4)) {
			brightnessBottomNorth = block.getBrightness(view, --x, y - 1, z);
			brightnessNorthEast = block.getBrightness(view, x, y, z - 1);
			brightnessNorthWest = block.getBrightness(view, x, y, z + 1);
			brightnessTopNorth = block.getBrightness(view, x, y + 1, z);
			brightnessBottomNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderBottomNorth ? block.getBrightness(view, x, y - 1, z - 1) : brightnessNorthEast;
			brightnessBottomNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderBottomNorth ? block.getBrightness(view, x, y - 1, z + 1) : brightnessNorthWest;
			brightnessTopNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderTopNorth ? block.getBrightness(view, x, y + 1, z - 1) : brightnessNorthEast;
			brightnessTopNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderTopNorth ? block.getBrightness(view, x, y + 1, z + 1) : brightnessNorthWest;
			++x;
			f5 = (brightnessBottomNorth + brightnessBottomNorthWest + brightnessNorth + brightnessNorthWest) / 4.0F;
			f2 = (brightnessNorth + brightnessNorthWest + brightnessTopNorth + brightnessTopNorthWest) / 4.0F;
			f3 = (brightnessNorthEast + brightnessNorth + brightnessTopNorthEast + brightnessTopNorth) / 4.0F;
			f4 = (brightnessBottomNorthEast + brightnessBottomNorth + brightnessNorthEast + brightnessNorth) / 4.0F;
			colurRed11 = colorRed10 = (bl6 ? f : 1.0f) * 0.6F;
			colorRed01 = colorRed10;
			colorRed00 = colorRed10;
			colorGreen11 = colorGreen10 = (bl6 ? g : 1.0f) * 0.6F;
			colorGreen01 = colorGreen10;
			colorGreen00 = colorGreen10;
			colorBlue11 = colorBlue10 = (bl6 ? h : 1.0f) * 0.6F;
			colorBlue01 = colorBlue10;
			colorBlue00 = colorBlue10;
			colorRed00 *= f2;
			colorGreen00 *= f2;
			colorBlue00 *= f2;
			colorRed01 *= f3;
			colorGreen01 *= f3;
			colorBlue01 *= f3;
			colurRed11 *= f4;
			colorGreen11 *= f4;
			colorBlue11 *= f4;
			colorRed10 *= f5;
			colorGreen10 *= f5;
			colorBlue10 *= f5;
			renderNorthFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 4));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				colorRed00 *= f;
				colorRed01 *= f;
				colurRed11 *= f;
				colorRed10 *= f;
				colorGreen00 *= g;
				colorGreen01 *= g;
				colorGreen11 *= g;
				colorGreen10 *= g;
				colorBlue00 *= h;
				colorBlue01 *= h;
				colorBlue11 *= h;
				colorBlue10 *= h;
				renderNorthFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x + 1, y, z, 5)) {
			brightnessBottomSouth = block.getBrightness(view, ++x, y - 1, z);
			brightnessSouthEast = block.getBrightness(view, x, y, z - 1);
			brightnessSouthWest = block.getBrightness(view, x, y, z + 1);
			brightnessTopSouth = block.getBrightness(view, x, y + 1, z);
			brightnessBottomSouthEast = allowsGrassUnderBottomSouth || allowsGrassUnderSouthEast ? block.getBrightness(view, x, y - 1, z - 1) : brightnessSouthEast;
			brightnessBottomSouthWest = allowsGrassUnderBottomSouth || allowsGrassUnderSouthWest ? block.getBrightness(view, x, y - 1, z + 1) : brightnessSouthWest;
			brightnessTopSouthEast = allowsGrassUnderTopSouth || allowsGrassUnderSouthEast ? block.getBrightness(view, x, y + 1, z - 1) : brightnessSouthEast;
			brightnessTopSouthWest = allowsGrassUnderTopSouth || allowsGrassUnderSouthWest ? block.getBrightness(view, x, y + 1, z + 1) : brightnessSouthWest;
			--x;
			f2 = (brightnessBottomSouth + brightnessBottomSouthWest + brightnessSouth + brightnessSouthWest) / 4.0F;
			f5 = (brightnessSouth + brightnessSouthWest + brightnessTopSouth + brightnessTopSouthWest) / 4.0F;
			f4 = (brightnessSouthEast + brightnessSouth + brightnessTopSouthEast + brightnessTopSouth) / 4.0F;
			f3 = (brightnessBottomSouthEast + brightnessBottomSouth + brightnessSouthEast + brightnessSouth) / 4.0F;
			colurRed11 = colorRed10 = (bl7 ? f : 1.0f) * 0.6F;
			colorRed01 = colorRed10;
			colorRed00 = colorRed10;
			colorGreen11 = colorGreen10 = (bl7 ? g : 1.0f) * 0.6F;
			colorGreen01 = colorGreen10;
			colorGreen00 = colorGreen10;
			colorBlue11 = colorBlue10 = (bl7 ? h : 1.0f) * 0.6F;
			colorBlue01 = colorBlue10;
			colorBlue00 = colorBlue10;
			colorRed00 *= f2;
			colorGreen00 *= f2;
			colorBlue00 *= f2;
			colorRed01 *= f3;
			colorGreen01 *= f3;
			colorBlue01 *= f3;
			colurRed11 *= f4;
			colorGreen11 *= f4;
			colorBlue11 *= f4;
			colorRed10 *= f5;
			colorGreen10 *= f5;
			colorBlue10 *= f5;
			renderSouthFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 5));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				colorRed00 *= f;
				colorRed01 *= f;
				colurRed11 *= f;
				colorRed10 *= f;
				colorGreen00 *= g;
				colorGreen01 *= g;
				colorGreen11 *= g;
				colorGreen10 *= g;
				colorBlue00 *= h;
				colorBlue01 *= h;
				colorBlue11 *= h;
				colorBlue10 *= h;
				renderSouthFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		shadeTopFace = false;
		return result;
	}
	
	private static boolean renderFast(BlockState state, int x, int y, int z, float r, float g, float b) {
		BaseBlock block = state.getBlock();
		float light;
		shadeTopFace = false;
		Tessellator tessellator = Tessellator.INSTANCE;
		boolean result = false;
		
		float tR = r;
		float tG = g;
		float tB = b;
		
		float bR = 0.5F;
		float bG = 0.5F;
		float bB = 0.5F;
		
		float ewR = 0.8F;
		float ewG = 0.8F;
		float ewB = 0.8F;
		
		float nsR = 0.6F;
		float nsG = 0.6F;
		float nsB = 0.6F;
		
		if (block != BaseBlock.GRASS) {
			bR *= r;
			ewR *= r;
			nsR *= r;
			bG *= g;
			ewG *= g;
			nsG *= g;
			bB *= b;
			ewB *= b;
			nsB *= b;
		}
		
		float light2 = item ? 1.0F : block.getBrightness(view, x, y, z);
		
		if (renderAllSides || block.isSideRendered(view, x, y - 1, z, 0)) {
			light = item ? 1.0F : block.getBrightness(view, x, y - 1, z);
			tessellator.color(bR * light, bG * light, bB * light);
			if (item) tessellator.setNormal(0.0f, -1.0f, 0.0f);
			renderBottomFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 0));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x, y + 1, z, 1)) {
			light = item ? 1.0F : block.getBrightness(view, x, y + 1, z);
			if (block.maxY != 1.0 && !block.material.isLiquid()) {
				light = light2;
			}
			tessellator.color(tR * light, tG * light, tB * light);
			if (item) tessellator.setNormal(0.0f, 1.0f, 0.0f);
			renderTopFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 1));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x, y, z - 1, 2)) {
			light = item ? 1.0F : block.getBrightness(view, x, y, z - 1);
			if (block.minZ > 0.0) {
				light = light2;
			}
			tessellator.color(ewR * light, ewG * light, ewB * light);
			if (item) tessellator.setNormal(0.0f, 0.0f, -1.0f);
			renderEastFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 2));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(ewR * light * r, ewG * light * g, ewB * light * b);
				renderEastFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x, y, z + 1, 3)) {
			light = item ? 1.0F : block.getBrightness(view, x, y, z + 1);
			if (block.maxZ < 1.0) {
				light = light2;
			}
			
			tessellator.color(ewR * light, ewG * light, ewB * light);
			if (item) tessellator.setNormal(0.0f, 0.0f, 1.0f);
			renderWestFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 3));
			
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(ewR * light * r, ewG * light * g, ewB * light * b);
				renderWestFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x - 1, y, z, 4)) {
			light = item ? 1.0F : block.getBrightness(view, x - 1, y, z);
			if (block.minX > 0.0) {
				light = light2;
			}
			tessellator.color(nsR * light, nsG * light, nsB * light);
			if (item) tessellator.setNormal(-1.0f, 0.0f, 0.0f);
			renderNorthFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 4));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(nsR * light * r, nsG * light * g, nsB * light * b);
				renderNorthFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(view, x + 1, y, z, 5)) {
			light = item ? 1.0F : block.getBrightness(view, x + 1, y, z);
			if (block.maxX < 1.0) {
				light = light2;
			}
			tessellator.color(nsR * light, nsG * light, nsB * light);
			if (item) tessellator.setNormal(1.0f, 0.0f, 0.0f);
			renderSouthFace(block, x, y, z, state.getTextureForIndex(view, x, y, z, 5));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(nsR * light * r, nsG * light * g, nsB * light * b);
				renderSouthFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		return result;
	}
	
	private static void renderBottomFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		UVPair uv = sample.getUV();
		float u11, u12, v11, v12;
		
		if (breaking) {
			u11 = (float) block.minX;
			u12 = (float) block.maxX;
			v11 = (float) block.maxZ;
			v12 = (float) block.minZ;
		}
		else {
			u11 = uv.getU(MathUtil.clamp((float) block.minX, 0, 1));
			u12 = uv.getU(MathUtil.clamp((float) block.maxX, 0, 1));
			v11 = uv.getV(MathUtil.clamp((float) block.maxZ, 0, 1));
			v12 = uv.getV(MathUtil.clamp((float) block.minZ, 0, 1));
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		/*if (bottomFaceRotation == 2) {
			u11 = ((double)n + arg.minZ * 16.0) / 256.0;
			v11 = ((double)(n2 + 16) - arg.maxX * 16.0) / 256.0;
			u12 = ((double)n + arg.maxZ * 16.0) / 256.0;
			v12 = ((double)(n2 + 16) - arg.minX * 16.0) / 256.0;
			u22 = u12;
			u21 = u11;
			v21 = v11;
			v22 = v12;
			u22 = u11;
			u21 = u12;
			v11 = v12;
			v12 = v21;
		}
		else if (bottomFaceRotation == 1) {
			u11 = ((double)(n + 16) - arg.maxZ * 16.0) / 256.0;
			v11 = ((double)n2 + arg.minX * 16.0) / 256.0;
			u12 = ((double)(n + 16) - arg.minZ * 16.0) / 256.0;
			v12 = ((double)n2 + arg.maxX * 16.0) / 256.0;
			u22 = u12;
			u21 = u11;
			v21 = v11;
			v22 = v12;
			u11 = u22;
			u12 = u21;
			v21 = v12;
			v22 = v11;
		}
		else if (bottomFaceRotatioblock == BaseBlock.GRASS) {
			u11 = ((double)(n + 16) - arg.minX * 16.0) / 256.0;
			u12 = ((double)(n + 16) - arg.maxX * 16.0 - 0.01) / 256.0;
			v11 = ((double)(n2 + 16) - arg.minZ * 16.0) / 256.0;
			v12 = ((double)(n2 + 16) - arg.maxZ * 16.0 - 0.01) / 256.0;
			u22 = u12;
			u21 = u11;
			v21 = v11;
			v22 = v12;
		}*/
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y1 = y + block.minY;
		double z1 = z + block.minZ;
		double z2 = z + block.maxZ;
		
		if (shadeTopFace) {
			tessellator.color(colorRed00, colorGreen00, colorBlue00);
			tessellator.vertex(x1, y1, z2, u21, v22);
			tessellator.color(colorRed01, colorGreen01, colorBlue01);
			tessellator.vertex(x1, y1, z1, u11, v11);
			tessellator.color(colurRed11, colorGreen11, colorBlue11);
			tessellator.vertex(x2, y1, z1, u22, v21);
			tessellator.color(colorRed10, colorGreen10, colorBlue10);
			tessellator.vertex(x2, y1, z2, u12, v12);
		}
		else {
			tessellator.vertex(x1, y1, z2, u21, v22);
			tessellator.vertex(x1, y1, z1, u11, v11);
			tessellator.vertex(x2, y1, z1, u22, v21);
			tessellator.vertex(x2, y1, z2, u12, v12);
		}
	}
	
	private static void renderTopFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		UVPair uv = sample.getUV();
		float u11, u12, v11, v12;
		
		if (breaking) {
			u11 = (float) block.minX;
			u12 = (float) block.maxX;
			v11 = (float) block.minZ;
			v12 = (float) block.maxZ;
		}
		else {
			u11 = uv.getU(MathUtil.clamp((float) block.minX, 0, 1));
			u12 = uv.getU(MathUtil.clamp((float) block.maxX, 0, 1));
			v11 = uv.getV(MathUtil.clamp((float) block.minZ, 0, 1));
			v12 = uv.getV(MathUtil.clamp((float) block.maxZ, 0, 1));
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		/*if (topFaceRotation == 1) {
			u11 = ((double) n + block.minZ * 16.0) / 256.0;
			v11 = ((double)(n2 + 16) - block.maxX * 16.0) / 256.0;
			u12 = ((double)n + block.maxZ * 16.0) / 256.0;
			v12 = ((double)(n2 + 16) - block.minX * 16.0) / 256.0;
			u22 = u12;
			u21 = u11;
			v21 = v11;
			v22 = v12;
			u22 = u11;
			u21 = u12;
			v11 = v12;
			v12 = v21;
		}
		else if (topFaceRotation == 2) {
			u11 = ((double)(n + 16) - block.maxZ * 16.0) / 256.0;
			v11 = ((double)n2 + block.minX * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.minZ * 16.0) / 256.0;
			v12 = ((double)n2 + block.maxX * 16.0) / 256.0;
			u22 = u12;
			u21 = u11;
			v21 = v11;
			v22 = v12;
			u11 = u22;
			u12 = u21;
			v21 = v12;
			v22 = v11;
		}
		else if (topFaceRotatioblock == BaseBlock.GRASS) {
			u11 = ((double)(n + 16) - block.minX * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.maxX * 16.0 - 0.01) / 256.0;
			v11 = ((double)(n2 + 16) - block.minZ * 16.0) / 256.0;
			v12 = ((double)(n2 + 16) - block.maxZ * 16.0 - 0.01) / 256.0;
			u22 = u12;
			u21 = u11;
			v21 = v11;
			v22 = v12;
		}*/
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y2 = y + block.maxY;
		double z1 = z + block.minZ;
		double z2 = z + block.maxZ;
		
		if (shadeTopFace) {
			tessellator.color(colorRed00, colorGreen00, colorBlue00);
			tessellator.vertex(x2, y2, z2, u12, v12);
			tessellator.color(colorRed01, colorGreen01, colorBlue01);
			tessellator.vertex(x2, y2, z1, u22, v21);
			tessellator.color(colurRed11, colorGreen11, colorBlue11);
			tessellator.vertex(x1, y2, z1, u11, v11);
			tessellator.color(colorRed10, colorGreen10, colorBlue10);
			tessellator.vertex(x1, y2, z2, u21, v22);
		}
		else {
			tessellator.vertex(x2, y2, z2, u12, v12);
			tessellator.vertex(x2, y2, z1, u22, v21);
			tessellator.vertex(x1, y2, z1, u11, v11);
			tessellator.vertex(x1, y2, z2, u21, v22);
		}
	}
	
	private static void renderEastFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		UVPair uv = sample.getUV();
		float u11, u12, v11, v12;
		
		if (breaking) {
			u11 = (float) block.minX;
			u12 = (float) block.maxX;
			v11 = (float) block.minY;
			v12 = (float) block.maxY;
		}
		else {
			u11 = uv.getU(MathUtil.clamp((float) block.minX, 0, 1));
			u12 = uv.getU(MathUtil.clamp((float) block.maxX, 0, 1));
			v11 = uv.getV(MathUtil.clamp((float) block.minY, 0, 1));
			v12 = uv.getV(MathUtil.clamp((float) block.maxY, 0, 1));
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		if (mirrorTexture) {
			u22 = u11;
			u11 = u12;
			u12 = u22;
		}
		
		/*if (eastFaceRotation == 2) {
			u11 = ((double)n + block.minY * 16.0) / 256.0;
			v11 = ((double)(n2 + 16) - block.minX * 16.0) / 256.0;
			u12 = ((double)n + block.maxY * 16.0) / 256.0;
			v12 = ((double)(n2 + 16) - block.maxX * 16.0) / 256.0;
			u22 = u12;
			U21 = u11;
			V21 = v11;
			V22 = v12;
			u22 = u11;
			U21 = u12;
			v11 = v12;
			v12 = V21;
		} else if (eastFaceRotation == 1) {
			u11 = ((double)(n + 16) - block.maxY * 16.0) / 256.0;
			v11 = ((double)n2 + block.maxX * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.minY * 16.0) / 256.0;
			v12 = ((double)n2 + block.minX * 16.0) / 256.0;
			u22 = u12;
			U21 = u11;
			V21 = v11;
			V22 = v12;
			u11 = u22;
			u12 = U21;
			V21 = v12;
			V22 = v11;
		} else if (eastFaceRotatioblock == BaseBlock.GRASS) {
			u11 = ((double)(n + 16) - block.minX * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.maxX * 16.0 - 0.01) / 256.0;
			v11 = ((double)n2 + block.maxY * 16.0) / 256.0;
			v12 = ((double)n2 + block.minY * 16.0 - 0.01) / 256.0;
			u22 = u12;
			U21 = u11;
			V21 = v11;
			V22 = v12;
		}*/
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y1 = y + block.minY;
		double y2 = y + block.maxY;
		double z1 = z + block.minZ;
		
		if (shadeTopFace) {
			tessellator.color(colorRed00, colorGreen00, colorBlue00);
			tessellator.vertex(x1, y2, z1, u22, v21);
			tessellator.color(colorRed01, colorGreen01, colorBlue01);
			tessellator.vertex(x2, y2, z1, u11, v11);
			tessellator.color(colurRed11, colorGreen11, colorBlue11);
			tessellator.vertex(x2, y1, z1, u21, v22);
			tessellator.color(colorRed10, colorGreen10, colorBlue10);
			tessellator.vertex(x1, y1, z1, u12, v12);
		}
		else {
			tessellator.vertex(x1, y2, z1, u22, v21);
			tessellator.vertex(x2, y2, z1, u11, v11);
			tessellator.vertex(x2, y1, z1, u21, v22);
			tessellator.vertex(x1, y1, z1, u12, v12);
		}
	}
	
	private static void renderWestFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		UVPair uv = sample.getUV();
		float u11, u12, v11, v12;
		
		if (breaking) {
			u11 = (float) block.minX;
			u12 = (float) block.maxX;
			v11 = (float) block.maxY;
			v12 = (float) block.minY;
		}
		else {
			u11 = uv.getU(MathUtil.clamp((float) block.minX, 0, 1));
			u12 = uv.getU(MathUtil.clamp((float) block.maxX, 0, 1));
			v11 = uv.getV(MathUtil.clamp((float) block.maxY, 0, 1));
			v12 = uv.getV(MathUtil.clamp((float) block.minY, 0, 1));
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		if (mirrorTexture) {
			u21 = u11;
			u11 = u12;
			u12 = u21;
		}
		
		u21 = u12;
		double d7 = u11;
		double d8 = v12;
		double d9 = v11;
		
		/*if (westFaceRotation == 1) {
			u11 = ((double)n + block.minY * 16.0) / 256.0;
			v11 = ((double)(n2 + 16) - block.minX * 16.0) / 256.0;
			u12 = ((double)n + block.maxY * 16.0) / 256.0;
			v12 = ((double)(n2 + 16) - block.maxX * 16.0) / 256.0;
			u21 = u12;
			d7 = u11;
			d8 = v12;
			d9 = v11;
			u21 = u11;
			d7 = u12;
			v12 = v11;
			v11 = d8;
		}
		else if (westFaceRotation == 2) {
			u11 = ((double)(n + 16) - block.maxY * 16.0) / 256.0;
			v12 = ((double)n2 + block.minX * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.minY * 16.0) / 256.0;
			v11 = ((double)n2 + block.maxX * 16.0) / 256.0;
			u21 = u12;
			d7 = u11;
			d8 = v12;
			d9 = v11;
			u11 = u21;
			u12 = d7;
			d8 = v11;
			d9 = v12;
		}
		else if (westFaceRotatioblock == BaseBlock.GRASS) {
			u11 = ((double)(n + 16) - block.minX * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.maxX * 16.0 - 0.01) / 256.0;
			v12 = ((double)n2 + block.maxY * 16.0) / 256.0;
			v11 = ((double)n2 + block.minY * 16.0 - 0.01) / 256.0;
			u21 = u12;
			d7 = u11;
			d8 = v12;
			d9 = v11;
		}*/
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y1 = y + block.minY;
		double y2 = y + block.maxY;
		double z2 = z + block.maxZ;
		
		if (shadeTopFace) {
			tessellator.color(colorRed00, colorGreen00, colorBlue00);
			tessellator.vertex(x1, y2, z2, u11, v12);
			tessellator.color(colorRed01, colorGreen01, colorBlue01);
			tessellator.vertex(x1, y1, z2, d7, d9);
			tessellator.color(colurRed11, colorGreen11, colorBlue11);
			tessellator.vertex(x2, y1, z2, u12, v11);
			tessellator.color(colorRed10, colorGreen10, colorBlue10);
			tessellator.vertex(x2, y2, z2, u21, d8);
		}
		else {
			tessellator.vertex(x1, y2, z2, u11, v12);
			tessellator.vertex(x1, y1, z2, d7, d9);
			tessellator.vertex(x2, y1, z2, u12, v11);
			tessellator.vertex(x2, y2, z2, u21, d8);
		}
	}
	
	// TODO change to SOUTH name
	private static void renderNorthFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		UVPair uv = sample.getUV();
		float u11, u12, v11, v12;
		
		if (breaking) {
			u11 = (float) block.minX;
			u12 = (float) block.maxX;
			v11 = (float) block.maxY;
			v12 = (float) block.minY;
		}
		else {
			u11 = uv.getU(MathUtil.clamp((float) block.minX, 0, 1));
			u12 = uv.getU(MathUtil.clamp((float) block.maxX, 0, 1));
			v11 = uv.getV(MathUtil.clamp((float) block.maxY, 0, 1));
			v12 = uv.getV(MathUtil.clamp((float) block.minY, 0, 1));
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		if (mirrorTexture) {
			u22 = u11;
			u11 = u12;
			u12 = u22;
		}
		
		/*if (northFaceRotation == 1) {
			u11 = ((double)n + block.minY * 16.0) / 256.0;
			v12 = ((double)(n2 + 16) - block.maxZ * 16.0) / 256.0;
			u12 = ((double)n + block.maxY * 16.0) / 256.0;
			v11 = ((double)(n2 + 16) - block.minZ * 16.0) / 256.0;
			u22 = u12;
			u21 = u11;
			v22 = v12;
			v21 = v11;
			u22 = u11;
			u21 = u12;
			v12 = v11;
			v11 = v22;
		}
		else if (northFaceRotation == 2) {
			u11 = ((double)(n + 16) - block.maxY * 16.0) / 256.0;
			v12 = ((double)n2 + block.minZ * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.minY * 16.0) / 256.0;
			v11 = ((double)n2 + block.maxZ * 16.0) / 256.0;
			u22 = u12;
			u21 = u11;
			v22 = v12;
			v21 = v11;
			u11 = u22;
			u12 = u21;
			v22 = v11;
			v21 = v12;
		}
		else if (northFaceRotatioblock == BaseBlock.GRASS) {
			u11 = ((double)(n + 16) - block.minZ * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.maxZ * 16.0 - 0.01) / 256.0;
			v12 = ((double)n2 + block.maxY * 16.0) / 256.0;
			v11 = ((double)n2 + block.minY * 16.0 - 0.01) / 256.0;
			u22 = u12;
			u21 = u11;
			v22 = v12;
			v21 = v11;
		}*/
		
		double x1 = x + block.minX;
		double y1 = y + block.minY;
		double y2 = y + block.maxY;
		double z1 = z + block.minZ;
		double z2 = z + block.maxZ;
		
		if (shadeTopFace) {
			tessellator.color(colorRed00, colorGreen00, colorBlue00);
			tessellator.vertex(x1, y2, z2, u22, v22);
			tessellator.color(colorRed01, colorGreen01, colorBlue01);
			tessellator.vertex(x1, y2, z1, u11, v12);
			tessellator.color(colurRed11, colorGreen11, colorBlue11);
			tessellator.vertex(x1, y1, z1, u21, v21);
			tessellator.color(colorRed10, colorGreen10, colorBlue10);
			tessellator.vertex(x1, y1, z2, u12, v11);
		}
		else {
			tessellator.vertex(x1, y2, z2, u22, v22);
			tessellator.vertex(x1, y2, z1, u11, v12);
			tessellator.vertex(x1, y1, z1, u21, v21);
			tessellator.vertex(x1, y1, z2, u12, v11);
		}
	}
	
	
	// TODO change to NORTH
	private static void renderSouthFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		UVPair uv = sample.getUV();
		float u11, u12, v11, v12;
		
		if (breaking) {
			u11 = (float) block.minZ;
			u12 = (float) block.maxZ;
			v11 = (float) block.maxY;
			v12 = (float) block.minY;
		}
		else {
			u11 = uv.getU(MathUtil.clamp((float) block.minZ, 0, 1));
			u12 = uv.getU(MathUtil.clamp((float) block.maxZ, 0, 1));
			v11 = uv.getV(MathUtil.clamp((float) block.maxY, 0, 1));
			v12 = uv.getV(MathUtil.clamp((float) block.minY, 0, 1));
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		if (mirrorTexture) {
			u22 = u11;
			u11 = u12;
			u12 = u22;
		}
		
		/*if (southFaceRotation == 2) {
			u11 = ((double)n + block.minY * 16.0) / 256.0;
			v12 = ((double)(n2 + 16) - block.minZ * 16.0) / 256.0;
			u12 = ((double)n + block.maxY * 16.0) / 256.0;
			v11 = ((double)(n2 + 16) - block.maxZ * 16.0) / 256.0;
			u22 = u12;
			u21 = u11;
			v22 = v12;
			v21 = v11;
			u22 = u11;
			u21 = u12;
			v12 = v11;
			v11 = v22;
		}
		else if (southFaceRotation == 1) {
			u11 = ((double)(n + 16) - block.maxY * 16.0) / 256.0;
			v12 = ((double)n2 + block.maxZ * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.minY * 16.0) / 256.0;
			v11 = ((double)n2 + block.minZ * 16.0) / 256.0;
			u22 = u12;
			u21 = u11;
			v22 = v12;
			v21 = v11;
			u11 = u22;
			u12 = u21;
			v22 = v11;
			v21 = v12;
		}
		else if (southFaceRotatioblock == BaseBlock.GRASS) {
			u11 = ((double)(n + 16) - block.minZ * 16.0) / 256.0;
			u12 = ((double)(n + 16) - block.maxZ * 16.0 - 0.01) / 256.0;
			v12 = ((double)n2 + block.maxY * 16.0) / 256.0;
			v11 = ((double)n2 + block.minY * 16.0 - 0.01) / 256.0;
			u22 = u12;
			u21 = u11;
			v22 = v12;
			v21 = v11;
		}*/
		
		double d10 = x + block.maxX;
		double d11 = y + block.minY;
		double d12 = y + block.maxY;
		double d13 = z + block.minZ;
		double d14 = z + block.maxZ;
		
		if (shadeTopFace) {
			tessellator.color(colorRed00, colorGreen00, colorBlue00);
			tessellator.vertex(d10, d11, d14, u21, v21);
			tessellator.color(colorRed01, colorGreen01, colorBlue01);
			tessellator.vertex(d10, d11, d13, u12, v11);
			tessellator.color(colurRed11, colorGreen11, colorBlue11);
			tessellator.vertex(d10, d12, d13, u22, v22);
			tessellator.color(colorRed10, colorGreen10, colorBlue10);
			tessellator.vertex(d10, d12, d14, u11, v12);
		}
		else {
			tessellator.vertex(d10, d11, d14, u21, v21);
			tessellator.vertex(d10, d11, d13, u12, v11);
			tessellator.vertex(d10, d12, d13, u22, v22);
			tessellator.vertex(d10, d12, d14, u11, v12);
		}
	}
}
