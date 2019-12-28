package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.client.EnumOverlaySide;
import com.chaosthedude.naturescompass.config.ConfigHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {

	private static final Minecraft mc = Minecraft.getInstance();
	private static final FontRenderer fontRenderer = mc.fontRenderer;

	public static void drawStringLeft(String string, FontRenderer fontRenderer, int x, int y, int color) {
        fontRenderer.drawStringWithShadow(string, x, y, color);
    }
    
    public static void drawStringRight(String string, FontRenderer fontRenderer, int x, int y, int color) {
        fontRenderer.drawStringWithShadow(string, x - fontRenderer.getStringWidth(string), y, color);
    }

	public static void drawConfiguredStringOnHUD(String string, int xOffset, int yOffset, int color, int lineOffset) {
		yOffset += lineOffset * 9;
		if (ConfigHandler.CLIENT.overlaySide.get() == EnumOverlaySide.LEFT) {
			drawStringLeft(string, fontRenderer, xOffset + 2, yOffset + 2, color);
		} else {
			drawStringRight(string, fontRenderer, mc.mainWindow.getScaledWidth() - xOffset - 2, yOffset + 2, color);
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

		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color4f(red, green, blue, alpha);

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
