import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Cleanup {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=giay;encrypt=true;trustServerCertificate=true";
        String user = "sa";
        String pass = "TungAnh@123456";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // Xóa khách hàng linh tinh
            PreparedStatement ps1 = conn.prepareStatement("DELETE FROM KhachHang WHERE TenDangNhap NOT IN ('tunganh784', 'nguyenvana') AND TenDangNhap LIKE '%tunganh%'");
            int deletedKH = ps1.executeUpdate();
            System.out.println("Deleted KhachHang: " + deletedKH);
            
            // Xóa nhân viên linh tinh
            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM NhanVien WHERE TenDangNhap NOT IN ('tunganh784') AND (TenDangNhap LIKE '%tunganh%' OR TenDangNhap LIKE '%dkf%')");
            int deletedNV = ps2.executeUpdate();
            System.out.println("Deleted NhanVien: " + deletedNV);
            
            // Maybe there are names like "dkfgjdjfkghndfjk"
            PreparedStatement ps3 = conn.prepareStatement("DELETE FROM KhachHang WHERE HoTen LIKE '%dkfg%' OR HoTen LIKE '%qwq%' OR TenDangNhap LIKE '%pekor%'");
            int deletedKH2 = ps3.executeUpdate();
            System.out.println("Deleted KhachHang (junk): " + deletedKH2);
            
            PreparedStatement ps4 = conn.prepareStatement("DELETE FROM NhanVien WHERE HoTen LIKE '%dkfg%' OR HoTen LIKE '%qwq%' OR TenDangNhap LIKE '%pekor%'");
            int deletedNV2 = ps4.executeUpdate();
            System.out.println("Deleted NhanVien (junk): " + deletedNV2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
