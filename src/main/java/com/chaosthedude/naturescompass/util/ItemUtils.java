package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemUtils {

	public static boolean verifyNBT(ItemStack stack) {
		if (stack == null || stack.getItem() != NaturesCompass.naturesCompass) {
			return false;
		} else if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}

		return true;
	}

}
