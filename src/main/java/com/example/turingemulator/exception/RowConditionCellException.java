package com.example.turingemulator.exception;

public class RowConditionCellException extends Exception {
    public RowConditionCellException(){
        super("Неверный формат ввода правил");
    }
}
