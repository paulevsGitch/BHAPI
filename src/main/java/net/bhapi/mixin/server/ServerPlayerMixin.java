package net.bhapi.mixin.server;

import net.bhapi.level.LevelHeightProvider;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MapBaseItem;
import net.minecraft.level.Level;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.MapChunk0x33S2CPacket;
import net.minecraft.packet.play.UpdatePlayerHealth0x8S2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerPlayerPacketHandler;
import net.minecraft.util.maths.Vec2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerBase {
	@Shadow public MinecraftServer server;
	@Shadow public ServerPlayerPacketHandler packetHandler;
	@Shadow public List field_264;
	@Shadow protected abstract void sendBlockEntity(BaseBlockEntity arg);
	@Shadow private int field_257;
	
	public ServerPlayerMixin(Level arg) {
		super(arg);
	}
	
	@ModifyConstant(method = {
		"tick(Z)V"
	}, constant = @Constant(intValue = 128))
	private int bhapi_changeMaxBlockHeight(int value) {
		return LevelHeightProvider.cast(this.server.getLevel(this.dimensionId)).getLevelHeight();
	}
	
	@Inject(method = "tick(Z)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_tick(boolean flag, CallbackInfo info) {
		info.cancel();
		Vec2i pos;
		Object object;
		super.tick();
		
		for (short i = 0; i < this.inventory.getInventorySize(); ++i) {
			ItemStack stack = this.inventory.getInventoryItem(i);
			if (stack == null || !stack.getType().isMap() || this.packetHandler.method_834() > 2 || (object = ((MapBaseItem) stack.getType()).method_1855(stack, this.level, this)) == null) continue;
			this.packetHandler.send((AbstractPacket) object);
		}
		
		if (flag && !this.field_264.isEmpty() && (pos = (Vec2i) this.field_264.get(0)) != null) {
			boolean bl2 = false;
			if (this.packetHandler.method_834() < 4) {
				bl2 = true;
			}
			if (bl2) {
				object = this.server.getLevel(this.dimensionId);
				this.field_264.remove(pos);
				this.packetHandler.send(new MapChunk0x33S2CPacket(pos.x << 4, 0, pos.z << 4, 16, 128, 16, (Level)object));
				List<?> entities = ((ServerLevel) object).method_330(pos.x << 4, 0, pos.z << 4, (pos.x << 4) + 16, 128, (pos.z << 4) + 16);
				for (int i = 0; i < entities.size(); ++i) {
					this.sendBlockEntity((BaseBlockEntity) entities.get(i));
				}
			}
		}
		
		if (this.field_512) {
			if (this.server.serverProperties.getBoolean("allow-nether", true)) {
				if (this.container != this.playerContainer) {
					this.closeContainer();
				}
				if (this.vehicle != null) {
					this.startRiding(this.vehicle);
				} else {
					this.field_513 += 0.0125f;
					if (this.field_513 >= 1.0f) {
						this.field_513 = 1.0f;
						this.field_511 = 10;
						this.server.serverPlayerConnectionManager.sendToOppositeDimension(ServerPlayer.class.cast(this));
					}
				}
				this.field_512 = false;
			}
		}
		else {
			if (this.field_513 > 0.0f) {
				this.field_513 -= 0.05f;
			}
			if (this.field_513 < 0.0f) {
				this.field_513 = 0.0f;
			}
		}
		
		if (this.field_511 > 0) {
			--this.field_511;
		}
		if (this.health != this.field_257) {
			this.packetHandler.send(new UpdatePlayerHealth0x8S2CPacket(this.health));
			this.field_257 = this.health;
		}
	}
}
