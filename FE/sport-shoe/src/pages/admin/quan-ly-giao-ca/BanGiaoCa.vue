<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
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
  X as CloseIcon,
  ChevronDown,
  FileCheck,
  Eye
} from "lucide-vue-next";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import { useAdminSession } from "../../../composable/useAdminSession";
import { useGiaoCa } from "../../../composable/useGiaoCa";
import { layDanhSachNhanVien } from "../../../services/nhan-vien";
import { layThongTinGiaoCaCurrent } from "../../../services/giao-ca";
import { layDanhSachHoaDon } from "../../../services/hoa-don";
import ThuChiModal from "../../../components/admin/giao-ca/ThuChiModal.vue";

const route = useRoute();
const router = useRouter();
const { adminSession } = useAdminSession();

const {
  activeShift,
  pendingHandovers,
  loadingShift,
  loadActiveShift,
  loadPendingHandovers,
  openShift,
  submitHandover,
  confirmHandover
} = useGiaoCa();

const listNhanVien = ref([]);
const currentStats = ref(null);
const loadingStats = ref(false);
const isPaidInvoicesCollapsed = ref(true);

const pendingInvoices = computed(() => {
  return shiftTransactions.value.filter(tx => 
    tx.trangThai === 'Chờ xác nhận' || tx.phuongThucThanhToan === 'Chưa thanh toán'
  );
});

const paidInvoices = computed(() => {
  return shiftTransactions.value.filter(tx => 
    tx.trangThai === 'Hoàn thành' || (tx.trangThai !== 'Chờ xác nhận' && tx.phuongThucThanhToan !== 'Chưa thanh toán')
  );
});

const buocHienTai = ref(1);

// Mở ca sáng sớm states
const tienMoCaSángSớm = ref(500000); // Default to 500k starting cash
const ghiChuMoCaSángSớm = ref("");

// Báo cáo sự cố
const showsIncidentModal = ref(false);
const lyDoSucos = ref("");

// Thu Chi Modal states
const showThuChiModal = ref(false);
const thuChiModalType = ref("THU");

function openThuChiModal(type) {
  thuChiModalType.value = type;
  showThuChiModal.value = true;
}

// Calculator popup states
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

// Handover form states
const tienThucTe = ref(0);
const nhanVienNhanId = ref("");
const lyDoChenhLech = ref("");
const ghiChu = ref("");

// Receiver confirmation note
const ghiChuNhanCa = ref("");

const processing = ref(false);

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

const pendingHandover = computed(() => {
  return pendingHandovers.value && pendingHandovers.value.length > 0 ? pendingHandovers.value[0] : null;
});

const displayShift = computed(() => activeShift.value || pendingHandover.value);

const isMyShift = computed(() => {
  if (!displayShift.value || !adminSession.value) return false;
  return String(adminSession.value.id) === String(displayShift.value.nhanVienTrongCaId);
});

const shiftTransactions = ref([]);
const loadingTransactions = ref(false);

const directDisplayInvoices = computed(() => {
  if (isMyShift.value) {
    return shiftTransactions.value;
  }
  return pendingInvoices.value;
});

// Load stats for active ca
async function taiThongKeCaHienTai() {
  if (!activeShift.value) return;
  loadingStats.value = true;
  try {
    const data = await layThongTinGiaoCaCurrent();
    currentStats.value = data;
    tienThucTe.value = data.tienCuoiCaHeThong || 0;
  } catch (err) {
    console.error("Lỗi tải thống kê ca:", err);
  } finally {
    loadingStats.value = false;
  }
}

async function loadShiftTransactions() {
  if (!displayShift.value || !displayShift.value.id) {
    shiftTransactions.value = [];
    return;
  }
  
  loadingTransactions.value = true;
  try {
    const res = await layDanhSachHoaDon({
      giaoCaId: displayShift.value.id
    });
    
    if (res && res.length > 0) {
      shiftTransactions.value = res.sort((a, b) => new Date(b.ngayTao).getTime() - new Date(a.ngayTao).getTime());
    } else {
      shiftTransactions.value = [];
    }
  } catch (err) {
    console.error("Lỗi tải chi tiết giao dịch trong ca:", err);
    shiftTransactions.value = [];
  } finally {
    loadingTransactions.value = false;
  }
}

// Load active employees list
async function taiNhanVienGiaoCa() {
  try {
    const list = await layDanhSachNhanVien({ trangThai: 1 });
    listNhanVien.value = list.filter(
      (nv) => String(nv.id) !== String(adminSession.value.id)
    );
  } catch (err) {
    console.error("Lỗi tải danh sách nhân viên:", err);
  }
}

// State Machine Sync based on BE states
async function syncState() {
  if (activeShift.value) {
    if (activeShift.value.trangThai === "MO_CA") {
      buocHienTai.value = 1;
    } else if (activeShift.value.trangThai === "CHO_BAN_GIAO") {
      buocHienTai.value = 2;
    }
    await taiThongKeCaHienTai();
    await taiNhanVienGiaoCa();
    await loadShiftTransactions();
  } else {
    if (pendingHandovers.value && pendingHandovers.value.length > 0) {
      buocHienTai.value = 3;
      await loadShiftTransactions();
    } else {
      // No active ca, no pending handover
      buocHienTai.value = 1; // Show Screen B (Mở ca) if in /admin/mo-ca, else Screen A empty state
    }
  }
  isPaidInvoicesCollapsed.value = !isMyShift.value;
}

onMounted(async () => {
  await loadActiveShift();
  await loadPendingHandovers();
  await syncState();
});

watch([activeShift, pendingHandovers], () => {
  syncState();
});

watch(() => route.path, () => {
  syncState();
});

const currentDateFormatted = computed(() => {
  const d = new Date();
  const day = String(d.getDate()).padStart(2, '0');
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const year = d.getFullYear();
  return `${day}/${month}/${year}`;
});

const calculatedCash = computed(() => {
  return denominations.value.reduce((sum, item) => sum + item.value * (item.count || 0), 0);
});

function formatVND(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value || 0).replace("₫", "đ");
}

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

function resetCalculator() {
  denominations.value.forEach(item => item.count = 0);
}

function applyCalculatedCash() {
  if (isMoCaSángSớmMode.value) {
    tienMoCaSángSớm.value = calculatedCash.value;
  } else {
    tienThucTe.value = calculatedCash.value;
  }
  showsCalculator.value = false;
  showSuccess(`Đã áp dụng số tiền đếm được: ${formatVND(calculatedCash.value)}`);
}

// Action: Mở ca
async function xacNhanMoCaSángSớmBtn() {
  if (tienMoCaSángSớm.value < 0) {
    showError("Số tiền mở ca không được âm");
    return;
  }
  showConfirm(
    `Số tiền mặt có trong két đếm được là: <strong>${formatVND(tienMoCaSángSớm.value)}</strong>`,
    "Xác nhận mở ca làm việc?"
  ).then(async (confirmed) => {
    if (confirmed) {
      processing.value = true;
      const res = await openShift(tienMoCaSángSớm.value, ghiChuMoCaSángSớm.value);
      processing.value = false;
      if (res.success) {
        showSuccess(res.message);
        router.push("/admin/ban-hang");
      } else {
        showError(res.message);
      }
    }
  });
}

// Action: Bàn giao ca
async function xacNhanBanGiaoCaBtn() {
  if (!nhanVienNhanId.value) {
    showError("Vui lòng chọn nhân viên nhận bàn giao ca");
    return;
  }
  if (chenhLech.value !== 0 && (!lyDoChenhLech.value || !lyDoChenhLech.value.trim())) {
    showError("Số tiền chênh lệch khác 0. Vui lòng nhập lý do chênh lệch.");
    return;
  }

  showConfirm(
    "Xác nhận bàn giao ca?",
    "Thông tin bàn giao sẽ được chuyển đến nhân viên nhận để xác nhận."
  ).then(async (confirmed) => {
    if (confirmed) {
      processing.value = true;
      const res = await submitHandover({
        tienCuoiCaThucTe: tienThucTe.value,
        nhanVienNhanId: nhanVienNhanId.value,
        lyDoChenhLech: lyDoChenhLech.value,
        ghiChu: ghiChu.value
      });
      processing.value = false;
      if (res.success) {
        showSuccess(res.message);
        await loadActiveShift();
        await loadPendingHandovers();
      } else {
        showError(res.message);
      }
    }
  });
}

// Action: Đồng ý nhận ca
async function dongYNhanCaBtn(id) {
  if (!id) return;
  showConfirm(
    "Đồng ý nhận bàn giao ca?",
    "Bạn sẽ chính thức tiếp quản ca làm việc tiếp theo và hệ thống tự động mở ca cho bạn."
  ).then(async (confirmed) => {
    if (confirmed) {
      processing.value = true;
      const res = await confirmHandover(id, ghiChuNhanCa.value);
      processing.value = false;
      if (res.success) {
        showSuccess(res.message);
        await loadActiveShift();
        await loadPendingHandovers();
        router.push("/admin/ban-hang");
      } else {
        showError(res.message);
      }
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

// Computeds for dynamic finance display
const tienDauCa = computed(() => activeShift.value?.tienDauCa || 0);
const doanhThuTienMat = computed(() => currentStats.value?.tienMatTrongCa || 0);
const tongThuKhac = ref(0);
const tongChiTrongCa = ref(0);
const tongHoanTien = ref(0);

const tienTheoHeThong = computed(() => {
  if (!currentStats.value) return activeShift.value?.tienDauCa || 0;
  return currentStats.value.tienCuoiCaHeThong;
});

const chenhLech = computed(() => {
  return tienThucTe.value - tienTheoHeThong.value;
});

const currentChenhLechAmount = computed(() => {
  return activeShift.value ? chenhLech.value : (pendingHandover.value?.tienChenhLech || 0);
});

const chenhLechDisplay = computed(() => {
  const amount = currentChenhLechAmount.value;
  if (amount === 0) {
    return {
      text: "0 đ",
      note: "(Khớp)",
      bgClass: "bg-slate-100/40 border border-slate-200 dark:bg-slate-900/20",
      textClass: "text-slate-700 dark:text-slate-300"
    };
  }
  if (amount > 0) {
    return {
      text: `+${formatVND(amount)}`,
      note: "(Thừa)",
      bgClass: "bg-blue-100/40 border border-blue-200 dark:bg-blue-950/20",
      textClass: "text-blue-700 dark:text-blue-400"
    };
  }
  return {
    text: `${formatVND(amount)}`,
    note: "(Thiếu)",
    bgClass: "bg-rose-100/40 border border-rose-200 dark:bg-rose-950/20",
    textClass: "text-rose-700 dark:text-rose-400"
  };
});

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
</script>

<template>
  <div class="space-y-6 max-w-7xl mx-auto">
    <!-- SCREEN B: MỞ CA SÁNG SỚM -->
    <div v-if="isMoCaSángSớmMode" class="flex items-center justify-center min-h-[70vh]">
      <!-- Case 1: Active shift already exists -->
      <div v-if="activeShift" class="w-full max-w-lg bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl p-8 text-center space-y-5">
        <div class="mx-auto h-16 w-16 bg-amber-50 dark:bg-amber-950/20 border-2 border-amber-500 rounded-full flex items-center justify-center text-amber-500 shadow-md">
          <AlertTriangle class="h-8 w-8 animate-pulse" />
        </div>
        <div>
          <h2 class="text-xl font-bold tracking-tight text-slate-800 dark:text-white uppercase">Ca làm việc đã hoạt động</h2>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
            Bạn hiện đang có một ca làm việc đang hoạt động (Mã ca: <span class="font-bold text-primary">{{ activeShift.ma }}</span>).
            Vui lòng thực hiện đóng ca và bàn giao ca trước khi mở ca mới.
          </p>
        </div>
        <button 
          @click="router.push('/admin/ban-giao-ca')" 
          class="w-full py-3.5 bg-primary hover:bg-primary/95 text-white font-bold rounded-2xl shadow-lg transition"
        >
          Đi đến Bàn giao ca
        </button>
      </div>

      <!-- Case 2: Pending Handover waiting for confirmation -->
      <div v-else-if="pendingHandover" class="w-full max-w-lg bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl p-8 space-y-6">
        <div class="text-center space-y-2">
          <div class="mx-auto h-14 w-14 bg-blue-50 dark:bg-blue-950/20 border-2 border-primary rounded-full flex items-center justify-center text-primary shadow-sm">
            <FileSpreadsheet class="h-7 w-7" />
          </div>
          <h2 class="text-xl font-bold tracking-tight text-slate-800 dark:text-white uppercase mt-2">
            Xác nhận bàn giao ca
          </h2>
          <p class="text-xs text-slate-500 dark:text-slate-400 leading-normal">
            Có ca bàn giao từ <span class="font-bold">{{ pendingHandover.nhanVienTrongCaTen }}</span> đang chờ bạn xác nhận để tiếp quản ca tiếp theo.
          </p>
        </div>

        <div class="rounded-xl border border-slate-100 dark:border-slate-700/60 p-4 bg-slate-50/50 dark:bg-slate-900/30 text-sm space-y-2.5">
          <div class="flex justify-between">
            <span class="text-slate-400">Số tiền bàn giao:</span>
            <span class="font-bold text-blue-600 dark:text-blue-400">{{ formatVND(pendingHandover.tienCuoiCaThucTe) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-slate-400">Giờ đóng ca:</span>
            <span class="font-medium text-slate-700 dark:text-slate-200">{{ new Date(pendingHandover.thoiGianRa).toLocaleString('vi-VN') }}</span>
          </div>
          <div class="flex justify-between">
            <span class="font-semibold" :class="chenhLechDisplay.textClass">Chênh lệch:</span>
            <span class="font-bold flex gap-1" :class="chenhLechDisplay.textClass">
              {{ chenhLechDisplay.text }}
              <span class="text-[11px] opacity-80 mt-0.5">{{ chenhLechDisplay.note }}</span>
            </span>
          </div>
          <div class="pt-2 border-t border-slate-200/60">
            <p class="text-xs text-slate-400 font-bold uppercase tracking-wider mb-1">Ghi chú từ đồng nghiệp:</p>
            <p class="text-slate-600 dark:text-slate-300 text-xs italic bg-white dark:bg-slate-800 p-2.5 rounded-lg border border-slate-100 dark:border-slate-700/60">
              "{{ pendingHandover.ghiChu || 'Không có ghi chú' }}"
            </p>
          </div>
        </div>

        <div class="flex items-start gap-2.5 p-3.5 bg-amber-50 dark:bg-amber-950/20 text-amber-800 dark:text-amber-400 border border-amber-200 dark:border-amber-900/50 rounded-xl text-xs font-medium">
          <AlertTriangle class="h-4 w-4 shrink-0" />
          <span>Vui lòng kiểm tra kỹ số tiền mặt thực tế trước khi đồng ý nhận bàn giao.</span>
        </div>

        <div>
          <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
            Ghi chú nhận ca
          </label>
          <input 
            type="text" 
            v-model="ghiChuNhanCa"
            placeholder="Nhập ghi chú phản hồi..."
            class="w-full rounded-xl border border-slate-200 bg-transparent px-3 py-2 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
          />
        </div>

        <button 
          @click="dongYNhanCaBtn(pendingHandover.id)"
          :disabled="processing"
          class="w-full py-4 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-2xl shadow-lg transition flex items-center justify-center gap-2 text-sm uppercase tracking-wide disabled:opacity-50"
        >
          <Check class="h-5 w-5" />
          {{ processing ? 'ĐANG XỬ LÝ...' : 'ĐỒNG Ý NHẬN CA' }}
        </button>
      </div>

      <!-- Case 3: No active ca, no pending handover - show normal starting cash declaration -->
      <div v-else class="w-full max-w-lg bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl p-8 space-y-6">
        <!-- Header -->
        <div class="text-center space-y-2">
          <h2 class="text-xl font-bold tracking-tight text-slate-800 dark:text-white uppercase">
            MỞ CA LÀM VIỆC - CA MỚI ({{ currentDateFormatted }})
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
              Ghi chú mở ca
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
            :disabled="processing"
            class="w-full py-4 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-2xl shadow-lg transition text-sm uppercase tracking-wider disabled:opacity-50"
          >
            {{ processing ? 'ĐANG MỞ CA...' : 'BẮT ĐẦU CA LÀM VIỆC' }}
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

        <div class="flex items-center gap-3">
          <button 
            v-if="isAdmin" 
            @click="forceStaffViewForAdmin = false"
            class="px-3 py-1.5 text-xs font-bold rounded-xl bg-primary/10 text-primary hover:bg-primary/20 transition"
          >
            Quay lại Giao diện Admin
          </button>
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

      <!-- Case when there's no active shift and no pending handover -->
      <div v-if="!activeShift && !pendingHandover" class="w-full max-w-lg bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl p-8 text-center space-y-5 mx-auto">
        <div class="mx-auto h-16 w-16 bg-slate-50 dark:bg-slate-900/40 border border-slate-200 dark:border-slate-700 rounded-full flex items-center justify-center text-slate-400">
          <Clock class="h-8 w-8" />
        </div>
        <div>
          <h2 class="text-xl font-bold tracking-tight text-slate-800 dark:text-white uppercase">Không có ca hoạt động</h2>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
            Bạn hiện không có ca làm việc nào đang hoạt động và không có ca bàn giao nào cần xác nhận.
            Vui lòng mở ca làm việc để bắt đầu thực hiện bán hàng và quản lý ca.
          </p>
        </div>
        <button 
          @click="router.push('/admin/mo-ca')" 
          class="w-full py-3.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-2xl shadow-lg transition"
        >
          Đi đến Mở ca làm việc
        </button>
      </div>

      <!-- Main Content Grid when active shift or pending handover is present -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Left Column: Shift Info & Stats -->
        <div class="lg:col-span-2 space-y-6">
          <!-- THÔNG TIN CA LÀM VIỆC -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider">Thông tin ca làm việc</h3>
              <span 
                class="px-2.5 py-0.5 text-xs font-semibold rounded-full bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300"
              >
                {{ activeShift ? (activeShift.trangThai === 'MO_CA' ? 'Đang hoạt động' : 'Đang bàn giao') : 'Đang chờ xác nhận' }}
              </span>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-3.5 text-sm">
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[100px]">Mã ca:</span>
                <span class="font-bold text-primary">{{ activeShift?.ma || pendingHandover?.ma }}</span>
              </div>
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[100px]">Thời gian vào:</span>
                <span class="font-medium text-slate-700 dark:text-slate-200">
                  {{ displayShift?.thoiGianChamCong ? new Date(displayShift.thoiGianChamCong).toLocaleString('vi-VN') : (displayShift?.thoiGianVao ? new Date(displayShift.thoiGianVao).toLocaleString('vi-VN') : 'N/A') }}
                </span>
              </div>
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[100px]">Nhân viên ca:</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">{{ displayShift?.nhanVienTrongCaTen }}</span>
              </div>
              <div class="flex justify-between md:justify-start gap-4">
                <span class="text-slate-400 min-w-[100px]">Trạng thái:</span>
                <span class="font-semibold text-slate-700 dark:text-slate-200">
                  {{ displayShift?.trangThai === 'MO_CA' ? 'Đang làm việc' : (displayShift?.trangThai === 'CHO_BAN_GIAO' ? 'Đang bàn giao' : (displayShift?.trangThai === 'DA_BAN_GIAO' ? 'Đã đóng ca' : 'N/A')) }}
                </span>
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
                <span class="text-slate-500">Doanh thu tiền mặt</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">+{{ formatVND(doanhThuTienMat) }}</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b border-slate-100 dark:border-slate-700/60 text-sm">
                <span class="text-slate-500">Doanh thu chuyển khoản</span>
                <span class="font-bold text-slate-700 dark:text-slate-200">+{{ formatVND(currentStats?.tienChuyenKhoanTrongCa || pendingHandover?.tienChuyenKhoanTrongCa) }}</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b border-slate-100 dark:border-slate-700/60 text-sm">
                <span class="text-slate-500 flex items-center gap-1.5">
                  Tổng thu khác
                  <button @click="openThuChiModal('THU')" class="text-slate-400 hover:text-primary transition rounded-full hover:bg-primary/10 p-1" title="Xem chi tiết tổng thu">
                    <Eye class="h-3.5 w-3.5" />
                  </button>
                </span>
                <span class="font-bold text-slate-700 dark:text-slate-200">+{{ formatVND(tongThuKhac) }}</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b border-slate-100 dark:border-slate-700/60 text-sm">
                <span class="text-slate-500 flex items-center gap-1.5">
                  Tổng chi trong ca
                  <button @click="openThuChiModal('CHI')" class="text-slate-400 hover:text-rose-500 transition rounded-full hover:bg-rose-50 p-1" title="Xem chi tiết tổng chi">
                    <Eye class="h-3.5 w-3.5" />
                  </button>
                </span>
                <span class="font-bold text-rose-500">{{ formatVND(tongChiTrongCa) }}</span>
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
                <p class="text-lg font-bold text-emerald-700 dark:text-emerald-300 mt-1">{{ formatVND(activeShift ? tienThucTe : pendingHandover?.tienCuoiCaThucTe) }}</p>
              </div>

              <!-- Chênh lệch -->
              <div class="rounded-2xl p-4 text-center flex flex-col justify-center items-center transition-colors"
                   :class="chenhLechDisplay.bgClass">
                <span class="text-[10px] font-bold uppercase tracking-wider"
                      :class="chenhLechDisplay.textClass">
                  Chênh lệch
                </span>
                <p class="text-lg font-bold mt-1" :class="chenhLechDisplay.textClass">
                  {{ chenhLechDisplay.text }}
                </p>
                <span class="text-[10px] font-semibold opacity-80 mt-0.5" :class="chenhLechDisplay.textClass">
                  {{ chenhLechDisplay.note }}
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

            <!-- Step 1: Đóng ca & Bàn giao -->
            <div v-if="buocHienTai === 1" class="space-y-4">
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                  Số tiền bàn giao thực tế
                </label>
                
                <div class="flex items-center gap-3">
                  <div class="relative flex-1">
                    <input 
                      type="number"
                      v-model.number="tienThucTe"
                      min="0"
                      placeholder="Nhập số tiền mặt thực tế..."
                      class="w-full h-12 pl-4 pr-12 rounded-xl border border-slate-200 dark:border-slate-700 bg-transparent text-lg font-bold text-blue-600 dark:text-blue-400 focus:outline-none focus:border-rose-400 transition"
                    />
                    <span class="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-slate-400">đ</span>
                  </div>
                  
                  <button 
                    type="button" 
                    @click="showsCalculator = true"
                    title="Mở bảng tính đếm tiền"
                    class="h-12 w-12 rounded-xl bg-primary/10 hover:bg-primary/20 text-primary flex items-center justify-center transition shadow-sm"
                  >
                    <Calculator class="h-6 w-6" />
                  </button>
                </div>

                <p class="mt-2 text-xs italic text-slate-500 dark:text-slate-400 leading-normal">
                  {{ docTienBangChu(tienThucTe) }}
                </p>
              </div>

              <!-- Receiver dropdown -->
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                  Chọn nhân viên nhận ca <span class="text-rose-500">*</span>
                </label>
                <select v-model="nhanVienNhanId" class="w-full h-11 px-3 border border-slate-200 dark:border-slate-700 rounded-xl bg-white dark:bg-slate-900/60 text-sm font-semibold text-slate-700 dark:text-slate-300 focus:outline-none focus:border-rose-400 transition cursor-pointer">
                  <option value="" disabled>-- Chọn nhân viên nhận ca --</option>
                  <option 
                    v-for="nv in listNhanVien" 
                    :key="nv.id" 
                    :value="nv.id"
                  >
                    {{ nv.hoTen }} ({{ nv.ma }}) - {{ nv.tenVaiTro }}
                  </option>
                </select>
              </div>

              <!-- Lý do chênh lệch (nếu lệch tiền) -->
              <div v-if="chenhLech !== 0">
                <label class="block text-xs font-bold uppercase tracking-wider text-rose-500 mb-1.5">
                  Lý do chênh lệch *
                </label>
                <input 
                  type="text" 
                  v-model="lyDoChenhLech"
                  placeholder="Ví dụ: Đếm sai, thối nhầm tiền..."
                  class="w-full h-11 px-3 border border-rose-300 dark:border-rose-800 rounded-xl bg-transparent text-sm focus:outline-none focus:border-rose-500 transition"
                />
              </div>

              <!-- Note area -->
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                  Ghi chú bàn giao
                </label>
                <textarea 
                  v-model="ghiChu" 
                  rows="3" 
                  maxlength="200"
                  class="w-full p-3 border border-slate-200 dark:border-slate-700 rounded-xl bg-transparent text-sm text-slate-700 dark:text-slate-200 focus:outline-none focus:border-rose-400 transition"
                  placeholder="Nhập ghi chú bàn giao..."
                ></textarea>
                <div class="flex justify-end text-[10px] text-slate-400 mt-1">
                  {{ ghiChu.length }}/200
                </div>
              </div>

              <!-- Button: GỬI YÊU CẦU BÀN GIAO -->
              <button 
                type="button"
                @click="xacNhanBanGiaoCaBtn"
                :disabled="processing"
                class="w-full py-3.5 bg-primary hover:bg-primary/95 text-white font-bold rounded-xl shadow-md transition text-xs uppercase tracking-wider disabled:opacity-50"
              >
                {{ processing ? 'ĐANG GỬI BÀN GIAO...' : 'Gửi yêu cầu bàn giao' }}
              </button>
            </div>

            <!-- Step 2: Chờ nhận ca xác nhận -->
            <div v-else-if="buocHienTai === 2" class="space-y-4">
              <div class="rounded-xl border border-slate-100 dark:border-slate-700/60 p-4 bg-slate-50/50 dark:bg-slate-900/30 text-sm text-center py-6 space-y-3">
                <div class="mx-auto h-12 w-12 bg-amber-50 dark:bg-amber-950/20 border-2 border-amber-500 rounded-full flex items-center justify-center text-amber-500 shadow-sm animate-pulse">
                  <Clock class="h-6 w-6" />
                </div>
                <div>
                  <h4 class="font-bold text-slate-800 dark:text-white text-xs uppercase tracking-wide">Đang chờ xác nhận</h4>
                  <p class="text-[11px] text-slate-400 mt-1 leading-normal">
                    Yêu cầu bàn giao ca đã được gửi thành công. Đang chờ nhân viên nhận ca: <span class="font-bold text-primary">{{ activeShift?.nhanVienNhanTen }}</span> kiểm tra két sắt thực tế và xác nhận bàn giao.
                  </p>
                </div>
                <div class="text-xs text-slate-400 italic pt-2">
                  Vui lòng liên hệ người tiếp quản ca hoặc Admin nếu cần sửa đổi.
                </div>
              </div>
            </div>

            <!-- Step 3: Xác nhận nhận ca -->
            <div v-else-if="buocHienTai === 3" class="space-y-5">
              <!-- Review Data Submitted by Sender -->
              <div class="rounded-xl border border-slate-100 dark:border-slate-700/60 p-4 bg-slate-50/50 dark:bg-slate-900/30 text-sm space-y-2.5">
                <div class="flex justify-between">
                  <span class="text-slate-400">Số tiền bàn giao:</span>
                  <span class="font-bold text-blue-600 dark:text-blue-400">{{ formatVND(pendingHandover?.tienCuoiCaThucTe) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-400">Người bàn giao:</span>
                  <span class="font-semibold text-slate-700 dark:text-slate-200">{{ pendingHandover?.nhanVienTrongCaTen }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="font-semibold" :class="chenhLechDisplay.textClass">Chênh lệch:</span>
                  <span class="font-bold flex gap-1" :class="chenhLechDisplay.textClass">
                    {{ chenhLechDisplay.text }}
                    <span class="text-[11px] opacity-80 mt-0.5">{{ chenhLechDisplay.note }}</span>
                  </span>
                </div>
                <div v-if="pendingHandover?.lyDoChenhLech" class="flex flex-col gap-1 border-t border-slate-100 dark:border-slate-700/50 pt-2">
                  <span class="text-[10px] text-rose-500 font-bold uppercase tracking-wider">Lý do chênh lệch:</span>
                  <span class="text-xs text-rose-600 dark:text-rose-400">{{ pendingHandover.lyDoChenhLech }}</span>
                </div>
                <div class="pt-2 border-t border-slate-200/60">
                  <p class="text-xs text-slate-400 font-bold uppercase tracking-wider mb-1">Ghi chú từ {{ pendingHandover?.nhanVienTrongCaTen }}:</p>
                  <p class="text-slate-600 dark:text-slate-300 text-xs italic bg-white dark:bg-slate-800 p-2.5 rounded-lg border border-slate-100 dark:border-slate-700/60">
                    "{{ pendingHandover?.ghiChu || 'Không có ghi chú' }}"
                  </p>
                </div>
              </div>

              <!-- Warning Notice -->
              <div class="flex items-start gap-2.5 p-3.5 bg-amber-50 dark:bg-amber-950/20 text-amber-800 dark:text-amber-400 border border-amber-200 dark:border-amber-900/50 rounded-xl text-xs font-medium">
                <AlertTriangle class="h-4 w-4 shrink-0" />
                <span>Vui lòng kiểm tra kỹ số tiền mặt thực tế tại két trước khi đồng ý nhận bàn giao ca.</span>
              </div>

              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                  Ghi chú nhận ca
                </label>
                <input 
                  type="text" 
                  v-model="ghiChuNhanCa"
                  placeholder="Nhập ghi chú phản hồi..."
                  class="w-full rounded-xl border border-slate-200 bg-transparent px-3 py-2 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                />
              </div>

              <!-- Action Button -->
              <button 
                @click="dongYNhanCaBtn(pendingHandover?.id)"
                :disabled="processing"
                class="w-full py-4 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl shadow-lg transition flex items-center justify-center gap-2 text-sm uppercase tracking-wide disabled:opacity-50"
              >
                <Check class="h-5 w-5" />
                {{ processing ? 'ĐANG TIẾP NHẬN...' : 'ĐỒNG Ý NHẬN CA' }}
              </button>
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
                  <span class="font-semibold text-slate-700 dark:text-slate-200">{{ displayShift?.nhanVienTrongCaTen }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-400">Người tiếp quản:</span>
                  <span class="font-semibold text-slate-700 dark:text-slate-200">{{ displayShift?.nhanVienNhanTen }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-400">Tiền cuối ca:</span>
                  <span class="font-bold text-emerald-600">{{ formatVND(displayShift?.tienCuoiCaThucTe) }}</span>
                </div>
              </div>
            </div>
          </div>
      </div>
    </div>

    <!-- Bottom Section: Transactions & Confirmations -->
    <div v-if="activeShift || pendingHandover" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <!-- CHI TIẾT GIAO DỊCH TRONG CA (Left 2 columns span) -->
      <div class="lg:col-span-2 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm space-y-4">
        <h3 class="text-sm font-bold text-slate-800 dark:text-white uppercase tracking-wider">Chi tiết giao dịch trong ca</h3>

        <!-- Tabs -->
        <div class="flex gap-2 overflow-x-auto pb-1">
          <button class="px-4 py-1.5 text-xs font-semibold rounded-lg bg-primary text-white shadow-sm shrink-0">
            Doanh thu ca làm việc
          </button>
        </div>

        <!-- Transactions Table -->
        <div class="overflow-x-auto">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="border-b border-slate-100 dark:border-slate-700/60 bg-slate-50/50 dark:bg-slate-900/30 text-slate-500 font-semibold">
                <th class="px-3 py-2.5">Thời gian</th>
                <th class="px-3 py-2.5">Mã hóa đơn</th>
                <th class="px-3 py-2.5">Loại doanh thu</th>
                <th class="px-3 py-2.5">Số tiền</th>
                <th class="px-3 py-2.5">Hình thức</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-50 dark:divide-slate-700/40">
              <tr v-if="loadingTransactions" class="text-center">
                <td colspan="5" class="px-3 py-4 text-slate-500">Đang tải dữ liệu...</td>
              </tr>
              <tr v-else-if="directDisplayInvoices.length === 0" class="text-center">
                <td colspan="5" class="px-3 py-4 text-slate-500">
                  {{ isMyShift ? 'Chưa có hóa đơn nào trong ca này' : 'Chưa có hóa đơn chưa thanh toán nào trong ca này' }}
                </td>
              </tr>
              <tr v-else v-for="tx in directDisplayInvoices" :key="tx.id" class="text-slate-600 dark:text-slate-300">
                <td class="px-3 py-3">{{ new Date(tx.ngayTao).toLocaleTimeString('vi-VN') }}</td>
                <td class="px-3 py-3 font-semibold text-primary">{{ tx.maHoaDon }}</td>
                <td class="px-3 py-3">Bán hàng tại quầy</td>
                <td class="px-3 py-3 font-semibold text-emerald-600">+{{ formatVND(tx.tongTien) }}</td>
                <td class="px-3 py-3">
                  <span class="px-2 py-0.5 text-[10px] font-medium rounded-full"
                        :class="tx.phuongThucThanhToan === 'Chưa thanh toán' ? 'bg-rose-100 text-rose-700 dark:bg-rose-900/40 dark:text-rose-300' : 'bg-slate-100 dark:bg-slate-700'">
                    {{ tx.phuongThucThanhToan || 'N/A' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Accordion / Collapse for Paid Invoices -->
        <div v-if="!isMyShift" class="mt-4 border-t border-slate-100 dark:border-slate-700/60 pt-4">
          <button 
            @click="isPaidInvoicesCollapsed = !isPaidInvoicesCollapsed"
            class="flex items-center justify-between w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-900/30 hover:bg-slate-100/70 dark:hover:bg-slate-900/50 rounded-xl text-xs font-semibold text-slate-700 dark:text-slate-300 transition-all border border-slate-100 dark:border-slate-700/40"
          >
            <span class="flex items-center gap-2">
              <FileCheck class="h-4 w-4 text-emerald-500" />
              <span>
                {{ isPaidInvoicesCollapsed ? `Xem ${paidInvoices.length} hóa đơn đã thanh toán trong ca` : 'Thu gọn' }}
              </span>
            </span>
            <ChevronDown 
              class="h-4 w-4 text-slate-400 transition-transform duration-200" 
              :class="{ 'rotate-180': !isPaidInvoicesCollapsed }"
            />
          </button>

          <!-- Collapsible Content -->
          <div v-show="!isPaidInvoicesCollapsed" class="mt-3 overflow-x-auto border border-slate-100 dark:border-slate-700 rounded-xl">
            <table class="w-full text-left text-xs border-collapse">
              <thead>
                <tr class="border-b border-slate-100 dark:border-slate-700/60 bg-slate-50/50 dark:bg-slate-900/30 text-slate-500 font-semibold">
                  <th class="px-3 py-2.5">Thời gian</th>
                  <th class="px-3 py-2.5">Mã hóa đơn</th>
                  <th class="px-3 py-2.5">Loại doanh thu</th>
                  <th class="px-3 py-2.5">Số tiền</th>
                  <th class="px-3 py-2.5">Hình thức</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-50 dark:divide-slate-700/40">
                <tr v-if="paidInvoices.length === 0" class="text-center">
                  <td colspan="5" class="px-3 py-4 text-slate-500">Không có hóa đơn đã thanh toán nào trong ca này</td>
                </tr>
                <tr v-else v-for="tx in paidInvoices" :key="tx.id" class="text-slate-600 dark:text-slate-300 hover:bg-slate-50/50 dark:hover:bg-slate-900/20">
                  <td class="px-3 py-3">{{ new Date(tx.ngayTao).toLocaleTimeString('vi-VN') }}</td>
                  <td class="px-3 py-3 font-semibold text-primary">{{ tx.maHoaDon }}</td>
                  <td class="px-3 py-3">Bán hàng tại quầy</td>
                  <td class="px-3 py-3 font-semibold text-emerald-600">+{{ formatVND(tx.tongTien) }}</td>
                  <td class="px-3 py-3">
                    <span class="px-2 py-0.5 text-[10px] font-medium rounded-full bg-slate-100 dark:bg-slate-700">
                      {{ tx.phuongThucThanhToan || 'N/A' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="text-center pt-2">
          <span class="text-xs text-slate-400">Số liệu được cập nhật tự động theo thời gian thực từ các hóa đơn tại quầy.</span>
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
                <h4 class="font-bold text-slate-700 dark:text-slate-200 mt-1">{{ displayShift?.nhanVienTrongCaTen || adminSession.hoTen }}</h4>
                <p class="text-[10px] text-slate-400 mt-0.5">
                  Vào ca: {{ displayShift?.thoiGianChamCong ? new Date(displayShift.thoiGianChamCong).toLocaleString('vi-VN') : (displayShift?.thoiGianVao ? new Date(displayShift.thoiGianVao).toLocaleString('vi-VN') : 'N/A') }}
                </p>
              </div>
              
              <span 
                class="px-2.5 py-0.5 text-[10px] font-bold rounded-full"
                :class="displayShift?.trangThai === 'DA_BAN_GIAO' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300' : 'bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300'"
              >
                {{ displayShift?.trangThai === 'DA_BAN_GIAO' ? 'Đã bàn giao' : (displayShift?.trangThai === 'CHO_BAN_GIAO' ? 'Chờ xác nhận' : 'Đang hoạt động') }}
              </span>
            </div>
            
            <div class="mt-4 h-16 border border-dashed border-slate-200 dark:border-slate-700 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-900/40">
              <span class="text-[10px] font-semibold text-slate-400">Không có chữ ký số</span>
            </div>
          </div>

          <!-- Card 2: Nhân viên nhận ca -->
          <div class="border border-slate-100 dark:border-slate-700 rounded-2xl p-4 bg-slate-50/30 dark:bg-slate-900/10">
            <div class="flex justify-between items-start">
              <div>
                <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Nhân viên nhận ca</p>
                <h4 class="font-bold text-slate-700 dark:text-slate-200 mt-1">{{ displayShift?.nhanVienNhanTen || 'Chưa nhận' }}</h4>
                <p class="text-[10px] text-slate-400 mt-0.5">
                  Nhận ca: {{ displayShift?.thoiGianRa ? new Date(displayShift.thoiGianRa).toLocaleString('vi-VN') : 'Chưa nhận' }}
                </p>
              </div>
              
              <span 
                class="px-2.5 py-0.5 text-[10px] font-bold rounded-full"
                :class="displayShift?.trangThai === 'DA_BAN_GIAO' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300' : 'bg-orange-100 text-orange-700 dark:bg-orange-950/40 dark:text-orange-300'"
              >
                {{ displayShift?.trangThai === 'DA_BAN_GIAO' ? 'Đã nhận ca' : 'Chờ xác nhận' }}
              </span>
            </div>

            <!-- Signature block -->
            <div class="mt-4 h-16 border border-dashed border-slate-200 dark:border-slate-700 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-900/40">
              <div v-if="displayShift?.trangThai === 'DA_BAN_GIAO'" class="text-center">
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
  
  <ThuChiModal 
    :show="showThuChiModal" 
    :type="thuChiModalType" 
    :shiftInfo="displayShift || {}"
    @close="showThuChiModal = false" 
  />
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
