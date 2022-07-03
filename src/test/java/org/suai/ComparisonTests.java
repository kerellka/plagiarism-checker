package org.suai;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.suai.core.PlagiarismChecker;
import org.suai.parser.utils.IOTreeUtils;

import java.io.IOException;
import java.util.stream.Stream;

public class ComparisonTests {

    @ParameterizedTest
    @MethodSource("provideFilePathToCompare2")
    public void astStructureComparisonTest(String leftFilePath, String rightFilePath) throws IOException {

        var leftTree = TestUtils.getCompilationUnitFromResource(leftFilePath);
        var rightTree = TestUtils.getCompilationUnitFromResource(rightFilePath);


        String jsonAST = IOTreeUtils.writeToJSON(leftTree);
        CompilationUnit left = (CompilationUnit) IOTreeUtils.readFromJSON(jsonAST);
        String jsonASTR = IOTreeUtils.writeToJSON(rightTree);
        CompilationUnit right = (CompilationUnit) IOTreeUtils.readFromJSON(jsonASTR);
        System.out.print(leftFilePath + ": ");
        System.out.println("Plagiarism percentage = " + PlagiarismChecker.countPlagiarismPercentage(left, right) + "%");

    }

    private static Stream<Arguments> provideFilePathToCompare() {
        return Stream.of(
                Arguments.of(
                        "/comparison/MainA.java",
                        "/comparison/MainB.java"
                ),
                Arguments.of(
                        "/comparison/CalculatorA.java",
                        "/comparison/CalculatorA.java"),
                Arguments.of(
                        "/comparison/CalculatorA.java",
                        "/comparison/CalculatorB.java"
                ),
                Arguments.of(
                        "/comparison/PrinterA.java",
                        "/comparison/PrinterB.java"
                ),
                Arguments.of(
                        "/comparison/PrinterB.java",
                        "/comparison/PrinterA.java"
                )
        );//.skip(4).limit(1);
    }

    private static Stream<Arguments> provideFilePathToCompare2() {
        return Stream.of(
                Arguments.of(
                        "/alg_comparison/add_rm_lines/Calculator1.java",
                        "/alg_comparison/add_rm_lines/Calculator2.java"
                ),
                Arguments.of(
                        "/alg_comparison/add_rm_lines/Printer1.java",
                        "/alg_comparison/add_rm_lines/Printer2.java"
                ),
                Arguments.of(
                        "/alg_comparison/dependency_graph_attack/Tmp1.java",
                        "/alg_comparison/dependency_graph_attack/Tmp2.java"
                ),
                Arguments.of(
                        "/alg_comparison/extract_constant/Const1.java",
                        "/alg_comparison/extract_constant/Const2.java"
                ),
                Arguments.of(
                        "/alg_comparison/extract_method/Tmp1.java",
                        "/alg_comparison/extract_method/Tmp2.java"
                ),
                Arguments.of(
                        "/alg_comparison/extract_param/Field1.java",
                        "/alg_comparison/extract_param/Field2.java"
                ),
                Arguments.of(
                        "/alg_comparison/for_while/ForWhile1.java",
                        "/alg_comparison/for_while/ForWhile2.java"
                ),
                Arguments.of(
                        "/alg_comparison/log_expr_swap/Main1.java",
                        "/alg_comparison/log_expr_swap/Main2.java"
                ),
                Arguments.of(
                        "/alg_comparison/moving/Calculator1.java",
                        "/alg_comparison/moving/Calculator2.java"
                ),
                Arguments.of(
                        "/alg_comparison/renaming/Calculator1.java",
                        "/alg_comparison/renaming/Calculator2.java"
                ),
                Arguments.of(
                        "/alg_comparison/performance/PlagiarismEqualsVisitor1.java",
                        "/alg_comparison/performance/PlagiarismEqualsVisitor2.java"
                )
        ).skip(8).limit(1);
    }


}
