<script setup lang="ts">
import { computed, onActivated, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  CheckCircle2, Eye, FileSpreadsheet, Filter, Home, MapPin, Package,
  Plus, RotateCcw, Search, ShoppingBag, Trash2, Users, X
} from "lucide-vue-next";
import {
  doiTrangThaiKhachHang, layDanhSachKhachHang,
  layDanhSachDiaChi, themDiaChi, capNhatDiaChi, xoaDiaChi, datMacDinhDiaChi,
  layHoaDonTheoKhachHang
} from "../../../services/khach-hang";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const router = useRouter();
const CUSTOMER_CREATE_TOAST_KEY = "admin-khach-hang-toast";

const danhSach = ref([]);
const dangTai = ref(false);
const loiTrang = ref("");
const boLoc = ref({ keyword: "", trangThai: "" });
const toast = ref({
  hienThi: false,
  loai: "success",
  tieuDe: "",
  noiDung: "",
});
let toastTimer: ReturnType<typeof setTimeout> | null = null;

const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Hoạt động", value: "1" },
  { label: "Khóa", value: "0" },
];

function hienThiThongBao(loai: "success" | "error", tieuDe: string, noiDung = "") {
  if (toastTimer) clearTimeout(toastTimer);
  toast.value = { hienThi: true, loai, tieuDe, noiDung };
  toastTimer = setTimeout(() => {
    toast.value.hienThi = false;
    toastTimer = null;
  }, 3200);
}

function taiThongBaoDieuHuong() {
  if (typeof window === "undefined") return;
  const raw = window.sessionStorage.getItem(CUSTOMER_CREATE_TOAST_KEY);
  if (!raw) return;
  window.sessionStorage.removeItem(CUSTOMER_CREATE_TOAST_KEY);

  try {
    const payload = JSON.parse(raw);
    const noiDung = typeof payload?.noiDung === "string"
      ? payload.noiDung.replace(/^Email đã lưu:\s*/i, "Đã gửi thông tin đăng nhập tới email: ")
      : "";
    hienThiThongBao(
      payload?.loai === "error" ? "error" : "success",
      payload?.tieuDe || "Thao tác thành công",
      noiDung,
    );
  } catch {
    hienThiThongBao("success", "Đã tạo khách hàng mới");
  }
}

function mauTrangThai(trangThai: number) {
  return trangThai === 1 ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600";
}

function dinhDangNgay(ngay: string) {
  if (!ngay) return "—";
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit", month: "2-digit", year: "numeric",
  }).format(new Date(ngay));
}

function dinhDangTien(n: number) {
  if (n == null) return "—";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);
}

function mauTrangThaiDon(trangThai: string) {
  const map: Record<string, string> = {
    "Hoàn thành": "bg-emerald-50 text-emerald-600",
    "Chờ giao hàng": "bg-sky-50 text-sky-600",
    "Chờ xác nhận": "bg-amber-50 text-amber-600",
    "Đã xác nhận": "bg-orange-50 text-orange-600",
    "Chờ lấy hàng": "bg-violet-50 text-violet-600",
    "Đã giao hàng": "bg-teal-50 text-teal-600",
    "Hủy": "bg-rose-50 text-rose-600",
    "Yêu cầu hủy": "bg-orange-50 text-orange-600",
    "Cần hoàn tiền": "bg-pink-50 text-pink-600",
  };
  return map[trangThai] ?? "bg-slate-50 text-slate-600";
}

// Phân trang
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const pageSizeOptions = [5, 10, 20];
const tongSoTrang = computed(() => Math.ceil(danhSach.value.length / soPhanTuMotTrang.value) || 1);
const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSach.value.slice(start, start + soPhanTuMotTrang.value);
});

watch(danhSach, () => { trangHienTai.value = 1; });
watch(soPhanTuMotTrang, () => { trangHienTai.value = 1; });

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const data = await layDanhSachKhachHang({
      keyword: boLoc.value.keyword || undefined,
      trangThai: boLoc.value.trangThai !== "" ? Number(boLoc.value.trangThai) : undefined,
    });
    danhSach.value = data;
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách khách hàng");
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", trangThai: "" };
}

function xemChiTiet(id: string) {
  router.push({ name: "admin-khach-hang-chi-tiet", params: { id } });
}

const dangDoiTrangThai = ref<string | null>(null);

async function toggleTrangThai(kh: any) {
  if (dangDoiTrangThai.value) return;
  const trangThaiMoi = kh.trangThai === 1 ? 0 : 1;
  const hanhDong = trangThaiMoi === 1 ? "kích hoạt" : "khóa";
  if (!window.confirm(`Bạn có chắc muốn ${hanhDong} ${kh.hoTen || kh.tenDangNhap} không?`)) return;
  dangDoiTrangThai.value = kh.id;
  try {
    await doiTrangThaiKhachHang(kh.id, trangThaiMoi);
    kh.trangThai = trangThaiMoi;
    kh.tenTrangThai = trangThaiMoi === 1 ? "Hoạt động" : "Khóa";
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể cập nhật trạng thái");
    setTimeout(() => (loiTrang.value = ""), 3000);
  } finally {
    dangDoiTrangThai.value = null;
  }
}

function themMoi() {
  router.push({ name: "admin-khach-hang-them" });
}

function xuatExcel() {
  if (!danhSach.value.length) { window.alert("Không có dữ liệu để xuất Excel."); return; }
  exportRowsToExcel({
    filename: "quan-ly-khach-hang",
    sheetName: "KhachHang",
    columns: [
      { label: "STT", value: (_, index) => index + 1 },
      { label: "Tên đăng nhập", key: "tenDangNhap" },
      { label: "Họ tên", key: "hoTen" },
      { label: "Email", value: (row) => row.email || "—" },
      { label: "Số điện thoại", value: (row) => row.sdt || "—" },
      { label: "Địa chỉ mặc định", value: (row) => row.diaChiMacDinh || "—" },
      { label: "Trạng thái", value: (row) => row.tenTrangThai || "—" },
    ],
    rows: danhSach.value,
  });
}

let timer: ReturnType<typeof setTimeout>;
watch(() => boLoc.value, () => {
  clearTimeout(timer);
  timer = setTimeout(taiDanhSach, 300);
}, { deep: true });

// ========================
// MODAL ĐỊA CHỈ
// ========================
const khModalDiaChi = ref<any>(null);
const dsDiaChiModal = ref<any[]>([]);
const dangTaiDiaChi = ref(false);
const loiDiaChi = ref("");
const hienFormDiaChi = ref(false);
const diaChiDangSua = ref<any>(null);
const dangLuuDiaChi = ref(false);
const formDiaChi = ref({
  hoTen: "", sdt: "", tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "", laMacDinh: false,
});

// Province cascade
const dsTinh = ref<any[]>([]);
const dsHuyen = ref<any[]>([]);
const dsXa = ref<any[]>([]);
const maTinhChon = ref<number | null>(null);
const maHuyenChon = ref<number | null>(null);
const dangTaiDiaPhuong = ref(false);

async function taiDsTinh() {
  if (dsTinh.value.length) return;
  dangTaiDiaPhuong.value = true;
  try {
    const res = await fetch("https://provinces.open-api.vn/api/p/");
    dsTinh.value = await res.json();
  } catch { dsTinh.value = []; } finally { dangTaiDiaPhuong.value = false; }
}

async function onTinhChange(code: number | null) {
  maTinhChon.value = code;
  dsHuyen.value = [];
  dsXa.value = [];
  maHuyenChon.value = null;
  formDiaChi.value.tinhThanh = dsTinh.value.find(t => t.code === code)?.name ?? "";
  formDiaChi.value.quanHuyen = "";
  formDiaChi.value.phuongXa = "";
  if (!code) return;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/p/${code}?depth=2`);
    const data = await res.json();
    dsHuyen.value = data.districts ?? [];
  } catch { dsHuyen.value = []; }
}

async function onHuyenChange(code: number | null) {
  maHuyenChon.value = code;
  dsXa.value = [];
  formDiaChi.value.quanHuyen = dsHuyen.value.find(h => h.code === code)?.name ?? "";
  formDiaChi.value.phuongXa = "";
  if (!code) return;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/d/${code}?depth=2`);
    const data = await res.json();
    dsXa.value = data.wards ?? [];
  } catch { dsXa.value = []; }
}

function onXaChange(tenXa: string) {
  formDiaChi.value.phuongXa = tenXa;
}

async function preFillCascadeForEdit(dc: any) {
  await taiDsTinh();
  const tinh = dsTinh.value.find(t => t.name === dc.tinhThanh);
  if (!tinh) return;
  maTinhChon.value = tinh.code;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/p/${tinh.code}?depth=2`);
    const data = await res.json();
    dsHuyen.value = data.districts ?? [];
    const huyen = dsHuyen.value.find(h => h.name === dc.quanHuyen);
    if (!huyen) return;
    maHuyenChon.value = huyen.code;
    const res2 = await fetch(`https://provinces.open-api.vn/api/d/${huyen.code}?depth=2`);
    const data2 = await res2.json();
    dsXa.value = data2.wards ?? [];
  } catch { /* ignore */ }
}

async function moModalDiaChi(kh: any) {
  khModalDiaChi.value = kh;
  hienFormDiaChi.value = false;
  diaChiDangSua.value = null;
  loiDiaChi.value = "";
  await taiDsModalDiaChi(kh.id);
  await taiDsTinh();
}

async function taiDsModalDiaChi(khId: string) {
  dangTaiDiaChi.value = true;
  try {
    dsDiaChiModal.value = await layDanhSachDiaChi(khId);
  } catch { dsDiaChiModal.value = []; } finally { dangTaiDiaChi.value = false; }
}

function dongModalDiaChi() {
  khModalDiaChi.value = null;
  dsDiaChiModal.value = [];
  hienFormDiaChi.value = false;
  diaChiDangSua.value = null;
  maTinhChon.value = null;
  maHuyenChon.value = null;
  dsHuyen.value = [];
  dsXa.value = [];
}

function moThemDiaChiModal() {
  diaChiDangSua.value = null;
  formDiaChi.value = {
    hoTen: khModalDiaChi.value?.hoTen ?? "",
    sdt: khModalDiaChi.value?.sdt ?? "",
    tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "",
    laMacDinh: dsDiaChiModal.value.length === 0,
  };
  maTinhChon.value = null;
  maHuyenChon.value = null;
  dsHuyen.value = [];
  dsXa.value = [];
  hienFormDiaChi.value = true;
}

async function moSuaDiaChiModal(dc: any) {
  diaChiDangSua.value = dc;
  formDiaChi.value = {
    hoTen: dc.hoTen, sdt: dc.sdt,
    tinhThanh: dc.tinhThanh, quanHuyen: dc.quanHuyen, phuongXa: dc.phuongXa,
    diaChiCuThe: dc.diaChiCuThe, laMacDinh: dc.laMacDinh,
  };
  maTinhChon.value = null;
  maHuyenChon.value = null;
  dsHuyen.value = [];
  dsXa.value = [];
  await preFillCascadeForEdit(dc);
  hienFormDiaChi.value = true;
}

async function luuDiaChiModal() {
  const f = formDiaChi.value;
  const dangSua = Boolean(diaChiDangSua.value);
  if (!f.hoTen || !f.sdt || !f.tinhThanh || !f.quanHuyen || !f.phuongXa || !f.diaChiCuThe) {
    loiDiaChi.value = "Vui lòng điền đầy đủ thông tin địa chỉ.";
    return;
  }
  loiDiaChi.value = "";
  dangLuuDiaChi.value = true;
  try {
    if (diaChiDangSua.value) {
      await capNhatDiaChi(diaChiDangSua.value.id, f);
    } else {
      await themDiaChi(khModalDiaChi.value.id, f);
    }
    await taiDsModalDiaChi(khModalDiaChi.value.id);
    // Cập nhật diaChiMacDinh trong bảng nếu có thay đổi mặc định
    if (f.laMacDinh) capNhatDiaChiMacDinhTrongBang(khModalDiaChi.value.id);
    hienFormDiaChi.value = false;
    diaChiDangSua.value = null;
    hienThiThongBao(
      "success",
      f.laMacDinh
        ? "Đã lưu địa chỉ mặc định"
        : dangSua
          ? "Đã cập nhật địa chỉ"
          : "Đã thêm địa chỉ mới"
    );
  } catch (e) {
    loiDiaChi.value = getDisplayErrorMessage(e, "Không thể lưu địa chỉ.");
  } finally {
    dangLuuDiaChi.value = false;
  }
}

async function xoaDiaChiModal(diaChiId: number) {
  if (!confirm("Bạn có chắc chắn muốn xóa địa chỉ này?")) return;
  try {
    await xoaDiaChi(diaChiId);
    await taiDsModalDiaChi(khModalDiaChi.value.id);
    capNhatDiaChiMacDinhTrongBang(khModalDiaChi.value.id);
    hienThiThongBao("success", "Đã xóa địa chỉ");
  } catch { loiDiaChi.value = "Không thể xóa địa chỉ."; }
}

async function datMacDinhModal(diaChiId: number) {
  try {
    await datMacDinhDiaChi(diaChiId);
    await taiDsModalDiaChi(khModalDiaChi.value.id);
    capNhatDiaChiMacDinhTrongBang(khModalDiaChi.value.id);
    hienThiThongBao("success", "Đã cập nhật địa chỉ mặc định");
  } catch { loiDiaChi.value = "Không thể đặt địa chỉ mặc định."; }
}

function capNhatDiaChiMacDinhTrongBang(khId: string) {
  const macDinh = dsDiaChiModal.value.find(dc => dc.laMacDinh);
  const kh = danhSach.value.find(k => k.id === khId);
  if (kh) {
    kh.diaChiMacDinh = macDinh
      ? `${macDinh.diaChiCuThe}, ${macDinh.phuongXa}, ${macDinh.quanHuyen}, ${macDinh.tinhThanh}`
      : null;
  }
}

// ========================
// MODAL ĐƠN HÀNG
// ========================
const khModalDonHang = ref<any>(null);
const dsHoaDonModal = ref<any[]>([]);
const dangTaiDonHang = ref(false);

async function moModalDonHang(kh: any) {
  khModalDonHang.value = kh;
  dangTaiDonHang.value = true;
  try {
    dsHoaDonModal.value = await layHoaDonTheoKhachHang(kh.id);
  } catch { dsHoaDonModal.value = []; } finally { dangTaiDonHang.value = false; }
}

function dongModalDonHang() {
  khModalDonHang.value = null;
  dsHoaDonModal.value = [];
}

function xemChiTietDon(id: number) {
  router.push({ name: "admin-hoa-don-chi-tiet", params: { id } });
}

onMounted(() => {
  taiDanhSach();
  taiThongBaoDieuHuong();
});

onActivated(() => {
  taiThongBaoDieuHuong();
});
</script>

<template>
  <div class="space-y-5">
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
        :class="toast.loai === 'success' ? 'border-emerald-100' : 'border-rose-100'"
      >
        <div class="flex gap-3 p-4">
          <div
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full"
            :class="toast.loai === 'success' ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'"
          >
            <CheckCircle2 class="h-5 w-5" />
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-bold text-slate-800">{{ toast.tieuDe }}</p>
            <p v-if="toast.noiDung" class="mt-1 text-sm leading-5 text-slate-600">{{ toast.noiDung }}</p>
          </div>
          <button
            type="button"
            class="rounded-full p-1 text-slate-400 transition hover:bg-white/70 hover:text-slate-600"
            @click="toast.hienThi = false"
          >
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="h-1.5 w-full" :class="toast.loai === 'success' ? 'bg-emerald-500' : 'bg-rose-500'"></div>
      </div>
    </Transition>

    <!-- Header -->
    <section>
      <h1 class="admin-page-title text-[30px]">Quản lý khách hàng</h1>
    </section>

    <!-- Bộ lọc -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
          <Filter class="h-5 w-5" />
        </div>
        <h2 class="admin-section-title">Bộ lọc</h2>
      </div>

      <div class="flex flex-col gap-4">
        <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
          <div class="min-w-0 flex-1">
            <div class="relative max-w-3xl">
              <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                v-model="boLoc.keyword"
                type="text"
                placeholder="Tìm theo tên đăng nhập, họ tên, email, SĐT..."
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </div>
          </div>
          <div class="flex flex-wrap items-center gap-3 xl:justify-end">
            <button @click="lamMoiBoLoc" class="admin-btn-soft"><RotateCcw class="h-4 w-4" /> Đặt lại</button>
            <button @click="xuatExcel" class="admin-btn-soft"><FileSpreadsheet class="h-4 w-4" /> Xuất Excel</button>
            <button @click="themMoi" class="admin-btn-primary"><Plus class="h-4 w-4" /> Thêm khách hàng</button>
          </div>
        </div>
        <div class="grid gap-4 md:max-w-sm">
          <label class="space-y-2">
            <span class="admin-filter-label mb-1">Trạng thái</span>
            <select v-model="boLoc.trangThai" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white">
              <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">{{ tt.label }}</option>
            </select>
          </label>
        </div>
      </div>
    </section>

    <!-- Danh sách -->
    <section class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-5 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-violet-50 text-violet-500">
          <Users class="h-5 w-5" />
        </div>
        <h2 class="admin-section-title">Danh sách khách hàng</h2>
      </div>

      <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="overflow-x-auto">
        <table class="min-w-[960px] w-full border-separate border-spacing-y-2 text-sm">
          <thead>
            <tr class="text-left text-sm font-bold text-slate-950">
              <th class="rounded-l-2xl bg-slate-100 px-4 py-3">STT</th>
              <th class="bg-slate-100 px-4 py-3">Ảnh</th>
              <th class="bg-slate-100 px-4 py-3">Tên đăng nhập</th>
              <th class="bg-slate-100 px-4 py-3">Họ tên</th>
              <th class="bg-slate-100 px-4 py-3">Email</th>
              <th class="bg-slate-100 px-4 py-3">Số điện thoại</th>
              <th class="bg-slate-100 px-4 py-3">Địa chỉ mặc định</th>
              <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
              <th class="rounded-r-2xl bg-slate-100 px-4 py-3 text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">Đang tải dữ liệu khách hàng...</td>
            </tr>
            <tr v-else-if="!danhSachPhanTrang.length">
              <td colspan="9" class="py-10 text-center text-sm text-slate-400">Không có khách hàng phù hợp.</td>
            </tr>
            <tr
              v-for="(kh, index) in danhSachPhanTrang"
              :key="kh.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-4 py-3 font-semibold">{{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}</td>
              <td class="px-4 py-3">
                <img
                  :src="kh.hinhAnh || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(kh.hoTen) + '&background=f1f5f9&color=475569&size=64'"
                  :alt="kh.hoTen"
                  class="h-10 w-10 rounded-full object-cover ring-2 ring-slate-100"
                />
              </td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ kh.tenDangNhap }}</td>
              <td class="px-4 py-3 font-semibold text-slate-800">{{ kh.hoTen }}</td>
              <td class="px-4 py-3 text-slate-600">{{ kh.email || '—' }}</td>
              <td class="px-4 py-3 text-slate-600">{{ kh.sdt || '—' }}</td>
              <td class="px-4 py-3 text-slate-500 max-w-[200px]">
                <span v-if="kh.diaChiMacDinh" class="line-clamp-2 text-xs leading-relaxed">{{ kh.diaChiMacDinh }}</span>
                <span v-else class="text-slate-300 italic text-xs">Chưa có địa chỉ</span>
              </td>
              <td class="px-4 py-3">
                <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="mauTrangThai(kh.trangThai)">
                  {{ kh.tenTrangThai }}
                </span>
              </td>
              <td class="rounded-r-2xl px-4 py-3 text-center">
                <div class="flex items-center justify-center gap-1">
                  <AdminQuickStatusAction
                    :loading="dangDoiTrangThai === kh.id"
                    :action-label="kh.trangThai === 1 ? 'Khóa tài khoản' : 'Kích hoạt tài khoản'"
                    :intent="kh.trangThai === 1 ? 'deactivate' : 'activate'"
                    @toggle="toggleTrangThai(kh)"
                  />
                  <button
                    @click="moModalDiaChi(kh)"
                    class="admin-table-action text-sky-500 hover:text-sky-700"
                    title="Quản lý địa chỉ"
                  >
                    <MapPin :size="14" />
                  </button>
                  <button
                    @click="moModalDonHang(kh)"
                    class="admin-table-action text-violet-500 hover:text-violet-700"
                    title="Xem đơn hàng"
                  >
                    <ShoppingBag :size="14" />
                  </button>
                  <button
                    @click="xemChiTiet(kh.id)"
                    class="admin-table-action text-slate-600 hover:text-slate-900"
                    title="Xem / Chỉnh sửa"
                  >
                    <Eye :size="14" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminTableFooter
        :current-page="trangHienTai"
        :page-size="soPhanTuMotTrang"
        :page-size-options="pageSizeOptions"
        :total-items="danhSach.length"
        :total-pages="tongSoTrang"
        compact
        show-refresh
        @refresh="taiDanhSach"
        @update:current-page="trangHienTai = $event"
        @update:page-size="soPhanTuMotTrang = $event"
      />
    </section>
  </div>

  <!-- ===== MODAL ĐỊA CHỈ ===== -->
  <Teleport to="body">
    <div v-if="khModalDiaChi" class="fixed inset-0 z-[100] flex items-start justify-center bg-slate-900/40 p-4 overflow-y-auto">
      <div class="w-full max-w-2xl rounded-[32px] bg-white shadow-2xl my-8">
        <!-- Header modal -->
        <div class="flex items-center justify-between border-b border-slate-100 px-8 py-5">
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-sky-50 text-sky-500">
              <MapPin class="h-5 w-5" />
            </div>
            <div>
              <h2 class="text-base font-bold text-slate-800">Địa chỉ của {{ khModalDiaChi.hoTen }}</h2>
              <p class="text-xs text-slate-400">Quản lý địa chỉ giao hàng</p>
            </div>
          </div>
          <button @click="dongModalDiaChi" class="h-8 w-8 flex items-center justify-center rounded-full bg-slate-100 text-slate-500 hover:bg-slate-200">
            <X class="h-4 w-4" />
          </button>
        </div>

        <div class="p-8 space-y-5">
          <!-- Lỗi -->
          <div v-if="loiDiaChi" class="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ loiDiaChi }}</div>

          <!-- Danh sách địa chỉ -->
          <div v-if="!hienFormDiaChi">
            <div v-if="dangTaiDiaChi" class="py-8 text-center text-sm text-slate-400">Đang tải...</div>
            <div v-else-if="!dsDiaChiModal.length" class="py-8 text-center border-2 border-dashed border-slate-100 rounded-3xl text-slate-400 text-sm">
              Chưa có địa chỉ nào.
            </div>
            <div v-else class="grid gap-3 sm:grid-cols-2">
              <div
                v-for="dc in dsDiaChiModal"
                :key="dc.id"
                class="group relative rounded-2xl border p-4 transition"
                :class="dc.laMacDinh ? 'border-emerald-200 bg-emerald-50/40' : 'border-slate-100 bg-slate-50 hover:border-rose-200 hover:bg-white'"
              >
                <div class="flex items-start justify-between gap-2">
                  <div class="space-y-1 flex-1 min-w-0">
                    <div class="flex items-center gap-2 flex-wrap">
                      <span class="font-bold text-slate-800 text-sm">{{ dc.hoTen }}</span>
                      <span v-if="dc.laMacDinh" class="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-700">
                        <CheckCircle2 class="h-3 w-3" /> Mặc định
                      </span>
                    </div>
                    <p class="text-[12px] text-slate-600">{{ dc.sdt }}</p>
                    <p class="text-[12px] text-slate-500 leading-relaxed">{{ dc.diaChiCuThe }}, {{ dc.phuongXa }}, {{ dc.quanHuyen }}, {{ dc.tinhThanh }}</p>
                  </div>
                  <div class="flex flex-col gap-1 shrink-0">
                    <button v-if="!dc.laMacDinh" @click="datMacDinhModal(dc.id)" class="h-7 px-2 rounded-lg bg-emerald-50 text-emerald-600 hover:bg-emerald-100 text-[11px] font-semibold transition">Mặc định</button>
                    <button @click="moSuaDiaChiModal(dc)" class="h-7 px-2 rounded-lg bg-slate-100 text-slate-500 hover:text-sky-600 hover:bg-sky-50 text-[11px] font-semibold transition">Sửa</button>
                    <button v-if="!dc.laMacDinh" @click="xoaDiaChiModal(dc.id)" class="h-7 px-2 rounded-lg bg-slate-100 text-slate-500 hover:text-rose-600 hover:bg-rose-50 text-[11px] font-semibold transition">Xóa</button>
                  </div>
                </div>
              </div>
            </div>

            <button @click="moThemDiaChiModal" class="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-slate-200 py-3 text-sm font-semibold text-slate-500 hover:border-rose-300 hover:text-rose-500 transition">
              <Plus class="h-4 w-4" /> Thêm địa chỉ mới
            </button>
          </div>

          <!-- Form thêm/sửa địa chỉ -->
          <div v-else class="space-y-4">
            <h3 class="text-sm font-bold text-slate-700">{{ diaChiDangSua ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
            <div class="grid gap-4 sm:grid-cols-2">
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Họ tên người nhận <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.hoTen" type="text" placeholder="Họ tên" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.sdt" type="tel" placeholder="Số điện thoại" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
                <select
                  :value="maTinhChon"
                  @change="onTinhChange(Number(($event.target as HTMLSelectElement).value) || null)"
                  class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
                >
                  <option value="">-- Chọn tỉnh/thành --</option>
                  <option v-for="t in dsTinh" :key="t.code" :value="t.code">{{ t.name }}</option>
                </select>
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Quận/Huyện <span class="text-rose-500">*</span></span>
                <select
                  :value="maHuyenChon"
                  @change="onHuyenChange(Number(($event.target as HTMLSelectElement).value) || null)"
                  :disabled="!maTinhChon"
                  class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white disabled:opacity-50"
                >
                  <option value="">-- Chọn quận/huyện --</option>
                  <option v-for="h in dsHuyen" :key="h.code" :value="h.code">{{ h.name }}</option>
                </select>
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] font-semibold text-slate-500">Phường/Xã <span class="text-rose-500">*</span></span>
                <select
                  :value="formDiaChi.phuongXa"
                  @change="onXaChange(($event.target as HTMLSelectElement).value)"
                  :disabled="!maHuyenChon"
                  class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white disabled:opacity-50"
                >
                  <option value="">-- Chọn phường/xã --</option>
                  <option v-for="x in dsXa" :key="x.code" :value="x.name">{{ x.name }}</option>
                </select>
              </label>
              <label class="space-y-1.5 sm:col-span-2">
                <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.diaChiCuThe" type="text" placeholder="Số nhà, tên đường..." class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
              </label>
              <div class="sm:col-span-2 flex items-center gap-2">
                <input v-model="formDiaChi.laMacDinh" type="checkbox" id="laMacDinhModal" class="h-4 w-4 rounded" />
                <label for="laMacDinhModal" class="text-sm font-semibold text-slate-600 cursor-pointer">Đặt làm địa chỉ mặc định</label>
              </div>
            </div>
            <div class="flex gap-3 pt-2">
              <button @click="luuDiaChiModal" :disabled="dangLuuDiaChi" class="flex-1 h-10 rounded-2xl bg-rose-500 text-sm font-bold text-white hover:bg-rose-600 transition disabled:opacity-50">
                {{ dangLuuDiaChi ? "Đang lưu..." : "Lưu địa chỉ" }}
              </button>
              <button @click="hienFormDiaChi = false; diaChiDangSua = null" class="flex-1 h-10 rounded-2xl border border-slate-200 text-sm font-semibold text-slate-600 hover:bg-slate-50 transition">
                Hủy
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- ===== MODAL ĐƠN HÀNG ===== -->
  <Teleport to="body">
    <div v-if="khModalDonHang" class="fixed inset-0 z-[100] flex items-start justify-center bg-slate-900/40 p-4 overflow-y-auto">
      <div class="w-full max-w-5xl rounded-[32px] bg-white shadow-2xl my-8">
        <!-- Header -->
        <div class="flex items-center justify-between border-b border-slate-100 px-8 py-5">
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-violet-50 text-violet-500">
              <ShoppingBag class="h-5 w-5" />
            </div>
            <div>
              <h2 class="text-base font-bold text-slate-800">Đơn hàng</h2>
              <p class="text-xs text-slate-400">Lịch sử mua hàng của khách</p>
            </div>
          </div>
          <button @click="dongModalDonHang" class="h-8 w-8 flex items-center justify-center rounded-full bg-slate-100 text-slate-500 hover:bg-slate-200">
            <X class="h-4 w-4" />
          </button>
        </div>

        <div class="flex flex-col xl:flex-row gap-0">
          <!-- Thông tin khách hàng -->
          <div class="xl:w-56 shrink-0 border-b xl:border-b-0 xl:border-r border-slate-100 p-6 flex flex-col items-center text-center gap-3">
            <img
              :src="khModalDonHang.hinhAnh || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(khModalDonHang.hoTen) + '&background=f1f5f9&color=475569&size=128'"
              :alt="khModalDonHang.hoTen"
              class="h-20 w-20 rounded-full object-cover ring-4 ring-slate-100"
            />
            <div>
              <p class="font-bold text-slate-800">{{ khModalDonHang.hoTen }}</p>
              <p class="text-xs text-slate-500 mt-1">{{ khModalDonHang.email || '—' }}</p>
              <p class="text-xs text-slate-500">{{ khModalDonHang.sdt || '—' }}</p>
              <p class="text-xs text-slate-500">{{ khModalDonHang.ngaySinh || '—' }}</p>
              <span class="mt-2 inline-flex rounded-full px-3 py-1 text-[11px] font-semibold" :class="mauTrangThai(khModalDonHang.trangThai)">
                {{ khModalDonHang.tenTrangThai }}
              </span>
            </div>
          </div>

          <!-- Bảng đơn hàng -->
          <div class="flex-1 p-6 overflow-x-auto">
            <div v-if="dangTaiDonHang" class="py-10 text-center text-sm text-slate-400">Đang tải đơn hàng...</div>
            <div v-else-if="!dsHoaDonModal.length" class="py-10 text-center text-sm text-slate-400">
              <Package class="h-10 w-10 mx-auto mb-2 text-slate-200" />
              Trống
            </div>
            <table v-else class="min-w-[560px] w-full border-separate border-spacing-y-2 text-sm">
              <thead>
                <tr class="text-left text-xs font-bold text-slate-950">
                  <th class="rounded-l-2xl bg-slate-100 px-4 py-3">#</th>
                  <th class="bg-slate-100 px-4 py-3">Mã đơn hàng</th>
                  <th class="bg-slate-100 px-4 py-3">Trạng thái</th>
                  <th class="bg-slate-100 px-4 py-3">Loại</th>
                  <th class="bg-slate-100 px-4 py-3">Tổng cộng</th>
                  <th class="bg-slate-100 px-4 py-3">Chi tiết</th>
                  <th class="rounded-r-2xl bg-slate-100 px-4 py-3">Tạo lúc</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(hd, idx) in dsHoaDonModal"
                  :key="hd.id"
                  class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
                >
                  <td class="rounded-l-2xl px-4 py-3 font-semibold text-slate-500">{{ idx + 1 }}</td>
                  <td class="px-4 py-3 font-mono font-semibold text-slate-800 text-xs">{{ hd.maHoaDon }}</td>
                  <td class="px-4 py-3">
                    <span class="inline-flex rounded-full px-2 py-0.5 text-[11px] font-semibold" :class="mauTrangThaiDon(hd.trangThai)">
                      {{ hd.trangThai }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-slate-600">{{ hd.loaiDon }}</td>
                  <td class="px-4 py-3 font-semibold text-slate-800">{{ dinhDangTien(hd.tongTien) }}</td>
                  <td class="px-4 py-3">
                    <button @click="xemChiTietDon(hd.id)" class="text-rose-500 hover:text-rose-700 text-xs font-semibold underline">Xem chi tiết</button>
                  </td>
                  <td class="rounded-r-2xl px-4 py-3 text-slate-500 text-xs">{{ dinhDangNgay(hd.ngayTao) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
