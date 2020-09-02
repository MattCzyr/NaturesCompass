package com.chaosthedude.naturescompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.CompassSearchPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.sorting.ISortingCategory;
import com.chaosthedude.naturescompass.sorting.NameCategory;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
	private TransparentTextField searchTextField;
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
	public boolean func_231043_a_(double scroll1, double scroll2, double scroll3) {
		return selectionList.func_231043_a_(scroll1, scroll2, scroll3);
	}

	@Override
	protected void func_231160_c_() {
		field_230706_i_.keyboardListener.enableRepeatEvents(true);
		setupButtons();
		setupTextFields();
		if (selectionList == null) {
			selectionList = new BiomeSearchList(this, field_230706_i_, field_230708_k_ + 110, field_230709_l_, 40, field_230709_l_, 45);
		}
		field_230705_e_.add(selectionList);
	}

	@Override
	public void func_231023_e_() {
		searchTextField.tick();
		teleportButton.field_230693_o_ = natureCompass.getState(stack) == CompassState.FOUND;
	}

	@Override
	public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		func_230446_a_(matrixStack);
		selectionList.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
		searchTextField.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
		field_230712_o_.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.selectBiome"), 65 - (field_230712_o_.getStringWidth(new TranslationTextComponent("string.naturescompass.selectBiome").getString()) / 2), 15, 0xffffff);
		super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean func_231046_a_(int par1, int par2, int par3) {
		boolean ret = super.func_231046_a_(par1, par2, par3);
		if (searchTextField.func_230999_j_()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	@Override
	public boolean func_231042_a_(char typedChar, int keyCode) {
		boolean ret = super.func_231042_a_(typedChar, keyCode);
		if (searchTextField.func_230999_j_()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	@Override
	public void func_231175_as__() {
		super.func_231175_as__();
		field_230706_i_.keyboardListener.enableRepeatEvents(false);
	}

	public void selectBiome(BiomeSearchEntry entry) {
		boolean enable = entry != null;
		startSearchButton.field_230693_o_ = enable;
		infoButton.field_230693_o_ = enable;
	}

	public void searchForBiome(Biome biome) {
		NaturesCompass.network.sendToServer(new CompassSearchPacket(BiomeUtils.getIDForBiome(biome), player.func_233580_cy_()));
		field_230706_i_.displayGuiScreen(null);
	}

	public void teleport() {
		NaturesCompass.network.sendToServer(new TeleportPacket());
		field_230706_i_.displayGuiScreen(null);
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
		field_230710_m_.clear();
		cancelButton = func_230480_a_(new TransparentButton(10, field_230709_l_ - 30, 110, 20, new TranslationTextComponent("gui.cancel"), (onPress) -> {
			field_230706_i_.displayGuiScreen(null);
		}));
		sortByButton = func_230480_a_(new TransparentButton(10, 90, 110, 20, new StringTextComponent(I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.func_238482_a_(new StringTextComponent(I18n.format("string.naturescompass.sortBy") + ": " + sortingCategory.getLocalizedName()));
			selectionList.refreshList();
		}));
		infoButton = func_230480_a_(new TransparentButton(10, 65, 110, 20, new TranslationTextComponent("string.naturescompass.info"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.func_230958_g_().viewInfo();
			}
		}));
		startSearchButton = func_230480_a_(new TransparentButton(10, 40, 110, 20, new TranslationTextComponent("string.naturescompass.startSearch"), (onPress) -> {
			if (selectionList.hasSelection()) {
				selectionList.func_230958_g_().searchForBiome();
			}
		}));
		teleportButton = func_230480_a_(new TransparentButton(field_230708_k_ - 120, 10, 110, 20, new TranslationTextComponent("string.naturescompass.teleport"), (onPress) -> {
			teleport();
		}));

		startSearchButton.field_230693_o_ = false;
		infoButton.field_230693_o_ = false;

		teleportButton.field_230694_p_ = NaturesCompass.canTeleport;
	}

	private void setupTextFields() {
		searchTextField = new TransparentTextField(field_230712_o_, 130, 10, 140, 20, new TranslationTextComponent("string.naturescompass.search"));
		field_230705_e_.add(searchTextField);
	}

}
