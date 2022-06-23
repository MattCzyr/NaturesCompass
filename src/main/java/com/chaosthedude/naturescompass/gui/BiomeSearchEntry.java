package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.sorting.TagsSorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.sorting.SourceSorting;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Precipitation;

@Environment(EnvType.CLIENT)
public class BiomeSearchEntry extends EntryListWidget.Entry<BiomeSearchEntry> {

	private final MinecraftClient mc;
	private final NaturesCompassScreen parentScreen;
	private final Biome biome;
	private final BiomeSearchList biomesList;
	private long lastClickTime;

	public BiomeSearchEntry(BiomeSearchList biomesList, Biome biome) {
		this.biomesList = biomesList;
		this.biome = biome;
		parentScreen = biomesList.getGuiNaturesCompass();
		mc = MinecraftClient.getInstance();
	}

	@Override
	public void render(MatrixStack matrixStack, int par1, int par2, int par3, int par4, int par5, int par6, int par7, boolean par8, float par9) {
		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biome);
		if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting) {
			title = I18n.translate("string.naturescompass.precipitation");
			value = I18n.translate("string.naturescompass.none");
			if (biome.getPrecipitation() == Precipitation.SNOW) {
				value = I18n.translate("string.naturescompass.snow");
			} else if (biome.getPrecipitation() == Precipitation.RAIN) {
				value = I18n.translate("string.naturescompass.rain");
			}
		}
		
		String tagsLine = I18n.translate("string.naturescompass.tags") + ": " + BiomeUtils.getBiomeTags(parentScreen.world, biome);
		if (mc.textRenderer.getWidth(tagsLine) > biomesList.getRowWidth()) {
			tagsLine = mc.textRenderer.trimToWidth(tagsLine + "...", biomesList.getRowWidth()) + "...";
		}

		mc.textRenderer.draw(matrixStack, BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome), par3 + 1, par2 + 1, 0xffffff);
		mc.textRenderer.draw(matrixStack, title + ": " + value, par3 + 1, par2 + mc.textRenderer.fontHeight + 3, 0x808080);
		mc.textRenderer.draw(matrixStack, tagsLine, par3 + 1, par2 + mc.textRenderer.fontHeight + 14, 0x808080);
		mc.textRenderer.draw(matrixStack, Text.translatable("string.naturescompass.source").append(": " + BiomeUtils.getBiomeSource(parentScreen.world, biome)), par3 + 1, par2 + mc.textRenderer.fontHeight + 25, 0x808080);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			biomesList.selectBiome(this);
			if (Util.getMeasuringTimeMs() - lastClickTime < 250L) {
				searchForBiome();
				return true;
			} else {
				lastClickTime = Util.getMeasuringTimeMs();
				return false;
			}
		}
		return false;
	}

	public void searchForBiome() {
		mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForBiome(biome);
	}

	public void viewInfo() {
		mc.setScreen(new BiomeInfoScreen(parentScreen, biome));
	}

}
