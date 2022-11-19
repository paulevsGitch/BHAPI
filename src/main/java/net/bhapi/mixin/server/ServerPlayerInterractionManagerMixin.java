package net.bhapi.mixin.server;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.util.BlockUtil;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.packet.play.BlockChangePacket;
import net.minecraft.server.ServerPlayerInterractionManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInterractionManager.class)
public abstract class ServerPlayerInterractionManagerMixin {
	@Shadow private int ticks;
	@Shadow private boolean skipPlacing;
	@Shadow private int blockTicks;
	@Shadow private ServerLevel level;
	@Shadow private int nextX;
	@Shadow private int nextY;
	@Shadow private int nextZ;
	@Shadow public PlayerBase player;
	@Shadow private int lastTicks;
	@Shadow private int posX;
	@Shadow private int posY;
	@Shadow private int posZ;
	
	@Shadow public abstract boolean processBlockBreak(int i, int j, int k);
	@Shadow public abstract boolean removeBlock(int i, int j, int k);
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void bhapi_tick(CallbackInfo info) {
		info.cancel();
		++this.ticks;
		if (this.skipPlacing) {
			int n = this.ticks - this.blockTicks;
			BlockState state = BlockStateProvider.cast(this.level).getBlockState(this.nextX, this.nextY, this.nextZ);
			if (!state.isAir()) {
				float f = state.getHardness(this.player) * (float) (n + 1);
				if (f >= 1.0f) {
					this.skipPlacing = false;
					this.processBlockBreak(this.nextX, this.nextY, this.nextZ);
				}
			} else {
				this.skipPlacing = false;
			}
		}
	}
	
	@Inject(method = "activateBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_activateBlock(int x, int y, int z, int l, CallbackInfo info) {
		info.cancel();
		this.level.firePlayer(null, x, y, z, l);
		this.lastTicks = this.ticks;
		BlockState state = BlockStateProvider.cast(this.level).getBlockState(x, y, z);
		if (!state.isAir()) {
			state.getBlock().activate(this.level, x, y, z, this.player);
		}
		if (!state.isAir() && state.getHardness(this.player) >= 1.0f) {
			this.processBlockBreak(x, y, z);
		}
		else {
			this.posX = x;
			this.posY = y;
			this.posZ = z;
		}
	}
	
	@Inject(method = "setBlockPos", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlockPos(int x, int y, int z, CallbackInfo info) {
		info.cancel();
		if (x == this.posX && y == this.posY && z == this.posZ) {
			int n = this.ticks - this.lastTicks;
			BlockState state = BlockStateProvider.cast(this.level).getBlockState(x, y, z);
			if (!state.isAir()) {
				float f = state.getHardness(this.player) * (float)(n + 1);
				if (f >= 0.7f) {
					this.processBlockBreak(x, y, z);
				}
				else if (!this.skipPlacing) {
					this.skipPlacing = true;
					this.nextX = x;
					this.nextY = y;
					this.nextZ = z;
					this.blockTicks = this.lastTicks;
				}
			}
		}
	}
	
	@Inject(method = "removeBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_removeBlock(int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(this.level).getBlockState(x, y, z);
		boolean result = BlockStateProvider.cast(this.level).setBlockState(x, y, z, BlockUtil.AIR_STATE);
		if (result) {
			int meta = this.level.getBlockMeta(x, y, z);
			state.getBlock().activate(this.level, x, y, z, meta);
		}
		info.setReturnValue(result);
	}
	
	@Inject(method = "processBlockBreak", at = @At("HEAD"), cancellable = true)
	private void bhapi_processBlockBreak(int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(this.level).getBlockState(x, y, z);
		int meta = this.level.getBlockMeta(x, y, z);
		this.level.playLevelEvent(this.player, 2001, x, y, z, state.getID());
		boolean result = this.removeBlock(x, y, z);
		ItemStack itemStack = this.player.getHeldItem();
		
		if (itemStack != null) {
			itemStack.postMine(state.getBlock().id, x, y, z, this.player);
			if (itemStack.count == 0) {
				this.player.breakHeldItem();
			}
		}
		
		if (result && this.player.canRemoveBlock(state.getBlock())) {
			BlockUtil.brokenBlock = state;
			state.getBlock().afterBreak(this.level, this.player, x, y, z, meta);
			((ServerPlayer) this.player).packetHandler.send(new BlockChangePacket(x, y, z, this.level));
		}
		
		info.setReturnValue(result);
	}
	
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void useOnBlock(PlayerBase player, Level level, ItemStack stack, int x, int y, int z, int l, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(level).getBlockState(x, y, z);
		if (state.getBlock().canUse(level, x, y, z, player)) {
			info.setReturnValue(true);
			return;
		}
		if (stack == null) {
			info.setReturnValue(false);
			return;
		}
		info.setReturnValue(stack.useOnBlock(player, level, x, y, z, l));
	}
}
