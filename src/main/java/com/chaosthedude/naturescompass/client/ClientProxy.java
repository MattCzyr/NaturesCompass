package com.chaosthedude.naturescompass.client;

import com.chaosthedude.naturescompass.proxy.CommonProxy;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerEvents() {
		FMLCommonHandler.instance().bus().register(new RenderTickHandler());
	}

}
