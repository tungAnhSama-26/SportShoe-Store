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
            <div class="flex items-center justify-center w-10 h-10 rounded-full bg-emerald-100/80 text-emerald-500">
              <Sun class="h-5 w-5" />
            </div>
            <div>
              <p class="font-bold text-emerald-700 text-sm">Ca hiện tại: Ca sáng</p>
              <p class="text-[11px] font-medium text-emerald-600/80 mt-0.5">08:00 - 12:00 • Văn phòng Hà Nội</p>
            </div>
          </div>
          <span class="px-2.5 py-1 text-[10px] font-bold rounded-full bg-emerald-100 text-emerald-600">
            Đang trong ca
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
              <span>Văn phòng Hà Nội</span>
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
            <!-- Timeline Item -->
            <div class="flex items-start gap-4">
              <div class="flex items-center justify-center w-8 h-8 rounded-full bg-emerald-50 text-emerald-500 shrink-0 mt-0.5">
                <ArrowDown class="h-4 w-4" />
              </div>
              <div class="flex-1 flex justify-between border-b border-slate-50 pb-4">
                <div>
                  <p class="text-sm font-bold text-slate-700">Chấm công vào</p>
                  <p class="text-xs text-slate-500 mt-0.5">07:58</p>
                </div>
                <p class="text-[11px] font-medium text-slate-400 mt-0.5">{{ formatDateShort(currentTime) }}</p>
              </div>
            </div>
            
            <!-- Timeline Item -->
            <div class="flex items-start gap-4">
              <div class="flex items-center justify-center w-8 h-8 rounded-full bg-orange-50 text-orange-500 shrink-0 mt-0.5">
                <ArrowUp class="h-4 w-4" />
              </div>
              <div class="flex-1 flex justify-between border-b border-slate-50 pb-4">
                <div>
                  <p class="text-sm font-bold text-slate-700">Chấm công ra</p>
                  <p class="text-xs text-slate-500 mt-0.5">12:03</p>
                </div>
                <p class="text-[11px] font-medium text-slate-400 mt-0.5">{{ formatDateShort(currentTime) }}</p>
              </div>
            </div>

            <!-- Timeline Item -->
            <div class="flex items-start gap-4">
              <div class="flex items-center justify-center w-8 h-8 rounded-full bg-emerald-50 text-emerald-500 shrink-0 mt-0.5">
                <ArrowDown class="h-4 w-4" />
              </div>
              <div class="flex-1 flex justify-between pb-1">
                <div>
                  <p class="text-sm font-bold text-slate-700">Chấm công vào</p>
                  <p class="text-xs text-slate-500 mt-0.5">13:01</p>
                </div>
                <p class="text-[11px] font-medium text-slate-400 mt-0.5">{{ formatDateShort(currentTime) }}</p>
              </div>
            </div>
          </div>
          
          <button class="w-full mt-4 py-3 rounded-2xl border border-slate-100 text-xs font-bold text-slate-600 hover:bg-slate-50 transition flex items-center justify-center gap-1">
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
            <button class="flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-200 text-sm font-semibold text-slate-600 hover:bg-slate-50 transition">
              <Lightbulb class="h-4 w-4" /> Bật đèn
            </button>
          </div>

          <!-- Camera Viewfinder -->
          <div class="relative w-full flex-1 min-h-[400px] rounded-3xl overflow-hidden bg-slate-100 flex items-center justify-center">
            <!-- Mock Portrait Image -->
            <img 
              src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=800&auto=format&fit=crop" 
              alt="Camera Feed Placeholder" 
              class="absolute inset-0 w-full h-full object-cover grayscale-[20%]"
            />
            
            <!-- Scanning Overlay -->
            <div class="absolute inset-0 bg-white/10"></div>
            
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
          <button @click="xuLyChamCong" :disabled="dangXuLy" class="w-full py-4 rounded-2xl bg-[#00A859] hover:bg-[#00914c] transition text-white font-bold text-lg flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed shadow-[0_8px_20px_rgba(0,168,89,0.25)]">
            <Camera v-if="!dangXuLy" class="h-6 w-6" />
            <div v-else class="w-6 h-6 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
            {{ dangXuLy ? 'Đang xử lý...' : 'Bắt đầu chấm công' }}
          </button>

          <!-- Privacy Note -->
          <div class="flex items-center justify-center gap-1.5 mt-4 text-[11px] font-semibold text-slate-400">
            <ShieldCheck class="h-4 w-4" />
            <span>Thông tin khuôn mặt của bạn được mã hóa và bảo mật tuyệt đối.</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { 
  Sun, MapPin, ArrowDown, ArrowUp, ChevronRight, 
  Lightbulb, Scan, User, Smile, Camera, ShieldCheck 
} from 'lucide-vue-next';
import { showSuccess, showError } from '../../../utils/alert';
import { checkIn } from '../../../services/cham-cong';
import { useAdminSession } from '../../../composable/useAdminSession';
import { getDisplayErrorMessage } from '../../../utils/error-message';

const { adminSession } = useAdminSession();

const currentTime = ref(new Date());
let timer = null;

const dangXuLy = ref(false);

onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
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

// Gọi API chấm công thực tế
async function xuLyChamCong() {
  if (!adminSession.value?.id) return;
  dangXuLy.value = true;
  try {
    await checkIn({ nhanVienId: adminSession.value.id });
    showSuccess('Chấm công thành công! Bạn đã được ghi nhận.');
  } catch (error) {
    showError(getDisplayErrorMessage(error, 'Không thể chấm công'));
  } finally {
    dangXuLy.value = false;
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
