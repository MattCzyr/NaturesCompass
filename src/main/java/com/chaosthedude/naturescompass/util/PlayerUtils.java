package com.chaosthedude.naturescompass.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class PlayerUtils {
	
	public static boolean canTeleport(EntityPlayer player) {
		return cheatModeEnabled(player) || isOp(player);
	}

	public static boolean cheatModeEnabled(EntityPlayer player) {
		final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
		if (server != null && server.isSinglePlayer()) {
			return server.getWorld(DimensionType.OVERWORLD).getWorldInfo().areCommandsAllowed();
		}

		return false;
	}
	
	public static boolean isOp(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			final UserListOpsEntry userEntry = ((EntityPlayerMP) player).getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
			return userEntry != null;
		}

		return false;
	}

}
