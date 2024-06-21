package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPacket(boolean canTeleport, List<ResourceLocation> allowedBiomes, ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedBiomeKeys) implements CustomPacketPayload {

	public static final Type<SyncPacket> TYPE = new Type<SyncPacket>(ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "sync"));
	
	public static final StreamCodec<FriendlyByteBuf, SyncPacket> CODEC = StreamCodec.ofMember(SyncPacket::write, SyncPacket::read);
	
	public static SyncPacket read(FriendlyByteBuf buf) {
		boolean canTeleport = buf.readBoolean();
		List<ResourceLocation> allowedBiomes = new ArrayList<ResourceLocation>();
		ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedBiomeKeys = ArrayListMultimap.create();
		
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			ResourceLocation biomeKey = buf.readResourceLocation();
			int numDimensions = buf.readInt();
			List<ResourceLocation> dimensionKeys = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionKeys.add(buf.readResourceLocation());
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
		for (ResourceLocation biomeKey : allowedBiomes) {
			buf.writeResourceLocation(biomeKey);
			List<ResourceLocation> dimensionKeys = dimensionKeysForAllowedBiomeKeys.get(biomeKey);
			buf.writeInt(dimensionKeys.size());
			for (ResourceLocation dimensionKey : dimensionKeys) {
				buf.writeResourceLocation(dimensionKey);
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
