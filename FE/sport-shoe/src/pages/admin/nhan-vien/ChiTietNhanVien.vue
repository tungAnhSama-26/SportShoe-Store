<script setup lang="ts">
import { useChiTietNhanVien } from "./useChiTietNhanVien";
const { nextTick, onMounted, onUnmounted, ref, watch, useRoute, useRouter, ArrowLeft, Camera, Save, ScanLine, X, dangQuet, loiCamera, videoRef, dangQuetFile, thongBaoQrOk, zxingReader, daXuLyQr, batDauQuet, xuLyKetQuaQr, isVneIdSecureQr, formatNgaySinh, syncCurrentAdminCccd, dungQuet, route, router, id, laMoi, dangTai, dangLuu, dangUpload, loiTrang, thongBao, nhanVien, fileInputAvatar, matKhauMoi, showDoiMatKhau, loiForm, form, dsVaiTro, dsQuanHuyenTheoTinh, dsTinhThanh, dsXaPhuongTheoQuan, dsQuanHuyen, dsXaPhuong, layLabel, gopDiaChi, apDungMaDiaChiDaQuet, taiChiTiet, luu, doiMatKhau, doiTrangThai, xoaNhanVienHienTai, xuLyUploadAnh } = useChiTietNhanVien();
</script>

<template>
  <div class="space-y-6">
    <section class="flex items-center gap-4">
      <div class="flex items-center gap-4">
        <button
          type="button"
          @click="router.push({ name: 'admin-nhan-vien' })"
          class="flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-700 transition hover:bg-slate-200"
        >
          <ArrowLeft class="h-5 w-5" />
        </button>
        <h1 class="text-[28px] font-bold tracking-tight text-slate-900">
          {{ laMoi ? "Thêm nhân viên mới" : "Chi tiết nhân viên" }}
        </h1>
      </div>

    </section>

    <Card v-if="dangTai" class="py-16 text-center text-sm text-slate-400">
      Đang tải thông tin nhân viên...
    </Card>

    <template v-else>
      <!-- Toast quét QR thành công - hiển thị cố định trên cùng -->
      <Teleport to="body">
        <Transition enter-active-class="transition duration-300 ease-out" enter-from-class="opacity-0 -translate-y-3" enter-to-class="opacity-100 translate-y-0" leave-active-class="transition duration-200 ease-in" leave-from-class="opacity-100 translate-y-0" leave-to-class="opacity-0 -translate-y-3">
          <div v-if="thongBaoQrOk" class="fixed top-5 left-1/2 z-[9999] -translate-x-1/2 flex items-center gap-2.5 rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-3 text-[13px] font-medium text-emerald-700 shadow-lg">
            {{ thongBaoQrOk }}
          </div>
        </Transition>
      </Teleport>
      <div
        v-if="thongBao"
        class="rounded-[20px] border border-emerald-100 bg-emerald-50 px-5 py-3 text-sm font-medium text-emerald-700"
      >
        {{ thongBao }}
      </div>
      <div
        v-if="loiTrang"
        class="rounded-[20px] border border-rose-100 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-700"
      >
        {{ loiTrang }}
      </div>

      <div class="grid gap-6 xl:grid-cols-[380px_minmax(0,1fr)]">
        <Card>
          <div class="flex items-center justify-between">
            <h2 class="text-base font-bold text-slate-800">Thông tin nhân viên</h2>
          </div>
          <div class="mt-7 h-px bg-slate-200"></div>

          <div class="pt-10">
            <div class="flex justify-center">
              <button
                type="button"
                @click="fileInputAvatar?.click()"
                class="relative flex h-[194px] w-[194px] items-center justify-center overflow-hidden rounded-full border-2 border-dashed border-slate-200 bg-slate-50 text-center transition hover:border-slate-300 hover:bg-slate-100"
              >
                <img
                  v-if="form.hinhAnh"
                  :src="form.hinhAnh"
                  alt="Ảnh nhân viên"
                  class="h-full w-full object-cover"
                />
                <span v-else class="text-[18px] font-medium text-slate-400">Chọn ảnh</span>
                <span
                  v-if="dangUpload"
                  class="absolute inset-0 flex items-center justify-center bg-white/80 text-sm font-semibold text-slate-600"
                >
                  Đang tải...
                </span>
              </button>
              <input
                ref="fileInputAvatar"
                type="file"
                accept="image/*,.jpg,.jpeg,.png,.gif,.webp"
                class="hidden"
                @change="xuLyUploadAnh"
              />
            </div>

            <label class="mt-8 block space-y-1.5">
              <span class="text-[13px] font-semibold text-slate-500">Họ và tên <span class="text-rose-500">*</span></span>
              <input
                v-model="form.hoTen"
                type="text"
                placeholder="Nhập họ và tên"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:bg-white',
                  loiForm.hoTen ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-primary/50 focus:ring-4 focus:ring-primary/10',
                ]"
              />
              <p v-if="loiForm.hoTen" class="text-xs text-rose-500">{{ loiForm.hoTen }}</p>
            </label>
          </div>
        </Card>

        <Card>
          <h2 class="text-base font-bold text-slate-800">Thông tin chi tiết</h2>
          <div class="mt-7 h-px bg-slate-200"></div>

          <!-- QR Scanner Modal -->
          <Teleport to="body">
            <div v-show="dangQuet" class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm">
              <div class="relative w-full max-w-lg rounded-[28px] bg-white p-6 shadow-2xl mx-4">

                <!-- Header -->
                <div class="mb-4 flex items-center justify-between">
                  <div>
                    <h3 class="text-[18px] font-bold text-slate-900">Quét QR</h3>
                  </div>
                  <button type="button" @click="dungQuet"
                    class="flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
                    <X class="h-4 w-4" />
                  </button>
                </div>

                <!-- Camera -->
                <div>
                  <div class="relative overflow-hidden rounded-[18px] bg-black" style="aspect-ratio:4/3">
                    <video ref="videoRef" class="h-full w-full object-cover" autoplay playsinline muted></video>
                    <!-- Khung quét overlay -->
                    <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                      <div class="relative h-48 w-64">
                        <span class="absolute top-0 left-0 h-8 w-8 border-t-[3px] border-l-[3px] border-white rounded-tl-md"></span>
                        <span class="absolute top-0 right-0 h-8 w-8 border-t-[3px] border-r-[3px] border-white rounded-tr-md"></span>
                        <span class="absolute bottom-0 left-0 h-8 w-8 border-b-[3px] border-l-[3px] border-white rounded-bl-md"></span>
                        <span class="absolute bottom-0 right-0 h-8 w-8 border-b-[3px] border-r-[3px] border-white rounded-br-md"></span>
                        <div class="scan-line"></div>
                      </div>
                    </div>
                  </div>
                  <p v-if="!loiCamera" class="mt-3 text-center text-[13px] text-slate-500">🔍 Đang tìm mã QR... Giữ camera ổn định</p>
                </div>

                <!-- Lỗi chung -->
                <div v-if="loiCamera" class="mt-4 rounded-[14px] bg-rose-50 px-4 py-3 text-[13px] font-medium text-rose-600">
                  ⚠️ {{ loiCamera }}
                </div>
              </div>
            </div>
          </Teleport>

          <!-- CCCD Field -->
          <div class="mt-6 space-y-1.5">
            <span class="text-[13px] font-semibold text-slate-500">Số CCCD</span>
            <div class="flex items-center gap-3 flex-wrap">
              <button
                type="button"
                @click="batDauQuet"
                :class="[
                  'flex h-11 items-center gap-2 rounded-2xl border px-4 text-sm font-semibold transition whitespace-nowrap',
                  loiForm.cccd ? 'border-rose-400 bg-rose-50 text-rose-600 hover:bg-rose-100' : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                ]"
              >
                <ScanLine class="h-4 w-4" />
                {{ form.cccd ? 'Quét lại CCCD' : 'Quét mã CCCD' }}
              </button>
              <span v-if="form.cccd" class="text-sm font-semibold text-slate-800">{{ form.cccd }}</span>
            </div>
            <p v-if="loiForm.cccd" class="text-xs text-rose-500">{{ loiForm.cccd }}</p>
          </div>

          <div class="mt-5 grid gap-x-5 gap-y-4 xl:grid-cols-12">
            <div class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Giới tính <span class="text-rose-500">*</span></span>
              <div class="flex h-11 items-center gap-6 px-1 text-sm text-slate-700">
                <label class="inline-flex items-center gap-2">
                  <input v-model="form.gioiTinh" type="radio" value="Nam" class="h-4 w-4 accent-primary" />
                  <span>Nam</span>
                </label>
                <label class="inline-flex items-center gap-2">
                  <input v-model="form.gioiTinh" type="radio" value="Nữ" class="h-4 w-4 accent-primary" />
                  <span>Nữ</span>
                </label>
              </div>
            </div>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Ngày sinh <span class="text-rose-500">*</span></span>
              <input
                v-model="form.ngaySinh"
                type="date"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              />
            </label>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Email <span class="text-rose-500">*</span></span>
              <input
                v-model="form.email"
                type="email"
                placeholder="Nhập email"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:bg-white',
                  loiForm.email ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-primary/50 focus:ring-4 focus:ring-primary/10',
                ]"
              />
              <p v-if="loiForm.email" class="text-xs text-rose-500">{{ loiForm.email }}</p>
            </label>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
              <select
                v-model="form.tinhThanh"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              >
                <option value="">Chọn tỉnh thành</option>
                <option v-for="item in dsTinhThanh" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Quận/Huyện <span class="text-rose-500">*</span></span>
              <select
                v-model="form.quanHuyen"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              >
                <option value="">Chọn quận huyện</option>
                <option v-for="item in dsQuanHuyen" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Xã/Phường/Thị trấn <span class="text-rose-500">*</span></span>
              <select
                v-model="form.xaPhuong"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              >
                <option value="">Chọn xã phường</option>
                <option v-for="item in dsXaPhuong" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-1.5 xl:col-span-6">
              <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
              <input
                v-model="form.sdt"
                type="tel"
                placeholder="Nhập số điện thoại"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:bg-white',
                  loiForm.sdt ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-primary/50 focus:ring-4 focus:ring-primary/10',
                ]"
              />
              <p v-if="loiForm.sdt" class="text-xs text-rose-500">{{ loiForm.sdt }}</p>
            </label>

            <label class="space-y-1.5 xl:col-span-6">
              <span class="text-[13px] font-semibold text-slate-500">Vai trò <span class="text-rose-500">*</span></span>
              <select
                v-model="form.vaiTro"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              >
                <option v-for="item in dsVaiTro" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-1.5 xl:col-span-12">
              <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
              <input
                v-model="form.diaChiCuThe"
                type="text"
                placeholder="Nhập địa chỉ cụ thể"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 placeholder:text-slate-400 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              />
            </label>
          </div>
        </Card>
      </div>

      <!-- Buttons cuối trang -->
      <Card class="flex flex-wrap items-center justify-end gap-3">
        <Button
          variant="soft"
          @click="router.push({ name: 'admin-nhan-vien' })"
          class="h-12 px-8 text-[15px] font-semibold"
        >
          Hủy
        </Button>
        <Button
          variant="primary"
          @click="luu"
          :disabled="dangLuu"
          class="h-12 px-8 text-[15px] font-semibold"
        >
          <Save class="h-4 w-4" />
          {{ dangLuu ? "Đang lưu..." : laMoi ? "Tạo nhân viên" : "Lưu thay đổi" }}
        </Button>
      </Card>

      <section v-if="!laMoi" class="grid gap-6 xl:grid-cols-2">
        <Card>
          <h3 class="text-[18px] font-bold text-slate-900">Đổi mật khẩu</h3>
          <div class="mt-6 space-y-4">
            <template v-if="showDoiMatKhau">
              <input
                v-model="matKhauMoi"
                type="password"
                placeholder="Mật khẩu mới tối thiểu 6 ký tự"
                class="h-12 w-full rounded-[18px] border border-slate-200 bg-white px-5 text-[16px] text-slate-700 outline-none focus:border-slate-300"
              />
              <div class="flex gap-3">
                <Button variant="primary" @click="doiMatKhau" :disabled="dangLuu" class="h-11 px-5">
                  Xác nhận
                </Button>
                <Button
                  variant="soft"
                  @click="showDoiMatKhau = false; matKhauMoi = ''"
                  class="h-11 px-5"
                >
                  Hủy
                </Button>
              </div>
            </template>
            <Button
              v-else
              variant="soft"
              @click="showDoiMatKhau = true"
              class="h-11 px-5"
            >
              Đổi mật khẩu
            </Button>
          </div>
        </Card>

        <Card>
          <h3 class="text-[18px] font-bold text-slate-900">Trạng thái tài khoản</h3>
          <div class="mt-6 flex flex-wrap gap-3">
            <Button
              v-if="nhanVien?.trangThai === 1"
              variant="danger"
              @click="doiTrangThai(0)"
              class="h-11 px-5"
            >
              Khóa tài khoản
            </Button>
            <Button
              v-else
              variant="success"
              @click="doiTrangThai(1)"
              class="h-11 px-5"
            >
              Kích hoạt tài khoản
            </Button>
            <Button
              variant="outline"
              @click="xoaNhanVienHienTai"
              class="h-11 px-5 text-rose-600 border-rose-200 hover:bg-rose-50"
            >
              Xóa nhân viên
            </Button>
          </div>
        </Card>
      </section>
    </template>
  </div>
</template>

<style scoped>
.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #38bdf8, #0ea5e9, #38bdf8, transparent);
  border-radius: 2px;
  animation: scanMove 2s linear infinite;
  box-shadow: 0 0 8px 2px rgba(56, 189, 248, 0.6);
}

@keyframes scanMove {
  0%   { top: 0%; opacity: 1; }
  48%  { top: 100%; opacity: 1; }
  50%  { top: 100%; opacity: 0; }
  52%  { top: 0%;   opacity: 0; }
  54%  { top: 0%;   opacity: 1; }
  100% { top: 100%; opacity: 1; }
}
</style>
