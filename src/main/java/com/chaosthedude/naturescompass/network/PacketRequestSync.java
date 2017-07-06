package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.util.PlayerUtils;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
			final boolean canTeleport = PlayerUtils.canTeleport(ctx.getServerHandler().player);
			return new PacketSync(canTeleport);
		}
	}

}
