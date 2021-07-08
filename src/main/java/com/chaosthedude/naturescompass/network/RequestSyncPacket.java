package com.chaosthedude.naturescompass.network;

import java.util.List;
import java.util.function.Supplier;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
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
			final List<ResourceLocation> allowedBiomeKeys = BiomeUtils.getAllowedBiomeKeys(ctx.get().getSender().getServerWorld());
			NaturesCompass.network.sendTo(new SyncPacket(canTeleport, allowedBiomeKeys), ctx.get().getSender().connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
		});
		ctx.get().setPacketHandled(true);
	}

}
