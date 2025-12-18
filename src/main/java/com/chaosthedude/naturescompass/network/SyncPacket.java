package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPacket(boolean canTeleport, List<Identifier> allowedBiomes, ListMultimap<Identifier, Identifier> dimensionKeysForAllowedBiomeKeys) implements CustomPacketPayload {

	public static final Type<SyncPacket> TYPE = new Type<SyncPacket>(Identifier.fromNamespaceAndPath(NaturesCompass.MODID, "sync"));
	
	public static final StreamCodec<FriendlyByteBuf, SyncPacket> CODEC = StreamCodec.ofMember(SyncPacket::write, SyncPacket::read);
	
	public static SyncPacket read(FriendlyByteBuf buf) {
		boolean canTeleport = buf.readBoolean();
		List<Identifier> allowedBiomes = new ArrayList<Identifier>();
		ListMultimap<Identifier, Identifier> dimensionKeysForAllowedBiomeKeys = ArrayListMultimap.create();
		
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			Identifier biomeKey = buf.readIdentifier();
			int numDimensions = buf.readInt();
			List<Identifier> dimensionKeys = new ArrayList<Identifier>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionKeys.add(buf.readIdentifier());
			}
			
			if (biomeKey != null) {
				allowedBiomes.add(biomeKey);
				dimensionKeysForAllowedBiomeKeys.putAll(biomeKey, dimensionKeys);
			}
		}
		
		return new SyncPacket(canTeleport, allowedBiomes, dimensionKeysForAllowedBiomeKeys);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedBiomes.size());
		for (Identifier biomeKey : allowedBiomes) {
			buf.writeIdentifier(biomeKey);
			List<Identifier> dimensionKeys = dimensionKeysForAllowedBiomeKeys.get(biomeKey);
			buf.writeInt(dimensionKeys.size());
			for (Identifier dimensionKey : dimensionKeys) {
				buf.writeIdentifier(dimensionKey);
			}
		}
	}

	public static void handle(SyncPacket packet, IPayloadContext context) {
		if (context.flow().isClientbound()) {
			context.enqueueWork(() -> {
				NaturesCompass.canTeleport = packet.canTeleport;
				NaturesCompass.allowedBiomes = packet.allowedBiomes;
				NaturesCompass.dimensionKeysForAllowedBiomeKeys = packet.dimensionKeysForAllowedBiomeKeys;
			});
		}
	}
	
	@Override
	public Type<SyncPacket> type() {
		return TYPE;
	}

}
