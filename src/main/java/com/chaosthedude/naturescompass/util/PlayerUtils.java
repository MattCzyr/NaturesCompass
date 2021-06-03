package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.OpEntry;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.server.permission.PermissionAPI;

public class PlayerUtils {

	public static boolean canTeleport(PlayerEntity player) {
		return cheatModeEnabled(player) || isOp(player) || hasPermission(player);
	}

	public static boolean cheatModeEnabled(PlayerEntity player) {
		final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
		if (server != null && server.isSinglePlayer()) {
			IWorldInfo worldInfo = server.getWorld(player.getEntityWorld().getDimensionKey()).getWorldInfo();
			if (worldInfo instanceof IServerWorldInfo) {
				return ((IServerWorldInfo) worldInfo).areCommandsAllowed();
			}
		}

		return false;
	}

	public static boolean isOp(PlayerEntity player) {
		if (player instanceof ServerPlayerEntity) {
			final OpEntry userEntry = ((ServerPlayerEntity) player).getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
			return userEntry != null;
		}

		return false;
	}

	public static boolean hasPermission(PlayerEntity player) {
		return PermissionAPI.hasPermission(player, NaturesCompass.TELEPORT_PERMISSION);
	}

}
