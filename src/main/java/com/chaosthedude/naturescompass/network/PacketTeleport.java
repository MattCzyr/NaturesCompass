package com.chaosthedude.naturescompass.network;

import java.util.EnumSet;
import java.util.Set;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;
import com.chaosthedude.naturescompass.util.EnumCompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

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
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.getServerHandler().player);
			if (!stack.isEmpty()) {
				final ItemNaturesCompass natureCompass = (ItemNaturesCompass) stack.getItem();
				final EntityPlayer player = ctx.getServerHandler().player;
				if (PlayerUtils.canTeleport(player)) {
					if (natureCompass.getState(stack) == EnumCompassState.FOUND) {
						final Set<SPacketPlayerPosLook.EnumFlags> set = EnumSet.<SPacketPlayerPosLook.EnumFlags> noneOf(SPacketPlayerPosLook.EnumFlags.class);
						final int x = natureCompass.getFoundBiomeX(stack);
						final int z = natureCompass.getFoundBiomeZ(stack);
						int y = 256;
						while (player.world.isAirBlock(new BlockPos(x, y - 1, z))) {
							y--;
						}

						player.dismountRidingEntity();
						((EntityPlayerMP) player).connection.setPlayerLocation(x, y, z, player.cameraYaw, player.cameraPitch, set);

						if (!player.isElytraFlying()) {
							player.motionY = 0.0D;
							player.onGround = true;
						}
					}
				} else {
					NaturesCompass.logger.warn("Player " + player.getDisplayNameString() + " tried to teleport but does not have permission.");
				}
			}

			return null;
		}
	}

}
