package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketLogin implements IMessage {

	private boolean canTeleport;

	public PacketLogin() {
	}
	
	public PacketLogin(boolean canTeleport) {
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

	public static class Handler implements IMessageHandler<PacketLogin, IMessage> {
		@Override
		public IMessage onMessage(PacketLogin packet, MessageContext ctx) {
			NaturesCompass.canTeleport = packet.canTeleport;

			return null;
		}
	}

}
