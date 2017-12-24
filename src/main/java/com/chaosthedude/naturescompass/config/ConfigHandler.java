package com.chaosthedude.naturescompass.config;

import java.io.File;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.Lists;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigHandler {

	public static Configuration config;

	public static boolean allowTeleport = true;
	public static String[] biomeBlacklist = {};
	public static int distanceModifier = 2500;
	public static int sampleSpaceModifier = 16;
	public static int maxSamples = 100000;
	public static boolean displayWithChatOpen = true;
	public static boolean fixBiomeNames = true;
	public static int lineOffset = 1;

	public static void loadConfig(File configFile) {
		config = new Configuration(configFile);

		config.load();
		init();

		MinecraftForge.EVENT_BUS.register(new ChangeListener());
	}

	public static void init() {
		String comment;

		comment = "Allows a player to teleport to a located biome when in creative mode, opped, or in cheat mode.";
		allowTeleport = loadBool(Configuration.CATEGORY_GENERAL, "naturescompass.allowTeleport", comment, allowTeleport);

		comment = "biomeSize * distanceModifier = maxSearchDistance. Raising this value will increase search accuracy but will potentially make the process more resource intensive.";
		distanceModifier = loadInt(Configuration.CATEGORY_GENERAL, "naturescompass.distanceModifier", comment, distanceModifier);

		comment = "biomeSize * sampleSpaceModifier = sampleSpace. Lowering this value will increase search accuracy but will make the process more resource intensive.";
		sampleSpaceModifier = loadInt(Configuration.CATEGORY_GENERAL, "naturescompass.sampleSpaceModifier", comment, sampleSpaceModifier);

		comment = "A list of biomes that the compass will not be able to search for. Both biome names and numerical biome IDs are accepted.";
		biomeBlacklist = loadStringArray(Configuration.CATEGORY_GENERAL, "naturescompass.biomeBlacklist", comment, biomeBlacklist);

		comment = "The maximum samples to be taken when searching for a biome.";
		maxSamples = loadInt(Configuration.CATEGORY_GENERAL, "naturescompass.maxSamples", comment, maxSamples);
		
		comment = "Displays Nature's Compass information even while chat is open.";
		displayWithChatOpen = loadBool(Configuration.CATEGORY_CLIENT, "naturescompass.displayWithChatOpen", comment, displayWithChatOpen);

		comment = "Fixes biome names by adding missing spaces. Ex: ForestHills becomes Forest Hills";
		fixBiomeNames = loadBool(Configuration.CATEGORY_CLIENT, "naturescompass.fixBiomeNames", comment, fixBiomeNames);

		comment = "The line offset for information rendered on the HUD.";
		lineOffset = loadInt(Configuration.CATEGORY_CLIENT, "naturescompass.lineOffset", comment, lineOffset);

		if (config.hasChanged()) {
			config.save();
		}
	}

	public static int loadInt(String category, String name, String comment, int def) {
		final Property prop = config.get(category, name, def);
		prop.setComment(comment);
		int val = prop.getInt(def);
		if (val < 0) {
			val = def;
			prop.set(def);
		}

		return val;
	}

	public static boolean loadBool(String category, String name, String comment, boolean def) {
		final Property prop = config.get(category, name, def);
		prop.setComment(comment);
		return prop.getBoolean(def);
	}

	public static String[] loadStringArray(String category, String comment, String name, String[] def) {
		Property prop = config.get(category, name, def);
		prop.setComment(comment);
		return prop.getStringList();
	}

	public static List<String> getBiomeBlacklist() {
		return Lists.newArrayList(biomeBlacklist);
	}

	public static class ChangeListener {
		@SubscribeEvent
		public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(NaturesCompass.MODID)) {
				init();
			}
		}
	}

}
