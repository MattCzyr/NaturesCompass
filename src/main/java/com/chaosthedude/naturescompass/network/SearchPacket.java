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
import net.minecraft.util.math.BlockPos;

public record SearchPacket(Identifier biomeID, BlockPos pos) implements CustomPayload {
	
	public static final CustomPayload.Id<SearchPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(NaturesCompass.MODID, "search"));
	
	public static final PacketCodec<RegistryByteBuf, SearchPacket> PACKET_CODEC = PacketCodec.of(SearchPacket::write, SearchPacket::read);
	
	public static SearchPacket read(RegistryByteBuf buf) {
		return new SearchPacket(buf.readIdentifier(), buf.readBlockPos());
	}
	
	public void write(RegistryByteBuf buf) {
		buf.writeIdentifier(biomeID);
		buf.writeBlockPos(pos);
	}
	
	@Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

    public static void apply(SearchPacket packet, ServerPlayNetworking.Context context) {
    	context.player().getServer().execute(() -> {
	    	final ItemStack stack = ItemUtils.getHeldNatureCompass(context.player());
			if (!stack.isEmpty()) {
				final NaturesCompassItem natureCompass = (NaturesCompassItem) stack.getItem();
				final ServerWorld world = context.player().getServerWorld();
				natureCompass.searchForBiome(world, context.player(), packet.biomeID(), packet.pos(), stack);
			}
		});
	}

}