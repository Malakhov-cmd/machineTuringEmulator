package com.example.turingemulator.view;

import com.example.turingemulator.exception.file.FileSystemInfoNotFoundException;
import com.example.turingemulator.exception.file.FileSystemInfoOpeningException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class SystemInfo extends Application {
    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();

    @Override
    public void start(Stage primaryStage) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);

        File html = null;
        try {
            html = getResource();
        } catch (FileSystemInfoNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Отсутствует файл справки");
            alert.showAndWait();
        }
        catch (FileSystemInfoOpeningException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл справки поврежден");
            alert.showAndWait();
        }
        URL url = null;
        try {
            url =html.toURI().toURL();
        } catch (MalformedURLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Неверное преобразование файла в URL");
            alert.showAndWait();
        }
        webEngine.load(url.toString());

        VBox root = new VBox();
        root.getChildren().addAll(browser);

        Scene scene = new Scene(root);
        stage.getIcons().add(new Image("/icon.png"));
        stage.setTitle("Информация о системе");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.showAndWait();
    }

    public File getResource() throws FileSystemInfoNotFoundException, FileSystemInfoOpeningException {
        URL resource = getClass().getClassLoader().getResource("SystemInfo.html");
        if (resource == null) {
            throw new FileSystemInfoNotFoundException();
        } else {
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                throw new FileSystemInfoOpeningException();
            }
        }
    }
}
