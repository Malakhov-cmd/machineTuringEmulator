package com.example.turingemulator.exception.analizator;

public class EmptyInitialRuleException extends Exception {
    public EmptyInitialRuleException() {
        super("Первое правило не должно быть пустым");
    }
}
