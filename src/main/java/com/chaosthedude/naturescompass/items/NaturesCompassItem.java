package com.chaosthedude.naturescompass.items;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.network.RequestSyncPacket;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NaturesCompassItem extends Item {

	public static final String NAME = "naturescompass";

	public NaturesCompassItem() {
		super(new Properties().maxStackSize(1).group(ItemGroup.TOOLS));
		setRegistryName(NAME);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		if (!player.isCrouching()) {
			if (world.isRemote) {
				final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
				NaturesCompass.network.sendToServer(new RequestSyncPacket());
				GuiWrapper.openGUI(world, player, stack);
			}
		} else {
			setState(player.getHeldItem(hand), null, CompassState.INACTIVE, player);
		}

		return new ActionResult<ItemStack>(ActionResultType.PASS, player.getHeldItem(hand));
	}

	public void searchForBiome(World world, PlayerEntity player, ResourceLocation biomeKey, BlockPos pos, ItemStack stack) {
		setSearching(stack, biomeKey, player);
		BiomeUtils.searchForBiome(world, player, stack, BiomeUtils.getBiomeForKey(biomeKey), pos);
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, ResourceLocation biomeKey, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("BiomeKey", biomeKey.toString());
			stack.getTag().putInt("State", CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, int x, int z, int samples, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.FOUND.getID());
			stack.getTag().putInt("FoundX", x);
			stack.getTag().putInt("FoundZ", z);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setNotFound(ItemStack stack, PlayerEntity player, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getTag().putInt("SearchRadius", searchRadius);
			stack.getTag().putInt("Samples", samples);
		}
	}

	public void setInactive(ItemStack stack, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", state.getID());
		}
	}

	public void setFoundBiomeX(ItemStack stack, int x, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundX", x);
		}
	}

	public void setFoundBiomeZ(ItemStack stack, int z, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundZ", z);
		}
	}

	public void setBiomeKey(ItemStack stack, ResourceLocation biomeKey, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("BiomeKey", biomeKey.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, PlayerEntity player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, PlayerEntity player) {
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

	public int getDistanceToBiome(PlayerEntity player, ItemStack stack) {
		return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
	}

}
