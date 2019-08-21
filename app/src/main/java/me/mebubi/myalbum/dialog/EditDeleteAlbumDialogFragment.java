package me.mebubi.myalbum.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.database.DatabaseHelper;
import me.mebubi.myalbum.database.DbBitmapUtility;
import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.database.model.Goal;
import me.mebubi.myalbum.view.AlbumView;

public class EditDeleteAlbumDialogFragment extends DialogFragment {

    private static final String LOGTAG = "EdDelAlbDialogFragment";

    public interface OnDeleteAlbumListener {
        void onDeleteAlbum(boolean success);
    }

    private OnDeleteAlbumListener onDeleteAlbumListener;

    private ImageView albumImageView;
    private TextView albumTitleText;
    private TextView albumDescriptionText;
    private Button editButton;
    private Button deleteButton;
    private Button cancelButton;

    private int currentAlbumId;
    private Bitmap currentImage;
    private String currentTitle;
    private String currentDescription;

    private Bitmap picToUpload;
    final int PICK_IMAGE = 3;
    private Uri picUri;


    public static EditDeleteAlbumDialogFragment getInstance(Bitmap image, String title, String description, int albumId) {
        EditDeleteAlbumDialogFragment editDeleteAlbumDialogFragment = new EditDeleteAlbumDialogFragment();
        Bundle args = new Bundle();
        args.putByteArray("image", DbBitmapUtility.getBytes(image));
        args.putString("title", title);
        args.putString("description", description);
        args.putInt("albumId", albumId);
        editDeleteAlbumDialogFragment.setArguments(args);
        return editDeleteAlbumDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            v = inflater.inflate(R.layout.fragment_edit_delete_album_landscape, container, false);
        } else {
            v = inflater.inflate(R.layout.fragment_edit_delete_album, container, false);
        }

        currentImage = DbBitmapUtility.getImage(getArguments().getByteArray("image"));
        currentTitle = getArguments().getString("title");
        currentDescription = getArguments().getString("description");
        currentAlbumId = getArguments().getInt("albumId");

        albumImageView = v.findViewById(R.id.goalImageViewEdit);
        albumTitleText = v.findViewById(R.id.goalTitleEditText);
        albumDescriptionText = v.findViewById(R.id.goalDescriptionEditText);
        editButton = v.findViewById(R.id.addButton);
        deleteButton = v.findViewById(R.id.editButton);
        cancelButton = v.findViewById(R.id.cancelButton);

        if (currentImage != null) {
            albumImageView.setImageBitmap(currentImage);
        }
        albumTitleText.setText(currentTitle);
        albumDescriptionText.setText(currentDescription);


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmDialogForAlbumDelete((Activity) getContext(), currentAlbumId);
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return v;
    }









    public void showConfirmDialogForAlbumDelete(Activity activity, final int albumId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Are you sure you want to delete album?");
        // Add the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                DatabaseHelper db = new DatabaseHelper(getContext());
                boolean success = db.deleteAlbumFromDatabase(albumId);
                Log.d(LOGTAG, "Album deletion success : " + success);
                db.close();
                onDeleteAlbumListener = (OnDeleteAlbumListener) getContext();
                onDeleteAlbumListener.onDeleteAlbum(success);
                dismiss();
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
