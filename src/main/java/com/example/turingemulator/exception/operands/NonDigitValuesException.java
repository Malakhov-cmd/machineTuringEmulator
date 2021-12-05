package com.example.turingemulator.exception.operands;

public class NonDigitValuesException extends Exception{
    public NonDigitValuesException(){
        super("Введите корректные значение операндов - целые числа");
    }
}
