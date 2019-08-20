package me.mebubi.myalbum.activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.concurrent.ExecutionException;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.adapter.AlbumAdapter;
import me.mebubi.myalbum.adapter.GoalAdapter;
import me.mebubi.myalbum.database.DatabaseHelper;
import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.database.model.Goal;
import me.mebubi.myalbum.model.AlbumModel;
import me.mebubi.myalbum.model.GoalModel;

public class AlbumListActivity extends AppCompatActivity {

    private static final String LOGTAG = "AlbumListActivity";

    private FloatingActionButton addAlbumButton;
    private RecyclerView albumRecyclerView;
    private AlbumAdapter albumAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar albumProgressBar;
    private Toolbar toolbar;

    private SharedPreferences prefs;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);
        initialize();
        setOnClickMethods();


        // TEST
        try {
            new AddAlbumTask(new Album(null, "Test Album","description")).execute().get();
            new AddAlbumTask(new Album(null, "Test Album","description")).execute().get();
            new AddAlbumTask(new Album(null, "Test Album","description")).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void initialize() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addAlbumButton = findViewById(R.id.addAlbumActionButton);
        albumRecyclerView = findViewById(R.id.albumRecyclerView);
        albumProgressBar = findViewById(R.id.albumProgressBar);

        // db helper and fetch sharedprefs
        db = new DatabaseHelper(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // recyclerview initialization
        albumAdapter = new AlbumAdapter(AlbumModel.getAlbums(), this);
        albumRecyclerView.setAdapter(albumAdapter);
        layoutManager = new GridLayoutManager(this, 1);
        albumRecyclerView.setLayoutManager(layoutManager);
        albumAdapter.notifyDataSetChanged();

        hideProgressBar();

    }

    private void setOnClickMethods() {
        addAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        albumRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean loading = false;

                final int visibleThreshold = 4;

                GridLayoutManager layoutManager = (GridLayoutManager)albumRecyclerView.getLayoutManager();
                int lastItem  = layoutManager.findLastCompletelyVisibleItemPosition();
                int currentTotalCount = albumAdapter.getItemCount();

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
        albumRecyclerView.setVisibility(View.GONE);
        addAlbumButton.hide();
        albumProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        albumRecyclerView.setVisibility(View.VISIBLE);
        addAlbumButton.show();
        albumProgressBar.setVisibility(View.GONE);
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
            if (albumAdapter.getAlbumList().isEmpty()) {
                success = db.loadAlbumsFromDatabase(0, clearAndLoad);
                return null;
            }
            success = db.loadAlbumsFromDatabase(albumAdapter.getAlbumList().get(albumAdapter.getItemCount() - 1).getCreationDate(), clearAndLoad);
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (success) {
                Log.d(LOGTAG, "Album list: " + albumAdapter.getAlbumList());
                albumAdapter.notifyDataSetChanged();
            }
            lock = false;
        }
    }


    private class AddAlbumTask extends AsyncTask {

        private DatabaseHelper db;
        private Album album;

        AddAlbumTask(Album album) {
            this.album = album;
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
            Log.d(LOGTAG, "Album to add : " + album);
            db.insertAlbumIntoDatabase(album);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            albumAdapter.notifyDataSetChanged();
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
