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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchEntry extends ObjectSelectionList.Entry<BiomeSearchEntry> {

    private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "textures/gui/level_1.png"),
            ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "textures/gui/level_2.png"),
            ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "textures/gui/level_3.png")
    };

    private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "textures/gui/level_1_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "textures/gui/level_2_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "textures/gui/level_3_disabled.png")
    };

	private final Minecraft mc;
	private final NaturesCompassScreen parentScreen;
	private final Player player;
	private final ResourceLocation biomeKey;
	private final BiomeSearchList biomesList;
	private final String tags;
	private final int xpLevels;
	private long lastClickTime;

	public BiomeSearchEntry(BiomeSearchList biomesList, ResourceLocation biomeKey, Player player) {
		this.biomesList = biomesList;
		this.biomeKey = biomeKey;
		this.player = player;
		parentScreen = biomesList.getParentScreen();
		mc = Minecraft.getInstance();
		tags = BiomeUtils.getBiomeTags(parentScreen.level, biomeKey);

		int levels = 0;
		if (NaturesCompass.xpLevelsForAllowedBiomes != null && NaturesCompass.xpLevelsForAllowedBiomes.containsKey(biomeKey)) {
			levels = Math.min(NaturesCompass.xpLevelsForAllowedBiomes.get(biomeKey), 3);
		}
		this.xpLevels = levels;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biomeKey);
		if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting || parentScreen.getSortingCategory() instanceof DimensionSorting) {
			title = I18n.get("string.naturescompass.dimension");
			value = BiomeUtils.dimensionKeysToString(NaturesCompass.dimensionKeysForAllowedBiomeKeys.get(biomeKey));
		}

		int maxTextWidth = width - 10;

		if (xpLevels > 0) {
            int spriteSize = (int) (height * 0.4F);
            int spriteBorder = (height - spriteSize) / 2;
            int spriteIndex = xpLevels - 1;
            ResourceLocation spriteTexture = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
            guiGraphics.blit(spriteTexture, left + width - spriteSize - spriteBorder, top + spriteBorder, spriteSize, spriteSize, 0f, 0f, 16, 16, 16, 16);

            // XP sprite is rendered, need extra room for it
            maxTextWidth = width - height - 5;
		}

		String tagsLine = I18n.get("string.naturescompass.tags") + ": " + tags;
		if (mc.font.width(tagsLine) > maxTextWidth) {
			tagsLine = mc.font.plainSubstrByWidth(tagsLine + "...", maxTextWidth) + "...";
		}

		int nameColor = isEnabled() ? 0xffffff : 0x808080;
		int infoColor = isEnabled() ? 0x808080 : 0x555555;
		guiGraphics.drawString(mc.font, Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biomeKey)), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 2), nameColor);
		guiGraphics.drawString(mc.font, Component.literal(title + ": " + value), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 1), infoColor);
		guiGraphics.drawString(mc.font, Component.literal(tagsLine), left + 5, top + (height / 2) - ((mc.font.lineHeight + 2) * 0), infoColor);
		guiGraphics.drawString(mc.font, Component.translatable("string.naturescompass.source").append(Component.literal(": " + BiomeUtils.getBiomeSource(parentScreen.level, biomeKey))), left + 5, top + (height / 2) + ((mc.font.lineHeight + 2) * 1), infoColor);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && isEnabled()) {
			biomesList.setSelected(this);
			if (Util.getMillis() - lastClickTime < 250L) {
				parentScreen.searchForBiome(biomeKey);
				return true;
			} else {
				lastClickTime = Util.getMillis();
				return false;
			}
		}
		return false;
	}

	public boolean isEnabled() {
		return NaturesCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

	public ResourceLocation getBiomeKey() {
		return biomeKey;
	}

	@Override
	public Component getNarration() {
		return Component.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.level, biomeKey));
	}

}
