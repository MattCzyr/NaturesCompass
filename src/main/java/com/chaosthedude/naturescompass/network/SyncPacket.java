package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncPacket(boolean canTeleport, int maxNextSearches, boolean infiniteXp, List<Identifier> allowedBiomeIDs, Map<Identifier, Integer> xpLevelsForAllowedBiomes, ListMultimap<Identifier, Identifier> dimensionIDsForAllowedBiomeIDs) implements CustomPayload {

	public static final CustomPayload.Id<SyncPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(NaturesCompass.MODID, "sync"));

	public static final PacketCodec<RegistryByteBuf, SyncPacket> PACKET_CODEC = PacketCodec.of(SyncPacket::write, SyncPacket::read);

	public static SyncPacket read(RegistryByteBuf buf) {
		boolean canTeleport = buf.readBoolean();
		int maxNextSearches = buf.readInt();
		boolean infiniteXp = buf.readBoolean();

		List<Identifier> allowedBiomeIDs = new ArrayList<Identifier>();
		Map<Identifier, Integer> xpLevelsForAllowedBiomes = new HashMap<Identifier, Integer>();
		ListMultimap<Identifier, Identifier> dimensionIDsForAllowedBiomeIDs = ArrayListMultimap.create();
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

		return new SyncPacket(canTeleport, maxNextSearches, infiniteXp, allowedBiomeIDs, xpLevelsForAllowedBiomes, dimensionIDsForAllowedBiomeIDs);
	}

	public void write(RegistryByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(maxNextSearches);
		buf.writeBoolean(infiniteXp);

		buf.writeInt(allowedBiomeIDs.size());
		for (Identifier biomeID : allowedBiomeIDs) {
			buf.writeIdentifier(biomeID);
			List<Identifier> dimensionIDs = dimensionIDsForAllowedBiomeIDs.get(biomeID);
			buf.writeInt(dimensionIDs.size());
			for (Identifier dimensionID : dimensionIDs) {
				buf.writeIdentifier(dimensionID);
			}
			int xpLevels = xpLevelsForAllowedBiomes.getOrDefault(biomeID, 0);
			buf.writeInt(xpLevels);
		}
	}

	@Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

    public static void apply(SyncPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			NaturesCompass.synced = true;
	        NaturesCompass.canTeleport = packet.canTeleport();
	        NaturesCompass.maxNextSearches = packet.maxNextSearches();
	        NaturesCompass.infiniteXp = packet.infiniteXp();
	        NaturesCompass.allowedBiomes = packet.allowedBiomeIDs();
	        NaturesCompass.xpLevelsForAllowedBiomes = packet.xpLevelsForAllowedBiomes();
	        NaturesCompass.dimensionIDsForAllowedBiomeIDs = packet.dimensionIDsForAllowedBiomeIDs();
		});
	}

}
