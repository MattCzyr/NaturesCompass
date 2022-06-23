package com.chaosthedude.naturescompass.registry;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@EventBusSubscriber(modid = NaturesCompass.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NaturesCompassRegistry {

	@SubscribeEvent
	public static void registerItems(RegisterEvent e) {
		e.register(ForgeRegistries.Keys.ITEMS, helper -> {
            NaturesCompass.naturesCompass = new NaturesCompassItem();
            helper.register(new ResourceLocation(NaturesCompass.MODID, NaturesCompassItem.NAME), NaturesCompass.naturesCompass);
        });
	}

}
