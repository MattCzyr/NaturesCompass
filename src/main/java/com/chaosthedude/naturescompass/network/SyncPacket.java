package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SyncPacket extends PacketByteBuf {

	public static final Identifier ID = new Identifier(NaturesCompass.MODID, "sync");

	public SyncPacket(boolean canTeleport, List<Identifier> allowedBiomeIDs) {
		super(Unpooled.buffer());
		writeBoolean(canTeleport);
		writeInt(allowedBiomeIDs.size());
		for (Identifier biomeID : allowedBiomeIDs) {
			writeIdentifier(biomeID);
		}
	}

    public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final boolean canTeleport = buf.readBoolean();
		final List<Identifier> allowedBiomeIDs = new ArrayList<Identifier>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			allowedBiomeIDs.add(buf.readIdentifier());
		}
		
		client.execute(() -> {
	        NaturesCompass.canTeleport = canTeleport;
	        NaturesCompass.allowedBiomes = allowedBiomeIDs;
		});
	}

}