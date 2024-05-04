package com.chaosthedude.naturescompass.registry;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = NaturesCompass.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NaturesCompassRegistry {
	
	@SubscribeEvent
	public static void register(RegisterEvent event) {
	    event.register(BuiltInRegistries.ITEM.key(), registry -> {
	    	NaturesCompass.naturesCompass = new NaturesCompassItem();
            registry.register(new ResourceLocation(NaturesCompass.MODID, NaturesCompassItem.NAME), NaturesCompass.naturesCompass);
        });
	}

}
