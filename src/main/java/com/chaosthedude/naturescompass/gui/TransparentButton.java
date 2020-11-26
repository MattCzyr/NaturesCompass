package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.RenderUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransparentButton extends Button {

	public TransparentButton(int x, int y, int width, int height, ITextComponent label, IPressable onPress) {
		super(x, y, width, height, label, onPress);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			Minecraft mc = Minecraft.getInstance();
			float state = 2;
			if (!active) {
				state = 5;
			} else if (isHovered()) {
				state = 4;
			}
			final float f = state / 2 * 0.9F + 0.1F;
			final int color = (int) (255.0F * f);

			RenderUtils.drawRect(x, y, x + width, y + height, color / 2 << 24);
			drawCenteredString(matrixStack, mc.fontRenderer, getMessage(), x + width / 2, y + (height - 8) / 2, 0xffffff);
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
