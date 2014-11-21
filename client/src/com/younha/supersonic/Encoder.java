package com.younha.supersonic;

import java.util.HashMap;

import android.util.Log;

public class Encoder {
	
	//String[] alphabat;
	//double min_frequency;
	//double max_frequency;
	
	HashMap<Character,Double> map;
	
	public Encoder(CharSequence alphabat, double min_frequency, double max_frequency){
		
		
		//this.alphabat = alphabat;
		//this.min_frequency = min_frequency;
		//this.max_frequency = max_frequency;
		
		map = new HashMap<Character, Double>();
		
		double interval = 0.0;
		if(alphabat.length() > 1){
			interval = (max_frequency - min_frequency) / (alphabat.length() - 1);
		}
		
		for(int i = 0; i<alphabat.length(); i++){
			map.put(alphabat.charAt(i), min_frequency + i * interval);
		}
		
		
	}
	
	public double[] encode(CharSequence input){
		double[] encoded = new double[input.length()];
		for(int i = 0; i<input.length(); i++){
			encoded[i] = map.get(input.charAt(i));
			Log.e("encoding", encoded[i] + "");
			
		}
		return encoded;
		
	}
}
