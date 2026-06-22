<script setup>
import { ref, computed, watch, onUnmounted } from "vue";
import { Printer, X, Sparkles, CheckCircle } from "lucide-vue-next";
import ghnLogo from "../../../assets/logo/Logo-GHN-Blue-Orange.webp";
const props = defineProps({
  activePendingInvoice: {
    type: Object,
    default: null
  },
  invoiceLoading: {
    type: Boolean,
    default: false
  },
  tongSoLuong: {
    type: Number,
    default: 0
  },
  tongTienSauGiamHienThi: {
    type: Number,
    default: 0
  },
  tienGiam: {
    type: Number,
    default: 0
  },
  tongTien: {
    type: Number,
    default: 0
  },
  sanPhamValidationMessage: {
    type: String,
    default: ""
  },
  couponCode: {
    type: String,
    default: ""
  },
  coTheApDungPhieu: {
    type: Boolean,
    default: false
  },
  applyingCoupon: {
    type: Boolean,
    default: false
  },
  showCouponDropdown: {
    type: Boolean,
    default: false
  },
  coTheTimPhieu: {
    type: Boolean,
    default: false
  },
  loadingCoupons: {
    type: Boolean,
    default: false
  },
  couponResults: {
    type: Array,
    default: () => []
  },
  nextTierCoupon: {
    type: Object,
    default: null
  },
  missingAmountForNextTier: {
    type: Number,
    default: 0
  },
  missingProductsForNextTier: {
    type: Number,
    default: 0
  },
  nextTierDiscountAmount: {
    type: Number,
    default: 0
  },
  isGuestCustomer: {
    type: Boolean,
    default: false
  },
  appliedCoupon: {
    type: Object,
    default: null
  },
  maPhieuChuaApDung: {
    type: Boolean,
    default: false
  },
  khachCanTra: {
    type: Number,
    default: 0
  },
  shippingInfo: {
    type: Object,
    default: () => ({})
  },
  tenKhachHangHienThi: {
    type: String,
    default: ""
  },
  soDienThoaiKhachHangHienThi: {
    type: String,
    default: ""
  },
  paymentMethod: {
    type: Number,
    default: 1
  },
  amountPaid: {
    type: String,
    default: ""
  },
  paymentValidationMessage: {
    type: String,
    default: ""
  },
  tienThua: {
    type: Number,
    default: 0
  },
  paymentNote: {
    type: String,
    default: ""
  },
  canCreatePendingInvoice: {
    type: Boolean,
    default: false
  },
  savingPendingInvoice: {
    type: Boolean,
    default: false
  },
  pendingInvoiceLimitReached: {
    type: Boolean,
    default: false
  },
  canPay: {
    type: Boolean,
    default: false
  },
  payingInvoice: {
    type: Boolean,
    default: false
  },
  cancelingPendingInvoice: {
    type: Boolean,
    default: false
  },
  hasPrintedInvoice: {
    type: Boolean,
    default: false
  },
  dinhDangTien: {
    type: Function,
    required: true
  }
});

const emit = defineEmits([
  "update:couponCode",
  "focus-coupon",
  "blur-coupon",
  "apply-coupon",
  "select-coupon",
  "remove-coupon",
  "update-shipping",
  "calculate-shipping",
  "update:paymentMethod",
  "amount-input",
  "update:paymentNote",
  "print-invoice",
  "pay-now",
  "cancel-pending-invoice",
  "create-empty-invoice",
  "pay-later"
]);

const timeLeft = ref(300);
const showLargeQr = ref(false);
const isAmountTouched = ref(false);
let timer = null;

watch(() => props.paymentMethod, (newVal) => {
  isAmountTouched.value = false;
  if (newVal === 2) {
    timeLeft.value = 300;
    startTimer();
  } else {
    stopTimer();
  }
});

function startTimer() {
  stopTimer();
  timer = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
    } else {
      stopTimer();
    }
  }, 1000);
}

function stopTimer() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

onUnmounted(() => {
  stopTimer();
});

const formattedTimeLeft = computed(() => {
  const m = Math.floor(timeLeft.value / 60);
  const s = timeLeft.value % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
});

const sepayQrUrl = computed(() => {
  if (!props.activePendingInvoice) return '';
  const bank = 'MB';
  const acc = '894932828';
  const prefix = 'SHOE';
  const amount = Math.max(Number(props.khachCanTra) || 0, 0);
  const description = encodeURIComponent(`${prefix}${props.activePendingInvoice.maHoaDon}`);
  const accountName = encodeURIComponent('TRAN VU TUNG ANH');
  return `https://img.vietqr.io/image/${bank}-${acc}-compact2.png?amount=${amount}&addInfo=${description}&accountName=${accountName}`;
});
</script>

<template>
  <div class="flex flex-col">
    <div class="flex flex-col rounded-[24px] bg-[linear-gradient(180deg,#fff7f4_0%,#ffffff_100%)] p-3">

      <div class="mt-1 space-y-3">

        <!-- Voucher khả dụng (Applied Coupon) -->
        <div v-if="appliedCoupon && appliedCoupon.ma" class="bg-[#F2F9F4] rounded-xl p-3.5 flex flex-col relative border border-[#E3F2E8]">
           <button @click="emit('remove-coupon')" class="absolute top-2.5 right-2.5 p-1 text-emerald-600 hover:bg-emerald-100 rounded-full transition-colors">
             <X class="w-4 h-4" />
           </button>
           <div class="flex items-center gap-2 mb-3">
             <CheckCircle class="w-[18px] h-[18px] text-emerald-600 fill-emerald-100" />
             <span class="text-[14px] font-semibold text-slate-800">Đang áp dụng voucher tốt nhất</span>
           </div>
           <div class="pl-[26px] pr-2 flex flex-col gap-2.5">
              <div class="flex items-center justify-between">
                <span class="font-bold text-slate-800 text-[15px]">{{ appliedCoupon.ma }}</span>
                <span v-if="appliedCoupon.loai === 1" class="text-xs font-semibold text-emerald-600 bg-emerald-100 border border-emerald-200 px-2 py-0.5 rounded">{{ appliedCoupon.giaTri }}%</span>
              </div>
              <div class="flex items-center justify-between text-[13px]">
                 <span class="text-slate-500">Giá trị giảm:</span>
                 <span class="font-bold text-emerald-600">-{{ dinhDangTien(appliedCoupon.soTienGiam) }}</span>
              </div>
           </div>
        </div>


        <div class="flex items-center justify-between mb-3 mt-4">
          <span class="text-[15px] font-bold text-emerald-600">Thông tin đơn hàng</span>
          <div class="flex items-center gap-2">
            <span class="text-[13px] font-medium" :class="shippingInfo.giaoHang ? 'text-emerald-600' : 'text-slate-500'">Giao hàng</span>
            <label class="relative inline-flex cursor-pointer items-center">
              <input
                type="checkbox"
                class="peer sr-only"
                :checked="shippingInfo.giaoHang"
                @change="emit('update-shipping', { giaoHang: $event.target.checked })"
              />
              <div class="h-5 w-9 rounded-full bg-slate-200 transition-colors peer-checked:bg-emerald-500 peer-focus:outline-none"></div>
              <div class="absolute left-[2px] top-[2px] h-4 w-4 rounded-full border border-slate-300 bg-white transition-all peer-checked:translate-x-full peer-checked:border-white"></div>
            </label>
          </div>
        </div>

        <div class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2">
          <span class="text-sm text-slate-500">Tổng tiền hàng:</span>
          <span class="max-w-[65%] break-all text-right text-[15px] font-bold text-slate-900">{{ dinhDangTien(tongTien) }}</span>
        </div>
        <div v-if="tienGiam > 0" class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2">
          <span class="text-sm text-slate-500">Tiền giảm</span>
          <span class="max-w-[65%] break-all text-right text-base font-bold text-emerald-600">-{{ dinhDangTien(tienGiam) }}</span>
        </div>
        <!-- Gợi ý mua thêm (Suggested Coupon) -->
        <div v-if="nextTierCoupon" class="mt-2 pb-2 border-b border-slate-200">
          <div class="flex items-center justify-between mb-3">
            <span class="text-[15px] font-bold text-emerald-600">Gợi ý mua thêm</span>
            <span class="text-[11px] font-semibold text-amber-600 bg-[#FFF8ED] border border-amber-200 px-2.5 py-0.5 rounded-full">1 đề xuất</span>
          </div>
          <div class="rounded-xl border border-slate-100 bg-white shadow-[0_2px_10px_rgba(0,0,0,0.02)] p-3 flex flex-col gap-2">
            <div class="flex items-center gap-3">
              <span class="text-[13px] font-semibold text-emerald-600 bg-emerald-50 border border-emerald-100 px-2.5 py-0.5 rounded-full min-w-[48px] text-center whitespace-nowrap">{{ nextTierCoupon.loai === 1 ? nextTierCoupon.giaTri + '%' : dinhDangTien(nextTierCoupon.giaTri) }}</span>
              <span class="font-bold text-[15px] text-slate-800">{{ nextTierCoupon.ma }}</span>
            </div>
            <div class="flex flex-col gap-1.5 mt-1 ml-[60px] mr-2">
              <div class="flex items-center justify-between text-[13px]">
                 <span class="text-slate-500">Cần mua thêm:</span>
                 <span class="font-bold text-slate-700">{{ dinhDangTien(missingAmountForNextTier) }}</span>
              </div>
              <div class="flex items-center justify-between text-[13px]">
                 <span class="text-slate-500">Sẽ được giảm:</span>
                 <span class="font-bold text-emerald-600">{{ dinhDangTien(nextTierDiscountAmount) }}</span>
              </div>
            </div>
          </div>
        </div>


        <!-- Shipping fee row — compact single line matching design reference -->
        <div v-if="shippingInfo.giaoHang" class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2">
          <!-- Left: logo + label -->
          <div class="flex items-center gap-1.5 shrink-0">
            <img :src="ghnLogo" alt="GHN" class="h-4 w-auto object-contain" />
            <span class="text-sm text-slate-500">Phí vận chuyển</span>
          </div>

          <!-- Right: fee box + calculate button -->
          <div class="flex items-center gap-2">
            <!-- Spinner while calculating -->
            <svg v-if="shippingInfo.dangTinhPhi" class="h-4 w-4 animate-spin text-slate-400" viewBox="0 0 24 24" fill="none">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
            </svg>

            <!-- Fee amount box -->
            <span
              class="inline-flex min-w-[72px] items-center justify-end rounded-lg border bg-white px-2.5 py-1 text-sm font-semibold"
              :class="shippingInfo.daTinhPhi && shippingInfo.phiVanChuyen > 0
                ? 'border-emerald-200 text-emerald-700'
                : 'border-slate-200 text-slate-400'"
            >
              {{ shippingInfo.dangTinhPhi ? '...' : dinhDangTien(shippingInfo.phiVanChuyen || 0) }}
            </span>

            <!-- Calculate icon button -->
            <button
              v-if="!shippingInfo.dangTinhPhi && shippingInfo.coTheTinhPhi"
              type="button"
              title="Tính phí vận chuyển"
              class="flex h-7 w-7 items-center justify-center rounded-lg border border-orange-200 bg-orange-50 text-orange-500 transition hover:bg-orange-100 active:scale-95"
              @click="emit('calculate-shipping')"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="23 4 23 10 17 10"/>
                <polyline points="1 20 1 14 7 14"/>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
              </svg>
            </button>

            <!-- Warning: no address -->
            <span
              v-else-if="!shippingInfo.dangTinhPhi && !shippingInfo.diaChiGiaoHang"
              title="Nhập địa chỉ giao hàng để tính phí"
              class="text-amber-400"
            ></span>
          </div>
        </div>

        <div v-if="khachCanTra > 0" class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2">
          <span class="text-sm text-slate-500">Khách cần trả</span>
          <span class="max-w-[65%] break-all text-right text-base font-bold text-slate-900">{{ dinhDangTien(khachCanTra) }}</span>
        </div>

        <!-- Shipping Section moved to BanHangShippingSection.vue -->
        <div>
          <p class="mb-1.5 text-sm text-slate-500">Hình thức thanh toán</p>
          <div class="grid grid-cols-2 gap-x-6 gap-y-2">
            <label class="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
              <input
                :checked="paymentMethod === 1"
                type="radio"
                class="h-4 w-4 accent-red-500"
                @change="emit('update:paymentMethod', 1)"
              />
              <span>Tiền mặt</span>
            </label>
            <label class="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
              <input
                :checked="paymentMethod === 2"
                type="radio"
                class="h-4 w-4 accent-red-500"
                @click="emit('update:paymentMethod', 2); showLargeQr = true"
              />
              <span>Chuyển khoản</span>
            </label>
          </div>
        </div>
        <div v-if="paymentMethod === 1">
          <label class="mb-1.5 block text-sm text-slate-500">Khách thanh toán</label>
          <input
            :value="amountPaid"
            type="text"
            inputmode="numeric"
            autocomplete="off"
            :disabled="paymentMethod !== 1"
            :placeholder="paymentMethod === 1 ? 'Nhập số tiền khách đưa' : 'Tự động bằng số tiền cần thanh toán'"
            class="w-full rounded-xl border px-3 py-2.5 text-left text-sm font-semibold text-slate-900 outline-none transition disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
            :class="isAmountTouched && paymentValidationMessage ? 'border-rose-300 bg-rose-50 focus:border-rose-400' : 'border-slate-200 focus:border-red-300'"
            @input="emit('amount-input', $event.target.value); isAmountTouched = false"
            @blur="isAmountTouched = true"
          />
          <p v-if="isAmountTouched && paymentValidationMessage" class="mt-1 text-xs font-medium text-rose-500">
            {{ paymentValidationMessage }}
          </p>
        </div>

        <div v-if="paymentMethod === 1" class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2">
          <span class="text-sm text-slate-500">Tiền thừa trả khách</span>
          <span class="max-w-[65%] break-all text-right text-base font-bold text-slate-900">{{ dinhDangTien(tienThua) }}</span>
        </div>

        <div v-if="paymentMethod === 2" class="flex flex-col items-center justify-center rounded-2xl border border-slate-200 bg-white p-3 shadow-sm">
          <p class="mb-2 text-sm font-semibold text-slate-800">Quét mã QR để thanh toán</p>
          <div v-if="timeLeft > 0">
            <img
              :src="sepayQrUrl"
              alt="VietQR"
              class="h-40 w-40 cursor-pointer rounded-xl border border-slate-100 object-contain transition hover:scale-105"
              title="Bấm để phóng to"
              @click="showLargeQr = true"
            />
            <p class="mt-2 text-center text-xs text-slate-500">
              QR sẽ hết hạn sau: <span class="font-bold text-red-500">{{ formattedTimeLeft }}</span>
            </p>
          </div>
          <div v-else class="flex h-40 w-40 flex-col items-center justify-center rounded-xl border border-dashed border-rose-200 bg-rose-50 text-center text-rose-500">
            <svg xmlns="http://www.w3.org/2000/svg" class="mb-2 h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span class="text-sm font-bold">Mã QR đã hết hạn</span>
            <button type="button" @click="timeLeft = 300; startTimer()" class="mt-2 rounded bg-rose-100 px-3 py-1 text-xs font-semibold text-rose-600 hover:bg-rose-200">Tạo lại</button>
          </div>
        </div>
        <div>
          <label class="mb-1.5 block text-sm text-slate-500">Ghi chú thanh toán</label>
          <textarea
            :value="paymentNote"
            rows="2"
            placeholder="Ghi chú thêm nếu cần"
            class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-red-300"
            @input="emit('update:paymentNote', $event.target.value)"
          />
        </div>
      </div>

      <div class="mt-3 shrink-0 border-t border-slate-100 pt-3 flex flex-col gap-3">
        <div class="grid gap-3 sm:grid-cols-2">
          <button
            type="button"
            class="rounded-xl bg-slate-200 px-3 py-3 text-sm font-bold text-slate-700 transition hover:bg-slate-300 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
            :disabled="cancelingPendingInvoice"
            @click="emit('cancel-pending-invoice')"
          >
            {{ cancelingPendingInvoice ? "Đang hủy..." : "Hủy hóa đơn" }}
          </button>

          <button
            type="button"
            class="rounded-xl bg-red-500 px-3 py-3 text-sm font-bold text-white shadow-[0_20px_40px_rgba(239,68,68,0.35)] transition hover:bg-red-600 disabled:cursor-not-allowed disabled:bg-slate-300 disabled:shadow-none whitespace-nowrap"
            :disabled="!canPay || payingInvoice"
            @click="emit('pay-now')"
          >
            {{ payingInvoice ? "Đang xử lý..." : "Thanh toán" }}
          </button>
        </div>
      </div>
      <p v-if="sanPhamValidationMessage" class="mt-3 text-xs font-medium text-rose-500">
        {{ sanPhamValidationMessage }}
      </p>

    </div>

    <Teleport to="body">
      <div v-if="showLargeQr" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm" @click="showLargeQr = false">
        <div class="relative rounded-[32px] bg-white p-8 shadow-2xl" @click.stop>
          <button
            class="absolute right-4 top-4 flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-500 hover:bg-slate-200"
            @click="showLargeQr = false"
          >
            <X class="h-6 w-6" />
          </button>
          <h3 class="mb-6 text-center text-xl font-bold text-slate-800">Quét mã QR để thanh toán</h3>
          <img :src="sepayQrUrl" alt="VietQR Large" class="h-96 w-96 rounded-2xl border-2 border-slate-100 object-contain shadow-sm" />
          <p class="mt-6 text-center text-base font-medium text-slate-600">
            QR sẽ hết hạn sau: <span class="font-bold text-red-500">{{ formattedTimeLeft }}</span>
          </p>
        </div>
      </div>
    </Teleport>
  </div>
</template>
