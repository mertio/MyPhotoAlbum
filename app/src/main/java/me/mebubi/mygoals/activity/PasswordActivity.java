package me.mebubi.mygoals.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import me.mebubi.mygoals.R;

public class PasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOGTAG = "PasswordActivity";

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

    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private ImageView star4;

    private char[] password;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

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


        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);

        password = new char[4];

    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.clearButton) {
            password = new char[4];
            currentIndex = 0;

            // ui update
            passwordUiUpdate(currentIndex);
        }

        if (view.getId() == R.id.backspaceButton) {
            if(currentIndex > 0) {
                currentIndex--;
            }
            // ui update
            passwordUiUpdate(currentIndex);
        }

        if (view.getId() == R.id.openAlbumButton) {
            Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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
                password[currentIndex] = ((Button) findViewById(view.getId())).getText().toString().charAt(0);
                currentIndex++;
                // ui update
                passwordUiUpdate(currentIndex);
            }
        }

        Log.d(LOGTAG, "On click method entered, current index: " + currentIndex);


    }


    private void passwordUiUpdate(int numOfStarsToLightUp) {

        star1.setImageResource(R.drawable.ic_empty_star);
        star2.setImageResource(R.drawable.ic_empty_star);
        star3.setImageResource(R.drawable.ic_empty_star);
        star4.setImageResource(R.drawable.ic_empty_star);

        if (numOfStarsToLightUp >= 1) {
            star1.setImageResource(R.drawable.ic_full_star);
        } if (numOfStarsToLightUp >= 2) {
            star2.setImageResource(R.drawable.ic_full_star);
        } if (numOfStarsToLightUp >= 3) {
            star3.setImageResource(R.drawable.ic_full_star);
        } if (numOfStarsToLightUp >= 4) {
            star4.setImageResource(R.drawable.ic_full_star);
        }

        Log.d(LOGTAG, "Current password : " + password[0] + password[1] + password[2] + password[3]);

    }


}
