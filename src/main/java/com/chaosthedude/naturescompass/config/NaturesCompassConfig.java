package com.chaosthedude.naturescompass.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.utils.OverlaySide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public class NaturesCompassConfig {
	
	private static Path configFilePath;
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static boolean allowTeleport = true;
	public static boolean displayCoordinates = true;
	public static int maxSamples = 50000;
	public static int radiusModifier = 2500;
	public static int sampleSpaceModifier = 16;
	public static List<String> biomeBlacklist = new ArrayList<String>();
	
	public static boolean displayWithChatOpen = true;
	public static boolean fixBiomeNames = true;
	public static int overlayLineOffset = 1;
	public static OverlaySide overlaySide = OverlaySide.LEFT;
	
	public static void load() {
		Reader reader;
		if(getFilePath().toFile().exists()) {
			try {
				reader = Files.newBufferedReader(getFilePath());
				
				Data data = gson.fromJson(reader, Data.class);
				
				allowTeleport = data.common.allowTeleport;
				displayCoordinates = data.common.displayCoordinates;
				maxSamples = data.common.maxSamples;
				radiusModifier = data.common.radiusModifier;
				sampleSpaceModifier = data.common.sampleSpaceModifier;
				biomeBlacklist = data.common.biomeBlacklist;
				
				displayWithChatOpen = data.client.displayWithChatOpen;
				fixBiomeNames = data.client.fixBiomeNames;
				overlayLineOffset = data.client.overlayLineOffset;
				overlaySide = data.client.overlaySide;
				
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		save();
	}
	
	public static void save() {
		try {
			Writer writer = Files.newBufferedWriter(getFilePath());
			Data data = new Data(new Data.Common(allowTeleport, displayCoordinates, maxSamples, radiusModifier, sampleSpaceModifier, biomeBlacklist), new Data.Client(displayWithChatOpen, fixBiomeNames, overlayLineOffset, overlaySide));
			gson.toJson(data, writer);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Path getFilePath() {
		if(configFilePath == null) {
			configFilePath = FabricLoader.getInstance().getConfigDir().resolve(NaturesCompass.MODID + ".json");
		}
		return configFilePath;
	}
	
	private static class Data {
		
		private Common common;
		private Client client;
		
		public Data(Common common, Client client) {
			this.common = common;
			this.client = client;
		}
		
		private static class Common {
			private final String allowTeleportComment = "Allows a player to teleport to a located biome when in creative mode, opped, or in cheat mode.";
			private final boolean allowTeleport;
			
			private final String displayCoordinatesComment = "Allows players to view the precise coordinates and distance of a located structure on the HUD, rather than relying on the direction the compass is pointing.";
			private final boolean displayCoordinates;
			
			private final String maxSamplesComment = "The maximum number of samples to be taken when searching for a biome.";
			private final int maxSamples;
			
			private final String radiusModifierComment = "biomeSize * radiusModifier = maxSearchRadius. Raising this value will increase search accuracy but will potentially make the process more resource .";
			private final int radiusModifier;
			
			private final String sampleSpaceModifierComment = "biomeSize * sampleSpaceModifier = sampleSpace. Lowering this value will increase search accuracy but will make the process more resource intensive.";
			private final int sampleSpaceModifier;
			
			private final String biomeBlacklistComment = "A list of biomes that the compass will not be able to search for, specified by resource location. The wildcard character * can be used to match any number of characters, and ? can be used to match one character. Ex (ignore backslashes): [\"minecraft:savanna\", \"minecraft:desert\", \"minecraft:*ocean*\"]";
			private final List<String> biomeBlacklist;
			
			private Common() {
				allowTeleport = true;
				displayCoordinates = true;
				maxSamples = 50000;
				radiusModifier = 2500;
				sampleSpaceModifier = 16;
				biomeBlacklist = new ArrayList<String>();
			}
			
			private Common(boolean allowTeleport, boolean displayCoordinates, int maxSamples, int radiusModifier, int sampleSpaceModifier, List<String> biomeBlacklist) {
				this.allowTeleport = allowTeleport;
				this.displayCoordinates = displayCoordinates;
				this.maxSamples = maxSamples;
				this.radiusModifier = radiusModifier;
				this.sampleSpaceModifier = sampleSpaceModifier;
				this.biomeBlacklist = biomeBlacklist;
			}
		}
		
		private static class Client {
			private final String displayWithChatOpenComment = "Displays Nature's Compass information even while chat is open.";
			private final boolean displayWithChatOpen;
			
			private final String fixBiomeNamesComment = "Fixes biome names by adding missing spaces. Ex: ForestHills becomes Forest Hills";
			private final boolean fixBiomeNames;
			
			private final String overlayLineOffsetComment = "The line offset for information rendered on the HUD.";
			private final int overlayLineOffset;
			
			private final String overlaySideComment = "The side for information rendered on the HUD. Ex: LEFT, RIGHT";
			private final OverlaySide overlaySide;
			
			private Client() {
				displayWithChatOpen = true;
				fixBiomeNames = true;
				overlayLineOffset = 1;
				overlaySide = OverlaySide.LEFT;
			}
			
			private Client(boolean displayWithChatOpen, boolean fixBiomeNames, int overlayLineOffset, OverlaySide overlaySide) {
				this.displayWithChatOpen = displayWithChatOpen;
				this.fixBiomeNames = fixBiomeNames;
				this.overlayLineOffset = overlayLineOffset;
				this.overlaySide = overlaySide;
			}
		}
	}
	
}
