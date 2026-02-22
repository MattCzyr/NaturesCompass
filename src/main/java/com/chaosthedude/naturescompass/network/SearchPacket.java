package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.utils.ItemUtils;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public record SearchPacket(Identifier biomeId, BlockPos pos) implements CustomPacketPayload {
	
	public static final Type<SearchPacket> TYPE = new Type<SearchPacket>(Identifier.fromNamespaceAndPath(NaturesCompass.MODID, "search"));
	
	public static final StreamCodec<FriendlyByteBuf, SearchPacket> CODEC = StreamCodec.ofMember(SearchPacket::write, SearchPacket::read);
	
	public static SearchPacket read(FriendlyByteBuf buf) {
		return new SearchPacket(buf.readIdentifier(), buf.readBlockPos());
	}
	
	public void write(FriendlyByteBuf buf) {
		buf.writeIdentifier(biomeId);
		buf.writeBlockPos(pos);
	}
	
	@Override
	public Type<SearchPacket> type() {
		return TYPE;
	}

    public static void apply(SearchPacket packet, ServerPlayNetworking.Context context) {
    	context.server().execute(() -> {
	    	final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				final ServerLevel level = context.player().level();
				natureCompass.searchForBiome(level, context.player(), packet.biomeId(), packet.pos(), stack);
			}
		});
	}

}