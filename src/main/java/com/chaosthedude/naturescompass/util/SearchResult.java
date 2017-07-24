package com.chaosthedude.naturescompass.util;

public class SearchResult {

	private int x;
	private int z;
	private int radius;
	private int samples;
	private boolean found;

	public SearchResult(int x, int z, int radius, int samples, boolean found) {
		this.x = x;
		this.z = z;
		this.radius = radius;
		this.samples = samples;
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
	
	public int getSamples() {
		return samples;
	}

	public boolean found() {
		return found;
	}

}
