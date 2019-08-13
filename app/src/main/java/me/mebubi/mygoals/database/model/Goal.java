package me.mebubi.mygoals.database.model;

import android.graphics.Bitmap;

public class Goal {


    public static final String TABLE_NAME = "goal";

    public static final String GOAL_ID = "goalId";
    public static final String ORIGINAL_IMAGE_FILE = "originalImageFile";
    public static final String IMAGE_FILE = "imageFile";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String CREATION_DATE = "creationDate";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + GOAL_ID + " INTEGER PRIMARY KEY,"
                    + ORIGINAL_IMAGE_FILE + " BLOB,"
                    + IMAGE_FILE + " BLOB,"
                    + TITLE + " TEXT,"
                    + DESCRIPTION + " TEXT,"
                    + CREATION_DATE + " BIGINT"
                    + ")";

    private int goalId;
    private Bitmap originalImage;
    private Bitmap image;
    private String title;
    private String description;
    private long creationDate;

    public Goal() {
    }

    public Goal(Bitmap originalImage, Bitmap image, String title, String description) {
        this.originalImage = originalImage;
        this.image = image;
        this.title = title;
        this.description = description;
        this.creationDate = System.currentTimeMillis();
    }

    public Goal(int goalId, Bitmap originalImage, Bitmap image, String title, String description, long creationDate) {
        this.goalId = goalId;
        this.originalImage = originalImage;
        this.image = image;
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
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

    @Override
    public String toString() {
        return "Goal{" +
                "image=" + image +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
