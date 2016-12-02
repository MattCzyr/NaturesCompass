package com.chaosthedude.naturescompass.network;

import java.util.EnumSet;
import java.util.Set;

import com.chaosthedude.naturescompass.items.ItemNaturesCompass;
import com.chaosthedude.naturescompass.util.EnumCompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.getServerHandler().playerEntity);
			if (stack != null) {
				final ItemNaturesCompass natureCompass = (ItemNaturesCompass) stack.getItem();
				final EntityPlayer player = ctx.getServerHandler().playerEntity;
				if (natureCompass.canTeleport(player) && natureCompass.getState(stack) == EnumCompassState.FOUND) {
					final Set<SPacketPlayerPosLook.EnumFlags> set = EnumSet.<SPacketPlayerPosLook.EnumFlags>noneOf(SPacketPlayerPosLook.EnumFlags.class);
					final int x = natureCompass.getFoundBiomeX(stack);
					final int z = natureCompass.getFoundBiomeZ(stack);
					int y = 256;
					while (player.worldObj.isAirBlock(new BlockPos(x, y - 1, z))) {
						y--;
					}

					player.dismountRidingEntity();
					((EntityPlayerMP) player).connection.setPlayerLocation(x, y, z, player.cameraYaw, player.cameraPitch, set);

					if (!player.isElytraFlying()) {
						player.motionY = 0.0D;
						player.onGround = true;
					}
				}
			}

			return null;
		}
	}

}
