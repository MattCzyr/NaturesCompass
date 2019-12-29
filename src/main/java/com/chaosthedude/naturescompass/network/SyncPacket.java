package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncPacket {

	private boolean canTeleport;
	private List<Biome> allowedBiomes;

	public SyncPacket() {}

	public SyncPacket(boolean canTeleport, List<Biome> allowedBiomes) {
		this.canTeleport = canTeleport;
		this.allowedBiomes = allowedBiomes;
	}

	public SyncPacket(PacketBuffer buf) {
		canTeleport = buf.readBoolean();
		allowedBiomes = new ArrayList<Biome>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			allowedBiomes.add(BiomeUtils.getBiomeForID(buf.readInt()));
		}
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedBiomes.size());
		for (Biome biome : allowedBiomes) {
			buf.writeInt(BiomeUtils.getIDForBiome(biome));
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
