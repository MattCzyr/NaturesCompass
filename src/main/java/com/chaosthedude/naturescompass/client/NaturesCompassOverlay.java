package com.chaosthedude.naturescompass.client;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.item.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.RenderUtils;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class NaturesCompassOverlay implements GuiLayer {
	
	public static final Minecraft mc = Minecraft.getInstance();

	@Override
	public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (mc.player != null && mc.level != null && !mc.options.hideGui && !mc.getDebugOverlay().showDebugScreen() && (mc.screen == null || (ConfigHandler.CLIENT.displayWithChatOpen.get() && mc.screen instanceof ChatScreen))) {
			final Player player = mc.player;
			final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
			if (stack != null && stack.getItem() instanceof NaturesCompassItem) {
				final NaturesCompassItem compass = (NaturesCompassItem) stack.getItem();
				if (compass.getCompassState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.status"), 5, 5, ARGB.opaque(0xFFFFFF), 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.searching"), 5, 5, ARGB.opaque(0xAAAAAA), 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.biome"), 5, 5, ARGB.opaque(0xFFFFFF), 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, BiomeUtils.getBiomeName(mc.level, Identifier.parse(stack.getOrDefault(NaturesCompass.BIOME_ID, ""))), 5, 5, ARGB.opaque(0xAAAAAA), 4);
					
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.radius"), 5, 5, ARGB.opaque(0xFFFFFF), 6);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(stack.getOrDefault(NaturesCompass.SEARCH_RADIUS, 0)), 5, 5, ARGB.opaque(0xAAAAAA), 7);
				} else if (compass.getCompassState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.status"), 5, 5, ARGB.opaque(0xFFFFFF), 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.found"), 5, 5, ARGB.opaque(0xAAAAAA), 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.biome"), 5, 5, ARGB.opaque(0xFFFFFF), 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, BiomeUtils.getBiomeName(mc.level, Identifier.parse(stack.getOrDefault(NaturesCompass.BIOME_ID, ""))), 5, 5, ARGB.opaque(0xAAAAAA), 4);

					if (stack.getOrDefault(NaturesCompass.DISPLAY_COORDS, false)) {
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.coordinates"), 5, 5, ARGB.opaque(0xFFFFFF), 6);
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, stack.getOrDefault(NaturesCompass.FOUND_X, 0) + ", " + stack.getOrDefault(NaturesCompass.FOUND_Z, 0), 5, 5, ARGB.opaque(0xAAAAAA), 7);
	
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.distance"), 5, 5, ARGB.opaque(0xFFFFFF), 9);
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(BiomeUtils.getDistanceToBiome(player, stack.getOrDefault(NaturesCompass.FOUND_X, 0), stack.getOrDefault(NaturesCompass.FOUND_Z, 0))), 5, 5, ARGB.opaque(0xAAAAAA), 10);
					}
				} else if (compass.getCompassState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.status"), 5, 5, ARGB.opaque(0xFFFFFF), 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.notFound"), 5, 5, ARGB.opaque(0xAAAAAA), 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.biome"), 5, 5, ARGB.opaque(0xFFFFFF), 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, BiomeUtils.getBiomeName(mc.level, Identifier.parse(stack.getOrDefault(NaturesCompass.BIOME_ID, ""))), 5, 5, ARGB.opaque(0xAAAAAA), 4);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.radius"), 5, 5, ARGB.opaque(0xFFFFFF), 6);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(stack.getOrDefault(NaturesCompass.SEARCH_RADIUS, 0)), 5, 5, ARGB.opaque(0xAAAAAA), 7);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.samples"), 5, 5, ARGB.opaque(0xFFFFFF), 9);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(stack.getOrDefault(NaturesCompass.SAMPLES, 0)), 5, 5, ARGB.opaque(0xAAAAAA), 10);
				}
			}
		}
	}

}