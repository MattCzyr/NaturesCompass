package com.chaosthedude.naturescompass.proxy;

import com.chaosthedude.naturescompass.events.NaturesCompassEvents;

import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

	public void registerEvents() {
		MinecraftForge.EVENT_BUS.register(new NaturesCompassEvents());
	}

	public void registerModels() {
	}

}
