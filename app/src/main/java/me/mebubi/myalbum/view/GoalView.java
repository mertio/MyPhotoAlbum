package me.mebubi.myalbum.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.database.DatabaseHelper;
import me.mebubi.myalbum.database.model.Goal;
import me.mebubi.myalbum.utility.PostTimeFormat;

public class GoalView extends ConstraintLayout {

    private static final String LOGTAG = "GoalView";

    public interface OnGoalClickListener {
        void onGoalClick(String originalImagePath);
        void onGoalLongClick(boolean success);
    }

    private ImageView goalImage;
    private TextView goalTitle;
    private TextView goalDescription;
    private TextView goalDate;

    private OnGoalClickListener onGoalClickListener;

    public GoalView(Context context) {
        super(context);
        inflate(getContext(), R.layout.goal_item, this);
    }

    public void init(final Goal goal) {

        goalImage = findViewById(R.id.goalImageViewEdit);
        goalTitle = findViewById(R.id.goalTitleEditText);
        goalDescription = findViewById(R.id.goalDescriptionEditText);
        goalDate = findViewById(R.id.goalDateText);

        if (goal.getDescription().equals("")) {
            goalDescription.setVisibility(GONE);
        } else {
            goalDescription.setVisibility(VISIBLE);
        }

        if(goal.getImage() != null) {
            goalImage.setImageBitmap(goal.getImage());
            goalImage.setVisibility(VISIBLE);
        } else {
            goalImage.setImageResource(R.drawable.ic_launcher_background);
            goalImage.setVisibility(GONE);
        }
        goalTitle.setText(goal.getTitle());
        goalDescription.setText(goal.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        goalDate.setText(sdf.format(new Date(goal.getCreationDate())).toLowerCase());

        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));
                showConfirmDialogForGoalDelete((Activity) getContext(), goal);
                return true;
            }
        });

        goalImage.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));
                showConfirmDialogForGoalDelete((Activity) getContext(), goal);
                return true;
            }
        });

        goalImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.click_animation));
                onGoalClickListener = (OnGoalClickListener) getContext();
                onGoalClickListener.onGoalClick(goal.getCreationDate() + ".jpg");
            }
        });

    }

    public void showConfirmDialogForGoalDelete(Activity activity, final Goal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(R.string.are_you_sure_delete_photo));
        // Add the buttons
        builder.setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                DatabaseHelper db = new DatabaseHelper(getContext());
                boolean success = db.deleteGoalFromDatabase(goal);
                db.close();
                onGoalClickListener = (OnGoalClickListener) getContext();
                onGoalClickListener.onGoalLongClick(success);
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




}
