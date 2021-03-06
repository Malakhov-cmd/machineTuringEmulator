package com.example.turingemulator.controller.service;

import com.example.turingemulator.MainView;
import com.example.turingemulator.controller.MainController;
import com.example.turingemulator.data.RowCondition;
import com.example.turingemulator.data.Rule;
import com.example.turingemulator.view.Trace;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AlgorithmsService {
    private MainController mainController;
    private MainView mainView;
    private boolean error = false;

    private Task<Void> task;

    public AlgorithmsService(MainController mainController, MainView mainView) {
        this.mainController = mainController;
        this.mainView = mainView;
    }

    public void onStepMovement() {
        Platform.runLater(() -> {
            coreAlgorithm();
            mainController.getCurrentPosition().setFinished(false);
        });
    }

    public void onStepMovementAuto() {
        Platform.runLater(() -> {
            error = false;
            while (!mainController.getCurrentPosition().isFinished() && !error) {
                coreAlgorithm();
            }
            mainController.getCurrentPosition().setFinished(false);
        });
        mainView.unColorized();
    }

    public void startDraw() {
        if (task != null && task.isRunning()) {
            task.cancel();
        }

        task = new StepTaskService(mainView.delay * 1000, mainController);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        mainController.getCurrentPosition().setFinished(false);

        mainView.stepForward.disableProperty().bind(task.runningProperty());
        mainView.stop.disableProperty().bind(task.runningProperty().not());
    }

    public void cancelDraw() {
        if (task != null)
            task.cancel();
        mainController.getCurrentPosition().setCurrentLentColumn(0);
        mainController.getCurrentPosition().setCurrentRowCondition(0);
        mainController.getCurrentPosition().setCurrentColumnCondition(1);
        mainController.getCurrentPosition().setFinished(false);
    }

    private void coreAlgorithm() {
        try {
            String lentDataBefore = mainController.getLentData().getAllSymbols();

            //???????????????? ?????????????? ??????????????
            Rule rowFrom = mainController.getRowConditions().get(mainController.getCurrentPosition().getCurrentRowCondition())
                    .getListRules().get(mainController.getCurrentPosition().getCurrentColumnCondition() - 1);

            mainView.unColorized();

            //???? ?????????? ?????????????????? ??????????????
            String conditionTo = rowFrom.getConditionTo();
            int newConditionInt;

            if (conditionTo.length() == 2) {
                newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 1)) + 1;
            } else {
                newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 2)) + 1;
            }
            //???????????????? ???? ???????????????????????????? ??????????????????
            if (newConditionInt < 0 || newConditionInt > 20) {
                error = true;

                Alert alert = new Alert(Alert.AlertType.ERROR, "???????????????? ?????????? ??????????????????. ???????? ???????????? ????????????" +
                        "\n ???????????? ???????? ?? ?????????????????? ???? 0 ???? 20");
                alert.showAndWait();

                if (mainView.saveStackTraceCheckBox.isSelected()) {
                    Date date = new Date();
                    mainController.currentLentState.append(mainController.lentStateCounter).append(" ???????????????? ?????????? ?????????????????? ?? ").append(date).append("\n");
                    mainController.lentStateCounter++;
                    Trace.getStringBuilder(mainController.currentLentState);
                }
            } else {
                //???????????????? ???? ???????????????????? ??????????????????
                if (newConditionInt > mainController.getRowConditions().get(0).getEnderIndex() ) {
                    error = true;

                    if (mainView.saveStackTraceCheckBox.isSelected()) {
                        Date date = new Date();
                        mainController.currentLentState.append(mainController.lentStateCounter).append(" ???????????? ???? ???????????????????????????? ?????????????????? ?? ").append(date).append("\n");
                        mainController.lentStateCounter++;
                        Trace.getStringBuilder(mainController.currentLentState);
                    }

                    Alert alert = new Alert(Alert.AlertType.ERROR, "???????????? ???? ???????????????????????????? ??????????????????");
                    alert.showAndWait();
                    //}
                } else {
                    mainController.getCurrentPosition().setCurrentColumnCondition(newConditionInt);

                    String symbolChangeTo = rowFrom.getSymbolChangeTo();

                    mainController.getLentData().getListLentData().set(mainController.getCurrentPosition().getCurrentLentColumn(), symbolChangeTo);

                    mainView.currentLentTable.filler(mainController.getLentData());
                    mainView.currentLentTable.update();

                    if (mainView.saveStackTraceCheckBox.isSelected()) {
                        mainController.currentLentState.append(mainController.lentStateCounter).append(mainController.getLentData().toString()).append("\n");
                        mainController.lentStateCounter++;
                        Trace.getStringBuilder(mainController.currentLentState);
                    }

                    //???????? ?????????????????????? ??????????????
                    String moveTo = rowFrom.getMoveTo();
                    if (moveTo.equals("R")) {
                        mainController.getCurrentPosition().setCurrentLentColumn(
                                mainController.getCurrentPosition().getCurrentLentColumn() + 1
                        );
                    } else {
                        if (moveTo.equals("L")) {
                            mainController.getCurrentPosition().setCurrentLentColumn(
                                    mainController.getCurrentPosition().getCurrentLentColumn() - 1
                            );
                        }
                    }

                    //?????????????????? ????????????
                    /*???????????????? ???? ?????????? ???? ?????????????? ??????????*/
                    String nextRow = null;
                    try {
                        nextRow = mainController.getLentData().getListLentData().get(mainController.getCurrentPosition().getCurrentLentColumn());
                    } catch (IndexOutOfBoundsException e) {
                        error = true;

                        mainController.getCurrentPosition().setCurrentLentColumn(0);
                        mainController.getCurrentPosition().setCurrentRowCondition(0);
                        mainController.getCurrentPosition().setCurrentColumnCondition(1);

                        Alert alert = new Alert(Alert.AlertType.ERROR, "?????????????????? ?????????? ?????????????????? ???? ?????????????????? ?????????????????????? \n" +
                                " ????????????????");
                        alert.showAndWait();

                        if (mainView.saveStackTraceCheckBox.isSelected()) {
                            Date date = new Date();
                            mainController.currentLentState.append(mainController.lentStateCounter).append(" ?????????????????? ?????????? ?????????????????? ???? ?????????????????? ?????????????????????? ???????????????? ?? ").append(date).append("\n");
                            mainController.lentStateCounter++;
                            Trace.getStringBuilder(mainController.currentLentState);
                        }

                        mainView.unColorized();
                    }
                    int nextRowInt = -1;
                    for (int i = 0; i < mainController.getRowConditions().size(); i++) {
                        if (mainController.getRowConditions().get(i).getSymbolLine().equals(nextRow)) {
                            mainController.getCurrentPosition().setCurrentRowCondition(i);
                            nextRowInt = i;
                        }
                    }

                    //???????????????? ???? ???? ???????????? ???? ?????????????????? ??????????????
                    Rule nextRule = null;
                    try {
                        nextRule = mainController.getRowConditions().get(nextRowInt).getListRules().get(newConditionInt - 1);
                    } catch (IndexOutOfBoundsException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "?????????? ???? ?????????? ???????????????????? ?????????? ????????");
                        alert.showAndWait();
                    }

                    boolean full = true;
                    if (nextRule.getConditionTo().equals(" ")
                            || nextRule.getSymbolChangeTo().equals(" ")
                            || nextRule.getMoveTo().equals(" ")) {
                        full = false;
                    }
                    if (newConditionInt - 1 == mainController.getRowConditions().get(0).getEnderIndex() - 1) {
                        mainController.getCurrentPosition().setCurrentLentColumn(0);
                        mainController.getCurrentPosition().setCurrentRowCondition(0);
                        mainController.getCurrentPosition().setCurrentColumnCondition(1);
                        mainController.getCurrentPosition().setFinished(true);

                        if (mainController.getView().saveStackTraceCheckBox.isSelected()) {
                            Date date = new Date();
                            mainController.currentLentState.append(mainController.lentStateCounter).append(" ???????????????? ?????????????? ???????????????? ?? ").append(date).append("\n");
                            Trace.getStringBuilder(mainController.currentLentState);

                            mainController.lentStateCounter = 0;
                            //?????????? ????????????????
                            Trace.getEnded(true);
                        }

                        boolean isAlgorithmContainEquality = false;
                        for (RowCondition condition:
                             mainController.getRowConditions()) {
                            if (condition.getSymbolLine().equals("=")) {
                                isAlgorithmContainEquality = true;
                                break;
                            }
                        }

                        Alert alert;
                        if (isAlgorithmContainEquality) {
                            alert = new Alert(Alert.AlertType.INFORMATION, "???????????????? ?????????????? ???????????????? " + "\n" +
                                    "??????????: " + getParseResult());
                        } else {
                            alert = new Alert(Alert.AlertType.INFORMATION, "???????????????? ?????????????? ???????????????? ");
                        }
                        alert.showAndWait();
                    } else {
                        if (!full) {
                            error = true;

                            mainController.getCurrentPosition().setCurrentLentColumn(0);
                            mainController.getCurrentPosition().setCurrentRowCondition(0);
                            mainController.getCurrentPosition().setCurrentColumnCondition(1);

                            Alert alert = new Alert(Alert.AlertType.ERROR, "?????????????????? ?????????????? ???? ??????????????????");
                            alert.showAndWait();

                            if (mainView.saveStackTraceCheckBox.isSelected()) {
                                Date date = new Date();
                                mainController.currentLentState.append(mainController.lentStateCounter).append(" ?????????????????? ?????????????? ???????? ???? ?????????????????? ?? ").append(date).append("\n");
                                mainController.lentStateCounter++;
                                Trace.getStringBuilder(mainController.currentLentState);
                            }

                            mainView.unColorized();
                        }
                    }
                    mainView.colorized();
                }
            }
            String lentDataAfter = mainController.getLentData().getAllSymbols();
            lentActuatorGraphical(lentDataBefore, lentDataAfter);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "??????-???? ?????????? ???? ??????." +
                    "\n ???????????????????? ???????????????? ???????????????????? ?? ????????????????????");
            alert.showAndWait();
        }
    }

    private void lentActuatorGraphical(String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            for (int i = 0; i < 200; i++) {
                int tmpOld = oldValue.charAt(i);
                int tmpNew = newValue.charAt(i);
                if (tmpNew != tmpOld && i > 100) {
                    mainView.listLentColumns.get(mainController.getLentData().getEnder()).setVisible(true);
                    mainController.getLentData().setEnder(mainController.getLentData().getEnder() + 1);

                    mainView.currentLentTable.filler(mainController.getLentData());
                    mainView.currentLentTable.update();
                }
            }
        }
    }

    private int getParseResult() {
        int counter;
        List<String> lentData = mainController.getLentData().getListLentData();
        for (int i = 0; i < lentData.size(); i++) {
            if (lentData.get(i).equals("=")){
                counter = i+1;
                int result = 0;
                while(!lentData.get(counter).equals("_")){
                    result++;
                    counter++;
                }
                return  result;
            }
        }
        return -1;
    }
}
