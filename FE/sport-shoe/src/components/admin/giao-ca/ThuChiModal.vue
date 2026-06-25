<script setup>
import { ref, watch, computed } from "vue";
import { X, ReceiptText, CalendarClock } from "lucide-vue-next";
import { useAdminSession } from "../../../composable/useAdminSession";

const props = defineProps({
  show: Boolean,
  type: String, // 'THU' | 'CHI'
  shiftInfo: Object, // { id, ma, nhanVienTrongCaTen }
});

const emit = defineEmits(["close"]);

const { adminSession } = useAdminSession();

const danhSachThuChi = ref([]);
const dangTai = ref(false);

const formatVND = (value) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value || 0).replace("₫", "đ");
};

const title = computed(() => {
  return props.type === 'THU' ? 'Tổng thu khác' : 'Tổng chi trong ca';
});

const totalAmount = computed(() => {
  return danhSachThuChi.value.reduce((sum, item) => sum + (item.soTien || 0), 0);
});

watch(() => props.show, async (newVal) => {
  if (newVal && props.shiftInfo?.id) {
    dangTai.value = true;
    try {
      // TODO (Backend): Cần implement API này trên Backend
      // Endpoint: GET /api/admin/giao-ca/{shiftId}/thu-chi?type={THU|CHI}&payment_method=CASH
      // Yêu cầu (Note cho BE): 
      // 1. Chỉ lấy các phiếu Thu/Chi bằng Tiền mặt (CASH), KHÔNG lấy chuyển khoản/Momo.
      // 2. Lọc theo giao_ca_id HOẶC (created_at >= shift.start_time AND <= shift.end_time)
      
      // MOCK DATA để test FE
      await new Promise(resolve => setTimeout(resolve, 500));
      
      if (props.type === 'CHI') {
        danhSachThuChi.value = [
          {
            id: 1,
            thoiGian: new Date().toISOString(),
            maPhieu: "PC0012",
            hangMuc: "Trả tiền ship đồng phục",
            soTien: 150000
          },
          {
            id: 2,
            thoiGian: new Date(Date.now() - 3600000).toISOString(),
            maPhieu: "PC0011",
            hangMuc: "Mua nước suối cho cửa hàng",
            soTien: 50000
          }
        ];
      } else {
        danhSachThuChi.value = [
          {
            id: 3,
            thoiGian: new Date().toISOString(),
            maPhieu: "PT0089",
            hangMuc: "Thu tiền bán phế liệu",
            soTien: 85000
          }
        ];
      }
    } catch (error) {
      console.error("Lỗi tải chi tiết thu chi:", error);
      danhSachThuChi.value = [];
    } finally {
      dangTai.value = false;
    }
  }
});
</script>

<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <!-- Backdrop overlay -->
    <div 
      class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" 
      @click="emit('close')"
    ></div>

    <!-- Modal Content -->
    <div class="relative w-full max-w-2xl bg-white dark:bg-slate-800 rounded-[24px] shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
      
      <!-- Header -->
      <div class="px-6 py-5 border-b border-slate-100 dark:border-slate-700/60 flex items-center justify-between bg-slate-50/50 dark:bg-slate-900/20">
        <div class="flex items-center gap-3">
          <div class="h-10 w-10 rounded-full flex items-center justify-center" :class="type === 'THU' ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'">
            <ReceiptText class="h-5 w-5" />
          </div>
          <div>
            <h3 class="text-lg font-bold text-slate-800 dark:text-white">Chi tiết {{ title }}</h3>
            <p class="text-xs font-medium text-slate-500 mt-0.5">
              Ca: <span class="text-primary font-bold">{{ shiftInfo?.ma || 'N/A' }}</span> | 
              Nhân viên: <span class="font-bold text-slate-700 dark:text-slate-300">{{ shiftInfo?.nhanVienTrongCaTen || 'N/A' }}</span>
            </p>
          </div>
        </div>
        
        <button 
          @click="emit('close')"
          class="h-9 w-9 rounded-full flex items-center justify-center text-slate-400 hover:bg-slate-200 hover:text-slate-600 transition"
        >
          <X class="h-5 w-5" />
        </button>
      </div>

      <!-- Body / Table -->
      <div class="p-6 overflow-y-auto flex-1">
        <div v-if="dangTai" class="py-12 flex flex-col items-center justify-center text-slate-400">
          <div class="h-8 w-8 rounded-full border-4 border-slate-200 border-t-primary animate-spin mb-3"></div>
          <span class="text-sm font-medium">Đang tải dữ liệu...</span>
        </div>
        
        <div v-else-if="!danhSachThuChi.length" class="py-12 text-center">
          <div class="mx-auto h-12 w-12 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 mb-3">
            <ReceiptText class="h-6 w-6" />
          </div>
          <p class="text-slate-500 text-sm font-medium">Không có khoản {{ type === 'THU' ? 'thu' : 'chi' }} nào bằng tiền mặt trong ca này.</p>
        </div>

        <div v-else class="border border-slate-200 dark:border-slate-700 rounded-2xl overflow-hidden">
          <table class="w-full text-left text-sm border-collapse">
            <thead>
              <tr class="bg-slate-50 dark:bg-slate-900/40 text-slate-500 dark:text-slate-400 font-semibold border-b border-slate-200 dark:border-slate-700 uppercase text-[11px] tracking-wider">
                <th class="px-4 py-3 w-32">Thời gian</th>
                <th class="px-4 py-3 w-32">Mã phiếu</th>
                <th class="px-4 py-3">Hạng mục</th>
                <th class="px-4 py-3 text-right w-40">Số tiền</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-700/50">
              <tr 
                v-for="item in danhSachThuChi" 
                :key="item.id"
                class="hover:bg-slate-50/50 dark:hover:bg-slate-800/50 text-slate-700 dark:text-slate-200"
              >
                <td class="px-4 py-3 font-mono text-xs">
                  <div class="flex items-center gap-1.5 text-slate-500">
                    <CalendarClock class="h-3.5 w-3.5" />
                    {{ new Date(item.thoiGian).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) }}
                  </div>
                </td>
                <td class="px-4 py-3 font-semibold text-primary text-xs">{{ item.maPhieu }}</td>
                <td class="px-4 py-3 text-sm font-medium">{{ item.hangMuc }}</td>
                <td class="px-4 py-3 text-right font-bold" :class="type === 'THU' ? 'text-emerald-600' : 'text-rose-600'">
                  {{ formatVND(item.soTien) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-6 py-4 border-t border-slate-100 dark:border-slate-700/60 bg-slate-50 dark:bg-slate-900/40 flex items-center justify-between">
        <div class="text-base">
          <span class="text-slate-500 font-medium">Tổng cộng:</span>
          <span class="ml-2 text-xl font-black tracking-tight" :class="type === 'THU' ? 'text-emerald-600' : 'text-rose-600'">
            {{ formatVND(totalAmount) }}
          </span>
        </div>
        <button 
          @click="emit('close')"
          class="px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-white font-bold rounded-xl transition text-sm shadow-sm"
        >
          Đóng
        </button>
      </div>

    </div>
  </div>
</template>
