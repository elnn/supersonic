package com.younha.supersonic;


public class Sound {
	private static final double DURATION_IN_SECONDS = 0.5;
	private static final int SAMPLE_RATE = 44100;
	private static final int SAMPLE_SIZE = (int) (DURATION_IN_SECONDS * SAMPLE_RATE);
	
	private byte[] rawBytes;
	private double frequency;

	public Sound(double frequency) {
		this.rawBytes = new byte[2 * SAMPLE_SIZE];
		this.frequency = frequency;
		
		for (int j = 0; j < SAMPLE_SIZE; ++j) {
			double sample = Math.sin(2 * Math.PI * j * frequency / SAMPLE_RATE);
			short val = (short) (sample * Short.MAX_VALUE);
			rawBytes[j * 2] = (byte) (val & 0x00ff);
			rawBytes[j * 2 + 1] = (byte) ((val & 0xff00) >>> 8);
		}
	}
	
	public static double getDurationInSeconds() {
		return DURATION_IN_SECONDS;
	}
	
	public static int getSampleRate() {
		return SAMPLE_RATE;
	}
	
	public static int getSampleSize() {
		return SAMPLE_SIZE;
	}
	
	public byte[] getRawBytes() {
		return rawBytes;
	}
	
	public double getFrequency() {
		return frequency;
	}
}