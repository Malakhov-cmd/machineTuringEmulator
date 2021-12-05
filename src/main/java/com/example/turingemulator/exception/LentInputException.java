package com.example.turingemulator.exception;

public class LentInputException extends Exception {
    public LentInputException() {
        super("Неверные данные, возможно такого символа нет в состояниях");
    }
}
