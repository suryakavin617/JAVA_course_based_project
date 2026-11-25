package com.polarlog.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/** All file handling (java.io): byte streams with serialization, and character streams for text. */
public class FileStorage {
    private FileStorage() { }

    private static void createParentFolder(File file) {
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    /** Byte stream + serialization: saves the whole DataStore object. */
    public static void save(DataStore store, String path) throws IOException {
        File file = new File(path);
        createParentFolder(file);
        try (ObjectOutputStream out =
                 new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeObject(store);
        }
    }

    /** Reads the DataStore back. Returns an empty store if there is no file yet. */
    public static DataStore load(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new DataStore();
        }
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return (DataStore) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not read saved data (" + e.getMessage() + "). Starting with empty data.");
            return new DataStore();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // nothing more to do
                }
            }
        }
    }

    /** Character stream: adds one line at the end of a text file. */
    public static void appendLine(String path, String line) throws IOException {
        File file = new File(path);
        createParentFolder(file);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(line);
            writer.newLine();
        }
    }

    /** Character stream: writes a whole text file (replaces old content). */
    public static void writeText(String path, String text) throws IOException {
        File file = new File(path);
        createParentFolder(file);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(text);
        }
    }

    /** Character stream: reads all lines of a text file. */
    public static List<String> readLines(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }

    public static boolean delete(String path) {
        File file = new File(path);
        return file.exists() && file.delete();
    }
}
