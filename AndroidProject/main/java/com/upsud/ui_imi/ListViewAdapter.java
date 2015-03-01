package com.upsud.ui_imi;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.upsud.ui_imi.R;
import com.upsud.ui_imi.music.MusicFragment;
import com.upsud.ui_imi.music.Song;
import com.upsud.ui_imi.music.SongList;
import com.upsud.ui_imi.queries.Queries;
/**
 * Created by Maxence Bobin on 20/02/15.
 */
public class ListViewAdapter extends BaseAdapter {

    private SongList songList = new SongList();
    //Un mécanisme pour gérer l'affichage graphique depuis un layout XML
    private LayoutInflater mInflater;

    private MusicFragment fragment;
    
    public ListViewAdapter(LayoutInflater mInflater, SongList songList, MusicFragment fragment) {
        super();
        this.mInflater = mInflater;
        this.songList = songList;
        this.fragment = fragment;
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.listview_item, null);

        ImageView cover = (ImageView) v.findViewById(R.id.cover);
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView artist = (TextView) v.findViewById(R.id.artist);
        TextView album = (TextView) v.findViewById(R.id.album);

        final Song s = songList.get(i);

        cover.setImageBitmap(s.getCover());
        title.setText(s.getTitle());
        artist.setText(s.getArtist());
        album.setText(s.getAlbum());
        
        v.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(GI_Activity.MODALITIES[Queries.IM_BUTTON]) { fragment.play(i); }
			}
		});

        return v;
    }
}
