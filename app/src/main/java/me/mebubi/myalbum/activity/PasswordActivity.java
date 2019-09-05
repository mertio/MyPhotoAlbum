package me.mebubi.myalbum.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import me.mebubi.myalbum.R;
import me.mebubi.myalbum.utility.HashFunctions;

public class PasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOGTAG = "PasswordActivity";

    private TextView passwordHeader;

    private Button zeroButton;
    private Button oneButton;
    private Button twoButton;
    private Button threeButton;
    private Button fourButton;
    private Button fiveButton;
    private Button sixButton;
    private Button sevenButton;
    private Button eightButton;
    private Button nineButton;

    private ImageButton clearButton;
    private ImageButton backspaceButton;
    private Button openAlbumButton;

    private EditText hintEditText;
    private Button showHintButton;

    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private ImageView star4;

    StringBuilder passwordBuilder;
    private int currentIndex = 0;

    SharedPreferences prefs;
    private String hashOfPassword;
    private String hintText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        hashOfPassword = prefs.getString("hashOfPassword", "");
        hintText = prefs.getString("hintText", "");

        passwordHeader = findViewById(R.id.passwordHeader);

        zeroButton = findViewById(R.id.zeroButton);
        zeroButton.setOnClickListener(this);

        oneButton = findViewById(R.id.oneButton);
        oneButton.setOnClickListener(this);

        twoButton = findViewById(R.id.twoButton);
        twoButton.setOnClickListener(this);

        threeButton = findViewById(R.id.threeButton);
        threeButton.setOnClickListener(this);

        fourButton = findViewById(R.id.fourButton);
        fourButton.setOnClickListener(this);

        fiveButton = findViewById(R.id.fiveButton);
        fiveButton.setOnClickListener(this);

        sixButton = findViewById(R.id.sixButton);
        sixButton.setOnClickListener(this);

        sevenButton = findViewById(R.id.sevenButton);
        sevenButton.setOnClickListener(this);

        eightButton = findViewById(R.id.eightButton);
        eightButton.setOnClickListener(this);

        nineButton = findViewById(R.id.nineButton);
        nineButton.setOnClickListener(this);


        clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this);

        backspaceButton = findViewById(R.id.backspaceButton);
        backspaceButton.setOnClickListener(this);

        openAlbumButton = findViewById(R.id.openAlbumButton);
        openAlbumButton.setOnClickListener(this);

        hintEditText = findViewById(R.id.hintEditText);

        showHintButton = findViewById(R.id.showHintButton);
        showHintButton.setOnClickListener(this);


        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);

        passwordBuilder = new StringBuilder();


        // set up
        if (hashOfPassword.equals("")) {
            openAlbumButton.setText("Set a password");
            passwordHeader.setText("Set a password");
            hintEditText.setVisibility(View.VISIBLE);
            showHintButton.setVisibility(View.INVISIBLE);
        } else {
            openAlbumButton.setText("Open");
            passwordHeader.setText("Enter your password");
            hintEditText.setVisibility(View.INVISIBLE);
            showHintButton.setVisibility(View.VISIBLE);
        }




    }


    @Override
    public void onClick(View view) {

        view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation));

        if (view.getId() == R.id.clearButton) {
            passwordBuilder = new StringBuilder();
            currentIndex = 0;

            // ui update
            passwordUiUpdate(currentIndex);
        }

        if (view.getId() == R.id.backspaceButton) {
            if(currentIndex > 0) {
                currentIndex--;
                passwordBuilder.replace(currentIndex,currentIndex + 1, "");
            }
            // ui update
            passwordUiUpdate(currentIndex);
        }

        if (view.getId() == R.id.openAlbumButton) {

            // check if password valid
            if (passwordBuilder.length() == 4) {
                if (hashOfPassword.equals("")) {
                    if(hintEditText.getText().toString().length() < 100) { // TODO make 100 into variable
                        prefs.edit().putString("hashOfPassword", HashFunctions.md5(passwordBuilder.toString())).commit();
                        prefs.edit().putString("hintText", hintEditText.getText().toString()).commit();
                        goToAlbums();
                    } else {
                        Toast.makeText(getApplicationContext(), "Hint can be maximum 100 characters long!", Toast.LENGTH_LONG).show();
                        return;
                    }

                } else {
                    if (HashFunctions.md5(passwordBuilder.toString()).equals(hashOfPassword)) {
                        goToAlbums();
                    } else {
                        Toast.makeText(getApplicationContext(), "Wrong password!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "Password must be 4 digits", Toast.LENGTH_LONG).show();
                return;
            }

        }

        if (view.getId() == R.id.zeroButton ||
                view.getId() == R.id.oneButton ||
                view.getId() == R.id.twoButton ||
                view.getId() == R.id.threeButton ||
                view.getId() == R.id.fourButton ||
                view.getId() == R.id.fiveButton ||
                view.getId() == R.id.sixButton ||
                view.getId() == R.id.sevenButton ||
                view.getId() == R.id.eightButton ||
                view.getId() == R.id.nineButton) {

            // add to char array
            if(currentIndex < 4) {
                passwordBuilder.append(((Button) findViewById(view.getId())).getText().toString().charAt(0));
                currentIndex++;
                // ui update
                passwordUiUpdate(currentIndex);
            }
        }

        if (view.getId() == R.id.showHintButton) {
            Toast toast;
            if (hintText.equals("")) {
                toast = Toast.makeText(getApplicationContext(), "No hint to show!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0,250);
                toast.show();
            } else {
                toast = Toast.makeText(getApplicationContext(), hintText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0,250);
                toast.show();
            }
        }

    }

    private void goToAlbums() {
        Intent intent = new Intent(PasswordActivity.this, AlbumListActivity.class);
        startActivity(intent);
        finish();
    }


    private void passwordUiUpdate(int numOfStarsToLightUp) {

        star1.setImageResource(R.drawable.ic_empty_star);
        star2.setImageResource(R.drawable.ic_empty_star);
        star3.setImageResource(R.drawable.ic_empty_star);
        star4.setImageResource(R.drawable.ic_empty_star);

        if (numOfStarsToLightUp >= 1) {
            star1.setImageResource(R.drawable.ic_full_star);
            star1.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation));
        } if (numOfStarsToLightUp >= 2) {
            star2.setImageResource(R.drawable.ic_full_star);
            star2.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation));
        } if (numOfStarsToLightUp >= 3) {
            star3.setImageResource(R.drawable.ic_full_star);
            star3.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation));
        } if (numOfStarsToLightUp >= 4) {
            star4.setImageResource(R.drawable.ic_full_star);
            star4.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation));
        }

    }


}
