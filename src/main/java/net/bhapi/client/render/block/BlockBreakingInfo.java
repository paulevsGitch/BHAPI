package net.bhapi.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface BlockBreakingInfo {
	@Environment(EnvType.CLIENT)
	void setBreaking(int stage);
	
	static BlockBreakingInfo cast(Object obj) {
		return (BlockBreakingInfo) obj;
	}
}
