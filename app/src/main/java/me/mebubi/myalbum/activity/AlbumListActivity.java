package me.mebubi.myalbum.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import me.mebubi.myalbum.R;

public class AlbumListActivity extends AppCompatActivity {

    private FloatingActionButton addAlbumButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);
        initialize();
        setOnClickMethods();

    }

    private void initialize() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addAlbumButton = findViewById(R.id.addAlbumActionButton);
    }

    private void setOnClickMethods() {
        addAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

}
