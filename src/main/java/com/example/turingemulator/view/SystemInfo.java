package com.example.turingemulator.view;

import javafx.application.Application;
import javafx.scene.Scene;
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

        File html = getResource();
        URL url = null;
        try {
            url =html.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        webEngine.load(url.toString());

        VBox root = new VBox();
        root.getChildren().addAll(browser);

        Scene scene = new Scene(root);
        stage.getIcons().add(new Image("/icon.png"));
        stage.setTitle("Информация о сестеме");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.showAndWait();
    }

    public File getResource() {
        URL resource = getClass().getClassLoader().getResource("SystemInfo.html");
        if (resource == null) {
            System.err.println("System Error");
        } else {
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
