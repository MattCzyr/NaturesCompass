package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

public class PlayerUtils {
	
	public static boolean canTeleport(EntityPlayer player) {
		return cheatModeEnabled(player) || isOp(player) || hasPermission(player);
	}

	public static boolean cheatModeEnabled(EntityPlayer player) {
		final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server != null && server.isSinglePlayer()) {
			return server.worlds[0].getWorldInfo().areCommandsAllowed();
		}

		return false;
	}

	public static boolean hasPermission(EntityPlayer player) {
		return PermissionAPI.hasPermission(player, NaturesCompass.TELEPORT_PERMISSION);
	}
	
	public static boolean isOp(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			final UserListOpsEntry userEntry = ((EntityPlayerMP) player).getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
			return userEntry != null;
		}

		return false;
	}

}
