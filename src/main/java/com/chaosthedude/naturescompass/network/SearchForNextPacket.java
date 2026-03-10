package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.item.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public record SearchForNextPacket() implements CustomPacketPayload {

	public static final Type<SearchForNextPacket> TYPE = new Type<SearchForNextPacket>(Identifier.fromNamespaceAndPath(NaturesCompass.MODID, "search_for_next"));

	public static final StreamCodec<FriendlyByteBuf, SearchForNextPacket> CODEC = StreamCodec.ofMember(SearchForNextPacket::write, SearchForNextPacket::read);

	public static SearchForNextPacket read(FriendlyByteBuf buf) {
		return new SearchForNextPacket();
	}

	public void write(FriendlyByteBuf buf) {
	}

	public static void apply(SearchForNextPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				final ServerLevel level = context.player().level();
				natureCompass.searchForNextBiome(level, context.player(), context.player().blockPosition(), stack);
			}
		});
	}

	@Override
	public Type<SearchForNextPacket> type() {
		return TYPE;
	}

}
