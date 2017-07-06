package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderUtils {

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final FontRenderer fontRenderer = mc.fontRenderer;

	public static void drawLineOffsetStringOnHUD(String string, int xOffset, int yOffset, int color, int lineOffset) {
		drawStringOnHUD(string, xOffset, yOffset, color, ConfigHandler.lineOffset + lineOffset);
	}

	public static void drawStringOnHUD(String string, int xOffset, int yOffset, int color, int lineOffset) {
		yOffset += lineOffset * 9;
		fontRenderer.drawString(string, 2 + xOffset, 2 + yOffset, color, true);
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

		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(red, green, blue, alpha);

		buffer.begin(7, DefaultVertexFormats.POSITION);
		buffer.pos((double) left, (double) bottom, 0.0D).endVertex();
		buffer.pos((double) right, (double) bottom, 0.0D).endVertex();
		buffer.pos((double) right, (double) top, 0.0D).endVertex();
		buffer.pos((double) left, (double) top, 0.0D).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

}
