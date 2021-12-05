package com.example.turingemulator;

import com.example.turingemulator.controller.MainController;
import com.example.turingemulator.controller.updaters.LentUpdater;
import com.example.turingemulator.controller.updaters.PositionUpdater;
import com.example.turingemulator.controller.updaters.RuleUpdater;
import com.example.turingemulator.data.LentData;
import com.example.turingemulator.data.RowCondition;
import com.example.turingemulator.exception.*;
import com.example.turingemulator.exception.addRow.AddingIncorrectSymbolException;
import com.example.turingemulator.exception.addRow.AlreadyBeingInSymbolsList;
import com.example.turingemulator.exception.deleteColumn.DeleteFinalColumnException;
import com.example.turingemulator.exception.deleteColumn.MinimumColumnSize;
import com.example.turingemulator.exception.deleteRow.SymbolRowsUseInAnotherTerms;
import com.example.turingemulator.exception.deleteRow.SymbolUseInLentData;
import com.example.turingemulator.exception.deleteRow.SystemSymbolUsageException;
import com.example.turingemulator.exception.operands.IncreaseMaxValueException;
import com.example.turingemulator.exception.operands.NonDigitValuesException;
import com.example.turingemulator.view.Trace;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class MainView extends Application implements Initializable {

    public GridPane gridPane;

    public TableView lentTable;
    public TableView mapConditionTable;

    /*Режим ввода*/
    //переключатели режима ввода
    public RadioButton onTheLineInput;
    public RadioButton numberedInput;

    //поля ввода
    public TextField aValueInputted;
    public TextField bValueInputted;

    //кнопки управления вводомм
    public Button clearValuesOperands;
    public Button applyValuesOperands;
    /**/

    /*Режим работы*/
    //переключатели режима работы
    public RadioButton step;
    public RadioButton auto;
    public RadioButton onlyResult;

    //слайдер задержки
    public Slider timeDelay;
    //отображение текущего значения
    public Label sliderValue;

    //кнопка шаг вперед
    public Button stepForward;
    //кнопка остановки
    public Button stop;
    //кнопка начала выполнения
    public Button start;
    /**/

    /*Автозаполнение*/
    public MenuItem plusMenu;
    public MenuItem NODMenu;
    /**/

    /*Очистка ленты и таблицы*/
    public MenuItem newFileLineMenu;
    public MenuItem newFileAlgorithmMenu;
    /**/

    /*Демонтсрация трассы*/
    public CheckBox saveStackTraceCheckBox;
    public Button openTrace;

    private static Stage pStage;

    public int lentStateCounter = 0;
    public StringBuilder currentLentState = new StringBuilder();

    boolean algorithmStarted = false;
    private Trace trace = new Trace();
    /**/

    /*Сохранение данных программы*/
    public MenuItem saveLineMenu;
    public MenuItem saveAlgorithmMenu;
    public MenuItem fileIncludeMenu;
    /**/

    /*Режим работы*/
    public MenuItem optionsMenu;
    public RadioMenuItem checkerOn;
    public RadioMenuItem checkerOff;
    /**/

    /*Меню информации*/
    public MenuItem aboutProgramMenuItem;
    public MenuItem infoAboutSystemMenu;

    private List<TableColumn> listLentColumns = new ArrayList<>();
    public List<TableColumn> listConditionsColumns = new ArrayList<>();

    private ObservableList<LentData> lentDataOserver = FXCollections.observableArrayList();
    private ObservableList<RowCondition> rulesDataOserver = FXCollections.observableArrayList();

    private LentData lentData = new LentData();
    private List<RowCondition> rowConditions = new ArrayList<>();

    public LentUpdater currentLentTable = new LentUpdater(lentTable, lentDataOserver);
    public RuleUpdater currentRuleTable = new RuleUpdater(mapConditionTable, rulesDataOserver);

    private PositionUpdater currentPosition = new PositionUpdater(0, 0, 1);

    private MainController controller;

    private double delay = 0.15;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainView.class.getResource("MainView.fxml"));
        pStage = stage;

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add("/stylesheets.css");

        stage.setScene(scene);
        stage.getIcons().add(new Image("/icon.png"));
        stage.setTitle("Turing Machine Simulation");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLentTableGraphical();
        initRowConditionsGraphical();
        initContextMenu();
        initInputValueType();
        initTypeWork();
        initIncludeAlgorithms();
        initResetOption();

        // возможность выделять конкретную ячейку (без этого выделяются строки)
        mapConditionTable.getSelectionModel().setCellSelectionEnabled(true);
        lentTable.getSelectionModel().setCellSelectionEnabled(true);

        controller = new MainController(this, lentData, rowConditions, currentPosition);

        EventHandler<TableColumn.CellEditEvent<LentData, String>> cellEditEventEventHandler = event -> {
            TablePosition<LentData, String> pos = event.getTablePosition();

            try {
                lentData = controller.commitLentTableCell(event.getNewValue(), pos.getColumn());
            } catch (LentInputException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Пожалуйста введите корректные данные");
                alert.showAndWait();
            }
            currentLentTable.filler(lentData);
            currentLentTable.update();
        };

        EventHandler<TableColumn.CellEditEvent<RowCondition, String>> cellEditEventEventHandlerRules = event -> {
            TablePosition<RowCondition, String> pos = event.getTablePosition();
            String newValue = event.getNewValue();

            try {
                rowConditions = controller.commitRowConditionTableCell(newValue, pos.getColumn(), pos.getRow());
            } catch (RowConditionCellException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Пожалуйста, введите корректные данные");
                alert.showAndWait();
            }
            currentRuleTable.filler(rowConditions);
            currentRuleTable.update();
        };

        for (TableColumn column :
                listLentColumns) {
            column.setOnEditCommit(cellEditEventEventHandler);
        }

        for (TableColumn column :
                listConditionsColumns) {
            column.setOnEditCommit(cellEditEventEventHandlerRules);
        }
    }


    private void initLentTableGraphical() {
        lentDataOserver.add(lentData);

        for (int i = 0; i < 201; i++) {
            TableColumn<LentData, String> column = new TableColumn<>(String.valueOf(i));
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            int finalI = i;
            column.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getListLentData().get(finalI)));

            column.setMaxWidth(30);

            if (i > 100) {
                column.setVisible(false);
            }

            listLentColumns.add(column);
            lentTable.getColumns().add(column);
        }
        lentTable.setItems(lentDataOserver);
    }

    private void initRowConditionsGraphical() {
        RowCondition downSpace = new RowCondition("_");
        rulesDataOserver.add(downSpace);

        RowCondition plus = new RowCondition("+");
        plus.setAction(true);
        rulesDataOserver.add(plus);

        RowCondition equality = new RowCondition("=");
        rulesDataOserver.add(equality);

        RowCondition one = new RowCondition("1");
        rulesDataOserver.add(one);

        RowCondition zero = new RowCondition("0");
        rulesDataOserver.add(zero);

        for (int i = 0; i < 21; i++) {
            TableColumn<RowCondition, String> column = null;
            if (i == 0) {
                column = new TableColumn<>("Знак");
                column.setCellFactory(TextFieldTableCell.forTableColumn());
                column.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getSymbolLine()));

                column.setMinWidth(45);
                column.setEditable(false);

            } else {
                int finalI = i - 1;
                column = new TableColumn<>("q" + finalI);
                column.setCellFactory(TextFieldTableCell.forTableColumn());
                column.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getListRules().get(finalI).toString()));

            }
            column.setMaxWidth(55);
            if (i == 3) {
                column.setEditable(false);
            }
            if (i > 3) {
                column.setVisible(false);
            }
            listConditionsColumns.add(column);
            mapConditionTable.getColumns().add(column);
        }

        rowConditions.add(downSpace);
        rowConditions.add(plus);
        rowConditions.add(one);
        rowConditions.add(zero);
        rowConditions.add(equality);

        mapConditionTable.setItems(rulesDataOserver);

        currentRuleTable.filler(rowConditions);
        currentRuleTable.update();
    }

    private void initContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        Menu wordIncludesTextParentMenu = new Menu("Управление символами");
        MenuItem addTextMenuItem = new MenuItem("Добавить");
        MenuItem clearTextMenuItem = new MenuItem("Удалить");
        wordIncludesTextParentMenu.getItems().addAll(addTextMenuItem, clearTextMenuItem);

        Menu conditionsGoveringTextParentMenu = new Menu("Управление состояниями");
        MenuItem addFromRightTextMenuItem = new MenuItem("Добавить справа");
        MenuItem addFromLeftTextMenuItem = new MenuItem("Добавить слева");
        MenuItem deleteConditionTextMenuItem = new MenuItem("Удалить");

        SeparatorMenuItem separatorMenuItem1 = new SeparatorMenuItem();

        Menu conditionsGoveringDopOptionTextParentMenu = new Menu("Дополнительные возможности");
        MenuItem refactorDescriptionTextMenuItem = new MenuItem("Добавить описание");
        MenuItem showDescriptionMenuItem = new MenuItem("Показать описание");

        conditionsGoveringDopOptionTextParentMenu.getItems().addAll(
                refactorDescriptionTextMenuItem,
                showDescriptionMenuItem);

        conditionsGoveringTextParentMenu.getItems().addAll(addFromRightTextMenuItem,
                addFromLeftTextMenuItem,
                deleteConditionTextMenuItem,
                separatorMenuItem1,
                conditionsGoveringDopOptionTextParentMenu);

        SeparatorMenuItem separatorMenuItem2 = new SeparatorMenuItem();

        Menu cellEditTextParentMenu = new Menu("Управление правилом");
        MenuItem editCellTextMenuItem = new MenuItem("Изменить");
        MenuItem clearCellTextMenuItem = new MenuItem("Отчистить");

        cellEditTextParentMenu.getItems().addAll(editCellTextMenuItem,
                clearCellTextMenuItem);

        contextMenu.getItems().addAll(wordIncludesTextParentMenu,
                conditionsGoveringTextParentMenu,
                separatorMenuItem2,
                cellEditTextParentMenu);

        // When user right-click on table
        mapConditionTable.setOnContextMenuRequested(event -> {

            contextMenu.show(mapConditionTable, event.getScreenX(), event.getScreenY());
            //System.out.println(mapConditionTable.getFocusModel().getFocusedCell().getColumn());
        });

        addTextMenuItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Введите новый символ");
            dialog.showAndWait().ifPresentOrElse(
                    result -> {
                        try {
                            controller.addRow(result);
                        } catch (AddingIncorrectSymbolException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Пожалуйста, введите правильные данные или удалите одну строку, если количество строк увеличится на 5");
                            alert.showAndWait();
                        } catch (AlreadyBeingInSymbolsList alreadyBeingInSymbolsList) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Символ уже находится в списке");
                            alert.showAndWait();
                        }
                        currentRuleTable.filler(rowConditions);
                        currentRuleTable.update();
                    },
                    () -> System.err.println("something go wrong"));
        });

        clearTextMenuItem.setOnAction(event -> {
            int numberSymbol = mapConditionTable.getFocusModel().getFocusedCell().getRow();

            try {
                rowConditions = controller.deleteRow(numberSymbol);
            } catch (SymbolRowsUseInAnotherTerms e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Этот символ используется в других выражениях");
                alert.showAndWait();
            } catch (SymbolUseInLentData e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Символ используется на ленте");
                alert.showAndWait();
            } catch (SystemSymbolUsageException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Этот символ используется в системе");
                alert.showAndWait();
            }

            currentRuleTable.filler(rowConditions);
            currentRuleTable.update();
        });

        addFromRightTextMenuItem.setOnAction(event -> {
            //если меньше 20 столбцов
            if (rowConditions.get(0).getEnderIndex() < 19) {
                //например 2 столбца
                int endedPoint = rowConditions.get(0).getEnderIndex();

                TableColumn openedColumn = listConditionsColumns.get(endedPoint + 1);
                TableColumn preOpenedColumn = listConditionsColumns.get(endedPoint);
                openedColumn.setVisible(true);
                openedColumn.setEditable(false);

                preOpenedColumn.setEditable(true);

                rowConditions = controller.addFromRight(mapConditionTable.getFocusModel().getFocusedCell().getColumn());

                currentRuleTable.filler(rowConditions);
                currentRuleTable.update();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Вы не можете создать больше 20 столбцов");
                alert.showAndWait();
            }
        });

        addFromLeftTextMenuItem.setOnAction(event -> {
            if (rowConditions.get(0).getEnderIndex() < 19) {
                //например 2 столбца
                int endedPoint = rowConditions.get(0).getEnderIndex();

                TableColumn openedColumn = listConditionsColumns.get(endedPoint + 1);
                TableColumn preOpenedColumn = listConditionsColumns.get(endedPoint);
                openedColumn.setVisible(true);
                openedColumn.setEditable(false);

                preOpenedColumn.setEditable(true);

                rowConditions = controller.addFromLeft(mapConditionTable.getFocusModel().getFocusedCell().getColumn() - 1);

                currentRuleTable.filler(rowConditions);
                currentRuleTable.update();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Вы не можете создать больше 20 столбцов");
                alert.showAndWait();
            }
        });

        deleteConditionTextMenuItem.setOnAction(event -> {
            int columnPicked = mapConditionTable.getFocusModel().getFocusedCell().getColumn() - 1;

            try {
                if (!controller.deleteColumn_checkInAlgorithmUsage(columnPicked)) {
                    rowConditions = controller.deleteColumn(columnPicked);

                    int endedPoint = rowConditions.get(0).getEnderIndex() + 1;

                    TableColumn lastColumn = listConditionsColumns.get(endedPoint);
                    TableColumn preLastColumn = listConditionsColumns.get(endedPoint - 1);
                    lastColumn.setVisible(false);
                    lastColumn.setEditable(true);

                    preLastColumn.setEditable(false);

                    currentRuleTable.filler(rowConditions);
                    currentRuleTable.update();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Состояние используется в алгоритме");
                    alert.showAndWait();
                }
            } catch (DeleteFinalColumnException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Удаление последнего столбца недопустимо");
                alert.showAndWait();
            } catch (MinimumColumnSize e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Минимальное количество столбов должно ровняться двум");
                alert.showAndWait();
            }
        });

        refactorDescriptionTextMenuItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Введите описание");
            dialog.showAndWait().ifPresentOrElse(
                    result -> {
                        int columnPicked = mapConditionTable.getFocusModel().getFocusedCell().getColumn() - 1;
                        int rowPicked = mapConditionTable.getFocusModel().getFocusedCell().getRow();

                        rowConditions = controller.editDescription(result, columnPicked, rowPicked);

                        currentRuleTable.filler(rowConditions);
                        currentRuleTable.update();
                    },
                    () -> System.out.println("System error"));
        });

        showDescriptionMenuItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Расширенная информация");
            alert.setHeaderText("Описание правила");

            int columnPicked = mapConditionTable.getFocusModel().getFocusedCell().getColumn() - 1;
            int rowPicked = mapConditionTable.getFocusModel().getFocusedCell().getRow();

            TextArea areaTXTError = new TextArea(rowConditions.get(rowPicked).getListRules().get(columnPicked).getDescription());
            areaTXTError.setEditable(false);

            alert.getDialogPane().setExpandableContent(areaTXTError);
            alert.showAndWait();
        });

        editCellTextMenuItem.setOnAction(event -> {
            int columnPicked = mapConditionTable.getFocusModel().getFocusedCell().getColumn() - 1;
            int rowPicked = mapConditionTable.getFocusModel().getFocusedCell().getRow();

            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Изменение правила");
            dialog.showAndWait().ifPresentOrElse(
                    result -> {
                        try {
                            rowConditions = controller.editRule(result, columnPicked, rowPicked);

                            currentRuleTable.filler(rowConditions);
                            currentRuleTable.update();
                        } catch (RowConditionCellException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Неверный формат ввода правил");
                            alert.showAndWait();
                        } catch (EditFinalStateException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Изменение последнего столбца недопустимо");
                            alert.showAndWait();
                        }
                    },
                    () -> System.err.println("System error"));
        });

        clearCellTextMenuItem.setOnAction(event -> {
            int columnPicked = mapConditionTable.getFocusModel().getFocusedCell().getColumn() - 1;
            int rowPicked = mapConditionTable.getFocusModel().getFocusedCell().getRow();

            rowConditions = controller.clearRule(columnPicked, rowPicked);

            currentRuleTable.filler(rowConditions);
            currentRuleTable.update();
        });

        //TODO не работает с первого раза добавление справа и слева
    }

    private void initInputValueType() {
        ToggleGroup group = new ToggleGroup();

        onTheLineInput.setToggleGroup(group);
        numberedInput.setToggleGroup(group);

        group.selectedToggleProperty().addListener((changed, oldValue, newValue) -> {
            // получаем выбранный элемент RadioButton
            RadioButton selectedBtn = (RadioButton) newValue;

            if (numberedInput.equals(selectedBtn)) {
                aValueInputted.setDisable(false);
                bValueInputted.setDisable(false);

                clearValuesOperands.setDisable(false);
                applyValuesOperands.setDisable(false);
            } else {
                aValueInputted.setDisable(true);
                bValueInputted.setDisable(true);

                clearValuesOperands.setDisable(true);
                applyValuesOperands.setDisable(true);
            }
        });

        applyValuesOperands.setOnAction(event -> {
            try {
                lentData = controller.applyOperandWay(aValueInputted.getText(), bValueInputted.getText());
            } catch (IncreaseMaxValueException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Максимальное значение операнда составляет 50");
                alert.showAndWait();
            } catch (NonDigitValuesException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Введите корректные значение операндов - целые числа");
                alert.showAndWait();
            }

            currentLentTable.filler(lentData);
            currentLentTable.update();
        });

        clearValuesOperands.setOnAction(event -> {
            lentData = controller.clearLent();

            currentLentTable.filler(lentData);
            currentLentTable.update();
        });
    }

    private void initTypeWork() {
        ToggleGroup group = new ToggleGroup();

        step.setToggleGroup(group);
        auto.setToggleGroup(group);
        onlyResult.setToggleGroup(group);

        sliderValue.setText("Value: " + timeDelay.getValue());

        group.selectedToggleProperty().addListener((changed, oldValue, newValue) -> {
            RadioButton selectedBtn = (RadioButton) newValue;

            timeDelay.setDisable(!auto.equals(selectedBtn));
            sliderValue.setVisible(auto.equals(selectedBtn));

            if (start.isDisabled()) {
                start.setDisable(false);

                stepForward.disableProperty().unbind();
                stop.disableProperty().unbind();
            }
        });

        timeDelay.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String formattedStr = newValue.toString().substring(0, 4);
                if (Double.parseDouble(formattedStr) > 0.98) {
                    formattedStr = "1";
                }
                sliderValue.setText("Value: " + formattedStr);
            } catch (StringIndexOutOfBoundsException e) {
                String formattedStr = newValue.toString().substring(0, 3);
                if (Double.parseDouble(formattedStr) > 0.98) {
                    formattedStr = "1";
                }
                sliderValue.setText("Value: " + formattedStr);
            } finally {
                delay = Double.parseDouble(sliderValue.getText().substring(7));
            }
        });

        start.setOnAction(event -> {
            controller.startAlgorithm(algorithmStarted, checkerOn.isSelected());
        });

        stepForward.setOnAction(event -> {
            //onStepMovement();
            controller.stepForward();
        });

        stop.setOnAction(event -> {
            currentPosition.setCurrentLentColumn(0);
            currentPosition.setCurrentRowCondition(0);
            currentPosition.setCurrentColumnCondition(1);

            lentStateCounter = 0;
            if (saveStackTraceCheckBox.isSelected() && Trace.getStatus()) {
                //Добавляем финальную надпись
                //Конец процесса
                Trace.getEnded(true);
                Trace.getStopByUser(true);
            }

            saveStackTraceCheckBox.setDisable(false);

            if (step.isSelected()) {
                applyValuesOperands.fire();
                stepForward.setDisable(true);
            }
            if (auto.isSelected()) {
                //cancelDraw();
            }
            fullUncolorized();
            start.setDisable(false);

            stepForward.disableProperty().unbind();
            stop.disableProperty().unbind();
        });
    }


    public void colorized() {
        for (TableColumn lentColumn :
                listLentColumns) {
            lentColumn.setStyle(" ");
        }

        listLentColumns.get(currentPosition.getCurrentLentColumn()).setStyle("-fx-background-color: linear-gradient(to right, red, blue);");

        TableCell cellConditionsTable = (TableCell) mapConditionTable.queryAccessibleAttribute(
                AccessibleAttribute.CELL_AT_ROW_COLUMN,
                currentPosition.getCurrentRowCondition(),
                currentPosition.getCurrentColumnCondition());
        cellConditionsTable.setStyle("-fx-background-color: linear-gradient(to right, red, blue);");
    }

    public void unColorized() {
        for (TableColumn lentColumn :
                listLentColumns) {
            lentColumn.setStyle(" ");
        }

        TableCell cellConditionsTable = (TableCell) mapConditionTable.queryAccessibleAttribute(
                AccessibleAttribute.CELL_AT_ROW_COLUMN,
                currentPosition.getCurrentRowCondition(),
                currentPosition.getCurrentColumnCondition());
        cellConditionsTable.setStyle(" ");
    }

    private void fullUncolorized() {
        for (TableColumn lentColumn :
                listLentColumns) {
            lentColumn.setStyle(" ");
        }

        for (int i = 0; i < rowConditions.size(); i++) {
            for (int j = 0; j < rowConditions.get(i).getListRules().size(); j++) {
                try {
                    TableCell cellConditionsTable = (TableCell) mapConditionTable.queryAccessibleAttribute(
                            AccessibleAttribute.CELL_AT_ROW_COLUMN,
                            i,
                            j + 1);
                    cellConditionsTable.setStyle(" ");
                } catch (NullPointerException ignored) {
                }
            }
        }
    }

    private void initIncludeAlgorithms() {

        plusMenu.setOnAction(event -> {
            newFileAlgorithmMenu.fire();

            controller.plusDataInit();

            currentRuleTable.filler(rowConditions);
            currentRuleTable.update();
        });
    }

    //Очистка таблиц
    private void initResetOption() {
        newFileLineMenu.setOnAction(event -> {
            fullUncolorized();

            for (int i = 0; i < lentData.getListLentData().size(); i++) {
                lentData.getListLentData().set(i, "_");
            }

            currentLentTable.filler(lentData);
            currentLentTable.update();
        });

        newFileAlgorithmMenu.setOnAction(event -> {
            System.out.println(rowConditions.get(0).getEnderIndex());
            fullUncolorized();

            int endedPoint = rowConditions.get(0).getEnderIndex();
            while (endedPoint != 2) {
                TableColumn openedColumn = listConditionsColumns.get(endedPoint);
                openedColumn.setVisible(false);
                openedColumn.setEditable(true);

                endedPoint--;
            }
            TableColumn third = listConditionsColumns.get(endedPoint);
            third.setEditable(false);

            rowConditions.clear();
            rulesDataOserver.clear();
            listConditionsColumns.clear();

            currentPosition.setCurrentLentColumn(0);
            currentPosition.setCurrentRowCondition(0);
            currentPosition.setCurrentColumnCondition(1);

            initRowConditionsGraphical();
        });
    }


}