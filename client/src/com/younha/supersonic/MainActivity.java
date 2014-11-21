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
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;


public class MainActivity extends Activity {
	private static final double MIN_FREQUENCY = 18000;
    private static final double MAX_FREQUENCY = 19000;
    private static final String ALPHABET = "0123456789";
    
    private String uuid;
    private HttpClient client;

    private Handler handler;
    private SoundScheduler soundScheduler;
    private TextView textviewStatus;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context c = this.getApplicationContext();
        this.uuid = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
        this.client = new HttpClient();
        registerDevice();

        initAudioReader();
    
        textviewStatus = (TextView) findViewById(R.id.text_notif);
        handler = new MainHandler();
        
        soundScheduler = new SoundScheduler(handler);

        String testInput = "0123456789";
        registerSounds(testInput);
        
        soundScheduler.start();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    private void registerDevice() {
        HttpClient.SendHttp send = client.Send();
        send.execute("register", uuid, new HttpClient.HttpListener() {

            @Override
            public void onSendError(int error) {
            }

            @Override
            public void onSendComplete(JSONObject result) {
                try {
                    Log.e("onsend", result.getString("user_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void registerSounds(String inputString) {
        SignalEncoder encoder = new SignalEncoder(ALPHABET, MIN_FREQUENCY, MAX_FREQUENCY);
        
        for (char code : inputString.toCharArray()) {
        	double frequency = encoder.encode(code);
        	Sound sound = new Sound(frequency);
        	soundScheduler.addSound(sound);
        }    	
    }

    void initAudioReader() {
        AudioReader audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        final int inputBlockSize = 256;
        final int sampleDecimate = 1;

        audioReader.startReader(Sound.getSampleRate(), inputBlockSize * sampleDecimate, new AudioReader.Listener() {
            @Override
            public final void onReadComplete(int dB) {
            }
            
            @Override
            public void onReadError(int error) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private class MainHandler extends Handler {
    	@Override
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		textviewStatus.setText((String) msg.obj);
    	}
    }
}