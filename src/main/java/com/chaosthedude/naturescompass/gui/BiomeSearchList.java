package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class BiomeSearchList extends ObjectSelectionList<BiomeSearchEntry> {

	private final NaturesCompassScreen parentScreen;
	private Player player;

	public BiomeSearchList(NaturesCompassScreen parentScreen, Minecraft mc, Player player, Identifier biomeIdToSelect, int x, int y, int width, int height, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		this.parentScreen = parentScreen;
		this.player = player;
        setX(x);
		refreshList(biomeIdToSelect);
	}

	@Override
	protected int scrollBarX() {
		return getRowLeft() + getRowWidth();
	}

	@Override
	public int getRowWidth() {
		return getWidth();
	}

	@Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        enableScissor(guiGraphics);
        extractListBackground(guiGraphics);
        extractListItems(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.disableScissor();
        extractScrollbar(guiGraphics, mouseX, mouseY);
    }

	@Override
	protected void extractListBackground(GuiGraphicsExtractor guiGraphics) {
		for (int i = 0; i < getItemCount(); ++i) {
			if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
				BiomeSearchEntry entry = children().get(i);
				int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelected());
				guiGraphics.fill(getRowLeft(), getRowTop(i), getRowLeft() + getRowWidth(), getRowTop(i) + defaultEntryHeight, fillColor);
			}
		}
	}

	@Override
	protected void extractSelection(GuiGraphicsExtractor guiGraphics, BiomeSearchEntry entry, int backgroundColor) {
		// Selection is rendered in renderListBackground()
	}

	@Override
	protected void extractScrollbar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (scrollable()) {
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

	@Override
	public void setSelected(BiomeSearchEntry entry) {
		if (entry == null || entry.isEnabled()) {
			super.setSelected(entry);
		}
	}

	public void refreshList(Identifier biomeIdToSelect) {
		clearEntries();
		for (Identifier biomeId : parentScreen.sortBiomes()) {
			BiomeSearchEntry entry = new BiomeSearchEntry(this, biomeId, player);
			addEntry(entry);
			if (biomeId.equals(biomeIdToSelect)) {
				setSelected(entry);
			}
		}
		setScrollAmount(0);
	}

	public void refreshList(boolean maintainSelection) {
		Identifier select = maintainSelection && hasSelection() ? getSelected().getBiomeId() : null;
		refreshList(select);
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public NaturesCompassScreen getParentScreen() {
		return parentScreen;
	}

}
