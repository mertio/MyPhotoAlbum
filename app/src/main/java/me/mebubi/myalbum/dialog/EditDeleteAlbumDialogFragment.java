package me.mebubi.myalbum.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.FileNotFoundException;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.database.DatabaseHelper;
import me.mebubi.myalbum.database.DbBitmapUtility;
import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.utility.ImageUtility;

import static android.app.Activity.RESULT_OK;

public class EditDeleteAlbumDialogFragment extends DialogFragment {

    private static final String LOGTAG = "EdDelAlbDialogFragment";

    public interface OnDeleteAlbumListener {
        void onDeleteAlbum(boolean success);
        void onEditAlbum(boolean success);
    }

    private OnDeleteAlbumListener onDeleteAlbumListener;

    private ImageView albumImageView;
    private TextView albumTitleText;
    private TextView albumDescriptionText;
    private TextView photoRemoveText;
    private Button editButton;
    private Button deleteButton;
    private Button cancelButton;

    private int currentAlbumId;
    private Bitmap currentImage;
    private String currentTitle;
    private String currentDescription;

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
        photoRemoveText = v.findViewById(R.id.removeText);
        editButton = v.findViewById(R.id.addButton);
        deleteButton = v.findViewById(R.id.editButton);
        cancelButton = v.findViewById(R.id.cancelButton);

        photoRemoveText.setVisibility(View.GONE);
        if (currentImage != null) {
            albumImageView.setImageBitmap(currentImage);
            photoRemoveText.setVisibility(View.VISIBLE);
        }
        albumTitleText.setText(currentTitle);
        albumDescriptionText.setText(currentDescription);

        albumImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                albumImageView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));

                try {

                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, getResources().getString(R.string.select_photo));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }
                catch(ActivityNotFoundException anfe){
                    //display an error message
                    String errorMessage = getResources().getString(R.string.your_device_doesnt_support_capturing_images);
                    Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        photoRemoveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                photoRemoveText.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));


                currentImage = null;
                albumImageView.setImageResource(R.drawable.ic_photo_add);
            }
        });


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // currentImage updated if photo uploaded
                currentTitle = albumTitleText.getText().toString();
                currentDescription = albumDescriptionText.getText().toString();
                Album album = new Album(currentAlbumId, currentImage, currentTitle, currentDescription, System.currentTimeMillis(), System.currentTimeMillis());
                if(!inputIsValid(album)) {
                    return;
                }
                DatabaseHelper db = new DatabaseHelper(getContext());
                boolean success = db.editAlbumInDatabase(album);
                db.close();
                if (success) {
                    dismiss();
                } else {
                    return;
                }
                onDeleteAlbumListener = (OnDeleteAlbumListener) getContext();
                onDeleteAlbumListener.onEditAlbum(success);
            }
        });

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
        builder.setTitle(getResources().getString(R.string.are_you_sure_delete_album));
        // Add the buttons
        builder.setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                DatabaseHelper db = new DatabaseHelper(getContext());
                boolean success = db.deleteAlbumFromDatabase(albumId);
                db.close();
                dismiss();

                onDeleteAlbumListener = (OnDeleteAlbumListener) getContext();
                onDeleteAlbumListener.onDeleteAlbum(success);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean inputIsValid(Album album) {
        if (album.getAlbumTitle().equals("")) {
            Toast.makeText(getContext(), getResources().getString(R.string.title_is_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == PICK_IMAGE){
            if (resultCode == RESULT_OK) {
                picUri = data.getData();
                CropImage.activity(picUri).setAspectRatio(1,1).setFixAspectRatio(true).start(getActivity());
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                picUri = result.getUri();

                try {
                    Bitmap largePic = ImageUtility.decodeUri(getContext(), picUri, 200);

                    albumImageView.setImageBitmap(largePic);
                    photoRemoveText.setVisibility(View.VISIBLE);
                    currentImage = largePic;


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.dismiss();
        this.show(getFragmentManager(), "editDeleteAlbumDialog");
    }

}
