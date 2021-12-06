package com.example.turingemulator.controller;

import com.example.turingemulator.MainView;
import com.example.turingemulator.controller.service.AlgorithmsService;
import com.example.turingemulator.controller.service.AnalizatorService;
import com.example.turingemulator.controller.service.ContextMenuService;
import com.example.turingemulator.controller.updaters.PositionUpdater;
import com.example.turingemulator.data.LentData;
import com.example.turingemulator.data.RowCondition;
import com.example.turingemulator.data.Rule;
import com.example.turingemulator.exception.*;
import com.example.turingemulator.exception.addRow.AddingIncorrectSymbolException;
import com.example.turingemulator.exception.addRow.AlreadyBeingInSymbolsList;
import com.example.turingemulator.exception.analizator.EmptyInitialRuleException;
import com.example.turingemulator.exception.analizator.EmptyLentDataException;
import com.example.turingemulator.exception.analizator.NoOneReferendToFinalStateException;
import com.example.turingemulator.exception.deleteColumn.DeleteFinalColumnException;
import com.example.turingemulator.exception.deleteColumn.MinimumColumnSize;
import com.example.turingemulator.exception.deleteRow.SymbolRowsUseInAnotherTerms;
import com.example.turingemulator.exception.deleteRow.SymbolUseInLentData;
import com.example.turingemulator.exception.deleteRow.SystemSymbolUsageException;
import com.example.turingemulator.exception.operands.IncreaseMaxValueException;
import com.example.turingemulator.exception.operands.NonDigitValuesException;
import com.example.turingemulator.view.Trace;
import javafx.scene.control.Alert;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class MainController {
    private MainView view;

    private LentData lentData;
    private List<RowCondition> rowConditions;
    private PositionUpdater currentPosition;

    private Pattern patternRuleInput = Pattern.compile("q\\d+\\|\\S\\S*\\|((R)|(L)|(S))");

    /*Для трассы*/
    public int lentStateCounter = 0;
    public StringBuilder currentLentState = new StringBuilder();

    boolean algorithmStarted = false;
    private Trace trace = new Trace();

    private ContextMenuService contextMenuService = new ContextMenuService();
    private AnalizatorService analizatorService = new AnalizatorService(this);
    private AlgorithmsService algorithmsService;

    public MainController(MainView view, LentData lentData, List<RowCondition> rowCondition, PositionUpdater positionUpdater) {
        this.view = view;
        this.lentData = lentData;
        this.rowConditions = rowCondition;
        this.currentPosition = positionUpdater;
        this.algorithmsService = new AlgorithmsService(this, view);
    }

    public MainView getView() {
        return view;
    }

    public void setView(MainView view) {
        this.view = view;
    }

    public LentData getLentData() {
        return lentData;
    }

    public void setLentData(LentData lentData) {
        this.lentData = lentData;
    }

    public List<RowCondition> getRowConditions() {
        return rowConditions;
    }

    public void setRowConditions(List<RowCondition> rowConditions) {
        this.rowConditions = rowConditions;
    }

    public PositionUpdater getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(PositionUpdater currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void commitLentTableCell(String newValue, int columnIndex) throws LentInputException {
        boolean existInRows = false;

        for (RowCondition row :
                rowConditions) {
            if (newValue.equals(row.getSymbolLine())) {
                existInRows = true;
                break;
            }
        }
        if (existInRows) {
            lentData.getListLentData().set(columnIndex, newValue);
        }
        throw new LentInputException();
    }

    public void commitRowConditionTableCell(
            String newCondition,
            int columnIndex,
            int rowIndex)
            throws RowConditionCellException {
        if (!patternRuleInput.matcher(newCondition).matches()) {
            throw new RowConditionCellException();
        } else {
            String conditionTo;
            String symbolChangeTo;
            String moveTo;
            int splitter = 0;

            for (int i = 0; i < newCondition.length(); i++) {
                if (newCondition.charAt(i) == '|') {
                    splitter = i;
                }
            }
            conditionTo = newCondition.substring(0, splitter - 2);
            symbolChangeTo = newCondition.substring(splitter - 1, splitter);

            //имеется ли такой символ в таблице
            boolean symbolExist = false;

            for (RowCondition row :
                    rowConditions) {
                if (row.getSymbolLine().equals(symbolChangeTo)) {
                    symbolExist = true;
                    break;
                }
            }
            if (symbolExist) {
                moveTo = newCondition.substring(splitter + 1);

                Rule currentRule = rowConditions.get(rowIndex).getListRules().get(columnIndex - 1);
                currentRule.setConditionTo(conditionTo);
                currentRule.setSymbolChangeTo(symbolChangeTo);
                currentRule.setMoveTo(moveTo);

                rowConditions.get(rowIndex).getListRules().set(columnIndex - 1, currentRule);
            } else {
                throw new RowConditionCellException();
            }
        }
    }

    public void addRow(String newSymbol)
            throws AddingIncorrectSymbolException,

            AlreadyBeingInSymbolsList {
        Pattern patternSymbolInput = Pattern.compile("\\S");

        if (patternSymbolInput.matcher(newSymbol).matches() && rowConditions.size() < 5 && !newSymbol.equals("q")) {
            boolean alreadyIn = false;
            //проверка на ввод повторного символа
            for (RowCondition condition :
                    rowConditions) {
                if (condition.getSymbolLine().equals(newSymbol)) {
                    alreadyIn = true;
                    break;
                }
            }
            if (!alreadyIn) {
                RowCondition newCondition = new RowCondition(newSymbol);
                rowConditions.add(newCondition);
            } else {
                throw new AlreadyBeingInSymbolsList();
            }
        } else {
            throw new AddingIncorrectSymbolException();
        }
    }

    public void deleteRow(int numberSymbol)
            throws SymbolRowsUseInAnotherTerms,
            SymbolUseInLentData,
            SystemSymbolUsageException {
        RowCondition symbolToDelete = rowConditions.get(numberSymbol);

        //содержится ли в других правилах
        for (RowCondition condition :
                rowConditions) {
            if (!condition.getSymbolLine().equals(symbolToDelete.getSymbolLine())) {
                for (Rule rule :
                        condition.getListRules()) {
                    if (rule.getSymbolChangeTo().equals(symbolToDelete.getSymbolLine())) {
                        throw new SymbolRowsUseInAnotherTerms();
                    }
                }
            }
        }

        //содержится ли на ленте
        for (String lentElement :
                lentData.getListLentData()) {
            if (lentElement.equals(symbolToDelete.getSymbolLine())) {
                throw new SymbolUseInLentData();
            }
        }

        if (!symbolToDelete.getSymbolLine().equals("1")
                && !symbolToDelete.getSymbolLine().equals("0")
                && !symbolToDelete.getSymbolLine().equals("_")) {
            rowConditions.remove(numberSymbol);
        } else {
            throw new SystemSymbolUsageException();
        }
    }

    public void addFromRight(int column) {
        int endedPoint = rowConditions.get(0).getEnderIndex();

        for (RowCondition rowCondition :
                rowConditions) {
            rowCondition.setEnderIndex(endedPoint + 1);
        }

        rowConditions = contextMenuService.addRightColumn(column, rowConditions);
    }

    public void addFromLeft(int column) {
        int endedPoint = rowConditions.get(0).getEnderIndex();

        for (RowCondition rowCondition :
                rowConditions) {
            rowCondition.setEnderIndex(endedPoint + 1);
        }

        rowConditions = contextMenuService.addLeftColumn(column, rowConditions);
    }

    public boolean deleteColumn_checkInAlgorithmUsage(int columnPicked)
            throws DeleteFinalColumnException, MinimumColumnSize {
        if (columnPicked == rowConditions.get(0).getEnderIndex() - 1) {
            throw new DeleteFinalColumnException();
        }
        if (rowConditions.get(0).getEnderIndex() < 3) {
            throw new MinimumColumnSize();
        }
        for (RowCondition rowCondition :
                rowConditions) {
            if (!rowCondition.getListRules().get(columnPicked).toString().equals("")) {
                return true;
            }
        }
        return false;
    }

    public void deleteColumn(int columnPicked) {
        rowConditions = contextMenuService.deleteColumnAlgorithm(columnPicked, rowConditions);
        int endedPoint = rowConditions.get(0).getEnderIndex();

        for (RowCondition rowCondition :
                rowConditions) {
            rowCondition.setEnderIndex(endedPoint - 1);
        }
    }

    public void editDescription(String description, int columnIndex, int rowIndex) {
        rowConditions.get(rowIndex).getListRules()
                .get(columnIndex).setDescription(description);
    }

    public void editRule(
            String newRuleSTR,
            int columnIndex,
            int rowIndex)
            throws RowConditionCellException,
            EditFinalStateException {
        if (columnIndex == rowConditions.get(0).getEnderIndex()) {
            throw new EditFinalStateException();
        }
        if (!patternRuleInput.matcher(newRuleSTR).matches()) {
            throw new RowConditionCellException();
        } else {
            String conditionTo;
            String symbolChangeTo;
            String moveTo;
            int splitter = 0;

            for (int i = 0; i < newRuleSTR.length(); i++) {
                if (newRuleSTR.charAt(i) == '|') {
                    splitter = i;
                }
            }
            conditionTo = newRuleSTR.substring(0, splitter - 2);
            symbolChangeTo = newRuleSTR.substring(splitter - 1, splitter);

            //имеется ли такой символ в таблице
            boolean symbolExist = false;

            for (RowCondition row :
                    rowConditions) {
                if (row.getSymbolLine().equals(symbolChangeTo)) {
                    symbolExist = true;
                    break;
                }
            }

            if (symbolExist) {
                moveTo = newRuleSTR.substring(splitter + 1);

                Rule currentRule = rowConditions.get(rowIndex).getListRules().get(columnIndex);
                currentRule.setConditionTo(conditionTo);
                currentRule.setSymbolChangeTo(symbolChangeTo);
                currentRule.setMoveTo(moveTo);

                rowConditions.get(rowIndex).getListRules().set(columnIndex, currentRule);
            } else {
                throw new RowConditionCellException();
            }
        }
    }

    public void clearRule(int columnIndex, int rowIndex) {
        rowConditions.get(rowIndex).getListRules().get(columnIndex)
                .reset(" ", " ", " ", " ");
    }

    public void applyOperandWay(String aValueInputted, String bValueInputted)
            throws IncreaseMaxValueException, NonDigitValuesException {
        Pattern patternLentInput = Pattern.compile("(\\d)|(\\d\\d)");

        if (patternLentInput.matcher(aValueInputted).matches()
                && patternLentInput.matcher(bValueInputted).matches()) {

            int aValue = Integer.parseInt(aValueInputted);
            int bValue = Integer.parseInt(bValueInputted);

            //TODO что-то сделать с масштабом
            if (aValue <= 50 || bValue <= 50) {
                //заполнение ленты значением а
                for (int i = 1; i < aValue + 1; i++) {
                    lentData.getListLentData().set(i, "1");
                }
                //вставка знака
                String sign = null;
                for (RowCondition rowCondition :
                        rowConditions) {
                    if (rowCondition.isAction()) sign = rowCondition.getSymbolLine();
                }
                if (sign == null) sign = "_";

                lentData.getListLentData().set(aValue + 1, sign);
                //заполниние ленты значением b
                for (int i = aValue + 2; i < aValue + bValue + 2; i++) {
                    lentData.getListLentData().set(i, "1");
                }
            } else {
                throw new IncreaseMaxValueException();
            }
        } else {
            throw new NonDigitValuesException();
        }
    }

    public void clearLent() {
        for (int i = 0; i < lentData.getListLentData().size(); i++) {
            lentData.getListLentData().set(i, "_");
        }
    }

    public List<RowCondition> clearAlgorithmData() {
        rowConditions.clear();
        currentPosition.setCurrentLentColumn(0);
        currentPosition.setCurrentRowCondition(0);
        currentPosition.setCurrentColumnCondition(1);

        return rowConditions;
    }

    public List<RowCondition> initDefaultAlgorithmData() {
        RowCondition downSpace = new RowCondition("_");
        RowCondition plus = new RowCondition("+");
        plus.setAction(true);
        RowCondition equality = new RowCondition("=");
        RowCondition one = new RowCondition("1");
        RowCondition zero = new RowCondition("0");

        rowConditions.add(downSpace);
        rowConditions.add(plus);
        rowConditions.add(one);
        rowConditions.add(zero);
        rowConditions.add(equality);

        return rowConditions;
    }

    /*AlgorithmExecuting*/
    public void startAlgorithm(boolean checkerOn) {
        //сигнал для отображения Trace
        algorithmStarted = true;

        //проверка настроек
        if (checkerOn) {
            try {
                if (analizatorService.analizator()) {
                    defaultStart();
                }
            } catch (EmptyInitialRuleException e) {
                currentPosition.setCurrentLentColumn(0);
                currentPosition.setCurrentRowCondition(0);
                currentPosition.setCurrentColumnCondition(1);

                Alert alert = new Alert(Alert.AlertType.ERROR, "Начальное правило не определено");
                alert.showAndWait();
            } catch (NoOneReferendToFinalStateException e) {
                currentPosition.setCurrentLentColumn(0);
                currentPosition.setCurrentRowCondition(0);
                currentPosition.setCurrentColumnCondition(1);

                Alert alert = new Alert(Alert.AlertType.ERROR, "Ни одно из правил не ссылает на конечное состояние");
                alert.showAndWait();
            } catch (EmptyLentDataException e) {
                currentPosition.setCurrentLentColumn(0);
                currentPosition.setCurrentRowCondition(0);
                currentPosition.setCurrentColumnCondition(1);

                Alert alert = new Alert(Alert.AlertType.ERROR, "На ленте установленые значения по умолчанию. Пожалуйста. установите значение операндов.");
                alert.showAndWait();
            }
        } else {
            defaultStart();
        }
    }

    public void stepForward() {
        algorithmsService.onStepMovement();
    }

    public void stop() {
        currentPosition.setCurrentLentColumn(0);
        currentPosition.setCurrentRowCondition(0);
        currentPosition.setCurrentColumnCondition(1);

        lentStateCounter = 0;
        if (view.saveStackTraceCheckBox.isSelected() && Trace.getStatus()) {
            //Добавляем финальную надпись
            //Конец процесса
            Trace.getEnded(true);
            Trace.getStopByUser(true);
        }

        view.saveStackTraceCheckBox.setDisable(false);

        if (view.step.isSelected()) {
            view.applyValuesOperands.fire();
            view.stepForward.setDisable(true);
        }
        if (view.auto.isSelected()) {
            //cancelDraw();
        }
        view.fullUncolorized();
        view.start.setDisable(false);

        view.stepForward.disableProperty().unbind();
        view.stop.disableProperty().unbind();
    }

    private void defaultStart() {
        //если кнопка выключена
        if (view.stepForward.isDisabled()) {
            view.stepForward.setDisable(false);
        }
        if (view.stop.isDisabled()) {
            view.stop.setDisable(false);
        }
        view.colorized();

        if (view.saveStackTraceCheckBox.isSelected()) {
            //формирование трассы
            Date date = new Date();
            currentLentState.append("Initial process at ").append(date).append(" \n");
            lentStateCounter++;
            Trace.getStringBuilder(currentLentState);
        }

        view.saveStackTraceCheckBox.setDisable(true);

        if (view.step.isSelected()) {
            view.start.setDisable(true);
            algorithmsService.onStepMovement();
        }
        if (view.auto.isSelected()) {
            view.stepForward.setDisable(true);
            view.start.setDisable(true);
            //startDraw();
        }
        if (view.onlyResult.isSelected()) {
            view.stepForward.setDisable(true);
            algorithmsService.onStepMovementAuto();
        }
    }

    public void plusDataInitPrepareData() {
        int endedPoint = rowConditions.get(0).getEnderIndex();

        for (RowCondition rowCondition :
                rowConditions) {
            rowCondition.setEnderIndex(endedPoint + 1);
        }
        rowConditions = contextMenuService.addRightColumn(2, rowConditions);
    }

    public void plusDataInit() {
        rowConditions.get(0).getListRules().get(0).reset("q0", "_", "R");
        rowConditions.get(0).getListRules().get(1).reset("q2", "=", "R");
        rowConditions.get(0).getListRules().get(2).reset("q3", "1", "L");
        rowConditions.get(0).getListRules().get(4).reset("q5", "_", "R");

        rowConditions.get(1).getListRules().get(0).reset("q0", "+", "R");
        rowConditions.get(1).getListRules().get(1).reset("q1", "+", "R");
        rowConditions.get(1).getListRules().get(3).reset("q3", "+", "L");
        rowConditions.get(1).getListRules().get(4).reset("q4", "+", "L");

        rowConditions.get(2).getListRules().get(0).reset("q1", "0", "R");
        rowConditions.get(2).getListRules().get(1).reset("q1", "1", "R");
        rowConditions.get(2).getListRules().get(2).reset("q2", "1", "R");
        rowConditions.get(2).getListRules().get(3).reset("q3", "1", "L");
        rowConditions.get(2).getListRules().get(5).reset("q6", "1", "S");

        rowConditions.get(3).getListRules().get(3).reset("q0", "0", "R");
        rowConditions.get(3).getListRules().get(4).reset("q4", "1", "L");

        rowConditions.get(4).getListRules().get(0).reset("q4", "=", "L");
        rowConditions.get(4).getListRules().get(1).reset("q2", "=", "R");
        rowConditions.get(4).getListRules().get(3).reset("q3", "=", "L");
    }
}