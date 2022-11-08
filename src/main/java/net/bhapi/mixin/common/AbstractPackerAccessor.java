package net.bhapi.mixin.common;

import net.minecraft.packet.AbstractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractPacket.class)
public interface AbstractPackerAccessor {
	@Invoker
	static void callRegister(int id, boolean serverToClient, boolean clientToServer, Class packetClass) {
		throw new AssertionError("@Invoker dummy body called");
	}
}
