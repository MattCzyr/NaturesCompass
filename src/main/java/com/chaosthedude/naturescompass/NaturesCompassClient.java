package com.chaosthedude.naturescompass;

import com.chaosthedude.naturescompass.network.SyncPacket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class NaturesCompassClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncPacket.PACKET_ID, SyncPacket::apply);
	}

}
