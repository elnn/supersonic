package com.younha.supersonic;

import java.util.ArrayList;

import com.younha.supersonic.MainActivity.MainHandler;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class SoundScheduler {

	private static Handler handler;
	private static ArrayList<Sound> soundList;
	private static Thread thread;
	
	public SoundScheduler(Handler handler) {
		
		SoundScheduler.handler = handler;
		SoundScheduler.soundList = new ArrayList<Sound>();
		
		thread = new Thread(new SoundRunnable());
	}
	
	public void addSound(Sound sound) {
		synchronized (soundList) {
			soundList.add(sound);
		}
	}
	
	public void start() {
		thread.start();
	}
	
	public void play(AudioTrack audioTrack, Sound sound) {
		
		int offset = 0;
		
		while (offset < sound.getRawBytes().length) {
			int state = audioTrack.write(sound.getRawBytes(), offset, sound.getRawBytes().length - offset);

			if (state == AudioTrack.ERROR_INVALID_OPERATION) {
				Log.e("playSound()", "ERROR_INVALID_OPERATION " + offset + " " + sound.getRawBytes().length + " " + audioTrack);
				return;
			} else if (state == AudioTrack.ERROR_BAD_VALUE) {
				Log.e("playSound()", "ERROR_BAD_VALUE");
			} else if (state == AudioTrack.ERROR) {
				Log.e("playSound()", "ERROR");
			} else {
				offset += state;
			}
		}
		
		audioTrack.play();
		audioTrack.flush();
	}

	private class SoundRunnable implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				for (Sound sound : soundList) {
					Message emitMessage = new Message();
					emitMessage.what = MainHandler.MODE_TEXTVIEW_UPDATE;
					emitMessage.obj = "Send: " + (int)sound.getFrequency();
					handler.sendMessage(emitMessage);
					
					AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_RING,
							Sound.getSampleRate(), AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT, Sound.getSampleSize(),
							AudioTrack.MODE_STATIC);
					
					play(audioTrack, sound);
					
					try {
						Thread.sleep((long) (Sound.getDurationInSeconds() * 1000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					audioTrack.release();
				}

				Message clearMessage = new Message();
				clearMessage.what = MainHandler.MODE_TEXTVIEW_UPDATE;
				clearMessage.obj = "Clear";
				handler.sendMessage(clearMessage);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
