package com.example.turingemulator.exception.addRow;

public class AddingIncorrectSymbolException extends Exception {
    public AddingIncorrectSymbolException(){
        super("Пожалуйста, введите правильные данные или удалите одну строку, если количество строк увеличится на 5");
    }
}
