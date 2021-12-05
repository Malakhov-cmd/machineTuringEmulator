package com.example.turingemulator.exception.addRow;

public class AlreadyBeingInSymbolsList extends Exception{
    public AlreadyBeingInSymbolsList() {
        super("Символ уже находится в списке");
    }
}
