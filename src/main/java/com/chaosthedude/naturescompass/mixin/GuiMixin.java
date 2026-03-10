package com.chaosthedude.naturescompass.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.item.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.RenderUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class GuiMixin {
	
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "TAIL"))
	private void renderCompassInfo(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
		if (minecraft.player != null && minecraft.level != null && !minecraft.options.hideGui && !minecraft.getDebugOverlay().showDebugScreen() && (minecraft.screen == null || (NaturesCompassConfig.displayWithChatOpen && minecraft.screen instanceof ChatScreen))) {
			final Player player = minecraft.player;
			final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
			if (stack != null && stack.getItem() instanceof NaturesCompassItem) {
				final NaturesCompassItem compass = (NaturesCompassItem) stack.getItem();
				if (compass.getCompassState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.status"), 5, 5, 0xffffffff, 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.searching"), 5, 5, 0xffaaaaaa, 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.biome"), 5, 5, 0xffffffff, 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, BiomeUtils.getBiomeName(minecraft.level, Identifier.parse(stack.getOrDefault(NaturesCompass.BIOME_ID, ""))), 5, 5, 0xffaaaaaa, 4);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.radius"), 5, 5, 0xffffffff, 6);
 					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(stack.getOrDefault(NaturesCompass.SEARCH_RADIUS, 0)), 5, 5, 0xffaaaaaa, 7);
				} else if (compass.getCompassState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.status"), 5, 5, 0xffffffff, 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.found"), 5, 5, 0xffaaaaaa, 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.biome"), 5, 5, 0xffffffff, 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, BiomeUtils.getBiomeName(minecraft.level, Identifier.parse(stack.getOrDefault(NaturesCompass.BIOME_ID, ""))), 5, 5, 0xffaaaaaa, 4);

					if (stack.getOrDefault(NaturesCompass.DISPLAY_COORDS, false)) {
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.coordinates"), 5, 5, 0xffffffff, 6);
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, stack.getOrDefault(NaturesCompass.FOUND_X, 0) + ", " + stack.getOrDefault(NaturesCompass.FOUND_Z, 0), 5, 5, 0xffaaaaaa, 7);

						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.distance"), 5, 5, 0xffffffff, 9);
						RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(BiomeUtils.getDistanceToBiome(player, stack.getOrDefault(NaturesCompass.FOUND_X, 0), stack.getOrDefault(NaturesCompass.FOUND_Z, 0))), 5, 5, 0xffaaaaaa, 10);
					}
				} else if (compass.getCompassState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.status"), 5, 5, 0xffffffff, 0);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.notFound"), 5, 5, 0xffaaaaaa, 1);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.biome"), 5, 5, 0xffffffff, 3);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, BiomeUtils.getBiomeName(minecraft.level, Identifier.parse(stack.getOrDefault(NaturesCompass.BIOME_ID, ""))), 5, 5, 0xffaaaaaa, 4);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.radius"), 5, 5, 0xffffffff, 6);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(stack.getOrDefault(NaturesCompass.SEARCH_RADIUS, 0)), 5, 5, 0xffaaaaaa, 7);

					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, I18n.get("string.naturescompass.samples"), 5, 5, 0xffffffff, 9);
					RenderUtils.drawConfiguredStringOnHUD(guiGraphics, String.valueOf(stack.getOrDefault(NaturesCompass.SAMPLES, 0)), 5, 5, 0xffaaaaaa, 10);
				}
			}
		}
	}
	
}