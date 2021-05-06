package com.chaosthedude.naturescompass.config;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.naturescompass.client.OverlaySide;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {

	private static final ForgeConfigSpec.Builder GENERAL_BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

	public static final General GENERAL = new General(GENERAL_BUILDER);
	public static final Client CLIENT = new Client(CLIENT_BUILDER);

	public static final ForgeConfigSpec GENERAL_SPEC = GENERAL_BUILDER.build();
	public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

	public static class General {
		public final ForgeConfigSpec.BooleanValue allowTeleport;
		public final ForgeConfigSpec.IntValue radiusModifier;
		public final ForgeConfigSpec.IntValue sampleSpaceModifier;
		public final ForgeConfigSpec.ConfigValue<List<String>> biomeBlacklist;
		public final ForgeConfigSpec.IntValue maxSamples;

		General(ForgeConfigSpec.Builder builder) {
			String desc;
			builder.push("General");

			desc = "Allows a player to teleport to a located biome when in creative mode, opped, or in cheat mode.";
			allowTeleport = builder.comment(desc).define("allowTeleport", true);

			desc = "biomeSize * radiusModifier = maxSearchRadius. Raising this value will increase search accuracy but will potentially make the process more resource .";
			radiusModifier = builder.comment(desc).defineInRange("radiusModifier", 2500, 0, 1000000);

			desc = "biomeSize * sampleSpaceModifier = sampleSpace. Lowering this value will increase search accuracy but will make the process more resource intensive.";
			sampleSpaceModifier = builder.comment(desc).defineInRange("sampleSpaceModifier", 16, 0, 1000000);

			desc = "A list of biomes that the compass will not be able to search for, specified by resource location. Ex: [\"minecraft:desert\", \"minecraft:savanna\"]";
			biomeBlacklist = builder.comment(desc).define("biomeBlacklist", new ArrayList<String>());

			desc = "The maximum number of samples to be taken when searching for a biome.";
			maxSamples = builder.comment(desc).defineInRange("maxSamples", 50000, 0, 1000000);

			builder.pop();
		}
	}

	public static class Client {
		public final ForgeConfigSpec.BooleanValue displayWithChatOpen;
		public final ForgeConfigSpec.BooleanValue fixBiomeNames;
		public final ForgeConfigSpec.EnumValue<OverlaySide> overlaySide;
		public final ForgeConfigSpec.IntValue overlayLineOffset;

		Client(ForgeConfigSpec.Builder builder) {
			String desc;
			builder.push("Client");

			desc = "Displays Nature's Compass information even while chat is open.";
			displayWithChatOpen = builder.comment(desc).define("displayWithChatOpen", true);

			desc = "Fixes biome names by adding missing spaces. Ex: ForestHills becomes Forest Hills";
			fixBiomeNames = builder.comment(desc).define("fixBiomeNames", true);

			desc = "The line offset for information rendered on the HUD.";
			overlayLineOffset = builder.comment(desc).defineInRange("overlayLineOffset", 1, 0, 50);

			desc = "The side for information rendered on the HUD. Ex: LEFT, RIGHT";
			overlaySide = builder.comment(desc).defineEnum("overlaySide", OverlaySide.LEFT);

			builder.pop();
		}
	}

}
