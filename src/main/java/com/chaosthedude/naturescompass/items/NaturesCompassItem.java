package com.chaosthedude.naturescompass.items;

import java.util.List;
import java.util.Optional;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.network.NetworkDirection;

public class NaturesCompassItem extends Item {

	public static final String NAME = "naturescompass";

	public NaturesCompassItem() {
		super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!player.isCrouching()) {
			if (level.isClientSide()) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				GuiWrapper.openGUI(level, player, stack);
			} else {
				final ServerPlayer serverPlayer = (ServerPlayer) player;
				final boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(serverPlayer.getServer(), player);
				final List<ResourceLocation> allowedBiomeKeys = BiomeUtils.getAllowedBiomeKeys(level);
				NaturesCompass.network.sendTo(new SyncPacket(canTeleport, allowedBiomeKeys), serverPlayer.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
			}
		} else {
			setState(player.getItemInHand(hand), null, CompassState.INACTIVE, player);
		}

		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, player.getItemInHand(hand));
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (getState(oldStack) == getState(newStack)) {
			return false;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	public void searchForBiome(Level level, Player player, ResourceLocation biomeKey, BlockPos pos, ItemStack stack) {
		setSearching(stack, biomeKey, player);
		Optional<Biome> optionalBiome = BiomeUtils.getBiomeForKey(level, biomeKey);
		if (optionalBiome.isPresent()) {
			BiomeUtils.searchForBiome(level, player, stack, optionalBiome.get(), pos);
		}
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, ResourceLocation biomeKey, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("BiomeKey", biomeKey.toString());
			stack.getTag().putInt("State", CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.FOUND.getID());
			stack.getTag().putInt("FoundX", x);
			stack.getTag().putInt("FoundZ", z);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setNotFound(ItemStack stack, Player player, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getTag().putInt("SearchRadius", searchRadius);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setInactive(ItemStack stack, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", state.getID());
		}
	}

	public void setFoundBiomeX(ItemStack stack, int x, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundX", x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundZ", z);
		}
	}

	public void setBiomeKey(ItemStack stack, ResourceLocation biomeKey, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("BiomeKey", biomeKey.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("Samples", samples);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return CompassState.fromID(stack.getTag().getInt("State"));
		}

		return null;
	}
	
	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
 		if (ItemUtils.verifyNBT(stack)) {
 			stack.getTag().putBoolean("DisplayCoordinates", displayPosition);
 		}
 	}

	public int getFoundBiomeX(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("FoundX");
		}

		return 0;
	}

	public int getFoundBiomeZ(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("FoundZ");
		}

		return 0;
	}

	public ResourceLocation getBiomeKey(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return new ResourceLocation(stack.getTag().getString("BiomeKey"));
		}

		return new ResourceLocation("");
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("SearchRadius");
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("Samples");
		}

		return -1;
	}

	public int getDistanceToBiome(Player player, ItemStack stack) {
		return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
	}
	
	public boolean shouldDisplayCoordinates(ItemStack stack) {
 		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("DisplayCoordinates")) {
 			return stack.getTag().getBoolean("DisplayCoordinates");
 		}

 		return true;
 	}

}
