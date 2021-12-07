package com.example.turingemulator.exception.lentCellOperation;

public class IndexOfLentHeaderOutOfBoundException extends Exception{
    public IndexOfLentHeaderOutOfBoundException() {
        super("Индекс пишущей головки находится за допустимыми пределами");
    }
}
