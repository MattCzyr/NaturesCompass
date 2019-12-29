package com.chaosthedude.naturescompass.util;

public enum CompassState {

	INACTIVE(0), SEARCHING(1), FOUND(2), NOT_FOUND(3);

	private int id;

	CompassState(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public static CompassState fromID(int id) {
		for (CompassState state : values()) {
			if (state.getID() == id) {
				return state;
			}
		}

		return null;
	}

}
