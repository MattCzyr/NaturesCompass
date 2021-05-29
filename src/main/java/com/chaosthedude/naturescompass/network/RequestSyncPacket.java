package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class RequestSyncPacket {

	public RequestSyncPacket() {}

	public RequestSyncPacket(PacketBuffer buf) {}

	public void fromBytes(PacketBuffer buf) {}

	public void toBytes(PacketBuffer buf) {}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			final boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(ctx.get().getSender());
			final List<Biome> allowedBiomes = BiomeUtils.getAllowedBiomes(ctx.get().getSender().getServerWorld());
			List<ResourceLocation> allowedBiomeKeys = new ArrayList<ResourceLocation>();
			for (Biome biome : allowedBiomes) {
				allowedBiomeKeys.add(BiomeUtils.getKeyForBiome(ctx.get().getSender().getServerWorld(), biome));
			}
			NaturesCompass.network.sendTo(new SyncPacket(canTeleport, allowedBiomeKeys), ctx.get().getSender().connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
		});
		ctx.get().setPacketHandled(true);
	}

}
