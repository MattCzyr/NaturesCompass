package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.RenderUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransparentTextField extends TextFieldWidget {

	private FontRenderer fontRenderer;
	private ITextComponent label;
	private int labelColor = 0x808080;

	private boolean pseudoIsEnabled = true;
	private boolean pseudoEnableBackgroundDrawing = true;
	private int pseudoMaxStringLength = 32;
	private int pseudoLineScrollOffset;
	private int pseudoEnabledColor = 14737632;
	private int pseudoDisabledColor = 7368816;
	private int pseudoCursorCounter;
	private int pseudoSelectionEnd;

	public TransparentTextField(FontRenderer fontRenderer, int x, int y, int width, int height, ITextComponent label) {
		super(fontRenderer, x, y, width, height, label);
		this.fontRenderer = fontRenderer;
		this.label = label;
	}

	@Override
	public void func_230431_b_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (getVisible()) {
			if (pseudoEnableBackgroundDrawing) {
				final int color = (int) (255.0F * 0.55f);
				RenderUtils.drawRect(field_230690_l_, field_230691_m_, field_230690_l_ + field_230688_j_, field_230691_m_ + field_230689_k_, color / 2 << 24);
			}
			boolean showLabel = !func_230999_j_() && getText().isEmpty();
            int i = showLabel ? labelColor : (pseudoIsEnabled ? pseudoEnabledColor : pseudoDisabledColor);
			int j = getCursorPosition() - pseudoLineScrollOffset;
			int k = pseudoSelectionEnd - pseudoLineScrollOffset;
			String text = showLabel ? label.getString() : getText();
			String s = fontRenderer.func_238413_a_(text.substring(pseudoLineScrollOffset), field_230688_j_, false);
			boolean flag = j >= 0 && j <= s.length();
			boolean flag1 = func_230999_j_() && pseudoCursorCounter / 6 % 2 == 0 && flag;
			int l = pseudoEnableBackgroundDrawing ? field_230690_l_ + 4 : field_230690_l_;
			int i1 = pseudoEnableBackgroundDrawing ? field_230691_m_ + (field_230689_k_ - 8) / 2 : field_230691_m_;
			int j1 = l;

			if (k > s.length()) {
				k = s.length();
			}

			if (!s.isEmpty()) {
				String s1 = flag ? s.substring(0, j) : s;
				j1 = fontRenderer.func_238405_a_(matrixStack, s1, (float) l, (float) i1, i);
			}

			boolean flag2 = getCursorPosition() < getText().length() || getText().length() >= pseudoMaxStringLength;
			int k1 = j1;

			if (!flag) {
				k1 = j > 0 ? l + field_230688_j_ : l;
			} else if (flag2) {
				k1 = j1 - 1;
				--j1;
			}

			if (!s.isEmpty() && flag && j < s.length()) {
				j1 = fontRenderer.func_238405_a_(matrixStack, s.substring(j), (float) j1, (float) i1, i);
			}

			if (flag1) {
				if (flag2) {
					RenderUtils.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + fontRenderer.FONT_HEIGHT, -3092272);
				} else {
					fontRenderer.func_238405_a_(matrixStack, "_", (float) k1, (float) i1, i);
				}
			}

			if (k != j) {
				int l1 = l + fontRenderer.getStringWidth(s.substring(0, k));
				drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + fontRenderer.FONT_HEIGHT);
			}
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		pseudoIsEnabled = enabled;
	}

	@Override
	public void setTextColor(int color) {
		super.setTextColor(color);
		pseudoEnabledColor = color;
	}

	@Override
	public void setDisabledTextColour(int color) {
		super.setDisabledTextColour(color);
		pseudoDisabledColor = color;
	}

	@Override
	public void func_230996_d_(boolean isFocused) {
		if (isFocused && !func_230999_j_()) {
			pseudoCursorCounter = 0;
		}
		super.func_230996_d_(isFocused);
	}
	
	@Override
	public void setEnableBackgroundDrawing(boolean enableBackgroundDrawing) {
		super.setEnableBackgroundDrawing(enableBackgroundDrawing);
		pseudoEnableBackgroundDrawing = enableBackgroundDrawing;
	}
	
	@Override
	public void setMaxStringLength(int length) {
		super.setMaxStringLength(length);
		pseudoMaxStringLength = length;
	}
	
	@Override
	public void tick() {
		super.tick();
		pseudoCursorCounter++;
	}
	
	@Override
	public void setSelectionPos(int position) {
		super.setSelectionPos(position);
		int i = getText().length();
	      pseudoSelectionEnd = MathHelper.clamp(position, 0, i);
	      if (fontRenderer != null) {
	         if (pseudoLineScrollOffset > i) {
	            pseudoLineScrollOffset = i;
	         }

	         int j = getAdjustedWidth();
	         String s = fontRenderer.func_238413_a_(getText().substring(this.pseudoLineScrollOffset), j, false);
	         int k = s.length() + pseudoLineScrollOffset;
	         if (pseudoSelectionEnd == pseudoLineScrollOffset) {
	            pseudoLineScrollOffset -= fontRenderer.func_238413_a_(getText(), j, true).length();
	         }

	         if (pseudoSelectionEnd > k) {
	        	 pseudoLineScrollOffset += pseudoSelectionEnd - k;
	         } else if (pseudoSelectionEnd <= pseudoLineScrollOffset) {
	        	 pseudoLineScrollOffset -= pseudoLineScrollOffset - pseudoSelectionEnd;
	         }

	         pseudoLineScrollOffset = MathHelper.clamp(pseudoLineScrollOffset, 0, i);
	      }
	}

	public void setLabel(ITextComponent label) {
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

		if (endX > field_230690_l_ + field_230688_j_) {
			endX = field_230690_l_ + field_230688_j_;
		}

		if (startX > field_230690_l_ + field_230688_j_) {
			startX = field_230690_l_ + field_230688_j_;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos((double) startX, (double) endY, 0.0D).endVertex();
		bufferbuilder.pos((double) endX, (double) endY, 0.0D).endVertex();
		bufferbuilder.pos((double) endX, (double) startY, 0.0D).endVertex();
		bufferbuilder.pos((double) startX, (double) startY, 0.0D).endVertex();
		tessellator.draw();
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}

}