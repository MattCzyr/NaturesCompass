package com.chaosthedude.naturescompass.registry;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = NaturesCompass.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NaturesCompassRegistry {
	
	@SubscribeEvent
	public static void register(RegisterEvent event) {
	    event.register(BuiltInRegistries.ITEM.key(), registry -> {
	    	NaturesCompass.naturesCompass = new NaturesCompassItem();
            registry.register(new ResourceLocation(NaturesCompass.MODID, NaturesCompassItem.NAME), NaturesCompass.naturesCompass);
        });
	    
	    event.register(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), registry -> {
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
