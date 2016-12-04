package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketSync implements IMessage {

	private boolean canTeleport;

	public PacketSync() {
	}
	
	public PacketSync(boolean canTeleport) {
		this.canTeleport = canTeleport;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		canTeleport = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(canTeleport);
	}

	public static class Handler implements IMessageHandler<PacketSync, IMessage> {
		@Override
		public IMessage onMessage(PacketSync packet, MessageContext ctx) {
			NaturesCompass.canTeleport = packet.canTeleport;

			return null;
		}
	}

}