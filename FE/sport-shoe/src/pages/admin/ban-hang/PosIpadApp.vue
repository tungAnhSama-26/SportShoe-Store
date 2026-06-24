<script setup>
import { useRouter } from "vue-router";
import { ChevronLeft } from "lucide-vue-next";
import { ref, onMounted, onUnmounted } from "vue";
import PhanHoaDonCho from "../../../components/admin/ban-hang/PhanHoaDonCho.vue";
import PhanSanPham from "../../../components/admin/ban-hang/PhanSanPham.vue";
import BangGioHang from "../../../components/admin/ban-hang/BangGioHang.vue";
import PhanGiaoHang from "../../../components/admin/ban-hang/PhanGiaoHang.vue";
import PhanKhachHang from "../../../components/admin/ban-hang/PhanKhachHang.vue";
import PhanThanhToan from "../../../components/admin/ban-hang/PhanThanhToan.vue";
import ModalSanPham from "../../../components/admin/ban-hang/ModalSanPham.vue";
import ModalQuetQR from "../../../components/admin/ban-hang/ModalQuetQR.vue";
import { LogicBanHangTaiQuay } from "../../../composable/LogicBanHangTaiQuay";
import { useRealtime } from "../../../composables/useRealtime";

const { subscribeTopic } = useRealtime();

const {
  TOI_DA_HOA_DON_CHO,
  danhSachHoaDonCho,
  dangTaiHoaDonCho,
  daDatGioiHanHoaDonCho,
  hoaDonChoDaChon,
  tuKhoaKhachHang,
  dangTaiKhachHang,
  hienThiDanhSachKhachHang,
  ketQuaTimKiemKhachHang,
  tenKhachHangHienThi,
  soDienThoaiKhachHangHienThi,
  khachHangDuocChon,
  laKhachVangLai,
  tuKhoaSanPham,
  dangTaiSanPham,
  hienThiDanhSachSanPham,
  ketQuaSanPham,
  sanPhamPhanTrang,
  trangHienTai,
  kichThuocTrang,
  tongSoMuc,
  tongSoTrang,
  boLocThuongHieuDaChon,
  boLocDanhMucDaChon,
  boLocMauSacDaChon,
  boLocKichCoDaChon,
  giaToiThieuDaChon,
  giaToiDaDaChon,
  giaToiDaCoSan,
  thuongHieuCoSan,
  danhMucCoSan,
  mauSacCoSan,
  kichCoCoSan,
  nhanTimKiemSanPham,
  cartItems,
  chiTietSanPhamDaChon,
  chiTietDangChon,
  hinhAnhDangChon,
  soLuongTonSauKhiChon,
  luaChonMauSac,
  luaChonKichCo,
  mauSacDaChon,
  kichCoDaChon,
  soLuongDaChon,
  soLuongTonKhaDungChiTiet,
  dangTaiChiTietHoaDon,
  tongSoLuong,
  tongTienSauGiamHienThi,
  tienGiam,
  tongTien,
  sanPhamValidationMessage,
  maPhieuGiamGia,
  coTheApDungPhieu,
  dangApDungPhieu,
  hienThiDanhSachPhieu,
  coTheTimPhieu,
  dangTaiPhieu,
  ketQuaTimKiemPhieu,
  phieuGiamGiaDaApDung,
  maPhieuChuaApDung,
  phieuGiamGiaMucTiepTheo,
  soTienThieuChoMucTiepTheo,
  soSanPhamThieuChoMucTiepTheo,
  soTienGiamMucTiepTheo,
  loiNhomPhieuGiamGiaTotHon,
  tuChoiPhieuGiamGiaTotHon,
  chapNhanPhieuGiamGiaTotHon,
  khachCanTra,
  thongTinGiaoHang,
  phuongThucThanhToan,
  tienKhachDua,
  thongBaoLoiThanhToan,
  tienThua,
  ghiChuThanhToan,
  coTheTaoHoaDonCho,
  dangLuuHoaDonCho,
  coTheThanhToan,
  dangThanhToan,
  dangHuyHoaDonCho,
  dinhDangTien,
  soLuongConLai,
  taiSanPham,
  xoaBanNhap,
  chonHoaDonCho,
  moDanhSachKhachHang,
  dongDanhSachKhachHang,
  chonKhachHang,
  chonKhachVangLai,
  boChonKhachHang,
  moDanhSachSanPham,
  dongDanhSachSanPham,
  moChiTietSanPham,
  themTrucTiepBienThe,
  xuLyQuetQrSanPham,
  tangSoLuong,
  giamSoLuong,
  dongChiTietSanPham,
  chonMauSac,
  chonKichCo,
  chonBienThe,
  giamSoLuongChiTiet,
  tangSoLuongChiTiet,
  capNhatSoLuongChiTiet,
  xoaSanPham,
  capNhatSoLuong,
  themBienTheDangChon,
  xuLyKhiFocusPhieu,
  xuLyKhiBlurPhieu,
  xuLyApDungPhieu,
  chonPhieuGiamGia,
  xuLyGoPhieu,
  capNhatThongTinGiaoHang,
  xuLyTinhPhiVanChuyen,
  xuLyTienKhachDuaInput,
  xuLyTaoHoaDonCho,
  xuLyTaoHoaDonChoMoi,
  xuLyThanhToanNgay,
  xuLyThanhToanSau,
  xuLyHuyHoaDonCho,
  xuLyInHoaDon,
  daInHoaDon,
  bienTheLienQuan
} = LogicBanHangTaiQuay();

import { onBeforeRouteLeave } from "vue-router";
import { useRealtime } from "../../../composables/useRealtime";

const router = useRouter();
const currentTime = ref("");
let timerInterval;

const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString("vi-VN", { hour: '2-digit', minute: '2-digit' });
};

onMounted(() => {
  updateTime();
  timerInterval = setInterval(updateTime, 60000);
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});

const { subscribeTopic } = useRealtime();

subscribeTopic('/topic/admin/san-pham', async (message) => {
  console.log("Realtime update (iPad): Product changed", message);
  taiSanPham();

  if (cartItems.value && cartItems.value.length > 0) {
    // Không tự động cập nhật giá trong giỏ hàng nữa theo yêu cầu của user
  }
});

subscribeTopic('/topic/admin/thuoc-tinh', (message) => {
  console.log("Realtime update (iPad): Attribute changed", message);
  taiSanPham();
});


onBeforeRouteLeave(async (to, from, next) => {
  if (cartItems.value && cartItems.value.length > 0 && !hoaDonChoDaChon.value) {
    try {
      await xuLyTaoHoaDonCho();
    } catch (error) {
      console.error("Tự động tạo hóa đơn chờ khi chuyển trang thất bại:", error);
    }
  }
  next();
});

const datTuKhoaKhachHang = (val) => { tuKhoaKhachHang.value = val; };
const datTuKhoaSanPham = (val) => { tuKhoaSanPham.value = val; };
const datTrangHienTai = (val) => { trangHienTai.value = val; };
const datKichThuocTrang = (val) => { kichThuocTrang.value = val; };
const datBoLocThuongHieu = (val) => { boLocThuongHieuDaChon.value = val; };
const datBoLocDanhMuc = (val) => { boLocDanhMucDaChon.value = val; };
const datBoLocMauSac = (val) => { boLocMauSacDaChon.value = val; };
const datBoLocKichCo = (val) => { boLocKichCoDaChon.value = val; };
const datGiaToiThieu = (val) => { giaToiThieuDaChon.value = val; };
const datGiaToiDa = (val) => { giaToiDaDaChon.value = val; };
const datMaPhieuGiamGia = (val) => { maPhieuGiamGia.value = val; };
const datPhuongThucThanhToan = (val) => { phuongThucThanhToan.value = val; };
const datGhiChuThanhToan = (val) => { ghiChuThanhToan.value = val; };

const showQrScanner = ref(false);

function moQuetQr() {
  showQrScanner.value = true;
}

function dongQuetQr() {
  showQrScanner.value = false;
}

function xuLyMaQuet(keyword) {
  dongQuetQr();
  if (keyword) {
    xuLyQuetQrSanPham(keyword);
  }
}
</script>

<template>
  <div class="flex items-center justify-center w-full h-full overflow-hidden bg-transparent p-2 lg:p-6">
    <!-- iPad Frame -->
    <div class="relative w-full max-w-[1280px] h-full max-h-[900px] bg-[#1c1c1e] rounded-[40px] lg:rounded-[48px] p-[12px] lg:p-[16px] shadow-[0_0_0_1px_rgba(255,255,255,0.05),0_24px_80px_rgba(0,0,0,0.2)] border-[2px] border-slate-800 flex flex-col">
      <!-- Volume buttons -->
      <div class="absolute left-[-4px] top-[120px] w-[4px] h-[50px] bg-slate-800 rounded-l-md hidden lg:block"></div>
      <div class="absolute left-[-4px] top-[190px] w-[4px] h-[50px] bg-slate-800 rounded-l-md hidden lg:block"></div>
      
      <!-- Power button -->
      <div class="absolute right-[100px] top-[-4px] w-[50px] h-[4px] bg-slate-800 rounded-t-md hidden lg:block"></div>

      <!-- Camera/Sensor -->
      <div class="absolute left-1/2 top-[6px] lg:top-[8px] -translate-x-1/2 flex items-center justify-center z-10 hidden lg:flex">
         <div class="w-2.5 h-2.5 rounded-full bg-black shadow-[inset_0_1px_2px_rgba(255,255,255,0.2)] ring-1 ring-slate-800"></div>
      </div>
      
      <!-- iPad Screen Area -->
      <div class="flex-1 w-full h-full bg-[#f4f4f9] dark:bg-slate-900 rounded-[28px] lg:rounded-[32px] overflow-hidden relative shadow-inner flex flex-col ring-1 ring-black/10">
    <!-- iPad App Header -->
    <header class="shrink-0 flex items-center justify-between px-6 py-3 bg-white/90 dark:bg-slate-800/90 backdrop-blur-md border-b border-slate-200/60 dark:border-slate-700/60 z-10 shadow-sm">
      <div class="flex items-center gap-4">
        <button @click="router.push('/admin/thong-ke')" class="p-2 -ml-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 transition-colors" title="Quay lại Admin">
          <ChevronLeft class="w-7 h-7" />
        </button>
      </div>
      
      <div class="flex items-center gap-4">
        <div class="hidden md:block text-right">
          <p class="text-sm font-bold text-slate-700 dark:text-slate-200">{{ currentTime }}</p>
          <p class="text-xs font-medium text-slate-500 dark:text-slate-400">Hôm nay</p>
        </div>
      </div>
    </header>

    <div class="flex-1 min-h-0 p-4 lg:p-6 overflow-hidden">
      <div class="grid h-full min-h-0 gap-6 lg:grid-cols-[2fr_1fr] xl:grid-cols-[2.2fr_1fr] items-stretch">
        <!-- Left Column: Pending Invoices, Cart -->
        <div class="flex flex-col gap-5 min-h-0 h-full">

        <!-- Pending Invoices Section -->
        <PhanHoaDonCho
          :pending-invoices="danhSachHoaDonCho"
          :loading-pending-invoices="dangTaiHoaDonCho"
          :max-pending-invoices="TOI_DA_HOA_DON_CHO"
          :pending-invoice-limit-reached="daDatGioiHanHoaDonCho"
          :active-pending-invoice="hoaDonChoDaChon"
          :dinh-dang-tien="dinhDangTien"
          @select-invoice="chonHoaDonCho"
          @create-empty-invoice="xuLyTaoHoaDonChoMoi"
        />

        <!-- Cart Section -->
        <section class="flex min-h-0 flex-1 flex-col overflow-hidden rounded-[24px] border border-slate-200/60 dark:border-slate-700/60 bg-white dark:bg-slate-800 shadow-sm transition-all">
          <div class="flex shrink-0 items-center justify-between border-b border-slate-100 dark:border-slate-700 p-4 bg-slate-50/50 dark:bg-slate-800/50">
            <div class="flex items-center gap-3">
              <h2 class="text-lg font-bold text-slate-800">Giỏ hàng</h2>
              <span class="rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-600">
                {{ tongSoLuong }} sản phẩm
              </span>
            </div>

            <div class="flex items-center gap-2">
              <PhanSanPham
                :active-pending-invoice="hoaDonChoDaChon"
                :product-keyword="tuKhoaSanPham"
                :loading-products="dangTaiSanPham"
                :show-product-dropdown="hienThiDanhSachSanPham"
                :product-results="ketQuaSanPham"
                :paginated-products="sanPhamPhanTrang"
                :current-page="trangHienTai"
                :page-size="kichThuocTrang"
                :total-items="tongSoMuc"
                :total-pages="tongSoTrang"
                :selected-brand-filter="boLocThuongHieuDaChon"
                :selected-category-filter="boLocDanhMucDaChon"
                :selected-color-filter="boLocMauSacDaChon"
                :selected-size-filter="boLocKichCoDaChon"
                :selected-min-price="giaToiThieuDaChon"
                :selected-max-price="giaToiDaDaChon"
                :max-available-price="giaToiDaCoSan"
                :available-brands="thuongHieuCoSan"
                :available-categories="danhMucCoSan"
                :available-colors="mauSacCoSan"
                :available-sizes="kichCoCoSan"
                :product-search-label="nhanTimKiemSanPham"
                :dinh-dang-tien="dinhDangTien"
                :so-luong-con-lai="soLuongConLai"
                @update:product-keyword="datTuKhoaSanPham"
                @update:current-page="datTrangHienTai"
                @update:page-size="datKichThuocTrang"
                @update:selected-brand-filter="datBoLocThuongHieu"
                @update:selected-category-filter="datBoLocDanhMuc"
                @update:selected-color-filter="datBoLocMauSac"
                @update:selected-size-filter="datBoLocKichCo"
                @update:selected-min-price="datGiaToiThieu"
                @update:selected-max-price="datGiaToiDa"
                @refresh="taiSanPham"
                @focus-product="moDanhSachSanPham"
                @blur-product="dongDanhSachSanPham"
                @open-product="themTrucTiepBienThe"
                @scan-product="xuLyQuetQrSanPham"
                @open-qr-scanner="moQuetQr"
                @update:show-product-dropdown="hienThiDanhSachSanPham = $event"
              />
            </div>
          </div>

          <div class="flex-1 overflow-y-auto p-4 custom-scrollbar">
            <BangGioHang
              :cart-items="cartItems"
              :dinh-dang-tien="dinhDangTien"
              :so-luong-con-lai="soLuongConLai"
              @increase-item="tangSoLuong"
              @decrease-item="giamSoLuong"
              @remove-item="xoaSanPham"
              @update-item="capNhatSoLuong"
            />
          </div>

          <div v-if="thongTinGiaoHang.giaoHang" class="p-4 border-t border-slate-100 bg-slate-50/50">
            <PhanGiaoHang
              :shipping-info="thongTinGiaoHang"
              :dinh-dang-tien="dinhDangTien"
              :customer-id="khachHangDuocChon?.id"
              @update-shipping="capNhatThongTinGiaoHang"
              @calculate-shipping="xuLyTinhPhiVanChuyen"
            />
          </div>
        </section>
      </div>

      <div class="flex flex-col gap-5 min-h-0 h-full overflow-y-auto pr-1 custom-scrollbar">

        <!-- Customer Section -->
        <section class="shrink-0 flex flex-col rounded-[24px] border border-slate-200/60 dark:border-slate-700/60 bg-white dark:bg-slate-800 p-4 shadow-sm transition-all">
          <h2 class="text-base font-bold text-slate-800 dark:text-slate-100 mb-3">Khách hàng</h2>
          <PhanKhachHang
            :customer-keyword="tuKhoaKhachHang"
            :loading-customers="dangTaiKhachHang"
            :show-customer-dropdown="hienThiDanhSachKhachHang"
            :customer-results="ketQuaTimKiemKhachHang"
            :ten-khach-hang-hien-thi="tenKhachHangHienThi"
            :so-dien-thoai-khach-hang-hien-thi="soDienThoaiKhachHangHienThi"
            :selected-customer="khachHangDuocChon"
            :is-guest-customer="laKhachVangLai"
            @update:customer-keyword="datTuKhoaKhachHang"
            @focus-customer="moDanhSachKhachHang"
            @blur-customer="dongDanhSachKhachHang"
            @select-customer="chonKhachHang"
            @select-guest="chonKhachVangLai"
            @clear-customer="boChonKhachHang"
          />
        </section>

        <!-- Order Info & Payment Section -->
        <section class="shrink-0 flex flex-col rounded-[24px] border border-slate-200/60 dark:border-slate-700/60 bg-white dark:bg-slate-800 p-4 shadow-sm transition-all">
          <div class="flex items-center justify-between mb-3">
            <h2 class="text-base font-bold text-slate-800 dark:text-slate-100">Thông tin đơn hàng</h2>
          </div>

          <PhanThanhToan
            :active-pending-invoice="hoaDonChoDaChon"
            :invoice-loading="dangTaiChiTietHoaDon"
            :tong-so-luong="tongSoLuong"
            :tong-tien-sau-giam-hien-thi="tongTienSauGiamHienThi"
            :tien-giam="tienGiam"
            :tong-tien="tongTien"
            :san-pham-validation-message="sanPhamValidationMessage"
            :coupon-code="maPhieuGiamGia"
            :co-the-ap-dung-phieu="coTheApDungPhieu"
            :applying-coupon="dangApDungPhieu"
            :show-coupon-dropdown="hienThiDanhSachPhieu"
            :co-the-tim-phieu="coTheTimPhieu"
            :loading-coupons="dangTaiPhieu"
            :coupon-results="ketQuaTimKiemPhieu"
            :next-tier-coupon="phieuGiamGiaMucTiepTheo"
            :missing-amount-for-next-tier="soTienThieuChoMucTiepTheo"
            :missing-products-for-next-tier="soSanPhamThieuChoMucTiepTheo"
            :next-tier-discount-amount="soTienGiamMucTiepTheo"
            :applied-coupon="phieuGiamGiaDaApDung"
            :ma-phieu-chua-ap-dung="maPhieuChuaApDung"
            :khach-can-tra="khachCanTra"
            :is-guest-customer="laKhachVangLai"
            :shipping-info="thongTinGiaoHang"
            :ten-khach-hang-hien-thi="tenKhachHangHienThi"
            :so-dien-thoai-khach-hang-hien-thi="soDienThoaiKhachHangHienThi"
            :payment-method="phuongThucThanhToan"
            :amount-paid="tienKhachDua"
            :payment-validation-message="thongBaoLoiThanhToan"
            :tien-thua="tienThua"
            :payment-note="ghiChuThanhToan"
            :can-create-pending-invoice="coTheTaoHoaDonCho"
            :saving-pending-invoice="dangLuuHoaDonCho"
            :pending-invoice-limit-reached="daDatGioiHanHoaDonCho"
            :can-pay="coTheThanhToan"
            :paying-invoice="dangThanhToan"
            :canceling-pending-invoice="dangHuyHoaDonCho"
            :dinh-dang-tien="dinhDangTien"
            :so-luong-con-lai="soLuongConLai"
            :has-printed-invoice="daInHoaDon"
            :better-coupon-prompt="loiNhomPhieuGiamGiaTotHon"
            @update:coupon-code="datMaPhieuGiamGia"
            @focus-coupon="xuLyKhiFocusPhieu"
            @blur-coupon="xuLyKhiBlurPhieu"
            @apply-coupon="xuLyApDungPhieu"
            @select-coupon="chonPhieuGiamGia"
            @remove-coupon="xuLyGoPhieu"
            @update-shipping="capNhatThongTinGiaoHang"
            @calculate-shipping="xuLyTinhPhiVanChuyen"
            @update:payment-method="datPhuongThucThanhToan"
            @amount-input="xuLyTienKhachDuaInput"
            @update:payment-note="datGhiChuThanhToan"
            @print-invoice="xuLyInHoaDon"
            @pay-now="xuLyThanhToanNgay"
            @pay-later="xuLyThanhToanSau"
            @cancel-pending-invoice="xuLyHuyHoaDonCho"
            @create-empty-invoice="xuLyTaoHoaDonChoMoi"
            @reject-better-coupon="tuChoiPhieuGiamGiaTotHon"
            @accept-better-coupon="chapNhanPhieuGiamGiaTotHon"
          />
        </section>
        </div>
      </div>
    </div>
      </div>
      
      <!-- Home Indicator -->
      <div class="absolute bottom-[6px] lg:bottom-[8px] left-1/2 -translate-x-1/2 w-24 lg:w-32 h-1.5 bg-slate-600/50 rounded-full z-10 hidden lg:block"></div>
    </div>
    
    <ModalSanPham
      :selected-product-detail="chiTietSanPhamDaChon"
      :chi-tiet-dang-chon="chiTietDangChon"
      :current-product-image="hinhAnhDangChon"
      :so-luong-ton-sau-khi-chon="soLuongTonSauKhiChon"
      :bien-the-lien-quan="bienTheLienQuan"
      :color-options="luaChonMauSac"
      :size-options="luaChonKichCo"
      :selected-color="mauSacDaChon"
      :selected-size="kichCoDaChon"
      :selected-quantity="soLuongDaChon"
      :so-luong-ton-kha-dung-chi-tiet="soLuongTonKhaDungChiTiet"
      :dinh-dang-tien="dinhDangTien"
      @close="dongChiTietSanPham"
      @select-color="chonMauSac"
      @select-size="chonKichCo"
      @select-variant="chonBienThe"
      @decrease-quantity="giamSoLuongChiTiet"
      @increase-quantity="tangSoLuongChiTiet"
      @update-quantity="capNhatSoLuongChiTiet"
      @add-selected-variant="themBienTheDangChon"
    />

    <ModalQuetQR
      :open="showQrScanner"
      :show-manual-section="false"
      :show-camera-hint="false"
      :show-retry-button="false"
      @close="dongQuetQr"
      @scan="xuLyMaQuet"
    />

  </div>
</template>
