package com.chaosthedude.naturescompass.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;

public class PlayerUtils {
	
	public static boolean canTeleport(MinecraftServer server, Player player) {
		return cheatModeEnabled(server, player) || isOp(player);
	}

	public static boolean cheatModeEnabled(MinecraftServer server, Player player) {
		if (server != null && server.isSingleplayer()) {
			LevelData levelData = server.getLevel(player.level().dimension()).getLevelData();
			if (levelData instanceof ServerLevelData) {
				return ((ServerLevelData) levelData).isAllowCommands();
			}
		}

		return false;
	}

	public static boolean isOp(Player player) {
		if (player instanceof ServerPlayer) {
			final ServerOpListEntry userEntry = ((ServerPlayer) player).level().getServer().getPlayerList().getOps().get(player.nameAndId());
			return userEntry != null;
		}

		return false;
	}

}
