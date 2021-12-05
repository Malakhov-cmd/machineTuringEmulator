package com.example.turingemulator.exception.deleteColumn;

public class MinimumColumnSize extends Exception{
    public MinimumColumnSize(){
        super("Минимальное количество столбов должно ровняться двум");
    }
}
