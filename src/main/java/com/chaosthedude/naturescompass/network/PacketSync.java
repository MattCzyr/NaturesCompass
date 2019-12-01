package com.chaosthedude.naturescompass.network;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSync implements IMessage {

	private boolean canTeleport;
	private List<Biome> allowedBiomes;

	public PacketSync() {
	}
	
	public PacketSync(boolean canTeleport, List<Biome> allowedBiomes) {
		this.canTeleport = canTeleport;
		this.allowedBiomes = allowedBiomes;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		canTeleport = buf.readBoolean();
		allowedBiomes = new ArrayList<Biome>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			allowedBiomes.add(Biome.getBiomeForId(buf.readInt()));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(canTeleport);
		buf.writeInt(allowedBiomes.size());
		for (Biome biome : allowedBiomes) {
			buf.writeInt(Biome.getIdForBiome(biome));
		}
	}

	public static class Handler implements IMessageHandler<PacketSync, IMessage> {
		@Override
		public IMessage onMessage(PacketSync packet, MessageContext ctx) {
			NaturesCompass.canTeleport = packet.canTeleport;
			NaturesCompass.allowedBiomes = packet.allowedBiomes;

			return null;
		}
	}

}
