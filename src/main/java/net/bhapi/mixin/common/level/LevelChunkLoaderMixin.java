package net.bhapi.mixin.common.level;

import net.bhapi.BHAPI;
import net.minecraft.level.Level;
import net.minecraft.level.LevelManager;
import net.minecraft.level.LevelProperties;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.chunk.LevelChunkLoader;
import net.minecraft.level.storage.RegionLoader;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

@Mixin(LevelChunkLoader.class)
public class LevelChunkLoaderMixin {
	@Shadow @Final private File file;
	
	@Inject(method = "loadChunk", at = @At("HEAD"), cancellable = true)
	private void bhapi_fixLoadChunk(Level level, int x, int z, CallbackInfoReturnable<Chunk> info) {
		DataInputStream stream = RegionLoader.getInputStream(this.file, x, z);
		if (stream == null) {
			info.setReturnValue(null);
			return;
		}
		
		CompoundTag tag = NBTIO.readTag(stream);
		Chunk chunk = LevelManager.loadData(level, tag);
		if (!chunk.equalPosition(x, z)) {
			BHAPI.log("Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + z + ", got " + chunk.x + ", " + chunk.z + ")");
			tag.put("x", x);
			tag.put("z", z);
			chunk = LevelManager.loadData(level, tag);
		}
		
		info.setReturnValue(chunk);
	}
	
	@Inject(method = "saveChunk", at = @At("HEAD"), cancellable = true)
	private void bhapi_fixSaveChunk(Level level, Chunk chunk, CallbackInfo info) {
		info.cancel();
		level.checkSessionLock();
		try {
			DataOutputStream stream = RegionLoader.getOutputStream(this.file, chunk.x, chunk.z);
			CompoundTag tag = new CompoundTag();
			LevelManager.saveData(chunk, level, tag);
			NBTIO.writeTag(tag, stream);
			stream.close();
			LevelProperties properties = level.getProperties();
			properties.setSizeOnDisk(properties.getSizeOnDisk() + (long) RegionLoader.getSizeDelta(this.file, chunk.x, chunk.z));
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
