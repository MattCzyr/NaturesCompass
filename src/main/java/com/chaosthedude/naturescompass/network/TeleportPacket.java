package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TeleportPacket() implements CustomPacketPayload {

	public static final Type<TeleportPacket> TYPE = new Type<TeleportPacket>(ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "teleport"));
	
	public static final StreamCodec<FriendlyByteBuf, TeleportPacket> CODEC = StreamCodec.ofMember(TeleportPacket::write, TeleportPacket::read);
	
	public static TeleportPacket read(FriendlyByteBuf buf) {
		return new TeleportPacket();
	}

	public void write(FriendlyByteBuf buf) {}

	public static void handle(TeleportPacket packet, IPayloadContext context) {
		if (context.flow().isServerbound()) {
			context.enqueueWork(() -> {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
				if (!stack.isEmpty()) {
					final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
					final ServerPlayer player = (ServerPlayer) context.player();
					if (ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(player.getServer(), player)) {
						if (natureCompass.getState(stack) == CompassState.FOUND) {
							final int x = natureCompass.getFoundBiomeX(stack);
							final int z = natureCompass.getFoundBiomeZ(stack);
							final int y = packet.findValidTeleportHeight(context.player().level(), x, z);
	
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
		}
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
	
	@Override
	public Type<TeleportPacket> type() {
		return TYPE;
	}

}
