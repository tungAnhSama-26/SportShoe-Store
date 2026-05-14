<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  ArrowUpRight,
  CheckCircle2,
  CheckSquare,
  CircleX,
  RefreshCcw,
  Save,
  Search,
  Square,
  Tag,
  X,
} from "lucide-vue-next";
import { ArrowLeft, ArrowUpRight, CheckCircle2, CheckSquare, CircleX, RefreshCcw, Save, Search, Square, Tag, X } from "lucide-vue-next";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import {
  createDotGiamGia,
  getDotGiamGiaDetail,
  updateDotGiamGia,
  getDotGiamGiaSanPhamList,
  syncDotGiamGiaSanPham,
} from "../../../services/khuyen-mai";
import {
  chiTietGiay,
  layDanhSachGiay,
  layBienThe,
} from "../../../services/san-pham-api";
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
  if (toast.value.loai === "success")
    return "border-emerald-100 bg-emerald-50 text-emerald-700";
  if (toast.value.loai === "warning")
    return "border-amber-100 bg-amber-50 text-amber-700";
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
const formErrors = reactive({});

const form = reactive({
  id: null,
  ma: "",
  ten: "",
  moTa: "",
  loaiGiam: "1",
  giaTriGiam: "",
  ngayBatDau: "",
  ngayKetThuc: "",
  kichHoat: "1",
});

const isReadOnly = computed(
  () => !laMoi && (Number(form.kichHoat) === 0 || Number(form.kichHoat) === 2),
);

const searchSP = ref("");
const danhSachSP = ref([]);
const selectedVariants = ref([]);
const blockedVariantIds = ref(new Set());
const trangBienThe = ref(1);
const soHangMoiTrang = ref(5);
const pageSizeOptions = [5, 10, 20, 50, 100];

const tatCaBienThe = computed(() => {
  const result = [];
  for (const sp of danhSachSP.value) {
    for (const bt of sp.bienThes || []) {
      result.push({ ...bt, _sp: sp });
    }
  }
  return result.sort((a, b) =>
    (a._sp?.ten || a.tenSanPham || '').localeCompare(b._sp?.ten || b.tenSanPham || '', 'vi')
  );
});

const tongSoTrang = computed(() => Math.max(1, Math.ceil(tatCaBienThe.value.length / soHangMoiTrang.value)));

const bienTheTrang = computed(() => {
  const start = (trangBienThe.value - 1) * soHangMoiTrang.value;
  return tatCaBienThe.value.slice(start, start + soHangMoiTrang.value);
});

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value || 0);
}

function resolveProductImage(product) {
  if (product?.hinhAnh) {
    return product.hinhAnh;
  }

  if (Array.isArray(product?.hinhAnhs) && product.hinhAnhs.length) {
    return (
      product.hinhAnhs.find((item) => item?.laHinhChinh)?.url ||
      product.hinhAnhs[0]?.url ||
      ""
    );
  }

  return "";
}

function normalizeVariantForSelection(variant, product = null) {
  const variantId =
    variant?.giayChiTietId ??
    variant?.idChiTietSanPham ??
    variant?.chiTietId ??
    variant?.id ??
    null;
  return {
    ...variant,
    linkId:
      variant?.giayChiTietId != null
        ? (variant?.id ?? null)
        : (variant?.linkId ?? null),
    id: variantId,
    giayChiTietId: variantId,
    maSanPham: variant?.maSanPham || product?.ma || "",
    tenSanPham: variant?.tenSanPham || variant?.tenGiay || product?.ten || "",
    maBienThe:
      variant?.maBienThe ||
      variant?.maChiTietSanPham ||
      variant?.sku ||
      variant?.ma ||
      "",
    giaBan: Number(variant?.giaBan ?? variant?.gia ?? 0),
    giaGoc: Number(variant?.giaGoc ?? variant?.giaBan ?? variant?.gia ?? 0),
    mauSac: variant?.mauSac || variant?.tenMauSac || "",
    kichCo: variant?.kichCo || variant?.tenKichCo || "",
    thuongHieu: variant?.thuongHieu || product?.thuongHieu || "",
    hinhAnh: variant?.hinhAnh || resolveProductImage(product),
    giayId: variant?.giayId || variant?.idGiay || product?.id || null,
    sku:
      variant?.sku ||
      variant?.maBienThe ||
      variant?.maChiTietSanPham ||
      variant?.ma ||
      "",
    soLuong: Number(variant?.soLuong ?? variant?.soLuongTon ?? 0),
  };
}

function hopNhatBienThe(existing, incoming) {
  return {
    ...existing,
    ...incoming,
    linkId: incoming?.linkId ?? existing?.linkId ?? null,
    id: incoming?.id ?? existing?.id ?? null,
    giayChiTietId:
      incoming?.giayChiTietId ??
      existing?.giayChiTietId ??
      incoming?.id ??
      existing?.id ??
      null,
    giayId: incoming?.giayId ?? existing?.giayId ?? null,
    maSanPham: incoming?.maSanPham || existing?.maSanPham || "",
    tenSanPham: incoming?.tenSanPham || existing?.tenSanPham || "",
    maBienThe: incoming?.maBienThe || existing?.maBienThe || "",
    giaBan: Number(incoming?.giaBan ?? existing?.giaBan ?? 0),
    giaGoc: Number(
      incoming?.giaGoc ??
        existing?.giaGoc ??
        incoming?.giaBan ??
        existing?.giaBan ??
        0,
    ),
    mauSac: incoming?.mauSac || existing?.mauSac || "",
    kichCo: incoming?.kichCo || existing?.kichCo || "",
    thuongHieu: incoming?.thuongHieu || existing?.thuongHieu || "",
    hinhAnh: incoming?.hinhAnh || existing?.hinhAnh || "",
    sku: incoming?.sku || existing?.sku || "",
    soLuong: Number(incoming?.soLuong ?? existing?.soLuong ?? 0),
  };
}

function dedupeSelectedVariants(items) {
  const selectedMap = new Map();

  for (const item of items) {
    const normalized = normalizeVariantForSelection(item);
    const variantId = Number(normalized.id);
    if (!Number.isInteger(variantId) || variantId <= 0) {
      continue;
    }

    const current = selectedMap.get(variantId);
    selectedMap.set(
      variantId,
      current ? hopNhatBienThe(current, normalized) : normalized,
    );
  }

  return Array.from(selectedMap.values());
}

function dongBoBienTheDaChonTheoDanhSachSanPham(products) {
  if (!selectedVariants.value.length) {
    return;
  }

  const variantLookup = new Map();
  for (const product of products) {
    for (const variant of product.bienThes || []) {
      const normalized = normalizeVariantForSelection(variant, product);
      const variantId = Number(normalized.id);
      if (Number.isInteger(variantId) && variantId > 0) {
        variantLookup.set(variantId, normalized);
      }
    }
  }

  selectedVariants.value = dedupeSelectedVariants(
    selectedVariants.value.map((variant) => {
      const variantId = Number(variant?.id);
      return variantLookup.get(variantId) ?? variant;
    }),
  );
}

async function taiSanPhamDaChonConThieu(items) {
  if (!selectedVariants.value.length || searchSP.value.trim()) {
    return items;
  }

  const existingProductIds = new Set(
    items
      .map((item) => Number(item?.id))
      .filter((productId) => Number.isInteger(productId) && productId > 0),
  );

  const missingProductIds = Array.from(
    new Set(
      selectedVariants.value
        .map((variant) => Number(variant?.giayId))
        .filter(
          (productId) =>
            Number.isInteger(productId) &&
            productId > 0 &&
            !existingProductIds.has(productId),
        ),
    ),
  );

  if (!missingProductIds.length) {
    return items;
  }

  const extraProducts = (
    await Promise.all(
      missingProductIds.map(async (productId) => {
        try {
          const [productDetail, variants] = await Promise.all([
            chiTietGiay(productId),
            layBienThe(productId),
          ]);

          return {
            id: productId,
            ten: productDetail?.ten || "",
            ma: productDetail?.ma || "",
            thuongHieu: productDetail?.thuongHieu || "",
            hinhAnh: resolveProductImage(productDetail),
            bienThes: (variants || []).map((variant) =>
              normalizeVariantForSelection(variant, productDetail),
            ),
          };
        } catch (error) {
          console.error("Khong the tai san pham da chon:", error);
          return null;
        }
      }),
    )
  ).filter(Boolean);

  return [...extraProducts, ...items];
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
    const res = await layDanhSachGiay({
      keyword: searchSP.value,
      page: 0,
      size: 50,
    });
    const items = [...(res?.content || res?.items || [])];

    // Load variants for each product to allow selection
    for (const item of items) {
      if (!item.bienThes) {
        try {
          const btRes = await layBienThe(item.id);
          item.bienThes = (btRes || []).map((bt) =>
            normalizeVariantForSelection(bt, item),
          );
        } catch (err) {
          item.bienThes = [];
        }
      }
    }
    const mergedItems = await taiSanPhamDaChonConThieu(items);
    danhSachSP.value = mergedItems;
    dongBoBienTheDaChonTheoDanhSachSanPham(mergedItems);
  } catch (e) {
    console.error("Lỗi tải sản phẩm:", e);
  } finally {
    dangTaiSP.value = false;
  }
}

let searchTimer;
watch(searchSP, () => {
  clearTimeout(searchTimer);
  trangBienThe.value = 1;
  searchTimer = setTimeout(taiDanhSachSP, 400);
});

watch(
  () => form.giaTriGiam,
  (newVal) => {
    if (newVal === "" || newVal === null || newVal === undefined) {
      delete formErrors.giaTriGiam;
      return;
    }
    const val = Number(newVal);
    if (val <= 0) {
      formErrors.giaTriGiam = "Giá trị giảm phải lớn hơn 0%";
    } else if (val > 100) {
      formErrors.giaTriGiam = "Phần trăm giảm không được vượt quá 100%";
    } else {
      delete formErrors.giaTriGiam;
    }
  },
);

function isVariantSelected(variantId) {
  return selectedVariants.value.some((v) => Number(v.id) === Number(variantId));
}

function isVariantBlocked(variantId) {
  return blockedVariantIds.value.has(Number(variantId));
}

const tatCaCoTheChon = computed(() =>
  tatCaBienThe.value.filter(bt => !isVariantBlocked(bt.id))
);

const tatCaDaChon = computed(() =>
  tatCaCoTheChon.value.length > 0 &&
  tatCaCoTheChon.value.every(bt => isVariantSelected(bt.id))
);

const motSoDaChon = computed(() =>
  !tatCaDaChon.value &&
  tatCaCoTheChon.value.some(bt => isVariantSelected(bt.id))
);

function toggleChonTatCa() {
  if (tatCaDaChon.value) {
    tatCaCoTheChon.value.forEach(bt => removeSelectedVariant(bt.id));
  } else {
    tatCaCoTheChon.value.forEach(bt => {
      if (!isVariantSelected(bt.id)) toggleVariant(bt, bt._sp);
    });
  }
}

function toggleVariant(variant, product) {
  const normalized = normalizeVariantForSelection(variant, product);
  const index = selectedVariants.value.findIndex(
    (v) => Number(v.id) === Number(normalized.id),
  );
  if (index === -1) {
    selectedVariants.value = dedupeSelectedVariants([
      ...selectedVariants.value,
      normalized,
    ]);
  } else {
    selectedVariants.value.splice(index, 1);
    selectedVariants.value = dedupeSelectedVariants(selectedVariants.value);
  }
}

function removeSelectedVariant(variantId) {
  selectedVariants.value = dedupeSelectedVariants(
    selectedVariants.value.filter((v) => Number(v.id) !== Number(variantId)),
  );
}

const expandedProducts = ref(new Set());

function toggleProductExpansion(productId) {
  if (expandedProducts.value.has(productId)) {
    expandedProducts.value.delete(productId);
  } else {
    expandedProducts.value.add(productId);
  }
}

async function taiChiTiet() {
  if (laMoi) {
    if (!form.ma) {
      taoMaNgauNhien();
    }
    form.ngayBatDau = getToday();
    // Load blocked variants (đã thuộc đợt khác) ngay cả khi tạo mới
    try {
      const spList = await getDotGiamGiaSanPhamList();
      blockedVariantIds.value = new Set(
        spList
          .map(item => Number(item.giayChiTietId ?? item.id))
          .filter(v => Number.isInteger(v) && v > 0)
      );
    } catch { /* bỏ qua nếu lỗi */ }
    await taiDanhSachSP();
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
      kichHoat: String(detail.kichHoat ?? 1),
    });

    // Load selected variants for this campaign
    const spList = await getDotGiamGiaSanPhamList();
    selectedVariants.value = dedupeSelectedVariants(
      spList
        .filter((item) => String(item.dotGiamGiaId) === String(id))
        .map((item) => normalizeVariantForSelection(item)),
    );

    // Chặn các biến thể đã thuộc đợt giảm giá khác
    blockedVariantIds.value = new Set(
      spList
        .filter(item => String(item.dotGiamGiaId) !== String(id))
        .map(item => Number(item.giayChiTietId ?? item.id))
        .filter(v => Number.isInteger(v) && v > 0)
    );

    await taiDanhSachSP();
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(
      e,
      "Không thể tải chi tiết đợt giảm giá",
    );
  } finally {
    dangTai.value = false;
  }
}

async function submitForm() {
  resetErrors();
  let isValid = true;
  const dangTaoMoi = laMoi;

  if (!form.ma.trim()) {
    formErrors.ma = "Vui lòng nhập mã đợt giảm giá";
    isValid = false;
  }
  if (!form.ten.trim()) {
    formErrors.ten = "Vui lòng nhập tên đợt giảm giá";
    isValid = false;
  }
  if (!form.giaTriGiam || Number(form.giaTriGiam) <= 0) {
    formErrors.giaTriGiam = "Giá trị giảm phải lớn hơn 0%";
    isValid = false;
  } else if (Number(form.giaTriGiam) > 100) {
    formErrors.giaTriGiam = "Phần trăm giảm không được vượt quá 100%";
    isValid = false;
  }
  if (!form.ngayBatDau) {
    formErrors.ngayBatDau = "Vui lòng chọn ngày bắt đầu áp dụng";
    isValid = false;
  } else if (laMoi && form.ngayBatDau < getToday()) {
    formErrors.ngayBatDau = "Ngày bắt đầu không được chọn trong quá khứ";
    isValid = false;
  }

  if (!form.ngayKetThuc) {
    formErrors.ngayKetThuc = "Vui lòng chọn ngày kết thúc áp dụng";
    isValid = false;
  } else if (form.ngayKetThuc < getToday()) {
    formErrors.ngayKetThuc = "Ngày kết thúc không được chọn trong quá khứ";
    isValid = false;
  }

  if (
    form.ngayBatDau &&
    form.ngayKetThuc &&
    form.ngayBatDau > form.ngayKetThuc
  ) {
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
      kichHoat: laMoi ? undefined : form.kichHoat,
      ngayTao: dangTaoMoi ? getToday() : undefined,
      ngayCapNhat: !dangTaoMoi ? getToday() : undefined,
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
      giayChiTietIds: Array.from(
        new Set(
          selectedVariants.value
            .map((variant) => Number(variant?.id))
            .filter(
              (variantId) => Number.isInteger(variantId) && variantId > 0,
            ),
        ),
      ),
    });

    alert(laMoi ? "Thêm đợt giảm giá thành công" : "Cập nhật thành công");
    router.push({ name: "admin-dot-giam-gia" });
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(
      error,
      "Không thể lưu đợt giảm giá",
    );
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
          <div
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full"
            :class="toastIconClass"
          >
            <component :is="ToastIcon" class="h-5 w-5" />
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-bold text-slate-800">{{ toast.tieuDe }}</p>
            <p
              v-if="toast.noiDung"
              class="mt-1 text-sm leading-5 text-slate-600"
            >
              {{ toast.noiDung }}
            </p>
          </div>
          <button
            type="button"
            class="rounded-full p-1 text-slate-400 transition hover:bg-white/70 hover:text-slate-600"
            @click="toast.hienThi = false"
          >
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="h-1.5 w-full" :class="toastAccentClass"></div>
      </div>
    </Transition>

    <!-- Header -->
    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-dot-giam-gia' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
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

    <div
      v-if="loiTrang"
      class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600"
    >
      {{ loiTrang }}
    </div>

    <div
      v-if="isReadOnly"
      class="rounded-2xl border border-amber-100 bg-amber-50 px-5 py-3 text-sm font-medium text-amber-700"
    >
      Đợt giảm giá này đã hết hạn hoặc ngừng hoạt động nên chỉ có thể xem chi
      tiết.
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-12 gap-6">
      <!-- Cột trái: Thông tin đợt giảm -->
      <div class="xl:col-span-4 space-y-6">
        <section
          class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6"
        >
          <fieldset :disabled="isReadOnly" class="space-y-6">
            <div class="flex items-center gap-3">
              <div
                class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500"
              >
                <Tag class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">
                  Thông tin đợt giảm
                </h2>
              </div>
            </div>

            <div class="space-y-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500"
                  >Mã đợt <span class="text-rose-500">*</span></label
                >
                <div class="relative">
                  <input
                    v-model="form.ma"
                    class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white disabled:opacity-70 disabled:bg-slate-100"
                    placeholder="Ví dụ: SUMMER2024"
                  />
                  <button
                    v-if="!isReadOnly"
                    @click="taoMaNgauNhien"
                    type="button"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-rose-500 transition-colors"
                  >
                    <RefreshCcw class="h-4 w-4" />
                  </button>
                </div>
                <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">
                  {{ formErrors.ma }}
                </p>
              </div>

              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500"
                  >Tên đợt <span class="text-rose-500">*</span></label
                >
                <input
                  v-model="form.ten"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                  placeholder="Ví dụ: Siêu giảm giá mùa hè"
                />
                <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">
                  {{ formErrors.ten }}
                </p>
              </div>

              <div class="grid grid-cols-1 gap-4">
                <div class="space-y-2">
                  <label class="text-[13px] font-semibold text-slate-500"
                    >Giá trị giảm (%)
                    <span class="text-rose-500">*</span></label
                  >
                  <div class="relative">
                    <input
                      v-model="form.giaTriGiam"
                      type="number"
                      min="1"
                      max="100"
                      class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-10 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                      placeholder="0"
                    />
                    <span
                      class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold"
                      >%</span
                    >
                  </div>
                  <p
                    v-if="formErrors.giaTriGiam"
                    class="text-xs text-rose-500 mt-1"
                  >
                    {{ formErrors.giaTriGiam }}
                  </p>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="text-[13px] font-semibold text-slate-500"
                    >Từ ngày <span class="text-rose-500">*</span></label
                  >
                  <input
                    v-model="form.ngayBatDau"
                    type="date"
                    class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
                  />
                  <p
                    v-if="formErrors.ngayBatDau"
                    class="text-xs text-rose-500 mt-1"
                  >
                    {{ formErrors.ngayBatDau }}
                  </p>
                </div>
                <div class="space-y-2">
                  <label class="text-[13px] font-semibold text-slate-500"
                    >Đến ngày <span class="text-rose-500">*</span></label
                  >
                  <input
                    v-model="form.ngayKetThuc"
                    :min="form.ngayBatDau || getToday()"
                    type="date"
                    class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
                  />
                  <p
                    v-if="formErrors.ngayKetThuc"
                    class="text-xs text-rose-500 mt-1"
                  >
                    {{ formErrors.ngayKetThuc }}
                  </p>
                </div>
              </div>

              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500"
                  >Mô tả</label
                >
                <textarea
                  v-model="form.moTa"
                  rows="3"
                  class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
                  placeholder="Nhập mô tả..."
                ></textarea>
              </div>
            </div>
          </fieldset>

          <div class="pt-4 flex flex-col gap-3">
            <button
              v-if="!isReadOnly"
              @click="submitForm"
              :disabled="saving"
              class="w-full inline-flex items-center justify-center gap-2 rounded-2xl bg-rose-500 px-6 py-3 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60"
            >
              <Save class="h-4 w-4" />
              {{
                saving
                  ? "Đang lưu..."
                  : laMoi
                    ? "Tạo đợt giảm giá"
                    : "Lưu thay đổi"
              }}
            </button>
            <button
              @click="router.push({ name: 'admin-dot-giam-gia' })"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-6 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-100"
            >
              {{ isReadOnly ? "Quay lại" : "Hủy" }}
            </button>
          </div>
        </section>
      </div>

      <!-- Cột phải: Chọn sản phẩm -->
      <div class="xl:col-span-8 space-y-6">
        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-5">
          <!-- Header -->
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div
                class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600"
              >
                <Search class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Chọn sản phẩm áp dụng</h2>
                <p class="text-[13px] text-slate-400">Đã chọn {{ selectedVariants.length }} biến thể</p>
              </div>
            </div>
          </div>

          <!-- Tìm kiếm -->
          <div class="flex gap-3">
            <div class="relative flex-1">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-rose-500" />
              <input v-model="searchSP" :disabled="isReadOnly" @keyup.enter="taiDanhSachSP" type="text" placeholder="Tìm theo tên hoặc mã sản phẩm..." class="h-11 w-full rounded-2xl border border-rose-100 bg-rose-50/40 pl-11 pr-4 text-sm text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white disabled:opacity-70 disabled:bg-slate-100" />
            </div>
            <button v-if="!isReadOnly" @click="taiDanhSachSP" class="inline-flex h-11 items-center gap-2 rounded-2xl bg-rose-500 px-5 text-sm font-medium text-white transition hover:bg-rose-600">
              <Search class="h-4 w-4" />
              Tìm kiếm
            </button>
          </div>

          <!-- Danh sách biến thể - 1 bảng duy nhất -->
          <div>
            <div v-if="dangTaiSP" class="py-10 text-center text-slate-400">Đang tải sản phẩm...</div>
            <div v-else-if="!tatCaBienThe.length" class="py-10 text-center text-slate-400">Không tìm thấy sản phẩm nào.</div>
            <template v-else>
              <table class="w-full text-sm">
                <thead>
                  <tr class="text-[11px] font-semibold text-slate-400 border-b border-slate-100">
                    <th class="px-3 py-2 text-left w-8">
                      <input
                        type="checkbox"
                        class="h-3.5 w-3.5 accent-rose-500 cursor-pointer"
                        :checked="tatCaDaChon"
                        :indeterminate="motSoDaChon"
                        :disabled="isReadOnly || tatCaCoTheChon.length === 0"
                        @change="toggleChonTatCa"
                      />
                    </th>
                    <th class="px-3 py-2 text-left w-8">STT</th>
                    <th class="px-3 py-2 text-left w-12">Ảnh</th>
                    <th class="px-3 py-2 text-left">Tên sản phẩm</th>
                    <th class="px-3 py-2 text-left">Màu sắc</th>
                    <th class="px-3 py-2 text-left">Kích cỡ</th>
                    <th class="px-3 py-2 text-left">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(bt, idx) in bienTheTrang" :key="bt.id"
                    class="border-b border-slate-50 last:border-0 transition"
                    :class="[
                      isVariantBlocked(bt.id) ? 'opacity-40 pointer-events-none bg-slate-50' :
                      isVariantSelected(bt.id) ? 'bg-rose-50/30' :
                      'hover:bg-slate-50'
                    ]"
                  >
                    <td class="px-3 py-2.5">
                      <button :disabled="isReadOnly || isVariantBlocked(bt.id)" @click="toggleVariant(bt, bt._sp)" class="flex items-center justify-center disabled:cursor-not-allowed">
                        <CheckSquare v-if="isVariantSelected(bt.id)" class="h-4 w-4 text-rose-500" />
                        <Square v-else class="h-4 w-4 text-slate-300" />
                      </button>
                    </td>
                    <td class="px-3 py-2.5 text-slate-400 text-xs">{{ (trangBienThe - 1) * soHangMoiTrang + idx + 1 }}</td>
                    <td class="px-3 py-2.5">
                      <div class="h-9 w-9 rounded-lg bg-slate-100 overflow-hidden border border-slate-100">
                        <img v-if="bt.hinhAnh || bt._sp?.hinhAnh" :src="bt.hinhAnh || bt._sp?.hinhAnh" class="h-full w-full object-cover" />
                      </div>
                    </td>
                    <td class="px-3 py-2.5 text-slate-600">{{ bt._sp?.ten || bt.tenSanPham }}</td>
                    <td class="px-3 py-2.5 text-slate-600">{{ bt.mauSac }}</td>
                    <td class="px-3 py-2.5 text-slate-600">{{ bt.kichCo }}</td>
                    <td class="px-3 py-2.5">
                      <span class="inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-medium"
                        :class="bt.soLuong > 0 ? 'bg-emerald-50 text-emerald-600 border border-emerald-100' : 'bg-slate-100 text-slate-400'">
                        {{ bt.soLuong > 0 ? 'Còn hàng' : 'Hết hàng' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- Phân trang -->
              <AdminTableFooter
                :current-page="trangBienThe"
                :page-size="soHangMoiTrang"
                :page-size-options="pageSizeOptions"
                :total-items="tatCaBienThe.length"
                :total-pages="tongSoTrang"
                compact
                @update:current-page="trangBienThe = $event"
                @update:page-size="soHangMoiTrang = $event; trangBienThe = 1"
              />
            </template>
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
