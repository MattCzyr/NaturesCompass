package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeInfoScreen extends Screen {

	private NaturesCompassScreen parentScreen;
	private Biome biome;
	private Button searchButton;
	private Button backButton;
	private String source;
	private String tags;
	private String temperature;
	private String rainfall;

	public BiomeInfoScreen(NaturesCompassScreen parentScreen, Biome biome) {
		super(Component.translatable(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biome)));
		this.parentScreen = parentScreen;
		this.biome = biome;
		
		source = BiomeUtils.getBiomeSource(parentScreen.level, biome);
		
		tags = BiomeUtils.getBiomeTags(parentScreen.level, biome);
		
		if (biome.getBaseTemperature() <= 0.5) {
			temperature = I18n.get("string.naturescompass.cold");
		} else if (biome.getBaseTemperature() <= 1.5) {
			temperature = I18n.get("string.naturescompass.medium");
		} else {
			temperature = I18n.get("string.naturescompass.warm");
		}

		if (biome.getModifiedClimateSettings().downfall() <= 0) {
			rainfall = I18n.get("string.naturescompass.none");
		} else if (biome.getModifiedClimateSettings().downfall() < 0.2) {
			rainfall = I18n.get("string.naturescompass.veryLow");
		} else if (biome.getModifiedClimateSettings().downfall() < 0.3) {
			rainfall = I18n.get("string.naturescompass.low");
		} else if (biome.getModifiedClimateSettings().downfall() < 0.5) {
			rainfall = I18n.get("string.naturescompass.average");
		} else if (biome.getModifiedClimateSettings().downfall() < 0.85) {
			rainfall = I18n.get("string.naturescompass.high");
		} else {
			rainfall = I18n.get("string.naturescompass.veryHigh");
		}
	}

	@Override
	public void init() {
		setupWidgets();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
		guiGraphics.drawString(font, Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biome)), (width / 2) - (font.width(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biome)) / 2), 20, 0xffffff);

		guiGraphics.drawString(font, Component.translatable("string.naturescompass.source"), width / 2 - 100, 40, 0xffffff);
		guiGraphics.drawString(font, Component.literal(source), width / 2 - 100, 50, 0x808080);

		int tagsMaxWidth = width / 2 - 50; // Margin of 10 on the right side
		String tagsLine = tags;
		if (font.width(tagsLine) > tagsMaxWidth) {
			tagsLine = font.plainSubstrByWidth(tagsLine + "...", tagsMaxWidth) + "...";
		}
		
		guiGraphics.drawString(font, Component.translatable("string.naturescompass.tags"), width / 2 + 40, 40, 0xffffff);
		guiGraphics.drawString(font, Component.literal(tagsLine), width / 2 + 40, 50, 0x808080);

		//guiGraphics.drawString(font, Component.translatable("string.naturescompass.precipitation"), width / 2 - 100, 70, 0xffffff);
		//guiGraphics.drawString(font, Component.literal(precipitation), width / 2 - 100, 80, 0x808080);
		
		guiGraphics.drawString(font, Component.translatable("string.naturescompass.rainfall"), width / 2 + 40, 70, 0xffffff);
		guiGraphics.drawString(font, Component.literal(rainfall), width / 2 + 40, 80, 0x808080);
		
		guiGraphics.drawString(font, Component.translatable("string.naturescompass.temperature"), width / 2 - 100, 100, 0xffffff);
		guiGraphics.drawString(font, Component.literal(temperature), width / 2 - 100, 110, 0x808080);
		
		//guiGraphics.drawString(font, Component.translatable("string.naturescompass.highHumidity"), width / 2 + 40, 100, 0xffffff);
		//guiGraphics.drawString(font, Component.literal(highHumidity), width / 2 + 40, 110, 0x808080);

		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	private void setupWidgets() {
		clearWidgets();
		backButton = addRenderableWidget(new TransparentButton(10, height - 30, 110, 20, Component.translatable("string.naturescompass.back"), (onPress) -> {
			minecraft.setScreen(parentScreen);
		}));
		searchButton = addRenderableWidget(new TransparentButton(width - 120, height - 30, 110, 20, Component.translatable("string.naturescompass.search"), (onPress) -> {
			parentScreen.searchForBiome(biome);
		}));
	}

}
