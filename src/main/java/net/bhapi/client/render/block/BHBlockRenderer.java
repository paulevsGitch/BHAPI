package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.properties.BlockPropertyType;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec3F;
import net.bhapi.util.MathUtil;
import net.bhapi.util.XorShift128;
import net.minecraft.block.BaseBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.level.BlockView;
import org.lwjgl.opengl.GL11;

public class BHBlockRenderer {
	private static XorShift128 xorShift = new XorShift128();
	private static BlockRenderer renderer;
	private static BlockView blockView;
	
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
		return renderType >= 0 && renderType <= BlockRenderTypes.FIRE;
	}
	
	public static void setRenderer(BlockView view, BlockRenderer renderer) {
		BHBlockRenderer.renderer = renderer;
		BHBlockRenderer.blockView = view;
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
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.start();
		item = true;
		
		itemColor.set(light);
		if (colorizeItem) {
			int color = state.getBlock().getBaseColor(0);
			float r = (float) (color >> 16 & 0xFF) / 255.0F;
			float g = (float) (color >> 8 & 0xFF) / 255.0F;
			float b = (float) (color & 0xFF) / 255.0F;
			itemColor.multiply(r, g, b);
		}
		
		state.getBlock().updateRenderBounds();
		renderAllSides(state, 0, 0, 0);
		
		item = false;
		tessellator.draw();
	}
	
	public static boolean render(BlockState state, int x, int y, int z) {
		byte type = state.getRenderType(blockView, x, y, z);
		if (type == BlockRenderTypes.EMPTY) return false;
		if (type == BlockRenderTypes.FULL_CUBE) return renderFullCube(state, x, y, z);
		if (type == BlockRenderTypes.CROSS) return renderCross(state, x, y, z);
		if (type == BlockRenderTypes.TORCH) return renderTorch(state, x, y, z);
		if (type == BlockRenderTypes.FIRE) return renderFire(state, x, y, z);
		if (type == BlockRenderTypes.CUSTOM) return true; // TODO make custom rendering
		else if (BlockRenderTypes.isVanilla(type)) {
			return renderer.render(state.getBlock(), x, y, z);
		}
		return false;
	}
	
	private static float getBrightness(BaseBlock block, int x, int y, int z) {
		return item ? 1.0F : block.getBrightness(blockView, x, y, z);
	}
	
	private static boolean renderFullCube(BlockState state, int x, int y, int z) {
		float r, g, b;
		if (item) {
			r = itemColor.x; g = itemColor.y; b = itemColor.z;
		}
		else {
			int color = state.getBlock().getColorMultiplier(blockView, x, y, z);
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
			return renderCubeSmooth(state, x, y, z, r, g, b);
		}
		
		return renderCubeFast(state, x, y, z, r, g, b);
	}
	
	private static boolean renderCubeSmooth(BlockState state, int x, int y, int z, float f, float g, float h) {
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
		
		brightnessMiddle = getBrightness(block, x, y, z);
		brightnessNorth = getBrightness(block, x - 1, y, z);
		brightnessBottom = getBrightness(block, x, y - 1, z);
		brightnessEast = getBrightness(block, x, y, z - 1);
		brightnessSouth = getBrightness(block, x + 1, y, z);
		brightnessTop = getBrightness(block, x, y + 1, z);
		brightnessWest = getBrightness(block, x, y, z + 1);
		
		if (blockView instanceof BlockStateProvider) {
			BlockStateProvider provider = BlockStateProvider.cast(blockView);
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
		
		if (renderAllSides || block.isSideRendered(blockView, x, y - 1, z, 0)) {
			brightnessBottomNorth = getBrightness(block, x - 1, --y, z);
			brightnessBottomEast = getBrightness(block, x, y, z - 1);
			brightnessBottomWest = getBrightness(block, x, y, z + 1);
			brightnessBottomSouth = getBrightness(block, x + 1, y, z);
			brightnessBottomNorthEast = allowsGrassUnderBottomEast || allowsGrassUnderBottomNorth ? getBrightness(block, x - 1, y, z - 1) : brightnessBottomNorth;
			brightnessBottomNorthWest = allowsGrassUnderBottomWest || allowsGrassUnderBottomNorth ? getBrightness(block, x - 1, y, z + 1) : brightnessBottomNorth;
			brightnessBottomSouthEast = allowsGrassUnderBottomEast || allowsGrassUnderBottomSouth ? getBrightness(block, x + 1, y, z - 1) : brightnessBottomSouth;
			brightnessBottomSouthWest = allowsGrassUnderBottomWest || allowsGrassUnderBottomSouth ? getBrightness(block, x + 1, y, z + 1) : brightnessBottomSouth;
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
			renderBottomFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 0));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x, y + 1, z, 1)) {
			brightnessTopNorth = getBrightness(block, x - 1, ++y, z);
			brightnessTopSouth = getBrightness(block, x + 1, y, z);
			brightnessTopEast = getBrightness(block, x, y, z - 1);
			brightnessTopWest = getBrightness(block, x, y, z + 1);
			brightnessTopNorthEast = allowsGrassUnderTopEast || allowsGrassUnderTopNorth ? getBrightness(block, x - 1, y, z - 1) : brightnessTopNorth;
			brightnessTopSouthEast = allowsGrassUnderTopEast || allowsGrassUnderTopSouth ? getBrightness(block, x + 1, y, z - 1) : brightnessTopSouth;
			brightnessTopNorthWest = allowsGrassUnderTopWest || allowsGrassUnderTopNorth ? getBrightness(block, x - 1, y, z + 1) : brightnessTopNorth;
			brightnessTopSouthWest = allowsGrassUnderTopWest || allowsGrassUnderTopSouth ? getBrightness(block, x + 1, y, z + 1) : brightnessTopSouth;
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
			renderTopFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 1));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x, y, z - 1, 2)) {
			brightnessNorthEast = getBrightness(block, x - 1, y, --z);
			brightnessBottomEast = getBrightness(block, x, y - 1, z);
			brightnessTopEast = getBrightness(block, x, y + 1, z);
			brightnessSouthEast = getBrightness(block, x + 1, y, z);
			brightnessBottomNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderBottomEast ? block.getBrightness(
				blockView, x - 1, y - 1, z) : brightnessNorthEast;
			brightnessTopNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderTopEast ? block.getBrightness(
				blockView, x - 1, y + 1, z) : brightnessNorthEast;
			brightnessBottomSouthEast = allowsGrassUnderSouthEast || allowsGrassUnderBottomEast ? block.getBrightness(
				blockView, x + 1, y - 1, z) : brightnessSouthEast;
			brightnessTopSouthEast = allowsGrassUnderSouthEast || allowsGrassUnderTopEast ? block.getBrightness(
				blockView, x + 1, y + 1, z) : brightnessSouthEast;
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
			renderEastFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 2));
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
		
		if (renderAllSides || block.isSideRendered(blockView, x, y, z + 1, 3)) {
			brightnessNorthWest = getBrightness(block, x - 1, y, ++z);
			brightnessSouthWest = getBrightness(block, x + 1, y, z);
			brightnessBottomWest = getBrightness(block, x, y - 1, z);
			brightnessTopWest = getBrightness(block, x, y + 1, z);
			brightnessBottomNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderBottomWest ? block.getBrightness(
				blockView, x - 1, y - 1, z) : brightnessNorthWest;
			brightnessTopNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderTopWest ? block.getBrightness(
				blockView, x - 1, y + 1, z) : brightnessNorthWest;
			brightnessBottomSouthWest = allowsGrassUnderSouthWest || allowsGrassUnderBottomWest ? block.getBrightness(
				blockView, x + 1, y - 1, z) : brightnessSouthWest;
			brightnessTopSouthWest = allowsGrassUnderSouthWest || allowsGrassUnderTopWest ? block.getBrightness(
				blockView, x + 1, y + 1, z) : brightnessSouthWest;
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
			renderWestFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 3));
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
		
		if (renderAllSides || block.isSideRendered(blockView, x - 1, y, z, 4)) {
			brightnessBottomNorth = getBrightness(block, --x, y - 1, z);
			brightnessNorthEast = getBrightness(block, x, y, z - 1);
			brightnessNorthWest = getBrightness(block, x, y, z + 1);
			brightnessTopNorth = getBrightness(block, x, y + 1, z);
			brightnessBottomNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderBottomNorth ? block.getBrightness(
				blockView, x, y - 1, z - 1) : brightnessNorthEast;
			brightnessBottomNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderBottomNorth ? block.getBrightness(
				blockView, x, y - 1, z + 1) : brightnessNorthWest;
			brightnessTopNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderTopNorth ? block.getBrightness(
				blockView, x, y + 1, z - 1) : brightnessNorthEast;
			brightnessTopNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderTopNorth ? block.getBrightness(
				blockView, x, y + 1, z + 1) : brightnessNorthWest;
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
			renderNorthFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 4));
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
		
		if (renderAllSides || block.isSideRendered(blockView, x + 1, y, z, 5)) {
			brightnessBottomSouth = getBrightness(block, ++x, y - 1, z);
			brightnessSouthEast = getBrightness(block, x, y, z - 1);
			brightnessSouthWest = getBrightness(block, x, y, z + 1);
			brightnessTopSouth = getBrightness(block, x, y + 1, z);
			brightnessBottomSouthEast = allowsGrassUnderBottomSouth || allowsGrassUnderSouthEast ? block.getBrightness(
				blockView, x, y - 1, z - 1) : brightnessSouthEast;
			brightnessBottomSouthWest = allowsGrassUnderBottomSouth || allowsGrassUnderSouthWest ? block.getBrightness(
				blockView, x, y - 1, z + 1) : brightnessSouthWest;
			brightnessTopSouthEast = allowsGrassUnderTopSouth || allowsGrassUnderSouthEast ? block.getBrightness(
				blockView, x, y + 1, z - 1) : brightnessSouthEast;
			brightnessTopSouthWest = allowsGrassUnderTopSouth || allowsGrassUnderSouthWest ? block.getBrightness(
				blockView, x, y + 1, z + 1) : brightnessSouthWest;
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
			renderSouthFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 5));
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
	
	private static boolean renderCubeFast(BlockState state, int x, int y, int z, float r, float g, float b) {
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
		
		float ewR, ewG, ewB, nsR, nsG, nsB;
		
		if (item) {
			ewR = 0.7F; ewG = 0.7F; ewB = 0.7F;
			nsR = 0.9F; nsG = 0.9F; nsB = 0.9F;
		}
		else {
			ewR = 0.8F; ewG = 0.8F; ewB = 0.8F;
			nsR = 0.6F; nsG = 0.6F; nsB = 0.6F;
		}
		
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
		
		float light2 = getBrightness(block, x, y, z);
		
		if (renderAllSides || block.isSideRendered(blockView, x, y - 1, z, 0)) {
			light = getBrightness(block, x, y - 1, z);
			tessellator.color(bR * light, bG * light, bB * light);
			if (item) tessellator.setNormal(0.0f, -1.0f, 0.0f);
			renderBottomFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 0));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x, y + 1, z, 1)) {
			light = getBrightness(block, x, y + 1, z);
			if (block.maxY != 1.0 && !block.material.isLiquid()) {
				light = light2;
			}
			tessellator.color(tR * light, tG * light, tB * light);
			if (item) tessellator.setNormal(0.0f, 1.0f, 0.0f);
			renderTopFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 1));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x, y, z - 1, 2)) {
			light = block.getBrightness(blockView, x, y, z - 1);
			if (block.minZ > 0.0) {
				light = light2;
			}
			tessellator.color(ewR * light, ewG * light, ewB * light);
			if (item) tessellator.setNormal(0.0f, 0.0f, -1.0f);
			renderEastFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 2));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(ewR * light * r, ewG * light * g, ewB * light * b);
				renderEastFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x, y, z + 1, 3)) {
			light = getBrightness(block, x, y, z + 1);
			if (block.maxZ < 1.0) {
				light = light2;
			}
			
			tessellator.color(ewR * light, ewG * light, ewB * light);
			if (item) tessellator.setNormal(0.0f, 0.0f, 1.0f);
			renderWestFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 3));
			
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(ewR * light * r, ewG * light * g, ewB * light * b);
				renderWestFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x - 1, y, z, 4)) {
			light = getBrightness(block, x - 1, y, z);
			if (block.minX > 0.0) {
				light = light2;
			}
			tessellator.color(nsR * light, nsG * light, nsB * light);
			if (item) tessellator.setNormal(-1.0f, 0.0f, 0.0f);
			renderNorthFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 4));
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(nsR * light * r, nsG * light * g, nsB * light * b);
				renderNorthFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x + 1, y, z, 5)) {
			light = getBrightness(block, x + 1, y, z);
			if (block.maxX < 1.0) {
				light = light2;
			}
			tessellator.color(nsR * light, nsG * light, nsB * light);
			if (item) tessellator.setNormal(1.0f, 0.0f, 0.0f);
			renderSouthFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 5));
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
	
	private static boolean renderCross(BlockState state, int x, int y, int z) {
		Tessellator tessellator = Tessellator.INSTANCE;
		BaseBlock block = state.getBlock();
		float light = getBrightness(block, x, y, z);
		int color = block.getColorMultiplier(blockView, x, y, z);
		
		float r = (float) (color >> 16 & 0xFF) / 255.0f;
		float g = (float) (color >> 8 & 0xFF) / 255.0f;
		float b = (float) (color & 0xFF) / 255.0f;
		
		if (GameRenderer.anaglyph3d) {
			float f5 = (r * 30.0f + g * 59.0f + b * 11.0f) / 100.0f;
			float f6 = (r * 30.0f + g * 70.0f) / 100.0f;
			float f7 = (r * 30.0f + b * 70.0f) / 100.0f;
			r = f5;
			g = f6;
			b = f7;
		}
		tessellator.color(light * r, light * g, light * b);
		
		double px = x;
		double py = y;
		double pz = z;
		
		// TODO Enhance random grass offsets (use xorshift)
		if (block == BaseBlock.TALLGRASS) {
			long l = ((long) x * 3129871) ^ (long) z * 116129781L ^ (long) y;
			l = l * l * 42317861L + l * 11L;
			px += ((double)((float)(l >> 16 & 0xFL) / 15.0f) - 0.5) * 0.5;
			py += ((double)((float)(l >> 20 & 0xFL) / 15.0f) - 1.0) * 0.2;
			pz += ((double)((float)(l >> 24 & 0xFL) / 15.0f) - 0.5) * 0.5;
			/*xorShift.setState(x, y, z, state.getID());
			px += (xorShift.getFloat() - 0.5F) * 0.5F;
			pz += (xorShift.getFloat() - 0.5F) * 0.5F;
			py -= xorShift.getFloat() * 0.2F;*/
		}
		
		renderCross(px, py, pz, state.getTextureForIndex(blockView, x, y, z, 0));
		return true;
	}
	
	private static void renderCross(double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		float u1, u2, v1, v2;
		
		if (breaking) {
			u1 = 0; u2 = 1;
			v1 = 0; v2 = 1;
		}
		else {
			UVPair uv = sample.getUV();
			u1 = uv.getU(0);
			u2 = uv.getU(1);
			v1 = uv.getV(0);
			v2 = uv.getV(1);
		}
		
		double x1 = x + 0.5 - 0.45;
		double x2 = x + 0.5 + 0.45;
		double z1 = z + 0.5 - 0.45;
		double z2 = z + 0.5 + 0.45;
		
		tessellator.vertex(x1, y + 1.0, z1, u1, v1);
		tessellator.vertex(x1, y + 0.0, z1, u1, v2);
		tessellator.vertex(x2, y + 0.0, z2, u2, v2);
		tessellator.vertex(x2, y + 1.0, z2, u2, v1);
		tessellator.vertex(x2, y + 1.0, z2, u1, v1);
		tessellator.vertex(x2, y + 0.0, z2, u1, v2);
		tessellator.vertex(x1, y + 0.0, z1, u2, v2);
		tessellator.vertex(x1, y + 1.0, z1, u2, v1);
		tessellator.vertex(x1, y + 1.0, z2, u1, v1);
		tessellator.vertex(x1, y + 0.0, z2, u1, v2);
		tessellator.vertex(x2, y + 0.0, z1, u2, v2);
		tessellator.vertex(x2, y + 1.0, z1, u2, v1);
		tessellator.vertex(x2, y + 1.0, z1, u1, v1);
		tessellator.vertex(x2, y + 0.0, z1, u1, v2);
		tessellator.vertex(x1, y + 0.0, z2, u2, v2);
		tessellator.vertex(x1, y + 1.0, z2, u2, v1);
	}
	
	private static boolean renderTorch(BlockState state, int x, int y, int z) {
		int meta = 0;
		StateProperty<?> property = state.getProperty("meta");
		if (property != null && property.getType() == BlockPropertyType.INTEGER) {
			meta = (int) state.getValue(property);
		}
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		float light = getBrightness(block, x, y, z);
		if (state.getEmittance() > 0) {
			light = 1.0f;
		}
		
		tessellator.color(light, light, light);
		
		double d = 0.4f;
		double d2 = 0.5 - d;
		double d3 = 0.2f;
		
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 0);
		
		switch (meta) {
			case 1 -> renderTorchSkewed(x - d2, y + d3, z, -d, 0.0, sample);
			case 2 -> renderTorchSkewed(x + d2, y + d3, z, d, 0.0, sample);
			case 3 -> renderTorchSkewed(x, y + d3, z - d2, 0.0, -d, sample);
			case 4 -> renderTorchSkewed(x, y + d3, z + d2, 0.0, d, sample);
			default -> renderTorchSkewed(x, y, z, 0.0, 0.0, sample);
		}
		
		return true;
	}
	
	private static void renderTorchSkewed(double x, double y, double z, double dx, double dz, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		UVPair uv = sample.getUV();
		
		float u11 = uv.getU(0);
		float u12 = uv.getU(1);
		float v11 = uv.getV(0);
		float v12 = uv.getV(1);
		
		double u21 = uv.getU(0.4375F);
		double u22 = uv.getU(0.375F);
		double v21 = uv.getV(0.5625F);
		double v22 = uv.getV(0.5F);
		
		double d6 = (x += 0.5) - 0.5;
		double d7 = x + 0.5;
		double d8 = (z += 0.5) - 0.5;
		double d9 = z + 0.5;
		double d10 = 0.0625;
		double d11 = 0.625;
		
		double x1 = x + dx * (1.0 - d11);
		double z1 = z + dz * (1.0 - d11);
		tessellator.vertex(x1 - d10, y + d11, z1 - d10, u21, u22);
		tessellator.vertex(x1 - d10, y + d11, z1 + d10, u21, v22);
		tessellator.vertex(x1 + d10, y + d11, z1 + d10, v21, v22);
		tessellator.vertex(x1 + d10, y + d11, z1 - d10, v21, u22);
		tessellator.vertex(x - d10, y + 1.0, d8, u11, v11);
		tessellator.vertex(x - d10 + dx, y + 0.0, d8 + dz, u11, v12);
		tessellator.vertex(x - d10 + dx, y + 0.0, d9 + dz, u12, v12);
		tessellator.vertex(x - d10, y + 1.0, d9, u12, v11);
		tessellator.vertex(x + d10, y + 1.0, d9, u11, v11);
		tessellator.vertex(x + dx + d10, y + 0.0, d9 + dz, u11, v12);
		tessellator.vertex(x + dx + d10, y + 0.0, d8 + dz, u12, v12);
		tessellator.vertex(x + d10, y + 1.0, d8, u12, v11);
		tessellator.vertex(d6, y + 1.0, z + d10, u11, v11);
		tessellator.vertex(d6 + dx, y + 0.0, z + d10 + dz, u11, v12);
		tessellator.vertex(d7 + dx, y + 0.0, z + d10 + dz, u12, v12);
		tessellator.vertex(d7, y + 1.0, z + d10, u12, v11);
		tessellator.vertex(d7, y + 1.0, z - d10, u11, v11);
		tessellator.vertex(d7 + dx, y + 0.0, z - d10 + dz, u11, v12);
		tessellator.vertex(d6 + dx, y + 0.0, z - d10 + dz, u12, v12);
		tessellator.vertex(d6, y + 1.0, z - d10, u12, v11);
	}
	
	private static boolean renderFire(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		float light = block.getBrightness(blockView, x, y, z);
		tessellator.color(light, light, light);
		
		UVPair uv1 = state.getTextureForIndex(blockView, x, y, z, 0).getUV();
		UVPair uv2 = state.getTextureForIndex(blockView, x, y, z, 1).getUV();
		float u1, u2, v1, v2;
		
		if (breaking) {
			u1 = 0; u2 = 1;
			v1 = 0; v2 = 1;
		}
		else {
			u1 = uv1.getU(0);
			u2 = uv1.getU(1);
			v1 = uv1.getV(0);
			v2 = uv1.getV(1);
		}
		
		float size = 1.4f;
		if (blockView.canSuffocate(x, y - 1, z) || BaseBlock.FIRE.method_1824(blockView, x, y - 1, z)) {
			double x2 = x + 0.5 + 0.2;
			double x1 = x + 0.5 - 0.2;
			double z2 = z + 0.5 + 0.2;
			double z1 = z + 0.5 - 0.2;
			double x3 = x + 0.5 - 0.3;
			double x4 = x + 0.5 + 0.3;
			double z3 = z + 0.5 - 0.3;
			double z4 = z + 0.5 + 0.3;
			
			tessellator.vertex(x3, y + size, z + 1, u2, v1);
			tessellator.vertex(x2, y, z + 1, u2, v2);
			tessellator.vertex(x2, y, z, u1, v2);
			tessellator.vertex(x3, y + size, z, u1, v1);
			tessellator.vertex(x4, y + size, z, u2, v1);
			tessellator.vertex(x1, y, z, u2, v2);
			tessellator.vertex(x1, y, z + 1, u1, v2);
			tessellator.vertex(x4, y + size, z + 1, u1, v1);
			
			u1 = uv2.getU(0);
			u2 = uv2.getU(1);
			v1 = uv2.getV(0);
			v2 = uv2.getV(1);
			
			tessellator.vertex(x + 1, y + size, z4, u2, v1);
			tessellator.vertex(x + 1, y, z1, u2, v2);
			tessellator.vertex(x, y, z1, u1, v2);
			tessellator.vertex(x, y + size, z4, u1, v1);
			tessellator.vertex(x, y + size, z3, u2, v1);
			tessellator.vertex(x, y, z2, u2, v2);
			tessellator.vertex(x + 1, y, z2, u1, v2);
			tessellator.vertex(x + 1, y + size, z3, u1, v1);
			
			x2 = x + 0.5 - 0.5;
			x1 = x + 0.5 + 0.5;
			z2 = z + 0.5 - 0.5;
			z1 = z + 0.5 + 0.5;
			x3 = x + 0.5 - 0.4;
			x4 = x + 0.5 + 0.4;
			z3 = z + 0.5 - 0.4;
			z4 = z + 0.5 + 0.4;
			
			tessellator.vertex(x3, y + size, z, u1, v1);
			tessellator.vertex(x2, y, z, u1, v2);
			tessellator.vertex(x2, y, z + 1, u2, v2);
			tessellator.vertex(x3, y + size, z + 1, u2, v1);
			tessellator.vertex(x4, y + size, z + 1, u1, v1);
			tessellator.vertex(x1, y, z + 1, u1, v2);
			tessellator.vertex(x1, y, z, u2, v2);
			tessellator.vertex(x4, y + size, z, u2, v1);
			
			u1 = uv1.getU(0);
			u2 = uv1.getU(1);
			v1 = uv1.getV(0);
			v2 = uv1.getV(1);
			
			tessellator.vertex(x, y + size, z4, u1, v1);
			tessellator.vertex(x, y, z1, u1, v2);
			tessellator.vertex(x + 1, y, z1, u2, v2);
			tessellator.vertex(x + 1, y + size, z4, u2, v1);
			tessellator.vertex(x + 1, y + size, z3, u1, v1);
			tessellator.vertex(x + 1, y, z2, u1, v2);
			tessellator.vertex(x, y, z2, u2, v2);
			tessellator.vertex(x, y + size, z3, u2, v1);
		}
		else {
			float f3 = 0.2f;
			float f4 = 0.0625f;
			
			if ((x + y + z & 1) == 1) {
				u1 = uv2.getU(0);
				u2 = uv2.getU(1);
				v1 = uv2.getV(0);
				v2 = uv2.getV(1);
			}
			
			if ((x / 2 + y / 2 + z / 2 & 1) == 1) {
				float u = u2;
				u2 = u1;
				u1 = u;
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x - 1, y, z)) {
				tessellator.vertex(x + f3, y + size + f4, z + 1, u2, v1);
				tessellator.vertex(x, y + f4, z + 1, u2, v2);
				tessellator.vertex(x, y + f4, z, u1, v2);
				tessellator.vertex(x + f3, y + size + f4, z, u1, v1);
				tessellator.vertex(x + f3, y + size + f4, z, u1, v1);
				tessellator.vertex(x, y + f4, z, u1, v2);
				tessellator.vertex(x, y + f4, z + 1, u2, v2);
				tessellator.vertex(x + f3, y + size + f4, z + 1, u2, v1);
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x + 1, y, z)) {
				tessellator.vertex(x + 1 - f3, y + size + f4, z, u1, v1);
				tessellator.vertex(x + 1, y + f4, z, u1, v2);
				tessellator.vertex(x + 1, y + f4, z + 1, u2, v2);
				tessellator.vertex(x + 1 - f3, y + size + f4, z + 1, u2, v1);
				tessellator.vertex(x + 1 - f3, y + size + f4, z + 1, u2, v1);
				tessellator.vertex(x + 1, y + f4, z + 1, u2, v2);
				tessellator.vertex(x + 1, y + f4, z, u1, v2);
				tessellator.vertex(x + 1 - f3, y + size + f4, z, u1, v1);
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x, y, z - 1)) {
				tessellator.vertex(x, y + size + f4, z + f3, u2, v1);
				tessellator.vertex(x, y + f4, z, u2, v2);
				tessellator.vertex(x + 1, y + f4, z, u1, v2);
				tessellator.vertex(x + 1, y + size + f4, z + f3, u1, v1);
				tessellator.vertex(x + 1, y + size + f4, z + f3, u1, v1);
				tessellator.vertex(x + 1, y + f4, z, u1, v2);
				tessellator.vertex(x, y + f4, z, u2, v2);
				tessellator.vertex(x, y + size + f4, z + f3, u2, v1);
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x, y, z + 1)) {
				tessellator.vertex(x + 1, y + size + f4, z + 1 - f3, u1, v1);
				tessellator.vertex(x + 1, y + f4, z + 1, u1, v2);
				tessellator.vertex(x, y + f4, z + 1, u2, v2);
				tessellator.vertex(x, y + size + f4, z + 1 - f3, u2, v1);
				tessellator.vertex(x, y + size + f4, z + 1 - f3, u2, v1);
				tessellator.vertex(x, y + f4, z + 1, u2, v2);
				tessellator.vertex(x + 1, y + f4, z + 1, u1, v2);
				tessellator.vertex(x + 1, y + size + f4, z + 1 - f3, u1, v1);
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x, y + 1, z)) {
				double x6 = x + 0.5 + 0.5;
				double x5 = x + 0.5 - 0.5;
				double z6 = z + 0.5 + 0.5;
				double z5 = z + 0.5 - 0.5;
				double x7 = x + 0.5 - 0.5;
				double x8 = x + 0.5 + 0.5;
				double z7 = z + 0.5 - 0.5;
				double z8 = z + 0.5 + 0.5;
				
				size = -0.2f;
				if ((x + ++y + z & 1) == 0) {
					tessellator.vertex(x7, y + size, z, u2, v1);
					tessellator.vertex(x6, y, z, u2, v2);
					tessellator.vertex(x6, y, z + 1, u1, v2);
					tessellator.vertex(x7, y + size, z + 1, u1, v1);
					
					u1 = uv2.getU(0);
					u2 = uv2.getU(1);
					v1 = uv2.getV(0);
					v2 = uv2.getV(1);
					
					tessellator.vertex(x8, y + size, z + 1, u2, v1);
					tessellator.vertex(x5, y, z + 1, u2, v2);
					tessellator.vertex(x5, y, z, u1, v2);
					tessellator.vertex(x8, y + size, z, u1, v1);
				}
				else {
					tessellator.vertex(x, y + size, z8, u2, v1);
					tessellator.vertex(x, y, z5, u2, v2);
					tessellator.vertex(x + 1, y, z5, u1, v2);
					tessellator.vertex(x + 1, y + size, z8, u1, v1);
					
					u1 = uv2.getU(0);
					u2 = uv2.getU(1);
					v1 = uv2.getV(0);
					v2 = uv2.getV(1);
					
					tessellator.vertex(x + 1, y + size, z7, u2, v1);
					tessellator.vertex(x + 1, y, z6, u2, v2);
					tessellator.vertex(x, y, z6, u1, v2);
					tessellator.vertex(x, y + size, z7, u1, v1);
				}
			}
		}
		
		return true;
	}
}
