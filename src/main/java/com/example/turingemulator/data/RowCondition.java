package com.example.turingemulator.data;

import java.util.ArrayList;
import java.util.List;

public class RowCondition {
    private List<Rule> listRules;

    private String symbolLine;
    private boolean isAction;
    private int enderIndex;

    public RowCondition(){

    }

    public RowCondition(String symbolLine) {
        listRules = new ArrayList<>();

        this.symbolLine = symbolLine;

        enderIndex = 2;
        isAction = false;

        for (int i = 0; i < 21; i++) {
            Rule rule = new Rule(symbolLine, "q" + i);
            listRules.add(rule);
        }
    }

    public List<Rule> getListRules() {
        return listRules;
    }

    public void setListRules(List<Rule> listRules) {
        this.listRules = listRules;
    }

    public String getSymbolLine() {
        return symbolLine;
    }

    public void setSymbolLine(String symbolLine) {
        this.symbolLine = symbolLine;
    }

    public boolean isAction() {
        return isAction;
    }

    public void setAction(boolean action) {
        isAction = action;
    }

    public int getEnderIndex() {
        return enderIndex;
    }

    public void setEnderIndex(int enderIndex) {
        this.enderIndex = enderIndex;
    }
}
