package com.chaosthedude.naturescompass.gui;

import java.util.Collections;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.network.PacketCompassSearch;
import com.chaosthedude.naturescompass.sorting.CategoryName;
import com.chaosthedude.naturescompass.sorting.ISortingCategory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

@SideOnly(Side.CLIENT)
public class GuiNaturesCompass extends GuiScreen {

	private World world;
	private EntityPlayer player;
	private List<BiomeGenBase> allowedBiomes;
	private GuiButton searchButton;
	private GuiButton infoButton;
	private GuiButton cancelButton;
	private GuiButton sortByButton;
	private GuiListBiomes selectionList;
	private ISortingCategory sortingCategory;

	public GuiNaturesCompass(World world, EntityPlayer player, List<BiomeGenBase> allowedBiomes) {
		this.world = world;
		this.player = player;
		this.allowedBiomes = allowedBiomes;

		sortingCategory = new CategoryName();
	}

	@Override
	public void initGui() {
		selectionList = new GuiListBiomes(this, mc, width, height, 32, height - 64, 36);
		setupButtons();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			GuiListBiomesEntry biomesEntry = selectionList.getSelectedBiome();
			if (button == searchButton) {
				if (biomesEntry != null) {
					biomesEntry.selectBiome();
				}
			} else if (button == infoButton) {
				biomesEntry.viewInfo();
			} else if (button == sortByButton) {
				sortingCategory = sortingCategory.next();
				sortByButton.displayString = I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName();
				selectionList.refreshList();
			} else if (button == cancelButton) {
				mc.displayGuiScreen(null);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		selectionList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRendererObj, I18n.format("string.naturescompass.selectBiome"), width / 2, 20, 0xffffff);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		selectionList.func_148179_a(mouseX, mouseY, mouseButton);
	}

	public void selectBiome(GuiListBiomesEntry entry) {
		boolean enable = entry != null;
		searchButton.enabled = enable;
		infoButton.enabled = enable;
	}

	public void searchForBiome(BiomeGenBase biome) {
		NaturesCompass.network.sendToServer(new PacketCompassSearch(biome.biomeID, (int) player.posX, (int) player.posZ));
		mc.displayGuiScreen(null);
	}

	public ISortingCategory getSortingCategory() {
		return sortingCategory;
	}

	public List<BiomeGenBase> sortBiomes() {
		final List<BiomeGenBase> biomes = allowedBiomes;
		Collections.sort(biomes, new CategoryName());
		Collections.sort(biomes, sortingCategory);

		return biomes;
	}

	protected <T extends GuiButton> T addButton(T button) {
		buttonList.add(button);
		return (T) button;
	}

	private void setupButtons() {
		buttonList.clear();
		cancelButton = addButton(new GuiButton(0, width / 2 + 64, height - 28, 90, 20, I18n.format("gui.cancel")));
		sortByButton = addButton(new GuiButton(1, width / 2 - 154, height - 28, 210, 20, I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()));
		infoButton = addButton(new GuiButton(2, width / 2 - 154, height - 52, 150, 20, I18n.format("string.naturescompass.info")));
		searchButton = addButton(new GuiButton(3, width / 2 + 4, height - 52, 150, 20, I18n.format("string.naturescompass.search")));

		searchButton.enabled = false;
		infoButton.enabled = false;
	}

}
