package com.chaosthedude.naturescompass.util;

public class SearchRadius {
    private int step;
    private int increment = 10000 / (1 << 4); // 625

    public SearchRadius () {
	this.step = 0;
    }
    
    private SearchRadius (int step) {
	this.step = Math.max(0,  step) % 5; // 0, 1, 2, 3, 4
    }
    
    public int getValue() {
	return (1 << step) * increment; // max is 16 * 625 == 10000
    }

    public SearchRadius next() {
	return new SearchRadius(step + 1);
    }
    
    public String getLocalizedName() {
	return Integer.toString(this.getValue());
    }
}
