package com.younha.supersonic;

import android.util.Log;

import java.util.HashMap;

public class Encoder {

    private CharSequence alphabet;
    private double minFrequency;
    private double maxFrequency;
    private HashMap<Character, Double> map;

    public Encoder(CharSequence alphabet, double minFrequency, double maxFrequency) {
        this.alphabet = alphabet;
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;

        map = new HashMap<Character, Double>();

        double interval = 0.0;
        if (alphabet.length() > 1) {
            interval = (maxFrequency - minFrequency) / (alphabet.length() - 1);
        }

        for (int i = 0; i < alphabet.length(); i++) {
            map.put(alphabet.charAt(i), minFrequency + i * interval);
        }
    }

    public double[] encode(CharSequence input) {
        double[] encoded = new double[input.length()];
        for (int i = 0; i < input.length(); i++) {
            encoded[i] = map.get(input.charAt(i));
            Log.e("encoding", encoded[i] + "");
        }

        return encoded;
    }
}
