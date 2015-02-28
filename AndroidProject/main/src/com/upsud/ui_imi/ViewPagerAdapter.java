package com.upsud.ui_imi;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Maxence Bobin on 20/02/15.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int pos) {

        // The position can only be 0, 1 or 2 (there are 3 tabs => 3 fragments)
        if (pos <= 3) {
            return MusicFragment.newInstance(pos);
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
