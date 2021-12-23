package com.example.turingemulator.controller.service;

import com.example.turingemulator.controller.MainController;
import com.example.turingemulator.data.RowCondition;
import com.example.turingemulator.data.Rule;
import com.example.turingemulator.view.Trace;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class StepTaskService extends Task<Void> {
    private MainController mainController;
    private final double totalDelay;
    private boolean error;

    public StepTaskService(double totalDelay, MainController mainController) {
        this.totalDelay = totalDelay;
        error = false;
        this.mainController = mainController;
    }

    @Override
    protected Void call() {
        try {
            stepMovement();
        } catch (Exception ex) {
            updateMessage(ex.getMessage());
        }
        return null;
    }

    private void stepMovement() {
        updateMessage("Рисование начато");

        error = false;
        while (!mainController.getCurrentPosition().isFinished() && !error) {
            if (isCancelled()) {
                updateMessage("Рисование прервано");
                return;
            }
            Platform.runLater(() -> {
                try {
                    String lentDataBefore = mainController.getLentData().getAllSymbols();

                    //получили текущее правило
                    Rule rowFrom = mainController.getRowConditions().get(mainController.getCurrentPosition().getCurrentRowCondition())
                            .getListRules().get(mainController.getCurrentPosition().getCurrentColumnCondition() - 1);

                    mainController.getView().unColorized();

                    //на какое состояние перейти
                    String conditionTo = rowFrom.getConditionTo();
                    int newConditionInt;
                    if (conditionTo.length() == 2) {
                        newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 1)) + 1;
                    } else {
                        newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 2)) + 1;
                    }
                    //проверка на несуществующие состояния
                    if (newConditionInt < 0 || newConditionInt > 20) {
                        error = true;

                        Alert alert = new Alert(Alert.AlertType.ERROR, "Неверный номер состояния. Ваши номера правил" +
                                "\n должны быть в диапазоне от 0 до 20");
                        alert.showAndWait();

                        if (mainController.getView().saveStackTraceCheckBox.isSelected()) {
                            Date date = new Date();
                            mainController.currentLentState.append(mainController.lentStateCounter).append(" Неверный номер состояния в ").append(date).append("\n");
                            mainController.lentStateCounter++;
                            Trace.getStringBuilder(mainController.currentLentState);
                        }
                    } else {
                        //проверка на неоткрытые состояния
                        if (newConditionInt > mainController.getRowConditions().get(0).getEnderIndex()) {

                                error = true;

                                Alert alert = new Alert(Alert.AlertType.ERROR, "Сслыка на несуществующее состояние");
                                alert.showAndWait();

                                if (mainController.getView().saveStackTraceCheckBox.isSelected()) {
                                    Date date = new Date();
                                    mainController.currentLentState.append(mainController.lentStateCounter).append(" Сслыка на несуществующее состояние в ").append(date).append("\n");
                                    mainController.lentStateCounter++;
                                    Trace.getStringBuilder(mainController.currentLentState);
                                }
                        } else {
                            mainController.getCurrentPosition().setCurrentColumnCondition(newConditionInt);

                            String symbolChangeTo = rowFrom.getSymbolChangeTo();

                            mainController.getLentData().getListLentData().set(mainController.getCurrentPosition().getCurrentLentColumn(), symbolChangeTo);

                            mainController.getView().currentLentTable.filler(mainController.getLentData());
                            mainController.getView().currentLentTable.update();

                            if (mainController.getView().saveStackTraceCheckBox.isSelected()) {
                                mainController.currentLentState.append(mainController.lentStateCounter).append(mainController.getLentData().toString()).append("\n");
                                mainController.lentStateCounter++;
                                Trace.getStringBuilder(mainController.currentLentState);
                            }

                            //куда передвинуть головку
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

                            //следующая строка
                            /*Проверка на выход за пределы ленты*/
                            String nextRow = null;
                            try {
                                nextRow = mainController.getLentData().getListLentData().get(mainController.getCurrentPosition().getCurrentLentColumn());
                            } catch (IndexOutOfBoundsException e) {
                                mainController.getCurrentPosition().setCurrentLentColumn(0);
                                mainController.getCurrentPosition().setCurrentRowCondition(0);
                                mainController.getCurrentPosition().setCurrentColumnCondition(1);

                                error = true;

                                Alert alert = new Alert(Alert.AlertType.ERROR, "Указатель ленты находится за пределами" +
                                        "\n разрешенных значений");
                                alert.showAndWait();

                                if (mainController.getView().saveStackTraceCheckBox.isSelected()) {
                                    Date date = new Date();
                                    mainController.currentLentState.append(mainController.lentStateCounter).append(" Указатель ленты находится за пределами разрешенных значений в ").append(date).append("\n");
                                    mainController.lentStateCounter++;
                                    Trace.getStringBuilder(mainController.currentLentState);
                                }

                                mainController.getView().unColorized();
                            }
                            int nextRowInt = -1;
                            for (int i = 0; i < mainController.getRowConditions().size(); i++) {
                                if (mainController.getRowConditions().get(i).getSymbolLine().equals(nextRow)) {
                                    mainController.getCurrentPosition().setCurrentRowCondition(i);
                                    nextRowInt = i;
                                }
                            }

                            //проверка на то пустое ли следующее правило
                            Rule nextRule = null;
                            try {
                                nextRule = mainController.getRowConditions().get(nextRowInt).getListRules().get(newConditionInt - 1);
                            } catch (IndexOutOfBoundsException e) {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Лента не может находиться левее нуля");
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

                                if ( mainController.getView().saveStackTraceCheckBox.isSelected()) {
                                    Date date = new Date();
                                    mainController.currentLentState.append(mainController.lentStateCounter).append(" Алгоритм успешно завершен в ").append(date).append("\n");
                                    Trace.getStringBuilder(mainController.currentLentState);

                                    mainController.lentStateCounter = 0;
                                    //Конец процесса
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
                                    alert = new Alert(Alert.AlertType.INFORMATION, "Алгоритм успешно завершен " + "\n" +
                                            "Ответ: " + getParseResult());
                                } else {
                                    alert = new Alert(Alert.AlertType.INFORMATION, "Алгоритм успешно завершен ");
                                }
                                alert.showAndWait();
                            } else {
                                if (!full) {

                                    mainController.getView().colorized();
                                    mainController.getCurrentPosition().setCurrentLentColumn(0);
                                    mainController.getCurrentPosition().setCurrentRowCondition(0);
                                    mainController.getCurrentPosition().setCurrentColumnCondition(1);

                                    error = true;

                                    Alert alert = new Alert(Alert.AlertType.ERROR, "Следующее правило не заполнено");
                                    alert.showAndWait();

                                    if (mainController.getView().saveStackTraceCheckBox.isSelected()) {
                                        Date date = new Date();
                                        mainController.currentLentState.append(mainController.lentStateCounter).append(" Следующее правило было не заполнено в ").append(date).append("\n");
                                        mainController.lentStateCounter++;
                                        Trace.getStringBuilder(mainController.currentLentState);
                                    }

                                    mainController.getView().unColorized();
                                }
                            }
                                mainController.getView().colorized();
                        }
                    }
                    String lentDataAfter = mainController.getLentData().getAllSymbols();
                    lentActuatorGraphical(lentDataBefore, lentDataAfter);
                } catch (Exception e) {
                    mainController.getCurrentPosition().setCurrentLentColumn(0);
                    mainController.getCurrentPosition().setCurrentRowCondition(0);
                    mainController.getCurrentPosition().setCurrentColumnCondition(1);

                    error = true;

                    Alert alert = new Alert(Alert.AlertType.ERROR, "Что-то пошло не так." +
                            "\n Попробуйте включить анализатор в настройках");
                    alert.showAndWait();
                }
            });

            try {
                Thread.sleep((long) this.totalDelay);
            } catch (InterruptedException interrupted) {
                if (isCancelled()) {
                    updateMessage("Рисование прервано");
                    return;
                }
            }
        }
        mainController.getView().unColorized();
        updateMessage("Рисование завершено");
    }

    @Override
    protected void updateMessage(String message) {
        super.updateMessage(message);
    }

    private void lentActuatorGraphical(String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            for (int i = 0; i < 200; i++) {
                int tmpOld = oldValue.charAt(i);
                int tmpNew = newValue.charAt(i);
                if (tmpNew != tmpOld && i > 100) {
                    mainController.getView().listLentColumns.get(mainController.getLentData().getEnder()).setVisible(true);
                    mainController.getLentData().setEnder(mainController.getLentData().getEnder() + 1);

                    mainController.getView().currentLentTable.filler(mainController.getLentData());
                    mainController.getView().currentLentTable.update();
                }
            }
        }
    }

    private int getParseResult() {
        int counter = 0;
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
