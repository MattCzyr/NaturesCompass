package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.sorting.DimensionSorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.sorting.SourceSorting;
import com.chaosthedude.naturescompass.sorting.TagsSorting;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchEntry extends ObjectSelectionList.Entry<BiomeSearchEntry> {

	private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[] {
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1"),
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2"),
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")
	};

	private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[] {
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1_disabled"),
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2_disabled"),
		ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled")
	};

	private final Minecraft mc;
	private final NaturesCompassScreen parentScreen;
	private final Biome biome;
	private final ResourceLocation biomeId;
	private final Player player;
	private final BiomeSearchList biomesList;
	private final String tags;
	private int xpLevels;
	private long lastClickTime;

	public BiomeSearchEntry(BiomeSearchList biomesList, Biome biome, Player player) {
		this.biomesList = biomesList;
		this.biome = biome;
		this.player = player;
		parentScreen = biomesList.getParentScreen();
		mc = Minecraft.getInstance();
		tags = BiomeUtils.getBiomeTags(parentScreen.level, biome);
		biomeId = BiomeUtils.getKeyForBiome(parentScreen.level, biome).orElse(null);

		// Get XP levels to consume
		this.xpLevels = 0;
		if (biomeId != null && NaturesCompass.xpLevelsForAllowedBiomes.containsKey(biomeId)) {
			int levels = NaturesCompass.xpLevelsForAllowedBiomes.get(biomeId);
			if (levels > 3) {
				levels = 3;
			}
			this.xpLevels = levels;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovering, float partialTick) {
		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biome);
		if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting || parentScreen.getSortingCategory() instanceof DimensionSorting) {
			title = I18n.get("string.naturescompass.dimension");
			if (biomeId != null) {
				value = BiomeUtils.dimensionKeysToString(NaturesCompass.dimensionKeysForAllowedBiomeKeys.get(biomeId));
			} else {
				value = "";
			}
		}

		int maxTextWidth = width - 10;

		if (xpLevels > 0) {
			int itemHeight = height + 4;
			int spriteSize = (int) (itemHeight * 0.4F);
			int spriteBorder = (itemHeight - spriteSize) / 2;
			int spriteIndex = xpLevels - 1;
			ResourceLocation spriteId = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
			guiGraphics.blitSprite(spriteId, left + width - spriteSize - spriteBorder, top + spriteBorder - 2, spriteSize, spriteSize);

			// XP sprite is rendered, need extra room for it
			maxTextWidth = width - itemHeight - 5;
		}

		String tagsLine = I18n.get("string.naturescompass.tags") + ": " + tags;
		if (mc.font.width(tagsLine) > maxTextWidth) {
			tagsLine = mc.font.plainSubstrByWidth(tagsLine + "...", maxTextWidth) + "...";
		}

		int nameColor = isEnabled() ? 0xffffffff : 0xff808080;
		int infoColor = isEnabled() ? 0xff808080 : 0xff555555;
		guiGraphics.drawString(mc.font, Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biome)), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 2), nameColor);
		guiGraphics.drawString(mc.font, Component.literal(title + ": " + value), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 1), infoColor);
		guiGraphics.drawString(mc.font, Component.literal(tagsLine), left + 5, top + (height / 2) + ((mc.font.lineHeight + 2) * 0), infoColor);
		guiGraphics.drawString(mc.font, Component.translatable("string.naturescompass.source").append(Component.literal(": " + BiomeUtils.getBiomeSource(parentScreen.level, biome))), left + 5, top + (height / 2) + ((mc.font.lineHeight + 2) * 1), infoColor);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && isEnabled()) {
			biomesList.setSelected(this);
			if (Util.getMillis() - lastClickTime < 250L) {
				parentScreen.searchForBiome(biomeId);
			}
			lastClickTime = Util.getMillis();
			return true;
		}
		return false;
	}

	public boolean isEnabled() {
		return NaturesCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

	public ResourceLocation getBiomeId() {
		return biomeId;
	}

	@Override
	public Component getNarration() {
		return Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biome));
	}

}
