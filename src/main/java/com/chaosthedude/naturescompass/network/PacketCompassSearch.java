package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.items.ItemNaturesCompass;
import com.chaosthedude.naturescompass.util.ItemUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCompassSearch implements IMessage {

	private int biomeID;
	private int radius;

	private int x;
	private int y;
	private int z;

	public PacketCompassSearch() {
	}

	public PacketCompassSearch(int biomeID, int radius, BlockPos pos) {
		this.biomeID = biomeID;
		this.radius = radius;

		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		biomeID = buf.readInt();
		radius = buf.readInt();

		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(biomeID);
		buf.writeInt(radius);

		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

	public static class Handler implements IMessageHandler<PacketCompassSearch, IMessage> {
		@Override
		public IMessage onMessage(PacketCompassSearch packet, MessageContext ctx) {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.getServerHandler().player);
			if (!stack.isEmpty()) {
				final ItemNaturesCompass natureCompass = (ItemNaturesCompass) stack.getItem();
				final World world = ctx.getServerHandler().player.world;
				natureCompass.searchForBiome(world, ctx.getServerHandler().player, packet.biomeID, packet.radius, new BlockPos(packet.x, packet.y, packet.z), stack);
			}

			return null;
		}
	}

}
