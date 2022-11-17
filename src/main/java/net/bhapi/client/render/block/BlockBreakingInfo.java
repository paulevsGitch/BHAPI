package net.bhapi.client.render.block;

public interface BlockBreakingInfo {
	void setBreaking(int stage);
	
	static BlockBreakingInfo cast(Object obj) {
		return (BlockBreakingInfo) obj;
	}
}
