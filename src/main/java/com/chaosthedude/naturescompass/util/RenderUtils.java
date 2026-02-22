package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.client.OverlaySide;
import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class RenderUtils {

	private static final Minecraft mc = Minecraft.getInstance();
	private static final Font font = mc.font;

	public static void drawStringLeft(GuiGraphics guiGraphics, String string, Font font, int x, int y, int color) {
		guiGraphics.drawString(font, string, x, y, color, true);
	}

	public static void drawStringRight(GuiGraphics guiGraphics, String string, Font font, int x, int y, int color) {
		guiGraphics.drawString(font, string, x - font.width(string), y, color);
	}

	public static void drawConfiguredStringOnHUD(GuiGraphics guiGraphics, String string, int xOffset, int yOffset, int color, int relLineOffset) {
		yOffset += (relLineOffset + ConfigHandler.CLIENT.overlayLineOffset.get()) * 9;
		if (ConfigHandler.CLIENT.overlaySide.get() == OverlaySide.LEFT) {
			drawStringLeft(guiGraphics, string, font, xOffset + 2, yOffset + 2, color);
		} else {
			drawStringRight(guiGraphics, string, font, mc.getWindow().getGuiScaledWidth() - xOffset - 2, yOffset + 2, color);
		}
	}
	
	public static int getBackgroundColor(boolean active, boolean hovered) {
		float state = 2;
		if (!active) {
			state = 5;
		} else if (hovered) {
			state = 4;
		}
		final float f = state / 2 * 0.9F + 0.1F;
		final int color = (int) (255.0F * f);
		
		return color / 2 << 24;
	}

}
