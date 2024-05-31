package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.TextureSampleProvider;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.registry.CommonRegistries;
import net.minecraft.block.Block;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.particle.DiggingParticle;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.technical.ParticleEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.MCMath;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("rawtypes")
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
	@Shadow protected Level level;
	@Shadow private Random rand;
	@Shadow private List[] renderLayers;
	@Shadow private TextureManager textureManager;
	
	@Shadow public abstract void addParticle(ParticleEntity arg);
	
	@Unique private final List<ParticleEntity> bhapi_particles = new ArrayList<>(4096);
	@Unique private final List<ParticleEntity> bhapi_items = new ArrayList<>(16);
	@Unique private byte bhapi_sortTicks;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onParticleManagerInit(Level level, TextureManager manager, CallbackInfo info) {
		this.renderLayers = null;
		this.textureManager = null;
	}
	
	@Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
	private void bhapi_addParticle(ParticleEntity particle, CallbackInfo info) {
		info.cancel();
		if (particle.getRenderType() == 3) {
			synchronized (bhapi_items) {
				if (bhapi_items.size() >= 15) {
					bhapi_items.remove(0);
				}
				bhapi_items.add(particle);
			}
		}
		else {
			synchronized (bhapi_particles) {
				if (bhapi_particles.size() >= 4095) {
					bhapi_particles.remove(0);
				}
				bhapi_particles.add(particle);
			}
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void bhapi_tick(CallbackInfo info) {
		info.cancel();
		
		for (int i = 0; i < bhapi_particles.size(); i++) {
			ParticleEntity particle = bhapi_particles.get(i);
			particle.tick();
			if (particle.removed) {
				bhapi_particles.remove(i--);
			}
		}
		
		for (int i = 0; i < bhapi_items.size(); i++) {
			ParticleEntity particle = bhapi_items.get(i);
			particle.tick();
			if (particle.removed) {
				bhapi_items.remove(i--);
			}
		}
	}
	
	@Inject(method = "renderAll", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderAll(Entity entity, float delta, CallbackInfo info) {
		info.cancel();
		synchronized (bhapi_particles) {
			if (bhapi_particles.isEmpty()) return;
			
			float coef = (float) Math.PI / 180.0F;
			float dx = MCMath.cos(entity.yaw * coef);
			float dz = MCMath.sin(entity.yaw * coef);
			float width = -dz * MCMath.sin(entity.pitch * coef);
			float height = dx * MCMath.sin(entity.pitch * coef);
			float dy = MCMath.cos(entity.pitch * coef);
			
			ParticleEntity.posX = entity.prevRenderX + (entity.x - entity.prevRenderX) * delta;
			ParticleEntity.posY = entity.prevRenderY + (entity.y - entity.prevRenderY) * delta;
			ParticleEntity.posZ = entity.prevRenderZ + (entity.z - entity.prevRenderZ) * delta;
			
			if (bhapi_sortTicks++ > 4) {
				bhapi_sortTicks = 0;
				bhapi_particles.sort((p1, p2) -> {
					double d1 = p1.distanceToSqr(entity);
					double d2 = p2.distanceToSqr(entity);
					return Double.compare(d2, d1);
				});
			}
			
			Textures.getAtlas().bind();
			Tessellator tessellator = Tessellator.INSTANCE;
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			tessellator.start();
			for (ParticleEntity particle : bhapi_particles) {
				particle.render(tessellator, delta, dx, dy, dz, width, height);
			}
			tessellator.render();
			GL11.glDisable(GL11.GL_BLEND);
		}
	}
	
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderPickup(Entity entity, float delta, CallbackInfo info) {
		info.cancel();
		Tessellator tessellator = Tessellator.INSTANCE;
		synchronized (bhapi_items) {
			bhapi_items.forEach(p -> p.render(tessellator, delta, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
		}
	}
	
	@Inject(method = "setLevel", at = @At("HEAD"), cancellable = true)
	private void bhapi_setLevel(Level level, CallbackInfo info) {
		info.cancel();
		this.level = level;
	}
	
	@Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
	private void bhapi_addBlockBreakParticles(int x, int y, int z, int rawID, int meta, CallbackInfo info) {
		info.cancel();
		BlockState state = CommonRegistries.BLOCKSTATES_MAP.get(rawID);
		if (state == null) return;
		for (int dx = 0; dx < 4; ++dx) {
			for (int dy = 0; dy < 4; ++dy) {
				for (int dz = 0; dz < 4; ++dz) {
					double px = x + (dx + 0.5) / 4.0;
					double py = y + (dy + 0.5) / 4.0;
					double pz = z + (dz + 0.5) / 4.0;
					int side = this.rand.nextInt(6);
					DiggingParticle particle = new DiggingParticle(
						this.level,
						px, py, pz,
						px - x - 0.5,
						py - y - 0.5,
						pz - z - 0.5,
						state.getBlock(),
						side, meta
					).applyColor(x, y, z);
					TextureSample sample = state.getTextureForIndex(this.level, x, y, z, side, 0);
					TextureSampleProvider.cast(particle).bhapi_setTextureSample(sample);
					this.addParticle(particle);
				}
			}
		}
	}
	
	@Inject(method = "addBlockClickParticle", at = @At("HEAD"), cancellable = true)
	private void bhapi_addBlockClickParticle(int x, int y, int z, int side, CallbackInfo info) {
		info.cancel();
		BlockState state = BlockStateProvider.cast(this.level).bhapi_getBlockState(x, y, z);
		if (state.isAir()) return;
		
		Block block = state.getBlock();
		
		float scale = 0.1f;
		double px = x + this.rand.nextDouble() * (block.maxX - block.minX - (scale * 2.0F)) + scale + block.minX;
		double py = y + this.rand.nextDouble() * (block.maxY - block.minY - (scale * 2.0F)) + scale + block.minY;
		double pz = z + this.rand.nextDouble() * (block.maxZ - block.minZ - (scale * 2.0F)) + scale + block.minZ;
		
		if (side == 0) py = y + block.minY - scale;
		if (side == 1) py = y + block.maxY + scale;
		if (side == 2) pz = z + block.minZ - scale;
		if (side == 3) pz = z + block.maxZ + scale;
		if (side == 4) px = x + block.minX - scale;
		if (side == 5) px = x + block.maxX + scale;
		
		ParticleEntity particle = new DiggingParticle(
			this.level,
			px, py, pz,
			0.0, 0.0, 0.0,
			block, side, 0
		).applyColor(x, y, z).scaleVelocity(0.2f).scaleSize(0.6f);
		TextureSample sample = state.getTextureForIndex(this.level, x, y, z, side, 0);
		TextureSampleProvider.cast(particle).bhapi_setTextureSample(sample);
		this.addParticle(particle);
	}
	
	@Inject(method = "asString", at = @At("HEAD"), cancellable = true)
	private void bhapi_asString(CallbackInfoReturnable<String> info) {
		info.setReturnValue(bhapi_particles.size() + " " + bhapi_items.size());
	}
}
