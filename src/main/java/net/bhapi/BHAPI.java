package net.bhapi;

import net.bhapi.block.BHBaseBlock;
import net.bhapi.block.LegacyBlockInfo;
import net.bhapi.blockstate.BlockState;
import net.bhapi.registry.DefaultRegistries;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.Identifier;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.material.Material;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BHAPI implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	private static BHAPI instance;
	
	@Override
	public void onInitialize() {
		instance = this;
		
		LegacyBlockInfo.init();
		DefaultRegistries.initBlocks();
		BlockUtil.init();
		
		Identifier id = Identifier.make("testblock");
		BHBaseBlock block = new BHBaseBlock(Material.STONE);
		DefaultRegistries.BLOCK_REGISTRY.register(id, block);
		BlockState.getDefaultState(block);
		
		id = Identifier.make("testblock2");
		block = new BHBaseBlock(Material.STONE);
		DefaultRegistries.BLOCK_REGISTRY.register(id, block);
		BlockState.getDefaultState(block);
	}
	
	public static BHAPI getInstance() {
		return instance;
	}
	
	public static void log(Object obj) {
		LOGGER.log(Level.INFO, obj == null ? "null" : obj.toString());
	}
	
	public static void warn(String message) {
		LOGGER.warn(message);
	}
	
	/**
	 * Check if object is not null, made for streams.
	 * Example: Arrays.stream(BaseBlock.BY_ID).filter(BHAPI::notNull)
	 */
	public static boolean notNull(Object obj) {
		return obj != null;
	}
}
