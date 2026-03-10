package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.sorting.DimensionSorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.sorting.SourceSorting;
import com.chaosthedude.naturescompass.sorting.TagsSorting;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class BiomeSearchEntry extends ObjectSelectionList.Entry<BiomeSearchEntry> {

	private static final Identifier[] ENABLED_LEVEL_SPRITES = new Identifier[] {
        Identifier.withDefaultNamespace("container/enchanting_table/level_1"),
        Identifier.withDefaultNamespace("container/enchanting_table/level_2"),
        Identifier.withDefaultNamespace("container/enchanting_table/level_3")
    };

    private static final Identifier[] DISABLED_LEVEL_SPRITES = new Identifier[] {
        Identifier.withDefaultNamespace("container/enchanting_table/level_1_disabled"),
        Identifier.withDefaultNamespace("container/enchanting_table/level_2_disabled"),
        Identifier.withDefaultNamespace("container/enchanting_table/level_3_disabled")
    };

	private final Minecraft mc;
	private final NaturesCompassScreen parentScreen;
	private final Player player;
	private final Identifier biomeId;
	private final BiomeSearchList biomesList;
	private final String tags;
	private int xpLevels;

	public BiomeSearchEntry(BiomeSearchList biomesList, Identifier biomeId, Player player) {
		this.biomesList = biomesList;
		this.biomeId = biomeId;
		this.player = player;
		parentScreen = biomesList.getParentScreen();
		mc = Minecraft.getInstance();
		tags = BiomeUtils.getBiomeTags(parentScreen.level, biomeId);

		// Get XP levels to consume
		this.xpLevels = 0;
		if (NaturesCompass.xpLevelsForAllowedBiomes.containsKey(biomeId)) {
			int xpLevels = NaturesCompass.xpLevelsForAllowedBiomes.get(biomeId);
			if (xpLevels > 3) {
				xpLevels = 3;
			}
			this.xpLevels = xpLevels;
		}
	}

	@Override
	public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biomeId);
		if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting || parentScreen.getSortingCategory() instanceof DimensionSorting) {
			title = I18n.get("string.naturescompass.dimension");
			value = BiomeUtils.dimensionIdsToString(NaturesCompass.dimensionsForAllowedBiomes.get(biomeId));
		}

		int maxTextWidth = getWidth() - 10;

		if (xpLevels > 0) {
			int spriteSize = (int) (getHeight() * 0.4F);
			int spriteBorder = (getHeight() - spriteSize) / 2;
			int spriteIndex = xpLevels - 1;
			Identifier spriteId = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, getX() + getWidth() - spriteSize - spriteBorder, getY() + spriteBorder, spriteSize, spriteSize);

			// XP sprite is rendered, need extra room for it
			maxTextWidth = getWidth() - getHeight() - 5;
		}

		String tagsLine = I18n.get("string.naturescompass.tags") + ": " + tags;
		if (mc.font.width(tagsLine) > maxTextWidth) {
			tagsLine = mc.font.plainSubstrByWidth(tagsLine + "...", maxTextWidth) + "...";
		}

		int nameColor = isEnabled() ? 0xffffffff : 0xff808080;
		int infoColor = isEnabled() ? 0xff808080 : 0xff555555;
		guiGraphics.drawString(mc.font, Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biomeId)), getX() + 5, getY() + (getHeight() / 2) - ((mc.font.lineHeight + 2) * 2), nameColor);
		guiGraphics.drawString(mc.font, Component.literal(title + ": " + value), getX() + 5, getY() + (getHeight() / 2) - ((mc.font.lineHeight + 2) * 1), infoColor);
		guiGraphics.drawString(mc.font, Component.literal(tagsLine), getX() + 5, getY() + (getHeight() / 2) + ((mc.font.lineHeight + 2) * 0), infoColor);
		guiGraphics.drawString(mc.font, Component.translatable("string.naturescompass.source").append(Component.literal(": " + BiomeUtils.getBiomeSource(parentScreen.level, biomeId))), getX() + 5, getY() + (getHeight() / 2) + ((mc.font.lineHeight + 2) * 1), infoColor);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (isEnabled()) {
			biomesList.setSelected(this);
			if (doubleClick) {
				parentScreen.searchForBiome(biomeId);
			}
		}
		return true;
	}

	public boolean isEnabled() {
		// TODO: is it safe to trust the ClientPlayer's experience level or should it be included in a SyncPacket?
		return NaturesCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

	public Identifier getBiomeId() {
		return biomeId;
	}

	@Override
	public Component getNarration() {
		return Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biomeId));
	}

}
