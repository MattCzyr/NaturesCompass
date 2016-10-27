package com.chaosthedude.naturescompass.util;

public class SearchResult {

	private int x;
	private int z;
	private int radius;
	private boolean found;

	public SearchResult(int x, int z, int radius, boolean found) {
		this.x = x;
		this.z = z;
		this.radius = radius;
		this.found = found;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public int getRadius() {
		return radius;
	}

	public boolean found() {
		return found;
	}

}
