package net.bhapi.client.render.level;

import net.bhapi.storage.Vec3I;

@FunctionalInterface
public interface UpdateCondition<T> {
	boolean needUpdate(Vec3I pos, T data);
}
