package com.kerellka;

import com.github.javaparser.printer.DotPrinter;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;

public class ParserExamples {

    @Test
    public void basicExampleDotOutput() throws IOException {
        var cu1 = TestUtils.getCompilationUnitFromResource("/comparison/MainA.java");
        var cu2 = TestUtils.getCompilationUnitFromResource("/comparison/MainB.java");

        DotPrinter printer = new DotPrinter(true);
        var fw1 = new FileWriter("MainA_AST.dot");
        fw1.write(printer.output(cu1));
        fw1.flush();
        var fw2 = new FileWriter("MainB_AST.dot");
        fw2.write(printer.output(cu2));
        fw2.flush();
    }

}
