package com.younha.supersonic;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	// originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private final int duration = 2; // seconds
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    //private double freqOfTone = 18000; // hz 
    
    boolean generating = false;
    
    private AudioReader audioReader;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;
    
    AudioTrack audioTrack;
    
    Context c;

    private final byte generatedSnd[] = new byte[2 * numSamples];
 
    Handler handler = new Handler();
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c = this.getApplicationContext();
        
        
        audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new AudioReader.Listener()
        {
            @Override
            public final void onReadComplete(int dB)
            {
                receiveDecibel(dB);
            }
             
            @Override
            public void onReadError(int error)
            {
                 
            }
        });
        

        //hard coded
        double frequency = 19000;
        
        final TextView text_freq = (TextView)findViewById(R.id.text_freq);
        text_freq.setText(frequency + "");
        
        final TextView text_notif = (TextView)findViewById(R.id.text_notif);
        
        
        genTone(frequency);
        
        Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					
					playSound();
					
					handler.post(new Runnable() {

	                    public void run() {
	                    	text_notif.setText("emitting!!");
	                    }
	                });
					
					
					
					try{
						Thread.sleep(2000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					handler.post(new Runnable() {

	                    public void run() {
	                    	text_notif.setText("");
	                    }
	                });
						
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					audioTrack.release();
					
				}
				
			}
		});
        t.start();
        
        
        
    }
    
    
    private void receiveDecibel(final int dB)
    {
        //Log.e("###", dB+" dB");
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        
    }

    void genTone(double frequency){
    	
    	if(generating)
    		return;
    	generating = true;
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            //sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        	sample[i] = Math.sin(2 * Math.PI * i * frequency / sampleRate);
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        

        generating = false;
    }

    void playSound(){
    	
    	
    	//final AudioTrack 
        audioTrack = new AudioTrack(AudioManager.STREAM_RING,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);

        int state = 0;
        int current = 0;
        while(true){
        	state = audioTrack.write(generatedSnd, current, generatedSnd.length - current);
        	if(state == AudioTrack.ERROR_INVALID_OPERATION){
        		Log.e("playSound()", "ERROR_INVALID_OPERATION");
        		return;
        	}
        	else if(state == AudioTrack.ERROR_BAD_VALUE){
        		Log.e("playSound()", "ERROR_BAD_VALUE");
        	}
        	else if(state == AudioTrack.ERROR){
        		Log.e("playSound()", "ERROR");
        	}
        	else{
        		current += state;
        	}
        	
        	if(current == generatedSnd.length){
        		//Log.e("playSound()", "success");
        		break;
        	}
        }
        
        //audioTrack.write(generatedSnd, 0, generatedSnd.length);
        
        audioTrack.setStereoVolume(1.0f, 1.0f);
        
        audioTrack.play();
        audioTrack.flush();
        
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

}

