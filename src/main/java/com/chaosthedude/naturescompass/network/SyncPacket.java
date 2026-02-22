package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncPacket(boolean canTeleport, boolean infiniteXp, List<Identifier> allowedBiomes, Map<Identifier, Integer> xpLevelsForAllowedBiomes, ListMultimap<Identifier, Identifier> dimensionKeysForAllowedBiomes) implements CustomPacketPayload {

	public static final Type<SyncPacket> TYPE = new Type<SyncPacket>(Identifier.fromNamespaceAndPath(NaturesCompass.MODID, "sync"));
	
	public static final StreamCodec<FriendlyByteBuf, SyncPacket> CODEC = StreamCodec.ofMember(SyncPacket::write, SyncPacket::read);
	
	public static SyncPacket read(FriendlyByteBuf buf) {
		boolean canTeleport = buf.readBoolean();
		boolean infiniteXp = buf.readBoolean();
		
		List<Identifier> allowedBiomes = new ArrayList<Identifier>();
		Map<Identifier, Integer> xpLevelsForAllowedBiomes = new HashMap<Identifier, Integer>();
		ListMultimap<Identifier, Identifier> dimensionKeysForAllowedBiomes = ArrayListMultimap.create();
		int listSize = buf.readInt();
		for (int i = 0; i < listSize; i++) {
			Identifier biomeKey = buf.readIdentifier();
			int numDimensions = buf.readInt();
			List<Identifier> dimensionKeys = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionKeys.add(buf.readIdentifier());
			}
			
			int xpLevels = buf.readInt();
			
			if (biomeKey != null) {
				allowedBiomes.add(biomeKey);
				xpLevelsForAllowedBiomes.put(biomeKey, xpLevels);
				dimensionKeysForAllowedBiomes.putAll(biomeKey, dimensionKeys);
			}
		}
		
		return new SyncPacket(canTeleport, infiniteXp, allowedBiomes, xpLevelsForAllowedBiomes, dimensionKeysForAllowedBiomes);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeBoolean(infiniteXp);
		
		buf.writeInt(allowedBiomes.size());
		for (Identifier biomeKey : allowedBiomes) {
			buf.writeIdentifier(biomeKey);
			System.out.println("allowed: " + biomeKey.toString());
			List<Identifier> dimensionKeys = dimensionKeysForAllowedBiomes.get(biomeKey);
			buf.writeInt(dimensionKeys.size());
			for (Identifier dimensionKey : dimensionKeys) {
				buf.writeIdentifier(dimensionKey);
			}
			int xpLevels = xpLevelsForAllowedBiomes.get(biomeKey);
			buf.writeInt(xpLevels);
		}
	}

	public static void apply(SyncPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			NaturesCompass.canTeleport = packet.canTeleport;
			NaturesCompass.infiniteXp = packet.infiniteXp;
			NaturesCompass.allowedBiomes = packet.allowedBiomes;
			NaturesCompass.xpLevelsForAllowedBiomes = packet.xpLevelsForAllowedBiomes;
			NaturesCompass.dimensionsForAllowedBiomes = packet.dimensionKeysForAllowedBiomes;
		});
	}
	
	@Override
	public Type<SyncPacket> type() {
		return TYPE;
	}

}
