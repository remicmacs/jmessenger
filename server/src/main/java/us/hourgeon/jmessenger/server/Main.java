package us.hourgeon.jmessenger.server;
import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        System.out.println("Hello, world");
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb" +
                "://localhost/", "root", "root")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                //execute query
                try (ResultSet rs = stmt.executeQuery("SELECT 'Hello World!'")) {
                    //position result to first
                    rs.first();
                    System.out.println(rs.getString(1)); //result is "Hello World!"
                }
            }
        }
    }
}
