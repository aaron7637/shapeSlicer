package com.extremefruitslicer.app;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends Activity {
    private Model model;
    private MainView mainView;
    private TitleView titleView;
    private Button playButton;
    private Button stopButton;
    static final int buttonHeight = 175;
    static final int buttonTextSize = 15;
    private View gameStateView;
    public static Point displaySize;
    public static int difficultyLevel = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setTitle("Shape Slice");

        // save display size
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        // initialize model
        model = new Model();

        // set view
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // create the views and add them to the main activity
        titleView = new TitleView(this.getApplicationContext(), model);
        ViewGroup v1 = (ViewGroup) findViewById(R.id.main_3);
        v1.addView(titleView);
        v1.setMinimumHeight(140);

        playButton = (Button) findViewById(R.id.playButton);
        playButton.setHeight(buttonHeight);
        playButton.setTextSize(buttonTextSize);

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                model.startGame();
            }
        });

        stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setHeight(buttonHeight);
        stopButton.setTextSize(buttonTextSize);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                model.pauseGame();
            }
        });

        gameStateView = (ViewGroup) findViewById(R.id.main_3);
        gameStateView.setMinimumHeight(buttonHeight + 20);

        mainView = new MainView(this.getApplicationContext(), model);
        ViewGroup v2 = (ViewGroup) findViewById(R.id.main_2);
        v2.addView(mainView);

        // notify all views
        model.initObservers();
    }

    @Override
    public void onBackPressed() {
        model.EndGame();
    }
}
