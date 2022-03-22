package org.suai;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.DotPrinter;
import org.junit.jupiter.api.Test;
import java.io.FileWriter;
import java.io.IOException;

public class ParserExamples {

    @Test
    public void basicExampleDotOutput() throws IOException {
        var cu1 = getCompilationUnitFromResource("/Calculator1.java");
        var cu2 = getCompilationUnitFromResource("/Calculator2.java");
        DotPrinter printer = new DotPrinter(true);
        var fw1 = new FileWriter("Calculator1_AST.dot");
        fw1.write(printer.output(cu1));
        fw1.flush();
        var fw2 = new FileWriter("Calculator2_AST.dot");
        fw2.write(printer.output(cu2));
        fw2.flush();
    }

    private CompilationUnit getCompilationUnitFromResource(String resourcePath) {
        var resource = getClass().getResourceAsStream(resourcePath);
        return StaticJavaParser.parse(resource);
    }

}
