<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Save, User } from "lucide-vue-next";
import {
  capNhatNhanVien,
  doiMatKhauNhanVien,
  doiTrangThaiNhanVien,
  layChiTietNhanVien,
  taoNhanVien,
  xoaNhanVien,
  uploadFile,
} from "../../../services/nhan-vien";
import { getDisplayErrorMessage, getFieldErrors } from "../../../utils/error-message";

const route = useRoute();
const router = useRouter();

const id = route.params.id as string | undefined;
const laMoi = !id;

const dangTai = ref(false);
const dangLuu = ref(false);
const dangUpload = ref(false);
const loiTrang = ref("");
const loiForm = ref({
  hoTen: "",
  email: "",
  matKhau: "",
});
const thongBao = ref("");
const nhanVien = ref(null);

const form = ref({
  hoTen: "",
  email: "",
  matKhau: "",
  sdt: "",
  diaChi: "",
  hinhAnh: "",
  vaiTro: 2,
});

const matKhauMoi = ref("");
const showDoiMatKhau = ref(false);

async function taiChiTiet() {
  if (laMoi) return;
  dangTai.value = true;
  try {
    const data = await layChiTietNhanVien(id!);
    nhanVien.value = data;
    form.value = {
      hoTen: data.hoTen,
      email: data.email,
      matKhau: "",
      sdt: data.sdt ?? "",
      diaChi: data.diaChi ?? "",
      hinhAnh: data.hinhAnh ?? "",
      vaiTro: data.vaiTro,
    };
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải thông tin nhân viên");
  } finally {
    dangTai.value = false;
  }
}

async function luu() {
  loiForm.value = { hoTen: "", email: "", matKhau: "" };
  let hasError = false;

  if (!form.value.hoTen.trim()) {
    loiForm.value.hoTen = "Vui lòng nhập họ tên nhân viên.";
    hasError = true;
  }
  
  if (!form.value.email.trim()) {
    loiForm.value.email = "Vui lòng nhập email nhân viên.";
    hasError = true;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    loiForm.value.email = "Email nhân viên chưa đúng định dạng.";
    hasError = true;
  }
  
  if (laMoi && !form.value.matKhau.trim()) {
    loiForm.value.matKhau = "Vui lòng nhập mật khẩu cho nhân viên mới.";
    hasError = true;
  } else if (laMoi && form.value.matKhau.length < 6) {
    loiForm.value.matKhau = "Mật khẩu nhân viên phải có ít nhất 6 ký tự.";
    hasError = true;
  }

  if (hasError) return;

  dangLuu.value = true;
  loiTrang.value = "";
  thongBao.value = "";
  try {
    if (laMoi) {
      await taoNhanVien({
        hoTen: form.value.hoTen,
        email: form.value.email,
        matKhau: form.value.matKhau,
        sdt: form.value.sdt || undefined,
        diaChi: form.value.diaChi || undefined,
        hinhAnh: form.value.hinhAnh || undefined,
        vaiTro: form.value.vaiTro,
      });
      router.push({ name: "admin-nhan-vien" });
    } else {
      const updated = await capNhatNhanVien(id!, {
        hoTen: form.value.hoTen,
        email: form.value.email,
        sdt: form.value.sdt || undefined,
        diaChi: form.value.diaChi || undefined,
        hinhAnh: form.value.hinhAnh || undefined,
        vaiTro: form.value.vaiTro,
      });
      nhanVien.value = updated;
      thongBao.value = "Đã lưu thay đổi thành công!";
      setTimeout(() => (thongBao.value = ""), 3000);
    }
  } catch (e) {
    Object.assign(loiForm.value, getFieldErrors(e));
    loiTrang.value = getDisplayErrorMessage(e, laMoi ? "Không thể tạo nhân viên" : "Không thể lưu thay đổi nhân viên");
  } finally {
    dangLuu.value = false;
  }
}

async function doiMatKhau() {
  if (!matKhauMoi.value.trim() || matKhauMoi.value.length < 6) {
    loiTrang.value = "Mật khẩu mới phải có ít nhất 6 ký tự.";
    return;
  }
  dangLuu.value = true;
  loiTrang.value = "";
  try {
    await doiMatKhauNhanVien(id!, matKhauMoi.value);
    matKhauMoi.value = "";
    showDoiMatKhau.value = false;
    thongBao.value = "Đã đổi mật khẩu thành công!";
    setTimeout(() => (thongBao.value = ""), 3000);
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể đổi mật khẩu nhân viên");
  } finally {
    dangLuu.value = false;
  }
}

async function doiTrangThai(trangThai: number) {
  try {
    const updated = await doiTrangThaiNhanVien(id!, trangThai);
    nhanVien.value = updated;
    thongBao.value = trangThai === 1 ? "Đã kích hoạt tài khoản!" : "Đã khóa tài khoản!";
    setTimeout(() => (thongBao.value = ""), 3000);
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể cập nhật trạng thái nhân viên");
  }
}

async function xoa() {
  if (!confirm("Bạn có chắc chắn muốn xóa nhân viên này?")) return;
  try {
    await xoaNhanVien(id!);
    router.push({ name: "admin-nhan-vien" });
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể xóa nhân viên");
  }
}

async function xuLyUploadAnh(event: Event) {
  const target = event.target as HTMLInputElement;
  if (!target.files?.length) return;
  dangUpload.value = true;
  try {
    const url = await uploadFile(target.files[0]);
    form.value.hinhAnh = url;
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải ảnh nhân viên");
  } finally {
    dangUpload.value = false;
  }
}

const dsVaiTro = [
  { value: 1, label: "Admin" },
  { value: 2, label: "Bán hàng" },
  { value: 3, label: "Kho" },
];

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-nhan-vien' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm nhân viên mới" : "Chi tiết nhân viên" }}
        </h1>
        <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin nhân viên mới vào form bên dưới." : `Mã: ${nhanVien?.ma ?? '...'}` }}</p>
      </div>
    </section>

    <!-- Loading -->
    <div v-if="dangTai" class="rounded-[24px] border bg-white p-10 text-center text-slate-400 text-sm">Đang tải...</div>

    <template v-else>
      <!-- Notification -->
      <div v-if="thongBao" class="rounded-2xl bg-emerald-50 border border-emerald-100 px-5 py-3 text-sm font-semibold text-emerald-700">✓ {{ thongBao }}</div>
      <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="flex flex-col-reverse xl:grid gap-5 xl:grid-cols-[320px_1fr]">
        
        <!-- Sidebar - moved to the left -->
        <div class="space-y-4">
          <!-- Avatar card -->
          <div class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm text-center">
            <div class="relative inline-block cursor-pointer" @click="($refs.fileInputAvatar as HTMLInputElement)?.click()" title="Bấm để thay đổi hình ảnh">
              <img
                :src="form.hinhAnh || nhanVien?.hinhAnh || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(form.hoTen || nhanVien?.hoTen || 'NV') + '&background=f1f5f9&color=475569&size=128'"
                class="mx-auto h-32 w-32 rounded-full object-cover ring-4 ring-slate-100 hover:opacity-80 transition"
              />
              <input type="file" ref="fileInputAvatar" @change="xuLyUploadAnh" accept="image/*,.jpg,.jpeg,.png,.gif,.webp" class="hidden" />
              <div v-if="dangUpload" class="absolute inset-0 flex items-center justify-center rounded-full bg-white/70">
                <span class="text-xs font-bold text-slate-600">Đang tải...</span>
              </div>
            </div>
            <p class="mt-4 text-base font-bold text-slate-800">{{ form.hoTen || nhanVien?.hoTen || "Nhân viên mới" }}</p>
            <p class="text-sm text-slate-400">{{ form.email || nhanVien?.email || "Chưa cập nhật email" }}</p>
            
            <div v-if="!laMoi" class="mt-2">
              <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="nhanVien?.trangThai === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'">
                {{ nhanVien?.tenTrangThai }}
              </span>
            </div>
            <p v-else class="mt-2 text-xs text-slate-400">(Bấm vào ảnh để chọn avatar)</p>
          </div>

          <!-- Các chức năng phụ (chỉ hiện khi sửa) -->
          <template v-if="!laMoi">
            <!-- Đổi mật khẩu -->
            <div class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
              <h3 class="mb-3 text-sm font-bold text-slate-800">Đổi mật khẩu</h3>
              <div v-if="!showDoiMatKhau">
                <button @click="showDoiMatKhau = true" class="w-full rounded-2xl border border-slate-200 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-50 transition">Đổi mật khẩu</button>
              </div>
              <div v-else class="space-y-3">
                <input v-model="matKhauMoi" type="password" placeholder="Mật khẩu mới (tối thiểu 6 ký tự)" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" />
                <div class="flex gap-2">
                  <button @click="doiMatKhau" :disabled="dangLuu" class="flex-1 rounded-2xl bg-rose-500 py-2 text-xs font-bold text-white hover:bg-rose-600 transition">Xác nhận</button>
                  <button @click="showDoiMatKhau = false; matKhauMoi = ''" class="flex-1 rounded-2xl border py-2 text-xs font-semibold text-slate-500 hover:bg-slate-50 transition">Hủy</button>
                </div>
              </div>
            </div>

            <!-- Đổi trạng thái -->
            <div class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm space-y-2">
              <h3 class="mb-3 text-sm font-bold text-slate-800">Trạng thái tài khoản</h3>
              <button
                v-if="nhanVien?.trangThai === 1"
                @click="doiTrangThai(0)"
                class="w-full rounded-2xl bg-rose-50 py-2.5 text-sm font-bold text-rose-600 hover:bg-rose-100 transition"
              >🔒 Khóa tài khoản</button>
              <button
                v-else
                @click="doiTrangThai(1)"
                class="w-full rounded-2xl bg-emerald-50 py-2.5 text-sm font-bold text-emerald-600 hover:bg-emerald-100 transition"
              >✓ Kích hoạt tài khoản</button>
            </div>
          </template>
        </div>

        <!-- Main form -->
        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-5">
          <div class="flex items-center gap-3 mb-2">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-violet-50 text-violet-500">
              <User class="h-5 w-5" />
            </div>
            <div>
              <h2 class="text-base font-bold text-slate-800">Thông tin cơ bản</h2>
              <p class="text-sm text-slate-400">Họ tên, email, liên hệ và vai trò trong hệ thống.</p>
            </div>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Họ và tên <span class="text-rose-500">*</span></span>
              <input v-model="form.hoTen" type="text" placeholder="Nhập họ tên" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.hoTen ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
              <p v-if="loiForm.hoTen" class="text-xs text-rose-500 mt-1">{{ loiForm.hoTen }}</p>
            </label>

            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Email <span class="text-rose-500">*</span></span>
              <input v-model="form.email" type="email" placeholder="Nhập email" :disabled="!laMoi" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.email ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300', !laMoi ? 'cursor-not-allowed opacity-60 text-slate-500' : '']" />
              <p v-if="loiForm.email" class="text-xs text-rose-500 mt-1">{{ loiForm.email }}</p>
            </label>

            <label class="space-y-2" v-if="laMoi">
              <span class="text-[13px] font-semibold text-slate-500">Mật khẩu <span class="text-rose-500">*</span></span>
              <input v-model="form.matKhau" type="password" placeholder="Tối thiểu 6 ký tự" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.matKhau ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
              <p v-if="loiForm.matKhau" class="text-xs text-rose-500 mt-1">{{ loiForm.matKhau }}</p>
            </label>

            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Số điện thoại</span>
              <input v-model="form.sdt" type="tel" placeholder="Nhập SĐT" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition" />
            </label>

            <label class="space-y-2">
              <span class="text-[13px] font-semibold text-slate-500">Vai trò <span class="text-rose-500">*</span></span>
              <select v-model="form.vaiTro" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition">
                <option v-for="vt in dsVaiTro" :key="vt.value" :value="vt.value">{{ vt.label }}</option>
              </select>
            </label>
          </div>

          <label class="space-y-2 block mb-6">
            <span class="text-[13px] font-semibold text-slate-500">Địa chỉ</span>
            <input v-model="form.diaChi" type="text" placeholder="Nhập địa chỉ" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition" />
          </label>

          <!-- Actions -->
          <div class="flex items-center gap-3 pt-4 border-t border-slate-100">
            <button @click="luu" :disabled="dangLuu" class="inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
              <Save class="h-4 w-4" />
              {{ dangLuu ? "Đang lưu..." : (laMoi ? "Tạo nhân viên" : "Lưu thay đổi") }}
            </button>
            <button @click="router.push({ name: 'admin-nhan-vien' })" class="rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100">Hủy</button>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>
