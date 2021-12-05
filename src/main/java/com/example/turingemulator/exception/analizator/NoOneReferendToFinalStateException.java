package com.example.turingemulator.exception.analizator;

public class NoOneReferendToFinalStateException extends Exception{
    public NoOneReferendToFinalStateException() {
        super("Ни одно из правил не ссылается на конечное состояние");
    }
}
