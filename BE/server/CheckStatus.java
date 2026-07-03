import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckStatus {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=giay;encrypt=true;trustServerCertificate=true";
        try (Connection conn = DriverManager.getConnection(url, "sa", "TungAnh@123456");
             Statement stmt = conn.createStatement()) {
            
            // Check if column email exists
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "hoa_don", "email")) {
                if (rs.next()) {
                    System.out.println("Column 'email' already exists in 'hoa_don' table.");
                } else {
                    System.out.println("Column 'email' does not exist. Adding it...");
                    stmt.executeUpdate("ALTER TABLE hoa_don ADD email NVARCHAR(100) NULL");
                    System.out.println("Column 'email' added successfully!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
