package com.chaosthedude.naturescompass.utils;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
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

	public static void drawRect(int left, int top, int right, int bottom, int color) {
		if (left < right) {
			int temp = left;
			left = right;
			right = temp;
		}

		if (top < bottom) {
			int temp = top;
			top = bottom;
			bottom = temp;
		}

		final float red = (float) (color >> 16 & 255) / 255.0F;
		final float green = (float) (color >> 8 & 255) / 255.0F;
		final float blue = (float) (color & 255) / 255.0F;
		final float alpha = (float) (color >> 24 & 255) / 255.0F;

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();

		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
		RenderSystem.color4f(red, green, blue, alpha);

		buffer.begin(7, VertexFormats.POSITION);
		buffer.vertex((double) left, (double) bottom, 0.0D).next();
		buffer.vertex((double) right, (double) bottom, 0.0D).next();
		buffer.vertex((double) right, (double) top, 0.0D).next();
		buffer.vertex((double) left, (double) top, 0.0D).next();
		tessellator.draw();

		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

}
