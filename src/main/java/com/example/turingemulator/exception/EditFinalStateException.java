package com.example.turingemulator.exception;

public class EditFinalStateException extends Exception{
    public EditFinalStateException(){
        super("Изменение последнего столбца недопустимо");
    }
}
