package com.example.turingemulator.exception.analizator;

public class IncorrectLEntConditionException extends Exception{
    public IncorrectLEntConditionException() {
        super("Не верное исходное состояние ленты. \n" +
                "После операндов присутсвуют символы");
    }
}
