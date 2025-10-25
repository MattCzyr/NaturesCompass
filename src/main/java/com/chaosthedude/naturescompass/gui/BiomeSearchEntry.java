package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.sorting.DimensionSorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.sorting.SourceSorting;
import com.chaosthedude.naturescompass.sorting.TagsSorting;
import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
	public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
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
		
		String tagsLine = I18n.translate("string.naturescompass.tags") + ": " + tags;
		if (mc.textRenderer.getWidth(tagsLine) > biomesList.getRowWidth()) {
			tagsLine = mc.textRenderer.trimToWidth(tagsLine + "...", biomesList.getRowWidth()) + "...";
		}

		context.drawText(mc.textRenderer, BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome), getX() + 1, getY() + 1, 0xffffffff, false);
		context.drawText(mc.textRenderer, title + ": " + value, getX() + 1, getY() + mc.textRenderer.fontHeight + 3, 0xff808080, false);
		context.drawText(mc.textRenderer, tagsLine, getX() + 1, getY() + mc.textRenderer.fontHeight + 14, 0xff808080, false);
		context.drawText(mc.textRenderer, Text.translatable("string.naturescompass.source").append(": " + BiomeUtils.getBiomeSource(parentScreen.world, biome)), getX() + 1, getY() + mc.textRenderer.fontHeight + 25, 0xff808080, false);
	}
	
	@Override
	public boolean mouseClicked(Click click, boolean doubleClick) {
		biomesList.selectBiome(this);
		if (doubleClick) {
			searchForBiome();
		}
		return true;
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
