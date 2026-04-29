<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, CheckCircle2, CircleX, Save, Users, X } from "lucide-vue-next";
import {
  createPhieuGiamGiaKhachHang,
  getEmailSuggestions,
  getPhieuGiamGiaKhachHangDetail,
  getPhieuGiamGiaList,
  updatePhieuGiamGiaKhachHang
} from "../../../services/khuyen-mai";
import { layChiTietKhachHang, layDanhSachKhachHang } from "../../../services/khach-hang";
import { getDisplayErrorMessage, getFieldErrors } from "../../../utils/error-message";

const route = useRoute();
const router = useRouter();

const id = route.params.id;
const laMoi = !id;

const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");
const formErrors = reactive({});

const toast = ref({
  hienThi: false,
  loai: "success",
  tieuDe: "",
  noiDung: "",
});
let toastTimer = null;

const toastClass = computed(() => {
  if (toast.value.loai === "success") return "border-emerald-100 bg-emerald-50 text-emerald-700";
  if (toast.value.loai === "warning") return "border-amber-100 bg-amber-50 text-amber-700";
  return "border-rose-100 bg-rose-50 text-rose-700";
});

const toastIconClass = computed(() => {
  if (toast.value.loai === "success") return "bg-emerald-100 text-emerald-600";
  if (toast.value.loai === "warning") return "bg-amber-100 text-amber-600";
  return "bg-rose-100 text-rose-600";
});

const toastAccentClass = computed(() => {
  if (toast.value.loai === "success") return "bg-emerald-500";
  if (toast.value.loai === "warning") return "bg-amber-500";
  return "bg-rose-500";
});

const ToastIcon = computed(() => {
  if (toast.value.loai === "success") return CheckCircle2;
  return CircleX;
});

function hienThiThongBao(loai, tieuDe, noiDung = "") {
  if (toastTimer) clearTimeout(toastTimer);
  toast.value = { hienThi: true, loai, tieuDe, noiDung };
  toastTimer = setTimeout(() => {
    toast.value.hienThi = false;
  }, 3200);
}

const phieuOptions = ref([]);
const emailOptions = ref([]);
const danhSachKh = ref([]);
const searchKh = ref("");
const dsEmailChon = ref([]);

const form = reactive({
  id: null,
  phieuGiamGiaId: "",
  email: "",
  ngaySuDung: "",
  trangThai: "1"
});

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

async function taiDuLieu() {
  dangTai.value = true;
  try {
    const dataOpts = await getPhieuGiamGiaList({ pageNo: 0, pageSize: 1000, trangThai: 1 });
    phieuOptions.value = (dataOpts?.content || []).filter((opt) => Number(opt.loaiPhieu) === 2);
    await taiKhachHang();

    const emails = await getEmailSuggestions();
    emailOptions.value = Array.isArray(emails) ? emails : [];

    if (!laMoi) {
      const detail = await getPhieuGiamGiaKhachHangDetail(id);
      Object.assign(form, {
        id: detail.id,
        phieuGiamGiaId: detail.phieuGiamGiaId ? String(detail.phieuGiamGiaId) : "",
        ngaySuDung: detail.ngaySuDung ? detail.ngaySuDung.slice(0, 10) : "",
        trangThai: String(detail.trangThai ?? 1)
      });
      if (detail.khachHangId) {
        try {
          const kh = await layChiTietKhachHang(detail.khachHangId);
          form.email = kh?.email || "";
        } catch (error) {
          console.error("Lỗi tải email khách hàng:", error);
        }
      }
    }
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải dữ liệu phiếu giảm giá khách hàng");
  } finally {
    dangTai.value = false;
  }
}

async function submitForm() {
  resetErrors();
  let isValid = true;

  if (!form.phieuGiamGiaId) {
    formErrors.phieuGiamGiaId = "Vui lòng chọn phiếu giảm giá cá nhân";
    isValid = false;
  }
  if (laMoi && dsEmailChon.value.length === 0) {
    formErrors.email = "Vui lòng chọn ít nhất một khách hàng để tặng phiếu";
    isValid = false;
  }
  if (!laMoi && (!form.email || !form.email.includes("@"))) {
    formErrors.email = "Email khách hàng chưa đúng định dạng";
    isValid = false;
  }

  if (!isValid) return;

  saving.value = true;
  loiTrang.value = "";

  try {
    if (laMoi) {
      let successCount = 0;
      let failCount = 0;
      let firstErrorMessage = "";

      for (const email of dsEmailChon.value) {
        try {
          await createPhieuGiamGiaKhachHang({
            phieuGiamGiaId: Number(form.phieuGiamGiaId),
            email,
            ngaySuDung: form.ngaySuDung || null,
            trangThai: Number(form.trangThai),
            ngayTao: getToday()
          });
          successCount++;
        } catch (error) {
          failCount++;
          if (!firstErrorMessage) {
            firstErrorMessage = getDisplayErrorMessage(error, "Không thể tặng phiếu cho một số khách hàng đã chọn");
          }
        }
      }

      if (successCount > 0 && failCount === 0) {
        hienThiThongBao("success", "Tặng phiếu thành công", `Đã tặng cho ${successCount} khách hàng`);
        setTimeout(() => {
          router.push({ name: "admin-phieu-giam-gia-khach-hang" });
        }, 1500);
        return;
      }

      if (successCount > 0) {
        hienThiThongBao("warning", "Tặng phiếu hoàn tất một phần", `Thành công: ${successCount}, Thất bại: ${failCount}`);
        setTimeout(() => {
          router.push({ name: "admin-phieu-giam-gia-khach-hang" });
        }, 1500);
        return;
      }

      hienThiThongBao("error", "Tặng phiếu thất bại", firstErrorMessage || `Thất bại: ${failCount}`);
      return;
    }

    const payload = {
      phieuGiamGiaId: Number(form.phieuGiamGiaId),
      email: form.email.trim(),
      ngaySuDung: form.ngaySuDung || null,
      trangThai: Number(form.trangThai)
    };
    await updatePhieuGiamGiaKhachHang(id, payload);
    hienThiThongBao("success", "Cập nhật thành công");
    setTimeout(() => {
      router.push({ name: "admin-phieu-giam-gia-khach-hang" });
    }, 1500);
  } catch (error) {
    const fieldErrors = getFieldErrors(error);
    Object.assign(formErrors, fieldErrors);
    if (!Object.keys(fieldErrors).length) {
      hienThiThongBao("error", "Lỗi dữ liệu", getDisplayErrorMessage(error, "Không thể lưu phiếu giảm giá khách hàng"));
    }
  } finally {
    saving.value = false;
  }
}

async function taiKhachHang() {
  try {
    const data = await layDanhSachKhachHang({ keyword: searchKh.value });
    if (Array.isArray(data)) {
      danhSachKh.value = data;
    } else if (data && Array.isArray(data.content)) {
      danhSachKh.value = data.content;
    } else {
      danhSachKh.value = [];
    }
  } catch (error) {
    console.error("Lỗi tải khách hàng:", error);
    danhSachKh.value = [];
  }
}

function toggleEmail(email) {
  const idx = dsEmailChon.value.indexOf(email);
  if (idx > -1) dsEmailChon.value.splice(idx, 1);
  else dsEmailChon.value.push(email);
}

function chonTatCa() {
  if (dsEmailChon.value.length === danhSachKh.value.length) {
    dsEmailChon.value = [];
    return;
  }
  dsEmailChon.value = danhSachKh.value.map((kh) => kh.email).filter((email) => email);
}

let searchTimer;
function handleSearch() {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => taiKhachHang(), 300);
}

onMounted(taiDuLieu);
</script>

<template>
  <div class="space-y-5 pb-10">
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

    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-phieu-giam-gia-khach-hang' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Tặng phiếu khách hàng" : "Chi tiết phiếu tặng" }}
        </h1>
      </div>
    </section>

    <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

    <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-blue-50 text-blue-500">
          <Users class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Thông tin tặng phiếu</h2>
        </div>
      </div>

      <div class="grid gap-6 md:grid-cols-2">
        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Chọn phiếu giảm giá <span class="text-rose-500">*</span></label>
          <select v-model="form.phieuGiamGiaId" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="">-- Chọn một phiếu --</option>
            <option v-for="opt in phieuOptions" :key="opt.id" :value="String(opt.id)">{{ opt.ten }} ({{ opt.ma }})</option>
          </select>
          <p v-if="formErrors.phieuGiamGiaId" class="text-xs text-rose-500 mt-1">{{ formErrors.phieuGiamGiaId }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Email khách hàng <span class="text-rose-500">*</span></label>
          <input
            v-model="form.email"
            type="email"
            list="email-suggestions"
            placeholder="Ví dụ: customer@example.com"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
          />
          <datalist id="email-suggestions">
            <option v-for="email in emailOptions" :key="email" :value="email"></option>
          </datalist>
          <p v-if="formErrors.email" class="text-xs text-rose-500 mt-1">{{ formErrors.email }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Ngày sử dụng</label>
          <input v-model="form.ngaySuDung" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span class="text-rose-500">*</span></label>
          <select v-model="form.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="1">Kích hoạt</option>
            <option value="0">Tắt</option>
          </select>
        </div>
      </div>

      <div class="flex flex-col-reverse gap-3 border-t border-slate-100 pt-6 sm:flex-row sm:items-center">
        <button @click="submitForm" :disabled="saving" class="inline-flex items-center justify-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60 whitespace-nowrap">
          <Save class="h-4 w-4" />
          {{ saving ? "Đang lưu..." : "Lưu dữ liệu" }}
        </button>
        <button @click="router.push({ name: 'admin-phieu-giam-gia-khach-hang' })" class="rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100 whitespace-nowrap">Hủy</button>
      </div>
    </section>
  </div>
</template>
