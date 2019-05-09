package com.chaosthedude.naturescompass.gui;

import java.lang.reflect.Field;

import com.chaosthedude.naturescompass.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;

public class GuiTransparentTextField extends GuiTextField {
	
	private FontRenderer fontRenderer;
	private String label;
	private int labelColor = 0x808080;

	public GuiTransparentTextField(int componentId, FontRenderer fontRenderer, int x, int y, int width, int height) {
		super(componentId, fontRenderer, x, y, width, height);
		this.fontRenderer = fontRenderer;
	}
	
	@Override
	public void drawTextBox() {
		if (getVisible()) {
            if (getEnableBackgroundDrawing()) {
            	final int color = (int) (255.0F * 0.55f);
            	RenderUtils.drawRect(x, y, x + width, y + height, color / 2 << 24);
            }

            // I apologize for having to do this
            Object isEnabledObj = null;
            Object lineScrollOffsetObj = null;
            Object enabledColorObj = null;
            Object disabledColorObj = null;
            Object cursorCounterObj = null;
			try {
				Field isEnabledField = getClass().getSuperclass().getDeclaredField("isEnabled");
				isEnabledField.setAccessible(true);
	            isEnabledObj = isEnabledField.get(this);
	            isEnabledField.setAccessible(false);
				
				Field lineScrollOffsetField = getClass().getSuperclass().getDeclaredField("lineScrollOffset");
				lineScrollOffsetField.setAccessible(true);
	            lineScrollOffsetObj = lineScrollOffsetField.get(this);
	            lineScrollOffsetField.setAccessible(false);
	            
	            Field enabledColorField = getClass().getSuperclass().getDeclaredField("enabledColor");
	            enabledColorField.setAccessible(true);
	            enabledColorObj = enabledColorField.get(this);
	            enabledColorField.setAccessible(false);
	            
	            Field disabledColorField = getClass().getSuperclass().getDeclaredField("disabledColor");
	            disabledColorField.setAccessible(true);
	            disabledColorObj = disabledColorField.get(this);
	            disabledColorField.setAccessible(false);
	            
	            Field cursorCounterField = getClass().getSuperclass().getDeclaredField("cursorCounter");
	            cursorCounterField.setAccessible(true);
	            cursorCounterObj = cursorCounterField.get(this);
	            cursorCounterField.setAccessible(false);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}  catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

            if (isEnabledObj != null && isEnabledObj instanceof Boolean
            		&& lineScrollOffsetObj != null && lineScrollOffsetObj instanceof Integer
            		&& enabledColorObj != null && enabledColorObj instanceof Integer
            		&& disabledColorObj != null && disabledColorObj instanceof Integer
            		&& cursorCounterObj != null && cursorCounterObj instanceof Integer) {
            	
            	boolean isEnabled = (Boolean) isEnabledObj;
            	int lineScrollOffset = (Integer) lineScrollOffsetObj;
            	int enabledColor = (Integer) enabledColorObj;
            	int disabledColor = (Integer) disabledColorObj;
            	int cursorCounter = (Integer) cursorCounterObj;
            	
            	boolean showLabel = !isFocused() && getText().isEmpty();
	            int i = showLabel ? labelColor : (isEnabled ? enabledColor : disabledColor);
	            int j = getCursorPosition() - lineScrollOffset;
	            int k = getSelectionEnd() - lineScrollOffset;
	            String text = showLabel ? label : getText();
	            String s = fontRenderer.trimStringToWidth(text.substring(lineScrollOffset), getWidth());
	            boolean flag = j >= 0 && j <= s.length();
	            boolean flag1 = isFocused() && cursorCounter / 6 % 2 == 0 && flag;
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
	                k1 = j > 0 ? l + width : l;
	            } else if (flag2) {
	                k1 = j1 - 1;
	                j1--;
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
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
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
