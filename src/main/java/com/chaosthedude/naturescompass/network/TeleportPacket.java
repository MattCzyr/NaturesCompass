package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.item.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record TeleportPacket() implements CustomPacketPayload {
	
	public static final Type<TeleportPacket> TYPE = new Type<TeleportPacket>(Identifier.fromNamespaceAndPath(NaturesCompass.MODID, "teleport"));

	public static final StreamCodec<FriendlyByteBuf, TeleportPacket> CODEC = StreamCodec.ofMember(TeleportPacket::write, TeleportPacket::read);

	public static TeleportPacket read(FriendlyByteBuf buf) {
		return new TeleportPacket();
	}
	
	public void write(FriendlyByteBuf buf) {
	}

	public static void apply(TeleportPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				if (NaturesCompassConfig.allowTeleport && PlayerUtils.canTeleport(context.player().level().getServer(), context.player())) {
					if (natureCompass.getState(stack) == CompassState.FOUND) {
						final int x = natureCompass.getFoundBiomeX(stack);
						final int z = natureCompass.getFoundBiomeZ(stack);
						final int y = findValidTeleportHeight(context.player().level(), x, z);

						context.player().stopRiding();
						context.player().connection.teleport(x, y, z, context.player().getYRot(), context.player().getXRot());

						if (!context.player().isFallFlying()) {
							context.player().setDeltaMovement(context.player().getDeltaMovement().x(), 0, context.player().getDeltaMovement().z());
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
	public Type<TeleportPacket> type() {
		return TYPE;
	}

	private static int findValidTeleportHeight(Level level, int x, int z) {
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
	
	private static boolean isValidTeleportPosition(Level level, BlockPos pos) {
		return isFree(level, pos) && isFree(level, pos.above()) && !isFree(level, pos.below());
	}
	
	private static boolean isFree(Level level, BlockPos pos) {
		return level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.FIRE) || level.getBlockState(pos).liquid() || level.getBlockState(pos).canBeReplaced();
	}

}