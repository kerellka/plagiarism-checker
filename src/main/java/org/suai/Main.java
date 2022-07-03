package org.suai;

import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PlagiarismCheckerRunner()).execute(args);
        System.exit(exitCode);
    }

}