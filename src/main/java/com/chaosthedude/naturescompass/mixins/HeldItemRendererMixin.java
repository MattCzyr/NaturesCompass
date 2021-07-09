package com.chaosthedude.naturescompass.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

	@Shadow
	private ItemStack mainHand;

	@Shadow
	private ItemStack offHand;

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "updateHeldItems()V", at = @At("HEAD"))
	private void cancelCompassAnimation(CallbackInfo ci) {
		ItemStack newMainStack = client.player.getMainHandStack();
		if (newMainStack.getItem() instanceof NaturesCompassItem && mainHand.getItem() instanceof NaturesCompassItem) {
			NaturesCompassItem newMainCompass = (NaturesCompassItem) newMainStack.getItem();
			NaturesCompassItem mainCompass = (NaturesCompassItem) mainHand.getItem();
			if (newMainCompass.getState(newMainStack) == mainCompass.getState(mainHand)) {
				mainHand = newMainStack;
			}
		}

		ItemStack newOffStack = client.player.getOffHandStack();
		if (newOffStack.getItem() instanceof NaturesCompassItem && offHand.getItem() instanceof NaturesCompassItem) {
			NaturesCompassItem newOffCompass = (NaturesCompassItem) newOffStack.getItem();
			NaturesCompassItem offCompass = (NaturesCompassItem) offHand.getItem();
			if (newOffCompass.getState(newOffStack) == offCompass.getState(offHand)) {
				offHand = newOffStack;
			}
		}
	}

}