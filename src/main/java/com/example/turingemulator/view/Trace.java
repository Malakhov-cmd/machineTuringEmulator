package com.example.turingemulator.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class Trace extends Application implements Initializable {
    public TextArea stackTraceTextArea;
    public Button clearTrace;
    public Button saveTrace;

    private static StringBuilder stringBuilder = new StringBuilder();
    private static boolean ended = false;
    private static boolean stopByUser = false;
    private static boolean isOpened = false;

    private String tempSTR;

    private static Stage pStage;

    public Trace() {}

    @Override
    public void start(Stage primaryStage) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);

        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource("/com/example/turingemulator/TraceView.fxml");
        loader.setLocation(xmlUrl);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pStage = primaryStage;
        isOpened = true;

        assert root != null;
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image("/icon.png"));
        stage.setTitle("Trace");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Вся трассировка стека будет удалена." +
                    "\n Вы уверены?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                stringBuilder.setLength(0);
                isOpened = false;
            } else {
                event.consume();
            }
        });

        stage.showAndWait();
    }

    public static Stage getPrimaryStage() {
        return pStage;
    }

    public static void getStringBuilder(StringBuilder sb) {
        stringBuilder = sb;
    }

    public static void getEnded(boolean end) {
        ended = end;
    }

    public static void getStopByUser(boolean stoped) {
        stopByUser = stoped;
    }

    public static boolean getStatus() {
        return isOpened;
    }

    private Task<Void> task;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tempSTR = stackTraceTextArea.getText();
        startDraw();

        clearTrace.setOnAction(event -> {
            stringBuilder.setLength(0);
        });

        saveTrace.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showSaveDialog(getPrimaryStage());

            if (file != null) {
                saveTextToFile(stackTraceTextArea.getText(), file);
            }
        });
    }

    private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            System.out.println();
        }
    }

    public void startDraw() {
        if (task != null && task.isRunning()) {
            task.cancel();
        }

        task = new dysplayTrace();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }


    private class dysplayTrace extends Task<Void> {
        public dysplayTrace() {}

        @Override
        protected Void call() {
            try {
                dysplay();
            } catch (Exception ex) {
                updateMessage(ex.getMessage());
            }
            return null;
        }

        private void dysplay() {
            updateMessage("Рисование начато");
            while (true) {
                while (!ended) {
                    if (isCancelled()) {
                        updateMessage("Рисование прервано");
                        return;
                    }
                    if (!tempSTR.equals(stringBuilder.toString())) {
                        Platform.runLater(() -> {
                            stackTraceTextArea.setText(stringBuilder.toString());
                            stackTraceTextArea.setScrollTop(Double.MAX_VALUE);
                        });
                        tempSTR = stringBuilder.toString();
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException interrupted) {
                        if (isCancelled()) {
                            updateMessage("Рисование прервано");
                            return;
                        }
                    }
                }
                if (stopByUser) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Процесс работы алгоритма останавлен пользователем");
                        alert.showAndWait();
                    });
                    Date date = new Date();
                    stringBuilder.append("Процесс работы алгоритма останавлен пользователем в ").append(date).append("\n");
                }
                Date date = new Date();
                stringBuilder.append("Завершение выполнения алгоритма в ").append(date).append("\n");
                stringBuilder.append("Новая итерация " + "\n");
                stackTraceTextArea.setText(stringBuilder.toString());

                ended = false;
                stopByUser = false;

                try {
                    Thread.sleep(200);
                } catch (InterruptedException interrupted) {
                    if (isCancelled()) {
                        updateMessage("Рисование прервано");
                        return;
                    }
                }
            }
        }

        @Override
        protected void updateMessage(String message) {
            super.updateMessage(message);
        }
    }
}
