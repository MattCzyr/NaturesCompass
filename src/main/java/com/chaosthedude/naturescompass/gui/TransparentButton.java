package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.utils.RenderUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TransparentButton extends ButtonWidget {

	public TransparentButton(int x, int y, int width, int height, Text label, PressAction onPress) {
		super(x, y, width, height, label, onPress);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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

			RenderUtils.drawRect(x, y, x + width, y + height, color / 2 << 24);
			drawCenteredText(matrixStack, mc.textRenderer, getMessage(), x + width / 2, y + (height - 8) / 2, 0xffffff);
		}
	}

	protected int getHoverState(boolean mouseOver) {
		int state = 2;
		if (!active) {
			state = 5;
		} else if (mouseOver) {
			state = 4;
		}

		return state;
	}

}