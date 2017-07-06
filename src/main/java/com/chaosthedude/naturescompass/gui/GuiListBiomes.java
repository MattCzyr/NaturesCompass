package com.chaosthedude.naturescompass.gui;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Mouse;

import com.chaosthedude.naturescompass.util.RenderUtils;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiListBiomes extends GuiListExtended {

	private final GuiNaturesCompass guiNaturesCompass;
	private final List<GuiListBiomesEntry> entries = Lists.<GuiListBiomesEntry> newArrayList();
	private int selectedIndex = -1;

	public GuiListBiomes(GuiNaturesCompass guiNaturesCompass, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
		super(mc, width, height, top, bottom, slotHeight);
		this.guiNaturesCompass = guiNaturesCompass;
		refreshList();
	}

	@Override
	protected int getScrollBarX() {
		return super.getScrollBarX() + 20;
	}

	@Override
	public int getListWidth() {
		return super.getListWidth() + 50;
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return slotIndex == selectedIndex;
	}
	
	@Override
	public void handleMouseInput() {
		int i2 = Mouse.getEventDWheel();
		if (i2 != 0) {
			if (i2 > 0) {
				i2 = -1;
			} else if (i2 < 0) {
				i2 = 1;
			}

			amountScrolled += (float) (i2 * slotHeight);
		} else {
			super.handleMouseInput();
		}
	}
	
	@Override
	public void drawScreen(int parMouseX, int parMouseY, float partialTicks) {
		if (visible) {
			mouseX = parMouseX;
			mouseY = parMouseY;
			drawBackground();
			int x = getScrollBarX();
			int j = x + 6;
			bindAmountScrolled();
			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder buffer = tessellator.getBuffer();
			drawContainerBackground(tessellator);
			final int insideLeft = left + width / 2 - getListWidth() / 2 + 2;
			final int insideTop = top + 4 - (int) amountScrolled;
			if (hasListHeader) {
				drawListHeader(insideLeft, insideTop, tessellator);
			}

			drawSelectionBox(insideLeft, insideTop, parMouseX, parMouseY, partialTicks);
		}
	}

	@Override
	protected void drawContainerBackground(Tessellator tessellator) {
		guiNaturesCompass.drawDefaultBackground();
	}
	
	@Override
	protected void drawSelectionBox(int insideLeft, int insideTop, int mouseX, int mouseY, float partialTicks) {
		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();

		for (int i = 0; i < getSize(); i++) {
			int k = insideTop + i * slotHeight + headerPadding;
			int l = slotHeight - 4;

			if (k > bottom || k + l < top) {
				updateItemPos(i, insideLeft, k, partialTicks);
			}

			if (showSelectionBox && isSelected(i)) {
				RenderUtils.drawRect(insideLeft - 4, k - 4, insideLeft + getListWidth() + 4, k + slotHeight, 255 / 2 << 24);
			}

			drawSlot(i, insideLeft, k, l, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public GuiListBiomesEntry getListEntry(int index) {
		return (GuiListBiomesEntry) entries.get(index);
	}

	@Override
	protected int getSize() {
		return entries.size();
	}

	@Nullable
	public GuiListBiomesEntry getSelectedBiome() {
		return selectedIndex >= 0 && selectedIndex < getSize() ? getListEntry(selectedIndex) : null;
	}

	public void refreshList() {
		entries.clear();
		for (Biome biome : guiNaturesCompass.sortBiomes()) {
			entries.add(new GuiListBiomesEntry(this, biome));
		}
	}

	public void selectBiome(int index) {
		selectedIndex = index;
		guiNaturesCompass.selectBiome(getSelectedBiome());
	}

	public GuiNaturesCompass getGuiNaturesCompass() {
		return guiNaturesCompass;
	}

}
