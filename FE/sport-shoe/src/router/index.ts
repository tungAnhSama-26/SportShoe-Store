import { createRouter, createWebHistory } from 'vue-router';
import TrangMacDinh from '../layouts/TrangMacDinh.vue';
import TrangChu from '../pages/TrangChu.vue';
import SanPham from '../pages/SanPham.vue';
import SanPhamNoiBat from '../pages/SanPhamNoiBat.vue';
import GioiThieu from '../pages/GioiThieu.vue';
import AdminLayout from '../layouts/admin/AdminLayout.vue';
import ThongKe from '../pages/admin/thong-ke/ThongKe.vue';
import PhieuGiamGia from '../pages/admin/khuyen-mai/PhieuGiamGia.vue';
import HoaDon from '../pages/admin/hoa-don/HoaDon.vue';
import BanHangTaiQuay from '../pages/admin/ban-hang/BanHangTaiQuay.vue';
import QuanLySanPham from '../pages/admin/san-pham/QuanLySanPham.vue';
import DotGiamGia from '../pages/admin/khuyen-mai/DotGiamGia.vue';
import QuanLyNhanVien from '../pages/admin/tai-khoan/QuanLyNhanVien.vue';
import QuanLyKhachHang from '../pages/admin/tai-khoan/QuanLyKhachHang.vue';
import LoaiGiay from '../pages/admin/danh-muc/LoaiGiay.vue';
import CoGiay from '../pages/admin/danh-muc/CoGiay.vue';
import DeGiay from '../pages/admin/danh-muc/DeGiay.vue';
import ThuongHieu from '../pages/admin/danh-muc/ThuongHieu.vue';
import CongNgheDem from '../pages/admin/danh-muc/CongNgheDem.vue';
import MauSac from '../pages/admin/danh-muc/MauSac.vue';
import KichCo from '../pages/admin/danh-muc/KichCo.vue';
import TrongLuong from '../pages/admin/danh-muc/TrongLuong.vue';

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth',
      }
    }
    return { top: 0, behavior: 'smooth' }
  },
  routes: [
    {
      path: '/',
      component: TrangMacDinh,
      children: [
        {
          path: '',
          name: 'trang-chu',
          component: TrangChu,
        },
        {
          path: 'danh-muc',
          name: 'danh-muc',
          component: SanPham,
        },
        {
          path: 'noi-bat',
          name: 'noi-bat',
          component: SanPhamNoiBat,
        },
        {
          path: 'gioi-thieu',
          name: 'gioi-thieu',
          component: GioiThieu,
        },
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: '',
          redirect: '/admin/thong-ke'
        },
        {
          path: 'thong-ke',
          name: 'admin-thong-ke',
          component: ThongKe
        },
        {
          path: 'phieu-giam-gia',
          name: 'admin-phieu-giam-gia',
          component: PhieuGiamGia
        },
        {
          path: 'hoa-don',
          name: 'admin-hoa-don',
          component: HoaDon
        },
        {
          path: 'ban-hang',
          name: 'admin-ban-hang',
          component: BanHangTaiQuay
        },
        {
          path: 'san-pham',
          name: 'admin-san-pham',
          component: QuanLySanPham
        },
        { path: 'loai-giay', name: 'admin-loai-giay', component: LoaiGiay },
        { path: 'co-giay', name: 'admin-co-giay', component: CoGiay },
        { path: 'de-giay', name: 'admin-de-giay', component: DeGiay },
        { path: 'thuong-hieu', name: 'admin-thuong-hieu', component: ThuongHieu },
        { path: 'cong-nghe-dem', name: 'admin-cong-nghe-dem', component: CongNgheDem },
        { path: 'mau-sac', name: 'admin-mau-sac', component: MauSac },
        { path: 'kich-co', name: 'admin-kich-co', component: KichCo },
        { path: 'trong-luong', name: 'admin-trong-luong', component: TrongLuong },
        {
          path: 'dot-giam-gia',
          name: 'admin-dot-giam-gia',
          component: DotGiamGia
        },
        {
          path: 'nhan-vien',
          name: 'admin-nhan-vien',
          component: QuanLyNhanVien
        },
        {
          path: 'khach-hang',
          name: 'admin-khach-hang',
          component: QuanLyKhachHang
        }
      ]
    },
  ],
});

export default router;
