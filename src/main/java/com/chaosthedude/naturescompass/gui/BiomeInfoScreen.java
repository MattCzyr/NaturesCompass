package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;
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
		super(new StringTextComponent(I18n.format(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome))));
		this.parentScreen = parentScreen;
		this.biome = biome;

		topBlock = I18n.format(biome.getGenerationSettings().getSurfaceBuilderConfig().getTop().getBlock().getTranslationKey()); // TODO: make sure this works
		fillerBlock = I18n.format(biome.getGenerationSettings().getSurfaceBuilderConfig().getUnder().getBlock().getTranslationKey());

		if (biome.getDepth() < -1) {
			baseHeight = I18n.format("string.naturescompass.veryLow");
		} else if (biome.getDepth() < 0) {
			baseHeight = I18n.format("string.naturescompass.low");
		} else if (biome.getDepth() < 0.4) {
			baseHeight = I18n.format("string.naturescompass.average");
		} else if (biome.getDepth() < 1) {
			baseHeight = I18n.format("string.naturescompass.high");
		} else {
			baseHeight = I18n.format("string.naturescompass.veryHigh");
		}

		if (biome.getScale() < 0.3) {
			heightVariation = I18n.format("string.naturescompass.average");
		} else if (biome.getScale() < 0.6) {
			heightVariation = I18n.format("string.naturescompass.high");
		} else {
			heightVariation = I18n.format("string.naturescompass.veryHigh");
		}

		if (biome.getPrecipitation() == RainType.SNOW) {
			precipitation = I18n.format("string.naturescompass.snow");
		} else if (biome.getPrecipitation() == RainType.RAIN) {
			precipitation = I18n.format("string.naturescompass.rain");
		} else {
			precipitation = I18n.format("string.naturescompass.none");
		}
		
		if (biome.getTemperature() <= 0.5) {
			temperature = I18n.format("string.naturescompass.cold");
		} else if (biome.getTemperature() <= 1.5) {
			temperature = I18n.format("string.naturescompass.medium");
		} else {
			temperature = I18n.format("string.naturescompass.warm");
		}

		if (biome.getDownfall() <= 0) {
			rainfall = I18n.format("string.naturescompass.none");
		} else if (biome.getDownfall() < 0.2) {
			rainfall = I18n.format("string.naturescompass.veryLow");
		} else if (biome.getDownfall() < 0.3) {
			rainfall = I18n.format("string.naturescompass.low");
		} else if (biome.getDownfall() < 0.5) {
			rainfall = I18n.format("string.naturescompass.average");
		} else if (biome.getDownfall() < 0.85) {
			rainfall = I18n.format("string.naturescompass.high");
		} else {
			rainfall = I18n.format("string.naturescompass.veryHigh");
		}

		if (biome.isHighHumidity()) {
			highHumidity = I18n.format("gui.yes");
		} else {
			highHumidity = I18n.format("gui.no");
		}
	}

	@Override
	public void init() {
		setupButtons();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		font.func_243248_b(matrixStack, new StringTextComponent(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome)), (width / 2) - (font.getStringWidth(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome)) / 2), 20, 0xffffff);

		font.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.topBlock"), width / 2 - 100, 40, 0xffffff);
		font.func_243248_b(matrixStack, new StringTextComponent(topBlock), width / 2 - 100, 50, 0x808080);

		font.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.precipitation"), width / 2 - 100, 70, 0xffffff);
		font.func_243248_b(matrixStack, new StringTextComponent(precipitation), width / 2 - 100, 80, 0x808080);

		font.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.baseHeight"), width / 2 - 100, 100, 0xffffff);
		font.func_243248_b(matrixStack, new StringTextComponent(baseHeight), width / 2 - 100, 110, 0x808080);

		font.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.rainfall"), width / 2 - 100, 130, 0xffffff);
		font.func_243248_b(matrixStack, new StringTextComponent(rainfall), width / 2 - 100, 140, 0x808080);

		font.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.fillerBlock"), width / 2 + 40, 40, 0xffffff);
		font.func_243248_b(matrixStack, new StringTextComponent(fillerBlock), width / 2 + 40, 50, 0x808080);

		font.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.temperature"), width / 2 + 40, 70, 0xffffff);
		font.func_243248_b(matrixStack, new StringTextComponent(temperature), width / 2 + 40, 80, 0x808080);

		font.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.heightVariation"), width / 2 + 40, 100, 0xffffff);
		font.func_243248_b(matrixStack, new StringTextComponent(heightVariation), width / 2 + 40, 110, 0x808080);

		font.func_243248_b(matrixStack, new TranslationTextComponent("string.naturescompass.highHumidity"), width / 2 + 40, 130, 0xffffff);
		font.func_243248_b(matrixStack, new StringTextComponent(highHumidity), width / 2 + 40, 140, 0x808080);

		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	private void setupButtons() {
		buttons.clear();
		backButton = addButton(new TransparentButton(10, height - 30, 110, 20, new TranslationTextComponent("string.naturescompass.back"), (onPress) -> {
			minecraft.displayGuiScreen(parentScreen);
		}));
		searchButton = addButton(new TransparentButton(width - 120, height - 30, 110, 20, new TranslationTextComponent("string.naturescompass.search"), (onPress) -> {
			parentScreen.searchForBiome(biome);
		}));
	}

}
