package com.chaosthedude.naturescompass;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.SearchPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class NaturesCompass implements ModInitializer {
	
	public static final String MODID = "naturescompass";
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static final NaturesCompassItem NATURES_COMPASS_ITEM = new NaturesCompassItem();
	
	public static boolean canTeleport;
	public static List<Identifier> allowedBiomes;
	
	@Override
	public void onInitialize() {
		NaturesCompassConfig.load();
		
		Registry.register(Registry.ITEM, new Identifier(MODID, "naturescompass"), NATURES_COMPASS_ITEM);
		
		ServerPlayNetworking.registerGlobalReceiver(SearchPacket.ID, SearchPacket::apply);
		ServerPlayNetworking.registerGlobalReceiver(TeleportPacket.ID, TeleportPacket::apply);
		
		allowedBiomes = new ArrayList<Identifier>();
	}
	
}
