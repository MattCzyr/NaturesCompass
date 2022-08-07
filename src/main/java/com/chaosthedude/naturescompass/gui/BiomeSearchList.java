package com.chaosthedude.naturescompass.gui;

import java.util.Objects;

import com.chaosthedude.naturescompass.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchList extends ObjectSelectionList<BiomeSearchEntry> {

	private final NaturesCompassScreen parentScreen;

	public BiomeSearchList(NaturesCompassScreen parentScreen, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
		super(mc, width, height, top, bottom, slotHeight);
		this.parentScreen = parentScreen;
		refreshList();
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 20;
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
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		renderList(poseStack, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderList(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < getItemCount(); ++i) {
			int top = getRowTop(i);
			int bottom = getRowBottom(i);
			if (bottom >= y0 && top <= y1) {
				BiomeSearchEntry entry = getEntry(i);
				if (isSelectedItem(i)) {
					final int insideLeft = x0 + width / 2 - getRowWidth() / 2 + 2;
					RenderUtils.drawRect(insideLeft - 4, top - 4, insideLeft + getRowWidth() + 4, top + itemHeight, 255 / 2 << 24);
				}
				entry.render(poseStack, i, top, getRowLeft(), getRowWidth(), itemHeight - 4, mouseX, mouseY, isMouseOver((double) mouseX, (double) mouseY) && Objects.equals(getEntryAtPosition((double) mouseX, (double) mouseY), entry), partialTicks);
			}
		}

	}

	private int getRowBottom(int itemIndex) {
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
