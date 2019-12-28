package com.chaosthedude.naturescompass.registry;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = NaturesCompass.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NaturesCompassRegistry {
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> e) {
		NaturesCompass.naturesCompass = new ItemNaturesCompass();
		e.getRegistry().register(NaturesCompass.naturesCompass);
	}

}
