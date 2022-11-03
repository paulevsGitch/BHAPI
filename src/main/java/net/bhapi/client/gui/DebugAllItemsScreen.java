package net.bhapi.client.gui;

import net.minecraft.client.gui.screen.container.ContainerBase;
import net.minecraft.inventory.InventoryBase;
import org.lwjgl.opengl.GL11;

public class DebugAllItemsScreen extends ContainerBase {
	private final DebugAllItems allItems;
	private final int rows;
	
	public DebugAllItemsScreen(InventoryBase playerInv, DebugAllItems allItems) {
		super(new WideChest(playerInv, allItems, 18));
		this.allItems = allItems;
		rows = allItems.getInventorySize() / 18;
		this.width = width * 2;
	}
	
	@Override
	protected void renderForeground() {
		int width = this.textManager.getTextWidth(allItems.getContainerName());
		this.textManager.drawText(allItems.getContainerName(), (containerWidth - width) >> 1, 6, 0xFFFFFF);
	}
	
	@Override
	protected void renderContainerBackground(float f) {
		int n = this.minecraft.textureManager.getTextureId("/gui/container.png");
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.minecraft.textureManager.bindTexture(n);
		int n2 = (this.width - this.containerWidth) / 2;
		int n3 = (this.height - this.containerHeight) / 2;
		this.blit(n2, n3 + this.rows * 18 + 17, 0, 126, this.containerWidth, 96);
	}
}
