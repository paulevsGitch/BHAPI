package net.bhapi.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.Command;

@FunctionalInterface
public interface BHCommand {
	void execute(Command command, MinecraftServer server, String[] args);
}
