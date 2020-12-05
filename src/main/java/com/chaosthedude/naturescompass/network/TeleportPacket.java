package com.chaosthedude.naturescompass.network;

import java.util.function.Supplier;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class TeleportPacket {

	public TeleportPacket() {}

	public TeleportPacket(PacketBuffer buf) {}

	public void fromBytes(PacketBuffer buf) {}

	public void toBytes(PacketBuffer buf) {}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.get().getSender());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				final PlayerEntity player = ctx.get().getSender();
				if (ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(player)) {
					if (natureCompass.getState(stack) == CompassState.FOUND) {
						final int x = natureCompass.getFoundBiomeX(stack);
						final int z = natureCompass.getFoundBiomeZ(stack);
						final int y = findValidTeleportHeight(player.getEntityWorld(), x, z);

						player.stopRiding();
						((ServerPlayerEntity) player).connection.setPlayerLocation(x, y, z, player.cameraYaw, player.rotationPitch);

						if (!player.isElytraFlying()) {
							player.setMotion(player.getMotion().getX(), 0, player.getMotion().getZ());
							player.setOnGround(true);
						}
					}
				} else {
					NaturesCompass.logger.warn("Player " + player.getDisplayName().getString() + " tried to teleport but does not have permission.");
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
	
	private int findValidTeleportHeight(World world, int x, int z) {
		int startY = world.getSeaLevel();
		int upY = startY;
		int downY = startY;
		while (!(isValidTeleportPosition(world, new BlockPos(x, upY, z)) || isValidTeleportPosition(world, new BlockPos(x, downY, z))) && (upY < 255 || downY > 1)) {
			upY++;
			downY--;
		}
		BlockPos upPos = new BlockPos(x, upY, z);
		BlockPos downPos = new BlockPos(x, downY, z);
		if (upY < 255 && isValidTeleportPosition(world, upPos)) {
			return upY;
		}
		if (downY > 1 && isValidTeleportPosition(world, downPos)) {
			return downY;
		}
		return 256;
	}
	
	private boolean isValidTeleportPosition(World world, BlockPos pos) {
		return !world.getBlockState(pos).isSolid() && Block.hasSolidSideOnTop(world, pos.down());
	}

}
