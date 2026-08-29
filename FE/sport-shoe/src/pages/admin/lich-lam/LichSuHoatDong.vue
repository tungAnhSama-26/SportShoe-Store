<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { 
  Search, 
  RefreshCw, 
  Calendar as CalendarIcon, 
  FileText, 
  LogIn, 
  LogOut, 
  ListChecks, 
  RotateCcw, 
  ChevronDown, 
  Download, 
  Eye,
  X as CloseIcon,
  Clock,
  CheckCircle2,
  Calculator,
  AlertTriangle,
  UserCheck,
  FileCheck,
  ArrowRightLeft
} from "lucide-vue-next";
import { useRouter } from "vue-router";
import { layLichSuGiaoCa } from "../../../services/giao-ca.js";
import { layDanhSachCaLam } from "../../../services/ca-lam.js";
import { dinhDangTienViet } from "../../../utils/dinhDangTien.js";
import { showError } from "../../../utils/alert.js";
import { exportRowsToExcel } from "../../../utils/export-excel.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import Button from "../../../components/ui/Button.vue";

const router = useRouter();

// Trạng thái chung
const dangTai = ref(false);
const danhSachLichSu = ref([]);

// Bộ lọc
const tuNgay = ref("");
const denNgay = ref("");
const searchQuery = ref("");
const selectedCaLam = ref("");
const selectedVaiTro = ref("");
const danhSachCaLam = ref([]);

// Danh sách hiển thị tự động lọc từ khóa (Client & Server dual-filter)
const danhSachHienThi = computed(() => {
  const q = searchQuery.value.toLowerCase().trim();
  const lowerNoSpace = q.replace(/\s+/g, "");

  return danhSachLichSu.value.filter((item) => {
    // Lọc theo ca làm việc (Shift filter)
    let isShiftMatch = true;
    if (selectedCaLam.value) {
      const selected = danhSachCaLam.value.find(c => String(c.id) === String(selectedCaLam.value));
      if (selected) {
        const caLamName = (item.caLamTen || getShiftDetails(item).tenCa || "").toLowerCase();
        isShiftMatch = caLamName.includes(selected.ten.toLowerCase()) || 
                       (item.caLamId && String(item.caLamId) === String(selectedCaLam.value));
      }
    }

    if (!isShiftMatch) return false;

    // Lọc theo vai trò (Role filter)
    let isRoleMatch = true;
    if (selectedVaiTro.value) {
      const v = parseInt(selectedVaiTro.value);
      const role = item.nhanVienTrongCaVaiTro || item.nhanVien?.vaiTro;
      // Note: If backend hasn't restarted, role might be undefined.
      // Vai trò 1 là quản trị viên, các vai trò còn lại là nhân viên.
      if (v === 1) {
        isRoleMatch = role === 1;
      } else if (v === 2) {
        isRoleMatch = role !== 1;
      }
    }

    if (!isRoleMatch) return false;

    if (!q) {
      return true;
    }
    const shiftDetails = getShiftDetails(item);
    const tenNhanVien = (item.nhanVienTrongCaTen || item.nhanVien?.tenNhanVien || item.nhanVien?.hoTen || item.tenTaiKhoan || "").toLowerCase();
    const maNhanVien = (item.nhanVienTrongCaMa || item.nhanVien?.ma || item.maNhanVien || "").toLowerCase();
    const maCa = (item.ma || "").toLowerCase();
    const tenCa = (shiftDetails.tenCa || "").toLowerCase();
    const gioCa = (shiftDetails.gioCa || "").toLowerCase();
    const ghiChu = (item.ghiChu || "").toLowerCase();

    let isShiftCodeMatch = false;
    if (lowerNoSpace === "ca00001" || lowerNoSpace === "ca001" || lowerNoSpace === "ca1" || lowerNoSpace.includes("casang")) {
      isShiftCodeMatch = tenCa.includes("sáng");
    } else if (lowerNoSpace === "ca00002" || lowerNoSpace === "ca002" || lowerNoSpace === "ca2" || lowerNoSpace.includes("cachieu")) {
      isShiftCodeMatch = tenCa.includes("chiều");
    } else if (lowerNoSpace === "ca00003" || lowerNoSpace === "ca003" || lowerNoSpace === "ca3" || lowerNoSpace.includes("catoi")) {
      isShiftCodeMatch = tenCa.includes("tối");
    }

    return (
      isShiftCodeMatch ||
      tenNhanVien.includes(q) ||
      maNhanVien.includes(q) ||
      maCa.includes(q) ||
      tenCa.includes(q) ||
      gioCa.includes(q) ||
      ghiChu.includes(q)
    );
  }).sort((a, b) => {
    const thoiGianA = Date.parse(a.thoiGianVao || a.thoiGianMoCa || "");
    const thoiGianB = Date.parse(b.thoiGianVao || b.thoiGianMoCa || "");
    const timestampA = Number.isNaN(thoiGianA) ? Number.NEGATIVE_INFINITY : thoiGianA;
    const timestampB = Number.isNaN(thoiGianB) ? Number.NEGATIVE_INFINITY : thoiGianB;
    return timestampB - timestampA;
  });
});

// Pagination
const currentPage = ref(0);
const pageSize = ref(10);
const pageSizeOptions = [5, 10, 20, 50];
const totalElements = ref(0);
const totalPages = ref(1);

// Lấy danh sách lịch sử
const fetchLichSu = async () => {
  dangTai.value = true;
  try {
    let tuNgayIso = undefined;
    let denNgayIso = undefined;

    if (tuNgay.value) {
      const d = new Date(`${tuNgay.value}T00:00:00`);
      if (!isNaN(d.getTime())) tuNgayIso = d.toISOString();
    }

    if (denNgay.value) {
      const d = new Date(`${denNgay.value}T23:59:59.999`);
      if (!isNaN(d.getTime())) denNgayIso = d.toISOString();
    }

    const filters = {
      tuNgay: tuNgayIso,
      denNgay: denNgayIso,
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value?.trim() || undefined 
    };
    
    const response = await layLichSuGiaoCa(filters);
    
    if (Array.isArray(response)) {
      danhSachLichSu.value = response;
      totalElements.value = response.length;
      totalPages.value = Math.ceil(response.length / pageSize.value) || 1;
    } else if (response) {
      // Giả định backend trả về object phân trang chuẩn: { content, totalElements, totalPages }
      danhSachLichSu.value = response.content || response.data || [];
      totalElements.value = response.totalElements || danhSachLichSu.value.length;
      totalPages.value = response.totalPages || 1;
    }
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể tải dữ liệu lịch sử hoạt động."));
  } finally {
    dangTai.value = false;
  }
};

const handleRefresh = () => {
  setNgayMacDinh();
  searchQuery.value = "";
  selectedCaLam.value = "";
  selectedVaiTro.value = "";
  currentPage.value = 0;
  fetchLichSu();
};

const xuatExcel = () => {
  if (!danhSachHienThi.value.length) {
    showError("Không có dữ liệu để xuất Excel.");
    return;
  }

  exportRowsToExcel({
    filename: "lich-su-hoat-dong",
    sheetName: "LichSu",
    columns: [
      { label: "STT", value: (_, index) => currentPage.value * pageSize.value + index + 1 },
      { label: "Mã nhân viên", value: (row) => row.nhanVienTrongCaMa || row.nhanVien?.ma || row.maNhanVien || "NV0000" },
      { label: "Tên nhân viên", value: (row) => row.nhanVienTrongCaTen || row.nhanVien?.tenNhanVien || row.nhanVien?.hoTen || row.tenTaiKhoan || "Chưa xác định" },
      { label: "Vai trò", value: (row) => (row.nhanVienTrongCaVaiTro === 1 || row.nhanVien?.vaiTro === 1) ? "Quản trị viên" : "Nhân viên" },
      { label: "Ca / Hoạt động", value: (row) => getShiftName(row) },
      { label: "Thời gian vào", value: (row) => formatDateTime(row.thoiGianVao || row.thoiGianMoCa) || "—" },
      { label: "Thời gian ra", value: (row) => formatDateTime(row.thoiGianRa || row.thoiGianDongCa) || "—" },
      { label: "Doanh thu", value: (row) => dinhDangTienViet(getRevenue(row)) },
      { label: "Trạng thái", value: (row) => hienThiTrangThai(row) },
      { label: "Ghi chú", value: (row) => row.ghiChu || "Không có" },
    ],
    rows: danhSachHienThi.value,
  });
};

const handlePageChange = (newPage) => {
  currentPage.value = newPage;
  fetchLichSu();
};

const handlePageSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 0;
  fetchLichSu();
};

const xemChiTietHoaDon = (item) => {
  if (!item?.hoaDonId) return;
  router.push({ name: "admin-hoa-don-chi-tiet", params: { id: item.hoaDonId } });
};

const xemChiTietCaLam = (item) => {
  if (!item?.id) return;
  router.push({
    name: "admin-lich-su-hoat-dong-chi-tiet",
    params: { id: item.id }
  });
};

watch([tuNgay, denNgay], () => {
  currentPage.value = 0;
  fetchLichSu();
});

// Hàm format thời gian (từ ISO về dạng HH:mm:ss DD/MM/YYYY)
const formatTime = (timeStr) => {
  if (!timeStr) return "—";
  try {
    const date = new Date(timeStr);
    const time = date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const day = date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    return { time, day };
  } catch (e) {
    return { time: "—", day: "—" };
  }
};

const formatDateTime = (timeStr) => {
  if (!timeStr) return null;
  try {
    const date = new Date(timeStr);
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    return `${hours}:${minutes} ${day}/${month}/${year}`;
  } catch (e) {
    return null;
  }
};

const getShiftDetails = (item) => {
  if (!item) {
    return {
      tenCa: "Ca làm việc",
      gioCa: "—",
      lateMin: 0,
      earlyMin: 0
    };
  }

  const timeVaoStr = item.thoiGianVao || item.thoiGianMoCa;
  const timeRaStr = item.thoiGianRa || item.thoiGianDongCa;

  let tenCa = item.caLamTen || "";
  let gioCa = "";
  let startHour = null;
  let startMin = 0;
  let endHour = null;
  let endMin = 0;

  if (item.gioBatDau && item.gioKetThuc) {
    gioCa = `${item.gioBatDau} - ${item.gioKetThuc}`;
    const [sH, sM] = item.gioBatDau.split(":").map(Number);
    const [eH, eM] = item.gioKetThuc.split(":").map(Number);
    startHour = sH;
    startMin = sM || 0;
    endHour = eH;
    endMin = eM || 0;
  }

  // Fallback nếu không có gioBatDau/gioKetThuc và có timeVaoStr
  if (startHour === null && timeVaoStr) {
    const dateVao = new Date(timeVaoStr);
    const hourVao = dateVao.getHours();
    if (hourVao < 13) {
      if (!tenCa) tenCa = "Ca Sáng";
      gioCa = "08:00 - 12:00";
      startHour = 8;
      startMin = 0;
      endHour = 12;
      endMin = 0;
    } else if (hourVao < 18) {
      if (!tenCa) tenCa = "Ca Chiều";
      gioCa = "13:00 - 17:00";
      startHour = 13;
      startMin = 0;
      endHour = 17;
      endMin = 0;
    } else {
      if (!tenCa) tenCa = "Ca Tối";
      gioCa = "17:30 - 21:30";
      startHour = 17;
      startMin = 30;
      endHour = 21;
      endMin = 30;
    }
  }

  let lateMin = 0;
  if (timeVaoStr && startHour !== null) {
    const dateVao = new Date(timeVaoStr);
    const actualVaoMinutes = dateVao.getHours() * 60 + dateVao.getMinutes();
    const targetVaoMinutes = startHour * 60 + startMin;
    lateMin = Math.max(0, actualVaoMinutes - targetVaoMinutes);
  }

  let earlyMin = 0;
  if (timeRaStr && endHour !== null) {
    const dateRa = new Date(timeRaStr);
    const actualRaMinutes = dateRa.getHours() * 60 + dateRa.getMinutes();
    const targetRaMinutes = endHour * 60 + endMin;
    earlyMin = Math.max(0, targetRaMinutes - actualRaMinutes);
  }

  return {
    tenCa: tenCa || "Ca làm việc",
    gioCa: gioCa || "—",
    lateMin,
    earlyMin
  };
};

const getShiftName = (item) => item.caLamTen || getShiftDetails(item).tenCa;

const getRevenue = (item) =>
  Number(item?.tienMatTrongCa || item?.tienMatGiaoCa || 0)
  + Number(item?.tienChuyenKhoanTrongCa || 0);

const hienThiTrangThai = (item) => {
  const trangThai = item?.trangThai;
  if (trangThai === "DA_BAN_HANG") return "Đã bán hàng";
  if (["MO_CA", "DANG_LAM", "0", 0].includes(trangThai)) return "Đang làm";
  if (["DA_BAN_GIAO", "HOAN_TAT", "1", 1].includes(trangThai)) return "Đã bàn giao";
  if (trangThai === "DA_KET_THUC") return "Đã kết thúc";
  if (["CHO_BAN_GIAO", "2", 2].includes(trangThai)) return "Chờ xác nhận";
  return trangThai || "—";
};

const setNgayMacDinh = () => {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, '0');
  const dd = String(today.getDate()).padStart(2, '0');
  const todayStr = `${yyyy}-${mm}-${dd}`;
  
  tuNgay.value = todayStr;
  denNgay.value = todayStr;
};

onMounted(async () => {
  setNgayMacDinh();
  danhSachCaLam.value = await layDanhSachCaLam();
  fetchLichSu();
});

// Chờ 500ms khi gõ tìm kiếm để gọi API (debounce)
let searchTimeout = null;
const onSearchInput = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    currentPage.value = 0;
    fetchLichSu();
  }, 500);
};

</script>

<template>
  <div class="space-y-6">
    <!-- Filter Section -->
    <div class="bg-white rounded-xl shadow-sm border border-slate-100 p-4 md:p-5 flex flex-col md:flex-row items-end gap-4">
      
      <!-- Tìm kiếm -->
      <div class="flex-1 w-full md:min-w-[250px]">
        <label class="block text-xs font-bold text-slate-600 mb-1.5 ml-1">Tìm kiếm</label>
        <div class="relative">
          <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search class="w-4 h-4 text-slate-400" />
          </div>
          <input 
            type="text" 
            v-model="searchQuery"
            @input="onSearchInput"
            placeholder="Tìm theo tài khoản / mã ca / mã hóa đơn..." 
            class="w-full pl-9 pr-4 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-rose-300 focus:ring-2 focus:ring-rose-100 transition-all bg-slate-50 focus:bg-white"
          />
        </div>
      </div>

      <!-- Lọc ca làm việc -->
      <div class="w-full md:w-40">
        <label class="block text-xs font-bold text-slate-600 mb-1.5 ml-1">Ca làm việc</label>
        <div class="relative">
          <select 
            v-model="selectedCaLam"
            class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-rose-300 focus:ring-2 focus:ring-rose-100 transition-all bg-slate-50 focus:bg-white appearance-none"
          >
            <option value="">Tất cả</option>
            <option v-for="ca in danhSachCaLam" :key="ca.id" :value="ca.id">
              {{ ca.ten }}
            </option>
          </select>
          <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
            <ChevronDown class="w-4 h-4 text-slate-400" />
          </div>
        </div>
      </div>

      <!-- Lọc vai trò -->
      <div class="w-full md:w-36">
        <label class="block text-xs font-bold text-slate-600 mb-1.5 ml-1">Vai trò</label>
        <div class="relative">
          <select 
            v-model="selectedVaiTro"
            class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-rose-300 focus:ring-2 focus:ring-rose-100 transition-all bg-slate-50 focus:bg-white appearance-none"
          >
            <option value="">Tất cả</option>
            <option value="1">Quản trị viên</option>
            <option value="2">Nhân viên</option>
          </select>
          <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
            <ChevronDown class="w-4 h-4 text-slate-400" />
          </div>
        </div>
      </div>

      <!-- Từ ngày -->
      <div class="w-full md:w-40">
        <label class="block text-xs font-bold text-slate-600 mb-1.5 ml-1">Từ ngày:</label>
        <div class="relative">
          <input 
            type="date" 
            v-model="tuNgay"
            class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-rose-300 focus:ring-2 focus:ring-rose-100 transition-all bg-white"
          />
        </div>
      </div>

      <!-- Đến ngày -->
      <div class="w-full md:w-40">
        <label class="block text-xs font-bold text-slate-600 mb-1.5 ml-1">Đến ngày:</label>
        <div class="relative">
          <input 
            type="date" 
            v-model="denNgay"
            class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-rose-300 focus:ring-2 focus:ring-rose-100 transition-all bg-white"
          />
        </div>
      </div>

      <!-- Nút đặt lại bộ lọc & Xuất Excel -->
      <div class="flex items-center gap-2">
        <Button variant="soft" @click="handleRefresh">
          <template #prefix><RotateCcw class="h-4 w-4" /></template>
          Đặt lại bộ lọc
        </Button>
        <button 
          @click="xuatExcel"
          class="flex items-center gap-2 px-4 py-2 bg-emerald-50 text-emerald-600 hover:bg-emerald-100 rounded-lg text-sm font-semibold transition-colors border border-emerald-100"
        >
          <Download class="w-4 h-4" />
          Xuất Excel
        </button>
      </div>

    </div>

    <!-- Table Section -->
    <div class="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
      <!-- Table Header -->
      <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/5 text-primary">
            <ListChecks class="h-5 w-5" />
          </div>
          <h2 class="admin-section-title">Danh sách hoạt động</h2>
        </div>
        <button 
          @click="fetchLichSu"
          class="h-8 w-8 rounded-full border border-slate-100 bg-white hover:bg-emerald-50 text-emerald-500 hover:text-emerald-600 flex items-center justify-center transition-all hover:scale-105 shadow-sm dark:border-slate-700 dark:bg-slate-800 dark:hover:bg-emerald-950/30"
          title="Tải lại dữ liệu"
        >
          <RefreshCw class="w-4 h-4" :class="dangTai ? 'animate-spin' : ''" />
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-slate-50/50 border-b border-slate-100 text-slate-500 text-[13px] font-semibold">
              <th class="py-3 px-4 w-16 text-center">STT</th>
              <th class="py-3 px-4 text-center">Mã nhân viên</th>
              <th class="py-3 px-4">Tên nhân viên</th>
              <th class="py-3 px-4 text-center">Vai trò</th>
              <th class="py-3 px-4 text-center">Ca / Hoạt động</th>
              <th class="py-3 px-4 text-center">Thời gian</th>
              <th class="py-3 px-4 text-right">Doanh thu</th>
              <th class="py-3 px-4 text-center whitespace-nowrap">Trạng thái</th>
              <th class="py-3 px-4">Ghi chú</th>
              <th class="py-3 px-4 text-center whitespace-nowrap">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai" class="border-b border-slate-100">
              <td colspan="10" class="py-8 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="danhSachHienThi.length === 0" class="border-b border-slate-100">
              <td colspan="10" class="py-8 text-center">
                <div class="flex flex-col items-center justify-center text-slate-400">
                  <FileText class="w-10 h-10 mb-2 opacity-50" />
                  <span class="text-sm">Không tìm thấy dữ liệu lịch sử hoạt động</span>
                </div>
              </td>
            </tr>
            <tr v-else v-for="(item, idx) in danhSachHienThi" :key="item.id || idx" class="border-b border-slate-100 hover:bg-slate-50/50 transition">
              <td class="py-4 px-4 text-[13px] text-slate-500 text-center">{{ currentPage * pageSize + idx + 1 }}</td>
              
              <!-- MÃ NHÂN VIÊN -->
              <td class="py-4 px-4 text-center">
                <span class="text-slate-500 font-semibold">{{ item.nhanVienTrongCaMa || item.nhanVien?.ma || item.maNhanVien || 'NV0000' }}</span>
              </td>

              <!-- TÊN NHÂN VIÊN -->
              <td class="py-4 px-4">
                <div class="flex items-center gap-3">
                  <div class="flex h-9 w-9 shrink-0 items-center justify-center overflow-hidden rounded-full bg-slate-100 text-slate-600 font-bold text-sm border border-slate-200 dark:bg-slate-800 dark:text-slate-400 dark:border-slate-700">
                    <img v-if="item.nhanVienTrongCaAnh || item.nhanVien?.anhDaiDien" :src="item.nhanVienTrongCaAnh || item.nhanVien?.anhDaiDien" class="h-full w-full object-cover" />
                    <span v-else>{{ (item.nhanVienTrongCaTen || item.nhanVien?.tenNhanVien || item.nhanVien?.hoTen || item.tenTaiKhoan || 'A')[0].toUpperCase() }}</span>
                  </div>
                  <div class="text-[14px] font-bold text-slate-700 dark:text-slate-200">
                    {{ item.nhanVienTrongCaTen || item.nhanVien?.tenNhanVien || item.nhanVien?.hoTen || item.tenTaiKhoan || 'Chưa xác định' }}
                  </div>
                </div>
              </td>

              <!-- VAI TRÒ -->
              <td class="py-4 px-4 text-center text-slate-600 font-medium">
                <span v-if="item.nhanVienTrongCaVaiTro === 1 || item.nhanVien?.vaiTro === 1">Quản trị viên</span>
                <span v-else>Nhân viên</span>
              </td>

              <!-- CA LÀM VIỆC -->
              <td class="py-4 px-4 text-center text-slate-600 font-medium">
                {{ getShiftName(item) }}
              </td>

              <!-- THỜI GIAN -->
              <td class="py-4 px-4">
                <div class="flex flex-col items-center justify-center text-[13px] text-slate-600">
                  <div class="flex items-center justify-center gap-1.5 mb-1 w-full">
                    <span>{{ formatDateTime(item.thoiGianVao || item.thoiGianMoCa) || '—' }}</span>
                  </div>
                  <div class="flex items-center justify-center gap-1.5 w-full">
                    <span>{{ formatDateTime(item.thoiGianRa || item.thoiGianDongCa) || '—' }}</span>
                  </div>
                </div>
              </td>

              <!-- DOANH THU CA -->
              <td class="py-4 px-4 text-right">
                <div class="text-[14px] font-bold text-slate-800 dark:text-slate-200">
                  {{ dinhDangTienViet(getRevenue(item)) }}
                </div>
              </td>

              <!-- TRẠNG THÁI -->
              <td class="py-4 px-4 text-center whitespace-nowrap">
                <span v-if="item.trangThai === 'DA_BAN_HANG'"
                      class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-blue-50 border border-blue-100 text-blue-600 dark:bg-blue-950/20 dark:border-blue-900/30 dark:text-blue-400">
                  Đã bán hàng
                </span>
                <span v-else-if="item.trangThai === 'MO_CA' || item.trangThai === 'DANG_LAM' || item.trangThai === '0' || item.trangThai === 0"
                      class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-orange-50 border border-orange-100 text-orange-500 dark:bg-orange-950/20 dark:border-orange-900/30 dark:text-orange-400">
                  Đang làm
                </span>
                <span v-else-if="item.trangThai === 'DA_BAN_GIAO' || item.trangThai === 'HOAN_TAT' || item.trangThai === '1' || item.trangThai === 1" 
                      class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-emerald-50 border border-emerald-100 text-emerald-500 dark:bg-emerald-950/20 dark:border-emerald-900/30 dark:text-emerald-400">
                  Đã bàn giao
                </span>
                <span v-else-if="item.trangThai === 'DA_KET_THUC'"
                      class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-slate-100 border border-slate-200 text-slate-600 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-300">
                  Đã kết thúc
                </span>
                <span v-else-if="item.trangThai === 'CHO_BAN_GIAO' || item.trangThai === '2' || item.trangThai === 2" 
                      class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-amber-50 border border-amber-100 text-amber-500 dark:bg-amber-950/20 dark:border-amber-900/30 dark:text-amber-400">
                  Chờ xác nhận
                </span>
                <span v-else class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-slate-50 border border-slate-100 text-slate-500 dark:bg-slate-900/30 dark:border-slate-800 dark:text-slate-400">
                  {{ item.trangThai || '—' }}
                </span>
              </td>

              <!-- GHI CHÚ -->
              <td class="py-4 px-4 text-[13px] text-slate-500 dark:text-slate-400">
                <div :title="item.ghiChu">
                  <span :class="!item.ghiChu ? 'italic text-slate-400' : ''">
                    {{ item.ghiChu || 'Không có' }}
                  </span>
                </div>
              </td>

              <!-- THAO TÁC -->
              <td class="py-4 px-4 text-center">
                <button
                  v-if="item.hoaDonId"
                  type="button"
                  @click="xemChiTietHoaDon(item)"
                  class="inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-primary/10 hover:text-primary dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-primary/20 shadow-sm"
                  title="Xem chi tiết hóa đơn"
                  aria-label="Xem chi tiết hóa đơn"
                >
                  <Eye class="h-4 w-4" />
                </button>
                <button
                  v-else
                  type="button"
                  @click="xemChiTietCaLam(item)"
                  class="inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-primary/10 hover:text-primary dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-primary/20 shadow-sm"
                  title="Xem chi tiết bàn giao ca"
                  aria-label="Xem chi tiết bàn giao ca"
                >
                  <Eye class="h-4 w-4" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <AdminTableFooter
        :current-page="currentPage"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total-items="totalElements"
        :total-pages="totalPages"
        zero-based
        compact
        no-margin
        show-refresh
        @refresh="fetchLichSu"
        @update:current-page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>
  </div>
</template>
