package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.CompassSearchPacket;
import com.chaosthedude.naturescompass.network.SearchForNextPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.sorting.ISorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NaturesCompassScreen extends Screen {

	public Level level;
	private Player player;
	private ItemStack stack;
	private NaturesCompassItem natureCompass;
	private List<ResourceLocation> allowedBiomes;
	private List<ResourceLocation> biomesMatchingSearch;
	private ResourceLocation foundBiomeKey;
	private Button startSearchButton;
	private Button searchForNextButton;
	private Button teleportButton;
	private Button cancelButton;
	private Button sortByButton;
	private TransparentTextField searchTextField;
	private BiomeSearchList selectionList;
	private ISorting<?> sortingCategory;

	public NaturesCompassScreen(Level level, Player player, ItemStack stack, NaturesCompassItem natureCompass, List<ResourceLocation> allowedBiomes) {
		super(Component.translatable("string.naturescompass.selectBiome"));
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.natureCompass = natureCompass;
		this.allowedBiomes = new ArrayList<ResourceLocation>(allowedBiomes);

		sortingCategory = new NameSorting();
		biomesMatchingSearch = new ArrayList<ResourceLocation>(this.allowedBiomes);

		if (natureCompass.getState(stack) == CompassState.FOUND) {
			foundBiomeKey = natureCompass.getBiomeKey(stack);
		}
	}

	@Override
	public boolean mouseScrolled(double scroll1, double scroll2, double scroll3) {
		return selectionList.mouseScrolled(scroll1, scroll2, scroll3);
	}

	@Override
	protected void init() {
		setupWidgets();
	}

	@Override
	public void tick() {
		searchTextField.tick();
		teleportButton.active = natureCompass.getState(stack) == CompassState.FOUND && selectionList.hasSelection() && selectionList.getSelected().getBiomeKey().equals(foundBiomeKey);
		searchForNextButton.active = teleportButton.active;
		startSearchButton.active = selectionList.hasSelection();

		// Check if the sync packet has been received
		if (NaturesCompass.synced) {
			teleportButton.visible = NaturesCompass.canTeleport;
			searchForNextButton.visible = NaturesCompass.maxNextSearches > 0;
			if (searchForNextButton.visible) {
				sortByButton.setPosition(10, 100);
			} else {
				sortByButton.setPosition(10, 65);
			}

			removeWidget(selectionList);
			allowedBiomes = new ArrayList<ResourceLocation>(NaturesCompass.allowedBiomes);
			biomesMatchingSearch = new ArrayList<ResourceLocation>(allowedBiomes);
			selectionList = new BiomeSearchList(this, minecraft, player, foundBiomeKey, 130, 40, width - 140, height - 50, 50);
			addRenderableWidget(selectionList);

			NaturesCompass.synced = false;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(font, I18n.get("string.naturescompass.selectBiome"), 65, 15, 0xffffff);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean ret = super.keyPressed(keyCode, scanCode, modifiers);
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

	public void selectBiome(BiomeSearchEntry entry) {
		startSearchButton.active = entry != null;
	}

	public void searchForBiome(ResourceLocation biomeKey) {
		NaturesCompass.network.sendToServer(new CompassSearchPacket(biomeKey, player.blockPosition()));
		minecraft.setScreen(null);
	}

	public void searchForNextBiome() {
		NaturesCompass.network.sendToServer(new SearchForNextPacket());
		minecraft.setScreen(null);
	}

	public void teleport() {
		NaturesCompass.network.sendToServer(new TeleportPacket());
		minecraft.setScreen(null);
	}

	public ISorting<?> getSortingCategory() {
		return sortingCategory;
	}

	public void processSearchTerm() {
		biomesMatchingSearch = new ArrayList<ResourceLocation>();
		String searchTerm = searchTextField.getValue().toLowerCase();
		for (ResourceLocation biomeKey : allowedBiomes) {
			if (searchTerm.startsWith("$")) {
				if (BiomeUtils.getBiomeTags(level, biomeKey).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biomeKey);
				}
			} else if (searchTerm.startsWith("@")) {
				if (BiomeUtils.getBiomeSource(level, biomeKey).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biomeKey);
				}
			} else if (BiomeUtils.getBiomeNameForDisplay(level, biomeKey).toLowerCase().contains(searchTerm)) {
				biomesMatchingSearch.add(biomeKey);
			}
		}
		selectionList.refreshList(true);
	}

	public List<ResourceLocation> sortBiomes() {
		final List<ResourceLocation> biomes = biomesMatchingSearch;
		Collections.sort(biomes, new NameSorting());
		Collections.sort(biomes, sortingCategory);
		return biomes;
	}

	private void setupWidgets() {
		clearWidgets();

		startSearchButton = addRenderableWidget(new TransparentButton(10, 40, 110, 20, Component.translatable("string.naturescompass.startSearch"), (onPress) -> {
			if (selectionList.hasSelection()) {
				searchForBiome(selectionList.getSelected().getBiomeKey());
			}
		}));
		startSearchButton.active = false;

		searchForNextButton = addRenderableWidget(new TransparentButton(10, 65, 110, 20, Component.translatable("string.naturescompass.searchForNext"), (onPress) -> {
			searchForNextBiome();
		}));
		searchForNextButton.visible = NaturesCompass.maxNextSearches > 0;
		searchForNextButton.active = false;

		sortByButton = addRenderableWidget(new TransparentButton(10, searchForNextButton.visible ? 100 : 65, 110, 20, Component.literal(I18n.get("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(Component.literal(I18n.get("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()));
			selectionList.refreshList(true);
		}));

		teleportButton = addRenderableWidget(new TransparentButton(width - 120, 10, 110, 20, Component.translatable("string.naturescompass.teleport"), (onPress) -> {
			teleport();
		}));
		teleportButton.visible = NaturesCompass.canTeleport;
		teleportButton.active = false;

		cancelButton = addRenderableWidget(new TransparentButton(10, height - 30, 110, 20, Component.translatable("gui.cancel"), (onPress) -> {
			minecraft.setScreen(null);
		}));

		searchTextField = addRenderableWidget(new TransparentTextField(font, 130, 10, 140, 20, Component.translatable("string.naturescompass.search")));

		selectionList = new BiomeSearchList(this, minecraft, player, foundBiomeKey, 130, 40, width - 140, height - 50, 50);
		addRenderableWidget(selectionList);
	}

}
