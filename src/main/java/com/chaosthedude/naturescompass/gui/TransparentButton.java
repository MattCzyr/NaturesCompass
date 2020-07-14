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
	public void func_230431_b_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (field_230694_p_) {
			Minecraft mc = Minecraft.getInstance();
			float state = 2;
			if (!field_230693_o_) {
				state = 5;
			} else if (func_231047_b_(mouseX, mouseY)) {
				state = 4;
			}
			final float f = state / 2 * 0.9F + 0.1F;
			final int color = (int) (255.0F * f);

			RenderUtils.drawRect(field_230690_l_, field_230691_m_, field_230690_l_ + field_230688_j_, field_230691_m_ + field_230689_k_, color / 2 << 24);
			func_238472_a_(matrixStack, mc.fontRenderer, func_230458_i_(), field_230690_l_ + field_230688_j_ / 2, field_230691_m_ + (field_230689_k_ - 8) / 2, 0xffffff);
		}
	}

	protected int getHoverState(boolean mouseOver) {
		int state = 2;
		if (!field_230693_o_) {
			state = 5;
		} else if (mouseOver) {
			state = 4;
		}

		return state;
	}

}
