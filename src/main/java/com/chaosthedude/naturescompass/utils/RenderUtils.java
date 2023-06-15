package com.chaosthedude.naturescompass.utils;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

@Environment(EnvType.CLIENT)
public class RenderUtils {
	
	public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static void drawStringLeft(DrawContext context, String string, TextRenderer textRenderer, int x, int y, int color) {
		context.drawTextWithShadow(textRenderer, string, x, y, color);
	}

	public static void drawStringRight(DrawContext context, String string, TextRenderer textRenderer, int x, int y, int color) {
		context.drawTextWithShadow(textRenderer, string, x - textRenderer.getWidth(string), y, color);
	}

	public static void drawConfiguredStringOnHUD(DrawContext context, String string, TextRenderer textRenderer, int xOffset, int yOffset, int color, int relLineOffset) {
		yOffset += (relLineOffset + NaturesCompassConfig.overlayLineOffset) * 9;
		if (NaturesCompassConfig.overlaySide == OverlaySide.LEFT) {
			drawStringLeft(context, string, textRenderer, xOffset + 2, yOffset + 2, color);
		} else {
			drawStringRight(context, string, textRenderer, mc.getWindow().getScaledWidth() - xOffset - 2, yOffset + 2, color);
		}
	}

}
