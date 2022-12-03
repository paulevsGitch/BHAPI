package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.level.BlockStateProvider;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.client.render.AreaRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.BlockEntityRenderDispatcher;
import net.minecraft.level.Level;
import net.minecraft.level.LevelPopulationRegion;
import net.minecraft.level.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;

@Mixin(AreaRenderer.class)
public abstract class AreaRendererMixin {
	@Shadow public boolean canUpdate;
	@Shadow public static int globalID;
	@Shadow public int startX;
	@Shadow public int startY;
	@Shadow public int startZ;
	@Shadow public int sideX;
	@Shadow public int sideY;
	@Shadow public int sideZ;
	@Shadow public boolean[] layerIsEmpty;
	@Shadow public List skipBlockEntities;
	@Shadow public Level level;
	@Shadow private int glListID;
	@Shadow private static Tessellator tesselator;
	@Shadow private List blockEntities;
	@Shadow public boolean hasSkyLight;
	@Shadow private boolean hasData;
	
	@Shadow protected abstract void offset();
	
	@Unique private final BHBlockRenderer bhapi_renderer = new BHBlockRenderer();
	
	@SuppressWarnings("all")
	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	private void bhapi_update(CallbackInfo info) {
		info.cancel();
		
		if (!this.canUpdate) {
			return;
		}
		
		++globalID;
		int x1 = this.startX;
		int y1 = this.startY;
		int z1 = this.startZ;
		int x2 = this.startX + this.sideX;
		int y2 = this.startY + this.sideY;
		int z2 = this.startZ + this.sideZ;
		
		for (int i = 0; i < 2; ++i) {
			this.layerIsEmpty[i] = true;
		}
		
		Chunk.hasSkyLight = false;
		HashSet hashSet = new HashSet();
		hashSet.addAll(this.skipBlockEntities);
		this.skipBlockEntities.clear();
		
		LevelPopulationRegion region = new LevelPopulationRegion(this.level, x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
		BlockStateProvider provider = BlockStateProvider.cast(region);
		bhapi_renderer.setView(region);
		
		for (byte pass = 0; pass < 2; ++pass) {
			boolean anotherPass = false;
			boolean layerHasData = false;
			boolean renderStarted = false;
			for (int py = y1; py < y2; ++py) {
				for (int pz = z1; pz < z2; ++pz) {
					for (int px = x1; px < x2; ++px) {
						BaseBlockEntity baseBlockEntity;
						BlockState state = provider.getBlockState(px, py, pz);
						
						if (!renderStarted) {
							renderStarted = true;
							GL11.glNewList(this.glListID + pass, 4864);
							GL11.glPushMatrix();
							this.offset();
							float f = 1.000001f;
							GL11.glTranslatef((float) (-this.sideZ) / 2.0f, (float) (-this.sideY) / 2.0f, (float) (-this.sideZ) / 2.0f);
							GL11.glScalef(f, f, f);
							GL11.glTranslatef((float) this.sideZ / 2.0f, (float) this.sideY / 2.0f, (float) this.sideZ / 2.0f);
							tesselator.start();
							tesselator.setOffset(-this.startX, -this.startY, -this.startZ);
						}
						
						if (pass == 0 && state.hasBlockEntity() && BlockEntityRenderDispatcher.INSTANCE.hasCustomRenderer(baseBlockEntity = region.getBlockEntity(px, py, pz))) {
							this.skipBlockEntities.add(baseBlockEntity);
						}
						
						BaseBlock baseBlock = state.getBlock();
						if (baseBlock.getRenderPass() != pass) {
							anotherPass = true;
							continue;
						}
						
						layerHasData |= bhapi_renderer.render(state, px, py, pz);
					}
				}
			}
			
			if (renderStarted) {
				tesselator.draw();
				GL11.glPopMatrix();
				GL11.glEndList();
				tesselator.setOffset(0.0, 0.0, 0.0);
			}
			else {
				layerHasData = false;
			}
			
			if (layerHasData) {
				this.layerIsEmpty[pass] = false;
			}
			
			if (!anotherPass) break;
		}
		
		HashSet<BaseBlockEntity> skipEntities = new HashSet<>();
		skipEntities.addAll(this.skipBlockEntities);
		skipEntities.removeAll(hashSet);
		
		this.blockEntities.addAll(skipEntities);
		hashSet.removeAll(this.skipBlockEntities);
		this.blockEntities.removeAll(hashSet);
		
		this.hasSkyLight = Chunk.hasSkyLight;
		this.hasData = true;
	}
}
