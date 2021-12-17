package com.chaosthedude.naturescompass.utils;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemUtils {
	
	public static boolean verifyNBT(ItemStack stack) {
		if (stack.isEmpty() || stack.getItem() != NaturesCompass.NATURES_COMPASS_ITEM) {
			return false;
		} else if (!stack.hasNbt()) {
			stack.setNbt(new NbtCompound());
		}

		return true;
	}

	public static ItemStack getHeldNatureCompass(PlayerEntity player) {
		return getHeldItem(player, NaturesCompass.NATURES_COMPASS_ITEM);
	}

	public static ItemStack getHeldItem(PlayerEntity player, Item item) {
		if (!player.getMainHandStack().isEmpty() && player.getMainHandStack().getItem() == item) {
			return player.getMainHandStack();
		} else if (!player.getOffHandStack().isEmpty() && player.getOffHandStack().getItem() == item) {
			return player.getOffHandStack();
		}

		return ItemStack.EMPTY;
	}

}
