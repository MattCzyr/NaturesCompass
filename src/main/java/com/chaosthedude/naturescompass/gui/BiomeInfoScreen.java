package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeInfoScreen extends Screen {

	private NaturesCompassScreen parentScreen;
	private Biome biome;
	private Button searchButton;
	private Button backButton;
	private String topBlock;
	private String fillerBlock;
	private String baseHeight;
	private String heightVariation;
	private String precipitation;
	private String temperature;
	private String rainfall;
	private String highHumidity;

	public BiomeInfoScreen(NaturesCompassScreen parentScreen, Biome biome) {
		super(new TranslatableComponent(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome)));
		this.parentScreen = parentScreen;
		this.biome = biome;

		topBlock = I18n.get(biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().getBlock().getDescriptionId());
		fillerBlock = I18n.get(biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().getBlock().getDescriptionId());

		if (biome.getDepth() < -1) {
			baseHeight = I18n.get("string.naturescompass.veryLow");
		} else if (biome.getDepth() < 0) {
			baseHeight = I18n.get("string.naturescompass.low");
		} else if (biome.getDepth() < 0.4) {
			baseHeight = I18n.get("string.naturescompass.average");
		} else if (biome.getDepth() < 1) {
			baseHeight = I18n.get("string.naturescompass.high");
		} else {
			baseHeight = I18n.get("string.naturescompass.veryHigh");
		}

		if (biome.getScale() < 0.3) {
			heightVariation = I18n.get("string.naturescompass.average");
		} else if (biome.getScale() < 0.6) {
			heightVariation = I18n.get("string.naturescompass.high");
		} else {
			heightVariation = I18n.get("string.naturescompass.veryHigh");
		}

		if (biome.getPrecipitation() == Precipitation.SNOW) {
			precipitation = I18n.get("string.naturescompass.snow");
		} else if (biome.getPrecipitation() == Precipitation.RAIN) {
			precipitation = I18n.get("string.naturescompass.rain");
		} else {
			precipitation = I18n.get("string.naturescompass.none");
		}
		
		if (biome.getBaseTemperature() <= 0.5) {
			temperature = I18n.get("string.naturescompass.cold");
		} else if (biome.getBaseTemperature() <= 1.5) {
			temperature = I18n.get("string.naturescompass.medium");
		} else {
			temperature = I18n.get("string.naturescompass.warm");
		}

		if (biome.getDownfall() <= 0) {
			rainfall = I18n.get("string.naturescompass.none");
		} else if (biome.getDownfall() < 0.2) {
			rainfall = I18n.get("string.naturescompass.veryLow");
		} else if (biome.getDownfall() < 0.3) {
			rainfall = I18n.get("string.naturescompass.low");
		} else if (biome.getDownfall() < 0.5) {
			rainfall = I18n.get("string.naturescompass.average");
		} else if (biome.getDownfall() < 0.85) {
			rainfall = I18n.get("string.naturescompass.high");
		} else {
			rainfall = I18n.get("string.naturescompass.veryHigh");
		}

		if (biome.isHumid()) {
			highHumidity = I18n.get("gui.yes");
		} else {
			highHumidity = I18n.get("gui.no");
		}
	}

	@Override
	public void init() {
		setupWidgets();
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(poseStack);
		font.draw(poseStack, new TextComponent(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome)), (width / 2) - (font.width(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome)) / 2), 20, 0xffffff);

		font.draw(poseStack, new TranslatableComponent("string.naturescompass.topBlock"), width / 2 - 100, 40, 0xffffff);
		font.draw(poseStack, new TextComponent(topBlock), width / 2 - 100, 50, 0x808080);

		font.draw(poseStack, new TranslatableComponent("string.naturescompass.precipitation"), width / 2 - 100, 70, 0xffffff);
		font.draw(poseStack, new TextComponent(precipitation), width / 2 - 100, 80, 0x808080);

		font.draw(poseStack, new TranslatableComponent("string.naturescompass.baseHeight"), width / 2 - 100, 100, 0xffffff);
		font.draw(poseStack, new TextComponent(baseHeight), width / 2 - 100, 110, 0x808080);

		font.draw(poseStack, new TranslatableComponent("string.naturescompass.rainfall"), width / 2 - 100, 130, 0xffffff);
		font.draw(poseStack, new TextComponent(rainfall), width / 2 - 100, 140, 0x808080);

		font.draw(poseStack, new TranslatableComponent("string.naturescompass.fillerBlock"), width / 2 + 40, 40, 0xffffff);
		font.draw(poseStack, new TextComponent(fillerBlock), width / 2 + 40, 50, 0x808080);

		font.draw(poseStack, new TranslatableComponent("string.naturescompass.temperature"), width / 2 + 40, 70, 0xffffff);
		font.draw(poseStack, new TextComponent(temperature), width / 2 + 40, 80, 0x808080);

		font.draw(poseStack, new TranslatableComponent("string.naturescompass.heightVariation"), width / 2 + 40, 100, 0xffffff);
		font.draw(poseStack, new TextComponent(heightVariation), width / 2 + 40, 110, 0x808080);

		font.draw(poseStack, new TranslatableComponent("string.naturescompass.highHumidity"), width / 2 + 40, 130, 0xffffff);
		font.draw(poseStack, new TextComponent(highHumidity), width / 2 + 40, 140, 0x808080);

		super.render(poseStack, mouseX, mouseY, partialTicks);
	}

	private void setupWidgets() {
		clearWidgets();
		backButton = addRenderableWidget(new TransparentButton(10, height - 30, 110, 20, new TranslatableComponent("string.naturescompass.back"), (onPress) -> {
			minecraft.setScreen(parentScreen);
		}));
		searchButton = addRenderableWidget(new TransparentButton(width - 120, height - 30, 110, 20, new TranslatableComponent("string.naturescompass.search"), (onPress) -> {
			parentScreen.searchForBiome(biome);
		}));
	}

}
