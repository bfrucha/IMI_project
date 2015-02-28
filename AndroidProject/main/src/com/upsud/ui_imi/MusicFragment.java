package com.upsud.ui_imi;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;


import com.android.internal.telephony.ITelephony;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.mobsandgeeks.adapters.CircularListAdapter;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment {

    private static final String TAG = "com.upsud.ui_imi.MusicFragment";

    private CallReceiver cr;

    private SongList songList;
    private TextView totalDurationTextView;
    private TextView currentDurationTextView;
    private SeekBar seekbar;

    private Handler mHandler = new Handler();

    private Utilities utils;

    //state 0 if play wasn't call and 1 if already called once
    private int state = 0;

    private CircularImageView cover;

    private TextView name;
    private RelativeLayout callLayout;

    private ImageButton playButton;

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

        // Get the menuExpandablelistView from the fragment XML file
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.music_fragment, container, false);

        //Create the receiver
        cr = new CallReceiver();

        //Get views
        playButton = (ImageButton) view.findViewById(R.id.play);
        final ImageButton pauseButton = (ImageButton) view.findViewById(R.id.pause);
        ImageButton nextButton = (ImageButton) view.findViewById(R.id.next);
        ImageButton previousButton = (ImageButton) view.findViewById(R.id.previous);
        listview = (ListView) view.findViewById(R.id.listview);
        currentDurationTextView = (TextView) view.findViewById(R.id.currentDuration);
        totalDurationTextView = (TextView) view.findViewById(R.id.totalDuration);
        seekbar = (SeekBar) view.findViewById(R.id.seekbar);

        ImageButton call = (ImageButton) view.findViewById(R.id.dialog);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ContactsManager cm = new ContactsManager(getActivity());
                final Contact c = cm.getRandomContact();

                new CallDialog(c.getName(), c.getPhoneNumber(), c.getPhoto(), false).show(getFragmentManager(), "call");
            }
        });

        //Get fields for the call layout
        //name = (TextView) view.findViewById(R.id.nameCall);
        //callLayout = (RelativeLayout) view.findViewById(R.id.callLayout);
        //ImageButton call = (ImageButton) view.findViewById(R.id.callButton);
        //ImageButton endCall = (ImageButton) view.findViewById(R.id.endCallButton);

        cover = (CircularImageView) view.getChildAt(1);

        //Create media player
        mp = new MediaPlayer();

        //Create utils
        utils = new Utilities();

        //Retrieve songs
        sm = new SongManager(getActivity());
        songList = sm.getSongList();

        switch (fragmentNumber) {
            case 0:
                songList.sortByTitles();
                break;
            case 1:
                songList.sortByArtists();
                break;
            case 2:
                songList.sortByAlbums();
                break;
            default:
                songList.sortByTitles();
        }

        //Create the adapter to the listview
        CircularListAdapter circularAdapter = new CircularListAdapter(new ListViewAdapter(inflater, songList));
        listview.setAdapter(circularAdapter);

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

                if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    Log.i("a", "scrolling stopped...");
                    changeSong();
                }

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            }
        });

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

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        });

        //Set listener for the call Layout
       /* call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Call ok", Toast.LENGTH_LONG).show();

                // Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
                TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                Class c = null;
                Method m = null;
                try {
                    c = Class.forName(tm.getClass().getName());
                    try {
                        m = c.getDeclaredMethod("getITelephony");

                        m.setAccessible(true);

                        ITelephony telephonyService;
                        try {
                            telephonyService = (ITelephony) m.invoke(tm);
                            // Silence the ringer and answer the call!
                            telephonyService.silenceRinger();
                            telephonyService.answerRingingCall();
                            //TODO : change the buttons (remove call)

                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }



            }
        });

        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "End call", Toast.LENGTH_LONG).show();

                //Hide the callLayout
                callLayout.setVisibility(View.GONE);
            }
        });*/

        return view;
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

            if (state == 0) {
                state = 1;
            }
            Song s = songList.get(position);

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
            //mp.start();
            playButton.performClick();

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
     * The Broadcast Receiver who tell when we have a call.
     */
    public class CallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //Get name from the intent
            String nameCall = intent.getStringExtra("name");

            //Set the name on the textview
            name.setText(nameCall);

            //Set visible the layout
            callLayout.setVisibility(View.VISIBLE);
        }
    }
}

