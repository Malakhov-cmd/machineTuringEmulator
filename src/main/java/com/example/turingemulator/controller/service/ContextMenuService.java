package com.example.turingemulator.controller.service;

import com.example.turingemulator.data.RowCondition;

import java.util.List;

public class ContextMenuService {

    public ContextMenuService() {}

    public List<RowCondition> addRightColumn(int positionAdded, List<RowCondition> rowConditions) {
        for (int i = 0; i < rowConditions.size(); i++) {
            for (int j = rowConditions.get(i).getEnderIndex(); j > positionAdded + 1; j--) {
                String conditionTo = rowConditions.get(i).getListRules().get(j - 2).getConditionTo();
                try {
                    int newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 1)) + 1;

                    String newCondition = String.valueOf(newConditionInt);
                    conditionTo = conditionTo.substring(0, conditionTo.length() - 1);
                    conditionTo = conditionTo + newCondition;
                } catch (NumberFormatException ignored) {
                }

                String symbolChangeTo = rowConditions.get(i).getListRules().get(j - 2).getSymbolChangeTo();
                String moveTo = rowConditions.get(i).getListRules().get(j - 2).getMoveTo();
                String description = rowConditions.get(i).getListRules().get(j - 2).getDescription();

                rowConditions.get(i).getListRules().get(j - 1).reset(
                        conditionTo,
                        symbolChangeTo,
                        moveTo,
                        description
                );

                rowConditions.get(i).getListRules().get(j - 2).reset(" ", " ", " ", " ");
            }
        }
        return rowConditions;
    }

    public List<RowCondition> addLeftColumn(int positionAdded, List<RowCondition> rowConditions) {
        for (int i = 0; i < rowConditions.size(); i++) {
            for (int j = rowConditions.get(i).getEnderIndex(); j > positionAdded + 1; j--) {
                String conditionTo = rowConditions.get(i).getListRules().get(j - 2).getConditionTo();

                try {
                    int newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 1)) + 1;

                    String newCondition = String.valueOf(newConditionInt);
                    conditionTo = conditionTo.substring(0, conditionTo.length() - 1);
                    System.out.println(conditionTo);
                    conditionTo = conditionTo + newCondition;
                } catch (NumberFormatException ignored) {
                }

                String symbolChangeTo = rowConditions.get(i).getListRules().get(j - 2).getSymbolChangeTo();
                String moveTo = rowConditions.get(i).getListRules().get(j - 2).getMoveTo();
                String description = rowConditions.get(i).getListRules().get(j - 2).getDescription();

                rowConditions.get(i).getListRules().get(j - 1).reset(
                        conditionTo,
                        symbolChangeTo,
                        moveTo,
                        description
                );

                rowConditions.get(i).getListRules().get(j - 2).reset(" ", " ", " ", " ");
            }
        }
        return rowConditions;
    }

    public List<RowCondition> deleteColumnAlgorithm(int positionDel, List<RowCondition> rowConditions) {
        for (int i = 0; i < rowConditions.size(); i++) {
            for (int j = positionDel; j < rowConditions.get(0).getEnderIndex() - 1; j++) {
                String conditionTo = rowConditions.get(i).getListRules().get(j + 1).getConditionTo();
                try {
                    int newConditionInt = Integer.parseInt(conditionTo.substring(conditionTo.length() - 1)) - 1;

                    String newCondition = String.valueOf(newConditionInt);
                    conditionTo = conditionTo.substring(0, conditionTo.length() - 1);
                    System.out.println(conditionTo);
                    conditionTo = conditionTo + newCondition;
                } catch (NumberFormatException ignored) {
                }

                String symbolChangeTo = rowConditions.get(i).getListRules().get(j + 1).getSymbolChangeTo();
                String moveTo = rowConditions.get(i).getListRules().get(j + 1).getMoveTo();
                String description = rowConditions.get(i).getListRules().get(j + 1).getDescription();

                rowConditions.get(i).getListRules().get(j).reset(
                        conditionTo,
                        symbolChangeTo,
                        moveTo,
                        description
                );
                rowConditions.get(i).getListRules().get(j + 1).reset(" ", " ", " ", " ");
            }
        }
        return rowConditions;
    }
}
