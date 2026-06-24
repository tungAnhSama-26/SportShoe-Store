import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckStatus {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=giay;encrypt=true;trustServerCertificate=true";
        try (Connection conn = DriverManager.getConnection(url, "sa", "TungAnh@123456");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 10 id, ma, trang_thai FROM hoa_don ORDER BY id DESC")) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Code: " + rs.getString("ma") + ", Status: " + rs.getInt("trang_thai"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
