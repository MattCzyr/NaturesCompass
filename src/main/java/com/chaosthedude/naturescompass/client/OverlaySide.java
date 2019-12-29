package com.chaosthedude.naturescompass.client;

public enum OverlaySide {

	LEFT, RIGHT;

	public static OverlaySide fromString(String str) {
		if (str.equals("RIGHT")) {
			return RIGHT;
		}
		return LEFT;
	}

}
