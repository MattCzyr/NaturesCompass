package com.chaosthedude.naturescompass.mixins;

import org.spongepowered.asm.mixin.Mixin;
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
	
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At(value = "TAIL"))
	private void renderCompassInfo(MatrixStack matrixStack, float tickDelta, CallbackInfo info) {
		if (mc.player != null && mc.world != null && !mc.options.hudHidden && !mc.options.debugEnabled && (mc.currentScreen == null || (NaturesCompassConfig.displayWithChatOpen && mc.currentScreen instanceof ChatScreen))) {
			final PlayerEntity player = mc.player;
			final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
			if (stack != null && stack.getItem() instanceof NaturesCompassItem) {
				final NaturesCompassItem compass = (NaturesCompassItem) stack.getItem();
				if (compass.getState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.status"), mc.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.searching"), mc.textRenderer, 5, 5, 0xAAAAAA, 1);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.biome"), mc.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(mc.world, compass.getBiomeID(stack)), mc.textRenderer, 5, 5, 0xAAAAAA, 4);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.radius"), mc.textRenderer, 5, 5, 0xFFFFFF, 6);
 					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSearchRadius(stack)), mc.textRenderer, 5, 5, 0xAAAAAA, 7);
				} else if (compass.getState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.status"), mc.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.found"), mc.textRenderer, 5, 5, 0xAAAAAA, 1);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.biome"), mc.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(mc.world, compass.getBiomeID(stack)), mc.textRenderer, 5, 5, 0xAAAAAA, 4);
	
					if (compass.shouldDisplayCoordinates(stack)) {
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.coordinates"), mc.textRenderer, 5, 5, 0xFFFFFF, 6);
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, compass.getFoundBiomeX(stack) + ", " + compass.getFoundBiomeZ(stack), mc.textRenderer, 5, 5, 0xAAAAAA, 7);
	
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.distance"), mc.textRenderer, 5, 5, 0xFFFFFF, 9);
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(BiomeUtils.getDistanceToBiome(player, compass.getFoundBiomeX(stack), compass.getFoundBiomeZ(stack))), mc.textRenderer, 5, 5, 0xAAAAAA, 10);
					}
				} else if (compass.getState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.status"), mc.textRenderer, 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.notFound"), mc.textRenderer, 5, 5, 0xAAAAAA, 1);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.biome"), mc.textRenderer, 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(mc.world, compass.getBiomeID(stack)), mc.textRenderer, 5, 5, 0xAAAAAA, 4);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.radius"), mc.textRenderer, 5, 5, 0xFFFFFF, 6);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSearchRadius(stack)), mc.textRenderer, 5, 5, 0xAAAAAA, 7);
	
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.translate("string.naturescompass.samples"), mc.textRenderer, 5, 5, 0xFFFFFF, 9);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSamples(stack)), mc.textRenderer, 5, 5, 0xAAAAAA, 10);
				}
			}
		}
	}
	
}