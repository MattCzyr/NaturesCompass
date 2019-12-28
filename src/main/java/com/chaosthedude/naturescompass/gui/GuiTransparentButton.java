package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTransparentButton extends GuiButton {
	
	public GuiTransparentButton(int id, int x, int y, int width, int height, String text) {
		super(id, x, y, width, height, text);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			Minecraft mc = Minecraft.getInstance();
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			final float state = getHoverState(hovered);
			final float f = state / 2 * 0.9F + 0.1F;
			final int color = (int) (255.0F * f);

			RenderUtils.drawRect(x, y, x + width, y + height, color / 2 << 24);
			drawCenteredString(mc.fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, 0xffffff);
		}
	}

	@Override
	protected int getHoverState(boolean mouseOver) {
		int state = 2;
		if (!enabled) {
			state = 5;
		} else if (mouseOver) {
			state = 4;
		}

		return state;
	}

}
