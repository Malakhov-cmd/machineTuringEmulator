package com.example.turingemulator.controller.updaters;

import com.example.turingemulator.data.LentData;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class LentUpdater {
    private static TableView lentTable;
    private static ObservableList<LentData> listObserverLent;

    public LentUpdater() {}

    public LentUpdater(TableView table, ObservableList<LentData> list) {
        lentTable = table;
        listObserverLent = list;
    }

    public void filler(LentData lentData) {
        listObserverLent.clear();
        listObserverLent.add(lentData);
    }

    public void update() {
        try {
            lentTable.getItems().removeAll();
            lentTable.setItems(listObserverLent);
        } catch (NullPointerException e) {
            System.out.println("potential empty lent");
        }
    }
}
