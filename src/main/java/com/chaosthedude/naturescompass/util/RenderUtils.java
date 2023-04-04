package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.client.OverlaySide;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {

	private static final Minecraft mc = Minecraft.getInstance();
	private static final Font font = mc.font;

	public static void drawStringLeft(PoseStack poseStack, String string, Font fontRenderer, int x, int y, int color) {
		fontRenderer.drawShadow(poseStack, string, x, y, color);
	}

	public static void drawStringRight(PoseStack poseStack, String string, Font fontRenderer, int x, int y, int color) {
		fontRenderer.drawShadow(poseStack, string, x - fontRenderer.width(string), y, color);
	}

	public static void drawConfiguredStringOnHUD(PoseStack poseStack, String string, int xOffset, int yOffset, int color, int relLineOffset) {
		yOffset += (relLineOffset + ConfigHandler.CLIENT.overlayLineOffset.get()) * 9;
		if (ConfigHandler.CLIENT.overlaySide.get() == OverlaySide.LEFT) {
			drawStringLeft(poseStack, string, font, xOffset + 2, yOffset + 2, color);
		} else {
			drawStringRight(poseStack, string, font, mc.getWindow().getGuiScaledWidth() - xOffset - 2, yOffset + 2, color);
		}
	}

}
