<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { Search, RefreshCw, Calendar as CalendarIcon, FileText, LogIn, LogOut, ListChecks, RotateCcw } from "lucide-vue-next";
import { layLichSuGiaoCa } from "../../../services/giao-ca.js";
import { dinhDangTienViet } from "../../../utils/dinhDangTien.js";
import { showError } from "../../../utils/alert.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import Button from "../../../components/ui/Button.vue";

// Trạng thái chung
const dangTai = ref(false);
const danhSachLichSu = ref([]);

// Bộ lọc
const tuNgay = ref("");
const denNgay = ref("");
const searchQuery = ref("");

// Danh sách hiển thị tự động lọc từ khóa (Client & Server dual-filter)
const danhSachHienThi = computed(() => {
  if (!searchQuery.value || !searchQuery.value.trim()) {
    return danhSachLichSu.value;
  }
  const q = searchQuery.value.trim().toLowerCase();
  return danhSachLichSu.value.filter((item) => {
    const tenNhanVien = (item.nhanVienTrongCaTen || item.nhanVien?.tenNhanVien || item.nhanVien?.hoTen || item.tenTaiKhoan || "").toLowerCase();
    const maNhanVien = (item.nhanVienTrongCaMa || item.nhanVien?.ma || item.maNhanVien || "").toLowerCase();
    const maCa = (item.ma || "").toLowerCase();
    const ghiChu = (item.ghiChu || "").toLowerCase();
    return tenNhanVien.includes(q) || maNhanVien.includes(q) || maCa.includes(q) || ghiChu.includes(q);
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
  tuNgay.value = "";
  denNgay.value = "";
  searchQuery.value = "";
  currentPage.value = 0;
  fetchLichSu();
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

const getShiftDetails = (timeVaoStr, timeRaStr) => {
  if (!timeVaoStr) {
    return {
      tenCa: "Ca làm việc",
      gioCa: "—",
      lateMin: 0,
      earlyMin: 0
    };
  }

  const dateVao = new Date(timeVaoStr);
  const hourVao = dateVao.getHours();
  const minVao = dateVao.getMinutes();

  let tenCa = "";
  let gioCa = "";
  let startHour = 0;
  let startMin = 0;
  let endHour = 0;
  let endMin = 0;

  if (hourVao < 13) {
    tenCa = "Ca Sáng";
    gioCa = "08:00 - 13:00";
    startHour = 8;
    startMin = 0;
    endHour = 13;
    endMin = 0;
  } else if (hourVao < 18) {
    tenCa = "Ca Chiều";
    gioCa = "13:00 - 18:00";
    startHour = 13;
    startMin = 0;
    endHour = 18;
    endMin = 0;
  } else {
    tenCa = "Ca Tối";
    gioCa = "19:00 - 23:00";
    startHour = 19;
    startMin = 0;
    endHour = 23;
    endMin = 0;
  }

  const actualVaoMinutes = hourVao * 60 + minVao;
  const targetVaoMinutes = startHour * 60 + startMin;
  const lateMin = Math.max(0, actualVaoMinutes - targetVaoMinutes);

  let earlyMin = 0;
  if (timeRaStr) {
    const dateRa = new Date(timeRaStr);
    const hourRa = dateRa.getHours();
    const minRa = dateRa.getMinutes();

    const actualRaMinutes = hourRa * 60 + minRa;
    const targetRaMinutes = endHour * 60 + endMin;
    earlyMin = Math.max(0, targetRaMinutes - actualRaMinutes);
  }

  return {
    tenCa,
    gioCa,
    lateMin,
    earlyMin
  };
};

onMounted(() => {
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
    <!-- Header -->
    <div>
      <h1 class="text-[20px] font-bold text-slate-800">Lịch sử hoạt động</h1>
      <p class="text-[13px] text-slate-500 mt-1">Theo dõi lịch sử đóng/mở ca và dòng tiền mặt trong két</p>
    </div>

    <!-- Filter Section -->
    <div class="bg-white rounded-xl shadow-sm border border-slate-100 p-4 md:p-5 flex flex-col md:flex-row items-end gap-4">
      
      <!-- Tìm kiếm -->
      <div class="flex-1 w-full">
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

      <!-- Từ ngày -->
      <div class="w-full md:w-48">
        <label class="block text-xs font-bold text-slate-600 mb-1.5 ml-1">Từ ngày:</label>
        <div class="relative">
          <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
            <CalendarIcon class="w-4 h-4 text-slate-400" />
          </div>
          <input 
            type="date" 
            v-model="tuNgay"
            class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-rose-300 focus:ring-2 focus:ring-rose-100 transition-all bg-white"
          />
        </div>
      </div>

      <!-- Đến ngày -->
      <div class="w-full md:w-48">
        <label class="block text-xs font-bold text-slate-600 mb-1.5 ml-1">Đến ngày:</label>
        <div class="relative">
          <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
            <CalendarIcon class="w-4 h-4 text-slate-400" />
          </div>
          <input 
            type="date" 
            v-model="denNgay"
            class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-rose-300 focus:ring-2 focus:ring-rose-100 transition-all bg-white"
          />
        </div>
      </div>

      <!-- Nút đặt lại bộ lọc -->
      <Button variant="soft" @click="handleRefresh">
        <template #prefix><RotateCcw class="h-4 w-4" /></template>
        Đặt lại bộ lọc
      </Button>

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
              <th class="py-3 px-4 min-w-[220px]">Nhân viên / Ca làm việc</th>
              <th class="py-3 px-4 text-center">Vào ca</th>
              <th class="py-3 px-4 text-center">Ra ca</th>
              <th class="py-3 px-4 text-right">Doanh thu ca</th>
              <th class="py-3 px-4 text-center">Trạng thái</th>
              <th class="py-3 px-4 min-w-[150px]">Ghi chú</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai" class="border-b border-slate-100">
              <td colspan="7" class="py-8 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="danhSachHienThi.length === 0" class="border-b border-slate-100">
              <td colspan="7" class="py-8 text-center">
                <div class="flex flex-col items-center justify-center text-slate-400">
                  <FileText class="w-10 h-10 mb-2 opacity-50" />
                  <span class="text-sm">Không tìm thấy dữ liệu lịch sử hoạt động</span>
                </div>
              </td>
            </tr>
            <tr v-else v-for="(item, idx) in danhSachHienThi" :key="item.id || idx" class="border-b border-slate-100 hover:bg-slate-50/50 transition">
              <td class="py-4 px-4 text-[13px] text-slate-500 text-center">{{ currentPage * pageSize + idx + 1 }}</td>
              
              <!-- NHÂN VIÊN / CA LÀM VIỆC -->
              <td class="py-4 px-4">
                <div class="flex items-center gap-3">
                  <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 font-bold text-sm border border-emerald-100 dark:bg-emerald-950/30 dark:text-emerald-400 dark:border-emerald-900/30">
                    {{ (item.nhanVienTrongCaTen || item.nhanVien?.tenNhanVien || item.nhanVien?.hoTen || item.tenTaiKhoan || 'A')[0].toUpperCase() }}
                  </div>
                  <div>
                    <div class="text-[14px] font-bold text-slate-700 dark:text-slate-200">
                      {{ item.nhanVienTrongCaTen || item.nhanVien?.tenNhanVien || item.nhanVien?.hoTen || item.tenTaiKhoan || 'Chưa xác định' }}
                      <span class="text-slate-500 font-semibold">({{ item.nhanVienTrongCaMa || item.nhanVien?.ma || item.maNhanVien || 'NV0000' }})</span>
                    </div>
                    <div class="text-[12px] text-slate-400 mt-0.5">
                      {{ getShiftDetails(item.thoiGianVao || item.thoiGianMoCa, item.thoiGianRa || item.thoiGianDongCa).tenCa }}
                      ({{ getShiftDetails(item.thoiGianVao || item.thoiGianMoCa, item.thoiGianRa || item.thoiGianDongCa).gioCa }})
                    </div>
                  </div>
                </div>
              </td>

              <!-- VÀO CA -->
              <td class="py-4 px-4">
                <div v-if="item.thoiGianVao || item.thoiGianMoCa" class="flex flex-col items-center justify-center text-center">
                  <div class="flex items-center gap-1.5 text-[13px] text-slate-700 dark:text-slate-300 font-medium">
                    <LogIn class="w-4 h-4 text-emerald-500" />
                    <span>{{ formatDateTime(item.thoiGianVao || item.thoiGianMoCa) }}</span>
                  </div>
                  <span v-if="getShiftDetails(item.thoiGianVao || item.thoiGianMoCa, item.thoiGianRa || item.thoiGianDongCa).lateMin > 0" 
                        class="mt-1 px-2 py-0.5 rounded text-[10px] font-bold bg-rose-50 border border-rose-100 text-rose-500 dark:bg-rose-950/20 dark:border-rose-900/30 dark:text-rose-400">
                    Đi muộn {{ getShiftDetails(item.thoiGianVao || item.thoiGianMoCa, item.thoiGianRa || item.thoiGianDongCa).lateMin }} phút
                  </span>
                </div>
                <div v-else class="text-center text-slate-400">—</div>
              </td>

              <!-- RA CA -->
              <td class="py-4 px-4">
                <div v-if="item.thoiGianRa || item.thoiGianDongCa" class="flex flex-col items-center justify-center text-center">
                  <div class="flex items-center gap-1.5 text-[13px] text-slate-700 dark:text-slate-300 font-medium">
                    <LogOut class="w-4 h-4 text-amber-500" />
                    <span>{{ formatDateTime(item.thoiGianRa || item.thoiGianDongCa) }}</span>
                  </div>
                  <span v-if="getShiftDetails(item.thoiGianVao || item.thoiGianMoCa, item.thoiGianRa || item.thoiGianDongCa).earlyMin > 0" 
                        class="mt-1 px-2 py-0.5 rounded text-[10px] font-bold bg-amber-50 border border-amber-100 text-amber-600 dark:bg-amber-950/20 dark:border-amber-900/30 dark:text-amber-400">
                    Ra sớm {{ getShiftDetails(item.thoiGianVao || item.thoiGianMoCa, item.thoiGianRa || item.thoiGianDongCa).earlyMin }} phút
                  </span>
                </div>
                <div v-else class="text-center text-slate-400">—</div>
              </td>

              <!-- DOANH THU CA -->
              <td class="py-4 px-4 text-right">
                <div class="text-[14px] font-bold text-slate-800 dark:text-slate-200">
                  {{ dinhDangTienViet((item.tienMatTrongCa || item.tienMatGiaoCa || 0) + (item.tienChuyenKhoanTrongCa || 0)) }}
                </div>
                <div class="text-[11px] text-emerald-600 dark:text-emerald-400 mt-0.5">
                  Tiền mặt: {{ dinhDangTienViet(item.tienMatTrongCa || item.tienMatGiaoCa || 0) }}
                </div>
                <div class="text-[11px] text-blue-600 dark:text-blue-400 mt-0.5">
                  Chuyển khoản: {{ dinhDangTienViet(item.tienChuyenKhoanTrongCa || 0) }}
                </div>
              </td>

              <!-- TRẠNG THÁI -->
              <td class="py-4 px-4 text-center">
                <span v-if="item.trangThai === 'MO_CA' || item.trangThai === 'DANG_LAM' || item.trangThai === '0' || item.trangThai === 0" 
                      class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-orange-50 border border-orange-100 text-orange-500 dark:bg-orange-950/20 dark:border-orange-900/30 dark:text-orange-400">
                  Đang làm
                </span>
                <span v-else-if="item.trangThai === 'DA_BAN_GIAO' || item.trangThai === 'HOAN_TAT' || item.trangThai === '1' || item.trangThai === 1" 
                      class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-emerald-50 border border-emerald-100 text-emerald-500 dark:bg-emerald-950/20 dark:border-emerald-900/30 dark:text-emerald-400">
                  Đã kết ca
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
                <span :class="!item.ghiChu ? 'italic text-slate-400' : ''">
                  {{ item.ghiChu || 'Không có' }}
                </span>
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
