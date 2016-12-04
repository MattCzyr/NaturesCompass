package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;
import com.chaosthedude.naturescompass.util.EnumCompassState;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketTeleport implements IMessage {

	public PacketTeleport() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}

	public static class Handler implements IMessageHandler<PacketTeleport, IMessage> {
		@Override
		public IMessage onMessage(PacketTeleport packet, MessageContext ctx) {
			final ItemStack stack = ctx.getServerHandler().playerEntity.getHeldItem();
			if (stack != null && stack.getItem() == NaturesCompass.naturesCompass) {
				final ItemNaturesCompass natureCompass = (ItemNaturesCompass) stack.getItem();
				final EntityPlayer player = ctx.getServerHandler().playerEntity;
				if (PlayerUtils.canTeleport(player)) {
					if (natureCompass.getState(stack) == EnumCompassState.FOUND) {
						final int x = natureCompass.getFoundBiomeX(stack);
						final int z = natureCompass.getFoundBiomeZ(stack);
						int y = 256;
						while (player.worldObj.isAirBlock(x, y - 1, z)) {
							y--;
						}

						player.mountEntity((Entity) null);
						player.setPositionAndUpdate(x, y, z);
					}
				} else {
					NaturesCompass.logger.warn("Player " + player.getDisplayName() + " tried to teleport but does not have permission.");
				}
			}

			return null;
		}
	}

}