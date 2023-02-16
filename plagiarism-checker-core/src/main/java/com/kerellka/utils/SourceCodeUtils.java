package com.kerellka.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class SourceCodeUtils {

    public static String concatFiles(String[] filePaths) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(filePaths).forEach(path -> {
            try {
                var fileIs = new FileInputStream(path);
                sb.append(new String(fileIs.readAllBytes(), StandardCharsets.UTF_8));
                sb.append("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return sb.toString();
    }

    public static String concatFilesFromProject(String pathToProjectRoot) {
        File rootProjectDir = new File(pathToProjectRoot);
        if (!rootProjectDir.isDirectory()) {
            throw new RuntimeException("provided path is not a directory");
        }

        StringBuilder sb = new StringBuilder();
        try {
            Files.walk(rootProjectDir.toPath())
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            FileInputStream fileIs = new FileInputStream(path.toString());
                            sb.append(new String(fileIs.readAllBytes(), StandardCharsets.UTF_8));
                            sb.append("\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
