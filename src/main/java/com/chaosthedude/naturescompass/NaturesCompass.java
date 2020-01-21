package com.chaosthedude.naturescompass;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.naturescompass.client.ClientProxy;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.CompassSearchPacket;
import com.chaosthedude.naturescompass.network.RequestSyncPacket;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.chaosthedude.naturescompass.proxy.CommonProxy;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(NaturesCompass.MODID)
public class NaturesCompass {

	public static final String MODID = "naturescompass";
	public static final String NAME = "Nature's Compass";
	public static final String VERSION = "1.8.4";

	public static final Logger logger = LogManager.getLogger(MODID);

	public static SimpleChannel network;
	public static NaturesCompassItem naturesCompass;

	public static boolean canTeleport;
	public static List<Biome> allowedBiomes;

	public static NaturesCompass instance;

	public static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new CommonProxy());

	public NaturesCompass() {
		instance = this;

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.GENERAL_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
	}

	private void preInit(FMLCommonSetupEvent event) {
		network = NetworkRegistry.newSimpleChannel(new ResourceLocation(NaturesCompass.MODID, NaturesCompass.MODID), () -> "1.0", s -> true, s -> true);

		// Server packets
		network.registerMessage(0, CompassSearchPacket.class, CompassSearchPacket::toBytes, CompassSearchPacket::new, CompassSearchPacket::handle);
		network.registerMessage(1, TeleportPacket.class, TeleportPacket::toBytes, TeleportPacket::new, TeleportPacket::handle);
		network.registerMessage(2, RequestSyncPacket.class, RequestSyncPacket::toBytes, RequestSyncPacket::new, RequestSyncPacket::handle);

		// Client packet
		network.registerMessage(3, SyncPacket.class, SyncPacket::toBytes, SyncPacket::new, SyncPacket::handle);

		proxy.registerEvents();

		allowedBiomes = BiomeUtils.getAllowedBiomes();
	}

}
