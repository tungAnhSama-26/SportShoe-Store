<script setup>
defineProps({
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
  "create-pending-invoice",
  "pay-now",
  "cancel-pending-invoice"
]);
</script>

<template>
  <aside class="rounded-[32px] border border-white/70 bg-white/95 p-6 shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
    <div class="rounded-[28px] bg-[linear-gradient(180deg,#fff7f4_0%,#ffffff_100%)] p-5">
      <p class="text-xs font-semibold uppercase tracking-[0.24em] text-red-400">Tổng quan</p>
      <h2 class="mt-3 text-2xl font-bold text-slate-900">
        {{ activePendingInvoice?.ma || "Hóa đơn mới" }}
      </h2>
      <p class="mt-2 text-sm text-slate-500">
        {{ invoiceLoading ? "Đang tải chi tiết hóa đơn..." : "Hóa đơn bán hàng tại quầy đang thao tác." }}
      </p>

      <div class="mt-6 space-y-4">
        <div class="flex items-center justify-between border-b border-slate-200 pb-3">
          <span class="text-sm text-slate-500">Tổng sản phẩm</span>
          <span class="text-lg font-bold text-slate-900">{{ tongSoLuong }}</span>
        </div>
        <div class="flex items-start justify-between gap-3 border-b border-slate-200 pb-3">
          <span class="text-sm text-slate-500">Tổng tiền hàng</span>
          <div class="text-right">
            <p class="text-lg font-bold text-slate-900">{{ dinhDangTien(tongTienSauGiamHienThi) }}</p>
            <p v-if="tienGiam > 0" class="mt-1 text-xs text-slate-400 line-through">
              {{ dinhDangTien(tongTien) }}
            </p>
          </div>
        </div>

        <div class="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm">
          <label class="block text-sm font-semibold text-slate-800">Áp phiếu giảm giá</label>

          <div class="mt-3" @focusin="emit('focus-coupon')" @focusout="emit('blur-coupon')">
            <div class="flex flex-col gap-2 sm:flex-row">
              <input
                :value="couponCode"
                type="text"
                placeholder="Nhập mã hoặc tên phiếu"
                class="min-w-0 flex-1 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                @input="emit('update:couponCode', $event.target.value)"
                @keyup.enter="emit('apply-coupon')"
              />
              <button
                type="button"
                class="rounded-2xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                :disabled="!coTheApDungPhieu"
                @click="emit('apply-coupon')"
              >
                {{ applyingCoupon ? "Đang áp dụng..." : "Áp dụng" }}
              </button>
            </div>

            <div v-if="showCouponDropdown" class="mt-2 rounded-2xl border border-slate-200 bg-slate-50 p-2">
              <div v-if="!coTheTimPhieu" class="px-3 py-3 text-sm text-slate-500">
                Thêm sản phẩm vào hóa đơn để xem phiếu phù hợp.
              </div>
              <div v-else-if="loadingCoupons" class="px-3 py-3 text-sm text-slate-500">
                Đang tìm phiếu giảm giá...
              </div>
              <div v-else-if="!couponResults.length" class="px-3 py-3 text-sm text-slate-500">
                {{ couponCode.trim() ? "Không tìm thấy phiếu giảm giá phù hợp." : "Chưa có phiếu giảm giá phù hợp cho hóa đơn này." }}
              </div>
              <div v-else class="space-y-2">
                <button
                  v-for="coupon in couponResults"
                  :key="coupon.id"
                  type="button"
                  class="w-full rounded-2xl border border-transparent bg-white px-3 py-3 text-left transition hover:border-red-200 hover:bg-red-50"
                  @mousedown.prevent
                  @click="emit('select-coupon', coupon)"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0">
                      <p class="truncate text-sm font-semibold text-slate-800">{{ coupon.ma }}</p>
                      <p class="mt-1 text-xs text-slate-500">{{ coupon.ten }}</p>
                    </div>
                    <div class="text-right">
                      <p class="text-[11px] uppercase tracking-[0.18em] text-slate-400">Giảm</p>
                      <p class="text-sm font-bold text-emerald-600">{{ dinhDangTien(coupon.soTienGiam) }}</p>
                    </div>
                  </div>
                </button>
              </div>
            </div>
          </div>

          <p v-if="maPhieuChuaApDung" class="mt-2 text-xs font-medium text-amber-600">
            Mã phiếu đang thay đổi. Vui lòng áp dụng lại trước khi lưu hoặc thanh toán.
          </p>

          <div
            v-if="appliedCoupon"
            class="mt-3 rounded-2xl border border-emerald-100 bg-emerald-50 px-4 py-3"
          >
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="text-sm font-semibold text-emerald-700">{{ appliedCoupon.ma }}</p>
                <p class="mt-1 text-xs text-emerald-600">{{ appliedCoupon.ten }}</p>
              </div>
              <button
                type="button"
                class="text-xs font-semibold text-emerald-700 transition hover:text-emerald-900"
                @click="emit('remove-coupon')"
              >
                Bỏ mã
              </button>
            </div>
            <div class="mt-3 flex items-start justify-between gap-3 text-sm">
              <span class="text-emerald-700">Tiền giảm</span>
              <span class="max-w-[65%] break-all text-right font-bold text-emerald-700">{{ dinhDangTien(tienGiam) }}</span>
            </div>
          </div>
        </div>

        <div class="flex items-start justify-between gap-3 border-b border-slate-200 pb-3">
          <span class="text-sm text-slate-500">Tiền giảm</span>
          <span class="max-w-[65%] break-all text-right text-lg font-bold text-emerald-600">{{ dinhDangTien(tienGiam) }}</span>
        </div>
        <div v-if="shippingInfo.giaoHang" class="flex items-start justify-between gap-3 border-b border-slate-200 pb-3">
          <span class="text-sm text-slate-500">Phí ship</span>
          <span class="max-w-[65%] break-all text-right text-lg font-bold text-slate-900">{{ dinhDangTien(shippingInfo.phiVanChuyen || 0) }}</span>
        </div>
        <div class="flex items-start justify-between gap-3 border-b border-slate-200 pb-3">
          <span class="text-sm text-slate-500">Khách cần trả</span>
          <span class="max-w-[65%] break-all text-right text-lg font-bold text-slate-900">{{ dinhDangTien(khachCanTra) }}</span>
        </div>
        <div class="flex items-center justify-between border-b border-slate-200 pb-3">
          <span class="text-sm text-slate-500">Khách hàng</span>
          <span class="text-right text-sm font-semibold text-slate-700">{{ tenKhachHangHienThi }}</span>
        </div>
        <div class="flex items-center justify-between border-b border-slate-200 pb-3">
          <span class="text-sm text-slate-500">Số điện thoại</span>
          <span class="text-right text-sm font-semibold text-slate-700">{{ soDienThoaiKhachHangHienThi }}</span>
        </div>
        <div class="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-sm font-semibold text-slate-800">Giao hàng</p>
              <p class="mt-1 text-xs text-slate-500">Tính phí ship theo luồng quản lý hóa đơn.</p>
            </div>
            <label class="inline-flex cursor-pointer items-center gap-3 text-sm font-medium text-slate-700">
              <input
                :checked="shippingInfo.giaoHang"
                type="checkbox"
                class="h-4 w-4 rounded accent-red-500"
                @change="emit('update-shipping', { giaoHang: $event.target.checked })"
              />
              <span>{{ shippingInfo.giaoHang ? "Bật" : "Tắt" }}</span>
            </label>
          </div>

          <div v-if="shippingInfo.giaoHang" class="mt-4 space-y-3">
            <div class="grid gap-3 sm:grid-cols-2">
              <label class="space-y-2">
                <span class="block text-xs font-medium text-slate-500">Người nhận</span>
                <input
                  :value="shippingInfo.tenNguoiNhan || ''"
                  type="text"
                  placeholder="Nhập tên người nhận"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                  @input="emit('update-shipping', { tenNguoiNhan: $event.target.value })"
                />
              </label>
              <label class="space-y-2">
                <span class="block text-xs font-medium text-slate-500">Số điện thoại</span>
                <input
                  :value="shippingInfo.soDienThoaiNguoiNhan || ''"
                  type="text"
                  inputmode="numeric"
                  placeholder="Nhập số điện thoại"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                  @input="emit('update-shipping', { soDienThoaiNguoiNhan: $event.target.value })"
                />
              </label>
            </div>

            <label class="space-y-2">
              <span class="block text-xs font-medium text-slate-500">Địa chỉ giao hàng</span>
              <textarea
                :value="shippingInfo.diaChiGiaoHang || ''"
                rows="2"
                placeholder="Nhập địa chỉ giao hàng đầy đủ"
                class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                @input="emit('update-shipping', { diaChiGiaoHang: $event.target.value })"
              />
            </label>

            <div v-if="shippingInfo.diaChiDaDo" class="rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600">
              GHN dò: <span class="font-semibold text-slate-800">{{ shippingInfo.diaChiDaDo }}</span>
            </div>

            <div class="grid gap-3 sm:grid-cols-2">
              <label class="space-y-2">
                <span class="block text-xs font-medium text-slate-500">Loại dịch vụ</span>
                <select
                  :value="shippingInfo.serviceTypeId ?? 2"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                  @change="emit('update-shipping', { serviceTypeId: Number($event.target.value) })"
                >
                  <option :value="2">Hàng nhẹ</option>
                  <option :value="5">Hàng nặng</option>
                </select>
              </label>
              <label class="space-y-2">
                <span class="block text-xs font-medium text-slate-500">Cân nặng (gram)</span>
                <input
                  :value="shippingInfo.weight ?? 500"
                  type="number"
                  min="1"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                  @input="emit('update-shipping', { weight: Number($event.target.value) })"
                />
              </label>
              <label class="space-y-2">
                <span class="block text-xs font-medium text-slate-500">Dài (cm)</span>
                <input
                  :value="shippingInfo.length ?? 30"
                  type="number"
                  min="1"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                  @input="emit('update-shipping', { length: Number($event.target.value) })"
                />
              </label>
              <label class="space-y-2">
                <span class="block text-xs font-medium text-slate-500">Rộng (cm)</span>
                <input
                  :value="shippingInfo.width ?? 20"
                  type="number"
                  min="1"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                  @input="emit('update-shipping', { width: Number($event.target.value) })"
                />
              </label>
              <label class="space-y-2 sm:col-span-2">
                <span class="block text-xs font-medium text-slate-500">Cao (cm)</span>
                <input
                  :value="shippingInfo.height ?? 12"
                  type="number"
                  min="1"
                  class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
                  @input="emit('update-shipping', { height: Number($event.target.value) })"
                />
              </label>
            </div>

            <div class="flex items-center justify-between gap-3 rounded-2xl bg-slate-50 px-4 py-3">
              <div class="min-w-0">
                <p class="text-xs font-medium uppercase tracking-[0.18em] text-slate-400">Phí ship</p>
                <p class="mt-1 text-lg font-bold text-slate-900">
                  {{ shippingInfo.daTinhPhi ? dinhDangTien(shippingInfo.phiVanChuyen || 0) : "Chưa tính" }}
                </p>
              </div>
              <button
                type="button"
                class="rounded-2xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                :disabled="!shippingInfo.coTheTinhPhi"
                @click="emit('calculate-shipping')"
              >
                {{ shippingInfo.dangTinhPhi ? "Đang tính..." : "Tính phí GHN" }}
              </button>
            </div>

            <p v-if="!shippingInfo.daTinhPhi" class="text-xs font-medium text-amber-600">
              Vui lòng tính phí ship trước khi lưu hoặc thanh toán.
            </p>
          </div>
        </div>
        <div>
          <p class="mb-2 text-sm text-slate-500">Hình thức thanh toán</p>
          <div class="grid grid-cols-2 gap-x-6 gap-y-3">
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
                @change="emit('update:paymentMethod', 2)"
              />
              <span>Chuyển khoản</span>
            </label>
            <label class="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
              <input
                :checked="paymentMethod === 4"
                type="radio"
                class="h-4 w-4 accent-red-500"
                @change="emit('update:paymentMethod', 4)"
              />
              <span>Thẻ</span>
            </label>
            <label class="flex cursor-pointer items-center gap-3 text-sm text-slate-700">
              <input
                :checked="paymentMethod === 3"
                type="radio"
                class="h-4 w-4 accent-red-500"
                @change="emit('update:paymentMethod', 3)"
              />
              <span>Ví</span>
            </label>
          </div>
        </div>
        <div>
          <label class="mb-2 block text-sm text-slate-500">Khách thanh toán</label>
          <input
            :value="amountPaid"
            type="text"
            inputmode="numeric"
            autocomplete="off"
            :disabled="paymentMethod !== 1"
            :placeholder="paymentMethod === 1 ? 'Nhập số tiền khách đưa' : 'Tự động bằng số tiền cần thanh toán'"
            class="w-full rounded-2xl border border-slate-200 px-4 py-3 text-left text-sm font-semibold text-slate-900 outline-none transition focus:border-red-300 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
            @input="emit('amount-input', $event.target.value)"
          />
        </div>
        <div class="flex items-start justify-between gap-3 border-b border-slate-200 pb-3">
          <span class="text-sm text-slate-500">Tiền thừa trả khách</span>
          <span class="max-w-[65%] break-all text-right text-lg font-bold text-slate-900">{{ dinhDangTien(tienThua) }}</span>
        </div>
        <div>
          <label class="mb-2 block text-sm text-slate-500">Ghi chú thanh toán</label>
          <textarea
            :value="paymentNote"
            rows="3"
            placeholder="Ghi chú thêm nếu cần"
            class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-red-300"
            @input="emit('update:paymentNote', $event.target.value)"
          />
        </div>
      </div>

      <div class="mt-8 grid gap-3 sm:grid-cols-2">
        <button
          type="button"
          class="rounded-2xl bg-slate-200 px-4 py-4 text-sm font-bold text-slate-700 transition hover:bg-slate-300 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
          :disabled="!canCreatePendingInvoice"
          @click="emit('create-pending-invoice')"
        >
          {{ savingPendingInvoice ? "Đang tạo..." : pendingInvoiceLimitReached ? "Đã đủ 5 hóa đơn chờ" : "Tạo hóa đơn chờ" }}
        </button>
        <button
          type="button"
          class="rounded-2xl bg-red-500 px-4 py-4 text-sm font-bold text-white shadow-[0_20px_40px_rgba(239,68,68,0.35)] transition hover:bg-red-600 disabled:cursor-not-allowed disabled:bg-slate-300 disabled:shadow-none"
          :disabled="!canPay"
          @click="emit('pay-now')"
        >
          {{ payingInvoice ? "Đang thanh toán..." : "Thanh toán" }}
        </button>
      </div>
      <button
        v-if="activePendingInvoice"
        type="button"
        class="mt-3 w-full rounded-2xl border border-red-200 bg-white px-4 py-3 text-sm font-semibold text-red-600 transition hover:bg-red-50 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-400"
        :disabled="cancelingPendingInvoice"
        @click="emit('cancel-pending-invoice')"
      >
        {{ cancelingPendingInvoice ? "Đang hủy hóa đơn chờ..." : "Hủy hóa đơn chờ" }}
      </button>
    </div>
  </aside>
</template>
