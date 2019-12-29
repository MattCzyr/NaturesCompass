package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiListBiomesEntry extends GuiListExtended.IGuiListEntry<GuiListBiomesEntry> {

	private final Minecraft mc;
	private final GuiNaturesCompass guiNaturesCompass;
	private final Biome biome;
	private final GuiListBiomes biomesList;
	private long lastClickTime;

	public GuiListBiomesEntry(GuiListBiomes biomesList, Biome biome) {
		this.biomesList = biomesList;
		this.biome = biome;
		guiNaturesCompass = biomesList.getGuiNaturesCompass();
		mc = Minecraft.getInstance();
	}

	@Override
	public void drawEntry(int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
		String precipitationState = I18n.format("string.naturescompass.none");
		if (biome.getPrecipitation() == RainType.SNOW) {
			precipitationState = I18n.format("string.naturescompass.snow");
		} else if (biome.getPrecipitation() == RainType.RAIN) {
			precipitationState = I18n.format("string.naturescompass.rain");
		}

		String title = guiNaturesCompass.getSortingCategory().getLocalizedName();
		Object value = guiNaturesCompass.getSortingCategory().getValue(biome);
		if (value == null) {
			title = I18n.format("string.naturescompass.topBlock");
			value = I18n.format(biome.getSurfaceBuilderConfig().getTop().getBlock().getTranslationKey());
		}

		mc.fontRenderer.drawString(BiomeUtils.getBiomeNameForDisplay(biome), getX() + 1, getY() + 1, 0xffffff);
		mc.fontRenderer.drawString(title + ": " + value, getX() + 1, getY() + mc.fontRenderer.FONT_HEIGHT + 3, 0x808080);
		mc.fontRenderer.drawString(I18n.format("string.naturescompass.precipitation") + ": " + precipitationState, getX() + 1, getY() + mc.fontRenderer.FONT_HEIGHT + 14, 0x808080);
		mc.fontRenderer.drawString(I18n.format("string.naturescompass.source") + ": " + BiomeUtils.getBiomeSource(biome), getX() + 1, getY() + mc.fontRenderer.FONT_HEIGHT + 25, 0x808080);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		biomesList.selectBiome(getIndex());
		if (Util.milliTime() - lastClickTime < 250L) {
			searchForBiome();
			return true;
		} else {
			lastClickTime = Util.milliTime();
			return false;
		}
	}

	public void searchForBiome() {
		mc.getSoundHandler().play(SimpleSound.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		guiNaturesCompass.searchForBiome(biome);
	}

	public void viewInfo() {
		mc.displayGuiScreen(new GuiBiomeInfo(guiNaturesCompass, biome));
	}

}
