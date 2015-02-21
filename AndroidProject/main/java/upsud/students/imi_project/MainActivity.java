package upsud.students.imi_project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import upsud.students.imi_project.dialogs.ScenarioDialog;
import upsud.students.imi_project.music.Song;
import upsud.students.imi_project.music.SongList;
import upsud.students.imi_project.queries.Queries;
import upsud.students.imi_project.services.VoiceCommand;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	TextToSpeech tts;
	
	Intent srIntent;
	VoiceCommand voiceCommand;
	
	SongList songList;
	
	// total number of modalities contained in Queries.java
	public static boolean[] MODALITIES = new boolean[Queries.MODALITIES_NUMBER];
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Text To Speech
		tts = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
			
			@Override
			public void onInit(int status) {
				if(status == TextToSpeech.ERROR) { Log.e("TTS", "Error on initialization"); }
				else { 
					tts.setLanguage(Locale.UK);
				}
			}
		});
		
		// Speech Recognizer
	    Context activityContext = getBaseContext();
	    srIntent = new Intent(activityContext, VoiceCommand.class);
	    activityContext.startService(srIntent);
	    
	    // Song list
	    songList = new SongList();
	    getSongList();
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
   
   public void getSongList() {
	   ContentResolver musicResolver = getContentResolver();
	   Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	   Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		   
	   while(musicCursor.moveToNext()) {
		   
		   // get song attributes
		   String title = musicCursor.getString(musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE));
		   
		   long id = musicCursor.getLong(musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID));
		   
		   String artist = musicCursor.getString(musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST));
		   
		   String album = musicCursor.getString(musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM));
		   
		   long albumId = musicCursor.getLong(musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID)); 
		   
		   // find and attach cover to the song
		   Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
           Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

           Bitmap cover = null;
           try {
        	   cover = BitmapFactory.decodeStream(musicResolver.openInputStream(albumArtUri));
        	   
        	   if(cover == null) {
        		   cover = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_cover);
        	   }
        	   
        	   // cover = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), albumArtUri);
           } catch (FileNotFoundException exception) {
               exception.printStackTrace();
               cover = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_cover);
           } catch (IOException e) {
        	   e.printStackTrace();
           }

		   Song song = new Song(id, title, artist, album, cover);
		   songList.addSong(song);
	   }

	   // DEBUG
	   // for(Song s : songList) { Log.d("songs", s.getTitle() + " " + s.getAlbum()); }
	   // for(Map.Entry<String, ArrayList<Song>> entry : songList.sortByAlbum().entrySet()) {
	   //   Log.d("song", "Album : " + entry.getKey());
		   
	   //   for(Song song : entry.getValue()) { Log.d("song", song.getTitle()); }
	   //}
   }
}
