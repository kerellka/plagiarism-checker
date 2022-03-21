package org.suai;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.printer.DotPrinter;
import guru.nidi.graphviz.engine.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class ParserExamples {

    @Test
    public void basicExample() throws IOException {
        var javaFile1 = getClass().getResourceAsStream("/Main1.java");
        var javaFile2 = getClass().getResourceAsStream("/Main2.java");
        var cu1 = StaticJavaParser.parse(javaFile1);
        var cu2 = StaticJavaParser.parse(javaFile2);
        DotPrinter printer = new DotPrinter(true);
        Graphviz.fromString(printer.output(cu1)).render(Format.PNG).toFile(new File("Main1_AST.png"));
        Graphviz.fromString(printer.output(cu2)).render(Format.PNG).toFile(new File("Main2_AST.png"));
    }

}
