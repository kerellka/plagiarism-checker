package com.kerellka.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC {

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        return getConnection(null);
    }

    public static Connection getConnection(String databaseURL) throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(databaseURL);
        }
        return connection;
    }

}
