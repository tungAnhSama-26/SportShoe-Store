<template>
  <div class="w-full bg-white rounded-3xl shadow-sm min-h-[calc(100vh-80px)] p-8">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-slate-800">Chấm công</h1>
      <p class="text-sm text-slate-500 mt-1">Chấm công bằng khuôn mặt</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
      <!-- CỘT TRÁI (LEFT COLUMN) -->
      <div class="lg:col-span-4 space-y-6">
        
        <!-- Ca hiện tại -->
        <div class="rounded-2xl border border-emerald-100 bg-emerald-50/50 p-4 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex items-center justify-center w-10 h-10 rounded-full text-emerald-500" :class="caHienTaiInfo.iconClass">
              <Sun class="h-5 w-5" />
            </div>
            <div>
              <p class="font-bold text-emerald-700 text-sm">Ca hiện tại: {{ caHienTaiInfo.name }}</p>
              <p class="text-[11px] font-medium text-emerald-600/80 mt-0.5">{{ caHienTaiInfo.timeRange }}</p>
            </div>
          </div>
          <span class="px-2.5 py-1 text-[10px] font-bold rounded-full" :class="caHienTaiInfo.statusClass">
            {{ caHienTaiInfo.status }}
          </span>
        </div>

        <!-- Thời gian hiện tại -->
        <div class="rounded-3xl border border-slate-200 p-6 shadow-[0_2px_10px_-3px_rgba(6,81,237,0.05)] bg-white text-center">
          <p class="text-sm font-medium text-slate-500 mb-2">Thời gian hiện tại</p>
          <div class="text-[40px] font-bold text-emerald-500 tracking-tight leading-none mb-2 font-mono">
            {{ formatTime(currentTime) }}
          </div>
          <p class="text-sm font-medium text-slate-600 mb-6">{{ formatDate(currentTime) }}</p>
          
          <div class="flex items-center justify-between pt-4 border-t border-slate-100 text-[11px] font-semibold">
            <div class="flex items-center gap-1.5 text-slate-500">
              <MapPin class="h-3.5 w-3.5" />
              <span>Cửa hàng</span>
            </div>
            <div class="flex items-center gap-1 text-emerald-500">
              <span>Định vị: Chính xác</span>
            </div>
          </div>
        </div>

        <!-- Lịch sử chấm công hôm nay -->
        <div class="rounded-3xl border border-slate-200 p-5 shadow-sm bg-white">
          <h3 class="font-bold text-slate-800 text-sm mb-5">Lịch sử chấm công hôm nay</h3>
          <div class="space-y-4">
            <div v-if="!todayLogs.length" class="text-center py-6 text-xs text-slate-400 font-medium">
              Chưa ghi nhận lượt chấm công nào hôm nay.
            </div>
            <!-- Timeline Item -->
            <div 
              v-else 
              v-for="(log, idx) in todayLogs" 
              :key="idx" 
              class="flex items-start gap-4"
            >
              <div 
                class="flex items-center justify-center w-8 h-8 rounded-full shrink-0 mt-0.5"
                :class="log.type === 'in' ? 'bg-emerald-50 text-emerald-500' : 'bg-orange-50 text-orange-500'"
              >
                <ArrowDown v-if="log.type === 'in'" class="h-4 w-4" />
                <ArrowUp v-else class="h-4 w-4" />
              </div>
              <div class="flex-1 flex justify-between pb-4" :class="idx < todayLogs.length - 1 ? 'border-b border-slate-50' : ''">
                <div>
                  <p class="text-sm font-bold text-slate-700">{{ log.label }}</p>
                  <div class="flex items-center gap-2 mt-0.5">
                    <span class="text-xs text-slate-500 font-semibold font-mono">{{ log.time }}</span>
                    <span class="px-1.5 py-0.5 rounded text-[9px] font-bold" :class="log.statusColor">{{ log.status }}</span>
                  </div>
                </div>
                <p class="text-[11px] font-medium text-slate-400 mt-0.5">{{ formatDateShort(currentTime) }}</p>
              </div>
            </div>
          </div>
          
          <button @click="taiLichSuToanBo" class="w-full mt-4 py-3 rounded-2xl border border-slate-100 text-xs font-bold text-slate-600 hover:bg-slate-50 transition flex items-center justify-center gap-1">
            Xem tất cả lịch sử chấm công <ChevronRight class="h-3 w-3" />
          </button>
        </div>

        <!-- Lưu ý -->
        <div class="rounded-3xl border border-slate-200 p-5 shadow-sm bg-white">
          <h3 class="font-bold text-slate-800 text-sm mb-4">Lưu ý</h3>
          <ul class="space-y-3 text-xs font-medium text-slate-500">
            <li class="flex items-center gap-2.5">
              <Lightbulb class="h-4 w-4 text-slate-400" />
              Đứng đối diện camera, nhìn thẳng
            </li>
            <li class="flex items-center gap-2.5">
              <Sun class="h-4 w-4 text-slate-400" />
              Đảm bảo đủ ánh sáng, không đeo khẩu trang
            </li>
            <li class="flex items-center gap-2.5">
              <Scan class="h-4 w-4 text-slate-400" />
              Không che khuôn mặt (tóc, tay, vật dụng...)
            </li>
          </ul>
        </div>
      </div>

      <!-- CỘT PHẢI (RIGHT COLUMN) -->
      <div class="lg:col-span-8 flex flex-col h-full">
        <div class="rounded-3xl border border-slate-200 p-8 shadow-sm bg-white flex-1 flex flex-col">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h2 class="text-xl font-bold text-slate-800">Chấm công bằng khuôn mặt</h2>
              <p class="text-sm text-slate-500 mt-1">Đưa khuôn mặt vào khung hình để xác thực</p>
            </div>
            <button 
              @click="toggleDen" 
              class="flex items-center gap-2 px-4 py-2 rounded-xl border transition text-sm font-semibold"
              :class="denBat ? 'bg-amber-500 border-amber-500 text-white hover:bg-amber-600' : 'border-slate-200 text-slate-600 hover:bg-slate-50'"
            >
              <Lightbulb class="h-4 w-4" :class="denBat ? 'fill-current' : ''" /> 
              <span>{{ denBat ? 'Tắt đèn' : 'Bật đèn' }}</span>
            </button>
          </div>

          <!-- Camera Viewfinder -->
          <div class="relative w-full flex-1 min-h-[400px] rounded-3xl overflow-hidden bg-slate-100 flex items-center justify-center">
            
            <!-- Scanning Overlay / Light helper -->
            <div 
              class="absolute inset-0 transition-colors duration-300 pointer-events-none"
              :class="denBat ? 'bg-white/40 ring-[16px] ring-white inset-0 z-10' : 'bg-white/10'"
            ></div>
            
            <!-- Focus Brackets -->
            <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-80">
              <!-- Top Left -->
              <div class="absolute top-0 left-0 w-10 h-10 border-t-4 border-l-4 border-emerald-500 rounded-tl-3xl"></div>
              <!-- Top Right -->
              <div class="absolute top-0 right-0 w-10 h-10 border-t-4 border-r-4 border-emerald-500 rounded-tr-3xl"></div>
              <!-- Bottom Left -->
              <div class="absolute bottom-0 left-0 w-10 h-10 border-b-4 border-l-4 border-emerald-500 rounded-bl-3xl"></div>
              <!-- Bottom Right -->
              <div class="absolute bottom-0 right-0 w-10 h-10 border-b-4 border-r-4 border-emerald-500 rounded-br-3xl"></div>
              
              <!-- Scanning Line Animation -->
              <div class="absolute top-0 left-0 w-full h-0.5 bg-emerald-500 shadow-[0_0_8px_2px_rgba(16,185,129,0.5)] animate-[scan_3s_ease-in-out_infinite]"></div>
            </div>
          </div>

          <!-- Icons -->
          <div class="flex justify-center gap-16 mt-8 mb-8">
            <div class="flex flex-col items-center gap-2">
              <Sun class="h-6 w-6 text-emerald-500" />
              <span class="text-xs font-semibold text-slate-500">Đủ ánh sáng</span>
            </div>
            <div class="flex flex-col items-center gap-2">
              <User class="h-6 w-6 text-emerald-500" />
              <span class="text-xs font-semibold text-slate-500">Nhìn thẳng</span>
            </div>
            <div class="flex flex-col items-center gap-2">
              <Smile class="h-6 w-6 text-emerald-500" />
              <span class="text-xs font-semibold text-slate-500">Không đeo<br>khẩu trang</span>
            </div>
          </div>

          <!-- Action Button -->
          <div v-if="!todayRecord?.thoiGianVao && currentGateStatus === 'VUNG_DO'" class="w-full py-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 font-bold text-lg flex items-center justify-center gap-2">
            <X class="h-6 w-6 text-rose-500" />
            <span>ĐÃ KHÓA CA (Vắng mặt không phép)</span>
          </div>
          <button 
            v-else
            @click="handleCheckInClick" 
            :disabled="dangXuLy || nutHanhDongInfo.disabled" 
            class="w-full py-4 rounded-2xl transition font-bold text-lg flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
            :class="nutHanhDongInfo.class"
          >
            <Camera v-if="!dangXuLy" class="h-6 w-6" />
            <div v-else class="w-6 h-6 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
            {{ dangXuLy ? 'Đang xử lý...' : nutHanhDongInfo.text }}
          </button>

          <!-- Privacy Note -->
          <div class="flex items-center justify-center gap-1.5 mt-4 text-[11px] font-semibold text-slate-400">
            <ShieldCheck class="h-4 w-4" />
            <span>Thông tin khuôn mặt của bạn được mã hóa và bảo mật tuyệt đối.</span>
          </div>
        </div>
      </div>
    </div>
    <FaceIdCheckInModal 
      :show="showFaceIdModal" 
      :saved-descriptor-string="faceDescriptorString"
      @close="showFaceIdModal = false"
      @success="thucHienCheckIn"
    />

    <!-- Modal Xem Lịch Sử Chấm Công -->
    <Teleport to="body">
      <div v-if="showHistoryModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-sm">
        <div class="relative w-full max-w-2xl rounded-[28px] bg-white p-6 shadow-2xl mx-4 flex flex-col max-h-[85vh]">
          <!-- Header -->
          <div class="mb-5 flex items-center justify-between border-b border-slate-100 pb-4">
            <div class="flex items-center gap-2.5">
              <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-500">
                <CalendarDays class="h-5 w-5" />
              </div>
              <div>
                <h3 class="text-[18px] font-bold text-slate-900">Lịch sử chấm công (30 ngày gần đây)</h3>
                <p class="text-xs text-slate-400 mt-0.5">{{ allHistory.length }} bản ghi</p>
              </div>
            </div>
            <button type="button" @click="showHistoryModal = false"
              class="flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
              <X class="h-4 w-4" />
            </button>
          </div>

          <!-- Content -->
          <div class="overflow-y-auto flex-1 pr-1 space-y-3">
            <div v-if="dangTaiHistory" class="py-12 text-center text-sm text-slate-400">
              <div class="inline-flex h-8 w-8 animate-spin items-center justify-center rounded-full border-4 border-slate-200 border-t-primary mb-3"></div>
              <p>Đang tải lịch sử chấm công...</p>
            </div>
            <div v-else-if="!allHistory.length" class="py-12 text-center text-sm text-slate-400">
              Không tìm thấy lịch sử chấm công nào.
            </div>
            <div 
              v-else 
              v-for="record in allHistory" 
              :key="record.id" 
              class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 rounded-2xl border border-slate-100 bg-slate-50/50 hover:bg-slate-50 transition"
            >
              <!-- Shift & Date info -->
              <div class="flex items-center gap-3">
                <div 
                  class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl font-bold text-xs"
                  :class="
                    record.ca === 'sang'
                      ? 'bg-amber-50 text-amber-700'
                      : record.ca === 'chieu'
                        ? 'bg-sky-50 text-sky-700'
                        : 'bg-violet-50 text-violet-700'
                  "
                >
                  {{ record.ca === 'sang' ? 'Sáng' : record.ca === 'chieu' ? 'Chiều' : 'Tối' }}
                </div>
                <div>
                  <p class="font-bold text-slate-800 text-sm">{{ formatNgay(record.ngay) }}</p>
                  <p class="text-xs text-slate-400 mt-0.5">Ca làm: {{ record.ca === 'sang' ? 'Ca sáng (08:00 - 12:00)' : record.ca === 'chieu' ? 'Ca chiều (13:00 - 17:00)' : 'Ca tối (18:00 - 22:00)' }}</p>
                </div>
              </div>

              <!-- Check-in / Check-out status -->
              <div class="flex flex-wrap items-center gap-2.5 sm:justify-end">
                <!-- Check-in state -->
                <div class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white border border-slate-100 text-xs font-semibold text-slate-600">
                  <ArrowDown class="h-3.5 w-3.5 text-emerald-500" />
                  <span>Vào: {{ record.gioVao || '—' }}</span>
                  <span 
                    v-if="record.gioVao" 
                    class="ml-1 px-1.5 py-0.5 rounded text-[10px]" 
                    :class="record.trangThaiVao === 'DI_TRE' ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'"
                  >
                    {{ record.trangThaiVao === 'DI_TRE' ? 'Trễ' : 'Đúng giờ' }}
                  </span>
                </div>
                <!-- Check-out state -->
                <div class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white border border-slate-100 text-xs font-semibold text-slate-600">
                  <ArrowUp class="h-3.5 w-3.5 text-orange-500" />
                  <span>Ra: {{ record.gioRa || '—' }}</span>
                  <span 
                    v-if="record.gioRa" 
                    class="ml-1 px-1.5 py-0.5 rounded text-[10px]" 
                    :class="record.trangThaiRa === 'VE_SOM' ? 'bg-rose-100 text-rose-700' : 'bg-emerald-100 text-emerald-700'"
                  >
                    {{ record.trangThaiRa === 'VE_SOM' ? 'Sớm' : 'Đúng giờ' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
    <!-- Popup Chúc mừng Chấm công thành công -->
    <div 
      v-if="showSuccessPopup" 
      class="fixed inset-0 bg-slate-900/60 backdrop-blur-md flex items-center justify-center z-[100] p-4 animate-in fade-in duration-300"
    >
      <div 
        class="bg-white/95 rounded-[32px] shadow-2xl border border-slate-100 max-w-md w-full p-8 text-center relative overflow-hidden transform transition-all scale-100 animate-in zoom-in-95 duration-300"
      >
        <div class="absolute -top-10 -left-10 w-40 h-40 bg-emerald-500/10 rounded-full blur-2xl pointer-events-none"></div>
        <div class="absolute -bottom-10 -right-10 w-40 h-40 bg-emerald-500/10 rounded-full blur-2xl pointer-events-none"></div>

        <div class="flex flex-col items-center">
          <div class="w-20 h-20 bg-emerald-50 rounded-full flex items-center justify-center mb-6 shadow-inner animate-[bounce_1.5s_infinite]">
            <span class="text-4xl">🎉</span>
          </div>

          <h3 class="text-2xl font-black text-slate-800 tracking-tight uppercase mb-2">
            {{ successPopupDetails.type === 'checkin' ? 'CHECK-IN THÀNH CÔNG!' : 'CHECK-OUT THÀNH CÔNG!' }}
          </h3>
          
          <div class="w-full bg-slate-50/80 border border-slate-100 rounded-2xl p-5 mb-6 text-left space-y-3">
            <div class="flex justify-between items-center text-sm">
              <span class="text-slate-400 font-semibold">Nhân viên</span>
              <span class="font-bold text-slate-700">{{ successPopupDetails.employeeName }}</span>
            </div>
            <div class="flex justify-between items-center text-sm">
              <span class="text-slate-400 font-semibold">Ca làm</span>
              <span class="font-bold text-slate-700 uppercase">Ca {{ successPopupDetails.caName }}</span>
            </div>
            <div class="flex justify-between items-center text-sm">
              <span class="text-slate-400 font-semibold">Giờ ghi nhận</span>
              <div class="text-right">
                <span class="font-bold text-slate-800 font-mono">{{ successPopupDetails.gioGhiNhan }}</span>
                <span 
                  class="ml-2 text-[10px] font-bold px-2 py-0.5 rounded-lg"
                  :class="successPopupDetails.note.includes('trễ') || successPopupDetails.note.includes('sớm') ? 'bg-rose-50 text-rose-600' : 'bg-emerald-50 text-emerald-600'"
                >
                  {{ successPopupDetails.note }}
                </span>
              </div>
            </div>
          </div>

          <p class="text-sm font-semibold text-slate-500 mb-8 leading-relaxed px-4">
            {{ successPopupDetails.message }}
          </p>

          <button 
            @click="closeSuccessPopup" 
            class="w-full py-4 bg-emerald-500 hover:bg-emerald-600 active:scale-[0.98] text-white font-bold rounded-2xl shadow-lg shadow-emerald-500/20 transition cursor-pointer"
          >
            Quay lại trang chủ
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { 
  Sun, MapPin, ArrowDown, ArrowUp, ChevronRight, 
  Lightbulb, Scan, User, Smile, Camera, ShieldCheck,
  X, CalendarDays
} from 'lucide-vue-next';
import { showSuccess, showError } from '../../../utils/alert';
import { checkIn, checkOut, layChamCong, layServerTime } from '../../../services/cham-cong';
import { useAdminSession } from '../../../composable/useAdminSession';
import { getDisplayErrorMessage } from '../../../utils/error-message';
import FaceIdCheckInModal from './components/FaceIdCheckInModal.vue';
import { layChiTietNhanVien } from '../../../services/nhan-vien';

const { adminSession } = useAdminSession();

const currentTime = ref(new Date());
const clockOffset = ref(0);
let timer = null;

const dangXuLy = ref(false);
const showFaceIdModal = ref(false);
const faceDescriptorString = ref("");

// State cho ca làm và logs hôm nay
const todayRecord = ref(null);
const todayLogs = ref([]);

// State cho tính năng bật đèn
const denBat = ref(false);

// State cho xem lịch sử
const showHistoryModal = ref(false);
const allHistory = ref([]);
const dangTaiHistory = ref(false);

// State cho popup chúc mừng chấm công
const showSuccessPopup = ref(false);
const successPopupDetails = ref({
  type: 'checkin',
  employeeName: '',
  caName: '',
  gioGhiNhan: '',
  message: '',
  note: ''
});

onMounted(async () => {
  await syncServerTime();
  
  timer = setInterval(() => {
    currentTime.value = new Date(Date.now() + clockOffset.value);
  }, 1000);
  
  await taiThongTinChamCong();
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

async function syncServerTime() {
  try {
    const res = await layServerTime();
    if (res && res.serverTime) {
      const serverTimeMs = new Date(res.serverTime).getTime();
      const localTimeMs = Date.now();
      clockOffset.value = serverTimeMs - localTimeMs;
    }
  } catch (error) {
    console.error("Lỗi đồng bộ thời gian từ server:", error);
  }
}

function getFriendlyCaName(ca) {
  if (ca === 'sang') return 'sáng';
  if (ca === 'chieu') return 'chiều';
  if (ca === 'toi') return 'tối';
  return ca;
}

function closeSuccessPopup() {
  showSuccessPopup.value = false;
  showFaceIdModal.value = false;
  taiThongTinChamCong();
}

const caTimeLimits = computed(() => {
  if (!todayRecord.value) return null;
  const ca = todayRecord.value.ca;
  const now = new Date(Date.now() + clockOffset.value);
  const start = new Date(now);
  const end = new Date(now);
  
  if (ca === 'sang') {
    start.setHours(8, 0, 0, 0);
    end.setHours(12, 0, 0, 0);
  } else if (ca === 'chieu') {
    start.setHours(13, 0, 0, 0);
    end.setHours(17, 0, 0, 0);
  } else if (ca === 'toi') {
    start.setHours(18, 0, 0, 0);
    end.setHours(22, 0, 0, 0);
  } else {
    start.setHours(8, 0, 0, 0);
    end.setHours(12, 0, 0, 0);
  }
  
  const opening = new Date(start.getTime() - 30 * 60 * 1000);
  const startPlus5 = new Date(start.getTime() + 5 * 60 * 1000);
  
  return { start, end, opening, startPlus5 };
});

const currentGateStatus = computed(() => {
  if (!caTimeLimits.value) return null;
  const now = new Date(Date.now() + clockOffset.value);
  const { opening, startPlus5, end } = caTimeLimits.value;
  
  if (now < opening) return 'VUNG_SOM';
  if (now >= opening && now <= startPlus5) return 'VUNG_XANH';
  if (now > startPlus5 && now <= end) return 'VUNG_CAM';
  return 'VUNG_DO';
});

function formatTime(date) {
  const h = String(date.getHours()).padStart(2, '0');
  const m = String(date.getMinutes()).padStart(2, '0');
  const s = String(date.getSeconds()).padStart(2, '0');
  return `${h}:${m}:${s}`;
}

function formatDate(date) {
  const dayOfWeek = date.getDay() === 0 ? "Chủ nhật" : `Thứ ${date.getDay() + 1}`;
  const d = String(date.getDate()).padStart(2, '0');
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const y = date.getFullYear();
  return `${dayOfWeek}, ${d}/${m}/${y}`;
}

function formatDateShort(date) {
  const dayOfWeek = date.getDay() === 0 ? "CN" : `Thứ ${date.getDay() + 1}`;
  const d = String(date.getDate()).padStart(2, '0');
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const y = date.getFullYear();
  return `${dayOfWeek}, ${d}/${m}/${y}`;
}

function formatTimeOnly(isoString) {
  if (!isoString) return "";
  const d = new Date(isoString);
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function formatNgay(ngay) {
  if (!ngay) return "—";
  if (ngay.includes("-")) {
    const [y, m, d] = ngay.split("-");
    return `${d}/${m}/${y}`;
  }
  return ngay;
}

// Bật/tắt đèn trợ sáng
function toggleDen() {
  denBat.value = !denBat.value;
}

// Tải thông tin chấm công hôm nay
async function taiThongTinChamCong() {
  if (!adminSession.value?.id) return;
  try {
    const todayStr = new Date().toLocaleDateString("en-CA");
    const dsToday = await layChamCong({ tuNgay: todayStr, denNgay: todayStr });
    const records = Array.isArray(dsToday) ? dsToday : [];
    
    // Tìm bản ghi hôm nay của nhân viên này
    const record = records.find(r => String(r.nhanVienId) === String(adminSession.value.id));
    todayRecord.value = record || null;

    // Xây dựng timeline logs hôm nay
    const logs = [];
    if (record) {
      if (record.thoiGianVao) {
        logs.push({
          type: "in",
          label: "Chấm công vào",
          time: formatTimeOnly(record.thoiGianVao),
          status: record.trangThaiVao === "DI_TRE" ? "Đi trễ" : "Đúng giờ",
          statusColor: record.trangThaiVao === "DI_TRE" ? "text-amber-500 bg-amber-50" : "text-emerald-500 bg-emerald-50"
        });
      }
      if (record.thoiGianRa) {
        logs.push({
          type: "out",
          label: "Chấm công ra",
          time: formatTimeOnly(record.thoiGianRa),
          status: record.trangThaiRa === "QUEN_CHECKOUT" ? "Quên checkout" : record.trangThaiRa === "VE_SOM" ? "Về sớm" : "Đúng giờ",
          statusColor: record.trangThaiRa === "QUEN_CHECKOUT" ? "text-rose-500 bg-rose-50" : record.trangThaiRa === "VE_SOM" ? "text-rose-500 bg-rose-50" : "text-emerald-500 bg-emerald-50"
        });
      }
    }
    todayLogs.value = logs;
  } catch (error) {
    console.error("Error loading employee shift and logs:", error);
  }
}

// Computed thông tin ca hiện tại
const caHienTaiInfo = computed(() => {
  if (!todayRecord.value) {
    return {
      name: "Không có ca hôm nay",
      timeRange: "Hôm nay bạn không có lịch làm việc",
      status: "Nghỉ",
      statusClass: "bg-slate-100 text-slate-500",
      iconClass: "bg-slate-100 text-slate-400"
    };
  }
  
  const ca = todayRecord.value.ca;
  let name = "Ca làm việc";
  let timeRange = "";
  if (ca === "sang") {
    name = "Ca sáng";
    timeRange = "08:00 - 12:00";
  } else if (ca === "chieu") {
    name = "Ca chiều";
    timeRange = "13:00 - 17:00";
  } else if (ca === "toi") {
    name = "Ca tối";
    timeRange = "18:00 - 22:00";
  } else {
    name = `Ca ${ca}`;
    timeRange = "—";
  }

  let status = "Chưa check-in";
  let statusClass = "bg-amber-100 text-amber-700";
  let iconClass = "bg-amber-100/80 text-amber-500";

  if (todayRecord.value.thoiGianRa) {
    status = todayRecord.value.trangThaiRa === 'QUEN_CHECKOUT' ? 'Thiếu check-out' : 'Đã kết thúc ca';
    statusClass = todayRecord.value.trangThaiRa === 'QUEN_CHECKOUT' ? 'bg-rose-100 text-rose-700' : 'bg-slate-100 text-slate-600';
    iconClass = todayRecord.value.trangThaiRa === 'QUEN_CHECKOUT' ? 'bg-rose-50 text-rose-400' : 'bg-slate-100 text-slate-400';
  } else if (todayRecord.value.thoiGianVao) {
    status = "Đang trong ca";
    statusClass = "bg-emerald-100 text-emerald-600";
    iconClass = "bg-emerald-100/80 text-emerald-500";
  } else if (currentGateStatus.value === 'VUNG_DO') {
    status = "Đã khóa ca";
    statusClass = "bg-rose-100 text-rose-700";
    iconClass = "bg-rose-50 text-rose-400";
  }

  return {
    name,
    timeRange: `${timeRange} • Cửa hàng`,
    status,
    statusClass,
    iconClass
  };
});

// Computed cấu hình nút hành động
const nutHanhDongInfo = computed(() => {
  if (!todayRecord.value) {
    return {
      text: "Không có ca làm việc hôm nay",
      disabled: true,
      class: "bg-slate-300 cursor-not-allowed text-slate-500 shadow-none hover:bg-slate-300"
    };
  }

  if (todayRecord.value.thoiGianRa) {
    const isQuen = todayRecord.value.trangThaiRa === 'QUEN_CHECKOUT';
    return {
      text: isQuen ? "Hệ thống đã tự đóng ca (quên checkout)" : "Đã hoàn thành ca làm hôm nay",
      disabled: true,
      class: "bg-slate-200 cursor-not-allowed text-slate-500 shadow-none hover:bg-slate-200"
    };
  }

  if (todayRecord.value.thoiGianVao) {
    return {
      text: "Chấm công ra (Kết thúc ca)",
      disabled: false,
      class: "bg-orange-600 hover:bg-orange-700 text-white shadow-[0_8px_20px_rgba(234,88,12,0.25)]"
    };
  }

  // 4 time gates check
  const gate = currentGateStatus.value;
  if (gate === 'VUNG_SOM') {
    const openingTimeStr = caTimeLimits.value.opening.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    return {
      text: `Chưa tới giờ điểm danh (Mở lúc ${openingTimeStr})`,
      disabled: false,
      class: "bg-slate-100 text-slate-400 cursor-pointer shadow-none hover:bg-slate-200"
    };
  }

  if (gate === 'VUNG_XANH') {
    return {
      text: "Bắt đầu chấm công (Đúng giờ)",
      disabled: false,
      class: "bg-emerald-500 hover:bg-emerald-600 text-white shadow-[0_8px_20px_rgba(16,185,129,0.25)]"
    };
  }

  if (gate === 'VUNG_CAM') {
    return {
      text: "Bắt đầu chấm công (Đi trễ)",
      disabled: false,
      class: "bg-amber-500 hover:bg-amber-600 text-white shadow-[0_8px_20px_rgba(245,158,11,0.25)]"
    };
  }

  return {
    text: "ĐÃ KHÓA CA (Quá 50% thời gian)",
    disabled: true,
    class: "bg-rose-200 text-rose-500 cursor-not-allowed shadow-none"
  };
});

async function handleCheckInClick() {
  if (!adminSession.value?.id) return;
  
  if (currentGateStatus.value === 'VUNG_SOM' && !todayRecord.value?.thoiGianVao) {
    const friendlyCa = getFriendlyCaName(todayRecord.value?.ca || 'sang');
    const openingTimeStr = caTimeLimits.value.opening.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    showError(`Chưa tới giờ điểm danh. Cổng Check-in ca ${friendlyCa} mở lúc ${openingTimeStr}`);
    return;
  }
  
  dangXuLy.value = true;
  try {
    const nv = await layChiTietNhanVien(adminSession.value.id);
    if (!nv || !nv.faceDescriptor) {
      showError("Bạn chưa đăng ký khuôn mặt. Vui lòng liên hệ Admin để đăng ký Face ID trước khi Check-in.");
      return;
    }
    faceDescriptorString.value = nv.faceDescriptor;
    showFaceIdModal.value = true;
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể kiểm tra thông tin khuôn mặt"));
  } finally {
    dangXuLy.value = false;
  }
}

// Gọi API chấm công thực tế (Vào / Ra)
async function thucHienCheckIn() {
  if (dangXuLy.value) return;
  showFaceIdModal.value = false;
  if (!adminSession.value?.id) return;
  dangXuLy.value = true;
  try {
    const isCheckout = todayRecord.value && todayRecord.value.thoiGianVao && !todayRecord.value.thoiGianRa;
    
    if (isCheckout) {
      await checkOut({ nhanVienId: adminSession.value.id });
    } else {
      await checkIn({ nhanVienId: adminSession.value.id });
    }
    
    // Populate details for congratulation popup
    const now = new Date(Date.now() + clockOffset.value);
    const timeStr = formatTime(now);
    
    let noteText = "Đúng giờ";
    if (!isCheckout) {
      const gate = currentGateStatus.value;
      if (gate === 'VUNG_CAM' && caTimeLimits.value) {
        const diffMs = now.getTime() - caTimeLimits.value.start.getTime();
        const diffMins = Math.ceil(diffMs / (60 * 1000));
        noteText = `Đi trễ ${diffMins} phút`;
      }
    } else {
      if (caTimeLimits.value && now.getTime() < caTimeLimits.value.end.getTime() - 5 * 60 * 1000) {
        const diffMs = caTimeLimits.value.end.getTime() - now.getTime();
        const diffMins = Math.ceil(diffMs / (60 * 1000));
        noteText = `Về sớm ${diffMins} phút`;
      }
    }
    
    successPopupDetails.value = {
      type: isCheckout ? 'checkout' : 'checkin',
      employeeName: adminSession.value.hoTen || 'Nhân viên',
      caName: getFriendlyCaName(todayRecord.value?.ca || 'sang'),
      gioGhiNhan: timeStr,
      message: isCheckout ? 'Cảm ơn bạn đã hoàn thành ca làm việc. Hẹn gặp lại bạn vào ca làm tiếp theo!' : 'Chúc bạn một ca làm việc nhiều năng lượng và năng suất!',
      note: noteText
    };
    
    showSuccessPopup.value = true;
    
    await taiThongTinChamCong();
  } catch (error) {
    showError(getDisplayErrorMessage(error, 'Không thể chấm công'));
  } finally {
    dangXuLy.value = false;
  }
}

// Xem lịch sử 30 ngày
async function taiLichSuToanBo() {
  if (!adminSession.value?.id) return;
  dangTaiHistory.value = true;
  showHistoryModal.value = true;
  try {
    const denNgayStr = new Date().toLocaleDateString("en-CA");
    const tuNgay = new Date();
    tuNgay.setDate(tuNgay.getDate() - 30);
    const tuNgayStr = tuNgay.toLocaleDateString("en-CA");

    const ds = await layChamCong({ tuNgay: tuNgayStr, denNgay: denNgayStr });
    const records = Array.isArray(ds) ? ds : [];
    
    allHistory.value = records
      .filter(r => String(r.nhanVienId) === String(adminSession.value.id))
      .map(r => {
        const formatGio = (iso) => {
          if (!iso) return undefined;
          const d = new Date(iso);
          return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
        };

        return {
          id: r.id,
          ngay: r.ngay,
          ca: r.ca,
          gioVao: formatGio(r.thoiGianVao),
          gioRa: formatGio(r.thoiGianRa),
          trangThaiVao: r.trangThaiVao,
          trangThaiRa: r.trangThaiRa
        };
      });
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể tải lịch sử chấm công"));
  } finally {
    dangTaiHistory.value = false;
  }
}
</script>

<style scoped>
@keyframes scan {
  0% { top: 0%; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}
</style>
