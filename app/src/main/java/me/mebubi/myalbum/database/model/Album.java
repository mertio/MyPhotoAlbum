package me.mebubi.myalbum.database.model;

import android.graphics.Bitmap;

public class Album {

    public static final String TABLE_NAME = "album";

    public static final String ALBUM_ID = "albumId";
    public static final String ALBUM_IMAGE = "albumImage";
    public static final String ALBUM_TITLE = "albumTitle";
    public static final String ALBUM_DESCRIPTION = "albumDescription";
    public static final String CREATION_DATE = "creationDate";
    public static final String LAST_OPENED_DATE = "lastOpenedDate";


    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + " ("
            + ALBUM_ID + " INTEGER PRIMARY KEY, "
            + ALBUM_IMAGE + " BLOB, "
            + ALBUM_TITLE + " TEXT, "
            + ALBUM_DESCRIPTION + " TEXT, "
            + CREATION_DATE + " BIGINT,"
            + LAST_OPENED_DATE + " BIGINT"
            + ")";

    private int albumId;
    private Bitmap albumImage;
    private String albumTitle;
    private String albumDescription;
    private long creationDate;
    private long lastOpenedDate;

    public Album() {
    }

    public Album(int albumId, Bitmap albumImage, String albumTitle, String albumDescription, long creationDate, long lastOpenedDate) {
        this.albumId = albumId;
        this.albumImage = albumImage;
        this.albumTitle = albumTitle;
        this.albumDescription = albumDescription;
        this.creationDate = creationDate;
        this.lastOpenedDate = lastOpenedDate;
    }

    public Album(Bitmap albumImage, String albumTitle, String albumDescription) {
        this.albumImage = albumImage;
        this.albumTitle = albumTitle;
        this.albumDescription = albumDescription;
        this.creationDate = System.currentTimeMillis();
        this.lastOpenedDate = System.currentTimeMillis();
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public Bitmap getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(Bitmap albumImage) {
        this.albumImage = albumImage;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public String getAlbumDescription() {
        return albumDescription;
    }

    public void setAlbumDescription(String albumDescription) {
        this.albumDescription = albumDescription;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getLastOpenedDate() {
        return lastOpenedDate;
    }

    public void setLastOpenedDate(long lastOpenedDate) {
        this.lastOpenedDate = lastOpenedDate;
    }

    @Override
    public String toString() {
        return "Album{" +
                "albumId=" + albumId +
                ", albumImage=" + albumImage +
                ", albumTitle='" + albumTitle + '\'' +
                ", albumDescription='" + albumDescription + '\'' +
                ", creationDate=" + creationDate +
                ", lastOpenedDate=" + lastOpenedDate +
                '}';
    }
}
