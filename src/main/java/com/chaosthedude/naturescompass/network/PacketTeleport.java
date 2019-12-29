package com.chaosthedude.naturescompass.network;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;
import com.chaosthedude.naturescompass.util.EnumCompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketTeleport {

	public PacketTeleport() {}

	public PacketTeleport(PacketBuffer buf) {}

	public void fromBytes(PacketBuffer buf) {}

	public void toBytes(PacketBuffer buf) {}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.get().getSender());
			if (!stack.isEmpty()) {
				final ItemNaturesCompass natureCompass = (ItemNaturesCompass) stack.getItem();
				final EntityPlayer player = ctx.get().getSender();
				if (ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(player)) {
					if (natureCompass.getState(stack) == EnumCompassState.FOUND) {
						final Set<SPacketPlayerPosLook.EnumFlags> set = EnumSet.<SPacketPlayerPosLook.EnumFlags>noneOf(SPacketPlayerPosLook.EnumFlags.class);
						final int x = natureCompass.getFoundBiomeX(stack);
						final int z = natureCompass.getFoundBiomeZ(stack);
						int y = 256;
						while (player.world.isAirBlock(new BlockPos(x, y - 1, z))) {
							y--;
						}

						player.stopRiding();
						((EntityPlayerMP) player).connection.setPlayerLocation(x, y, z, player.cameraYaw, player.cameraPitch, set);

						if (!player.isElytraFlying()) {
							player.motionY = 0.0D;
							player.onGround = true;
						}
					}
				} else {
					NaturesCompass.logger.warn("Player " + player.getDisplayName().getFormattedText() + " tried to teleport but does not have permission.");
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}
