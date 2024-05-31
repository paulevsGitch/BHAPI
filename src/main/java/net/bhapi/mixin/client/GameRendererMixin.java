package net.bhapi.mixin.client;

import net.bhapi.level.ChunkHeightProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.level.Level;
import net.minecraft.level.biome.Biome;
import net.minecraft.util.maths.MCMath;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow private Minecraft minecraft;
	@Shadow private Random random;
	@Shadow private int randomOffset;
	
	@Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
	private void bhapi_renderWeather(float delta, CallbackInfo info) {
		info.cancel();
		
		float gradient = this.minecraft.level.getRainGradient(delta);
		if (gradient <= 0.0f) return;
		
		float f2;
		int y2;
		short height;
		int dx, dy, dz;
		
		LivingEntity entity = this.minecraft.viewEntity;
		Level level = this.minecraft.level;
		
		int ix = MCMath.floor(entity.x);
		int iy = MCMath.floor(entity.y);
		int iz = MCMath.floor(entity.z);
		
		Tessellator tessellator = Tessellator.INSTANCE;
		GL11.glDisable(2884);
		GL11.glNormal3f(0.0f, 1.0f, 0.0f);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		GL11.glAlphaFunc(516, 0.01f);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textureManager.getTextureId("/environment/snow.png"));
		
		double posX = entity.prevRenderX + (entity.x - entity.prevRenderX) * delta;
		double posY = entity.prevRenderY + (entity.y - entity.prevRenderY) * delta;
		double posZ = entity.prevRenderZ + (entity.z - entity.prevRenderZ) * delta;
		int minY = MCMath.floor(posY);
		
		int distance = 5;
		if (this.minecraft.options.fancyGraphics) {
			distance = 10;
		}
		
		Biome[] baseBiomeArray = level.getBiomeSource().getBiomes(ix - distance, iz - distance, distance * 2 + 1, distance * 2 + 1);
		int index = 0;
		for (dx = ix - distance; dx <= ix + distance; ++dx) {
			for (dz = iz - distance; dz <= iz + distance; ++dz) {
				if (!baseBiomeArray[index++].canSnow()) continue;
				
				height = ChunkHeightProvider.cast(level.getChunk(dx, dz)).getHeightmapData(dx & 15, dz & 15);
				
				if ((dy = height) < minY) dy = minY;
				
				y2 = iy - distance;
				int h = iy + distance;
				if (y2 < height) {
					y2 = height;
				}
				if (h < height) {
					h = height;
				}
				
				f2 = 1.0f;
				if (y2 == h) continue;
				this.random.setSeed(dx * dx * 3121L + dx * 45238971L + dz * dz * 418711L + dz * 13761L);
				float offset = this.randomOffset + delta;
				float f5 = ((this.randomOffset & 0x1FF) + delta) / 512.0F;
				float f6 = this.random.nextFloat() + offset * 0.01f * (float)this.random.nextGaussian();
				float f7 = this.random.nextFloat() + offset * (float) this.random.nextGaussian() * 0.001F;
				double d4 = dx + 0.5 - entity.x;
				double d5 = dz + 0.5 - entity.z;
				float f8 = MCMath.sqrt(d4 * d4 + d5 * d5) / (float)distance;
				
				tessellator.start();
				float light = level.getBrightness(dx, dy, dz);
				GL11.glColor4f(light, light, light, ((1.0f - f8 * f8) * 0.3f + 0.5f) * gradient);
				
				tessellator.setOffset(-posX, -posY, -posZ);
				
				float v1 = y2 * f2 / 4.0f + f5 * f2 + f7;
				float v2 = h * f2 / 4.0f + f5 * f2 + f7;
				
				tessellator.vertex(dx, y2, dz + 0.5, f6, v1);
				tessellator.vertex(dx + 1, y2, dz + 0.5, f2 + f6, v1);
				tessellator.vertex(dx + 1, h, dz + 0.5, f2 + f6, v2);
				tessellator.vertex(dx, h, dz + 0.5, f6, v2);
				tessellator.vertex(dx + 0.5, y2, dz, f6, v1);
				tessellator.vertex(dx + 0.5, y2, dz + 1, f2 + f6, v1);
				tessellator.vertex(dx + 0.5, h, dz + 1, f2 + f6, v2);
				tessellator.vertex(dx + 0.5, h, dz, f6, v2);
				tessellator.setOffset(0.0, 0.0, 0.0);
				tessellator.render();
			}
		}
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textureManager.getTextureId("/environment/rain.png"));
		
		index = 0;
		for (dx = ix - distance; dx <= ix + distance; ++dx) {
			for (dz = iz - distance; dz <= iz + distance; ++dz) {
				if (!baseBiomeArray[index++].canRain()) continue;
				
				height = ChunkHeightProvider.cast(level.getChunk(dx, dz)).getHeightmapData(dx & 15, dz & 15);
				
				dy = iy - distance;
				y2 = iy + distance;
				
				if (dy < height) {
					dy = height;
				}
				if (y2 < height) {
					y2 = height;
				}
				
				float u = 1.0f;
				if (dy == y2) continue;
				this.random.setSeed((long) dx * dx * 3121L + dx * 45238971L + (long) dz * dz * 418711L + dz * 13761L);
				
				f2 = ((this.randomOffset + dx * dx * 3121 + dx * 45238971 + dz * dz * 418711 + dz * 13761 & 0x1F) + delta) / 32.0F * (3.0f + this.random.nextFloat());
				double distX = dx + 0.5 - entity.x;
				double distZ = dz + 0.5 - entity.z;
				
				float length = MCMath.sqrt(distX * distX + distZ * distZ) / distance;
				
				tessellator.start();
				float light = level.getBrightness(dx, dy, dz) * 0.85f + 0.15f;
				GL11.glColor4f(light, light, light, ((1.0f - length * length) * 0.5f + 0.5f) * gradient);
				
				tessellator.setOffset(-posX, -posY, -posZ);
				
				float v1 = dy * u / 4.0f + f2 * u;
				float v2 = y2 * u / 4.0f + f2 * u;
				
				tessellator.vertex(dx, dy, dz + 0.5, 0, v1);
				tessellator.vertex(dx + 1, dy, dz + 0.5, u, v1);
				tessellator.vertex(dx + 1, y2, dz + 0.5, u, v2);
				tessellator.vertex(dx, y2, dz + 0.5, 0, v2);
				tessellator.vertex(dx + 0.5, dy, dz, 0, v1);
				tessellator.vertex(dx + 0.5, dy, dz + 1, u, v1);
				tessellator.vertex(dx + 0.5, y2, dz + 1, u, v2);
				tessellator.vertex(dx + 0.5, y2, dz, 0, v2);
				tessellator.setOffset(0.0, 0.0, 0.0);
				tessellator.render();
			}
		}
		GL11.glEnable(2884);
		GL11.glDisable(3042);
		GL11.glAlphaFunc(516, 0.1f);
	}
}
