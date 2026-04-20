<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Save, Ticket } from "lucide-vue-next";
import {
  createPhieuGiamGia,
  getPhieuGiamGiaDetail,
  updatePhieuGiamGia
} from "../../../services/khuyen-mai";

const route = useRoute();
const router = useRouter();

const id = route.params.id;
const laMoi = !id;

const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");
const formErrors = reactive({});

const form = reactive({
  id: null,
  ma: "",
  ten: "",
  loai: "1",
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

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

async function taiChiTiet() {
  if (laMoi) return;
  dangTai.value = true;
  try {
    const detail = await getPhieuGiamGiaDetail(id);
    Object.assign(form, {
      id: detail.id,
      ma: detail.ma ?? "",
      ten: detail.ten ?? "",
      loai: String(detail.loai ?? 1),
      giaTri: detail.giaTri ?? "",
      giaTriToiThieu: detail.giaTriToiThieu ?? "0",
      giamToiDa: detail.giamToiDa ?? "0",
      ngayBatDau: detail.ngayBatDau ?? "",
      ngayKetThuc: detail.ngayKetThuc ?? "",
      soLuong: detail.soLuong ?? "",
      trangThai: String(detail.trangThai ?? 1)
    });
  } catch (e) {
    loiTrang.value = e.message || "Không thể tải chi tiết phiếu giảm giá";
  } finally {
    dangTai.value = false;
  }
}

async function submitForm() {
  resetErrors();
  let isValid = true;

  if (!form.ma.trim()) { formErrors.ma = "Mã phiếu không được để trống"; isValid = false; }
  if (!form.ten.trim()) { formErrors.ten = "Tên phiếu không được để trống"; isValid = false; }
  if (!form.giaTri || Number(form.giaTri) <= 0) { formErrors.giaTri = "Giá trị giảm không hợp lệ"; isValid = false; }
  if (!form.soLuong || Number(form.soLuong) <= 0) { formErrors.soLuong = "Số lượng không hợp lệ"; isValid = false; }
  if (!form.ngayBatDau) { formErrors.ngayBatDau = "Chọn ngày bắt đầu"; isValid = false; }
  if (!form.ngayKetThuc) { formErrors.ngayKetThuc = "Chọn ngày kết thúc"; isValid = false; }
  
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
      alert("Thêm phiếu giảm giá thành công");
    } else {
      await updatePhieuGiamGia(id, payload);
      alert("Cập nhật phiếu giảm giá thành công");
    }
    router.push({ name: "admin-phieu-giam-gia" });
  } catch (error) {
    if (error.errors) {
      Object.assign(formErrors, error.errors);
    } else {
      loiTrang.value = error.message || "Lưu thất bại";
    }
  } finally {
    saving.value = false;
  }
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-5">
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
        <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin phiếu giảm giá mới vào form bên dưới." : `Mã: ${form.ma || '...'}` }}</p>
      </div>
    </section>

    <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

    <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500">
          <Ticket class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Thông tin phiếu</h2>
          <p class="text-sm text-slate-400">Thiết lập các thông số giảm giá và thời gian áp dụng.</p>
        </div>
      </div>

      <div class="grid gap-6 md:grid-cols-2">
        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Mã phiếu <span class="text-rose-500">*</span></label>
          <input v-model="form.ma" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: VOUCHER2024" />
          <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">{{ formErrors.ma }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Tên phiếu <span class="text-rose-500">*</span></label>
          <input v-model="form.ten" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: Giảm giá hè 2024" />
          <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">{{ formErrors.ten }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Loại giảm <span class="text-rose-500">*</span></label>
          <div class="flex gap-6 pt-2">
            <label class="flex items-center gap-2 cursor-pointer group">
              <div class="relative flex items-center justify-center">
                <input type="radio" v-model="form.loai" value="1" class="peer h-5 w-5 cursor-pointer appearance-none rounded-full border border-slate-300 checked:border-rose-500 transition-all" />
                <div class="pointer-events-none absolute h-2.5 w-2.5 rounded-full bg-rose-500 opacity-0 peer-checked:opacity-100 transition-opacity"></div>
              </div>
              <span class="text-sm font-medium text-slate-600 group-hover:text-slate-800 transition-colors">Phần trăm (%)</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer group">
              <div class="relative flex items-center justify-center">
                <input type="radio" v-model="form.loai" value="2" class="peer h-5 w-5 cursor-pointer appearance-none rounded-full border border-slate-300 checked:border-rose-500 transition-all" />
                <div class="pointer-events-none absolute h-2.5 w-2.5 rounded-full bg-rose-500 opacity-0 peer-checked:opacity-100 transition-opacity"></div>
              </div>
              <span class="text-sm font-medium text-slate-600 group-hover:text-slate-800 transition-colors">Tiền mặt (VNĐ)</span>
            </label>
          </div>
          <p v-if="formErrors.loai" class="text-xs text-rose-500 mt-1">{{ formErrors.loai }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Giá trị giảm <span class="text-rose-500">*</span></label>
          <input v-model="form.giaTri" type="number" min="0" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Nhập giá trị..." />
          <p v-if="formErrors.giaTri" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTri }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Giá trị đơn tối thiểu (VNĐ)</label>
          <input v-model="form.giaTriToiThieu" type="number" min="0" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.giaTriToiThieu" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTriToiThieu }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Giảm tối đa (VNĐ)</label>
          <input v-model="form.giamToiDa" type="number" min="0" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.giamToiDa" class="text-xs text-rose-500 mt-1">{{ formErrors.giamToiDa }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Số lượng <span class="text-rose-500">*</span></label>
          <input v-model="form.soLuong" type="number" min="1" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.soLuong" class="text-xs text-rose-500 mt-1">{{ formErrors.soLuong }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span class="text-rose-500">*</span></label>
          <select v-model="form.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="1">Kích hoạt</option>
            <option value="0">Tắt</option>
          </select>
          <p v-if="formErrors.trangThai" class="text-xs text-rose-500 mt-1">{{ formErrors.trangThai }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Ngày bắt đầu <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayBatDau" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.ngayBatDau" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayBatDau }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Ngày kết thúc <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayKetThuc" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.ngayKetThuc" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayKetThuc }}</p>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center gap-3 pt-6 border-t border-slate-100">
        <button @click="submitForm" :disabled="saving" class="inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
          <Save class="h-4 w-4" />
          {{ saving ? "Đang lưu..." : (laMoi ? "Tạo phiếu giảm giá" : "Lưu thay đổi") }}
        </button>
        <button @click="router.push({ name: 'admin-phieu-giam-gia' })" class="rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100">Hủy</button>
      </div>
    </section>
  </div>
</template>
