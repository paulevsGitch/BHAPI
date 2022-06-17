package net.bhapi.mixin.common;

import net.minecraft.level.Level;
import net.minecraft.level.LevelManager;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.util.io.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelManager.class)
public class LevelManagerMixin {
	@Inject(
		method = "loadData(Lnet/minecraft/level/Level;Lnet/minecraft/util/io/CompoundTag;)Lnet/minecraft/level/chunk/Chunk;",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/util/io/CompoundTag;getBoolean(Ljava/lang/String;)Z",
			shift = Shift.AFTER
		), locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void bhapi_loadChunk(Level level, CompoundTag tag, CallbackInfoReturnable<Chunk> info, int x, int z, Chunk chunk) {
		//chunk.blocks = null;
	}
	
	private static void bhapi_saveChunk() {
	
	}
	
	@Redirect(
		method = "loadData(Lnet/minecraft/level/Level;Lnet/minecraft/util/io/CompoundTag;)Lnet/minecraft/level/chunk/Chunk;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/io/CompoundTag;getByteArray(Ljava/lang/String;)[B",
			ordinal = 0
		)
	)
	private static byte[] bhapi_stopLoadingBlocks(CompoundTag tag, String key) { return null; }
	
	@Redirect(
		method = "saveData(Lnet/minecraft/level/chunk/Chunk;Lnet/minecraft/level/Level;Lnet/minecraft/util/io/CompoundTag;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/io/CompoundTag;put(Ljava/lang/String;[B)V",
			ordinal = 0
		)
	)
	private static void bhapi_stopSavingBlocks(CompoundTag tag, String key, byte[] data) {}
}
