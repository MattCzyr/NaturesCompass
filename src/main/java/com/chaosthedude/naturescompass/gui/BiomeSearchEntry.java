package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.sorting.DimensionSorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.sorting.SourceSorting;
import com.chaosthedude.naturescompass.sorting.TagsSorting;
import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.Biome;

@Environment(EnvType.CLIENT)
public class BiomeSearchEntry extends ObjectSelectionList.Entry<BiomeSearchEntry> {

	private final Minecraft mc;
	private final NaturesCompassScreen parentScreen;
	private final Biome biome;
	private final BiomeSearchList biomesList;
	private final String tags;

	public BiomeSearchEntry(BiomeSearchList biomesList, Biome biome) {
		this.biomesList = biomesList;
		this.biome = biome;
		parentScreen = biomesList.getParentScreen();
		mc = Minecraft.getInstance();
		tags = BiomeUtils.getBiomeTags(parentScreen.level, biome);
	}

	@Override
	public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biome);
		if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting || parentScreen.getSortingCategory() instanceof DimensionSorting) {
			title = I18n.get("string.naturescompass.dimension");
			Identifier biomeID = BiomeUtils.getIdentifierForBiome(parentScreen.level, biome);
			if (biomeID != null) {
				value = BiomeUtils.dimensionKeysToString(NaturesCompass.dimensionIDsForAllowedBiomeIDs.get(biomeID));
			} else {
				value = "";
			}
		}
		
		String tagsLine = I18n.get("string.naturescompass.tags") + ": " + tags;
		if (mc.font.width(tagsLine) > biomesList.getRowWidth()) {
			tagsLine = mc.font.plainSubstrByWidth(tagsLine + "...", biomesList.getRowWidth()) + "...";
		}

		guiGraphics.drawString(mc.font, BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biome), getX() + 1, getY() + 1, 0xffffffff, false);
		guiGraphics.drawString(mc.font, title + ": " + value, getX() + 1, getY() + mc.font.lineHeight + 3, 0xff808080, false);
		guiGraphics.drawString(mc.font, tagsLine, getX() + 1, getY() + mc.font.lineHeight + 14, 0xff808080, false);
		guiGraphics.drawString(mc.font, Component.translatable("string.naturescompass.source").append(": " + BiomeUtils.getBiomeSource(parentScreen.level, biome)), getX() + 1, getY() + mc.font.lineHeight + 25, 0xff808080, false);
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		biomesList.selectBiome(this);
		if (doubleClick) {
			searchForBiome();
		}
		return true;
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
