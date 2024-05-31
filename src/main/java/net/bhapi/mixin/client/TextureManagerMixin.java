package net.bhapi.mixin.client;

import net.bhapi.BHAPI;
import net.bhapi.client.ClientRegistries;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.bhapi.client.render.texture.TextureAtlas;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.client.render.texture.UVPair;
import net.bhapi.util.BufferUtil;
import net.bhapi.util.ImageUtil;
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

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;

@SuppressWarnings("rawtypes")
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
			
			UVPair uv = atlas.getUV(binder.index);
			
			binder.update();
			
			if (currentImageBuffer.capacity() < binder.grid.length) {
				currentImageBuffer = BufferUtil.createByteBuffer(binder.grid.length);
			}
			
			this.currentImageBuffer.clear();
			this.currentImageBuffer.put(binder.grid);
			this.currentImageBuffer.position(0);
			
			for (x = 0; x < binder.textureSize; ++x) {
				for (y = 0; y < binder.textureSize; ++y) {
					int width = uv.getWidth() / binder.textureSize;
					int height = uv.getHeight() / binder.textureSize;
					GL11.glTexSubImage2D(
						GL11.GL_TEXTURE_2D, 0,
						uv.getX() + x * width,
						uv.getY() + y * height,
						width, height,
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
		BHBlockRenderer.clearItemCache();
		Textures.preReload();
		BHAPI.processEntryPoints("bhapi:client_events", ClientRegistries.EVENT_REGISTRY_RELOAD);
		Textures.reload();
		ImageUtil.processAnimations();
	}
	
	@Inject(method = "bindImage(Ljava/awt/image/BufferedImage;I)V", at = @At("HEAD"))
	private void bhapi_bindImage(BufferedImage image, int target, CallbackInfo info) {
		int capacity = image.getWidth() * image.getHeight() * 4;
		if (currentImageBuffer.capacity() < capacity) {
			currentImageBuffer = BufferUtil.createByteBuffer(capacity);
		}
	}
}
