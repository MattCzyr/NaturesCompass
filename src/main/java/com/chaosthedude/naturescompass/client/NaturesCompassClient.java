package com.chaosthedude.naturescompass.client;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = NaturesCompass.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NaturesCompassClient {
	
	@SubscribeEvent
    public static void registerItemModelProperty(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "angle"), NaturesCompassAngle.MAP_CODEC);
    }
	
	@SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "natures_compass"), new NaturesCompassOverlay());
    }

}