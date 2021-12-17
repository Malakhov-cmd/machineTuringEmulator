package com.example.turingemulator.exception.file;

public class FileDoesntExist extends Exception {
    public FileDoesntExist() {
        super("Файл с заданным именем не существует");
    }
}
