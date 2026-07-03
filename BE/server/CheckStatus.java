import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckStatus {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=giay;encrypt=true;trustServerCertificate=true";
        try (Connection conn = DriverManager.getConnection(url, "sa", "Huy@123456");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 10 id, ten FROM giay")) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("ten"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
