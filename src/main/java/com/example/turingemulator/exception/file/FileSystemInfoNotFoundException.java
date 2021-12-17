package com.example.turingemulator.exception.file;

import java.io.FileNotFoundException;

public class FileSystemInfoNotFoundException extends FileNotFoundException {
    public FileSystemInfoNotFoundException() {
        super("Отсутствует файл справки");
    }
}
