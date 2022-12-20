package net.bhapi.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

@Environment(EnvType.CLIENT)
public class VBO {
	private final int vaoTarget;
	
	private int vertexTarget;
	private int normalTarget;
	private int colorTarget;
	private int uvTarget;
	
	private boolean update;
	private int size;
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer uvBuffer;
	
	public VBO() {
		vaoTarget = GL30.glGenVertexArrays();
		vertexTarget = GL15.glGenBuffers();
		normalTarget = GL15.glGenBuffers();
		colorTarget = GL15.glGenBuffers();
		uvTarget = GL15.glGenBuffers();
	}
	
	public void setData(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer colorBuffer, FloatBuffer uvBuffer) {
		this.vertexBuffer = vertexBuffer;
		this.normalBuffer = normalBuffer;
		this.colorBuffer = colorBuffer;
		this.uvBuffer = uvBuffer;
		this.size = vertexBuffer.capacity() / 3;
	}
	
	public void setEmpty() {
		this.size = 0;
	}
	
	public void markToUpdate() {
		if (isEmpty()) return;
		update = true;
	}
	
	private void attachBuffer(int target, FloatBuffer buffer) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, target);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public void render() {
		GL30.glBindVertexArray(vaoTarget);
		
		if (update) {
			attachBuffer(vertexTarget, vertexBuffer);
			attachBuffer(normalTarget, normalBuffer);
			attachBuffer(colorTarget, colorBuffer);
			attachBuffer(uvTarget, uvBuffer);
			update = false;
			
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
		
		GL11.glDrawArrays(GL11.GL_QUADS, 0, size);
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
}
