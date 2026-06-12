<script setup>
import { onMounted, ref } from "vue";
import DanhSachDanhMuc from "../components/trang-chu/DanhSachDanhMuc.vue";
import DanhSachSanPham from "../components/trang-chu/DanhSachSanPham.vue";
import GiaTriThuongHieu from "../components/trang-chu/GiaTriThuongHieu.vue";
import KhoiAnhHung from "../components/trang-chu/KhoiAnhHung.vue";
import { giaTriNoiBat, thongKeTrangChu } from "../constants/trangChu";
import { laySanPhamNoiBat, layHangNoiBat, layThongKeTrangChu } from "../services/san-pham";

const sanPhamNoiBat = ref([]);
const hangNoiBat = ref([]);
// Số liệu banner: mặc định dùng số tĩnh, thay bằng số thật từ DB khi tải xong.
const thongKe = ref(thongKeTrangChu);

onMounted(async () => {
  // Tải song song, từng phần lỗi không ảnh hưởng phần còn lại của trang chủ.
  const [sanPham, hang, tk] = await Promise.allSettled([
    laySanPhamNoiBat(8),
    layHangNoiBat(4),
    layThongKeTrangChu(),
  ]);
  sanPhamNoiBat.value = sanPham.status === "fulfilled" ? sanPham.value : [];
  hangNoiBat.value = hang.status === "fulfilled" ? hang.value : [];
  if (tk.status === "fulfilled" && tk.value) {
    thongKe.value = [
      { so: `${tk.value.soKhachHang}+`, nhan: "Khách hàng tin dùng" },
      { so: `${tk.value.soSanPham}`, nhan: "Mẫu giày đang bán" },
      {
        so: tk.value.soDanhGia ? `${tk.value.diemTrungBinh}/5` : "5/5",
        nhan: tk.value.soDanhGia ? `Từ ${tk.value.soDanhGia} đánh giá` : "Đánh giá trung bình",
      },
    ];
  }
});
</script>

<template>
  <main id="trang-chu">
    <KhoiAnhHung :thong-ke="thongKe" />
    <DanhSachDanhMuc :danh-muc="hangNoiBat" />
    <DanhSachSanPham :san-pham="sanPhamNoiBat" />
    <GiaTriThuongHieu :danh-sach="giaTriNoiBat" />
  </main>
</template>
