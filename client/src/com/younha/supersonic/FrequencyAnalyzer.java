package com.younha.supersonic;

import java.util.ArrayList;

public class FrequencyAnalyzer {

    private final int SAMPLE_RATE = 44100;
    private int FRAME_SIZE;

    private ArrayList<Double> data;

    public FrequencyAnalyzer(ArrayList<Double> data) {
        this.data = data;
        FRAME_SIZE = data.size();
    }

    public int frequencyToIndex(double freq) {
        return (int) Math.round(freq / SAMPLE_RATE * FRAME_SIZE);
    }
    public double indexToFrequency(int idx) {
        return (double) idx * SAMPLE_RATE / FRAME_SIZE;
    }

    public double getMostProbableFrequency(double minFrequency, double maxFrequency, double percentage) {
        Complex[] waveData = new Complex[FRAME_SIZE];
        for (int i = 0; i < FRAME_SIZE; i++) {
            waveData[i] = new Complex(data.get(i), 0.);
        }
        Complex[] frequencyComplexData = FFT.fft(waveData);

        ArrayList<Double> frequencyData = new ArrayList<Double>();
        for (int i = 0; i < frequencyComplexData.length; i++) {
            frequencyData.add(frequencyComplexData[i].abs());
        }

        int maxRank = -1;
        int maxRankIndex = -1;

        int minIndex = frequencyToIndex(minFrequency);
        int maxIndex = frequencyToIndex(maxFrequency);

        for (int i = minIndex; i <= maxIndex; i ++) {
            int rank = 0;
            for (double value : frequencyData) {
                if (frequencyData.get(i) > value) {
                    rank ++;
                }
            }
            if (rank > maxRank) {
                maxRank = rank;
                maxRankIndex = i;
            }
        }
        
        if (maxRank + 1 >= frequencyData.size() * (1.0 - percentage)) {
            return indexToFrequency(maxRankIndex);
        }
        
        return -1000.0;
    }
    
}
