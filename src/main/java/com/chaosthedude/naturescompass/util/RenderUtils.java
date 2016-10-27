package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class RenderUtils {

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final FontRenderer fontRenderer = mc.fontRendererObj;

	public static void drawLineOffsetStringOnHUD(String string, int xOffset, int yOffset, int color, int lineOffset) {
		drawStringOnHUD(string, xOffset, yOffset, color, ConfigHandler.lineOffset + lineOffset);
	}

	public static void drawStringOnHUD(String string, int xOffset, int yOffset, int color, int lineOffset) {
		yOffset += lineOffset * 9;
		fontRenderer.drawString(string, 2 + xOffset, 2 + yOffset, color, true);
	}

}
