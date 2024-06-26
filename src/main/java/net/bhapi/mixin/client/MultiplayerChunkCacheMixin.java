package net.bhapi.mixin.client;

import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.chunk.MultiplayerChunkCache;
import net.minecraft.util.maths.Vec2I;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(MultiplayerChunkCache.class)
public class MultiplayerChunkCacheMixin {
	@Shadow private Level level;
	
	@Shadow private Map multiplayerChunkCache;
	
	@Inject(method = "loadChunk", at = @At("HEAD"), cancellable = true)
	private void bhapi_loadChunk(int x, int z, CallbackInfoReturnable<Chunk> info) {
		Vec2I pos = new Vec2I(x, z);
		Chunk chunk = new Chunk(this.level, new byte[0], x, z);
		this.multiplayerChunkCache.put(pos, chunk);
		chunk.canHaveBlockEntities = true;
		info.setReturnValue(chunk);
	}
}
