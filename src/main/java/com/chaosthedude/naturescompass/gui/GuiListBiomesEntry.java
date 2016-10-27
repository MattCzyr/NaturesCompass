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
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
		String precipitationState = "None";
		if (biome.getEnableSnow()) {
			precipitationState = "Snow";
		} else if (biome.canRain()) {
			precipitationState = "Rain";
		}

		mc.fontRendererObj.drawString(BiomeUtils.getBiomeName(biome), x + 1, y + 1, 0xffffff);
		mc.fontRendererObj.drawString(I18n.format("string.naturescompass.topBlock") + ": " + biome.topBlock.getBlock().getLocalizedName(), x + 1, y + mc.fontRendererObj.FONT_HEIGHT + 3, 0x808080);
		mc.fontRendererObj.drawString(I18n.format("string.naturescompass.precipitation") + ": " + precipitationState, x + 1, y + mc.fontRendererObj.FONT_HEIGHT + 14, 0x808080);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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

	@Override
	public void setSelected(int par1, int par2, int par3) {
	}

	public void selectBiome() {
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		guiNaturesCompass.searchForBiome(biome);
	}

	public void viewInfo() {
		mc.displayGuiScreen(new GuiBiomeInfo(guiNaturesCompass, biome));
	}

}
