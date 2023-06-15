package com.chaosthedude.naturescompass.gui;

import java.util.Objects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class BiomeSearchList extends EntryListWidget<BiomeSearchEntry> {

	private final NaturesCompassScreen guiNaturesCompass;

	public BiomeSearchList(NaturesCompassScreen guiNaturesCompass, MinecraftClient mc, int width, int height, int top, int bottom, int slotHeight) {
		super(mc, width, height, top, bottom, slotHeight);
		this.guiNaturesCompass = guiNaturesCompass;
		refreshList();
	}

	@Override
	protected int getScrollbarPositionX() {
		return super.getScrollbarPositionX() + 20;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	protected boolean isSelectedEntry(int slotIndex) {
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelectedOrNull()) : false;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float par3) {
		renderList(context, mouseX, mouseY, par3);
	}

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float par5) {
		int i = getEntryCount();
		for (int j = 0; j < i; ++j) {
			int k = getRowTop(j);
			int l = getRowBottom(j);
			if (l >= top && k <= bottom) {
				int j1 = this.itemHeight - 4;
				BiomeSearchEntry e = getEntry(j);
				int k1 = getRowWidth();
				if (/*renderSelection*/ true && isSelectedEntry(j)) {
					final int insideLeft = left + width / 2 - getRowWidth() / 2 + 2;
					context.fill(insideLeft - 4, k - 4, insideLeft + getRowWidth() + 4, k + itemHeight, 255 / 2 << 24);
				}

				int j2 = getRowLeft();
				e.render(context, j, k, j2, k1, j1, mouseX, mouseY, isMouseOver((double) mouseX, (double) mouseY) && Objects .equals(getEntryAtPosition((double) mouseX, (double) mouseY), e), par5);
			}
		}

		if (getMaxScroll() > 0) {
			int left = getScrollbarPositionX();
			int right = left + 6;
			int height = (int) ((float) ((bottom - top) * (bottom - top)) / (float) getMaxPosition());
			height = MathHelper.clamp(height, 32, bottom - top - 8);
			int scrollbarTop = (int) getScrollAmount() * (bottom - top - height) / getMaxScroll() + top;
			if (scrollbarTop < top) {
				scrollbarTop = top;
			}
			
			context.fill(left, scrollbarTop, right, bottom, (int) (2.35F * 255.0F) / 2 << 24);
			context.fill(left, scrollbarTop, right, scrollbarTop + height, (int) (1.9F * 255.0F) / 2 << 24);
		}
	}

	protected int getRowBottom(int index) {
		return getRowTop(index) + itemHeight;
	}

	public void refreshList() {
		clearEntries();
		for (Biome biome : guiNaturesCompass.sortBiomes()) {
			addEntry(new BiomeSearchEntry(this, biome));
		}
		selectBiome(null);
	}

	public void selectBiome(BiomeSearchEntry entry) {
		setSelected(entry);
		guiNaturesCompass.selectBiome(entry);
	}

	public boolean hasSelection() {
		return getSelectedOrNull() != null;
	}

	public NaturesCompassScreen getGuiNaturesCompass() {
		return guiNaturesCompass;
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		// TODO Auto-generated method stub
		
	}

}