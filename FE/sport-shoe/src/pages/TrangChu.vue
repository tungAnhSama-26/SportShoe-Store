<script setup lang="ts">
import DanhSachDanhMuc from "../components/trang-chu/DanhSachDanhMuc.vue";
import DanhSachSanPham from "../components/trang-chu/DanhSachSanPham.vue";
import GiaTriThuongHieu from "../components/trang-chu/GiaTriThuongHieu.vue";
import KhoiAnhHung from "../components/trang-chu/KhoiAnhHung.vue";
import { danhMucGiay, giaTriNoiBat, thongKeTrangChu } from "../constants/trangChu";
import { laySanPhamNoiBat } from "../services/san-pham";

const sanPhamNoiBat = laySanPhamNoiBat();
</script>

<template>
  <main id="trang-chu">
    <KhoiAnhHung :thong-ke="thongKeTrangChu" />
    <DanhSachDanhMuc :danh-muc="danhMucGiay" />
    <DanhSachSanPham :san-pham="sanPhamNoiBat" />
    <GiaTriThuongHieu :danh-sach="giaTriNoiBat" />
  </main>
</template>
