package com.upsud.ui_imi;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class Song {

    public static final int WIDTH = 200;
    public static final int HEIGHT = 200;

    private long id;
    private String title;
    private String artist;
    private String album;
    private Bitmap cover;
    private String path;

    public Song(long id, String title, String artist, String album, Bitmap cover, String path) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;

        if (cover != null) {
            this.cover = Bitmap.createScaledBitmap(cover, WIDTH, HEIGHT, true);
        } else {
            android.util.Log.d("song", "No cover for the song " + title);
        }

        this.path = path;
    }

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public Bitmap getCover() {
        return cover;
    }

    public String getPath() {
        return path;
    }

    public void showCover(ImageView enclosingView) {
        new BitmapWorkerTask(enclosingView).execute();
    }


    // to display correctly album art cover
    public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            return cover;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    Log.d("song", "Set bitmap");
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

}