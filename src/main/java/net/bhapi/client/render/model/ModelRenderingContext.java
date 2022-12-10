package net.bhapi.client.render.model;

import net.bhapi.blockstate.BlockState;
import net.bhapi.storage.EnumArray;
import net.bhapi.util.BlockDirection;
import net.minecraft.client.render.Tessellator;
import net.minecraft.level.BlockView;

public class ModelRenderingContext {
	private final EnumArray<BlockDirection, Boolean> renderFaces;
	private Tessellator tessellator;
	private BlockView blockView;
	private BlockState state;
	private boolean breaking;
	private double x;
	private double y;
	private double z;
	
	public ModelRenderingContext() {
		renderFaces = new EnumArray<>(BlockDirection.class);
		setRenderAllFaces(true);
	}
	
	public Tessellator getTessellator() {
		return tessellator;
	}
	
	public void setTessellator(Tessellator tessellator) {
		this.tessellator = tessellator;
	}
	
	public BlockView getBlockView() {
		return blockView;
	}
	
	public void setBlockView(BlockView blockView) {
		this.blockView = blockView;
	}
	
	public BlockState getState() {
		return state;
	}
	
	public void setState(BlockState state) {
		this.state = state;
	}
	
	public void setPosition(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public void setRenderAllFaces(boolean render) {
		for (BlockDirection dir: BlockDirection.VALUES) renderFaces.set(dir, render);
	}
	
	public void setRenderFace(BlockDirection dir, boolean render) {
		renderFaces.set(dir, render);
	}
	
	public void setBreaking(boolean breaking) {
		this.breaking = breaking;
	}
	
	public boolean isBreaking() {
		return breaking;
	}
	
	public boolean renderFace(BlockDirection dir) {
		return renderFaces.get(dir);
	}
}
