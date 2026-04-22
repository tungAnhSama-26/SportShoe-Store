<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  createDotGiamGia,
  getDotGiamGiaDetail,
  updateDotGiamGia,
  getDotGiamGiaSanPhamList,
  createDotGiamGiaSanPham,
  deleteDotGiamGiaSanPham,
  syncDotGiamGiaSanPham
} from "../../../services/khuyen-mai";
import { layDanhSachGiay, layBienThe } from "../../../services/san-pham-api";
import {
  ArrowLeft, Save, Tag, Search, Plus, Minus,
  ChevronRight, ChevronDown, CheckSquare, Square, Package, X, RefreshCcw,
  CheckCircle2, CircleX
} from "lucide-vue-next";
import { computed } from "vue";

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
  id: null, ma: "", ten: "", moTa: "", loaiGiam: "1", giaTriGiam: "",
  ngayBatDau: "", ngayKetThuc: "", kichHoat: "1"
});

// Product Selection State
const searchSP = ref("");
const danhSachSP = ref([]);
const dangTaiSP = ref(false);
const expandedProducts = ref(new Set());
const variantCache = ref({}); // { productId: [variants] }
const selectedVariants = ref([]); // List of full variant objects
const loadingVariants = ref(new Set());

// Filters
const searchBrand = ref("");
const searchCate = ref("");

async function taiDanhSachSP() {
  dangTaiSP.value = true;
  try {
    const [res, applied] = await Promise.all([
      layDanhSachGiay({
        keyword: searchSP.value,
        size: 100, // Tăng kích thước lấy về
        trangThai: 1
      }),
      getDotGiamGiaSanPhamList()
    ]);

    // Lấy danh sách ID các biến thể đang tham gia đợt giảm giá khác (đang kích hoạt)
    const activeVariantIds = new Set(
      (applied || [])
        .filter(a => a.trangThai === 1 && a.dotGiamGiaId !== Number(id))
        .map(a => a.giayChiTietId)
    );

    // Kiểm tra cả res.items (từ san-pham-api) và res.content (mặc định Spring Page)
    const allProducts = res.items || res.content || res || [];
    
    // Lưu tạm danh sách SP
    danhSachSP.value = allProducts;
    
    // Nếu lọc xong mà trống, nhưng danh sách gốc có dữ liệu -> Báo cho người dùng biết
    if (danhSachSP.value.length === 0 && allProducts.length > 0) {
      console.warn("Tất cả sản phẩm tìm thấy đều đã có đợt giảm giá khác.");
    }
  } catch (e) {
    console.error("Lỗi tải sp:", e);
  } finally {
    dangTaiSP.value = false;
  }
}

async function toggleExpand(product) {
  if (expandedProducts.value.has(product.id)) {
    expandedProducts.value.delete(product.id);
  } else {
    expandedProducts.value.add(product.id);
    loadingVariants.value.add(product.id);
    try {
      // 1. Tải đồng thời biến thể và danh sách áp dụng mới nhất
      const [variants, allApplied] = await Promise.all([
        layBienThe(product.id),
        getDotGiamGiaSanPhamList()
      ]);
      
      const currentId = Number(route.params.id);
      
      // 2. Cập nhật cache với thông tin chiến dịch chồng chéo
      variantCache.value[product.id] = (variants || []).map(v => {
        // Tìm xem biến thể này có đang ở đợt giảm giá KHÁC không
        const otherLink = (allApplied || []).find(a => 
          a.giayChiTietId === v.id && 
          a.dotGiamGiaId !== currentId
        );
        
        return {
          ...v,
          giayId: product.id,
          tenSanPham: product.ten,
          thuongHieu: product.thuongHieu,
          loaiGiay: product.loaiGiay,
          hinhAnh: v.hinhAnh || product.hinhAnh,
          activeCampaign: otherLink ? (otherLink.maDotGiamGia || otherLink.tenDotGiamGia || `Đợt #${otherLink.dotGiamGiaId}`) : null
        };
      });
    } catch (e) {
      console.error("Lỗi khi mở rộng sản phẩm:", e);
    } finally {
      loadingVariants.value.delete(product.id);
    }
  }
}

function isVariantSelected(variantId) {
  return selectedVariants.value.some(v => v.id === variantId);
}

function isProductSelected(productId) {
  const variants = variantCache.value[productId];
  if (!variants || !variants.length) return false;
  const selectable = variants.filter(v => !v.activeCampaign);
  if (selectable.length === 0) return false;
  return selectable.every(v => isVariantSelected(v.id));
}

function toggleVariant(variant) {
  if (variant.activeCampaign) return;
  const idx = selectedVariants.value.findIndex(v => v.id === variant.id);
  if (idx > -1) {
    selectedVariants.value.splice(idx, 1);
  } else {
    selectedVariants.value.push(variant);
  }
}

async function toggleProduct(product) {
  if (!variantCache.value[product.id]) {
    await toggleExpand(product); // Tải biến thể nếu chưa có
  }
  const variants = variantCache.value[product.id] || [];
  const allSelected = isProductSelected(product.id);

  if (allSelected) {
    // Bỏ chọn tất cả biến thể của sp này
    const variantIds = variants.map(v => v.id);
    selectedVariants.value = selectedVariants.value.filter(v => !variantIds.includes(v.id));
  } else {
    // Chọn tất cả biến thể chưa được chọn (chỉ những cái có thể chọn)
    variants.forEach(v => {
      if (!v.activeCampaign && !isVariantSelected(v.id)) {
        selectedVariants.value.push(v);
      }
    });
  }
}

function removeSelectedVariant(variantId) {
  selectedVariants.value = selectedVariants.value.filter(v => v.id !== variantId);
}

function formatCurrency(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

function tinhGiaGiam(giaGoc) {
  const phanTram = Number(form.giaTriGiam) || 0;
  if (phanTram <= 0) return giaGoc;
  return giaGoc * (1 - phanTram / 100);
}

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

function taoMaNgauNhien() {
  form.ma = 'DGG' + Math.random().toString(36).substring(2, 10).toUpperCase();
}

// Track original associations for sync
let originalGiayIds = new Set();

async function taiChiTiet() {
  if (laMoi) {
    taoMaNgauNhien();
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

    // Tải danh sách biến thể đã áp dụng
    const applied = await getDotGiamGiaSanPhamList();
    const myApplied = (applied || []).filter(a => a.dotGiamGiaId === Number(id));

    for (const item of myApplied) {
      const gId = item.giayId;
      if (!gId) {
        console.warn("Thiếu giayId cho liên kết:", item);
        continue;
      }

      if (!variantCache.value[gId]) {
        try {
          const variants = await layBienThe(gId);
          variantCache.value[gId] = (variants || []).map(v => ({
            ...v,
            giayId: gId,
            tenSanPham: item.tenGiay || "Sản phẩm",
            idLienKet: myApplied.find(ma => ma.giayChiTietId === v.id)?.id
          }));
        } catch (e) {
          console.error(`Lỗi tải biến thể cho giày #${gId}:`, e);
        }
      }
      
      // Tìm biến thể cụ thể đã được gán
      const variantsOfShoe = variantCache.value[gId] || [];
      const targetVariant = variantsOfShoe.find(v => v.id === item.giayChiTietId);
      
      if (targetVariant) {
        if (!isVariantSelected(targetVariant.id)) {
          selectedVariants.value.push({
            ...targetVariant,
            idLienKet: item.id
          });
        }
      } else {
        // Fallback: Nếu không tìm thấy trong cache (có thể do API layBienThe chưa kịp trả về hoặc dữ liệu lệch)
        // Ta vẫn thêm vào selectedVariants để hiển thị được tên
        if (!isVariantSelected(item.giayChiTietId)) {
          selectedVariants.value.push({
            id: item.giayChiTietId,
            giayId: gId,
            tenSanPham: item.tenGiay,
            mauSac: item.mauSac,
            kichCo: item.kichCo,
            giaBan: 0, // Sẽ được cập nhật khi cache tải xong hoặc từ item nếu có
            idLienKet: item.id,
            maBienThe: "Đang tải..."
          });
        }
      }
    }

    taiDanhSachSP();
  } catch (e) {
    loiTrang.value = e.message || "Không thể tải chi tiết đợt giảm giá";
  } finally {
    dangTai.value = false;
  }
}

async function submitForm() {
  resetErrors();
  let isValid = true;

  if (!form.ma.trim()) { formErrors.ma = "Mã đợt không được để trống"; isValid = false; }
  if (!form.ten.trim()) { formErrors.ten = "Tên đợt không được để trống"; isValid = false; }
  if (!form.giaTriGiam || Number(form.giaTriGiam) <= 0) {
    formErrors.giaTriGiam = "Giá trị giảm không hợp lệ";
    isValid = false;
  } else if (Number(form.giaTriGiam) > 100) {
    formErrors.giaTriGiam = "Phần trăm giảm không được vượt quá 100%";
    isValid = false;
  }
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
      moTa: (form.moTa || "").trim(),
      loaiGiam: Number(form.loaiGiam),
      giaTriGiam: Number(form.giaTriGiam),
      ngayBatDau: form.ngayBatDau,
      ngayKetThuc: form.ngayKetThuc,
      kichHoat: Number(form.kichHoat),
      ngayTao: laMoi ? getToday() : undefined,
      ngayCapNhat: !laMoi ? getToday() : undefined
    };

    let savedId = id;
    if (laMoi) {
      const res = await createDotGiamGia(payload);
      if (!res || !res.id) throw new Error("Không nhận được phản hồi từ máy chủ.");
      savedId = res.id;
      
      // Quan trọng: Chuyển sang chế độ cập nhật ngay lập tức
      id = savedId;
      laMoi = false;
      // Cập nhật URL mà không reload trang (tùy chọn, để nếu nhấn F5 vẫn ở trang chi tiết)
      try { router.replace({ name: 'admin-dot-giam-gia-chi-tiet', params: { id: savedId } }); } catch(e) {}
    } else {
      await updateDotGiamGia(id, payload);
    }

    const currentVariantIds = selectedVariants.value.map(v => v.id);
    await syncDotGiamGiaSanPham({
      dotGiamGiaId: Number(savedId),
      giayChiTietIds: currentVariantIds
    });

    hienThiThongBao("success", laMoi ? "Thêm đợt giảm giá thành công" : "Cập nhật thành công");
    setTimeout(() => { router.push({ name: "admin-dot-giam-gia" }); }, 1000);
  } catch (error) {
    hienThiThongBao("error", "Lỗi lưu dữ liệu", error.message);
    if (error.errors) Object.assign(formErrors, error.errors);
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
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm đợt giảm giá mới" : "Chi tiết đợt giảm giá" }}
        </h1>
        <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin đợt giảm giá mới vào form bên dưới." : `Mã:
          ${form.ma || '...'}` }}</p>
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
              <p class="text-sm text-slate-400">Cấu hình chương trình.</p>
            </div>
          </div>

          <div class="space-y-4">
            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Mã đợt <span
                  class="text-rose-500">*</span></label>
              <div class="relative">
                <input v-model="form.ma"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                  placeholder="Ví dụ: SUMMER2024" />
                <button @click="taoMaNgauNhien" type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-rose-500 transition-colors">
                  <RefreshCcw class="h-4 w-4" />
                </button>
              </div>
              <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">{{ formErrors.ma }}</p>
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Tên đợt <span
                  class="text-rose-500">*</span></label>
              <input v-model="form.ten"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                placeholder="Ví dụ: Siêu giảm giá mùa hè" />
              <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">{{ formErrors.ten }}</p>
            </div>

            <div class="grid grid-cols-1 gap-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Giá trị giảm (%) <span
                    class="text-rose-500">*</span></label>
                <div class="relative">
                  <input v-model="form.giaTriGiam" type="number"
                    class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-10 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                    placeholder="0" />
                  <span class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold">%</span>
                </div>
                <p v-if="formErrors.giaTriGiam" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTriGiam }}</p>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Từ ngày <span
                    class="text-rose-500">*</span></label>
                <input v-model="form.ngayBatDau" type="date"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
                <p v-if="formErrors.ngayBatDau" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayBatDau }}</p>
              </div>
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Đến ngày <span
                    class="text-rose-500">*</span></label>
                <input v-model="form.ngayKetThuc" type="date"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
                <p v-if="formErrors.ngayKetThuc" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayKetThuc }}</p>
              </div>
            </div>

            <div v-if="!laMoi" class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span
                  class="text-rose-500">*</span></label>
              <select v-model="form.kichHoat"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
                <option value="1">Kích hoạt</option>
                <option value="0">Tắt</option>
              </select>
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Mô tả</label>
              <textarea v-model="form.moTa" rows="3"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
                placeholder="Nhập mô tả..."></textarea>
            </div>
          </div>

          <div class="pt-4 flex flex-col gap-3">
            <button @click="submitForm" :disabled="saving"
              class="w-full inline-flex items-center justify-center gap-2 rounded-2xl bg-rose-500 px-6 py-3 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
              <Save class="h-4 w-4" />
              {{ saving ? "Đang lưu..." : (laMoi ? "Tạo đợt giảm giá" : "Lưu thay đổi") }}
            </button>
            <button @click="router.push({ name: 'admin-dot-giam-gia' })"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-6 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-100 uppercase">Hủy</button>
          </div>
        </section>
      </div>

      <!-- Cột phải: Chọn sản phẩm -->
      <div class="xl:col-span-8 space-y-6">
        <section
          class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm overflow-hidden flex flex-col max-h-[700px]">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-500">
                <Package class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Chọn sản phẩm áp dụng</h2>
                <p class="text-sm text-slate-400">Tìm kiếm và chọn sản phẩm hoặc biến thể.</p>
              </div>
            </div>
          </div>

          <!-- Search & Filters -->
          <div class="mb-4 flex gap-3">
            <div class="relative flex-1">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input v-model="searchSP" @keyup.enter="taiDanhSachSP" type="text"
                placeholder="Tìm theo tên hoặc mã sản phẩm..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>
            <button @click="taiDanhSachSP"
              class="h-11 px-6 rounded-2xl bg-slate-800 text-white text-sm font-bold hover:bg-slate-900 transition">Tìm
              kiếm</button>
          </div>

          <!-- Danh sách sản phẩm -->
          <div class="flex-1 overflow-y-auto pr-2 custom-scrollbar">
            <div v-if="dangTaiSP" class="py-20 text-center text-slate-400">
              <div
                class="animate-spin inline-block w-6 h-6 border-2 border-current border-t-transparent text-rose-500 rounded-full mb-2">
              </div>
              <p>Đang tải danh sách sản phẩm...</p>
            </div>
            <div v-else-if="!danhSachSP.length" class="py-20 text-center text-slate-400">Không tìm thấy sản phẩm nào.
            </div>
            <div v-else class="space-y-3">
              <div v-for="sp in danhSachSP" :key="sp.id"
                class="border border-slate-100 rounded-2xl overflow-hidden bg-slate-50/50">
                <div class="p-4 flex items-center justify-between hover:bg-slate-50 transition cursor-pointer"
                  @click="toggleExpand(sp)">
                  <div class="flex items-center gap-4">
                    <button @click.stop="toggleProduct(sp)" class="text-slate-400 hover:text-rose-500 transition">
                      <CheckSquare v-if="isProductSelected(sp.id)" class="h-5 w-5 text-rose-500" />
                      <Square v-else class="h-5 w-5" />
                    </button>
                    <div class="h-12 w-12 rounded-xl bg-white border border-slate-200 overflow-hidden">
                      <img v-if="sp.hinhAnh" :src="sp.hinhAnh" class="h-full w-full object-cover" />
                      <div v-else class="h-full w-full flex items-center justify-center text-slate-300">
                        <Tag class="h-6 w-6" />
                      </div>
                    </div>
                    <div>
                      <h4 class="text-sm font-bold text-slate-800">{{ sp.ten }}</h4>
                      <p class="text-xs text-slate-400">{{ sp.ma }} - {{ sp.thuongHieu }} - {{ sp.loaiGiay }}</p>
                    </div>
                  </div>
                  <div class="flex items-center gap-4">
                    <div class="text-right hidden sm:block">
                      <p class="text-sm font-bold text-slate-800">{{ formatCurrency(sp.giaMin) }} - {{
                        formatCurrency(sp.giaMax) }}</p>
                      <p class="text-xs text-slate-400">Tổng cộng: {{ sp.tongSoLuong }} cái</p>
                    </div>
                    <ChevronDown v-if="expandedProducts.has(sp.id)" class="h-5 w-5 text-slate-400" />
                    <ChevronRight v-else class="h-5 w-5 text-slate-400" />
                  </div>
                </div>

                <!-- BIẾN THỂ -->
                <div v-if="expandedProducts.has(sp.id)" class="bg-white border-t border-slate-100">
                  <div v-if="loadingVariants.has(sp.id)" class="p-4 text-center text-xs text-slate-400 italic">Đang tải
                    biến thể...</div>
                  <div v-else-if="!variantCache[sp.id]?.length" class="p-4 text-center text-xs text-slate-400 italic">
                    Không có biến thể nào.</div>
                  <div v-else class="divide-y divide-slate-50">
                    <div v-for="bt in variantCache[sp.id]" :key="bt.id"
                      class="p-3 pl-16 flex items-center justify-between hover:bg-slate-50/50 transition"
                      :class="{ 'opacity-60 grayscale-[0.5]': bt.activeCampaign }">
                      <div class="flex items-center gap-4">
                        <button @click="!bt.activeCampaign && toggleVariant(bt)" 
                          class="transition"
                          :class="bt.activeCampaign ? 'cursor-not-allowed text-slate-300' : 'text-slate-400 hover:text-rose-500'">
                          <CheckSquare v-if="isVariantSelected(bt.id)" class="h-5 w-5 text-rose-500" />
                          <Square v-else class="h-5 w-5" />
                        </button>
                        <div class="text-sm flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-0">
                          <div class="flex items-center">
                            <span class="font-semibold text-slate-700">Màu: {{ bt.mauSac }}</span>
                            <span class="mx-2 text-slate-300 hidden sm:inline">|</span>
                            <span class="font-semibold text-slate-700">Size: {{ bt.kichCo }}</span>
                          </div>
                          <span v-if="bt.activeCampaign" class="text-[10px] font-bold text-rose-500 sm:ml-4 bg-rose-50 px-2 py-0.5 rounded-full border border-rose-100 flex items-center gap-1">
                            <X class="h-3 w-3" />
                            Đã tham gia: {{ bt.activeCampaign }}
                          </span>
                        </div>
                      </div>
                      <div class="flex items-center gap-6">
                        <div class="text-right">
                          <p class="text-sm font-bold text-slate-600">{{ formatCurrency(bt.giaBan) }}</p>
                          <p class="text-xs text-slate-400">SL: {{ bt.soLuong }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- Section: Danh sách sản phẩm được chọn (Dưới cùng) -->
      <div class="xl:col-span-12">
        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-500">
                <CheckSquare class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Danh sách chi tiết sản phẩm được chọn</h2>
                <p class="text-sm text-slate-400">Các biến thể sẽ được áp dụng đợt giảm giá này.</p>
              </div>
            </div>
            <div class="px-4 py-2 bg-slate-100 rounded-xl text-sm font-bold text-slate-600">Đã chọn {{
              selectedVariants.length }} biến thể</div>
          </div>

          <div class="overflow-x-auto min-h-[200px]">
            <table class="w-full text-sm text-left border-separate border-spacing-y-2">
              <thead>
                <tr class="text-slate-500 font-bold text-[11px] tracking-wider">
                  <th class="px-4 py-2">STT</th>
                  <th class="px-4 py-2">Ảnh</th>
                  <th class="px-4 py-2">Mã SP (CT)</th>
                  <th class="px-4 py-2">Tên sản phẩm</th>
                  <th class="px-4 py-2">Giá bán</th>
                  <th class="px-4 py-2">Phiên bản</th>
                  <th class="px-4 py-2 text-center">Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!selectedVariants.length" class="bg-slate-50/30 rounded-2xl">
                  <td colspan="7" class="py-10 text-center text-slate-400 font-medium italic">Chưa có sản phẩm nào được
                    chọn.</td>
                </tr>
                <tr v-for="(v, index) in selectedVariants" :key="v.id"
                  class="bg-white ring-1 ring-slate-100 shadow-sm rounded-2xl group transition hover:ring-rose-200">
                  <td class="px-4 py-3 font-semibold text-slate-400">{{ index + 1 }}</td>
                  <td class="px-4 py-3">
                    <div class="h-10 w-10 rounded-lg bg-slate-50 border border-slate-100 overflow-hidden">
                      <img v-if="v.hinhAnh" :src="v.hinhAnh" class="h-full w-full object-cover" />
                      <div v-else
                        class="h-full w-full flex items-center justify-center text-slate-300 font-bold text-[10px]">NO
                        PIC</div>
                    </div>
                  </td>
                  <td class="px-4 py-3 font-bold text-slate-700 tracking-tight">{{ v.sku || v.maBienThe || '—' }}</td>
                  <td class="px-4 py-3">
                    <p class="font-bold text-slate-800">{{ v.tenSanPham }}</p>
                    <p class="text-[10px] text-slate-400 uppercase tracking-tighter">{{ v.thuongHieu }} • {{ v.loaiGiay
                      }}</p>
                  </td>
                  <td class="px-4 py-3 font-bold">
                    <div v-if="Number(form.giaTriGiam) > 0">
                      <span class="text-[11px] text-slate-400 line-through block font-normal">{{
                        formatCurrency(v.giaBan) }}</span>
                      <span class="text-rose-600">{{ formatCurrency(tinhGiaGiam(v.giaBan)) }}</span>
                    </div>
                    <div v-else class="text-slate-800">
                      {{ formatCurrency(v.giaBan) }}
                    </div>
                  </td>
                  <td class="px-4 py-3">
                    <div class="flex gap-1 flex-wrap">
                      <span class="px-2 py-0.5 bg-slate-100 rounded text-[10px] font-bold text-slate-600">Màu: {{
                        v.mauSac }}</span>
                      <span class="px-2 py-0.5 bg-slate-100 rounded text-[10px] font-bold text-slate-600">Size: {{
                        v.kichCo }}</span>
                    </div>
                  </td>
                  <td class="px-4 py-3 text-center">
                    <button @click="removeSelectedVariant(v.id)"
                      class="h-8 w-8 inline-flex items-center justify-center rounded-lg text-slate-400 hover:bg-rose-50 hover:text-rose-500 transition">
                      <X class="h-4 w-4" />
                    </button>
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

tr {
  transition: all 0.2s ease;
}

input[type="date"]::-webkit-calendar-picker-indicator {
  cursor: pointer;
  filter: opacity(0.5);
  transition: filter 0.2s;
}

input[type="date"]::-webkit-calendar-picker-indicator:hover {
  filter: opacity(1);
}
</style>
