<script setup>
import { onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Save, Ticket, RefreshCcw, Search, Users, CheckSquare } from "lucide-vue-next";
import {
  createPhieuGiamGia,
  getPhieuGiamGiaDetail,
  updatePhieuGiamGia,
  createPhieuGiamGiaKhachHang,
  deletePhieuGiamGiaKhachHang,
  getPhieuGiamGiaKhachHangList
} from "../../../services/khuyen-mai";
import { layDanhSachKhachHang } from "../../../services/khach-hang";

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
  loaiPhieu: "1", // 1: Public, 2: Private
  giaTri: "",
  giaTriToiThieu: "0",
  giamToiDa: "0",
  ngayBatDau: "",
  ngayKetThuc: "",
  soLuong: "",
  trangThai: "1"
});

const searchKh = ref("");
const danhSachKh = ref([]);
const dsEmailChon = ref([]);
const dangTaiKh = ref(false);

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

function taoMaNgauNhien() {
  form.ma = "VCH" + Math.random().toString(36).substring(2, 8).toUpperCase();
}

function formatVnd(value) {
    if (!value) return "0";
    return String(value).replace(/\D/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function handleVndInput(field, event) {
    const val = event.target.value.replace(/\D/g, "");
    form[field] = val;
    event.target.value = formatVnd(val);
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

async function taiChiTiet() {
  if (laMoi) {
    taiDanhSachKh();
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
      giaTriToiThieu: detail.giaTriToiThieu ?? "0",
      giamToiDa: detail.giamToiDa ?? "0",
      ngayBatDau: detail.ngayBatDau ?? "",
      ngayKetThuc: detail.ngayKetThuc ?? "",
      soLuong: detail.soLuong ?? "",
      trangThai: String(detail.trangThai ?? 1)
    });

    if (form.loaiPhieu === '2') {
        const khs = await getPhieuGiamGiaKhachHangList({ keyword: detail.ma, pageSize: 1000 });
        dsEmailChon.value = (khs?.content || []).map(item => item.email);
    }
    
    taiDanhSachKh();
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
  
  if (form.loaiPhieu === '2' && dsEmailChon.value.length === 0) {
      formErrors.email = "Phải chọn ít nhất 1 khách hàng cho phiếu cá nhân";
      isValid = false;
  }

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

    let phieuId = id;
    if (laMoi) {
      const res = await createPhieuGiamGia(payload);
      phieuId = res.id;
    } else {
      await updatePhieuGiamGia(id, payload);
    }

    // Sync private customers if needed
    if (form.loaiPhieu === '2') {
        // Logic to create PhieuGiamGiaKhachHang for each email
        for (const email of dsEmailChon.value) {
            try {
                await createPhieuGiamGiaKhachHang({
                    phieuGiamGiaId: phieuId,
                    email: email,
                    trangThai: 1
                });
            } catch (err) {}
        }
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
          <p v-if="formErrors.ten" class="text-xs text-rose-500 mt-1">{{ formErrors.ten }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Hình thức phiếu <span class="text-rose-500">*</span></label>
          <div class="flex gap-6 pt-2">
            <label class="flex items-center gap-2 cursor-pointer group">
              <input type="radio" v-model="form.loaiPhieu" value="1" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-medium text-slate-600">Công khai</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer group">
              <input type="radio" v-model="form.loaiPhieu" value="2" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-medium text-slate-600">Cá nhân</span>
            </label>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Loại giảm <span class="text-rose-500">*</span></label>
          <div class="flex gap-6 pt-2">
            <label class="flex items-center gap-2 cursor-pointer group">
              <input type="radio" v-model="form.loai" value="1" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-medium text-slate-600">Phần trăm (%)</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer group">
              <input type="radio" v-model="form.loai" value="2" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-medium text-slate-600">Tiền mặt (VNĐ)</span>
            </label>
          </div>
        </div>

        <div class="min-w-0 space-y-2">
           <label class="block text-[13px] font-semibold text-slate-700 whitespace-nowrap">Giá trị giảm ({{ form.loai === '1' ? '%' : 'VNĐ' }}) <span class="text-rose-500">*</span></label>
           <div class="relative">
             <input :value="form.giaTri" :type="form.loai === '1' ? 'number' : 'text'" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" :placeholder="form.loai === '1' ? '0' : '0'" @input="form.loai === '2' ? handleVndInput('giaTri', $event) : form.giaTri = $event.target.value" />
             <span class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-[11px]">{{ form.loai === '1' ? '%' : 'VNĐ' }}</span>
           </div>
           <p v-if="formErrors.giaTri" class="text-xs text-rose-500 mt-1">{{ formErrors.giaTri }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Giá trị đơn tối thiểu (VNĐ)</label>
          <input :value="form.giaTriToiThieu" type="text" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" @input="handleVndInput('giaTriToiThieu', $event)" />
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Giảm tối đa (VNĐ)</label>
          <input :value="form.giamToiDa" type="text" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" @input="handleVndInput('giamToiDa', $event)" />
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Số lượng <span class="text-rose-500">*</span></label>
          <input v-model="form.soLuong" type="number" min="1" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.soLuong" class="text-xs text-rose-500 mt-1">{{ formErrors.soLuong }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Ngày bắt đầu <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayBatDau" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Ngày kết thúc <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayKetThuc" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
        </div>

        <div v-if="!laMoi" class="min-w-0 space-y-2">
          <label class="block text-[13px] font-semibold text-slate-500 whitespace-nowrap">Trạng thái <span class="text-rose-500">*</span></label>
          <select v-model="form.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="1">Đang hoạt động</option>
            <option value="0">Ngưng hoạt động</option>
          </select>
        </div>
      </div>

      <!-- Customer Selection for Private Coupons -->
      <div v-if="form.loaiPhieu === '2'" class="space-y-4 rounded-3xl border border-slate-100 bg-slate-50/30 p-5 mt-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3 text-slate-800">
              <Users class="h-5 w-5 text-rose-500" />
              <span class="text-sm font-bold">Chọn khách hàng mục tiêu</span>
            </div>
            <button type="button" @click="chonTatCa" class="text-xs font-semibold text-rose-500 hover:text-rose-600 transition-colors">
              {{ dsEmailChon.length === danhSachKh.length && danhSachKh.length > 0 ? 'Bỏ chọn tất cả' : 'Chọn tất cả trang này' }}
            </button>
          </div>

          <div class="relative">
            <Search class="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input v-model="searchKh" type="text" placeholder="Tìm theo tên hoặc số điện thoại..." @input="handleSearch" class="h-11 w-full rounded-2xl border border-slate-200 bg-white pl-11 pr-4 text-sm font-medium text-slate-950 outline-none transition focus:border-rose-300" />
          </div>

          <div class="max-h-[300px] overflow-y-auto rounded-2xl border border-slate-100 bg-white shadow-sm custom-scrollbar">
            <table class="w-full text-left text-sm border-collapse">
              <thead class="sticky top-0 z-10 bg-slate-50 text-[12px] font-bold text-slate-500 uppercase">
                <tr>
                  <th class="px-4 py-3 w-12 text-center">#</th>
                  <th class="px-4 py-3">Khách hàng</th>
                  <th class="px-4 py-3">SĐT / Email</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="kh in danhSachKh" :key="kh.id" @click="toggleEmail(kh.email)" class="cursor-pointer transition-colors hover:bg-rose-50/50" :class="dsEmailChon.includes(kh.email) ? 'bg-rose-50/30' : ''">
                  <td class="px-4 py-3 text-center">
                    <CheckSquare v-if="dsEmailChon.includes(kh.email)" class="h-5 w-5 text-rose-500 mx-auto" />
                    <div v-else class="h-5 w-5 border border-slate-300 rounded mx-auto bg-white"></div>
                  </td>
                  <td class="px-4 py-3 font-semibold text-slate-800">{{ kh.hoTen }}</td>
                  <td class="px-4 py-3 text-slate-500">{{ kh.sdt }} <br/> {{ kh.email }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="text-xs font-medium text-slate-400">Đã chọn: <span class="text-rose-600 font-bold">{{ dsEmailChon.length }}</span> khách hàng</div>
          <p v-if="formErrors.email" class="text-xs text-rose-500">{{ formErrors.email }}</p>
      </div>

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
</style>
