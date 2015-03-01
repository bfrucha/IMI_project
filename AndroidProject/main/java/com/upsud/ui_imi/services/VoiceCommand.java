package com.upsud.ui_imi.services;

import java.util.ArrayList;
import java.util.Locale;

import com.upsud.ui_imi.GI_Activity;
import com.upsud.ui_imi.music.MusicFragment;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class VoiceCommand extends Service {
 
	AudioManager audioManager;
	SpeechRecognizer speechRecognizer;
	Intent speechRecognizerIntent;
	
	HotwordRecognitionListener hotwordListener;
	CommandRecognitionListener commandListener;
	
	LocalBroadcastManager broadcastManager;
	
	private boolean streamMuted = false;
	
	private boolean DEBUG = true;
	private String TAG = "speech";
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		hotwordListener = new HotwordRecognitionListener();
		commandListener = new CommandRecognitionListener();
		
		broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		/* Intent i = new Intent();
		i.setAction(Intent.ACTION_CALL);
		i.putExtra("name", "Bruno");
		broadcastManager.sendBroadcast(i); */

		
	    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
	    speechRecognizer.setRecognitionListener(new HotwordRecognitionListener());
	    
	    speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    speechRecognizerIntent.putExtra(	RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	                                     	"en-UK");
	    speechRecognizerIntent.putExtra(	RecognizerIntent.EXTRA_CALLING_PACKAGE,
	                                     	this.getPackageName());
		
	    
	    Log.d("speech", "Listening");

		if(!streamMuted) { 
			streamMuted = true;
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, streamMuted);
			Log.d("speech", "muted");
		}
		speechRecognizer.startListening(speechRecognizerIntent);
		
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if(streamMuted) { 
			streamMuted = false;
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, streamMuted);
			
			Log.d("speech", "unmuted");
		}
		
		speechRecognizer.stopListening();
		speechRecognizer.destroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	

	
	// reset speech recognizer and re-launch it 
	private void reset() {
		if(!streamMuted) {
			streamMuted = true;
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, streamMuted);
			
			Log.d("speech", "muted");
		}
		
		speechRecognizer.cancel();
		
		speechRecognizer.setRecognitionListener(hotwordListener);
		speechRecognizer.startListening(speechRecognizerIntent);
	}

	
	// change speech recognizer's listener
	private void listenToCommands() {
		if(streamMuted) {
			streamMuted = false;
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, streamMuted);
			
			Log.d("speech", "unmuted");
		}

		speechRecognizer.cancel();
		
		speechRecognizer.setRecognitionListener(commandListener);
		speechRecognizer.startListening(speechRecognizerIntent);
	}
	
	
	private class HotwordRecognitionListener implements RecognitionListener {
		
		@Override
		public void onReadyForSpeech(Bundle params) {
			// TODO Auto-generated method stub
			if(DEBUG) { Log.d(TAG, "Ready for speech"); }
			
			if(streamMuted) { 
				streamMuted = false;
				audioManager.setStreamMute(AudioManager.STREAM_MUSIC, streamMuted);
			}
		}

		@Override
		public void onBeginningOfSpeech() {
			// TODO Auto-generated method stub
			if(DEBUG) { Log.d(TAG, "Beginning of speech"); }
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			// TODO Auto-generated method stub
			// if(DEBUG) { Log.d(TAG, "RMS changed"); }
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			// TODO Auto-generated method stub
			if(DEBUG) { Log.d(TAG, "Buffer received"); }
		}

		@Override
		public void onEndOfSpeech() {
			// reset();
		}

		@Override
		public void onError(int error) {
			switch(error) {
			case SpeechRecognizer.ERROR_AUDIO: Log.e(TAG, "Audio error"); break;
			case SpeechRecognizer.ERROR_CLIENT: Log.e(TAG, "Client error"); break;
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: Log.e(TAG, "Insufficient permissions error"); break;
			case SpeechRecognizer.ERROR_NETWORK: Log.e(TAG, "Network error"); break;
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: Log.e(TAG, "Network timeout error"); break;
			case SpeechRecognizer.ERROR_NO_MATCH: Log.e(TAG, "No match error"); break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: 
				Log.e(TAG, "Recognizer busy error"); 
				speechRecognizer.stopListening(); 
				sendWarning();
				break;
			case SpeechRecognizer.ERROR_SERVER: Log.e(TAG, "Server error"); break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: if(DEBUG) { Log.e(TAG, "Speech timeout error"); } break;
			default: Log.e(TAG, "Unknown error");
			}
			
			reset();
		}

		@Override
		public void onResults(Bundle results) {
			analyzeResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			analyzeResults(partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)); 
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			// TODO Auto-generated method stub
			if(DEBUG) { Log.d(TAG, "Event"); }
		}
		
		
		private void analyzeResults(ArrayList<String> results) {
			
			try {
				int index = 0;
				while(true) {
					String res = results.get(index).toLowerCase(Locale.UK);
					
					Log.d("speech", "Results : " + res);
					
					if(res.contains("ok") && res.contains("google")) {
						listenToCommands();
						return;
					} else { index++; }
				}
			} catch (IndexOutOfBoundsException exn) { reset(); }	
		}
	}
	
	
	private class CommandRecognitionListener implements RecognitionListener {

		@Override
		public void onReadyForSpeech(Bundle params) {
			// TODO Auto-generated method stub
			if(DEBUG) { Log.d(TAG, "Ready for speech (command)"); }
		}

		@Override
		public void onBeginningOfSpeech() {
			// TODO Auto-generated method stub
			if(DEBUG) { Log.d(TAG, "Beginning of speech (command)"); }
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			// TODO Auto-generated method stub
			// if(DEBUG) { Log.d(TAG, "RMS changed"); }
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			// TODO Auto-generated method stub
			if(DEBUG) { Log.d(TAG, "Buffer received (command)"); }
		}

		@Override
		public void onEndOfSpeech() {
			// reset();
		}
		
		@Override
		public void onError(int error) {
			switch(error) {
			case SpeechRecognizer.ERROR_AUDIO: Log.e(TAG, "Audio error (command)"); break;
			case SpeechRecognizer.ERROR_CLIENT: Log.e(TAG, "Client error (command)"); break;
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: Log.e(TAG, "Insufficient permissions error (command)"); break;
			case SpeechRecognizer.ERROR_NETWORK: Log.e(TAG, "Network error (command)"); break;
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: Log.e(TAG, "Network timeout error (command)"); break;
			case SpeechRecognizer.ERROR_NO_MATCH: Log.e(TAG, "No match error (command)"); break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: 
				Log.e(TAG, "Recognizer busy error (command)"); 
				speechRecognizer.stopListening(); 
				sendWarning(); 
				break;
			case SpeechRecognizer.ERROR_SERVER: Log.e(TAG, "Server error (command)"); break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: if(DEBUG) { Log.e(TAG, "Speech timeout error (command)"); } break;
			default: Log.e(TAG, "Unknown error (command)");
			}
			
			reset();
		}

		@Override
		public void onResults(Bundle results) {
			analyzeResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			analyzeResults(partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			// TODO Auto-generated method stub
			
		}
		
		
		private void analyzeResults(ArrayList<String> results) {
			for(String res : results) {
				Log.d(TAG, "Results (command) : " + res);
				
				if(res.toLowerCase(Locale.getDefault()).contains("call")) {
					String contactName = res.substring(res.indexOf("call") + 4).trim();
					
					if(!contactName.equals("")) {
						// send "call" message
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_CALL);
						intent.putExtra("name", contactName);
						broadcastManager.sendBroadcast(intent);
						
						stopSelf();
						
						sendWarning();
						
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							public void run() {
								reset();
								sendRunning();
							}
						}, 2000);
						
						return;
					}
				} else if(res.toLowerCase(Locale.getDefault()).contains("play")) { 
					String songName = res.substring(res.indexOf("play") + 4).trim();
					
					if(!songName.equals("")) {
						// send "call" message
						Intent intent = new Intent();
						intent.setAction(MusicFragment.PLAY_SONG_ACTION);
						intent.putExtra("name", songName.toLowerCase(Locale.getDefault()));
						broadcastManager.sendBroadcast(intent);
						
						stopSelf();
						
						sendWarning();
						
						/*Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							public void run() {
								reset();
							}
						}, 2000);*/
						
						return;
					}
				}
			}
			
			reset();
		}
	}
	
	/**
	 * Send a warning when the speech recognizer stopped
	 */
	public void sendWarning() {
		Intent i = new Intent();
		i.setAction(MusicFragment.VOICE_COMMAND_ACTION);
		i.putExtra("status", "stopped");
		broadcastManager.sendBroadcast(i);
	}
	
	public void sendRunning() {
		Intent i = new Intent();
		i.setAction(MusicFragment.VOICE_COMMAND_ACTION);
		i.putExtra("status", "running");
		broadcastManager.sendBroadcast(i);
	}
}
