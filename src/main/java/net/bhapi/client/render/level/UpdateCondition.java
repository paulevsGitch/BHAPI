package net.bhapi.client.render.level;

import net.bhapi.storage.Vec3I;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface UpdateCondition<T> {
	boolean needUpdate(Vec3I pos, T data);
}
