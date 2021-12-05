package com.example.turingemulator.exception.deleteRow;

public class SymbolRowsUseInAnotherTerms extends Exception {
    public SymbolRowsUseInAnotherTerms(){
        super("Этот символ используется в других выражениях");
    }
}
