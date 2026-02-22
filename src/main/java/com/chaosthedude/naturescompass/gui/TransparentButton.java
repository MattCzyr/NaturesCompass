package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.utils.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class TransparentButton extends Button {

	public TransparentButton(int x, int y, int width, int height, Component label, OnPress onPress) {
		super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
	}

	@Override
	public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			Minecraft mc = Minecraft.getInstance();
			int color = RenderUtils.getBackgroundColor(active, isHovered);

			guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
			guiGraphics.drawCenteredString(mc.font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xffffffff);
		}
	}

}
