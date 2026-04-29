<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, CheckCircle2, CircleX, RefreshCcw, Save, Ticket, X } from "lucide-vue-next";
import {
  createPhieuGiamGia,
  getPhieuGiamGiaDetail,
  updatePhieuGiamGia
} from "../../../services/khuyen-mai";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const route = useRoute();
const router = useRouter();

const id = route.params.id;
const laMoi = !id;

const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");
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
  toastTimer = setTimeout(() => { toast.value.hienThi = false; }, 3200);
}
const formErrors = reactive({});

const form = reactive({
  id: null,
  ma: "",
  ten: "",
  loai: "1",
  loaiPhieu: "1",
  giaTri: "",
  giaTriToiThieu: "0",
  giamToiDa: "0",
  ngayBatDau: "",
  ngayKetThuc: "",
  soLuong: "",
  trangThai: "1"
});

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function parseVndNumber(value) {
  const rawValue = String(value ?? "").replace(/[^\d]/g, "");
  return rawValue ? Number(rawValue) : 0;
}

function formatVndNumber(value) {
  const numberValue = parseVndNumber(value);
  return numberValue ? numberValue.toLocaleString("vi-VN") : "0";
}

function handleVndInput(field, event) {
  form[field] = formatVndNumber(event.target.value);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

function taoMaNgauNhien() {
  form.ma = "VCH" + Math.random().toString(36).substring(2, 8).toUpperCase();
}

function dongBoSoLuongPhieuCaNhan() {
  if (form.loaiPhieu === "2") {
    form.soLuong = String(dsEmailChon.value.length);
  }
}


async function taiDanhSachKh() {
    dangTaiKh.value = true;
    try {
        const res = await layDanhSachKhachHang({ keyword: searchKh.value, page: 0, size: 50 });
        danhSachKh.value = Array.isArray(res) ? res : (res?.content || []);
    } catch (e) {
        console.error("Lỗi tải khách hàng:", e);
    } finally {
        dangTaiKh.value = false;
    }
}

function toggleEmail(email) {
    if (!email) return;
    const index = dsEmailChon.value.indexOf(email);
    if (index === -1) dsEmailChon.value.push(email);
    else dsEmailChon.value.splice(index, 1);
}

function chonTatCa() {
    if (dsEmailChon.value.length === danhSachKh.value.length) {
        dsEmailChon.value = [];
    } else {
        dsEmailChon.value = danhSachKh.value.map(kh => kh.email).filter(e => !!e);
    }
}

let searchTimer;
function handleSearch() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(taiDanhSachKh, 500);
}

watch(() => form.loaiPhieu, (loaiPhieuMoi) => {
  if (loaiPhieuMoi === "2") {
    dongBoSoLuongPhieuCaNhan();
  }
});

watch(dsEmailChon, () => {
  dongBoSoLuongPhieuCaNhan();
}, { deep: true });


async function taiChiTiet() {
  if (laMoi) {
    if (!form.ma) {
      taoMaNgauNhien();
    }
    return;
  }
  dangTai.value = true;
  try {
    const detail = await getPhieuGiamGiaDetail(id);
    Object.assign(form, {
      id: detail.id,
      ma: detail.ma ?? "",
      ten: detail.ten ?? "",
      loai: String(detail.loai ?? 1),
      loaiPhieu: String(detail.loaiPhieu ?? 1),
      giaTri: detail.giaTri ?? "",
      giaTriToiThieu: formatVndNumber(detail.giaTriToiThieu ?? "0"),
      giamToiDa: formatVndNumber(detail.giamToiDa ?? "0"),
      ngayBatDau: detail.ngayBatDau ?? "",
      ngayKetThuc: detail.ngayKetThuc ?? "",
      soLuong: detail.soLuong ?? "",
      trangThai: String(detail.trangThai ?? 1)
    });
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải chi tiết phiếu giảm giá");
  } finally {
    dangTai.value = false;
  }
}

async function submitForm() {
  resetErrors();
  let isValid = true;

  if (!form.ma.trim()) { formErrors.ma = "Vui lòng nhập mã phiếu giảm giá"; isValid = false; }
  if (!form.ten.trim()) { formErrors.ten = "Vui lòng nhập tên phiếu giảm giá"; isValid = false; }
  if (!form.giaTri || parseVndNumber(form.giaTri) <= 0) { 
    formErrors.giaTri = "Giá trị giảm phải lớn hơn 0"; 
    isValid = false; 
  } else if (Number(form.loai) === 1 && parseVndNumber(form.giaTri) > 100) {
    formErrors.giaTri = "Phần trăm giảm không được vượt quá 100%";
    isValid = false;
  }

  if (form.giaTriToiThieu && parseVndNumber(form.giaTriToiThieu) < 1) {
    formErrors.giaTriToiThieu = "Giá trị đơn tối thiểu phải lớn hơn 0";
    isValid = false;
  }

  if (!form.soLuong || Number(form.soLuong) <= 0) { formErrors.soLuong = "Số lượng phiếu phải lớn hơn 0"; isValid = false; }
  if (!form.ngayBatDau) { formErrors.ngayBatDau = "Vui lòng chọn ngày bắt đầu áp dụng"; isValid = false; }
  if (!form.ngayKetThuc) { formErrors.ngayKetThuc = "Vui lòng chọn ngày kết thúc áp dụng"; isValid = false; }

  if (form.ngayBatDau && form.ngayKetThuc && form.ngayBatDau > form.ngayKetThuc) {
    formErrors.ngayKetThuc = "Ngày kết thúc phải sau ngày bắt đầu";
    isValid = false;
  }

  if (!isValid) return;

  saving.value = true;
  loiTrang.value = "";
  try {
    const payload = {
      ma: form.ma.trim(),
      ten: form.ten.trim(),
      loai: Number(form.loai),
      loaiPhieu: Number(form.loaiPhieu),
      giaTri: Number(form.giaTri),
      giaTriToiThieu: Number(form.giaTriToiThieu),
      giamToiDa: Number(form.giamToiDa),
      ngayBatDau: form.ngayBatDau,
      ngayKetThuc: form.ngayKetThuc,
      soLuong: Number(form.soLuong),
      trangThai: Number(form.trangThai),
      ngayTao: laMoi ? getToday() : undefined,
      ngayCapNhat: !laMoi ? getToday() : undefined
    };

    if (laMoi) {
      await createPhieuGiamGia(payload);
    } else {
      await updatePhieuGiamGia(id, payload);
    }

    alert(laMoi ? "Thêm phiếu giảm giá thành công" : "Cập nhật thành công");
    router.push({ name: "admin-phieu-giam-gia" });
  } catch (error) {
    loiTrang.value = error.message || "Lưu thất bại";
  } finally {
    saving.value = false;
  }
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-5 pb-10">
    <!-- Toast Notification -->
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
    <!-- Header -->
    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-phieu-giam-gia' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm phiếu giảm giá mới" : "Chi tiết phiếu giảm giá" }}
        </h1>
      </div>
    </section>

    <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

    <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500">
          <Ticket class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-semibold text-slate-700">Thông tin phiếu</h2>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Mã phiếu <span class="text-rose-500">*</span></label>
          <div class="relative">
            <input v-model="form.ma" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm font-normal text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: VOUCHER2024" />
            <button @click="taoMaNgauNhien" type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-rose-500 transition-colors">
              <RefreshCcw class="h-4 w-4" />
            </button>
          </div>
          <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">{{ formErrors.ma }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Tên phiếu <span class="text-rose-500">*</span></label>
          <input v-model="form.ten" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: Giảm giá hè 2024" />
          <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">{{ formErrors.ten }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-400">Loại giảm <span class="text-rose-500">*</span></label>
          <div class="flex gap-6 pt-2">
            <label class="flex items-center gap-2 cursor-pointer group">
              <input type="radio" v-model="form.loai" value="1" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-normal text-slate-500">Phần trăm (%)</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer group">
              <input type="radio" v-model="form.loai" value="2" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-normal text-slate-500">Tiền mặt (VNĐ)</span>
            </label>
          </div>
        </div>

        <div class="min-w-0 space-y-2">
           <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Giá trị giảm ({{ form.loai === '1' ? '%' : 'VNĐ' }}) <span class="text-rose-500">*</span></label>
           <div class="relative">
             <input :value="form.giaTri" :type="form.loai === '1' ? 'number' : 'text'" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" :placeholder="form.loai === '1' ? '0' : '0'" @input="form.loai === '2' ? handleVndInput('giaTri', $event) : form.giaTri = $event.target.value" />
             <span class="absolute right-4 top-1/2 -translate-y-1/2 text-[11px] font-semibold text-slate-300">{{ form.loai === '1' ? '%' : 'VNĐ' }}</span>
           </div>
           <p v-if="formErrors.giaTri" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTri }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Giá trị đơn tối thiểu (VNĐ)</label>
          <input :value="form.giaTriToiThieu" type="text" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" @input="handleVndInput('giaTriToiThieu', $event)" />
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Giảm tối đa (VNĐ)</label>
          <input :value="form.giamToiDa" type="text" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" @input="handleVndInput('giamToiDa', $event)" />
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Số lượng <span class="text-rose-500">*</span></label>
          <input
            v-model="form.soLuong"
            type="number"
            min="1"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
          />
          <p v-if="formErrors.soLuong" class="text-xs text-rose-500 mt-1">{{ formErrors.soLuong }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Ngày bắt đầu <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayBatDau" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white" />
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Ngày kết thúc <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayKetThuc" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white" />
        </div>

        <div v-if="!laMoi" class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-400 whitespace-nowrap">Trạng thái <span class="text-rose-500">*</span></label>
          <select v-model="form.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="1">Đang hoạt động</option>
            <option value="0">Ngưng hoạt động</option>
          </select>
        </div>
      </div>

      <div class="flex items-center gap-3 pt-6 border-t border-slate-100">
        <button @click="submitForm" :disabled="saving" class="inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
          <Save class="h-4 w-4" />
          {{ saving ? "Đang lưu..." : (laMoi ? "Tạo phiếu giảm giá" : "Lưu thay đổi") }}
        </button>
        <button @click="router.push({ name: 'admin-phieu-giam-gia' })" class="rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100 whitespace-nowrap">Hủy</button>
      </div>
    </section>
  </div>
</template>

