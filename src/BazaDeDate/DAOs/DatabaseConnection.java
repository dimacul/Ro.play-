package BazaDeDate.DAOs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/music_app";
    private static final String USER = "root";
    private static final String PASSWORD = "pufuini57";

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("CONECTAT LA BD");
            } catch (SQLException e) {
                System.out.println("Eroare la conectarea la BD: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }
}

