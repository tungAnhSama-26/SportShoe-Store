<script setup>
import { ref, computed, watch } from "vue";
import { useRoute } from "vue-router";
import { 
  ArrowRightLeft, 
  CalendarDays, 
  CheckCircle2, 
  Clock, 
  SlidersHorizontal,
  Calculator,
  UserCheck,
  UserX,
  FileSpreadsheet,
  AlertTriangle,
  RotateCcw,
  Check,
  X as CloseIcon
} from "lucide-vue-next";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";

import { useAdminSession } from "../../../composable/useAdminSession";

const route = useRoute();
const { adminSession } = useAdminSession();
const getLastWordOfName = computed(() => {
  const parts = (adminSession.value.hoTen || "Nhân viên").trim().split(/\s+/);
  return parts[parts.length - 1];
});
const isMoCaSángSớmMode = computed(() => route.path === "/admin/mo-ca");

const isAdmin = computed(() => adminSession.value.vaiTro === "Quản trị viên");
const forceStaffViewForAdmin = ref(false);
const showAdminView = computed(() => isAdmin.value && !forceStaffViewForAdmin.value);

const adminRejectionReason = ref("");
const selectedHachToan = ref("tru-luong");
const isCaDaChot = ref(false);

function pheDuyetVaChotSo() {
  showConfirm(
    "Hành động này sẽ chốt số liệu ca làm việc này và không thể hoàn tác.",
    "Phê duyệt & chốt số ca này?"
  ).then((confirmed) => {
    if (confirmed) {
      isCaDaChot.value = true;
      showSuccess("Đã phê duyệt đối soát và chốt số ca thành công!");
    }
  });
}

function cuongCheKetThucCa() {
  showConfirm(
    `Xác nhận cưỡng chế kết thúc ca làm việc của ${adminSession.value.hoTen}?`,
    "Cưỡng chế kết thúc ca?"
  ).then((confirmed) => {
    if (confirmed) {
      isCaDaChot.value = true;
      showSuccess("Đã cưỡng chế kết thúc ca làm việc!");
    }
  });
}

// Active step control for interactive exploration:
// 1: Mở ca (active), 2: Bàn giao, 3: Xác nhận, 4: Hoàn thành
const buocHienTai = ref(1);

// Mở ca state
const tienMoCaThucTe = ref(5000000);
const ghiChuMoCa = ref("");
const showsIncidentModal = ref(false);
const lyDoSucos = ref("");

// Mở ca sáng sớm states
const tienMoCaSángSớm = ref(15000000);
const ghiChuMoCaSángSớm = ref("");

const currentDateFormatted = computed(() => {
  const d = new Date();
  const day = String(d.getDate()).padStart(2, '0');
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const year = d.getFullYear();
  return `${day}/${month}/${year}`;
});

// Denomination state for cash calculator popup
const showsCalculator = ref(false);
const denominations = ref([
  { value: 500000, count: 0 },
  { value: 200000, count: 0 },
  { value: 100000, count: 0 },
  { value: 50000, count: 0 },
  { value: 20000, count: 0 },
  { value: 10000, count: 0 },
  { value: 5000, count: 0 },
  { value: 2000, count: 0 },
  { value: 1000, count: 0 },
]);

// Real-time actual cash calculated by calculator
const calculatedCash = computed(() => {
  return denominations.value.reduce((sum, item) => sum + item.value * (item.count || 0), 0);
});

// Format currency
function formatVND(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value || 0).replace("₫", "đ");
}

// Financial stats
const tienDauCa = ref(5000000);
const doanhThuTienMat = ref(12450000);
const tongThuKhac = ref(250000);
const tongChiTrongCa = ref(-150000);
const tongHoanTien = ref(-200000);

const tienTheoHeThong = computed(() => {
  return tienDauCa.value + doanhThuTienMat.value + tongThuKhac.value + tongChiTrongCa.value + tongHoanTien.value;
});

// Tiền thực tế starting value is 15050000
const tienThucTe = ref(15050000);

const chenhLech = computed(() => {
  return tienThucTe.value - tienTheoHeThong.value;
});

// Form state
const nhanVienNhan = ref("Trần Thị B");
const ghiChu = ref("Bàn giao đầy đủ tiền mặt và chứng từ trong ca.");

// Rejection state for Step 3
const lyDoTuChoi = ref("");
const showsRejectionModal = ref(false);

// Helper to convert number to Vietnamese words
function docTienBangChu(money) {
  if (money === 0) return "Không đồng";
  
  const textDenominations = ["", " nghìn", " triệu", " tỷ"];
  const digits = ["không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"];
  
  let str = "";
  let tempMoney = Math.abs(money);
  let groupCount = 0;
  
  while (tempMoney > 0) {
    let group = tempMoney % 1000;
    tempMoney = Math.floor(tempMoney / 1000);
    
    if (group > 0 || groupCount === 0) {
      let groupStr = "";
      let hundreds = Math.floor(group / 100);
      let tens = Math.floor((group % 100) / 10);
      let units = group % 10;
      
      if (hundreds > 0 || (tempMoney > 0)) {
        groupStr += digits[hundreds] + " trăm ";
      }
      
      if (tens > 0) {
        if (tens === 1) groupStr += "mười ";
        else groupStr += digits[tens] + " mươi ";
      } else if (hundreds > 0 && units > 0) {
        groupStr += "lẻ ";
      }
      
      if (units > 0) {
        if (units === 1 && tens > 1) groupStr += "mốt";
        else if (units === 5 && tens > 0) groupStr += "lăm";
        else groupStr += digits[units];
      }
      
      str = groupStr + textDenominations[groupCount] + " " + str;
    }
    groupCount++;
  }
  
  str = str.trim();
  if (str.length > 0) {
    str = str.charAt(0).toUpperCase() + str.slice(1);
  }
  return `(Bằng chữ: ${str} đồng)`;
}

// Reset denominations count
function resetCalculator() {
  denominations.value.forEach(item => item.count = 0);
}

// Apply cash calculator result
function applyCalculatedCash() {
  if (isMoCaSángSớmMode.value) {
    tienMoCaSángSớm.value = calculatedCash.value;
  } else {
    tienThucTe.value = calculatedCash.value;
  }
  showsCalculator.value = false;
  showSuccess(`Đã áp dụng số tiền đếm được: ${formatVND(calculatedCash.value)}`);
}

// Actions
function xacNhanMoCaSángSớmBtn() {
  if (tienMoCaSángSớm.value < 0) {
    showError("Số tiền mở ca không được âm");
    return;
  }
  showConfirm(
    `Số tiền mặt có trong két đếm được là: <strong>${formatVND(tienMoCaSángSớm.value)}</strong>`,
    "Xác nhận mở ca làm việc?"
  ).then((confirmed) => {
    if (confirmed) {
      showSuccess("Mở ca làm việc thành công!");
    }
  });
}

function xacNhanMoCa() {
  if (tienMoCaThucTe.value < 0) {
    showError("Số tiền mở ca không được âm");
    return;
  }
  showConfirm(
    `Số tiền bàn giao đầu ca thực tế đếm được là: <strong>${formatVND(tienMoCaThucTe.value)}</strong>`,
    "Xác nhận mở ca làm việc?"
  ).then((confirmed) => {
    if (confirmed) {
      tienDauCa.value = tienMoCaThucTe.value;
      buocHienTai.value = 2; // Proceed to Step 2
      showSuccess("Mở ca làm việc thành công!");
    }
  });
}

function guiBaoCaoSuCo() {
  if (!lyDoSucos.value.trim()) {
    showError("Vui lòng mô tả tình trạng két tiền");
    return;
  }
  showsIncidentModal.value = false;
  showSuccess("Đã gửi báo cáo khẩn cấp đến Quản lý hệ thống thành công!");
  lyDoSucos.value = "";
}

function xacNhanBanGiao() {
  showConfirm(
    "Xác nhận bàn giao ca?",
    "Thông tin bàn giao sẽ được chuyển đến Trần Thị B xác nhận.",
    () => {
      buocHienTai.value = 3;
      showSuccess("Đã gửi yêu cầu bàn giao ca!");
    }
  );
}

function dongYNhantCa() {
  showConfirm(
    "Đồng ý nhận bàn giao ca?",
    "Bạn sẽ chính thức tiếp quản ca làm việc tiếp theo.",
    () => {
      buocHienTai.value = 4;
      showSuccess("Nhận bàn giao ca thành công!");
    }
  );
}

function submitTuChoi() {
  if (!lyDoTuChoi.value.trim()) {
    showError("Vui lòng nhập lý do từ chối bàn giao");
    return;
  }
  showsRejectionModal.value = false;
  showSuccess("Đã từ chối nhận bàn giao ca!");
  buocHienTai.value = 2; // Return to editing
}

function huyBanGiao() {
  showConfirm(
    "Hủy bàn giao ca?",
    "Hành động này sẽ xóa dữ liệu nháp hiện tại.",
    () => {
      tienThucTe.value = 15000000;
      ghiChu.value = "";
      showSuccess("Đã hủy bàn giao ca.");
    }
  );
}
</script>

<template>
  <div class="space-y-6 max-w-7xl mx-auto">
    <!-- SCREEN B: MỞ CA SÁNG SỚM -->
    <div v-if="isMoCaSángSớmMode" class="flex items-center justify-center min-h-[70vh]">
      <div class="w-full max-w-lg bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl p-8 space-y-6">
        
        <!-- Header -->
        <div class="text-center space-y-2">
          <h2 class="text-xl font-bold tracking-tight text-slate-800 dark:text-white uppercase">
            MỞ CA LÀM VIỆC - CA SÁNG ({{ currentDateFormatted }})
          </h2>
          <div class="text-sm font-semibold text-slate-500 dark:text-slate-400">
            Thu ngân: <span class="text-slate-800 dark:text-white font-bold">{{ adminSession.hoTen }}</span>
          </div>
          <div>
            <button 
              type="button"
              @click="showsIncidentModal = true" 
              class="text-xs font-semibold text-rose-600 hover:text-rose-700 hover:underline inline-flex items-center gap-1 transition-colors mt-1"
            >
              <AlertTriangle class="h-4 w-4" />
              Cảnh báo: Két sắt có dấu hiệu bị cạy phá
            </button>
          </div>
        </div>

        <!-- Form fields -->
        <div class="space-y-5">
          <!-- Input tiền mặt -->
          <div>
            <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Nhập số tiền mặt có trong két <span class="text-rose-500">*</span>
            </label>
            
            <div class="flex items-center gap-3">
              <div class="relative flex-1">
                <input 
                  type="number"
                  v-model.number="tienMoCaSángSớm"
                  min="0"
                  placeholder="Nhập số tiền mặt có trong két..."
                  class="w-full h-12 pl-4 pr-12 rounded-2xl border border-slate-200 dark:border-slate-700 bg-transparent text-lg font-bold text-slate-800 dark:text-white focus:outline-none focus:border-rose-400 transition"
                />
                <span class="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-slate-400">đ</span>
              </div>
              
              <!-- Calculator icon -->
              <button 
                type="button" 
                @click="showsCalculator = true"
                title="Mở bảng tính đếm tiền"
                class="h-12 w-12 rounded-2xl bg-primary/10 hover:bg-primary/20 text-primary flex items-center justify-center transition shadow-sm"
              >
                <Calculator class="h-6 w-6" />
              </button>
            </div>
            
            <!-- Diễn giải bằng chữ -->
            <p class="mt-2 text-xs italic text-slate-500 dark:text-slate-400 leading-normal">
              {{ docTienBangChu(tienMoCaSángSớm) }}
            </p>
          </div>

          <!-- Ghi chú -->
          <div>
            <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Ghi chú
            </label>
            <textarea 
              v-model="ghiChuMoCaSángSớm" 
              rows="3" 
              maxlength="200"
              class="w-full p-3.5 border border-slate-200 dark:border-slate-700 rounded-2xl bg-transparent text-sm text-slate-700 dark:text-slate-200 focus:outline-none focus:border-rose-400 transition"
              placeholder="Nhập ghi chú mở ca..."
            ></textarea>
          </div>

          <!-- Submit Button -->
          <button 
            type="button"
            @click="xacNhanMoCaSángSớmBtn"
            class="w-full py-4 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-2xl shadow-lg transition text-sm uppercase tracking-wider"
          >
            BẮT ĐẦU CA LÀM VIỆC
          </button>
        </div>

      </div>
    </div>

    <!-- SCREEN C: ADMIN HANDOVER VIEW -->
    <div v-else-if="showAdminView" class="space-y-6">
      <!-- Breadcrumb & Role Switcher -->
      <div class="flex items-center justify-between text-xs text-slate-400">
        <div class="flex items-center gap-1">
          <span>Quản lý lịch làm</span>
          <span>/</span>
          <span>Bàn giao ca</span>
          <span>/</span>
          <span class="font-bold text-slate-600 dark:text-slate-300">Chi tiết ca: CA20250620001</span>
        </div>
        <button 
          @click="forceStaffViewForAdmin = true" 
          class="text-primary hover:underline font-semibold flex items-center gap-1 transition"
        >
          <RotateCcw class="h-3 w-3" />
          Xem với vai trò Nhân viên
        </button>
      </div>

      <!-- Header -->
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div class="flex items-center gap-3">
          <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
            Chi tiết ca làm việc
          </h1>
          <span 
            v-if="!isCaDaChot" 
            class="px-3 py-1 text-xs font-bold rounded-full bg-rose-50 text-rose-600 border border-rose-200 dark:bg-rose-950/40 dark:text-rose-400 dark:border-rose-800/40"
          >
            Tranh chấp: Lệch tiền
          </span>
          <span 
            v-else 
            class="px-3 py-1 text-xs font-bold rounded-full bg-emerald-50 text-emerald-600 border border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-400 dark:border-emerald-800/40"
          >
            Đã đối soát &amp; chốt số
          </span>
        </div>

        <button 
          v-if="!isCaDaChot"
          @click="cuongCheKetThucCa"
          class="h-10 px-4 bg-slate-900 hover:bg-slate-800 dark:bg-slate-700 dark:hover:bg-slate-600 text-white text-xs font-bold rounded-xl shadow-md transition flex items-center gap-1.5"
        >
          <CloseIcon class="h-4 w-4" />
          Cưỡng chế kết thúc ca
        </button>
      </div>

      <!-- Warning Banner -->
      <div 
        v-if="!isCaDaChot" 
        class="bg-rose-50/50 dark:bg-rose-950/10 border-l-4 border-rose-500 p-4 rounded-r-2xl flex gap-3 text-sm text-rose-800 dark:text-rose-300 shadow-sm"
      >
        <AlertTriangle class="h-5 w-5 text-rose-600 dark:text-rose-400 shrink-0 mt-0.5" />
        <div>
          <h4 class="font-bold text-rose-900 dark:text-rose-200">Phát hiện bất thường tài chính!</h4>
          <p class="mt-1 text-xs leading-relaxed text-slate-600 dark:text-slate-400">
            Số tiền mặt thu ngân {{ adminSession.hoTen }} khai báo nộp két đang hụt 2.300.000 đ so với dữ liệu ghi nhận trên phần mềm. Yêu cầu Quản trị viên đối soát trước khi chốt số.
          </p>
        </div>
      </div>
      <div 
        v-else 
        class="bg-emerald-50/50 dark:bg-emerald-950/10 border-l-4 border-emerald-500 p-4 rounded-r-2xl flex gap-3 text-sm text-emerald-800 dark:text-emerald-300 shadow-sm"
      >
        <CheckCircle2 class="h-5 w-5 text-emerald-600 dark:text-emerald-400 shrink-0 mt-0.5" />
        <div>
          <h4 class="font-bold text-emerald-900 dark:text-emerald-200">Đã chốt số ca làm việc!</h4>
          <p class="mt-1 text-xs leading-relaxed text-slate-600 dark:text-slate-400">
            Quản trị viên đã phê duyệt đối soát chênh lệch ca này thành công. Trạng thái ca: Hoàn thành.
          </p>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Left 2 Columns -->
        <div class="lg:col-span-2 space-y-6">
          
          <!-- DỮ LIỆU KHAI BÁO TỪ THU NGÂN -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-5">
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider">
              Dữ liệu khai báo từ thủ ngân
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-3.5 text-sm">
              <div class="flex justify-between border-b border-slate-50 dark:border-slate-700/40 pb-2">
                <span class="text-slate-400">Mã ca:</span>
                <span class="font-bold text-slate-700 dark:text-white">CA20250620001</span>
              </div>
              <div class="flex justify-between border-b border-slate-50 dark:border-slate-700/40 pb-2">
                <span class="text-slate-400">Thời gian:</span>
                <span class="font-medium text-slate-700 dark:text-slate-200">08:00 - 16:00 (20/06)</span>
              </div>
              <div class="flex justify-between border-b border-slate-50 dark:border-slate-700/40 pb-2">
                <span class="text-slate-400">Nhân viên chốt:</span>
                <span class="font-bold text-slate-700 dark:text-white">{{ adminSession.hoTen }}</span>
              </div>
              <div class="flex justify-between border-b border-slate-50 dark:border-slate-700/40 pb-2">
                <span class="text-slate-400">Người nhận:</span>
                <span class="font-semibold text-amber-600">Trần Thị B (Chưa xác nhận)</span>
              </div>
            </div>

            <!-- 3 Stat boxes -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
              <!-- Box 1 -->
              <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-900/30 border border-slate-100 dark:border-slate-700 text-center space-y-1">
                <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Tổng doanh thu hệ thống</p>
                <p class="text-lg font-extrabold text-blue-600 dark:text-blue-400">17.350.000 đ</p>
              </div>

              <!-- Box 2 -->
              <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-900/30 border border-slate-100 dark:border-slate-700 text-center space-y-1">
                <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Tiền mặt thực tế {{ getLastWordOfName }} báo cáo</p>
                <p class="text-lg font-extrabold text-emerald-600 dark:text-emerald-400">15.050.000 đ</p>
              </div>

              <!-- Box 3 -->
              <div class="p-4 rounded-xl bg-rose-50 dark:bg-rose-950/20 border border-rose-100 dark:border-rose-900/40 text-center space-y-1">
                <p class="text-[10px] font-bold text-rose-500 dark:text-rose-400 uppercase tracking-wider">Chênh lệch (Thiếu)</p>
                <p class="text-lg font-extrabold text-rose-600 dark:text-rose-400">-2.300.000 đ</p>
              </div>
            </div>
          </div>

          <!-- LỊCH SỬ GIAO DỊCH TRONG CA (RÚT GỌN) -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-4">
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider">
              Lịch sử giao dịch trong ca (Rút gọn)
            </h3>

            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs border-collapse">
                <thead>
                  <tr class="border-b border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-900/30 text-slate-500 font-semibold uppercase tracking-wider">
                    <th class="px-4 py-3">Thời gian</th>
                    <th class="px-4 py-3">Mã GD</th>
                    <th class="px-4 py-3">Hình thức</th>
                    <th class="px-4 py-3 text-right">Số tiền</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 dark:divide-slate-700/40">
                  <tr class="text-slate-600 dark:text-slate-300 hover:bg-slate-50/50 dark:hover:bg-slate-900/20">
                    <td class="px-4 py-3">15:45</td>
                    <td class="px-4 py-3 font-semibold text-primary">HD000128</td>
                    <td class="px-4 py-3">Tiền mặt</td>
                    <td class="px-4 py-3 text-right font-bold">2.150.000 đ</td>
                  </tr>
                  <tr class="text-slate-600 dark:text-slate-300 hover:bg-slate-50/50 dark:hover:bg-slate-900/20">
                    <td class="px-4 py-3">15:52</td>
                    <td class="px-4 py-3 font-semibold text-primary">HD000129</td>
                    <td class="px-4 py-3">Tiền mặt</td>
                    <td class="px-4 py-3 text-right font-bold">950.000 đ</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="text-center pt-2">
              <a href="#" class="text-xs font-semibold text-primary hover:underline">Xem toàn bộ 35 giao dịch →</a>
            </div>
          </div>

        </div>

        <!-- Right 1 Column -->
        <div class="space-y-6">
          
          <!-- NHẬT KÝ HOẠT ĐỘNG CA -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-4">
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider">
              Nhật ký hoạt động ca
            </h3>

            <div class="relative pl-6 space-y-5 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-100 dark:before:bg-slate-700">
              <!-- Item 1 -->
              <div class="relative">
                <div class="absolute -left-[22px] top-1.5 h-3.5 w-3.5 rounded-full border-2 border-white dark:border-slate-800 bg-slate-300"></div>
                <p class="text-[10px] font-bold text-slate-400">08:00 - 20/06</p>
                <p class="text-xs text-slate-700 dark:text-slate-300 mt-0.5 leading-relaxed">
                  <span class="font-bold">{{ adminSession.hoTen }}</span> mở ca. Tiền đầu ca: <span class="font-semibold text-slate-800 dark:text-white">5.000.000đ</span>
                </p>
              </div>

              <!-- Item 2 -->
              <div class="relative">
                <div class="absolute -left-[22px] top-1.5 h-3.5 w-3.5 rounded-full border-2 border-white dark:border-slate-800 bg-slate-300"></div>
                <p class="text-[10px] font-bold text-slate-400">16:00 - 20/06</p>
                <p class="text-xs text-slate-700 dark:text-slate-300 mt-0.5 leading-relaxed">
                  <span class="font-bold">{{ adminSession.hoTen }}</span> thực hiện đóng ca. Nhập két thực tế: <span class="font-semibold text-slate-800 dark:text-white">15.050.000đ</span>
                </p>
              </div>

              <!-- Item 3 -->
              <div class="relative">
                <div class="absolute -left-[22px] top-1.5 h-3.5 w-3.5 rounded-full border-2 border-white dark:border-slate-800 bg-slate-300"></div>
                <p class="text-[10px] font-bold text-rose-500">16:01 - 20/06</p>
                <p class="text-xs text-rose-600 dark:text-rose-400 mt-0.5 leading-relaxed">
                  Hệ thống phát hiện hụt <span class="font-bold">2.300.000đ</span>. Đã gửi cảnh báo.
                </p>
              </div>

              <!-- Item 4 -->
              <div class="relative">
                <div class="absolute -left-[22px] top-1.5 h-3.5 w-3.5 rounded-full border-2 border-white dark:border-slate-800 bg-amber-500" :class="!isCaDaChot ? 'animate-pulse' : ''"></div>
                <p class="text-[10px] font-bold text-amber-500">Hiện tại</p>
                <p class="text-xs text-amber-600 dark:text-amber-400 mt-0.5 leading-relaxed font-medium">
                  {{ isCaDaChot ? 'Đã hoàn thành đối soát ca này.' : 'Đang chờ Admin xử lý chênh lệch để chốt số.' }}
                </p>
              </div>
            </div>
          </div>

          <!-- QUYẾT ĐỊNH XỬ LÝ CHÊNH LỆCH -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-4">
            <h3 class="text-xs font-bold text-rose-600 uppercase tracking-wider flex items-center gap-1">
              <SlidersHorizontal class="h-4 w-4" />
              Quyết định xử lý chênh lệch
            </h3>

            <div class="space-y-4">
              <!-- Textarea -->
              <div>
                <textarea 
                  v-model="adminRejectionReason" 
                  rows="3" 
                  :disabled="isCaDaChot"
                  class="w-full p-3 text-xs border border-slate-200 dark:border-slate-700 rounded-xl bg-transparent text-slate-700 dark:text-slate-200 focus:outline-none focus:border-rose-400 transition"
                  placeholder="Nhập kết quả kiểm tra Camera hoặc lý do chênh lệch vào đây..."
                ></textarea>
              </div>

              <!-- Options -->
              <div class="space-y-2.5">
                <label class="flex items-start gap-3 cursor-pointer group text-xs text-slate-600 dark:text-slate-300">
                  <input 
                    type="radio" 
                    value="tru-luong" 
                    v-model="selectedHachToan"
                    :disabled="isCaDaChot"
                    class="mt-0.5 accent-rose-600"
                  />
                  <span class="group-hover:text-slate-800 dark:group-hover:text-white transition">
                    Gán trách nhiệm: Trừ lương Thu ngân ({{ adminSession.hoTen }})
                  </span>
                </label>

                <label class="flex items-start gap-3 cursor-pointer group text-xs text-slate-600 dark:text-slate-300">
                  <input 
                    type="radio" 
                    value="ghi-no" 
                    v-model="selectedHachToan"
                    :disabled="isCaDaChot"
                    class="mt-0.5 accent-rose-600"
                  />
                  <span class="group-hover:text-slate-800 dark:group-hover:text-white transition">
                    Ghi nhận nợ: Khách quên thanh toán (Theo dõi thu hồi sau)
                  </span>
                </label>

                <label class="flex items-start gap-3 cursor-pointer group text-xs text-slate-600 dark:text-slate-300">
                  <input 
                    type="radio" 
                    value="mien-tru" 
                    v-model="selectedHachToan"
                    :disabled="isCaDaChot"
                    class="mt-0.5 accent-rose-600"
                  />
                  <span class="group-hover:text-slate-800 dark:group-hover:text-white transition">
                    Miễn trừ: Bù hụt két từ quỹ rủi ro của Cửa hàng
                  </span>
                </label>
              </div>

              <!-- Submit Button -->
              <button 
                type="button"
                @click="pheDuyetVaChotSo"
                :disabled="isCaDaChot"
                class="w-full py-3.5 bg-rose-600 hover:bg-rose-700 disabled:bg-slate-100 disabled:text-slate-400 dark:disabled:bg-slate-700/60 dark:disabled:text-slate-500 text-white font-bold rounded-xl shadow-md disabled:shadow-none transition text-xs uppercase tracking-wider flex items-center justify-center gap-1.5"
              >
                <Check class="h-4.5 w-4.5" />
                {{ isCaDaChot ? 'Đã phê duyệt & chốt số' : 'Phê duyệt & chốt số ca này' }}
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- SCREEN A: BÀN GIAO CA -->
    <div v-else class="space-y-6">
      <!-- Header -->
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <ArrowRightLeft class="h-6 w-6 text-primary" />
            Bàn giao ca
          </h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Quản lý bàn giao ca giữa các nhân viên
          </p>
        </div>

        <!-- Quick Step Navigator for Testing & Switch back -->
        <div class="flex items-center gap-3">
          <button 
            v-if="isAdmin" 
            @click="forceStaffViewForAdmin = false"
            class="px-3 py-1.5 text-xs font-bold rounded-xl bg-primary/10 text-primary hover:bg-primary/20 transition"
          >
            Quay lại Giao diện Admin
          </button>
          
          <div class="flex items-center gap-1.5 bg-slate-100 dark:bg-slate-800 p-1 rounded-xl">
            <button 
              v-for="step in [1, 2, 3, 4]" 
              :key="step"
              @click="buocHienTai = step"
              class="px-3 py-1.5 text-xs font-semibold rounded-lg transition"
              :class="buocHienTai === step ? 'bg-white dark:bg-slate-700 text-primary shadow-sm' : 'text-slate-500 hover:text-slate-700'"
            >
              Bước {{ step }}
            </button>
          </div>
        </div>
      </div>

      <!-- Stepper Progress -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <!-- Step 1 -->
          <div class="flex items-center gap-3 p-3 rounded-xl border transition"
               :class="buocHienTai >= 1 ? 'border-primary/20 bg-primary/5 dark:bg-primary/10' : 'border-slate-100 dark:border-slate-700/60'">
            <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold"
                 :class="buocHienTai >= 1 ? 'bg-primary text-white' : 'bg-slate-100 dark:bg-slate-700 text-slate-500'">
              1
            </div>
            <div>
              <p class="text-xs font-bold" :class="buocHienTai >= 1 ? 'text-slate-800 dark:text-white' : 'text-slate-400'">1. Đóng ca</p>
              <p class="text-[10px] text-slate-400">Nhân viên kết thúc ca</p>
            </div>
          </div>

        <!-- Step 2 -->
        <div class="flex items-center gap-3 p-3 rounded-xl border transition"
             :class="buocHienTai >= 2 ? 'border-primary/20 bg-primary/5 dark:bg-primary/10' : 'border-slate-100 dark:border-slate-700/60'">
          <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold"
               :class="buocHienTai >= 2 ? 'bg-primary text-white' : 'bg-slate-100 dark:bg-slate-700 text-slate-500'">
            2
          </div>
          <div>
            <p class="text-xs font-bold" :class="buocHienTai >= 2 ? 'text-slate-800 dark:text-white' : 'text-slate-400'">2. Bàn giao</p>
            <p class="text-[10px] text-slate-400">Giao ca cho nhân viên tiếp theo</p>
          </div>
        </div>

        <!-- Step 3 -->
        <div class="flex items-center gap-3 p-3 rounded-xl border transition"
             :class="buocHienTai >= 3 ? 'border-primary/20 bg-primary/5 dark:bg-primary/10' : 'border-slate-100 dark:border-slate-700/60'">
          <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold"
               :class="buocHienTai >= 3 ? 'bg-primary text-white' : 'bg-slate-100 dark:bg-slate-700 text-slate-500'">
            3
          </div>
          <div>
            <p class="text-xs font-bold" :class="buocHienTai >= 3 ? 'text-slate-800 dark:text-white' : 'text-slate-400'">3. Xác nhận</p>
            <p class="text-[10px] text-slate-400">Nhân viên nhận ca xác nhận</p>
          </div>
        </div>

        <!-- Step 4 -->
        <div class="flex items-center gap-3 p-3 rounded-xl border transition"
             :class="buocHienTai >= 4 ? 'border-primary/20 bg-primary/5 dark:bg-primary/10' : 'border-slate-100 dark:border-slate-700/60'">
          <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold"
               :class="buocHienTai >= 4 ? 'bg-primary text-white' : 'bg-slate-100 dark:bg-slate-700 text-slate-500'">
            4
          </div>
          <div>
            <p class="text-xs font-bold" :class="buocHienTai >= 4 ? 'text-slate-800 dark:text-white' : 'text-slate-400'">4. Hoàn thành</p>
            <p class="text-[10px] text-slate-400">Bàn giao ca thành công</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <!-- Left Column: Shift Info & Stats -->
      <div class="lg:col-span-2 space-y-6">
        
        <!-- THÔNG TIN CA LÀM VIỆC -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider">Thông tin ca làm việc</h3>
            <span class="px-2.5 py-0.5 text-xs font-semibold rounded-full bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300">Đã đóng</span>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-3.5 text-sm">
            <div class="flex justify-between md:justify-start gap-4">
              <span class="text-slate-400 min-w-[100px]">Mã ca:</span>
              <span class="font-bold text-primary">CA20250620001</span>
            </div>
            <div class="flex justify-between md:justify-start gap-4">
              <span class="text-slate-400 min-w-[100px]">Thời gian:</span>
              <span class="font-medium text-slate-700 dark:text-slate-200">08:00 - 16:00 (20/06/2026)</span>
            </div>
            <div class="flex justify-between md:justify-start gap-4">
              <span class="text-slate-400 min-w-[100px]">Nhân viên:</span>
              <span class="font-bold text-slate-700 dark:text-slate-200">{{ adminSession.hoTen }}</span>
            </div>
            <div class="flex justify-between md:justify-start gap-4">
              <span class="text-slate-400 min-w-[100px]">Chức vụ:</span>
              <span class="font-medium text-slate-700 dark:text-slate-200">Nhân viên bán hàng</span>
            </div>
            <div class="flex justify-between md:justify-start gap-4">
              <span class="text-slate-400 min-w-[100px]">Ca làm việc:</span>
              <span class="font-semibold text-slate-700 dark:text-slate-200">Ca sáng</span>
            </div>
            <div class="flex justify-between md:justify-start gap-4">
              <span class="text-slate-400 min-w-[100px]">Giờ đóng ca:</span>
              <span class="font-medium text-slate-700 dark:text-slate-200">16:00 20/06/2026</span>
            </div>
          </div>
        </div>

        <!-- TỔNG HỢP DOANH THU -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-4">
          <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider">Tổng hợp doanh thu</h3>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex justify-between items-center py-2 border-b border-slate-100 dark:border-slate-700/60 text-sm">
              <span class="text-slate-500">Tiền đầu ca</span>
              <span class="font-bold text-slate-700 dark:text-slate-200">{{ formatVND(tienDauCa) }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-slate-100 dark:border-slate-700/60 text-sm">
              <span class="text-slate-500">Tổng doanh thu tiền mặt</span>
              <span class="font-bold text-slate-700 dark:text-slate-200">+{{ formatVND(doanhThuTienMat) }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-slate-100 dark:border-slate-700/60 text-sm">
              <span class="text-slate-500">Tổng thu khác</span>
              <span class="font-bold text-slate-700 dark:text-slate-200">+{{ formatVND(tongThuKhac) }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-slate-100 dark:border-slate-700/60 text-sm">
              <span class="text-slate-500">Tổng chi trong ca</span>
              <span class="font-bold text-rose-500">{{ formatVND(tongChiTrongCa) }}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-slate-100 dark:border-slate-700/60 text-sm">
              <span class="text-slate-500">Tổng hoàn tiền</span>
              <span class="font-bold text-rose-500">{{ formatVND(tongHoanTien) }}</span>
            </div>
          </div>

          <!-- Totals Banner -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-slate-200 dark:border-slate-700">
            <!-- Hệ thống -->
            <div class="bg-blue-50/50 dark:bg-blue-950/10 border border-blue-100 dark:border-blue-900/50 rounded-2xl p-4 text-center">
              <span class="text-[10px] font-bold uppercase tracking-wider text-blue-600 dark:text-blue-400">Tiền theo hệ thống</span>
              <p class="text-lg font-bold text-blue-700 dark:text-blue-300 mt-1">{{ formatVND(tienTheoHeThong) }}</p>
            </div>

            <!-- Thực tế -->
            <div class="bg-emerald-50/50 dark:bg-emerald-950/10 border border-emerald-100 dark:border-emerald-900/50 rounded-2xl p-4 text-center">
              <span class="text-[10px] font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400">Tiền thực tế</span>
              <p class="text-lg font-bold text-emerald-700 dark:text-emerald-300 mt-1">{{ formatVND(tienThucTe) }}</p>
            </div>

            <!-- Chênh lệch -->
            <div class="rounded-2xl p-4 text-center flex flex-col justify-center items-center"
                 :class="chenhLech >= 0 ? 'bg-emerald-100/40 border border-emerald-200 dark:bg-emerald-950/20' : 'bg-rose-100/40 border border-rose-200 dark:bg-rose-950/20'">
              <span class="text-[10px] font-bold uppercase tracking-wider"
                    :class="chenhLech >= 0 ? 'text-emerald-700' : 'text-rose-700'">
                Chênh lệch
              </span>
              <p class="text-lg font-bold mt-1" :class="chenhLech >= 0 ? 'text-emerald-700' : 'text-rose-700'">
                {{ chenhLech >= 0 ? '+' : '' }}{{ formatVND(chenhLech) }}
              </p>
              <span class="text-[10px] font-semibold text-slate-500 mt-0.5">
                ({{ chenhLech >= 0 ? 'Thừa' : 'Thiếu' }})
              </span>
            </div>
          </div>
        </div>

      </div>

      <!-- Right Column: Handover Form / Status details -->
      <div class="space-y-6">
        
        <!-- THÔNG TIN BÀN GIAO -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-4">
          <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider">Thông tin bàn giao</h3>

          <!-- Step 1: Đóng ca (A nhập) -->
          <div v-if="buocHienTai === 1" class="space-y-4">
            <!-- Cash handover amount (READ-ONLY TEXT WITH CALCULATOR ICON) -->
            <div>
              <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                Số tiền bàn giao
              </label>
              
              <div class="flex items-center gap-3">
                <!-- Static text display resembling an input but disabled/non-editable -->
                <div class="flex-1 h-12 px-4 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/60 flex items-center justify-between text-lg font-bold text-blue-600 dark:text-blue-400">
                  <span>{{ formatVND(tienThucTe) }}</span>
                </div>
                
                <!-- Cash calculator button -->
                <button 
                  type="button" 
                  @click="showsCalculator = true"
                  title="Mở bảng tính đếm tiền"
                  class="h-12 w-12 rounded-xl bg-primary/10 hover:bg-primary/20 text-primary flex items-center justify-center transition shadow-sm"
                >
                  <Calculator class="h-6 w-6" />
                </button>
              </div>

              <!-- Vietnamese Words Representation -->
              <p class="mt-2 text-xs italic text-slate-500 dark:text-slate-400 leading-normal">
                {{ docTienBangChu(tienThucTe) }}
              </p>
            </div>

            <!-- Receiver dropdown -->
            <div>
              <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                Chọn nhân viên nhận ca <span class="text-rose-500">*</span>
              </label>
              <select v-model="nhanVienNhan" class="w-full h-11 px-3 border border-slate-200 dark:border-slate-700 rounded-xl bg-white dark:bg-slate-900/60 text-sm font-semibold text-slate-700 dark:text-slate-300 focus:outline-none focus:border-rose-400 transition cursor-pointer">
                <option value="Trần Thị B">Trần Thị B (NV0002) - Nhân viên</option>
                <option value="Lê Văn C">Lê Văn C (NV0003) - Nhân viên</option>
              </select>
            </div>

            <!-- Note area -->
            <div>
              <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                Ghi chú
              </label>
              <textarea 
                v-model="ghiChu" 
                rows="3" 
                maxlength="200"
                class="w-full p-3 border border-slate-200 dark:border-slate-700 rounded-xl bg-transparent text-sm text-slate-700 dark:text-slate-200 focus:outline-none focus:border-rose-400 transition"
                placeholder="Nhập nội dung bàn giao..."
              ></textarea>
              <div class="flex justify-end text-[10px] text-slate-400 mt-1">
                {{ ghiChu.length }}/200
              </div>
            </div>

            <!-- Button: GỬI YÊU CẦU BÀN GIAO -->
            <button 
              type="button"
              @click="buocHienTai = 2; showSuccess('Đã gửi yêu cầu bàn giao ca!')"
              class="w-full py-3.5 bg-primary hover:bg-primary/95 text-white font-bold rounded-xl shadow-md transition text-xs uppercase tracking-wider"
            >
              Gửi yêu cầu bàn giao
            </button>
          </div>

          <!-- Step 2: Bàn giao (Waiting layout) -->
          <div v-else-if="buocHienTai === 2" class="space-y-4">
            <div class="rounded-xl border border-slate-100 dark:border-slate-700/60 p-4 bg-slate-50/50 dark:bg-slate-900/30 text-sm text-center py-6 space-y-3">
              <div class="mx-auto h-12 w-12 bg-amber-50 dark:bg-amber-950/20 border-2 border-amber-500 rounded-full flex items-center justify-center text-amber-500 shadow-sm animate-pulse">
                <Clock class="h-6 w-6" />
              </div>
              <div>
                <h4 class="font-bold text-slate-800 dark:text-white text-xs uppercase tracking-wide">Chờ Trần Thị B xác nhận</h4>
                <p class="text-[11px] text-slate-400 mt-1 leading-normal">
                  Yêu cầu bàn giao ca đã được gửi thành công. Đang chờ nhân viên nhận ca {{ nhanVienNhan }} kiểm tra thực tế và xác nhận.
                </p>
              </div>
              <button 
                type="button"
                @click="buocHienTai = 1; showSuccess('Đã hủy yêu cầu bàn giao ca.')"
                class="mt-2 text-xs font-semibold text-rose-600 hover:underline"
              >
                Hủy yêu cầu bàn giao
              </button>
            </div>
          </div>

          <!-- Step 3: Receiver Confirmation (Person B Actions) -->
          <div v-else-if="buocHienTai === 3" class="space-y-5">
            <!-- Review Data Submitted by Person A -->
            <div class="rounded-xl border border-slate-100 dark:border-slate-700/60 p-4 bg-slate-50/50 dark:bg-slate-900/30 text-sm space-y-2.5">
              <div class="flex justify-between">
                <span class="text-slate-400">Số tiền bàn giao:</span>
                <span class="font-bold text-blue-600 dark:text-blue-400">{{ formatVND(tienThucTe) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-400">Nhân viên nhận:</span>
                <span class="font-semibold text-slate-700 dark:text-slate-200">{{ nhanVienNhan }}</span>
              </div>
              <div class="pt-2 border-t border-slate-200/60">
                <p class="text-xs text-slate-400 font-bold uppercase tracking-wider mb-1">Ghi chú từ {{ adminSession.hoTen }}:</p>
                <p class="text-slate-600 dark:text-slate-300 text-xs italic bg-white dark:bg-slate-800 p-2.5 rounded-lg border border-slate-100 dark:border-slate-700/60">
                  "{{ ghiChu }}"
                </p>
              </div>
            </div>

            <!-- Warning Notice -->
            <div class="flex items-start gap-2.5 p-3.5 bg-amber-50 dark:bg-amber-950/20 text-amber-800 dark:text-amber-400 border border-amber-200 dark:border-amber-900/50 rounded-xl text-xs font-medium">
              <AlertTriangle class="h-4 w-4 shrink-0" />
              <span>Vui lòng kiểm tra kỹ số tiền mặt thực tế tại két trước khi đồng ý nhận bàn giao ca.</span>
            </div>

            <!-- 2 BIG ACTION BUTTONS FOR PERSON B (ĐỒNG Ý NHẬN CA / TỪ CHỐI) -->
            <div class="space-y-3 pt-2">
              <button 
                @click="dongYNhantCa"
                class="w-full py-4 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl shadow-lg transition flex items-center justify-center gap-2 text-sm uppercase tracking-wide"
              >
                <Check class="h-5 w-5" />
                ĐỒNG Ý NHẬN CA
              </button>

              <button 
                @click="showsRejectionModal = true"
                class="w-full py-3.5 bg-rose-600 hover:bg-rose-700 text-white font-semibold rounded-xl transition flex items-center justify-center gap-2 text-xs uppercase tracking-wide"
              >
                <CloseIcon class="h-4.5 w-4.5" />
                TỪ CHỐI - ĐẾM SAI TIỀN
              </button>
            </div>
          </div>

          <!-- Step 4: Finished/Completed Details -->
          <div v-else-if="buocHienTai === 4" class="text-center py-6 space-y-4">
            <div class="mx-auto h-16 w-16 bg-emerald-50 dark:bg-emerald-950/30 border-2 border-emerald-500 rounded-full flex items-center justify-center text-emerald-500 shadow-md">
              <CheckCircle2 class="h-8 w-8 animate-bounce" />
            </div>
            <div>
              <h4 class="font-bold text-slate-800 dark:text-white">Bàn giao ca thành công</h4>
              <p class="text-xs text-slate-400 mt-1">Hệ thống đã cập nhật số dư ca làm việc mới.</p>
            </div>

            <div class="rounded-xl border border-slate-100 p-4 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/40 text-xs text-left space-y-2 max-w-xs mx-auto">
              <div class="flex justify-between">
                <span class="text-slate-400">Người bàn giao:</span>
                <span class="font-semibold text-slate-700 dark:text-slate-200">{{ adminSession.hoTen }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-400">Người tiếp quản:</span>
                <span class="font-semibold text-slate-700 dark:text-slate-200">Trần Thị B</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-400">Tiền cuối ca:</span>
                <span class="font-bold text-emerald-600">{{ formatVND(tienThucTe) }}</span>
              </div>
            </div>

            <button @click="buocHienTai = 2" class="mt-4 px-4 py-2 border border-slate-200 hover:bg-slate-50 rounded-xl text-xs font-semibold text-slate-600 transition flex items-center gap-1.5 mx-auto">
              <RotateCcw class="h-3.5 w-3.5" />
              Làm lại bàn giao
            </button>
          </div>

          <!-- Other steps placeholder -->
          <div v-else class="text-center py-8 text-slate-400 text-xs">
            Vui lòng chọn Bước 2 hoặc 3 trên bộ điều hướng để xem chi tiết.
          </div>

        </div>

      </div>

    </div>

    <!-- Bottom Section: Transactions & Confirmations -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <!-- CHI TIẾT GIAO DỊCH TRONG CA (Left 2 columns span) -->
      <div class="lg:col-span-2 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-4">
        <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider">Chi tiết giao dịch trong ca</h3>

        <!-- Tabs -->
        <div class="flex gap-2 overflow-x-auto pb-1">
          <button class="px-4 py-1.5 text-xs font-semibold rounded-lg bg-primary text-white shadow-sm shrink-0">
            Doanh thu (35)
          </button>
          <button class="px-4 py-1.5 text-xs font-semibold rounded-lg bg-slate-50 text-slate-600 hover:bg-slate-100 dark:bg-slate-700/40 dark:text-slate-300 dark:hover:bg-slate-700 shrink-0">
            Thu khác (3)
          </button>
          <button class="px-4 py-1.5 text-xs font-semibold rounded-lg bg-slate-50 text-slate-600 hover:bg-slate-100 dark:bg-slate-700/40 dark:text-slate-300 dark:hover:bg-slate-700 shrink-0">
            Chi tiền (5)
          </button>
          <button class="px-4 py-1.5 text-xs font-semibold rounded-lg bg-slate-50 text-slate-600 hover:bg-slate-100 dark:bg-slate-700/40 dark:text-slate-300 dark:hover:bg-slate-700 shrink-0">
            Hoàn tiền (2)
          </button>
        </div>

        <!-- Transactions Table -->
        <div class="overflow-x-auto">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="border-b border-slate-100 dark:border-slate-700/60 bg-slate-50/50 dark:bg-slate-900/30 text-slate-500 font-semibold">
                <th class="px-3 py-2.5">Thời gian</th>
                <th class="px-3 py-2.5">Mã giao dịch</th>
                <th class="px-3 py-2.5">Nội dung</th>
                <th class="px-3 py-2.5">Số tiền</th>
                <th class="px-3 py-2.5">Hình thức</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-50 dark:divide-slate-700/40">
              <tr class="text-slate-600 dark:text-slate-300">
                <td class="px-3 py-3">08:15</td>
                <td class="px-3 py-3 font-semibold text-primary">HD000125</td>
                <td class="px-3 py-3">Bán hàng</td>
                <td class="px-3 py-3 font-semibold">450.000</td>
                <td class="px-3 py-3">Tiền mặt</td>
              </tr>
              <tr class="text-slate-600 dark:text-slate-300">
                <td class="px-3 py-3">08:45</td>
                <td class="px-3 py-3 font-semibold text-primary">HD000126</td>
                <td class="px-3 py-3">Bán hàng</td>
                <td class="px-3 py-3 font-semibold">1.250.000</td>
                <td class="px-3 py-3">Tiền mặt</td>
              </tr>
              <tr class="text-slate-600 dark:text-slate-300">
                <td class="px-3 py-3">09:20</td>
                <td class="px-3 py-3 font-semibold text-primary">HD000127</td>
                <td class="px-3 py-3">Bán hàng</td>
                <td class="px-3 py-3 font-semibold">780.000</td>
                <td class="px-3 py-3">Tiền mặt</td>
              </tr>
              <tr class="text-slate-600 dark:text-slate-300">
                <td class="px-3 py-3">10:05</td>
                <td class="px-3 py-3 font-semibold text-primary">HD000128</td>
                <td class="px-3 py-3">Bán hàng</td>
                <td class="px-3 py-3 font-semibold">2.150.000</td>
                <td class="px-3 py-3">Tiền mặt</td>
              </tr>
              <tr class="text-slate-600 dark:text-slate-300">
                <td class="px-3 py-3">10:30</td>
                <td class="px-3 py-3 font-semibold text-primary">HD000129</td>
                <td class="px-3 py-3">Bán hàng</td>
                <td class="px-3 py-3 font-semibold">950.000</td>
                <td class="px-3 py-3">Tiền mặt</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="text-center pt-2">
          <a href="#" class="text-xs font-semibold text-primary hover:underline">Xem tất cả giao dịch →</a>
        </div>
      </div>

      <!-- XÁC NHẬN BÀN GIAO (Right Column) -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-4">
        <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider">Xác nhận bàn giao</h3>

        <div class="space-y-4">
          <!-- Card 1: Nhân viên giao ca -->
          <div class="border border-slate-100 dark:border-slate-700 rounded-2xl p-4 bg-slate-50/30 dark:bg-slate-900/10">
            <div class="flex justify-between items-start">
              <div>
                <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Nhân viên giao ca</p>
                <h4 class="font-bold text-slate-700 dark:text-slate-200 mt-1">{{ adminSession.hoTen }}</h4>
                <p class="text-[10px] text-slate-400 mt-0.5">Thời gian: 16:05 20/06/2026</p>
              </div>
              
              <!-- Badge: "Chờ gửi bàn giao" instead of "Đã xác nhận", signature image deleted -->
              <span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300">
                Chờ gửi bàn giao
              </span>
            </div>
            
            <!-- Signature area replaced with a clean empty state text/box as requested (xóa hình chữ ký) -->
            <div class="mt-4 h-16 border border-dashed border-slate-200 dark:border-slate-700 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-900/40">
              <span class="text-[10px] font-semibold text-slate-400">Không có chữ ký số</span>
            </div>
          </div>

          <!-- Card 2: Nhân viên nhận ca -->
          <div class="border border-slate-100 dark:border-slate-700 rounded-2xl p-4 bg-slate-50/30 dark:bg-slate-900/10">
            <div class="flex justify-between items-start">
              <div>
                <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Nhân viên nhận ca</p>
                <h4 class="font-bold text-slate-700 dark:text-slate-200 mt-1">{{ nhanVienNhan }}</h4>
                <p class="text-[10px] text-slate-400 mt-0.5">
                  Thời gian: {{ buocHienTai === 4 ? '16:10 20/06/2026' : 'Chưa xác nhận' }}
                </p>
              </div>
              
              <span class="px-2.5 py-0.5 text-[10px] font-bold rounded-full"
                    :class="buocHienTai === 4 ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300' : 'bg-orange-100 text-orange-700 dark:bg-orange-950/40 dark:text-orange-300'">
                {{ buocHienTai === 4 ? 'Đã nhận ca' : 'Chờ xác nhận' }}
              </span>
            </div>

            <!-- Signature block -->
            <div class="mt-4 h-16 border border-dashed border-slate-200 dark:border-slate-700 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-900/40">
              <div v-if="buocHienTai === 4" class="text-center">
                <span class="text-[10px] font-bold text-emerald-600">✓ ĐÃ KÝ XÁC NHẬN</span>
              </div>
              <span v-else class="text-[10px] font-semibold text-slate-400">Chưa có chữ ký</span>
            </div>
          </div>
        </div>
      </div>

    </div> <!-- Closes bottom section grid -->
  </div> <!-- Closes Screen A wrapper (v-else) -->

    <!-- POPUP MODAL: CASH CALCULATOR FOR QUICK BILL COUNTING -->
    <div v-if="showsCalculator" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <!-- Backdrop overlay -->
      <div class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showsCalculator = false"></div>

      <!-- Modal panel -->
      <div class="relative w-full max-w-md overflow-hidden rounded-3xl border border-slate-100 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-2xl p-6 transition-all space-y-4">
        <div class="flex items-center justify-between border-b border-slate-100 dark:border-slate-700 pb-3">
          <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider flex items-center gap-1.5">
            <Calculator class="h-5 w-5 text-primary" />
            Hỗ trợ đếm tiền mặt
          </h3>
          <button @click="showsCalculator = false" class="p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200">
            <CloseIcon class="h-5 w-5" />
          </button>
        </div>

        <!-- Calculator grid of denominations -->
        <div class="max-h-[50vh] overflow-y-auto space-y-2.5 pr-1">
          <div 
            v-for="(item, idx) in denominations" 
            :key="idx"
            class="flex items-center justify-between gap-4 p-2 rounded-xl bg-slate-50/50 dark:bg-slate-900/20 border border-slate-100 dark:border-slate-700/60"
          >
            <span class="text-xs font-bold text-slate-700 dark:text-slate-300 min-w-[70px]">
              {{ formatVND(item.value).replace(" đ", "") }}
            </span>
            
            <span class="text-xs text-slate-400">x</span>
            
            <!-- Quantity input field -->
            <input 
              type="number" 
              v-model.number="item.count" 
              min="0"
              placeholder="0"
              class="w-24 h-8 px-2.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-xs font-bold text-center text-slate-800 dark:text-slate-200 focus:outline-none focus:border-rose-400"
            />
            
            <span class="text-xs text-slate-400 font-medium min-w-[80px] text-right">
              = {{ formatVND(item.value * (item.count || 0)).replace(" đ", "") }}
            </span>
          </div>
        </div>

        <!-- Total calculated summary -->
        <div class="pt-4 border-t border-slate-100 dark:border-slate-700 flex justify-between items-center text-sm">
          <span class="font-bold text-slate-500 uppercase tracking-wider text-[11px]">Tổng cộng:</span>
          <span class="text-base font-bold text-blue-600 dark:text-blue-400">{{ formatVND(calculatedCash) }}</span>
        </div>

        <!-- Actions -->
        <div class="grid grid-cols-3 gap-3 pt-2">
          <button @click="resetCalculator" class="h-10 border border-slate-200 hover:bg-slate-50 rounded-xl text-xs font-semibold text-slate-500 transition">
            Đặt lại
          </button>
          <button @click="applyCalculatedCash" class="col-span-2 h-10 bg-primary hover:bg-primary/95 text-white font-bold rounded-xl text-xs transition shadow-md flex items-center justify-center gap-1.5">
            <Check class="h-4.5 w-4.5" />
            ÁP DỤNG
          </button>
        </div>
      </div>
    </div>

    <!-- POPUP MODAL: REJECTION REASON FOR STEP 3 -->
    <div v-if="showsRejectionModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <div class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showsRejectionModal = false"></div>
      
      <div class="relative w-full max-w-sm overflow-hidden rounded-3xl border border-slate-100 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-2xl p-6 transition-all space-y-4">
        <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider">Lý do từ chối nhận ca</h3>
        
        <div>
          <label class="block text-xs font-medium text-slate-400 mb-1.5">
            Lý do chi tiết (ví dụ: Thiếu 50.000đ tiền mặt tại két...)
          </label>
          <textarea 
            v-model="lyDoTuChoi" 
            rows="3" 
            class="w-full p-3 border border-slate-200 dark:border-slate-700 rounded-xl bg-transparent text-sm text-slate-700 dark:text-slate-200 focus:outline-none focus:border-rose-400 transition"
            placeholder="Nhập lý do..."
          ></textarea>
        </div>

        <div class="flex justify-end gap-3 pt-2">
          <button @click="showsRejectionModal = false" class="h-10 px-4 border border-slate-200 hover:bg-slate-50 rounded-xl text-xs font-semibold text-slate-500 transition">
            Hủy
          </button>
          <button @click="submitTuChoi" class="h-10 px-5 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl text-xs transition shadow-md">
            GỬI TỪ CHỐI
          </button>
        </div>
      </div>
    </div>

    <!-- POPUP MODAL: INCIDENT REPORT FOR STEP 1 -->
    <div v-if="showsIncidentModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <div class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showsIncidentModal = false"></div>
      
      <div class="relative w-full max-w-md overflow-hidden rounded-3xl border border-slate-100 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-2xl p-6 transition-all space-y-4">
        <div class="flex items-center gap-2 text-rose-600 dark:text-rose-400 border-b border-slate-100 dark:border-slate-700 pb-3">
          <AlertTriangle class="h-5 w-5 shrink-0" />
          <h3 class="text-sm font-bold uppercase tracking-wider">Báo cáo két có dấu hiệu bị cạy/thất thoát</h3>
        </div>

        <div class="p-3 bg-rose-50 dark:bg-rose-950/20 text-rose-800 dark:text-rose-400 border border-rose-200 dark:border-rose-900/50 rounded-xl text-xs font-semibold leading-relaxed">
          CẢNH BÁO: Vui lòng chụp ảnh hiện trường, giữ nguyên vị trí các đồ vật xung quanh két tiền và báo cáo sự việc khẩn cấp cho Quản lý.
        </div>
        
        <div>
          <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">
            Mô tả tình trạng két tiền <span class="text-rose-500">*</span>
          </label>
          <textarea 
            v-model="lyDoSucos" 
            rows="4" 
            class="w-full p-3 border border-slate-200 dark:border-slate-700 rounded-xl bg-transparent text-sm text-slate-700 dark:text-slate-200 focus:outline-none focus:border-rose-400 transition"
            placeholder="Mô tả cụ thể (ví dụ: Két tiền bị nạy khóa, mất khoảng bao nhiêu tiền...)"
          ></textarea>
        </div>

        <div class="flex justify-end gap-3 pt-2">
          <button @click="showsIncidentModal = false" class="h-10 px-4 border border-slate-200 hover:bg-slate-50 rounded-xl text-xs font-semibold text-slate-500 transition">
            Hủy
          </button>
          <button @click="guiBaoCaoSuCo" class="h-10 px-5 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl text-xs transition shadow-md">
            GỬI BÁO CÁO KHẨN CẤP
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* Chrome, Safari, Edge, Opera: hide number input arrows */
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Firefox: hide number input arrows */
input[type=number] {
  -moz-appearance: textfield;
}
</style>
