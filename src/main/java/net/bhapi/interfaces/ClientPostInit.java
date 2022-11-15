package net.bhapi.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ClientPostInit {
	@Environment(EnvType.CLIENT)
	void afterClientInit();
	
	static ClientPostInit cast(Object obj) {
		return (ClientPostInit) obj;
	}
}
