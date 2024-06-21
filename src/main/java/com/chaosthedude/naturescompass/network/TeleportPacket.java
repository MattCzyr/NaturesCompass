package com.chaosthedude.naturescompass.network;

import java.util.Collections;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.utils.CompassState;
import com.chaosthedude.naturescompass.utils.ItemUtils;
import com.chaosthedude.naturescompass.utils.PlayerUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record TeleportPacket() implements CustomPayload {

public static final CustomPayload.Id<TeleportPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(NaturesCompass.MODID, "teleport"));
	
	public static final PacketCodec<RegistryByteBuf, TeleportPacket> PACKET_CODEC = PacketCodec.of(TeleportPacket::write, TeleportPacket::read);

	public static TeleportPacket read(RegistryByteBuf buf) {
		return new TeleportPacket();
	}
	
	public void write(RegistryByteBuf buf) {
	}

	public static void apply(TeleportPacket packet, ServerPlayNetworking.Context context) {
		context.player().getServer().execute(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				if (NaturesCompassConfig.allowTeleport && PlayerUtils.canTeleport(context.player())) {
					if (natureCompass.getState(stack) == CompassState.FOUND) {
						final int x = natureCompass.getFoundBiomeX(stack);
						final int z = natureCompass.getFoundBiomeZ(stack);
						final int y = findValidTeleportHeight(context.player().getEntityWorld(), x, z);

						context.player().stopRiding();
						context.player().networkHandler.requestTeleport(x, y, z, context.player().getYaw(), context.player().getPitch(), Collections.emptySet());

						if (!context.player().isFallFlying()) {
							context.player().setVelocity(context.player().getVelocity().getX(), 0, context.player().getVelocity().getZ());
							context.player().setOnGround(true);
						}
					}
				} else {
					NaturesCompass.LOGGER.warn("Player " + context.player().getDisplayName().getString() + " tried to teleport but does not have permission.");
				}
			}
		});
	}
	
	@Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

	private static int findValidTeleportHeight(World world, int x, int z) {
		int upY = world.getSeaLevel();
		int downY = world.getSeaLevel();
		while ((!world.isOutOfHeightLimit(upY) || !world.isOutOfHeightLimit(downY)) && !(isValidTeleportPosition(world, new BlockPos(x, upY, z)) || isValidTeleportPosition(world, new BlockPos(x, downY, z)))) {
			upY++;
			downY--;
		}
		BlockPos upPos = new BlockPos(x, upY, z);
		BlockPos downPos = new BlockPos(x, downY, z);
		if (isValidTeleportPosition(world, upPos)) {
			return upY;
		}
		if (isValidTeleportPosition(world, downPos)) {
			return downY;
		}
		return 256;
	}
	
	private static boolean isValidTeleportPosition(World world, BlockPos pos) {
		return isFree(world, pos) && isFree(world, pos.up()) && !isFree(world, pos.down());
	}
	
	private static boolean isFree(World world, BlockPos pos) {
		return world.getBlockState(pos).isAir() || world.getBlockState(pos).isIn(BlockTags.FIRE) || world.getBlockState(pos).isLiquid() || world.getBlockState(pos).isReplaceable();
	}

}