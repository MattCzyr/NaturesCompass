package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.utils.RenderUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class BiomeSearchList extends EntryListWidget<BiomeSearchEntry> {

	private final NaturesCompassScreen guiNaturesCompass;
	private PlayerEntity player;

	public BiomeSearchList(NaturesCompassScreen guiNaturesCompass, MinecraftClient mc, PlayerEntity player, Identifier biomeIdToSelect, int x, int y, int width, int height, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		this.guiNaturesCompass = guiNaturesCompass;
		this.player = player;
        setX(x);
		refreshList(biomeIdToSelect);
	}

	@Override
	public int getRowWidth() {
        return getWidth();
    }

	@Override
	protected int getDefaultScrollbarX() {
        return getX() + getWidth();
    }

	@Override
	protected boolean isSelectedEntry(int slotIndex) {
		return slotIndex >= 0 && slotIndex < children().size() ? children().get(slotIndex).equals(getSelectedOrNull()) : false;
	}

    @Override
    public void renderWidget(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        enableScissor(guiGraphics);
        // Render backgrounds
        for (int i = 0; i < getEntryCount(); ++i) {
            if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
                BiomeSearchEntry entry = getEntry(i);
                int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelectedOrNull());
                guiGraphics.fill(getRowLeft(), getRowTop(i), getRight(), getRowBottom(i), fillColor);
            }
        }
        // Render entries
        for (int i = 0; i < getEntryCount(); ++i) {
            int top = getRowTop(i);
            int bottom = getRowBottom(i);
            if (bottom >= getY() && top <= getBottom()) {
                BiomeSearchEntry entry = getEntry(i);
                boolean isHovering = isMouseOver(mouseX, mouseY) && Objects.equals(getEntryAtPosition(mouseX, mouseY), entry);
                entry.render(guiGraphics, i, top, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, isHovering, partialTicks);
            }
        }
        guiGraphics.disableScissor();
        // Render scrollbar
        if (getMaxScroll() > 0) {
            int left = getScrollbarX();
            int right = left + 6;
            int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) getMaxPosition());
            height = MathHelper.clamp(height, 32, getBottom() - getY() - 8);
            int top = (int) getScrollAmount() * (getBottom() - getY() - height) / getMaxScroll() + getY();
            if (top < getY()) {
                top = getY();
            }
            guiGraphics.fill(left, getY(), right, getBottom(), RenderUtils.getBackgroundColor(false, false));
            guiGraphics.fill(left, top, right, top + height, RenderUtils.getBackgroundColor(true, true));
        }
    }

	@Override
	public void setSelected(BiomeSearchEntry entry) {
		if (entry == null || entry.isEnabled()) {
			super.setSelected(entry);
		}
	}

	public void refreshList(Identifier biomeIdToSelect) {
		clearEntries();
		for (Identifier biomeId : guiNaturesCompass.sortBiomes()) {
			BiomeSearchEntry entry = new BiomeSearchEntry(this, biomeId, player);
			addEntry(entry);
			if (biomeId.equals(biomeIdToSelect)) {
				setSelected(entry);
			}
		}
		if (biomeIdToSelect == null) {
			setSelected(null);
		}
	}

	public void refreshList(boolean maintainSelection) {
		Identifier select = maintainSelection && hasSelection() ? getSelectedOrNull().getBiomeId() : null;
		refreshList(select);
	}

	public boolean hasSelection() {
		return getSelectedOrNull() != null;
	}

	public NaturesCompassScreen getGuiNaturesCompass() {
		return guiNaturesCompass;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

}
