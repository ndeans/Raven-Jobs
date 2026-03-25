package us.deans.raven;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SchemaUpdate {
    public static void main(String[] args) {
        String url = "jdbc:mariadb://vortex:3306/raven_1";
        String user = "bambam";
        String pass = "bambam";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
                Statement stmt = conn.createStatement()) {

            System.out.println("Adding 'pruned' column...");
            stmt.execute("ALTER TABLE uploads ADD COLUMN IF NOT EXISTS pruned TINYINT(1) NOT NULL DEFAULT 0");

            System.out.println("Adding 'pruned_at' column...");
            stmt.execute("ALTER TABLE uploads ADD COLUMN IF NOT EXISTS pruned_at DATETIME NULL");

            System.out.println("Schema update completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
