package com.younha.supersonic;

import java.util.HashMap;
import java.util.Iterator;


public class SignalController {

    private String alphabet;
    private double minFrequency1;
    private double maxFrequency1;
    private double minFrequency2;
    private double maxFrequency2;
    private HashMap<Character, Double> lookup1;
    private HashMap<Character, Double> lookup2;

    public SignalController(String alphabet, double minFrequency1, double maxFrequency1, double minFrequency2, double maxFrequency2) {
        this.alphabet = alphabet;
        this.minFrequency1 = minFrequency1;
        this.maxFrequency1 = maxFrequency1;
        this.minFrequency2 = minFrequency2;
        this.maxFrequency2 = maxFrequency2;
        this.lookup1 = new HashMap<Character, Double>();
        this.lookup2 = new HashMap<Character, Double>();

        double interval1 = 0.0;
        if (alphabet.length() > 1) {
            interval1 = (maxFrequency1 - minFrequency1) / (alphabet.length() - 1);
        }

        for (int i = 0; i < alphabet.length(); i++) {
            lookup1.put(alphabet.charAt(i), minFrequency1 + i * interval1);
        }
        
        double interval2 = 0.0;
        if (alphabet.length() > 1) {
        	interval2 = (maxFrequency2 - minFrequency2) / (alphabet.length() - 1);
        }

        for (int i = 0; i < alphabet.length(); i++) {
            lookup2.put(alphabet.charAt(i), minFrequency2 + i * interval2);
        }
    }

    public String getAlphabet() {
    	return alphabet;
    }
    
    public double getMinFrequency1() {
    	return minFrequency1;
    }
    
    public double getMaxFrequency1() {
    	return maxFrequency1;
    }
    
    public double getMinFrequency2() {
    	return minFrequency2;
    }
    
    public double getMaxFrequency2() {
    	return maxFrequency2;
    }
    
    public double encode(char code) {
    	return lookup1.get(code);
    }
    
    public char decode(double frequency) {
    	Iterator<Character> itr = lookup2.keySet().iterator();
    	
    	double min = 1e+12;
    	char minKey = 0;
    	
    	while(itr.hasNext()) {
    		char key = (Character) itr.next();
    		double value = lookup2.get(key);
    		if(Math.abs(value - frequency) < min){
    			min = Math.abs(value - frequency);
    			minKey = key;
    		}
    	}
    	return minKey;
    }
}
