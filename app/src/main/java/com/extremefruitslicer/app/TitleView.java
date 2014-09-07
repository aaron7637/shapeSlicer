package com.extremefruitslicer.app;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

/*
 * View to display the Title, and Score
 * Score currently just increments every time we get an update
 * from the model (i.e. a new fruit is added).
 */
public class TitleView extends TextView implements Observer {
    private Model model;

    // Constructor requires model reference
    public TitleView(Context context, Model model) {
        super(context);

        // register with model so that we get updates
        model.addObserver(this);
        this.model = model;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setText("Score: " + model.getScore() + " Lives: " + model.getLives());
    }

    // Update from model
    @Override
    public void update(Observable observable, Object data) {
        invalidate();
    }
}
