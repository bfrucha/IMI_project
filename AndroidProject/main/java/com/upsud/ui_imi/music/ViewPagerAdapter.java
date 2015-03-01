package com.upsud.ui_imi.music;


import com.upsud.ui_imi.GI_Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Maxence Bobin on 20/02/15.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
	
	private MusicFragment[] fragments; 
	
    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        
        fragments = new MusicFragment[3];
        for(int index = 0; index < 3; index++) {
        	fragments[index] = MusicFragment.newInstance(index);
        }
    }

    @Override
    public Fragment getItem(int pos) {
    	// The position can only be 0, 1 or 2 (there are 3 tabs => 3 fragments)
        if (pos <= 3) {
        	return fragments[pos];
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "PLAYLIST";
            case 1:
                return "ALBUM";
            case 2:
                return "ARTIST";
            case 3:
                return "TITLE";
            default:
                return "Erreur";
        }
    }

    @Override
    public int getCount() {
        // Three tabs are used to show the menu
        return 3;
    }
}
