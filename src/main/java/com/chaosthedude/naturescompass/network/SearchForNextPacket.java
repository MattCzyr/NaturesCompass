package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SearchForNextPacket() implements CustomPacketPayload {

	public static final Type<SearchForNextPacket> TYPE = new Type<SearchForNextPacket>(ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "search_for_next"));

	public static final StreamCodec<FriendlyByteBuf, SearchForNextPacket> CODEC = StreamCodec.ofMember(SearchForNextPacket::write, SearchForNextPacket::read);

	public static SearchForNextPacket read(FriendlyByteBuf buf) {
		return new SearchForNextPacket();
	}

	public void write(FriendlyByteBuf buf) {
	}

	public static void handle(SearchForNextPacket packet, IPayloadContext context) {
		if (context.flow().isServerbound()) {
			context.enqueueWork(() -> {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
				if (!stack.isEmpty()) {
					final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
					natureCompass.searchForNextBiome((ServerLevel) context.player().level(), context.player(), context.player().blockPosition(), stack);
				}
			});
		}
	}

	@Override
	public Type<SearchForNextPacket> type() {
		return TYPE;
	}

}
