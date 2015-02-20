package upsud.students.imi_project.service;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class VoiceCommand extends Service {
 
	AudioManager audioManager;
	SpeechRecognizer speechRecognizer;
	Intent speechRecognizerIntent;
	
	HotwordRecognitionListener hotwordListener;
	CommandRecognitionListener commandListener;
	
	private boolean streamMuted = true;
	
	private boolean DEBUG = true;
	private String TAG = "speech";
	
	
	public final ServiceConnection mServiceConnection = new ServiceConnection() {
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service) {
	        // if (DEBUG) {Log.d(TAG, "onServiceConnected");} //$NON-NLS-1$
	    	Log.d("speech", "Service connected");
	    }

	    @Override
	    public void onServiceDisconnected(ComponentName name) {
	        // if (DEBUG) {Log.d(TAG, "onServiceDisconnected");} //$NON-NLS-1$
	    	Log.d("speech", "Service disconnected");
	    }

	};
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		hotwordListener = new HotwordRecognitionListener();
		commandListener = new CommandRecognitionListener();
		
	    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
	    speechRecognizer.setRecognitionListener(new HotwordRecognitionListener());
	    
	    speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    speechRecognizerIntent.putExtra(	RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	                                     	RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    speechRecognizerIntent.putExtra(	RecognizerIntent.EXTRA_CALLING_PACKAGE,
	                                     	this.getPackageName());
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
		speechRecognizer.startListening(speechRecognizerIntent);
		
		return START_STICKY;
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
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: Log.e(TAG, "Recognizer busy error"); break;
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
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: Log.e(TAG, "Recognizer busy error (command)"); break;
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
			}
			
			reset();
		}
	}
}
