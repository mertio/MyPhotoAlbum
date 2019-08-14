package me.mebubi.myalbum.dialog;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.FileNotFoundException;
import java.io.IOException;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.database.model.Goal;

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

    private Bitmap originalPicToUpload;
    private Bitmap picToUpload;

    final int PICK_IMAGE = 3;
    private Uri picUri;


    public static AddGoalDialogFragment getInstance() {
        AddGoalDialogFragment addGoalDialogFragment = new AddGoalDialogFragment();

        return addGoalDialogFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_create_edit_goal, container, false);


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

                Goal goal = new Goal(originalPicToUpload, picToUpload, goalTitleEditText.getText().toString(), goalDescriptionEditText.getText().toString());
                if(!inputIsValid(goal)) {
                    return;
                }

                onAddGoalListener = (OnAddGoalListener) getContext();
                onAddGoalListener.onGoalAdd(goal);

                dismiss();
            }
        });



        goalImageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

        /*
        if (goal.getImage() == null) {
            Toast.makeText(getContext(), "No image uploaded!", Toast.LENGTH_LONG).show();
            return false;
        }
        */


        return true;
    }


    // https://stackoverflow.com/questions/10773511/how-to-resize-an-image-i-picked-from-the-gallery-in-android
    private Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == PICK_IMAGE){
            if (resultCode == RESULT_OK) {
                picUri = data.getData();

                try {
                    //originalPicToUpload = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), picUri);
                    originalPicToUpload = decodeUri(getContext(), picUri, 600);
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
                    Bitmap largePic = decodeUri(getContext(), picUri, 300);

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
}
