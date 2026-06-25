<script setup>
import { computed, ref, watch } from "vue";
import { 
  X, 
  Wallet, 
  ArrowRightLeft, 
  AlertTriangle, 
  CheckCircle2, 
  UserPlus, 
  CircleDollarSign,
  FileSpreadsheet
} from "lucide-vue-next";
import { useGiaoCa } from "../../../composable/useGiaoCa";
import { useAdminSession } from "../../../composable/useAdminSession";
import { layDanhSachNhanVien } from "../../../services/nhan-vien";
import { layThongTinGiaoCaCurrent } from "../../../services/giao-ca";

const props = defineProps({
  show: Boolean
});

const emit = defineEmits(["close"]);

const { 
  activeShift, 
  pendingHandovers, 
  openShift, 
  submitHandover, 
  confirmHandover,
  loadPendingHandovers
} = useGiaoCa();

const { adminSession } = useAdminSession();

// Form opening shift
const tienDauCa = ref(500000);
const moCaGhiChu = ref("");

// Form closing shift
const tienCuoiCaThucTe = ref(0);
const nhanVienNhanId = ref("");
const lyDoChenhLech = ref("");
const banGiaoGhiChu = ref("");
const listNhanVien = ref([]);
const currentStats = ref(null);
const loadingStats = ref(false);

// Form accepting handover
const confirmGhiChu = ref("");

const processing = ref(false);
const messageError = ref("");
const messageSuccess = ref("");

const formatVND = (value) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value || 0);
};

// Fetch employees
async function taiNhanVienGiaoCa() {
  try {
    const list = await layDanhSachNhanVien({ trangThai: 1 });
    // Filter out active user
    listNhanVien.value = list.filter(
      (nv) => String(nv.id) !== String(adminSession.value.id)
    );
  } catch (err) {
    console.error("Lỗi tải danh sách nhân viên:", err);
  }
}

// Fetch active shift stats
async function taiThongKeCaHienTai() {
  if (!activeShift.value) return;
  loadingStats.value = true;
  try {
    const data = await layThongTinGiaoCaCurrent();
    currentStats.value = data;
    tienCuoiCaThucTe.value = data.tienCuoiCaHeThong || 0;
  } catch (err) {
    console.error("Lỗi tải thống kê ca:", err);
  } finally {
    loadingStats.value = false;
  }
}

// Discrepancy computation
const chenhLech = computed(() => {
  if (!currentStats.value) return 0;
  return (tienCuoiCaThucTe.value || 0) - currentStats.value.tienCuoiCaHeThong;
});

const getChenhLechDisplay = (amount) => {
  if (!amount && amount !== 0) amount = 0;
  if (amount === 0) {
    return {
      text: "0 đ",
      note: "(Khớp)",
      bgClass: "bg-slate-50 text-slate-800 dark:bg-slate-950/20 dark:text-slate-300",
      textClass: "text-slate-700 dark:text-slate-300"
    };
  }
  if (amount > 0) {
    return {
      text: `+${formatVND(amount)}`,
      note: "(Thừa)",
      bgClass: "bg-blue-50 text-blue-800 dark:bg-blue-950/20 dark:text-blue-400",
      textClass: "text-blue-700 dark:text-blue-400"
    };
  }
  return {
    text: `${formatVND(amount)}`,
    note: "(Thiếu)",
    bgClass: "bg-rose-50 text-rose-800 dark:bg-rose-950/20 dark:text-rose-400",
    textClass: "text-rose-700 dark:text-rose-400"
  };
};

const currentChenhLechDisplay = computed(() => getChenhLechDisplay(chenhLech.value));

// Trigger loaders on show
watch(() => props.show, async (newVal) => {
  if (newVal) {
    messageError.value = "";
    messageSuccess.value = "";
    moCaGhiChu.value = "";
    banGiaoGhiChu.value = "";
    lyDoChenhLech.value = "";
    nhanVienNhanId.value = "";
    confirmGhiChu.value = "";
    
    if (activeShift.value) {
      await taiThongKeCaHienTai();
      await taiNhanVienGiaoCa();
    } else {
      await loadPendingHandovers();
    }
  }
});

// Submit mo ca
async function submitMoCa() {
  if (tienDauCa.value < 0) {
    messageError.value = "Số tiền đầu ca không thể nhỏ hơn 0";
    return;
  }
  processing.value = true;
  messageError.value = "";
  messageSuccess.value = "";
  
  const res = await openShift(tienDauCa.value, moCaGhiChu.value);
  processing.value = false;
  if (res.success) {
    messageSuccess.value = res.message;
    setTimeout(() => {
      emit("close");
    }, 1500);
  } else {
    messageError.value = res.message;
  }
}

// Submit ban giao ca
async function submitBanGiao() {
  if (!nhanVienNhanId.value) {
    messageError.value = "Vui lòng chọn nhân viên nhận bàn giao ca";
    return;
  }
  if (chenhLech.value !== 0 && (!lyDoChenhLech.value || !lyDoChenhLech.value.trim())) {
    messageError.value = "Vui lòng nhập lý do chênh lệch tiền mặt";
    return;
  }
  
  processing.value = true;
  messageError.value = "";
  messageSuccess.value = "";
  
  const res = await submitHandover({
    tienCuoiCaThucTe: tienCuoiCaThucTe.value,
    nhanVienNhanId: nhanVienNhanId.value,
    lyDoChenhLech: lyDoChenhLech.value,
    ghiChu: banGiaoGhiChu.value
  });
  
  processing.value = false;
  if (res.success) {
    messageSuccess.value = res.message;
    setTimeout(() => {
      emit("close");
    }, 2000);
  } else {
    messageError.value = res.message;
  }
}

// Submit confirm handover
async function submitNhanBanGiao(id) {
  processing.value = true;
  messageError.value = "";
  messageSuccess.value = "";
  
  const res = await confirmHandover(id, confirmGhiChu.value);
  processing.value = false;
  if (res.success) {
    messageSuccess.value = res.message;
    setTimeout(() => {
      emit("close");
    }, 1500);
  } else {
    messageError.value = res.message;
  }
}
</script>

<template>
  <div 
    v-if="show" 
    class="fixed inset-0 z-50 flex items-center justify-center p-4"
  >
    <!-- Overlay -->
    <div 
      class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity"
      @click="emit('close')"
    ></div>

    <!-- Modal Content -->
    <div 
      class="relative w-full max-w-lg overflow-hidden rounded-3xl border border-slate-100 bg-white shadow-2xl transition-all dark:border-slate-700 dark:bg-slate-800"
    >
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4 dark:border-slate-700">
        <h3 class="text-lg font-bold text-slate-800 dark:text-slate-100">
          {{ activeShift ? 'Chi tiết & Bàn giao ca' : 'Mở ca làm việc' }}
        </h3>
        <button 
          @click="emit('close')" 
          class="rounded-xl p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-700 dark:hover:text-slate-200"
        >
          <X class="h-5 w-5" />
        </button>
      </div>

      <!-- Alert Box -->
      <div v-if="messageError" class="mx-6 mt-4 flex items-start gap-2.5 rounded-2xl bg-rose-50 p-3.5 text-sm font-semibold text-rose-700 dark:bg-rose-950/20 dark:text-rose-400">
        <AlertTriangle class="h-5 w-5 shrink-0" />
        <span>{{ messageError }}</span>
      </div>
      <div v-if="messageSuccess" class="mx-6 mt-4 flex items-start gap-2.5 rounded-2xl bg-emerald-50 p-3.5 text-sm font-semibold text-emerald-700 dark:bg-emerald-950/20 dark:text-emerald-400">
        <CheckCircle2 class="h-5 w-5 shrink-0" />
        <span>{{ messageSuccess }}</span>
      </div>

      <!-- Body -->
      <div class="max-h-[75vh] overflow-y-auto p-6">
        
        <!-- CASE 1: ACTIVE SHIFT EXISTS (VIEW STATS & HANDOVER) -->
        <div v-if="activeShift" class="space-y-5">
          <div class="rounded-2xl bg-slate-50 p-4 dark:bg-slate-700/40">
            <div class="grid grid-cols-2 gap-y-2.5 text-sm">
              <span class="font-medium text-slate-500 dark:text-slate-400">Mã ca:</span>
              <span class="font-bold text-slate-800 dark:text-slate-200 text-right">{{ activeShift.ma }}</span>
              
              <span class="font-medium text-slate-500 dark:text-slate-400">Nhân viên:</span>
              <span class="font-semibold text-slate-800 dark:text-slate-200 text-right">{{ activeShift.nhanVienTrongCaTen }}</span>
              
              <span class="font-medium text-slate-500 dark:text-slate-400">Bắt đầu:</span>
              <span class="text-slate-800 dark:text-slate-200 text-right">
                {{ new Date(activeShift.thoiGianVao).toLocaleString('vi-VN') }}
              </span>
            </div>
          </div>

          <!-- Live Financial Stats -->
          <div v-if="loadingStats" class="flex flex-col items-center py-6">
            <div class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
            <span class="mt-2 text-sm text-slate-500">Đang tải thống kê doanh thu...</span>
          </div>

          <div v-else-if="currentStats" class="space-y-4">
            <h4 class="text-sm font-bold text-slate-800 dark:text-slate-200">
              Doanh thu phát sinh trong ca
            </h4>
            
            <div class="grid grid-cols-2 gap-3">
              <div class="rounded-2xl border border-slate-100 p-4 dark:border-slate-700 bg-white dark:bg-slate-800">
                <span class="text-[11px] font-bold uppercase tracking-wider text-slate-400">Tiền đầu ca</span>
                <p class="mt-1 text-base font-bold text-slate-800 dark:text-slate-100">
                  {{ formatVND(activeShift.tienDauCa) }}
                </p>
              </div>

              <div class="rounded-2xl border border-slate-100 p-4 dark:border-slate-700 bg-white dark:bg-slate-800">
                <span class="text-[11px] font-bold uppercase tracking-wider text-slate-400">Doanh thu tiền mặt</span>
                <p class="mt-1 text-base font-bold text-slate-800 dark:text-slate-100">
                  +{{ formatVND(currentStats.tienMatTrongCa) }}
                </p>
              </div>

              <div class="rounded-2xl border border-slate-100 p-4 dark:border-slate-700 bg-white dark:bg-slate-800">
                <span class="text-[11px] font-bold uppercase tracking-wider text-slate-400">Doanh thu chuyển khoản</span>
                <p class="mt-1 text-base font-bold text-slate-800 dark:text-slate-100">
                  {{ formatVND(currentStats.tienChuyenKhoanTrongCa) }}
                </p>
              </div>

              <div class="rounded-2xl border border-emerald-100 bg-emerald-50/20 p-4 dark:border-emerald-900/50 dark:bg-emerald-950/10">
                <span class="text-[11px] font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400">Tổng tiền mặt hệ thống</span>
                <p class="mt-1 text-base font-bold text-emerald-700 dark:text-emerald-400">
                  {{ formatVND(currentStats.tienCuoiCaHeThong) }}
                </p>
              </div>
            </div>

            <!-- Handover Form Fields -->
            <div class="border-t border-slate-100 pt-4 dark:border-slate-700 space-y-4">
              <h4 class="text-sm font-bold text-slate-800 dark:text-slate-200">
                Thông tin bàn giao
              </h4>

              <!-- Actual Cash Input -->
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                  Tiền mặt thực tế tại quầy <span class="text-rose-500">*</span>
                </label>
                <div class="relative rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden focus-within:border-primary">
                  <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-sm">đ</span>
                  <input 
                    type="number" 
                    v-model.number="tienCuoiCaThucTe" 
                    placeholder="Nhập tổng số tiền mặt thực tế tại két"
                    class="w-full bg-transparent pl-8 pr-4 py-3 text-sm font-semibold outline-none text-slate-800 dark:text-slate-100"
                  />
                </div>
                <p class="mt-1.5 text-xs text-slate-500">
                  Định dạng: {{ formatVND(tienCuoiCaThucTe) }}
                </p>
              </div>

              <!-- Discrepancy indicator -->
              <div 
                class="rounded-2xl p-4 text-sm font-semibold transition-colors"
                :class="currentChenhLechDisplay.bgClass"
              >
                <div class="flex items-center gap-2">
                  <AlertTriangle class="h-5 w-5" v-if="chenhLech !== 0" />
                  <CheckCircle2 class="h-5 w-5" v-else />
                  <span>
                    Chênh lệch tiền mặt: 
                    {{ currentChenhLechDisplay.text }}
                    {{ currentChenhLechDisplay.note }}
                  </span>
                </div>
                
                <!-- Reason for discrepancy -->
                <div class="mt-3">
                  <label class="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-1.5">
                    Lý do chênh lệch <span class="text-rose-500">*</span>
                  </label>
                  <input 
                    type="text" 
                    v-model="lyDoChenhLech"
                    placeholder="Nhập chi tiết lý do chênh lệch (ví dụ: thối nhầm tiền...)"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-primary dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100"
                  />
                </div>
              </div>

              <!-- Next Employee Dropdown -->
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                  Nhân viên nhận bàn giao <span class="text-rose-500">*</span>
                </label>
                <select 
                  v-model="nhanVienNhanId"
                  class="w-full rounded-2xl border border-slate-200 bg-transparent px-4 py-3 text-sm font-semibold text-slate-700 outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300"
                >
                  <option value="" disabled>-- Chọn nhân viên tiếp quản --</option>
                  <option 
                    v-for="nv in listNhanVien" 
                    :key="nv.id" 
                    :value="nv.id"
                  >
                    {{ nv.hoTen }} ({{ nv.ma }}) - {{ nv.tenVaiTro }}
                  </option>
                </select>
              </div>

              <!-- Note -->
              <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                  Ghi chú bàn giao
                </label>
                <textarea 
                  v-model="banGiaoGhiChu" 
                  rows="2"
                  placeholder="Nhập ghi chú thêm..."
                  class="w-full rounded-2xl border border-slate-200 bg-transparent p-3 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                ></textarea>
              </div>
            </div>

            <!-- Handover Button -->
            <button 
              @click="submitBanGiao"
              :disabled="processing"
              class="w-full py-3.5 bg-primary hover:bg-primary/95 text-white font-bold rounded-2xl shadow-lg transition disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <ArrowRightLeft class="h-5 w-5" />
              <span>{{ processing ? 'Đang xử lý bàn giao...' : 'Thực hiện bàn giao ca' }}</span>
            </button>
          </div>
        </div>

        <!-- CASE 2: NO ACTIVE SHIFT (OPEN NEW SHIFT / VIEW PENDING HANDOVERS) -->
        <div v-else class="space-y-6">
          
          <!-- Section 2A: Pending Handover to Accept -->
          <div v-if="pendingHandovers && pendingHandovers.length > 0" class="space-y-4">
            <h4 class="text-xs font-bold uppercase tracking-wider text-slate-400">
              Có ca bàn giao đang chờ bạn xác nhận
            </h4>

            <div 
              v-for="pc in pendingHandovers" 
              :key="pc.id" 
              class="rounded-3xl border border-primary/20 bg-primary/5 p-5 dark:border-primary/30 dark:bg-slate-700/50"
            >
              <div class="flex items-center gap-3">
                <div class="rounded-full bg-primary/10 p-2.5 text-primary">
                  <FileSpreadsheet class="h-5 w-5" />
                </div>
                <div>
                  <h5 class="font-bold text-slate-800 dark:text-slate-200">Ca làm việc: {{ pc.ma }}</h5>
                  <p class="text-xs text-slate-500 mt-0.5">
                    Bàn giao bởi: <span class="font-semibold text-slate-700 dark:text-slate-300">{{ pc.nhanVienTrongCaTen }}</span>
                  </p>
                </div>
              </div>

              <div class="mt-4 grid grid-cols-2 gap-2 text-sm border-t border-slate-200/50 dark:border-slate-600/50 pt-3.5">
                <span class="text-slate-500">Tiền mặt bàn giao:</span>
                <span class="font-bold text-slate-800 dark:text-slate-200 text-right">{{ formatVND(pc.tienCuoiCaThucTe) }}</span>
                
                <span class="text-slate-500">Giờ kết thúc:</span>
                <span class="text-slate-800 dark:text-slate-200 text-right">
                  {{ new Date(pc.thoiGianRa).toLocaleTimeString('vi-VN') }} {{ new Date(pc.thoiGianRa).toLocaleDateString('vi-VN') }}
                </span>
                
                <span class="font-semibold" :class="getChenhLechDisplay(pc.tienChenhLech).textClass">Chênh lệch:</span>
                <span class="font-bold text-right" :class="getChenhLechDisplay(pc.tienChenhLech).textClass">
                  {{ getChenhLechDisplay(pc.tienChenhLech).text }} {{ getChenhLechDisplay(pc.tienChenhLech).note }}
                </span>
              </div>

              <!-- Note / Comment from previous shift -->
              <div v-if="pc.ghiChu" class="mt-3 text-xs bg-white/50 dark:bg-slate-800/60 p-2.5 rounded-xl border border-slate-100 dark:border-slate-700/60">
                <span class="font-semibold text-slate-500">Ghi chú:</span> {{ pc.ghiChu }}
              </div>

              <!-- Textbox to comment when receiving -->
              <div class="mt-4">
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                  Ghi chú khi nhận ca
                </label>
                <input 
                  type="text" 
                  v-model="confirmGhiChu"
                  placeholder="Nhập phản hồi (nếu có)..."
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                />
              </div>

              <!-- Confirm Button -->
              <button 
                @click="submitNhanBanGiao(pc.id)"
                :disabled="processing"
                class="mt-4 w-full py-3 bg-emerald-600 hover:bg-emerald-600/95 text-white font-bold rounded-2xl shadow-lg transition flex items-center justify-center gap-2 disabled:opacity-50"
              >
                <CheckCircle2 class="h-5 w-5" />
                <span>{{ processing ? 'Đang xác nhận...' : 'Nhận bàn giao & Mở ca' }}</span>
              </button>
            </div>
            
            <div class="relative flex py-2 items-center">
              <div class="flex-grow border-t border-slate-200 dark:border-slate-700"></div>
              <span class="flex-shrink mx-4 text-slate-400 text-xs font-bold uppercase tracking-wider">Hoặc mở ca độc lập</span>
              <div class="flex-grow border-t border-slate-200 dark:border-slate-700"></div>
            </div>
          </div>

          <!-- Section 2B: Normal Open Shift Form -->
          <div class="space-y-4">
            <div class="flex items-center gap-3">
              <div class="rounded-2xl bg-primary/10 p-3 text-primary">
                <Wallet class="h-6 w-6" />
              </div>
              <div>
                <h4 class="font-bold text-slate-800 dark:text-slate-200">Khai báo tiền mặt đầu ca</h4>
                <p class="text-xs text-slate-500 mt-0.5">Nhập số tiền mặt có sẵn trong két để thối tiền</p>
              </div>
            </div>

            <!-- Starting Cash Input -->
            <div>
              <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                Số tiền mặt đầu ca <span class="text-rose-500">*</span>
              </label>
              <div class="relative rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden focus-within:border-primary">
                <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-sm">đ</span>
                <input 
                  type="number" 
                  v-model.number="tienDauCa" 
                  placeholder="Ví dụ: 500000"
                  class="w-full bg-transparent pl-8 pr-4 py-3 text-sm font-semibold outline-none text-slate-800 dark:text-slate-100"
                />
              </div>
              <p class="mt-1.5 text-xs text-slate-500">
                Định dạng: {{ formatVND(tienDauCa) }}
              </p>
            </div>

            <!-- Ghi chu -->
            <div>
              <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
                Ghi chú mở ca
              </label>
              <textarea 
                v-model="moCaGhiChu" 
                rows="2"
                placeholder="Ví dụ: Ca sáng thứ hai, két tiền ngăn nắp..."
                class="w-full rounded-2xl border border-slate-200 bg-transparent p-3 text-sm outline-none focus:border-primary dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
              ></textarea>
            </div>

            <!-- Open Button -->
            <button 
              @click="submitMoCa"
              :disabled="processing"
              class="w-full py-3.5 bg-primary hover:bg-primary/95 text-white font-bold rounded-2xl shadow-lg transition disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <UserPlus class="h-5 w-5" />
              <span>{{ processing ? 'Đang mở ca...' : 'Xác nhận mở ca làm việc' }}</span>
            </button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>
