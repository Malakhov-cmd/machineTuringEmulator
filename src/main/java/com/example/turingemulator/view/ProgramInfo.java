package com.example.turingemulator.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProgramInfo extends Application implements Initializable {
    public Button closeBtn;
    private static Stage pStage;

    @Override
    public void start(Stage primaryStage) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);

        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource("/com/example/turingemulator/AboutProgramVishual.fxml");
        loader.setLocation(xmlUrl);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pStage = stage;

        Scene scene = new Scene(root);
        stage.getIcons().add(new Image("/icon.png"));
        stage.setTitle("Информация о создателях");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.showAndWait();
    }

    public static Stage getPrimaryStage() {
        return pStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeBtn.setOnAction(event -> {
            getPrimaryStage().close();
        });
    }
}
