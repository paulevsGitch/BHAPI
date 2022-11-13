package net.bhapi.mixin.client;

import net.bhapi.blockstate.BlockState;
import net.bhapi.level.BlockStateProvider;
import net.minecraft.block.BlockSounds;
import net.minecraft.client.BaseClientInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MultiPlayerClientInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientPlayNetworkHandler;
import net.minecraft.packet.play.PlayerDiggingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerClientInteractionManager.class)
public abstract class MultiPlayerClientInteractionManagerMixin extends BaseClientInteractionManager {
	@Shadow private int posX;
	@Shadow private int posY;
	@Shadow private int posZ;
	@Shadow private boolean canBreakBlock;
	@Shadow private ClientPlayNetworkHandler networkHandler;
	@Shadow private float hardness;
	@Shadow private float oldHardness;
	@Shadow private float field_2613;
	@Shadow private int field_2614;
	
	@Shadow protected abstract void updateHotbarSlot();
	
	public MultiPlayerClientInteractionManagerMixin(Minecraft minecraft) {
		super(minecraft);
	}
	
	@Inject(method = "activateBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_activateBlock(int x, int y, int z, int l, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockStateProvider.cast(this.minecraft.level).getBlockState(x, y, z);
		boolean result = super.activateBlock(x, y, z, l);
		ItemStack stack = this.minecraft.player.getHeldItem();
		if (stack != null) {
			stack.postMine(state.getBlock().id, x, y, z, this.minecraft.player);
			if (stack.count == 0) {
				stack.unusedEmptyMethod1(this.minecraft.player);
				this.minecraft.player.breakHeldItem();
			}
		}
		info.setReturnValue(result);
	}
	
	@Inject(method = "playerDigBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_playerDigBlock(int x, int y, int z, int l, CallbackInfo info) {
		info.cancel();
		if (!this.canBreakBlock || x != this.posX || y != this.posY || z != this.posZ) {
			this.networkHandler.sendPacket(new PlayerDiggingPacket(0, x, y, z, l));
			BlockState state = BlockStateProvider.cast(this.minecraft.level).getBlockState(x, y, z);
			
			if (!state.isAir() && this.hardness == 0.0f) {
				state.getBlock().activate(this.minecraft.level, x, y, z, this.minecraft.player);
			}
			
			if (!state.isAir() && state.getHardness(this.minecraft.player) >= 1.0f) {
				this.activateBlock(x, y, z, l);
			}
			else {
				this.canBreakBlock = true;
				this.posX = x;
				this.posY = y;
				this.posZ = z;
				this.hardness = 0.0f;
				this.oldHardness = 0.0f;
				this.field_2613 = 0.0f;
			}
		}
	}
	
	@Inject(method = "digBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_digBlock(int x, int y, int z, int l, CallbackInfo info) {
		info.cancel();
		
		if (!this.canBreakBlock) {
			return;
		}
		
		this.updateHotbarSlot();
		if (this.field_2614 > 0) {
			--this.field_2614;
			return;
		}
		
		if (x == this.posX && y == this.posY && z == this.posZ) {
			BlockState state = BlockStateProvider.cast(this.minecraft.level).getBlockState(x, y, z);
			
			if (state.isAir()) {
				this.canBreakBlock = false;
				return;
			}
			
			this.hardness += state.getHardness(this.minecraft.player);
			if (this.field_2613 % 4.0f == 0.0f) {
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
			
			this.field_2613 += 1.0f;
			if (this.hardness >= 1.0f) {
				this.canBreakBlock = false;
				this.networkHandler.sendPacket(new PlayerDiggingPacket(2, x, y, z, l));
				this.activateBlock(x, y, z, l);
				this.hardness = 0.0f;
				this.oldHardness = 0.0f;
				this.field_2613 = 0.0f;
				this.field_2614 = 5;
			}
		}
		else {
			this.playerDigBlock(x, y, z, l);
		}
	}
}
