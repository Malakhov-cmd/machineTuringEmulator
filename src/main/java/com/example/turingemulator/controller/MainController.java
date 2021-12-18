package com.example.turingemulator.controller;

import com.example.turingemulator.MainView;
import com.example.turingemulator.controller.service.AlgorithmsService;
import com.example.turingemulator.controller.service.AnalizatorService;
import com.example.turingemulator.controller.service.ContextMenuService;
import com.example.turingemulator.controller.service.FileProcessingService;
import com.example.turingemulator.controller.updaters.PositionUpdater;
import com.example.turingemulator.data.LentData;
import com.example.turingemulator.data.RowCondition;
import com.example.turingemulator.data.Rule;
import com.example.turingemulator.exception.EditFinalStateException;
import com.example.turingemulator.exception.LentInputException;
import com.example.turingemulator.exception.RowConditionCellException;
import com.example.turingemulator.exception.addRow.AddingIncorrectSymbolException;
import com.example.turingemulator.exception.addRow.AlreadyBeingInSymbolsList;
import com.example.turingemulator.exception.analizator.EmptyInitialRuleException;
import com.example.turingemulator.exception.analizator.EmptyLentDataException;
import com.example.turingemulator.exception.analizator.IncorrectLEntConditionException;
import com.example.turingemulator.exception.analizator.NoOneReferendToFinalStateException;
import com.example.turingemulator.exception.deleteColumn.DeleteFinalColumnException;
import com.example.turingemulator.exception.deleteColumn.MinimumColumnSize;
import com.example.turingemulator.exception.deleteRow.SymbolRowsUseInAnotherTerms;
import com.example.turingemulator.exception.deleteRow.SymbolUseInLentData;
import com.example.turingemulator.exception.deleteRow.SystemSymbolUsageException;
import com.example.turingemulator.exception.file.FileDoesntExist;
import com.example.turingemulator.exception.file.IncorrectFileData;
import com.example.turingemulator.exception.lentCellOperation.IncorrectLentSymbolEnteredException;
import com.example.turingemulator.exception.lentCellOperation.IndexOfLentHeaderOutOfBoundException;
import com.example.turingemulator.exception.lentCellOperation.MinimumLentSizeException;
import com.example.turingemulator.exception.operands.IncreaseMaxValueException;
import com.example.turingemulator.exception.operands.NonDigitValuesException;
import com.example.turingemulator.view.ProgramInfo;
import com.example.turingemulator.view.SystemInfo;
import com.example.turingemulator.view.Trace;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
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
    private ProgramInfo programInfo = new ProgramInfo();
    private SystemInfo systemInfo = new SystemInfo();

    private int aValue;
    private int bValue;

    private ContextMenuService contextMenuService = new ContextMenuService();
    private AnalizatorService analizatorService = new AnalizatorService(this);
    private FileProcessingService fileProcessingService = new FileProcessingService(this);
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

    public Trace getTrace() {
        return trace;
    }

    public void setTrace(Trace trace) {
        this.trace = trace;
    }

    public int getaValue() {
        return aValue;
    }

    public void setaValue(int aValue) {
        this.aValue = aValue;
    }

    public int getbValue() {
        return bValue;
    }

    public void setbValue(int bValue) {
        this.bValue = bValue;
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
        } else {
            throw new LentInputException();
        }
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
        if (rowConditions.get(0).getEnderIndex() < 4) {
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

            this.aValue = aValue;
            this.bValue = bValue;

            if (aValue <= 13 && bValue <= 13) {
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
    public void startAlgorithm(boolean checkerOn) throws IncreaseMaxValueException, NonDigitValuesException {
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

                Alert alert = new Alert(Alert.AlertType.ERROR, "На ленте установленые значения по умолчанию." +
                        "\n Пожалуйста. установите значение операндов.");
                alert.showAndWait();
            } catch (IncorrectLEntConditionException e) {
                currentPosition.setCurrentLentColumn(0);
                currentPosition.setCurrentRowCondition(0);
                currentPosition.setCurrentColumnCondition(1);

                Alert alert = new Alert(Alert.AlertType.ERROR, "Не верное исходное состояние ленты. \n" +
                        "После операндов присутсвуют символы");
                alert.showAndWait();

                clearLent();
                applyOperandWay(String.valueOf(aValue), String.valueOf(bValue));
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
            algorithmsService.cancelDraw();
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
            algorithmsService.startDraw();
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

    public void multyDataInit() {
        rowConditions.get(1).setSymbolLine("*");
        for (Rule rule :
                rowConditions.get(1).getListRules()) {
            rule.setSymbol("*");
        }

        rowConditions.get(0).getListRules().get(0).reset("q0", "_", "R");
        rowConditions.get(0).getListRules().get(1).reset("q2", "=", "L");
        rowConditions.get(0).getListRules().get(2).reset("q3", "_", "R");
        rowConditions.get(0).getListRules().get(3).reset("q3", "_", "R");
        rowConditions.get(0).getListRules().get(6).reset("q9", "_", "R");
        rowConditions.get(0).getListRules().get(7).reset("q8", "1", "R");
        rowConditions.get(0).getListRules().get(8).reset("q5", "_", "L");

        rowConditions.get(1).getListRules().get(1).reset("q1", "*", "R");
        rowConditions.get(1).getListRules().get(2).reset("q2", "*", "L");
        rowConditions.get(1).getListRules().get(3).reset("q4", "*", "R");
        rowConditions.get(1).getListRules().get(5).reset("q6", "*", "L");
        rowConditions.get(1).getListRules().get(7).reset("q7", "*", "R");
        rowConditions.get(1).getListRules().get(9).reset("q10", "*", "R");
        rowConditions.get(1).getListRules().get(11).reset("q12", "*", "S");

        rowConditions.get(2).getListRules().get(0).reset("q1", "1", "R");
        rowConditions.get(2).getListRules().get(1).reset("q1", "1", "R");
        rowConditions.get(2).getListRules().get(2).reset("q2", "1", "L");
        rowConditions.get(2).getListRules().get(3).reset("q3", "1", "R");
        rowConditions.get(2).getListRules().get(4).reset("q5", "0", "R");
        rowConditions.get(2).getListRules().get(5).reset("q5", "1", "L");
        rowConditions.get(2).getListRules().get(6).reset("q7", "0", "R");
        rowConditions.get(2).getListRules().get(7).reset("q7", "1", "R");
        rowConditions.get(2).getListRules().get(10).reset("q5", "0", "R");

        rowConditions.get(3).getListRules().get(5).reset("q5", "0", "L");
        rowConditions.get(3).getListRules().get(6).reset("q6", "0", "L");
        rowConditions.get(3).getListRules().get(7).reset("q7", "0", "R");
        rowConditions.get(3).getListRules().get(9).reset("q9", "1", "R");
        rowConditions.get(3).getListRules().get(10).reset("q10", "0", "R");
        rowConditions.get(3).getListRules().get(11).reset("q12", "1", "L");

        rowConditions.get(4).getListRules().get(5).reset("q5", "=", "L");
        rowConditions.get(4).getListRules().get(7).reset("q7", "=", "R");
        rowConditions.get(4).getListRules().get(10).reset("q11", "=", "L");
    }

    public void isSaveStackTrace() {
        lentStateCounter = 0;
        Trace.getStringBuilder(currentLentState);
    }

    public void showStackTrace() {
        if (!Trace.getStatus()) {
            myLaunch(trace);
        }
    }

    public static void myLaunch(Application applicationClass) {
        Platform.runLater(() -> {
            try {
                Application application = applicationClass;
                Stage primaryStage = new Stage();
                application.start(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void formedStringPresenterOfRowCondition(File file) throws FileDoesntExist, IncorrectFileData {
        if (file == null) {
            throw new FileDoesntExist();
        }
        StringBuilder algorithmFormation = new StringBuilder();

        for (RowCondition row :
                rowConditions) {
            for (Rule rule :
                    row.getListRules()) {
                algorithmFormation
                        .append(rule.getConditionFrom()).append("|")
                        .append(rule.getSymbol()).append("|")
                        .append(rule.getConditionTo()).append("|")
                        .append(rule.getSymbolChangeTo()).append("|")
                        .append(rule.getMoveTo()).append("|")
                        .append(rule.getDescription())
                        .append(";");
            }
            algorithmFormation.append("\n");
        }
        algorithmFormation.append(rowConditions.get(0).getEnderIndex());

        fileProcessingService.saveAlgorithmData(algorithmFormation.toString(), file);
    }

    public void formedAndSavingLentData(File file) throws FileDoesntExist, IncorrectFileData {
        if (file != null) {
            fileProcessingService.saveLentData(file);
        } else {
            throw new FileDoesntExist();
        }
    }

    public void readingAndAnalyzingFiles(File uploadFile) throws FileDoesntExist, IncorrectFileData {
        if (uploadFile == null) {
            throw new FileDoesntExist();
        }
        String fileSTR = null;
        try {
            fileSTR = fileProcessingService.readUsingFiles(uploadFile);
        } catch (IOException e) {
            System.err.println("System error");
        }
        fileProcessingService.analizeFileSTR(fileSTR);
    }

    public void addLentItemViaContextMenu() throws IndexOfLentHeaderOutOfBoundException {
        if (lentData.getEnder() < 201) {
            view.listLentColumns.get(lentData.getEnder()).setVisible(true);
            lentData.setEnder(lentData.getEnder() + 1);
        } else {
            throw new IndexOfLentHeaderOutOfBoundException();
        }
    }

    public void clearLentItemViaContextMenu() throws MinimumLentSizeException {
        if (lentData.getEnder() > 101) {
            view.listLentColumns.get(lentData.getEnder() - 1).setVisible(false);
            lentData.setEnder(lentData.getEnder() - 1);
        } else {
            throw new MinimumLentSizeException();
        }
    }

    public void editLentItemViaContextMenu(String newValue)
            throws IncorrectLentSymbolEnteredException {
        boolean existInRows = false;

        for (RowCondition row :
                rowConditions) {
            if (newValue.equals(row.getSymbolLine())) {
                existInRows = true;
                break;
            }
        }

        if (!existInRows) {
            throw new IncorrectLentSymbolEnteredException();
        } else {
            int column = view.lentTable.getFocusModel().getFocusedCell().getColumn();
            lentData.getListLentData().set(column, newValue);
        }
    }

    public void showProgramInfo() {
        myLaunch(programInfo);
    }

    public void showSystemInfo() {
        myLaunch(systemInfo);
    }
}