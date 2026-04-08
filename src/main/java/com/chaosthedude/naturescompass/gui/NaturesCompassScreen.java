package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.SearchForNextPacket;
import com.chaosthedude.naturescompass.network.SearchPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.sorting.ISorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class NaturesCompassScreen extends Screen {

	public Level level;
	private Player player;
	private List<Biome> allowedBiomes;
	private List<Biome> biomesMatchingSearch;
	private ResourceLocation foundBiomeKey;
	private ItemStack stack;
	private NaturesCompassItem natureCompass;
	private Button searchForBiomeButton;
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
		this.allowedBiomes = new ArrayList<Biome>();
		loadAllowedBiomes(allowedBiomes);

		sortingCategory = new NameSorting();
		biomesMatchingSearch = new ArrayList<Biome>(this.allowedBiomes);

		if (natureCompass.getState(stack) == CompassState.FOUND) {
			String foundBiomeKeyStr = stack.has(NaturesCompass.BIOME_ID) ? stack.get(NaturesCompass.BIOME_ID) : null;
			if (foundBiomeKeyStr != null) {
				foundBiomeKey = ResourceLocation.parse(foundBiomeKeyStr);
			}
		}
	}

	@Override
	public boolean mouseScrolled(double par1, double par2, double par3, double par4) {
		return selectionList.mouseScrolled(par1, par2, par3, par4);
	}

	@Override
	protected void init() {
		setupWidgets();
	}

	@Override
	public void tick() {
		ResourceLocation selectedId = selectionList != null && selectionList.hasSelection() ? selectionList.getSelected().getBiomeId() : null;
		searchForNextButton.active = teleportButton.active = selectedId != null && selectedId.equals(foundBiomeKey);
		searchForBiomeButton.active = selectionList.hasSelection();

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
			loadAllowedBiomes(NaturesCompass.allowedBiomes);
			biomesMatchingSearch = new ArrayList<Biome>(allowedBiomes);
			selectionList = new BiomeSearchList(this, minecraft, player, foundBiomeKey, 130, 40, width - 140, height - 50, 50);
			addRenderableWidget(selectionList);
			NaturesCompass.synced = false;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
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

	public void searchForBiome(ResourceLocation biomeId) {
		if (biomeId != null) {
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			PacketDistributor.sendToServer(new SearchPacket(biomeId, player.blockPosition()));
		}
		minecraft.setScreen(null);
	}

	public void searchForNextBiome() {
		PacketDistributor.sendToServer(new SearchForNextPacket());
		minecraft.setScreen(null);
	}

	public void teleport() {
		PacketDistributor.sendToServer(new TeleportPacket());
		minecraft.setScreen(null);
	}

	public ISorting<?> getSortingCategory() {
		return sortingCategory;
	}

	public void processSearchTerm() {
		biomesMatchingSearch = new ArrayList<Biome>();
		String searchTerm = searchTextField.getValue().toLowerCase();
		for (Biome biome : allowedBiomes) {
			if (searchTerm.startsWith("$")) {
				if (BiomeUtils.getBiomeTags(level, biome).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biome);
				}
			} else if (searchTerm.startsWith("@")) {
				if (BiomeUtils.getBiomeSource(level, biome).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biome);
				}
			} else if (BiomeUtils.getBiomeNameForDisplay(level, biome).toLowerCase().contains(searchTerm)) {
				biomesMatchingSearch.add(biome);
			}
		}
		selectionList.refreshList(true);
	}

	public List<Biome> sortBiomes() {
		final List<Biome> biomes = biomesMatchingSearch;
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

		searchTextField = addRenderableWidget(new TransparentTextField(font, 130, 10, 140, 20, Component.translatable("string.naturescompass.search")));

		selectionList = new BiomeSearchList(this, minecraft, player, foundBiomeKey, 130, 40, width - 140, height - 50, 50);
		addRenderableWidget(selectionList);
	}

	private void loadAllowedBiomes(List<ResourceLocation> allowedBiomeKeys) {
		this.allowedBiomes = new ArrayList<Biome>();
		for (ResourceLocation biomeKey : allowedBiomeKeys) {
			Optional<Biome> optionalBiome = BiomeUtils.getBiomeForKey(level, biomeKey);
			if (optionalBiome.isPresent()) {
				this.allowedBiomes.add(optionalBiome.get());
			}
		}
	}

}
