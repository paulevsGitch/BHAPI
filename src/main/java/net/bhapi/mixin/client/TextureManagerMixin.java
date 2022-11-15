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
		if (Textures.isBuilding()) return;
		TextureAtlas atlas = Textures.getAtlas();
		for (String mask: MASKS) {
			if (mask.equals(name)) {
				info.setReturnValue(atlas.getGlTarget());
				return;
			}
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void bhapi_tick(CallbackInfo info) {
		info.cancel();
		
		int side, level, y, x, index;
		TextureBinder binder;
		
		TextureAtlas atlas = Textures.getAtlas();
		atlas.bind();
		
		for (index = 0; index < this.textureBinders.size(); ++index) {
			binder = (TextureBinder) this.textureBinders.get(index);
			binder.render3d = this.gameOptions.anaglyph3d;
			binder.update();
			
			this.currentImageBuffer.clear();
			this.currentImageBuffer.put(binder.grid);
			this.currentImageBuffer.position(0);
			
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
					
					for (level = 1; level <= 4; ++level) {
						side = uv.getWidth() >> level;
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
	}
	
	@Inject(method = "reloadTexturesFromTexturePack", at = @At("RETURN"))
	private void bhapi_reloadTexturesFromTexturePack(CallbackInfo info) {
		Textures.reload();
	}
}
