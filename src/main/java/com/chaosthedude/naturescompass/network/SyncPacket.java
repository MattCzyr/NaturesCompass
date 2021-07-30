package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class SyncPacket {

	private boolean canTeleport;
	private List<ResourceLocation> allowedBiomes;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<ResourceLocation> allowedBiomes) {
		this.canTeleport = canTeleport;
		this.allowedBiomes = allowedBiomes;
	}

	public SyncPacket(FriendlyByteBuf buf) {
		canTeleport = buf.readBoolean();
		allowedBiomes = new ArrayList<ResourceLocation>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			allowedBiomes.add(new ResourceLocation(buf.readUtf()));
		}
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedBiomes.size());
		for (ResourceLocation biome : allowedBiomes) {
			buf.writeResourceLocation(biome);
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			NaturesCompass.canTeleport = canTeleport;
			NaturesCompass.allowedBiomes = allowedBiomes;
		});
		ctx.get().setPacketHandled(true);
	}

}
