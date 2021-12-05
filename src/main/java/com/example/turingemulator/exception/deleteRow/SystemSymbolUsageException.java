package com.example.turingemulator.exception.deleteRow;

public class SystemSymbolUsageException extends Exception{
    public SystemSymbolUsageException() {
        super("Этот символ используется в системе");
    }
}
