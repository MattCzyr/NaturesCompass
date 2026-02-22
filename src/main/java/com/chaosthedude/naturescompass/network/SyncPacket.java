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

public record SyncPacket(boolean canTeleport, boolean infiniteXp, List<Identifier> allowedBiomes, Map<Identifier, Integer> xpLevelsForAllowedBiomes, ListMultimap<Identifier, Identifier> dimensionsForAllowedBiomes) implements CustomPacketPayload {

	public static final Type<SyncPacket> TYPE = new Type<SyncPacket>(Identifier.fromNamespaceAndPath(NaturesCompass.MODID, "sync"));
	
	public static final StreamCodec<FriendlyByteBuf, SyncPacket> CODEC = StreamCodec.ofMember(SyncPacket::write, SyncPacket::read);
	
	public static SyncPacket read(FriendlyByteBuf buf) {
		boolean canTeleport = buf.readBoolean();
		boolean infiniteXp = buf.readBoolean();
		
		List<Identifier> allowedBiomes = new ArrayList<Identifier>();
		Map<Identifier, Integer> xpLevelsForAllowedBiomes = new HashMap<Identifier, Integer>();
		ListMultimap<Identifier, Identifier> dimensionsForAllowedBiomes = ArrayListMultimap.create();
		int listSize = buf.readInt();
		for (int i = 0; i < listSize; i++) {
			Identifier biomeId = buf.readIdentifier();
			int numDimensions = buf.readInt();
			List<Identifier> dimensionIds = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionIds.add(buf.readIdentifier());
			}
			
			int xpLevels = buf.readInt();
			
			if (biomeId != null) {
				allowedBiomes.add(biomeId);
				xpLevelsForAllowedBiomes.put(biomeId, xpLevels);
				dimensionsForAllowedBiomes.putAll(biomeId, dimensionIds);
			}
		}
		
		return new SyncPacket(canTeleport, infiniteXp, allowedBiomes, xpLevelsForAllowedBiomes, dimensionsForAllowedBiomes);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeBoolean(infiniteXp);
		
		buf.writeInt(allowedBiomes.size());
		for (Identifier biomeId : allowedBiomes) {
			buf.writeIdentifier(biomeId);
			List<Identifier> dimensionIds = dimensionsForAllowedBiomes.get(biomeId);
			buf.writeInt(dimensionIds.size());
			for (Identifier dimensionId : dimensionIds) {
				buf.writeIdentifier(dimensionId);
			}
			int xpLevels = xpLevelsForAllowedBiomes.get(biomeId);
			buf.writeInt(xpLevels);
		}
	}

	public static void apply(SyncPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			NaturesCompass.canTeleport = packet.canTeleport;
			NaturesCompass.infiniteXp = packet.infiniteXp;
			NaturesCompass.allowedBiomes = packet.allowedBiomes;
			NaturesCompass.xpLevelsForAllowedBiomes = packet.xpLevelsForAllowedBiomes;
			NaturesCompass.dimensionsForAllowedBiomes = packet.dimensionsForAllowedBiomes;
		});
	}
	
	@Override
	public Type<SyncPacket> type() {
		return TYPE;
	}

}
