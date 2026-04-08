package com.chaosthedude.naturescompass.gui;

import java.util.Objects;

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

@Environment(EnvType.CLIENT)
public class BiomeSearchList extends EntryListWidget<BiomeSearchEntry> {

	private final NaturesCompassScreen guiNaturesCompass;
	private final PlayerEntity player;

	public BiomeSearchList(NaturesCompassScreen guiNaturesCompass, MinecraftClient mc, PlayerEntity player, Identifier biomeIDToSelect, int x, int y, int width, int height, int slotHeight) {
		super(mc, width, height, y, y + height, slotHeight);
		this.guiNaturesCompass = guiNaturesCompass;
		this.player = player;
        setLeftPos(x);
		refreshList(biomeIDToSelect);
	}

	@Override
	protected int getScrollbarPositionX() {
		return left + width;
	}

	@Override
	public int getRowWidth() {
		return width;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float par3) {
		renderList(context, mouseX, mouseY, par3);
	}

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float par5) {
        enableScissor(context);
        // Render backgrounds
        for (int i = 0; i < getEntryCount(); ++i) {
            if (getRowBottom(i) >= top && getRowTop(i) <= bottom) {
                BiomeSearchEntry entry = getEntry(i);
                int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelectedOrNull());
                context.fill(getRowLeft(), getRowTop(i), right, getRowBottom(i), fillColor);
            }
        }
        // Render entries
        for (int i = 0; i < getEntryCount(); ++i) {
            int entryTop = getRowTop(i);
            int entryBottom = getRowBottom(i);
            if (entryBottom >= top && entryTop <= bottom) {
                BiomeSearchEntry entry = getEntry(i);
                boolean isHovering = isMouseOver(mouseX, mouseY) && Objects.equals(getEntryAtPosition(mouseX, mouseY), entry);
                entry.render(context, i, entryTop, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, isHovering, par5);
            }
        }
        context.disableScissor();
        // Render scrollbar
        if (getMaxScroll() > 0) {
            int scrollbarLeft = getScrollbarPositionX();
            int scrollbarRight = scrollbarLeft + 6;
            int scrollbarHeight = (int) ((float) ((bottom - top) * (bottom - top)) / (float) getMaxPosition());
            scrollbarHeight = MathHelper.clamp(scrollbarHeight, 32, bottom - top - 8);
            int scrollbarTop = (int) getScrollAmount() * (bottom - top - scrollbarHeight) / getMaxScroll() + top;
            if (scrollbarTop < top) {
                scrollbarTop = top;
            }
            context.fill(scrollbarLeft, top, scrollbarRight, bottom, RenderUtils.getBackgroundColor(false, false));
            context.fill(scrollbarLeft, scrollbarTop, scrollbarRight, scrollbarTop + scrollbarHeight, RenderUtils.getBackgroundColor(true, true));
        }
	}

    @Override
    public void setSelected(BiomeSearchEntry entry) {
        if (entry == null || entry.isEnabled()) {
            super.setSelected(entry);
        }
    }

    public void refreshList(Identifier biomeIDToSelect) {
		clearEntries();
		for (Identifier biomeID : guiNaturesCompass.sortBiomes()) {
			BiomeSearchEntry entry = new BiomeSearchEntry(this, biomeID, player);
			addEntry(entry);
			if (biomeID.equals(biomeIDToSelect)) {
				setSelected(entry);
			}
		}
		setScrollAmount(0);
	}

	public void refreshList(boolean maintainSelection) {
		Identifier select = maintainSelection && hasSelection() ? getSelectedOrNull().getBiomeID() : null;
		refreshList(select);
	}

	public boolean hasSelection() {
		return getSelectedOrNull() != null;
	}

	public NaturesCompassScreen getGuiNaturesCompass() {
		return guiNaturesCompass;
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
	}

}
