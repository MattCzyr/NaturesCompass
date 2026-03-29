package com.chaosthedude.naturescompass.gui;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchList extends ObjectSelectionList<BiomeSearchEntry> {

	private final NaturesCompassScreen parentScreen;

	public BiomeSearchList(NaturesCompassScreen parentScreen, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
		super(mc, width, height, top, bottom, slotHeight);
		setLeftPos(130);
		this.parentScreen = parentScreen;
		refreshList();
	}

	@Override
	protected int getScrollbarPosition() {
		return x1;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderList(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderList(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        enableScissor(guiGraphics);
		for (int i = 0; i < getItemCount(); ++i) {
			int top = getRowTop(i);
			int bottom = getRowBottom(i);
			if (bottom >= y0 && top <= y1) {
                guiGraphics.fill(x0, top, x1, bottom, 255 / 2 << 24);
				BiomeSearchEntry entry = getEntry(i);
				if (isSelectedItem(i)) {
					guiGraphics.fill(x0, top, x1, bottom, 255 / 2 << 24);
				}
				entry.render(guiGraphics, i, top, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, isMouseOver((double) mouseX, (double) mouseY) && Objects.equals(getEntryAtPosition((double) mouseX, (double) mouseY), entry), partialTicks);
			}
		}
        guiGraphics.disableScissor();

		if (getMaxScroll() > 0) {
			int left = getScrollbarPosition();
			int right = left + 6;
			int height = (int) ((float) ((y1 - y0) * (y1 - y0)) / (float) getMaxPosition());
			height = Mth.clamp(height, 32, y1 - y0 - 8);
			int top = (int) getScrollAmount() * (y1 - y0 - height) / getMaxScroll() + y0;
			if (top < y0) {
				top = y0;
			}
			
			guiGraphics.fill(left, y0, right, y1, 255 / 2 << 24);
			guiGraphics.fill(left, top, right, top + height, 255 / 2 << 24);
		}
	}

    @Override
    public int getRowLeft() {
        return x0;
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
