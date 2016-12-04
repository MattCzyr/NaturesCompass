package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.util.PlayerUtils;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketRequestSync implements IMessage {

	public PacketRequestSync() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}

	public static class Handler implements IMessageHandler<PacketRequestSync, IMessage> {
		@Override
		public IMessage onMessage(PacketRequestSync packet, MessageContext ctx) {
			final boolean canTeleport = PlayerUtils.canTeleport(ctx.getServerHandler().playerEntity);
			return new PacketSync(canTeleport);
		}
	}

}