package me.mebubi.myalbum.activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.adapter.AlbumAdapter;
import me.mebubi.myalbum.adapter.GoalAdapter;
import me.mebubi.myalbum.database.DatabaseHelper;
import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.database.model.Goal;
import me.mebubi.myalbum.dialog.EditDeleteAlbumDialogFragment;
import me.mebubi.myalbum.model.AlbumModel;
import me.mebubi.myalbum.model.GoalModel;
import me.mebubi.myalbum.view.AlbumView;

public class AlbumListActivity extends AppCompatActivity implements AlbumView.OnAlbumClickListener, EditDeleteAlbumDialogFragment.OnDeleteAlbumListener {

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

        try {
            new AddAlbumTask(new Album(null, "title", "desc")).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new LoadDatabaseTask(true, null).execute();


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

    @Override
    public void onAlbumClick(int albumId) {

    }

    @Override
    public void onAlbumLongClick(Album album) {
        /*
        // called when ok clicked in dialog
        if (success) {
            albumAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getApplicationContext(), "Delete unsuccessful", Toast.LENGTH_LONG).show();
        }
        */
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment dialogFragment = EditDeleteAlbumDialogFragment.getInstance(album.getAlbumImage(), album.getAlbumTitle(), album.getAlbumDescription(), album.getAlbumId());
        dialogFragment.show(fm, "editdeletealbumdialog");
    }

    @Override
    public void onDeleteAlbum(boolean success) {
        // called when ok clicked in dialog
        if (success) {
            albumAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getApplicationContext(), "Delete unsuccessful", Toast.LENGTH_LONG).show();
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
