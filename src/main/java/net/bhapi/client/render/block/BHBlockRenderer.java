package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec3F;
import net.bhapi.util.MathUtil;
import net.bhapi.util.XorShift128;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.RedstoneDustBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.technical.MagicBedNumbers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.level.BlockView;
import net.minecraft.util.maths.MathHelper;
import net.minecraft.util.maths.Vec3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BHBlockRenderer {
	private static final XorShift128 xorShift = new XorShift128();
	private static final List<BlockRenderingFunction> RENDER_FUNCTIONS = new ArrayList<>();
	private static BlockRenderer renderer;
	private static BlockView blockView;
	
	static {
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderFullCube);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderCross);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderTorch);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderFire);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderFluid);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderRedstoneDust);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderCrops);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderDoor);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderLadder);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderRails);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderStairs);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderFence);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderLever);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderCactus);
		RENDER_FUNCTIONS.add(BHBlockRenderer::renderBed);
	}
	
	private static boolean mirrorTexture = false;
	private static boolean renderAllSides = false;
	public static boolean itemColorEnabled = true;
	private static final int eastFaceRotation = 0;
	private static final int westFaceRotation = 0;
	private static final int southFaceRotation = 0;
	private static final int northFaceRotation = 0;
	private static final int topFaceRotation = 0;
	private static final int bottomFaceRotation = 0;
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
	private static final boolean fancyGraphics = true;
	private static boolean breaking = false;
	private static boolean item = false;
	
	private static final Vec3F itemColor = new Vec3F();
	
	public static boolean isImplemented(int renderType) {
		return renderType >= 0 && renderType < RENDER_FUNCTIONS.size();
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
		state.getBlock().updateBoundingBox(blockView, x, y, z);
		byte type = state.getRenderType(blockView, x, y, z);
		if (type == BlockRenderTypes.EMPTY) return false;
		if (type < RENDER_FUNCTIONS.size()) {
			return RENDER_FUNCTIONS.get(type).render(state, x, y, z);
		}
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
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking && block.getTextureForSide(blockView, x, y, z, 2) != 68) {
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
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking && block.getTextureForSide(blockView, x, y, z, 2) != 68) {
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
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking && block.getTextureForSide(blockView, x, y, z, 2) != 68) {
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
			if (fancyGraphics && block == BaseBlock.GRASS && !breaking && block.getTextureForSide(blockView, x, y, z, 2) != 68) {
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
			v11 = uv.getV(MathUtil.clamp(1F - (float) block.minZ, 0, 1));
			v12 = uv.getV(MathUtil.clamp(1F - (float) block.maxZ, 0, 1));
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
			u11 = uv.getU(MathUtil.clamp(1F - (float) block.maxX, 0, 1));
			u12 = uv.getU(MathUtil.clamp(1F - (float) block.minX, 0, 1));
			v11 = uv.getV(MathUtil.clamp(1F - (float) block.maxY, 0, 1));
			v12 = uv.getV(MathUtil.clamp(1F - (float) block.minY, 0, 1));
		}
		
		if (mirrorTexture) {
			float u = u11;
			u11 = u12;
			u12 = u;
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		/*if (mirrorTexture) {
			u22 = u11;
			u11 = u12;
			u12 = u22;
		}*/
		
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
			v11 = uv.getV(MathUtil.clamp(1F - (float) block.minY, 0, 1));
			v12 = uv.getV(MathUtil.clamp(1F - (float) block.maxY, 0, 1));
		}
		
		if (mirrorTexture) {
			float u = u11;
			u11 = u12;
			u12 = u;
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		/*if (mirrorTexture) {
			u21 = u11;
			u11 = u12;
			u12 = u21;
		}*/
		
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
	
	private static void renderNorthFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
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
			v11 = uv.getV(MathUtil.clamp(1F - (float) block.minY, 0, 1));
			v12 = uv.getV(MathUtil.clamp(1F - (float) block.maxY, 0, 1));
		}
		
		if (mirrorTexture) {
			float u = u11;
			u11 = u12;
			u12 = u;
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		/*if (mirrorTexture) {
			u22 = u11;
			u11 = u12;
			u12 = u22;
		}*/
		
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
			u11 = uv.getU(MathUtil.clamp(1F - (float) block.maxZ, 0, 1));
			u12 = uv.getU(MathUtil.clamp(1F - (float) block.minZ, 0, 1));
			v11 = uv.getV(MathUtil.clamp(1F - (float) block.minY, 0, 1));
			v12 = uv.getV(MathUtil.clamp(1F - (float) block.maxY, 0, 1));
		}
		
		if (mirrorTexture) {
			float u = u11;
			u11 = u12;
			u12 = u;
		}
		
		float u22 = u12;
		float u21 = u11;
		float v21 = v11;
		float v22 = v12;
		
		/*if (mirrorTexture) {
			u22 = u11;
			u11 = u12;
			u12 = u22;
		}*/
		
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
		int meta = state.getMeta();
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
		
		float u21 = uv.getU(0.4375F);
		float u22 = uv.getU(0.5625F);
		float v21 = uv.getV(0.375F);
		float v22 = uv.getV(0.5F);
		
		double x2 = (x += 0.5) - 0.5;
		double x3 = x + 0.5;
		double z2 = (z += 0.5) - 0.5;
		double z3 = z + 0.5;
		
		double x1 = x + dx * (1.0 - 0.625);
		double z1 = z + dz * (1.0 - 0.625);
		
		tessellator.vertex(x1 - 0.0625, y + 0.625, z1 - 0.0625, u21, v21);
		tessellator.vertex(x1 - 0.0625, y + 0.625, z1 + 0.0625, u21, v22);
		tessellator.vertex(x1 + 0.0625, y + 0.625, z1 + 0.0625, u22, v22);
		tessellator.vertex(x1 + 0.0625, y + 0.625, z1 - 0.0625, u22, v21);
		
		tessellator.vertex(x - 0.0625, y + 1.0, z2, u11, v11);
		tessellator.vertex(x - 0.0625 + dx, y + 0.0, z2 + dz, u11, v12);
		tessellator.vertex(x - 0.0625 + dx, y + 0.0, z3 + dz, u12, v12);
		tessellator.vertex(x - 0.0625, y + 1.0, z3, u12, v11);
		
		tessellator.vertex(x + 0.0625, y + 1.0, z3, u11, v11);
		tessellator.vertex(x + dx + 0.0625, y + 0.0, z3 + dz, u11, v12);
		tessellator.vertex(x + dx + 0.0625, y + 0.0, z2 + dz, u12, v12);
		tessellator.vertex(x + 0.0625, y + 1.0, z2, u12, v11);
		
		tessellator.vertex(x2, y + 1.0, z + 0.0625, u11, v11);
		tessellator.vertex(x2 + dx, y + 0.0, z + 0.0625 + dz, u11, v12);
		tessellator.vertex(x3 + dx, y + 0.0, z + 0.0625 + dz, u12, v12);
		tessellator.vertex(x3, y + 1.0, z + 0.0625, u12, v11);
		
		tessellator.vertex(x3, y + 1.0, z - 0.0625, u11, v11);
		tessellator.vertex(x3 + dx, y + 0.0, z - 0.0625 + dz, u11, v12);
		tessellator.vertex(x2 + dx, y + 0.0, z - 0.0625 + dz, u12, v12);
		tessellator.vertex(x2, y + 1.0, z - 0.0625, u12, v11);
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
	
	private static boolean renderFluid(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		
		Tessellator tessellator = Tessellator.INSTANCE;
		
		int color = block.getColorMultiplier(blockView, x, y, z);
		float r = (float) (color >> 16 & 0xFF) / 255.0f;
		float g = (float) (color >> 8 & 0xFF) / 255.0f;
		float b = (float) (color & 0xFF) / 255.0f;
		
		boolean renderTop = block.isSideRendered(blockView, x, y + 1, z, 1);
		boolean renderBottom = block.isSideRendered(blockView, x, y - 1, z, 0);
		
		boolean[] renderSides = new boolean[] {
			block.isSideRendered(blockView, x, y, z - 1, 2),
			block.isSideRendered(blockView, x, y, z + 1, 3),
			block.isSideRendered(blockView, x - 1, y, z, 4),
			block.isSideRendered(blockView, x + 1, y, z, 5)
		};
		
		if (!(renderTop || renderBottom || renderSides[0] || renderSides[1] || renderSides[2] || renderSides[3])) {
			return false;
		}
		
		boolean result = false;
		double d = 0.0;
		double d2 = 1.0;
		
		Material material = block.material;
		int meta = blockView.getBlockMeta(x, y, z);
		float h1 = getFluidHeight(x, y, z, material);
		float h2 = getFluidHeight(x, y, z + 1, material);
		float h3 = getFluidHeight(x + 1, y, z + 1, material);
		float h4 = getFluidHeight(x + 1, y, z, material);
		
		if (renderAllSides || renderTop) {
			float angle = (float) FluidBlock.getFluidAngle(blockView, x, y, z, material);
			
			boolean isFlowing = angle > -999.0f;
			TextureSample sample = state.getTextureForIndex(blockView, x, y, z, isFlowing ? 2 : 1);
			UVPair uv = sample.getUV();
			
			float u1 = isFlowing ? 0.25F : 0.5F;
			float v1 = isFlowing ? 0.25F : 0.5F;
			
			if (angle < -999.0f) {
				angle = 0.0f;
			}
			else {
				u1 = isFlowing ? 0.5F : 1.0F;
				v1 = isFlowing ? 0.5F : 1.0F;
			}
			
			float sin = MathHelper.sin(angle) * (isFlowing ? 0.25F : 0.5F);
			float cos = MathHelper.cos(angle) * (isFlowing ? 0.25F : 0.5F);
			
			float light = block.getBrightness(blockView, x, y, z);
			tessellator.color(light * r, light * g, light * b);
			
			tessellator.vertex(x, y + h1, z, uv.getU(u1 - cos - sin), uv.getV(v1 - cos + sin));
			tessellator.vertex(x, y + h2, z + 1, uv.getU(u1 - cos + sin), uv.getV(v1 + cos + sin));
			tessellator.vertex(x + 1, y + h3, z + 1, uv.getU(u1 + cos + sin), uv.getV(v1 + cos - sin));
			tessellator.vertex(x + 1, y + h4, z, uv.getU(u1 + cos - sin), uv.getV(v1 - cos - sin));
			result = true;
		}
		
		if (renderAllSides || renderBottom) {
			float light = getBrightness(block, x, y - 1, z) * 0.5F;
			tessellator.color(light, light, light);
			renderBottomFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 0));
			result = true;
		}
		
		float px1, px2, py1, py2, pz1, pz2;
		for (int side = 0; side < 4; ++side) {
			int dx = x;
			int dy = y;
			int dz = z;
			
			if (side == 0) --dz;
			if (side == 1) ++dz;
			if (side == 2) --dx;
			if (side == 3) ++dx;
			
			/*int n7 = block.getTextureForSide(side + 2, meta);
			int n8 = (n7 & 0xF) << 4;
			int n9 = n7 & 0xF0;*/
			
			if (!renderAllSides && !renderSides[side]) continue;
			
			if (side == 0) {
				py1 = h1;
				py2 = h4;
				px1 = x;
				px2 = x + 1;
				pz1 = z;
				pz2 = z;
			}
			else if (side == 1) {
				py1 = h3;
				py2 = h2;
				px1 = x + 1;
				px2 = x;
				pz1 = z + 1;
				pz2 = z + 1;
			}
			else if (side == 2) {
				py1 = h2;
				py2 = h1;
				px1 = x;
				px2 = x;
				pz1 = z + 1;
				pz2 = z;
			}
			else {
				py1 = h4;
				py2 = h3;
				px1 = x + 1;
				px2 = x + 1;
				pz1 = z;
				pz2 = z + 1;
			}
			
			TextureSample sample = state.getTextureForIndex(blockView, x, y, z, side + 2);
			UVPair uv = sample.getUV();
			
			double u1 = uv.getU(0);
			double u2 = uv.getU(0.5F);
			double v1 = uv.getV(0.5F - py1 * 0.5F);
			double v2 = uv.getV(0.5F - py2 * 0.5F);
			double v3 = uv.getV(0.5F);
			float light = block.getBrightness(blockView, dx, dy, dz);
			
			light = side < 2 ? (light * 0.8f) : (light * 0.6f);
			
			tessellator.color(light * r, light * g, light * b);
			
			tessellator.vertex(px1, y + py1, pz1, u1, v1);
			tessellator.vertex(px2, y + py2, pz2, u2, v2);
			tessellator.vertex(px2, y, pz2, u2, v3);
			tessellator.vertex(px1, y, pz1, u1, v3);
			result = true;
		}
		
		block.minY = d;
		block.maxY = d2;
		
		return result;
	}
	
	private static float getFluidHeight(int x, int y, int z, Material material) {
		int iteration = 0;
		float offset = 0.0f;
		
		for (int i2 = 0; i2 < 4; ++i2) {
			int px = x - (i2 & 1);
			int py = y;
			int pz = z - (i2 >> 1 & 1);
			
			if (blockView.getMaterial(px, py + 1, pz) == material) {
				return 1.0f;
			}
			
			Material levelMaterial = blockView.getMaterial(px, py, pz);
			
			if (material == levelMaterial) {
				int meta = blockView.getBlockMeta(px, py, pz);
				if (meta >= 8 || meta == 0) {
					offset += FluidBlock.getVisualHeight(meta) * 10.0f;
					iteration += 10;
				}
				offset += FluidBlock.getVisualHeight(meta);
				++iteration;
				continue;
			}
			if (levelMaterial.isSolid()) continue;
			
			offset += 1.0f;
			++iteration;
		}
		
		return 1.0f - offset / (float) iteration;
	}
	
	private static boolean renderRedstoneDust(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		
		Tessellator tessellator = Tessellator.INSTANCE;
		int meta = blockView.getBlockMeta(x, y, z);
		int n2 = block.getTextureForSide(1, meta);
		
		float light = block.getBrightness(blockView, x, y, z);
		float power = (float) meta / 15.0f;
		
		float r = power * 0.6f + 0.4f;
		float g = power * power * 0.7f - 0.5f;
		float b = power * power * 0.6f - 0.7f;
		
		if (meta == 0) r = 0.3f;
		if (g < 0.0f) g = 0.0f;
		if (b < 0.0f) b = 0.0f;
		
		tessellator.color(light * r, light * g, light * b);
		
		UVPair uv1 = state.getTextureForIndex(blockView, x, y, z, 0).getUV();
		UVPair uv2 = state.getTextureForIndex(blockView, x, y, z, 1).getUV();
		
		float u1 = uv1.getU(0);
		float u2 = uv1.getU(1);
		float v1 = uv1.getV(0);
		float v2 = uv1.getV(1);
		
		boolean cx1 = RedstoneDustBlock.canConnect(blockView, x - 1, y, z, 1) || !blockView.canSuffocate(x - 1, y, z) && RedstoneDustBlock.canConnect(blockView, x - 1, y - 1, z, -1);
		boolean cx2 = RedstoneDustBlock.canConnect(blockView, x + 1, y, z, 3) || !blockView.canSuffocate(x + 1, y, z) && RedstoneDustBlock.canConnect(blockView, x + 1, y - 1, z, -1);
		boolean cz1 = RedstoneDustBlock.canConnect(blockView, x, y, z - 1, 2) || !blockView.canSuffocate(x, y, z - 1) && RedstoneDustBlock.canConnect(blockView, x, y - 1, z - 1, -1);
		boolean cz2 = RedstoneDustBlock.canConnect(blockView, x, y, z + 1, 0) || !blockView.canSuffocate(x, y, z + 1) && RedstoneDustBlock.canConnect(blockView, x, y - 1, z + 1, -1);
		
		if (!blockView.canSuffocate(x, y + 1, z)) {
			if (blockView.canSuffocate(x - 1, y, z) && RedstoneDustBlock.canConnect(blockView, x - 1, y + 1, z, -1)) {
				cx1 = true;
			}
			if (blockView.canSuffocate(x + 1, y, z) && RedstoneDustBlock.canConnect(blockView, x + 1, y + 1, z, -1)) {
				cx2 = true;
			}
			if (blockView.canSuffocate(x, y, z - 1) && RedstoneDustBlock.canConnect(blockView, x, y + 1, z - 1, -1)) {
				cz1 = true;
			}
			if (blockView.canSuffocate(x, y, z + 1) && RedstoneDustBlock.canConnect(blockView, x, y + 1, z + 1, -1)) {
				cz2 = true;
			}
		}
		
		float x1 = x;
		float x2 = x + 1;
		float z1 = z;
		float z2 = z + 1;
		
		int type = 0;
		if ((cx1 || cx2) && !cz1 && !cz2) type = 1;
		if ((cz1 || cz2) && !cx2 && !cx1) type = 2;
		
		if (type != 0) {
			u1 = uv2.getU(0);
			u2 = uv2.getU(1);
			v1 = uv2.getV(0);
			v2 = uv2.getV(1);
		}
		
		if (type == 0) {
			if (cx2 || cz1 || cz2 || cx1) {
				if (!cx1) x1 += 0.3125f;
				if (!cx1) u1 = uv1.getU(0.3125F);
				if (!cx2) x2 -= 0.3125f;
				if (!cx2) u2 = uv1.getU(0.6875F);
				if (!cz1) z1 += 0.3125f;
				if (!cz1) v1 = uv1.getV(0.3125F);
				if (!cz2) z2 -= 0.3125f;
				if (!cz2) v2 = uv1.getV(0.6875F);
			}
			
			tessellator.vertex(x2, y + 0.015625f, z2, u2, v2);
			tessellator.vertex(x2, y + 0.015625f, z1, u2, v1);
			tessellator.vertex(x1, y + 0.015625f, z1, u1, v1);
			tessellator.vertex(x1, y + 0.015625f, z2, u1, v2);
		}
		else if (type == 1) {
			tessellator.vertex(x2, y + 0.015625f, z2, u2, v2);
			tessellator.vertex(x2, y + 0.015625f, z1, u2, v1);
			tessellator.vertex(x1, y + 0.015625f, z1, u1, v1);
			tessellator.vertex(x1, y + 0.015625f, z2, u1, v2);
		}
		else {
			tessellator.vertex(x2, y + 0.015625f, z2, u2, v2);
			tessellator.vertex(x2, y + 0.015625f, z1, u1, v2);
			tessellator.vertex(x1, y + 0.015625f, z1, u1, v1);
			tessellator.vertex(x1, y + 0.015625f, z2, u2, v1);
		}
		
		if (!blockView.canSuffocate(x, y + 1, z)) {
			u1 = uv2.getU(0);
			u2 = uv2.getU(1);
			v1 = uv2.getV(0);
			v2 = uv2.getV(1);
			
			if (blockView.canSuffocate(x - 1, y, z) && blockView.getBlockId(x - 1, y + 1, z) == BaseBlock.REDSTONE_DUST.id) {
				tessellator.color(light * r, light * g, light * b);
				tessellator.vertex(x + 0.015625f, (float)(y + 1) + 0.021875f, z + 1, u2, v1);
				tessellator.vertex(x + 0.015625f, y + 0, z + 1, u1, v1);
				tessellator.vertex(x + 0.015625f, y + 0, z + 0, u1, v2);
				tessellator.vertex(x + 0.015625f, (float)(y + 1) + 0.021875f, z + 0, u2, v2);
			}
			
			if (blockView.canSuffocate(x + 1, y, z) && blockView.getBlockId(x + 1, y + 1, z) == BaseBlock.REDSTONE_DUST.id) {
				tessellator.color(light * r, light * g, light * b);
				tessellator.vertex((float)(x + 1) - 0.015625f, y + 0, z + 1, u1, v2);
				tessellator.vertex((float)(x + 1) - 0.015625f, (float)(y + 1) + 0.021875f, z + 1, u2, v2);
				tessellator.vertex((float)(x + 1) - 0.015625f, (float)(y + 1) + 0.021875f, z + 0, u2, v1);
				tessellator.vertex((float)(x + 1) - 0.015625f, y + 0, z + 0, u1, v1);
			}
			
			if (blockView.canSuffocate(x, y, z - 1) && blockView.getBlockId(x, y + 1, z - 1) == BaseBlock.REDSTONE_DUST.id) {
				tessellator.color(light * r, light * g, light * b);
				tessellator.vertex(x + 1, y + 0, (float)z + 0.015625f, u1, v2);
				tessellator.vertex(x + 1, (float)(y + 1) + 0.021875f, (float)z + 0.015625f, u2, v2);
				tessellator.vertex(x + 0, (float)(y + 1) + 0.021875f, (float)z + 0.015625f, u2, v1);
				tessellator.vertex(x + 0, y + 0, (float)z + 0.015625f, u1, v1);
			}
			
			if (blockView.canSuffocate(x, y, z + 1) && blockView.getBlockId(x, y + 1, z + 1) == BaseBlock.REDSTONE_DUST.id) {
				tessellator.color(light * r, light * g, light * b);
				tessellator.vertex(x + 1, (float)(y + 1) + 0.021875f, (float)(z + 1) - 0.015625f, u2, v1);
				tessellator.vertex(x + 1, y + 0, (float)(z + 1) - 0.015625f, u1, v1);
				tessellator.vertex(x + 0, y + 0, (float)(z + 1) - 0.015625f, u1, v2);
				tessellator.vertex(x + 0, (float)(y + 1) + 0.021875f, (float)(z + 1) - 0.015625f, u2, v2);
			}
		}
		
		return true;
	}
	
	private static boolean renderCrops(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		float light = getBrightness(block, x, y, z);
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.color(light, light, light);
		int meta = blockView.getBlockMeta(x, y, z);
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, meta);
		renderCrop(x, y - 0.0625f, z, sample);
		return true;
	}
	
	private static void renderCrop(double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		UVPair uv = sample.getUV();
		
		float u1 = uv.getU(0);
		float u2 = uv.getU(1);
		float v1 = uv.getV(0);
		float v2 = uv.getV(1);
		
		double x1 = x + 0.5 - 0.25;
		double x2 = x + 0.5 + 0.25;
		double z1 = z + 0.5 - 0.5;
		double z2 = z + 0.5 + 0.5;
		
		tessellator.vertex(x1, y + 1.0, z1, u1, v1);
		tessellator.vertex(x1, y + 0.0, z1, u1, v2);
		tessellator.vertex(x1, y + 0.0, z2, u2, v2);
		tessellator.vertex(x1, y + 1.0, z2, u2, v1);
		tessellator.vertex(x1, y + 1.0, z2, u1, v1);
		tessellator.vertex(x1, y + 0.0, z2, u1, v2);
		tessellator.vertex(x1, y + 0.0, z1, u2, v2);
		tessellator.vertex(x1, y + 1.0, z1, u2, v1);
		tessellator.vertex(x2, y + 1.0, z2, u1, v1);
		tessellator.vertex(x2, y + 0.0, z2, u1, v2);
		tessellator.vertex(x2, y + 0.0, z1, u2, v2);
		tessellator.vertex(x2, y + 1.0, z1, u2, v1);
		tessellator.vertex(x2, y + 1.0, z1, u1, v1);
		tessellator.vertex(x2, y + 0.0, z1, u1, v2);
		tessellator.vertex(x2, y + 0.0, z2, u2, v2);
		tessellator.vertex(x2, y + 1.0, z2, u2, v1);
		
		x1 = x + 0.5 - 0.5;
		x2 = x + 0.5 + 0.5;
		z1 = z + 0.5 - 0.25;
		z2 = z + 0.5 + 0.25;
		
		tessellator.vertex(x1, y + 1.0, z1, u1, v1);
		tessellator.vertex(x1, y + 0.0, z1, u1, v2);
		tessellator.vertex(x2, y + 0.0, z1, u2, v2);
		tessellator.vertex(x2, y + 1.0, z1, u2, v1);
		tessellator.vertex(x2, y + 1.0, z1, u1, v1);
		tessellator.vertex(x2, y + 0.0, z1, u1, v2);
		tessellator.vertex(x1, y + 0.0, z1, u2, v2);
		tessellator.vertex(x1, y + 1.0, z1, u2, v1);
		tessellator.vertex(x2, y + 1.0, z2, u1, v1);
		tessellator.vertex(x2, y + 0.0, z2, u1, v2);
		tessellator.vertex(x1, y + 0.0, z2, u2, v2);
		tessellator.vertex(x1, y + 1.0, z2, u2, v1);
		tessellator.vertex(x1, y + 1.0, z2, u1, v1);
		tessellator.vertex(x1, y + 0.0, z2, u1, v2);
		tessellator.vertex(x2, y + 0.0, z2, u2, v2);
		tessellator.vertex(x2, y + 1.0, z2, u2, v1);
	}
	
	private static boolean renderDoor(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		DoorBlock doorBlock = (DoorBlock) block;
		
		// TODO switch to this in the future
		//renderCubeSmooth(state, x, y, z, 1F, 1F, 1F);
		
		float f = 0.5f;
		float f2 = 1.0f;
		float f3 = 0.8f;
		float f4 = 0.6f;
		
		float lightT = block.getBrightness(blockView, x, y, z);
		float lightB = block.getBrightness(blockView, x, y - 1, z);
		
		if (doorBlock.minY > 0.0) {
			lightB = lightT;
		}
		
		if (state.getEmittance() > 0) {
			lightB = 1.0f;
		}
		
		tessellator.color(f * lightB, f * lightB, f * lightB);
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 0);
		renderBottomFace(block, x, y, z, sample);
		
		lightB = block.getBrightness(blockView, x, y + 1, z);
		
		if (doorBlock.maxY < 1.0) {
			lightB = lightT;
		}
		
		if (state.getEmittance() > 0) {
			lightB = 1.0f;
		}
		
		tessellator.color(f2 * lightB, f2 * lightB, f2 * lightB);
		sample = state.getTextureForIndex(blockView, x, y, z, 1);
		renderTopFace(block, x, y, z, sample);
		lightB = block.getBrightness(blockView, x, y, z - 1);
		
		if (doorBlock.minZ > 0.0) {
			lightB = lightT;
		}
		
		if (state.getEmittance() > 0) {
			lightB = 1.0f;
		}
		
		tessellator.color(f3 * lightB, f3 * lightB, f3 * lightB);
		sample = state.getTextureForIndex(blockView, x, y, z, 2);
		int t = block.getTextureForSide(blockView, x, y, z, 2);
		if (t < 0) {
			mirrorTexture = true; // TODO invert samples
			//t = -t;
		}
		
		renderEastFace(block, x, y, z, sample);
		mirrorTexture = false;
		
		lightB = block.getBrightness(blockView, x, y, z + 1);
		if (doorBlock.maxZ < 1.0) {
			lightB = lightT;
		}
		
		if (state.getEmittance() > 0) {
			lightB = 1.0f;
		}
		
		tessellator.color(f3 * lightB, f3 * lightB, f3 * lightB);
		sample = state.getTextureForIndex(blockView, x, y, z, 3);
		t = block.getTextureForSide(blockView, x, y, z, 3);
		if (t < 0) {
			mirrorTexture = true;
			//t = -t;
		}
		
		renderWestFace(block, x, y, z, sample);
		mirrorTexture = false;
		
		lightB = block.getBrightness(blockView, x - 1, y, z);
		if (doorBlock.minX > 0.0) {
			lightB = lightT;
		}
		
		if (state.getEmittance() > 0) {
			lightB = 1.0f;
		}
		
		tessellator.color(f4 * lightB, f4 * lightB, f4 * lightB);
		sample = state.getTextureForIndex(blockView, x, y, z, 4);
		t = block.getTextureForSide(blockView, x, y, z, 4);
		if (t < 0) {
			mirrorTexture = true;
			//t = -t;
		}
		
		renderNorthFace(block, x, y, z, sample);
		mirrorTexture = false;
		
		lightB = block.getBrightness(blockView, x + 1, y, z);
		if (doorBlock.maxX < 1.0) {
			lightB = lightT;
		}
		
		if (state.getEmittance() > 0) {
			lightB = 1.0f;
		}
		
		tessellator.color(f4 * lightB, f4 * lightB, f4 * lightB);
		sample = state.getTextureForIndex(blockView, x, y, z, 5);
		t = block.getTextureForSide(blockView, x, y, z, 5);
		
		if (t < 0) {
			mirrorTexture = true;
			//t = -t;
		}
		
		renderSouthFace(block, x, y, z, sample);
		mirrorTexture = false;
		
		return true;
	}
	
	private static boolean renderLadder(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		float light = block.getBrightness(blockView, x, y, z);
		tessellator.color(light, light, light);
		
		int meta = blockView.getBlockMeta(x, y, z);
		float u1, u2, v1, v2;
		if (breaking) {
			boolean dirZ = meta > 3;
			u1 = dirZ ? (float) block.minZ : (float) block.minX;
			u2 = dirZ ? (float) block.maxZ : (float) block.maxX;
			v1 = (float) block.minY;
			v2 = (float) block.maxY;
		}
		else {
			UVPair uv = state.getTextureForIndex(blockView, x, y, z, 0).getUV();
			u1 = uv.getU(0);
			u2 = uv.getU(1);
			v1 = uv.getV(0);
			v2 = uv.getV(1);
		}
		
		float f2 = 0.0f;
		float f3 = 0.05f;
		
		if (meta == 5) {
			tessellator.vertex(x + f3, y + 1 + f2, z + 1 + f2, u1, v1);
			tessellator.vertex(x + f3, y - f2, z + 1 + f2, u1, v2);
			tessellator.vertex(x + f3, y - f2, z - f2, u2, v2);
			tessellator.vertex(x + f3, y + 1 + f2, z - f2, u2, v1);
		}
		
		if (meta == 4) {
			tessellator.vertex(x + 1 - f3, y - f2, z + 1 + f2, u2, v2);
			tessellator.vertex(x + 1 - f3, y + 1 + f2, z + 1 + f2, u2, v1);
			tessellator.vertex(x + 1 - f3, y + 1 + f2, z - f2, u1, v1);
			tessellator.vertex(x + 1 - f3, y - f2, z - f2, u1, v2);
		}
		
		if (meta == 3) {
			tessellator.vertex(x + 1 + f2, y - f2, z + f3, u2, v2);
			tessellator.vertex(x + 1 + f2, y + 1 + f2, z + f3, u2, v1);
			tessellator.vertex(x - f2, y + 1 + f2, z + f3, u1, v1);
			tessellator.vertex(x - f2, y - f2, z + f3, u1, v2);
		}
		
		if (meta == 2) {
			tessellator.vertex(x + 1 + f2, y + 1 + f2, z + 1 - f3, u1, v1);
			tessellator.vertex(x + 1 + f2, y - f2, z + 1 - f3, u1, v2);
			tessellator.vertex(x - f2, y - f2, z + 1 - f3, u2, v2);
			tessellator.vertex(x - f2, y + 1 + f2, z + 1 - f3, u2, v1);
		}
		
		return true;
	}
	
	private static boolean renderRails(BlockState state, int x, int y, int z) {
		RailBlock arg = (RailBlock) state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		int meta = blockView.getBlockMeta(x, y, z);
		if (arg.wrapMeta()) {
			meta &= 7;
		}
		
		float light = arg.getBrightness(blockView, x, y, z);
		tessellator.color(light, light, light);
		
		UVPair uv = state.getTextureForIndex(blockView, x, y, z, meta).getUV();
		
		float u1 = uv.getU(0);
		float u2 = uv.getU(1);
		float v1 = uv.getV(0);
		float v2 = uv.getV(1);
		
		float x1 = x + 1;
		float x2 = x1;
		float x3 = x;
		float x4 = x;
		float z1 = z;
		float z2 = z + 1;
		float z3 = z2;
		float z4 = z;
		
		float y1 = (float) y + 0.0625f;
		float y2 = y1;
		float y3 = y1;
		float y4 = y1;
		
		if (meta == 1 || meta == 2 || meta == 3 || meta == 7) {
			x1 = x4 = (float) (x + 1);
			x2 = x3 = (float) x;
			z1 = z2 = (float) (z + 1);
			z3 = z4 = (float) z;
		}
		else if (meta == 8) {
			x1 = x2 = (float) x;
			x3 = x4 = (float) (x + 1);
			z1 = z4 = (float) (z + 1);
			z2 = z3 = (float) z;
		}
		else if (meta == 9) {
			x1 = x4 = (float) x;
			x2 = x3 = (float) (x + 1);
			z1 = z2 = (float) z;
			z3 = z4 = (float) (z + 1);
		}
		
		if (meta == 2 || meta == 4) {
			y1 += 1.0f;
			y4 += 1.0f;
		}
		else if (meta == 3 || meta == 5) {
			y2 += 1.0f;
			y3 += 1.0f;
		}
		
		tessellator.vertex(x1, y1, z1, u2, v1);
		tessellator.vertex(x2, y2, z2, u2, v2);
		tessellator.vertex(x3, y3, z3, u1, v2);
		tessellator.vertex(x4, y4, z4, u1, v1);
		tessellator.vertex(x4, y4, z4, u1, v1);
		tessellator.vertex(x3, y3, z3, u1, v2);
		tessellator.vertex(x2, y2, z2, u2, v2);
		tessellator.vertex(x1, y1, z1, u2, v1);
		
		return true;
	}
	
	private static boolean renderStairs(BlockState state, int i, int j, int k) {
		BaseBlock block = state.getBlock();
		boolean result = false;
		
		int meta = blockView.getBlockMeta(i, j, k);
		if (meta == 0) {
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 1.0f);
			renderFullCube(state, i, j, k);
			block.setBoundingBox(0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
			renderFullCube(state, i, j, k);
			result = true;
		}
		else if (meta == 1) {
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f);
			renderFullCube(state, i, j, k);
			block.setBoundingBox(0.5f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f);
			renderFullCube(state, i, j, k);
			result = true;
		}
		else if (meta == 2) {
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 0.5f);
			renderFullCube(state, i, j, k);
			block.setBoundingBox(0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f);
			renderFullCube(state, i, j, k);
			result = true;
		}
		else if (meta == 3) {
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
			renderFullCube(state, i, j, k);
			block.setBoundingBox(0.0f, 0.0f, 0.5f, 1.0f, 0.5f, 1.0f);
			renderFullCube(state, i, j, k);
			result = true;
		}
		
		block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		return result;
	}
	
	private static boolean renderFence(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		block.setBoundingBox(0.375f, 0.0f, 0.375f, 0.625f, 1.0f, 0.625f);
		renderFullCube(state, x, y, z);
		
		BlockStateProvider provider = BlockStateProvider.cast(blockView);
		
		boolean cx1 = provider.getBlockState(x - 1, y, z).is(block);
		boolean cx2 = provider.getBlockState(x + 1, y, z).is(block);
		boolean cz1 = provider.getBlockState(x, y, z - 1).is(block);
		boolean cz2 = provider.getBlockState(x, y, z + 1).is(block);
		
		boolean cx = cx1 || cx2;
		boolean cz = cz1 || cz2;
		
		if (!cx && !cz) cx = true;
		
		float dx1 = cx1 ? 0.0f : 0.4375f;
		float dx2 = cx2 ? 1.0f : 0.5625f;
		float dz1 = cz1 ? 0.0f : 0.4375f;
		float dz2 = cz2 ? 1.0f : 0.5625f;
		
		if (cx) {
			block.setBoundingBox(dx1, 0.75f, 0.4375f, dx2, 0.9375f, 0.5625f);
			renderFullCube(state, x, y, z);
		}
		
		if (cz) {
			block.setBoundingBox(0.4375f, 0.75f, dz1, 0.5625f, 0.9375f, dz2);
			renderFullCube(state, x, y, z);
		}
		
		if (cx) {
			block.setBoundingBox(dx1, 0.375f, 0.4375f, dx2, 0.5625f, 0.5625f);
			renderFullCube(state, x, y, z);
		}
		
		if (cz) {
			block.setBoundingBox(0.4375f, 0.375f, dz1, 0.5625f, 0.5625f, dz2);
			renderFullCube(state, x, y, z);
		}
		
		block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		return true;
	}
	
	private static boolean renderLever(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		
		boolean bl;
		int meta = blockView.getBlockMeta(x, y, z);
		int wrappedMeta = meta & 7;
		boolean isActive = (meta & 8) > 0;
		
		//bl = this.textureOverride >= 0;
		
		//if (!bl) {
			//this.textureOverride = BaseBlock.COBBLESTONE.texture;
		//}
		
		if (wrappedMeta == 5) {
			block.setBoundingBox(0.5f - 0.1875f, 0.0f, 0.5f - 0.25f, 0.5f + 0.1875f, 0.1875f, 0.5f + 0.25f);
		}
		else if (wrappedMeta == 6) {
			block.setBoundingBox(0.5f - 0.25f, 0.0f, 0.5f - 0.1875f, 0.5f + 0.25f, 0.1875f, 0.5f + 0.1875f);
		}
		else if (wrappedMeta == 4) {
			block.setBoundingBox(0.5f - 0.1875f, 0.5f - 0.25f, 1.0f - 0.1875f, 0.5f + 0.1875f, 0.5f + 0.25f, 1.0f);
		}
		else if (wrappedMeta == 3) {
			block.setBoundingBox(0.5f - 0.1875f, 0.5f - 0.25f, 0.0f, 0.5f + 0.1875f, 0.5f + 0.25f, 0.1875f);
		}
		else if (wrappedMeta == 2) {
			block.setBoundingBox(1.0f - 0.1875f, 0.5f - 0.25f, 0.5f - 0.1875f, 1.0f, 0.5f + 0.25f, 0.5f + 0.1875f);
		}
		else if (wrappedMeta == 1) {
			block.setBoundingBox(0.0f, 0.5f - 0.25f, 0.5f - 0.1875f, 0.1875f, 0.5f + 0.25f, 0.5f + 0.1875f);
		}
		
		renderFullCube(state, x, y, z);
		
		//if (!bl) {
			//this.textureOverride = -1;
		//}
		
		float light = block.getBrightness(blockView, x, y, z);
		if (BaseBlock.EMITTANCE[block.id] > 0) {
			light = 1.0f;
		}
		
		Tessellator tessellator = Tessellator.INSTANCE;
		UVPair uv = state.getTextureForIndex(blockView, x, y, z, 6).getUV();
		tessellator.color(light, light, light);
		
		float u1, u2, v1, v2;
		if (breaking) {
			u1 = 0;
			u2 = 1;
			v1 = 0;
			v2 = 1;
		}
		else {
			u1 = uv.getU(0);
			u2 = uv.getU(1);
			v1 = uv.getV(0);
			v2 = uv.getV(1);
		}
		
		Vec3f[] points = new Vec3f[8];
		
		points[0] = Vec3f.getFromCacheAndSet(-0.0625f, 0.0, -0.0625f);
		points[1] = Vec3f.getFromCacheAndSet(0.0625f, 0.0, -0.0625f);
		points[2] = Vec3f.getFromCacheAndSet(0.0625f, 0.0, 0.0625f);
		points[3] = Vec3f.getFromCacheAndSet(-0.0625f, 0.0, 0.0625f);
		points[4] = Vec3f.getFromCacheAndSet(-0.0625f, 0.625f, -0.0625f);
		points[5] = Vec3f.getFromCacheAndSet(0.0625f, 0.625f, -0.0625f);
		points[6] = Vec3f.getFromCacheAndSet(0.0625f, 0.625f, 0.0625f);
		points[7] = Vec3f.getFromCacheAndSet(-0.0625f, 0.625f, 0.0625f);
		
		for (int i2 = 0; i2 < 8; ++i2) {
			if (isActive) {
				points[i2].z -= 0.0625;
				points[i2].rotateX(0.69813174f);
			}
			else {
				points[i2].z += 0.0625;
				points[i2].rotateX(-0.69813174f);
			}
			
			if (wrappedMeta == 6) {
				points[i2].rotateY(1.5707964f);
			}
			
			if (wrappedMeta < 5) {
				points[i2].y -= 0.375;
				points[i2].rotateX(1.5707964f);
				if (wrappedMeta == 4) {
					points[i2].rotateY(0.0f);
				}
				if (wrappedMeta == 3) {
					points[i2].rotateY((float)Math.PI);
				}
				if (wrappedMeta == 2) {
					points[i2].rotateY(1.5707964f);
				}
				if (wrappedMeta == 1) {
					points[i2].rotateY(-1.5707964f);
				}
				points[i2].x += x + 0.5;
				points[i2].y += y + 0.5;
				points[i2].z += z + 0.5;
				continue;
			}
			
			points[i2].x += x + 0.5;
			points[i2].y += y + 0.125;
			points[i2].z += z + 0.5;
		}
		
		Vec3f p1 = null;
		Vec3f p2 = null;
		Vec3f p3 = null;
		Vec3f p4 = null;
		
		for (int side = 0; side < 6; ++side) {
			if (side == 0) {
				u1 = uv.getU(7F / 16F);
				u2 = uv.getU(9F / 16F);
				v1 = uv.getV(6F / 16F);
				v2 = uv.getV(8F / 16F);
			}
			else if (side == 2) {
				u1 = uv.getU(7F / 16F);
				u2 = uv.getU(9F / 16F);
				v1 = uv.getV(6F / 16F);
				v2 = uv.getV(1);
			}
			
			if (side == 0) {
				p1 = points[0];
				p2 = points[1];
				p3 = points[2];
				p4 = points[3];
			}
			else if (side == 1) {
				p1 = points[7];
				p2 = points[6];
				p3 = points[5];
				p4 = points[4];
			}
			else if (side == 2) {
				p1 = points[1];
				p2 = points[0];
				p3 = points[4];
				p4 = points[5];
			}
			else if (side == 3) {
				p1 = points[2];
				p2 = points[1];
				p3 = points[5];
				p4 = points[6];
			}
			else if (side == 4) {
				p1 = points[3];
				p2 = points[2];
				p3 = points[6];
				p4 = points[7];
			}
			else if (side == 5) {
				p1 = points[0];
				p2 = points[3];
				p3 = points[7];
				p4 = points[4];
			}
			
			tessellator.vertex(p1.x, p1.y, p1.z, u1, v2);
			tessellator.vertex(p2.x, p2.y, p2.z, u2, v2);
			tessellator.vertex(p3.x, p3.y, p3.z, u2, v1);
			tessellator.vertex(p4.x, p4.y, p4.z, u1, v1);
		}
		
		return true;
	}
	
	private static boolean renderCactus(BlockState state, int i, int j, int k) {
		BaseBlock block = state.getBlock();
		int color = block.getColorMultiplier(blockView, i, j, k);
		float r = (float) (color >> 16 & 0xFF) / 255.0f;
		float g = (float) (color >> 8 & 0xFF) / 255.0f;
		float b = (float) (color & 0xFF) / 255.0f;
		
		if (GameRenderer.anaglyph3d) {
			float nr = (r * 30.0f + g * 59.0f + b * 11.0f) / 100.0f;
			float ng = (r * 30.0f + g * 70.0f) / 100.0f;
			float nb = (r * 30.0f + b * 70.0f) / 100.0f;
			r = nr;
			g = ng;
			b = nb;
		}
		
		return renderCactusBlock(state, block, i, j, k, r, g, b);
	}
	
	private static boolean renderCactusBlock(BlockState state, BaseBlock block, int x, int y, int z, float r, float g, float b) {
		float r1 = item ? r : 0.5f * r;
		float r2 = item ? r : 0.8f * r;
		float r3 = item ? r : 0.6f * r;
		float g1 = item ? g : 0.5f * g;
		float g2 = item ? g : 0.8f * g;
		float g3 = item ? g : 0.6f * g;
		float b1 = item ? b : 0.5f * b;
		float b2 = item ? b : 0.8f * b;
		float b3 = item ? b : 0.6f * b;
		
		float blockLight = getBrightness(block, x, y, z);
		float light;
		
		Tessellator tessellator = Tessellator.INSTANCE;
		boolean result = false;
		
		if (renderAllSides || block.isSideRendered(blockView, x, y - 1, z, 0)) {
			light = getBrightness(block, x, y - 1, z);
			if (item) tessellator.setNormal(0.0f, -1.0f, 0.0f);
			tessellator.color(r1 * light, g1 * light, b1 * light);
			renderBottomFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 0));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x, y + 1, z, 1)) {
			light = getBrightness(block, x, y + 1, z);
			if (block.maxY != 1.0 && !block.material.isLiquid()) {
				light = blockLight;
			}
			if (item) tessellator.setNormal(0.0f, 1.0f, 0.0f);
			tessellator.color(r * light, g * light, b * light);
			renderTopFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 1));
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x, y, z - 1, 2)) {
			light = getBrightness(block, x, y, z - 1);
			if (block.minZ > 0.0) {
				light = blockLight;
			}
			if (item) tessellator.setNormal(0.0f, 0.0f, -1.0f);
			tessellator.color(r2 * light, g2 * light, b2 * light);
			tessellator.addOffset(0.0f, 0.0f, 0.0625f);
			renderEastFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 2));
			tessellator.addOffset(0.0f, 0.0f, -0.0625f);
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x, y, z + 1, 3)) {
			light = getBrightness(block, x, y, z + 1);
			if (block.maxZ < 1.0) {
				light = blockLight;
			}
			if (item) tessellator.setNormal(0.0f, 0.0f, 1.0f);
			tessellator.color(r2 * light, g2 * light, b2 * light);
			tessellator.addOffset(0.0f, 0.0f, -0.0625f);
			renderWestFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 3));
			tessellator.addOffset(0.0f, 0.0f, 0.0625f);
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x - 1, y, z, 4)) {
			light = getBrightness(block, x - 1, y, z);
			if (block.minX > 0.0) {
				light = blockLight;
			}
			if (item) tessellator.setNormal(-1.0f, 0.0f, 0.0f);
			tessellator.color(r3 * light, g3 * light, b3 * light);
			tessellator.addOffset(0.0625f, 0.0f, 0.0f);
			renderNorthFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 4));
			tessellator.addOffset(-0.0625f, 0.0f, 0.0f);
			result = true;
		}
		
		if (renderAllSides || block.isSideRendered(blockView, x + 1, y, z, 5)) {
			light = getBrightness(block, x + 1, y, z);
			if (block.maxX < 1.0) {
				light = blockLight;
			}
			if (item) tessellator.setNormal(1.0f, 0.0f, 0.0f);
			tessellator.color(r3 * light, g3 * light, b3 * light);
			tessellator.addOffset(-0.0625f, 0.0f, 0.0f);
			renderSouthFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 5));
			tessellator.addOffset(0.0625f, 0.0f, 0.0f);
			result = true;
		}
		
		return result;
	}
	
	private static boolean renderBed(BlockState state, int x, int y, int z) {
		BaseBlock arg = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		int meta = state.getMeta();
		int facing = BedBlock.orientationOnly(meta);
		boolean isFoot = BedBlock.isFoot(meta);
		
		float f = 0.5f;
		float f2 = 1.0f;
		float f3 = 0.8f;
		float f4 = 0.6f;
		
		float r2 = f2;
		float g2 = f2;
		float b2 = f2;
		float r = f;
		float f9 = f3;
		float f10 = f4;
		float g = f;
		float f12 = f3;
		float f13 = f4;
		float b = f;
		float f15 = f3;
		float f16 = f4;
		
		float light = arg.getBrightness(blockView, x, y, z);
		tessellator.color(r * light, g * light, b * light);
		
		UVPair uv = state.getTextureForIndex(blockView, x, y, z, 0).getUV();
		float u11 = uv.getU(0);
		float u12 = uv.getU(1);
		float v11 = uv.getV(0);
		float v12 = uv.getV(1);
		
		double x1 = x + arg.minX;
		double x2 = x + arg.maxX;
		double y2 = y + arg.minY + 0.1875;
		double z1 = z + arg.minZ;
		double z2 = z + arg.maxZ;
		
		tessellator.vertex(x1, y2, z2, u11, v12);
		tessellator.vertex(x1, y2, z1, u11, v11);
		tessellator.vertex(x2, y2, z1, u12, v11);
		tessellator.vertex(x2, y2, z2, u12, v12);
		
		float light2 = arg.getBrightness(blockView, x, y + 1, z);
		tessellator.color(r2 * light2, g2 * light2, b2 * light2);
		
		uv = state.getTextureForIndex(blockView, x, y, z, 1).getUV();
		float u21 = uv.getU(0);
		float u22 = uv.getU(1);
		float v21 = uv.getV(0);
		float v22 = uv.getV(1);
		
		float d14 = u21;
		float d15 = u22;
		float d16 = v21;
		float d17 = v21;
		float d18 = u21;
		float d19 = u22;
		float d20 = v22;
		float d21 = v22;
		
		if (facing == 0) {
			d15 = u21;
			d16 = v22;
			d18 = u22;
			d21 = v21;
		}
		else if (facing == 2) {
			d14 = u22;
			d17 = v22;
			d19 = u21;
			d20 = v21;
		}
		else if (facing == 3) {
			d14 = u22;
			d17 = v22;
			d19 = u21;
			d20 = v21;
			d15 = u21;
			d16 = v22;
			d18 = u22;
			d21 = v21;
		}
		
		double d22 = (double) x + arg.minX;
		double d23 = (double) x + arg.maxX;
		double d24 = (double) y + arg.maxY;
		double d25 = (double) z + arg.minZ;
		double d26 = (double) z + arg.maxZ;
		
		tessellator.vertex(d23, d24, d26, d18, d20);
		tessellator.vertex(d23, d24, d25, d14, d16);
		tessellator.vertex(d22, d24, d25, d15, d17);
		tessellator.vertex(d22, d24, d26, d19, d21);
		
		int magic = MagicBedNumbers.field_792[facing];
		if (isFoot) {
			magic = MagicBedNumbers.field_792[MagicBedNumbers.field_793[facing]];
		}
		
		int face = 4;
		switch (facing) {
			case 0 -> face = 5;
			case 3 -> face = 2;
			case 1 -> face = 3;
		}
		
		if (magic != 2 && (renderAllSides || arg.isSideRendered(blockView, x, y, z - 1, 2))) {
			float f19 = arg.getBrightness(blockView, x, y, z - 1);
			if (arg.minZ > 0.0) {
				f19 = light;
			}
			tessellator.color(f9 * f19, f12 * f19, f15 * f19);
			mirrorTexture = face == 2;
			renderEastFace(arg, x, y, z, state.getTextureForIndex(blockView, x, y, z, 2));
		}
		
		if (magic != 3 && (renderAllSides || arg.isSideRendered(blockView, x, y, z + 1, 3))) {
			float f20 = arg.getBrightness(blockView, x, y, z + 1);
			if (arg.maxZ < 1.0) {
				f20 = light;
			}
			tessellator.color(f9 * f20, f12 * f20, f15 * f20);
			mirrorTexture = face == 3;
			renderWestFace(arg, x, y, z, state.getTextureForIndex(blockView, x, y, z, 3));
		}
		
		if (magic != 4 && (renderAllSides || arg.isSideRendered(blockView, x - 1, y, z, 4))) {
			float f21 = arg.getBrightness(blockView, x - 1, y, z);
			if (arg.minX > 0.0) {
				f21 = light;
			}
			tessellator.color(f10 * f21, f13 * f21, f16 * f21);
			mirrorTexture = face == 4;
			renderNorthFace(arg, x, y, z, state.getTextureForIndex(blockView, x, y, z, 4));
		}
		
		if (magic != 5 && (renderAllSides || arg.isSideRendered(blockView, x + 1, y, z, 5))) {
			float f22 = arg.getBrightness(blockView, x + 1, y, z);
			if (arg.maxX < 1.0) {
				f22 = light;
			}
			tessellator.color(f10 * f22, f13 * f22, f16 * f22);
			mirrorTexture = face == 5;
			renderSouthFace(arg, x, y, z, state.getTextureForIndex(blockView, x, y, z, 5));
		}
		
		mirrorTexture = false;
		return true;
	}
}
