package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.item.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.SearchForNextPacket;
import com.chaosthedude.naturescompass.network.SearchPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.sorting.ISorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NaturesCompassScreen extends Screen {

	public Level level;
	private Player player;
	private List<Identifier> allowedBiomes;
	private List<Identifier> biomesMatchingSearch;
	private Identifier foundBiomeId;
	private Button searchForBiomeButton;
	private Button searchForNextButton;
	private Button teleportButton;
	private Button cancelButton;
	private Button sortByButton;
	private TransparentEditBox searchBox;
	private BiomeSearchList selectionList;
	private ISorting<?> sortingCategory;

	public NaturesCompassScreen(Level level, Player player, ItemStack stack, NaturesCompassItem natureCompass, List<Identifier> allowedBiomes) {
		super(Component.translatable("string.naturescompass.selectBiome"));
		this.level = level;
		this.player = player;
		this.allowedBiomes = new ArrayList<Identifier>(allowedBiomes);

		sortingCategory = new NameSorting();
		biomesMatchingSearch = new ArrayList<Identifier>(this.allowedBiomes);

		if (natureCompass.getCompassState(stack) == CompassState.FOUND) {
			String foundBiomeIdStr = stack.getOrDefault(NaturesCompass.BIOME_ID, null);
			if (foundBiomeIdStr != null) {
				foundBiomeId = Identifier.parse(foundBiomeIdStr);
			}
		}
	}

	@Override
	protected void init() {
		setupWidgets();
	}

	@Override
	public void tick() {
		searchForNextButton.active = teleportButton.active = selectionList.hasSelection() ? selectionList.getSelected().getBiomeId().equals(foundBiomeId) : false;
		searchForBiomeButton.active = selectionList.hasSelection();

		// Check if the sync packet has been received
		if (NaturesCompass.synced) {
			teleportButton.visible = NaturesCompass.canTeleport;
			removeWidget(selectionList);
			allowedBiomes = new ArrayList<Identifier>(NaturesCompass.allowedBiomes);
			biomesMatchingSearch = new ArrayList<Identifier>(allowedBiomes);
            selectionList = addRenderableWidget(new BiomeSearchList(this, minecraft, player, foundBiomeId, 130, 40, width - 140, height - 50, 50));

			teleportButton.visible = NaturesCompass.canTeleport;
			searchForNextButton.visible = NaturesCompass.maxNextSearches > 0;
			if (searchForNextButton.visible) {
				sortByButton.setPosition(10, 100);
			} else {
				sortByButton.setPosition(10, 65);
			}

			NaturesCompass.synced = false;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.centeredText(font, I18n.get("string.naturescompass.selectBiome"), 65, 15, 0xffffffff);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		boolean ret = super.keyPressed(event);
		if (searchBox.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		boolean ret = super.charTyped(event);
		if (searchBox.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	public void searchForBiome(Identifier biomeId) {
		ClientPlayNetworking.send(new SearchPacket(biomeId, player.blockPosition()));
		minecraft.setScreen(null);
	}

	public void searchForNextBiome() {
		ClientPlayNetworking.send(new SearchForNextPacket());
		minecraft.setScreen(null);
	}

	public void teleport() {
		ClientPlayNetworking.send(new TeleportPacket());
		minecraft.setScreen(null);
	}

	public ISorting<?> getSortingCategory() {
		return sortingCategory;
	}

	public void processSearchTerm() {
		biomesMatchingSearch = new ArrayList<Identifier>();
		String searchTerm = searchBox.getValue().toLowerCase();
		for (Identifier biomeId : allowedBiomes) {
			if (searchTerm.startsWith("$")) {
				if (BiomeUtils.getBiomeTags(level, biomeId).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biomeId);
				}
			} else if (searchTerm.startsWith("@")) {
				if (BiomeUtils.getBiomeSource(level, biomeId).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biomeId);
				}
			} else if (BiomeUtils.getBiomeNameForDisplay(level, biomeId).toLowerCase().contains(searchTerm)) {
				biomesMatchingSearch.add(biomeId);
			}
		}
		selectionList.refreshList(true);
	}

	public List<Identifier> sortBiomes() {
		final List<Identifier> biomes = biomesMatchingSearch;
		Collections.sort(biomes, new NameSorting());
		Collections.sort(biomes, sortingCategory);

		return biomes;
	}

	private void setupWidgets() {
		clearWidgets();

		searchForBiomeButton = addRenderableWidget(new TransparentButton(10, 40, 110, 20, Component.translatable("string.naturescompass.search"), (onPress) -> {
			if (selectionList.hasSelection()) {
				searchForBiome(selectionList.getSelected().getBiomeId());
			}
		}));
		searchForBiomeButton.active = false;

		searchForNextButton = addRenderableWidget(new TransparentButton(10, 65, 110, 20, Component.translatable("string.naturescompass.searchForNext"), (onPress) -> {
			searchForNextBiome();
		}));
		searchForNextButton.visible = NaturesCompass.maxNextSearches > 0;
		searchForNextButton.active = false;

		sortByButton = addRenderableWidget(new TransparentButton(10, 100, 110, 20, Component.literal(I18n.get("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(Component.literal(I18n.get("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()));
			selectionList.refreshList(true);
		}));
		if (!searchForNextButton.visible) {
			sortByButton.setPosition(10, 65);
		}

		teleportButton = addRenderableWidget(new TransparentButton(width - 120, 10, 110, 20, Component.translatable("string.naturescompass.teleport"), (onPress) -> {
			teleport();
		}));
		teleportButton.visible = NaturesCompass.canTeleport;
		teleportButton.active = false;

		cancelButton = addRenderableWidget(new TransparentButton(10, height - 30, 110, 20, Component.translatable("gui.cancel"), (onPress) -> {
			minecraft.setScreen(null);
		}));

		searchBox = addRenderableWidget(new TransparentEditBox(font, 130, 10, 140, 20, Component.translatable("string.naturescompass.search").withColor(0xff808080)));

        selectionList = addRenderableWidget(new BiomeSearchList(this, minecraft, player, foundBiomeId, 130, 40, width - 140, height - 50, 50));
	}

}
