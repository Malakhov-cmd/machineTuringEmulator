package com.example.turingemulator.controller.updaters;

import com.example.turingemulator.data.RowCondition;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.util.List;

public class RuleUpdater {
    private static TableView ruleTable;
    private static ObservableList<RowCondition> listObserveRule;

    public RuleUpdater() {}

    public RuleUpdater(TableView table, ObservableList<RowCondition> list) {
        ruleTable = table;
        listObserveRule = list;
    }

    public void filler(List<RowCondition> ruleData) {
        listObserveRule.clear();
        listObserveRule.addAll(ruleData);
    }

    public void update() {
        try {
            ruleTable.getItems().removeAll();
            ruleTable.setItems(listObserveRule);
        } catch (NullPointerException e) {
            System.out.println("potential empty table");
        }
    }
}
