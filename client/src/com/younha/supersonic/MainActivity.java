package com.younha.supersonic;

import org.json.JSONException;
import org.json.JSONObject;

import com.younha.supersonic.HttpClient.SendHttp;

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
    private final double duration = 1.0; // seconds
    private final int sampleRate = 44100;
    private int numSamples;// = (int)(duration * sampleRate);
    private double sample[][];// = new double[numSamples];
    
    
    boolean generating = false;
    
    AudioTrack audioTrack;
    
    Context c;
 
    Handler handler = new Handler();
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c = this.getApplicationContext();
        final HttpClient client = new HttpClient(this.getApplicationContext());
        ////Audio Reader
        
        
        /**  register **/
        HttpClient.SendHttp send = client.Send();
        send.execute("register", client.uuid, new HttpClient.HttpListener() {
			
			@Override
			public void onSendError(int error) {
				
			}
			@Override
			public void onSendComplete(JSONObject result) {
				try {
					Log.e("onsend", result.getString("user_id"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        /******************/
        
        
        
        
        initAudioReader();
        
        final TextView text_notif = (TextView)findViewById(R.id.text_notif);
        
        
        //genTone(frequency);
        
        Encoder encoder = new Encoder("0123456789",18000,18900);
        
        final CharSequence input = "0123456789";
        final byte generatedSnd[][] = genTones(encoder.encode(input));
        
        Thread t = new Thread(new Runnable() {
    		
    		@Override
    		public void run() {
    			while(true){
    				
    				handler.post(new Runnable() {
                        public void run() {
                        	text_notif.setText("emitting!! ");
                        }
                    });

    				
    				
    				for(int i = 0; i<input.length(); i++){
    					playSound(generatedSnd[i]);
    					try{
        					Thread.sleep((long)(duration * 1000));
        				}catch(InterruptedException e){}
    					Log.e("emit", input.charAt(i)+"");
    					audioTrack.release();
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
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    }
    
    
    void initAudioReader(){
    	AudioReader audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
       
        int inputBlockSize = 256;
        int sampleDecimate = 1;
        
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
    }
    
    byte[][] genTones(double[] frequency){
    	// fill out the array
    	
    	int freq_num = frequency.length;
    	numSamples = (int)(duration * sampleRate);
    	sample = new double[freq_num][numSamples];
    	
    	for(int i = 0; i < freq_num; i++) {
    		
    		for (int j = 0; j < duration * sampleRate; ++j) {
                //sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
            	sample[i][j] = Math.sin(2 * Math.PI * j * frequency[i] / sampleRate);
            	
            }
    	}
    	
    	
    	byte[][] generatedSnd = new byte[freq_num][2 * numSamples];

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        
        for( int i = 0; i < freq_num; i ++ ) {
        	int idx = 0;
	        for (final double dVal : sample[i]) {
	            // scale to maximum amplitude
	            final short val = (short) ((dVal * 32767));
	            // in 16 bit wav PCM, first byte is the low order byte
	            generatedSnd[i][idx++] = (byte) (val & 0x00ff);
	            generatedSnd[i][idx++] = (byte) ((val & 0xff00) >>> 8);
	        }
        }
        return generatedSnd;
    }
    
    void playSound(byte[] generatedSound){
    	
    	
    	//final AudioTrack 
        audioTrack = new AudioTrack(AudioManager.STREAM_RING,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);

        int state = 0;
        int current = 0;
        while(true){
        	state = audioTrack.write(generatedSound, current, generatedSound.length - current);
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
        	
        	if(current == generatedSound.length){
        		break;
        	}
        }
        
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

