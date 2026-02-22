package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public class BiomeSearchList extends ObjectSelectionList<BiomeSearchEntry> {

	private final NaturesCompassScreen parentScreen;
	private Player player;

	public BiomeSearchList(NaturesCompassScreen parentScreen, Minecraft mc, Player player, int width, int height, int y, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		this.parentScreen = parentScreen;
		this.player = player;
		refreshList();
	}

	@Override
	protected int scrollBarX() {
		return getRowLeft() + getRowWidth();
	}

	@Override
	public int getRowWidth() {
		return 270;
	}
	
	@Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        enableScissor(guiGraphics);
        renderListBackground(guiGraphics);
        renderListItems(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.disableScissor();
        renderScrollbar(guiGraphics, mouseX, mouseY);
    }
	
	@Override
	protected void renderListBackground(GuiGraphics guiGraphics) {
		for (int i = 0; i < getItemCount(); ++i) {
			if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
				BiomeSearchEntry entry = children().get(i);
				int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelected());
				guiGraphics.fill(getRowLeft(), getRowTop(i), getRowLeft() + getRowWidth(), getRowTop(i) + defaultEntryHeight, fillColor);
			}
		}
	}
	
	@Override
	protected void renderSelection(GuiGraphics guiGraphics, BiomeSearchEntry entry, int backgroundColor) {
		// Selection is rendered in renderListBackground()
	}
	
	@Override
	protected void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (scrollbarVisible()) {
			int left = scrollBarX();
			int right = left + 6;
			int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) contentHeight());
			height = Mth.clamp(height, 32, getBottom() - getY() - 8);
			int top = (int) scrollAmount() * (getBottom() - getY() - height) / maxScrollAmount() + getY();
			if (top < getY()) {
				top = getY();
			}
			
			int backgroundFillColor = RenderUtils.getBackgroundColor(false, false);
			int scrollbarFillColor = RenderUtils.getBackgroundColor(true, true);
			guiGraphics.fill(left, getY(), right, getBottom(), backgroundFillColor);
			guiGraphics.fill(left, top, right, top + height, scrollbarFillColor);
		}
	}

	public void refreshList() {
		clearEntries();
		for (Biome biome : parentScreen.sortBiomes()) {
			addEntry(new BiomeSearchEntry(this, biome, player));
		}
		selectBiome(null);
		setScrollAmount(0);
	}

	public boolean selectBiome(BiomeSearchEntry entry) {
		if (entry == null || entry.isEnabled()) {
			setSelected(entry);
			parentScreen.selectBiome(entry);
			return true;
		}
		return false;
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public NaturesCompassScreen getParentScreen() {
		return parentScreen;
	}

}
