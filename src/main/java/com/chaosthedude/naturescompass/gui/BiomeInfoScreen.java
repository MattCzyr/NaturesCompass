package com.chaosthedude.naturescompass.gui;

import java.util.Optional;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class BiomeInfoScreen extends Screen {

	private NaturesCompassScreen parentScreen;
	private Identifier biomeID;
	private ButtonWidget searchButton;
	private ButtonWidget backButton;
	private String source;
	private String tags;
	private String temperature;
	private String rainfall;

	public BiomeInfoScreen(NaturesCompassScreen parentScreen, Identifier biomeID) {
		super(Text.translatable(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biomeID)));
		this.parentScreen = parentScreen;
		this.biomeID = biomeID;

		source = BiomeUtils.getBiomeSource(parentScreen.world, biomeID);
		tags = BiomeUtils.getBiomeTags(parentScreen.world, biomeID);

		Optional<Biome> optionalBiome = BiomeUtils.getBiomeForIdentifier(parentScreen.world, biomeID);
		if (optionalBiome.isPresent()) {
			Biome biome = optionalBiome.get();
			if (biome.getTemperature() <= 0.5) {
				temperature = I18n.translate("string.naturescompass.cold");
			} else if (biome.getTemperature() <= 1.5) {
				temperature = I18n.translate("string.naturescompass.medium");
			} else {
				temperature = I18n.translate("string.naturescompass.warm");
			}

			if (biome.weather.downfall() <= 0) {
				rainfall = I18n.translate("string.naturescompass.none");
			} else if (biome.weather.downfall() < 0.2) {
				rainfall = I18n.translate("string.naturescompass.veryLow");
			} else if (biome.weather.downfall() < 0.3) {
				rainfall = I18n.translate("string.naturescompass.low");
			} else if (biome.weather.downfall() < 0.5) {
				rainfall = I18n.translate("string.naturescompass.average");
			} else if (biome.weather.downfall() < 0.85) {
				rainfall = I18n.translate("string.naturescompass.high");
			} else {
				rainfall = I18n.translate("string.naturescompass.veryHigh");
			}
		} else {
			temperature = "";
			rainfall = "";
		}
	}

	@Override
	public void init() {
		clearChildren();
		setupButtons();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		renderBackground(context);
		String displayName = BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biomeID);
		context.drawText(textRenderer, displayName, (width / 2) - (textRenderer.getWidth(displayName) / 2), 20, 0xffffff, false);

		context.drawText(textRenderer, Text.translatable("string.naturescompass.source"), width / 2 - 100, 40, 0xffffff, false);
		context.drawText(textRenderer, source, width / 2 - 100, 50, 0x808080, false);

		int tagsMaxWidth = width / 2 - 50;
		String tagsLine = tags;
		if (textRenderer.getWidth(tagsLine) > tagsMaxWidth) {
			tagsLine = textRenderer.trimToWidth(tagsLine + "...", tagsMaxWidth) + "...";
		}

		context.drawText(textRenderer, Text.translatable("string.naturescompass.tags"), width / 2 + 40, 40, 0xffffff, false);
		context.drawText(textRenderer, tagsLine, width / 2 + 40, 50, 0x808080, false);

		context.drawText(textRenderer, Text.translatable("string.naturescompass.rainfall"), width / 2 + 40, 70, 0xffffff, false);
		context.drawText(textRenderer, rainfall, width / 2 + 40, 80, 0x808080, false);

		context.drawText(textRenderer, Text.translatable("string.naturescompass.temperature"), width / 2 - 100, 100, 0xffffff, false);
		context.drawText(textRenderer, temperature, width / 2 - 100, 110, 0x808080, false);

		super.render(context, mouseX, mouseY, partialTicks);
	}

	private void setupButtons() {
		backButton = addDrawableChild(new TransparentButton(10, height - 30, 110, 20, Text.translatable("string.naturescompass.back"), (button) -> {
			client.setScreen(parentScreen);
		}));
		searchButton = addDrawableChild(new TransparentButton(width - 120, height - 30, 110, 20, Text.translatable("string.naturescompass.search"), (button) -> {
			parentScreen.searchForBiome(biomeID);
		}));
	}

}
