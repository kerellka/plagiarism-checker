package org.suai.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import org.suai.core.PlagiarismEqualsVisitor;

public class PlagiarismChecker {

    public static long countPlagiarismPercentage(CompilationUnit left, CompilationUnit right) {
        return PlagiarismEqualsVisitor.countPlagiarismPercentage(left, right);
    }

}
