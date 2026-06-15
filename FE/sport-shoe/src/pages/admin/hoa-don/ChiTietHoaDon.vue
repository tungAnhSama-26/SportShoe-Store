<script setup lang="ts">
import { useChiTietHoaDon } from "./useChiTietHoaDon";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import TaoPhieuTraHangModal from "../../../components/admin/hoa-don/TaoPhieuTraHangModal.vue";
import ChinhSuaGiaoHangModal from "../../../components/common/ChinhSuaGiaoHangModal.vue";
import { API_BASE_URL } from "../../../services/api-client";
const { computed, onMounted, ref, watch, markRaw, useRoute, useRouter, ArrowLeft, Banknote, CheckCircle2, CircleCheck, CircleX, ClipboardList, ClipboardCheck, Flag, History, Hourglass, MapPin, Package, Pencil, Printer, Search, Trash2, TriangleAlert, Truck, User, X, Card, Button, capNhatSanPhamHoaDon, capNhatTrangThaiHoaDon, layChiTietHoaDon, tinhPhiVanChuyenGhn, xacNhanHoanTien, xacNhanThanhToanCod, timSanPhamTaiQuay, printInvoiceToPdf, getDisplayErrorMessage, logoGhn, route, router, hoaDon, dangTai, loiTrang, dangCapNhat, hienModalXacNhan, hienModalLichSu, hienModalSanPham, hienModalXacNhanHuy, hienModalThanhToanCod, dangXacNhanThanhToanCod, formThanhToanCod, hienModalHoanTien, dangXacNhanHoanTien, formHoanTien, hienModalThongTin, hienModalGiaoHang, dangLuuGiaoHang, diaChiDaLuu, tabHienTai, formThongTin, formGhn, dangTinhPhiGhn, diaChiGhnDaDo, trangThaiMoiXacNhan, ghiChuXacNhan, tuKhoaSanPham, ketQuaTimKiem, dangTimKiem, giaTuSanPham, giaDenSanPham, tuKhoaLocSanPham, loaiSanPhamDangLoc, sapXepSanPham, danhSachLoaiSanPham, giaTuSanPhamSo, giaDenSanPhamSo, giaLonNhatSanPham, nhanKhoangGiaSanPham, styleKhoangGiaSanPham, trangSanPhamHienTai, soSanPhamMoiTrang, danhSachSanPhamDaLoc, danhSachSanPhamPhanTrang, tongTrangSanPham, hienPhanTrangSanPham, danhSachSanPhamUpdate, cacBuocCoDinh, cacBuocYeuCauHuy, cacBuocDaHuy, laDonTaiQuay, cacBuoc, dinhDangTien, dinhDangNgay, dinhDangGio, vietHoaChuCaiDau, buocHienTai, donDaHoanThanh, donYeuCauHuy, donDaHuy, donDaKetThuc, coTheSuaThongTinGiaoHang, hienThiThongBao, thongBaoDonDaHoanThanh, moModalThongTin, moModalSuaDiaChi, handleLuuGiaoHang, tongTienHang, tongKhachCanTra, coPhieuGiamGia, moTaGiaTriPhieuGiamGia, thanhToanGanNhat, thanhToanCodDangCho, coTheThanhToanCod, thanhToanCanHoanTien, coTheHoanTien, tongTienHoan, tongTienThanhToanCod, noiDungChuyenKhoanCod, qrThanhToanCodUrl, tienThieuThanhToanCod, lichSuRutGon, thongTinBuoc, cacBuocHienThi, lopVongTrangThai, lopTenTrangThai, taiChiTiet, openModalXacNhan, handleXacNhanTrangThai, handleXuLyYeuCauHuy, moModalXacNhanHuy, handleXacNhanHuyDon, timKiemSanPham, themSanPham, removeSanPham, handleSaveSanPham, danhSachTrangThaiHienThi, indexTrangThaiHienTai, isOptionDisabled, hienThiOptionTrangThai, handleLuuThongTin, handleTinhPhiGhn, handlePrint, moModalThanhToanCod, handleXacNhanThanhToanCod, moModalHoanTien, handleXacNhanHoanTien } = useChiTietHoaDon();

const productImageFallback =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='72' height='72' viewBox='0 0 72 72'%3E%3Crect width='72' height='72' rx='14' fill='%23f8fafc'/%3E%3Cpath d='M18 44h35c3 0 5-2 5-5 0-2-1-4-3-5l-10-5-7 8H25l-5-5-6 5v3c0 2 2 4 4 4z' fill='%23e2e8f0'/%3E%3Cpath d='M24 48h30' stroke='%2394a3b8' stroke-width='3' stroke-linecap='round'/%3E%3C/svg%3E";
const apiOrigin = API_BASE_URL.replace(/\/api\/v1\/?$/, "");

function resolveProductImageUrl(url?: string) {
  const value = String(url || "").trim();
  if (!value) return productImageFallback;
  if (/^(https?:|data:|blob:)/i.test(value)) return value;
  if (value.startsWith("/uploads/")) return `${apiOrigin}${value}`;
  if (value.startsWith("uploads/")) return `${apiOrigin}/${value}`;
  return value.startsWith("/") ? value : `/${value}`;
}

function handleProductImageError(event: Event) {
  const target = event.currentTarget as HTMLImageElement | null;
  if (target && target.src !== productImageFallback) {
    target.src = productImageFallback;
  }
}

</script>

<template>
  <div class="invoice-flat space-y-4 pb-10">
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-[22px] font-bold leading-tight text-slate-800 md:text-[24px]">Chi Tiết Đơn Hàng</h1>
        <div v-if="hoaDon" class="mt-2 space-y-1 text-[13px] text-slate-500">
          <p>
            Mã Đơn Hàng: <span class="font-semibold text-slate-700">{{ hoaDon.maHoaDon }}</span>
            <span class="mx-2 text-slate-300">|</span>
            Ngày Tạo: {{ dinhDangGio(hoaDon.ngayTao) }} {{ dinhDangNgay(hoaDon.ngayTao) }}
          </p>
          <p>
            Tạo Bởi:
            <span class="font-medium text-slate-700">{{ hoaDon.nguoiTao || "Hệ Thống" }}</span>
            <span class="mx-2 text-slate-300">|</span>
            Cập Nhật Gần Nhất:
            <span class="font-medium text-slate-700">
              {{
                hoaDon.lichSuHoaDon?.[0]
                  ? `${dinhDangGio(hoaDon.lichSuHoaDon[0].ngayTao)} ${dinhDangNgay(hoaDon.lichSuHoaDon[0].ngayTao)} -
              ${hoaDon.lichSuHoaDon[0].maNhanVien}`
                  : "Chưa Có"
              }}
            </span>
          </p>
        </div>
      </div>

      <Button variant="soft" @click="router.push({ name: 'admin-hoa-don' })" class="h-10">
        <template #prefix>
          <ArrowLeft class="h-4 w-4" />
        </template>
        Quay Lại Danh Sách
      </Button>
    </div>

    <Card v-if="dangTai" class="p-10 text-center text-sm text-slate-400">
      Đang Tải Chi Tiết Hóa Đơn...
    </Card>

    <Card v-else-if="loiTrang || !hoaDon" class="p-10 text-center">
      <h2 class="text-2xl font-bold text-slate-800">Không Tìm Thấy Hóa Đơn</h2>
      <p class="mt-3 text-sm text-slate-400">{{ loiTrang || "Hóa Đơn Không Tồn Tại." }}</p>
    </Card>

    <template v-else>
      <section class="grid items-stretch gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
        <Card class="flex h-full flex-col px-4 py-4 md:px-5 xl:col-span-2">
          <template #header>
            <div class="flex w-full flex-wrap items-center justify-between gap-3">
              <div class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
                <ClipboardList class="h-4.5 w-4.5 text-slate-500" />
                Trạng Thái Đơn Hàng
              </div>
              <div class="flex flex-wrap items-center justify-end gap-2">
                <span class="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
                  {{ hoaDon.trangThai }}
                </span>
              </div>
            </div>
          </template>

          <div class="relative mt-7 px-2 pt-2 flex justify-center">
            <div class="flex w-full items-start justify-around relative max-w-4xl">
              <!-- Đường ziczac động -->
              <div class="absolute top-7 hidden h-[2px] bg-slate-200 md:block z-0"
                :style="{ left: (100 / cacBuocHienThi.length / 2) + '%', right: (100 / cacBuocHienThi.length / 2) + '%' }">
              </div>

              <div v-for="buoc in cacBuocHienThi" :key="buoc.id"
                class="relative z-10 flex w-32 flex-col items-center text-center">
                <div
                  class="flex h-[58px] w-[58px] items-center justify-center overflow-visible rounded-full border-[2.5px] transition-all"
                  :class="lopVongTrangThai(buoc)">
                  <component :is="buoc.icon" class="h-[22px] w-[22px] block shrink-0" stroke-width="2.25" />
                </div>
                <p class="mt-3 whitespace-nowrap text-[12px] font-semibold" :class="lopTenTrangThai(buoc)">{{ buoc.ten
                  }}</p>
                <div class="mt-1 min-h-[32px]">
                  <p v-if="buoc.thoiGian" class="text-[11px] leading-4 text-slate-400">
                    {{ dinhDangGio(buoc.thoiGian) }} {{ dinhDangNgay(buoc.thoiGian) }}
                  </p>
                  <p v-if="buoc.nhanVien" class="text-[11px] text-slate-400">{{ buoc.nhanVien }}</p>
                </div>
              </div>
            </div>
          </div>

          <div v-if="donYeuCauHuy"
            class="mt-5 flex items-center justify-center gap-2 rounded-lg border border-amber-200 bg-amber-50 px-4 py-2.5 text-sm font-semibold text-amber-700">
            <TriangleAlert class="h-4 w-4" />
            Khách hàng yêu cầu hủy - đang chờ xác nhận
          </div>
          <div v-if="donDaHuy"
            class="mt-5 flex items-center justify-center gap-2 rounded-lg border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm font-semibold text-rose-700">
            <CircleX class="h-4 w-4" />
            Đơn hàng đã bị hủy
          </div>

          <div class="mt-5 flex justify-end">
            <Button variant="primary" @click="hienModalLichSu = true">
              <template #prefix>
                <History class="h-4 w-4" />
              </template>
              Lịch Sử Thao Tác
            </Button>
          </div>
        </Card>

        <Card class="flex h-full flex-col px-5 py-4">
          <template #header>
            <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
              <Banknote class="h-4.5 w-4.5 text-slate-500" />
              Tổng Kết Thanh Toán
            </h2>
          </template>

          <div class="mt-4 flex-1 space-y-3 text-sm">
            <div class="flex items-center justify-between">
              <span class="text-slate-500">Tổng Tiền Hàng</span>
              <span class="font-semibold text-slate-700">{{ dinhDangTien(tongTienHang) }}</span>
            </div>
            <div v-if="coPhieuGiamGia" class="flex items-center justify-between">
              <span class="text-slate-500">
                Phiếu Giảm Giá
                <span class="ml-1 font-semibold text-slate-600">
                  {{ hoaDon.voucher }}
                </span>
              </span>
              <span class="font-semibold text-emerald-500">{{ moTaGiaTriPhieuGiamGia }}</span>
            </div>
            <div v-if="Number(hoaDon.phiVanChuyen || 0) > 0" class="flex items-center justify-between">
              <span class="flex items-center gap-2 text-slate-500">
                Phí Vận Chuyển
                <img :src="logoGhn" alt="GHN" class="h-4 w-auto object-contain" />
              </span>
              <span class="font-semibold text-slate-700">+ {{ dinhDangTien(hoaDon.phiVanChuyen) }}</span>
            </div>

            <div class="border-t border-slate-200 pt-4">
              <div class="flex items-center justify-between">
                <span class="text-[15px] font-bold tracking-wide text-[#B82220]">Tổng Tiền</span>
                <span class="text-[18px] font-bold text-[#B82220]">{{ dinhDangTien(tongKhachCanTra) }}</span>
              </div>
            </div>
          </div>
        </Card>
      </section>

      <section class="grid gap-3 xl:grid-cols-[1fr_1fr_0.95fr]">
        <Card class="px-5 py-4">
          <template #header>
            <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
              <User class="h-4.5 w-4.5 text-slate-500" />
              Thông Tin Khách Hàng
            </h2>
          </template>

          <div class="mt-4 space-y-4 text-sm">
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <span class="text-slate-400">Tên Khách Hàng</span>
              <span class="font-semibold text-slate-700">{{ hoaDon.tenKhachHang }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <span class="text-slate-400">Số Điện Thoại</span>
              <span class="font-semibold text-slate-700">{{ hoaDon.soDienThoai }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-400">Email</span>
              <span class="font-semibold text-slate-700">{{ hoaDon.email || "—" }}</span>
            </div>
          </div>
        </Card>

        <Card class="px-5 py-4">
          <template #header>
            <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
              <MapPin class="h-4.5 w-4.5 text-slate-500" />
              Thông Tin Giao Hàng
            </h2>
          </template>

          <div class="mt-4 space-y-4 text-sm">
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <span class="text-slate-400">Địa Chỉ</span>
              <span class="max-w-[58%] text-right font-semibold text-slate-700">{{ hoaDon.diaChi || "—" }}</span>
            </div>
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <span class="text-slate-400">Loại Đơn</span>
              <span class="font-semibold text-slate-700">{{ hoaDon.loaiDon }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-400">Ghi Chú</span>
              <span class="max-w-[58%] text-right font-semibold text-slate-700">{{ hoaDon.ghiChu || "—" }}</span>
            </div>
          </div>
        </Card>

        <div class="space-y-3">
          <Card class="px-5 py-4">
            <template #header>
              <div class="flex items-center justify-between gap-3">
                <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
                  <History class="h-4.5 w-4.5 text-slate-500" />
                  Lịch Sử Thanh Toán
                </h2>
                <div class="flex flex-wrap items-center justify-end gap-2">
                  <button v-if="hoaDon.phieuTraHangId" type="button"
                    class="inline-flex h-9 items-center gap-2 rounded-full border border-[#B82220]/20 bg-[#B82220]/5 px-3.5 text-xs font-bold text-[#B82220] transition hover:bg-[#B82220]/10 active:scale-95"
                    @click="router.push({ name: 'admin-tra-hang-chi-tiet', params: { id: hoaDon.phieuTraHangId } })">
                    <Package class="h-4 w-4" />
                    {{ hoaDon.trangThaiPhieuTraHang === 6 ? 'Xử lý trả hàng' : 'Xem phiếu trả hàng' }}
                  </button>
                  <button v-if="coTheThanhToanCod" type="button"
                    class="inline-flex h-9 items-center gap-2 rounded-full bg-[#B82220] px-3.5 text-xs font-bold text-white shadow-[0_10px_22px_rgba(184,34,32,0.22)] transition hover:bg-[#991b1b] active:scale-95"
                    @click="moModalThanhToanCod">
                    <Banknote class="h-4 w-4" />
                    Thanh toán
                  </button>
                  <button v-if="coTheHoanTien" type="button"
                    class="inline-flex h-9 items-center gap-2 rounded-full bg-amber-500 px-3.5 text-xs font-bold text-white shadow-[0_10px_22px_rgba(245,158,11,0.22)] transition hover:bg-amber-600 active:scale-95"
                    @click="moModalHoanTien">
                    <Banknote class="h-4 w-4" />
                    Hoàn tiền
                  </button>
                </div>
              </div>
            </template>

            <div v-if="hoaDon.lichSuThanhToan?.length" class="mt-4 space-y-3 text-sm">
              <div v-for="thanhToan in hoaDon.lichSuThanhToan" :key="thanhToan.id"
                class="rounded-2xl border border-slate-100 bg-slate-50/60 p-3">
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0">
                    <p class="font-semibold text-slate-700">{{ thanhToan.phuongThucThanhToan }}</p>
                    <p class="mt-1 text-xs text-slate-400">{{ thanhToan.loaiGiaoDich }}</p>
                  </div>
                  <span class="shrink-0 rounded-full px-2.5 py-1 text-[11px] font-semibold" :class="thanhToan.trangThaiThanhToan === 'Đã thanh toán'
                    ? 'bg-emerald-50 text-emerald-600'
                    : thanhToan.trangThaiThanhToan === 'Cần hoàn tiền'
                      ? 'bg-amber-50 text-amber-600'
                      : thanhToan.trangThaiThanhToan === 'Đã hoàn tiền'
                        ? 'bg-sky-50 text-sky-600'
                        : thanhToan.trangThaiThanhToan === 'Đã hủy' || thanhToan.trangThaiThanhToan === 'Thanh toán thất bại'
                          ? 'bg-rose-50 text-rose-600'
                          : 'bg-slate-100 text-slate-500'">
                    {{ thanhToan.trangThaiThanhToan }}
                  </span>
                </div>
                <div class="mt-3 flex items-center justify-between gap-3">
                  <span class="text-xs text-slate-400">
                    {{ dinhDangGio(thanhToan.thoiGian) }} {{ dinhDangNgay(thanhToan.thoiGian) }}
                  </span>
                  <span class="font-bold text-[#B82220]">{{ dinhDangTien(thanhToan.tongTien) }}</span>
                </div>
                <p v-if="thanhToan.ghiChu" class="mt-2 text-xs text-slate-500">{{ thanhToan.ghiChu }}</p>
              </div>
            </div>
            <div v-else class="mt-4 text-sm text-slate-400">Chưa Có Lịch Sử Thanh Toán.</div>
          </Card>

          <div v-if="donYeuCauHuy" class="rounded-[26px] border border-rose-100 bg-white px-5 py-4 shadow-sm">
            <h2 class="flex items-center gap-2 text-[15px] font-semibold text-[#B82220]">
              <TriangleAlert class="h-4.5 w-4.5" />
              Khách Hàng Yêu Cầu Hủy Đơn
            </h2>
            <p class="mt-2 text-xs text-slate-500">Xem lịch sử thao tác để biết lý do yêu cầu hủy.</p>
            <div class="mt-4 grid gap-2 sm:grid-cols-2">
              <button type="button" @click="moModalXacNhanHuy" :disabled="dangCapNhat"
                class="inline-flex h-9 items-center justify-center gap-2 rounded-full bg-[#B82220] px-4 text-sm font-semibold text-white transition hover:bg-[#B82220]/90 disabled:cursor-not-allowed disabled:opacity-60">
                <CircleCheck class="h-4 w-4" />
                Xác Nhận Hủy
              </button>
              <button type="button" @click="handleXuLyYeuCauHuy('Chờ xác nhận')" :disabled="dangCapNhat"
                class="inline-flex h-9 items-center justify-center gap-2 rounded-full border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">
                <CircleX class="h-4 w-4" />
                Từ Chối Hủy
              </button>
            </div>
          </div>

          <Button v-if="donDaHoanThanh" @click="handlePrint"
            class="w-full bg-sky-500 hover:bg-sky-600 text-white border-transparent">
            <template #prefix>
              <Printer class="h-4 w-4" />
            </template>
            In Hóa Đơn
          </Button>

          <TaoPhieuTraHangModal v-if="laDonTaiQuay" :hoa-don="hoaDon" />

          <Button v-if="!donDaKetThuc" @click="moModalThongTin"
            class="w-full bg-amber-500 hover:bg-amber-600 text-white border-transparent">
            <template #prefix>
              <Pencil class="h-4 w-4" />
            </template>
            Chỉnh Sửa Đơn Hàng
          </Button>
        </div>
      </section>

      <section class="grid gap-3">
        <Card class="px-6 py-5">
          <template #header>
            <div>
              <h2 class="flex shrink-0 items-center gap-2 whitespace-nowrap text-base font-semibold text-slate-700">
                <span class="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-50 text-slate-500">
                  <Package class="h-5 w-5" />
                </span>
                <span>Danh Sách Sản Phẩm ({{ hoaDon.sanPham?.length || 0 }})</span>
              </h2>
            </div>
          </template>

          <div class="mb-6 space-y-4 rounded-2xl border border-slate-100 bg-slate-50/50 p-4">
            <div class="grid gap-3 lg:grid-cols-[minmax(260px,1.35fr)_minmax(190px,0.85fr)_minmax(210px,0.9fr)]">
              <label class="block">
                <span class="mb-1.5 block text-xs font-semibold text-slate-500">Tìm kiếm</span>
                <div class="relative">
                  <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                  <input v-model.trim="tuKhoaLocSanPham" type="text"
                    class="h-11 w-full rounded-xl border border-slate-200 bg-white pl-9 pr-3 text-sm font-medium text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-[#B82220] focus:ring-2 focus:ring-rose-100"
                    placeholder="Tên sản phẩm, màu, size..." />
                </div>
              </label>

              <label class="block">
                <span class="mb-1.5 block text-xs font-semibold text-slate-500">Loại sản phẩm</span>
                <select v-model="loaiSanPhamDangLoc"
                  class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 outline-none transition focus:border-[#B82220] focus:ring-2 focus:ring-rose-100">
                  <option value="">Tất cả loại</option>
                  <option v-for="loai in danhSachLoaiSanPham" :key="loai" :value="loai">{{ loai }}</option>
                </select>
              </label>

              <label class="block">
                <span class="mb-1.5 block text-xs font-semibold text-slate-500">Sắp xếp</span>
                <select v-model="sapXepSanPham"
                  class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 outline-none transition focus:border-[#B82220] focus:ring-2 focus:ring-rose-100">
                  <option value="macDinh">Mặc định</option>
                  <option value="giaTang">Giá thấp đến cao</option>
                  <option value="giaGiam">Giá cao đến thấp</option>
                  <option value="soLuongGiam">Số lượng nhiều nhất</option>
                  <option value="tongTienGiam">Tổng tiền cao nhất</option>
                </select>
              </label>
            </div>

            <div class="w-full">
              <div class="mb-3 flex items-center justify-between gap-4 text-xs font-semibold text-slate-400">
                <span>Khoảng giá</span>
                <span class="text-right text-slate-500">{{ nhanKhoangGiaSanPham }}</span>
              </div>
              <div class="relative h-8 w-full">
                <div class="absolute left-[9px] right-[9px] top-1/2 h-1 -translate-y-1/2 rounded-full"
                  :style="styleKhoangGiaSanPham"></div>
                <input v-model.number="giaTuSanPhamSo" type="range" min="0" :max="giaLonNhatSanPham || 0" step="1"
                  class="price-range-input" aria-label="Giá từ" />
                <input v-model.number="giaDenSanPhamSo" type="range" min="0" :max="giaLonNhatSanPham || 0" step="1"
                  class="price-range-input" aria-label="Giá đến" />
              </div>
            </div>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full table-auto text-[15px]">
              <thead>
                <tr class="bg-slate-100 text-left text-xs font-bold tracking-wide text-slate-950">
                  <th class="rounded-l-2xl px-5 py-3.5">STT</th>
                  <th class="px-5 py-3.5">Ảnh</th>
                  <th class="px-5 py-3.5">Sản Phẩm</th>
                  <th class="px-5 py-3.5">Số Lượng</th>
                  <th class="px-5 py-3.5">Thời Gian</th>
                  <th class="px-5 py-3.5">Đơn Giá</th>
                  <th class="rounded-r-2xl px-5 py-3.5">Tổng Tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!danhSachSanPhamPhanTrang.length">
                  <td colspan="7" class="px-5 py-10 text-center text-sm text-slate-400">Không có sản phẩm phù hợp với bộ
                    lọc.
                  </td>
                </tr>
                <tr v-for="(item, index) in danhSachSanPhamPhanTrang" :key="item.id"
                  class="border-b border-slate-100 last:border-b-0">
                  <td class="px-5 py-6 font-semibold text-slate-600">{{ (trangSanPhamHienTai - 1) * soSanPhamMoiTrang +
                    index
                    + 1 }}</td>
                  <td class="px-5 py-6">
                    <img :src="resolveProductImageUrl(item.hinhAnh)" @error="handleProductImageError"
                      class="h-14 w-14 rounded-xl object-cover" />
                  </td>
                  <td class="px-5 py-6">
                    <p class="text-base font-semibold text-slate-800">{{ vietHoaChuCaiDau(item.tenSanPham) }}</p>
                    <p class="mt-1 text-sm text-slate-400">{{ item.phanLoai }}</p>
                  </td>
                  <td class="px-5 py-6 font-semibold text-slate-700">{{ item.soLuong }}</td>
                  <td class="px-5 py-6">
                    <p class="text-sm font-semibold text-slate-700">{{ dinhDangGio(hoaDon.ngayTao) }}</p>
                    <p class="text-sm text-slate-400">{{ dinhDangNgay(hoaDon.ngayTao) }}</p>
                  </td>
                  <td class="px-5 py-6 font-semibold text-[#B82220]">{{ dinhDangTien(item.donGia) }}</td>
                  <td class="px-5 py-6 font-semibold text-[#B82220]">{{ dinhDangTien(item.thanhTien) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <AdminTableFooter v-if="hienPhanTrangSanPham" :current-page="trangSanPhamHienTai"
            :page-size="soSanPhamMoiTrang" :page-size-options="[5]" :total-items="danhSachSanPhamDaLoc.length"
            :total-pages="tongTrangSanPham" compact @update:current-page="trangSanPhamHienTai = $event"
            @update:page-size="() => { }" />
        </Card>
      </section>
    </template>

    <div v-if="hienModalThanhToanCod"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-3 backdrop-blur-sm">
      <div
        class="flex max-h-[calc(100vh-1.5rem)] w-full max-w-[780px] flex-col overflow-hidden rounded-[24px] border border-white/70 bg-white shadow-[0_28px_90px_rgba(15,23,42,0.28)]">
        <div class="relative shrink-0 bg-gradient-to-br from-white via-rose-50/70 to-white px-5 pb-3 pt-4">
          <button type="button"
            class="absolute right-5 top-5 inline-flex h-9 w-9 items-center justify-center rounded-full bg-white/80 text-slate-400 shadow-sm transition hover:bg-white hover:text-slate-700"
            @click="hienModalThanhToanCod = false">
            <X class="h-5 w-5" />
          </button>
          <div class="flex items-center gap-3">
            <div
              class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#B82220] text-white shadow-[0_14px_30px_rgba(184,34,32,0.25)]">
              <Banknote class="h-6 w-6" />
            </div>
            <div>
              <h3 class="text-lg font-extrabold uppercase tracking-wide text-slate-900">Thanh toán COD</h3>
              <p class="mt-1 text-sm text-slate-500">{{ hoaDon.maHoaDon }} - {{ hoaDon.tenKhachHang }}</p>
            </div>
          </div>

          <div class="mt-4 rounded-2xl border border-rose-100 bg-white/80 px-4 py-3">
            <div class="flex items-center justify-between">
              <span class="text-sm font-semibold text-slate-500">Tổng tiền hàng</span>
              <span class="text-lg font-extrabold text-[#B82220]">{{ dinhDangTien(tongTienThanhToanCod) }}</span>
            </div>
          </div>
        </div>

        <div class="min-h-0 flex-1 space-y-3 overflow-hidden px-5 py-4">
          <div class="grid grid-cols-2 gap-2 rounded-2xl bg-slate-100 p-1">
            <button type="button"
              class="h-10 rounded-xl text-sm font-bold outline-none transition focus-visible:ring-4 focus-visible:ring-rose-100"
              :class="formThanhToanCod.hinhThucThanhToan === 2 ? 'bg-[#B82220] text-white shadow-sm' : 'text-slate-500 hover:bg-white/70'"
              @click="formThanhToanCod.hinhThucThanhToan = 2">
              Chuyển khoản
            </button>
            <button type="button"
              class="h-10 rounded-xl text-sm font-bold outline-none transition focus-visible:ring-4 focus-visible:ring-rose-100"
              :class="formThanhToanCod.hinhThucThanhToan === 1 ? 'bg-[#B82220] text-white shadow-sm' : 'text-slate-500 hover:bg-white/70'"
              @click="formThanhToanCod.hinhThucThanhToan = 1">
              Tiền mặt
            </button>
          </div>

          <div class="grid gap-4"
            :class="formThanhToanCod.hinhThucThanhToan === 2 ? 'lg:grid-cols-[1.05fr_0.95fr]' : 'place-items-center'">
            <div v-if="formThanhToanCod.hinhThucThanhToan === 2"
              class="rounded-2xl border border-slate-200 bg-white p-4 text-center shadow-sm">
              <div class="space-y-1 text-sm">
                <p><span class="font-bold text-slate-800">Ngân hàng:</span> <span class="text-slate-600">MB Bank</span>
                </p>
                <p><span class="font-bold text-slate-800">Số tài khoản:</span> <span
                    class="text-slate-600">0876524519</span></p>
                <p><span class="font-bold text-slate-800">Nội dung:</span> <span class="text-[#B82220]">{{
                    noiDungChuyenKhoanCod }}</span></p>
              </div>
              <div class="mx-auto mt-3 flex w-fit rounded-2xl border border-slate-200 bg-white p-3 shadow-inner">
                <img :src="qrThanhToanCodUrl" alt="QR thanh toán COD" class="h-52 w-52 object-contain" />
              </div>
              <p class="mt-2 text-xs text-slate-400">Quét mã để thanh toán đúng số tiền.</p>
            </div>

            <div class="w-full space-y-3" :class="formThanhToanCod.hinhThucThanhToan === 1 ? 'max-w-[420px]' : ''">
              <label v-if="formThanhToanCod.hinhThucThanhToan === 1" class="block space-y-2">
                <span class="text-sm font-bold text-slate-600">Tiền khách đưa</span>
                <input v-model="formThanhToanCod.tienKhachDua" type="number" min="0" inputmode="numeric"
                  placeholder="Nhập số tiền..."
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-800 outline-none transition focus:border-rose-300 focus:bg-white focus:ring-4 focus:ring-rose-100" />
              </label>

              <div class="overflow-hidden rounded-2xl border border-slate-200">
                <table class="w-full text-sm">
                  <thead class="bg-slate-50 text-xs font-bold text-slate-500">
                    <tr>
                      <th class="px-3 py-2.5 text-left">STT</th>
                      <th class="px-3 py-2.5 text-left">Phương thức</th>
                      <th class="px-3 py-2.5 text-right">Số tiền</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="border-t border-slate-100">
                      <td class="px-3 py-3 font-semibold text-slate-500">1</td>
                      <td class="px-3 py-3 font-semibold text-slate-700">{{ formThanhToanCod.hinhThucThanhToan === 2 ?
                        'Chuyển khoản' : 'Tiền mặt' }}</td>
                      <td class="px-3 py-3 text-right font-bold text-[#B82220]">{{ dinhDangTien(tongTienThanhToanCod) }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <label class="block space-y-2">
                <span class="text-sm font-bold text-slate-600">Ghi chú</span>
                <textarea v-model="formThanhToanCod.ghiChu" rows="2"
                  class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white focus:ring-4 focus:ring-rose-100"></textarea>
              </label>

              <div class="flex items-center justify-between rounded-2xl bg-rose-50 px-4 py-3">
                <span class="text-sm font-bold text-slate-600">Tiền thiếu</span>
                <span class="text-lg font-extrabold text-[#B82220]">{{ dinhDangTien(tienThieuThanhToanCod) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="shrink-0 border-t border-slate-100 bg-slate-50/90 px-5 py-4 sm:px-6">
          <div class="flex flex-col-reverse gap-3 sm:flex-row sm:items-center sm:justify-end">
            <button type="button"
              class="h-11 rounded-2xl border border-slate-200 bg-white px-5 text-sm font-bold text-slate-600 outline-none transition hover:bg-slate-100 focus-visible:ring-4 focus-visible:ring-slate-200"
              @click="hienModalThanhToanCod = false">
              Hủy
            </button>
            <button type="button"
              class="inline-flex h-11 items-center justify-center gap-2 rounded-2xl bg-[#B82220] px-5 text-sm font-bold text-white shadow-[0_12px_28px_rgba(184,34,32,0.24)] outline-none transition hover:bg-[#991b1b] focus-visible:ring-4 focus-visible:ring-rose-100 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="dangXacNhanThanhToanCod" @click="handleXacNhanThanhToanCod">
              <CheckCircle2 class="h-4 w-4" />
              {{ dangXacNhanThanhToanCod ? 'Đang xác nhận...' : 'Xác nhận thanh toán' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalHoanTien"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm">
      <div class="w-full max-w-[560px] overflow-hidden rounded-[24px] bg-white shadow-2xl">
        <div class="bg-amber-50 px-6 py-5">
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-start gap-3">
              <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-amber-500 text-white">
                <Banknote class="h-5 w-5" />
              </div>
              <div>
                <h3 class="text-lg font-extrabold uppercase tracking-wide text-slate-800">Xác nhận hoàn tiền</h3>
                <p class="mt-1 text-sm text-slate-500">{{ hoaDon.maHoaDon }} - {{ hoaDon.tenKhachHang }}</p>
              </div>
            </div>
            <button type="button"
              class="flex h-9 w-9 items-center justify-center rounded-full bg-white text-slate-400 shadow-sm transition hover:text-slate-700"
              @click="hienModalHoanTien = false">
              <X class="h-5 w-5" />
            </button>
          </div>
        </div>

        <div class="space-y-4 px-6 py-5">
          <div class="flex items-center justify-between rounded-2xl border border-amber-100 bg-amber-50/60 px-4 py-3">
            <span class="text-sm font-bold text-slate-600">Số tiền cần hoàn</span>
            <span class="text-lg font-extrabold text-amber-600">{{ dinhDangTien(tongTienHoan) }}</span>
          </div>

          <label class="block space-y-2">
            <span class="text-sm font-bold text-slate-600">Phương thức hoàn</span>
            <select v-model.number="formHoanTien.hinhThucHoanTien"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100">
              <option :value="2">Chuyển khoản</option>
              <option :value="1">Tiền mặt</option>
              <option :value="3">Hoàn qua cổng thanh toán</option>
            </select>
          </label>

          <!-- Customer Bank Account Selection & VietQR Code -->
          <div v-if="formHoanTien.hinhThucHoanTien === 2" class="space-y-4">
            <div class="space-y-2">
              <span class="text-sm font-bold text-slate-600">Tài khoản ngân hàng nhận tiền của khách</span>
              <div v-if="dangTaiNganHangKhach" class="text-xs text-slate-400">Đang tải danh sách tài khoản...</div>
              <select v-else-if="dsTaiKhoanNganHangKhach.length > 0" v-model="taiKhoanNganHangChon"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100">
                <option v-for="tk in dsTaiKhoanNganHangKhach" :key="tk.id" :value="tk">
                  {{ tk.tenNganHang }} - {{ tk.soTaiKhoan }} ({{ tk.tenChuTaiKhoan }}) {{ tk.laMacDinh ? '[Mặc định]' : '' }}
                </option>
              </select>
              <div v-else class="rounded-2xl border border-rose-100 bg-rose-50/50 px-4 py-3 text-xs font-semibold text-rose-700">
                Khách hàng chưa liên kết tài khoản ngân hàng nào.
              </div>
            </div>

            <!-- VietQR Code Image -->
            <div v-if="taiKhoanNganHangChon" class="flex flex-col items-center justify-center border border-slate-100 rounded-3xl p-5 bg-slate-50/80 gap-3">
              <span class="text-xs font-bold text-slate-500 uppercase tracking-wider">Quét mã VietQR để chuyển tiền</span>
              <div class="h-52 w-52 overflow-hidden rounded-2xl border border-slate-200 bg-white p-2.5 flex items-center justify-center shadow-sm">
                <img :src="qrHoanTienUrl" alt="VietQR Hoàn Tiền" class="h-full w-full object-contain" />
              </div>
              <div class="text-center space-y-0.5">
                <p class="text-xs font-bold text-slate-700">Chủ TK: <span class="uppercase text-[#B82220]">{{ taiKhoanNganHangChon.tenChuTaiKhoan }}</span></p>
                <p class="text-xs font-semibold text-slate-500">STK: {{ taiKhoanNganHangChon.soTaiKhoan }} ({{ taiKhoanNganHangChon.tenNganHang }})</p>
              </div>
            </div>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="block space-y-2">
              <span class="text-sm font-bold text-slate-600">Số tiền hoàn</span>
              <input v-model="formHoanTien.soTienHoan" type="number" min="0" inputmode="numeric"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-semibold text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100" />
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-bold text-slate-600">Mã giao dịch hoàn</span>
              <input v-model="formHoanTien.maGiaoDichHoan" type="text" placeholder="VD: RF20260524..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100" />
            </label>
          </div>

          <label class="block space-y-2">
            <span class="text-sm font-bold text-slate-600">Ghi chú</span>
            <textarea v-model="formHoanTien.ghiChu" rows="3"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-amber-300 focus:bg-white focus:ring-4 focus:ring-amber-100"></textarea>
          </label>
        </div>

        <div
          class="flex flex-col-reverse gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 sm:flex-row sm:justify-end">
          <button type="button"
            class="h-11 rounded-2xl border border-slate-200 bg-white px-5 text-sm font-bold text-slate-600 transition hover:bg-slate-100"
            @click="hienModalHoanTien = false">
            Hủy
          </button>
          <button type="button"
            class="inline-flex h-11 items-center justify-center gap-2 rounded-2xl bg-amber-500 px-5 text-sm font-bold text-white shadow-[0_12px_28px_rgba(245,158,11,0.24)] transition hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="dangXacNhanHoanTien || (formHoanTien.hinhThucHoanTien === 2 && !taiKhoanNganHangChon)"
            @click="handleXacNhanHoanTien">
            <CheckCircle2 class="h-4 w-4" />
            {{ dangXacNhanHoanTien ? 'Đang xác nhận...' : 'Xác nhận hoàn tiền' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="hienModalXacNhan"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-md overflow-hidden rounded-[28px] bg-white shadow-2xl">
        <div class="p-7">
          <h3 class="text-xl font-bold text-slate-800">Xác Nhận Cập Nhật Trạng Thái</h3>
          <p class="mt-2 text-sm text-slate-400">
            Cập Nhật Đơn Hàng Sang Trạng Thái
            <span class="font-semibold text-slate-700">{{ trangThaiMoiXacNhan }}</span>
          </p>
          <div class="mt-6">
            <label class="mb-2 block text-xs font-semibold uppercase tracking-wide text-slate-400">Ghi Chú</label>
            <textarea v-model="ghiChuXacNhan" rows="4" placeholder="Nhập Ghi Chú Cho Bước Này..."
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"></textarea>
          </div>
          <div class="mt-7 flex gap-3">
            <Button @click="hienModalXacNhan = false" variant="soft" class="flex-1">
              Đóng
            </Button>
            <Button @click="handleXacNhanTrangThai" :disabled="dangCapNhat" variant="primary" class="flex-1">
              {{ dangCapNhat ? "Đang Lưu..." : "Xác Nhận" }}
            </Button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalXacNhanHuy"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-md overflow-hidden rounded-[24px] bg-white shadow-2xl">
        <div class="p-6">
          <div class="flex items-start gap-4">
            <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-rose-50 text-[#B82220]">
              <TriangleAlert class="h-6 w-6" />
            </div>
            <div>
              <h3 class="text-[19px] font-bold text-slate-800">Xác Nhận Hủy Đơn Hàng?</h3>
              <p class="mt-2 text-sm leading-6 text-slate-500">
                Bạn có chắc chắn muốn xác nhận hủy đơn hàng này không? Sau khi xác nhận, đơn hàng sẽ chuyển sang trạng
                thái
                đã hủy.
              </p>
            </div>
          </div>

          <div class="mt-7 flex gap-3">
            <Button @click="hienModalXacNhanHuy = false" :disabled="dangCapNhat" variant="soft" class="flex-1">
              Quay Lại
            </Button>
            <Button @click="handleXacNhanHuyDon" :disabled="dangCapNhat" variant="primary" class="flex-1">
              {{ dangCapNhat ? "Đang Hủy..." : "Xác Nhận Hủy" }}
            </Button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalLichSu"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-2xl overflow-hidden rounded-[24px] bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
          <div class="flex items-center gap-3">
            <History class="h-5 w-5 text-slate-500" />
            <h3 class="text-[17px] font-bold text-slate-800">Lịch sử thao tác</h3>
          </div>
          <button @click="hienModalLichSu = false" class="text-slate-400 transition hover:text-slate-600">
            <CircleX class="h-5 w-5" />
          </button>
        </div>

        <div class="max-h-[70vh] overflow-y-auto px-6 py-8">
          <div v-if="!lichSuRutGon.length" class="py-10 text-center text-sm text-slate-400">Chưa có lịch sử thao tác.
          </div>
          <div v-else class="relative pl-8">
            <!-- Trục timeline đỏ -->
            <div class="absolute bottom-0 left-[3.5px] top-0 w-[1.5px] bg-[#B82220]/20"></div>

            <div class="space-y-6">
              <div v-for="log in lichSuRutGon" :key="log.id" class="relative">
                <!-- Chấm tròn đỏ trên trục -->
                <div
                  class="absolute -left-[32px] top-4 h-2 w-2 rounded-full border-2 border-white bg-[#B82220] shadow-[0_0_0_2px_rgba(184,34,32,0.15)]">
                </div>

                <div
                  class="rounded-2xl border border-slate-50 bg-slate-50/50 p-4 transition-colors hover:bg-slate-100/50">
                  <div class="text-[12px] text-slate-400 font-medium">
                    {{ dinhDangGio(log.ngayTao) }} {{ dinhDangNgay(log.ngayTao) }}
                  </div>
                  <div class="mt-1 text-[13px] font-semibold text-slate-400">
                    {{ log.maNhanVien || 'Hệ thống' }} - {{ log.tenNhanVien || 'admin' }}
                  </div>
                  <p class="mt-2 text-[15px] font-bold text-slate-800">{{ log.trangThai }}</p>
                  <p v-if="log.ghiChu" class="mt-2 text-[13px] text-slate-500 italic">{{ log.ghiChu }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalSanPham"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-4xl overflow-hidden rounded-[32px] bg-white shadow-2xl">
        <div class="grid lg:grid-cols-2">
          <div class="border-r border-slate-100 p-7">
            <h3 class="text-xl font-bold text-slate-800">Tìm Sản Phẩm</h3>
            <div class="relative mt-6">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input v-model="tuKhoaSanPham" @input="timKiemSanPham" type="text" placeholder="Tên Sản Phẩm, SKU..."
                class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>
            <div class="mt-6 max-h-[420px] space-y-3 overflow-y-auto pr-2">
              <div v-if="dangTimKiem" class="py-10 text-center text-sm text-slate-400">Đang Tìm Kiếm...</div>
              <div v-else-if="!ketQuaTimKiem.length" class="py-10 text-center text-sm text-slate-400">Nhập Từ Khóa Để
                Tìm
                Sản Phẩm</div>
              <div v-for="sp in ketQuaTimKiem" :key="sp.chiTietId" @click="themSanPham(sp)"
                class="cursor-pointer rounded-2xl border border-slate-100 p-4 transition hover:border-rose-200 hover:bg-rose-50/40">
                <div class="flex items-center justify-between gap-4">
                  <div>
                    <p class="font-semibold text-slate-800">{{ sp.tenSanPham }}</p>
                    <p class="mt-1 text-xs text-slate-400">{{ sp.mauSac }} · {{ sp.kichCo }} · Tồn: {{ sp.soLuongTon }}
                    </p>
                  </div>
                  <p class="font-semibold text-[#B82220]">{{ dinhDangTien(sp.giaBan) }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-slate-50/60 p-7">
            <h3 class="text-xl font-bold text-slate-800">Chỉnh Sửa Đơn Hàng</h3>
            <div class="mt-6 max-h-[420px] space-y-3 overflow-y-auto pr-2">
              <div v-if="!danhSachSanPhamUpdate.length" class="py-10 text-center text-sm text-slate-400">Chưa Có Sản
                Phẩm
                Nào</div>
              <div v-for="item in danhSachSanPhamUpdate" :key="item.chiTietId"
                class="flex items-center gap-4 rounded-2xl bg-white p-4 shadow-sm">
                <div class="flex-1">
                  <p class="font-semibold text-slate-800">{{ item.tenSanPham }}</p>
                  <p class="mt-1 text-xs text-slate-400">{{ item.maBienThe }}</p>
                </div>
                <input v-model.number="item.soLuong" type="number" min="1"
                  class="h-10 w-16 rounded-xl border border-slate-200 bg-slate-50 text-center text-sm font-semibold outline-none focus:border-rose-300" />
                <button @click="removeSanPham(item.chiTietId)"
                  class="rounded-xl bg-[#B82220]/10 p-2 text-[#B82220] transition hover:bg-[#B82220]/20">
                  <Trash2 class="h-4 w-4" />
                </button>
              </div>
            </div>
            <div class="mt-6 border-t border-slate-200 pt-5">
              <div class="flex items-center justify-between text-sm font-semibold">
                <span class="text-slate-700">Tổng Tiền Hàng</span>
                <span class="text-[#B82220]">{{dinhDangTien(danhSachSanPhamUpdate.reduce((t, i) => t + i.giaBan *
                  i.soLuong, 0)) }}</span>
              </div>
              <div class="mt-5 flex gap-3">
                <Button @click="hienModalSanPham = false" variant="soft" class="flex-1">
                  Đóng
                </Button>
                <Button @click="handleSaveSanPham" :disabled="dangCapNhat || donDaHoanThanh"
                  class="flex-1 bg-amber-500 hover:bg-amber-600 text-white border-transparent">
                  {{ dangCapNhat ? "Đang Lưu..." : "Lưu Thay Đổi" }}
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalThongTin"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-[600px] overflow-hidden rounded-[16px] bg-white shadow-2xl">
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
          <h3 class="text-[17px] font-semibold text-slate-800">Cập nhật thông tin đơn hàng</h3>
          <button @click="hienModalThongTin = false" class="text-slate-400 transition hover:text-slate-600">
            <CircleX class="h-5 w-5" />
          </button>
        </div>

        <div class="flex gap-6 border-b border-slate-100 px-6 pt-3 text-[14px]">
          <button
            :class="tabHienTai === 'donHang' ? 'border-b-2 border-slate-700 font-semibold text-slate-800' : 'text-slate-500 hover:text-slate-700'"
            class="pb-3 transition-colors" @click="tabHienTai = 'donHang'">
            Thông tin đơn hàng
          </button>
          <button
            :class="tabHienTai === 'khachHang' ? 'border-b-2 border-blue-500 font-semibold text-blue-500' : 'text-blue-500 hover:text-blue-600'"
            class="pb-3 transition-colors" @click="tabHienTai = 'khachHang'">
            Thông tin khách hàng
          </button>
        </div>

        <div class="p-6">
          <div v-if="tabHienTai === 'donHang'" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Mã đơn hàng</label>
                <input type="text" readonly :value="hoaDon.maHoaDon"
                  class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none" />
              </div>
              <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Ngày tạo</label>
                <input type="text" readonly :value="dinhDangGio(hoaDon.ngayTao) + ' ' + dinhDangNgay(hoaDon.ngayTao)"
                  class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none" />
              </div>
            </div>
            <div>
              <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Trạng thái</label>
              <select v-model="formThongTin.trangThai"
                class="w-full rounded-[8px] border border-blue-400 px-3 py-2.5 text-[14px] text-slate-800 outline-none ring-1 ring-blue-100 transition focus:border-blue-500 focus:ring-blue-300 disabled:cursor-not-allowed disabled:bg-slate-100">
                <template v-for="(st, index) in danhSachTrangThaiHienThi" :key="st.key">
                  <option v-if="hienThiOptionTrangThai(index, st.key)" :value="st.key"
                    :disabled="isOptionDisabled(st.key)">
                    {{ st.label }}
                  </option>
                </template>
              </select>
            </div>
          </div>

          <div v-if="tabHienTai === 'khachHang'" class="space-y-4">
            <div>
              <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Tên người nhận</label>
              <input type="text" v-model="formThongTin.tenKhachHang"
                :readonly="!coTheSuaThongTinGiaoHang"
                :class="coTheSuaThongTinGiaoHang ? 'bg-white focus:border-rose-300' : 'cursor-not-allowed bg-slate-100 text-slate-500'"
                class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition" />
            </div>
            <div>
              <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Số điện thoại</label>
              <input type="text" v-model="formThongTin.soDienThoai"
                :readonly="!coTheSuaThongTinGiaoHang"
                :class="coTheSuaThongTinGiaoHang ? 'bg-white focus:border-rose-300' : 'cursor-not-allowed bg-slate-100 text-slate-500'"
                class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition" />
            </div>
            <div>
              <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Email</label>
              <input type="text" v-model="formThongTin.email" readonly
                class="w-full cursor-not-allowed rounded-[8px] border border-slate-200 bg-slate-100 px-3 py-2.5 text-[14px] text-slate-500 outline-none" />
            </div>
            <div>
              <div class="mb-1.5 flex items-center justify-between gap-3">
                <label class="block text-[13px] font-medium text-slate-600">Địa chỉ giao hàng</label>
                <button
                  v-if="coTheSuaThongTinGiaoHang"
                  type="button"
                  class="inline-flex items-center gap-1.5 text-xs font-bold text-[#B82220] hover:text-[#9f1d1b]"
                  @click="moModalSuaDiaChi"
                >
                  <Pencil class="h-3.5 w-3.5" />
                  Sửa địa chỉ
                </button>
              </div>
              <input type="text" v-model="formThongTin.diaChi"
                :readonly="!coTheSuaThongTinGiaoHang"
                :class="coTheSuaThongTinGiaoHang ? 'bg-white focus:border-rose-300' : 'cursor-not-allowed bg-slate-100 text-slate-500'"
                class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition" />
            </div>
            <p v-if="!coTheSuaThongTinGiaoHang" class="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-700">
              Thông tin giao hàng đã khóa vì đơn đã bắt đầu giao.
            </p>
          </div>

        </div>

        <div class="flex justify-end gap-3 border-t border-slate-100 px-6 py-4">
          <Button @click="hienModalThongTin = false"
            class="bg-slate-500 hover:bg-slate-600 text-white border-transparent rounded-full">
            Hủy
          </Button>
          <Button @click="handleLuuThongTin" :disabled="dangCapNhat || donDaHoanThanh"
            class="bg-[#B82220] hover:bg-[#9f1d1b] text-white border-transparent rounded-full">
            {{ dangCapNhat ? 'Đang Lưu...' : 'Lưu' }}
          </Button>
        </div>
      </div>
    </div>

    <ChinhSuaGiaoHangModal
      v-model="hienModalGiaoHang"
      title="Chỉnh sửa thông tin nhận hàng"
      :initial-data="{
        tenNguoiNhan: formThongTin.tenKhachHang,
        sdtNguoiNhan: formThongTin.soDienThoai,
        diaChiGiaoHang: formThongTin.diaChi,
      }"
      :saved-addresses="diaChiDaLuu"
      :saving="dangLuuGiaoHang"
      @save="handleLuuGiaoHang"
    />
  </div>
</template>

<style scoped>
.invoice-flat :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
.price-range-input {
  display: block;
  pointer-events: none;
  position: absolute;
  left: 0;
  top: 50%;
  height: 18px;
  width: 100%;
  margin: 0;
  transform: translateY(-50%);
  appearance: none;
  background: transparent;
}

.price-range-input::-webkit-slider-thumb {
  pointer-events: auto;
  height: 18px;
  width: 18px;
  margin-top: -7px;
  appearance: none;
  border: 3px solid #ffffff;
  border-radius: 9999px;
  background: #ffffff;
  box-shadow: 0 0 0 1px #d9dde6, 0 4px 12px rgba(184, 34, 32, 0.2);
  cursor: pointer;
}

.price-range-input::-moz-range-thumb {
  pointer-events: auto;
  height: 18px;
  width: 18px;
  border: 3px solid #ffffff;
  border-radius: 9999px;
  background: #ffffff;
  box-shadow: 0 0 0 1px #d9dde6, 0 4px 12px rgba(184, 34, 32, 0.2);
  cursor: pointer;
}

.price-range-input::-webkit-slider-runnable-track {
  height: 4px;
  background: transparent;
}

.price-range-input::-moz-range-track {
  height: 4px;
  background: transparent;
}
</style>
