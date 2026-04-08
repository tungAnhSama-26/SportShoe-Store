import { createRouter, createWebHistory } from 'vue-router';
import TrangMacDinh from '../layouts/TrangMacDinh.vue';
import TrangChu from '../pages/TrangChu.vue';
import SanPham from '../pages/SanPham.vue';
import SanPhamNoiBat from '../pages/SanPhamNoiBat.vue';
import GioiThieu from '../pages/GioiThieu.vue';
import AdminLayout from '../layouts/admin/AdminLayout.vue';
import ThongKe from '../pages/admin/ThongKe.vue';
import PhieuGiamGia from '../pages/admin/PhieuGiamGia.vue';

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
        }
      ]
    },
  ],
});

export default router;
