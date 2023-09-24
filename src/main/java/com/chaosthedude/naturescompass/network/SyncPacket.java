package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SyncPacket {

	private boolean canTeleport;
	private List<ResourceLocation> allowedBiomes;
	private ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedBiomeKeys;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<ResourceLocation> allowedBiomes, ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedBiomeKeys) {
		this.canTeleport = canTeleport;
		this.allowedBiomes = allowedBiomes;
		this.dimensionKeysForAllowedBiomeKeys = dimensionKeysForAllowedBiomeKeys;
	}

	public SyncPacket(FriendlyByteBuf buf) {
		canTeleport = buf.readBoolean();
		allowedBiomes = new ArrayList<ResourceLocation>();
		dimensionKeysForAllowedBiomeKeys = ArrayListMultimap.create();
		
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
	}

	public void toBytes(FriendlyByteBuf buf) {
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

	public static void handle(SyncPacket packet, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			NaturesCompass.canTeleport = packet.canTeleport;
			NaturesCompass.allowedBiomes = packet.allowedBiomes;
			NaturesCompass.dimensionKeysForAllowedBiomeKeys = packet.dimensionKeysForAllowedBiomeKeys;
		});
		ctx.setPacketHandled(true);
	}

}
