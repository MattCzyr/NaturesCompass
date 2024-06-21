package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SearchPacket(ResourceLocation biomeKey, BlockPos pos) implements CustomPacketPayload {
	
	public static final Type<SearchPacket> TYPE = new Type<SearchPacket>(ResourceLocation.fromNamespaceAndPath(NaturesCompass.MODID, "search"));
	
	public static final StreamCodec<FriendlyByteBuf, SearchPacket> CODEC = StreamCodec.ofMember(SearchPacket::write, SearchPacket::read);

	public static SearchPacket read(FriendlyByteBuf buf) {
		return new SearchPacket(buf.readResourceLocation(), buf.readBlockPos());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeResourceLocation(biomeKey);
		buf.writeBlockPos(pos);
	}

	public static void handle(SearchPacket packet, IPayloadContext context) {
		if (context.flow().isServerbound()) {
			context.enqueueWork(() -> {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
				if (!stack.isEmpty()) {
					final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
					natureCompass.searchForBiome((ServerLevel) context.player().level(), context.player(), packet.biomeKey, packet.pos, stack);
				}
			});
		}
	}

	@Override
	public Type<SearchPacket> type() {
		return TYPE;
	}

}
