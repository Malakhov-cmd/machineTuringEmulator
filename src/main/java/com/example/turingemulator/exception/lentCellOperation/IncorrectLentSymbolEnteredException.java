package com.example.turingemulator.exception.lentCellOperation;

public class IncorrectLentSymbolEnteredException extends Exception{
    public IncorrectLentSymbolEnteredException() {
        super("Введен некорректный символ");
    }
}
