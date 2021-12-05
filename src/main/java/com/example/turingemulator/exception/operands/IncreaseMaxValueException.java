package com.example.turingemulator.exception.operands;

public class IncreaseMaxValueException extends Exception{
    public IncreaseMaxValueException() {
        super("Максимальное значение операнда составляет 50");
    }
}
