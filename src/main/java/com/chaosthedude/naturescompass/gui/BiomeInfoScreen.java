package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
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
	private String climate;
	private String rainfall;
	private String highHumidity;

	public BiomeInfoScreen(NaturesCompassScreen parentScreen, Biome biome) {
		super(new StringTextComponent(I18n.format(BiomeUtils.getBiomeNameForDisplay(biome))));
		this.parentScreen = parentScreen;
		this.biome = biome;

		topBlock = biome.getSurfaceBuilderConfig().getTop().getBlock().getNameTextComponent().getFormattedText();
		fillerBlock = biome.getSurfaceBuilderConfig().getUnder().getBlock().getNameTextComponent().getFormattedText();

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

		if (biome.getTempCategory() == Biome.TempCategory.COLD) {
			climate = I18n.format("string.naturescompass.cold");
		} else if (biome.getTempCategory() == Biome.TempCategory.OCEAN) {
			climate = I18n.format("string.naturescompass.ocean");
		} else if (biome.getTempCategory() == Biome.TempCategory.WARM) {
			climate = I18n.format("string.naturescompass.warm");
		} else {
			climate = I18n.format("string.naturescompass.medium");
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
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		drawCenteredString(font, BiomeUtils.getBiomeNameForDisplay(biome), width / 2, 20, 0xffffff);

		drawString(font, I18n.format("string.naturescompass.topBlock"), width / 2 - 100, 40, 0xffffff);
		drawString(font, topBlock, width / 2 - 100, 50, 0x808080);

		drawString(font, I18n.format("string.naturescompass.precipitation"), width / 2 - 100, 70, 0xffffff);
		drawString(font, precipitation, width / 2 - 100, 80, 0x808080);

		drawString(font, I18n.format("string.naturescompass.baseHeight"), width / 2 - 100, 100, 0xffffff);
		drawString(font, baseHeight, width / 2 - 100, 110, 0x808080);

		drawString(font, I18n.format("string.naturescompass.rainfall"), width / 2 - 100, 130, 0xffffff);
		drawString(font, rainfall, width / 2 - 100, 140, 0x808080);

		drawString(font, I18n.format("string.naturescompass.fillerBlock"), width / 2 + 40, 40, 0xffffff);
		drawString(font, fillerBlock, width / 2 + 40, 50, 0x808080);

		drawString(font, I18n.format("string.naturescompass.climate"), width / 2 + 40, 70, 0xffffff);
		drawString(font, climate, width / 2 + 40, 80, 0x808080);

		drawString(font, I18n.format("string.naturescompass.heightVariation"), width / 2 + 40, 100, 0xffffff);
		drawString(font, heightVariation, width / 2 + 40, 110, 0x808080);

		drawString(font, I18n.format("string.naturescompass.highHumidity"), width / 2 + 40, 130, 0xffffff);
		drawString(font, highHumidity, width / 2 + 40, 140, 0x808080);

		super.render(mouseX, mouseY, partialTicks);
	}

	private void setupButtons() {
		buttons.clear();
		backButton = addButton(new GuiTransparentButton(10, height - 30, 110, 20, I18n.format("string.naturescompass.back"), (onPress) -> {
			minecraft.displayGuiScreen(parentScreen);
		}));
		searchButton = addButton(new GuiTransparentButton(width - 120, height - 30, 110, 20, I18n.format("string.naturescompass.search"), (onPress) -> {
			parentScreen.searchForBiome(biome);
		}));
	}

}
