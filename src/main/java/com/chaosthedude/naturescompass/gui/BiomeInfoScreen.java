package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Precipitation;

@Environment(EnvType.CLIENT)
public class BiomeInfoScreen extends Screen {

	private NaturesCompassScreen parentScreen;
	private Biome biome;
	private ButtonWidget searchButton;
	private ButtonWidget backButton;
	private String source;
	private String tags;
	private String precipitation;
	private String temperature;
	private String rainfall;
	private String highHumidity;

	public BiomeInfoScreen(NaturesCompassScreen parentScreen, Biome biome) {
		super(Text.translatable(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome)));
		this.parentScreen = parentScreen;
		this.biome = biome;

		source = BiomeUtils.getBiomeSource(parentScreen.world, biome);
		
		tags = BiomeUtils.getBiomeTags(parentScreen.world, biome);

		if (biome.getPrecipitation() == Precipitation.SNOW) {
			precipitation = I18n.translate("string.naturescompass.snow");
		} else if (biome.getPrecipitation() == Precipitation.RAIN) {
			precipitation = I18n.translate("string.naturescompass.rain");
		} else {
			precipitation = I18n.translate("string.naturescompass.none");
		}
		
		if (biome.getTemperature() <= 0.5) {
			temperature = I18n.translate("string.naturescompass.cold");
		} else if (biome.getTemperature() <= 1.5) {
			temperature = I18n.translate("string.naturescompass.medium");
		} else {
			temperature = I18n.translate("string.naturescompass.warm");
		}

		if (biome.getDownfall() <= 0) {
			rainfall = I18n.translate("string.naturescompass.none");
		} else if (biome.getDownfall() < 0.2) {
			rainfall = I18n.translate("string.naturescompass.veryLow");
		} else if (biome.getDownfall() < 0.3) {
			rainfall = I18n.translate("string.naturescompass.low");
		} else if (biome.getDownfall() < 0.5) {
			rainfall = I18n.translate("string.naturescompass.average");
		} else if (biome.getDownfall() < 0.85) {
			rainfall = I18n.translate("string.naturescompass.high");
		} else {
			rainfall = I18n.translate("string.naturescompass.veryHigh");
		}
		
		if (biome.hasHighHumidity()) {
			highHumidity = I18n.translate("gui.yes");
		} else {
			highHumidity = I18n.translate("gui.no");
		}
	}

	@Override
	public void init() {
		clearChildren();
		setupButtons();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		textRenderer.draw(matrixStack, BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome), (width / 2) - (textRenderer.getWidth(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome)) / 2), 20, 0xffffff);

		textRenderer.draw(matrixStack, Text.translatable("string.naturescompass.source"), width / 2 - 100, 40, 0xffffff);
		textRenderer.draw(matrixStack, source, width / 2 - 100, 50, 0x808080);
		
		int tagsMaxWidth = width / 2 - 50; // Margin of 10 on the right side
		String tagsLine = tags;
		if (textRenderer.getWidth(tagsLine) > tagsMaxWidth) {
			tagsLine = textRenderer.trimToWidth(tagsLine + "...", tagsMaxWidth) + "...";
		}

		textRenderer.draw(matrixStack, Text.translatable("string.naturescompass.tags"), width / 2 + 40, 40, 0xffffff);
		textRenderer.draw(matrixStack, tagsLine, width / 2 + 40, 50, 0x808080);

		textRenderer.draw(matrixStack, Text.translatable("string.naturescompass.precipitation"), width / 2 - 100, 70, 0xffffff);
		textRenderer.draw(matrixStack, precipitation, width / 2 - 100, 80, 0x808080);
		
		textRenderer.draw(matrixStack, Text.translatable("string.naturescompass.rainfall"), width / 2 + 40, 70, 0xffffff);
		textRenderer.draw(matrixStack, rainfall, width / 2 + 40, 80, 0x808080);
		
		textRenderer.draw(matrixStack, Text.translatable("string.naturescompass.temperature"), width / 2 - 100, 100, 0xffffff);
		textRenderer.draw(matrixStack, temperature, width / 2 - 100, 110, 0x808080);
		
		textRenderer.draw(matrixStack, Text.translatable("string.naturescompass.highHumidity"), width / 2 + 40, 100, 0xffffff);
		textRenderer.draw(matrixStack, highHumidity, width / 2 + 40, 110, 0x808080);

		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	private void setupButtons() {
		backButton = addDrawableChild(new TransparentButton(10, height - 30, 110, 20, Text.translatable("string.naturescompass.back"), (button) -> {
			client.setScreen(parentScreen);
		}));
		searchButton = addDrawableChild(new TransparentButton(width - 120, height - 30, 110, 20, Text.translatable("string.naturescompass.search"), (button) -> {
			parentScreen.searchForBiome(biome);
		}));
	}

}