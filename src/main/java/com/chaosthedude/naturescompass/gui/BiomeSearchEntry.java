package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.sorting.DimensionSorting;
import com.chaosthedude.naturescompass.sorting.NameSorting;
import com.chaosthedude.naturescompass.sorting.SourceSorting;
import com.chaosthedude.naturescompass.sorting.TagsSorting;
import com.chaosthedude.naturescompass.utils.BiomeUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class BiomeSearchEntry extends AlwaysSelectedEntryListWidget.Entry<BiomeSearchEntry> {

	private static final Identifier[] ENABLED_LEVEL_SPRITES = new Identifier[] {
		Identifier.of("container/enchanting_table/level_1"),
		Identifier.of("container/enchanting_table/level_2"),
		Identifier.of("container/enchanting_table/level_3")
	};

	private static final Identifier[] DISABLED_LEVEL_SPRITES = new Identifier[] {
		Identifier.of("container/enchanting_table/level_1_disabled"),
		Identifier.of("container/enchanting_table/level_2_disabled"),
		Identifier.of("container/enchanting_table/level_3_disabled")
	};

	private final MinecraftClient mc;
	private final NaturesCompassScreen parentScreen;
	private final PlayerEntity player;
	private final Identifier biomeId;
	private final BiomeSearchList biomesList;
	private final String tags;
	private int xpLevels;
	private long lastClickTime;

	public BiomeSearchEntry(BiomeSearchList biomesList, Identifier biomeId, PlayerEntity player) {
		this.biomesList = biomesList;
		this.biomeId = biomeId;
		this.player = player;
		parentScreen = biomesList.getGuiNaturesCompass();
		mc = MinecraftClient.getInstance();
		tags = BiomeUtils.getBiomeTags(parentScreen.world, biomeId);

		// Get XP levels to consume
		this.xpLevels = 0;
		if (NaturesCompass.xpLevelsForAllowedBiomes.containsKey(biomeId)) {
			int levels = NaturesCompass.xpLevelsForAllowedBiomes.get(biomeId);
			if (levels > 3) {
				levels = 3;
			}
			this.xpLevels = levels;
		}
	}

    @Override
    public void render(DrawContext context, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovering, float partialTick) {
        String title = parentScreen.getSortingCategory().getLocalizedName();
        Object value = parentScreen.getSortingCategory().getValue(biomeId);
        if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting || parentScreen.getSortingCategory() instanceof DimensionSorting) {
            title = I18n.translate("string.naturescompass.dimension");
            if (biomeId != null) {
                value = BiomeUtils.dimensionKeysToString(NaturesCompass.dimensionIDsForAllowedBiomeIDs.get(biomeId));
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
            Identifier spriteId = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
            context.drawGuiTexture(spriteId, left + width - spriteSize - spriteBorder, top + spriteBorder - 2, spriteSize, spriteSize);

            // XP sprite is rendered, need extra room for it
            maxTextWidth = width - itemHeight - 5;
        }

        String tagsLine = I18n.translate("string.naturescompass.tags") + ": " + tags;
        if (mc.textRenderer.getWidth(tagsLine) > maxTextWidth) {
            tagsLine = mc.textRenderer.trimToWidth(tagsLine + "...", maxTextWidth) + "...";
        }

        int nameColor = isEnabled() ? 0xffffffff : 0xff808080;
        int infoColor = isEnabled() ? 0xff808080 : 0xff555555;
        context.drawText(mc.textRenderer, Text.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biomeId)), left + 5, top + (height / 2) - ((mc.textRenderer.fontHeight + 2) * 2), nameColor, true);
        context.drawText(mc.textRenderer, Text.literal(title + ": " + value), left + 5, top + (height / 2) - ((mc.textRenderer.fontHeight + 2) * 1), infoColor, true);
        context.drawText(mc.textRenderer, Text.literal(tagsLine), left + 5, top + (height / 2) + ((mc.textRenderer.fontHeight + 2) * 0), infoColor, true);
        context.drawText(mc.textRenderer, Text.translatable("string.naturescompass.source").append(Text.literal(": " + BiomeUtils.getBiomeSource(parentScreen.world, biomeId))), left + 5, top + (height / 2) + ((mc.textRenderer.fontHeight + 2) * 1), infoColor, true);
    }

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && isEnabled()) {
			biomesList.setSelected(this);
			if (Util.getMeasuringTimeMs() - lastClickTime < 250L) {
				searchForBiome();
				return true;
			} else {
				lastClickTime = Util.getMeasuringTimeMs();
				return false;
			}
		}
		return false;
	}

	public boolean isEnabled() {
		return NaturesCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

	public Identifier getBiomeId() {
		return biomeId;
	}

	public void searchForBiome() {
		mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForBiome(biomeId);
	}

	@Override
	public Text getNarration() {
		return Text.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biomeId));
	}

}
