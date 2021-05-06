package com.chaosthedude.naturescompass.network;

import java.util.function.Supplier;

import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

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

	public CompassSearchPacket(PacketBuffer buf) {
		biomeKey = buf.readResourceLocation();

		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeResourceLocation(biomeKey);

		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(ctx.get().getSender());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				final World world = ctx.get().getSender().world;
				natureCompass.searchForBiome(world, ctx.get().getSender(), biomeKey, new BlockPos(x, y, z), stack);
			}
		});
		ctx.get().setPacketHandled(true);
	}

}
