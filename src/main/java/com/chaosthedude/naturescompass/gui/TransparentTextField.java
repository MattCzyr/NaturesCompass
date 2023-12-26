package com.chaosthedude.naturescompass.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class TransparentTextField extends TextFieldWidget {

	private TextRenderer textRenderer;
	private Text label;
	private int labelColor = 0x808080;

	private boolean pseudoEditable = true;
	private boolean pseudoEnableBackgroundDrawing = true;
	private int pseudoMaxLength = 32;
	private int pseudoLineScrollOffset;
	private int pseudoEditableColor = 14737632;
	private int pseudoUneditableColor = 7368816;
	private int pseudoSelectionEnd;
	private long pseudoFocusTime;

	public TransparentTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text label) {
		super(textRenderer, x, y, width, height, label);
		this.textRenderer = textRenderer;
		this.label = label;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		if (isVisible()) {
			if (pseudoEnableBackgroundDrawing) {
				final int color = (int) (255.0F * 0.55f);
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color / 2 << 24);
			}
			boolean showLabel = !isFocused() && getText().isEmpty();
			int i = showLabel ? labelColor : (pseudoEditable ? pseudoEditableColor : pseudoUneditableColor);
			int j = getCursor() - pseudoLineScrollOffset;
			int k = pseudoSelectionEnd - pseudoLineScrollOffset;
			String text = showLabel ? label.getString() : getText();
			String s = textRenderer.trimToWidth(text.substring(pseudoLineScrollOffset), getWidth());
			boolean flag = j >= 0 && j <= s.length();
			boolean flag1 = isFocused() && (Util.getMeasuringTimeMs() - pseudoFocusTime) / 300L % 2L == 0L && flag;
			int l = pseudoEnableBackgroundDrawing ? getX() + 4 : getX();
			int i1 = pseudoEnableBackgroundDrawing ? getY() + (getHeight() - 8) / 2 : getY();
			int j1 = l;

			if (k > s.length()) {
				k = s.length();
			}

			if (!s.isEmpty()) {
				String s1 = flag ? s.substring(0, j) : s;
				j1 = context.drawTextWithShadow(textRenderer, s1, l, i1, i);
			}

			boolean flag2 = getCursor() < getText().length() || getText().length() >= pseudoMaxLength;
			int k1 = j1;

			if (!flag) {
				k1 = j > 0 ? l + width : l;
			} else if (flag2) {
				k1 = j1 - 1;
				--j1;
			}

			if (!s.isEmpty() && flag && j < s.length()) {
				j1 = context.drawTextWithShadow(textRenderer, s.substring(j), j1, i1, i);
			}

			if (flag1) {
				if (flag2) {
					context.fill(RenderLayer.getGuiTextHighlight(), k1, i1 - 1, k1 + 1, i1 + 1 + textRenderer.fontHeight, -3092272);
				} else {
					context.drawTextWithShadow(textRenderer, "_", k1, i1, i);
				}
			}

			if (k != j) {
				int l1 = l + textRenderer.getWidth(s.substring(0, k));
				drawSelectionBox(context, k1, i1 - 1, l1 - 1, i1 + 1 + textRenderer.fontHeight);
			}
		}
	}

	@Override
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		pseudoEditable = editable;
	}

	@Override
	public void setEditableColor(int color) {
		super.setEditableColor(color);
		pseudoEditableColor = color;
	}

	@Override
	public void setUneditableColor(int color) {
		super.setUneditableColor(color);
		pseudoUneditableColor = color;
	}

	@Override
	public void setFocused(boolean isFocused) {
		if (isFocused && !isFocused()) {
			pseudoFocusTime = Util.getMeasuringTimeMs();
		}
		super.setFocused(isFocused);
	}

	@Override
	public void setDrawsBackground(boolean drawsBackground) {
		super.setDrawsBackground(drawsBackground);
		pseudoEnableBackgroundDrawing = drawsBackground;
	}

	@Override
	public void setMaxLength(int length) {
		super.setMaxLength(length);
		pseudoMaxLength = length;
	}

	@Override
	public void setSelectionEnd(int position) {
		super.setSelectionEnd(position);
		int i = getText().length();
		pseudoSelectionEnd = MathHelper.clamp(position, 0, i);
		if (textRenderer != null) {
			if (pseudoLineScrollOffset > i) {
				pseudoLineScrollOffset = i;
			}

			int j = getInnerWidth();
			String s = textRenderer.trimToWidth(getText().substring(pseudoLineScrollOffset), j, false);
			int k = s.length() + pseudoLineScrollOffset;
			if (pseudoSelectionEnd == pseudoLineScrollOffset) {
				pseudoLineScrollOffset -= textRenderer.trimToWidth(getText(), j, true).length();
			}

			if (pseudoSelectionEnd > k) {
				pseudoLineScrollOffset += pseudoSelectionEnd - k;
			} else if (pseudoSelectionEnd <= pseudoLineScrollOffset) {
				pseudoLineScrollOffset -= pseudoLineScrollOffset - pseudoSelectionEnd;
			}

			pseudoLineScrollOffset = MathHelper.clamp(pseudoLineScrollOffset, 0, i);
		}
	}

	public void setLabel(Text label) {
		this.label = label;
	}

	public void setLabelColor(int labelColor) {
		this.labelColor = labelColor;
	}

	private void drawSelectionBox(DrawContext context, int startX, int startY, int endX, int endY) {
		if (startX < endX) {
			int i = startX;
			startX = endX;
			endX = i;
		}

		if (startY < endY) {
			int j = startY;
			startY = endY;
			endY = j;
		}

		if (endX > getX() + getWidth()) {
			endX = getX() + getWidth();
		}

		if (startX > getX() + getWidth()) {
			startX = getX() + getWidth();
		}

		context.fill(RenderLayer.getGuiTextHighlight(), startX, startY, endX, endY, -16776961);
	}

}
