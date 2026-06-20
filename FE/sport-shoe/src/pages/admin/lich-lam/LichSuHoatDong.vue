<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { Search, RefreshCw, Calendar as CalendarIcon, FileText } from "lucide-vue-next";
import { layLichSuGiaoCa } from "../../../services/giao-ca.js";
import { dinhDangTienViet } from "../../../utils/dinhDangTien.js";
import { showError } from "../../../utils/alert.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";

// Trạng thái chung
const dangTai = ref(false);
const danhSachLichSu = ref([]);

// Bộ lọc
const tuNgay = ref("");
const denNgay = ref("");
const searchQuery = ref("");

// Pagination
const currentPage = ref(0);
const pageSize = ref(10);
const totalElements = ref(0);
const totalPages = ref(1);

// Lấy danh sách lịch sử
const fetchLichSu = async () => {
  dangTai.value = true;
  try {
    const filters = {
      tuNgay: tuNgay.value ? new Date(`${tuNgay.value}T00:00:00`).toISOString() : undefined,
      denNgay: denNgay.value ? new Date(`${denNgay.value}T23:59:59.999`).toISOString() : undefined,
      page: currentPage.value,
      size: pageSize.value,
      // Backend có thể không nhận keyword, nhưng ta cứ truyền nếu có cập nhật API sau này
      keyword: searchQuery.value || undefined 
    };
    
    const response = await layLichSuGiaoCa(filters);
    
    if (response) {
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

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < totalPages.value) {
    currentPage.value = newPage;
    fetchLichSu();
  }
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

onMounted(() => {
  // Lấy ngày hôm nay làm mặc định cho Từ ngày - Đến ngày
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, '0');
  const dd = String(today.getDate()).padStart(2, '0');
  const todayStr = `${yyyy}-${mm}-${dd}`;
  
  tuNgay.value = todayStr;
  denNgay.value = todayStr;
  
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

      <!-- Nút Tải lại -->
      <div class="w-full md:w-auto">
        <button 
          @click="handleRefresh"
          class="w-full md:w-auto flex items-center justify-center gap-2 px-5 py-2 border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 text-sm font-bold rounded-lg transition-all"
        >
          <RefreshCw class="w-4 h-4" :class="dangTai ? 'animate-spin text-rose-500' : ''" />
          Tải lại
        </button>
      </div>

    </div>

    <!-- Table Section -->
    <div class="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-100">
              <th class="py-3 px-4 text-[12px] font-bold text-slate-500 uppercase tracking-wider w-16 text-center">#</th>
              <th class="py-3 px-4 text-[12px] font-bold text-slate-500 uppercase tracking-wider">Tài khoản</th>
              <th class="py-3 px-4 text-[12px] font-bold text-slate-500 uppercase tracking-wider text-center">Ca</th>
              <th class="py-3 px-4 text-[12px] font-bold text-slate-500 uppercase tracking-wider text-center">Mở</th>
              <th class="py-3 px-4 text-[12px] font-bold text-slate-500 uppercase tracking-wider text-center">Đóng</th>
              <th class="py-3 px-4 text-[12px] font-bold text-slate-500 uppercase tracking-wider text-right">Tiền mặt</th>
              <th class="py-3 px-4 text-[12px] font-bold text-slate-500 uppercase tracking-wider text-right">Chênh lệch</th>
              <th class="py-3 px-4 text-[12px] font-bold text-slate-500 uppercase tracking-wider text-center">Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="dangTai" class="border-b border-slate-100">
              <td colspan="8" class="py-8 text-center text-sm text-slate-400">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="danhSachLichSu.length === 0" class="border-b border-slate-100">
              <td colspan="8" class="py-8 text-center">
                <div class="flex flex-col items-center justify-center text-slate-400">
                  <FileText class="w-10 h-10 mb-2 opacity-50" />
                  <span class="text-sm">Không tìm thấy dữ liệu lịch sử hoạt động</span>
                </div>
              </td>
            </tr>
            <tr v-else v-for="(item, idx) in danhSachLichSu" :key="item.id || idx" class="border-b border-slate-100 hover:bg-slate-50/50 transition">
              <td class="py-3 px-4 text-[13px] text-slate-500 text-center">{{ currentPage * pageSize + idx + 1 }}</td>
              
              <!-- TÀI KHOẢN -->
              <td class="py-3 px-4">
                <div class="text-[14px] font-bold text-slate-700">{{ item.nhanVien?.tenTaiKhoan || item.nhanVien?.tenNhanVien || item.tenTaiKhoan || 'admin' }}</div>
                <div class="text-[12px] text-slate-400 mt-0.5">
                  Mã ca: {{ item.maCa || item.id || '—' }}
                </div>
              </td>

              <!-- CA -->
              <td class="py-3 px-4 text-center">
                <span class="inline-block px-3 py-1 rounded-full text-[12px] font-bold" 
                      :class="item.caLam?.tenCa === 'ADMIN' ? 'bg-rose-50 text-rose-500' : 'bg-slate-100 text-slate-600'">
                  {{ item.caLam?.tenCa || item.tenCa || '—' }}
                </span>
              </td>

              <!-- MỞ -->
              <td class="py-3 px-4 text-center">
                <div v-if="item.thoiGianVao || item.thoiGianMoCa">
                  <div class="text-[13px] font-bold text-slate-800">{{ formatTime(item.thoiGianVao || item.thoiGianMoCa).time }}</div>
                  <div class="text-[11px] text-slate-400">{{ formatTime(item.thoiGianVao || item.thoiGianMoCa).day }}</div>
                </div>
                <div v-else class="text-slate-400">—</div>
              </td>

              <!-- ĐÓNG -->
              <td class="py-3 px-4 text-center">
                <div v-if="item.thoiGianRa || item.thoiGianDongCa">
                  <div class="text-[13px] font-bold text-slate-800">{{ formatTime(item.thoiGianRa || item.thoiGianDongCa).time }}</div>
                  <div class="text-[11px] text-slate-400">{{ formatTime(item.thoiGianRa || item.thoiGianDongCa).day }}</div>
                </div>
                <div v-else class="text-slate-400">—</div>
              </td>

              <!-- TIỀN MẶT -->
              <td class="py-3 px-4 text-right text-[13px] font-bold text-slate-800">
                {{ item.tienMatGiaoCa != null ? dinhDangTienViet(item.tienMatGiaoCa) : (item.tongTienBanGiao != null ? dinhDangTienViet(item.tongTienBanGiao) : '0 đ') }}
              </td>

              <!-- CHÊNH LỆCH -->
              <td class="py-3 px-4 text-right text-[13px] font-bold" :class="(item.tienChenhLech && item.tienChenhLech < 0) ? 'text-rose-500' : 'text-emerald-500'">
                {{ item.tienChenhLech != null ? dinhDangTienViet(item.tienChenhLech) : '—' }}
              </td>

              <!-- TRẠNG THÁI -->
              <td class="py-3 px-4 text-center">
                <span v-if="item.trangThai === 0 || item.trangThai === 'DANG_LAM'" class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-orange-50 border border-orange-100 text-orange-500">
                  Đang làm
                </span>
                <span v-else-if="item.trangThai === 1 || item.trangThai === 'HOAN_TAT'" class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-emerald-50 border border-emerald-100 text-emerald-500">
                  Hoàn tất
                </span>
                <span v-else class="inline-block px-3 py-1 rounded-full text-[12px] font-bold bg-slate-50 border border-slate-100 text-slate-500">
                  {{ item.trangThai === 2 ? 'Cho xác nhận' : item.trangThai || '—' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Phân trang -->
      <div v-if="totalPages > 1" class="border-t border-slate-100 p-4 flex items-center justify-between">
        <span class="text-sm text-slate-500">
          Hiển thị trang <span class="font-bold text-slate-700">{{ currentPage + 1 }}</span> / {{ totalPages }}
        </span>
        <div class="flex items-center gap-1">
          <button 
            @click="changePage(currentPage - 1)" 
            :disabled="currentPage === 0"
            class="px-3 py-1.5 rounded-lg border border-slate-200 text-sm font-medium hover:bg-slate-50 disabled:opacity-50 transition-colors"
          >
            Trang trước
          </button>
          <div class="flex items-center gap-1 px-2">
            <template v-for="page in totalPages" :key="page">
              <button 
                v-if="page - 1 === currentPage || page - 1 === 0 || page - 1 === totalPages - 1 || (page - 1 >= currentPage - 1 && page - 1 <= currentPage + 1)"
                @click="changePage(page - 1)"
                class="w-8 h-8 flex items-center justify-center rounded-lg text-sm font-medium transition-colors"
                :class="page - 1 === currentPage ? 'bg-rose-500 text-white shadow-sm' : 'hover:bg-slate-100 text-slate-600'"
              >
                {{ page }}
              </button>
              <span v-else-if="page - 1 === currentPage - 2 || page - 1 === currentPage + 2" class="px-1 text-slate-400">...</span>
            </template>
          </div>
          <button 
            @click="changePage(currentPage + 1)" 
            :disabled="currentPage === totalPages - 1"
            class="px-3 py-1.5 rounded-lg border border-slate-200 text-sm font-medium hover:bg-slate-50 disabled:opacity-50 transition-colors"
          >
            Trang sau
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
