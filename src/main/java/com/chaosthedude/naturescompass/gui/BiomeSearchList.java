package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchList extends ObjectSelectionList<BiomeSearchEntry> {

	private final NaturesCompassScreen parentScreen;
	private final Player player;

	public BiomeSearchList(NaturesCompassScreen parentScreen, Minecraft mc, Player player, ResourceLocation biomeKeyToSelect, int x, int y, int width, int height, int slotHeight) {
		super(mc, width, height, y, y + height, slotHeight);
		this.parentScreen = parentScreen;
		this.player = player;
        setLeftPos(x);
		refreshList(biomeKeyToSelect);
	}

	@Override
	protected int getScrollbarPosition() {
		return getLeft() + getWidth();
	}

	@Override
	public int getRowWidth() {
		return getWidth();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderList(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderList(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        enableScissor(guiGraphics);
        // Render backgrounds
        for (int i = 0; i < getItemCount(); ++i) {
            if (getRowBottom(i) >= getTop() && getRowTop(i) <= getBottom()) {
                BiomeSearchEntry entry = getEntry(i);
                int fillColor = RenderUtils.getBackgroundColor(entry.isEnabled(), entry == getSelected());
                guiGraphics.fill(getRowLeft(), getRowTop(i), getRight(), getRowBottom(i), fillColor);
            }
        }
        // Render entries
        for (int i = 0; i < getItemCount(); ++i) {
            int top = getRowTop(i);
            int bottom = getRowBottom(i);
            if (bottom >= getTop() && top <= getBottom()) {
                BiomeSearchEntry entry = getEntry(i);
                boolean isHovering = isMouseOver(mouseX, mouseY) && Objects.equals(getEntryAtPosition(mouseX, mouseY), entry);
                entry.render(guiGraphics, i, top, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, isHovering, partialTicks);
            }
        }
        guiGraphics.disableScissor();
        // Render scrollbar
        if (getMaxScroll() > 0) {
            int left = getScrollbarPosition();
            int right = left + 6;
            int height = (int) ((float) ((getBottom() - getTop()) * (getBottom() - getTop())) / (float) getMaxPosition());
            height = Mth.clamp(height, 32, getBottom() - getTop() - 8);
            int top = (int) getScrollAmount() * (getBottom() - getTop() - height) / getMaxScroll() + getTop();
            if (top < getTop()) {
                top = getTop();
            }
            guiGraphics.fill(left, getTop(), right, getBottom(), RenderUtils.getBackgroundColor(false, false));
            guiGraphics.fill(left, top, right, top + height, RenderUtils.getBackgroundColor(true, true));
        }
	}

    @Override
    public void setSelected(BiomeSearchEntry entry) {
        if (entry == null || entry.isEnabled()) {
            super.setSelected(entry);
        }
    }

    public void refreshList(ResourceLocation biomeKeyToSelect) {
		clearEntries();
		for (ResourceLocation biomeKey : parentScreen.sortBiomes()) {
			BiomeSearchEntry entry = new BiomeSearchEntry(this, biomeKey, player);
			addEntry(entry);
			if (biomeKey.equals(biomeKeyToSelect)) {
				setSelected(entry);
			}
		}
		setScrollAmount(0);
	}

	public void refreshList(boolean maintainSelection) {
		ResourceLocation select = maintainSelection && hasSelection() ? getSelected().getBiomeKey() : null;
		refreshList(select);
	}

	public boolean hasSelection() {
		return getSelected() != null;
	}

	public NaturesCompassScreen getParentScreen() {
		return parentScreen;
	}

}
