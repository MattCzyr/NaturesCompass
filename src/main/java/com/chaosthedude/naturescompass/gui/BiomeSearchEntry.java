package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.sorting.DimensionSorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.sorting.SourceSorting;
import com.chaosthedude.naturescompass.sorting.TagsSorting;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class BiomeSearchEntry extends AlwaysSelectedEntryListWidget.Entry<BiomeSearchEntry> {

	private final MinecraftClient mc;
	private final NaturesCompassScreen parentScreen;
	private final Biome biome;
	private final BiomeSearchList biomesList;
	private final String tags;
	private long lastClickTime;

	public BiomeSearchEntry(BiomeSearchList biomesList, Biome biome) {
		this.biomesList = biomesList;
		this.biome = biome;
		parentScreen = biomesList.getGuiNaturesCompass();
		mc = MinecraftClient.getInstance();
		tags = BiomeUtils.getBiomeTags(parentScreen.world, biome);
	}

	@Override
	public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int par6, int par7, boolean par8, float par9) {
		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biome);
		if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting || parentScreen.getSortingCategory() instanceof DimensionSorting) {
			title = I18n.translate("string.naturescompass.dimension");
			Identifier biomeID = BiomeUtils.getIdentifierForBiome(parentScreen.world, biome);
			if (biomeID != null) {
				value = BiomeUtils.dimensionKeysToString(NaturesCompass.dimensionIDsForAllowedBiomeIDs.get(biomeID));
			} else {
				value = "";
			}
		}
		
		String tagsLine = I18n.translate("string.naturescompass.tags") + ": " + BiomeUtils.getBiomeTags(parentScreen.world, biome);
		if (mc.textRenderer.getWidth(tagsLine) > biomesList.getRowWidth()) {
			tagsLine = mc.textRenderer.trimToWidth(tagsLine + "...", biomesList.getRowWidth()) + "...";
		}

		mc.textRenderer.draw(matrixStack, BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome), left + 1, top + 1, 0xffffff);
		mc.textRenderer.draw(matrixStack, title + ": " + value, left + 1, top + mc.textRenderer.fontHeight + 3, 0x808080);
		mc.textRenderer.draw(matrixStack, tagsLine, left + 1, top + mc.textRenderer.fontHeight + 14, 0x808080);
		mc.textRenderer.draw(matrixStack, Text.translatable("string.naturescompass.source").append(": " + BiomeUtils.getBiomeSource(parentScreen.world, biome)), left + 1, top + mc.textRenderer.fontHeight + 25, 0x808080);
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

	@Override
	public Text getNarration() {
		return Text.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome));
	}

}
