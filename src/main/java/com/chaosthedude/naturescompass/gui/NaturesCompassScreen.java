package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.CompassSearchPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.sorting.ISorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NaturesCompassScreen extends Screen {

	public Level level;
	private Player player;
	private List<Biome> allowedBiomes;
	private List<Biome> biomesMatchingSearch;
	private ItemStack stack;
	private NaturesCompassItem natureCompass;
	private Button startSearchButton;
	private Button teleportButton;
	private Button infoButton;
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
		teleportButton.active = natureCompass.getState(stack) == CompassState.FOUND;
		
		// Check if the sync packet has been received
		if (allowedBiomes.size() != NaturesCompass.allowedBiomes.size()) {
			teleportButton.visible = NaturesCompass.canTeleport;
			removeWidget(selectionList);
			loadAllowedBiomes(NaturesCompass.allowedBiomes);
			biomesMatchingSearch = new ArrayList<Biome>(allowedBiomes);
			selectionList = new BiomeSearchList(this, minecraft, width + 110, height, 40, height, 45);
			addRenderableWidget(selectionList);
		}
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, I18n.get("string.naturescompass.selectBiome"), 65, 15, 0xffffff);
		super.render(poseStack, mouseX, mouseY, partialTicks);
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
		boolean enable = entry != null;
		startSearchButton.active = enable;
		infoButton.active = enable;
	}

	public void searchForBiome(Biome biome) {
		if (BiomeUtils.getKeyForBiome(level, biome).isPresent()) {
			NaturesCompass.network.sendToServer(new CompassSearchPacket(BiomeUtils.getKeyForBiome(level, biome).get(), player.blockPosition()));
		}
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
		biomesMatchingSearch = new ArrayList<Biome>();
		for (Biome biome : allowedBiomes) {
			if (BiomeUtils.getBiomeNameForDisplay(level, biome).toLowerCase().contains(searchTextField.getValue().toLowerCase())) {
				biomesMatchingSearch.add(biome);
			}
		}
		selectionList.refreshList();
	}

	public List<Biome> sortBiomes() {
		final List<Biome> biomes = biomesMatchingSearch;
		Collections.sort(biomes, new NameSorting());
		Collections.sort(biomes, sortingCategory);

		return biomes;
	}

	private void setupWidgets() {
		clearWidgets();
		cancelButton = addRenderableWidget(new TransparentButton(10, height - 30, 110, 20, Component.translatable("gui.cancel"), (onPress) -> {
			minecraft.setScreen(null);
		}));
		sortByButton = addRenderableWidget(new TransparentButton(10, 90, 110, 20, Component.literal(I18n.get("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(Component.literal(I18n.get("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()));
			selectionList.refreshList();
		}));
		infoButton = addRenderableWidget(new TransparentButton(10, 65, 110, 20, Component.translatable("string.naturescompass.info"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelected().viewInfo();
			}
		}));
		startSearchButton = addRenderableWidget(new TransparentButton(10, 40, 110, 20, Component.translatable("string.naturescompass.startSearch"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelected().searchForBiome();
			}
		}));
		teleportButton = addRenderableWidget(new TransparentButton(width - 120, 10, 110, 20, Component.translatable("string.naturescompass.teleport"), (onPress) -> {
			teleport();
		}));

		startSearchButton.active = false;
		infoButton.active = false;

		teleportButton.visible = NaturesCompass.canTeleport;
		
		searchTextField = addRenderableWidget(new TransparentTextField(font, 130, 10, 140, 20, Component.translatable("string.naturescompass.search")));
		
		if (selectionList == null) {
			selectionList = new BiomeSearchList(this, minecraft, width + 110, height, 40, height, 45);
		}
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