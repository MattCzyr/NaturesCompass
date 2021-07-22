package com.chaosthedude.naturescompass.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.chaosthedude.naturescompass.utils.CompassState;
import com.chaosthedude.naturescompass.utils.ItemUtils;
import com.chaosthedude.naturescompass.utils.RenderUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class HUDMixin {
	
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At(value = "TAIL"))
	private void renderCompassInfo(MatrixStack matrixStack, float tickDelta, CallbackInfo info) {
		if (client.player != null && client.world != null && !client.options.hudHidden && !client.options.debugEnabled && (client.currentScreen == null || (NaturesCompassConfig.displayWithChatOpen && client.currentScreen instanceof ChatScreen))) {
			final PlayerEntity player = client.player;
			final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
			if (stack != null && stack.getItem() instanceof NaturesCompassItem) {
				final NaturesCompassItem compass = (NaturesCompassItem) stack.getItem();
				if (compass.getState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.status"), client.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.searching"), client.textRenderer, 5, 5, 0xAAAAAA, 1);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.biome"), client.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(client.world, compass.getBiomeID(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 4);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.radius"), client.textRenderer, 5, 5, 0xFFFFFF, 6);
 					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSearchRadius(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 7);
				} else if (compass.getState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.status"), client.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.found"), client.textRenderer, 5, 5, 0xAAAAAA, 1);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.biome"), client.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(client.world, compass.getBiomeID(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 4);
	
					if (compass.shouldDisplayCoordinates(stack)) {
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.coordinates"), client.textRenderer, 5, 5, 0xFFFFFF, 6);
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, compass.getFoundBiomeX(stack) + ", " + compass.getFoundBiomeZ(stack), client.textRenderer, 5, 5, 0xAAAAAA, 7);
	
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.distance"), client.textRenderer, 5, 5, 0xFFFFFF, 9);
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(BiomeUtils.getDistanceToBiome(player, compass.getFoundBiomeX(stack), compass.getFoundBiomeZ(stack))), client.textRenderer, 5, 5, 0xAAAAAA, 10);
					}
				} else if (compass.getState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.status"), client.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.notFound"), client.textRenderer, 5, 5, 0xAAAAAA, 1);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.biome"), client.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(client.world, compass.getBiomeID(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 4);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.radius"), client.textRenderer, 5, 5, 0xFFFFFF, 6);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSearchRadius(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 7);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.samples"), client.textRenderer, 5, 5, 0xFFFFFF, 9);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSamples(stack)), client.textRenderer, 5, 5, 0xAAAAAA, 10);
				}
			}
		}
	}
	
}