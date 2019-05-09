package com.chaosthedude.naturescompass.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;
import com.chaosthedude.naturescompass.network.PacketCompassSearch;
import com.chaosthedude.naturescompass.network.PacketTeleport;
import com.chaosthedude.naturescompass.sorting.CategoryName;
import com.chaosthedude.naturescompass.sorting.ISortingCategory;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.EnumCompassState;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiNaturesCompass extends GuiScreen {

	private World world;
	private EntityPlayer player;
	private List<Biome> allowedBiomes;
	private List<Biome> biomesMatchingSearch;
	private ItemStack stack;
	private ItemNaturesCompass natureCompass;
	private GuiButton startSearchButton;
	private GuiButton teleportButton;
	private GuiButton infoButton;
	private GuiButton cancelButton;
	private GuiButton sortByButton;
	private GuiTransparentTextField searchTextField;
	private GuiListBiomes selectionList;
	private ISortingCategory sortingCategory;

	public GuiNaturesCompass(World world, EntityPlayer player, ItemStack stack, ItemNaturesCompass natureCompass, List<Biome> allowedBiomes) {
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.natureCompass = natureCompass;
		this.allowedBiomes = allowedBiomes;

		sortingCategory = new CategoryName();
		biomesMatchingSearch = new ArrayList<Biome>(allowedBiomes);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		setupButtons();
		setupTextFields();
		if (selectionList == null) {
			selectionList = new GuiListBiomes(this, mc, width + 110, height, 40, height, 36);
		}
	}
	
	@Override
	public void updateScreen() {
		searchTextField.updateCursorCounter();
		teleportButton.enabled = natureCompass.getState(stack) == EnumCompassState.FOUND;
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		selectionList.handleMouseInput();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			GuiListBiomesEntry biomesEntry = selectionList.getSelectedBiome();
			if (button == startSearchButton) {
				if (biomesEntry != null) {
					biomesEntry.selectBiome();
				}
			} else if (button == teleportButton) {
				teleport();
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
		selectionList.drawScreen(mouseX, mouseY, partialTicks);
		searchTextField.drawTextBox();
		drawCenteredString(fontRenderer, I18n.format("string.naturescompass.selectBiome"), 65, 15, 0xffffff);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		selectionList.mouseClicked(mouseX, mouseY, mouseButton);
		searchTextField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		selectionList.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (searchTextField.isFocused()) {
			searchTextField.textboxKeyTyped(typedChar, keyCode);
			processSearchTerm();
		}
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected <T extends GuiButton> T addButton(T button) {
		buttonList.add(button);
		return (T) button;
	}

	public void selectBiome(GuiListBiomesEntry entry) {
		boolean enable = entry != null;
		startSearchButton.enabled = enable;
		infoButton.enabled = enable;
	}

	public void searchForBiome(Biome biome) {
		NaturesCompass.network.sendToServer(new PacketCompassSearch(Biome.getIdForBiome(biome), player.getPosition()));
		mc.displayGuiScreen(null);
	}

	public void teleport() {
		NaturesCompass.network.sendToServer(new PacketTeleport());
		mc.displayGuiScreen(null);
	}

	public ISortingCategory getSortingCategory() {
		return sortingCategory;
	}
	
	public void processSearchTerm() {
		biomesMatchingSearch = new ArrayList<Biome>();
		for (Biome biome : allowedBiomes) {
			if (BiomeUtils.getBiomeName(biome).toLowerCase().contains(searchTextField.getText().toLowerCase())) {
				biomesMatchingSearch.add(biome);
			}
		}
		selectionList.refreshList();
	}

	public List<Biome> sortBiomes() {
		final List<Biome> biomes = biomesMatchingSearch;
		Collections.sort(biomes, new CategoryName());
		Collections.sort(biomes, sortingCategory);

		return biomes;
	}

	private void setupButtons() {
		buttonList.clear();
		cancelButton = addButton(new GuiTransparentButton(0, 10, height - 30, 110, 20, I18n.format("gui.cancel")));
		sortByButton = addButton(new GuiTransparentButton(1, 10, 90, 110, 20, I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()));
		infoButton = addButton(new GuiTransparentButton(2, 10, 65, 110, 20, I18n.format("string.naturescompass.info")));
		startSearchButton = addButton(new GuiTransparentButton(3, 10, 40, 110, 20, I18n.format("string.naturescompass.startSearch")));
		teleportButton = addButton(new GuiTransparentButton(4, width - 120, 10, 110, 20, I18n.format("string.naturescompass.teleport")));

		startSearchButton.enabled = false;
		infoButton.enabled = false;

		teleportButton.visible = NaturesCompass.canTeleport;
	}
	
	private void setupTextFields() {
		searchTextField = new GuiTransparentTextField(0, fontRenderer, 130, 10, 140, 20);
		searchTextField.setLabel(I18n.format("string.naturescompass.search"));
	}

}
