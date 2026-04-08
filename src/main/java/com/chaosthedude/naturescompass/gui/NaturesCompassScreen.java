package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.SearchForNextPacket;
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

@Environment(EnvType.CLIENT)
public class NaturesCompassScreen extends Screen {

	public World world;
	private PlayerEntity player;
	private List<Identifier> allowedBiomes;
	private List<Identifier> biomesMatchingSearch;
	private Identifier foundBiomeID;
	private ItemStack stack;
	private NaturesCompassItem natureCompass;
	private ButtonWidget searchForBiomeButton;
	private ButtonWidget searchForNextButton;
	private ButtonWidget teleportButton;
	private ButtonWidget cancelButton;
	private ButtonWidget sortByButton;
	private TransparentTextField searchTextField;
	private BiomeSearchList selectionList;
	private ISorting<?> sortingCategory;

	public NaturesCompassScreen(World world, PlayerEntity player, ItemStack stack, NaturesCompassItem natureCompass, List<Identifier> allowedBiomes) {
		super(Text.translatable("string.naturescompass.selectBiome"));
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.natureCompass = natureCompass;
		this.allowedBiomes = new ArrayList<Identifier>(allowedBiomes);

		sortingCategory = new NameSorting();
		biomesMatchingSearch = new ArrayList<Identifier>(this.allowedBiomes);

		if (natureCompass.getState(stack) == CompassState.FOUND) {
			if (stack.hasNbt() && stack.getNbt().contains("BiomeID")) {
				foundBiomeID = new Identifier(stack.getNbt().getString("BiomeID"));
			}
		}
	}

	@Override
	public boolean mouseScrolled(double scroll1, double scroll2, double scroll3) {
		return selectionList.mouseScrolled(scroll1, scroll2, scroll3);
	}

	@Override
	protected void init() {
		clearChildren();
		setupButtons();
		setupTextFields();
		selectionList = new BiomeSearchList(this, client, player, foundBiomeID, 130, 40, width - 140, height- 50, 50);
		addDrawableChild(selectionList);
	}

	@Override
	public void tick() {
		searchTextField.tick();
		searchForNextButton.active = teleportButton.active = selectionList.hasSelection() ? selectionList.getSelectedOrNull().getBiomeID().equals(foundBiomeID) : false;
		searchForBiomeButton.active = selectionList.hasSelection();

		// Check if the sync packet has been received
		if (NaturesCompass.synced) {
			teleportButton.visible = NaturesCompass.canTeleport;
			remove(selectionList);
			allowedBiomes = new ArrayList<Identifier>(NaturesCompass.allowedBiomes);
			biomesMatchingSearch = new ArrayList<Identifier>(allowedBiomes);
			selectionList = new BiomeSearchList(this, client, player, foundBiomeID, 130, 40, width - 140, height - 50, 50);
			addDrawableChild(selectionList);

			searchForNextButton.visible = NaturesCompass.maxNextSearches > 0;
			if (searchForNextButton.visible) {
				sortByButton.setY(100);
			} else {
				sortByButton.setY(65);
			}

			NaturesCompass.synced = false;
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, partialTicks);
		context.drawCenteredTextWithShadow(textRenderer, I18n.translate("string.naturescompass.selectBiome"), 65, 15, 0xffffff);
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

	public void searchForBiome(Identifier biomeID) {
		ClientPlayNetworking.send(SearchPacket.ID, new SearchPacket(biomeID, player.getBlockPos()));
		client.setScreen(null);
	}

	public void searchForNextBiome() {
		ClientPlayNetworking.send(SearchForNextPacket.ID, new SearchForNextPacket());
		client.setScreen(null);
	}

	public void teleport() {
		ClientPlayNetworking.send(TeleportPacket.ID, new TeleportPacket());
		client.setScreen(null);
	}

	public ISorting<?> getSortingCategory() {
		return sortingCategory;
	}

	public void processSearchTerm() {
		biomesMatchingSearch = new ArrayList<Identifier>();
		String searchTerm = searchTextField.getText().toLowerCase();
		for (Identifier biomeID : allowedBiomes) {
			if (searchTerm.startsWith("$")) {
				if (BiomeUtils.getBiomeTags(world, biomeID).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biomeID);
				}
			} else if (searchTerm.startsWith("@")) {
				if (BiomeUtils.getBiomeSource(world, biomeID).toLowerCase().contains(searchTerm.substring(1))) {
					biomesMatchingSearch.add(biomeID);
				}
			} else if (BiomeUtils.getBiomeNameForDisplay(world, biomeID).toLowerCase().contains(searchTerm)) {
				biomesMatchingSearch.add(biomeID);
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

	private void setupButtons() {
		searchForBiomeButton = addDrawableChild(new TransparentButton(10, 40, 110, 20, Text.translatable("string.naturescompass.search"), (onPress) -> {
			if (selectionList.hasSelection()) {
				searchForBiome(selectionList.getSelectedOrNull().getBiomeID());
			}
		}));
		searchForBiomeButton.active = false;

		searchForNextButton = addDrawableChild(new TransparentButton(10, 65, 110, 20, Text.translatable("string.naturescompass.searchForNext"), (onPress) -> {
			searchForNextBiome();
		}));
		searchForNextButton.visible = NaturesCompass.maxNextSearches > 0;
		searchForNextButton.active = false;

		sortByButton = addDrawableChild(new TransparentButton(10, 100, 110, 20, Text.literal(I18n.translate("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(Text.literal(I18n.translate("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()));
			selectionList.refreshList(true);
		}));
		if (!searchForNextButton.visible) {
			sortByButton.setY(65);
		}

		cancelButton = addDrawableChild(new TransparentButton(10, height - 30, 110, 20, Text.translatable("gui.cancel"), (onPress) -> {
			client.setScreen(null);
		}));

		teleportButton = addDrawableChild(new TransparentButton(width - 120, 10, 110, 20, Text.translatable("string.naturescompass.teleport"), (onPress) -> {
			teleport();
		}));
		teleportButton.visible = NaturesCompass.canTeleport;
		teleportButton.active = false;
	}

	private void setupTextFields() {
		searchTextField = new TransparentTextField(textRenderer, 130, 10, 140, 20, Text.translatable("string.naturescompass.search"));
		addDrawableChild(searchTextField);
	}

}
