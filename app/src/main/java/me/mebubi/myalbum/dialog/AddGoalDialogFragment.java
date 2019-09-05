package me.mebubi.myalbum.dialog;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import me.mebubi.myalbum.database.model.Goal;
import me.mebubi.myalbum.utility.ImageUtility;

import static android.app.Activity.RESULT_OK;

public class AddGoalDialogFragment extends DialogFragment {

    private static final String LOGTAG = "AddGoalDialogFragment";

    public interface OnAddGoalListener {
        void onGoalAdd(Goal goal);
    }

    private OnAddGoalListener onAddGoalListener;

    private ImageView goalImageViewEdit;
    private EditText goalTitleEditText;
    private EditText goalDescriptionEditText;
    private Button addButton;
    private Button cancelButton;

    private int albumId;

    private Bitmap originalPicToUpload;
    private Bitmap picToUpload;
    final int PICK_IMAGE = 3;
    private Uri picUri;


    public static AddGoalDialogFragment getInstance(int albumId) {
        AddGoalDialogFragment addGoalDialogFragment = new AddGoalDialogFragment();
        Bundle args = new Bundle();
        args.putInt("albumId", albumId);
        addGoalDialogFragment.setArguments(args);
        return addGoalDialogFragment;
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

        albumId = getArguments().getInt("albumId");

        goalImageViewEdit = v.findViewById(R.id.goalImageViewEdit);
        goalTitleEditText = v.findViewById(R.id.goalTitleEditText);
        goalDescriptionEditText = v.findViewById(R.id.goalDescriptionEditText);
        addButton = v.findViewById(R.id.addButton);
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

                Goal goal = new Goal(originalPicToUpload, picToUpload, goalTitleEditText.getText().toString(), goalDescriptionEditText.getText().toString(), albumId);
                if(!inputIsValid(goal)) {
                    return;
                }

                onAddGoalListener = (OnAddGoalListener) getContext();
                onAddGoalListener.onGoalAdd(goal);

                dismiss();
            }
        });

        if (picToUpload != null) {
            goalImageViewEdit.setImageBitmap(picToUpload);
        }


        goalImageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goalImageViewEdit.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));


                try {

                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }
                catch(ActivityNotFoundException anfe){
                    //display an error message
                    String errorMessage = "Whoops - your device doesn't support capturing images!";
                    Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }


            }
        });

        return v;
    }


    private boolean inputIsValid(Goal goal) {
        if (goal.getTitle().equals("")) {
            Toast.makeText(getContext(), "Title is empty", Toast.LENGTH_LONG).show();
            return false;
        }
        if (goal.getImage() == null) {
            Toast.makeText(getContext(), "No image uploaded", Toast.LENGTH_LONG).show();
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

                try {
                    //originalPicToUpload = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), picUri);
                    originalPicToUpload = ImageUtility.decodeUri(getContext(), picUri, 600);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                CropImage.activity(picUri).setAspectRatio(1,1).setFixAspectRatio(true).start(getActivity());

            }
        }

        Log.d(LOGTAG, "An activity has returned a result...");

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                picUri = result.getUri();

                try {
                    Bitmap largePic = ImageUtility.decodeUri(getContext(), picUri, 300);

                    Log.d(LOGTAG, "Image upload method : image : " + largePic);
                    // TODO upload them as jpg
                    goalImageViewEdit.setImageBitmap(largePic);
                    picToUpload = largePic;


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(LOGTAG, "Error occured in picture crop");
            }
        }

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.dismiss();
        this.show(getFragmentManager(), "addGoalDialog");
    }
}
