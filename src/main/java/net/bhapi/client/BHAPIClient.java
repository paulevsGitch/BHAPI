package net.bhapi.client;

import net.bhapi.BHAPI;
import net.bhapi.client.render.block.BHBlockRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class BHAPIClient implements ClientModInitializer {
	private static BHBlockRenderer blockRenderer;
	
	@Override
	public void onInitializeClient() {
		BHAPI.log("Init Client");
		ClientRegistries.init();
		blockRenderer = new BHBlockRenderer();
	}
	
	@SuppressWarnings("deprecation")
	public static Minecraft getMinecraft() {
		return (Minecraft) FabricLoader.getInstance().getGameInstance();
	}
	
	public static BHBlockRenderer getBlockRenderer() {
		return blockRenderer;
	}
}
