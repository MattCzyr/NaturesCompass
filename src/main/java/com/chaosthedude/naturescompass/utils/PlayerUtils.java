package com.chaosthedude.naturescompass.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerUtils {
	
	public static boolean canTeleport(PlayerEntity player) {
		return cheatModeEnabled(player) || isOp(player);
	}

	public static boolean cheatModeEnabled(PlayerEntity player) {
		if (player instanceof ServerPlayerEntity) {
			final MinecraftServer server = ((ServerPlayerEntity) player).getServer();
			if (server != null && server.isSingleplayer()) {
				return server.getPlayerManager().areCheatsAllowed();
			}
		}

		return false;
	}

	public static boolean isOp(PlayerEntity player) {
		if (player instanceof ServerPlayerEntity) {
			final MinecraftServer server = ((ServerPlayerEntity) player).getServer();
			if (server != null) {
				return server.getPlayerManager().isOperator(player.getGameProfile());
			}
		}

		return false;
	}

}
