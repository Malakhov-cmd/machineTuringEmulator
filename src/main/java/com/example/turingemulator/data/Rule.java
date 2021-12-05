package com.example.turingemulator.data;

public class Rule {
    private String symbol;
    private String conditionFrom;

    private String conditionTo;
    private String symbolChangeTo;
    private String moveTo;

    private String description;

    public Rule(String symbol, String conditionFrom) {
        this.symbol = symbol;
        this.conditionFrom = conditionFrom;

        conditionTo = " ";
        symbolChangeTo = " ";
        moveTo = " ";

        description = " ";
    }

    public Rule(String symbol, String conditionFrom, String conditionTo, String symbolChangeTo, String moveTo, String description) {
        this.symbol = symbol;
        this.conditionFrom = conditionFrom;
        this.conditionTo = conditionTo;
        this.symbolChangeTo = symbolChangeTo;
        this.moveTo = moveTo;
        this.description = description;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getConditionFrom() {
        return conditionFrom;
    }

    public void setConditionFrom(String conditionFrom) {
        this.conditionFrom = conditionFrom;
    }

    public String getConditionTo() {
        return conditionTo;
    }

    public void setConditionTo(String conditionTo) {
        this.conditionTo = conditionTo;
    }

    public String getSymbolChangeTo() {
        return symbolChangeTo;
    }

    public void setSymbolChangeTo(String symbolChangeTo) {
        this.symbolChangeTo = symbolChangeTo;
    }

    public String getMoveTo() {
        return moveTo;
    }

    public void setMoveTo(String moveTo) {
        this.moveTo = moveTo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void reset(String conditionTo,
                      String symbolChangeTo,
                      String moveTo) {
        this.conditionTo = conditionTo;
        this.symbolChangeTo = symbolChangeTo;
        this.moveTo = moveTo;
    }

    public void reset(String conditionTo,
                      String symbolChangeTo,
                      String moveTo,
                      String description) {
        this.conditionTo = conditionTo;
        this.symbolChangeTo = symbolChangeTo;
        this.moveTo = moveTo;
        this.description = description;
    }

    public void reset(String conditionFrom,
                      String symbol,
                      String conditionTo,
                      String symbolChangeTo,
                      String moveTo,
                      String description) {
        this.conditionFrom = conditionFrom;
        this.symbol = symbol;
        this.conditionTo = conditionTo;
        this.symbolChangeTo = symbolChangeTo;
        this.moveTo = moveTo;

        this.description = description;
    }

    @Override
    public String toString() {
        if (!conditionTo.equals(" ")
                && !symbolChangeTo.equals(" ")
                && !moveTo.equals(" ")) {
            return conditionTo + '|' +
                    symbolChangeTo + '|' +
                    moveTo;
        }
        return "";
    }
}
