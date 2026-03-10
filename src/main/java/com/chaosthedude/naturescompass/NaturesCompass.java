package com.chaosthedude.naturescompass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.item.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.SearchForNextPacket;
import com.chaosthedude.naturescompass.network.SearchPacket;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.network.TeleportPacket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.serialization.Codec;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;

public class NaturesCompass implements ModInitializer {

	public static final String MODID = "naturescompass";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static final NaturesCompassItem NATURES_COMPASS_ITEM = new NaturesCompassItem();

	public static final DataComponentType<String> BIOME_ID_COMPONENT = DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
	public static final DataComponentType<Integer> COMPASS_STATE_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> FOUND_X_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> FOUND_Z_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> SEARCH_RADIUS_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Integer> SAMPLES_COMPONENT = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
	public static final DataComponentType<Boolean> DISPLAY_COORDS_COMPONENT = DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
	public static final DataComponentType<List<BlockPos>> PREV_POS_COMPONENT = DataComponentType.<List<BlockPos>>builder().persistent(BlockPos.CODEC.listOf().xmap(ArrayList::new, list -> list)).networkSynchronized(ByteBufCodecs.collection(ArrayList::new, BlockPos.STREAM_CODEC)).build();

	public static boolean synced;
	public static boolean canTeleport;
	public static int maxNextSearches;
	public static boolean infiniteXp;
	public static List<Identifier> allowedBiomes;
	public static Map<Identifier, Integer> xpLevelsForAllowedBiomes;
	public static ListMultimap<Identifier, Identifier> dimensionsForAllowedBiomes;

	@Override
	public void onInitialize() {
		NaturesCompassConfig.load();

		Registry.register(BuiltInRegistries.ITEM, NaturesCompassItem.KEY, NATURES_COMPASS_ITEM);

		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "biome_id"), BIOME_ID_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "compass_state"), COMPASS_STATE_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "found_x"), FOUND_X_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "found_z"), FOUND_Z_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "search_radius"), SEARCH_RADIUS_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "samples"), SAMPLES_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "display_coords"), DISPLAY_COORDS_COMPONENT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MODID, "prev_pos"), PREV_POS_COMPONENT);

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(NATURES_COMPASS_ITEM));

		PayloadTypeRegistry.playC2S().register(SearchPacket.TYPE, SearchPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(SearchForNextPacket.TYPE, SearchForNextPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportPacket.TYPE, TeleportPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(SyncPacket.TYPE, SyncPacket.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SearchPacket.TYPE, SearchPacket::apply);
		ServerPlayNetworking.registerGlobalReceiver(SearchForNextPacket.TYPE, SearchForNextPacket::apply);
		ServerPlayNetworking.registerGlobalReceiver(TeleportPacket.TYPE, TeleportPacket::apply);

		allowedBiomes = new ArrayList<Identifier>();
		xpLevelsForAllowedBiomes = new HashMap<Identifier, Integer>();
		dimensionsForAllowedBiomes = ArrayListMultimap.create();
	}

}
