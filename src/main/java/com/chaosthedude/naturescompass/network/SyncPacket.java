package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SyncPacket extends PacketByteBuf {

	public static final Identifier ID = new Identifier(NaturesCompass.MODID, "sync");

	public SyncPacket(boolean canTeleport, int maxNextSearches, boolean infiniteXp, List<Identifier> allowedBiomeIDs, Map<Identifier, Integer> xpLevelsForAllowedBiomes, ListMultimap<Identifier, Identifier> dimensionIDsForAllowedBiomeIDs) {
		super(Unpooled.buffer());
		writeBoolean(canTeleport);
		writeInt(maxNextSearches);
		writeBoolean(infiniteXp);
		writeInt(allowedBiomeIDs.size());
		for (Identifier biomeID : allowedBiomeIDs) {
			writeIdentifier(biomeID);
			List<Identifier> dimensionIDs = dimensionIDsForAllowedBiomeIDs.get(biomeID);
			writeInt(dimensionIDs.size());
			for (Identifier dimensionID : dimensionIDs) {
				writeIdentifier(dimensionID);
			}
			int xpLevels = xpLevelsForAllowedBiomes.containsKey(biomeID) ? xpLevelsForAllowedBiomes.get(biomeID) : 0;
			writeInt(xpLevels);
		}
	}

    public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final boolean canTeleport = buf.readBoolean();
		final int maxNextSearches = buf.readInt();
		final boolean infiniteXp = buf.readBoolean();
		final List<Identifier> allowedBiomeIDs = new ArrayList<Identifier>();
		final Map<Identifier, Integer> xpLevelsForAllowedBiomes = new HashMap<Identifier, Integer>();
		final ListMultimap<Identifier, Identifier> dimensionIDsForAllowedBiomeIDs = ArrayListMultimap.create();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			Identifier biomeID = buf.readIdentifier();
			int numDimensions = buf.readInt();
			List<Identifier> dimensionIDs = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionIDs.add(buf.readIdentifier());
			}
			int xpLevels = buf.readInt();

			if (biomeID != null) {
				allowedBiomeIDs.add(biomeID);
				xpLevelsForAllowedBiomes.put(biomeID, xpLevels);
				dimensionIDsForAllowedBiomeIDs.putAll(biomeID, dimensionIDs);
			}
		}

		client.execute(() -> {
			NaturesCompass.synced = true;
	        NaturesCompass.canTeleport = canTeleport;
	        NaturesCompass.maxNextSearches = maxNextSearches;
	        NaturesCompass.infiniteXp = infiniteXp;
	        NaturesCompass.allowedBiomes = allowedBiomeIDs;
	        NaturesCompass.xpLevelsForAllowedBiomes = xpLevelsForAllowedBiomes;
	        NaturesCompass.dimensionIDsForAllowedBiomeIDs = dimensionIDsForAllowedBiomeIDs;
		});
	}

}
