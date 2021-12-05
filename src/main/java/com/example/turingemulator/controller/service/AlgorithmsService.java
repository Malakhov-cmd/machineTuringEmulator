package com.example.turingemulator.controller.service;

import com.example.turingemulator.MainView;
import com.example.turingemulator.controller.updaters.PositionUpdater;
import com.example.turingemulator.data.LentData;
import com.example.turingemulator.data.RowCondition;
import com.example.turingemulator.data.Rule;
import com.example.turingemulator.view.Trace;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.Date;
import java.util.List;

public class AlgorithmsService {
    public AlgorithmsService() {}

    public void onStepMovement(List<RowCondition> rowConditions, LentData lentData, MainView model, PositionUpdater currentPosition) {

        Platform.runLater(() -> {
            try {
                //получили текущее правило
                Rule rowFrom = rowConditions.get(currentPosition.getCurrentRowCondition())
                        .getListRules().get(currentPosition.getCurrentColumnCondition() - 1);

                model.unColorized();

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
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Incorrect condition state number. Your Conditions must be between 0 and 20");
                    alert.showAndWait();

                    if (model.saveStackTraceCheckBox.isSelected()) {
                        Date date = new Date();
                        model.currentLentState.append(model.lentStateCounter).append(" Incorrect condition state number at ").append(date).append("\n");
                        model.lentStateCounter++;
                        Trace.getStringBuilder(model.currentLentState);
                    }
                } else {
                    //проверка на неоткрытые состояния
                    if (newConditionInt > rowConditions.get(0).getEnderIndex()) {
                        if (newConditionInt - 1 == rowConditions.get(0).getEnderIndex()) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Process of working algorithm is done");
                            alert.showAndWait();

                            currentPosition.setCurrentLentColumn(0);
                            currentPosition.setCurrentRowCondition(0);
                            currentPosition.setCurrentColumnCondition(1);
                            currentPosition.setFinished(true);

                            if (model.saveStackTraceCheckBox.isSelected()) {
                                Date date = new Date();
                                model.currentLentState.append(model.lentStateCounter).append(" Process of working algorithm is done at ").append(date).append("\n");
                                Trace.getStringBuilder(model.currentLentState);

                                model.lentStateCounter = 0;
                                //Конец процесса
                                Trace.getEnded(true);
                            }

                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "You are linking to condition that doesn't exist");
                            alert.showAndWait();

                            if (model.saveStackTraceCheckBox.isSelected()) {
                                Date date = new Date();
                                model.currentLentState.append(model.lentStateCounter).append(" Linking to condition that doesn't exist at ").append(date).append("\n");
                                model.lentStateCounter++;
                                Trace.getStringBuilder(model.currentLentState);
                            }

                        }
                    } else {
                        currentPosition.setCurrentColumnCondition(newConditionInt);

                        String symbolChangeTo = rowFrom.getSymbolChangeTo();

                        lentData.getListLentData().set(currentPosition.getCurrentLentColumn(), symbolChangeTo);

                        model.currentLentTable.filler(lentData);
                        model.currentLentTable.update();

                        if (model.saveStackTraceCheckBox.isSelected()) {
                            model.currentLentState.append(model.lentStateCounter).append(lentData.toString()).append("\n");
                            model.lentStateCounter++;
                            Trace.getStringBuilder(model.currentLentState);
                        }

                        //куда передвинуть головку
                        String moveTo = rowFrom.getMoveTo();
                        if (moveTo.equals("R")) {
                            currentPosition.setCurrentLentColumn(
                                    currentPosition.getCurrentLentColumn() + 1
                            );
                        } else {
                            if (moveTo.equals("L")) {
                                currentPosition.setCurrentLentColumn(
                                        currentPosition.getCurrentLentColumn() - 1
                                );
                            }
                        }

                        //следующая строка
                        /*Проверка на выход за пределы ленты*/
                        String nextRow = null;
                        try {
                            nextRow = lentData.getListLentData().get(currentPosition.getCurrentLentColumn());
                        } catch (IndexOutOfBoundsException e) {
                            currentPosition.setCurrentLentColumn(0);
                            currentPosition.setCurrentRowCondition(0);
                            currentPosition.setCurrentColumnCondition(1);

                            Alert alert = new Alert(Alert.AlertType.ERROR, "Index of writing head out of bounds");
                            alert.showAndWait();

                            if (model.saveStackTraceCheckBox.isSelected()) {
                                Date date = new Date();
                                model.currentLentState.append(model.lentStateCounter).append(" Index of writing head out of bounds at ").append(date).append("\n");
                                model.lentStateCounter++;
                                Trace.getStringBuilder(model.currentLentState);
                            }

                            model.unColorized();
                        }
                        int nextRowInt = -1;
                        for (int i = 0; i < rowConditions.size(); i++) {
                            if (rowConditions.get(i).getSymbolLine().equals(nextRow)) {
                                currentPosition.setCurrentRowCondition(i);
                                nextRowInt = i;
                            }
                        }

                        //проверка на то пустое ли следующее правило
                        Rule nextRule = null;
                        try {
                            nextRule = rowConditions.get(nextRowInt).getListRules().get(newConditionInt - 1);
                        } catch (IndexOutOfBoundsException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Lent couldn't be at index less than 0");
                            alert.showAndWait();
                        }


                        boolean full = true;
                        if (nextRule.getConditionTo().equals(" ")
                                || nextRule.getSymbolChangeTo().equals(" ")
                                || nextRule.getMoveTo().equals(" ")) {
                            full = false;
                        }
                        if (!full) {
                            currentPosition.setCurrentLentColumn(0);
                            currentPosition.setCurrentRowCondition(0);
                            currentPosition.setCurrentColumnCondition(1);

                            Alert alert = new Alert(Alert.AlertType.ERROR, "Next rule is not filled");
                            alert.showAndWait();

                            if (model.saveStackTraceCheckBox.isSelected()) {
                                Date date = new Date();
                                model.currentLentState.append(model.lentStateCounter).append(" Next rule is not filled at ").append(date).append("\n");
                                model.lentStateCounter++;
                                Trace.getStringBuilder(model.currentLentState);
                            }

                            model.unColorized();
                        }
                        model.colorized();
                    }
                }
                currentPosition.setFinished(false);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Something go wrong. Try turn on Analyze in option");
                alert.showAndWait();
            }
        });
    }
}
