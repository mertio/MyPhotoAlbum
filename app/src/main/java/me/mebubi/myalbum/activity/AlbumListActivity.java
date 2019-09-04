package me.mebubi.myalbum.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import me.mebubi.myalbum.dialog.AddAlbumDialogFragment;
import me.mebubi.myalbum.dialog.EditDeleteAlbumDialogFragment;
import me.mebubi.myalbum.model.AlbumModel;
import me.mebubi.myalbum.model.GoalModel;
import me.mebubi.myalbum.view.AlbumView;

public class AlbumListActivity extends AppCompatActivity implements AlbumView.OnAlbumClickListener, EditDeleteAlbumDialogFragment.OnDeleteAlbumListener, AddAlbumDialogFragment.OnAddAlbumListener {

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

        //new LoadDatabaseTask(true, null).execute();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_albums, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reset_password) {
            prefs.edit().putString("hashOfPassword", "").commit();
            Intent intent = new Intent(AlbumListActivity.this, PasswordActivity.class);
            startActivity(intent);
            finish();
        }

        if (id == R.id.action_export_all) {
            showConfirmDialogForExportAllPhotos(AlbumListActivity.this);
        }

        return super.onOptionsItemSelected(item);
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
                FragmentManager fm = getSupportFragmentManager();
                DialogFragment dialogFragment = AddAlbumDialogFragment.getInstance();
                dialogFragment.setCancelable(false);
                dialogFragment.show(fm, "addAlbumDialog");
            }
        });


        albumRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean loading = false;

                final int visibleThreshold = 5;

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

    public void showConfirmDialogForExportAllPhotos(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Are you sure you want to export all of your photos?");
        // Add the buttons
        builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            new ExportAllPhotosTask().execute();
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
        boolean success = db.updateLastOpenedDateOfAlbum(albumId);
        Intent intent = new Intent(AlbumListActivity.this, PhotoActivity.class);
        intent.putExtra("albumId", albumId);
        startActivity(intent);
        if (success) {

        }
    }

    @Override
    public void onAlbumLongClick(Album album) {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment dialogFragment = EditDeleteAlbumDialogFragment.getInstance(album.getAlbumImage(), album.getAlbumTitle(), album.getAlbumDescription(), album.getAlbumId());
        dialogFragment.setCancelable(false);
        dialogFragment.show(fm, "editDeleteAlbumDialog");
    }

    @Override
    public void onDeleteAlbum(boolean success) {
        // called when ok clicked in dialog
        if (success) {
            albumAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Album deleted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Delete unsuccessful", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onEditAlbum(boolean success) {
        // called when edit button clicked in dialog
        if (success) {
            albumRecyclerView.scrollToPosition(0);
            new LoadDatabaseTask(true, null).execute();
        } else {
            Toast.makeText(getApplicationContext(), "Edit unsuccessful", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAddAlbum(Album album) {
        try {
            new AddAlbumTask(album).execute();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Add unsuccessful", Toast.LENGTH_LONG).show();
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
            success = db.loadAlbumsFromDatabase(albumAdapter.getAlbumList().get(albumAdapter.getItemCount() - 1).getLastOpenedDate(), clearAndLoad);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
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
            db.insertAlbumIntoDatabase(album);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            albumRecyclerView.scrollToPosition(0);
            new LoadDatabaseTask(true, null).execute();
            // hide loading bar
            hideProgressBar();
            db.close();
        }
    }

    private class ExportAllPhotosTask extends AsyncTask {

        private DatabaseHelper db;
        private boolean success;

        ExportAllPhotosTask() {
            this.success = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = new DatabaseHelper(getApplicationContext());
            displayProgressBar();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            success = db.exportAllImagesToGallery();
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (success) {
                albumAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Export successful! Check the /my_albums_exported_images directory", Toast.LENGTH_LONG).show();
            }
            hideProgressBar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        albumRecyclerView.scrollToPosition(0);
        new LoadDatabaseTask(true, null).execute();
    }
}
