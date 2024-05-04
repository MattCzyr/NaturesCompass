package com.chaosthedude.naturescompass.gui;

import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.sorting.DimensionSorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.sorting.SourceSorting;
import com.chaosthedude.naturescompass.sorting.TagsSorting;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchEntry extends ObjectSelectionList.Entry<BiomeSearchEntry> {

	private final Minecraft mc;
	private final NaturesCompassScreen parentScreen;
	private final Biome biome;
	private final BiomeSearchList biomesList;
	private final String tags;
	private long lastClickTime;

	public BiomeSearchEntry(BiomeSearchList biomesList, Biome biome) {
		this.biomesList = biomesList;
		this.biome = biome;
		parentScreen = biomesList.getParentScreen();
		mc = Minecraft.getInstance();
		tags = BiomeUtils.getBiomeTags(parentScreen.level, biome);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int par6, int par7, boolean par8, float par9) {
		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biome);
		if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting || parentScreen.getSortingCategory() instanceof DimensionSorting) {
			title = I18n.get("string.naturescompass.dimension");
			Optional<ResourceLocation> biomeKey = BiomeUtils.getKeyForBiome(parentScreen.level, biome);
			if (biomeKey.isPresent()) {
				value = BiomeUtils.dimensionKeysToString(NaturesCompass.dimensionKeysForAllowedBiomeKeys.get(biomeKey.get()));
			} else {
				value = "";
			}
		}
		
		String tagsLine = I18n.get("string.naturescompass.tags") + ": " + tags;
		if (mc.font.width(tagsLine) > biomesList.getRowWidth()) {
			tagsLine = mc.font.plainSubstrByWidth(tagsLine + "...", biomesList.getRowWidth()) + "...";
		}

		guiGraphics.drawString(mc.font, Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biome)), left + 1, top + 1, 0xffffff);
		guiGraphics.drawString(mc.font, Component.literal(title + ": " + value), left + 1, top + mc.font.lineHeight + 3, 0x808080);
		guiGraphics.drawString(mc.font, Component.literal(tagsLine), left + 1, top + mc.font.lineHeight + 14, 0x808080);
		guiGraphics.drawString(mc.font, Component.translatable("string.naturescompass.source").append(Component.literal(": " + BiomeUtils.getBiomeSource(parentScreen.level, biome))), left + 1, top + mc.font.lineHeight + 25, 0x808080);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			biomesList.selectBiome(this);
			if (Util.getMillis() - lastClickTime < 250L) {
				searchForBiome();
				return true;
			} else {
				lastClickTime = Util.getMillis();
				return false;
			}
		}
		return false;
	}

	public void searchForBiome() {
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForBiome(biome);
	}

	public void viewInfo() {
		mc.setScreen(new BiomeInfoScreen(parentScreen, biome));
	}

	@Override
	public Component getNarration() {
		return Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biome));
	}

}
