package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiListBiomesEntry implements GuiListExtended.IGuiListEntry {

	private final Minecraft mc;
	private final GuiNaturesCompass guiNaturesCompass;
	private final Biome biome;
	private final GuiListBiomes biomesList;
	private long lastClickTime;

	public GuiListBiomesEntry(GuiListBiomes biomesList, Biome biome) {
		this.biomesList = biomesList;
		this.biome = biome;
		guiNaturesCompass = biomesList.getGuiNaturesCompass();
		mc = Minecraft.getMinecraft();
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
		String precipitationState = I18n.format("string.naturescompass.none");
		if (biome.getEnableSnow()) {
			precipitationState = I18n.format("string.naturescompass.snow");
		} else if (biome.canRain()) {
			precipitationState = I18n.format("string.naturescompass.rain");
		}

		String title = guiNaturesCompass.getSortingCategory().getLocalizedName();
		Object value = guiNaturesCompass.getSortingCategory().getValue(biome);
		if (value == null) {
			title = I18n.format("string.naturescompass.topBlock");
			value = biome.topBlock.getBlock().getLocalizedName();
		}

		mc.fontRenderer.drawString(BiomeUtils.getBiomeName(biome), x + 1, y + 1, 0xffffff);
		mc.fontRenderer.drawString(title + ": " + value, x + 1, y + mc.fontRenderer.FONT_HEIGHT + 3, 0x808080);
		mc.fontRenderer.drawString(I18n.format("string.naturescompass.precipitation") + ": " + precipitationState, x + 1, y + mc.fontRenderer.FONT_HEIGHT + 14, 0x808080);
		mc.fontRenderer.drawString("Source: " + BiomeUtils.getBiomeModId(biome), x+1, y + mc.fontRenderer.FONT_HEIGHT + 25, 0x808080);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
		biomesList.selectBiome(slotIndex);
		if (Minecraft.getSystemTime() - lastClickTime < 250L) {
			selectBiome();
			return true;
		}

		lastClickTime = Minecraft.getSystemTime();
		return false;
	}

	@Override
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
	}

	@Override
	public void updatePosition(int par1, int par2, int par3, float par4) {
	}

	public void selectBiome() {
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		guiNaturesCompass.searchForBiome(biome);
	}

	public void viewInfo() {
		mc.displayGuiScreen(new GuiBiomeInfo(guiNaturesCompass, biome));
	}

}
