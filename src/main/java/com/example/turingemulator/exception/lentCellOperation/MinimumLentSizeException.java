package com.example.turingemulator.exception.lentCellOperation;

public class MinimumLentSizeException extends Exception {
    public MinimumLentSizeException() {
        super("Минимальный размер длины ленты - 100");
    }
}
