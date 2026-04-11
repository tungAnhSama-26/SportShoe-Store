<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { CalendarDays, ChevronLeft, ChevronRight, Eye, FileText, Filter, QrCode, RefreshCw, Search } from "lucide-vue-next";
import { layDanhSachHoaDon, type HoaDonItem } from "../../../services/hoa-don";

type TrangThaiLoc = "Tất cả" | HoaDonItem["trangThai"];

const router = useRouter();
const danhSach = ref<HoaDonItem[]>([]);
const dangTai = ref(false);
const loiTrang = ref("");
const boLoc = ref({ keyword: "", tuNgay: "", denNgay: "", loaiDon: "" });
const trangThaiDangChon = ref<TrangThaiLoc>("Tất cả");
const dsTrangThai: TrangThaiLoc[] = ["Tất cả", "Chờ xác nhận", "Đã xác nhận", "Chờ vận chuyển", "Vận chuyển", "Đã hoàn thành", "Hủy"];

const mauTrangThai: Record<HoaDonItem["trangThai"], string> = {
  "Chờ xác nhận": "bg-amber-50 text-amber-600",
  "Đã xác nhận": "bg-blue-50 text-blue-600",
  "Chờ vận chuyển": "bg-violet-50 text-violet-600",
  "Vận chuyển": "bg-cyan-50 text-cyan-600",
  "Đã hoàn thành": "bg-emerald-50 text-emerald-600",
  Hủy: "bg-rose-50 text-rose-600",
};

function dinhDangTien(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND", maximumFractionDigits: 0 }).format(value || 0);
}

function dinhDangNgay(ngay: string) {
  return new Intl.DateTimeFormat("vi-VN", { hour: "2-digit", minute: "2-digit", day: "2-digit", month: "2-digit", year: "numeric" }).format(new Date(ngay));
}

const tongTheoTrangThai = computed(() => dsTrangThai.map((trangThai) => ({
  ten: trangThai,
  tong: trangThai === "Tất cả" ? danhSach.value.length : danhSach.value.filter((hoaDon) => hoaDon.trangThai === trangThai).length,
})));

const danhSachHienThi = computed(() => {
  if (trangThaiDangChon.value === "Tất cả") return danhSach.value;
  return danhSach.value.filter(h => h.trangThai === trangThaiDangChon.value);
});

// Phân trang
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);

const tongSoTrang = computed(() => Math.ceil(danhSachHienThi.value.length / soPhanTuMotTrang.value) || 1);

const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSachHienThi.value.slice(start, start + soPhanTuMotTrang.value);
});

watch(danhSachHienThi, () => {
  trangHienTai.value = 1;
});

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    danhSach.value = await layDanhSachHoaDon({
      keyword: boLoc.value.keyword,
      tuNgay: boLoc.value.tuNgay,
      denNgay: boLoc.value.denNgay,
      loaiDon: boLoc.value.loaiDon,
      // Không gửi trạng thái lên backend, lấy TẤT CẢ để hiển thị số đếm cho chuẩn
      trangThai: undefined, 
    });
  } catch (error) {
    loiTrang.value = error instanceof Error ? error.message : "Không thể tải danh sách hóa đơn";
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", tuNgay: "", denNgay: "", loaiDon: "" };
  trangThaiDangChon.value = "Tất cả";
}

function xemChiTiet(id: number) {
  router.push({ name: "admin-hoa-don-chi-tiet", params: { id } });
}

// Lọc theo bộ lọc với hiệu ứng debounce (tránh gọi API liên tục khi gõ)
let boLocTimeout: ReturnType<typeof setTimeout>;
watch(() => boLoc.value, () => {
  clearTimeout(boLocTimeout);
  boLocTimeout = setTimeout(() => {
    taiDanhSach();
  }, 300);
}, { deep: true });

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5">
    <section>
      <h1 class="text-[30px] font-bold tracking-tight text-slate-800">Quản lý hóa đơn</h1>
      <p class="mt-2 text-sm text-slate-400">Theo dõi đơn hàng, trạng thái thanh toán và tra cứu chi tiết hóa đơn.</p>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600"><Filter class="h-5 w-5" /></div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Bộ lọc</h2>
          <p class="text-sm text-slate-400">Lọc theo từ khóa, thời gian tạo và loại đơn.</p>
        </div>
      </div>

      <div class="grid gap-4 xl:grid-cols-[1.3fr_1fr_1fr_1fr_auto]">
        <label class="space-y-2">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Tìm kiếm</span>
          <div class="relative">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input v-model="boLoc.keyword" type="text" placeholder="Tìm theo mã hóa đơn, tên khách hàng, tên nhân viên" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          </div>
        </label>
        <label class="space-y-2">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Ngày bắt đầu</span>
          <div class="relative">
            <CalendarDays class="absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input v-model="boLoc.tuNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-11 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          </div>
        </label>
        <label class="space-y-2">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Ngày kết thúc</span>
          <div class="relative">
            <CalendarDays class="absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input v-model="boLoc.denNgay" type="date" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 pr-11 text-sm outline-none transition focus:border-rose-300 focus:bg-white" />
          </div>
        </label>
        <label class="space-y-2">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Loại đơn</span>
          <select v-model="boLoc.loaiDon" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
            <option value="">Tất cả loại đơn</option>
            <option value="Tại cửa hàng">Tại cửa hàng</option>
            <option value="Online">Online</option>
          </select>
        </label>
        <div class="grid gap-3 sm:grid-cols-2 xl:flex xl:items-end">
          <button type="button" @click="lamMoiBoLoc" class="h-11 rounded-2xl bg-slate-400 px-5 text-sm font-semibold text-white transition hover:bg-slate-500">Làm mới</button>
          <button type="button" class="h-11 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600">Xuất file</button>
          <button type="button" class="h-11 rounded-2xl bg-rose-500 px-5 text-sm font-semibold text-white transition hover:bg-rose-600"><span class="inline-flex items-center gap-2"><QrCode class="h-4 w-4" />Scan QR</span></button>
        </div>
      </div>
    </section>

    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500"><FileText class="h-5 w-5" /></div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Danh sách hóa đơn</h2>
          <p class="text-sm text-slate-400">{{ danhSachHienThi.length }} hóa đơn trong phạm vi bộ lọc hiện tại.</p>
        </div>
      </div>

      <div class="mb-4 flex flex-wrap gap-2">
        <button v-for="item in tongTheoTrangThai" :key="item.ten" type="button" @click="trangThaiDangChon = item.ten" class="rounded-2xl px-4 py-2 text-sm font-semibold transition" :class="trangThaiDangChon === item.ten ? 'bg-rose-100 text-rose-600' : 'bg-slate-50 text-slate-500 hover:bg-slate-100'">
          {{ item.ten }}
          <span class="ml-2 rounded-full bg-white px-2 py-0.5 text-xs text-slate-500">{{ item.tong }}</span>
        </button>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table class="min-w-[1080px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-500">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Mã hóa đơn</th>
              <th class="bg-slate-100 px-4 py-3">Khách hàng</th>
              <th class="bg-slate-100 px-4 py-3">Nhân viên</th>
              <th class="bg-slate-100 px-4 py-3">Tổng tiền</th>
              <th class="bg-slate-100 px-4 py-3">Ngày tạo</th>
              <th class="bg-slate-100 px-4 py-3">Loại đơn</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai"><td colspan="9" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu hóa đơn...</td></tr>
            <tr v-else-if="!danhSachPhanTrang.length"><td colspan="9" class="py-10 text-center text-sm text-slate-400">Không có hóa đơn phù hợp.</td></tr>
            <tr v-for="(hoaDon, index) in danhSachPhanTrang" :key="hoaDon.id" class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100">
              <td class="rounded-l-2xl px-4 py-4 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-4 font-semibold text-slate-800">{{ hoaDon.maHoaDon }}</td>
              <td class="px-4 py-4">{{ hoaDon.tenKhachHang }}</td>
              <td class="px-4 py-4">{{ hoaDon.tenNhanVien }}</td>
              <td class="px-4 py-4 font-semibold text-slate-800">{{ dinhDangTien(hoaDon.tongTien) }}</td>
              <td class="px-4 py-4">{{ dinhDangNgay(hoaDon.ngayTao) }}</td>
              <td class="px-4 py-4">{{ hoaDon.loaiDon }}</td>
              <td class="px-4 py-4"><span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai[hoaDon.trangThai]">{{ hoaDon.trangThai }}</span></td>
              <td class="rounded-r-2xl px-4 py-4 text-center"><button type="button" @click="xemChiTiet(hoaDon.id)" class="inline-flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-rose-50 hover:text-rose-500"><Eye class="h-4 w-4" /></button></td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-5 flex items-center justify-end text-sm">
        <div class="flex items-center gap-2">
          <button type="button" @click="taiDanhSach" class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 hover:bg-slate-200 transition" title="Làm mới"><RefreshCw class="h-4 w-4" /></button>
          
          <div class="flex items-center gap-1 ml-2">
            <button @click="trangHienTai = Math.max(1, trangHienTai - 1)" :disabled="trangHienTai === 1" class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-50 disabled:hover:bg-slate-100"><ChevronLeft class="h-4 w-4" /></button>
            
            <button v-for="page in tongSoTrang" :key="page" @click="trangHienTai = page" class="flex h-8 w-8 items-center justify-center rounded-lg transition" :class="trangHienTai === page ? 'border border-violet-200 bg-violet-50 text-violet-600 font-bold' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'">{{ page }}</button>
            
            <button @click="trangHienTai = Math.min(tongSoTrang, trangHienTai + 1)" :disabled="trangHienTai === tongSoTrang" class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500 transition hover:bg-slate-200 disabled:opacity-50 disabled:hover:bg-slate-100"><ChevronRight class="h-4 w-4" /></button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
