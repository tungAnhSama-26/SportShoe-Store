<script setup>
import { onMounted, reactive, ref, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Save, Package, Search, CheckCircle2, CircleX, X } from "lucide-vue-next";
import {
  createDotGiamGiaSanPham,
  getDotGiamGiaSanPhamDetail,
  updateDotGiamGiaSanPham,
  getDotGiamGiaList
} from "../../../services/khuyen-mai";
import { layDanhSachGiay } from "../../../services/san-pham-api";
import { getDisplayErrorMessage, getFieldErrors } from "../../../utils/error-message";
import { showSuccess, showError } from "../../../utils/alert";

const route = useRoute();
const router = useRouter();

const id = route.params.id;
const laMoi = !id;

const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");

const formErrors = reactive({});

const dotOptions = ref([]);
const productSearch = ref("");
const productHints = ref([]);
const loadingProductHints = ref(false);
const selectedGiay = ref(null);

const form = reactive({
  id: null,
  dotGiamGiaId: "",
  giayId: "",
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
    // Tải danh sách đợt giảm giá active
    const dataOpts = await getDotGiamGiaList({ pageNo: 0, pageSize: 1000, trangThai: 1 });
    dotOptions.value = dataOpts?.content || [];

    if (!laMoi) {
      const detail = await getDotGiamGiaSanPhamDetail(id);
      Object.assign(form, {
        id: detail.id,
        dotGiamGiaId: String(detail.dotGiamGiaId ?? ""),
        giayId: String(detail.giayId ?? ""),
        trangThai: String(detail.trangThai ?? 1)
      });
      productSearch.value = detail.tenGiay ?? "";
    }
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải dữ liệu liên kết đợt giảm giá");
  } finally {
    dangTai.value = false;
  }
}

watch(productSearch, async (val) => {
  if (selectedGiay.value && selectedGiay.value.ten === val) {
    productHints.value = [];
    return;
  }
  if ((val || "").trim().length < 2) {
    productHints.value = [];
    return;
  }
  loadingProductHints.value = true;
  try {
    const res = await layDanhSachGiay({ keyword: val.trim(), size: 10 });
    productHints.value = res?.content || res?.items || [];
  } catch (e) {
    productHints.value = [];
  } finally {
    loadingProductHints.value = false;
  }
});

function chonGiay(item) {
  form.giayId = String(item.id);
  selectedGiay.value = item;
  productSearch.value = item.ten;
  productHints.value = [];
}

async function submitForm() {
  resetErrors();
  let isValid = true;

  if (!form.dotGiamGiaId) {
    formErrors.dotGiamGiaId = "Vui lòng chọn đợt giảm giá cần áp dụng";
    isValid = false;
  }
  if (!form.giayId) {
    formErrors.giayId = "Vui lòng chọn sản phẩm cần áp dụng";
    isValid = false;
  }

  if (!isValid) return;

  saving.value = true;
  loiTrang.value = "";
  try {
    const payload = {
      dotGiamGiaId: Number(form.dotGiamGiaId),
      giayId: Number(form.giayId),
      trangThai: Number(form.trangThai),
      ngayTao: laMoi ? getToday() : undefined
    };

    if (laMoi) {
      await createDotGiamGiaSanPham(payload);
      showSuccess("Thêm thành công");
    } else {
      await updateDotGiamGiaSanPham(id, payload);
      showSuccess("Cập nhật thành công");
    }
    setTimeout(() => {
      router.push({ name: "admin-dot-giam-gia-san-pham" });
    }, 1000);
  } catch (error) {
    const fieldErrors = getFieldErrors(error);
    Object.assign(formErrors, fieldErrors);
    if (!Object.keys(fieldErrors).length) {
      showError(getDisplayErrorMessage(error, "Không thể lưu liên kết đợt giảm giá và sản phẩm"), "Lỗi lưu dữ liệu");
    }
  } finally {
    saving.value = false;
  }
}

onMounted(taiDuLieu);
</script>

<template>
  <div class="space-y-5 radius-6px">
    <!-- Header -->
    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-dot-giam-gia-san-pham' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Áp dụng sản phẩm vào đợt" : "Chi tiết liên kết đợt - sản phẩm" }}
        </h1>
      </div>
    </section>

    <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

    <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-amber-50 text-amber-500">
          <Package class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Cấu hình liên kết</h2>
        </div>
      </div>

      <div class="grid gap-6 md:grid-cols-2">
        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Đợt giảm giá <span class="text-rose-500">*</span></label>
          <select v-model="form.dotGiamGiaId" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="">-- Chọn đợt giảm giá --</option>
            <option v-for="item in dotOptions" :key="item.id" :value="String(item.id)">{{ item.ten }} ({{ item.ma }})</option>
          </select>
          <p v-if="formErrors.dotGiamGiaId" class="text-xs text-rose-500 mt-1">{{ formErrors.dotGiamGiaId }}</p>
        </div>

        <div class="space-y-2 relative">
          <label class="text-[13px] font-semibold text-slate-500">Tìm sản phẩm <span class="text-rose-500">*</span></label>
          <div class="relative">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input 
              v-model="productSearch" 
              type="text" 
              placeholder="Nhập tên hoặc mã sản phẩm..." 
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm font-medium text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" 
            />
          </div>
          <p v-if="formErrors.giayId" class="text-xs text-rose-500 mt-1">{{ formErrors.giayId }}</p>
          
          <!-- Dropdown gợi ý -->
          <div v-if="productHints.length" class="absolute z-10 mt-1 flex max-h-60 w-full flex-col gap-1 overflow-y-auto rounded-2xl bg-white p-2 shadow-xl ring-1 ring-slate-200">
            <div 
              v-for="item in productHints" 
              :key="item.id" 
              @click="chonGiay(item)" 
              class="flex items-center justify-between cursor-pointer rounded-xl p-3 hover:bg-slate-50 transition"
            >
              <div>
                <p class="text-sm font-bold text-slate-900">{{ item.ten }}</p>
                <p class="text-xs text-slate-500">{{ item.ma }}</p>
              </div>
              <button class="text-xs font-bold text-rose-500">Chọn</button>
            </div>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span class="text-rose-500">*</span></label>
          <select v-model="form.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
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
        <button @click="router.push({ name: 'admin-dot-giam-gia', query: { tab: 'san-pham' } })" class="rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100">Hủy</button>
      </div>
    </section>
  </div>
</template>
