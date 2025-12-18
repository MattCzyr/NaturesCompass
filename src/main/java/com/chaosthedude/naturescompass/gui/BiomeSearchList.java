package com.chaosthedude.naturescompass.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

@Environment(EnvType.CLIENT)
public class BiomeSearchList extends ObjectSelectionList<BiomeSearchEntry> {

	private final NaturesCompassScreen parentScreen;
	private int itemHeight;

	public BiomeSearchList(NaturesCompassScreen parentScreen, Minecraft mc, int width, int height, int y, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		this.parentScreen = parentScreen;
		this.itemHeight = itemHeight;
		refreshList();
	}

	@Override
	protected int scrollBarX() {
		return getRowLeft() + getRowWidth() - 2;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		guiGraphics.fill(getRowLeft() - 4, getY(), getRowLeft() + getRowWidth() + 4, getY() + getHeight() + 4, 255 / 2 << 24);
		
		enableScissor(guiGraphics);
		for (int i = 0; i < getItemCount(); ++i) {
			if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
				BiomeSearchEntry entry = children().get(i);
				if (entry == getSelected()) {
					guiGraphics.fill(getRowLeft() - 4, getRowTop(i) - 4, getRowLeft() + getRowWidth() + 4, getRowTop(i) + itemHeight, 255 / 2 << 24);
				}
				entry.renderContent(guiGraphics, mouseX, mouseY, entry == getHovered(), partialTicks);
			}
		}
		guiGraphics.disableScissor();

		if (maxScrollAmount() > 0) {
			int left = scrollBarX();
			int right = left + 6;
			int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) contentHeight());
			height = Mth.clamp(height, 32, getBottom() - getY() - 8);
			int top = (int) scrollAmount() * (getBottom() - getY() - height) / maxScrollAmount() + getY();
			if (top < getY()) {
				top = getY();
			}
			
			guiGraphics.fill(left, getY(), right, getBottom(), (int) (2.35F * 255.0F) / 2 << 24);
			guiGraphics.fill(left, top, right, top + height, (int) (1.9F * 255.0F) / 2 << 24);
		}
	}
	
	@Override
	protected void enableScissor(GuiGraphics guiGraphics) {
		guiGraphics.enableScissor(getX(), getY(), getRight(), getBottom());
	}

	public void refreshList() {
		clearEntries();
		for (Biome biome : parentScreen.sortBiomes()) {
			addEntry(new BiomeSearchEntry(this, biome));
		}
		selectBiome(null);
	}

	public void selectBiome(BiomeSearchEntry entry) {
		setSelected(entry);
		parentScreen.selectBiome(entry);
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public NaturesCompassScreen getParentScreen() {
		return parentScreen;
	}

}