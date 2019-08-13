package me.mebubi.mygoals.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;

import java.util.ArrayList;

import me.mebubi.mygoals.R;
import me.mebubi.mygoals.adapter.GoalAdapter;
import me.mebubi.mygoals.database.DatabaseHelper;
import me.mebubi.mygoals.dialog.AddGoalDialogFragment;
import me.mebubi.mygoals.database.model.Goal;
import me.mebubi.mygoals.listener.OnSwipeTouchListener;
import me.mebubi.mygoals.model.GoalModel;
import me.mebubi.mygoals.view.GoalView;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements AddGoalDialogFragment.OnAddGoalListener, GoalView.OnGoalClickListener {

    private static final String LOGTAG = "MainActivity";

    private ImageView fullScreenImageView;
    private ImageButton fullScreenImageCloseButton;
    private FloatingActionButton addGoalButton;
    private RecyclerView goalsRecyclerView;
    private GoalAdapter goalAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private SharedPreferences prefs;
    private DatabaseHelper db;

    private int spanCount;
    Bitmap imageToShowFullSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        spanCount = prefs.getInt("spanCount", 1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        goalsRecyclerView = findViewById(R.id.goalRecyclerView);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        fullScreenImageCloseButton = findViewById(R.id.fullScreenImageCloseButton);
        fullScreenImageView.setOnTouchListener(new ImageMatrixTouchHandler(getApplicationContext()));




        addGoalButton = findViewById(R.id.addGoalActionButton);
        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = AddGoalDialogFragment.getInstance();

                dialogFragment.show(getSupportFragmentManager(), "addGoalDialog");
            }
        });

        fullScreenImageCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenImageView.setImageResource(0);
                exitFullScreenImageDisplayMode();
            }
        });

        goalAdapter = new GoalAdapter(GoalModel.getGoals(), this);
        goalsRecyclerView.setAdapter(goalAdapter);
        layoutManager = new GridLayoutManager(this, spanCount);
        goalsRecyclerView.setLayoutManager(layoutManager);
        goalAdapter.notifyDataSetChanged();

        new LoadDatabaseTask().execute();

        exitFullScreenImageDisplayMode();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_grid_span_one) {
            changeSpanCountOnGrid(1);
        } else if (id == R.id.action_grid_span_two) {
            changeSpanCountOnGrid(2);
        }

        return super.onOptionsItemSelected(item);
    }


    private void changeSpanCountOnGrid(int newSpanCount) {
        spanCount = newSpanCount;
        layoutManager = new GridLayoutManager(this, spanCount);
        goalsRecyclerView.setLayoutManager(layoutManager);
        goalAdapter.notifyDataSetChanged();

        // save setting
        prefs.edit().putInt("spanCount", spanCount).commit();
    }


    @Override
    public void onGoalAdd(Goal goal) {
        try {
            Log.d(LOGTAG, "Goal to add : " + goal);
            db.insertGoalIntoDatabase(goal);
            goalAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Add unsuccessful", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onGoalClick(int goalId) {
        imageToShowFullSize = db.fetchOriginalImageOfGoal(goalId);
        if(imageToShowFullSize != null) {
            fullScreenImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            fullScreenImageView.setImageBitmap(imageToShowFullSize);
            enterFullScreenImageDisplayMode();
        }
    }

    @Override
    public void onGoalLongClick(boolean success) {
        // dialog to delete
        if (success) {
            goalAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getApplicationContext(), "Delete unsuccessful", Toast.LENGTH_LONG).show();
        }
    }


    private class LoadDatabaseTask extends AsyncTask {

        DatabaseHelper db;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = new DatabaseHelper(getApplicationContext());
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            db.loadGoalsFromDatabase();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.d(LOGTAG, "Goal list: " + goalAdapter.getGoalList());
            goalAdapter.notifyDataSetChanged();
        }
    }

    private void enterFullScreenImageDisplayMode() {
        getSupportActionBar().hide();
        addGoalButton.hide();
        fullScreenImageView.setVisibility(View.VISIBLE);
        fullScreenImageCloseButton.setVisibility(View.VISIBLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void exitFullScreenImageDisplayMode() {
        getSupportActionBar().show();
        addGoalButton.show();
        fullScreenImageView.setVisibility(View.GONE);
        fullScreenImageCloseButton.setVisibility(View.GONE);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (fullScreenImageView != null && imageToShowFullSize != null) {
            fullScreenImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }


}
