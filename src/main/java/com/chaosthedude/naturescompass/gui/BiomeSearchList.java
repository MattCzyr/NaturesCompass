package com.chaosthedude.naturescompass.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class BiomeSearchList extends EntryListWidget<BiomeSearchEntry> {

	private final NaturesCompassScreen guiNaturesCompass;

	public BiomeSearchList(NaturesCompassScreen guiNaturesCompass, MinecraftClient mc, int width, int height, int top, int bottom) {
		super(mc, width, height, top, bottom);
		this.guiNaturesCompass = guiNaturesCompass;
		refreshList();
	}
	
	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}
	
	@Override
	protected int getScrollbarX() {
        return getRowLeft() + getRowWidth() - 2;
    }

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float par3) {
		renderList(context, mouseX, mouseY, par3);
	}

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float par5) {
		context.fill(getRowLeft() - 4, getY(), getRowLeft() + getRowWidth() + 4, getY() + getHeight() + 4, 255 / 2 << 24);
		
		enableScissor(context);
		for (int i = 0; i < getEntryCount(); ++i) {
			if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
				BiomeSearchEntry e = children().get(i);
				if (e == getSelectedOrNull()) {
					context.fill(getRowLeft() - 4, getRowTop(i) - 4, getRowLeft() + getRowWidth() + 4, getRowTop(i) + itemHeight, 255 / 2 << 24);
				}

				e.render(context, mouseX, mouseY, e == getHoveredEntry(), par5);
			}
		}
		context.disableScissor();

		if (getMaxScrollY() > 0) {
			int left = getScrollbarX();
			int right = left + 6;
			int height = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) getContentsHeightWithPadding());
			height = MathHelper.clamp(height, 32, getBottom() - getY() - 8);
			int scrollbarTop = (int) getScrollY() * (getBottom() - getY() - height) / getMaxScrollY() + getY();
			if (scrollbarTop < getY()) {
				scrollbarTop = getY();
			}
			
			context.fill(left, scrollbarTop, right, getBottom(), (int) (2.35F * 255.0F) / 2 << 24);
			context.fill(left, scrollbarTop, right, scrollbarTop + height, (int) (1.9F * 255.0F) / 2 << 24);
		}
	}
	
	@Override
	protected void enableScissor(DrawContext context) {
        context.enableScissor(getX(), getY(), getRight(), getBottom());
    }

	@Override
	public int getRowBottom(int index) {
		return getRowTop(index) + itemHeight;
	}

	public void refreshList() {
		clearEntries();
		for (Biome biome : guiNaturesCompass.sortBiomes()) {
			addEntry(new BiomeSearchEntry(this, biome));
		}
		selectBiome(null);
	}

	public void selectBiome(BiomeSearchEntry entry) {
		setSelected(entry);
		guiNaturesCompass.selectBiome(entry);
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