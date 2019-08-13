package me.mebubi.mygoals.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.List;

import me.mebubi.mygoals.database.model.Goal;
import me.mebubi.mygoals.model.GoalModel;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOGTAG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "goal_db";

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


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        System.out.println("Original image : " + goal.getOriginalImage());
        values.put(Goal.ORIGINAL_IMAGE_FILE, DbBitmapUtility.getBytes(goal.getOriginalImage()));
        values.put(Goal.IMAGE_FILE, DbBitmapUtility.getBytes(goal.getImage()));
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

    public Bitmap fetchOriginalImageOfGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String lastRowIdQuery = "SELECT " + Goal.ORIGINAL_IMAGE_FILE +" FROM " + Goal.TABLE_NAME + " WHERE " + Goal.GOAL_ID + " = '" + goalId + "'";
        Cursor cursor = db.rawQuery(lastRowIdQuery, null);
        System.out.println("Goal id : " + goalId);
        if(cursor.moveToFirst()) {
            return DbBitmapUtility.getImage(cursor.getBlob(0));
        }
        return null;
    }

    public Bitmap fetchPreviousImageOfGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String lastRowIdQuery = "SELECT " + Goal.ORIGINAL_IMAGE_FILE +" FROM " + Goal.TABLE_NAME + " WHERE " + Goal.GOAL_ID + " > '" + goalId + "' LIMIT 1";
        Cursor cursor = db.rawQuery(lastRowIdQuery, null);
        System.out.println("Goal id : " + goalId);
        if(cursor.moveToFirst()) {
            return DbBitmapUtility.getImage(cursor.getBlob(0));
        }
        return null;
    }

    public Bitmap fetchNextImageOfGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String lastRowIdQuery = "SELECT " + Goal.ORIGINAL_IMAGE_FILE +" FROM " + Goal.TABLE_NAME + " WHERE " + Goal.GOAL_ID + " < '" + goalId + "' LIMIT 1";
        Cursor cursor = db.rawQuery(lastRowIdQuery, null);
        System.out.println("Goal id : " + goalId);
        if(cursor.moveToFirst()) {
            return DbBitmapUtility.getImage(cursor.getBlob(0));
        }
        return null;
    }

    public boolean deleteGoalFromDatabase(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = db.delete(Goal.TABLE_NAME, Goal.GOAL_ID + "=?", new String[]{goalId + ""}) > 0;
        if(success) {
            GoalModel.deleteGoal(goalId);
        }
        db.close();
        return success;
    }


    public void loadGoalsFromDatabase() {

        GoalModel.clearGoals();

        String selectQuery = "SELECT " + Goal.GOAL_ID + ", " + Goal.IMAGE_FILE + ", " + Goal.TITLE + ", " + Goal.DESCRIPTION + ", " + Goal.CREATION_DATE + " FROM " + Goal.TABLE_NAME + " ORDER BY " + Goal.CREATION_DATE + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {

            do {
                Goal goal = new Goal();
                goal.setGoalId(cursor.getInt(cursor.getColumnIndex(Goal.GOAL_ID)));
                goal.setImage(DbBitmapUtility.getImage(cursor.getBlob(cursor.getColumnIndex(Goal.IMAGE_FILE))));
                goal.setTitle(cursor.getString(cursor.getColumnIndex(Goal.TITLE)));
                goal.setDescription(cursor.getString(cursor.getColumnIndex(Goal.DESCRIPTION)));
                goal.setCreationDate(cursor.getLong(cursor.getColumnIndex(Goal.CREATION_DATE)));

                GoalModel.addGoal(goal);

            } while (cursor.moveToNext());

            db.close();
        }

    }



}
