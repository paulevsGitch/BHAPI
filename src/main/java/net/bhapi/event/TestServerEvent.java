package net.bhapi.event;

import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.packet.play.MapChunk0x33S2CPacket;
import net.minecraft.util.maths.MathHelper;

// TODO remove this
public class TestServerEvent {
	@EventListener // Test Commands
	public void registerCommands(CommandRegistryEvent event) {
		event.register("explode", (command, server, args) -> {
			int radius = 5;
			if (args.length == 2) {
				radius = Integer.parseInt(args[1]);
			}
			ServerPlayer player = server.serverPlayerConnectionManager.getServerPlayer(command.source.getName());
			if (player == null) return;
			player.level.createExplosion(player, player.x, player.y, player.z, radius);
			int x1 = MathHelper.floor(player.x - radius);
			int y1 = MathHelper.floor(player.y - radius);
			int z1 = MathHelper.floor(player.z - radius);
			int delta = radius * 2 + 1;
			player.packetHandler.send(new MapChunk0x33S2CPacket(x1, y1, z1, delta, delta, delta, player.level));
		});
	}
}
