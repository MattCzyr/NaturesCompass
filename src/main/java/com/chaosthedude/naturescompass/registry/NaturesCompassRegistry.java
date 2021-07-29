package com.chaosthedude.naturescompass.registry;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = NaturesCompass.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NaturesCompassRegistry {

	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> e) {
		NaturesCompass.naturesCompass = new NaturesCompassItem();
		e.getRegistry().register(NaturesCompass.naturesCompass);
	}

}
