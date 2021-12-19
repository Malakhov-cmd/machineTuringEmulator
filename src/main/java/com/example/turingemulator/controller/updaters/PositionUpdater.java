package com.example.turingemulator.controller.updaters;

public class PositionUpdater {
    volatile int currentLentColumn;
    volatile int currentRowCondition;
    volatile int currentColumnCondition;

    volatile boolean isFinished;

    public PositionUpdater(int currentLentColumn, int currentRowCondition, int currentColumnCondition) {
        this.currentLentColumn = currentLentColumn;
        this.currentRowCondition = currentRowCondition;
        this.currentColumnCondition = currentColumnCondition;

        isFinished = false;
    }

    public int getCurrentLentColumn() {
        return currentLentColumn;
    }

    public void setCurrentLentColumn(int currentLentColumn) {
        this.currentLentColumn = currentLentColumn;
    }

    public int getCurrentRowCondition() {
        return currentRowCondition;
    }

    public void setCurrentRowCondition(int currentRowCondition) {
        this.currentRowCondition = currentRowCondition;
    }

    public int getCurrentColumnCondition() {
        return currentColumnCondition;
    }

    public void setCurrentColumnCondition(int currentColumnCondition) {
        this.currentColumnCondition = currentColumnCondition;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    @Override
    public String toString() {
        return "PositionUpdater{" +
                "currentLentColumn=" + currentLentColumn +
                ", currentRowCondition=" + currentRowCondition +
                ", currentColumnCondition=" + currentColumnCondition +
                '}';
    }
}
