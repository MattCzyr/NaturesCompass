package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.utils.RenderUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
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
	private int pseudoCursorCounter;
	private int pseudoSelectionEnd;

	public TransparentTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text label) {
		super(textRenderer, x, y, width, height, label);
		this.textRenderer = textRenderer;
		this.label = label;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (isVisible()) {
			if (pseudoEnableBackgroundDrawing) {
				final int color = (int) (255.0F * 0.55f);
				RenderUtils.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color / 2 << 24);
			}
			boolean showLabel = !isFocused() && getText().isEmpty();
            int i = showLabel ? labelColor : (pseudoEditable ? pseudoEditableColor : pseudoUneditableColor);
			int j = getCursor() - pseudoLineScrollOffset;
			int k = pseudoSelectionEnd - pseudoLineScrollOffset;
			String text = showLabel ? label.getString() : getText();
			String s = textRenderer.trimToWidth(text.substring(pseudoLineScrollOffset), getWidth());
			boolean flag = j >= 0 && j <= s.length();
			boolean flag1 = isFocused() && pseudoCursorCounter / 6 % 2 == 0 && flag;
			int l = pseudoEnableBackgroundDrawing ? getX() + 4 : getX();
			int i1 = pseudoEnableBackgroundDrawing ? getY() + (getHeight() - 8) / 2 : getY();
			int j1 = l;

			if (k > s.length()) {
				k = s.length();
			}

			if (!s.isEmpty()) {
				String s1 = flag ? s.substring(0, j) : s;
				j1 = textRenderer.drawWithShadow(matrixStack, s1, (float) l, (float) i1, i);
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
				j1 = textRenderer.drawWithShadow(matrixStack, s.substring(j), (float) j1, (float) i1, i);
			}

			if (flag1) {
				if (flag2) {
					RenderUtils.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + textRenderer.fontHeight, -3092272);
				} else {
					textRenderer.drawWithShadow(matrixStack, "_", (float) k1, (float) i1, i);
				}
			}

			if (k != j) {
				int l1 = l + textRenderer.getWidth(s.substring(0, k));
				drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + textRenderer.fontHeight);
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
			pseudoCursorCounter = 0;
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
	public void tick() {
		super.tick();
		pseudoCursorCounter++;
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

	private void drawSelectionBox(int startX, int startY, int endX, int endY) {
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

		if (endX > getX() + width) {
			endX = getX() + width;
		}

		if (startX > getX() + width) {
			startX = getX() + width;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		RenderSystem.setShaderColor(0.0F, 0.0F, 255.0F, 255.0F);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(DrawMode.QUADS, VertexFormats.POSITION);
		bufferbuilder.vertex((double) startX, (double) endY, 0.0D).next();
		bufferbuilder.vertex((double) endX, (double) endY, 0.0D).next();
		bufferbuilder.vertex((double) endX, (double) startY, 0.0D).next();
		bufferbuilder.vertex((double) startX, (double) startY, 0.0D).next();
		tessellator.draw();
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}

}
