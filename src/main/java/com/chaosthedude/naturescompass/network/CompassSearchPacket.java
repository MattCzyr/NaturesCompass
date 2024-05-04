package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent.ServerCustomPayloadEvent;

public class CompassSearchPacket {

	private ResourceLocation biomeKey;
	private int x;
	private int y;
	private int z;

	public CompassSearchPacket() {}

	public CompassSearchPacket(ResourceLocation biomeKey, BlockPos pos) {
		this.biomeKey = biomeKey;

		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}

	public CompassSearchPacket(FriendlyByteBuf buf) {
		biomeKey = buf.readResourceLocation();

		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeResourceLocation(biomeKey);

		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

	public static void handle(CompassSearchPacket packet, ServerCustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.getSender());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				natureCompass.searchForBiome(ctx.getSender().serverLevel(), ctx.getSender(), packet.biomeKey, new BlockPos(packet.x, packet.y, packet.z), stack);
			}
		});
		ctx.setPacketHandled(true);
	}

}
