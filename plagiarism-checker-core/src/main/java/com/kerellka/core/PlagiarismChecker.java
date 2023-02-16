package com.kerellka.core;

import com.github.javaparser.ast.CompilationUnit;

public class PlagiarismChecker {

    public static long countPlagiarismPercentage(CompilationUnit left, CompilationUnit right) {
        return PlagiarismEqualsVisitor.countPlagiarismPercentage(left, right);
    }

}
