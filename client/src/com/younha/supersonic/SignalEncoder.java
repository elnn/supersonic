package com.younha.supersonic;

import java.util.HashMap;


public class SignalEncoder {

    private String alphabet;
    private double minFrequency;
    private double maxFrequency;
    private HashMap<Character, Double> lookup;

    public SignalEncoder(String alphabet, double minFrequency, double maxFrequency) {
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
}
