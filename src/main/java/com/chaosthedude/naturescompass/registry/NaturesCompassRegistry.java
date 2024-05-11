package com.chaosthedude.naturescompass.registry;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.core.registries.BuiltInRegistries;
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
		
		e.register(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), registry -> {
	    	registry.register(new ResourceLocation(NaturesCompass.MODID, "biome_id"), NaturesCompass.BIOME_ID);
	    	registry.register(new ResourceLocation(NaturesCompass.MODID, "compass_state"), NaturesCompass.COMPASS_STATE);
	    	registry.register(new ResourceLocation(NaturesCompass.MODID, "found_x"), NaturesCompass.FOUND_X);
	    	registry.register(new ResourceLocation(NaturesCompass.MODID, "found_z"), NaturesCompass.FOUND_Z);
	    	registry.register(new ResourceLocation(NaturesCompass.MODID, "search_radius"), NaturesCompass.SEARCH_RADIUS);
	    	registry.register(new ResourceLocation(NaturesCompass.MODID, "samples"), NaturesCompass.SAMPLES);
	    	registry.register(new ResourceLocation(NaturesCompass.MODID, "display_coords"), NaturesCompass.DISPLAY_COORDS);
	    });
	}

}
