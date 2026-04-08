package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public class SyncPacket {

	private boolean canTeleport;
	private int maxNextSearches;
	private boolean infiniteXp;
	private List<ResourceLocation> allowedBiomes;
	private Map<ResourceLocation, Integer> xpLevelsForAllowedBiomes;
	private ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedBiomeKeys;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, int maxNextSearches, boolean infiniteXp, List<ResourceLocation> allowedBiomes, Map<ResourceLocation, Integer> xpLevelsForAllowedBiomes, ListMultimap<ResourceLocation, ResourceLocation> dimensionKeysForAllowedBiomeKeys) {
		this.canTeleport = canTeleport;
		this.maxNextSearches = maxNextSearches;
		this.infiniteXp = infiniteXp;
		this.allowedBiomes = allowedBiomes;
		this.xpLevelsForAllowedBiomes = xpLevelsForAllowedBiomes;
		this.dimensionKeysForAllowedBiomeKeys = dimensionKeysForAllowedBiomeKeys;
	}

	public SyncPacket(FriendlyByteBuf buf) {
		canTeleport = buf.readBoolean();
		maxNextSearches = buf.readInt();
		infiniteXp = buf.readBoolean();
		allowedBiomes = new ArrayList<ResourceLocation>();
		xpLevelsForAllowedBiomes = new HashMap<ResourceLocation, Integer>();
		dimensionKeysForAllowedBiomeKeys = ArrayListMultimap.create();

		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			ResourceLocation biomeKey = buf.readResourceLocation();
			int numDimensions = buf.readInt();
			List<ResourceLocation> dimensionKeys = new ArrayList<ResourceLocation>();
			for (int j = 0; j < numDimensions; j++) {
				dimensionKeys.add(buf.readResourceLocation());
			}
			int xpLevels = buf.readInt();

			if (biomeKey != null) {
				allowedBiomes.add(biomeKey);
				xpLevelsForAllowedBiomes.put(biomeKey, xpLevels);
				dimensionKeysForAllowedBiomeKeys.putAll(biomeKey, dimensionKeys);
			}
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(maxNextSearches);
		buf.writeBoolean(infiniteXp);
		buf.writeInt(allowedBiomes.size());
		for (ResourceLocation biomeKey : allowedBiomes) {
			buf.writeResourceLocation(biomeKey);
			List<ResourceLocation> dimensionKeys = dimensionKeysForAllowedBiomeKeys.get(biomeKey);
			buf.writeInt(dimensionKeys.size());
			for (ResourceLocation dimensionKey : dimensionKeys) {
				buf.writeResourceLocation(dimensionKey);
			}
			int xpLevels = xpLevelsForAllowedBiomes.getOrDefault(biomeKey, 0);
			buf.writeInt(xpLevels);
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			NaturesCompass.synced = true;
			NaturesCompass.canTeleport = canTeleport;
			NaturesCompass.maxNextSearches = maxNextSearches;
			NaturesCompass.infiniteXp = infiniteXp;
			NaturesCompass.allowedBiomes = allowedBiomes;
			NaturesCompass.xpLevelsForAllowedBiomes = xpLevelsForAllowedBiomes;
			NaturesCompass.dimensionKeysForAllowedBiomeKeys = dimensionKeysForAllowedBiomeKeys;
		});
		ctx.get().setPacketHandled(true);
	}

}
