package com.chaosthedude.naturescompass.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TransparentButton extends ButtonWidget {

	public TransparentButton(int x, int y, int width, int height, Text label, PressAction onPress) {
		super(x, y, width, height, label, onPress, DEFAULT_NARRATION_SUPPLIER);
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			MinecraftClient mc = MinecraftClient.getInstance();
			float state = 2;
			if (!active) {
				state = 5;
			} else if (isHovered()) {
				state = 4;
			}
			final float f = state / 2 * 0.9F + 0.1F;
			final int color = (int) (255.0F * f);	

			context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color / 2 << 24);
			context.drawCenteredTextWithShadow(mc.textRenderer, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xffffff);
		}
	}

}