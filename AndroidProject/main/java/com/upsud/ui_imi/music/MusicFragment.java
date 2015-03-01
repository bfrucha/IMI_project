package com.upsud.ui_imi.music;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.mobsandgeeks.adapters.CircularListAdapter;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.upsud.ui_imi.GI_Activity;
import com.upsud.ui_imi.ListViewAdapter;
import com.upsud.ui_imi.R;
import com.upsud.ui_imi.Utilities;
import com.upsud.ui_imi.R.color;
import com.upsud.ui_imi.R.drawable;
import com.upsud.ui_imi.R.id;
import com.upsud.ui_imi.R.layout;
import com.upsud.ui_imi.call.Contact;
import com.upsud.ui_imi.call.ContactsManager;
import com.upsud.ui_imi.dialogs.CallDialog;
import com.upsud.ui_imi.queries.Queries;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment {

    private static final String TAG = "com.upsud.ui_imi.MusicFragment";

    public static final String VOICE_COMMAND_ACTION = "SPEECH_RECOGNIZER";
    public static final String PLAY_SONG_ACTION = "PLAY_SONG";
    
   // private CallReceiver cr;

    public static boolean nightModeOn = false;
    private ImageButton nightModeButton;

    private RelativeLayout view;

    private SongList songList;
    private TextView totalDurationTextView;
    private TextView currentDurationTextView;
    private SeekBar seekbar;

    private Handler mHandler = new Handler();

    private Drawable nighticon;
    private Drawable dayicon;
    private int white;
    private int grey;
    private int black;

    private Utilities utils;

    private Resources res;
    //state 0 if play wasn't call and 1 if already called once
    private int state = 0;

    private CircularImageView cover;
    /*private TextView name;
    private RelativeLayout callLayout;*/

    private ImageButton playButton;
    private ImageButton pauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private OnSeekBarChangeListener seekbarListener;
    private ImageView voiceCommandFeedback;
    
    private SongManager sm;
    private MediaPlayer mp;
    private int currentSongIndex = -1;
    private ListView listview;
    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            totalDurationTextView.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            currentDurationTextView.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = utils.getProgressPercentage(currentDuration, totalDuration);
            //Log.d("Progress", ""+progress);
            seekbar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    // Will be used every time we create a MenuFragment
    public static MusicFragment newInstance(int fragmentNumber) {
        MusicFragment fragment = new MusicFragment();

        // Use a bundle to transmit the fragment number
        // which will be displayed by the onCreateView method
        Bundle args = new Bundle();
        args.putInt("KEY_INT", fragmentNumber);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Get the bundle to recover the fragment number
        Bundle args = getArguments();
        int fragmentNumber = args.getInt("KEY_INT");

        res = getActivity().getResources();

        // Get the menuExpandablelistView from the fragment XML file
        view = (RelativeLayout) inflater.inflate(R.layout.music_fragment, container, false);

        //Create the receiver
        //cr = new CallReceiver();

        //Get views
        playButton = (ImageButton) view.findViewById(R.id.play);
        pauseButton = (ImageButton) view.findViewById(R.id.pause);
        nextButton = (ImageButton) view.findViewById(R.id.next);
        previousButton = (ImageButton) view.findViewById(R.id.previous);
        listview = (ListView) view.findViewById(R.id.listview);
        currentDurationTextView = (TextView) view.findViewById(R.id.currentDuration);
        totalDurationTextView = (TextView) view.findViewById(R.id.totalDuration);
        seekbar = (SeekBar) view.findViewById(R.id.seekbar);

        // nighticon = res.getDrawable(R.drawable.night);
        // dayicon = res.getDrawable(R.drawable.day);
        white = res.getColor(R.color.white);
        grey = res.getColor(R.color.grey);
        black = res.getColor(R.color.black);

        if(GI_Activity.MODALITIES[Queries.OM_NIGHT_GRAPHICS]) { view.setBackgroundColor(grey); }
        else 			{ view.setBackgroundColor(white); }
        
        //Retrieve songs
        sm = new SongManager(getActivity());
        songList = sm.getSongList();

        switch (fragmentNumber) {
            case 0:
                songList.sortByTitles();
                break;
            case 1:
                songList.sortByAlbums();
                break;
            case 2:
                songList.sortByArtists();
                break;
        }

        //Create the adapter to the listview
        final CircularListAdapter circularAdapter = new CircularListAdapter(new ListViewAdapter(inflater, songList, this));
        listview.setAdapter(circularAdapter);

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

                if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    Log.i("a", "scrolling stopped...");
                    
                    if(GI_Activity.MODALITIES[Queries.IM_AIR_GESTURE]) { changeSong(); }
                }

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            }
        });

        // visual feedback to say whether speech recognizer is on
        voiceCommandFeedback = (ImageView) view.findViewById(R.id.voice_command);
        if(GI_Activity.MODALITIES[Queries.IM_SPEECH]) {
        	voiceCommandFeedback.setImageDrawable(res.getDrawable(R.drawable.voice_icon));
        }
        
        // listen for events from VoiceCommand to update feedback
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOICE_COMMAND_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String status = intent.getStringExtra("status");
				
				if(status.equals("stopped")) { voiceCommandFeedback.setImageDrawable(res.getDrawable(R.drawable.no_voice_icon)); }
				else						 { voiceCommandFeedback.setImageDrawable(res.getDrawable(R.drawable.voice_icon)); }
			}
		}, filter);;
        
        
        cover = (CircularImageView) view.getChildAt(1);

        //Create media player
        mp = new MediaPlayer();

        //Create utils
        utils = new Utilities();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If play not called yes
                if (state == 0) {
                    play(0);
                    playButton.setVisibility(View.GONE);
                    pauseButton.setVisibility(View.VISIBLE);
                }
                //If play already called
                else {
                    mp.start();
                    playButton.setVisibility(View.GONE);
                    pauseButton.setVisibility(View.VISIBLE);
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.pause();
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "NEXT", Toast.LENGTH_LONG).show();
                // check if next song is there or not
                if (currentSongIndex < (songList.getSize() - 1)) {
                    play(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                } else {
                    // play first song
                    play(0);
                    currentSongIndex = 0;
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "PREVIOUS", Toast.LENGTH_LONG).show();

                if (currentSongIndex > 0) {
                    play(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                } else {
                    // play last song
                    play(songList.getSize() - 1);
                    currentSongIndex = songList.getSize() - 1;
                }
            }
        });

        seekbarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // remove message Handler from updating progress bar
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = mp.getDuration();
                int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

                // forward or backward to certain seconds
                mp.seekTo(currentPosition);

                // update timer progress again
                updateProgressBar();
            }
        }; 
        seekbar.setOnSeekBarChangeListener(seekbarListener);
        
        
        return view;
    }

    
    /**
     * Switch to day/night mode
     */
    public void updateBackgroundColor() {
    	
    	if(view != null) {
    		if(GI_Activity.MODALITIES[Queries.OM_NIGHT_GRAPHICS]) { view.setBackgroundColor(grey); }
    		else 												  { view.setBackgroundColor(white); }
    		
    		view.invalidate();
    	}
    }
    
    /**
     * Show/hide buttons according to current modalities
     */
    public void updateButtons() {
    	boolean visible = GI_Activity.MODALITIES[Queries.IM_BUTTON];
    	
    	// default => all gone
    	playButton.setVisibility(View.GONE);
    	pauseButton.setVisibility(View.GONE);
    	previousButton.setVisibility(View.GONE);
    	nextButton.setVisibility(View.GONE);
    	seekbar.setOnSeekBarChangeListener(null);
    	
    	// show buttons
    	if(visible) {
    		if(state == 0) { playButton.setVisibility(View.VISIBLE); }
    		else		   { pauseButton.setVisibility(View.VISIBLE); }
    		nextButton.setVisibility(View.VISIBLE);
    		previousButton.setVisibility(View.VISIBLE);
    		
    		seekbar.setOnSeekBarChangeListener(seekbarListener);
    	}
    }
    
    /**
     * Update the voice command feedback
     */
    public void updateVoiceCommand() {
    	if(voiceCommandFeedback != null) {
    		if(GI_Activity.MODALITIES[Queries.IM_SPEECH]) { voiceCommandFeedback.setImageDrawable(getResources().getDrawable(R.drawable.voice_icon)); }
    		else										  { voiceCommandFeedback.setImageDrawable(getResources().getDrawable(R.drawable.no_voice_icon)); }
    	}
    }
    
   /**
    * mute/unmute the mediaplayer if music can't be play
    */
    public void updateMusic() {
    	if(GI_Activity.MODALITIES[Queries.OM_MUSIC]) { mp.setVolume(1, 1); }
    	else 										 { mp.setVolume(0, 0); }
    }
    
    
    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Play the first song on the list
     */
    public void play(int position) {

        if (position < songList.getSize() && position >= 0) {

        	if(GI_Activity.MODALITIES[Queries.IM_BUTTON]) { 
        		playButton.setVisibility(View.GONE);
        		pauseButton.setVisibility(View.VISIBLE);
        	}
        	
            if (state == 0) {
                state = 1;
            }
            Song s = songList.get(position);

            s.showCover(cover);
            
            currentSongIndex = position;

            mp.reset();

            try {
                // Set data source -
                mp.setDataSource(s.getPath());
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Play audio
            mp.start();
           // playButton.performClick();

            // set Progress bar values
            seekbar.setProgress(0);
            seekbar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } else {
            Log.d("test", "error position");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //Unregister the receiver.
        //getActivity().unregisterReceiver(cr);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Register the receiver on the system to listen the broadcast event com.upsud.ui_imi.CALL_ENTER
        //getActivity().registerReceiver(cr, new IntentFilter("com.upsud.ui_imi.CALL_ENTER"));

    }

    public void changeSong () {
        int first = listview.getFirstVisiblePosition();
        int last = listview.getLastVisiblePosition();

        int middle = last - first / 2;

        listview.setSelection(middle);

        play(middle);
    }
    

    /**
     * play a song if a voice command containing play has been spoken
     */
    public void findAndPlay(String songTitle) {
    	// look for the title of the song
    	int index = 0;
		while(index < songList.getSize() && !songList.get(index).getTitle().toLowerCase(Locale.getDefault()).contains(songTitle)) {
			Log.d("songs", songList.get(index).getTitle().toLowerCase(Locale.getDefault()) +" "+songTitle);
			index++; 
		}
		
		if(index < songList.getSize()) { 
			Song s = songList.get(index);
			
			GI_Activity.tts.speakText("Playing " + s.getTitle() + " by " + s.getArtist());
			play(index);
		}
    }
}

