package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemUtils {

	public static boolean verifyNBT(ItemStack stack) {
		if (!stackExists(stack) || stack.getItem() != NaturesCompass.naturesCompass) {
			return false;
		} else if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}

		return true;
	}

	public static ItemStack getHeldNatureCompass(EntityPlayer player) {
		return getHeldItem(player, NaturesCompass.naturesCompass);
	}

	public static ItemStack getHeldItem(EntityPlayer player, Item item) {
		if (stackExists(player.getHeldItemMainhand()) && player.getHeldItemMainhand().getItem() == item) {
			return player.getHeldItemMainhand();
		} else if (stackExists(player.getHeldItemOffhand()) && player.getHeldItemOffhand().getItem() == item) {
			return player.getHeldItemOffhand();
		}

		return ItemStack.field_190927_a;
	}

	public static boolean stackExists(ItemStack stack) {
		return !stack.func_190926_b();
	}

}
