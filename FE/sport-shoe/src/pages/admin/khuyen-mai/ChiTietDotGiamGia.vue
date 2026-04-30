<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, ArrowUpRight, CheckCircle2, CheckSquare, CircleX, RefreshCcw, Save, Search, Square, Tag, X } from "lucide-vue-next";
import {
  createDotGiamGia,
  getDotGiamGiaDetail,
  updateDotGiamGia,
  getDotGiamGiaSanPhamList,
  syncDotGiamGiaSanPham
} from "../../../services/khuyen-mai";
import { layDanhSachGiay, layBienThe } from "../../../services/san-pham-api";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const route = useRoute();
const router = useRouter();

const id = route.params.id;
const laMoi = !id;

const dangTai = ref(false);
const dangTaiSP = ref(false);
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
  id: null, ma: "", ten: "", moTa: "", loaiGiam: "1", giaTriGiam: "",
  ngayBatDau: "", ngayKetThuc: "", kichHoat: "1"
});

const searchSP = ref("");
const danhSachSP = ref([]);
const selectedVariants = ref([]);

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value || 0);
}

function normalizeVariantForSelection(variant, product = null) {
  return {
    ...variant,
    id: variant?.id ?? variant?.idChiTietSanPham ?? null,
    tenSanPham: variant?.tenSanPham || variant?.tenGiay || product?.ten || "",
    maBienThe: variant?.maBienThe || variant?.maChiTietSanPham || variant?.sku || variant?.ma || "",
    giaBan: Number(variant?.giaBan ?? variant?.gia ?? 0),
    giaGoc: Number(variant?.giaGoc ?? variant?.giaBan ?? variant?.gia ?? 0),
    mauSac: variant?.mauSac || variant?.tenMauSac || "",
    kichCo: variant?.kichCo || variant?.tenKichCo || "",
    hinhAnh: variant?.hinhAnh || product?.hinhAnh || "",
    giayId: variant?.giayId || variant?.idGiay || product?.id || null,
    sku: variant?.sku || variant?.maBienThe || variant?.maChiTietSanPham || variant?.ma || "",
  };
}

function tinhGiaGiam(giaGoc) {
  const giam = Number(form.giaTriGiam) || 0;
  return giaGoc * (1 - giam / 100);
}

function taoMaNgauNhien() {
  form.ma = "DGG" + Math.random().toString(36).substring(2, 8).toUpperCase();
}

async function taiDanhSachSP() {
  dangTaiSP.value = true;
  try {
    const res = await layDanhSachGiay({ keyword: searchSP.value, page: 0, size: 50 });
    const items = res?.content || res?.items || [];
    
    // Load variants for each product to allow selection
    for (const item of items) {
        if (!item.bienThes) {
            try {
                const btRes = await layBienThe(item.id);
                item.bienThes = (btRes || []).map((bt) => normalizeVariantForSelection(bt, item));
            } catch (err) {
                item.bienThes = [];
            }
        }
    }
    danhSachSP.value = items;
  } catch (e) {
    console.error("Lỗi tải sản phẩm:", e);
  } finally {
    dangTaiSP.value = false;
  }
}

let searchTimer;
watch(searchSP, () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(taiDanhSachSP, 400);
});

function isVariantSelected(variantId) {
  return selectedVariants.value.some(v => v.id === variantId);
}

function toggleVariant(variant, product) {
  const index = selectedVariants.value.findIndex(v => v.id === variant.id);
  if (index === -1) {
    selectedVariants.value.push(normalizeVariantForSelection(variant, product));
  } else {
    selectedVariants.value.splice(index, 1);
  }
}

function removeSelectedVariant(variantId) {
  selectedVariants.value = selectedVariants.value.filter(v => v.id !== variantId);
}

function moChiTietSanPham(giayId, variantId) {
  router.push({
    name: 'admin-bien-the-san-pham',
    query: {
      giayId: String(giayId),
      chiTietId: String(variantId)
    }
  });
}

async function taiChiTiet() {
  if (laMoi) {
    if (!form.ma) {
      taoMaNgauNhien();
    }
    taiDanhSachSP();
    return;
  }
  dangTai.value = true;
  try {
    const detail = await getDotGiamGiaDetail(id);
    Object.assign(form, {
      id: detail.id,
      ma: detail.ma ?? "",
      ten: detail.ten ?? "",
      moTa: detail.moTa ?? "",
      loaiGiam: String(detail.loaiGiam ?? 1),
      giaTriGiam: detail.giaTriGiam ?? "",
      ngayBatDau: detail.ngayBatDau ?? "",
      ngayKetThuc: detail.ngayKetThuc ?? "",
      kichHoat: String(detail.kichHoat ?? 1)
    });

    // Load selected variants for this campaign
    const spList = await getDotGiamGiaSanPhamList();
    selectedVariants.value = spList
      .filter(item => String(item.dotGiamGiaId) === String(id))
      .map((item) => normalizeVariantForSelection(item));

    taiDanhSachSP();
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải chi tiết đợt giảm giá");
  } finally {
    dangTai.value = false;
  }
}

async function submitForm() {
  resetErrors();
  let isValid = true;
  const dangTaoMoi = laMoi;

  if (!form.ma.trim()) { formErrors.ma = "Vui lòng nhập mã đợt giảm giá"; isValid = false; }
  if (!form.ten.trim()) { formErrors.ten = "Vui lòng nhập tên đợt giảm giá"; isValid = false; }
  if (!form.giaTriGiam || Number(form.giaTriGiam) <= 0) {
    formErrors.giaTriGiam = "Giá trị giảm phải lớn hơn 0%";
    isValid = false;
  } else if (Number(form.giaTriGiam) > 100) {
    formErrors.giaTriGiam = "Phần trăm giảm không được vượt quá 100%";
    isValid = false;
  }
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
      moTa: (form.moTa || "").trim(),
      loaiGiam: Number(form.loaiGiam),
      giaTriGiam: Number(form.giaTriGiam),
      ngayBatDau: form.ngayBatDau,
      ngayKetThuc: form.ngayKetThuc,
      kichHoat: Number(form.kichHoat),
      ngayTao: dangTaoMoi ? getToday() : undefined,
      ngayCapNhat: !dangTaoMoi ? getToday() : undefined
    };

    let campaignId = id;
    if (laMoi) {
      const res = await createDotGiamGia(payload);
      campaignId = res.id;
    } else {
      await updateDotGiamGia(id, payload);
    }

    await syncDotGiamGiaSanPham({
      dotGiamGiaId: Number(campaignId),
      giayChiTietIds: selectedVariants.value
        .map((variant) => Number(variant?.id))
        .filter((variantId) => Number.isInteger(variantId) && variantId > 0)
    });

    alert(laMoi ? "Thêm đợt giảm giá thành công" : "Cập nhật thành công");
    router.push({ name: "admin-dot-giam-gia" });
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể lưu đợt giảm giá");
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
      <button @click="router.push({ name: 'admin-dot-giam-gia' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200">
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div class="flex-1 min-w-0">
        <div>
          <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
            {{ laMoi ? "Thêm đợt giảm giá mới" : "Chi tiết đợt giảm giá" }}
          </h1>
        </div>
      </div>
    </section>

    <div v-if="loiTrang"
      class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-12 gap-6">
      <!-- Cột trái: Thông tin đợt giảm -->
      <div class="xl:col-span-4 space-y-6">
        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
          <div class="flex items-center gap-3">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500">
              <Tag class="h-5 w-5" />
            </div>
            <div>
              <h2 class="text-base font-bold text-slate-800">Thông tin đợt giảm</h2>
            </div>
          </div>

          <div class="space-y-4">
            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Mã đợt <span class="text-rose-500">*</span></label>
              <div class="relative">
                <input v-model="form.ma" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: SUMMER2024" />
                <button @click="taoMaNgauNhien" type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-rose-500 transition-colors">
                  <RefreshCcw class="h-4 w-4" />
                </button>
              </div>
              <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">{{ formErrors.ma }}</p>
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Tên đợt <span class="text-rose-500">*</span></label>
              <input v-model="form.ten" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: Siêu giảm giá mùa hè" />
              <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">{{ formErrors.ten }}</p>
            </div>

            <div class="grid grid-cols-1 gap-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Giá trị giảm (%) <span class="text-rose-500">*</span></label>
                <div class="relative">
                  <input v-model="form.giaTriGiam" type="number" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-10 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" placeholder="0" />
                  <span class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold">%</span>
                </div>
                <p v-if="formErrors.giaTriGiam" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTriGiam }}</p>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Từ ngày <span class="text-rose-500">*</span></label>
                <input v-model="form.ngayBatDau" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
                <p v-if="formErrors.ngayBatDau" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayBatDau }}</p>
              </div>
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Đến ngày <span class="text-rose-500">*</span></label>
                <input v-model="form.ngayKetThuc" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
                <p v-if="formErrors.ngayKetThuc" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayKetThuc }}</p>
              </div>
            </div>

            <div v-if="!laMoi" class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span class="text-rose-500">*</span></label>
              <select v-model="form.kichHoat" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
                <option value="1">Kích hoạt</option>
                <option value="0">Tắt</option>
              </select>
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Mô tả</label>
              <textarea v-model="form.moTa" rows="3" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" placeholder="Nhập mô tả..."></textarea>
            </div>
          </div>

          <div class="pt-4 flex flex-col gap-3">
            <button @click="submitForm" :disabled="saving" class="w-full inline-flex items-center justify-center gap-2 rounded-2xl bg-rose-500 px-6 py-3 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
              <Save class="h-4 w-4" />
              {{ saving ? "Đang lưu..." : (laMoi ? "Tạo đợt giảm giá" : "Lưu thay đổi") }}
            </button>
            <button @click="router.push({ name: 'admin-dot-giam-gia' })" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-6 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-100">Hủy</button>
          </div>
        </section>
      </div>

      <!-- Cột phải: Chọn sản phẩm -->
      <div class="xl:col-span-8 space-y-6">
        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
                <Search class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Chọn sản phẩm áp dụng</h2>
              </div>
            </div>
          </div>

          <div class="mb-4 flex gap-3">
            <div class="relative flex-1">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-rose-500" />
              <input v-model="searchSP" @keyup.enter="taiDanhSachSP" type="text" placeholder="Tìm theo tên hoặc mã sản phẩm..." class="h-11 w-full rounded-2xl border border-rose-100 bg-rose-50/40 pl-11 pr-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" />
            </div>
            <button @click="taiDanhSachSP" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-medium text-white shadow-[0_12px_24px_rgba(244,63,94,0.22)] transition hover:bg-rose-600">
              <Search class="h-4 w-4" />
              Tìm kiếm
            </button>
          </div>

          <div class="max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
            <div v-if="dangTaiSP" class="py-10 text-center text-slate-400">Đang tải sản phẩm...</div>
            <div v-else-if="!danhSachSP.length" class="py-10 text-center text-slate-400">Không tìm thấy sản phẩm nào.</div>
            <div v-else class="space-y-4">
              <div v-for="sp in danhSachSP" :key="sp.id" class="rounded-2xl border border-slate-100 p-4 transition hover:border-rose-100 hover:bg-rose-50/10">
                <div class="flex items-center gap-4">
                  <div class="h-12 w-12 rounded-xl bg-slate-50 border border-slate-100 overflow-hidden">
                    <img v-if="sp.hinhAnh" :src="sp.hinhAnh" class="h-full w-full object-cover" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="font-normal text-slate-800 truncate">{{ sp.ten }}</p>
                    <p class="text-xs text-slate-400">{{ sp.ma }} • {{ sp.thuongHieu }}</p>
                  </div>
                </div>

                <div class="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-2">
                  <div v-for="bt in sp.bienThes" :key="bt.id" class="flex items-center justify-between rounded-xl bg-white p-3 border border-slate-50 shadow-sm transition hover:shadow-md">
                    <div class="flex items-center gap-3">
                      <button @click="toggleVariant(bt, sp)" class="h-6 w-6 flex items-center justify-center transition" :title="isVariantSelected(bt.id) ? 'Bỏ chọn' : 'Chọn'">
                        <CheckSquare v-if="isVariantSelected(bt.id)" class="h-5 w-5 text-rose-500" />
                        <Square v-else class="h-5 w-5 text-slate-300" />
                      </button>
                      <div class="text-[13px] font-normal text-slate-700">
                        Màu: {{ bt.mauSac }} | Kích cỡ: {{ bt.kichCo }}
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-[13px] font-normal text-slate-900">{{ formatCurrency(bt.giaBan) }}</p>
                      <p class="text-[10px] text-slate-400">Kho: {{ bt.soLuong }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Section: Danh sách sản phẩm được chọn (Dưới cùng) -->
        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-500">
                <CheckSquare class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Sản phẩm đã chọn ({{ selectedVariants.length }})</h2>
              </div>
            </div>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-sm text-left border-separate border-spacing-y-2">
              <thead>
                <tr class="bg-slate-50 text-[11px] font-semibold tracking-wider text-slate-500 rounded-xl">
                  <th class="px-4 py-3 first:rounded-l-xl">Ảnh</th>
                  <th class="px-4 py-3">Tên sản phẩm / SKU</th>
                  <th class="px-4 py-3">Giá bán</th>
                  <th class="px-4 py-3">Màu sắc / Kích cỡ</th>
                  <th class="px-4 py-3 last:rounded-r-xl text-center">Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!selectedVariants.length">
                  <td colspan="5" class="py-10 text-center text-slate-400 italic">Chưa có sản phẩm nào được chọn.</td>
                </tr>
                <tr v-for="v in selectedVariants" :key="v.id" class="bg-white ring-1 ring-slate-100 shadow-sm rounded-2xl transition hover:ring-rose-200">
                  <td class="px-4 py-3 first:rounded-l-2xl">
                    <div class="h-10 w-10 rounded-lg bg-slate-50 border border-slate-100 overflow-hidden">
                      <img v-if="v.hinhAnh" :src="v.hinhAnh" class="h-full w-full object-cover" />
                    </div>
                  </td>
                  <td class="px-4 py-3">
                    <p class="font-normal text-slate-800">{{ v.tenSanPham }}</p>
                    <p class="text-[10px] text-slate-400">{{ v.sku || v.maBienThe }}</p>
                  </td>
                  <td class="px-4 py-3 font-normal">
                    <div v-if="Number(form.giaTriGiam) > 0">
                      <span class="text-rose-600">{{ formatCurrency(tinhGiaGiam(v.giaGoc || v.giaBan)) }}</span>
                      <span class="text-[10px] text-slate-400 line-through block font-normal">{{ formatCurrency(v.giaGoc || v.giaBan) }}</span>
                    </div>
                    <div v-else>{{ formatCurrency(v.giaBan) }}</div>
                  </td>
                  <td class="px-4 py-3">
                    <span class="rounded bg-slate-100 px-2 py-0.5 text-[10px] font-normal text-slate-600">
                      {{ v.mauSac || "Chưa có màu" }} / {{ v.kichCo || "Chưa có kích cỡ" }}
                    </span>
                  </td>
                  <td class="px-4 py-3 last:rounded-r-2xl text-center">
                    <div class="flex items-center justify-center gap-2">
                      <button type="button" class="h-8 w-8 inline-flex items-center justify-center rounded-lg text-slate-400 hover:bg-slate-100 hover:text-rose-500" @click="moChiTietSanPham(v.giayId, v.id)">
                        <ArrowUpRight class="h-4 w-4" />
                      </button>
                      <button @click="removeSelectedVariant(v.id)" class="h-8 w-8 inline-flex items-center justify-center rounded-lg text-slate-400 hover:bg-rose-50 hover:text-rose-500 transition">
                        <X class="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}
table {
  border-collapse: separate;
  border-spacing: 0 8px;
}
</style>
