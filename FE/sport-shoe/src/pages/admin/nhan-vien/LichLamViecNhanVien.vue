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
            <button 
              v-if="trangThaiChamCong('sang') === 'Chưa Check-in'"
              @click="handleCheckInClick"
              :disabled="dangXuLy"
              class="px-4 py-2 text-sm font-bold bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl shadow-sm transition disabled:opacity-50"
            >
              Check-in
            </button>
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
            <button 
              v-if="trangThaiChamCong('chieu') === 'Chưa Check-in'"
              @click="handleCheckInClick"
              :disabled="dangXuLy"
              class="px-4 py-2 text-sm font-bold bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl shadow-sm transition disabled:opacity-50"
            >
              Check-in
            </button>
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
            <button 
              v-if="trangThaiChamCong('toi') === 'Chưa Check-in'"
              @click="handleCheckInClick"
              :disabled="dangXuLy"
              class="px-4 py-2 text-sm font-bold bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl shadow-sm transition disabled:opacity-50"
            >
              Check-in
            </button>
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
                <p class="text-sm font-medium text-slate-500">Ca làm phân công</p>
                <p class="text-xl font-bold text-slate-800">{{ tongCaHomNay }}/3</p>
              </div>
              <div class="flex justify-between items-center border-b border-slate-100 pb-4">
                <p class="text-sm font-medium text-slate-500">Giờ làm việc dự kiến</p>
                <p class="text-xl font-bold text-slate-800">{{ tongGioLamHomNay }}h</p>
              </div>
              <div class="flex justify-between items-center">
                <p class="text-sm font-medium text-slate-500">Thời gian còn lại</p>
                <p class="text-xl font-bold text-[#CC0000]">{{ gioConLai }}</p>
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
      :saved-descriptor-string="adminUser?.faceDescriptor"
      @close="showFaceIdModal = false"
      @success="thucHienCheckIn"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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
  Megaphone 
} from 'lucide-vue-next';
import { useAdminSession } from '../../../composable/useAdminSession.js';
import { layLichLamViec } from '../../../services/lich-lam.js';
import { layChamCong } from '../../../services/cham-cong.js';
import { getDisplayErrorMessage } from '../../../utils/error-message.js';

const NHAN_TUAN = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

const { adminSession } = useAdminSession();
const userId = computed(() => adminSession.value.id);

const ngayHienTai = ref(new Date());
const cacNgayTrongTuan = ref([]);
const chonNgayIdx = ref(0);
const dangTai = ref(false);

const lichLamViecTuan = ref([]);
const chamCongTuan = ref([]);

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
  
  // Set selected day to today if week is current week, else default to Monday
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

onMounted(() => {
  initDate();
  taiDuLieu();
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

import { checkIn, checkOut } from '../../../services/cham-cong.js';
import { showSuccess, showError } from '../../../utils/alert.js';
import { getCurrentAdminUser } from '../../../services/auth';
import FaceIdCheckInModal from './components/FaceIdCheckInModal.vue';

const showFaceIdModal = ref(false);
const adminUser = getCurrentAdminUser();

async function handleCheckInClick() {
  if (!adminUser || !adminUser.faceDescriptor) {
    showError("Bạn chưa đăng ký khuôn mặt. Vui lòng liên hệ Admin để đăng ký Face ID trước khi Check-in.");
    return;
  }
  showFaceIdModal.value = true;
}

async function thucHienCheckIn() {
  showFaceIdModal.value = false;
  if (!userId.value) return;
  dangXuLy.value = true;
  try {
    await checkIn({ nhanVienId: userId.value });
    showSuccess("Check-in thành công!");
    await taiDuLieu(); // Tải lại để cập nhật trạng thái
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Check-in thất bại"));
  } finally {
    dangXuLy.value = false;
  }
}

async function thucHienCheckOut() {
  if (!userId.value) return;
  dangXuLy.value = true;
  try {
    await checkOut({ nhanVienId: userId.value });
    showSuccess("Check-out thành công!");
    await taiDuLieu(); // Tải lại để cập nhật trạng thái
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Check-out thất bại"));
  } finally {
    dangXuLy.value = false;
  }
}

const tongCaHomNay = computed(() => lichNgayDangChon.value.length);
const tongGioLamHomNay = computed(() => tongCaHomNay.value * 4); // Assume 4h per shift

const gioConLai = computed(() => {
  if (tongGioLamHomNay.value === 0) return "0h 00m";
  // Mock logic for remaining hours
  const today = formatISODate(new Date());
  const selected = formatISODate(cacNgayTrongTuan.value[chonNgayIdx.value]);
  
  if (selected < today) return "0h 00m";
  if (selected > today) return `${tongGioLamHomNay.value}h 00m`;
  
  // If today, mock something like half of it is done
  return `${Math.ceil(tongGioLamHomNay.value / 2)}h 30m`;
});
</script>
