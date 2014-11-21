package com.younha.supersonic;

import java.util.HashMap;
import java.util.Iterator;


public class SignalController {

    private String alphabet;
    private double minFrequency;
    private double maxFrequency;
    private HashMap<Character, Double> lookup;

    public SignalController(String alphabet, double minFrequency, double maxFrequency) {
        this.alphabet = alphabet;
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        this.lookup = new HashMap<Character, Double>();

        double interval = 0.0;
        if (alphabet.length() > 1) {
            interval = (maxFrequency - minFrequency) / (alphabet.length() - 1);
        }

        for (int i = 0; i < alphabet.length(); i++) {
            lookup.put(alphabet.charAt(i), minFrequency + i * interval);
        }
    }

    public String getAlphabet() {
    	return alphabet;
    }
    
    public double getMinFrequency() {
    	return minFrequency;
    }
    
    public double getMaxFrequency() {
    	return maxFrequency;
    }
    
    public double encode(char code) {
    	return lookup.get(code);
    }
    
    public char decode(double frequency) {
    	Iterator<Character> itr = lookup.keySet().iterator();
    	
    	double min = 1e+12;
    	char minKey = 0;
    	
    	while(itr.hasNext()) {
    		char key = (Character) itr.next();
    		double value = lookup.get(key);
    		if(Math.abs(value - frequency) < min){
    			min = Math.abs(value - frequency);
    			minKey = key;
    		}
    	}
    	return minKey;
    }
}
