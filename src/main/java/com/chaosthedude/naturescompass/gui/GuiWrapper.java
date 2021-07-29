package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuiWrapper {
	
	public static void openGUI(Level level, Player player, ItemStack stack) {
		Minecraft.getInstance().setScreen(new NaturesCompassScreen(level, player, stack, (NaturesCompassItem) stack.getItem(), NaturesCompass.allowedBiomes));
	}

}
