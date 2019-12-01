package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.ItemNaturesCompass;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.ItemUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		final ItemStack stack = ItemUtils.getHeldNatureCompass(player);
		if (!stack.isEmpty()) {
			return new GuiNaturesCompass(world, player, stack, (ItemNaturesCompass) stack.getItem(), NaturesCompass.allowedBiomes);
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

}
