package com.example.turingemulator.exception.analizator;

public class EmptyLentDataException extends Exception{
    public EmptyLentDataException() {
        super("На ленте нет введенных значений");
    }
}
