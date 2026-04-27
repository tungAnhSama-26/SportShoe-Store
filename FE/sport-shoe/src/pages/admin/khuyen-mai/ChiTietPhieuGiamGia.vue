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
<<<<<<< Updated upstream
  if (laMoi) return;
=======
  if (laMoi) {
    return;
  }
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
      <div class="grid gap-6 md:grid-cols-2">
        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Mã phiếu <span class="text-rose-500">*</span></label>
          <input v-model="form.ma" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: VOUCHER2024" />
          <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">{{ formErrors.ma }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Tên phiếu <span class="text-rose-500">*</span></label>
          <input v-model="form.ten" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: Giảm giá hè 2024" />
=======
      <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Mã phiếu <span class="text-rose-500">*</span></label>
          <div class="relative">
            <input v-model="form.ma" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: VOUCHER2024" />
            <button @click="taoMaNgauNhien" type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-rose-500 transition-colors">
              <RefreshCcw class="h-4 w-4" />
            </button>
          </div>
          <p v-if="formErrors.ma" class="text-xs text-rose-500 mt-1">{{ formErrors.ma }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Tên phiếu <span class="text-rose-500">*</span></label>
          <input v-model="form.ten" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: Giảm giá hè 2024" />
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
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
=======
        <div class="min-w-0 space-y-2">
           <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Giá trị giảm ({{ form.loai === '1' ? '%' : 'VNĐ' }}) <span class="text-rose-500">*</span></label>
           <div class="relative">
             <input 
               :value="form.giaTri" 
               :type="form.loai === '1' ? 'number' : 'text'"
               class="h-11 w-full rounded-2xl border bg-slate-50 pl-4 pr-12 text-sm font-medium text-slate-950 outline-none transition focus:ring-2 focus:ring-rose-500/20" 
               :class="formErrors.giaTri ? 'border-rose-500 bg-rose-50 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300 focus:bg-white'"
               :placeholder="form.loai === '1' ? '0' : '0'"
               @input="form.loai === '2' ? handleVndInput('giaTri', $event) : form.giaTri = $event.target.value"
             />
             <span class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-[11px]">{{ form.loai === '1' ? '%' : 'VNĐ' }}</span>
           </div>
           <p v-if="formErrors.giaTri" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTri }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Giá trị đơn tối thiểu (VNĐ)</label>
          <input :value="form.giaTriToiThieu" type="text" inputmode="numeric" class="h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:ring-2 focus:ring-rose-500/20" :class="formErrors.giaTriToiThieu ? 'border-rose-500 bg-rose-50 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300 focus:bg-white'" @input="handleVndInput('giaTriToiThieu', $event)" />
          <p v-if="formErrors.giaTriToiThieu" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTriToiThieu }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Giảm tối đa (VNĐ)</label>
          <input :value="form.giamToiDa" type="text" inputmode="numeric" class="h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:ring-2 focus:ring-rose-500/20" :class="formErrors.giamToiDa ? 'border-rose-500 bg-rose-50 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300 focus:bg-white'" @input="handleVndInput('giamToiDa', $event)" />
          <p v-if="formErrors.giamToiDa" class="text-xs text-rose-500 mt-1">{{ formErrors.giamToiDa }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Số lượng <span class="text-rose-500">*</span></label>
          <input v-model="form.soLuong" type="number" min="1" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.soLuong" class="text-xs text-rose-500 mt-1">{{ formErrors.soLuong }}</p>
        </div>

        <div v-if="!laMoi" class="order-last min-w-0 space-y-2 xl:col-span-1">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Trạng thái <span class="text-rose-500">*</span></label>
          <select v-model="form.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition whitespace-nowrap focus:border-rose-300 focus:bg-white">
            <option value="1">Đang hoạt động</option>
            <option value="0">Ngưng hoạt động</option>
>>>>>>> Stashed changes
          </select>
          <p v-if="formErrors.trangThai" class="text-xs text-rose-500 mt-1">{{ formErrors.trangThai }}</p>
        </div>

<<<<<<< Updated upstream
        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Ngày bắt đầu <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayBatDau" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.ngayBatDau" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayBatDau }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Ngày kết thúc <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayKetThuc" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
=======
        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Ngày bắt đầu <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayBatDau" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.ngayBatDau" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayBatDau }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Ngày kết thúc <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayKetThuc" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
>>>>>>> Stashed changes
          <p v-if="formErrors.ngayKetThuc" class="text-xs text-rose-500 mt-1">{{ formErrors.ngayKetThuc }}</p>
        </div>
      </div>

<<<<<<< Updated upstream
=======
      <!-- Customer Selection for Private Coupons -->
      <Transition
        enter-active-class="transition duration-300 ease-out"
        enter-from-class="transform translate-y-4 opacity-0"
        enter-to-class="transform translate-y-0 opacity-100"
        leave-active-class="transition duration-200 ease-in"
        leave-from-class="transform translate-y-0 opacity-100"
        leave-to-class="transform translate-y-4 opacity-0"
      >
        <div v-if="form.loaiPhieu === '2'" class="space-y-4 rounded-3xl border border-slate-100 bg-slate-50/30 p-5">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3 text-slate-800">
              <Users class="h-5 w-5 text-rose-500" />
              <span class="text-sm font-bold">Chọn khách hàng mục tiêu</span>
            </div>
            <button type="button" @click="chonTatCa" class="text-xs font-semibold text-rose-500 hover:text-rose-600 transition-colors">
              {{ dsEmailChon.length === danhSachKh.length && danhSachKh.length > 0 ? 'Bỏ chọn tất cả' : 'Chọn tất cả bản ghi hiện tại' }}
            </button>
          </div>

          <div class="relative">
            <Search class="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input 
              v-model="searchKh"
              type="text"
              placeholder="Tìm theo tên hoặc số điện thoại khách hàng..."
              @input="handleSearch"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-white pl-11 pr-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:ring-4 focus:ring-rose-500/5"
            />
          </div>

          <div class="max-h-[350px] overflow-y-auto rounded-2xl border border-slate-100 bg-white shadow-sm custom-scrollbar">
            <table class="w-full text-left border-collapse">
              <thead class="sticky top-0 z-10 bg-slate-50 text-[13px] font-bold text-slate-950">
                <tr>
                  <th class="px-4 py-3 w-12 text-center">#</th>
                  <th class="px-4 py-3">Họ tên</th>
                  <th class="px-4 py-3 w-32">Số điện thoại</th>
                  <th class="px-4 py-3">Email</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100 text-sm text-slate-800">
                <tr v-for="kh in danhSachKh" :key="kh.id" 
                    @click="toggleEmail(kh.email)"
                    class="cursor-pointer transition-colors hover:bg-rose-50/50"
                    :class="dsEmailChon.some(e => e.trim().toLowerCase() === kh.email?.trim().toLowerCase()) ? 'bg-rose-50/30' : ''">
                  <td class="px-4 py-3 text-center">
                    <div class="flex items-center justify-center">
                      <div class="h-5 w-5 rounded-md border transition-all flex items-center justify-center"
                           :class="dsEmailChon.some(e => e.trim().toLowerCase() === kh.email?.trim().toLowerCase()) ? 'bg-rose-500 border-rose-500' : 'border-slate-300 bg-white'">
                        <CheckSquare v-if="dsEmailChon.some(e => e.trim().toLowerCase() === kh.email?.trim().toLowerCase())" class="h-3.5 w-3.5 text-white" />
                      </div>
                    </div>
                  </td>
                  <td class="px-4 py-3 font-semibold text-slate-800">{{ kh.hoTen }}</td>
                  <td class="px-4 py-3">{{ kh.sdt || '—' }}</td>
                  <td class="px-4 py-3">{{ kh.email }}</td>
                </tr>
                <tr v-if="!danhSachKh.length && !dangTaiKh">
                  <td colspan="4" class="py-12 text-center text-sm text-slate-400 font-medium">Không tìm thấy khách hàng nào.</td>
                </tr>
                <tr v-if="dangTaiKh">
                  <td colspan="4" class="py-12 text-center">
                    <div class="inline-block h-6 w-6 animate-spin rounded-full border-2 border-rose-500 border-t-transparent"></div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <div class="flex items-center justify-between pt-2 border-t border-slate-100">
            <span class="text-xs font-medium text-slate-400">Đã chọn: <span class="text-rose-600 font-bold">{{ dsEmailChon.length }}</span> khách hàng</span>
            <p v-if="formErrors.email" class="text-xs text-rose-500 font-medium">{{ formErrors.email }}</p>
          </div>
        </div>
      </Transition>

>>>>>>> Stashed changes
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
