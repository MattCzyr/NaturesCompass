package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.utils.ItemUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public record SearchForNextPacket() implements CustomPayload {

	public static final CustomPayload.Id<SearchForNextPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(NaturesCompass.MODID, "search_for_next"));

	public static final PacketCodec<RegistryByteBuf, SearchForNextPacket> PACKET_CODEC = PacketCodec.of(SearchForNextPacket::write, SearchForNextPacket::read);

	public static SearchForNextPacket read(RegistryByteBuf buf) {
		return new SearchForNextPacket();
	}

	public void write(RegistryByteBuf buf) {
	}

	@Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

    public static void apply(SearchForNextPacket packet, ServerPlayNetworking.Context context) {
		context.player().getServer().execute(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				final ServerWorld world = context.player().getServerWorld();
				natureCompass.searchForNextBiome(world, context.player(), context.player().getBlockPos(), stack);
			}
		});
	}

}
