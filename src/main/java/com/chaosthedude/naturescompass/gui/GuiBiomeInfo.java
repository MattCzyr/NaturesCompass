package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.BiomeGenBase;

@SideOnly(Side.CLIENT)
public class GuiBiomeInfo extends GuiScreen {

	private GuiNaturesCompass parentScreen;
	private BiomeGenBase biome;
	private GuiButton searchButton;
	private GuiButton backButton;
	private String baseHeight;
	private String heightVariation;
	private String precipitation;
	private String climate;
	private String rainfall;
	private String highHumidity;

	public GuiBiomeInfo(GuiNaturesCompass parentScreen, BiomeGenBase biome) {
		this.parentScreen = parentScreen;
		this.biome = biome;

		if (biome.rootHeight < -1) {
			baseHeight = I18n.format("string.naturescompass.veryLow");
		} else if (biome.rootHeight < 0) {
			baseHeight = I18n.format("string.naturescompass.low");
		} else if (biome.rootHeight < 0.4) {
			baseHeight = I18n.format("string.naturescompass.average");
		} else if (biome.rootHeight < 1) {
			baseHeight = I18n.format("string.naturescompass.high");
		} else {
			baseHeight = I18n.format("string.naturescompass.veryHigh");
		}

		if (biome.heightVariation < 0.3) {
			heightVariation = I18n.format("string.naturescompass.average");
		} else if (biome.heightVariation < 0.6) {
			heightVariation = I18n.format("string.naturescompass.high");
		} else {
			heightVariation = I18n.format("string.naturescompass.veryHigh");
		}

		if (biome.getEnableSnow()) {
			precipitation = I18n.format("string.naturescompass.snow");
		} else if (biome.rainfall > 0) {
			precipitation = I18n.format("string.naturescompass.rain");
		} else {
			precipitation = I18n.format("string.naturescompass.none");
		}

		if (biome.getTempCategory() == BiomeGenBase.TempCategory.COLD) {
			climate = I18n.format("string.naturescompass.cold");
		} else if (biome.getTempCategory() == BiomeGenBase.TempCategory.OCEAN) {
			climate = I18n.format("string.naturescompass.ocean");
		} else if (biome.getTempCategory() == BiomeGenBase.TempCategory.WARM) {
			climate = I18n.format("string.naturescompass.warm");
		} else {
			climate = I18n.format("string.naturescompass.medium");
		}

		if (biome.rainfall <= 0) {
			rainfall = I18n.format("string.naturescompass.none");
		} else if (biome.rainfall < 0.2) {
			rainfall = I18n.format("string.naturescompass.veryLow");
		} else if (biome.rainfall < 0.3) {
			rainfall = I18n.format("string.naturescompass.low");
		} else if (biome.rainfall < 0.5) {
			rainfall = I18n.format("string.naturescompass.average");
		} else if (biome.rainfall < 0.85) {
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
	public void initGui() {
		setupButtons();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button == searchButton) {
				parentScreen.searchForBiome(biome);
			} else if (button == backButton) {
				mc.displayGuiScreen(parentScreen);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, BiomeUtils.getBiomeName(biome), width / 2, 20, 0xffffff);

		drawString(fontRendererObj, I18n.format("string.naturescompass.precipitation"), width / 2 - 100, 40, 0xffffff);
		drawString(fontRendererObj, precipitation, width / 2 - 100, 50, 0x808080);

		drawString(fontRendererObj, I18n.format("string.naturescompass.baseHeight"), width / 2 - 100, 70, 0xffffff);
		drawString(fontRendererObj, baseHeight, width / 2 - 100, 80, 0x808080);

		drawString(fontRendererObj, I18n.format("string.naturescompass.rainfall"), width / 2 - 100, 100, 0xffffff);
		drawString(fontRendererObj, rainfall, width / 2 - 100, 110, 0x808080);

		drawString(fontRendererObj, I18n.format("string.naturescompass.climate"), width / 2 + 40, 40, 0xffffff);
		drawString(fontRendererObj, climate, width / 2 + 40, 50, 0x808080);

		drawString(fontRendererObj, I18n.format("string.naturescompass.heightVariation"), width / 2 + 40, 70, 0xffffff);
		drawString(fontRendererObj, heightVariation, width / 2 + 40, 80, 0x808080);

		drawString(fontRendererObj, I18n.format("string.naturescompass.highHumidity"), width / 2 + 40, 100, 0xffffff);
		drawString(fontRendererObj, highHumidity, width / 2 + 40, 110, 0x808080);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected <T extends GuiButton> T addButton(T button) {
		buttonList.add(button);
		return (T) button;
	}

	private void setupButtons() {
		buttonList.clear();
		backButton = addButton(new GuiButton(0, width / 2 - 154, height - 52, 150, 20, I18n.format("string.naturescompass.back")));
		searchButton = addButton(new GuiButton(1, width / 2 + 4, height - 52, 150, 20, I18n.format("string.naturescompass.search")));
	}

}
