<script setup>
import { onMounted, ref } from "vue";
import DanhSachDanhMuc from "../components/trang-chu/DanhSachDanhMuc.vue";
import DanhSachSanPham from "../components/trang-chu/DanhSachSanPham.vue";
import GiaTriThuongHieu from "../components/trang-chu/GiaTriThuongHieu.vue";
import KhoiAnhHung from "../components/trang-chu/KhoiAnhHung.vue";
import { giaTriNoiBat, thongKeTrangChu } from "../constants/trangChu";
import { laySanPhamNoiBat, layHangNoiBat } from "../services/san-pham";

const sanPhamNoiBat = ref([]);
const hangNoiBat = ref([]);

onMounted(async () => {
  // Tải song song, từng phần lỗi không ảnh hưởng phần còn lại của trang chủ.
  const [sanPham, hang] = await Promise.allSettled([laySanPhamNoiBat(8), layHangNoiBat(4)]);
  sanPhamNoiBat.value = sanPham.status === "fulfilled" ? sanPham.value : [];
  hangNoiBat.value = hang.status === "fulfilled" ? hang.value : [];
});
</script>

<template>
  <main id="trang-chu">
    <KhoiAnhHung :thong-ke="thongKeTrangChu" />
    <DanhSachDanhMuc :danh-muc="hangNoiBat" />
    <DanhSachSanPham :san-pham="sanPhamNoiBat" />
    <GiaTriThuongHieu :danh-sach="giaTriNoiBat" />
  </main>
</template>
