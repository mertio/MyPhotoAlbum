package me.mebubi.myalbum.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;

import java.util.concurrent.ExecutionException;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.adapter.GoalAdapter;
import me.mebubi.myalbum.database.DatabaseHelper;
import me.mebubi.myalbum.dialog.AddGoalDialogFragment;
import me.mebubi.myalbum.database.model.Goal;
import me.mebubi.myalbum.model.GoalModel;
import me.mebubi.myalbum.view.GoalView;

public class PhotoActivity extends AppCompatActivity implements AddGoalDialogFragment.OnAddGoalListener, GoalView.OnGoalClickListener {

    private static final String LOGTAG = "PhotoActivity";

    private ImageView fullScreenImageView;
    private ImageButton fullScreenImageCloseButton;
    private FloatingActionButton addGoalButton;
    private RecyclerView goalsRecyclerView;
    private GoalAdapter goalAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar goalProgressBar;
    private Toolbar toolbar;

    private SharedPreferences prefs;
    private DatabaseHelper db;

    private int currentAlbumId;
    private int spanCount;
    Bitmap imageToShowFullSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get albumId
        Intent intent = getIntent();
        currentAlbumId = intent.getIntExtra("albumId", 0);
        Log.d(LOGTAG, "Fetched album id: " + currentAlbumId);

        initialize();
        setOnClickMethods();
        new LoadDatabaseTask(true, null).execute();

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
            new AddGoalTask(goal).execute();
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
    public void onGoalClick(String originalImagePath) {
        imageToShowFullSize = db.fetchOriginalImageOfGoal(originalImagePath);
        if(imageToShowFullSize != null) {
            fullScreenImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            fullScreenImageView.setImageBitmap(imageToShowFullSize);
            enterFullScreenImageDisplayMode();
        }
    }

    @Override
    public void onGoalLongClick(boolean success) {
        // called when ok clicked in dialog
        if (success) {
            goalAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getApplicationContext(), "Delete unsuccessful", Toast.LENGTH_LONG).show();
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

    private void initialize() {

        // view initialization
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        goalsRecyclerView = findViewById(R.id.goalRecyclerView);
        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        fullScreenImageCloseButton = findViewById(R.id.fullScreenImageCloseButton);
        fullScreenImageView.setOnTouchListener(new ImageMatrixTouchHandler(getApplicationContext()));
        addGoalButton = findViewById(R.id.addGoalActionButton);
        goalProgressBar = findViewById(R.id.goalProgressBar);

        // db helper and fetch sharedprefs
        db = new DatabaseHelper(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        spanCount = prefs.getInt("spanCount", 1);

        // recyclerview initialization
        goalAdapter = new GoalAdapter(GoalModel.getGoals(), this);
        goalsRecyclerView.setAdapter(goalAdapter);
        layoutManager = new GridLayoutManager(this, spanCount);
        goalsRecyclerView.setLayoutManager(layoutManager);
        goalAdapter.notifyDataSetChanged();

        // hide accessory widgets on initialization
        exitFullScreenImageDisplayMode();
        hideProgressBar();
    }

    private void setOnClickMethods() {

        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = AddGoalDialogFragment.getInstance(currentAlbumId);
                dialogFragment.setCancelable(false);
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

        goalsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean loading = false;

                final int visibleThreshold = 4;

                GridLayoutManager layoutManager = (GridLayoutManager)goalsRecyclerView.getLayoutManager();
                int lastItem  = layoutManager.findLastCompletelyVisibleItemPosition();
                int currentTotalCount = goalAdapter.getItemCount();

                if(!loading && currentTotalCount <= lastItem + visibleThreshold){
                    //loading = true;
                    //show your loading view
                    // load content in background

                    new LoadDatabaseTask(false, loading).execute();

                    //loading = false;
                }


            }
        });

    }

    private void displayProgressBar() {
        goalsRecyclerView.setVisibility(View.GONE);
        addGoalButton.hide();
        goalProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        goalsRecyclerView.setVisibility(View.VISIBLE);
        addGoalButton.show();
        goalProgressBar.setVisibility(View.GONE);
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

    private class LoadDatabaseTask extends AsyncTask {

        private DatabaseHelper db;
        private boolean clearAndLoad;
        private boolean success;
        private Boolean lock;

        LoadDatabaseTask(boolean clearAndLoad, Boolean lock) {
            this.clearAndLoad = clearAndLoad;
            this.success = false;
            this.lock = lock;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = new DatabaseHelper(getApplicationContext());
            lock = true;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (goalAdapter.getGoalList().isEmpty()) {
                success = db.loadGoalsFromDatabase(0, clearAndLoad, currentAlbumId);
                return null;
            }
            success = db.loadGoalsFromDatabase(goalAdapter.getGoalList().get(goalAdapter.getItemCount() - 1).getCreationDate(), clearAndLoad, currentAlbumId);
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (success) {
                Log.d(LOGTAG, "Goal list: " + goalAdapter.getGoalList());
                goalAdapter.notifyDataSetChanged();
            }
            lock = false;
        }
    }

    private class AddGoalTask extends AsyncTask {

        private DatabaseHelper db;
        private Goal goal;

        AddGoalTask(Goal goal) {
            this.goal = goal;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = new DatabaseHelper(getApplicationContext());
            // display loading bar
            displayProgressBar();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Log.d(LOGTAG, "Goal to add : " + goal);
            db.insertGoalIntoDatabase(goal);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            goalAdapter.notifyDataSetChanged();
            // hide loading bar
            hideProgressBar();
            db.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
