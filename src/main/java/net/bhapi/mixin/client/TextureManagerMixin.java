package net.bhapi.mixin.client;

import net.bhapi.client.render.texture.TextureAtlas;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.util.BufferUtil;
import net.minecraft.client.TexturePackManager;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.util.List;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
	@Shadow private List textureBinders;
	@Shadow private GameOptions gameOptions;
	@Shadow private ByteBuffer currentImageBuffer;
	@Shadow public static boolean field_1245;
	
	@Shadow protected abstract int hashInts(int i, int j);
	
	@Unique private static final String[] MASKS = new String[] {
		"/terrain.png",
		"/gui/items.png",
		"/particles.png",
	};
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bhapi_onTextureManagerInit(TexturePackManager manager, GameOptions options, CallbackInfo info) {
		currentImageBuffer = BufferUtil.createByteBuffer(4096 * 4096 * 4); // 4k texture
	}
	
	@Inject(method = "getTextureId", at = @At("HEAD"), cancellable = true)
	private void bhapi_getTextureId(String name, CallbackInfoReturnable<Integer> info) {
		TextureAtlas atlas = Textures.getAtlas();
		if (atlas == null) return;
		for (String mask: MASKS) {
			if (mask.equals(name)) {
				info.setReturnValue(atlas.getGlTarget());
				return;
			}
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void bhapi_tick(CallbackInfo info) {
		info.cancel();
		
		int n, n2, n3, py, px, side, preSide, level, y, x, index;
		TextureBinder binder;
		
		TextureAtlas atlas = Textures.getAtlas();
		atlas.bind();
		
		for (index = 0; index < this.textureBinders.size(); ++index) {
			binder = (TextureBinder) this.textureBinders.get(index);
			binder.render3d = this.gameOptions.anaglyph3d;
			binder.update();
			
			this.currentImageBuffer.clear();
			this.currentImageBuffer.put(binder.grid);
			this.currentImageBuffer.position(0);//.limit(binder.grid.length);
			
			for (x = 0; x < binder.textureSize; ++x) {
				for (y = 0; y < binder.textureSize; ++y) {
					UVPair uv = atlas.getUV(binder.index);
					
					GL11.glTexSubImage2D(
						GL11.GL_TEXTURE_2D, 0,
						uv.getX(), uv.getY(),
						uv.getWidth(), uv.getHeight(),
						GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer
					);
					
					if (!field_1245) continue;
					
					// Is this some sort of noise generator?
					// Ported but disabled
					for (level = 1; level <= 4; ++level) {
						//preSide = uv.getWidth() >> level - 1;
						side = uv.getWidth() >> level;
						/*for (px = 0; px < side; ++px) {
							for (py = 0; py < side; ++py) {
								n3 = this.currentImageBuffer.getInt((px * 2 + (py * 2) * preSide) * 4);
								n2 = this.currentImageBuffer.getInt((px * 2 + 1 + (py * 2) * preSide) * 4);
								n = this.currentImageBuffer.getInt((px * 2 + 1 + (py * 2 + 1) * preSide) * 4);
								int n12 = this.currentImageBuffer.getInt((px * 2 + (py * 2 + 1) * preSide) * 4);
								int n13 = this.hashInts(this.hashInts(n3, n2), this.hashInts(n, n12));
								this.currentImageBuffer.putInt((px + py * side) * 4, n13);
							}
						}*/
						
						GL11.glTexSubImage2D(
							GL11.GL_TEXTURE_2D, level,
							uv.getX() * side, uv.getY() * side,
							side, side,
							GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer
						);
					}
				}
			}
		}
		
		// Repeating?
		/*for (index = 0; index < this.textureBinders.size(); ++index) {
			binder = (TextureBinder) this.textureBinders.get(index);
			if (binder.id <= 0) continue;
			this.currentImageBuffer.clear();
			this.currentImageBuffer.put(binder.grid);
			this.currentImageBuffer.position(0).limit(binder.grid.length);
			GL11.glBindTexture(3553, binder.id);
			GL11.glTexSubImage2D(3553, 0, 0, 0, 16, 16, 6408, 5121, this.currentImageBuffer);
			if (!field_1245) continue;
			for (x = 1; x <= 4; ++x) {
				y = 16 >> x - 1;
				level = 16 >> x;
				for (preSide = 0; preSide < level; ++preSide) {
					for (side = 0; side < level; ++side) {
						px = this.currentImageBuffer.getInt((preSide * 2 + (side * 2) * y) * 4);
						py = this.currentImageBuffer.getInt((preSide * 2 + 1 + (side * 2) * y) * 4);
						n3 = this.currentImageBuffer.getInt((preSide * 2 + 1 + (side * 2 + 1) * y) * 4);
						n2 = this.currentImageBuffer.getInt((preSide * 2 + (side * 2 + 1) * y) * 4);
						n = this.hashInts(this.hashInts(px, py), this.hashInts(n3, n2));
						this.currentImageBuffer.putInt((preSide + side * level) * 4, n);
					}
				}
				GL11.glTexSubImage2D(3553, x, 0, 0, level, level, 6408, 5121, this.currentImageBuffer);
			}
		}*/
	}
}
