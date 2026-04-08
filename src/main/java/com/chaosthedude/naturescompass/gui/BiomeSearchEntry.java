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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class BiomeSearchEntry extends AlwaysSelectedEntryListWidget.Entry<BiomeSearchEntry> {

	private static final Identifier[] ENABLED_LEVEL_SPRITES = new Identifier[] {
			new Identifier(NaturesCompass.MODID, "textures/gui/level_1.png"),
			new Identifier(NaturesCompass.MODID, "textures/gui/level_2.png"),
			new Identifier(NaturesCompass.MODID, "textures/gui/level_3.png")
	};

	private static final Identifier[] DISABLED_LEVEL_SPRITES = new Identifier[] {
			new Identifier(NaturesCompass.MODID, "textures/gui/level_1_disabled.png"),
			new Identifier(NaturesCompass.MODID, "textures/gui/level_2_disabled.png"),
			new Identifier(NaturesCompass.MODID, "textures/gui/level_3_disabled.png")
	};

	private final MinecraftClient mc;
	private final NaturesCompassScreen parentScreen;
	private final PlayerEntity player;
	private final Identifier biomeID;
	private final BiomeSearchList biomesList;
	private final String tags;
	private final int xpLevels;
	private long lastClickTime;

	public BiomeSearchEntry(BiomeSearchList biomesList, Identifier biomeID, PlayerEntity player) {
		this.biomesList = biomesList;
		this.biomeID = biomeID;
		this.player = player;
		parentScreen = biomesList.getGuiNaturesCompass();
		mc = MinecraftClient.getInstance();
		tags = BiomeUtils.getBiomeTags(parentScreen.world, biomeID);

		// Get XP levels to consume
		int levels = 0;
		if (NaturesCompass.xpLevelsForAllowedBiomes.containsKey(biomeID)) {
			levels = NaturesCompass.xpLevelsForAllowedBiomes.get(biomeID);
			if (levels > 3) {
				levels = 3;
			}
		}
		this.xpLevels = levels;
	}

	@Override
	public void render(DrawContext context, int index, int top, int left, int width, int height, int par6, int par7, boolean par8, float par9) {
		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biomeID);
		if (parentScreen.getSortingCategory() instanceof NameSorting || parentScreen.getSortingCategory() instanceof SourceSorting || parentScreen.getSortingCategory() instanceof TagsSorting || parentScreen.getSortingCategory() instanceof DimensionSorting) {
			title = I18n.translate("string.naturescompass.dimension");
			value = BiomeUtils.dimensionKeysToString(NaturesCompass.dimensionIDsForAllowedBiomeIDs.get(biomeID));
		}

		int nameColor = isEnabled() ? 0xffffff : 0x808080;
		int infoColor = isEnabled() ? 0x808080 : 0x555555;

		int maxTextWidth = width - 10;

		if (xpLevels > 0) {
			int spriteSize = (int) (height * 0.4F);
			int spriteBorder = (height - spriteSize) / 2;
			int spriteIndex = xpLevels - 1;
			Identifier spriteTexture = isEnabled() ? ENABLED_LEVEL_SPRITES[spriteIndex] : DISABLED_LEVEL_SPRITES[spriteIndex];
			context.drawTexture(spriteTexture, left + width - spriteSize - spriteBorder, top + spriteBorder, spriteSize, spriteSize, 0f, 0f, 16, 16, 16, 16);

			// XP sprite is rendered, need extra room for it
			maxTextWidth = width - height - 5;
		}

		String tagsLine = I18n.translate("string.naturescompass.tags") + ": " + tags;
		if (mc.textRenderer.getWidth(tagsLine) > maxTextWidth) {
			tagsLine = mc.textRenderer.trimToWidth(tagsLine + "...", maxTextWidth) + "...";
		}

		context.drawText(mc.textRenderer, BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biomeID), left + 5, top + (height / 2) - ((mc.textRenderer.fontHeight + 2) * 2), nameColor, true);
		context.drawText(mc.textRenderer, title + ": " + value, left + 5, top + (height / 2) - ((mc.textRenderer.fontHeight + 2) * 1), infoColor, true);
		context.drawText(mc.textRenderer, tagsLine, left + 5, top + (height / 2) - ((mc.textRenderer.fontHeight + 2) * 0), infoColor, true);
		context.drawText(mc.textRenderer, Text.translatable("string.naturescompass.source").append(": " + BiomeUtils.getBiomeSource(parentScreen.world, biomeID)), left + 5, top + (height / 2) + ((mc.textRenderer.fontHeight + 2) * 1), infoColor, true);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			if (isEnabled()) {
				biomesList.setSelected(this);
				if (Util.getMeasuringTimeMs() - lastClickTime < 250L) {
					searchForBiome();
					return true;
				} else {
					lastClickTime = Util.getMeasuringTimeMs();
					return false;
				}
			}
		}
		return false;
	}

	public boolean isEnabled() {
		return NaturesCompass.infiniteXp || player.experienceLevel >= xpLevels;
	}

	public void searchForBiome() {
		parentScreen.searchForBiome(biomeID);
	}

	public void viewInfo() {
		mc.setScreen(new BiomeInfoScreen(parentScreen, biomeID));
	}

	public Identifier getBiomeID() {
		return biomeID;
	}

	@Override
	public Text getNarration() {
		return Text.literal(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biomeID));
	}

}
