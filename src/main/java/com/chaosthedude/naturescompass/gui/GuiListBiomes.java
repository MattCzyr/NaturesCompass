package com.chaosthedude.naturescompass.gui;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.world.biome.BiomeGenBase;

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
		for (BiomeGenBase biome : guiNaturesCompass.sortBiomes()) {
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
