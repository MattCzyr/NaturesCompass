package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.util.BiomeUtils;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

	public static final int ID_NATURES_COMPASS = 0;

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == ID_NATURES_COMPASS) {
			final ItemStack stack = player.getHeldItem();
			if (stack != null && stack.getItem() == NaturesCompass.naturesCompass) {
				return new GuiNaturesCompass(world, player, BiomeUtils.getAllowedBiomes());
			}
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

}
