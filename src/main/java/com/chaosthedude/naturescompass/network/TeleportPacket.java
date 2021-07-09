package com.chaosthedude.naturescompass.network;

import java.util.EnumSet;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.utils.CompassState;
import com.chaosthedude.naturescompass.utils.ItemUtils;
import com.chaosthedude.naturescompass.utils.PlayerUtils;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportPacket extends PacketByteBuf {

	public static final Identifier ID = new Identifier(NaturesCompass.MODID, "teleport");

	public TeleportPacket() {
		super(Unpooled.buffer());
	}

	public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		server.execute(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				if (NaturesCompassConfig.allowTeleport && PlayerUtils.canTeleport(player)) {
					if (natureCompass.getState(stack) == CompassState.FOUND) {
						final int x = natureCompass.getFoundBiomeX(stack);
						final int z = natureCompass.getFoundBiomeZ(stack);
						final int y = findValidTeleportHeight(player.getEntityWorld(), x, z, player);

						player.stopRiding();
						((ServerPlayerEntity) player).networkHandler.requestTeleport(x, y, z, player.yaw, player.pitch, EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class));

						if (!player.isFallFlying()) {
							player.setVelocity(player.getVelocity().getX(), 0, player.getVelocity().getZ());
							player.setOnGround(true);
						}
					}
				} else {
					NaturesCompass.LOGGER.warn("Player " + player.getDisplayName().getString() + " tried to teleport but does not have permission.");
				}
			}
		});
	}

	private static int findValidTeleportHeight(World world, int x, int z, PlayerEntity player) {
		int startY = world.getSeaLevel();
		int upY = startY;
		int downY = startY;
		while (!(isValidTeleportPosition(world, new BlockPos(x, upY, z), player) || isValidTeleportPosition(world, new BlockPos(x, downY, z), player)) && (upY < 255 || downY > 1)) {
			upY++;
			downY--;
		}
		BlockPos upPos = new BlockPos(x, upY, z);
		BlockPos downPos = new BlockPos(x, downY, z);
		if (upY < 255 && isValidTeleportPosition(world, upPos, player)) {
			return upY;
		}
		if (downY > 1 && isValidTeleportPosition(world, downPos, player)) {
			return downY;
		}
		return 256;
	}

	private static boolean isValidTeleportPosition(World world, BlockPos pos, PlayerEntity player) {
		return !world.getBlockState(pos).isSolidBlock(world, pos) && world.getBlockState(pos).hasSolidTopSurface(world, pos.down(), player);
	}

}