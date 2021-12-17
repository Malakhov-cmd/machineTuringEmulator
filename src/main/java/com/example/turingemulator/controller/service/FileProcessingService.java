package com.example.turingemulator.controller.service;

import com.example.turingemulator.controller.MainController;
import com.example.turingemulator.data.Rule;
import com.example.turingemulator.exception.file.IncorrectFileData;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessingService {
    private MainController mainController;

    public FileProcessingService(MainController mainController) {
        this.mainController = mainController;
    }

    public void saveLentData(File file) throws IncorrectFileData {
        try {
            StringBuilder builder = new StringBuilder();
            List<String> lentList = mainController.getLentData().getListLentData();
            for (int i = 0; i < 201; i++) {
                if (i > 101 && lentList.get(i).equals("_")) {
                    break;
                }
                builder.append(lentList.get(i));
            }
            try {
                PrintWriter writer;
                writer = new PrintWriter(file);
                writer.println(builder);
                writer.close();
            } catch (IOException ex) {
                System.err.println("System error");
            }
        } catch (Exception e) {
            throw new IncorrectFileData();
        }
    }

    public void saveAlgorithmData(String content, File file) throws IncorrectFileData {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            System.err.println("System error");
        } catch (Exception e) {
            throw new IncorrectFileData();
        }
    }

    public static String readUsingFiles(File file) throws IOException {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран");
            alert.showAndWait();
        }
        return null;
    }

    public void analizeFileSTR(String fileSTR) throws IncorrectFileData {
        try {
            if (fileSTR != null) {
                if (fileSTR.charAt(0) != 'q') {
                    for (int i = 0; i < fileSTR.length() - 2; i++) {
                        mainController.getLentData().getListLentData().set((i), String.valueOf(fileSTR.charAt(i)));
                        if (i > 101) {
                            mainController.getView().listLentColumns
                                    .get(i).setVisible(true);
                        }
                    }
                    mainController.getLentData().setEnder(fileSTR.length() - 2);
                    mainController.getView().currentLentTable.filler(mainController.getLentData());
                    mainController.getView().currentLentTable.update();
                } else {
                    mainController.getView().newFileAlgorithmMenu.fire();

                    //Узнаем сколько строк
                    Matcher m = Pattern.compile("\r\n|\r|\n").matcher(fileSTR);
                    int lines = 1;
                    while (m.find()) {
                        lines++;
                    }
                    //Анализируем каждую строку
                    Pattern pattern = Pattern.compile("\\;|(\\;\\n)");
                    String[] rule = pattern.split(fileSTR);


                    String enderIndexSTR = rule[rule.length - 1].replaceAll("\\n|\\s", "");
                    int enderIndex = Integer.parseInt(enderIndexSTR);

                    for (int i = 0; i < enderIndex - 2; i++) {
                        mainController.getView().addColumnFromRightSide();
                    }

                    int counter = 0;
                    for (int i = 0; i < lines - 2; i++) {
                        for (int j = 0; j < 20; j++) {
                            Rule ruleData = mainController.getRowConditions().get(i).getListRules().get(j);
                            String temp = rule[counter];
                            String clearTemp = temp.replaceAll("\\n", "");

                            Pattern patternDetermination = Pattern.compile("\\|");
                            String[] ruleElements = patternDetermination.split(clearTemp);

                            ruleData.reset(ruleElements[0], ruleElements[1], ruleElements[2], ruleElements[3], ruleElements[4], ruleElements[5]);
                            mainController.getRowConditions().get(i).getListRules().set(j, ruleData);
                            counter++;
                        }
                        mainController.getRowConditions().get(i).setSymbolLine(mainController.getRowConditions().get(i).getListRules().get(0).getSymbol());
                    }

                    //mainController.getView().currentRuleTable.filler(mainController.getRowConditions());
                    //mainController.getView().currentRuleTable.update();
                }
            }
        } catch (Exception e) {
            throw new IncorrectFileData();
        }
    }
}
