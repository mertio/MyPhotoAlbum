package me.mebubi.myalbum.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
    private ImageView exportImageButton;
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
    private boolean sortAsc;
    Bitmap imageToShowFullSize;

    // state
    private boolean fullScreenImageDisplayMode = false;

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
        new LoadDatabaseTask(true, null, sortAsc).execute();

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
        if (id == R.id.action_grid_span_one) {
            changeSpanCountOnGrid(1);
        } else if (id == R.id.action_grid_span_two) {
            changeSpanCountOnGrid(2);
        } else if (id == R.id.action_export_album) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {
                new ExportAlbumTask(currentAlbumId).execute();
            }
        } else if (id == R.id.action_sort_asc) {
            setAscSort(true);
        } else if (id == R.id.action_sort_desc) {
            setAscSort(false);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, 101);
    }


    private void changeSpanCountOnGrid(int newSpanCount) {
        spanCount = newSpanCount;
        layoutManager = new GridLayoutManager(this, spanCount);
        goalsRecyclerView.setLayoutManager(layoutManager);
        goalAdapter.notifyDataSetChanged();

        // save setting
        prefs.edit().putInt("spanCount", spanCount).commit();
    }

    private void setAscSort(boolean sortAsc) {
        this.sortAsc = sortAsc;
        prefs.edit().putBoolean("sortAsc", sortAsc).commit();
        new LoadDatabaseTask(true, null, sortAsc).execute();
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
            Toast.makeText(getApplicationContext(), "Photo deleted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Delete unsuccessful", Toast.LENGTH_LONG).show();
        }
    }

    private void enterFullScreenImageDisplayMode() {
        fullScreenImageDisplayMode = true;

        getSupportActionBar().hide();
        addGoalButton.hide();
        fullScreenImageView.setVisibility(View.VISIBLE);
        fullScreenImageCloseButton.setVisibility(View.VISIBLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void exitFullScreenImageDisplayMode() {
        fullScreenImageDisplayMode = false;


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
        exportImageButton = findViewById(R.id.exportImageView);
        fullScreenImageView.setOnTouchListener(new ImageMatrixTouchHandler(getApplicationContext()));
        addGoalButton = findViewById(R.id.addGoalActionButton);
        goalProgressBar = findViewById(R.id.goalProgressBar);

        // db helper and fetch sharedprefs
        db = new DatabaseHelper(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        spanCount = prefs.getInt("spanCount", 1);
        sortAsc = prefs.getBoolean("sortAsc", true);

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

        exportImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageToShowFullSize != null) {
                    if (!checkIfAlreadyhavePermission()) {
                        requestForSpecificPermission();
                    } else {
                        showConfirmDialogForExportImage(PhotoActivity.this);
                    }
                }
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

                    new LoadDatabaseTask(false, loading, sortAsc).execute();

                    //loading = false;
                }


            }
        });

    }

    public void showConfirmDialogForExportImage(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Are you sure you want to export this image?");
        // Add the buttons
        builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                new ExportImageTask(imageToShowFullSize).execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted

                } else {
                    //not granted
                    Toast.makeText(getApplicationContext(), "To export album, please turn on the storage permission from the settings", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class LoadDatabaseTask extends AsyncTask {

        private DatabaseHelper db;
        private boolean clearAndLoad;
        private boolean success;
        private Boolean lock;
        private boolean sortAsc;

        LoadDatabaseTask(boolean clearAndLoad, Boolean lock, boolean sortAsc) {
            this.clearAndLoad = clearAndLoad;
            this.success = false;
            this.lock = lock;
            this.sortAsc = sortAsc;
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
                success = db.loadGoalsFromDatabase(0, clearAndLoad, currentAlbumId, sortAsc);
                return null;
            }
            success = db.loadGoalsFromDatabase(goalAdapter.getGoalList().get(goalAdapter.getItemCount() - 1).getCreationDate(), clearAndLoad, currentAlbumId, sortAsc);
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

    private class ExportAlbumTask extends AsyncTask {

        private int albumId;
        private DatabaseHelper db;
        private boolean success;

        ExportAlbumTask(int albumId) {
            this.success = false;
            this.albumId = albumId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = new DatabaseHelper(getApplicationContext());
            displayProgressBar();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            success = db.exportAlbumPhotos(albumId);
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (success) {
                goalAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Export successful! Check the /my_albums_exported_images directory", Toast.LENGTH_LONG).show();
            }
            hideProgressBar();
        }
    }

    private class ExportImageTask extends AsyncTask {

        private Bitmap imageToExport;
        private DatabaseHelper db;
        private boolean success;

        ExportImageTask(Bitmap imageToExport) {
            this.success = false;
            this.imageToExport = imageToExport;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = new DatabaseHelper(getApplicationContext());
            exportImageButton.setVisibility(View.GONE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            success = db.exportSingleImage(imageToExport);
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (success) {
                Toast.makeText(getApplicationContext(), "Export successful! Check the /my_albums_exported_images directory", Toast.LENGTH_LONG).show();
            }
            exportImageButton.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
        if (fullScreenImageDisplayMode) {
            exitFullScreenImageDisplayMode();
        } else {
            super.onBackPressed();
        }
    }
}
