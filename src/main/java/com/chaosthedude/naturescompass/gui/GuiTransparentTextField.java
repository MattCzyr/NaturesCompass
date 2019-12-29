package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.RenderUtils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTransparentTextField extends GuiTextField {

	private FontRenderer fontRenderer;
	private String label;
	private int labelColor = 0x808080;

	boolean pseudoIsEnabled = true;
	int pseudoLineScrollOffset;
	int pseudoEnabledColor = 14737632;
	int pseudoDisabledColor = 7368816;
	int pseudoCursorCounter;

	public GuiTransparentTextField(int componentId, FontRenderer fontRenderer, int x, int y, int width, int height) {
		super(componentId, fontRenderer, x, y, width, height);
		this.fontRenderer = fontRenderer;
	}

	@Override
	public void drawTextField(int mouseX, int mouseY, float partialTicks) {
		if (getVisible()) {
			if (getEnableBackgroundDrawing()) {
				final int color = (int) (255.0F * 0.55f);
				RenderUtils.drawRect(x, y, x + width, y + height, color / 2 << 24);
			}
			boolean showLabel = !isFocused() && getText().isEmpty();
            int i = showLabel ? labelColor : (pseudoIsEnabled ? pseudoEnabledColor : pseudoDisabledColor);
			int j = getCursorPosition() - pseudoLineScrollOffset;
			int k = getSelectionEnd() - pseudoLineScrollOffset;
			String text = showLabel ? label : getText();
			String s = fontRenderer.trimStringToWidth(text.substring(pseudoLineScrollOffset), getWidth());
			boolean flag = j >= 0 && j <= s.length();
			boolean flag1 = isFocused() && pseudoCursorCounter / 6 % 2 == 0 && flag;
			int l = getEnableBackgroundDrawing() ? x + 4 : x;
			int i1 = getEnableBackgroundDrawing() ? y + (height - 8) / 2 : y;
			int j1 = l;

			if (k > s.length()) {
				k = s.length();
			}

			if (!s.isEmpty()) {
				String s1 = flag ? s.substring(0, j) : s;
				j1 = fontRenderer.drawStringWithShadow(s1, (float) l, (float) i1, i);
			}

			boolean flag2 = getCursorPosition() < getText().length() || getText().length() >= getMaxStringLength();
			int k1 = j1;

			if (!flag) {
				k1 = j > 0 ? l + this.width : l;
			} else if (flag2) {
				k1 = j1 - 1;
				--j1;
			}

			if (!s.isEmpty() && flag && j < s.length()) {
				j1 = fontRenderer.drawStringWithShadow(s.substring(j), (float) j1, (float) i1, i);
			}

			if (flag1) {
				if (flag2) {
					Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + fontRenderer.FONT_HEIGHT, -3092272);
				} else {
					fontRenderer.drawStringWithShadow("_", (float) k1, (float) i1, i);
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
	public void setFocused(boolean isFocused) {
		if (isFocused && !isFocused()) {
			pseudoCursorCounter = 0;
		}
		super.setFocused(isFocused);
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

		if (position > i) {
			position = i;
		}

		if (position < 0) {
			position = 0;
		}

		if (fontRenderer != null) {
			if (pseudoLineScrollOffset > i) {
				pseudoLineScrollOffset = i;
			}

			int j = getWidth();
			String s = fontRenderer.trimStringToWidth(getText().substring(pseudoLineScrollOffset), j);
			int k = s.length() + pseudoLineScrollOffset;

			if (position == pseudoLineScrollOffset) {
				pseudoLineScrollOffset -= fontRenderer.trimStringToWidth(getText(), j, true).length();
			}

			if (position > k) {
				pseudoLineScrollOffset += position - k;
			} else if (position <= pseudoLineScrollOffset) {
				pseudoLineScrollOffset -= pseudoLineScrollOffset - position;
			}

			pseudoLineScrollOffset = MathHelper.clamp(pseudoLineScrollOffset, 0, i);
		}
	}

	public void setLabel(String label) {
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

		if (endX > x + width) {
			endX = x + width;
		}

		if (startX > x + width) {
			startX = x + width;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.color4f(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos((double) startX, (double) endY, 0.0D).endVertex();
		bufferbuilder.pos((double) endX, (double) endY, 0.0D).endVertex();
		bufferbuilder.pos((double) endX, (double) startY, 0.0D).endVertex();
		bufferbuilder.pos((double) startX, (double) startY, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

}