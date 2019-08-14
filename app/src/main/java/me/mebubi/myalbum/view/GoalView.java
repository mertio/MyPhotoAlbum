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
import me.mebubi.myalbum.database.model.Goal;

public class GoalView extends ConstraintLayout {

    private static final String LOGTAG = "GoalView";

    public interface OnGoalClickListener {
        void onGoalClick(String originalImagePath);
        void onGoalLongClick(boolean success);
    }

    private ImageView goalImage;
    private TextView goalTitle;
    private TextView goalDescription;

    private OnGoalClickListener onGoalClickListener;

    public GoalView(Context context) {
        super(context);
        inflate(getContext(), R.layout.goal_item, this);
    }

    public void init(final Goal goal) {

        goalImage = findViewById(R.id.goalImageViewEdit);
        goalTitle = findViewById(R.id.goalTitleEditText);
        goalDescription = findViewById(R.id.goalDescriptionEditText);

        if(goal.getImage() != null) {
            goalImage.setImageBitmap(goal.getImage());
        } else {
            goalImage.setImageResource(R.drawable.ic_launcher_background);
            goalImage.setVisibility(GONE);
        }
        goalTitle.setText(goal.getTitle());
        goalDescription.setText(goal.getDescription());

        goalImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOGTAG, "Clicked on goal");
                onGoalClickListener = (OnGoalClickListener) getContext();
                onGoalClickListener.onGoalClick(goal.getCreationDate() + ".jpg");
            }
        });

        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showConfirmDialogForGoalDelete((Activity) getContext(), goal);
                return true;
            }
        });

    }

    public void showConfirmDialogForGoalDelete(Activity activity, final Goal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Are you sure you want to delete goal?");
        // Add the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                DatabaseHelper db = new DatabaseHelper(getContext());
                boolean success = db.deleteGoalFromDatabase(goal);
                Log.d(LOGTAG, "Goal deletion success : " + success);
                db.close();
                onGoalClickListener = (OnGoalClickListener) getContext();
                onGoalClickListener.onGoalLongClick(success);
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
