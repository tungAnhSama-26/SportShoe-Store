<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Save, Users, Search } from "lucide-vue-next";
import {
  createPhieuGiamGiaKhachHang,
  getPhieuGiamGiaKhachHangDetail,
  updatePhieuGiamGiaKhachHang,
  getPhieuGiamGiaList,
  getEmailSuggestions
} from "../../../services/khuyen-mai";
import { layChiTietKhachHang } from "../../../services/khach-hang";

const route = useRoute();
const router = useRouter();

const id = route.params.id;
const laMoi = !id;

const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");
const formErrors = reactive({});

const phieuOptions = ref([]);
const emailOptions = ref([]);

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
    // Tải danh sách phiếu active
    const dataOpts = await getPhieuGiamGiaList({ pageNo: 0, pageSize: 1000, trangThai: 1 });
    phieuOptions.value = dataOpts?.content || [];

    // Tải danh sách email gợi ý
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
        } catch (e) {
          console.error("Lỗi tải email khách hàng:", e);
        }
      }
    }
  } catch (e) {
    loiTrang.value = e.message || "Không thể tải dữ liệu";
  } finally {
    dangTai.value = false;
  }
}

async function submitForm() {
  resetErrors();
  let isValid = true;

  if (!form.phieuGiamGiaId) {
    formErrors.phieuGiamGiaId = "Vui lòng chọn phiếu giảm giá";
    isValid = false;
  }
  if (!form.email || !form.email.includes('@')) {
    formErrors.email = "Email không hợp lệ";
    isValid = false;
  }

  if (!isValid) return;

  saving.value = true;
  loiTrang.value = "";
  try {
    const payload = {
      phieuGiamGiaId: Number(form.phieuGiamGiaId),
      email: form.email.trim(),
      ngaySuDung: form.ngaySuDung || null,
      trangThai: Number(form.trangThai),
      ngayTao: laMoi ? getToday() : undefined
    };

    if (laMoi) {
      await createPhieuGiamGiaKhachHang(payload);
      alert("Tặng phiếu cho khách hàng thành công");
    } else {
      await updatePhieuGiamGiaKhachHang(id, payload);
      alert("Cập nhật thành công");
    }
    router.push({ name: "admin-phieu-giam-gia", query: { tab: 'khach-hang' } });
  } catch (error) {
    loiTrang.value = error.message || "Lưu thất bại";
  } finally {
    saving.value = false;
  }
}

onMounted(taiDuLieu);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-phieu-giam-gia', query: { tab: 'khach-hang' } })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Tặng phiếu khách hàng" : "Chi tiết phiếu tặng" }}
        </h1>
        <p class="text-sm text-slate-400">Gửi phiếu giảm giá cho khách hàng cụ thể.</p>
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
          <p class="text-sm text-slate-400">Chọn phiếu và khách hàng mục tiêu.</p>
        </div>
      </div>

      <div class="grid gap-6 md:grid-cols-2">
        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Chọn Phiếu giảm giá <span class="text-rose-500">*</span></label>
          <select v-model="form.phieuGiamGiaId" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="">-- Chọn một phiếu --</option>
            <option v-for="opt in phieuOptions" :key="opt.id" :value="String(opt.id)">{{ opt.ten }} ({{ opt.ma }})</option>
          </select>
          <p v-if="formErrors.phieuGiamGiaId" class="text-xs text-rose-500 mt-1">{{ formErrors.phieuGiamGiaId }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Email Khách hàng <span class="text-rose-500">*</span></label>
          <input 
            v-model="form.email" 
            type="email" 
            list="email-suggestions" 
            placeholder="Ví dụ: customer@example.com"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" 
          />
          <datalist id="email-suggestions">
            <option v-for="em in emailOptions" :key="em" :value="em"></option>
          </datalist>
          <p v-if="formErrors.email" class="text-xs text-rose-500 mt-1">{{ formErrors.email }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Ngày sử dụng</label>
          <input v-model="form.ngaySuDung" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span class="text-rose-500">*</span></label>
          <select v-model="form.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="1">Kích hoạt</option>
            <option value="0">Tắt</option>
          </select>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center gap-3 pt-6 border-t border-slate-100">
        <button @click="submitForm" :disabled="saving" class="inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
          <Save class="h-4 w-4" />
          {{ saving ? "Đang lưu..." : "Lưu dữ liệu" }}
        </button>
        <button @click="router.push({ name: 'admin-phieu-giam-gia', query: { tab: 'khach-hang' } })" class="rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100">Hủy</button>
      </div>
    </section>
  </div>
</template>
