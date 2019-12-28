package com.chaosthedude.naturescompass.client;

import com.chaosthedude.naturescompass.proxy.CommonProxy;

import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerEvents() {
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
	}

}
