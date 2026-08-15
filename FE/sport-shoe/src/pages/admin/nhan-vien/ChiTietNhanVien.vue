<script setup>
import { ref } from "vue";
import { useChiTietNhanVien } from "./useChiTietNhanVien";
import { Lock, MapPin, User } from "lucide-vue-next";
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";
import { showSuccess, showError } from "../../../utils/alert";

const { ArrowLeft, Camera, Save, ScanLine, X, dangQuet, loiCamera, videoRef, dangQuetFile, batDauQuet, dungQuet, router, laMoi, dangTai, dangLuu, dangUpload, loiTrang, nhanVien, fileInputAvatar, matKhauMoi, showDoiMatKhau, loiForm, form, dsVaiTro, dsTinhThanh, dsXaPhuong, chonTinhThanhNhanVien, chonPhuongXaNhanVien, luu, doiMatKhau, doiTrangThai, xoaNhanVienHienTai, xuLyUploadAnh, laChinhMinh, ngaySinhToiDa, ngaySinhToiThieu, dangDoiChieuDiaChi, thongBaoAnhXaDiaChi, anhXaDiaChiThanhCong } = useChiTietNhanVien();

function taoChuCaiDaiDien(value) {
  return String(value || "NV")
    .trim()
    .split(/\s+/)
    .slice(-2)
    .map((word) => word.charAt(0).toUpperCase())
    .join("") || "NV";
}
</script>

<template>
  <div class="space-y-6">
    <section class="flex items-center gap-4 border-b border-slate-100 pb-4">
      <button
        type="button"
        @click="router.push({ name: 'admin-nhan-vien' })"
        class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-slate-100 text-slate-700 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div v-if="!laMoi" class="text-sm text-slate-500">
        Mã nhân viên: <span class="font-semibold text-slate-700">{{ nhanVien?.ma || "Chưa cập nhật" }}</span>
      </div>
    </section>

    <Card v-if="dangTai" class="py-16 text-center text-sm text-slate-400">
      Đang tải thông tin nhân viên...
    </Card>

    <template v-else>
      <div
        v-if="loiTrang"
        class="rounded-[20px] border border-rose-100 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-700"
      >
        {{ loiTrang }}
      </div>

      <div class="grid gap-6 xl:grid-cols-[340px_minmax(0,1fr)]">
        <div class="space-y-5">
          <Card class="text-center !bg-white shadow-sm">
            <div class="flex justify-center pt-2">
              <button
                type="button"
                @click="fileInputAvatar?.click()"
                class="relative flex h-32 w-32 items-center justify-center overflow-hidden rounded-full bg-slate-100 text-center text-[42px] font-light text-slate-500 transition hover:bg-slate-200"
              >
                <img
                  v-if="form.hinhAnh"
                  :src="form.hinhAnh"
                  alt="Ảnh nhân viên"
                  class="h-full w-full object-cover"
                />
                <span v-else>{{ taoChuCaiDaiDien(form.hoTen) }}</span>
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

            <h2 class="mt-5 text-base font-bold text-slate-900">{{ form.hoTen || "Nhân viên mới" }}</h2>
            <p class="mt-1 text-sm text-slate-400">{{ form.email || "Chưa cập nhật email" }}</p>
            <div v-if="!laMoi" class="mt-3">
              <span
                class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
                :class="nhanVien?.trangThai === 1 ? 'bg-emerald-50 text-emerald-600' : (nhanVien?.trangThai === 2 ? 'bg-amber-50 text-amber-600' : 'bg-rose-50 text-rose-600')"
              >
                {{ nhanVien?.trangThai === 1 ? "Hoạt động" : (nhanVien?.trangThai === 2 ? "Chờ đổi mật khẩu" : "Khóa") }}
              </span>
            </div>
            <p v-else class="mt-3 text-xs text-slate-400">(Bấm vào ảnh để chọn avatar)</p>
          </Card>



          <template v-if="!laMoi">
            <Card class="!bg-white shadow-sm">
              <h3 class="mb-3 text-sm font-bold text-slate-800">Đổi mật khẩu</h3>
              <div v-if="!showDoiMatKhau">
                <Button variant="outline" class="w-full justify-center" @click="showDoiMatKhau = true">Đổi mật khẩu</Button>
              </div>
              <div v-else class="space-y-3">
                <input
                  v-model="matKhauMoi"
                  type="password"
                  placeholder="Mật khẩu mới tối thiểu 6 ký tự"
                  class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-primary/50 focus:bg-white focus:ring-2 focus:ring-primary/10"
                />
                <div class="flex gap-2">
                  <Button variant="primary" class="flex-1 justify-center" @click="doiMatKhau" :disabled="dangLuu">Xác nhận</Button>
                  <Button variant="soft" class="flex-1 justify-center" @click="showDoiMatKhau = false; matKhauMoi = ''">Hủy</Button>
                </div>
              </div>
            </Card>

            <Card class="space-y-2 !bg-white shadow-sm">
              <h3 class="mb-3 text-sm font-bold text-slate-800">Trạng thái tài khoản</h3>
              <Button
                v-if="nhanVien?.trangThai !== 0"
                variant="soft"
                class="w-full justify-center bg-rose-50 text-rose-600 hover:bg-rose-100 disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="laChinhMinh"
                :title="laChinhMinh ? 'Bạn không thể tự khóa tài khoản của chính mình' : ''"
                @click="doiTrangThai(0)"
              >
                <Lock class="h-4 w-4" />
                Khóa tài khoản
              </Button>
              <Button
                v-else
                variant="soft"
                class="w-full justify-center bg-emerald-50 text-emerald-600 hover:bg-emerald-100"
                @click="doiTrangThai(1)"
              >
                Kích hoạt tài khoản
              </Button>
            </Card>
          </template>
        </div>

        <div class="space-y-5">
        <Card class="space-y-5 !bg-white shadow-sm">
          <div class="mb-2 flex items-center gap-3">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-violet-50 text-violet-500">
              <User class="h-5 w-5" />
            </div>
            <div>
              <h2 class="text-base font-bold text-slate-800">Thông tin cơ bản</h2>
              <p class="text-sm text-slate-400">Họ tên, email, liên hệ và tài khoản.</p>
            </div>
          </div>

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
                   {{ loiCamera }}
                </div>
              </div>
            </div>
          </Teleport>

          <div class="mb-5 rounded-2xl  border-rose-100 bg-white p-4">
            <!-- <div class="mb-3 flex items-center gap-3">
              
              
            </div> -->
            <div class="flex flex-wrap items-center gap-3">
              <button
                type="button"
                @click="batDauQuet"
                class="inline-flex h-11 items-center gap-2 rounded-2xl border border-rose-200 bg-white px-4 text-sm font-bold text-black hover:bg-rose-50 transition whitespace-nowrap shadow-sm"
              >
                <ScanLine class="h-4 w-4" />
                Quét mã CCCD
              </button>
            </div>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="space-y-2">
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

            <label class="space-y-2">
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

            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
              <input
                v-model="form.sdt"
                type="tel"
                maxlength="10"
                placeholder="Nhập số điện thoại"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:bg-white',
                  loiForm.sdt ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-primary/50 focus:ring-4 focus:ring-primary/10',
                ]"
                @input="form.sdt = form.sdt.replace(/\D/g, '')"
              />
              <p v-if="loiForm.sdt" class="text-xs text-rose-500">{{ loiForm.sdt }}</p>
            </label>

            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Vai trò <span class="text-rose-500">*</span></span>
              <select
                v-model="form.vaiTro"
                :disabled="laChinhMinh"
                :title="laChinhMinh ? 'Bạn không thể tự đổi vai trò của chính mình' : ''"
                :class="[
                  'h-11 w-full rounded-2xl border px-4 text-sm outline-none transition duration-200',
                  laChinhMinh ? 'bg-slate-100 text-slate-500 cursor-not-allowed border-slate-200' : 'border-slate-200 bg-slate-50 text-slate-700 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10'
                ]"
              >
                <option v-for="item in dsVaiTro" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Giới tính <span class="text-rose-500">*</span></span>
              <select
                v-model="form.gioiTinh"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              >
                <option value="">-- Chọn giới tính --</option>
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
                <option value="Khác">Khác</option>
              </select>
            </label>

            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Ngày sinh <span class="text-rose-500">*</span></span>
              <input
                v-model="form.ngaySinh"
                type="date"
                :max="ngaySinhToiDa"
                :min="ngaySinhToiThieu"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:bg-white',
                  loiForm.ngaySinh ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-primary/50 focus:ring-4 focus:ring-primary/10',
                ]"
              />
              <p v-if="loiForm.ngaySinh" class="text-xs text-rose-500">{{ loiForm.ngaySinh }}</p>
            </label>

          </div>
        </Card>

        <Card class="space-y-5 !bg-white shadow-sm">
          <div class="mb-2 flex items-center gap-3">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-sky-50 text-sky-500">
              <MapPin class="h-5 w-5" />
            </div>
            <div>
              <h2 class="text-base font-bold text-slate-800">Địa chỉ</h2>
            </div>
          </div>

        

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
              <select
                v-model="form.tinhThanhCode"
                @change="chonTinhThanhNhanVien(form.tinhThanhCode)"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              >
                <option value="">Chọn tỉnh thành</option>
                <option v-for="item in dsTinhThanh" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Xã/Phường/Thị trấn <span class="text-rose-500">*</span></span>
              <select
                v-model="form.phuongXaCode"
                @change="chonPhuongXaNhanVien(form.phuongXaCode)"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
              >
                <option value="">Chọn xã phường</option>
                <option v-for="item in dsXaPhuong" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-2 sm:col-span-2">
              <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể</span>
              <input
                v-model="form.diaChiCuThe"
                type="text"
                placeholder="Nhập địa chỉ cụ thể"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:bg-white',
                  loiForm.diaChiCuThe ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-primary/50 focus:ring-4 focus:ring-primary/10',
                ]"
              />
              <p v-if="loiForm.diaChiCuThe" class="text-xs text-rose-500">{{ loiForm.diaChiCuThe }}</p>
            </label>
          </div>
          <div class="mt-5 flex items-center gap-3 border-t border-slate-100 pt-4">
            <button
              type="button"
              @click="luu"
              :disabled="dangLuu || dangDoiChieuDiaChi"
              class="inline-flex h-11 items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-rose-500 to-red-500 px-6 text-sm font-bold text-white shadow-[0_14px_30px_rgba(239,68,68,0.28)] transition hover:-translate-y-0.5 hover:from-rose-600 hover:to-red-500 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <Save class="h-4 w-4" />
              {{ dangDoiChieuDiaChi ? "Đang chuyển địa chỉ..." : dangLuu ? "Đang lưu..." : laMoi ? "Tạo nhân viên" : "Lưu thay đổi" }}
            </button>
            <button
              type="button"
              @click="router.push({ name: 'admin-nhan-vien' })"
              class="inline-flex h-11 items-center justify-center rounded-2xl border border-rose-200 bg-white px-6 text-sm font-semibold text-rose-500 shadow-[0_10px_24px_rgba(244,63,94,0.08)] transition hover:border-rose-300 hover:bg-rose-50/70 hover:text-rose-600"
            >
              Hủy
            </button>
          </div>
        </Card>
        </div>
      </div>
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
