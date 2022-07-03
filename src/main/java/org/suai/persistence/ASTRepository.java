package org.suai.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.UnexpectedException;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class ASTRepository {

    private Connection connection;

    public ASTRepository(Connection connection) {
        this.connection = connection;
    }

    public void initTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("create table if not exists ast(id int primary key, student_login varchar(100), lab_number int, ast clob, source_code clob)");
        statement.executeUpdate("create unique index if not exists ast_unique on ast (student_login, lab_number);");
    }

    public void insertData(String studentLogin, int labNumber, byte[] astRepresentation, byte[] sourceCode) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("insert into ast (student_login, lab_number, ast, source_code) values (?, ?, ?, ?)");
        statement.setString(1, studentLogin);
        statement.setInt(2, labNumber);
        statement.setBytes(3, astRepresentation);
        statement.setBytes(4, sourceCode);
        statement.executeUpdate();
    }

    public void updateData(String studentLogin, int labNumber, byte[] astRepresentation, byte[] sourceCode) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("update ast set ast = ?, source_code = ? where student_login = ? and lab_number = ?");
        statement.setString(1, studentLogin);
        statement.setInt(2, labNumber);
        statement.setBytes(3, astRepresentation);
        statement.setBytes(4, sourceCode);
        statement.executeUpdate();
    }

    public void deleteData(String studentLogin, int labNumber) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("delete from ast where student_login = ? and lab_number = ?");
        statement.setString(1, studentLogin);
        statement.setInt(2, labNumber);
        statement.executeUpdate();
    }

    public Map<String, byte[]> findAllAstForLabAndStudent(String studentLogin, int labNumber) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select ast, student_login from ast where lab_number = ? and student_login <> ?");
        statement.setInt(1, labNumber);
        statement.setString(2, studentLogin);
        var rs = statement.executeQuery();
        Map<String, byte[]> asts = new java.util.HashMap<>();
        while (rs.next()) {
            var studentName = rs.getString("student_login");
            var ast = rs.getBytes("ast");
            asts.put(studentName, ast);
        }
        return asts;
    }

    public String getSourceCodeByStudentLoginAndLabNumber(String studentLogin, int labNumber) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select source_code from ast where lab_number = ? and student_login = ?");
        statement.setInt(1, labNumber);
        statement.setString(2, studentLogin);
        var rs = statement.executeQuery();
        rs.next();
        return rs.getString("source_code");
    }

}
