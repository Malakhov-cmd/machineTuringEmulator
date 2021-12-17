package com.example.turingemulator.exception.file;

import java.io.IOException;

public class FileSystemInfoOpeningException extends IOException {
    public FileSystemInfoOpeningException() {
        super("Файл справки поврежден");
    }
}
