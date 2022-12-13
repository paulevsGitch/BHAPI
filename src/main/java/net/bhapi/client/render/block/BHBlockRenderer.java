package net.bhapi.client.render.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.BHAPIClient;
import net.bhapi.client.render.model.CustomModel;
import net.bhapi.client.render.model.ModelRenderingContext;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.CircleCache;
import net.bhapi.storage.EnumArray;
import net.bhapi.storage.PermutationTable;
import net.bhapi.storage.Vec2F;
import net.bhapi.storage.Vec3F;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.MathUtil;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.RedstoneDustBlock;
import net.minecraft.block.RedstoneRepeaterBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.technical.MagicBedNumbers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.level.BlockView;
import net.minecraft.util.maths.MathHelper;
import net.minecraft.util.maths.Vec3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BHBlockRenderer {
	private static final PermutationTable[] TABLES = {
		new PermutationTable(0),
		new PermutationTable(1),
		new PermutationTable(2)
	};
	private final EnumArray<BlockDirection, Integer> rotation = new EnumArray<>(BlockDirection.class);
	private final List<BlockRenderingFunction> renderingFunctions = new ArrayList<>();
	private final CircleCache<Vec2F> uvCache = new CircleCache<Vec2F>(8).fill(Vec2F::new);
	private final ModelRenderingContext context = new ModelRenderingContext();
	private final boolean[] renderSides = new boolean[4];
	private final Vec3F itemColor = new Vec3F();
	private final Vec3I pos = new Vec3I();
	
	private boolean forceRotation = false;
	private boolean renderAllSides = false;
	private boolean shadeTopFace;
	private float cr00;
	private float cr01;
	private float cr11;
	private float cr10;
	private float cg00;
	private float cg01;
	private float cg11;
	private float cg10;
	private float cb00;
	private float cb01;
	private float cb11;
	private float cb10;
	private boolean allowsGrassUnderTopEast;
	private boolean allowsGrassUnderTopSouth;
	private boolean allowsGrassUnderTopNorth;
	private boolean allowsGrassUnderTopWest;
	private boolean allowsGrassUnderNorthEast;
	private boolean allowsGrassUnderSouthWest;
	private boolean allowsGrassUnderNorthWest;
	private boolean allowsGrassUnderSouthEast;
	private boolean allowsGrassUnderBottomEast;
	private boolean allowsGrassUnderBottomSouth;
	private boolean allowsGrassUnderBottomNorth;
	private boolean allowsGrassUnderBottomWest;
	private boolean breaking = false;
	private boolean item = false;
	private BlockView blockView;
	private byte overlayIndex;
	
	public BHBlockRenderer() {
		renderingFunctions.add(this::renderFullCube);
		renderingFunctions.add(this::renderCross);
		renderingFunctions.add(this::renderTorch);
		renderingFunctions.add(this::renderFire);
		renderingFunctions.add(this::renderFluid);
		renderingFunctions.add(this::renderRedstoneDust);
		renderingFunctions.add(this::renderCrops);
		renderingFunctions.add(this::renderDoor);
		renderingFunctions.add(this::renderLadder);
		renderingFunctions.add(this::renderRails);
		renderingFunctions.add(this::renderStairs);
		renderingFunctions.add(this::renderFence);
		renderingFunctions.add(this::renderLever);
		renderingFunctions.add(this::renderCactus);
		renderingFunctions.add(this::renderBed);
		renderingFunctions.add(this::renderRedstoneRepeater);
		renderingFunctions.add(this::renderPiston);
		renderingFunctions.add(this::renderPistonHead);
		context.setTessellator(Tessellator.INSTANCE);
	}
	
	public void setView(BlockView view) {
		this.blockView = view;
	}
	
	public void renderBlockBreak(BlockState state, int x, int y, int z) {
		breaking = true;
		render(state, x, y, z);
		breaking = false;
	}
	
	public void renderAllSides(BlockState state, int x, int y, int z) {
		renderAllSides = true;
		render(state, x, y, z);
		renderAllSides = false;
	}
	
	public void renderPistonHeadAllSides(BlockState state, int x, int y, int z, boolean extended) {
		this.renderAllSides = true;
		this.renderPistonHead(state, x, y, z, extended);
		this.renderAllSides = false;
	}
	
	public void renderPistonExtended(BlockState state, int x, int y, int z) {
		this.renderAllSides = true;
		this.renderPiston(state, x, y, z, true);
		this.renderAllSides = false;
	}
	
	public void renderItem(BlockState state, boolean colorizeItem, float light) {
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
	
	public boolean render(BlockState state, int x, int y, int z) {
		byte type = state.getRenderType(blockView, x, y, z);
		if (type == BlockRenderTypes.EMPTY) return false;
		state.getBlock().updateBoundingBox(blockView, x, y, z);
		int overlays = state.getOverlayCount(blockView, x, y, z);
		if (breaking) overlays = 1;
		boolean result = false;
		for (overlayIndex = 0; overlayIndex < overlays; overlayIndex++) {
			if (type == BlockRenderTypes.CUSTOM) {
				CustomModel model = state.getModel(blockView, x, y, z);
				if (model == null) break;
				if (renderAllSides) context.setRenderAllFaces(true);
				else for (BlockDirection dir : BlockDirection.VALUES) {
					pos.set(x, y, z).move(dir);
					boolean renderSide = state.isSideRendered(blockView, pos.x, pos.y, pos.z, dir);
					context.setRenderFace(dir, renderSide);
				}
				context.setOverlayIndex(overlayIndex);
				context.setBlockView(blockView);
				context.setBreaking(breaking);
				context.setPosition(x, y, z);
				context.setState(state);
				context.setInGUI(item && itemColor.x == 1.0F);
				context.setLight(item ? itemColor.x : 1.0F);
				model.render(context, uvCache);
				result = true;
			}
			if (type < renderingFunctions.size()) {
				result = renderingFunctions.get(type).render(state, x, y, z) | result;
			}
		}
		return result;
	}
	
	private float getBrightness(BaseBlock block, int x, int y, int z) {
		return item ? 1.0F : block.getBrightness(blockView, x, y, z);
	}
	
	private boolean isFancy() {
		return BHAPIClient.getMinecraft().options.fancyGraphics;
	}
	
	private boolean renderFullCube(BlockState state, int x, int y, int z) {
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
	
	private boolean renderCubeSmooth(BlockState state, int x, int y, int z, float f, float g, float h) {
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
		
		float brightnessNegX = getBrightness(block, x - 1, y, z);
		float brightnessNegY = getBrightness(block, x, y - 1, z);
		float brightnessNegZ = getBrightness(block, x, y, z - 1);
		float brightnessPosX = getBrightness(block, x + 1, y, z);
		float brightnessPosY = getBrightness(block, x, y + 1, z);
		float brightnessPosZ = getBrightness(block, x, y, z + 1);
		
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
		
		float brightnessBottomSouthWest;
		float brightnessBottomSouth;
		float brightnessBottomSouthEast;
		float brightnessBottomWest;
		float brightnessBottomEast;
		float brightnessBottomNorthWest;
		float brightnessBottomNorth;
		float brightnessBottomNorthEast;
		if (renderAllSides || state.isSideRendered(blockView, x, y - 1, z, BlockDirection.NEG_Y)) {
			brightnessBottomNorth = getBrightness(block, x - 1, --y, z);
			brightnessBottomEast = getBrightness(block, x, y, z - 1);
			brightnessBottomWest = getBrightness(block, x, y, z + 1);
			brightnessBottomSouth = getBrightness(block, x + 1, y, z);
			brightnessBottomNorthEast = allowsGrassUnderBottomEast || allowsGrassUnderBottomNorth ? getBrightness(block, x - 1, y, z - 1) : brightnessBottomNorth;
			brightnessBottomNorthWest = allowsGrassUnderBottomWest || allowsGrassUnderBottomNorth ? getBrightness(block, x - 1, y, z + 1) : brightnessBottomNorth;
			brightnessBottomSouthEast = allowsGrassUnderBottomEast || allowsGrassUnderBottomSouth ? getBrightness(block, x + 1, y, z - 1) : brightnessBottomSouth;
			brightnessBottomSouthWest = allowsGrassUnderBottomWest || allowsGrassUnderBottomSouth ? getBrightness(block, x + 1, y, z + 1) : brightnessBottomSouth;
			++y;
			f2 = (brightnessBottomNorthWest + brightnessBottomNorth + brightnessBottomWest + brightnessNegY) / 4.0F;
			f5 = (brightnessBottomWest + brightnessNegY + brightnessBottomSouthWest + brightnessBottomSouth) / 4.0F;
			f4 = (brightnessNegY + brightnessBottomEast + brightnessBottomSouth + brightnessBottomSouthEast) / 4.0F;
			f3 = (brightnessBottomNorth + brightnessBottomNorthEast + brightnessNegY + brightnessBottomEast) / 4.0F;
			cr11 = cr10 = (bl2 ? f : 1.0f) * 0.5F;
			cr01 = cr10;
			cr00 = cr10;
			cg11 = cg10 = (bl2 ? g : 1.0f) * 0.5F;
			cg01 = cg10;
			cg00 = cg10;
			cb11 = cb10 = (bl2 ? h : 1.0f) * 0.5F;
			cb01 = cb10;
			cb00 = cb10;
			cr00 *= f2;
			cg00 *= f2;
			cb00 *= f2;
			cr01 *= f3;
			cg01 *= f3;
			cb01 *= f3;
			cr11 *= f4;
			cg11 *= f4;
			cb11 *= f4;
			cr10 *= f5;
			cg10 *= f5;
			cb10 *= f5;
			TextureSample sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.NEG_Y.getFacing(), overlayIndex);
			if (sample != null) {
				renderNegYFace(block, x, y, z, sample);
				result = true;
			}
		}
		
		float brightnessTopSouthWest;
		float brightnessTopWest;
		float brightnessTopSouth;
		float brightnessTopSouthEast;
		float brightnessTopEast;
		float brightnessTopNorthWest;
		float brightnessTopNorth;
		float brightnessTopNorthEast;
		if (renderAllSides || state.isSideRendered(blockView, x, y + 1, z, BlockDirection.POS_Y)) {
			brightnessTopNorth = getBrightness(block, x - 1, ++y, z);
			brightnessTopSouth = getBrightness(block, x + 1, y, z);
			brightnessTopEast = getBrightness(block, x, y, z - 1);
			brightnessTopWest = getBrightness(block, x, y, z + 1);
			brightnessTopNorthEast = allowsGrassUnderTopEast || allowsGrassUnderTopNorth ? getBrightness(block, x - 1, y, z - 1) : brightnessTopNorth;
			brightnessTopSouthEast = allowsGrassUnderTopEast || allowsGrassUnderTopSouth ? getBrightness(block, x + 1, y, z - 1) : brightnessTopSouth;
			brightnessTopNorthWest = allowsGrassUnderTopWest || allowsGrassUnderTopNorth ? getBrightness(block, x - 1, y, z + 1) : brightnessTopNorth;
			brightnessTopSouthWest = allowsGrassUnderTopWest || allowsGrassUnderTopSouth ? getBrightness(block, x + 1, y, z + 1) : brightnessTopSouth;
			--y;
			f5 = (brightnessTopNorthWest + brightnessTopNorth + brightnessTopWest + brightnessPosY) / 4.0F;
			f2 = (brightnessTopWest + brightnessPosY + brightnessTopSouthWest + brightnessTopSouth) / 4.0F;
			f3 = (brightnessPosY + brightnessTopEast + brightnessTopSouth + brightnessTopSouthEast) / 4.0F;
			f4 = (brightnessTopNorth + brightnessTopNorthEast + brightnessPosY + brightnessTopEast) / 4.0F;
			cr10 = bl3 ? f : 1.0F;
			cr11 = cr10;
			cr01 = cr10;
			cr00 = cr10;
			cg10 = bl3 ? g : 1.0F;
			cg11 = cg10;
			cg01 = cg10;
			cg00 = cg10;
			cb10 = bl3 ? h : 1.0F;
			cb11 = cb10;
			cb01 = cb10;
			cb00 = cb10;
			cr00 *= f2;
			cg00 *= f2;
			cb00 *= f2;
			cr01 *= f3;
			cg01 *= f3;
			cb01 *= f3;
			cr11 *= f4;
			cg11 *= f4;
			cb11 *= f4;
			cr10 *= f5;
			cg10 *= f5;
			cb10 *= f5;
			TextureSample sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.POS_Y.getFacing(), overlayIndex);
			if (sample != null) {
				renderPosYFace(block, x, y, z, sample);
				result = true;
			}
		}
		
		float brightnessSouthEast;
		float brightnessNorthEast;
		if (renderAllSides || state.isSideRendered(blockView, x, y, z - 1, BlockDirection.NEG_Z)) {
			brightnessNorthEast = getBrightness(block, x - 1, y, --z);
			brightnessBottomEast = getBrightness(block, x, y - 1, z);
			brightnessTopEast = getBrightness(block, x, y + 1, z);
			brightnessSouthEast = getBrightness(block, x + 1, y, z);
			brightnessBottomNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderBottomEast ? block.getBrightness(blockView, x - 1, y - 1, z) : brightnessNorthEast;
			brightnessTopNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderTopEast ? block.getBrightness(blockView, x - 1, y + 1, z) : brightnessNorthEast;
			brightnessBottomSouthEast = allowsGrassUnderSouthEast || allowsGrassUnderBottomEast ? block.getBrightness(blockView, x + 1, y - 1, z) : brightnessSouthEast;
			brightnessTopSouthEast = allowsGrassUnderSouthEast || allowsGrassUnderTopEast ? block.getBrightness(blockView, x + 1, y + 1, z) : brightnessSouthEast;++z;
			f2 = (brightnessNorthEast + brightnessTopNorthEast + brightnessNegZ + brightnessTopEast) / 4.0F;
			f3 = (brightnessNegZ + brightnessTopEast + brightnessSouthEast + brightnessTopSouthEast) / 4.0F;
			f4 = (brightnessBottomEast + brightnessNegZ + brightnessBottomSouthEast + brightnessSouthEast) / 4.0F;
			f5 = (brightnessBottomNorthEast + brightnessNorthEast + brightnessBottomEast + brightnessNegZ) / 4.0F;
			cr11 = cr10 = (bl4 ? f : 1.0f) * 0.8F;
			cr01 = cr10;
			cr00 = cr10;
			cg11 = cg10 = (bl4 ? g : 1.0f) * 0.8F;
			cg01 = cg10;
			cg00 = cg10;
			cb11 = cb10 = (bl4 ? h : 1.0f) * 0.8F;
			cb01 = cb10;
			cb00 = cb10;
			cr00 *= f2;
			cg00 *= f2;
			cb00 *= f2;
			cr01 *= f3;
			cg01 *= f3;
			cb01 *= f3;
			cr11 *= f4;
			cg11 *= f4;
			cb11 *= f4;
			cr10 *= f5;
			cg10 *= f5;
			cb10 *= f5;
			TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 2, overlayIndex);
			if (sample != null) {
				renderNegZFace(block, x, y, z, sample);
				if (isFancy() && block == BaseBlock.GRASS && !breaking && block.getTextureForSide(blockView, x, y, z, BlockDirection.NEG_Z.getFacing()) != 68) {
					cr00 *= f;
					cr01 *= f;
					cr11 *= f;
					cr10 *= f;
					cg00 *= g;
					cg01 *= g;
					cg11 *= g;
					cg10 *= g;
					cb00 *= h;
					cb01 *= h;
					cb11 *= h;
					cb10 *= h;
					renderNegZFace(block, x, y, z, Textures.getVanillaBlockSample(38));
				}
				result = true;
			}
		}
		
		float brightnessSouthWest;
		float brightnessNorthWest;
		if (renderAllSides || state.isSideRendered(blockView, x, y, z + 1, BlockDirection.POS_Z)) {
			brightnessNorthWest = getBrightness(block, x - 1, y, ++z);
			brightnessSouthWest = getBrightness(block, x + 1, y, z);
			brightnessBottomWest = getBrightness(block, x, y - 1, z);
			brightnessTopWest = getBrightness(block, x, y + 1, z);
			brightnessBottomNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderBottomWest ? block.getBrightness(blockView, x - 1, y - 1, z) : brightnessNorthWest;
			brightnessTopNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderTopWest ? block.getBrightness(blockView, x - 1, y + 1, z) : brightnessNorthWest;
			brightnessBottomSouthWest = allowsGrassUnderSouthWest || allowsGrassUnderBottomWest ? block.getBrightness(blockView, x + 1, y - 1, z) : brightnessSouthWest;
			brightnessTopSouthWest = allowsGrassUnderSouthWest || allowsGrassUnderTopWest ? block.getBrightness(blockView, x + 1, y + 1, z) : brightnessSouthWest;--z;
			f2 = (brightnessNorthWest + brightnessTopNorthWest + brightnessPosZ + brightnessTopWest) / 4.0F;
			f5 = (brightnessPosZ + brightnessTopWest + brightnessSouthWest + brightnessTopSouthWest) / 4.0F;
			f4 = (brightnessBottomWest + brightnessPosZ + brightnessBottomSouthWest + brightnessSouthWest) / 4.0F;
			f3 = (brightnessBottomNorthWest + brightnessNorthWest + brightnessBottomWest + brightnessPosZ) / 4.0F;
			cr11 = cr10 = (bl5 ? f : 1.0f) * 0.8F;
			cr01 = cr10;
			cr00 = cr10;
			cg11 = cg10 = (bl5 ? g : 1.0f) * 0.8F;
			cg01 = cg10;
			cg00 = cg10;
			cb11 = cb10 = (bl5 ? h : 1.0f) * 0.8F;
			cb01 = cb10;
			cb00 = cb10;
			cr00 *= f2;
			cg00 *= f2;
			cb00 *= f2;
			cr01 *= f3;
			cg01 *= f3;
			cb01 *= f3;
			cr11 *= f4;
			cg11 *= f4;
			cb11 *= f4;
			cr10 *= f5;
			cg10 *= f5;
			cb10 *= f5;
			TextureSample sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.POS_Z.getFacing(), overlayIndex);
			if (sample != null) {
				renderPosZFace(block, x, y, z, sample);
				if (isFancy() && block == BaseBlock.GRASS && !breaking && block.getTextureForSide(blockView, x, y, z, 2) != 68) {
					cr00 *= f;
					cr01 *= f;
					cr11 *= f;
					cr10 *= f;
					cg00 *= g;
					cg01 *= g;
					cg11 *= g;
					cg10 *= g;
					cb00 *= h;
					cb01 *= h;
					cb11 *= h;
					cb10 *= h;
					renderPosZFace(block, x, y, z, Textures.getVanillaBlockSample(38));
				}
				result = true;
			}
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x - 1, y, z, BlockDirection.NEG_X)) {
			brightnessBottomNorth = getBrightness(block, --x, y - 1, z);
			brightnessNorthEast = getBrightness(block, x, y, z - 1);
			brightnessNorthWest = getBrightness(block, x, y, z + 1);
			brightnessTopNorth = getBrightness(block, x, y + 1, z);
			brightnessBottomNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderBottomNorth ? block.getBrightness(blockView, x, y - 1, z - 1) : brightnessNorthEast;
			brightnessBottomNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderBottomNorth ? block.getBrightness(blockView, x, y - 1, z + 1) : brightnessNorthWest;
			brightnessTopNorthEast = allowsGrassUnderNorthEast || allowsGrassUnderTopNorth ? block.getBrightness(blockView, x, y + 1, z - 1) : brightnessNorthEast;
			brightnessTopNorthWest = allowsGrassUnderNorthWest || allowsGrassUnderTopNorth ? block.getBrightness(blockView, x, y + 1, z + 1) : brightnessNorthWest;
			++x;
			f5 = (brightnessBottomNorth + brightnessBottomNorthWest + brightnessNegX + brightnessNorthWest) / 4.0F;
			f2 = (brightnessNegX + brightnessNorthWest + brightnessTopNorth + brightnessTopNorthWest) / 4.0F;
			f3 = (brightnessNorthEast + brightnessNegX + brightnessTopNorthEast + brightnessTopNorth) / 4.0F;
			f4 = (brightnessBottomNorthEast + brightnessBottomNorth + brightnessNorthEast + brightnessNegX) / 4.0F;
			cr11 = cr10 = (bl6 ? f : 1.0f) * 0.6F;
			cr01 = cr10;
			cr00 = cr10;
			cg11 = cg10 = (bl6 ? g : 1.0f) * 0.6F;
			cg01 = cg10;
			cg00 = cg10;
			cb11 = cb10 = (bl6 ? h : 1.0f) * 0.6F;
			cb01 = cb10;
			cb00 = cb10;
			cr00 *= f2;
			cg00 *= f2;
			cb00 *= f2;
			cr01 *= f3;
			cg01 *= f3;
			cb01 *= f3;
			cr11 *= f4;
			cg11 *= f4;
			cb11 *= f4;
			cr10 *= f5;
			cg10 *= f5;
			cb10 *= f5;
			TextureSample sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.NEG_X.getFacing(), overlayIndex);
			if (sample != null) {
				renderNegXFace(block, x, y, z, sample);
				if (isFancy() && block == BaseBlock.GRASS && !breaking && block.getTextureForSide(blockView, x, y, z, 2) != 68) {
					cr00 *= f;
					cr01 *= f;
					cr11 *= f;
					cr10 *= f;
					cg00 *= g;
					cg01 *= g;
					cg11 *= g;
					cg10 *= g;
					cb00 *= h;
					cb01 *= h;
					cb11 *= h;
					cb10 *= h;
					renderNegXFace(block, x, y, z, Textures.getVanillaBlockSample(38));
				}
				result = true;
			}
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x + 1, y, z, BlockDirection.POS_X)) {
			brightnessBottomSouth = getBrightness(block, ++x, y - 1, z);
			brightnessSouthEast = getBrightness(block, x, y, z - 1);
			brightnessSouthWest = getBrightness(block, x, y, z + 1);
			brightnessTopSouth = getBrightness(block, x, y + 1, z);
			brightnessBottomSouthEast = allowsGrassUnderBottomSouth || allowsGrassUnderSouthEast ? block.getBrightness(blockView, x, y - 1, z - 1) : brightnessSouthEast;
			brightnessBottomSouthWest = allowsGrassUnderBottomSouth || allowsGrassUnderSouthWest ? block.getBrightness(blockView, x, y - 1, z + 1) : brightnessSouthWest;
			brightnessTopSouthEast = allowsGrassUnderTopSouth || allowsGrassUnderSouthEast ? block.getBrightness(blockView, x, y + 1, z - 1) : brightnessSouthEast;
			brightnessTopSouthWest = allowsGrassUnderTopSouth || allowsGrassUnderSouthWest ? block.getBrightness(blockView, x, y + 1, z + 1) : brightnessSouthWest;
			--x;
			f2 = (brightnessBottomSouth + brightnessBottomSouthWest + brightnessPosX + brightnessSouthWest) / 4.0F;
			f5 = (brightnessPosX + brightnessSouthWest + brightnessTopSouth + brightnessTopSouthWest) / 4.0F;
			f4 = (brightnessSouthEast + brightnessPosX + brightnessTopSouthEast + brightnessTopSouth) / 4.0F;
			f3 = (brightnessBottomSouthEast + brightnessBottomSouth + brightnessSouthEast + brightnessPosX) / 4.0F;
			cr11 = cr10 = (bl7 ? f : 1.0f) * 0.6F;
			cr01 = cr10;
			cr00 = cr10;
			cg11 = cg10 = (bl7 ? g : 1.0f) * 0.6F;
			cg01 = cg10;
			cg00 = cg10;
			cb11 = cb10 = (bl7 ? h : 1.0f) * 0.6F;
			cb01 = cb10;
			cb00 = cb10;
			cr00 *= f2;
			cg00 *= f2;
			cb00 *= f2;
			cr01 *= f3;
			cg01 *= f3;
			cb01 *= f3;
			cr11 *= f4;
			cg11 *= f4;
			cb11 *= f4;
			cr10 *= f5;
			cg10 *= f5;
			cb10 *= f5;
			TextureSample sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.POS_X.getFacing(), overlayIndex);
			if (sample != null) {
				renderPosXFace(block, x, y, z, sample);
				if (isFancy() && block == BaseBlock.GRASS && !breaking && block.getTextureForSide(blockView, x, y, z, 2) != 68) {
					cr00 *= f;
					cr01 *= f;
					cr11 *= f;
					cr10 *= f;
					cg00 *= g;
					cg01 *= g;
					cg11 *= g;
					cg10 *= g;
					cb00 *= h;
					cb01 *= h;
					cb11 *= h;
					cb10 *= h;
					renderPosXFace(block, x, y, z, Textures.getVanillaBlockSample(38));
				}
				result = true;
			}
		}
		
		shadeTopFace = false;
		return result;
	}
	
	private boolean renderCubeFast(BlockState state, int x, int y, int z, float r, float g, float b) {
		BaseBlock block = state.getBlock();
		float light;
		shadeTopFace = false;
		Tessellator tessellator = Tessellator.INSTANCE;
		boolean result = false;
		
		float bR = 0.5F;
		float bG = 0.5F;
		float bB = 0.5F;
		
		float ewR, ewG, ewB, nsR, nsG, nsB;
		
		if (item) {
			ewR = 0.6F; ewG = 0.6F; ewB = 0.6F;
			nsR = 0.8F; nsG = 0.8F; nsB = 0.8F;
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
		
		if (renderAllSides || state.isSideRendered(blockView, x, y - 1, z, BlockDirection.NEG_Y)) {
			light = getBrightness(block, x, y - 1, z);
			cr00 = bR * light;
			cg00 = bG * light;
			cb00 = bB * light;
			renderNegYFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex));
			result = true;
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x, y + 1, z, BlockDirection.POS_Y)) {
			light = getBrightness(block, x, y + 1, z);
			if (block.maxY != 1.0 && !block.material.isLiquid()) {
				light = light2;
			}
			cr00 = r * light;
			cg00 = g * light;
			cb00 = b * light;
			renderPosYFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 1, overlayIndex));
			result = true;
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x, y, z - 1, BlockDirection.NEG_Z)) {
			light = block.getBrightness(blockView, x, y, z - 1);
			if (block.minZ > 0.0) {
				light = light2;
			}
			cr00 = ewR * light;
			cg00 = ewG * light;
			cb00 = ewB * light;
			renderNegZFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 2, overlayIndex));
			if (isFancy() && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(ewR * light * r, ewG * light * g, ewB * light * b);
				renderNegZFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x, y, z + 1, BlockDirection.POS_Z)) {
			light = getBrightness(block, x, y, z + 1);
			if (block.maxZ < 1.0) {
				light = light2;
			}
			
			cr00 = ewR * light;
			cg00 = ewG * light;
			cb00 = ewB * light;
			renderPosZFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 3, overlayIndex));
			
			if (isFancy() && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(ewR * light * r, ewG * light * g, ewB * light * b);
				renderPosZFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			
			result = true;
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x - 1, y, z, BlockDirection.NEG_X)) {
			light = getBrightness(block, x - 1, y, z);
			if (block.minX > 0.0) {
				light = light2;
			}
			cr00 = nsR * light;
			cg00 = nsG * light;
			cb00 = nsB * light;
			renderNegXFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 4, overlayIndex));
			if (isFancy() && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(nsR * light * r, nsG * light * g, nsB * light * b);
				renderNegXFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x + 1, y, z, BlockDirection.POS_X)) {
			light = getBrightness(block, x + 1, y, z);
			if (block.maxX < 1.0) {
				light = light2;
			}
			cr00 = nsR * light;
			cg00 = nsG * light;
			cb00 = nsB * light;
			renderPosXFace(block, x, y, z, state.getTextureForIndex(blockView, x, y, z, 5, overlayIndex));
			if (isFancy() && block == BaseBlock.GRASS && !breaking) {
				tessellator.color(nsR * light * r, nsG * light * g, nsB * light * b);
				renderPosXFace(block, x, y, z, Textures.getVanillaBlockSample(38));
			}
			result = true;
		}
		
		return result;
	}
	
	private void updateColors(float light, boolean allColors) {
		cr00 = light == 1 ? 1 : Math.max(cr00, light);
		cg00 = light == 1 ? 1 : Math.max(cg00, light);
		cb00 = light == 1 ? 1 : Math.max(cb00, light);
		if (allColors) {
			cr01 = light == 1 ? 1 : Math.max(cr01, light);
			cg01 = light == 1 ? 1 : Math.max(cg01, light);
			cb01 = light == 1 ? 1 : Math.max(cb01, light);
			cr10 = light == 1 ? 1 : Math.max(cr10, light);
			cg10 = light == 1 ? 1 : Math.max(cg10, light);
			cb10 = light == 1 ? 1 : Math.max(cb10, light);
			cr11 = light == 1 ? 1 : Math.max(cr11, light);
			cg11 = light == 1 ? 1 : Math.max(cg11, light);
			cb11 = light == 1 ? 1 : Math.max(cb11, light);
		}
	}
	
	private void renderNegYFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		float u11, u12, v11, v12;
		
		if (forceRotation) sample.setRotation(rotation.get(BlockDirection.NEG_Y));
		
		u11 = 1F - (float) block.minX;
		u12 = 1F - (float) block.maxX;
		v11 = (float) block.maxZ;
		v12 = (float) block.minZ;
		
		if (!breaking) {
			u11 = MathUtil.clamp(u11, 0, 1);
			u12 = MathUtil.clamp(u12, 0, 1);
			v11 = MathUtil.clamp(v11, 0, 1);
			v12 = MathUtil.clamp(v12, 0, 1);
		}
		
		Vec2F u1v1 = sample.getUV(u11, v11, uvCache.get());
		Vec2F u2v1 = sample.getUV(u12, v11, uvCache.get());
		Vec2F u1v2 = sample.getUV(u11, v12, uvCache.get());
		Vec2F u2v2 = sample.getUV(u12, v12, uvCache.get());
		if (breaking) {
			u1v1.set(u11, v11);
			u2v1.set(u12, v11);
			u1v2.set(u11, v12);
			u2v2.set(u12, v12);
		}
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y1 = y + block.minY;
		double z1 = z + block.minZ;
		double z2 = z + block.maxZ;
		
		updateColors(sample.getLight(), shadeTopFace);
		tessellator.setNormal(0, -1, 0);
		if (shadeTopFace) {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(x1, y1, z2, u1v1.x, u1v1.y);
			tessellator.color(cr01, cg01, cb01);
			tessellator.vertex(x1, y1, z1, u1v2.x, u1v2.y);
			tessellator.color(cr11, cg11, cb11);
			tessellator.vertex(x2, y1, z1, u2v2.x, u2v2.y);
			tessellator.color(cr10, cg10, cb10);
			tessellator.vertex(x2, y1, z2, u2v1.x, u2v1.y);
		}
		else {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(x1, y1, z2, u1v1.x, u1v1.y);
			tessellator.vertex(x1, y1, z1, u1v2.x, u1v2.y);
			tessellator.vertex(x2, y1, z1, u2v2.x, u2v2.y);
			tessellator.vertex(x2, y1, z2, u2v1.x, u2v1.y);
		}
	}
	
	private void renderPosYFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		float u11, u12, v11, v12;
		
		if (forceRotation) sample.setRotation(rotation.get(BlockDirection.POS_Y));
		
		u11 = 1 - (float) block.maxX;
		u12 = 1 - (float) block.minX;
		v11 = 1 - (float) block.maxZ;
		v12 = 1 - (float) block.minZ;
		
		if (!breaking) {
			u11 = MathUtil.clamp(u11, 0, 1);
			u12 = MathUtil.clamp(u12, 0, 1);
			v11 = MathUtil.clamp(v11, 0, 1);
			v12 = MathUtil.clamp(v12, 0, 1);
		}
		
		Vec2F u1v1 = sample.getUV(u11, v11, uvCache.get());
		Vec2F u2v1 = sample.getUV(u12, v11, uvCache.get());
		Vec2F u1v2 = sample.getUV(u11, v12, uvCache.get());
		Vec2F u2v2 = sample.getUV(u12, v12, uvCache.get());
		if (breaking) {
			u1v1.set(u11, v11);
			u2v1.set(u12, v11);
			u1v2.set(u11, v12);
			u2v2.set(u12, v12);
		}
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y2 = y + block.maxY;
		double z1 = z + block.minZ;
		double z2 = z + block.maxZ;
		
		updateColors(sample.getLight(), shadeTopFace);
		tessellator.setNormal(0, 1, 0);
		if (shadeTopFace) {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(x2, y2, z2, u1v1.x, u1v1.y);
			tessellator.color(cr01, cg01, cb01);
			tessellator.vertex(x2, y2, z1, u1v2.x, u1v2.y);
			tessellator.color(cr11, cg11, cb11);
			tessellator.vertex(x1, y2, z1, u2v2.x, u2v2.y);
			tessellator.color(cr10, cg10, cb10);
			tessellator.vertex(x1, y2, z2, u2v1.x, u2v1.y);
		}
		else {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(x2, y2, z2, u1v1.x, u1v1.y);
			tessellator.vertex(x2, y2, z1, u1v2.x, u1v2.y);
			tessellator.vertex(x1, y2, z1, u2v2.x, u2v2.y);
			tessellator.vertex(x1, y2, z2, u2v1.x, u2v1.y);
		}
	}
	
	private void renderNegZFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		float u11, u12, v11, v12;
		
		if (forceRotation) sample.setRotation(rotation.get(BlockDirection.NEG_Z));
		
		u11 = 1F - (float) block.maxX;
		u12 = 1F - (float) block.minX;
		v11 = 1F - (float) block.maxY;
		v12 = 1F - (float) block.minY;
		
		if (!breaking) {
			u11 = MathUtil.clamp(u11, 0, 1);
			u12 = MathUtil.clamp(u12, 0, 1);
			v11 = MathUtil.clamp(v11, 0, 1);
			v12 = MathUtil.clamp(v12, 0, 1);
		}
		
		Vec2F u1v1 = sample.getUV(u11, v11, uvCache.get());
		Vec2F u2v1 = sample.getUV(u12, v11, uvCache.get());
		Vec2F u1v2 = sample.getUV(u11, v12, uvCache.get());
		Vec2F u2v2 = sample.getUV(u12, v12, uvCache.get());
		if (breaking) {
			u1v1.set(u11, v11);
			u2v1.set(u12, v11);
			u1v2.set(u11, v12);
			u2v2.set(u12, v12);
		}
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y1 = y + block.minY;
		double y2 = y + block.maxY;
		double z1 = z + block.minZ;
		
		updateColors(sample.getLight(), shadeTopFace);
		tessellator.setNormal(0, 0, -1);
		if (shadeTopFace) {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(x1, y2, z1, u2v1.x, u2v1.y);
			tessellator.color(cr01, cg01, cb01);
			tessellator.vertex(x2, y2, z1, u1v1.x, u1v1.y);
			tessellator.color(cr11, cg11, cb11);
			tessellator.vertex(x2, y1, z1, u1v2.x, u1v2.y);
			tessellator.color(cr10, cg10, cb10);
			tessellator.vertex(x1, y1, z1, u2v2.x, u2v2.y);
		}
		else {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(x1, y2, z1, u2v1.x, u2v1.y);
			tessellator.vertex(x2, y2, z1, u1v1.x, u1v1.y);
			tessellator.vertex(x2, y1, z1, u1v2.x, u1v2.y);
			tessellator.vertex(x1, y1, z1, u2v2.x, u2v2.y);
		}
	}
	
	private void renderPosZFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		float u11, u12, v11, v12;
		
		if (forceRotation) sample.setRotation(rotation.get(BlockDirection.POS_Z));
		
		u11 = (float) block.minX;
		u12 = (float) block.maxX;
		v11 = 1F - (float) block.maxY;
		v12 = 1F - (float) block.minY;
		
		if (!breaking) {
			u11 = MathUtil.clamp(u11, 0, 1);
			u12 = MathUtil.clamp(u12, 0, 1);
			v11 = MathUtil.clamp(v11, 0, 1);
			v12 = MathUtil.clamp(v12, 0, 1);
		}
		
		Vec2F u1v1 = sample.getUV(u11, v11, uvCache.get());
		Vec2F u2v1 = sample.getUV(u12, v11, uvCache.get());
		Vec2F u1v2 = sample.getUV(u11, v12, uvCache.get());
		Vec2F u2v2 = sample.getUV(u12, v12, uvCache.get());
		if (breaking) {
			u1v1.set(u11, v11);
			u2v1.set(u12, v11);
			u1v2.set(u11, v12);
			u2v2.set(u12, v12);
		}
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y1 = y + block.minY;
		double y2 = y + block.maxY;
		double z2 = z + block.maxZ;
		
		updateColors(sample.getLight(), shadeTopFace);
		tessellator.setNormal(0, 0, 1);
		if (shadeTopFace) {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(x1, y2, z2, u1v1.x, u1v1.y);
			tessellator.color(cr01, cg01, cb01);
			tessellator.vertex(x1, y1, z2, u1v2.x, u1v2.y);
			tessellator.color(cr11, cg11, cb11);
			tessellator.vertex(x2, y1, z2, u2v2.x, u2v2.y);
			tessellator.color(cr10, cg10, cb10);
			tessellator.vertex(x2, y2, z2, u2v1.x, u2v1.y);
		}
		else {
			tessellator.vertex(x1, y2, z2, u1v1.x, u1v1.y);
			tessellator.vertex(x1, y1, z2, u1v2.x, u1v2.y);
			tessellator.vertex(x2, y1, z2, u2v2.x, u2v2.y);
			tessellator.vertex(x2, y2, z2, u2v1.x, u2v1.y);
		}
	}
	
	private void renderNegXFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		float u11, u12, v11, v12;
		
		if (forceRotation) sample.setRotation(rotation.get(BlockDirection.NEG_X));
		
		u11 = (float) block.maxZ;
		u12 = (float) block.minZ;
		v11 = 1F - (float) block.minY;
		v12 = 1F - (float) block.maxY;
		
		if (!breaking) {
			u11 = MathUtil.clamp(u11, 0, 1);
			u12 = MathUtil.clamp(u12, 0, 1);
			v11 = MathUtil.clamp(v11, 0, 1);
			v12 = MathUtil.clamp(v12, 0, 1);
		}
		
		Vec2F u1v1 = sample.getUV(u11, v11, uvCache.get());
		Vec2F u2v1 = sample.getUV(u12, v11, uvCache.get());
		Vec2F u1v2 = sample.getUV(u11, v12, uvCache.get());
		Vec2F u2v2 = sample.getUV(u12, v12, uvCache.get());
		if (breaking) {
			u1v1.set(u11, v11);
			u2v1.set(u12, v11);
			u1v2.set(u11, v12);
			u2v2.set(u12, v12);
		}
		
		double x1 = x + block.minX;
		double y1 = y + block.minY;
		double y2 = y + block.maxY;
		double z1 = z + block.minZ;
		double z2 = z + block.maxZ;
		
		updateColors(sample.getLight(), shadeTopFace);
		tessellator.setNormal(-1, 0, 0);
		if (shadeTopFace) {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(x1, y2, z2, u1v2.x, u1v2.y);
			tessellator.color(cr01, cg01, cb01);
			tessellator.vertex(x1, y2, z1, u2v2.x, u2v2.y);
			tessellator.color(cr11, cg11, cb11);
			tessellator.vertex(x1, y1, z1, u2v1.x, u2v1.y);
			tessellator.color(cr10, cg10, cb10);
			tessellator.vertex(x1, y1, z2, u1v1.x, u1v1.y);
		}
		else {
			tessellator.vertex(x1, y2, z2, u1v2.x, u1v2.y);
			tessellator.vertex(x1, y2, z1, u2v2.x, u2v2.y);
			tessellator.vertex(x1, y1, z1, u2v1.x, u2v1.y);
			tessellator.vertex(x1, y1, z2, u1v1.x, u1v1.y);
		}
	}
	
	private void renderPosXFace(BaseBlock block, double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		float u11, u12, v11, v12;
		
		if (forceRotation) sample.setRotation(rotation.get(BlockDirection.POS_X));
		
		u11 = 1F - (float) block.maxZ;
		u12 = 1F - (float) block.minZ;
		v11 = 1F - (float) block.maxY;
		v12 = 1F - (float) block.minY;
		
		if (!breaking) {
			u11 = MathUtil.clamp(u11, 0, 1);
			u12 = MathUtil.clamp(u12, 0, 1);
			v11 = MathUtil.clamp(v11, 0, 1);
			v12 = MathUtil.clamp(v12, 0, 1);
		}
		
		Vec2F u1v1 = sample.getUV(u11, v11, uvCache.get());
		Vec2F u2v1 = sample.getUV(u12, v11, uvCache.get());
		Vec2F u1v2 = sample.getUV(u11, v12, uvCache.get());
		Vec2F u2v2 = sample.getUV(u12, v12, uvCache.get());
		if (breaking) {
			u1v1.set(u11, v11);
			u2v1.set(u12, v11);
			u1v2.set(u11, v12);
			u2v2.set(u12, v12);
		}
		
		double d10 = x + block.maxX;
		double d11 = y + block.minY;
		double d12 = y + block.maxY;
		double d13 = z + block.minZ;
		double d14 = z + block.maxZ;
		
		updateColors(sample.getLight(), shadeTopFace);
		tessellator.setNormal(1, 0, 0);
		if (shadeTopFace) {
			tessellator.color(cr00, cg00, cb00);
			tessellator.vertex(d10, d11, d14, u1v2.x, u1v2.y);
			tessellator.color(cr01, cg01, cb01);
			tessellator.vertex(d10, d11, d13, u2v2.x, u2v2.y);
			tessellator.color(cr11, cg11, cb11);
			tessellator.vertex(d10, d12, d13, u2v1.x, u2v1.y);
			tessellator.color(cr10, cg10, cb10);
			tessellator.vertex(d10, d12, d14, u1v1.x, u1v1.y);
		}
		else {
			tessellator.vertex(d10, d11, d14, u1v2.x, u1v2.y);
			tessellator.vertex(d10, d11, d13, u2v2.x, u2v2.y);
			tessellator.vertex(d10, d12, d13, u2v1.x, u2v1.y);
			tessellator.vertex(d10, d12, d14, u1v1.x, u1v1.y);
		}
	}
	
	private boolean renderCross(BlockState state, int x, int y, int z) {
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex);
		if (sample == null) return false;
		
		Tessellator tessellator = Tessellator.INSTANCE;
		BaseBlock block = state.getBlock();
		
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
		
		float light = getBrightness(block, x, y, z);
		r *= light;
		g *= light;
		b *= light;
		
		light = sample.getLight();
		r = light == 1 ? 1 : Math.max(r, light);
		g = light == 1 ? 1 : Math.max(g, light);
		b = light == 1 ? 1 : Math.max(b, light);
		tessellator.color(r, g, b);
		
		double px = x;
		double py = y;
		double pz = z;
		
		if (block == BaseBlock.TALLGRASS) {
			px += TABLES[0].getFloat(x, y, z) * 0.5F - 0.25F;
			pz += TABLES[1].getFloat(x, y, z) * 0.5F - 0.25F;
			py -= TABLES[2].getFloat(x, y, z) * 0.2F;
		}
		
		renderCross(px, py, pz, sample);
		return true;
	}
	
	private void renderCross(double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		Vec2F u1v1 = sample.getUV(0, 0, uvCache.get());
		Vec2F u2v1 = sample.getUV(1, 0, uvCache.get());
		Vec2F u1v2 = sample.getUV(0, 1, uvCache.get());
		Vec2F u2v2 = sample.getUV(1, 1, uvCache.get());
		
		double x1 = x + 0.5 - 0.45;
		double x2 = x + 0.5 + 0.45;
		double z1 = z + 0.5 - 0.45;
		double z2 = z + 0.5 + 0.45;
		
		tessellator.vertex(x1, y + 1.0, z1, u1v1.x, u1v1.y);
		tessellator.vertex(x1, y + 0.0, z1, u1v2.x, u1v2.y);
		tessellator.vertex(x2, y + 0.0, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x2, y + 1.0, z2, u2v1.x, u2v1.y);
		tessellator.vertex(x2, y + 1.0, z2, u1v1.x, u1v1.y);
		tessellator.vertex(x2, y + 0.0, z2, u1v2.x, u1v2.y);
		tessellator.vertex(x1, y + 0.0, z1, u2v2.x, u2v2.y);
		tessellator.vertex(x1, y + 1.0, z1, u2v1.x, u2v1.y);
		tessellator.vertex(x1, y + 1.0, z2, u1v1.x, u1v1.y);
		tessellator.vertex(x1, y + 0.0, z2, u1v2.x, u1v2.y);
		tessellator.vertex(x2, y + 0.0, z1, u2v2.x, u2v2.y);
		tessellator.vertex(x2, y + 1.0, z1, u2v1.x, u2v1.y);
		tessellator.vertex(x2, y + 1.0, z1, u1v1.x, u1v1.y);
		tessellator.vertex(x2, y + 0.0, z1, u1v2.x, u1v2.y);
		tessellator.vertex(x1, y + 0.0, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x1, y + 1.0, z2, u2v1.x, u2v1.y);
	}
	
	private boolean renderTorch(BlockState state, int x, int y, int z) {
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex);
		if (sample == null) return false;
		
		int meta = state.getMeta();
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		float light = getBrightness(block, x, y, z);
		if (state.getEmittance() > 0) {
			light = 1.0f;
		}
		
		light = Math.max(light, sample.getLight());
		tessellator.color(light, light, light);
		
		double d = 0.4f;
		double d2 = 0.5 - d;
		double d3 = 0.2f;
		
		switch (meta) {
			case 1 -> renderTorchSkewed(x - d2, y + d3, z, -d, 0.0, sample);
			case 2 -> renderTorchSkewed(x + d2, y + d3, z, d, 0.0, sample);
			case 3 -> renderTorchSkewed(x, y + d3, z - d2, 0.0, -d, sample);
			case 4 -> renderTorchSkewed(x, y + d3, z + d2, 0.0, d, sample);
			default -> renderTorchSkewed(x, y, z, 0.0, 0.0, sample);
		}
		
		return true;
	}
	
	private void renderTorchSkewed(double x, double y, double z, double dx, double dz, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		
		Vec2F u1v1 = sample.getUV(0, 0, uvCache.get());
		Vec2F u2v2 = sample.getUV(1, 1, uvCache.get());
		Vec2F u3v3 = sample.getUV(0.4375F, 0.375F, uvCache.get());
		Vec2F u4v4 = sample.getUV(0.5625F, 0.5F, uvCache.get());
		
		double x2 = (x += 0.5) - 0.5;
		double x3 = x + 0.5;
		double z2 = (z += 0.5) - 0.5;
		double z3 = z + 0.5;
		
		double x1 = x + dx * (1.0 - 0.625);
		double z1 = z + dz * (1.0 - 0.625);
		
		tessellator.vertex(x1 - 0.0625, y + 0.625, z1 - 0.0625, u3v3.x, u3v3.y);
		tessellator.vertex(x1 - 0.0625, y + 0.625, z1 + 0.0625, u3v3.x, u4v4.y);
		tessellator.vertex(x1 + 0.0625, y + 0.625, z1 + 0.0625, u4v4.x, u4v4.y);
		tessellator.vertex(x1 + 0.0625, y + 0.625, z1 - 0.0625, u4v4.x, u3v3.y);
		
		tessellator.vertex(x - 0.0625, y + 1.0, z2, u1v1.x, u1v1.y);
		tessellator.vertex(x - 0.0625 + dx, y + 0.0, z2 + dz, u1v1.x, u2v2.y);
		tessellator.vertex(x - 0.0625 + dx, y + 0.0, z3 + dz, u2v2.x, u2v2.y);
		tessellator.vertex(x - 0.0625, y + 1.0, z3, u2v2.x, u1v1.y);
		
		tessellator.vertex(x + 0.0625, y + 1.0, z3, u1v1.x, u1v1.y);
		tessellator.vertex(x + dx + 0.0625, y + 0.0, z3 + dz, u1v1.x, u2v2.y);
		tessellator.vertex(x + dx + 0.0625, y + 0.0, z2 + dz, u2v2.x, u2v2.y);
		tessellator.vertex(x + 0.0625, y + 1.0, z2, u2v2.x, u1v1.y);
		
		tessellator.vertex(x2, y + 1.0, z + 0.0625, u1v1.x, u1v1.y);
		tessellator.vertex(x2 + dx, y + 0.0, z + 0.0625 + dz, u1v1.x, u2v2.y);
		tessellator.vertex(x3 + dx, y + 0.0, z + 0.0625 + dz, u2v2.x, u2v2.y);
		tessellator.vertex(x3, y + 1.0, z + 0.0625, u2v2.x, u1v1.y);
		
		tessellator.vertex(x3, y + 1.0, z - 0.0625, u1v1.x, u1v1.y);
		tessellator.vertex(x3 + dx, y + 0.0, z - 0.0625 + dz, u1v1.x, u2v2.y);
		tessellator.vertex(x2 + dx, y + 0.0, z - 0.0625 + dz, u2v2.x, u2v2.y);
		tessellator.vertex(x2, y + 1.0, z - 0.0625, u2v2.x, u1v1.y);
	}
	
	private boolean renderFire(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		TextureSample sample1 = state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex);
		TextureSample sample2 = state.getTextureForIndex(blockView, x, y, z, 1, overlayIndex);
		
		if (sample1 == null || sample2 == null) return false;
		
		float light = block.getBrightness(blockView, x, y, z);
		light = Math.max(light, Math.max(sample1.getLight(), sample2.getLight()));
		tessellator.color(light, light, light);
		
		Vec2F u1v1 = sample1.getUV(0, 0, uvCache.get());
		Vec2F u1v2 = sample1.getUV(0, 1, uvCache.get());
		Vec2F u2v1 = sample1.getUV(1, 0, uvCache.get());
		Vec2F u2v2 = sample1.getUV(1, 1, uvCache.get());
		
		if (breaking) {
			u1v1.set(0, 0);
			u1v2.set(0, 1);
			u2v1.set(1, 0);
			u2v2.set(1, 1);
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
			
			tessellator.vertex(x3, y + size, z + 1, u2v1.x, u2v1.y);
			tessellator.vertex(x2, y, z + 1, u2v2.x, u2v2.y);
			tessellator.vertex(x2, y, z, u1v2.x, u1v2.y);
			tessellator.vertex(x3, y + size, z, u1v1.x, u1v1.y);
			tessellator.vertex(x4, y + size, z, u2v1.x, u2v1.y);
			tessellator.vertex(x1, y, z, u2v2.x, u2v2.y);
			tessellator.vertex(x1, y, z + 1, u1v2.x, u1v2.y);
			tessellator.vertex(x4, y + size, z + 1, u1v1.x, u1v1.y);
			
			u1v1 = sample2.getUV(0, 0, u1v1);
			u1v2 = sample2.getUV(0, 1, u1v2);
			u2v1 = sample2.getUV(1, 0, u2v1);
			u2v2 = sample2.getUV(1, 1, u2v2);
			
			tessellator.vertex(x + 1, y + size, z4, u2v1.x, u2v1.y);
			tessellator.vertex(x + 1, y, z1, u2v2.x, u2v2.y);
			tessellator.vertex(x, y, z1, u1v2.x, u1v2.y);
			tessellator.vertex(x, y + size, z4, u1v1.x, u1v1.y);
			tessellator.vertex(x, y + size, z3, u2v1.x, u2v1.y);
			tessellator.vertex(x, y, z2, u2v2.x, u2v2.y);
			tessellator.vertex(x + 1, y, z2, u1v2.x, u1v2.y);
			tessellator.vertex(x + 1, y + size, z3, u1v1.x, u1v1.y);
			
			x2 = x + 0.5 - 0.5;
			x1 = x + 0.5 + 0.5;
			z2 = z + 0.5 - 0.5;
			z1 = z + 0.5 + 0.5;
			x3 = x + 0.5 - 0.4;
			x4 = x + 0.5 + 0.4;
			z3 = z + 0.5 - 0.4;
			z4 = z + 0.5 + 0.4;
			
			tessellator.vertex(x3, y + size, z, u1v1.x, u1v1.y);
			tessellator.vertex(x2, y, z, u1v2.x, u1v2.y);
			tessellator.vertex(x2, y, z + 1, u2v2.x, u2v2.y);
			tessellator.vertex(x3, y + size, z + 1, u2v1.x, u2v1.y);
			tessellator.vertex(x4, y + size, z + 1, u1v1.x, u1v1.y);
			tessellator.vertex(x1, y, z + 1, u1v2.x, u1v2.y);
			tessellator.vertex(x1, y, z, u2v2.x, u2v2.y);
			tessellator.vertex(x4, y + size, z, u2v1.x, u2v1.y);
			
			u1v1 = sample1.getUV(0, 0, u1v1);
			u1v2 = sample1.getUV(0, 1, u1v2);
			u2v1 = sample1.getUV(1, 0, u2v1);
			u2v2 = sample1.getUV(1, 1, u2v2);
			
			tessellator.vertex(x, y + size, z4, u1v1.x, u1v1.y);
			tessellator.vertex(x, y, z1, u1v2.x, u1v2.y);
			tessellator.vertex(x + 1, y, z1, u2v2.x, u2v2.y);
			tessellator.vertex(x + 1, y + size, z4, u2v1.x, u2v1.y);
			tessellator.vertex(x + 1, y + size, z3, u1v1.x, u1v1.y);
			tessellator.vertex(x + 1, y, z2, u1v2.x, u1v2.y);
			tessellator.vertex(x, y, z2, u2v2.x, u2v2.y);
			tessellator.vertex(x, y + size, z3, u2v1.x, u2v1.y);
		}
		else {
			float f3 = 0.2f;
			float f4 = 0.0625f;
			
			if ((x / 2 + y / 2 + z / 2 & 1) == 1) {
				sample2.setMirrorU(true);
			}
			
			if ((x + y + z & 1) == 1) {
				u1v1 = sample2.getUV(0, 0, u1v1);
				u1v2 = sample2.getUV(0, 1, u1v2);
				u2v1 = sample2.getUV(1, 0, u2v1);
				u2v2 = sample2.getUV(1, 1, u2v2);
			}
			
			if ((x / 2 + y / 2 + z / 2 & 1) == 1) {
				float u = u1v1.x;
				u1v1.x = u2v1.x;
				u1v2.x = u2v1.x;
				u2v1.x = u;
				u2v2.x = u;
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x - 1, y, z)) {
				tessellator.vertex(x + f3, y + size + f4, z + 1, u2v1.x, u2v1.y);
				tessellator.vertex(x, y + f4, z + 1, u2v2.x, u2v2.y);
				tessellator.vertex(x, y + f4, z, u1v2.x, u1v2.y);
				tessellator.vertex(x + f3, y + size + f4, z, u1v1.x, u1v1.y);
				tessellator.vertex(x + f3, y + size + f4, z, u1v1.x, u1v1.y);
				tessellator.vertex(x, y + f4, z, u1v2.x, u1v2.y);
				tessellator.vertex(x, y + f4, z + 1, u2v2.x, u2v2.y);
				tessellator.vertex(x + f3, y + size + f4, z + 1, u2v1.x, u2v1.y);
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x + 1, y, z)) {
				tessellator.vertex(x + 1 - f3, y + size + f4, z, u1v1.x, u1v1.y);
				tessellator.vertex(x + 1, y + f4, z, u1v2.x, u1v2.y);
				tessellator.vertex(x + 1, y + f4, z + 1, u2v2.x, u2v2.y);
				tessellator.vertex(x + 1 - f3, y + size + f4, z + 1, u2v1.x, u2v1.y);
				tessellator.vertex(x + 1 - f3, y + size + f4, z + 1, u2v1.x, u2v1.y);
				tessellator.vertex(x + 1, y + f4, z + 1, u2v2.x, u2v2.y);
				tessellator.vertex(x + 1, y + f4, z, u1v2.x, u1v2.y);
				tessellator.vertex(x + 1 - f3, y + size + f4, z, u1v1.x, u1v1.y);
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x, y, z - 1)) {
				tessellator.vertex(x, y + size + f4, z + f3, u2v1.x, u2v1.y);
				tessellator.vertex(x, y + f4, z, u2v2.x, u2v2.y);
				tessellator.vertex(x + 1, y + f4, z, u1v2.x, u1v2.y);
				tessellator.vertex(x + 1, y + size + f4, z + f3, u1v1.x, u1v1.y);
				tessellator.vertex(x + 1, y + size + f4, z + f3, u1v1.x, u1v1.y);
				tessellator.vertex(x + 1, y + f4, z, u1v2.x, u1v2.y);
				tessellator.vertex(x, y + f4, z, u2v2.x, u2v2.y);
				tessellator.vertex(x, y + size + f4, z + f3, u2v1.x, u2v1.y);
			}
			
			if (BaseBlock.FIRE.method_1824(blockView, x, y, z + 1)) {
				tessellator.vertex(x + 1, y + size + f4, z + 1 - f3, u1v1.x, u1v1.y);
				tessellator.vertex(x + 1, y + f4, z + 1, u1v2.x, u1v2.y);
				tessellator.vertex(x, y + f4, z + 1, u2v2.x, u2v2.y);
				tessellator.vertex(x, y + size + f4, z + 1 - f3, u2v1.x, u2v1.y);
				tessellator.vertex(x, y + size + f4, z + 1 - f3, u2v1.x, u2v1.y);
				tessellator.vertex(x, y + f4, z + 1, u2v2.x, u2v2.y);
				tessellator.vertex(x + 1, y + f4, z + 1, u1v2.x, u1v2.y);
				tessellator.vertex(x + 1, y + size + f4, z + 1 - f3, u1v1.x, u1v1.y);
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
					tessellator.vertex(x7, y + size, z, u2v1.x, u2v1.y);
					tessellator.vertex(x6, y, z, u2v2.x, u2v2.y);
					tessellator.vertex(x6, y, z + 1, u1v2.x, u1v2.y);
					tessellator.vertex(x7, y + size, z + 1, u1v1.x, u1v1.y);
					
					u1v1 = sample2.getUV(0, 0, u1v1);
					u1v2 = sample2.getUV(0, 1, u1v2);
					u2v1 = sample2.getUV(1, 0, u2v1);
					u2v2 = sample2.getUV(1, 1, u2v2);
					
					tessellator.vertex(x8, y + size, z + 1, u2v1.x, u2v1.y);
					tessellator.vertex(x5, y, z + 1, u2v2.x, u2v2.y);
					tessellator.vertex(x5, y, z, u1v2.x, u1v2.y);
					tessellator.vertex(x8, y + size, z, u1v1.x, u1v1.y);
				}
				else {
					tessellator.vertex(x, y + size, z8, u2v1.x, u2v1.y);
					tessellator.vertex(x, y, z5, u2v2.x, u2v2.y);
					tessellator.vertex(x + 1, y, z5, u1v2.x, u1v2.y);
					tessellator.vertex(x + 1, y + size, z8, u1v1.x, u1v1.y);
					
					u1v1 = sample2.getUV(0, 0, u1v1);
					u1v2 = sample2.getUV(0, 1, u1v2);
					u2v1 = sample2.getUV(1, 0, u2v1);
					u2v2 = sample2.getUV(1, 1, u2v2);
					
					tessellator.vertex(x + 1, y + size, z7, u2v1.x, u2v1.y);
					tessellator.vertex(x + 1, y, z6, u2v2.x, u2v2.y);
					tessellator.vertex(x, y, z6, u1v2.x, u1v2.y);
					tessellator.vertex(x, y + size, z7, u1v1.x, u1v1.y);
				}
			}
		}
		
		return true;
	}
	
	private boolean renderFluid(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		
		Tessellator tessellator = Tessellator.INSTANCE;
		
		int color = block.getColorMultiplier(blockView, x, y, z);
		float r = (float) (color >> 16 & 0xFF) / 255.0f;
		float g = (float) (color >> 8 & 0xFF) / 255.0f;
		float b = (float) (color & 0xFF) / 255.0f;
		
		boolean renderTop = state.isSideRendered(blockView, x, y + 1, z, BlockDirection.POS_Y);
		boolean renderBottom = state.isSideRendered(blockView, x, y - 1, z, BlockDirection.NEG_Y);
		
		renderSides[0] = state.isSideRendered(blockView, x, y, z - 1, BlockDirection.NEG_Z);
		renderSides[1] = state.isSideRendered(blockView, x, y, z + 1, BlockDirection.POS_Z);
		renderSides[2] = state.isSideRendered(blockView, x - 1, y, z, BlockDirection.NEG_X);
		renderSides[3] = state.isSideRendered(blockView, x + 1, y, z, BlockDirection.POS_X);
		
		if (!(renderTop || renderBottom || renderSides[0] || renderSides[1] || renderSides[2] || renderSides[3])) {
			return false;
		}
		
		boolean result = false;
		double minY = 0.0;
		double maxY = 1.0;
		
		Material material = block.material;
		float h1 = getFluidHeight(x, y, z, material);
		float h2 = getFluidHeight(x, y, z + 1, material);
		float h3 = getFluidHeight(x + 1, y, z + 1, material);
		float h4 = getFluidHeight(x + 1, y, z, material);
		
		TextureSample sample;
		if (renderAllSides || renderTop) {
			float angle = (float) FluidBlock.getFluidAngle(blockView, x, y, z, material);
			
			boolean isFlowing = angle > -999.0f;
			sample = state.getTextureForIndex(blockView, x, y, z, isFlowing ? 2 : 1, overlayIndex);
			if (sample != null) {
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
				light = Math.max(light, sample.getLight());
				tessellator.color(light * r, light * g, light * b);
				
				Vec2F uv1 = sample.getUV(u1 - cos - sin, v1 - cos + sin, uvCache.get());
				Vec2F uv2 = sample.getUV(u1 - cos + sin, v1 + cos + sin, uvCache.get());
				Vec2F uv3 = sample.getUV(u1 + cos + sin, v1 + cos - sin, uvCache.get());
				Vec2F uv4 = sample.getUV(u1 + cos - sin, v1 - cos - sin, uvCache.get());
				
				tessellator.vertex(x, y + h1, z, uv1.x, uv1.y);
				tessellator.vertex(x, y + h2, z + 1, uv2.x, uv2.y);
				tessellator.vertex(x + 1, y + h3, z + 1, uv3.x, uv3.y);
				tessellator.vertex(x + 1, y + h4, z, uv4.x, uv4.y);
				
				result = true;
			}
		}
		
		if (renderAllSides || renderBottom) {
			sample = state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex);
			if (sample != null) {
				float light = getBrightness(block, x, y - 1, z) * 0.5F;
				light = Math.max(light, sample.getLight());
				tessellator.color(light, light, light);
				renderNegYFace(block, x, y, z, sample);
				result = true;
			}
		}
		
		float px1, px2, py1, py2, pz1, pz2;
		for (int side = 0; side < 4; ++side) {
			int dx = x;
			int dz = z;
			
			if (side == 0) --dz;
			if (side == 1) ++dz;
			if (side == 2) --dx;
			if (side == 3) ++dx;
			
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
			
			sample = state.getTextureForIndex(blockView, x, y, z, side + 2, overlayIndex);
			if (sample == null) return result;
			
			Vec2F u1v1 = sample.getUV(0, 0.5F - py1 * 0.5F, uvCache.get());
			Vec2F u2v2 = sample.getUV(0.5F, 0.5F - py2 * 0.5F, uvCache.get());
			Vec2F u2v3 = sample.getUV(0.5F, 0.5F, uvCache.get());
			Vec2F u1v3 = sample.getUV(0, 0.5F, uvCache.get());
			
			float light = block.getBrightness(blockView, dx, y, dz);
			light = side < 2 ? (light * 0.8f) : (light * 0.6f);
			light = Math.max(light, sample.getLight());
			tessellator.color(light * r, light * g, light * b);
			
			tessellator.vertex(px1, y + py1, pz1, u1v1.x, u1v1.y);
			tessellator.vertex(px2, y + py2, pz2, u2v2.x, u2v2.y);
			tessellator.vertex(px2, y, pz2, u2v3.x, u2v3.y);
			tessellator.vertex(px1, y, pz1, u1v3.x, u1v3.y);
			result = true;
		}
		
		block.minY = minY;
		block.maxY = maxY;
		
		return result;
	}
	
	private float getFluidHeight(int x, int y, int z, Material material) {
		int iteration = 0;
		float offset = 0.0f;
		
		for (int i2 = 0; i2 < 4; ++i2) {
			int px = x - (i2 & 1);
			int pz = z - (i2 >> 1 & 1);
			
			if (blockView.getMaterial(px, y + 1, pz) == material) {
				return 1.0f;
			}
			
			Material levelMaterial = blockView.getMaterial(px, y, pz);
			
			if (material == levelMaterial) {
				int meta = blockView.getBlockMeta(px, y, pz);
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
	
	private boolean renderRedstoneDust(BlockState state, int x, int y, int z) {
		TextureSample sample1 = state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex);
		TextureSample sample2 = state.getTextureForIndex(blockView, x, y, z, 1, overlayIndex);
		
		if (sample1 == null || sample2 == null) return false;
		
		BaseBlock block = state.getBlock();
		
		Tessellator tessellator = Tessellator.INSTANCE;
		int meta = state.getMeta();
		
		float light = block.getBrightness(blockView, x, y, z);
		float power = (float) meta / 15.0f;
		
		float r = power * 0.6f + 0.4f;
		float g = power * power * 0.7f - 0.5f;
		float b = power * power * 0.6f - 0.7f;
		
		if (meta == 0) r = 0.3f;
		if (g < 0.0f) g = 0.0f;
		if (b < 0.0f) b = 0.0f;
		
		light = Math.max(light, Math.max(sample1.getLight(), sample2.getLight()));
		tessellator.color(light * r, light * g, light * b);
		
		Vec2F u1v1 = sample1.getUV(0, 0, uvCache.get());
		Vec2F u1v2 = sample1.getUV(0, 1, uvCache.get());
		Vec2F u2v1 = sample1.getUV(1, 0, uvCache.get());
		Vec2F u2v2 = sample1.getUV(1, 1, uvCache.get());
		
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
			u1v1 = sample2.getUV(0, 0, u1v1);
			u1v2 = sample2.getUV(0, 1, u1v2);
			u2v1 = sample2.getUV(1, 0, u2v1);
			u2v2 = sample2.getUV(1, 1, u2v2);
		}
		
		if (type == 0) {
			if (cx2 || cz1 || cz2 || cx1) {
				float u1 = 0;
				float u2 = 1;
				float v1 = 0;
				float v2 = 1;
				if (!cx1) x1 += 0.3125f;
				if (!cx1) u1 = 0.3125F;
				if (!cx2) x2 -= 0.3125f;
				if (!cx2) u2 = 0.6875F;
				if (!cz1) z1 += 0.3125f;
				if (!cz1) v1 = 0.3125F;
				if (!cz2) z2 -= 0.3125f;
				if (!cz2) v2 = 0.6875F;
				u1v1 = sample1.getUV(u1, v1, u1v1);
				u1v2 = sample1.getUV(u1, v2, u1v2);
				u2v1 = sample1.getUV(u2, v1, u2v1);
				u2v2 = sample1.getUV(u2, v2, u2v2);
			}
			
			tessellator.vertex(x2, y + 0.015625f, z2, u2v2.x, u2v2.y);
			tessellator.vertex(x2, y + 0.015625f, z1, u2v1.x, u2v1.y);
			tessellator.vertex(x1, y + 0.015625f, z1, u1v1.x, u1v1.y);
			tessellator.vertex(x1, y + 0.015625f, z2, u1v2.x, u1v2.y);
		}
		else if (type == 1) {
			tessellator.vertex(x2, y + 0.015625f, z2, u2v2.x, u2v2.y);
			tessellator.vertex(x2, y + 0.015625f, z1, u2v1.x, u2v1.y);
			tessellator.vertex(x1, y + 0.015625f, z1, u1v1.x, u1v1.y);
			tessellator.vertex(x1, y + 0.015625f, z2, u1v2.x, u1v2.y);
		}
		else {
			tessellator.vertex(x2, y + 0.015625f, z2, u2v2.x, u2v2.y);
			tessellator.vertex(x2, y + 0.015625f, z1, u1v2.x, u1v2.y);
			tessellator.vertex(x1, y + 0.015625f, z1, u1v1.x, u1v1.y);
			tessellator.vertex(x1, y + 0.015625f, z2, u2v1.x, u2v1.y);
		}
		
		if (!blockView.canSuffocate(x, y + 1, z)) {
			u1v1 = sample2.getUV(0, 0, u1v1);
			u1v2 = sample2.getUV(0, 1, u1v2);
			u2v1 = sample2.getUV(1, 0, u2v1);
			u2v2 = sample2.getUV(1, 1, u2v2);
			
			if (blockView.canSuffocate(x - 1, y, z) && blockView.getBlockId(x - 1, y + 1, z) == BaseBlock.REDSTONE_DUST.id) {
				tessellator.color(light * r, light * g, light * b);
				tessellator.vertex(x + 0.015625, y + 1.021875, z + 1, u2v1.x, u2v1.y);
				tessellator.vertex(x + 0.015625, y, z + 1, u1v1.x, u1v1.y);
				tessellator.vertex(x + 0.015625, y, z, u1v2.x, u1v2.y);
				tessellator.vertex(x + 0.015625, y + 1.021875, z, u2v2.x, u2v2.y);
			}
			
			if (blockView.canSuffocate(x + 1, y, z) && blockView.getBlockId(x + 1, y + 1, z) == BaseBlock.REDSTONE_DUST.id) {
				tessellator.color(light * r, light * g, light * b);
				tessellator.vertex(x + 0.984375, y, z + 1, u1v2.x, u1v2.y);
				tessellator.vertex(x + 0.984375, y + 1.021875, z + 1, u2v2.y, u2v2.y);
				tessellator.vertex(x + 0.984375, y + 1.021875, z, u2v1.y, u2v1.y);
				tessellator.vertex(x + 0.984375, y, z, u1v1.y, u1v1.y);
			}
			
			if (blockView.canSuffocate(x, y, z - 1) && blockView.getBlockId(x, y + 1, z - 1) == BaseBlock.REDSTONE_DUST.id) {
				tessellator.color(light * r, light * g, light * b);
				tessellator.vertex(x + 1, y, z + 0.015625, u1v2.x, u1v2.y);
				tessellator.vertex(x + 1, y + 1.021875, z + 0.015625, u2v2.x, u2v2.y);
				tessellator.vertex(x, y + 1.021875, z + 0.015625, u2v1.x, u2v1.y);
				tessellator.vertex(x, y, z + 0.015625, u1v1.x, u1v1.y);
			}
			
			if (blockView.canSuffocate(x, y, z + 1) && blockView.getBlockId(x, y + 1, z + 1) == BaseBlock.REDSTONE_DUST.id) {
				tessellator.color(light * r, light * g, light * b);
				tessellator.vertex(x + 1, y + 1.021875, z + 0.984375, u2v1.x, u2v1.y);
				tessellator.vertex(x + 1, y, z + 0.984375, u1v1.x, u1v1.y);
				tessellator.vertex(x, y, z + 0.984375, u1v2.x, u1v2.y);
				tessellator.vertex(x, y + 1.021875, z + 0.984375, u2v2.x, u2v2.y);
			}
		}
		
		return true;
	}
	
	private boolean renderCrops(BlockState state, int x, int y, int z) {
		int meta = state.getMeta();
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, meta, overlayIndex);
		if (sample == null) return false;
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		float light = getBrightness(block, x, y, z);
		light = Math.max(light, sample.getLight());
		tessellator.color(light, light, light);
		renderCrop(x, y - 0.0625, z, sample);
		return true;
	}
	
	private void renderCrop(double x, double y, double z, TextureSample sample) {
		Tessellator tessellator = Tessellator.INSTANCE;
		Vec2F u1v1 = sample.getUV(0, 0, uvCache.get());
		Vec2F u1v2 = sample.getUV(0, 1, uvCache.get());
		Vec2F u2v1 = sample.getUV(1, 0, uvCache.get());
		Vec2F u2v2 = sample.getUV(1, 1, uvCache.get());
		
		double x1 = x + 0.5 - 0.25;
		double x2 = x + 0.5 + 0.25;
		double z1 = z + 0.5 - 0.5;
		double z2 = z + 0.5 + 0.5;
		double y2 = y + 1;
		
		tessellator.vertex(x1, y2, z1, u1v1.x, u1v1.y);
		tessellator.vertex(x1, y, z1, u1v2.x, u1v2.y);
		tessellator.vertex(x1, y, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x1, y2, z2, u2v1.x, u2v1.y);
		tessellator.vertex(x1, y2, z2, u1v1.x, u1v1.y);
		tessellator.vertex(x1, y, z2, u1v2.x, u1v2.y);
		tessellator.vertex(x1, y, z1, u2v2.x, u2v2.y);
		tessellator.vertex(x1, y2, z1, u2v1.x, u2v1.y);
		tessellator.vertex(x2, y2, z2, u1v1.x, u1v1.y);
		tessellator.vertex(x2, y, z2, u1v2.x, u1v2.y);
		tessellator.vertex(x2, y, z1, u2v2.x, u2v2.y);
		tessellator.vertex(x2, y2, z1, u2v1.x, u2v1.y);
		tessellator.vertex(x2, y2, z1, u1v1.x, u1v1.y);
		tessellator.vertex(x2, y, z1, u1v2.x, u1v2.y);
		tessellator.vertex(x2, y, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x2, y2, z2, u2v1.x, u2v1.y);
		
		x1 = x + 0.5 - 0.5;
		x2 = x + 0.5 + 0.5;
		z1 = z + 0.5 - 0.25;
		z2 = z + 0.5 + 0.25;
		
		tessellator.vertex(x1, y2, z1, u1v1.x, u1v1.y);
		tessellator.vertex(x1, y, z1, u1v2.x, u1v2.y);
		tessellator.vertex(x2, y, z1, u2v2.x, u2v2.y);
		tessellator.vertex(x2, y2, z1, u2v1.x, u2v1.y);
		tessellator.vertex(x2, y2, z1, u1v1.x, u1v1.y);
		tessellator.vertex(x2, y, z1, u1v2.x, u1v2.y);
		tessellator.vertex(x1, y, z1, u2v2.x, u2v2.y);
		tessellator.vertex(x1, y2, z1, u2v1.x, u2v1.y);
		tessellator.vertex(x2, y2, z2, u1v1.x, u1v1.y);
		tessellator.vertex(x2, y, z2, u1v2.x, u1v2.y);
		tessellator.vertex(x1, y, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x1, y2, z2, u2v1.x, u2v1.y);
		tessellator.vertex(x1, y2, z2, u1v1.x, u1v1.y);
		tessellator.vertex(x1, y, z2, u1v2.x, u1v2.y);
		tessellator.vertex(x2, y, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x2, y2, z2, u2v1.x, u2v1.y);
	}
	
	private boolean renderDoor(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		boolean result = false;
		
		float lightT = block.getBrightness(blockView, x, y, z);
		float lightB = block.getBrightness(blockView, x, y - 1, z);
		
		if (block.minY > 0.0) lightB = lightT;
		if (state.getEmittance() > 0) lightB = 1.0f;
		
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.NEG_Y.getFacing(), overlayIndex);
		if (sample != null) {
			lightB = Math.max(lightB * 0.5F, sample.getLight());
			cr00 = lightB; cg00 = lightB; cb00 = lightB;
			renderNegYFace(block, x, y, z, sample);
			result = true;
		}
		
		lightB = block.getBrightness(blockView, x, y + 1, z);
		if (block.maxY < 1.0) lightB = lightT;
		if (state.getEmittance() > 0) lightB = 1.0f;
		
		sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.POS_Y.getFacing(), overlayIndex);
		if (sample != null) {
			lightB = Math.max(lightB, sample.getLight());
			cr00 = lightB; cg00 = lightB; cb00 = lightB;
			renderPosYFace(block, x, y, z, sample);
			result = true;
		}
		
		lightB = block.getBrightness(blockView, x, y, z - 1);
		if (block.minZ > 0.0) lightB = lightT;
		if (state.getEmittance() > 0) lightB = 1.0f;
		
		int texture;
		sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.NEG_Z.getFacing(), overlayIndex);
		if (sample != null) {
			texture = block.getTextureForSide(blockView, x, y, z, BlockDirection.NEG_Z.getFacing());
			if (texture < 0) sample.setMirrorU(true);
			lightB = Math.max(lightB * 0.8f, sample.getLight());
			cr00 = lightB; cg00 = lightB; cb00 = lightB;
			renderNegZFace(block, x, y, z, sample);
			sample.setMirrorU(false);
			result = true;
		}
		
		lightB = block.getBrightness(blockView, x, y, z + 1);
		if (block.maxZ < 1.0) lightB = lightT;
		if (state.getEmittance() > 0) lightB = 1.0f;
		
		sample = state.getTextureForIndex(blockView, x, y, z, 3, overlayIndex);
		if (sample != null) {
			texture = block.getTextureForSide(blockView, x, y, z, 3);
			if (texture < 0) sample.setMirrorU(true);
			lightB = Math.max(lightB * 0.8f, sample.getLight());
			cr00 = lightB; cg00 = lightB; cb00 = lightB;
			renderPosZFace(block, x, y, z, sample);
			sample.setMirrorU(false);
			result = true;
		}
		
		lightB = block.getBrightness(blockView, x - 1, y, z);
		if (block.minX > 0.0) lightB = lightT;
		if (state.getEmittance() > 0) lightB = 1.0f;
		
		sample = state.getTextureForIndex(blockView, x, y, z, 4, overlayIndex);
		if (sample != null) {
			texture = block.getTextureForSide(blockView, x, y, z, 4);
			if (texture < 0) sample.setMirrorU(true);
			lightB = Math.max(lightB * 0.6f, sample.getLight());
			cr00 = lightB; cg00 = lightB; cb00 = lightB;
			renderNegXFace(block, x, y, z, sample);
			sample.setMirrorU(false);
			result = true;
		}
		
		lightB = block.getBrightness(blockView, x + 1, y, z);
		if (block.maxX < 1.0) lightB = lightT;
		if (state.getEmittance() > 0) lightB = 1.0f;
		
		sample = state.getTextureForIndex(blockView, x, y, z, 5, overlayIndex);
		if (sample != null) {
			texture = block.getTextureForSide(blockView, x, y, z, 5);
			if (texture < 0) sample.setMirrorU(true);
			lightB = Math.max(lightB * 0.6f, sample.getLight());
			cr00 = lightB; cg00 = lightB; cb00 = lightB;
			renderPosXFace(block, x, y, z, sample);
			sample.setMirrorU(false);
			result = true;
		}
		
		return result;
	}
	
	private boolean renderLadder(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex);
		if (sample == null) return false;
		
		Vec2F u1v1 = sample.getUV(0, 0, uvCache.get());
		Vec2F u1v2 = sample.getUV(0, 1, uvCache.get());
		Vec2F u2v1 = sample.getUV(1, 0, uvCache.get());
		Vec2F u2v2 = sample.getUV(1, 1, uvCache.get());
		
		float light = block.getBrightness(blockView, x, y, z);
		light = Math.max(light, sample.getLight());
		tessellator.color(light, light, light);
		
		int meta = state.getMeta();
		if (breaking) {
			u1v1.set(0, 0);
			u1v2.set(0, 1);
			u2v1.set(1, 0);
			u2v2.set(1, 1);
		}
		
		switch (meta) {
			case 5 -> {
				tessellator.vertex(x + 0.05, y + 1, z + 1, u1v1.x, u1v1.y);
				tessellator.vertex(x + 0.05, y, z + 1, u1v2.x, u1v2.y);
				tessellator.vertex(x + 0.05, y, z, u2v2.x, u2v2.y);
				tessellator.vertex(x + 0.05, y + 1, z, u2v1.x, u2v1.y);
				return true;
			}
			case 4 -> {
				tessellator.vertex(x + 0.95, y, z + 1, u2v2.x, u2v2.y);
				tessellator.vertex(x + 0.95, y + 1, z + 1, u2v1.x, u2v1.y);
				tessellator.vertex(x + 0.95, y + 1, z, u1v1.x, u1v1.y);
				tessellator.vertex(x + 0.95, y, z, u1v2.x, u1v2.y);
				return true;
			}
			case 3 -> {
				tessellator.vertex(x + 1, y, z + 0.05, u2v2.x, u2v2.y);
				tessellator.vertex(x + 1, y + 1, z + 0.05, u2v1.x, u2v1.y);
				tessellator.vertex(x, y + 1, z + 0.05, u1v1.x, u1v1.y);
				tessellator.vertex(x, y, z + 0.05, u1v2.x, u1v2.y);
				return true;
			}
			case 2 -> {
				tessellator.vertex(x + 1, y + 1, z + 0.95, u1v1.x, u1v1.y);
				tessellator.vertex(x + 1, y, z + 0.95, u1v2.x, u1v2.y);
				tessellator.vertex(x, y, z + 0.95, u2v2.x, u2v2.y);
				tessellator.vertex(x, y + 1, z + 0.95, u2v1.x, u2v1.y);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean renderRails(BlockState state, int x, int y, int z) {
		int meta = state.getMeta();
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, meta, overlayIndex);
		if (sample == null) return false;
		
		Tessellator tessellator = Tessellator.INSTANCE;
		RailBlock block = (RailBlock) state.getBlock();
		
		if (block.wrapMeta()) {
			meta &= 7;
		}
		
		float light = block.getBrightness(blockView, x, y, z);
		light = Math.max(light, sample.getLight());
		tessellator.color(light, light, light);
		
		Vec2F u1v1 = sample.getUV(0, 0, uvCache.get());
		Vec2F u1v2 = sample.getUV(0, 1, uvCache.get());
		Vec2F u2v1 = sample.getUV(1, 0, uvCache.get());
		Vec2F u2v2 = sample.getUV(1, 1, uvCache.get());
		
		if (breaking) {
			u1v1.set(0, 0);
			u1v2.set(0, 1);
			u2v1.set(1, 0);
			u2v2.set(1, 1);
		}
		
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
		
		tessellator.vertex(x1, y1, z1, u2v1.x, u2v1.y);
		tessellator.vertex(x2, y2, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x3, y3, z3, u1v2.x, u1v2.y);
		tessellator.vertex(x4, y4, z4, u1v1.x, u1v1.y);
		tessellator.vertex(x4, y4, z4, u1v1.x, u1v1.y);
		tessellator.vertex(x3, y3, z3, u1v2.x, u1v2.y);
		tessellator.vertex(x2, y2, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x1, y1, z1, u2v1.x, u2v1.y);
		
		return true;
	}
	
	private boolean renderStairs(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		boolean result = false;
		
		int meta = state.getMeta();
		if (meta == 0) {
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 1.0f);
			result = renderFullCube(state, x, y, z) | result;
			block.setBoundingBox(0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
			result = renderFullCube(state, x, y, z) | result;
		}
		else if (meta == 1) {
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f);
			result = renderFullCube(state, x, y, z) | result;
			block.setBoundingBox(0.5f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f);
			result = renderFullCube(state, x, y, z) | result;
		}
		else if (meta == 2) {
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 0.5f);
			result = renderFullCube(state, x, y, z) | result;
			block.setBoundingBox(0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f);
			result = renderFullCube(state, x, y, z) | result;
		}
		else if (meta == 3) {
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
			result = renderFullCube(state, x, y, z) | result;
			block.setBoundingBox(0.0f, 0.0f, 0.5f, 1.0f, 0.5f, 1.0f);
			result = renderFullCube(state, x, y, z) | result;
		}
		
		block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		return result;
	}
	
	private boolean renderFence(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		block.setBoundingBox(0.375f, 0.0f, 0.375f, 0.625f, 1.0f, 0.625f);
		boolean result = renderFullCube(state, x, y, z);
		
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
			result = renderFullCube(state, x, y, z) | result;
			block.setBoundingBox(dx1, 0.375f, 0.4375f, dx2, 0.5625f, 0.5625f);
			result = renderFullCube(state, x, y, z) | result;
		}
		
		if (cz) {
			block.setBoundingBox(0.4375f, 0.75f, dz1, 0.5625f, 0.9375f, dz2);
			result = renderFullCube(state, x, y, z) | result;
			block.setBoundingBox(0.4375f, 0.375f, dz1, 0.5625f, 0.5625f, dz2);
			result = renderFullCube(state, x, y, z) | result;
		}
		
		block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		return result;
	}
	
	private boolean renderLever(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		
		int meta = state.getMeta();
		int wrappedMeta = meta & 7;
		boolean isActive = (meta & 8) > 0;
		
		switch (wrappedMeta) {
			case 1 -> block.setBoundingBox(0.0f, 0.5f - 0.25f, 0.5f - 0.1875f, 0.1875f, 0.5f + 0.25f, 0.5f + 0.1875f);
			case 2 -> block.setBoundingBox(1.0f - 0.1875f, 0.5f - 0.25f, 0.5f - 0.1875f, 1.0f, 0.5f + 0.25f, 0.5f + 0.1875f);
			case 3 -> block.setBoundingBox(0.5f - 0.1875f, 0.5f - 0.25f, 0.0f, 0.5f + 0.1875f, 0.5f + 0.25f, 0.1875f);
			case 4 -> block.setBoundingBox(0.5f - 0.1875f, 0.5f - 0.25f, 1.0f - 0.1875f, 0.5f + 0.1875f, 0.5f + 0.25f, 1.0f);
			case 5 -> block.setBoundingBox(0.5f - 0.1875f, 0.0f, 0.5f - 0.25f, 0.5f + 0.1875f, 0.1875f, 0.5f + 0.25f);
			case 6 -> block.setBoundingBox(0.5f - 0.25f, 0.0f, 0.5f - 0.1875f, 0.5f + 0.25f, 0.1875f, 0.5f + 0.1875f);
		}
		
		boolean result = renderFullCube(state, x, y, z);
		
		Tessellator tessellator = Tessellator.INSTANCE;
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 6, overlayIndex);
		if (sample == null) return result;
		
		float light = block.getBrightness(blockView, x, y, z);
		if (state.getEmittance() > 0) light = 1.0f;
		light = Math.max(light, sample.getLight());
		
		Vec2F u1v1 = sample.getUV(0, 0, uvCache.get());
		Vec2F u1v2 = sample.getUV(0, 1, uvCache.get());
		Vec2F u2v1 = sample.getUV(1, 0, uvCache.get());
		Vec2F u2v2 = sample.getUV(1, 1, uvCache.get());
		tessellator.color(light, light, light);
		
		if (breaking) {
			u1v1.set(0, 0);
			u1v2.set(0, 1);
			u2v1.set(1, 0);
			u2v2.set(1, 1);
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
				u1v1 = sample.getUV(7F / 16F, 6F / 16F, u1v1);
				u1v2 = sample.getUV(7F / 16F, 8F / 16F, u1v2);
				u2v1 = sample.getUV(9F / 16F, 6F / 16F, u2v1);
				u2v2 = sample.getUV(9F / 16F, 8F / 16F, u2v2);
			}
			else if (side == 2) {
				u1v1 = sample.getUV(7F / 16F, 6F / 16F, u1v1);
				u1v2 = sample.getUV(7F / 16F, 1, u1v2);
				u2v1 = sample.getUV(9F / 16F, 6F / 16F, u2v1);
				u2v2 = sample.getUV(9F / 16F, 1, u2v2);
			}
			
			switch (side) {
				case 0 -> {
					p1 = points[0];
					p2 = points[1];
					p3 = points[2];
					p4 = points[3];
				}
				case 1 -> {
					p1 = points[7];
					p2 = points[6];
					p3 = points[5];
					p4 = points[4];
				}
				case 2 -> {
					p1 = points[1];
					p2 = points[0];
					p3 = points[4];
					p4 = points[5];
				}
				case 3 -> {
					p1 = points[2];
					p2 = points[1];
					p3 = points[5];
					p4 = points[6];
				}
				case 4 -> {
					p1 = points[3];
					p2 = points[2];
					p3 = points[6];
					p4 = points[7];
				}
				case 5 -> {
					p1 = points[0];
					p2 = points[3];
					p3 = points[7];
					p4 = points[4];
				}
			}
			
			tessellator.vertex(p1.x, p1.y, p1.z, u1v2.x, u1v2.y);
			tessellator.vertex(p2.x, p2.y, p2.z, u2v2.x, u2v2.y);
			tessellator.vertex(p3.x, p3.y, p3.z, u2v1.x, u2v1.y);
			tessellator.vertex(p4.x, p4.y, p4.z, u1v1.x, u1v1.y);
		}
		
		return true;
	}
	
	private boolean renderCactus(BlockState state, int i, int j, int k) {
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
	
	private boolean renderCactusBlock(BlockState state, BaseBlock block, int x, int y, int z, float r, float g, float b) {
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
		
		TextureSample sample;
		if (renderAllSides || state.isSideRendered(blockView, x, y - 1, z, BlockDirection.NEG_Y)) {
			sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.NEG_Y.getFacing(), overlayIndex);
			if (sample != null) {
				light = getBrightness(block, x, y - 1, z);
				light = Math.max(light, sample.getLight());
				cr00 = r1 * light; cg00 = g1 * light; cb00 = b1 * light;
				renderNegYFace(block, x, y, z, sample);
				result = true;
			}
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x, y + 1, z, BlockDirection.POS_Y)) {
			sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.POS_Y.getFacing(), overlayIndex);
			if (sample != null) {
				light = getBrightness(block, x, y + 1, z);
				if (block.maxY != 1.0 && !block.material.isLiquid()) light = blockLight;
				light = Math.max(light, sample.getLight());
				cr00 = r1 * light; cg00 = g1 * light; cb00 = b1 * light;
				renderPosYFace(block, x, y, z, sample);
				result = true;
			}
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x, y, z - 1, BlockDirection.NEG_Z)) {
			sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.NEG_Z.getFacing(), overlayIndex);
			if (sample != null) {
				light = getBrightness(block, x, y, z - 1);
				if (block.minZ > 0.0) light = blockLight;
				light = Math.max(light, sample.getLight());
				cr00 = r2 * light; cg00 = g2 * light; cb00 = b2 * light;
				tessellator.addOffset(0.0f, 0.0f, 0.0625f);
				renderNegZFace(block, x, y, z, sample);
				tessellator.addOffset(0.0f, 0.0f, -0.0625f);
				result = true;
			}
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x, y, z + 1, BlockDirection.POS_Z)) {
			sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.POS_Z.getFacing(), overlayIndex);
			if (sample != null) {
				light = getBrightness(block, x, y, z + 1);
				if (block.maxZ < 1.0) light = blockLight;
				light = Math.max(light, sample.getLight());
				cr00 = r2 * light; cg00 = g2 * light; cb00 = b2 * light;
				tessellator.addOffset(0.0f, 0.0f, -0.0625f);
				renderPosZFace(block, x, y, z, sample);
				tessellator.addOffset(0.0f, 0.0f, 0.0625f);
				result = true;
			}
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x - 1, y, z, BlockDirection.NEG_X)) {
			sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.NEG_X.getFacing(), overlayIndex);
			if (sample != null) {
				light = getBrightness(block, x - 1, y, z);
				if (block.minX > 0.0) light = blockLight;
				light = Math.max(light, sample.getLight());
				cr00 = r3 * light; cg00 = g3 * light; cb00 = b3 * light;
				tessellator.addOffset(0.0625f, 0.0f, 0.0f);
				renderNegXFace(block, x, y, z, sample);
				tessellator.addOffset(-0.0625f, 0.0f, 0.0f);
				result = true;
			}
		}
		
		if (renderAllSides || state.isSideRendered(blockView, x + 1, y, z, BlockDirection.POS_X)) {
			sample = state.getTextureForIndex(blockView, x, y, z, BlockDirection.POS_X.getFacing(), overlayIndex);
			if (sample != null) {
				light = getBrightness(block, x + 1, y, z);
				if (block.maxX < 1.0) light = blockLight;
				light = Math.max(light, sample.getLight());
				cr00 = r3 * light; cg00 = g3 * light; cb00 = b3 * light;
				tessellator.addOffset(-0.0625f, 0.0f, 0.0f);
				renderPosXFace(block, x, y, z, sample);
				tessellator.addOffset(0.0625f, 0.0f, 0.0f);
				result = true;
			}
		}
		
		return result;
	}
	
	//TODO finish bed rendering
	private boolean renderBed(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		Tessellator tessellator = Tessellator.INSTANCE;
		
		int meta = state.getMeta();
		int facing = BedBlock.orientationOnly(meta);
		boolean isFoot = BedBlock.isFoot(meta);
		
		float light = block.getBrightness(blockView, x, y, z);
		tessellator.color(0.5f * light, 0.5f * light, 0.5f * light);
		
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex);
		float u11 = sample.getU(0);
		float u12 = sample.getU(1);
		float v11 = sample.getV(0);
		float v12 = sample.getV(1);
		
		double x1 = x + block.minX;
		double x2 = x + block.maxX;
		double y2 = y + block.minY + 0.1875;
		double z1 = z + block.minZ;
		double z2 = z + block.maxZ;
		
		tessellator.vertex(x1, y2, z2, u11, v12);
		tessellator.vertex(x1, y2, z1, u11, v11);
		tessellator.vertex(x2, y2, z1, u12, v11);
		tessellator.vertex(x2, y2, z2, u12, v12);
		
		float light2 = block.getBrightness(blockView, x, y + 1, z);
		tessellator.color(light2, light2, light2);
		
		sample = state.getTextureForIndex(blockView, x, y, z, 1, overlayIndex);
		float u21 = sample.getU(0);
		float u22 = sample.getU(1);
		float v21 = sample.getV(0);
		float v22 = sample.getV(1);
		
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
		
		double d22 = (double) x + block.minX;
		double d23 = (double) x + block.maxX;
		double d24 = (double) y + block.maxY;
		double d25 = (double) z + block.minZ;
		double d26 = (double) z + block.maxZ;
		
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
		
		if (magic != 2 && (renderAllSides || state.isSideRendered(blockView, x, y, z - 1, BlockDirection.NEG_Z))) {
			float f19 = block.getBrightness(blockView, x, y, z - 1);
			if (block.minZ > 0.0) {
				f19 = light;
			}
			tessellator.color(0.8f * f19, 0.8f * f19, 0.8f * f19);
			sample = state.getTextureForIndex(blockView, x, y, z, 2, overlayIndex);
			sample.setMirrorU(face == 2);
			renderNegZFace(block, x, y, z, sample);
		}
		
		if (magic != 3 && (renderAllSides || state.isSideRendered(blockView, x, y, z + 1, BlockDirection.POS_Z))) {
			float f20 = block.getBrightness(blockView, x, y, z + 1);
			if (block.maxZ < 1.0) {
				f20 = light;
			}
			tessellator.color(0.8f * f20, 0.8f * f20, 0.8f * f20);
			sample = state.getTextureForIndex(blockView, x, y, z, 3, overlayIndex);
			sample.setMirrorU(face == 3);
			renderPosZFace(block, x, y, z, sample);
		}
		
		if (magic != 4 && (renderAllSides || state.isSideRendered(blockView, x - 1, y, z, BlockDirection.NEG_X))) {
			float f21 = block.getBrightness(blockView, x - 1, y, z);
			if (block.minX > 0.0) {
				f21 = light;
			}
			tessellator.color(0.6f * f21, 0.6f * f21, 0.6f * f21);
			sample = state.getTextureForIndex(blockView, x, y, z, 3, overlayIndex);
			sample.setMirrorU(face == 4);
			renderNegXFace(block, x, y, z, sample);
		}
		
		if (magic != 5 && (renderAllSides || state.isSideRendered(blockView, x + 1, y, z, BlockDirection.POS_X))) {
			float f22 = block.getBrightness(blockView, x + 1, y, z);
			if (block.maxX < 1.0) {
				f22 = light;
			}
			tessellator.color(0.6f * f22, 0.6f * f22, 0.6f * f22);
			sample = state.getTextureForIndex(blockView, x, y, z, 3, overlayIndex);
			sample.setMirrorU(face == 5);
			renderPosXFace(block, x, y, z, sample);
		}
		
		return true;
	}
	
	private boolean renderRedstoneRepeater(BlockState state, int x, int y, int z) {
		BaseBlock block = state.getBlock();
		int meta = state.getMeta();
		int wrappedMeta = meta & 3;
		int powered = (meta & 0xC) >> 2;
		renderFullCube(state, x, y, z);
		Tessellator tessellator = Tessellator.INSTANCE;
		
		float light = block.getBrightness(blockView, x, y, z);
		if (state.getEmittance() > 0) light = (light + 1.0f) * 0.5f;
		
		double dy = -0.1875;
		double dx = 0.0;
		double dz = 0.0;
		double d4 = 0.0;
		double d5 = 0.0;
		
		switch (wrappedMeta) {
			case 0 -> {
				d5 = -0.3125;
				dz = RedstoneRepeaterBlock.renderOffset[powered];
			}
			case 2 -> {
				d5 = 0.3125;
				dz = -RedstoneRepeaterBlock.renderOffset[powered];
			}
			case 3 -> {
				d4 = -0.3125;
				dx = RedstoneRepeaterBlock.renderOffset[powered];
			}
			case 1 -> {
				d4 = 0.3125;
				dx = -RedstoneRepeaterBlock.renderOffset[powered];
			}
		}
		
		boolean result = false;
		TextureSample sample = state.getTextureForIndex(blockView, x, y, z, 6, overlayIndex);
		if (sample != null) {
			float light2 = Math.max(light, sample.getLight());
			tessellator.color(light2, light2, light2);
			renderTorchSkewed(x + dx, y + dy, z + dz, 0.0, 0.0, sample);
			renderTorchSkewed(x + d4, y + dy, z + d5, 0.0, 0.0, sample);
			result = true;
		}
		
		sample = state.getTextureForIndex(blockView, x, y, z, 0, overlayIndex);
		if (sample == null) return result;
		
		light = Math.max(light, sample.getLight());
		tessellator.color(light, light, light);
		Vec2F u1v1 = sample.getUV(0, 0, uvCache.get());
		Vec2F u1v2 = sample.getUV(0, 1, uvCache.get());
		Vec2F u2v1 = sample.getUV(1, 0, uvCache.get());
		Vec2F u2v2 = sample.getUV(1, 1, uvCache.get());
		
		double x1 = x + 1;
		double x2 = x1;
		double x3 = x;
		double x4 = x;
		double z1 = z;
		double z2 = z + 1;
		double z3 = z2;
		double z4 = z;
		
		float y1 = (float) y + 0.125f;
		
		if (wrappedMeta == 2) {
			x1 = x2 = x;
			x3 = x4 = x + 1;
			z1 = z4 = z + 1;
			z2 = z3 = z;
		}
		else if (wrappedMeta == 3) {
			x1 = x4 = x;
			x2 = x3 = x + 1;
			z1 = z2 = z;
			z3 = z4 = z + 1;
		}
		else if (wrappedMeta == 1) {
			x1 = x4 = x + 1;
			x2 = x3 = x;
			z1 = z2 = z + 1;
			z3 = z4 = z;
		}
		
		tessellator.vertex(x4, y1, z4, u1v1.x, u1v1.y);
		tessellator.vertex(x3, y1, z3, u1v2.x, u1v2.y);
		tessellator.vertex(x2, y1, z2, u2v2.x, u2v2.y);
		tessellator.vertex(x1, y1, z1, u2v1.x, u2v1.y);
		
		return true;
	}
	
	private boolean renderPiston(BlockState state, int x, int y, int z) {
		return renderPiston(state, x, y, z, false);
	}
	
	private void resetSamplesRotation() {
		for (BlockDirection d: BlockDirection.VALUES) this.rotation.set(d, 0);
	}
	
	private boolean renderPiston(BlockState state, int x, int y, int z, boolean extend) {
		BaseBlock block = state.getBlock();
		int meta = state.getMeta();
		boolean extended = extend || (meta & 8) != 0;
		int rotation = PistonBlock.getRotationByMeta(meta);
		boolean result = false;
		if (extended) {
			resetSamplesRotation();
			forceRotation = true;
			switch (rotation) {
				case 0 -> {
					this.rotation.set(BlockDirection.NEG_Z, 2);
					this.rotation.set(BlockDirection.POS_Z, 2);
					this.rotation.set(BlockDirection.POS_X, 2);
					this.rotation.set(BlockDirection.NEG_X, 2);
					block.setBoundingBox(0.0f, 0.25f, 0.0f, 1.0f, 1.0f, 1.0f);
				}
				case 1 -> block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 0.75f, 1.0f);
				case 2 -> {
					this.rotation.set(BlockDirection.POS_X, 1);
					this.rotation.set(BlockDirection.NEG_X, 3);
					this.rotation.set(BlockDirection.POS_Y, 2);
					block.setBoundingBox(0.0f, 0.0f, 0.25f, 1.0f, 1.0f, 1.0f);
				}
				case 3 -> {
					this.rotation.set(BlockDirection.POS_X, 3);
					this.rotation.set(BlockDirection.NEG_X, 1);
					this.rotation.set(BlockDirection.NEG_Y, 2);
					block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.75f);
				}
				case 4 -> {
					this.rotation.set(BlockDirection.NEG_Z, 1);
					this.rotation.set(BlockDirection.POS_Z, 3);
					this.rotation.set(BlockDirection.POS_Y, 1);
					this.rotation.set(BlockDirection.NEG_Y, 1);
					block.setBoundingBox(0.25f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
				}
				case 5 -> {
					this.rotation.set(BlockDirection.POS_Z, 1);
					this.rotation.set(BlockDirection.POS_Y, 3);
					this.rotation.set(BlockDirection.NEG_Y, 3);
					this.rotation.set(BlockDirection.NEG_Z, 3);
					block.setBoundingBox(0.0f, 0.0f, 0.0f, 0.75f, 1.0f, 1.0f);
				}
			}
			
			result = renderFullCube(state, x, y, z) | result;
			block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
			forceRotation = false;
		}
		else {
			if (!item) {
				resetSamplesRotation();
				forceRotation = true;
				switch (rotation) {
					case 0 -> {
						this.rotation.set(BlockDirection.NEG_Z, 2);
						this.rotation.set(BlockDirection.POS_Z, 2);
						this.rotation.set(BlockDirection.POS_X, 2);
						this.rotation.set(BlockDirection.NEG_X, 2);
					}
					case 2 -> {
						this.rotation.set(BlockDirection.POS_X, 1);
						this.rotation.set(BlockDirection.NEG_X, 3);
						this.rotation.set(BlockDirection.POS_Y, 2);
					}
					case 3 -> {
						this.rotation.set(BlockDirection.POS_X, 3);
						this.rotation.set(BlockDirection.NEG_X, 1);
						this.rotation.set(BlockDirection.NEG_Y, 2);
					}
					case 4 -> {
						this.rotation.set(BlockDirection.NEG_Z, 1);
						this.rotation.set(BlockDirection.POS_Z, 3);
						this.rotation.set(BlockDirection.POS_Y, 1);
						this.rotation.set(BlockDirection.NEG_Y, 1);
					}
					case 5 -> {
						this.rotation.set(BlockDirection.POS_Z, 1);
						this.rotation.set(BlockDirection.POS_Y, 3);
						this.rotation.set(BlockDirection.NEG_Y, 3);
						this.rotation.set(BlockDirection.NEG_Z, 3);
					}
				}
			}
			
			result = renderFullCube(state, x, y, z) | result;
			forceRotation = false;
		}
		
		return result;
	}
	
	private boolean renderPistonHead(BlockState state, int x, int y, int z) {
		return renderPistonHead(state, x, y, z, true);
	}
	
	private boolean renderPistonHead(BlockState state, int x, int y, int z, boolean extended) {
		BaseBlock block = state.getBlock();
		int meta = state.getMeta();
		int offset = PistonHeadBlock.getOffsetIndex(meta);
		float light = block.getBrightness(blockView, x, y, z);
		float delta = extended ? 1.0F : 0.5F;
		float scale = extended ? 16.0F : 8.0F;
		resetSamplesRotation();
		forceRotation = true;
		boolean result = false;
		switch (offset) {
			case 0 -> {
				this.rotation.set(BlockDirection.NEG_Z, 2);
				this.rotation.set(BlockDirection.POS_Z, 2);
				this.rotation.set(BlockDirection.POS_X, 2);
				this.rotation.set(BlockDirection.NEG_X, 2);
				block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 0.25f, 1.0f);
				result = renderFullCube(state, x, y, z) | result;
				renderPistonHead(x + 0.375f, x + 0.625f, y + 0.25f, y + 0.25f + delta, z + 0.625f, z + 0.625f, light * 0.8f, scale, 0);
				renderPistonHead(x + 0.625f, x + 0.375f, y + 0.25f, y + 0.25f + delta, z + 0.375f, z + 0.375f, light * 0.8f, scale, 0);
				renderPistonHead(x + 0.375f, x + 0.375f, y + 0.25f, y + 0.25f + delta, z + 0.375f, z + 0.625f, light * 0.6f, scale, 0);
				renderPistonHead(x + 0.625f, x + 0.625f, y + 0.25f, y + 0.25f + delta, z + 0.625f, z + 0.375f, light * 0.6f, scale, 0);
			}
			case 1 -> {
				block.setBoundingBox(0.0f, 0.75f, 0.0f, 1.0f, 1.0f, 1.0f);
				result = renderFullCube(state, x, y, z) | result;
				renderPistonHead(x + 0.375f, x + 0.625f, y - 0.25f + 1.0f - delta, y - 0.25f + 1.0f, z + 0.625f, z + 0.625f, light * 0.8f, scale, 0);
				renderPistonHead(x + 0.625f, x + 0.375f, y - 0.25f + 1.0f - delta, y - 0.25f + 1.0f, z + 0.375f, z + 0.375f, light * 0.8f, scale, 0);
				renderPistonHead(x + 0.375f, x + 0.375f, y - 0.25f + 1.0f - delta, y - 0.25f + 1.0f, z + 0.375f, z + 0.625f, light * 0.6f, scale, 0);
				renderPistonHead(x + 0.625f, x + 0.625f, y - 0.25f + 1.0f - delta, y - 0.25f + 1.0f, z + 0.625f, z + 0.375f, light * 0.6f, scale, 0);
			}
			case 2 -> {
				this.rotation.set(BlockDirection.POS_X, 1);
				this.rotation.set(BlockDirection.NEG_X, 3);
				this.rotation.set(BlockDirection.POS_Y, 2);
				block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.25f);
				result = renderFullCube(state, x, y, z) | result;
				renderPistonHead(x + 0.375f, x + 0.375f, y + 0.625f, y + 0.375f, z + 0.25f, z + 0.25f + delta, light * 0.6f, scale, 1);
				renderPistonHead(x + 0.625f, x + 0.625f, y + 0.375f, y + 0.625f, z + 0.25f, z + 0.25f + delta, light * 0.6f, scale, 1);
				renderPistonHead(x + 0.375f, x + 0.625f, y + 0.375f, y + 0.375f, z + 0.25f, z + 0.25f + delta, light * 0.5f, scale, 1);
				renderPistonHead(x + 0.625f, x + 0.375f, y + 0.625f, y + 0.625f, z + 0.25f, z + 0.25f + delta, light, scale, 1);
			}
			case 3 -> {
				this.rotation.set(BlockDirection.POS_X, 3);
				this.rotation.set(BlockDirection.NEG_X, 1);
				this.rotation.set(BlockDirection.NEG_Y, 2);
				block.setBoundingBox(0.0f, 0.0f, 0.75f, 1.0f, 1.0f, 1.0f);
				result = renderFullCube(state, x, y, z) | result;
				renderPistonHead(x + 0.375f, x + 0.375f, y + 0.625f, y + 0.375f, z - 0.25f + 1.0f - delta, z - 0.25f + 1.0f, light * 0.6f, scale, 1);
				renderPistonHead(x + 0.625f, x + 0.625f, y + 0.375f, y + 0.625f, z - 0.25f + 1.0f - delta, z - 0.25f + 1.0f, light * 0.6f, scale, 1);
				renderPistonHead(x + 0.375f, x + 0.625f, y + 0.375f, y + 0.375f, z - 0.25f + 1.0f - delta, z - 0.25f + 1.0f, light * 0.5f, scale, 1);
				renderPistonHead(x + 0.625f, x + 0.375f, y + 0.625f, y + 0.625f, z - 0.25f + 1.0f - delta, z - 0.25f + 1.0f, light, scale, 1);
			}
			case 4 -> {
				this.rotation.set(BlockDirection.NEG_Z, 1);
				this.rotation.set(BlockDirection.POS_Z, 3);
				this.rotation.set(BlockDirection.POS_Y, 1);
				this.rotation.set(BlockDirection.NEG_Y, 1);
				block.setBoundingBox(0.0f, 0.0f, 0.0f, 0.25f, 1.0f, 1.0f);
				result = renderFullCube(state, x, y, z) | result;
				renderPistonHead(x + 0.25f, x + 0.25f + delta, y + 0.375f, y + 0.375f, z + 0.625f, z + 0.375f, light * 0.5f, scale, 2);
				renderPistonHead(x + 0.25f, x + 0.25f + delta, y + 0.625f, y + 0.625f, z + 0.375f, z + 0.625f, light, scale, 2);
				renderPistonHead(x + 0.25f, x + 0.25f + delta, y + 0.375f, y + 0.625f, z + 0.375f, z + 0.375f, light * 0.6f, scale, 2);
				renderPistonHead(x + 0.25f, x + 0.25f + delta, y + 0.625f, y + 0.375f, z + 0.625f, z + 0.625f, light * 0.6f, scale, 2);
			}
			case 5 -> {
				this.rotation.set(BlockDirection.NEG_Z, 3);
				this.rotation.set(BlockDirection.POS_Z, 1);
				this.rotation.set(BlockDirection.POS_Y, 3);
				this.rotation.set(BlockDirection.NEG_Y, 3);
				block.setBoundingBox(0.75f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
				result = renderFullCube(state, x, y, z) | result;
				renderPistonHead(x - 0.25f + 1.0f - delta, x - 0.25f + 1.0f, y + 0.375f, y + 0.375f, z + 0.625f, z + 0.375f, light * 0.5f, scale, 2);
				renderPistonHead(x - 0.25f + 1.0f - delta, x - 0.25f + 1.0f, y + 0.625f, y + 0.625f, z + 0.375f, z + 0.625f, light, scale, 2);
				renderPistonHead(x - 0.25f + 1.0f - delta, x - 0.25f + 1.0f, y + 0.375f, y + 0.625f, z + 0.375f, z + 0.375f, light * 0.6f, scale, 2);
				renderPistonHead(x - 0.25f + 1.0f - delta, x - 0.25f + 1.0f, y + 0.625f, y + 0.375f, z + 0.625f, z + 0.625f, light * 0.6f, scale, 2);
			}
		}
		block.setBoundingBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		forceRotation = false;
		return result;
	}
	
	private void renderPistonHead(double x1, double x2, double y1, double y2, double z1, double z2, float light, float delta, int type) {
		Tessellator tessellator = Tessellator.INSTANCE;
		TextureSample sample = Textures.getVanillaBlockSample(108);
		if (sample == null) return;
		sample.setRotation(0);
		Vec2F uv1 = sample.getUV(0, 0, uvCache.get());
		Vec2F uv2 = sample.getUV(delta / 16F, 0.25F, uvCache.get());
		light = Math.max(light, sample.getLight());
		tessellator.color(light, light, light);
		switch (type) {
			case 0 -> {
				tessellator.vertex(x1, y2, z1, uv2.x, uv1.y);
				tessellator.vertex(x1, y1, z1, uv1.x, uv1.y);
				tessellator.vertex(x2, y1, z2, uv1.x, uv2.y);
				tessellator.vertex(x2, y2, z2, uv2.x, uv2.y);
			}
			case 1 -> {
				tessellator.vertex(x1, y1, z2, uv2.x, uv1.y);
				tessellator.vertex(x1, y1, z1, uv1.x, uv1.y);
				tessellator.vertex(x2, y2, z1, uv1.x, uv2.y);
				tessellator.vertex(x2, y2, z2, uv2.x, uv2.y);
			}
			case 2 -> {
				tessellator.vertex(x2, y1, z1, uv2.x, uv1.y);
				tessellator.vertex(x1, y1, z1, uv1.x, uv1.y);
				tessellator.vertex(x1, y2, z2, uv1.x, uv2.y);
				tessellator.vertex(x2, y2, z2, uv2.x, uv2.y);
			}
		}
	}
}
