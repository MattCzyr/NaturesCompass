package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.SearchPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.sorting.ISorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.chaosthedude.naturescompass.utils.CompassState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class NaturesCompassScreen extends Screen {

	public World world;
	private PlayerEntity player;
	private List<Biome> allowedBiomes;
	private List<Biome> biomesMatchingSearch;
	private ItemStack stack;
	private NaturesCompassItem natureCompass;
	private ButtonWidget startSearchButton;
	private ButtonWidget teleportButton;
	private ButtonWidget cancelButton;
	private ButtonWidget sortByButton;
	private TransparentTextField searchTextField;
	private BiomeSearchList selectionList;
	private ISorting sortingCategory;

	public NaturesCompassScreen(World world, PlayerEntity player, ItemStack stack, NaturesCompassItem natureCompass, List<Identifier> allowedBiomes) {
		super(Text.translatable("string.naturescompass.selectBiome"));
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.natureCompass = natureCompass;
		this.allowedBiomes = new ArrayList<Biome>();
		loadAllowedBiomes(allowedBiomes);

		sortingCategory = new NameSorting();
		biomesMatchingSearch = new ArrayList<Biome>(this.allowedBiomes);
	}

	@Override
	public boolean mouseScrolled(double par1, double par2, double par3, double par4) {
		return selectionList.mouseScrolled(par1, par2, par3, par4);
	}

	@Override
	protected void init() {
		clearChildren();
		setupButtons();
		setupTextFields();
		if (selectionList == null) {
			selectionList = new BiomeSearchList(this, client, width + 110, height - 40, 40, 45);
		}
		addDrawableChild(selectionList);
	}

	@Override
	public void tick() {
		teleportButton.active = natureCompass.getState(stack) == CompassState.FOUND;
		
		// Check if the sync packet has been received
		if (allowedBiomes.size() != NaturesCompass.allowedBiomes.size()) {
			teleportButton.visible = NaturesCompass.canTeleport;
			remove(selectionList);
			loadAllowedBiomes(NaturesCompass.allowedBiomes);
			biomesMatchingSearch = new ArrayList<Biome>(allowedBiomes);
			selectionList = new BiomeSearchList(this, client, width + 110, height - 40, 40, 45);
			addDrawableChild(selectionList);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		context.drawCenteredTextWithShadow(textRenderer, I18n.translate("string.naturescompass.selectBiome"), 65, 15, 0xffffff);
		super.render(context, mouseX, mouseY, partialTicks);
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

	public void selectBiome(BiomeSearchEntry entry) {
		boolean enable = entry != null;
		startSearchButton.active = enable;
	}

	public void searchForBiome(Biome biome) {
		ClientPlayNetworking.send(SearchPacket.ID, new SearchPacket(BiomeUtils.getIdentifierForBiome(world, biome), player.getBlockPos()));
		client.setScreen(null);
	}

	public void teleport() {
		ClientPlayNetworking.send(TeleportPacket.ID, new TeleportPacket());
		client.setScreen(null);
	}

	public ISorting getSortingCategory() {
		return sortingCategory;
	}

	public void processSearchTerm() {
		biomesMatchingSearch = new ArrayList<Biome>();
		String searchTerm = searchTextField.getText().toLowerCase();
		for (Biome biome : allowedBiomes) {
			if (searchTerm.startsWith("$")) {
				if (BiomeUtils.getBiomeTags(world, biome).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biome);
				}
			} else if (searchTerm.startsWith("@")) {
				if (BiomeUtils.getBiomeSource(world, biome).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biome);
				}
			} else if (BiomeUtils.getBiomeNameForDisplay(world, biome).toLowerCase().contains(searchTerm)) {
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

	private void setupButtons() {
		cancelButton = addDrawableChild(new TransparentButton(10, height - 30, 110, 20, Text.translatable("gui.cancel"), (onPress) -> {
			client.setScreen(null);
		}));
		sortByButton = addDrawableChild(new TransparentButton(10, 65, 110, 20, Text.literal(I18n.translate("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(Text.literal(I18n.translate("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()));
			selectionList.refreshList();
		}));
		startSearchButton = addDrawableChild(new TransparentButton(10, 40, 110, 20, Text.translatable("string.naturescompass.startSearch"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.getSelectedOrNull().searchForBiome();
			}
		}));
		teleportButton = addDrawableChild(new TransparentButton(width - 120, 10, 110, 20, Text.translatable("string.naturescompass.teleport"), (onPress) -> {
			teleport();
		}));

		startSearchButton.active = false;

		teleportButton.visible = NaturesCompass.canTeleport;
	}

	private void setupTextFields() {
		searchTextField = new TransparentTextField(textRenderer, 130, 10, 140, 20, Text.translatable("string.naturescompass.search"));
		addDrawableChild(searchTextField);
	}
	
	private void loadAllowedBiomes(List<Identifier> allowedBiomeIDs) {
 		this.allowedBiomes = new ArrayList<Biome>();
 		for (Identifier biomeID : allowedBiomeIDs) {
 			Optional<Biome> optionalBiome = BiomeUtils.getBiomeForIdentifier(world, biomeID);
 			if (optionalBiome.isPresent()) {
 				this.allowedBiomes.add(optionalBiome.get());
 			}
 		}
 	}

}
