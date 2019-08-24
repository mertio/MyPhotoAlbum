package me.mebubi.myalbum.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.database.model.Goal;
import me.mebubi.myalbum.model.AlbumModel;
import me.mebubi.myalbum.model.GoalModel;
import me.mebubi.myalbum.security.Crypto;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOGTAG = "DatabaseHelper";

    private static final String PASSWORD_FOR_FILE_STORAGE = "ergwowerghoaergnfegvkasovkaeorga";

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "goal_db";
    // Pagination item count to load at a time
    private static final int ITEM_COUNT_TO_LOAD_EACH_TIME = 20;

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Goal.CREATE_TABLE);
        sqLiteDatabase.execSQL(Album.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // drop older table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Goal.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Album.TABLE_NAME);
        // create table again
        onCreate(sqLiteDatabase);
    }

    public void insertAlbumIntoDatabase(Album album) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (album.getAlbumImage() != null) {
            values.put(Album.ALBUM_IMAGE, DbBitmapUtility.getBytes(album.getAlbumImage()));
        }
        values.put(Album.ALBUM_TITLE, album.getAlbumTitle());
        values.put(Album.ALBUM_DESCRIPTION, album.getAlbumDescription());
        values.put(Album.CREATION_DATE, album.getCreationDate());
        values.put(Album.LAST_OPENED_DATE, album.getLastOpenedDate());

        db.insert(Album.TABLE_NAME, null, values);

        String lastRowIdQuery = "SELECT last_insert_rowid() FROM " + Album.TABLE_NAME;
        Cursor cursor = db.rawQuery(lastRowIdQuery, null);
        if(cursor.moveToFirst()) {
            album.setAlbumId(cursor.getInt(0));
        }
        //AlbumModel.addAlbum(album);
        db.close();

    }


    public void insertGoalIntoDatabase(Goal goal) {
        String filePath = "";
        if (goal.getImage() != null) {
            // first write file to the inner directory and returns absolute path to be written in database
            filePath = saveToInternalSorage(goal.getOriginalImage(), goal.getCreationDate() + ".jpg");
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (goal.getImage() != null) {
            values.put(Goal.ORIGINAL_IMAGE_PATH, filePath);
            values.put(Goal.IMAGE_FILE, DbBitmapUtility.getBytes(goal.getImage()));
        }
        values.put(Goal.TITLE, goal.getTitle());
        values.put(Goal.DESCRIPTION, goal.getDescription());
        values.put(Goal.CREATION_DATE, goal.getCreationDate());
        values.put(Goal.ALBUM_ID, goal.getAlbumId());

        db.insert(Goal.TABLE_NAME, null, values);
        String lastRowIdQuery = "SELECT last_insert_rowid() FROM " + Goal.TABLE_NAME;
        Cursor cursor = db.rawQuery(lastRowIdQuery, null);
        if(cursor.moveToFirst()) {
            goal.setGoalId(cursor.getInt(0));
        }
        GoalModel.addGoal(goal);

        db.close();

    }

    public Bitmap fetchOriginalImageOfGoal(String path) {
        Bitmap image = loadImageFromStorage(path);
        return image;
    }


    public boolean deleteAlbumFromDatabase(int albumId) {
        boolean success;
        List<Long> creationDatesOfPhotosInAlbum = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        // TODO HERE delete all goals associated with album
        Cursor cursor = db.rawQuery("SELECT " + Goal.CREATION_DATE + " FROM " + Goal.TABLE_NAME + " WHERE " + Goal.ALBUM_ID + " = " + albumId, null);

        if(cursor.moveToFirst()) {
            do {
                creationDatesOfPhotosInAlbum.add(cursor.getLong(cursor.getColumnIndex(Goal.CREATION_DATE)));
            } while (cursor.moveToNext());
        }

        for (int i = 0; i < creationDatesOfPhotosInAlbum.size(); i++) {
            deleteImageFromStorage(creationDatesOfPhotosInAlbum.get(i) + ".jpg");
        }

        success = db.delete(Album.TABLE_NAME, Album.ALBUM_ID + "=?", new String[]{albumId + ""}) > 0;
        db.close();

        if (success) {
            AlbumModel.deleteAlbum(albumId);
        }

        return success;
    }

    public boolean editAlbumInDatabase(Album album) {
        boolean success;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (album.getAlbumImage() != null) {
            values.put(Album.ALBUM_IMAGE, DbBitmapUtility.getBytes(album.getAlbumImage()));
        } else {
            values.putNull(Album.ALBUM_IMAGE);
        }
        values.put(Album.ALBUM_TITLE, album.getAlbumTitle());
        values.put(Album.ALBUM_DESCRIPTION, album.getAlbumDescription());
        values.put(Album.CREATION_DATE, album.getCreationDate());
        values.put(Album.LAST_OPENED_DATE, album.getLastOpenedDate());

        success = db.update(Album.TABLE_NAME, values, Album.ALBUM_ID + " =?", new String[]{album.getAlbumId() + ""}) > 0;
        db.close();

        if (success) {
            AlbumModel.editAlbum(album);
        }

        return success;
    }



    public boolean deleteGoalFromDatabase(Goal goal) {
        boolean success;
        if (goal.getImage() != null) {
            success = deleteImageFromStorage(goal.getCreationDate() + ".jpg");
            if (!success) {
                return success;
            }
        }

        SQLiteDatabase db = this.getWritableDatabase();
        success = db.delete(Goal.TABLE_NAME, Goal.GOAL_ID + "=?", new String[]{goal.getGoalId() + ""}) > 0;
        if(success) {
            GoalModel.deleteGoal(goal.getGoalId());
        }
        db.close();
        return success;
    }

    public boolean loadAlbumsFromDatabase(long lastOpenedDateOfLastAlbum, boolean clearAndLoad) {
        if(clearAndLoad) {
            AlbumModel.clearAlbums();
            lastOpenedDateOfLastAlbum = Long.MAX_VALUE;
        }

        Log.d(LOGTAG, "Loading " + ITEM_COUNT_TO_LOAD_EACH_TIME + " items from a list starting from album last opened date of " + lastOpenedDateOfLastAlbum);

        String selectQuery = "SELECT " + Album.ALBUM_ID + ", " + Album.ALBUM_IMAGE + ", " + Album.ALBUM_TITLE + ", " + Album.ALBUM_DESCRIPTION + ", " + Album.CREATION_DATE + ", " + Album.LAST_OPENED_DATE +
                " FROM " + Album.TABLE_NAME + " WHERE " + Album.LAST_OPENED_DATE + " < " + lastOpenedDateOfLastAlbum +
                " ORDER BY " + Album.LAST_OPENED_DATE + " DESC LIMIT " + ITEM_COUNT_TO_LOAD_EACH_TIME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {

            do {
                Album album = new Album();
                album.setAlbumId(cursor.getInt(cursor.getColumnIndex(Album.ALBUM_ID)));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(Album.ALBUM_IMAGE));
                if (image != null) {
                    album.setAlbumImage(DbBitmapUtility.getImage(image));
                }
                album.setAlbumTitle(cursor.getString(cursor.getColumnIndex(Album.ALBUM_TITLE)));
                album.setAlbumDescription(cursor.getString(cursor.getColumnIndex(Album.ALBUM_DESCRIPTION)));
                album.setCreationDate(cursor.getLong(cursor.getColumnIndex(Album.CREATION_DATE)));
                album.setLastOpenedDate(cursor.getLong(cursor.getColumnIndex(Album.LAST_OPENED_DATE)));

                AlbumModel.addAlbum(album);

            } while (cursor.moveToNext());

            db.close();
        } else {
            return false;
        }
        return true;
    }



    public boolean loadGoalsFromDatabase(long createdDateOfLastItem, boolean clearAndLoad, int albumId) {

        if(clearAndLoad) {
            GoalModel.clearGoals();
            createdDateOfLastItem = 0;
        }

        Log.d(LOGTAG, "Loading " + ITEM_COUNT_TO_LOAD_EACH_TIME + " items from a list starting from goal creation date of " + createdDateOfLastItem);

        String selectQuery = "SELECT " + Goal.GOAL_ID + ", " + Goal.IMAGE_FILE + ", " + Goal.TITLE + ", " + Goal.DESCRIPTION + ", " + Goal.CREATION_DATE + ", " + Goal.ALBUM_ID +
                " FROM " + Goal.TABLE_NAME + " WHERE " + Goal.ALBUM_ID + " = " + albumId + " AND " + Goal.CREATION_DATE + " > " + createdDateOfLastItem +
                " ORDER BY " + Goal.CREATION_DATE + " ASC LIMIT " + ITEM_COUNT_TO_LOAD_EACH_TIME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {

            do {
                Goal goal = new Goal();
                goal.setGoalId(cursor.getInt(cursor.getColumnIndex(Goal.GOAL_ID)));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(Goal.IMAGE_FILE));
                if (image != null) {
                    goal.setImage(DbBitmapUtility.getImage(image));
                }
                goal.setTitle(cursor.getString(cursor.getColumnIndex(Goal.TITLE)));
                goal.setDescription(cursor.getString(cursor.getColumnIndex(Goal.DESCRIPTION)));
                goal.setCreationDate(cursor.getLong(cursor.getColumnIndex(Goal.CREATION_DATE)));
                goal.setAlbumId(cursor.getInt(cursor.getColumnIndex(Goal.ALBUM_ID)));

                GoalModel.addGoal(goal);

            } while (cursor.moveToNext());

            db.close();
        } else {
            return false;
        }
        return true;
    }

    private String saveToInternalSorage(Bitmap bitmapImage, String fileName) {



        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, fileName);

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to
            // the OutputStream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            byte[] encryptedFile = Crypto.encodeFile(PASSWORD_FOR_FILE_STORAGE.getBytes(), byteArray);
            fos.write(encryptedFile);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage(String path) {

        try {
            ContextWrapper cw = new ContextWrapper(context);
            File path1 = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f = new File(path1, path);
            DataInputStream dis = new DataInputStream(new FileInputStream(f));
            byte[] bytes = new byte[(int) f.length()];
            dis.readFully(bytes);
            bytes = Crypto.decodeFile(PASSWORD_FOR_FILE_STORAGE.getBytes(), bytes);
            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getByte(String path) {
        byte[] getBytes = {};
        try {
            File file = new File(path);
            getBytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(getBytes);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getBytes;
    }

    private boolean deleteImageFromStorage(String path) {

        try {
            ContextWrapper cw = new ContextWrapper(context);
            File path1 = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f = new File(path1, path);
            return f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateLastOpenedDateOfAlbum(int albumId) {

        boolean success;
        long openedDate = System.currentTimeMillis();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Album.LAST_OPENED_DATE, openedDate);
        success = db.update(Album.TABLE_NAME, values, Album.ALBUM_ID + " =?", new String[]{albumId + ""}) > 0;
        db.close();

        return success;

    }

}
