package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class TeleportPacket {

	public TeleportPacket() {}

	public TeleportPacket(FriendlyByteBuf buf) {}

	public void encode(FriendlyByteBuf buf) {}

	public static void handle(TeleportPacket packet, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.getSender());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				final ServerPlayer player = ctx.getSender();
				if (ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(player.getServer(), player)) {
					if (natureCompass.getState(stack) == CompassState.FOUND) {
						final int x = natureCompass.getFoundBiomeX(stack);
						final int z = natureCompass.getFoundBiomeZ(stack);
						final int y = packet.findValidTeleportHeight(player.level(), x, z);

						player.stopRiding();
						player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());

						if (!player.isFallFlying()) {
							player.setDeltaMovement(player.getDeltaMovement().x(), 0, player.getDeltaMovement().z());
							player.setOnGround(true);
						}
					}
				} else {
					NaturesCompass.LOGGER.warn("Player " + player.getDisplayName().getString() + " tried to teleport but does not have permission.");
				}
			}
		});
		ctx.setPacketHandled(true);
	}
	
	private int findValidTeleportHeight(Level level, int x, int z) {
		int upY = level.getSeaLevel();
		int downY = level.getSeaLevel();
		while ((!level.isOutsideBuildHeight(upY) || !level.isOutsideBuildHeight(downY)) && !(isValidTeleportPosition(level, new BlockPos(x, upY, z)) || isValidTeleportPosition(level, new BlockPos(x, downY, z)))) {
			upY++;
			downY--;
		}
		BlockPos upPos = new BlockPos(x, upY, z);
		BlockPos downPos = new BlockPos(x, downY, z);
		if (isValidTeleportPosition(level, upPos)) {
			return upY;
		}
		if (isValidTeleportPosition(level, downPos)) {
			return downY;
		}
		return 256;
	}
	
	private boolean isValidTeleportPosition(Level level, BlockPos pos) {
		return isFree(level, pos) && isFree(level, pos.above()) && !isFree(level, pos.below());
	}
	
	private boolean isFree(Level level, BlockPos pos) {
		return level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.FIRE) || level.getBlockState(pos).liquid() || level.getBlockState(pos).canBeReplaced();
	}

}
