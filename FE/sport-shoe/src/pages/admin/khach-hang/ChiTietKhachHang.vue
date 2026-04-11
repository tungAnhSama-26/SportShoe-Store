<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Save, User, MapPin, Plus, Trash2, Home, CheckCircle2 } from "lucide-vue-next";
import {
  capNhatKhachHang,
  doiMatKhauKhachHang,
  doiTrangThaiKhachHang,
  layChiTietKhachHang,
  taoKhachHang,
  xoaKhachHang,
  layDanhSachDiaChi,
  themDiaChi,
  capNhatDiaChi,
  xoaDiaChi,
  uploadFile
} from "../../../services/khach-hang";

const route = useRoute();
const router = useRouter();

const id = route.params.id as string | undefined;
const laMoi = !id;

const dangTai = ref(false);
const dangLuu = ref(false);
const dangUpload = ref(false);
const loiTrang = ref("");
const loiForm = ref({
  tenDangNhap: "",
  hoTen: "",
  email: "",
  matKhau: "",
});
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

const dsDiaChi = ref([]);
const hienModalDiaChi = ref(false);
const dangLuuDiaChi = ref(false);
const diaChiDangSua = ref(null);
const formDiaChi = ref({
  hoTen: "",
  sdt: "",
  tinhThanh: "",
  quanHuyen: "",
  phuongXa: "",
  diaChiCuThe: "",
  laMacDinh: false,
});

const matKhauMoi = ref("");
const showDoiMatKhau = ref(false);

async function taiChiTiet() {
  if (laMoi) return;
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
    await taiDsDiaChi();
  } catch (e) {
    loiTrang.value = e instanceof Error ? e.message : "Không thể tải thông tin khách hàng";
  } finally {
    dangTai.value = false;
  }
}

async function taiDsDiaChi() {
  if (laMoi) return;
  try {
    dsDiaChi.value = await layDanhSachDiaChi(id!);
  } catch (e) {
    console.error("Lỗi tải địa chỉ:", e);
  }
}

async function luu() {
  loiForm.value = { tenDangNhap: "", hoTen: "", email: "", matKhau: "" };
  let hasError = false;

  if (laMoi && !form.value.tenDangNhap.trim()) {
    loiForm.value.tenDangNhap = "Vui lòng nhập tên đăng nhập.";
    hasError = true;
  }

  if (!form.value.hoTen.trim()) {
    loiForm.value.hoTen = "Vui lòng nhập họ tên.";
    hasError = true;
  }

  if (form.value.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    loiForm.value.email = "Email không hợp lệ.";
    hasError = true;
  }

  if (laMoi && !form.value.matKhau.trim()) {
    loiForm.value.matKhau = "Vui lòng nhập mật khẩu.";
    hasError = true;
  }

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
      router.push({ name: "admin-khach-hang" });
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
    loiTrang.value = e instanceof Error ? e.message : "Có lỗi xảy ra";
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
    loiTrang.value = e instanceof Error ? e.message : "Không thể đổi mật khẩu";
  } finally {
    dangLuu.value = false;
  }
}

async function doiTrangThai(trangThai: number) {
  try {
    const updated = await doiTrangThaiKhachHang(id!, trangThai);
    khachHang.value = updated;
    thongBao.value = trangThai === 1 ? "Đã kích hoạt khách hàng!" : "Đã khóa khách hàng!";
    setTimeout(() => (thongBao.value = ""), 3000);
  } catch (e) {
    loiTrang.value = e instanceof Error ? e.message : "Không thể đổi trạng thái";
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
    loiTrang.value = e instanceof Error ? e.message : "Có lỗi khi tải ảnh";
  } finally {
    dangUpload.value = false;
  }
}

async function luuDiaChi() {
  if (!formDiaChi.value.hoTen || !formDiaChi.value.sdt || !formDiaChi.value.tinhThanh || !formDiaChi.value.quanHuyen || !formDiaChi.value.phuongXa || !formDiaChi.value.diaChiCuThe) {
    alert("Vui lòng điền đầy đủ thông tin địa chỉ.");
    return;
  }

  dangLuuDiaChi.value = true;
  try {
    if (diaChiDangSua.value) {
      await capNhatDiaChi(diaChiDangSua.value.id, formDiaChi.value);
    } else {
      await themDiaChi(id!, formDiaChi.value);
    }
    await taiDsDiaChi();
    dongModalDiaChi();
    thongBao.value = "Đã lưu địa chỉ thành công!";
    setTimeout(() => (thongBao.value = ""), 3000);
  } catch (e) {
    alert(e instanceof Error ? e.message : "Không thể lưu địa chỉ");
  } finally {
    dangLuuDiaChi.value = false;
  }
}

function moThemDiaChi() {
  diaChiDangSua.value = null;
  formDiaChi.value = {
    hoTen: form.value.hoTen,
    sdt: form.value.sdt,
    tinhThanh: "",
    quanHuyen: "",
    phuongXa: "",
    diaChiCuThe: "",
    laMacDinh: dsDiaChi.value.length === 0,
  };
  hienModalDiaChi.value = true;
}

function moSuaDiaChi(dc: any) {
  diaChiDangSua.value = dc;
  formDiaChi.value = {
    hoTen: dc.hoTen,
    sdt: dc.sdt,
    tinhThanh: dc.tinhThanh,
    quanHuyen: dc.quanHuyen,
    phuongXa: dc.phuongXa,
    diaChiCuThe: dc.diaChiCuThe,
    laMacDinh: dc.laMacDinh,
  };
  hienModalDiaChi.value = true;
}

function dongModalDiaChi() {
  hienModalDiaChi.value = false;
  diaChiDangSua.value = null;
}

async function xoaDiaChiKh(diaChiId: number) {
  if (!confirm("Bạn có chắc chắn muốn xóa địa chỉ này?")) return;
  try {
    await xoaDiaChi(diaChiId);
    await taiDsDiaChi();
  } catch (e) {
    alert("Không thể xóa địa chỉ");
  }
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-khach-hang' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm khách hàng mới" : "Chi tiết khách hàng" }}
        </h1>
        <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin khách hàng mới vào form bên dưới." : `Tên đăng nhập: ${khachHang?.tenDangNhap ?? '...'}` }}</p>
      </div>
    </section>

    <!-- Loading -->
    <div v-if="dangTai" class="rounded-[24px] border bg-white p-10 text-center text-slate-400 text-sm">Đang tải...</div>

    <template v-else>
      <!-- Notification -->
      <div v-if="thongBao" class="rounded-2xl bg-emerald-50 border border-emerald-100 px-5 py-3 text-sm font-semibold text-emerald-700">✓ {{ thongBao }}</div>
      <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="flex flex-col-reverse xl:grid gap-5 xl:grid-cols-[320px_1fr]">
        
        <!-- Sidebar -->
        <div class="space-y-4">
          <!-- Avatar card -->
          <div class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm text-center">
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
          </div>

          <!-- Extra functions -->
          <template v-if="!laMoi">
            <!-- Reset Password -->
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

            <!-- Status Toggle -->
            <div class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm space-y-2">
              <h3 class="mb-3 text-sm font-bold text-slate-800">Trạng thái tài khoản</h3>
              <button
                v-if="khachHang?.trangThai === 1"
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

        <!-- Main Content -->
        <div class="space-y-5">
          <!-- Basic Info Card -->
          <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-5">
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
              <label class="space-y-2" v-if="laMoi">
                <span class="text-[13px] font-semibold text-slate-500">Tên đăng nhập <span class="text-rose-500">*</span></span>
                <input v-model="form.tenDangNhap" type="text" placeholder="Nhập tên đăng nhập" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.tenDangNhap ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.tenDangNhap" class="text-xs text-rose-500 mt-1">{{ loiForm.tenDangNhap }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Họ và tên <span class="text-rose-500">*</span></span>
                <input v-model="form.hoTen" type="text" placeholder="Nhập họ tên" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.hoTen ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.hoTen" class="text-xs text-rose-500 mt-1">{{ loiForm.hoTen }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Email</span>
                <input v-model="form.email" type="email" placeholder="Nhập email" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.email ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.email" class="text-xs text-rose-500 mt-1">{{ loiForm.email }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Số điện thoại</span>
                <input v-model="form.sdt" type="tel" placeholder="Nhập SĐT" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition" />
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Ngày sinh</span>
                <input v-model="form.ngaySinh" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition" />
              </label>

              <label class="space-y-2" v-if="laMoi">
                <span class="text-[13px] font-semibold text-slate-500">Mật khẩu <span class="text-rose-500">*</span></span>
                <input v-model="form.matKhau" type="password" placeholder="Tối thiểu 6 ký tự" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.matKhau ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.matKhau" class="text-xs text-rose-500 mt-1">{{ loiForm.matKhau }}</p>
              </label>
            </div>

            <div class="flex items-center gap-3 pt-4 border-t border-slate-100">
              <button @click="luu" :disabled="dangLuu" class="inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
                <Save class="h-4 w-4" />
                {{ dangLuu ? "Đang lưu..." : (laMoi ? "Tạo khách hàng" : "Lưu thay đổi") }}
              </button>
            </div>
          </section>

          <!-- Address List Card -->
          <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-5">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-3">
                <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-sky-50 text-sky-500">
                  <MapPin class="h-5 w-5" />
                </div>
                <div>
                  <h2 class="text-base font-bold text-slate-800">Sổ địa chỉ</h2>
                  <p class="text-sm text-slate-400">Quản lý các địa chỉ nhận hàng của khách hàng.</p>
                </div>
              </div>
              <button
                v-if="!laMoi"
                @click="moThemDiaChi"
                class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-rose-500 hover:text-white transition"
                title="Thêm địa chỉ mới"
              >
                <Plus class="h-5 w-5" />
              </button>
            </div>

            <div v-if="laMoi" class="p-10 text-center border-2 border-dashed border-slate-100 rounded-3xl text-slate-400 text-sm italic">
              Vui lòng tạo khách hàng trước khi quản lý địa chỉ.
            </div>

            <div v-else-if="!dsDiaChi.length" class="p-10 text-center border-2 border-dashed border-slate-100 rounded-3xl text-slate-400 text-sm">
              Chưa có địa chỉ nào trong sổ địa chỉ.
            </div>

            <div v-else class="grid gap-4 sm:grid-cols-2">
              <div
                v-for="dc in dsDiaChi"
                :key="dc.id"
                class="group relative rounded-2xl border border-slate-100 bg-slate-50 p-4 transition hover:border-rose-200 hover:bg-white shadow-sm"
              >
                <div class="flex items-start justify-between">
                  <div class="space-y-1">
                    <div class="flex items-center gap-2">
                      <span class="font-bold text-slate-800">{{ dc.hoTen }}</span>
                      <span v-if="dc.laMacDinh" class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-bold text-emerald-600">
                        <CheckCircle2 class="h-3 w-3" /> Mặc định
                      </span>
                    </div>
                    <p class="text-[13px] text-slate-600 font-medium">{{ dc.sdt }}</p>
                    <p class="text-[13px] text-slate-500 leading-relaxed">{{ dc.diaChiCuThe }}, {{ dc.phuongXa }}, {{ dc.quanHuyen }}, {{ dc.tinhThanh }}</p>
                  </div>
                  <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition">
                    <button @click="moSuaDiaChi(dc)" class="h-8 w-8 flex items-center justify-center rounded-lg bg-white text-slate-400 hover:text-sky-500 shadow-sm border border-slate-100" title="Sửa địa chỉ"><MapPin class="h-4 w-4" /></button>
                    <button @click="xoaDiaChiKh(dc.id)" v-if="!dc.laMacDinh" class="h-8 w-8 flex items-center justify-center rounded-lg bg-white text-slate-400 hover:text-rose-500 shadow-sm border border-slate-100" title="Xóa địa chỉ"><Trash2 class="h-4 w-4" /></button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- Modal Address -->
      <div v-if="hienModalDiaChi" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/40 p-4 transition-all">
        <div class="w-full max-w-lg rounded-[32px] bg-white p-8 shadow-2xl animate-in fade-in zoom-in duration-300">
          <div class="mb-6 flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-rose-50 text-rose-500">
              <Home class="h-6 w-6" />
            </div>
            <div>
              <h2 class="text-xl font-bold text-slate-800">{{ diaChiDangSua ? "Cập nhật địa chỉ" : "Thêm địa chỉ mới" }}</h2>
              <p class="text-sm text-slate-400">Nhập thông tin địa chỉ giao hàng chi tiết.</p>
            </div>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="space-y-1.5">
              <span class="text-[13px] font-semibold text-slate-500">Họ tên người nhận <span class="text-rose-500">*</span></span>
              <input v-model="formDiaChi.hoTen" type="text" placeholder="Họ tên" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
            </label>
            <label class="space-y-1.5">
              <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
              <input v-model="formDiaChi.sdt" type="tel" placeholder="Số điện thoại" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
            </label>
            <label class="space-y-1.5">
              <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
              <input v-model="formDiaChi.tinhThanh" type="text" placeholder="Tỉnh/Thành phố" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
            </label>
            <label class="space-y-1.5">
              <span class="text-[13px] font-semibold text-slate-500">Quận/Huyện <span class="text-rose-500">*</span></span>
              <input v-model="formDiaChi.quanHuyen" type="text" placeholder="Quận/Huyện" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
            </label>
            <label class="space-y-1.5">
              <span class="text-[13px] font-semibold text-slate-500">Phường/Xã <span class="text-rose-500">*</span></span>
              <input v-model="formDiaChi.phuongXa" type="text" placeholder="Phường/Xã" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
            </label>
            <label class="space-y-1.5 col-span-2">
              <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
              <input v-model="formDiaChi.diaChiCuThe" type="text" placeholder="Địa chỉ cụ thể" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
            </label>
            <div class="col-span-2 flex items-center gap-2 mt-2">
              <input v-model="formDiaChi.laMacDinh" type="checkbox" id="laMacDinh" class="h-4 w-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500" />
              <label for="laMacDinh" class="text-sm font-semibold text-slate-600 cursor-pointer">Đặt làm địa chỉ mặc định</label>
            </div>
          </div>

          <div class="mt-8 flex gap-3">
            <button
              @click="luuDiaChi"
              :disabled="dangLuuDiaChi"
              class="flex-1 rounded-2xl bg-rose-500 h-11 text-sm font-bold text-white hover:bg-rose-600 transition disabled:opacity-50"
            >{{ dangLuuDiaChi ? "Đang lưu..." : "Lưu địa chỉ" }}</button>
            <button
              @click="dongModalDiaChi"
              class="flex-1 rounded-2xl border border-slate-200 h-11 text-sm font-semibold text-slate-600 hover:bg-slate-50 transition"
            >Đóng</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
