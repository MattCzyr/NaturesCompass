package com.chaosthedude.naturescompass.client;

public enum EnumOverlaySide {
	
	LEFT,
	RIGHT;
	
	public static EnumOverlaySide fromString(String str) {
		if (str.equals("RIGHT")) {
			return RIGHT;
		}
		return LEFT;
	}
	
}