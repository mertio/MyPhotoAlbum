package me.mebubi.myalbum.database.model;

import android.graphics.Bitmap;

public class Goal {


    public static final String TABLE_NAME = "goal";

    public static final String GOAL_ID = "goalId";
    public static final String ORIGINAL_IMAGE_PATH = "originalImagePath";
    public static final String IMAGE_FILE = "imageFile";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String CREATION_DATE = "creationDate";
    public static final String ALBUM_ID = "albumId";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + GOAL_ID + " INTEGER PRIMARY KEY,"
                    + ORIGINAL_IMAGE_PATH + " TEXT,"
                    + IMAGE_FILE + " BLOB,"
                    + TITLE + " TEXT,"
                    + DESCRIPTION + " TEXT,"
                    + CREATION_DATE + " BIGINT,"
                    + ALBUM_ID + " INT"
                    + ")";

    private int goalId;
    private Bitmap originalImage;
    private Bitmap image;
    private String title;
    private String description;
    private long creationDate;
    private int albumId;

    public Goal() {
    }

    public Goal(Bitmap originalImage, Bitmap image, String title, String description, int albumId) {
        this.originalImage = originalImage;
        this.image = image;
        this.title = title;
        this.description = description;
        this.creationDate = System.currentTimeMillis();
        this.albumId = albumId;
    }

    public Goal(int goalId, Bitmap originalImage, Bitmap image, String title, String description, long creationDate, int albumId) {
        this.goalId = goalId;
        this.originalImage = originalImage;
        this.image = image;
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.albumId = albumId;
    }

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public Bitmap getOriginalImage() {
        return originalImage;
    }

    public void setOriginalImage(Bitmap originalImage) {
        this.originalImage = originalImage;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "goalId=" + goalId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creationDate=" + creationDate +
                ", albumId=" + albumId +
                '}';
    }
}
