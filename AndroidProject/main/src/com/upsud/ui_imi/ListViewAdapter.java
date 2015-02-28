package com.upsud.ui_imi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.upsud.ui_imi.R;
/**
 * Created by Maxence Bobin on 20/02/15.
 */
public class ListViewAdapter extends BaseAdapter {

    private SongList songList = new SongList();
    //Un mécanisme pour gérer l'affichage graphique depuis un layout XML
    private LayoutInflater mInflater;

    public ListViewAdapter(LayoutInflater mInflater, SongList songList) {
        super();
        this.mInflater = mInflater;
        this.songList = songList;
    }

    @Override
    public int getCount() {
        return songList.getSize();
    }

    @Override
    public Object getItem(int i) {
        return songList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.listview_item, null);

        ImageView cover = (ImageView) v.findViewById(R.id.cover);
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView artist = (TextView) v.findViewById(R.id.artist);
        TextView album = (TextView) v.findViewById(R.id.album);

        Song s = songList.get(i);

        cover.setImageBitmap(s.getCover());
        title.setText(s.getTitle());
        artist.setText(s.getArtist());
        album.setText(s.getAlbum());

        return v;
    }
}
