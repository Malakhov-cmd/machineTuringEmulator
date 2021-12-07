package com.example.turingemulator.controller.service;

import com.example.turingemulator.controller.MainController;
import com.example.turingemulator.data.Rule;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessingService {
    private MainController mainController;

    public FileProcessingService(MainController mainController) {
        this.mainController = mainController;
    }

    public void saveLentData(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            System.err.println("System error");
        }
    }

    public void saveAlgorithmData(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            System.err.println("System error");
        }
    }

    public static String readUsingFiles(File file) throws IOException {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (NullPointerException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран");
            alert.showAndWait();
        }
        return null;
    }

    public void analizeFileSTR(String fileSTR) {
        if (fileSTR != null) {
            if (fileSTR.charAt(0) != 'q') {
                System.out.println(fileSTR.length());
                for (int i = 0; i < fileSTR.length() - 2; i++) {
                    System.out.println(fileSTR.charAt(i));
                    mainController.getLentData().getListLentData().set((i), String.valueOf(fileSTR.charAt(i)));
                }

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

                //Arrays.asList(rule).forEach(animal -> System.out.println(animal + " "));
                int counter = 0;
                for (int i = 0; i < lines - 2; i++) {
                    for (int j = 0; j < 20; j++) {
                        Rule ruleData = mainController.getRowConditions().get(i).getListRules().get(j);
                        String temp = rule[counter];
                        String clearTemp = temp.replaceAll("\\n", "");

                        Pattern patternDetermination = Pattern.compile("\\|");
                        String[] ruleElements = patternDetermination.split(clearTemp);

                        //System.out.println(j + " " + temp);
                        //System.out.println(ruleElements[0] + " " + ruleElements[1]+ " " + ruleElements[2]+ " " + ruleElements[3]+ " " + ruleElements[4]+ " " + ruleElements[5]);

                        ruleData.reset(ruleElements[0], ruleElements[1], ruleElements[2], ruleElements[3], ruleElements[4], ruleElements[5]);

                        counter++;

                    }
                    //System.out.println(mainController.getRowConditions().get(i).getListRules().get(0).getSymbol());
                    mainController.getRowConditions().get(i).setSymbolLine(mainController.getRowConditions().get(i).getListRules().get(0).getSymbol());
                }

                mainController.getView().currentRuleTable.filler(mainController.getRowConditions());
                mainController.getView().currentRuleTable.update();
            }
        }
    }
}
