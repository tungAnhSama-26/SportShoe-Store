<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Save, Tag } from "lucide-vue-next";
import {
  createDotGiamGia,
  getDotGiamGiaDetail,
  updateDotGiamGia
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
  moTa: "",
  loaiGiam: "1",
  giaTriGiam: "",
  ngayBatDau: "",
  ngayKetThuc: "",
  kichHoat: "1"
});

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

async function taiChiTiet() {
<<<<<<< Updated upstream
  if (laMoi) return;
=======
  if (laMoi) {
    taiDanhSachSP();
    return;
  }
>>>>>>> Stashed changes
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
  if (!form.giaTriGiam || Number(form.giaTriGiam) <= 0) { formErrors.giaTriGiam = "Giá trị giảm không hợp lệ"; isValid = false; }
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
      moTa: form.moTa.trim(),
      loaiGiam: Number(form.loaiGiam),
      giaTriGiam: Number(form.giaTriGiam),
      ngayBatDau: form.ngayBatDau,
      ngayKetThuc: form.ngayKetThuc,
      kichHoat: Number(form.kichHoat),
      ngayTao: laMoi ? getToday() : undefined,
      ngayCapNhat: !laMoi ? getToday() : undefined
    };

    if (laMoi) {
      await createDotGiamGia(payload);
      alert("Thêm đợt giảm giá thành công");
    } else {
      await updateDotGiamGia(id, payload);
      alert("Cập nhật đợt giảm giá thành công");
    }
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
<<<<<<< Updated upstream
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm đợt giảm giá mới" : "Chi tiết đợt giảm giá" }}
        </h1>
        <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin đợt giảm giá mới vào form bên dưới." : `Mã: ${form.ma || '...'}` }}</p>
=======
      <div class="flex-1 flex items-center gap-4 min-w-0">
        <div>
          <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
            {{ laMoi ? "Thêm đợt giảm giá mới" : "Chi tiết đợt giảm giá" }}
          </h1>
          <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin đợt giảm giá mới vào form bên dưới." : `Mã:
            ${form.ma || '...'}` }}</p>
        </div>
        <div v-if="Number(form.giaTriGiam) > 0" class="flex items-center gap-2 rounded-2xl bg-rose-500 px-4 py-2 text-white shadow-sm animate-in zoom-in duration-300">
          <Tag class="h-5 w-5 fill-white/20" />
          <span class="text-lg font-black tracking-tight">{{ form.giaTriGiam }}% OFF</span>
        </div>
>>>>>>> Stashed changes
      </div>
    </section>

    <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

<<<<<<< Updated upstream
    <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm space-y-6">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500">
          <Tag class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Thông tin đợt giảm</h2>
          <p class="text-sm text-slate-400">Thiết lập chương trình khuyến mãi cho sản phẩm.</p>
        </div>
=======
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
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
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
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
                placeholder="Ví dụ: Siêu giảm giá mùa hè" />
              <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">{{ formErrors.ten }}</p>
            </div>

            <div class="grid grid-cols-1 gap-4">
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Giá trị giảm (%) <span
                    class="text-rose-500">*</span></label>
                <div class="relative">
                  <input v-model="form.giaTriGiam" type="number"
                    class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-10 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
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
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
                <p v-if="formErrors.ngayBatDau" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayBatDau }}</p>
              </div>
              <div class="space-y-2">
                <label class="text-[13px] font-semibold text-slate-500">Đến ngày <span
                    class="text-rose-500">*</span></label>
                <input v-model="form.ngayKetThuc" type="date"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
                <p v-if="formErrors.ngayKetThuc" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayKetThuc }}</p>
              </div>
            </div>

            <div v-if="!laMoi" class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span
                  class="text-rose-500">*</span></label>
              <select v-model="form.kichHoat"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
                <option value="1">Kích hoạt</option>
                <option value="0">Tắt</option>
              </select>
            </div>

            <div class="space-y-2">
              <label class="text-[13px] font-semibold text-slate-500">Mô tả</label>
              <textarea v-model="form.moTa" rows="3"
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white"
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
>>>>>>> Stashed changes
      </div>

      <div class="grid gap-6 md:grid-cols-2">
        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Mã đợt <span class="text-rose-500">*</span></label>
          <input v-model="form.ma" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: SUMMER2024" />
          <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">{{ formErrors.ma }}</p>
        </div>

<<<<<<< Updated upstream
        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Tên đợt <span class="text-rose-500">*</span></label>
          <input v-model="form.ten" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: Siêu giảm giá mùa hè" />
          <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">{{ formErrors.ten }}</p>
        </div>
=======
          <!-- Search & Filters -->
          <div class="mb-4 flex gap-3">
            <div class="relative flex-1">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input v-model="searchSP" @keyup.enter="taiDanhSachSP" type="text"
                placeholder="Tìm theo tên hoặc mã sản phẩm..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
            </div>
            <button @click="taiDanhSachSP"
              class="h-11 px-6 rounded-2xl bg-slate-800 text-white text-sm font-bold hover:bg-slate-900 transition">Tìm
              kiếm</button>
          </div>
>>>>>>> Stashed changes

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Loại giảm <span class="text-rose-500">*</span></label>
          <div class="flex gap-6 pt-2">
            <label class="flex items-center gap-2 cursor-pointer group">
              <div class="relative flex items-center justify-center">
                <input type="radio" v-model="form.loaiGiam" value="1" class="peer h-5 w-5 cursor-pointer appearance-none rounded-full border border-slate-300 checked:border-rose-500 transition-all" />
                <div class="pointer-events-none absolute h-2.5 w-2.5 rounded-full bg-rose-500 opacity-0 peer-checked:opacity-100 transition-opacity"></div>
              </div>
              <span class="text-sm font-medium text-slate-600 group-hover:text-slate-800 transition-colors">Phần trăm (%)</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer group">
              <div class="relative flex items-center justify-center">
                <input type="radio" v-model="form.loaiGiam" value="2" class="peer h-5 w-5 cursor-pointer appearance-none rounded-full border border-slate-300 checked:border-rose-500 transition-all" />
                <div class="pointer-events-none absolute h-2.5 w-2.5 rounded-full bg-rose-500 opacity-0 peer-checked:opacity-100 transition-opacity"></div>
              </div>
              <span class="text-sm font-medium text-slate-600 group-hover:text-slate-800 transition-colors">Tiền mặt (VNĐ)</span>
            </label>
          </div>
          <p v-if="formErrors.loaiGiam" class="text-xs text-rose-500 mt-1">{{ formErrors.loaiGiam }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Giá trị giảm <span class="text-rose-500">*</span></label>
          <input v-model="form.giaTriGiam" type="number" min="0" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Nhập giá trị..." />
          <p v-if="formErrors.giaTriGiam" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTriGiam }}</p>
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

        <div class="md:col-span-2 space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Mô tả</label>
          <textarea v-model="form.moTa" rows="4" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Nhập mô tả chi tiết về chương trình..."></textarea>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Trạng thái <span class="text-rose-500">*</span></label>
          <select v-model="form.kichHoat" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="1">Kích hoạt</option>
            <option value="0">Tắt</option>
          </select>
          <p v-if="formErrors.kichHoat" class="text-xs text-rose-500 mt-1">{{ formErrors.kichHoat }}</p>
        </div>
      </div>

<<<<<<< Updated upstream
      <!-- Actions -->
      <div class="flex items-center gap-3 pt-6 border-t border-slate-100">
        <button @click="submitForm" :disabled="saving" class="inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
          <Save class="h-4 w-4" />
          {{ saving ? "Đang lưu..." : (laMoi ? "Tạo đợt giảm giá" : "Lưu thay đổi") }}
        </button>
        <button @click="router.push({ name: 'admin-dot-giam-gia' })" class="rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100">Hủy</button>
=======
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
                <tr class="text-slate-950 font-bold text-[11px] tracking-wider">
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
                  <td class="px-4 py-3 font-bold text-slate-900 tracking-tight">{{ v.sku || v.maBienThe || '—' }}</td>
                  <td class="px-4 py-3">
                    <p class="font-bold text-slate-900">{{ v.tenSanPham }}</p>
                    <p class="text-[10px] text-slate-500 uppercase tracking-tighter">{{ v.thuongHieu }} • {{ v.loaiGiay
                      }}</p>
                  </td>
                  <td class="px-4 py-3">
                    <div v-if="Number(form.giaTriGiam) > 0">
                      <div class="flex items-center gap-1.5">
                        <span class="text-rose-600 font-bold text-base">{{ formatCurrency(tinhGiaGiam(v.giaGoc || v.giaBan)) }}</span>
                        <span class="inline-flex items-center rounded-md bg-rose-50 px-1.5 py-0.5 text-[10px] font-bold text-rose-600 ring-1 ring-inset ring-rose-500/20">
                          -{{ form.giaTriGiam }}%
                        </span>
                      </div>
                      <span class="text-[12px] text-slate-400 line-through block font-normal">{{
                        formatCurrency(v.giaGoc || v.giaBan) }}</span>
                    </div>
                    <div v-else class="text-slate-800 font-bold text-base">
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
                    <div class="flex items-center justify-center gap-2">
                      <button
                        type="button"
                        class="inline-flex h-8 items-center gap-1 rounded-lg border border-slate-200 bg-white px-2.5 text-[11px] font-semibold text-slate-600 transition hover:border-rose-200 hover:text-rose-600"
                        @click="moChiTietSanPham(v.giayId, v.id)"
                      >
                        <ArrowUpRight class="h-3.5 w-3.5" />
                        CTSP
                      </button>
                      <button @click="removeSelectedVariant(v.id)"
                        class="h-8 w-8 inline-flex items-center justify-center rounded-lg text-slate-400 hover:bg-rose-50 hover:text-rose-500 transition">
                        <X class="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
>>>>>>> Stashed changes
      </div>
    </section>
  </div>
</template>
