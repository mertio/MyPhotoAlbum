package me.mebubi.myalbum.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.database.DatabaseHelper;
import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.database.model.Goal;

public class AlbumView extends ConstraintLayout {

    private static final String LOGTAG = "AlbumView";

    public interface OnAlbumClickListener {
        void onAlbumClick(int albumId);
        void onAlbumLongClick(boolean success);
    }

    private ImageView albumImageView;
    private TextView albumTitleText;
    private TextView albumDescriptionText;

    private OnAlbumClickListener onAlbumClickListener;

    public AlbumView(Context context) {
        super(context);
        inflate(getContext(), R.layout.album_item, this);
    }

    public void init(final Album album) {

        albumImageView = findViewById(R.id.albumImageView);
        albumTitleText = findViewById(R.id.albumTitleText);
        albumDescriptionText = findViewById(R.id.albumDescriptionText);

        if (album.getAlbumDescription().equals("")) {
            albumDescriptionText.setVisibility(GONE);
        } else {
            albumDescriptionText.setVisibility(VISIBLE);
        }

        if(album.getAlbumImage() != null) {
            albumImageView.setImageBitmap(album.getAlbumImage());
            albumImageView.setVisibility(VISIBLE);
        } else {
            albumImageView.setImageResource(R.drawable.ic_launcher_background);
            albumImageView.setVisibility(GONE);
        }
        albumTitleText.setText(album.getAlbumTitle());
        albumDescriptionText.setText(album.getAlbumDescription());


        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showConfirmDialogForAlbumDelete((Activity) getContext(), album);
                return true;
            }
        });

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onAlbumClickListener = (OnAlbumClickListener) getContext();
                onAlbumClickListener.onAlbumClick(album.getAlbumId());
            }
        });

    }


    public void showConfirmDialogForAlbumDelete(Activity activity, final Album album) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Are you sure you want to delete album?");
        // Add the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                DatabaseHelper db = new DatabaseHelper(getContext());
                boolean success = db.deleteAlbumFromDatabase(album);
                Log.d(LOGTAG, "Album deletion success : " + success);
                db.close();
                onAlbumClickListener = (OnAlbumClickListener) getContext();
                onAlbumClickListener.onAlbumLongClick(success);
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




}
