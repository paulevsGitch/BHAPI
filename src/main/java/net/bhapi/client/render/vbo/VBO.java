package net.bhapi.client.render.vbo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

@Environment(EnvType.CLIENT)
public class VBO {
	protected int size;
	
	protected int vaoTarget;
	protected int vertexTarget;
	protected int normalTarget;
	protected int colorTarget;
	protected int uvTarget;
	
	protected boolean update;
	private VBOData data;
	
	public VBO() {}
	
	public void setData(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer, FloatBuffer uvBuffer) {
		data = new VBOData(
			vertexBuffer,
			normalBuffer,
			colorBuffer,
			uvBuffer
		);
		update = true;
	}
	
	public void setEmpty() {
		this.size = 0;
	}
	
	protected void attachBuffer(int target, FloatBuffer buffer) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, target);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}
	
	public boolean isEmpty() {
		return !update && size == 0;
	}
	
	public void render() {
		init();
		bind();
		update();
		GL11.glDrawArrays(GL11.GL_QUADS, 0, size);
	}
	
	protected void bind() {
		GL30.glBindVertexArray(vaoTarget);
	}
	
	protected void init() {
		if (vaoTarget != 0) return;
		vaoTarget = GL30.glGenVertexArrays();
		vertexTarget = GL15.glGenBuffers();
		normalTarget = GL15.glGenBuffers();
		colorTarget = GL15.glGenBuffers();
		uvTarget = GL15.glGenBuffers();
	}
	
	protected void update() {
		if (!update) return;
		VBOData data = this.data;
		update = false;
		
		if (data == null) return;
		
		attachBuffer(vertexTarget, data.vertexBuffer);
		attachBuffer(normalTarget, data.normalBuffer);
		attachBuffer(colorTarget, data.colorBuffer);
		attachBuffer(uvTarget, data.uvBuffer);
		this.size = data.vertexBuffer.capacity() / 3;
		
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexTarget);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalTarget);
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);
		
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorTarget);
		GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);
		
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvTarget);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
	}
	
	public static void unbind() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}
	
	public void dispose() {
		GL15.glDeleteBuffers(vaoTarget);
		GL15.glDeleteBuffers(vertexTarget);
		GL15.glDeleteBuffers(normalTarget);
		GL15.glDeleteBuffers(colorTarget);
		GL15.glDeleteBuffers(uvTarget);
	}
	
	private record VBOData(
		FloatBuffer vertexBuffer,
		FloatBuffer normalBuffer,
		FloatBuffer colorBuffer,
		FloatBuffer uvBuffer
	) {}
}
