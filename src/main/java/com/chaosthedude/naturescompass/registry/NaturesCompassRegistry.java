package com.chaosthedude.naturescompass.registry;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = NaturesCompass.MODID)
public class NaturesCompassRegistry {
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> e) {
		e.getRegistry().register(NaturesCompass.naturesCompass);
	}

}
