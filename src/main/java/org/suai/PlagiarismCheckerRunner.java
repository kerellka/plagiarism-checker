package org.suai;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;
import org.suai.core.PlagiarismChecker;
import org.suai.persistence.ASTRepository;
import org.suai.parser.utils.IOTreeUtils;
import org.suai.persistence.JDBC;
import org.suai.parser.utils.SourceCodeUtils;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@CommandLine.Command(name = "PlagChecker")
public class PlagiarismCheckerRunner {

    @CommandLine.Command(name = "insertProjectInDb")
    void insertProjectInDb(@CommandLine.Option(names = {"--database-url", "-dburl"}) String dbUrl,
                           @CommandLine.Option(names = "--student-name") String studentName,
                           @CommandLine.Option(names = "--lab-number") int labNumber,
                           @CommandLine.Parameters(paramLabel = "pathToProjectRoot") String pathToProjectRoot) {
        try {
            var dbConnection = JDBC.getConnection(dbUrl);
            var astRepository = new ASTRepository(dbConnection);
            astRepository.initTable();
            astRepository.insertData(
                    studentName,
                    labNumber,
                    IOTreeUtils.writeProjectToJSONAsAST(pathToProjectRoot).getBytes(StandardCharsets.UTF_8),
                    SourceCodeUtils.concatFilesFromProject(pathToProjectRoot).getBytes(StandardCharsets.UTF_8)
            );
            System.out.println("ast inserted successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @CommandLine.Command(name = "insertInDb")
    void insertInDb(@CommandLine.Option(names = {"--database-url", "-dburl"}) String dbUrl,
                          @CommandLine.Option(names = "--student-name") String studentName,
                          @CommandLine.Option(names = "--lab-number") int labNumber,
                          @CommandLine.Parameters(arity = "1..*", paramLabel = "<sourceCodePaths>") String[] sourceCodePaths) {

        try {
            var dbConnection = JDBC.getConnection(dbUrl);
            var astRepository = new ASTRepository(dbConnection);
            astRepository.initTable();
            astRepository.insertData(
                    studentName,
                    labNumber,
                    IOTreeUtils.writeToJSON(sourceCodePaths).getBytes(StandardCharsets.UTF_8),
                    SourceCodeUtils.concatFiles(sourceCodePaths).getBytes(StandardCharsets.UTF_8)
            );
            System.out.println("ast inserted successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @CommandLine.Command(name = "updateInDb")
    void updateInDb(@CommandLine.Option(names = {"--database-url", "-dburl"}) String dbUrl,
                       @CommandLine.Option(names = "--student-name") String studentName,
                       @CommandLine.Option(names = "--lab-number") int labNumber,
                       @CommandLine.Parameters(arity = "1..*", paramLabel = "<sourceCodePaths>") String[] sourceCodePaths) {

        try {
            var dbConnection = JDBC.getConnection(dbUrl);
            var astRepository = new ASTRepository(dbConnection);
            astRepository.initTable();
            astRepository.updateData(
                    studentName,
                    labNumber,
                    IOTreeUtils.writeToJSON(sourceCodePaths).getBytes(StandardCharsets.UTF_8),
                    SourceCodeUtils.concatFiles(sourceCodePaths).getBytes(StandardCharsets.UTF_8)
            );
            System.out.println("ast updated successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @CommandLine.Command(name = "deleteInDb")
    void deleteFromDb(@CommandLine.Option(names = {"--database-url", "-dburl"}) String dbUrl,
                      @CommandLine.Option(names = "--student-name") String studentName,
                      @CommandLine.Option(names = "--lab-number") int labNumber) {
        try {
            var dbConnection = JDBC.getConnection(dbUrl);
            var astRepository = new ASTRepository(dbConnection);
            astRepository.initTable();
            astRepository.deleteData(studentName, labNumber);
            System.out.println("ast deleted successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @CommandLine.Command(name = "compareToDb")
    void compareToOther(@CommandLine.Option(names = {"--database-url", "-dburl"}) String dbUrl,
                        @CommandLine.Option(names = "--student-name") String studentName,
                        @CommandLine.Option(names = "--lab-number") int labNumber,
                        @CommandLine.Parameters(arity = "1..*", paramLabel = "<sourceCodePaths>") String[] sourceCodePaths) {

        try {
            var dbConnection = JDBC.getConnection(dbUrl);
            var astRepository = new ASTRepository(dbConnection);
            var currentAst = StaticJavaParser.parse(SourceCodeUtils.concatFiles(sourceCodePaths));
            Map<String, Node> dbAstsToCompare = astRepository.findAllAstForLabAndStudent(studentName, labNumber)
                    .entrySet()
                    .stream()
                    .map(entry -> new Pair<>(entry.getKey(), IOTreeUtils.readFromJSON(new String(entry.getValue(), StandardCharsets.UTF_8))))
                    .collect(Collectors.toMap(pair -> pair.a, pair -> pair.b));
            var plagResults = new HashMap<String, Long>();
            if (dbAstsToCompare.isEmpty()) {
                System.out.println("AST to compare not found");
                return;
            }
            dbAstsToCompare.forEach((key, value) ->
                    plagResults.put(key, PlagiarismChecker.countPlagiarismPercentage(currentAst, (CompilationUnit) value))
            );
            var maxPlagEntry = plagResults.entrySet().stream().max(Comparator.comparingLong(Map.Entry::getValue)).get();
            System.out.println("Plagiarism percentage is " + maxPlagEntry.getValue());
            System.out.println("With student: " + maxPlagEntry.getKey());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @CommandLine.Command(name = "getSourceCode")
    void getSourceCode(@CommandLine.Option(names = {"--database-url", "-dburl"}) String dbUrl,
                       @CommandLine.Option(names = "--student-name") String studentName,
                       @CommandLine.Option(names = "--lab-number") int labNumber) {
        try {
            var dbConnection = JDBC.getConnection(dbUrl);
            var astRepository = new ASTRepository(dbConnection);
            String sourceCode = astRepository.getSourceCodeByStudentLoginAndLabNumber(studentName, labNumber);
            System.out.println(sourceCode);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
