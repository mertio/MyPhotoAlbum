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

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.database.model.Goal;
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
    private static final int ITEM_COUNT_TO_LOAD_EACH_TIME = 12;

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Goal.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // drop older table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Goal.TABLE_NAME);
        // create table again
        onCreate(sqLiteDatabase);
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


    public boolean loadGoalsFromDatabase(long goalIdOfLastItem, boolean clearAndLoad) {

        if(clearAndLoad) {
            GoalModel.clearGoals();
            goalIdOfLastItem = 0;
        }

        Log.d(LOGTAG, "Loading " + ITEM_COUNT_TO_LOAD_EACH_TIME + " items from a list starting from goal id of " + goalIdOfLastItem);

        String selectQuery = "SELECT " + Goal.GOAL_ID + ", " + Goal.IMAGE_FILE + ", " + Goal.TITLE + ", " + Goal.DESCRIPTION + ", " + Goal.CREATION_DATE +
                " FROM " + Goal.TABLE_NAME + " WHERE " + Goal.GOAL_ID + " > " + goalIdOfLastItem +
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



}
