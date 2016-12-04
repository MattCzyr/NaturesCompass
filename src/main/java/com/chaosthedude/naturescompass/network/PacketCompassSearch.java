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

	private int x;
	private int y;
	private int z;

	public PacketCompassSearch() {
	}

	public PacketCompassSearch(int biomeID, BlockPos pos) {
		this.biomeID = biomeID;

		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		biomeID = buf.readInt();

		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(biomeID);

		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

	public static class Handler implements IMessageHandler<PacketCompassSearch, IMessage> {
		@Override
		public IMessage onMessage(PacketCompassSearch packet, MessageContext ctx) {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.getServerHandler().playerEntity);
			if (ItemUtils.stackExists(stack)) {
				final ItemNaturesCompass natureCompass = (ItemNaturesCompass) stack.getItem();
				final World world = ctx.getServerHandler().playerEntity.worldObj;
				natureCompass.searchForBiome(world, ctx.getServerHandler().playerEntity, packet.biomeID, new BlockPos(packet.x, packet.y, packet.z), stack);
			}

			return null;
		}
	}

}
