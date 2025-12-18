package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncPacket(boolean canTeleport, List<Identifier> allowedBiomeIDs, ListMultimap<Identifier, Identifier> dimensionIDsForAllowedBiomeIDs) implements CustomPacketPayload {

public static final Type<SyncPacket> TYPE = new Type<SyncPacket>(Identifier.fromNamespaceAndPath(NaturesCompass.MODID, "sync"));
	
	public static final StreamCodec<FriendlyByteBuf, SyncPacket> CODEC = StreamCodec.ofMember(SyncPacket::write, SyncPacket::read);

	public static SyncPacket read(FriendlyByteBuf buf) {
		boolean canTeleport = buf.readBoolean();
		List<Identifier>allowedBiomeIDs = new ArrayList<Identifier>();
		ListMultimap<Identifier, Identifier> dimensionIDsForAllowedBiomeIDs = ArrayListMultimap.create();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			Identifier biomeID = buf.readIdentifier();
			int numDimensions = buf.readInt();
			List<Identifier> dimensionIDs = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionIDs.add(buf.readIdentifier());
			}

			if (biomeID != null) {
				allowedBiomeIDs.add(biomeID);
				dimensionIDsForAllowedBiomeIDs.putAll(biomeID, dimensionIDs);
			}
		}
		
		return new SyncPacket(canTeleport, allowedBiomeIDs, dimensionIDsForAllowedBiomeIDs);
	}
	
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedBiomeIDs.size());
		for (Identifier biomeID : allowedBiomeIDs) {
			buf.writeIdentifier(biomeID);
			List<Identifier> dimensionIDs = dimensionIDsForAllowedBiomeIDs.get(biomeID);
			buf.writeInt(dimensionIDs.size());
			for (Identifier dimensionID : dimensionIDs) {
				buf.writeIdentifier(dimensionID);
			}
		}
	}
	
	@Override
	public Type<SyncPacket> type() {
		return TYPE;
	}

    public static void apply(SyncPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
	        NaturesCompass.canTeleport = packet.canTeleport();
	        NaturesCompass.allowedBiomes = packet.allowedBiomeIDs();
	        NaturesCompass.dimensionIDsForAllowedBiomeIDs = packet.dimensionIDsForAllowedBiomeIDs();
		});
	}

}