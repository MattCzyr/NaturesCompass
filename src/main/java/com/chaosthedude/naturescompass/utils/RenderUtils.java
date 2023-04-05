package com.chaosthedude.naturescompass.utils;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class RenderUtils {
	
	public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static void drawStringLeft(MatrixStack matrixStack, String string, TextRenderer textRenderer, int x, int y, int color) {
		textRenderer.drawWithShadow(matrixStack, string, x, y, color);
	}

	public static void drawStringRight(MatrixStack matrixStack, String string, TextRenderer textRenderer, int x, int y, int color) {
		textRenderer.drawWithShadow(matrixStack, string, x - textRenderer.getWidth(string), y, color);
	}

	public static void drawConfiguredStringOnHUD(MatrixStack matrixStack, String string, TextRenderer textRenderer, int xOffset, int yOffset, int color, int relLineOffset) {
		yOffset += (relLineOffset + NaturesCompassConfig.overlayLineOffset) * 9;
		if (NaturesCompassConfig.overlaySide == OverlaySide.LEFT) {
			drawStringLeft(matrixStack, string, textRenderer, xOffset + 2, yOffset + 2, color);
		} else {
			drawStringRight(matrixStack, string, textRenderer, mc.getWindow().getScaledWidth() - xOffset - 2, yOffset + 2, color);
		}
	}

}
