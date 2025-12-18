package com.chaosthedude.naturescompass.utils;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

@Environment(EnvType.CLIENT)
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
		yOffset += (relLineOffset + NaturesCompassConfig.overlayLineOffset) * 9;
		if (NaturesCompassConfig.overlaySide == OverlaySide.LEFT) {
			drawStringLeft(guiGraphics, string, font, xOffset + 2, yOffset + 2, color);
		} else {
			drawStringRight(guiGraphics, string, font, mc.getWindow().getGuiScaledWidth() - xOffset - 2, yOffset + 2, color);
		}
	}

}
