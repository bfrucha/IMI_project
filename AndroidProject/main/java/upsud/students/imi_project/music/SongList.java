package upsud.students.imi_project.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


// provides function to sort the songs list
/** BASED ON SORTED FILES IN ANDROID FOLDERS **/
public class SongList implements Iterable<Song> {

	ArrayList<Song> unsortedList;
	
	public SongList() {
		unsortedList = new ArrayList<Song>();
	}
	
	
	public void addSong(Song song) { unsortedList.add(song); }
	
	public void removeSong(Song song) { unsortedList.remove(song); }
	
	public ArrayList<Song> getSongList() { return unsortedList; }
	
	// sort songs list by album
	public HashMap<String, ArrayList<Song>> sortByAlbum() {
		HashMap<String, ArrayList<Song>> sortedMap = new HashMap<String, ArrayList<Song>>();
		
		String currentAlbum = "";
		for(Song song : unsortedList) {
			// change album title
			if(!currentAlbum.equals(song.getAlbum())) { currentAlbum = song.getAlbum(); }
			
			// try to add songs to the list
			try { sortedMap.get(currentAlbum).add(song); }
			
			// create the list if it is not already
			catch (NullPointerException exn) { 
				ArrayList<Song> songs = new ArrayList<Song>();
				songs.add(song);
				sortedMap.put(currentAlbum, songs);
			}
		}
		
		return sortedMap;
	}
	
	// sort songs list by artist
	public HashMap<String, ArrayList<Song>> sortByArtist() {
		HashMap<String, ArrayList<Song>> sortedMap = new HashMap<String, ArrayList<Song>>();
		
		String currentArtist = "";
		for(Song song : unsortedList) {
			// change album title
			if(!currentArtist.equals(song.getArtist())) { currentArtist = song.getAlbum(); }
			
			// try to add songs to the list
			try { sortedMap.get(currentArtist).add(song); }
			
			// create the list if it is not already
			catch (NullPointerException exn) { 
				ArrayList<Song> songs = new ArrayList<Song>();
				songs.add(song);
				sortedMap.put(currentArtist, songs);
			}
		}
		
		return sortedMap;
	}


	@Override
	public Iterator<Song> iterator() {
		return (Iterator<Song>) unsortedList.iterator();
	}
	
}
