package com.chaosthedude.naturescompass.client;

import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.RenderUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

	private static final Minecraft mc = Minecraft.getInstance();

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.END && mc.player != null && mc.world != null && !mc.gameSettings.hideGUI && !mc.gameSettings.showDebugInfo && (mc.currentScreen == null || (ConfigHandler.CLIENT.displayWithChatOpen.get() && mc.currentScreen instanceof ChatScreen))) {
			final PlayerEntity player = mc.player;
			final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
			if (stack != null && stack.getItem() instanceof NaturesCompassItem) {
				MatrixStack matrixStack = new MatrixStack();
				final NaturesCompassItem compass = (NaturesCompassItem) stack.getItem();
				if (compass.getState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.searching"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.biome"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(mc.world, compass.getBiomeKey(stack)), 5, 5, 0xAAAAAA, 4);
					
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.radius"), 5, 5, 0xFFFFFF, 6);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSearchRadius(stack)), 5, 5, 0xAAAAAA, 7);
				} else if (compass.getState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.found"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.biome"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(mc.world, compass.getBiomeKey(stack)), 5, 5, 0xAAAAAA, 4);

					if (compass.shouldDisplayCoordinates(stack)) {
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.coordinates"), 5, 5, 0xFFFFFF, 6);
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, compass.getFoundBiomeX(stack) + ", " + compass.getFoundBiomeZ(stack), 5, 5, 0xAAAAAA, 7);
	
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.distance"), 5, 5, 0xFFFFFF, 9);
						RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(BiomeUtils.getDistanceToBiome(player, compass.getFoundBiomeX(stack), compass.getFoundBiomeZ(stack))), 5, 5, 0xAAAAAA, 10);
					}
				} else if (compass.getState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.notFound"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.biome"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, BiomeUtils.getBiomeName(mc.world, compass.getBiomeKey(stack)), 5, 5, 0xAAAAAA, 4);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.radius"), 5, 5, 0xFFFFFF, 6);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSearchRadius(stack)), 5, 5, 0xAAAAAA, 7);

					RenderUtils.drawConfiguredStringOnHUD(matrixStack, I18n.format("string.naturescompass.samples"), 5, 5, 0xFFFFFF, 9);
					RenderUtils.drawConfiguredStringOnHUD(matrixStack, String.valueOf(compass.getSamples(stack)), 5, 5, 0xAAAAAA, 10);
				}
			}
		}
	}

}
