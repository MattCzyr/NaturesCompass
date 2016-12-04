package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

@SideOnly(Side.CLIENT)
public class GuiListBiomesEntry implements GuiListExtended.IGuiListEntry {

	private final Minecraft mc;
	private final GuiNaturesCompass guiNaturesCompass;
	private final BiomeGenBase biome;
	private final GuiListBiomes biomesList;
	private long lastClickTime;

	public GuiListBiomesEntry(GuiListBiomes biomesList, BiomeGenBase biome) {
		this.biomesList = biomesList;
		this.biome = biome;
		guiNaturesCompass = biomesList.getGuiNaturesCompass();
		mc = Minecraft.getMinecraft();
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
		String precipitationState = I18n.format("string.naturescompass.none");
		if (biome.getEnableSnow()) {
			precipitationState = I18n.format("string.naturescompass.snow");
		} else if (biome.rainfall > 0) {
			precipitationState = I18n.format("string.naturescompass.rain");
		}

		String title = guiNaturesCompass.getSortingCategory().getLocalizedName();
		Object value = guiNaturesCompass.getSortingCategory().getValue(biome);
		if (value == null) {
			title = I18n.format("string.naturescompass.climate");
			if (biome.getTempCategory() == BiomeGenBase.TempCategory.COLD) {
				value = I18n.format("string.naturescompass.cold");
			} else if (biome.getTempCategory() == BiomeGenBase.TempCategory.OCEAN) {
				value = I18n.format("string.naturescompass.ocean");
			} else if (biome.getTempCategory() == BiomeGenBase.TempCategory.WARM) {
				value = I18n.format("string.naturescompass.warm");
			} else {
				value = I18n.format("string.naturescompass.medium");
			}
		}

		mc.fontRenderer.drawString(BiomeUtils.getBiomeName(biome), x + 1, y + 1, 0xffffff);
		mc.fontRenderer.drawString(title + ": " + value, x + 1, y + mc.fontRenderer.FONT_HEIGHT + 3, 0x808080);
		mc.fontRenderer.drawString(I18n.format("string.naturescompass.precipitation") + ": " + precipitationState, x + 1, y + mc.fontRenderer.FONT_HEIGHT + 14, 0x808080);
	}

	@Override
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
		biomesList.selectBiome(slotIndex);
		if (relativeX <= 32 && relativeX < 32) {
			selectBiome();
			return true;
		} else if (Minecraft.getSystemTime() - lastClickTime < 250L) {
			selectBiome();
			return true;
		}

		lastClickTime = Minecraft.getSystemTime();
		return false;
	}

	@Override
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
	}

	public void selectBiome() {
		mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		guiNaturesCompass.searchForBiome(biome);
	}

	public void viewInfo() {
		mc.displayGuiScreen(new GuiBiomeInfo(guiNaturesCompass, biome));
	}

}
