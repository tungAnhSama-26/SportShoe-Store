<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  createDotGiamGia,
  getDotGiamGiaDetail,
  updateDotGiamGia,
  getDotGiamGiaSanPhamList,
  createDotGiamGiaSanPham,
  deleteDotGiamGiaSanPham
} from "../../../services/khuyen-mai";
import { layDanhSachGiay, layBienThe } from "../../../services/san-pham-api";
import { 
  ArrowLeft, Save, Tag, Search, Plus, Minus, 
  ChevronRight, ChevronDown, CheckSquare, Square, Package, X, RefreshCcw 
} from "lucide-vue-next";

const route = useRoute();
const router = useRouter();

const id = route.params.id;
const laMoi = !id;

const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");
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
    const res = await layDanhSachGiay({ 
      keyword: searchSP.value, 
      size: 50,
      trangThai: 1 // Chỉ lấy sp đang bán
    });
    danhSachSP.value = res?.items || [];
  } catch (e) {
    console.error(e);
  } finally {
    dangTaiSP.value = false;
  }
}

async function toggleExpand(product) {
  if (expandedProducts.value.has(product.id)) {
    expandedProducts.value.delete(product.id);
  } else {
    expandedProducts.value.add(product.id);
    if (!variantCache.value[product.id]) {
      loadingVariants.value.add(product.id);
      try {
        const variants = await layBienThe(product.id);
        variantCache.value[product.id] = variants.map(v => ({
          ...v,
          giayId: product.id,
          tenSanPham: product.ten,
          thuongHieu: product.thuongHieu,
          loaiGiay: product.loaiGiay,
          chatLieu: product.chatLieu,
          hinhAnh: v.hinhAnh || product.hinhAnh
        }));
      } catch (e) {
        console.error(e);
      } finally {
        loadingVariants.value.delete(product.id);
      }
    }
  }
}

function isVariantSelected(variantId) {
  return selectedVariants.value.some(v => v.id === variantId);
}

function isProductSelected(productId) {
  const variants = variantCache.value[productId];
  if (!variants || !variants.length) return false;
  return variants.every(v => isVariantSelected(v.id));
}

function toggleVariant(variant) {
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
    // Chọn tất cả biến thể chưa được chọn
    variants.forEach(v => {
      if (!isVariantSelected(v.id)) {
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

    // Tải danh sách sản phẩm đã áp dụng
    const applied = await getDotGiamGiaSanPhamList();
    const myApplied = applied.filter(a => a.dotGiamGiaId === Number(id));
    
    for (const item of myApplied) {
      originalGiayIds.add(item.giayId);
      if (!variantCache.value[item.giayId]) {
        try {
          const variants = await layBienThe(item.giayId);
          variantCache.value[item.giayId] = variants.map(v => ({
            ...v,
            giayId: item.giayId,
            tenSanPham: item.tenGiay,
            idLienKet: item.id
          }));
          // Giả định nếu đã liên kết sp thì coi như chọn tất cả biến thể (vì DB hiện tại chỉ lưu cấp sp)
          variants.forEach(v => {
            if (!isVariantSelected(v.id)) {
              selectedVariants.value.push({
                ...v,
                giayId: item.giayId,
                tenSanPham: item.tenGiay,
                idLienKet: item.id
              });
            }
          });
        } catch(e) {}
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
      savedId = res.id;
    } else {
      await updateDotGiamGia(id, payload);
    }

    // Xử lý liên kết sản phẩm
    const currentGiayIds = new Set(selectedVariants.value.map(v => v.giayId));
    
    // 1. Xóa các liên kết không còn trong list được chọn
    const applied = await getDotGiamGiaSanPhamList();
    const myApplied = applied.filter(a => a.dotGiamGiaId === Number(savedId));
    
    for (const item of myApplied) {
      if (!currentGiayIds.has(item.giayId)) {
        try { await deleteDotGiamGiaSanPham(item.id); } catch(e) {}
      }
    }

    // 2. Thêm các liên kết mới
    const alreadyLinkedIds = new Set(myApplied.map(a => a.giayId));
    for (const gId of currentGiayIds) {
      if (!alreadyLinkedIds.has(gId)) {
        try {
          await createDotGiamGiaSanPham({
            dotGiamGiaId: Number(savedId),
            giayId: Number(gId),
            trangThai: 1,
            ngayTao: getToday()
          });
        } catch (e) {}
      }
    }

    alert(laMoi ? "Thêm đợt giảm giá thành công" : "Cập nhật đợt giảm giá thành công");
    router.push({ name: "admin-dot-giam-gia" });
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
        @click="router.push({ name: 'admin-dot-giam-gia' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm đợt giảm giá mới" : "Chi tiết đợt giảm giá" }}
        </h1>
        <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin đợt giảm giá mới vào form bên dưới." : `Mã: ${form.ma || '...'}` }}</p>
      </div>
    </section>

    <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

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
              <label class="text-[13px] font-semibold text-slate-500">Mã đợt <span class="text-rose-500">*</span></label>
              <div class="relative">
                <input v-model="form.ma" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: SUMMER2024" />
                <button @click="taoMaNgauNhien" type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-rose-500 transition-colors">
                  <RefreshCcw class="h-4 w-4" />
                </button>
              </div>
              <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">{{ formErrors.ma }}</p>
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Tên đợt <span class="text-rose-500">*</span></label>
              <input v-model="form.ten" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: Siêu giảm giá mùa hè" />
              <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">{{ formErrors.ten }}</p>
            </div>

            <div class="grid grid-cols-1 gap-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Giá trị giảm (%) <span class="text-rose-500">*</span></label>
                <div class="relative">
                  <input v-model="form.giaTriGiam" type="number" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-10 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="0" />
                  <span class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold">%</span>
                </div>
                <p v-if="formErrors.giaTriGiam" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTriGiam }}</p>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Từ ngày <span class="text-rose-500">*</span></label>
                <input v-model="form.ngayBatDau" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
                <p v-if="formErrors.ngayBatDau" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayBatDau }}</p>
              </div>
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Đến ngày <span class="text-rose-500">*</span></label>
                <input v-model="form.ngayKetThuc" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
                <p v-if="formErrors.ngayKetThuc" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayKetThuc }}</p>
              </div>
            </div>

            <div v-if="!laMoi" class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span class="text-rose-500">*</span></label>
              <select v-model="form.kichHoat" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
                <option value="1">Kích hoạt</option>
                <option value="0">Tắt</option>
              </select>
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Mô tả</label>
              <textarea v-model="form.moTa" rows="3" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Nhập mô tả..."></textarea>
            </div>
          </div>

          <div class="pt-4 flex flex-col gap-3">
             <button @click="submitForm" :disabled="saving" class="w-full inline-flex items-center justify-center gap-2 rounded-2xl bg-rose-500 px-6 py-3 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
              <Save class="h-4 w-4" />
              {{ saving ? "Đang lưu..." : (laMoi ? "Tạo đợt giảm giá" : "Lưu thay đổi") }}
            </button>
            <button @click="router.push({ name: 'admin-dot-giam-gia' })" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-6 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-100 uppercase">Hủy</button>
          </div>
        </section>
      </div>

      <!-- Cột phải: Chọn sản phẩm -->
      <div class="xl:col-span-8 space-y-6">
        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm overflow-hidden flex flex-col max-h-[700px]">
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
              <input 
                v-model="searchSP" 
                @keyup.enter="taiDanhSachSP"
                type="text" 
                placeholder="Tìm theo tên hoặc mã sản phẩm..." 
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" 
              />
            </div>
            <button @click="taiDanhSachSP" class="h-11 px-6 rounded-2xl bg-slate-800 text-white text-sm font-bold hover:bg-slate-900 transition">Tìm kiếm</button>
          </div>

          <!-- Danh sách sản phẩm -->
          <div class="flex-1 overflow-y-auto pr-2 custom-scrollbar">
            <div v-if="dangTaiSP" class="py-20 text-center text-slate-400">
              <div class="animate-spin inline-block w-6 h-6 border-2 border-current border-t-transparent text-rose-500 rounded-full mb-2"></div>
              <p>Đang tải danh sách sản phẩm...</p>
            </div>
            <div v-else-if="!danhSachSP.length" class="py-20 text-center text-slate-400">Không tìm thấy sản phẩm nào.</div>
            <div v-else class="space-y-3">
              <div v-for="sp in danhSachSP" :key="sp.id" class="border border-slate-100 rounded-2xl overflow-hidden bg-slate-50/50">
                <div class="p-4 flex items-center justify-between hover:bg-slate-50 transition cursor-pointer" @click="toggleExpand(sp)">
                  <div class="flex items-center gap-4">
                    <button @click.stop="toggleProduct(sp)" class="text-slate-400 hover:text-rose-500 transition">
                      <CheckSquare v-if="isProductSelected(sp.id)" class="h-5 w-5 text-rose-500" />
                      <Square v-else class="h-5 w-5" />
                    </button>
                    <div class="h-12 w-12 rounded-xl bg-white border border-slate-200 overflow-hidden">
                       <img v-if="sp.hinhAnh" :src="sp.hinhAnh" class="h-full w-full object-cover" />
                       <div v-else class="h-full w-full flex items-center justify-center text-slate-300"><Tag class="h-6 w-6" /></div>
                    </div>
                    <div>
                      <h4 class="text-sm font-bold text-slate-800">{{ sp.ten }}</h4>
                      <p class="text-xs text-slate-400">{{ sp.ma }} - {{ sp.thuongHieu }} - {{ sp.loaiGiay }}</p>
                    </div>
                  </div>
                  <div class="flex items-center gap-4">
                    <div class="text-right hidden sm:block">
                      <p class="text-sm font-bold text-slate-800">{{ formatCurrency(sp.giaMin) }} - {{ formatCurrency(sp.giaMax) }}</p>
                      <p class="text-xs text-slate-400">Tổng cộng: {{ sp.tongSoLuong }} cái</p>
                    </div>
                    <ChevronDown v-if="expandedProducts.has(sp.id)" class="h-5 w-5 text-slate-400" />
                    <ChevronRight v-else class="h-5 w-5 text-slate-400" />
                  </div>
                </div>

                <!-- BIẾN THỂ -->
                <div v-if="expandedProducts.has(sp.id)" class="bg-white border-t border-slate-100">
                  <div v-if="loadingVariants.has(sp.id)" class="p-4 text-center text-xs text-slate-400 italic">Đang tải biến thể...</div>
                  <div v-else-if="!variantCache[sp.id]?.length" class="p-4 text-center text-xs text-slate-400 italic">Không có biến thể nào.</div>
                  <div v-else class="divide-y divide-slate-50">
                    <div v-for="bt in variantCache[sp.id]" :key="bt.id" class="p-3 pl-16 flex items-center justify-between hover:bg-slate-50/50 transition">
                      <div class="flex items-center gap-4">
                        <button @click="toggleVariant(bt)" class="text-slate-400 hover:text-rose-500 transition">
                          <CheckSquare v-if="isVariantSelected(bt.id)" class="h-5 w-5 text-rose-500" />
                          <Square v-else class="h-5 w-5" />
                        </button>
                        <div class="text-sm">
                           <span class="font-semibold text-slate-700">Màu: {{ bt.mauSac }}</span>
                           <span class="mx-2 text-slate-300">|</span>
                           <span class="font-semibold text-slate-700">Size: {{ bt.kichCo }}</span>
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
            <div class="px-4 py-2 bg-slate-100 rounded-xl text-sm font-bold text-slate-600">Đã chọn {{ selectedVariants.length }} biến thể</div>
          </div>

          <div class="overflow-x-auto min-h-[200px]">
            <table class="w-full text-sm text-left border-separate border-spacing-y-2">
              <thead>
                <tr class="text-slate-500 font-bold uppercase text-[11px] tracking-wider">
                  <th class="px-4 py-2">STT</th>
                  <th class="px-4 py-2">Ảnh</th>
                  <th class="px-4 py-2">Mã SP (CT)</th>
                  <th class="px-4 py-2">Tên sản phẩm</th>
                  <th class="px-4 py-2 text-right">Giá bán</th>
                  <th class="px-4 py-2">Phiên bản</th>
                  <th class="px-4 py-2 text-center">Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!selectedVariants.length" class="bg-slate-50/30 rounded-2xl">
                  <td colspan="7" class="py-10 text-center text-slate-400 font-medium italic">Chưa có sản phẩm nào được chọn.</td>
                </tr>
                <tr v-for="(v, index) in selectedVariants" :key="v.id" class="bg-white ring-1 ring-slate-100 shadow-sm rounded-2xl group transition hover:ring-rose-200">
                  <td class="px-4 py-3 font-semibold text-slate-400">{{ index + 1 }}</td>
                  <td class="px-4 py-3">
                    <div class="h-10 w-10 rounded-lg bg-slate-50 border border-slate-100 overflow-hidden">
                       <img v-if="v.hinhAnh" :src="v.hinhAnh" class="h-full w-full object-cover" />
                       <div v-else class="h-full w-full flex items-center justify-center text-slate-300 font-bold text-[10px]">NO PIC</div>
                    </div>
                  </td>
                  <td class="px-4 py-3 font-bold text-slate-700 tracking-tight">{{ v.sku || v.maBienThe || '—' }}</td>
                  <td class="px-4 py-3">
                    <p class="font-bold text-slate-800">{{ v.tenSanPham }}</p>
                    <p class="text-[10px] text-slate-400 uppercase tracking-tighter">{{ v.thuongHieu }} • {{ v.loaiGiay }}</p>
                  </td>
                  <td class="px-4 py-3 text-right font-bold">
                    <div v-if="Number(form.giaTriGiam) > 0">
                      <span class="text-[11px] text-slate-400 line-through block font-normal">{{ formatCurrency(v.giaBan) }}</span>
                      <span class="text-rose-600">{{ formatCurrency(tinhGiaGiam(v.giaBan)) }}</span>
                    </div>
                    <div v-else class="text-slate-800">
                      {{ formatCurrency(v.giaBan) }}
                    </div>
                  </td>
                  <td class="px-4 py-3">
                    <div class="flex gap-1 flex-wrap">
                      <span class="px-2 py-0.5 bg-slate-100 rounded text-[10px] font-bold text-slate-600">Màu: {{ v.mauSac }}</span>
                      <span class="px-2 py-0.5 bg-slate-100 rounded text-[10px] font-bold text-slate-600">Size: {{ v.kichCo }}</span>
                    </div>
                  </td>
                  <td class="px-4 py-3 text-center">
                    <button @click="removeSelectedVariant(v.id)" class="h-8 w-8 inline-flex items-center justify-center rounded-lg text-slate-400 hover:bg-rose-50 hover:text-rose-500 transition">
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
