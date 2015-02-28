package com.upsud.ui_imi;

import java.util.ArrayList;

import com.upsud.ui_imi.dialogs.ScenarioDialog;
import com.upsud.ui_imi.queries.Queries;
import com.upsud.ui_imi.services.SynthetizeText;
import com.upsud.ui_imi.services.VoiceCommand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.upsud.ui_imi.R;

public class GI_Activity extends ActionBarActivity {

    private ViewPager mViewPager;
    
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
	    //startService(srIntent);
	    
	    /*IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_CALL);
	    LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(new CallBroadcastReceiver(), filter);*/
    }

    private void setUpViewPager() {
        // Set up the adapter
        ViewPagerAdapter mSectionsPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // The ViewPager will keep in memory only 3 pages
        mViewPager.setOffscreenPageLimit(4);

        // Add tabs to the actionBar
        final ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //Set up the left icon to go back to Establishment page
        //actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.hide();
        // Add a tabListener to navigate through an other page
       /*ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab,
                                      FragmentTransaction fragmentTransaction) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab,
                                        FragmentTransaction fragmentTransaction) {
                // nothing to do here
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab,
                                        FragmentTransaction fragmentTransaction) {
                // nothing to do here
            }
        };*/


        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        if (tabs != null) {
            tabs.setViewPager(mViewPager);

        } else {
            Log.d("test", "tabs null");
        }
        
        /*
        // Add titles and tabListeners to the three actionBar tabs
        actionBar.addTab(actionBar.newTab().setText("Soft").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Hard").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Tapas/Snack").setTabListener(tabListener));

        // Add a setOnPageChangerListener to indicate where we are inside the container
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });*/
        //  tabs.setTextColor(getResources().getColor(R.color.blue));
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
		
		stopService(srIntent);
		if(MODALITIES[Queries.IM_SPEECH]) { startService(srIntent); }
		else { Log.d("speech", "Not started"); }
		
		// simulate call
		if(scenarioID == R.raw.scenario_12am) {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				
				public void run() {
					ContactsManager cm = new ContactsManager(getBaseContext());
		            Contact c = cm.getRandomContact();
	
		            new CallDialog(c.getName(), c.getPhoneNumber(), c.getPhoto(), false).show(getSupportFragmentManager(), "call");
				}
				
			}, 10000);
		}
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
			
			new CallDialog(name, number.trim(), null, true).show(getSupportFragmentManager(), "Calling");
		}
	
	}
}
