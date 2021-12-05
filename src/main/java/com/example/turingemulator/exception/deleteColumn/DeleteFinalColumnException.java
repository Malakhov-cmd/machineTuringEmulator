package com.example.turingemulator.exception.deleteColumn;

public class DeleteFinalColumnException extends Exception{
    public DeleteFinalColumnException(){
        super("Удаление последнего столбца недопустимо");
    }
}
