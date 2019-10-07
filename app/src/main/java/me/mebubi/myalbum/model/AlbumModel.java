package me.mebubi.myalbum.model;

import java.util.ArrayList;
import java.util.List;

import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.database.model.Goal;

public class AlbumModel {

    private static List<Album> albums;

    private AlbumModel() {
    }

    public synchronized static List<Album> getAlbums() {
        if(albums == null) {
            albums = new ArrayList<>();
            return albums;
        }
        return albums;
    }

    public static void addAlbum(Album album) {
        synchronized (getAlbums()) {
            albums.add(album);
        }
    }

    public static void deleteAlbum(int albumId) {
        synchronized (getAlbums()) {
            for (int i = 0; i < albums.size(); i++) {
                if(albums.get(i).getAlbumId() == albumId) {
                    AlbumModel.getAlbums().remove(i);
                }
            }
        }
    }

    public static void editAlbum(Album album) {
        synchronized (getAlbums()) {
            for (int i = 0; i < albums.size(); i++) {
                if(albums.get(i).getAlbumId() == album.getAlbumId()) {
                    AlbumModel.getAlbums().get(i).setAlbumImage(album.getAlbumImage());
                    AlbumModel.getAlbums().get(i).setAlbumTitle(album.getAlbumTitle());
                    AlbumModel.getAlbums().get(i).setAlbumDescription(album.getAlbumDescription());
                    AlbumModel.getAlbums().get(i).setCreationDate(album.getCreationDate());
                    AlbumModel.getAlbums().get(i).setLastOpenedDate(album.getLastOpenedDate());
                }
            }
        }
    }


    public static void clearAlbums() {
        synchronized (getAlbums()) {
            albums.clear();
        }
    }
}
