package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Environment(EnvType.CLIENT)
public class RenderUtils {
	
	private static final Minecraft mc = Minecraft.getInstance();
	private static final Font font = mc.font;

	public static void drawStringLeft(GuiGraphicsExtractor guiGraphics, String string, Font font, int x, int y, int color) {
		guiGraphics.text(font, string, x, y, color, true);
	}

	public static void drawStringRight(GuiGraphicsExtractor guiGraphics, String string, Font font, int x, int y, int color) {
		guiGraphics.text(font, string, x - font.width(string), y, color);
	}

	public static void drawConfiguredStringOnHUD(GuiGraphicsExtractor guiGraphics, String string, int xOffset, int yOffset, int color, int relLineOffset) {
		yOffset += (relLineOffset + NaturesCompassConfig.overlayLineOffset) * 9;
		if (NaturesCompassConfig.overlaySide == OverlaySide.LEFT) {
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
			state = 3;
		}
		final float f = state / 2 * 0.9F + 0.1F;
		final int color = (int) (255.0F * f);
		
		return color / 2 << 24;
	}

}
