package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.CompassSearchPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.sorting.NameCategory;
import com.chaosthedude.naturescompass.sorting.ISortingCategory;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NaturesCompassScreen extends Screen {

	private World world;
	private PlayerEntity player;
	private List<Biome> allowedBiomes;
	private List<Biome> biomesMatchingSearch;
	private ItemStack stack;
	private NaturesCompassItem natureCompass;
	private Button startSearchButton;
	private Button teleportButton;
	private Button infoButton;
	private Button cancelButton;
	private Button sortByButton;
	private GuiTransparentTextField searchTextField;
	private BiomeSearchList selectionList;
	private ISortingCategory sortingCategory;

	public NaturesCompassScreen(World world, PlayerEntity player, ItemStack stack, NaturesCompassItem natureCompass, List<Biome> allowedBiomes) {
		super(new StringTextComponent(I18n.format("string.naturescompass.selectBiome")));
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.natureCompass = natureCompass;
		this.allowedBiomes = allowedBiomes;

		sortingCategory = new NameCategory();
		biomesMatchingSearch = new ArrayList<Biome>(allowedBiomes);
	}

	@Override
	public boolean mouseScrolled(double scroll1, double scroll2, double scroll3) {
		return selectionList.mouseScrolled(scroll1, scroll2, scroll3);
	}

	@Override
	protected void init() {
		minecraft.keyboardListener.enableRepeatEvents(true);
		setupButtons();
		setupTextFields();
		if (selectionList == null) {
			selectionList = new BiomeSearchList(this, minecraft, width + 110, height, 40, height, 45);
		}
		children.add(selectionList);
	}

	@Override
	public void tick() {
		searchTextField.tick();
		teleportButton.active = natureCompass.getState(stack) == CompassState.FOUND;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		selectionList.render(mouseX, mouseY, partialTicks);
		searchTextField.render(mouseX, mouseY, partialTicks);
		drawCenteredString(font, I18n.format("string.naturescompass.selectBiome"), 65, 15, 0xffffff);
		super.render(mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean keyPressed(int par1, int par2, int par3) {
		boolean ret = super.keyPressed(par1, par2, par3);
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
	public void onClose() {
		super.onClose();
		minecraft.keyboardListener.enableRepeatEvents(false);
	}

	public void selectBiome(BiomeSearchEntry entry) {
		boolean enable = entry != null;
		startSearchButton.active = enable;
		infoButton.active = enable;
	}

	public void searchForBiome(Biome biome) {
		NaturesCompass.network.sendToServer(new CompassSearchPacket(BiomeUtils.getIDForBiome(biome), player.getPosition()));
		minecraft.displayGuiScreen(null);
	}

	public void teleport() {
		NaturesCompass.network.sendToServer(new TeleportPacket());
		minecraft.displayGuiScreen(null);
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
		Collections.sort(biomes, new NameCategory());
		Collections.sort(biomes, sortingCategory);

		return biomes;
	}

	private void setupButtons() {
		buttons.clear();
		cancelButton = addButton(new GuiTransparentButton(10, height - 30, 110, 20, I18n.format("gui.cancel"), (onPress) -> {
			minecraft.displayGuiScreen(null);
		}));
		sortByButton = addButton(new GuiTransparentButton(10, 90, 110, 20, I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName(), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName());
			selectionList.refreshList();
		}));
		infoButton = addButton(new GuiTransparentButton(10, 65, 110, 20, I18n.format("string.naturescompass.info"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelected().viewInfo();
			}
		}));
		startSearchButton = addButton(new GuiTransparentButton(10, 40, 110, 20, I18n.format("string.naturescompass.startSearch"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelected().searchForBiome();
			}
		}));
		teleportButton = addButton(new GuiTransparentButton(width - 120, 10, 110, 20, I18n.format("string.naturescompass.teleport"), (onPress) -> {
			teleport();
		}));

		startSearchButton.active = false;
		infoButton.active = false;

		teleportButton.visible = NaturesCompass.canTeleport;
	}

	private void setupTextFields() {
		searchTextField = new GuiTransparentTextField(font, 130, 10, 140, 20, I18n.format("string.naturescompass.search"));
		children.add(searchTextField);
	}

}
