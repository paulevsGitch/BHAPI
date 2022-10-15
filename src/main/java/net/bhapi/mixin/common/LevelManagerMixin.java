package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.util.NBTSerializable;
import net.minecraft.level.Level;
import net.minecraft.level.LevelManager;
import net.minecraft.level.LevelProperties;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Mixin(LevelManager.class)
public abstract class LevelManagerMixin {
	@Shadow protected abstract File getLevelDat(int i, int j);
	
	@Shadow private File levelFile;
	
	@Inject(method = "loadChunk", at = @At("HEAD"), cancellable = true)
	private void bhapi_loadChunk(Level level, int x, int z, CallbackInfoReturnable<Chunk> info) {
		File file = this.getLevelDat(x, z);
		if (file != null && file.exists()) {
			try {
				FileInputStream stream = new FileInputStream(file);
				CompoundTag tag = NBTIO.readGzipped(stream);
				Chunk chunk = LevelManager.loadData(level, tag);
				if (!chunk.equalPosition(x, z)) {
					BHAPI.log("Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + z + ", got " + chunk.x + ", " + chunk.z + ")");
					tag.put("xPos", x);
					tag.put("zPos", z);
					chunk = LevelManager.loadData(level, tag);
				}
				chunk.setBlockMask();
				info.setReturnValue(chunk);
				return;
			}
			catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		info.setReturnValue(null);
	}
	
	@Inject(method = "saveChunk", at = @At("HEAD"), cancellable = true)
	public void bhapi_saveChunk(Level level, Chunk chunk, CallbackInfo info) {
		info.cancel();
		level.checkSessionLock();
		File file = this.getLevelDat(chunk.x, chunk.z);
		if (file.exists()) {
			LevelProperties properties = level.getProperties();
			properties.setSizeOnDisk(properties.getSizeOnDisk() - file.length());
		}
		try {
			File temp = new File(this.levelFile, "tmp_chunk.dat");
			FileOutputStream stream = new FileOutputStream(temp);
			CompoundTag tag = new CompoundTag();
			LevelManager.saveData(chunk, level, tag);
			NBTIO.writeGzipped(tag, stream);
			stream.close();
			if (file.exists()) {
				file.delete();
			}
			temp.renameTo(file);
			LevelProperties levelProperties = level.getProperties();
			levelProperties.setSizeOnDisk(levelProperties.getSizeOnDisk() + file.length());
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	@Inject(method = "saveData", at = @At("HEAD"), cancellable = true)
	private static void bhapi_saveData(Chunk chunk, Level level, CompoundTag tag, CallbackInfo info) {
		info.cancel();
		level.checkSessionLock();
		NBTSerializable.cast(chunk).saveToNBT(tag);
		tag.put("lastUpdate", level.getLevelTime());
	}
	
	@Inject(method = "loadData", at = @At("HEAD"), cancellable = true)
	private static void bhapi_loadData(Level level, CompoundTag tag, CallbackInfoReturnable<Chunk> info) {
		int x = tag.getInt("x");
		int z = tag.getInt("z");
		Chunk chunk = new Chunk(level, x, z);
		NBTSerializable.cast(chunk).loadFromNBT(tag);
		info.setReturnValue(chunk);
	}
}
