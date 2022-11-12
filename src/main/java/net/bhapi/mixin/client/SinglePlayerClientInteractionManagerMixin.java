package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.minecraft.block.BlockSounds;
import net.minecraft.client.BaseClientInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SinglePlayerClientInteractionManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SinglePlayerClientInteractionManager.class)
public abstract class SinglePlayerClientInteractionManagerMixin extends BaseClientInteractionManager {
	@Shadow private float damage;
	@Shadow private int hitDelay;
	@Shadow private int blockX;
	@Shadow private int blockY;
	@Shadow private int blockZ;
	@Shadow private float field_2186;
	@Shadow private float oldDamage;
	
	public SinglePlayerClientInteractionManagerMixin(Minecraft minecraft) {
		super(minecraft);
	}
	
	@Inject(method = "activateBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_activateBlock(int x, int y, int z, int l, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(this.minecraft.level).getBlockState(x, y, z);
		boolean result = super.activateBlock(x, y, z, l);
		ItemStack itemStack = this.minecraft.player.getHeldItem();
		
		if (itemStack != null) {
			itemStack.postMine(state.getBlock().id, x, y, z, this.minecraft.player);
			if (itemStack.count == 0) {
				itemStack.unusedEmptyMethod1(this.minecraft.player);
				this.minecraft.player.breakHeldItem();
			}
		}
		
		boolean canRemove = this.minecraft.player.canRemoveBlock(state.getBlock());
		if (result && canRemove) {
			int meta = this.minecraft.level.getBlockMeta(x, y, z);
			state.getBlock().afterBreak(this.minecraft.level, this.minecraft.player, x, y, z, meta);
		}
		
		info.setReturnValue(result);
	}
	
	@Inject(method = "playerDigBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_playerDigBlock(int x, int y, int z, int l, CallbackInfo info) {
		info.cancel();
		this.minecraft.level.firePlayer(this.minecraft.player, x, y, z, l);
		BlockState state = BlockStateProvider.cast(this.minecraft.level).getBlockState(x, y, z);
		if (!state.isAir() && this.damage == 0.0f) {
			state.getBlock().activate(this.minecraft.level, x, y, z, this.minecraft.player);
		}
		if (!state.isAir() && state.getHardness(this.minecraft.player) >= 1.0f) {
			this.activateBlock(x, y, z, l);
		}
	}
	
	@Inject(method = "digBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_digBlock(int x, int y, int z, int l, CallbackInfo info) {
		info.cancel();
		
		if (this.hitDelay > 0) {
			--this.hitDelay;
			return;
		}
		
		if (x == this.blockX && y == this.blockY && z == this.blockZ) {
			BlockState state = BlockStateProvider.cast(this.minecraft.level).getBlockState(x, y, z);
			if (state.isAir()) return;
			
			this.damage += state.getHardness(this.minecraft.player);
			if (this.field_2186 % 4.0f == 0.0f) {
				BlockSounds sounds = state.getSounds();
				this.minecraft.soundHelper.playSound(
					sounds.getWalkSound(),
					x + 0.5f,
					y + 0.5f,
					z + 0.5f,
					(sounds.getVolume() + 1.0f) / 8.0f,
					sounds.getPitch() * 0.5f
				);
			}
			
			this.field_2186 += 1.0f;
			if (this.damage >= 1.0f) {
				this.activateBlock(x, y, z, l);
				this.damage = 0.0f;
				this.oldDamage = 0.0f;
				this.field_2186 = 0.0f;
				this.hitDelay = 5;
			}
		}
		else {
			this.damage = 0.0f;
			this.oldDamage = 0.0f;
			this.field_2186 = 0.0f;
			this.blockX = x;
			this.blockY = y;
			this.blockZ = z;
		}
	}
}
