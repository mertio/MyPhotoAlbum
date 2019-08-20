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
        synchronized (albums) {
            albums.add(album);
        }
    }

    public static void deleteAlbum(int albumId) {
        synchronized (albums) {
            for (int i = 0; i < albums.size(); i++) {
                if(albums.get(i).getAlbumId() == albumId) {
                    AlbumModel.getAlbums().remove(i);
                }
            }
        }
    }

    public static void clearAlbums() {
        synchronized (albums) {
            albums.clear();
        }
    }
}
