package com.chaosthedude.naturescompass.config;

import java.io.File;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.google.common.collect.Lists;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigHandler {

	public static Configuration config;

	public static int maxSearchDistance = 10000;
	public static int sampleSpace = 64;
	public static String[] biomeBlacklist = {};
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

		comment = "The maximum distance the compass will search. Raising this value will increase search accuracy but will potentially make the process more resource intensive.";
		maxSearchDistance = loadInt(Configuration.CATEGORY_GENERAL, "naturescompass.distanceModifier", comment, maxSearchDistance);

		comment = "The space between samples taken by the compass when searching. Lowering this value will increase search accuracy but will make the process more resource intensive.";
		sampleSpace = loadInt(Configuration.CATEGORY_GENERAL, "naturescompass.sampleSpaceModifier", comment, sampleSpace);

		comment = "A list of biomes that the compass will not be able to search for. Both biome names and numerical biome IDs are accepted.";
		biomeBlacklist = loadStringArray(Configuration.CATEGORY_GENERAL, "naturescompass.biomeBlacklist", comment, biomeBlacklist);

		comment = "Displays Nature's Compass information even while chat is open.";
		displayWithChatOpen = loadBool("client", "naturescompass.displayWithChatOpen", comment, displayWithChatOpen);

		comment = "Fixes biome names by adding missing spaces. Ex: ForestHills becomes Forest Hills";
		fixBiomeNames = loadBool("client", "naturescompass.fixBiomeNames", comment, fixBiomeNames);

		comment = "The line offset for information rendered on the HUD.";
		lineOffset = loadInt("client", "naturescompass.lineOffset", comment, lineOffset);

		if (config.hasChanged()) {
			config.save();
		}
	}

	public static int loadInt(String category, String name, String comment, int def) {
		final Property prop = config.get(category, name, def);
		prop.comment = comment;
		int val = prop.getInt(def);
		if (val < 0) {
			val = def;
			prop.set(def);
		}

		return val;
	}

	public static boolean loadBool(String category, String name, String comment, boolean def) {
		final Property prop = config.get(category, name, def);
		prop.comment = comment;
		return prop.getBoolean(def);
	}

	public static String[] loadStringArray(String category, String comment, String name, String[] def) {
		Property prop = config.get(category, name, def);
		prop.comment = comment;
		return prop.getStringList();
	}

	public static List<String> getBiomeBlacklist() {
		return Lists.newArrayList(biomeBlacklist);
	}

	public static class ChangeListener {
		@SubscribeEvent
		public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.modID.equals(NaturesCompass.MODID)) {
				init();
			}
		}
	}

}
