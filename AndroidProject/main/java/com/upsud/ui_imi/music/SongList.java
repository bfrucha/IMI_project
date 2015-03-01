package com.upsud.ui_imi.music;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;


// provides function to sort the songs list

/**
 * BASED ON SORTED FILES IN ANDROID FOLDERS *
 */
public class SongList {

    ArrayList<Song> unsortedList;

    public SongList() {
        unsortedList = new ArrayList<Song>();
    }


    public void addSong(Song song) {
        unsortedList.add(song);
    }

    public void removeSong(Song song) {
        unsortedList.remove(song);
    }

    public ArrayList<Song> getSongList() {
        return unsortedList;
    }

    public int getSize() {
        return unsortedList.size();
    }

    public Song get(int i) {
        return unsortedList.get(i);
    }

    public void sortByTitles() {
        Collections.sort(unsortedList, new SongComparatorbyTitle());
    }

    public void sortByArtists() {
        Collections.sort(unsortedList, new SongComparatorbyArtist());
    }

    public void sortByAlbums() {
        Collections.sort(unsortedList, new SongComparatorbyAlbum());
    }


    private class SongComparatorbyTitle implements Comparator<Song> {
        @Override
        public int compare(Song s1, Song s2) {
            return s1.getTitle().compareTo(s2.getTitle());
        }
    }

    private class SongComparatorbyArtist implements Comparator<Song> {
        @Override
        public int compare(Song s1, Song s2) {
            return s1.getArtist().compareTo(s2.getArtist());
        }
    }

    private class SongComparatorbyAlbum implements Comparator<Song> {
        @Override
        public int compare(Song s1, Song s2) {
            return s1.getAlbum().compareTo(s2.getAlbum());
        }
    }

}
