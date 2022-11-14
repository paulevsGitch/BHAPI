package net.bhapi.client;

import net.bhapi.BHAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class BHAPIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BHAPI.log("Init Client");
		ClientRegistries.init();
	}
	
	@SuppressWarnings("deprecation")
	public static Minecraft getMinecraft() {
		return (Minecraft) FabricLoader.getInstance().getGameInstance();
	}
}
