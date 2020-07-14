package com.chaosthedude.naturescompass.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.OpEntry;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class PlayerUtils {

	public static boolean canTeleport(PlayerEntity player) {
		return cheatModeEnabled(player) || isOp(player);
	}

	public static boolean cheatModeEnabled(PlayerEntity player) {
		final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
		if (server != null && server.isSinglePlayer()) {
			IWorldInfo worldInfo = server.getWorld(player.getEntityWorld().func_234923_W_()).getWorldInfo();
			if (worldInfo instanceof IServerWorldInfo) {
				System.out.println("Cheat mode enabled: " + ((IServerWorldInfo) worldInfo).areCommandsAllowed());
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

}
