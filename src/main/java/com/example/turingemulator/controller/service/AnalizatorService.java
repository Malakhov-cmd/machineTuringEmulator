package com.example.turingemulator.controller.service;

import com.example.turingemulator.controller.MainController;
import com.example.turingemulator.controller.updaters.PositionUpdater;
import com.example.turingemulator.data.LentData;
import com.example.turingemulator.data.RowCondition;
import com.example.turingemulator.data.Rule;
import com.example.turingemulator.exception.analizator.EmptyInitialRuleException;
import com.example.turingemulator.exception.analizator.EmptyLentDataException;
import com.example.turingemulator.exception.analizator.NoOneReferendToFinalStateException;
import javafx.scene.control.Alert;

import java.util.List;

public class AnalizatorService {
    private MainController mainController;

    public AnalizatorService(MainController mainController) {
        this.mainController = mainController;
    }

    public boolean analizator() throws
            EmptyInitialRuleException,
            NoOneReferendToFinalStateException,
            EmptyLentDataException
    {
        boolean isEmpty = mainController.getLentData().getListLentData().stream().allMatch(e -> e.equals("_"));

        //проверка наличия первого правила
        if (mainController.getRowConditions().get(0).getListRules().get(0).getConditionTo().equals(" ")
                || mainController.getRowConditions().get(0).getListRules().get(0).getSymbolChangeTo().equals(" ")
                || mainController.getRowConditions().get(0).getListRules().get(0).getMoveTo().equals(" ")) {
            throw new EmptyInitialRuleException();
        } else {
            boolean flagEndExist = false;

            for (RowCondition condition :
                    mainController.getRowConditions()) {
                for (Rule rule :
                        condition.getListRules()) {
                    String conditionTo = rule.getConditionTo();
                    int newConditionInt = -1;
                    //заполнено ли правило
                    if (conditionTo.length() > 1) {
                        if (conditionTo.length() == 2) {
                            newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 1));
                        } else {
                            newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 2));
                        }
                    }

                    if (newConditionInt == condition.getEnderIndex()-1) {
                        flagEndExist = true;
                    }
                }
            }

            if (!flagEndExist) {
                throw new NoOneReferendToFinalStateException();
            }
            if (isEmpty) {
                throw new EmptyLentDataException();
            }
        }
        return true;
    }
}
