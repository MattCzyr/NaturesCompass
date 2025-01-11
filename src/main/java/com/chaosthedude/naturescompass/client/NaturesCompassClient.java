package com.chaosthedude.naturescompass.client;

import java.lang.reflect.Field;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = NaturesCompass.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NaturesCompassClient {

	private static final Field LAYERS = ObfuscationReflectionHelper.findField(Gui.class, "layers");
	private static final Field ID_MAPPER = ObfuscationReflectionHelper.findField(RangeSelectItemModelProperties.class, "ID_MAPPER");

	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			try {
				LayeredDraw layers = (LayeredDraw) LAYERS.get(mc.gui);
				layers.add(new NaturesCompassOverlay());
			} catch (IllegalAccessException e) {
				NaturesCompass.LOGGER.error("Failed to add Nature's Compass GUI layer");
				throw new RuntimeException("Failed to add layer");
			}
		});
	}

	@SubscribeEvent
	public static void constructMod(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			try {
				ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends RangeSelectItemModelProperty>> idMapper = (ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends RangeSelectItemModelProperty>>) ID_MAPPER.get(null);
				idMapper.put(ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "angle"), NaturesCompassAngle.MAP_CODEC);
			} catch (IllegalAccessException e) {
				NaturesCompass.LOGGER.error("Failed to register Nature's Compass model property");
				throw new RuntimeException("Failed to register model property");
			}
		});
	}

}
