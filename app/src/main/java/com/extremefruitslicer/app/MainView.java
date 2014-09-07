package com.extremefruitslicer.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/*  
 * View of the main game area.
 * Displays pieces of fruit, and allows players to slice them.
 */
public class MainView extends View implements Observer {
    private final Model model;
    private final MouseDrag drag = new MouseDrag();
    private Handler handler = new Handler();
    private static long clockTicks = 0;
    private static final int tickTime = 25;
    private ArrayList<Fruit> shapeLibrary = new ArrayList<Fruit>();
    private Random randomGenerator = new Random();
    private Context context;

    private Runnable runnable = new Runnable() {
        public void run() {
            if (model.isGameEnded()) {
                Intent intent = new Intent(context, StartScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("score", model.getScore());
                context.startActivity(intent);
                return;
            } else if (!model.isGameRunning()) {
                handler.postDelayed(this, tickTime);
                return;
            }

            handler.postDelayed(this, tickTime);

            clockTicks++;
            if (clockTicks % 30 == 0) {
                generateRandomFruit();
            }

            model.translateFruits(MainView.this.getHeight());
        }
    };

    // Constructor
    MainView(Context context, Model m) {
        super(context);
        this.context = context;
        // register this view with the model
        model = m;
        model.addObserver(this);

        Path cucumber = new Path();
        cucumber.addOval(new RectF(0, 0, 120, 180), Path.Direction.CCW);
        Fruit f1 = new Fruit(cucumber, context);
        f1.setFillColor(Color.MAGENTA);
        shapeLibrary.add(f1);

        Path circle = new Path();
        circle.addCircle(0, 0, 100, Path.Direction.CCW);
        Fruit f2 = new Fruit(circle, context);
        f2.setFillColor(Color.rgb(0xFF, 0x99, 0x12));
        shapeLibrary.add(f2);


/*
        Path banana = new Path();
        banana.addArc(new RectF(0, 0, 200, 200), 0, 90);
        Fruit f3 = new Fruit(banana, context);
        f3.setFillColor(Color.YELLOW);
        shapeLibrary.add(f3);
*/

        Path square = new Path();
        square.addRect(0, 0, 180, 180, Path.Direction.CCW);
        Fruit f4 = new Fruit(square, context);
        f4.setFillColor(Color.BLUE);
        shapeLibrary.add(f4);

        model.startGame();
        handler.postDelayed(runnable, tickTime * 50);

        // add controller
        // capture touch movement, and determine if we intersect a shape
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        drag.start(event.getX(), event.getY());
                        break;

                    case MotionEvent.ACTION_UP:
                        drag.stop(event.getX(), event.getY());

                        if (!model.isGameRunning()) {
                            break;
                        }

                        // find intersected shapes
                        Iterator<Fruit> i = model.getShapes().iterator();
                        while (i.hasNext()) {
                            Fruit s = i.next();

                            if (s.intersects(drag.getStart(), drag.getEnd())) {
                                try {
                                    Fruit[] newFruits = s.split(drag.getStart(), drag.getEnd());
                                    model.remove(s);
                                    model.add(newFruits[0]);
                                    model.add(newFruits[1]);
                                    model.incrementScore();

                                } catch (Exception ex) {
                                    Log.e("fruit_ninja", "Error: " + ex.getMessage());
                                }
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

    void generateRandomFruit() {
        int randomLocation = randomGenerator.nextInt(MainActivity.displaySize.x - 200);

        Fruit fruitSelection = (Fruit) shapeLibrary.get(randomGenerator.nextInt(shapeLibrary.size())).clone();

        int xVelocity = randomGenerator.nextInt();
        int yVelocity = randomGenerator.nextInt();

        xVelocity = xVelocity % 35 + 100;
        yVelocity = -1 * (int) ((float) (yVelocity % 200 + 1100) * ((float) 1 + (float) MainActivity.difficultyLevel / 25.0));

        fruitSelection.setVelocity(xVelocity, yVelocity);
        fruitSelection.translate(randomLocation, this.getHeight() - 250);

        Fruit.incrementAcceleration();

        model.add(fruitSelection);
    }

    // inner class to track mouse drag
    // a better solution *might* be to dynamically track touch movement
    // in the controller above
    class MouseDrag {
        private float startx, starty;
        private float endx, endy;

        protected PointF getStart() {
            return new PointF(startx, starty);
        }

        protected PointF getEnd() {
            return new PointF(endx, endy);
        }

        protected void start(float x, float y) {
            this.startx = x;
            this.starty = y;
        }

        protected void stop(float x, float y) {
            this.endx = x;
            this.endy = y;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw background
        setBackgroundColor(Color.WHITE);

        // draw all pieces of fruit
        for (Fruit s : model.getShapes()) {
            s.draw(canvas);
        }
        getResources();
    }

    @Override
    public void update(Observable observable, Object data) {
        invalidate();
    }
}
