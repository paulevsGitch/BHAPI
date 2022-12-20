package net.bhapi.client.render.culling;

import net.bhapi.storage.Matrix4x4;
import net.bhapi.storage.Vec3F;

public class FrustumCulling {
	private static final Vec3F[] NORMALS;
	
	private final Matrix4x4 rotation;
	private final Vec3F[] defaultNormals;
	private final Vec3F[] planes;
	
	public FrustumCulling() {
		defaultNormals = new Vec3F[4];
		rotation = new Matrix4x4();
		planes = new Vec3F[4];
		for (byte i = 0; i < 4; i++) {
			defaultNormals[i] = NORMALS[i].clone();
			planes[i] = NORMALS[i].clone();
		}
	}
	
	public void setViewAngle(float angle) {
		for (byte i = 0; i < 4; i++) {
			Vec3F normal = defaultNormals[i];
			normal.set(NORMALS[i]);
			rotation.identity();
			if (normal.x != 0) {
				rotation.rotationY(normal.x > 0 ? angle : -angle);
			}
			else {
				rotation.rotationX(normal.y > 0 ? -angle : angle);
			}
			rotation.multiply(normal);
		}
	}
	
	public void rotate(float yaw, float pitch) {
		rotation.identity();
		rotation.rotateY(yaw);
		rotation.rotateX(pitch);
		for (byte i = 0; i < 4; i++) {
			planes[i].set(defaultNormals[i]);
			rotation.multiply(planes[i]);
		}
	}
	
	public boolean isOutside(Vec3F pos, float distance) {
		for (byte i = 0; i < 4; i++) {
			if (planes[i].dot(pos) > distance) return true;
		}
		return false;
	}
	
	static {
		NORMALS = new Vec3F[] {
			new Vec3F( 1, 0, 0),
			new Vec3F(-1, 0, 0),
			new Vec3F(0,  1, 0),
			new Vec3F(0, -1, 0)
		};
	}
}
