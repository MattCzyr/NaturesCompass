package com.chaosthedude.naturescompass.util;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;

public class PlayerUtils {
	
	public static boolean canTeleport(EntityPlayer player) {
		return cheatModeEnabled(player) || isOp(player);
	}

	public static boolean cheatModeEnabled(EntityPlayer player) {
		final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server != null && server.isSinglePlayer()) {
			return server.worldServers[0].getWorldInfo().areCommandsAllowed();
		}

		return false;
	}

	public static boolean isOp(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			return ((EntityPlayerMP) player).mcServer.getConfigurationManager().func_152596_g(player.getGameProfile());
		}

		return false;
	}

}