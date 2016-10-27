package com.chaosthedude.naturescompass.util;

public enum EnumCompassState {

	INACTIVE(0), SEARCHING(1), FOUND(2), NOT_FOUND(3);

	private int id;

	EnumCompassState(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public static EnumCompassState fromID(int id) {
		for (EnumCompassState state : values()) {
			if (state.getID() == id) {
				return state;
			}
		}

		return null;
	}

}
