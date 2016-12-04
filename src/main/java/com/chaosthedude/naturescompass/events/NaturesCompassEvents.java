package com.chaosthedude.naturescompass.events;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.network.PacketLogin;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class NaturesCompassEvents {
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if (event.player != null && event.player instanceof EntityPlayerMP) {
			NaturesCompass.network.sendTo(new PacketLogin(PlayerUtils.canTeleport(event.player)), (EntityPlayerMP) event.player);
		}
	}

}
