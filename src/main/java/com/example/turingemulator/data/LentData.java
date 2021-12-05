package com.example.turingemulator.data;

import java.util.ArrayList;
import java.util.List;

public class LentData {
    private List<String> listLentData;
    private int ender;

    public LentData() {
        this.ender = 101;
        listLentData = new ArrayList<>();

        for (int i = 0; i < 201; i++) {
            listLentData.add("_");
        }
    }

    public LentData(List<String> listLentData) {
        this.listLentData = listLentData;
    }

    public List<String> getListLentData() {
        return listLentData;
    }

    public void setListLentData(List<String> listLentData) {
        this.listLentData = listLentData;
    }

    public int getEnder() {
        return ender;
    }

    public void setEnder(int ender) {
        this.ender = ender;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listLentData.size(); i++) {
            if (ender > i) {
                sb.append(listLentData.get(i));
            }
        }
        return sb.toString();
    }
}
