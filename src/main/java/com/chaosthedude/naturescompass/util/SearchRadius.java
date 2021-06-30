package com.chaosthedude.naturescompass.util;

public class SearchRadius {
    private int step;
    private int increment = (1 << 7); // 128

    public SearchRadius () {
	this.step = 0;
    }
    
    private SearchRadius (int step) {
	this.step = Math.max(0,  step) % 7; // cycles through 0 - 6
    }
    
    public int getValue() {
	return increment << step; // 128, 256, 512, ..., 8192
    }

    public SearchRadius next() {
	return new SearchRadius(step + 1);
    }
    
    public String getLocalizedName() {
	if (this.getValue() > 4096) {
	    	return "Vast";
	} else {
		return Integer.toString(this.getValue());	    
	}
    }
}
