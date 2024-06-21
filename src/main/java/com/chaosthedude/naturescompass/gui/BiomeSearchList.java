package com.chaosthedude.naturescompass.gui;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchList extends ObjectSelectionList<BiomeSearchEntry> {

	private final NaturesCompassScreen parentScreen;

	public BiomeSearchList(NaturesCompassScreen parentScreen, Minecraft mc, int width, int height, int y, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		this.parentScreen = parentScreen;
		refreshList();
	}

	@Override
	protected int getScrollbarPosition() {
		return getRowLeft() + getRowWidth() - 2;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	protected boolean isSelectedItem(int slotIndex) {
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelected()) : false;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		guiGraphics.fill(getRowLeft() - 4, getY(), getRowLeft() + getRowWidth() + 4, getY() + getHeight() + 4, 255 / 2 << 24);
		
		enableScissor(guiGraphics);
		for (int i = 0; i < getItemCount(); ++i) {
			int top = getRowTop(i);
			int bottom = getRowBottom(i);
			if (bottom >= getY() && top <= getBottom()) {
				BiomeSearchEntry entry = getEntry(i);
				if (isSelectedItem(i)) {
					final int insideLeft = getX() + width / 2 - getRowWidth() / 2 + 2;
					guiGraphics.fill(insideLeft - 4, top - 4, insideLeft + getRowWidth() + 4, top + itemHeight, 255 / 2 << 24);
				}
				entry.render(guiGraphics, i, top, getRowLeft(), getRowWidth(), itemHeight - 4, mouseX, mouseY, isMouseOver((double) mouseX, (double) mouseY) && Objects.equals(getEntryAtPosition((double) mouseX, (double) mouseY), entry), partialTicks);
			}
		}
		guiGraphics.disableScissor();

		if (getMaxScroll() > 0) {
			int left = getScrollbarPosition();
			int right = left + 6;
			int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) getMaxPosition());
			height = Mth.clamp(height, 32, getBottom() - getY() - 8);
			int top = (int) getScrollAmount() * (getBottom() - getY() - height) / getMaxScroll() + getY();
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

	@Override
	protected int getRowBottom(int itemIndex) {
		return getRowTop(itemIndex) + itemHeight;
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
