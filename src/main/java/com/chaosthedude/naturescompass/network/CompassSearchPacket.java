package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class CompassSearchPacket {

	private ResourceLocation biomeKey;
	private BlockPos pos;

	public CompassSearchPacket() {}

	public CompassSearchPacket(ResourceLocation biomeKey, BlockPos pos) {
		this.biomeKey = biomeKey;
		this.pos = pos;
	}

	public CompassSearchPacket(FriendlyByteBuf buf) {
		biomeKey = buf.readResourceLocation();
		pos = buf.readBlockPos();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeResourceLocation(biomeKey);
		buf.writeBlockPos(pos);
	}

	public static void handle(CompassSearchPacket packet, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.getSender());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				natureCompass.searchForBiome(ctx.getSender().serverLevel(), ctx.getSender(), packet.biomeKey, packet.pos, stack);
			}
		});
		ctx.setPacketHandled(true);
	}

}
