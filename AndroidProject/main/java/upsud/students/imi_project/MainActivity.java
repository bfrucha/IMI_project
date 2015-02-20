package upsud.students.imi_project;

import java.util.ArrayList;
import java.util.Locale;

import upsud.students.imi_project.dialogs.ScenarioDialog;
import upsud.students.imi_project.queries.Queries;
import upsud.students.imi_project.service.VoiceCommand;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	TextToSpeech tts;
	
	Intent srIntent;
	VoiceCommand voiceCommand;
	
	// total number of modalities contained in Queries.java
	public static boolean[] MODALITIES = new boolean[Queries.MODALITIES_NUMBER];
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tts = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
			
			@Override
			public void onInit(int status) {
				if(status == TextToSpeech.ERROR) { Log.e("TTS", "Error on initialization"); }
				else { 
					tts.setLanguage(Locale.UK);
				}
			}
		});
		
	    Context activityContext = getBaseContext();
	    srIntent = new Intent(activityContext, VoiceCommand.class);
	    activityContext.startService(srIntent);
	}


	@Override
	public void onPause() {
		if(tts !=null){
	         tts.stop();
	         tts.shutdown();
	      }
		
	    super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopService(srIntent);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.load_scenario) {
			new ScenarioDialog(this).show(getSupportFragmentManager(), "Scenarios");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public void changeScenario(int scenarioID) {
		LinearLayout mainView = (LinearLayout) findViewById(R.id.activity_main);
		mainView.removeAllViews();
		
		// first iteration => input modalities
		// second iteration => output modalities
		String[] titles = { "Input modalities :\n", "Output modalities :\n" };
		int index = 0;
		
		ArrayList<ArrayList<Integer>> res = Queries.adaptToScenario(this, scenarioID);
		
		if(res.size() == 0) { android.util.Log.d("Modalities", "No results"); }
		for(ArrayList<Integer> modalities: res) {
			TextView view = new TextView(getBaseContext());
			
			String text = "";
			// update modalities to use
			MODALITIES = new boolean[Queries.MODALITIES_NUMBER];
			for(Integer code : modalities) { 
				text += Queries.decodeModality(code) + " ";
				MODALITIES[code] = true;
			}
			
			view.setText(titles[index++]+text);
			
			android.util.Log.d("Modalities", text);
			mainView.addView(view);
		}
		
		setContentView(mainView);
	} 
	
	
   public void speakText(String text){
	   if(MODALITIES[Queries.OM_TEXT_TO_SPEECH]) {
	      tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	   } else {
		   Toast.makeText(getBaseContext(), "Text to speech is disabled", Toast.LENGTH_SHORT).show();
	   }
   }
}
