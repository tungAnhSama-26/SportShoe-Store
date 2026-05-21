<script setup lang="ts">
import { useChiTietHoaDon } from "./useChiTietHoaDon";
const { computed, onMounted, ref, watch, markRaw, useRoute, useRouter, ArrowLeft, Banknote, CheckCircle2, CircleCheck, CircleX, ClipboardList, ClipboardCheck, Flag, History, Hourglass, MapPin, Package, Pencil, Printer, Search, Trash2, TriangleAlert, Truck, User, X, Card, Button, capNhatSanPhamHoaDon, capNhatTrangThaiHoaDon, layChiTietHoaDon, tinhPhiVanChuyenGhn, timSanPhamTaiQuay, printInvoiceToPdf, getDisplayErrorMessage, logoGhn, route, router, hoaDon, dangTai, loiTrang, dangCapNhat, toast, toastTimer, hienModalXacNhan, hienModalLichSu, hienModalSanPham, hienModalXacNhanHuy, hienModalThongTin, tabHienTai, formThongTin, formGhn, dangTinhPhiGhn, diaChiGhnDaDo, trangThaiMoiXacNhan, ghiChuXacNhan, tuKhoaSanPham, ketQuaTimKiem, dangTimKiem, danhSachSanPhamUpdate, cacBuocCoDinh, cacBuocYeuCauHuy, cacBuocDaHuy, laDonTaiQuay, cacBuoc, dinhDangTien, dinhDangNgay, dinhDangGio, vietHoaChuCaiDau, buocHienTai, donDaHoanThanh, donYeuCauHuy, donDaHuy, donDaKetThuc, toastClass, toastIconClass, toastAccentClass, ToastIcon, hienThiThongBao, thongBaoDonDaHoanThanh, moModalThongTin, tongTienHang, tongKhachCanTra, thanhToanGanNhat, lichSuRutGon, thongTinBuoc, cacBuocHienThi, lopVongTrangThai, lopTenTrangThai, taiChiTiet, openModalXacNhan, handleXacNhanTrangThai, handleXuLyYeuCauHuy, moModalXacNhanHuy, handleXacNhanHuyDon, timKiemSanPham, themSanPham, removeSanPham, handleSaveSanPham, danhSachTrangThaiHienThi, indexTrangThaiHienTai, isOptionDisabled, hienThiOptionTrangThai, handleLuuThongTin, handleTinhPhiGhn, handlePrint } = useChiTietHoaDon();
</script>

<template>
  <div class="space-y-4 pb-10">
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="translate-y-3 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-3 opacity-0"
    >
      <div
        v-if="toast.hienThi"
        class="fixed right-5 top-5 z-[70] w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-2xl border bg-white shadow-[0_18px_60px_rgba(15,23,42,0.18)]"
        :class="toastClass"
      >
        <div class="flex gap-3 p-4">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full" :class="toastIconClass">
            <component :is="ToastIcon" class="h-5 w-5" />
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-bold text-slate-800">{{ toast.tieuDe }}</p>
            <p v-if="toast.noiDung" class="mt-1 text-sm leading-5 text-slate-600">{{ toast.noiDung }}</p>
          </div>
          <button type="button" class="rounded-full p-1 text-slate-400 transition hover:bg-white/70 hover:text-slate-600" @click="toast.hienThi = false">
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="h-1.5 w-full" :class="toastAccentClass"></div>
      </div>
    </Transition>

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
            <span class="font-medium text-slate-700">{{ hoaDon.tenNhanVien || "Hệ Thống" }}</span>
            <span class="mx-2 text-slate-300">|</span>
            Cập Nhật Gần Nhất:
            <span class="font-medium text-slate-700">
              {{
                hoaDon.lichSuHoaDon?.[0]
                  ? `${dinhDangGio(hoaDon.lichSuHoaDon[0].ngayTao)} ${dinhDangNgay(hoaDon.lichSuHoaDon[0].ngayTao)} - ${hoaDon.lichSuHoaDon[0].tenNhanVien}`
                  : "Chưa Có"
              }}
            </span>
          </p>
        </div>
      </div>

      <Button
        variant="soft"
        @click="router.push({ name: 'admin-hoa-don' })"
        class="h-10"
      >
        <template #prefix><ArrowLeft class="h-4 w-4" /></template>
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
            <div class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
              <ClipboardList class="h-4.5 w-4.5 text-slate-500" />
              Trạng Thái Đơn Hàng
            </div>
          </template>

          <div class="relative mt-7 px-2 pt-2 flex justify-center">
            <div class="flex w-full items-start justify-around relative max-w-4xl">
              <!-- Đường ziczac động -->
              <div 
                class="absolute top-7 hidden h-[2px] bg-slate-200 md:block z-0" 
                :style="{ left: (100 / cacBuocHienThi.length / 2) + '%', right: (100 / cacBuocHienThi.length / 2) + '%' }"
              ></div>

              <div v-for="buoc in cacBuocHienThi" :key="buoc.id" class="relative z-10 flex w-32 flex-col items-center text-center">
                <div
                  class="flex h-[58px] w-[58px] items-center justify-center overflow-visible rounded-full border-[2.5px] transition-all"
                  :class="lopVongTrangThai(buoc)"
                >
                  <component :is="buoc.icon" class="h-[22px] w-[22px] block shrink-0" stroke-width="2.25" />
                </div>
                <p class="mt-3 whitespace-nowrap text-[12px] font-semibold" :class="lopTenTrangThai(buoc)">{{ buoc.ten }}</p>
                <div class="mt-1 min-h-[32px]">
                  <p v-if="buoc.thoiGian" class="text-[11px] leading-4 text-slate-400">
                    {{ dinhDangGio(buoc.thoiGian) }} {{ dinhDangNgay(buoc.thoiGian) }}
                  </p>
                  <p v-if="buoc.nhanVien" class="text-[11px] text-slate-400">{{ buoc.nhanVien }}</p>
                </div>
              </div>
            </div>
          </div>

          <div
            v-if="donYeuCauHuy"
            class="mt-5 flex items-center justify-center gap-2 rounded-lg border border-amber-200 bg-amber-50 px-4 py-2.5 text-sm font-semibold text-amber-700"
          >
            <TriangleAlert class="h-4 w-4" />
            Khách hàng yêu cầu hủy - đang chờ xác nhận
          </div>
          <div
            v-if="donDaHuy"
            class="mt-5 flex items-center justify-center gap-2 rounded-lg border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm font-semibold text-rose-700"
          >
            <CircleX class="h-4 w-4" />
            Đơn hàng đã bị hủy
          </div>

          <div class="mt-5 flex justify-end">
            <Button
              variant="primary"
              @click="hienModalLichSu = true"
            >
              <template #prefix><History class="h-4 w-4" /></template>
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
            <div class="flex items-center justify-between">
              <span class="text-slate-500">Giảm Giá</span>
              <span class="font-semibold text-emerald-500">- {{ dinhDangTien(hoaDon.giamGia) }}</span>
            </div>
            <div class="flex items-center justify-between">
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
              <h2 class="flex items-center gap-2 text-[15px] font-semibold text-slate-700">
                <History class="h-4.5 w-4.5 text-slate-500" />
                Lịch Sử Thanh Toán
              </h2>
            </template>

            <div v-if="thanhToanGanNhat" class="mt-4 space-y-3 text-sm">
              <div class="flex items-center justify-between">
                <span class="font-semibold text-slate-700">{{ thanhToanGanNhat.phuongThucThanhToan }}</span>
                <span class="font-bold text-[#B82220]">{{ dinhDangTien(thanhToanGanNhat.tongTien) }}</span>
              </div>
              <div class="text-xs text-slate-400">
                {{ dinhDangGio(thanhToanGanNhat.thoiGian) }} {{ dinhDangNgay(thanhToanGanNhat.thoiGian) }}
                <span class="mx-1">-</span>
                {{ hoaDon.tenNhanVien || "Admin" }}
              </div>
            </div>
            <div v-else class="mt-4 text-sm text-slate-400">Chưa Có Lịch Sử Thanh Toán.</div>
          </Card>

          <div
            v-if="donYeuCauHuy"
            class="rounded-[26px] border border-rose-100 bg-white px-5 py-4 shadow-sm"
          >
            <h2 class="flex items-center gap-2 text-[15px] font-semibold text-[#B82220]">
              <TriangleAlert class="h-4.5 w-4.5" />
              Khách Hàng Yêu Cầu Hủy Đơn
            </h2>
            <p class="mt-2 text-xs text-slate-500">Xem lịch sử thao tác để biết lý do yêu cầu hủy.</p>
            <div class="mt-4 grid gap-2 sm:grid-cols-2">
              <button
                type="button"
                @click="moModalXacNhanHuy"
                :disabled="dangCapNhat"
                class="inline-flex h-9 items-center justify-center gap-2 rounded-full bg-[#B82220] px-4 text-sm font-semibold text-white transition hover:bg-[#B82220]/90 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <CircleCheck class="h-4 w-4" />
                Xác Nhận Hủy
              </button>
              <button
                type="button"
                @click="handleXuLyYeuCauHuy('Chờ xác nhận')"
                :disabled="dangCapNhat"
                class="inline-flex h-9 items-center justify-center gap-2 rounded-full border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <CircleX class="h-4 w-4" />
                Từ Chối Hủy
              </button>
            </div>
          </div>

          <Button
            @click="handlePrint"
            class="w-full bg-sky-500 hover:bg-sky-600 text-white border-transparent"
          >
            <template #prefix><Printer class="h-4 w-4" /></template>
            In Hóa Đơn
          </Button>

          <Button
            v-if="!donDaKetThuc"
            @click="moModalThongTin"
            class="w-full bg-amber-500 hover:bg-amber-600 text-white border-transparent"
          >
            <template #prefix><Pencil class="h-4 w-4" /></template>
            Chỉnh Sửa Đơn Hàng
          </Button>
        </div>
      </section>

      <section class="grid gap-3">
        <Card class="px-6 py-5">
          <template #header>
            <div class="mb-4 flex items-center justify-between gap-4">
              <h2 class="flex items-center gap-2 text-base font-semibold text-slate-700">
                <Package class="h-5 w-5 text-slate-500" />
                Danh Sách Sản Phẩm ({{ hoaDon.sanPham?.length || 0 }})
              </h2>
            </div>
          </template>

          <div class="overflow-x-auto">
            <table class="w-full table-auto text-[15px]">
              <thead>
                <tr class="bg-slate-100 text-left text-xs font-bold tracking-wide text-slate-950">
                  <th class="rounded-l-2xl px-5 py-3.5">STT</th>
                  <th class="px-5 py-3.5">Ảnh</th>
                  <th class="px-5 py-3.5">Sản Phẩm</th>
                  <th class="px-5 py-3.5">Màu Sắc</th>
                  <th class="px-5 py-3.5">Số Lượng</th>
                  <th class="px-5 py-3.5">Thời Gian</th>
                  <th class="px-5 py-3.5">Đơn Giá</th>
                  <th class="rounded-r-2xl px-5 py-3.5">Tổng Tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in hoaDon.sanPham" :key="item.id" class="border-b border-slate-100 last:border-b-0">
                  <td class="px-5 py-6 font-semibold text-slate-600">{{ index + 1 }}</td>
                  <td class="px-5 py-6">
                    <img :src="item.hinhAnh || 'https://via.placeholder.com/72x72?text=Shoe'" class="h-14 w-14 rounded-xl object-cover" />
                  </td>
                  <td class="px-5 py-6">
                    <p class="text-base font-semibold text-slate-800">{{ vietHoaChuCaiDau(item.tenSanPham) }}</p>
                    <p class="mt-1 text-sm text-slate-400">{{ item.phanLoai }}</p>
                  </td>
                  <td class="px-5 py-6 text-slate-600">{{ item.mauSac }}</td>
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
        </Card>
      </section>
    </template>

    <div v-if="hienModalXacNhan" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-md overflow-hidden rounded-[28px] bg-white shadow-2xl">
        <div class="p-7">
          <h3 class="text-xl font-bold text-slate-800">Xác Nhận Cập Nhật Trạng Thái</h3>
          <p class="mt-2 text-sm text-slate-400">
            Cập Nhật Đơn Hàng Sang Trạng Thái
            <span class="font-semibold text-slate-700">{{ trangThaiMoiXacNhan }}</span>
          </p>
          <div class="mt-6">
            <label class="mb-2 block text-xs font-semibold uppercase tracking-wide text-slate-400">Ghi Chú</label>
            <textarea
              v-model="ghiChuXacNhan"
              rows="4"
              placeholder="Nhập Ghi Chú Cho Bước Này..."
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
            ></textarea>
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

    <div v-if="hienModalXacNhanHuy" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-md overflow-hidden rounded-[24px] bg-white shadow-2xl">
        <div class="p-6">
          <div class="flex items-start gap-4">
            <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-rose-50 text-[#B82220]">
              <TriangleAlert class="h-6 w-6" />
            </div>
            <div>
              <h3 class="text-[19px] font-bold text-slate-800">Xác Nhận Hủy Đơn Hàng?</h3>
              <p class="mt-2 text-sm leading-6 text-slate-500">
                Bạn có chắc chắn muốn xác nhận hủy đơn hàng này không? Sau khi xác nhận, đơn hàng sẽ chuyển sang trạng thái đã hủy.
              </p>
            </div>
          </div>

          <div class="mt-7 flex gap-3">
            <Button
              @click="hienModalXacNhanHuy = false"
              :disabled="dangCapNhat"
              variant="soft"
              class="flex-1"
            >
              Quay Lại
            </Button>
            <Button
              @click="handleXacNhanHuyDon"
              :disabled="dangCapNhat"
              variant="primary"
              class="flex-1"
            >
              {{ dangCapNhat ? "Đang Hủy..." : "Xác Nhận Hủy" }}
            </Button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalLichSu" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
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
          <div v-if="!lichSuRutGon.length" class="py-10 text-center text-sm text-slate-400">Chưa có lịch sử thao tác.</div>
          <div v-else class="relative pl-8">
            <!-- Trục timeline đỏ -->
            <div class="absolute bottom-0 left-[3.5px] top-0 w-[1.5px] bg-[#B82220]/20"></div>
            
            <div class="space-y-6">
              <div v-for="log in lichSuRutGon" :key="log.id" class="relative">
                <!-- Chấm tròn đỏ trên trục -->
                <div class="absolute -left-[32px] top-4 h-2 w-2 rounded-full border-2 border-white bg-[#B82220] shadow-[0_0_0_2px_rgba(184,34,32,0.15)]"></div>
                
                <div class="rounded-2xl border border-slate-50 bg-slate-50/50 p-4 transition-colors hover:bg-slate-100/50">
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

    <div v-if="hienModalSanPham" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
      <div class="w-full max-w-4xl overflow-hidden rounded-[32px] bg-white shadow-2xl">
        <div class="grid lg:grid-cols-2">
          <div class="border-r border-slate-100 p-7">
            <h3 class="text-xl font-bold text-slate-800">Tìm Sản Phẩm</h3>
            <div class="relative mt-6">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="tuKhoaSanPham"
                @input="timKiemSanPham"
                type="text"
                placeholder="Tên Sản Phẩm, SKU..."
                class="h-12 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
            <div class="mt-6 max-h-[420px] space-y-3 overflow-y-auto pr-2">
              <div v-if="dangTimKiem" class="py-10 text-center text-sm text-slate-400">Đang Tìm Kiếm...</div>
              <div v-else-if="!ketQuaTimKiem.length" class="py-10 text-center text-sm text-slate-400">Nhập Từ Khóa Để Tìm Sản Phẩm</div>
              <div v-for="sp in ketQuaTimKiem" :key="sp.chiTietId" @click="themSanPham(sp)" class="cursor-pointer rounded-2xl border border-slate-100 p-4 transition hover:border-rose-200 hover:bg-rose-50/40">
                <div class="flex items-center justify-between gap-4">
                  <div>
                    <p class="font-semibold text-slate-800">{{ sp.tenSanPham }}</p>
                    <p class="mt-1 text-xs text-slate-400">{{ sp.mauSac }} · {{ sp.kichCo }} · Tồn: {{ sp.soLuongTon }}</p>
                  </div>
                  <p class="font-semibold text-[#B82220]">{{ dinhDangTien(sp.giaBan) }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-slate-50/60 p-7">
            <h3 class="text-xl font-bold text-slate-800">Chỉnh Sửa Đơn Hàng</h3>
            <div class="mt-6 max-h-[420px] space-y-3 overflow-y-auto pr-2">
              <div v-if="!danhSachSanPhamUpdate.length" class="py-10 text-center text-sm text-slate-400">Chưa Có Sản Phẩm Nào</div>
              <div v-for="item in danhSachSanPhamUpdate" :key="item.chiTietId" class="flex items-center gap-4 rounded-2xl bg-white p-4 shadow-sm">
                <div class="flex-1">
                  <p class="font-semibold text-slate-800">{{ item.tenSanPham }}</p>
                  <p class="mt-1 text-xs text-slate-400">{{ item.maBienThe }}</p>
                </div>
                <input v-model.number="item.soLuong" type="number" min="1" class="h-10 w-16 rounded-xl border border-slate-200 bg-slate-50 text-center text-sm font-semibold outline-none focus:border-rose-300" />
                <button @click="removeSanPham(item.chiTietId)" class="rounded-xl bg-[#B82220]/10 p-2 text-[#B82220] transition hover:bg-[#B82220]/20">
                  <Trash2 class="h-4 w-4" />
                </button>
              </div>
            </div>
            <div class="mt-6 border-t border-slate-200 pt-5">
              <div class="flex items-center justify-between text-sm font-semibold">
                <span class="text-slate-700">Tổng Tiền Hàng</span>
                <span class="text-[#B82220]">{{ dinhDangTien(danhSachSanPhamUpdate.reduce((t, i) => t + i.giaBan * i.soLuong, 0)) }}</span>
              </div>
              <div class="mt-5 flex gap-3">
                <Button @click="hienModalSanPham = false" variant="soft" class="flex-1">
                  Đóng
                </Button>
                <Button @click="handleSaveSanPham" :disabled="dangCapNhat || donDaHoanThanh" class="flex-1 bg-amber-500 hover:bg-amber-600 text-white border-transparent">
                  {{ dangCapNhat ? "Đang Lưu..." : "Lưu Thay Đổi" }}
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hienModalThongTin" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
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
            class="pb-3 transition-colors"
            @click="tabHienTai = 'donHang'"
          >
            Thông tin đơn hàng
          </button>
          <button
            :class="tabHienTai === 'khachHang' ? 'border-b-2 border-blue-500 font-semibold text-blue-500' : 'text-blue-500 hover:text-blue-600'"
            class="pb-3 transition-colors"
            @click="tabHienTai = 'khachHang'"
          >
            Thông tin khách hàng
          </button>
          <button
            :class="tabHienTai === 'giaoHang' ? 'border-b-2 border-blue-500 font-semibold text-blue-500' : 'text-blue-500 hover:text-blue-600'"
            class="pb-3 transition-colors"
            @click="tabHienTai = 'giaoHang'"
          >
            Thông tin giao hàng
          </button>
        </div>

        <div class="p-6">
          <div v-if="tabHienTai === 'donHang'" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Mã đơn hàng</label>
                <input type="text" readonly :value="hoaDon.maHoaDon" class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none" />
              </div>
              <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Ngày tạo</label>
                <input type="text" readonly :value="dinhDangGio(hoaDon.ngayTao) + ' ' + dinhDangNgay(hoaDon.ngayTao)" class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none" />
              </div>
            </div>
            <div>
              <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Trạng thái</label>
              <select v-model="formThongTin.trangThai" class="w-full rounded-[8px] border border-blue-400 px-3 py-2.5 text-[14px] text-slate-800 outline-none ring-1 ring-blue-100 transition focus:border-blue-500 focus:ring-blue-300 disabled:cursor-not-allowed disabled:bg-slate-100">
                <template v-for="(st, index) in danhSachTrangThaiHienThi" :key="st.key">
                  <option 
                    v-if="hienThiOptionTrangThai(index, st.key)"
                    :value="st.key" 
                    :disabled="isOptionDisabled(st.key)"
                  >
                    {{ st.label }}
                  </option>
                </template>
              </select>
            </div>
          </div>

          <div v-if="tabHienTai === 'khachHang'" class="space-y-4">
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Tên khách hàng</label>
                <input type="text" v-model="formThongTin.tenKhachHang" class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition focus:border-blue-400" />
             </div>
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Số điện thoại</label>
                <input type="text" v-model="formThongTin.soDienThoai" class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition focus:border-blue-400" />
             </div>
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Email</label>
                <input type="text" v-model="formThongTin.email" class="w-full rounded-[8px] border border-slate-200 px-3 py-2.5 text-[14px] text-slate-800 outline-none transition focus:border-blue-400" />
             </div>
          </div>

          <div v-if="tabHienTai === 'giaoHang'" class="space-y-4">
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Địa chỉ cụ thể</label>
                <textarea v-model="formThongTin.diaChi" rows="2" class="w-full rounded-[8px] border border-slate-200 px-3 py-2 text-[14px] text-slate-800 outline-none transition focus:border-blue-400"></textarea>
             </div>
             <div>
                <label class="mb-1.5 block text-[13px] font-medium text-slate-600">Loại đơn</label>
                <input type="text" readonly :value="formThongTin.loaiDon" class="w-full rounded-[8px] bg-slate-100 px-3 py-2.5 text-[14px] text-slate-600 outline-none" />
             </div>
             <div class="rounded-2xl border border-rose-100 bg-rose-50/40 p-4">
                <div class="mb-3 flex items-center justify-between gap-3">
                  <div>
                    <p class="flex items-center gap-2 text-[13px] font-semibold text-slate-700">
                      Tự Tính Phí Vận Chuyển
                      <img :src="logoGhn" alt="GHN" class="h-4 w-auto object-contain" />
                    </p>
                  </div>
                  <Button
                    @click="handleTinhPhiGhn"
                    :disabled="dangTinhPhiGhn || laDonTaiQuay || donDaHoanThanh"
                    class="bg-rose-500 hover:bg-rose-600 text-white border-transparent rounded-full px-4 py-2 text-xs"
                  >
                    {{ dangTinhPhiGhn ? "Đang Tính..." : "Tự Tính Phí GHN" }}
                  </Button>
                </div>
                <div class="grid gap-3 md:grid-cols-2">
                  <div v-if="diaChiGhnDaDo" class="md:col-span-2 rounded-xl bg-white px-3 py-2 text-sm text-slate-600">
                    GHN Đã Dò: <span class="font-semibold text-slate-800">{{ diaChiGhnDaDo }}</span>
                  </div>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Loại Dịch Vụ</span>
                    <select v-model.number="formGhn.serviceTypeId" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300">
                      <option :value="2">Hàng Nhẹ</option>
                      <option :value="5">Hàng Nặng</option>
                    </select>
                  </label>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Cân Nặng (gram)</span>
                    <input v-model.number="formGhn.weight" type="number" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300" />
                  </label>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Dài (cm)</span>
                    <input v-model.number="formGhn.length" type="number" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300" />
                  </label>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Rộng (cm)</span>
                    <input v-model.number="formGhn.width" type="number" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300" />
                  </label>
                  <label class="space-y-1.5">
                    <span class="text-xs font-medium text-slate-500">Cao (cm)</span>
                    <input v-model.number="formGhn.height" type="number" class="w-full rounded-[8px] border border-slate-200 bg-white px-3 py-2 text-[14px] outline-none transition focus:border-rose-300" />
                  </label>
                  <div class="flex items-end justify-between rounded-xl bg-white px-3 py-2 text-sm">
                    <span class="text-slate-500">Phí Hiện Tại</span>
                    <span class="font-semibold text-rose-500">{{ dinhDangTien(hoaDon.phiVanChuyen) }}</span>
                  </div>
                </div>
             </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 border-t border-slate-100 px-6 py-4">
          <Button @click="hienModalThongTin = false" class="bg-slate-500 hover:bg-slate-600 text-white border-transparent rounded-full">
            Hủy
          </Button>
          <Button @click="handleLuuThongTin" :disabled="dangCapNhat || donDaHoanThanh" class="bg-emerald-600 hover:bg-emerald-700 text-white border-transparent rounded-full">
            {{ dangCapNhat ? 'Đang Lưu...' : 'Lưu' }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
