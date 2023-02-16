package com.kerellka.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.serialization.JavaParserJsonDeserializer;
import com.github.javaparser.serialization.JavaParserJsonSerializer;
import com.github.pgelinas.jackson.javax.json.spi.JacksonProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class IOTreeUtils {

    public static String writeProjectToJSONAsAST(String rootProjectDir) {
        return writeToJSON(StaticJavaParser.parse(SourceCodeUtils.concatFilesFromProject(rootProjectDir)));
    }

    public static String writeToJSON(String[] pathsToSourceCode) {
        return writeToJSON(StaticJavaParser.parse(SourceCodeUtils.concatFiles(pathsToSourceCode)));
    }

    public static String writeToJSON(CompilationUnit cu) {
        var serializer = new JavaParserJsonSerializer();
        var jacksonProvider = new JacksonProvider();
        var outputStream = new ByteArrayOutputStream();
        serializer.serialize(cu, jacksonProvider.createGenerator(outputStream));
        return outputStream.toString();
    }

    public static Node readFromJSON(String astJson) {
        var serializer = new JavaParserJsonDeserializer();
        var jacksonProvider = new JacksonProvider();
        var jsonReader = jacksonProvider.createReader(new ByteArrayInputStream(astJson.getBytes(StandardCharsets.UTF_8)));
        return serializer.deserializeObject(jsonReader);
    }


}
