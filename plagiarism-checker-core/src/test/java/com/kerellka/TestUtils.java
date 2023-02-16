package com.kerellka;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class TestUtils {

    public static CompilationUnit getCompilationUnitFromResource(String resourcePath) {
        var resource = TestUtils.class.getResourceAsStream(resourcePath);
        return StaticJavaParser.parse(resource);
    }

}
