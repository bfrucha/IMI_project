package com.upsud.ui_imi.music;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.upsud.ui_imi.R;
import com.upsud.ui_imi.R.drawable;
/**
 * Created by Maxence Bobin on 23/02/15.
 */
public class SongManager {

    private Context context;

    public SongManager(Context context) {
        this.context = context;
    }

    public SongList getSongList() {
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
        };

        Cursor musicCursor = musicResolver.query(musicUri, projection, selection, null, null);

        SongList songList = new SongList();
        while (musicCursor.moveToNext()) {
            long id = Long.parseLong(musicCursor.getString(0));
            String artist = musicCursor.getString(1);
            String title = musicCursor.getString(2);
            String album = musicCursor.getString(3);
            long albumId = Long.parseLong(musicCursor.getString(4));
            String data = musicCursor.getString(5);

		   // find and attach cover to the song
           Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
           Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

           Bitmap cover = null;
           try {
        	   cover = BitmapFactory.decodeStream(musicResolver.openInputStream(albumArtUri));

        	   if(cover == null) {
        		   cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_cover);
        	   }

        	   // cover = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), albumArtUri);
           } catch (FileNotFoundException exception) {
               exception.printStackTrace();
               cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_cover);
           } catch (IOException e) {
        	   e.printStackTrace();
           }

            // cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_cover);

            Song song = new Song(id, title, artist, album, cover, data);
            songList.addSong(song);
        }

        // DEBUG
        // for(Song s : songList) { Log.d("songs", s.getTitle() + " " + s.getAlbum()); }
        // for(Map.Entry<String, ArrayList<Song>> entry : songList.sortByAlbum().entrySet()) {
        //   Log.d("song", "Album : " + entry.getKey());

        //   for(Song song : entry.getValue()) { Log.d("song", song.getTitle()); }
        //}

        return songList;
    }
}
