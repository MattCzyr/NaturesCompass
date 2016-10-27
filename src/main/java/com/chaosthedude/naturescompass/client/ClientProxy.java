package com.chaosthedude.naturescompass.client;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.proxy.CommonProxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerEvents() {
		MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
	}

	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(NaturesCompass.naturesCompass, 0, new ModelResourceLocation("naturescompass:natures_compass", "inventory"));
	}

}
