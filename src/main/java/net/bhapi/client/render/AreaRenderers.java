package net.bhapi.client.render;

import net.bhapi.client.BHAPIClient;
import net.bhapi.storage.Vec3I;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.AreaRenderer;
import net.minecraft.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class AreaRenderers {
	private static final Map<Vec3I, AreaRenderer> RENDERERS = new ConcurrentHashMap<>();
	private static Level level;
	
	public static void add(Vec3I pos, AreaRenderer renderer) {
		check();
		RENDERERS.put(pos, renderer);
	}
	
	public static void change(Vec3I pos, AreaRenderer renderer) {
		check();
		for (Vec3I key: RENDERERS.keySet()) {
			if (RENDERERS.get(key) == renderer) {
				RENDERERS.remove(key);
				break;
			}
		}
		RENDERERS.put(pos, renderer);
	}
	
	public static void update(Vec3I pos) {
		AreaRenderer renderer = RENDERERS.get(pos);
		if (renderer == null) return;
		renderer.markToUpdate();
		//LevelRendererAccessor accessor = (LevelRendererAccessor) BHAPIClient.getMinecraft().worldRenderer;
		//accessor.getUpdateAreas().add(renderer);
		//renderer.update();
	}
	
	public static void clean() {
		/*LevelRendererAccessor accessor = (LevelRendererAccessor) BHAPIClient.getMinecraft().worldRenderer;
		List<AreaRenderer> areas = accessor.getUpdateAreas();
		for (int i = 0; i < areas.size(); i++) {
			if (areas.get(i) == null) {
				BHAPI.warn("Null area!");
				areas.remove(i--);
			}
		}*/
	}
	
	private static void check() {
		if (level == null || level != BHAPIClient.getMinecraft().level) {
			RENDERERS.clear();
			level = BHAPIClient.getMinecraft().level;
		}
	}
}
