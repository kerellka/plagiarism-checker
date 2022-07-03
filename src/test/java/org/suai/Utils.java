package org.suai;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class Utils {

    public static CompilationUnit getCompilationUnitFromResource(String resourcePath) {
        var resource = Utils.class.getResourceAsStream(resourcePath);
        return StaticJavaParser.parse(resource);
    }

}
