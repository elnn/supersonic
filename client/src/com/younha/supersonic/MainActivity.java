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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;


public class MainActivity extends Activity {
	public static final double MIN_FREQUENCY1 = 44100. / 1024 * 402;
	public static final double MAX_FREQUENCY1 = 44100. / 1024 * 424;
	public static final double MIN_FREQUENCY2 = 44100. / 1024 * 442;
	public static final double MAX_FREQUENCY2 = 44100. / 1024 * 464;
	private static final String ALPHABET = "^0123456789$";
	private static final int AUDIO_READER_BLOCK_SIZE = 2048;

	private SignalController signalController;

	private int userId;
	private String uuid;
	private HttpClient client;
	private String lastResult;
	private String realResult;
	private String beforeRealResult;

	private MainHandler handler;
	private SoundScheduler soundScheduler;
	private TextView textviewStatus;
	private TextView textviewRead;
	private TextView textviewDecode;
	private TextView textviewExtra;
	private EditText editTextAge;
	private EditText editTextLocation;
	private RadioGroup radioGroupGender;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Context c = this.getApplicationContext();
		this.uuid = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
		this.client = new HttpClient();
		this.handler = new MainHandler();
		this.lastResult = "";
		this.realResult = "";
		this.beforeRealResult = "";
		soundScheduler = new SoundScheduler(handler);
		signalController = new SignalController(ALPHABET, MIN_FREQUENCY1, MAX_FREQUENCY1, MIN_FREQUENCY2, MAX_FREQUENCY2);
		
		textviewStatus = (TextView) findViewById(R.id.text_notif);
		textviewRead = (TextView) findViewById(R.id.textRead);
		textviewDecode = (TextView) findViewById(R.id.textDecode);
		textviewExtra = (TextView) findViewById(R.id.textExtra);

		editTextAge = (EditText)findViewById(R.id.editAge);
		editTextAge.setSingleLine();
		editTextLocation = (EditText)findViewById(R.id.editLocation);
		editTextLocation.setSingleLine();
		radioGroupGender = (RadioGroup)findViewById(R.id.radioGroup);
		
		Button updateButton = (Button)findViewById(R.id.updateButton);
		
		registerDevice();
		initAudioReader();
		
		updateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				String gender = "남";
				if(radioGroupGender.getCheckedRadioButtonId() == R.id.woman)
					gender = "여";
				String age = editTextAge.getText().toString();
				String location = editTextLocation.getText().toString();
				
				setUserExtra(gender, age, location);
			}
			
		});
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
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
		HttpClient.SendHttpGet send = client.SendGet();
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
					
					JSONObject userExtraInformation = result.getJSONObject("user_info");
					
					String age = userExtraInformation.getString("age");
					String location = userExtraInformation.getString("location");
					String gender = userExtraInformation.getString("gender");

					editTextAge.setText(age);
					editTextLocation.setText(location);
					if(gender.equals("남"))
						radioGroupGender.check(R.id.man);
					else
						radioGroupGender.check(R.id.woman);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void getInformationUser(int user_id) {
		HttpClient.SendHttpGet send = client.SendGet();
		send.execute("user", user_id + "", new HttpClient.HttpListener() {

			@Override
			public void onSendError(int error) {
			}

			@Override
			public void onSendComplete(JSONObject result) {
				try {
					JSONObject userExtraInformation = result.getJSONObject("user_info");
					
					String gender = userExtraInformation.getString("gender");
					String age = userExtraInformation.getString("age");
					String location = userExtraInformation.getString("location");
					
					textviewExtra.setText(gender + "/" + age + "/" + location);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	private void setUserExtra(String gender, String age, String location) {
		HttpClient.SendHttpPost send = client.SendPost();
		JSONObject json = new JSONObject();
		
		try {
			json.put("gender", gender);
			json.put("age", age);
			json.put("location", location);
		}catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		send.execute("set_extra", this.uuid, json.toString(), new HttpClient.HttpListener() {

			@Override
			public void onSendError(int error) {
			}

			@Override
			public void onSendComplete(JSONObject result) {

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

	public boolean hasConcecutiveCharacter(int number, int ignore) {
		if( lastResult.length() < number + ignore) {
			return false;
		}
		
		for (int i = ignore + 1; i <= ignore + number; i ++) {
			if (lastResult.charAt(lastResult.length() - i) != lastResult.charAt(lastResult.length() - ignore - 1)) {
				return false;
			}
		}
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
						lastResult = lastResult.substring(lastResult.length() - 10);
					}

					if (lastResult.length() > 4 &&
						lastResult.charAt(lastResult.length() - 1) == '^' &&
						hasConcecutiveCharacter(4, 0)) {

						lastResult = "";
						if (realResult.length() > 0) {
							if (beforeRealResult.equals(realResult)) {
								getInformationUser(Integer.parseInt(realResult));
							}
							beforeRealResult = realResult;
						}
						realResult = "";
					}
					if (lastResult.length() > 4 &&
						lastResult.charAt(lastResult.length() - 1) == '$' &&
						lastResult.charAt(lastResult.length() - 2) != '$' && 
						lastResult.charAt(lastResult.length() - 2) != '^' &&
						hasConcecutiveCharacter(3, 1)) {
						realResult += lastResult.charAt(lastResult.length() - 2);
					}
				}
				textviewDecode.setText(realResult);
				textviewRead.setText("Read: " + (int)frequency);
				break;
			}
		}
	}
}