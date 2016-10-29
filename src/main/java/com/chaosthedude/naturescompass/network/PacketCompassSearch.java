package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PacketCompassSearch implements IMessage {

	private int biomeID;

	private int x;
	private int z;

	public PacketCompassSearch() {
	}

	public PacketCompassSearch(int biomeID, int x, int z) {
		this.biomeID = biomeID;

		this.x = x;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		biomeID = buf.readInt();

		x = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(biomeID);

		buf.writeInt(x);
		buf.writeInt(z);
	}

	public static class Handler implements IMessageHandler<PacketCompassSearch, IMessage> {
		@Override
		public IMessage onMessage(PacketCompassSearch packet, MessageContext ctx) {
			final ItemStack stack = ctx.getServerHandler().playerEntity.getHeldItem();
			if (stack != null && stack.getItem() == NaturesCompass.naturesCompass) {
				final ItemNaturesCompass natureCompass = (ItemNaturesCompass) stack.getItem();
				final World world = ctx.getServerHandler().playerEntity.worldObj;
				natureCompass.searchForBiome(world, ctx.getServerHandler().playerEntity, packet.biomeID, packet.x, packet.z, stack);
			}

			return null;
		}
	}

}
