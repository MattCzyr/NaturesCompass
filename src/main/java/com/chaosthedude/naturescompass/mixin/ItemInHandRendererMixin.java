package com.chaosthedude.naturescompass.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.naturescompass.item.NaturesCompassItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	@Shadow
	private ItemStack mainHandItem;

	@Shadow
	private ItemStack offHandItem;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "tick()V", at = @At("HEAD"))
	private void cancelCompassAnimation(CallbackInfo ci) {
		ItemStack newMainStack = minecraft.player.getMainHandItem();
		if (newMainStack.getItem() instanceof NaturesCompassItem && mainHandItem.getItem() instanceof NaturesCompassItem) {
			NaturesCompassItem newMainCompass = (NaturesCompassItem) newMainStack.getItem();
			NaturesCompassItem mainCompass = (NaturesCompassItem) mainHandItem.getItem();
			if (newMainCompass.getCompassState(newMainStack) == mainCompass.getCompassState(mainHandItem)) {
				mainHandItem = newMainStack;
			}
		}

		ItemStack newOffStack = minecraft.player.getOffhandItem();
		if (newOffStack.getItem() instanceof NaturesCompassItem && offHandItem.getItem() instanceof NaturesCompassItem) {
			NaturesCompassItem newOffCompass = (NaturesCompassItem) newOffStack.getItem();
			NaturesCompassItem offCompass = (NaturesCompassItem) offHandItem.getItem();
			if (newOffCompass.getCompassState(newOffStack) == offCompass.getCompassState(offHandItem)) {
				offHandItem = newOffStack;
			}
		}
	}

}