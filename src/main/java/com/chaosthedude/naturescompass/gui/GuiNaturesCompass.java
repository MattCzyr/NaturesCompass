package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
	public boolean mouseScrolled(double amount) {
	      return selectionList.mouseScrolled(amount);
	   }

	@Override
	public void initGui() {
		mc.keyboardListener.enableRepeatEvents(true);
		setupButtons();
		setupTextFields();
		if (selectionList == null) {
			selectionList = new GuiListBiomes(this, mc, width + 110, height, 40, height, 45);
			children.add(selectionList);
		}
	}
	
	@Override
	public void tick() {
		searchTextField.tick();
		teleportButton.enabled = natureCompass.getState(stack) == EnumCompassState.FOUND;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		selectionList.drawScreen(mouseX, mouseY, partialTicks);
		searchTextField.drawTextField(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("string.naturescompass.selectBiome"), 65, 15, 0xffffff);
		super.render(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		boolean ret = super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		boolean ret = super.charTyped(typedChar, keyCode);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}
	
	@Override
	public void onGuiClosed() {
		mc.keyboardListener.enableRepeatEvents(false);
	}

	public void selectBiome(GuiListBiomesEntry entry) {
		boolean enable = entry != null;
		startSearchButton.enabled = enable;
		infoButton.enabled = enable;
	}

	public void searchForBiome(Biome biome) {
		NaturesCompass.network.sendToServer(new PacketCompassSearch(BiomeUtils.getIDForBiome(biome), player.getPosition()));
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
			if (BiomeUtils.getBiomeNameForDisplay(biome).toLowerCase().contains(searchTextField.getText().toLowerCase())) {
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
		buttons.clear();
		cancelButton = addButton(new GuiTransparentButton(0, 10, height - 30, 110, 20, I18n.format("gui.cancel")) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				mc.displayGuiScreen(null);
			}
		});
		sortByButton = addButton(new GuiTransparentButton(1, 10, 90, 110, 20, I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				sortingCategory = sortingCategory.next();
				sortByButton.displayString = I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName();
				selectionList.refreshList();
			}
		});
		infoButton = addButton(new GuiTransparentButton(2, 10, 65, 110, 20, I18n.format("string.naturescompass.info")) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				if (selectionList.hasSelection()) {
					selectionList.getSelectedBiome().viewInfo();
				}
			}
		});
		startSearchButton = addButton(new GuiTransparentButton(3, 10, 40, 110, 20, I18n.format("string.naturescompass.startSearch")) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				if (selectionList.hasSelection()) {
					selectionList.getSelectedBiome().searchForBiome();
				}
			}
		});
		teleportButton = addButton(new GuiTransparentButton(4, width - 120, 10, 110, 20, I18n.format("string.naturescompass.teleport")) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				teleport();
			}
		});

		startSearchButton.enabled = false;
		infoButton.enabled = false;

		teleportButton.visible = NaturesCompass.canTeleport;
	}
	
	private void setupTextFields() {
		searchTextField = new GuiTransparentTextField(0, fontRenderer, 130, 10, 140, 20);
		searchTextField.setLabel(I18n.format("string.naturescompass.search"));
		children.add(searchTextField);
	}

}
