package net.bhapi.mixin.server;

import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.Identifier;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerPlayerConnectionManager;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
	@Shadow private MinecraftServer server;
	
	@Shadow protected abstract void sendFeedbackAndLog(String string, String string2);
	
	@Inject(method = "processCommand", at = @At("HEAD"), cancellable = true)
	private void bhapi_processCommand(Command command, CallbackInfo info) {
		String commandLC = command.commandString.toLowerCase(Locale.ROOT);
		if (commandLC.startsWith("give ")) {
			info.cancel();
			ServerPlayerConnectionManager serverPlayerConnectionManager = this.server.serverPlayerConnectionManager;
			CommandSource commandSource = command.source;
			String name = commandSource.getName();
			String[] args = commandLC.split(" ");
			
			if (args.length < 2 || args.length > 3) {
				commandSource.sendFeedback("Wrong args, usage: /give [player] item");
				return;
			}
			
			Identifier id = Identifier.make(args[args.length - 1]);
			BaseItem item = CommonRegistries.ITEM_REGISTRY.get(id);
			if (item == null) {
				commandSource.sendFeedback("No such item: " + id);
				return;
			}
			
			ItemStack stack = new ItemStack(item);
			
			if (args.length == 2) {
				ServerPlayer player = serverPlayerConnectionManager.getServerPlayer(name);
				if (player != null) {
					if (!player.inventory.addStack(stack)) {
						player.dropItem(stack);
					}
				}
				this.sendFeedbackAndLog(name, "Giving " + player.name + " some " + id);
			}
			else {
				ServerPlayer player = serverPlayerConnectionManager.getServerPlayer(args[1]);
				if (player == null) {
					commandSource.sendFeedback("Can't find user " + args[1]);
					return;
				}
				if (!player.inventory.addStack(stack)) {
					player.dropItem(stack);
				}
				this.sendFeedbackAndLog(name, "Giving " + player.name + " some " + id);
			}
		}
	}
}
