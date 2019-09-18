package me.mebubi.myalbum.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
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
        void onAlbumLongClick(Album album);
    }

    private ImageView albumImageView;
    private CardView albumCardView;
    private TextView albumTitleText;
    private TextView albumDescriptionText;

    private OnAlbumClickListener onAlbumClickListener;

    private Context context;

    public AlbumView(Context context) {
        super(context);
        this.context = context;
        inflate(getContext(), R.layout.album_item, this);
    }

    public void init(final Album album) {

        albumImageView = findViewById(R.id.albumImageView);
        albumCardView = findViewById(R.id.albumCardView);
        albumTitleText = findViewById(R.id.albumTitleText);
        albumDescriptionText = findViewById(R.id.albumDescriptionText);

        if (album.getAlbumDescription().equals("")) {
            albumDescriptionText.setVisibility(GONE);
        } else {
            albumDescriptionText.setVisibility(VISIBLE);
        }

        if(album.getAlbumImage() != null) {
            albumImageView.setImageBitmap(album.getAlbumImage());
        } else {
            albumImageView.setImageResource(R.drawable.ic_album_placeholder);
        }
        albumTitleText.setText(album.getAlbumTitle());
        albumDescriptionText.setText(album.getAlbumDescription());


        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));
                //showConfirmDialogForAlbumDelete((Activity) getContext(), album);
                onAlbumClickListener = (OnAlbumClickListener) getContext();
                onAlbumClickListener.onAlbumLongClick(album);
                return true;
            }
        });

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));
                onAlbumClickListener = (OnAlbumClickListener) getContext();
                onAlbumClickListener.onAlbumClick(album.getAlbumId());
            }
        });

    }




}
