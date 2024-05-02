package com.chaosthedude.naturescompass;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.CompassSearchPacket;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

@Mod(NaturesCompass.MODID)
public class NaturesCompass {

	public static final String MODID = "naturescompass";

	public static final PermissionNode<Boolean> TELEPORT_PERMISSION = new PermissionNode<>(MODID, "naturescompass.teleport", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> false);

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static SimpleChannel network;
	public static NaturesCompassItem naturesCompass;

	public static boolean canTeleport;
	public static List<ResourceLocation> allowedBiomes;
	public static ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedBiomeKeys;

	public static NaturesCompass instance;

	public NaturesCompass() {
		instance = this;

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::buildCreativeTabContents);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.GENERAL_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void preInit(FMLCommonSetupEvent event) {
		network = ChannelBuilder.named(new ResourceLocation(NaturesCompass.MODID, NaturesCompass.MODID)).networkProtocolVersion(1).optionalClient().clientAcceptedVersions(Channel.VersionTest.exact(1)).simpleChannel();

		// Server packets
		network.messageBuilder(CompassSearchPacket.class).encoder(CompassSearchPacket::toBytes).decoder(CompassSearchPacket::new).consumerMainThread(CompassSearchPacket::handle).add();
		network.messageBuilder(TeleportPacket.class).encoder(TeleportPacket::toBytes).decoder(TeleportPacket::new).consumerMainThread(TeleportPacket::handle).add();

		// Client packet
		network.messageBuilder(SyncPacket.class).encoder(SyncPacket::toBytes).decoder(SyncPacket::new).consumerMainThread(SyncPacket::handle).add();

		allowedBiomes = new ArrayList<ResourceLocation>();
		dimensionKeysForAllowedBiomeKeys = ArrayListMultimap.create();
	}

	private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(new ItemStack(naturesCompass));
		}
	}

	@SubscribeEvent
	public void registerNodes(PermissionGatherEvent.Nodes event) {
		event.addNodes(TELEPORT_PERMISSION);
	}

}
