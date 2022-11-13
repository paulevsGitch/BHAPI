package net.bhapi.mixin.server;

import net.bhapi.command.BHCommand;
import net.bhapi.registry.CommonRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
	@Shadow private MinecraftServer server;
	
	@Inject(method = "processCommand", at = @At("HEAD"), cancellable = true)
	private void bhapi_processCommand(Command command, CallbackInfo info) {
		String commandLC = command.commandString.toLowerCase(Locale.ROOT);
		String[] args = commandLC.split(" ");
		if (args.length == 0) return;
		BHCommand bhCommand = CommonRegistries.COMMAND_REGISTRY.get(args[0]);
		if (bhCommand != null) {
			bhCommand.execute(command, this.server, args);
			info.cancel();
		}
	}
}
