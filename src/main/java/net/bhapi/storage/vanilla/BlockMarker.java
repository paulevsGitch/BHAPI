package net.bhapi.storage.vanilla;

public class BlockMarker {
	private static boolean vanillaInitiated;
	
	public static boolean isVanillaInitiated() {
		return vanillaInitiated;
	}
	
	public static void setVanillaInitiated() {
		vanillaInitiated = true;
	}
}
