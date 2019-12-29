package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class ItemUtils {

	public static boolean verifyNBT(ItemStack stack) {
		if (stack.isEmpty() || stack.getItem() != NaturesCompass.naturesCompass) {
			return false;
		} else if (!stack.hasTag()) {
			stack.setTag(new CompoundNBT());
		}

		return true;
	}

	public static ItemStack getHeldNatureCompass(PlayerEntity player) {
		return getHeldItem(player, NaturesCompass.naturesCompass);
	}

	public static ItemStack getHeldItem(PlayerEntity player, Item item) {
		if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == item) {
			return player.getHeldItemMainhand();
		} else if (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() == item) {
			return player.getHeldItemOffhand();
		}

		return ItemStack.EMPTY;
	}

}
