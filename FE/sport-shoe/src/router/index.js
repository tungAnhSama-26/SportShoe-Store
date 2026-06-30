import { createRouter, createWebHistory } from "vue-router";
import { getCurrentAdminUser, isAdminAuthenticated, isAdminRole } from "../services/auth";

const TrangMacDinh = () => import("../layouts/TrangMacDinh.vue");
const TrangChu = () => import("../pages/TrangChu.vue");
const SanPham = () => import("../pages/SanPham.vue");
const ChiTietSanPham = () => import("../pages/ChiTietSanPham.vue");
const GioHang = () => import("../pages/GioHang.vue");
const ThanhToan = () => import("../pages/ThanhToan.vue");
const DonHangCuaToi = () => import("../pages/DonHangCuaToi.vue");
const ChiTietDonHang = () => import("../pages/ChiTietDonHang.vue");
const DanhGiaDonHang = () => import("../pages/DanhGiaDonHang.vue");
const SanPhamNoiBat = () => import("../pages/SanPhamNoiBat.vue");
const GioiThieu = () => import("../pages/GioiThieu.vue");
const TraCuuDonHang = () => import("../pages/TraCuuDonHang.vue");
const DanhGiaCongKhai = () => import("../pages/DanhGiaCongKhai.vue");
const ClientProfile = () => import("../pages/Profile.vue");
const Login = () => import("../pages/login/Login.vue");
const AdminLogin = () => import("../pages/login/AdminLogin.vue");
const Register = () => import("../pages/register/Register.vue");
const ForgotPassword = () => import("../pages/login/ForgotPassword.vue");
const ErrorPage = () => import("../pages/error/ErrorPage.vue");
const AdminLayout = () => import("../layouts/admin/AdminLayout.vue");
const ThongKe = () => import("../pages/admin/thong-ke/ThongKe.vue");
const PhieuGiamGia = () => import("../pages/admin/khuyen-mai/PhieuGiamGia.vue");
const ChiTietPhieuGiamGia = () => import("../pages/admin/khuyen-mai/ChiTietPhieuGiamGia.vue");
const ChiTietPhieuGiamGiaKhachHang = () => import("../pages/admin/khuyen-mai/ChiTietPhieuGiamGiaKhachHang.vue");
const DotGiamGia = () => import("../pages/admin/khuyen-mai/DotGiamGia.vue");
const ChiTietDotGiamGia = () => import("../pages/admin/khuyen-mai/ChiTietDotGiamGia.vue");
const HoaDon = () => import("../pages/admin/hoa-don/HoaDon.vue");
const ChiTietHoaDon = () => import("../pages/admin/hoa-don/ChiTietHoaDon.vue");
const TraHang = () => import("../pages/admin/tra-hang/TraHang.vue");
const QuanLyDanhGia = () => import("../pages/admin/danh-gia/QuanLyDanhGia.vue");
const ChiTietTraHang = () => import("../pages/admin/tra-hang/ChiTietTraHang.vue");
const BanHangTaiQuay = () => import("../pages/admin/ban-hang/BanHangTaiQuay.vue");
const PosLayout = () => import("../layouts/admin/PosLayout.vue");
const PosIpadApp = () => import("../pages/admin/ban-hang/PosIpadApp.vue");
const DanhSachSanPham = () => import("../pages/admin/san-pham/DanhSachSanPham.vue");
const DanhSachChiTietSanPham = () => import("../pages/admin/san-pham/DanhSachChiTietSanPham.vue");
const ChiTietSanPhamForm = () => import("../pages/admin/san-pham/ChiTietSanPhamForm.vue");
const LoaiGiay = () => import("../pages/admin/danh-muc/LoaiGiay.vue");
const CoGiay = () => import("../pages/admin/danh-muc/CoGiay.vue");
const DeGiay = () => import("../pages/admin/danh-muc/DeGiay.vue");
const ChatLieuGiay = () => import("../pages/admin/danh-muc/ChatLieuGiay.vue");
const ThuongHieu = () => import("../pages/admin/danh-muc/ThuongHieu.vue");
const CongNgheDem = () => import("../pages/admin/danh-muc/CongNgheDem.vue");
const MauSac = () => import("../pages/admin/danh-muc/MauSac.vue");
const KichCo = () => import("../pages/admin/danh-muc/KichCo.vue");
const TrongLuong = () => import("../pages/admin/danh-muc/TrongLuong.vue");
const QuanLyNhanVien = () => import("../pages/admin/nhan-vien/QuanLyNhanVien.vue");
const ChiTietNhanVien = () => import("../pages/admin/nhan-vien/ChiTietNhanVien.vue");
const QuanLyLichLam = () => import("../pages/admin/nhan-vien/QuanLyLichLam.vue");
const QuanLyChamCong = () => import("../pages/admin/lich-lam/QuanLyChamCong.vue");
const QuanLyCaLam = () => import("../pages/admin/lich-lam/QuanLyCaLam.vue");
const LichSuHoatDong = () => import("../pages/admin/lich-lam/LichSuHoatDong.vue");
const BanGiaoCa = () => import("../pages/admin/quan-ly-giao-ca/BanGiaoCa.vue");
const Profile = () => import("../pages/admin/profile/Profile.vue");
const QuanLyKhachHang = () => import("../pages/admin/khach-hang/QuanLyKhachHang.vue");
const ChiTietKhachHang = () => import("../pages/admin/khach-hang/ChiTietKhachHang.vue");
const DonHangKhachHang = () => import("../pages/admin/khach-hang/DonHangKhachHang.vue");
const ChatManagement = () => import("../pages/admin/chat/ChatManagement.vue");

const STAFF_ALLOWED_ADMIN_PATHS = [
  "/admin/ban-hang",
  "/pos",
  "/admin/hoa-don",
  "/admin/tra-hang",
  "/admin/khach-hang",
  "/admin/lich-lam-viec",
  "/admin/cham-cong",
  "/admin/ban-giao-ca",
  "/admin/mo-ca",
  "/admin/chat",
  "/admin/profile"
];

function isStaffAllowedPath(path) {
  return STAFF_ALLOWED_ADMIN_PATHS.some((allowedPath) => path.startsWith(allowedPath));
}

function ownEmployeeProfilePath() {
  return isAdminRole() ? "/admin/profile" : "/nhanvien/profile";
}

function isOwnEmployeeProfile(path) {
  return path === "/admin/profile"
    || path.startsWith("/admin/profile")
    || path === "/nhanvien/profile"
    || path.startsWith("/nhanvien/profile");
}

function isProtectedAdminArea(path) {
  return path.startsWith("/admin") || path.startsWith("/nhanvien") || path.startsWith("/pos");
}

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      return {
        el: to.hash,
        behavior: "smooth"
      };
    }
    return { top: 0, behavior: "smooth" };
  },
  routes: [
    {
      path: "/login",
      name: "login",
      component: Login
    },
    {
      path: "/admin/login",
      name: "admin-login",
      component: AdminLogin
    },
    {
      path: "/register",
      name: "register",
      component: Register
    },
    {
      path: "/forgot-password",
      name: "forgot-password",
      component: ForgotPassword
    },
    {
      path: "/error/:status(\\d+)",
      name: "error-page",
      component: ErrorPage
    },
    {
      path: "/",
      redirect: "/khachhang"
    },
    {
      path: "/khachhang",
      component: TrangMacDinh,
      children: [
        {
          path: "",
          name: "trang-chu",
          component: TrangChu
        },
        {
          path: "danh-muc",
          name: "danh-muc",
          component: SanPham
        },
        {
          path: "san-pham",
          name: "san-pham",
          component: SanPham
        },
        {
          path: "san-pham/:id(\\d+)",
          name: "san-pham-chi-tiet",
          component: ChiTietSanPham
        },
        {
          path: "gio-hang",
          name: "gio-hang",
          component: GioHang
        },
        {
          path: "thanh-toan",
          name: "thanh-toan",
          component: ThanhToan
        },
        {
          path: "don-hang",
          name: "don-hang",
          component: DonHangCuaToi
        },
        {
          path: "don-hang/:id(\\d+)",
          name: "don-hang-chi-tiet",
          component: ChiTietDonHang
        },
        {
          path: "don-hang/:id(\\d+)/danh-gia",
          name: "don-hang-danh-gia",
          component: DanhGiaDonHang
        },
        {
          path: "noi-bat",
          name: "noi-bat",
          component: SanPhamNoiBat
        },
        {
          path: "gioi-thieu",
          name: "gioi-thieu",
          component: GioiThieu
        },
        {
          path: "tra-cuu-don",
          name: "tra-cuu-don",
          component: TraCuuDonHang
        },
        {
          path: "danh-gia",
          name: "danh-gia-cong-khai",
          component: DanhGiaCongKhai
        },
        {
          path: "profile",
          name: "client-profile",
          component: ClientProfile
        }
      ]
    },
    {
      path: "/nhanvien",
      component: AdminLayout,
      children: [
        {
          path: "",
          redirect: "/nhanvien/profile"
        },
        {
          path: "profile",
          name: "nhanvien-profile",
          component: Profile
        }
      ]
    },
    {
      path: "/pos",
      component: PosLayout,
      children: [
        {
          path: "",
          name: "admin-pos-ipad",
          component: PosIpadApp
        }
      ]
    },
    {
      path: "/admin",
      component: AdminLayout,
      children: [
        {
          path: "",
          redirect: "/admin/thong-ke"
        },
        {
          path: "thong-ke",
          name: "admin-thong-ke",
          component: ThongKe
        },
        {
          path: "phieu-giam-gia",
          name: "admin-phieu-giam-gia",
          component: PhieuGiamGia
        },
        {
          path: "phieu-giam-gia-khach-hang",
          name: "admin-phieu-giam-gia-khach-hang",
          component: PhieuGiamGia
        },
        {
          path: "phieu-giam-gia/them",
          name: "admin-phieu-giam-gia-them",
          component: ChiTietPhieuGiamGia
        },
        {
          path: "phieu-giam-gia/:id",
          name: "admin-phieu-giam-gia-chi-tiet",
          component: ChiTietPhieuGiamGia
        },
        {
          path: "phieu-giam-gia-khach-hang/them",
          name: "admin-phieu-giam-gia-khach-hang-them",
          component: ChiTietPhieuGiamGiaKhachHang
        },
        {
          path: "phieu-giam-gia-khach-hang/:id",
          name: "admin-phieu-giam-gia-khach-hang-chi-tiet",
          component: ChiTietPhieuGiamGiaKhachHang
        },
        {
          path: "hoa-don",
          name: "admin-hoa-don",
          component: HoaDon
        },
        {
          path: "hoa-don/:id",
          name: "admin-hoa-don-chi-tiet",
          component: ChiTietHoaDon
        },
        {
          path: "tra-hang",
          name: "admin-tra-hang",
          component: TraHang
        },
        {
          path: "tra-hang/:id",
          name: "admin-tra-hang-chi-tiet",
          component: ChiTietTraHang
        },
        {
          path: "danh-gia",
          name: "admin-danh-gia",
          component: QuanLyDanhGia
        },
        {
          path: "ban-hang",
          name: "admin-ban-hang",
          component: BanHangTaiQuay
        },
        {
          path: "san-pham",
          name: "admin-san-pham",
          component: DanhSachSanPham
        },
        {
          path: "san-pham/them",
          name: "admin-san-pham-them",
          component: ChiTietSanPhamForm
        },
        {
          path: "chi-tiet-san-pham/new",
          name: "admin-chi-tiet-san-pham-new",
          component: ChiTietSanPhamForm
        },
        {
          path: "bien-the-san-pham",
          name: "admin-bien-the-san-pham",
          component: DanhSachChiTietSanPham
        },
        {
          path: "bien-the-san-pham/them",
          name: "admin-bien-the-san-pham-them",
          component: ChiTietSanPhamForm
        },
        {
          path: "chi-tiet-san-pham/:giayId(\\d+)",
          name: "admin-chi-tiet-san-pham",
          component: ChiTietSanPhamForm
        },
        { path: "loai-giay", name: "admin-loai-giay", component: LoaiGiay },
        { path: "co-giay", name: "admin-co-giay", component: CoGiay },
        { path: "de-giay", name: "admin-de-giay", component: DeGiay },
        { path: "chat-lieu-giay", name: "admin-chat-lieu-giay", component: ChatLieuGiay },
        { path: "thuong-hieu", name: "admin-thuong-hieu", component: ThuongHieu },
        { path: "cong-nghe-dem", name: "admin-cong-nghe-dem", component: CongNgheDem },
        { path: "mau-sac", name: "admin-mau-sac", component: MauSac },
        { path: "kich-co", name: "admin-kich-co", component: KichCo },
        { path: "trong-luong", name: "admin-trong-luong", component: TrongLuong },
        {
          path: "dot-giam-gia",
          name: "admin-dot-giam-gia",
          component: DotGiamGia
        },
        {
          path: "dot-giam-gia-san-pham",
          name: "admin-dot-giam-gia-san-pham",
          redirect: "/admin/dot-giam-gia"
        },
        {
          path: "dot-giam-gia/them",
          name: "admin-dot-giam-gia-them",
          component: ChiTietDotGiamGia
        },
        {
          path: "dot-giam-gia/:id",
          name: "admin-dot-giam-gia-chi-tiet",
          component: ChiTietDotGiamGia
        },
        {
          path: "dot-giam-gia-san-pham/them",
          name: "admin-dot-giam-gia-san-pham-them",
          redirect: "/admin/dot-giam-gia"
        },
        {
          path: "dot-giam-gia-san-pham/:id",
          name: "admin-dot-giam-gia-san-pham-chi-tiet",
          redirect: "/admin/dot-giam-gia"
        },
        {
          path: "nhan-vien",
          name: "admin-nhan-vien",
          component: QuanLyNhanVien
        },
        {
          path: "nhan-vien/them",
          name: "admin-nhan-vien-them",
          component: ChiTietNhanVien
        },
        {
          path: "nhan-vien/lich-lam",
          name: "admin-nhan-vien-lich-lam",
          component: QuanLyLichLam
        },
        {
          path: "nhan-vien/:id",
          name: "admin-nhan-vien-chi-tiet",
          component: ChiTietNhanVien
        },
        {
          path: "nhan-vien/:id/lich-lam",
          name: "admin-nhan-vien-lich-lam-chi-tiet",
          component: QuanLyLichLam
        },
        {
          path: "profile",
          name: "admin-profile",
          component: Profile
        },
        {
          path: "khach-hang",
          name: "admin-khach-hang",
          component: QuanLyKhachHang
        },
        {
          path: "khach-hang/them",
          name: "admin-khach-hang-them",
          component: ChiTietKhachHang
        },
        {
          path: "khach-hang/:id/don-hang",
          name: "admin-khach-hang-don-hang",
          component: DonHangKhachHang
        },
        {
          path: "khach-hang/:id",
          name: "admin-khach-hang-chi-tiet",
          component: ChiTietKhachHang
        },
        {
          path: "lich-lam-viec",
          name: "admin-lich-lam-viec",
          component: QuanLyLichLam
        },
        {
          path: "lich-ca-lam",
          name: "admin-lich-ca-lam",
          component: QuanLyCaLam
        },
        {
          path: "cham-cong",
          name: "admin-cham-cong",
          component: QuanLyChamCong
        },
        {
          path: "ban-giao-ca",
          name: "admin-ban-giao-ca",
          component: BanGiaoCa
        },
        {
          path: "mo-ca",
          name: "admin-mo-ca",
          component: BanGiaoCa
        },
        {
          path: "lich-su-hoat-dong",
          name: "admin-lich-su-hoat-dong",
          component: LichSuHoatDong
},{
          path: "chat",
          name: "admin-chat",
          component: ChatManagement
        }
      ]
    },
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      redirect: (to) => ({
        path: "/error/404",
        query: {
          message: "Đường dẫn không tồn tại.",
          redirect: to.fullPath
        }
      })
    }
  ]
});

router.beforeEach((to) => {
  if (to.name === "admin-login") {
    return true;
  }

  if (!isProtectedAdminArea(to.path)) {
    return true;
  }

  if (!isAdminAuthenticated()) {
    return {
      path: "/error/401",
      query: {
        redirect: to.fullPath,
        message: "Không tìm thấy token đăng nhập. Vui lòng đăng nhập lại."
      }
    };
  }


  if (to.path.startsWith("/nhanvien")) {
    return isOwnEmployeeProfile(to.path) ? true : ownEmployeeProfilePath();
  }

  if (isAdminRole()) {
    return true;
  }

  if (to.path === "/admin" || to.path === "/admin/") {
    return "/admin/ban-hang";
  }

  if (!isStaffAllowedPath(to.path)) {
    return {
      path: "/error/403",
      query: {
        redirect: to.fullPath,
        message: "Tài khoản nhân viên không có quyền truy cập chức năng dành cho admin."
      }
    };
  }

  return true;
});

export default router;
