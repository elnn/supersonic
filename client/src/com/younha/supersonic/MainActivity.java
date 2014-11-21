package com.younha.supersonic;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;


public class MainActivity extends Activity {
	public static final double MIN_FREQUENCY = 17743;
	public static final double MAX_FREQUENCY = 19466;
	private static final String ALPHABET = "^0123456789$";
	private static final int AUDIO_READER_BLOCK_SIZE = 2048;

	private SignalController signalController;

	private int userId;
	private String uuid;
	private HttpClient client;
	private String lastResult;

	private MainHandler handler;
	private SoundScheduler soundScheduler;
	private TextView textviewStatus;
	private TextView textviewDecode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Context c = this.getApplicationContext();
		this.uuid = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
		this.client = new HttpClient();
		this.handler = new MainHandler();
		this.lastResult = "";
		soundScheduler = new SoundScheduler(handler);
		signalController = new SignalController(ALPHABET, MIN_FREQUENCY, MAX_FREQUENCY);
		textviewStatus = (TextView) findViewById(R.id.text_notif);
		textviewDecode = (TextView) findViewById(R.id.textDecode);

		registerDevice();
		initAudioReader();
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
					userId = Integer.parseInt(result.getString("user_id"));
					StringBuilder userStringBuilder = new StringBuilder();
					for (char c : (userId + "").toCharArray()) {
						userStringBuilder = userStringBuilder.append(c);
						userStringBuilder = userStringBuilder.append("$");
					}
					userStringBuilder = userStringBuilder.append("^^^");
					registerSounds(userStringBuilder.toString());
					soundScheduler.start();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void registerSounds(String inputString) {

		for (char code : inputString.toCharArray()) {
			double frequency = signalController.encode(code);
			Sound sound = new Sound(frequency);
			soundScheduler.addSound(sound);
		}    	
	}

	void initAudioReader() {
		AudioReader audioReader = new AudioReader(handler);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		audioReader.startReader(Sound.getSampleRate(), AUDIO_READER_BLOCK_SIZE, new AudioReader.Listener() {
			@Override
			public final void onReadComplete(int dB) {
			}

			@Override
			public void onReadError(int error) {
				Log.e("error", "errorcode : " + error);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class MainHandler extends Handler {

		public static final int MODE_TEXTVIEW_UPDATE = 1;
		public static final int MODE_AUDIO_DECODE = 2;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch(msg.what)
			{
			case MODE_TEXTVIEW_UPDATE:
				textviewStatus.setText((String) msg.obj);
				break;
			case MODE_AUDIO_DECODE:
				double frequency = (Double)msg.obj;
				if(frequency > 0) {
					char c = signalController.decode((Double)msg.obj);
					lastResult += c;
					if( lastResult.length() > 10 ) {
						lastResult = lastResult.substring(lastResult.length() - 10 );
					}
				}
				textviewDecode.setText(lastResult);
				break;
			}
		}
	}
}