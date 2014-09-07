package com.extremefruitslicer.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class StartScreenActivity extends Activity {
    private Button startGame;
    private SeekBar difficultySeekBar;
    private TextView previousScoreTextView;
    private TextView highscoreTextView;
    private int previousScore;
    private int highScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        Intent intent = getIntent();
        previousScore = intent.getIntExtra("score", -1);

        //getting preferences
        SharedPreferences prefs = this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0); //0 is the default value
        highScore = Math.max(highScore, previousScore);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("high_score", highScore);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        difficultySeekBar = (SeekBar) findViewById(R.id.difficultySeekBar);
        startGame = (Button) findViewById(R.id.startGameButton);
        previousScoreTextView = (TextView) findViewById(R.id.scoreTextView);
        highscoreTextView = (TextView) findViewById(R.id.topScoreTextView);

        if (previousScore == -1) previousScoreTextView.setText("");
        else previousScoreTextView.setText("Game over! Your Score is " + previousScore + "!!!");

        highscoreTextView.setText("High Score: " + highScore);

        difficultySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.difficultyLevel = progress;
            }
        });

        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
                StartScreenActivity.this.startActivity(intent);
            }
        });
    }
}
