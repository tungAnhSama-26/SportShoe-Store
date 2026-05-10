import { createRouter, createWebHistory } from "vue-router";
import TrangMacDinh from "../layouts/TrangMacDinh.vue";
import TrangChu from "../pages/TrangChu.vue";
import SanPham from "../pages/SanPham.vue";
import SanPhamNoiBat from "../pages/SanPhamNoiBat.vue";
import GioiThieu from "../pages/GioiThieu.vue";
import AdminLayout from "../layouts/admin/AdminLayout.vue";
import ThongKe from "../pages/admin/thong-ke/ThongKe.vue";
import PhieuGiamGia from "../pages/admin/khuyen-mai/PhieuGiamGia.vue";
import HoaDon from "../pages/admin/hoa-don/HoaDon.vue";
import ChiTietHoaDon from "../pages/admin/hoa-don/ChiTietHoaDon.vue";
import BanHangTaiQuay from "../pages/admin/ban-hang/BanHangTaiQuay.vue";
import DanhSachSanPham from "../pages/admin/san-pham/DanhSachSanPham.vue";
import DanhSachChiTietSanPham from "../pages/admin/san-pham/DanhSachChiTietSanPham.vue";
import ChiTietSanPhamForm from "../pages/admin/san-pham/ChiTietSanPhamForm.vue";
import DotGiamGia from "../pages/admin/khuyen-mai/DotGiamGia.vue";
import QuanLyNhanVien from "../pages/admin/nhan-vien/QuanLyNhanVien.vue";
import ChiTietNhanVien from "../pages/admin/nhan-vien/ChiTietNhanVien.vue";
import QuanLyLichLam from "../pages/admin/nhan-vien/QuanLyLichLam.vue";
import QuanLyKhachHang from "../pages/admin/khach-hang/QuanLyKhachHang.vue";
import ChiTietKhachHang from "../pages/admin/khach-hang/ChiTietKhachHang.vue";
import ChiTietPhieuGiamGia from "../pages/admin/khuyen-mai/ChiTietPhieuGiamGia.vue";
import ChiTietPhieuGiamGiaKhachHang from "../pages/admin/khuyen-mai/ChiTietPhieuGiamGiaKhachHang.vue";
import ChiTietDotGiamGia from "../pages/admin/khuyen-mai/ChiTietDotGiamGia.vue";

import LoaiGiay from "../pages/admin/danh-muc/LoaiGiay.vue";
import CoGiay from "../pages/admin/danh-muc/CoGiay.vue";
import DeGiay from "../pages/admin/danh-muc/DeGiay.vue";
import ChatLieuGiay from "../pages/admin/danh-muc/ChatLieuGiay.vue";
import ThuongHieu from "../pages/admin/danh-muc/ThuongHieu.vue";
import CongNgheDem from "../pages/admin/danh-muc/CongNgheDem.vue";
import MauSac from "../pages/admin/danh-muc/MauSac.vue";
import KichCo from "../pages/admin/danh-muc/KichCo.vue";
import TrongLuong from "../pages/admin/danh-muc/TrongLuong.vue";
import Login from "../pages/login/Login.vue";
import AdminLogin from "../pages/login/AdminLogin.vue";
import Register from "../pages/register/Register.vue";
import ForgotPassword from "../pages/login/ForgotPassword.vue";
import { getCurrentAdminUser, hasRequiredAdminCccd, isAdminAuthenticated, isAdminRole } from "../services/auth";

const STAFF_ALLOWED_ADMIN_PATHS = [
  "/admin/ban-hang",
  "/admin/hoa-don",
  "/admin/khach-hang",
  "/admin/lich-lam-viec",
  "/admin/chat"
];

function isStaffAllowedPath(path) {
  return STAFF_ALLOWED_ADMIN_PATHS.some((allowedPath) => path.startsWith(allowedPath));
}

function ownEmployeeProfilePath() {
  const id = getCurrentAdminUser()?.id;
  return id ? `/admin/nhan-vien/${id}` : "/admin/login";
}

function isOwnEmployeeProfile(path) {
  return path === ownEmployeeProfilePath();
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
      path: "/",
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
          path: "noi-bat",
          name: "noi-bat",
          component: SanPhamNoiBat
        },
        {
          path: "gioi-thieu",
          name: "gioi-thieu",
          component: GioiThieu
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
          path: "khach-hang/:id",
          name: "admin-khach-hang-chi-tiet",
          component: ChiTietKhachHang
        }
      ]
    }
  ]
});

router.beforeEach((to) => {
  if (to.name === "admin-login") {
    return true;
  }

  if (!to.path.startsWith("/admin")) {
    return true;
  }

  if (!isAdminAuthenticated()) {
    return { path: "/admin/login", query: { redirect: to.fullPath } };
  }

  if (!hasRequiredAdminCccd() && !isOwnEmployeeProfile(to.path)) {
    return {
      path: ownEmployeeProfilePath(),
      query: { requireCccd: "1", redirect: to.fullPath }
    };
  }

  if (!hasRequiredAdminCccd() && isOwnEmployeeProfile(to.path)) {
    return true;
  }

  if (isAdminRole()) {
    return true;
  }

  if (to.path === "/admin" || to.path === "/admin/") {
    return "/admin/ban-hang";
  }

  if (!isStaffAllowedPath(to.path)) {
    return "/admin/ban-hang";
  }

  return true;
});

export default router;
