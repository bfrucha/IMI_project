package com.upsud.ui_imi;

import java.util.ArrayList;

import com.upsud.ui_imi.call.Contact;
import com.upsud.ui_imi.call.ContactsManager;
import com.upsud.ui_imi.dialogs.CallDialog;
import com.upsud.ui_imi.dialogs.ScenarioDialog;
import com.upsud.ui_imi.music.MusicFragment;
import com.upsud.ui_imi.music.Song;
import com.upsud.ui_imi.music.ViewPagerAdapter;
import com.upsud.ui_imi.queries.Queries;
import com.upsud.ui_imi.services.SynthetizeText;
import com.upsud.ui_imi.services.VoiceCommand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.upsud.ui_imi.R;

public class GI_Activity extends ActionBarActivity {

	// to post delayed thread
	Handler handler;
	Runnable fakeCall;
	
	
    private ViewPager mViewPager;
    private ViewPagerAdapter mSectionsPagerAdapter;
    public static PagerSlidingTabStrip tabs;
    
    // text to speech
	public static SynthetizeText tts;
	
	// speech recognizer
	Intent srIntent;

	// total number of modalities contained in Queries.java
	public static boolean[] MODALITIES = new boolean[Queries.MODALITIES_NUMBER];
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpViewPager();
        
		// Text To Speech
		tts = new SynthetizeText(getBaseContext());
		
		// Speech Recognizer
	    Context activityContext = getBaseContext();
	    srIntent = new Intent(activityContext, VoiceCommand.class);
	    //startService(srIntent); MODALITIES[Queries.IM_SPEECH] = true;
	    
	    IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_CALL);
	    LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(new CallBroadcastReceiver(), filter);
	    
    }

    private void setUpViewPager() {
        // Set up the adapter
        mSectionsPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // The ViewPager will keep in memory only 3 pages
        mViewPager.setOffscreenPageLimit(3);

        // Add tabs to the actionBar
        final ActionBar actionBar = getSupportActionBar();

        actionBar.hide();


        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        if (tabs != null) {
            tabs.setViewPager(mViewPager);

        } else {
            Log.d("test", "tabs null");
        }
        
        

        // voice command for playing a song
        // look for the title of the song
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicFragment.PLAY_SONG_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
        	public void onReceive(Context context, Intent intent) {
        		
        		MusicFragment fragment = ((MusicFragment) mSectionsPagerAdapter.getItem(0));
        		fragment.findAndPlay(intent.getStringExtra("name"));
        	}
        }, filter);
    }
    
    
    @Override
	public void onPause() {
		if(tts != null){
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
		// first iteration => input modalities
		// second iteration => output modalities
		String[] titles = { "Input modalities :\n", "Output modalities :\n" };
		int index = 0;
		
		ArrayList<ArrayList<Integer>> res = Queries.adaptToScenario(this, scenarioID);
		
		if(res.size() == 0) { android.util.Log.d("Modalities", "No results"); }
		
		// update modalities to use
		MODALITIES = new boolean[Queries.MODALITIES_NUMBER];
		
		for(ArrayList<Integer> modalities: res) {
			TextView view = new TextView(getBaseContext());
			
			String text = "";
			for(Integer code : modalities) { 
				text += Queries.decodeModality(code) + " ";
				MODALITIES[code] = true;
			}
			
			view.setText(titles[index++]+text);
			
			android.util.Log.d("Modalities", text);
		}
		
		// simulate call
		if(scenarioID == R.raw.scenario_12pm) {
			handler = new Handler();
			fakeCall = new Runnable() {
				
				public void run() {
					// call from random contact
					ContactsManager cm = new ContactsManager(getBaseContext());
		            Contact c = cm.getRandomContact();
	
		            tts.speakText(c.getName() + " is calling you");
		            
		            new CallDialog(c.getName(), c.getPhoneNumber(), c.getPhoto(), false) {
		            	private boolean[] savedModalities = new boolean[Queries.MODALITIES_NUMBER];
            		
		            	public void onCreate(Bundle savedInstanceState) {
		            		super.onCreate(savedInstanceState);

		            		for(int index = 0; index < savedModalities.length; index++) {
		            			savedModalities[index] = MODALITIES[index];
		            		}
		            		
		            		// dynamically change modalities
		            		MODALITIES[Queries.IM_SPEECH] = false;
		            		MODALITIES[Queries.OM_MUSIC] = false;
		            		MODALITIES[Queries.OM_TEXT_TO_SPEECH] = false;
		            		
		            		update();
		            	}
		            	
		            	
		            	@Override
		            	public void onDestroy() {
		            		super.onDestroy();
		            		
				            // reset saved modalities
		            		for(int index = 0; index < savedModalities.length; index++) {
		            			MODALITIES[index] = savedModalities[index];
		            		}
		            		
		            		update();
		            	}
		            	
		            }.show(getSupportFragmentManager(), "call");
				}
				
			};
			handler.postDelayed(fakeCall, 5000);
		} else { 
			if(handler != null) { handler.removeCallbacks(fakeCall); }
		}
		
		
		/* UPDATE ACCORDING TO MODALITIES */
		update();
	}
	
	
	/**
	 * Update all the fragments according to the current modalities
	 */
	private void update() {
		Resources res = getResources();
		
		int grey = res.getColor(R.color.grey);
		int white = res.getColor(R.color.white);
		int black = res.getColor(R.color.black);
		
		// change background color of each musicfragment
		for(int position = 0; position < 3; position++) {
			MusicFragment fragment = ((MusicFragment) mSectionsPagerAdapter.getItem(position));
			
			fragment.updateBackgroundColor();
			fragment.updateButtons();
			fragment.updateVoiceCommand();
			fragment.updateMusic();
		}
		
		if(MODALITIES[Queries.OM_DAY_GRAPHICS]) {
            tabs.setIndicatorColor(black);
            tabs.setUnderlineColor(black);
            tabs.setBackgroundColor(white);
            tabs.setTextColor(black);
        }
        else {
            tabs.setIndicatorColor(white);
            tabs.setUnderlineColor(white);
            tabs.setBackgroundColor(grey);
            tabs.setTextColor(white);
        }
		

		// set speech recognizer on/off
		stopService(srIntent);
		if(MODALITIES[Queries.IM_SPEECH]) { startService(srIntent); }
		else { Log.d("speech", "Not started"); }
	}
	
	
	
	
	
	private class CallBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("broadcast", "intent received");
			String name = intent.getStringExtra("name");
			
			// fake number
			String number = "06 ";
			for(int i = 0; i < 4; i ++) { 
				number += (int)(10 * (9 * Math.random())) + " ";
			}
			
			tts.speakText("Trying to call " + name);
			
			new CallDialog(name, number.trim(), null, true).show(getSupportFragmentManager(), "Calling");
		}
	
	}
}
