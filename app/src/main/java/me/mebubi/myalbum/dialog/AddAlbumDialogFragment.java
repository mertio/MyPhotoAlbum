package me.mebubi.myalbum.dialog;

import android.content.ActivityNotFoundException;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.FileNotFoundException;
import java.io.IOException;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.database.model.Album;
import me.mebubi.myalbum.database.model.Goal;
import me.mebubi.myalbum.utility.ImageUtility;

import static android.app.Activity.RESULT_OK;

public class AddAlbumDialogFragment extends DialogFragment {

    private static final String LOGTAG = "AddAlbumDialogFragment";

    public interface OnAddAlbumListener {
        void onAddAlbum(Album album);
    }

    private OnAddAlbumListener onAddAlbumListener;

    private ImageView albumImageViewEdit;
    private EditText albumTitleEditText;
    private EditText albumDescriptionEditText;
    private Button addButton;
    private Button cancelButton;

    private Bitmap picToUpload;
    final int PICK_IMAGE = 4;
    private Uri picUri;

    public static AddAlbumDialogFragment getInstance() {
        AddAlbumDialogFragment addAlbumDialogFragment = new AddAlbumDialogFragment();
        return addAlbumDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            v = inflater.inflate(R.layout.fragment_add_goal_landscape, container, false);
        } else {
            v = inflater.inflate(R.layout.fragment_add_goal, container, false);
        }

        albumImageViewEdit = v.findViewById(R.id.goalImageViewEdit);
        albumTitleEditText = v.findViewById(R.id.goalTitleEditText);
        albumTitleEditText.setHint(getResources().getString(R.string.album_title));
        albumDescriptionEditText = v.findViewById(R.id.goalDescriptionEditText);
        albumDescriptionEditText.setHint(getResources().getString(R.string.album_description));
        addButton = v.findViewById(R.id.addButton);
        addButton.setText(getResources().getString(R.string.add_album));
        cancelButton = v.findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Album album = new Album(picToUpload, albumTitleEditText.getText().toString(), albumDescriptionEditText.getText().toString());
                if(!inputIsValid(album)) {
                    return;
                }

                onAddAlbumListener = (OnAddAlbumListener) getContext();
                onAddAlbumListener.onAddAlbum(album);

                dismiss();
            }
        });

        if (picToUpload != null) {
            albumImageViewEdit.setImageBitmap(picToUpload);
        }


        albumImageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                albumImageViewEdit.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));


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

        return v;
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

                    albumImageViewEdit.setImageBitmap(largePic);
                    picToUpload = largePic;


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
        this.show(getFragmentManager(), "addAlbumDialog");
    }





}
