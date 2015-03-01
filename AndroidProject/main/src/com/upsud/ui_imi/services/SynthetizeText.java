package com.upsud.ui_imi.services;

import java.util.Locale;

import com.upsud.ui_imi.GI_Activity;
import com.upsud.ui_imi.queries.Queries;

import android.annotation.SuppressLint;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

public class SynthetizeText extends TextToSpeech {

	Context context;
	
	public SynthetizeText(Context context) {
		super(context, new TextToSpeech.OnInitListener() {
			
			@Override
			public void onInit(int status) {
				if(status == TextToSpeech.ERROR) { Log.e("TTS", "Error on initialization"); }
			}
		});
		
		this.context = context;
	}
	
	

	public void speakText(String text){
		if(GI_Activity.MODALITIES[Queries.OM_TEXT_TO_SPEECH]) {
			
			setLanguage(Locale.UK);
			
			speak(text, TextToSpeech.QUEUE_ADD, null);
		} else {
			//Toast.makeText(context, "Text to speech is disabled", Toast.LENGTH_SHORT).show();
		}
	}
}
