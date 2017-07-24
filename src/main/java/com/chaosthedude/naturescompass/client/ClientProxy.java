package com.chaosthedude.naturescompass.client;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.proxy.CommonProxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerEvents() {
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
	}

}
