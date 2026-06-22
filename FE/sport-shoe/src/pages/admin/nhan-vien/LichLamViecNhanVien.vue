<template>
  <div class="w-full bg-white rounded-3xl shadow-sm min-h-[calc(100vh-80px)] p-8">
    <!-- Header -->
    <div class="flex items-center justify-between mb-10 border-b border-slate-100 pb-5">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">Lịch làm việc cá nhân</h1>
        <p class="text-sm text-slate-500 mt-1">Theo dõi ca làm và lịch sử chấm công của bạn</p>
      </div>
      <div class="flex items-center gap-4">
        <button @click="veHienTai" class="text-sm font-semibold text-primary hover:text-primary-hover px-4 py-2 bg-primary/10 rounded-xl transition">
          Hôm nay
        </button>
        <button class="relative text-slate-600 hover:text-slate-800 transition p-2 bg-slate-50 rounded-full">
          <Bell class="h-5 w-5" />
          <span class="absolute top-1 right-1 block h-2 w-2 rounded-full bg-red-500 ring-2 ring-white"></span>
        </button>
      </div>
    </div>

    <!-- Date selector & Week navigation -->
    <div class="mb-10">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-3">
          <button @click="tuanTruoc" class="p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-full transition">
            <ChevronLeft class="h-5 w-5" />
          </button>
          <div class="flex items-center gap-2 text-slate-700">
            <Calendar class="h-5 w-5 text-slate-400" />
            <span class="font-bold text-lg">{{ formatNgayHienThi(cacNgayTrongTuan[chonNgayIdx]) }}</span>
          </div>
          <button @click="tuanSau" class="p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-full transition">
            <ChevronRight class="h-5 w-5" />
          </button>
        </div>
      </div>

      <div class="grid grid-cols-7 gap-4">
        <button 
          v-for="(ngay, idx) in cacNgayTrongTuan" 
          :key="idx"
          @click="chonNgayIdx = idx"
          class="flex flex-col items-center justify-center py-4 rounded-2xl transition border-2 cursor-pointer"
          :class="chonNgayIdx === idx ? 'bg-[#CC0000] border-[#CC0000] text-white shadow-lg shadow-red-500/20' : 'border-slate-100 text-slate-500 hover:border-slate-300 hover:bg-slate-50'"
        >
          <span class="text-sm font-semibold mb-1 uppercase tracking-wide">{{ NHAN_TUAN[idx] }}</span>
          <span class="text-2xl font-bold">{{ ngay.getDate() }}</span>
          <span class="text-[10px] mt-1 opacity-70" v-if="coCaVaoNgay(idx)">Có ca</span>
        </button>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
      <!-- Shift Cards (Left side) -->
      <div class="lg:col-span-2 space-y-4">
        <h2 class="text-lg font-bold text-slate-800 mb-4">Ca làm việc trong ngày</h2>
        
      <!-- Ca Sáng -->
      <div 
        v-if="coCa('sang')"
        class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-[20px] border border-emerald-100 bg-emerald-50/50 hover:bg-emerald-50 transition"
      >
        <div class="flex items-center gap-4">
          <div class="flex items-center justify-center shrink-0 w-12 h-12 rounded-full bg-emerald-100/80 text-emerald-500">
            <Sun class="h-6 w-6" />
          </div>
          <div>
            <h3 class="font-bold text-slate-800 text-base">Ca sáng</h3>
            <p class="text-sm font-semibold text-emerald-600 mb-1">08:00 - 12:00</p>
            <div class="flex items-center gap-1 text-xs text-slate-500">
              <MapPin class="h-3 w-3" />
              <span>Văn phòng Hà Nội</span>
            </div>
            <div class="mt-2 text-xs font-medium" v-if="getChamCongData('sang')?.thoiGianVao || getChamCongData('sang')?.thoiGianRa">
              <span v-if="getChamCongData('sang')?.thoiGianVao" class="text-emerald-600 mr-2">Vào: {{ formatTime(getChamCongData('sang').thoiGianVao) }}</span>
              <span v-if="getChamCongData('sang')?.thoiGianRa" class="text-emerald-600">Ra: {{ formatTime(getChamCongData('sang').thoiGianRa) }}</span>
            </div>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <template v-if="laNgayHomNay">
            <!-- If not checked in -->
            <template v-if="trangThaiChamCong('sang') === 'Chưa Check-in'">
              <!-- Mốc 4 (Đã khóa ca) -->
              <span v-if="getCaGateStatus('sang').status === 'VUNG_DO'" class="px-3 py-1.5 text-xs font-bold rounded-full bg-rose-100 text-rose-600">
                Đã khóa ca
              </span>
              <!-- Mốc 1, 2, 3 -->
              <template v-else>
                <button 
                  @click="getCaGateStatus('sang').status === 'VUNG_SOM' ? showError('Chưa tới giờ điểm danh. Cổng Check-in ca sáng mở lúc ' + getCaGateStatus('sang').openingTimeStr) : handleCheckInClick()"
                  :disabled="dangXuLy"
                  class="px-4 py-2 text-sm font-bold text-white rounded-xl shadow-sm transition disabled:opacity-50"
                  :class="getCaGateStatus('sang').status === 'VUNG_SOM' ? 'bg-slate-300 hover:bg-slate-300 cursor-not-allowed' : (getCaGateStatus('sang').status === 'VUNG_XANH' ? 'bg-emerald-500 hover:bg-emerald-600' : 'bg-amber-500 hover:bg-amber-600')"
                >
                  Check-in
                </button>
                
                <!-- Nút Cầu Cứu next to Check-in button -->
                <div class="relative inline-block text-left">
                  <button 
                    @click.stop="toggleDropdown('sang')"
                    class="px-3 py-2 text-sm font-semibold bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl transition flex items-center justify-center gap-1"
                  >
                    <span>Xin phép</span>
                    <ChevronDown class="h-3.5 w-3.5" />
                  </button>
                  
                  <!-- Dropdown Menu -->
                  <div 
                    v-if="activeDropdown === 'sang'"
                    class="absolute right-0 mt-2 w-48 rounded-2xl bg-white border border-slate-100 shadow-xl z-50 py-2"
                  >
                    <button @click="handleXinPhep('den_muon', 'sang')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition flex items-center gap-2">
                      <Clock class="h-4 w-4 text-amber-500" />
                      Xin đến muộn
                    </button>
                    <button @click="handleXinPhep('da_ca', 'sang')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition flex items-center gap-2">
                      <UserPlus class="h-4 w-4 text-sky-500" />
                      Xin nhờ đá ca
                    </button>
                    <button @click="handleXinPhep('nghi_phep', 'sang')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-rose-50 hover:text-rose-600 transition flex items-center gap-2">
                      <CalendarX class="h-4 w-4 text-rose-500" />
                      Xin nghỉ phép
                    </button>
                  </div>
                </div>
              </template>
            </template>

            <!-- If already checked in -->
            <button 
              v-else-if="trangThaiChamCong('sang') === 'Đã check-in'"
              @click="thucHienCheckOut"
              :disabled="dangXuLy"
              class="px-4 py-2 text-sm font-bold bg-orange-500 hover:bg-orange-600 text-white rounded-xl shadow-sm transition disabled:opacity-50"
            >
              Check-out
            </button>
            <span v-else class="px-3 py-1.5 text-xs font-bold rounded-full bg-emerald-100 text-emerald-600">
              Đã check-out
            </span>
          </template>
          <template v-else>
            <span class="px-3 py-1.5 text-xs font-bold rounded-full bg-slate-100 text-slate-600">
              {{ trangThaiChamCong('sang') }}
            </span>
          </template>
        </div>
      </div>

      <!-- Ca Chiều -->
      <div 
        v-if="coCa('chieu')"
        class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-[20px] border border-orange-100 bg-orange-50/50 hover:bg-orange-50 transition"
      >
        <div class="flex items-center gap-4">
          <div class="flex items-center justify-center shrink-0 w-12 h-12 rounded-full bg-orange-100 text-orange-500">
            <Sunset class="h-6 w-6" />
          </div>
          <div>
            <h3 class="font-bold text-orange-600 text-base">Ca chiều</h3>
            <p class="text-sm font-semibold text-orange-500 mb-1">12:00 - 17:00</p>
            <div class="flex items-center gap-1 text-xs text-slate-500">
              <MapPin class="h-3 w-3" />
              <span>Văn phòng Hà Nội</span>
            </div>
            <div class="mt-2 text-xs font-medium" v-if="getChamCongData('chieu')?.thoiGianVao || getChamCongData('chieu')?.thoiGianRa">
              <span v-if="getChamCongData('chieu')?.thoiGianVao" class="text-orange-600 mr-2">Vào: {{ formatTime(getChamCongData('chieu').thoiGianVao) }}</span>
              <span v-if="getChamCongData('chieu')?.thoiGianRa" class="text-orange-600">Ra: {{ formatTime(getChamCongData('chieu').thoiGianRa) }}</span>
            </div>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <template v-if="laNgayHomNay">
            <!-- If not checked in -->
            <template v-if="trangThaiChamCong('chieu') === 'Chưa Check-in'">
              <!-- Mốc 4 (Đã khóa ca) -->
              <span v-if="getCaGateStatus('chieu').status === 'VUNG_DO'" class="px-3 py-1.5 text-xs font-bold rounded-full bg-rose-100 text-rose-600">
                Đã khóa ca
              </span>
              <!-- Mốc 1, 2, 3 -->
              <template v-else>
                <button 
                  @click="getCaGateStatus('chieu').status === 'VUNG_SOM' ? showError('Chưa tới giờ điểm danh. Cổng Check-in ca chiều mở lúc ' + getCaGateStatus('chieu').openingTimeStr) : handleCheckInClick()"
                  :disabled="dangXuLy"
                  class="px-4 py-2 text-sm font-bold text-white rounded-xl shadow-sm transition disabled:opacity-50"
                  :class="getCaGateStatus('chieu').status === 'VUNG_SOM' ? 'bg-slate-300 hover:bg-slate-300 cursor-not-allowed' : (getCaGateStatus('chieu').status === 'VUNG_XANH' ? 'bg-emerald-500 hover:bg-emerald-600' : 'bg-amber-500 hover:bg-amber-600')"
                >
                  Check-in
                </button>
                
                <!-- Nút Cầu Cứu next to Check-in button -->
                <div class="relative inline-block text-left">
                  <button 
                    @click.stop="toggleDropdown('chieu')"
                    class="px-3 py-2 text-sm font-semibold bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl transition flex items-center justify-center gap-1"
                  >
                    <span>Xin phép</span>
                    <ChevronDown class="h-3.5 w-3.5" />
                  </button>
                  
                  <!-- Dropdown Menu -->
                  <div 
                    v-if="activeDropdown === 'chieu'"
                    class="absolute right-0 mt-2 w-48 rounded-2xl bg-white border border-slate-100 shadow-xl z-50 py-2"
                  >
                    <button @click="handleXinPhep('den_muon', 'chieu')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition flex items-center gap-2">
                      <Clock class="h-4 w-4 text-amber-500" />
                      Xin đến muộn
                    </button>
                    <button @click="handleXinPhep('da_ca', 'chieu')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition flex items-center gap-2">
                      <UserPlus class="h-4 w-4 text-sky-500" />
                      Xin nhờ đá ca
                    </button>
                    <button @click="handleXinPhep('nghi_phep', 'chieu')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-rose-50 hover:text-rose-600 transition flex items-center gap-2">
                      <CalendarX class="h-4 w-4 text-rose-500" />
                      Xin nghỉ phép
                    </button>
                  </div>
                </div>
              </template>
            </template>

            <!-- If already checked in -->
            <button 
              v-else-if="trangThaiChamCong('chieu') === 'Đã check-in'"
              @click="thucHienCheckOut"
              :disabled="dangXuLy"
              class="px-4 py-2 text-sm font-bold bg-orange-500 hover:bg-orange-600 text-white rounded-xl shadow-sm transition disabled:opacity-50"
            >
              Check-out
            </button>
            <span v-else class="px-3 py-1.5 text-xs font-bold rounded-full bg-emerald-100 text-emerald-600">
              Đã check-out
            </span>
          </template>
          <template v-else>
            <span class="px-3 py-1.5 text-xs font-bold rounded-full bg-slate-100 text-slate-600">
              {{ trangThaiChamCong('chieu') }}
            </span>
          </template>
        </div>
      </div>

      <!-- Ca Tối -->
      <div 
        v-if="coCa('toi')"
        class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-[20px] border border-purple-100 bg-purple-50/50 hover:bg-purple-50 transition"
      >
        <div class="flex items-center gap-4">
          <div class="flex items-center justify-center shrink-0 w-12 h-12 rounded-full bg-purple-100 text-purple-600">
            <Moon class="h-6 w-6" />
          </div>
          <div>
            <h3 class="font-bold text-purple-700 text-base">Ca tối</h3>
            <p class="text-sm font-semibold text-purple-600 mb-1">17:00 - 22:00</p>
            <div class="flex items-center gap-1 text-xs text-slate-500">
              <MapPin class="h-3 w-3" />
              <span>Văn phòng Hà Nội</span>
            </div>
            <div class="mt-2 text-xs font-medium" v-if="getChamCongData('toi')?.thoiGianVao || getChamCongData('toi')?.thoiGianRa">
              <span v-if="getChamCongData('toi')?.thoiGianVao" class="text-purple-600 mr-2">Vào: {{ formatTime(getChamCongData('toi').thoiGianVao) }}</span>
              <span v-if="getChamCongData('toi')?.thoiGianRa" class="text-purple-600">Ra: {{ formatTime(getChamCongData('toi').thoiGianRa) }}</span>
            </div>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <template v-if="laNgayHomNay">
            <!-- If not checked in -->
            <template v-if="trangThaiChamCong('toi') === 'Chưa Check-in'">
              <!-- Mốc 4 (Đã khóa ca) -->
              <span v-if="getCaGateStatus('toi').status === 'VUNG_DO'" class="px-3 py-1.5 text-xs font-bold rounded-full bg-rose-100 text-rose-600">
                Đã khóa ca
              </span>
              <!-- Mốc 1, 2, 3 -->
              <template v-else>
                <button 
                  @click="getCaGateStatus('toi').status === 'VUNG_SOM' ? showError('Chưa tới giờ điểm danh. Cổng Check-in ca tối mở lúc ' + getCaGateStatus('toi').openingTimeStr) : handleCheckInClick()"
                  :disabled="dangXuLy"
                  class="px-4 py-2 text-sm font-bold text-white rounded-xl shadow-sm transition disabled:opacity-50"
                  :class="getCaGateStatus('toi').status === 'VUNG_SOM' ? 'bg-slate-300 hover:bg-slate-300 cursor-not-allowed' : (getCaGateStatus('toi').status === 'VUNG_XANH' ? 'bg-emerald-500 hover:bg-emerald-600' : 'bg-amber-500 hover:bg-amber-600')"
                >
                  Check-in
                </button>
                
                <!-- Nút Cầu Cứu next to Check-in button -->
                <div class="relative inline-block text-left">
                  <button 
                    @click.stop="toggleDropdown('toi')"
                    class="px-3 py-2 text-sm font-semibold bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl transition flex items-center justify-center gap-1"
                  >
                    <span>Xin phép</span>
                    <ChevronDown class="h-3.5 w-3.5" />
                  </button>
                  
                  <!-- Dropdown Menu -->
                  <div 
                    v-if="activeDropdown === 'toi'"
                    class="absolute right-0 mt-2 w-48 rounded-2xl bg-white border border-slate-100 shadow-xl z-50 py-2"
                  >
                    <button @click="handleXinPhep('den_muon', 'toi')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition flex items-center gap-2">
                      <Clock class="h-4 w-4 text-amber-500" />
                      Xin đến muộn
                    </button>
                    <button @click="handleXinPhep('da_ca', 'toi')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition flex items-center gap-2">
                      <UserPlus class="h-4 w-4 text-sky-500" />
                      Xin nhờ đá ca
                    </button>
                    <button @click="handleXinPhep('nghi_phep', 'toi')" class="w-full text-left px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-rose-50 hover:text-rose-600 transition flex items-center gap-2">
                      <CalendarX class="h-4 w-4 text-rose-500" />
                      Xin nghỉ phép
                    </button>
                  </div>
                </div>
              </template>
            </template>

            <!-- If already checked in -->
            <button 
              v-else-if="trangThaiChamCong('toi') === 'Đã check-in'"
              @click="thucHienCheckOut"
              :disabled="dangXuLy"
              class="px-4 py-2 text-sm font-bold bg-orange-500 hover:bg-orange-600 text-white rounded-xl shadow-sm transition disabled:opacity-50"
            >
              Check-out
            </button>
            <span v-else class="px-3 py-1.5 text-xs font-bold rounded-full bg-emerald-100 text-emerald-600">
              Đã check-out
            </span>
          </template>
          <template v-else>
            <span class="px-3 py-1.5 text-xs font-bold rounded-full bg-slate-100 text-slate-600">
              {{ trangThaiChamCong('toi') }}
            </span>
          </template>
        </div>
      </div>

      <!-- Trạng thái không có ca làm -->
      <div v-if="!coCa('sang') && !coCa('chieu') && !coCa('toi') && !dangTai" class="py-10 text-center flex flex-col items-center">
        <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mb-3">
          <CalendarDays class="h-8 w-8 text-slate-300" />
        </div>
        <p class="text-slate-500 font-medium">Bạn không có ca làm việc nào trong ngày này.</p>
      </div>
      
      <!-- Loading state -->
      <div v-if="dangTai" class="py-12 text-center flex justify-center">
        <div class="w-10 h-10 border-4 border-slate-200 border-t-[#CC0000] rounded-full animate-spin"></div>
      </div>
      </div>

      <!-- Right Side (Summary & Notes) -->
      <div class="space-y-6">
        <!-- Summary -->
        <div>
          <div class="rounded-3xl border border-slate-200 p-6 shadow-sm bg-white">
            <h3 class="font-bold text-slate-800 text-lg mb-6">Tổng quan ngày</h3>
            <div class="flex flex-col gap-6">
               <div class="flex justify-between items-center border-b border-slate-100 pb-4">
                 <p class="text-sm font-medium text-slate-500">Tổng số ca hôm nay</p>
                 <p class="text-xl font-bold text-slate-800">{{ String(tongCaHomNay).padStart(2, '0') }} ca</p>
               </div>
               <div class="flex justify-between items-center border-b border-slate-100 pb-4">
                 <p class="text-sm font-medium text-slate-500">Giờ làm việc dự kiến</p>
                 <p class="text-xl font-bold text-slate-800">{{ tongGioLamHomNay }}h</p>
               </div>
               <div class="flex flex-col gap-2 pb-2">
                 <div class="flex justify-between items-center">
                   <p class="text-sm font-medium text-slate-500">Định mức ca</p>
                   <p class="text-base font-bold text-slate-700">4.0 giờ</p>
                 </div>
                 <!-- Progress Bar -->
                 <div class="w-full bg-slate-100 rounded-full h-2 mt-1">
                   <div 
                     class="bg-emerald-500 h-2 rounded-full transition-all duration-500" 
                     :style="{ width: `${thongTinTienTrinhCa.phanTram}%` }"
                   ></div>
                 </div>
                 <div class="flex justify-between items-center text-xs text-slate-400 font-semibold mt-1">
                   <span>{{ thongTinTienTrinhCa.label }}</span>
                   <span>{{ thongTinTienTrinhCa.phanTram }}%</span>
                 </div>
                </div>
            </div>
          </div>
        </div>

        <!-- Notes -->
        <div>
          <h3 class="font-bold text-slate-800 text-lg mb-4">Ghi chú & Quy định</h3>
          <div class="rounded-2xl bg-rose-50 border border-rose-100 p-5 flex items-start gap-4">
            <div class="bg-white p-2 rounded-full shadow-sm">
              <Megaphone class="h-6 w-6 text-[#CC0000] shrink-0" />
            </div>
            <p class="text-sm font-medium text-rose-800 leading-relaxed pt-1">
              Hãy tan ca đúng giờ quy định. Nhớ dọn dẹp vệ sinh khu vực làm việc và kiểm tra lại thiết bị điện trước khi ra về.
            </p>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { 
  CalendarDays, 
  Bell, 
  Calendar, 
  Sun, 
  Sunset, 
  Moon, 
  MapPin, 
  ChevronRight,
  ChevronLeft, 
  Megaphone,
  ChevronDown,
  Clock,
  UserPlus,
  CalendarX
} from 'lucide-vue-next';
import Swal from 'sweetalert2';
import { useAdminSession } from '../../../composable/useAdminSession.js';
import { layLichLamViec } from '../../../services/lich-lam.js';
import { layChamCong, layServerTime, checkIn, checkOut } from '../../../services/cham-cong.js';
import { layChiTietNhanVien } from '../../../services/nhan-vien.js';
import { getDisplayErrorMessage } from '../../../utils/error-message.js';
import { showSuccess, showError } from '../../../utils/alert.js';
import FaceIdCheckInModal from './components/FaceIdCheckInModal.vue';

const NHAN_TUAN = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

const { adminSession } = useAdminSession();
const userId = computed(() => adminSession.value.id);

const ngayHienTai = ref(new Date());
const cacNgayTrongTuan = ref([]);
const chonNgayIdx = ref(0);
const dangTai = ref(false);

const lichLamViecTuan = ref([]);
const chamCongTuan = ref([]);

const currentTime = ref(new Date());
const clockOffset = ref(0);
let timer = null;

const activeDropdown = ref(null);

function toggleDropdown(caName) {
  activeDropdown.value = activeDropdown.value === caName ? null : caName;
}

function tinhCacNgayTrongTuan(ngay) {
  const d = new Date(ngay);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  const t2 = new Date(d.setDate(diff));
  
  const ds = [];
  for (let i = 0; i < 7; i++) {
    const curr = new Date(t2);
    curr.setDate(t2.getDate() + i);
    ds.push(curr);
  }
  return ds;
}

function initDate(date = new Date()) {
  ngayHienTai.value = date;
  cacNgayTrongTuan.value = tinhCacNgayTrongTuan(date);
  
  const todayStr = formatISODate(new Date());
  const idx = cacNgayTrongTuan.value.findIndex(d => formatISODate(d) === todayStr);
  if (idx >= 0) {
    chonNgayIdx.value = idx;
  } else {
    chonNgayIdx.value = 0;
  }
}

async function tuanTruoc() {
  const t = new Date(cacNgayTrongTuan.value[0]);
  t.setDate(t.getDate() - 7);
  initDate(t);
  await taiDuLieu();
}

async function tuanSau() {
  const t = new Date(cacNgayTrongTuan.value[0]);
  t.setDate(t.getDate() + 7);
  initDate(t);
  await taiDuLieu();
}

function veHienTai() {
  initDate(new Date());
  taiDuLieu();
}

function formatISODate(d) {
  if (!d) return "";
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatNgayHienThi(d) {
  if (!d) return "";
  const dayOfWeek = d.getDay() === 0 ? "Chủ nhật" : `Thứ ${d.getDay() + 1}`;
  return `${dayOfWeek}, ${d.getDate()} tháng ${d.getMonth() + 1}, ${d.getFullYear()}`;
}

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

async function taiDuLieu() {
  if (!userId.value) return;
  dangTai.value = true;
  try {
    const tuNgay = formatISODate(cacNgayTrongTuan.value[0]);
    const denNgay = formatISODate(cacNgayTrongTuan.value[6]);
    
    const lichData = await layLichLamViec(tuNgay, denNgay);
    let chamCongData = [];
    try {
      chamCongData = await layChamCong({ tuNgay, denNgay, nhanVienId: userId.value });
    } catch (e) {
      console.error("Lỗi khi tải chấm công", e);
    }
    
    lichLamViecTuan.value = Array.isArray(lichData) ? lichData.filter(l => String(l.nhanVienId) === String(userId.value)) : [];
    chamCongTuan.value = Array.isArray(chamCongData) ? chamCongData : [];
    
  } catch (error) {
    console.error(error);
  } finally {
    dangTai.value = false;
  }
}

const clickOutsideHandler = (e) => {
  if (!e.target.closest('.relative')) {
    activeDropdown.value = null;
  }
};

onMounted(async () => {
  initDate();
  await syncServerTime();
  
  timer = setInterval(() => {
    currentTime.value = new Date(Date.now() + clockOffset.value);
  }, 1000);
  
  await taiDuLieu();
  window.addEventListener('click', clickOutsideHandler);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  window.removeEventListener('click', clickOutsideHandler);
});

// Helpers
const lichNgayDangChon = computed(() => {
  if (!cacNgayTrongTuan.value[chonNgayIdx.value]) return [];
  const selectedDateStr = formatISODate(cacNgayTrongTuan.value[chonNgayIdx.value]);
  return lichLamViecTuan.value.filter(l => l.ngay === selectedDateStr);
});

const laNgayHomNay = computed(() => {
  if (!cacNgayTrongTuan.value[chonNgayIdx.value]) return false;
  return formatISODate(cacNgayTrongTuan.value[chonNgayIdx.value]) === formatISODate(new Date());
});

function coCa(caNhan) {
  return lichNgayDangChon.value.some(l => l.ca === caNhan);
}

function coCaVaoNgay(ngayIdx) {
  if (!cacNgayTrongTuan.value[ngayIdx]) return false;
  const dateStr = formatISODate(cacNgayTrongTuan.value[ngayIdx]);
  return lichLamViecTuan.value.some(l => l.ngay === dateStr);
}

function getChamCongData(caNhan) {
  if (!cacNgayTrongTuan.value[chonNgayIdx.value]) return null;
  const ngayStr = formatISODate(cacNgayTrongTuan.value[chonNgayIdx.value]);
  return chamCongTuan.value.find(c => c.ngay === ngayStr && c.ca === caNhan);
}

function trangThaiChamCong(caNhan) {
  const c = getChamCongData(caNhan);
  if (!c || !c.thoiGianVao) return 'Chưa Check-in';
  if (c.thoiGianVao && !c.thoiGianRa) return 'Đã check-in';
  return 'Đã check-out';
}

function formatTime(isoString) {
  if (!isoString) return "";
  const d = new Date(isoString);
  return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

const dangXuLy = ref(false);
const showFaceIdModal = ref(false);
const faceDescriptorString = ref("");

function getCaGateStatus(caName) {
  const now = currentTime.value;
  const start = new Date(now);
  const end = new Date(now);
  
  if (caName === 'sang') {
    start.setHours(8, 0, 0, 0);
    end.setHours(12, 0, 0, 0);
  } else if (caName === 'chieu') {
    start.setHours(13, 0, 0, 0);
    end.setHours(17, 0, 0, 0);
  } else if (caName === 'toi') {
    start.setHours(18, 0, 0, 0);
    end.setHours(22, 0, 0, 0);
  } else {
    start.setHours(8, 0, 0, 0);
    end.setHours(12, 0, 0, 0);
  }
  
  const opening = new Date(start.getTime() - 30 * 60 * 1000);
  const startPlus5 = new Date(start.getTime() + 5 * 60 * 1000);
  const midpoint = new Date(start.getTime() + 2 * 60 * 60 * 1000);
  
  if (now < opening) return { status: 'VUNG_SOM', label: 'Chưa mở', openingTimeStr: opening.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) };
  if (now >= opening && now <= startPlus5) return { status: 'VUNG_XANH', label: 'Đúng giờ' };
  if (now > startPlus5 && now <= midpoint) return { status: 'VUNG_CAM', label: 'Đi trễ' };
  return { status: 'VUNG_DO', label: 'Đã khóa' };
}

async function handleCheckInClick() {
  if (!userId.value) return;
  dangXuLy.value = true;
  try {
    const nv = await layChiTietNhanVien(userId.value);
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

async function thucHienCheckIn() {
  if (dangXuLy.value) return;
  showFaceIdModal.value = false;
  if (!userId.value) return;
  dangXuLy.value = true;
  try {
    await checkIn({ nhanVienId: userId.value });
    showSuccess("Check-in thành công!");
    await taiDuLieu();
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Check-in thất bại"));
  } finally {
    dangXuLy.value = false;
  }
}

async function thucHienCheckOut() {
  if (dangXuLy.value) return;
  if (!userId.value) return;
  dangXuLy.value = true;
  try {
    await checkOut({ nhanVienId: userId.value });
    showSuccess("Check-out thành công!");
    await taiDuLieu();
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Check-out thất bại"));
  } finally {
    dangXuLy.value = false;
  }
}

const tongCaHomNay = computed(() => lichNgayDangChon.value.length);
const tongGioLamHomNay = computed(() => tongCaHomNay.value * 4);

const thongTinTienTrinhCa = computed(() => {
  const activeCa = ['sang', 'chieu', 'toi'].find(ca => trangThaiChamCong(ca) === 'Đã check-in');
  if (!activeCa) {
    const completedCa = ['sang', 'chieu', 'toi'].find(ca => trangThaiChamCong(ca) === 'Đã check-out');
    if (completedCa) {
      return {
        dangLam: false,
        daLam: 4.0,
        phanTram: 100,
        label: 'Đã làm: 4.0 / 4.0h'
      };
    }
    return {
      dangLam: false,
      daLam: 0.0,
      phanTram: 0,
      label: 'Đã làm: 0.0 / 4.0h'
    };
  }

  const c = getChamCongData(activeCa);
  if (!c || !c.thoiGianVao) {
    return { dangLam: false, daLam: 0.0, phanTram: 0, label: 'Đã làm: 0.0 / 4.0h' };
  }

  const timeVao = new Date(c.thoiGianVao).getTime();
  const nowTime = currentTime.value.getTime();
  const diffMs = nowTime - timeVao;
  let diffHours = diffMs / (1000 * 60 * 60);
  if (diffHours < 0) diffHours = 0;
  if (diffHours > 4.0) diffHours = 4.0;
  
  const roundedHours = Math.round(diffHours * 10) / 10;
  const percent = Math.min(100, Math.round((roundedHours / 4.0) * 100));

  return {
    dangLam: true,
    daLam: roundedHours,
    phanTram: percent,
    label: `Đã làm: ${roundedHours.toFixed(1)} / 4.0h`
  };
});

async function handleXinPhep(loai, caName) {
  activeDropdown.value = null;
  const caText = caName === 'sang' ? 'Ca sáng' : caName === 'chieu' ? 'Ca chiều' : 'Ca tối';
  
  if (loai === 'den_muon') {
    const { value: reason } = await Swal.fire({
      title: 'Xin đến muộn',
      text: `Nhập lý do xin đến muộn cho ${caText}:`,
      input: 'textarea',
      inputPlaceholder: 'Ví dụ: Hỏng xe, có lịch học đột xuất...',
      showCancelButton: true,
      confirmButtonText: 'Gửi yêu cầu',
      cancelButtonText: 'Hủy',
      confirmButtonColor: '#00A859',
      cancelButtonColor: '#94a3b8',
      customClass: {
        popup: 'rounded-3xl p-6 font-sans',
        confirmButton: 'rounded-xl px-5 py-2.5 text-sm font-semibold',
        cancelButton: 'rounded-xl px-5 py-2.5 text-sm font-semibold'
      }
    });
    if (reason) {
      Swal.fire({
        icon: 'success',
        title: 'Đã gửi yêu cầu',
        text: 'Yêu cầu xin đến muộn đã được gửi tới Đức admin phê duyệt.',
        confirmButtonColor: '#00A859',
        customClass: { popup: 'rounded-3xl p-6 font-sans' }
      });
    }
  } else if (loai === 'da_ca') {
    const { value: companion } = await Swal.fire({
      title: 'Xin nhờ đá ca',
      text: `Nhập tên đồng nghiệp bạn muốn nhờ làm thay cho ${caText}:`,
      input: 'text',
      inputPlaceholder: 'Nhập tên đồng nghiệp...',
      showCancelButton: true,
      confirmButtonText: 'Gửi yêu cầu',
      cancelButtonText: 'Hủy',
      confirmButtonColor: '#00A859',
      cancelButtonColor: '#94a3b8',
      customClass: {
        popup: 'rounded-3xl p-6 font-sans',
        confirmButton: 'rounded-xl px-5 py-2.5 text-sm font-semibold',
        cancelButton: 'rounded-xl px-5 py-2.5 text-sm font-semibold'
      }
    });
    if (companion) {
      Swal.fire({
        icon: 'success',
        title: 'Đã gửi yêu cầu',
        text: `Đã gửi yêu cầu đá ca cùng ${companion} tới Quản lý phê duyệt.`,
        confirmButtonColor: '#00A859',
        customClass: { popup: 'rounded-3xl p-6 font-sans' }
      });
    }
  } else if (loai === 'nghi_phep') {
    const { value: reason } = await Swal.fire({
      title: 'Xin nghỉ phép',
      text: `Nhập lý do xin nghỉ phép cho ${caText}:`,
      input: 'textarea',
      inputPlaceholder: 'Nhập lý do nghỉ phép...',
      showCancelButton: true,
      confirmButtonText: 'Gửi yêu cầu',
      cancelButtonText: 'Hủy',
      confirmButtonColor: '#00A859',
      cancelButtonColor: '#94a3b8',
      customClass: {
        popup: 'rounded-3xl p-6 font-sans',
        confirmButton: 'rounded-xl px-5 py-2.5 text-sm font-semibold',
        cancelButton: 'rounded-xl px-5 py-2.5 text-sm font-semibold'
      }
    });
    if (reason) {
      Swal.fire({
        icon: 'success',
        title: 'Đã gửi yêu cầu',
        text: 'Yêu cầu xin nghỉ phép đã được gửi đi.',
        confirmButtonColor: '#00A859',
        customClass: { popup: 'rounded-3xl p-6 font-sans' }
      });
    }
  }
}
</script>
