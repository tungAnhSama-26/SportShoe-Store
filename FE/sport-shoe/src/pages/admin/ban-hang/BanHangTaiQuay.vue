<script setup>
import PhanHoaDonCho from "../../../components/admin/ban-hang/PhanHoaDonCho.vue";
import PhanSanPham from "../../../components/admin/ban-hang/PhanSanPham.vue";
import BangGioHang from "../../../components/admin/ban-hang/BangGioHang.vue";
import PhanGiaoHang from "../../../components/admin/ban-hang/PhanGiaoHang.vue";
import PhanKhachHang from "../../../components/admin/ban-hang/PhanKhachHang.vue";
import PhanThanhToan from "../../../components/admin/ban-hang/PhanThanhToan.vue";
import ModalSanPham from "../../../components/admin/ban-hang/ModalSanPham.vue";
import ModalQuetQR from "../../../components/admin/ban-hang/ModalQuetQR.vue";
import ModalThemNhanhKhachHang from "../../../components/admin/ban-hang/ModalThemNhanhKhachHang.vue";
import { LogicBanHangTaiQuay } from "../../../composable/LogicBanHangTaiQuay";
import { ref } from "vue";

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
  tienMatKetHop,
  tienChuyenKhoanKetHop,
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
  itemDangDoiBienThe,
  xuLyMoDoiBienTheInCart,
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
  xuLyTienMatKetHopInput,
  xuLyTienChuyenKhoanKetHopInput,
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

const showAddCustomerModal = ref(false);
const sdtGoiY = ref("");

function moThemNhanhKhachHang(keyword) {
  sdtGoiY.value = keyword && !isNaN(keyword.replace(/\s/g, '')) ? keyword : "";
  showAddCustomerModal.value = true;
}

function dongThemNhanhKhachHang() {
  showAddCustomerModal.value = false;
}

function xuLyThemKhachHang(khachHangMoi) {
  // Khi thêm thành công, ta tự động gán keyword bằng sdt khách và gọi selectKhachHang hoặc tương tự
  tuKhoaKhachHang.value = khachHangMoi.sdt || khachHangMoi.hoTen;
  // Cố gắng chọn khách hàng đó luôn
  chonKhachHang(khachHangMoi);
}
</script>

<template>
  <div class="flex flex-col gap-2 p-2 h-full lg:overflow-hidden overflow-y-auto custom-scrollbar radius-6px">
    <div class="grid min-h-0 flex-1 gap-4 lg:grid-cols-[2fr_1fr] items-stretch">
      <!-- Left Column: Pending Invoices, Cart -->
      <div class="flex flex-col gap-4 min-h-0 lg:h-auto h-[600px] lg:flex-1 shrink-0">

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
        <section class="flex min-h-0 flex-1 flex-col overflow-hidden rounded-[24px] border border-white/70 bg-white/95 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
          <div class="flex shrink-0 items-center justify-between border-b border-slate-100 p-4">
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
              @edit-item="xuLyMoDoiBienTheInCart"
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

      <div class="flex flex-col gap-3 lg:h-full lg:overflow-y-auto pr-1 custom-scrollbar shrink-0">

        <!-- Customer Section -->
        <section class="shrink-0 flex flex-col rounded-[24px] border border-white/70 bg-white/95 p-3 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
          <h2 class="text-base font-bold text-slate-800 mb-2">Khách hàng</h2>
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
            @open-add-customer="moThemNhanhKhachHang"
          />
        </section>

        <!-- Order Info & Payment Section -->
        <section class="shrink-0 flex flex-col rounded-[24px] border border-white/70 bg-white/95 p-3 shadow-[0_24px_6px_rgba(15,23,42,0.08)]">
          <div class="flex items-center justify-between mb-2">
            <h2 class="text-base font-bold text-slate-800">Thông tin đơn hàng</h2>
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
            :tien-mat-ket-hop="tienMatKetHop"
            :tien-chuyen-khoan-ket-hop="tienChuyenKhoanKetHop"
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
            @cash-split-input="xuLyTienMatKetHopInput"
            @transfer-split-input="xuLyTienChuyenKhoanKetHopInput"
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
      :so-luong-con-lai="soLuongConLai"
      :is-edit-mode="Boolean(itemDangDoiBienThe)"
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
      :fallback-helper-text="''"
      :show-manual-section="false"
      :show-camera-hint="false"
      :show-retry-button="false"
      @close="dongQuetQr"
      @scan="xuLyMaQuet"
    />

    <ModalThemNhanhKhachHang
      :open="showAddCustomerModal"
      :sdt-goi-y="sdtGoiY"
      @close="dongThemNhanhKhachHang"
      @created="xuLyThemKhachHang"
    />

  </div>
</template>
