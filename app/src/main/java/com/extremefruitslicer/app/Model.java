package com.extremefruitslicer.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

/*
 * Class the contains a list of fruit to display.
 * Follows MVC pattern, with methods to add observers,
 * and notify them when the fruit list changes.
 */
public class Model extends Observable {
    // List of fruit that we want to display
    private ArrayList<Fruit> shapes = new ArrayList<Fruit>();

    private int score;
    private int fruitsLost;
    private final static int MAXFRUITSLOST = 5;

    private enum GameState {
        RUNNING,
        PAUSED,
        ENDED,
    }

    private GameState gameState;

    // Constructor
    Model() {
        shapes.clear();
        score = 0;
        fruitsLost = 0;
        gameState = GameState.ENDED;
    }

    // Model methods
    // You may need to add more methods here, depending on required functionality.
    // For instance, this sample makes to effort to discard fruit from the list.
    public void add(Fruit s) {
        shapes.add(s);
        setChanged();
        notifyObservers();
    }

    public void remove(Fruit s) {
        shapes.remove(s);
    }

    public ArrayList<Fruit> getShapes() {
        return (ArrayList<Fruit>) shapes.clone();
    }

    // MVC methods
    // Basic MVC methods to bind view and model together.
    public void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    // a helper to make it easier to initialize all observers
    public void initObservers() {
        setChanged();
        notifyObservers();
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
        super.deleteObserver(observer);
        setChanged();
        notifyObservers();
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        setChanged();

        notifyObservers();
    }

    public void removeShape(int index) {
        shapes.remove(index);
    }

    public void translateFruits(int maxY) {
        Iterator<Fruit> iter = shapes.iterator();

        while (iter.hasNext()) {
            Fruit f = iter.next();
            f.translateFruit();

            if (f.getFBounds().bottom >= maxY) {
                if (!f.isSplitPiece()) fruitsLost++;
                iter.remove();
            }
        }

        if (fruitsLost >= MAXFRUITSLOST) EndGame();

        setChanged();
        notifyObservers();
    }

    public int getLives() {
        return MAXFRUITSLOST - fruitsLost;
    }

    public int getScore() {
        return score;
    }

    public void incrementScore() {
        score += 1;
        setChanged();
        notifyObservers();
    }

    public void startGame() {
        if (gameState == GameState.ENDED) {
            score = 0;
            fruitsLost = 0;
            shapes.clear();
        }

        gameState = GameState.RUNNING;
        setChanged();
        notifyObservers();
    }

    public void pauseGame() {
        if (gameState == GameState.RUNNING) gameState = GameState.PAUSED;
        setChanged();
        notifyObservers();
    }

    public void resetGame() {
        gameState = GameState.ENDED;
        startGame();
    }

    public void EndGame() {
        gameState = GameState.ENDED;
    }

    public boolean isGameRunning() {
        if (gameState == GameState.RUNNING) return true;
        return false;
    }

    public boolean isGamePaused() {
        if (gameState == GameState.PAUSED) return true;
        return false;
    }

    public boolean isGameEnded() {
        return gameState == GameState.ENDED;
    }
}
