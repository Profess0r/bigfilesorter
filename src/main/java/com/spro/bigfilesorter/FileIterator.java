package com.spro.bigfilesorter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;

public class FileIterator implements Closeable {

    private String fileName;
    private BufferedReader bufferedReader;
    private int topValue;

    FileIterator(String fileName) {
        this.fileName = fileName;
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            String line = bufferedReader.readLine();
            topValue = Integer.valueOf(line);
        } catch (IOException e) {
            throw new RuntimeException("Can't create iterator for file " + fileName, e);
        }
    }

    public int peek() {
        return topValue;
    }

    public boolean hasNext() {
        try {
            String line = bufferedReader.readLine();
            if (line == null) {
                return false;
            }
            topValue = Integer.valueOf(line);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Can't read from file " + fileName, e);
        }
    }

    @Override
    public void close() {
        try {
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException("Can't close file " + fileName, e);
        }
    }

    public String getFileName() {
        return fileName;
    }
}
