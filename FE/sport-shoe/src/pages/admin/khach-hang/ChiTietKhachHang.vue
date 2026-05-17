<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, MapPin, Save, User } from "lucide-vue-next";
import {
  capNhatKhachHang,
  doiMatKhauKhachHang,
  doiTrangThaiKhachHang,
  layChiTietKhachHang,
  taoKhachHang,
  themDiaChi,
  uploadFile
} from "../../../services/khach-hang";
import { getDisplayErrorMessage, getFieldErrors } from "../../../utils/error-message";
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";

const route = useRoute();
const router = useRouter();

const id = route.params.id as string | undefined;
const laMoi = !id;
const CUSTOMER_CREATE_TOAST_KEY = "admin-khach-hang-toast";

const dangTai = ref(false);
const dangLuu = ref(false);
const dangUpload = ref(false);
const loiTrang = ref("");
const loiForm = ref({ tenDangNhap: "", hoTen: "", email: "", matKhau: "" });

function taoMatKhauNgauNhien(): string {
  const chars = 'ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$!';
  let result = '';
  for (let i = 0; i < 10; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}
const thongBao = ref("");
const khachHang = ref(null);

const form = ref({
  tenDangNhap: "",
  hoTen: "",
  email: "",
  sdt: "",
  ngaySinh: "",
  hinhAnh: "",
  matKhau: "",
});

// Form địa chỉ bắt buộc khi thêm mới
const formDiaChi = ref({
  hoTen: "",
  sdt: "",
  tinhThanh: "",
  quanHuyen: "",
  phuongXa: "",
  diaChiCuThe: "",
  laMacDinh: true,
});
const loiDiaChi = ref({ hoTen: "", sdt: "", tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "" });

// Province cascade
const dsTinh = ref<any[]>([]);
const dsHuyen = ref<any[]>([]);
const dsXa = ref<any[]>([]);
const maTinhChon = ref<number | null>(null);
const maHuyenChon = ref<number | null>(null);

async function taiDsTinh() {
  if (dsTinh.value.length) return;
  try {
    const res = await fetch("https://provinces.open-api.vn/api/p/");
    dsTinh.value = await res.json();
  } catch { dsTinh.value = []; }
}

async function onTinhChange(code: number | null) {
  maTinhChon.value = code;
  dsHuyen.value = [];
  dsXa.value = [];
  maHuyenChon.value = null;
  formDiaChi.value.tinhThanh = dsTinh.value.find(t => t.code === code)?.name ?? "";
  formDiaChi.value.quanHuyen = "";
  formDiaChi.value.phuongXa = "";
  if (!code) return;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/p/${code}?depth=2`);
    const data = await res.json();
    dsHuyen.value = data.districts ?? [];
  } catch { dsHuyen.value = []; }
}

async function onHuyenChange(code: number | null) {
  maHuyenChon.value = code;
  dsXa.value = [];
  formDiaChi.value.quanHuyen = dsHuyen.value.find(h => h.code === code)?.name ?? "";
  formDiaChi.value.phuongXa = "";
  if (!code) return;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/d/${code}?depth=2`);
    const data = await res.json();
    dsXa.value = data.wards ?? [];
  } catch { dsXa.value = []; }
}

// Luôn đồng bộ họ tên & SĐT xuống form địa chỉ khi ở chế độ thêm mới
watch(() => form.value.hoTen, (v) => { if (laMoi) formDiaChi.value.hoTen = v; });
watch(() => form.value.sdt, (v) => { if (laMoi) formDiaChi.value.sdt = v; });

// Tự động tạo tên đăng nhập từ phần trước @ của email
watch(() => form.value.email, (email) => {
  if (!laMoi) return;
  const atIndex = email.indexOf('@');
  form.value.tenDangNhap = atIndex > 0 ? email.substring(0, atIndex) : '';
});

const matKhauMoi = ref("");
const showDoiMatKhau = ref(false);

async function taiChiTiet() {
  if (laMoi) {
    form.value.matKhau = taoMatKhauNgauNhien();
    await taiDsTinh();
    return;
  }
  dangTai.value = true;
  try {
    const data = await layChiTietKhachHang(id!);
    khachHang.value = data;
    form.value = {
      tenDangNhap: data.tenDangNhap,
      hoTen: data.hoTen,
      email: data.email ?? "",
      sdt: data.sdt ?? "",
      ngaySinh: data.ngaySinh ?? "",
      hinhAnh: data.hinhAnh ?? "",
      matKhau: "",
    };
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải thông tin khách hàng");
  } finally {
    dangTai.value = false;
  }
}

function validateDiaChi(): boolean {
  const f = formDiaChi.value;
  const err = { hoTen: "", sdt: "", tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "" };
  let ok = true;
  if (!f.hoTen.trim()) { err.hoTen = "Vui lòng nhập họ tên người nhận."; ok = false; }
  if (!f.sdt.trim()) { err.sdt = "Vui lòng nhập số điện thoại."; ok = false; }
  if (!f.tinhThanh) { err.tinhThanh = "Vui lòng chọn tỉnh/thành phố."; ok = false; }
  if (!f.quanHuyen) { err.quanHuyen = "Vui lòng chọn quận/huyện."; ok = false; }
  if (!f.phuongXa) { err.phuongXa = "Vui lòng chọn phường/xã."; ok = false; }
  if (!f.diaChiCuThe.trim()) { err.diaChiCuThe = "Vui lòng nhập địa chỉ cụ thể."; ok = false; }
  loiDiaChi.value = err;
  return ok;
}

async function luu() {
  loiForm.value = { tenDangNhap: "", hoTen: "", email: "", matKhau: "" };
  let hasError = false;

  if (!form.value.hoTen.trim()) {
    loiForm.value.hoTen = "Vui lòng nhập họ tên khách hàng.";
    hasError = true;
  }
  if (form.value.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    loiForm.value.email = "Email khách hàng chưa đúng định dạng.";
    hasError = true;
  }
  if (laMoi && !validateDiaChi()) hasError = true;
  if (hasError) return;

  dangLuu.value = true;
  loiTrang.value = "";
  thongBao.value = "";
  try {
    if (laMoi) {
      const result = await taoKhachHang({
        tenDangNhap: form.value.tenDangNhap,
        hoTen: form.value.hoTen,
        email: form.value.email || undefined,
        sdt: form.value.sdt || undefined,
        ngaySinh: form.value.ngaySinh || undefined,
        hinhAnh: form.value.hinhAnh || undefined,
        matKhau: form.value.matKhau,
      });
      await themDiaChi(result.id, formDiaChi.value);
      if (typeof window !== "undefined") {
        const emailDaNhap = form.value.email.trim();
        const matKhauDaTao = form.value.matKhau;
        const tenDangNhapDaTao = form.value.tenDangNhap;
        window.sessionStorage.setItem(
          CUSTOMER_CREATE_TOAST_KEY,
          JSON.stringify({
            loai: "success",
            tieuDe: "Đã tạo khách hàng mới",
            noiDung: emailDaNhap
              ? `Email đã lưu: ${emailDaNhap} | Tên đăng nhập: ${tenDangNhapDaTao} | Mật khẩu: ${matKhauDaTao}`
              : `Tên đăng nhập: ${tenDangNhapDaTao} | Mật khẩu: ${matKhauDaTao}`,
          }),
        );
      }
      await router.push({ name: "admin-khach-hang" });
      await nextTick();
    } else {
      const updated = await capNhatKhachHang(id!, {
        hoTen: form.value.hoTen,
        email: form.value.email || undefined,
        sdt: form.value.sdt || undefined,
        ngaySinh: form.value.ngaySinh || undefined,
        hinhAnh: form.value.hinhAnh || undefined,
      });
      khachHang.value = updated;
      thongBao.value = "Đã lưu thay đổi thành công!";
      setTimeout(() => (thongBao.value = ""), 3000);
    }
  } catch (e) {
    Object.assign(loiForm.value, getFieldErrors(e));
    loiTrang.value = getDisplayErrorMessage(e, laMoi ? "Không thể tạo khách hàng" : "Không thể lưu thay đổi khách hàng");
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
    await doiMatKhauKhachHang(id!, matKhauMoi.value);
    matKhauMoi.value = "";
    showDoiMatKhau.value = false;
    thongBao.value = "Đã đổi mật khẩu thành công!";
    setTimeout(() => (thongBao.value = ""), 3000);
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể đổi mật khẩu khách hàng");
  } finally {
    dangLuu.value = false;
  }
}

async function doiTrangThai(trangThai: number) {
  const hanhDong = trangThai === 1 ? "kích hoạt" : "khóa";
  const tenKhachHang = form.value.hoTen || form.value.tenDangNhap || "khách hàng này";
  if (!window.confirm(`Bạn có chắc muốn ${hanhDong} ${tenKhachHang} không?`)) return;
  try {
    const updated = await doiTrangThaiKhachHang(id!, trangThai);
    khachHang.value = updated;
    thongBao.value = trangThai === 1 ? "Đã kích hoạt khách hàng!" : "Đã khóa khách hàng!";
    setTimeout(() => (thongBao.value = ""), 3000);
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể cập nhật trạng thái khách hàng");
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
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải ảnh khách hàng");
  } finally {
    dangUpload.value = false;
  }
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section class="flex items-center gap-4">
      <Button
        variant="ghost"
        size="icon"
        class="rounded-full bg-slate-100 hover:bg-slate-200"
        @click="router.push({ name: 'admin-khach-hang' })"
      >
        <ArrowLeft class="h-5 w-5" />
      </Button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm khách hàng mới" : "Chi tiết khách hàng" }}
        </h1>
        <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin khách hàng mới vào form bên dưới." : `Tên đăng nhập: ${khachHang?.tenDangNhap ?? '...'}` }}</p>
      </div>
    </section>

    <div v-if="dangTai" class="rounded-[24px] border bg-white p-10 text-center text-slate-400 text-sm">Đang tải...</div>

    <template v-else>
      <div v-if="thongBao" class="rounded-2xl bg-emerald-50 border border-emerald-100 px-5 py-3 text-sm font-semibold text-emerald-700">✓ {{ thongBao }}</div>
      <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="flex flex-col-reverse xl:grid gap-5 xl:grid-cols-[320px_1fr]">

        <!-- Sidebar -->
        <div class="space-y-4">
          <!-- Avatar -->
          <Card class="text-center">
            <div class="relative inline-block cursor-pointer" @click="($refs.fileInputAvatar as HTMLInputElement)?.click()" title="Bấm để thay đổi hình ảnh">
              <img
                :src="form.hinhAnh || khachHang?.hinhAnh || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(form.hoTen || khachHang?.hoTen || 'KH') + '&background=f1f5f9&color=475569&size=128'"
                class="mx-auto h-32 w-32 rounded-full object-cover ring-4 ring-slate-100 hover:opacity-80 transition"
              />
              <input type="file" ref="fileInputAvatar" @change="xuLyUploadAnh" accept="image/*,.jpg,.jpeg,.png,.gif,.webp" class="hidden" />
              <div v-if="dangUpload" class="absolute inset-0 flex items-center justify-center rounded-full bg-white/70">
                <span class="text-xs font-bold text-slate-600">Đang tải...</span>
              </div>
            </div>
            <p class="mt-4 text-base font-bold text-slate-800">{{ form.hoTen || khachHang?.hoTen || "Khách hàng mới" }}</p>
            <p class="text-sm text-slate-400">{{ form.email || khachHang?.email || "Chưa cập nhật email" }}</p>
            <div v-if="!laMoi" class="mt-2">
              <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="khachHang?.trangThai === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'">
                {{ khachHang?.tenTrangThai }}
              </span>
            </div>
            <p v-else class="mt-2 text-xs text-slate-400">(Bấm vào ảnh để chọn avatar)</p>
          </Card>

          <!-- Extra functions for edit mode -->
          <template v-if="!laMoi">
            <Card>
              <h3 class="mb-3 text-sm font-bold text-slate-800">Đổi mật khẩu</h3>
              <div v-if="!showDoiMatKhau">
                <Button variant="outline" class="w-full justify-center" @click="showDoiMatKhau = true">Đổi mật khẩu</Button>
              </div>
              <div v-else class="space-y-3">
                <input v-model="matKhauMoi" type="password" placeholder="Mật khẩu mới (tối thiểu 6 ký tự)" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" />
                <div class="flex gap-2">
                  <Button variant="primary" class="flex-1 justify-center" @click="doiMatKhau" :disabled="dangLuu">Xác nhận</Button>
                  <Button variant="soft" class="flex-1 justify-center" @click="showDoiMatKhau = false; matKhauMoi = ''">Hủy</Button>
                </div>
              </div>
            </Card>

            <Card class="space-y-2">
              <h3 class="mb-3 text-sm font-bold text-slate-800">Trạng thái tài khoản</h3>
              <Button v-if="khachHang?.trangThai === 1" variant="soft" class="w-full justify-center text-rose-600 bg-rose-50 hover:bg-rose-100" @click="doiTrangThai(0)">🔒 Khóa tài khoản</Button>
              <Button v-else variant="soft" class="w-full justify-center text-emerald-600 bg-emerald-50 hover:bg-emerald-100" @click="doiTrangThai(1)">✓ Kích hoạt tài khoản</Button>
            </Card>
          </template>
        </div>

        <!-- Main content -->
        <div class="space-y-5">
          <!-- Thông tin cơ bản -->
          <Card class="space-y-5">
            <div class="flex items-center gap-3 mb-2">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-violet-50 text-violet-500">
                <User class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Thông tin cơ bản</h2>
                <p class="text-sm text-slate-400">Họ tên, email, liên hệ và tài khoản.</p>
              </div>
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Họ và tên <span class="text-rose-500">*</span></span>
                <input v-model="form.hoTen" type="text" placeholder="Nhập họ tên" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.hoTen ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.hoTen" class="text-xs text-rose-500">{{ loiForm.hoTen }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Email</span>
                <input v-model="form.email" type="email" placeholder="Nhập email" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.email ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.email" class="text-xs text-rose-500">{{ loiForm.email }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Số điện thoại</span>
                <input v-model="form.sdt" type="tel" placeholder="Nhập SĐT" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition" />
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Ngày sinh</span>
                <input v-model="form.ngaySinh" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition" />
              </label>
            </div>


            <!-- Nút Lưu thay đổi chỉ hiện ở đây khi chỉnh sửa -->
            <div v-if="!laMoi" class="flex items-center gap-3 pt-4 border-t border-slate-100">
              <Button variant="primary" class="px-6" @click="luu" :disabled="dangLuu">
                <Save class="h-4 w-4 mr-2" />
                {{ dangLuu ? "Đang lưu..." : "Lưu thay đổi" }}
              </Button>
            </div>
          </Card>

          <!-- Địa chỉ giao hàng (chỉ khi thêm mới) -->
          <Card v-if="laMoi" class="space-y-5">
            <div class="flex items-center gap-3 mb-2">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-sky-50 text-sky-500">
                <MapPin class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Địa chỉ giao hàng <span class="text-rose-500">*</span></h2>
                <p class="text-sm text-slate-400">Địa chỉ mặc định của khách hàng mới.</p>
              </div>
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Họ tên người nhận <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.hoTen" type="text" placeholder="Họ tên người nhận" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.hoTen ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiDiaChi.hoTen" class="text-xs text-rose-500">{{ loiDiaChi.hoTen }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.sdt" type="tel" placeholder="Số điện thoại" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.sdt ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiDiaChi.sdt" class="text-xs text-rose-500">{{ loiDiaChi.sdt }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
                <select
                  :value="maTinhChon"
                  @change="onTinhChange(Number(($event.target as HTMLSelectElement).value) || null)"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.tinhThanh ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn tỉnh/thành --</option>
                  <option v-for="t in dsTinh" :key="t.code" :value="t.code">{{ t.name }}</option>
                </select>
                <p v-if="loiDiaChi.tinhThanh" class="text-xs text-rose-500">{{ loiDiaChi.tinhThanh }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Quận/Huyện <span class="text-rose-500">*</span></span>
                <select
                  :value="maHuyenChon"
                  @change="onHuyenChange(Number(($event.target as HTMLSelectElement).value) || null)"
                  :disabled="!maTinhChon"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white disabled:opacity-50', loiDiaChi.quanHuyen ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn quận/huyện --</option>
                  <option v-for="h in dsHuyen" :key="h.code" :value="h.code">{{ h.name }}</option>
                </select>
                <p v-if="loiDiaChi.quanHuyen" class="text-xs text-rose-500">{{ loiDiaChi.quanHuyen }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Phường/Xã <span class="text-rose-500">*</span></span>
                <select
                  :value="formDiaChi.phuongXa"
                  @change="formDiaChi.phuongXa = ($event.target as HTMLSelectElement).value"
                  :disabled="!maHuyenChon"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white disabled:opacity-50', loiDiaChi.phuongXa ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn phường/xã --</option>
                  <option v-for="x in dsXa" :key="x.code" :value="x.name">{{ x.name }}</option>
                </select>
                <p v-if="loiDiaChi.phuongXa" class="text-xs text-rose-500">{{ loiDiaChi.phuongXa }}</p>
              </label>

              <label class="space-y-2 sm:col-span-2">
                <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.diaChiCuThe" type="text" placeholder="Số nhà, tên đường..." :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.diaChiCuThe ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiDiaChi.diaChiCuThe" class="text-xs text-rose-500">{{ loiDiaChi.diaChiCuThe }}</p>
              </label>
            </div>

            <!-- Nút Tạo khách hàng nằm dưới cùng form địa chỉ -->
            <div class="flex items-center gap-3 pt-4 border-t border-slate-100">
              <Button variant="primary" class="px-6" @click="luu" :disabled="dangLuu">
                <Save class="h-4 w-4 mr-2" />
                {{ dangLuu ? "Đang tạo..." : "Tạo khách hàng" }}
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </template>
  </div>
</template>
