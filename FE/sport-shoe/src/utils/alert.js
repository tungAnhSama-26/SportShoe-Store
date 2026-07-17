import Swal from 'sweetalert2';
import { dinhDangTienViet } from './dinhDangTien';

const BRAND_RED = '#cf1018';
const BRAND_RED_DARK = '#a90d14';

const ALERT_STYLE_ID = 'sportshoe-alert-style';

function ensureAlertStyles() {
  if (typeof document === 'undefined' || document.getElementById(ALERT_STYLE_ID)) {
    return;
  }

  const style = document.createElement('style');
  style.id = ALERT_STYLE_ID;
  style.textContent = `
    @keyframes sportshoeFadeIn {
      from {
        opacity: 0;
        transform: scale(0.92);
      }
      to {
        opacity: 1;
        transform: scale(1);
      }
    }
    
    @keyframes sportshoeFadeOut {
      from {
        opacity: 1;
        transform: scale(1);
      }
      to {
        opacity: 0;
        transform: scale(0.95);
      }
    }

    @keyframes sportshoeIconScale {
      from { transform: scale(0.4); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }

    .sportshoe-popup-show {
      animation: sportshoeFadeIn 0.25s cubic-bezier(0.34, 1.56, 0.64, 1) forwards !important;
    }

    .sportshoe-popup-hide {
      animation: sportshoeFadeOut 0.15s ease-out forwards !important;
    }

    .sportshoe-success-popup {
      width: min(430px, calc(100vw - 32px)) !important;
      padding: 34px 34px 28px !important;
      border-radius: 24px !important;
      border: 1px solid rgba(207, 16, 24, 0.16) !important;
      box-shadow: 0 24px 80px rgba(15, 23, 42, 0.22) !important;
    }

    .sportshoe-success-popup .swal2-icon {
      margin: 0 auto 18px !important;
      width: 82px !important;
      height: 82px !important;
      animation: sportshoeIconScale 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) both !important;
    }

    .sportshoe-success-popup .swal2-success-ring {
      border-color: rgba(207, 16, 24, 0.72) !important;
    }

    .sportshoe-success-popup .swal2-success-line-tip,
    .sportshoe-success-popup .swal2-success-line-long {
      background-color: ${BRAND_RED} !important;
    }

    .sportshoe-success-popup .swal2-success-circular-line-left,
    .sportshoe-success-popup .swal2-success-circular-line-right,
    .sportshoe-success-popup .swal2-success-fix {
      display: none !important;
    }

    .sportshoe-success-title {
      margin: 0 !important;
      color: ${BRAND_RED_DARK} !important;
      font-size: 28px !important;
      font-weight: 800 !important;
      line-height: 1.2 !important;
      letter-spacing: 0 !important;
    }

    .sportshoe-success-text {
      margin: 14px auto 0 !important;
      max-width: 330px !important;
      color: #475569 !important;
      font-size: 18px !important;
      font-weight: 500 !important;
      line-height: 1.45 !important;
    }

    .sportshoe-success-progress {
      background: linear-gradient(90deg, ${BRAND_RED}, #ff6a00) !important;
      height: 4px !important;
    }

    .sportshoe-error-progress {
      background: #cbd5e1 !important;
      height: 4px !important;
    }
  `;
  document.head.appendChild(style);
}

export const toastSwal = Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timerProgressBar: true,
  customClass: {
    popup: 'rounded-2xl shadow-xl border-0 pr-4',
    title: 'text-sm font-semibold text-slate-800',
    htmlContainer: 'text-xs text-slate-500',
    timerProgressBar: 'bg-rose-400',
  },
});

const confirmSwal = Swal.mixin({
  customClass: {
    popup: 'rounded-[32px] p-4 shadow-xl border-0',
    title: 'text-xl font-bold text-slate-800',
    htmlContainer: 'text-sm text-slate-500 font-medium',
    confirmButton:
      'bg-[#cf1018] text-white px-8 py-2.5 rounded-full font-semibold hover:bg-rose-700 transition focus:ring-4 focus:ring-rose-100',
    cancelButton:
      'bg-slate-100 text-slate-700 px-8 py-2.5 rounded-full font-semibold hover:bg-slate-200 transition focus:ring-4 focus:ring-slate-200',
    actions: 'gap-4 mt-6',
    icon: 'border-0',
  },
  iconColor: BRAND_RED,
  buttonsStyling: false,
});

function normalizeSuccessTitle(title) {
  const value = String(title || '').trim();
  if (!value) return 'Thành công!';
  return value.toLowerCase() === 'thành công' ? 'Thành công!' : value;
}

export function showSuccess(text = '', title = 'Thành công!') {
  return toastSwal.fire({
    icon: 'success',
    title: normalizeSuccessTitle(title),
    text: text || undefined,
    timer: 2000,
    iconColor: BRAND_RED,
    target: document.getElementById('pos-tablet-screen') || 'body',
  });
}

export function showError(text = '', title = 'Thất bại!') {
  ensureAlertStyles();

  return Swal.fire({
    icon: 'error',
    title,
    text: text || undefined,
    toast: false,
    position: 'center',
    showConfirmButton: false,
    timer: 3500,
    timerProgressBar: true,
    background: '#ffffff',
    color: '#334155',
    iconColor: BRAND_RED,
    target: document.getElementById('pos-tablet-screen') || 'body',
    showClass: {
      popup: 'sportshoe-popup-show',
      backdrop: 'swal2-noanimation',
    },
    hideClass: {
      popup: 'sportshoe-popup-hide',
      backdrop: 'swal2-noanimation',
    },
    customClass: {
      popup: 'sportshoe-success-popup',
      title: 'sportshoe-success-title',
      htmlContainer: 'sportshoe-success-text',
      timerProgressBar: 'sportshoe-error-progress',
    },
  });
}

/** Popup thành công TO giữa màn hình (giống showError nhưng icon xanh) - dùng cho các mốc lớn như đặt hàng. */
export function showBigSuccess(text = '', title = 'Thành công!') {
  ensureAlertStyles();

  return Swal.fire({
    icon: 'success',
    title: normalizeSuccessTitle(title),
    html: text || undefined,
    toast: false,
    position: 'center',
    showConfirmButton: false,
    timer: 2600,
    timerProgressBar: true,
    background: '#ffffff',
    color: '#334155',
    iconColor: '#16a34a',
    target: document.getElementById('pos-tablet-screen') || 'body',
    showClass: {
      popup: 'sportshoe-popup-show',
      backdrop: 'swal2-noanimation',
    },
    hideClass: {
      popup: 'sportshoe-popup-hide',
      backdrop: 'swal2-noanimation',
    },
    customClass: {
      popup: 'sportshoe-success-popup',
      title: 'sportshoe-success-title',
      htmlContainer: 'sportshoe-success-text',
      timerProgressBar: 'sportshoe-success-progress',
    },
  });
}

export function showToastSuccess(text = '', title = 'Thành công!') {
  return toastSwal.fire({
    icon: 'success',
    title: normalizeSuccessTitle(title),
    text: text || undefined,
    timer: 2000,
    iconColor: BRAND_RED,
    target: document.getElementById('pos-tablet-screen') || 'body',
  });
}

export function showWarning(text = '', title = 'Thông báo') {
  return toastSwal.fire({
    icon: 'warning',
    title,
    text: text || undefined,
    timer: 4000,
    iconColor: '#f59e0b',
    target: document.getElementById('pos-tablet-screen') || 'body',
  });
}

export async function showConfirm(
  text,
  title = 'Xác nhận',
  confirmText = 'Đồng ý',
  cancelText = 'Hủy',
) {
  const result = await confirmSwal.fire({
    title,
    html: text,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: confirmText,
    cancelButtonText: cancelText,
    reverseButtons: true,
    focusConfirm: false,
    target: document.getElementById('pos-tablet-screen') || 'body',
  });

  return result.isConfirmed;
}

export async function showPaymentConfirmWithCoupon({
  oldCouponCode,
  newCouponCode,
  oldDiscount,
  newDiscount,
  tongTienHang
}) {
  const savings = newDiscount - oldDiscount;
  const finalTotal = tongTienHang - newDiscount;

  const html = `
    <div class="text-left font-sans text-slate-800">
      <div class="bg-red-50 border border-red-100 rounded-lg p-3 mb-5 flex items-center gap-2">
        <svg class="w-5 h-5 text-[#cf1018] flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
        <span class="text-[#a90d14] font-medium text-[15px]">Có voucher tốt hơn cho đơn hàng của bạn!</span>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-5">
        <div>
          <div class="text-sm text-slate-500 mb-1.5 font-medium">Voucher hiện tại</div>
          ${oldCouponCode ? `<div class="inline-block px-2.5 py-1 bg-blue-50 text-blue-600 border border-blue-100 rounded-md text-sm font-semibold mb-2">${oldCouponCode}</div>` : `<div class="text-sm text-slate-400 mb-2 italic">Không có</div>`}
          <div class="text-sm text-slate-600 flex items-center gap-1">Giảm: <span class="${oldDiscount > 0 ? 'line-through text-slate-400' : ''}">${dinhDangTienViet(oldDiscount)}</span></div>
        </div>
        <div>
          <div class="text-sm text-[#cf1018] mb-1.5 font-medium">Voucher mới</div>
          <div class="inline-block px-2.5 py-1 bg-red-50 text-[#cf1018] border border-red-100 rounded-md text-sm font-semibold mb-2">${newCouponCode}</div>
          <div class="text-sm text-[#cf1018] font-medium">Giảm: ${dinhDangTienViet(newDiscount)}</div>
        </div>
      </div>

      <div class="bg-red-50 rounded-lg p-3 mb-5 flex justify-between items-center">
        <span class="text-slate-700 font-semibold text-[15px]">Bạn tiết kiệm thêm:</span>
        <span class="text-[#cf1018] font-bold text-[15px]">+${dinhDangTienViet(savings)}</span>
      </div>

      <div class="border-t border-slate-100 pt-4 mb-2">
        <div class="flex justify-between items-center mb-2.5">
          <span class="text-slate-500 text-[15px]">Tổng tiền hàng:</span>
          <span class="text-slate-700 font-medium">${dinhDangTienViet(tongTienHang)}</span>
        </div>
        <div class="flex justify-between items-center mb-4">
          <span class="text-slate-500 text-[15px]">Giảm giá:</span>
          <span class="text-[#cf1018] font-medium">-${dinhDangTienViet(newDiscount)}</span>
        </div>
        <div class="flex justify-between items-center pt-3 border-t border-slate-100">
          <span class="text-slate-800 font-semibold text-[15px]">Tổng thanh toán:</span>
          <span class="text-[#cf1018] font-bold text-lg">${dinhDangTienViet(finalTotal)}</span>
        </div>
      </div>
    </div>
  `;

  const result = await Swal.fire({
    title: `<div class="flex items-center gap-2"><div class="w-7 h-7 rounded-full bg-[#cf1018] text-white flex items-center justify-center font-bold text-[15px]">i</div> <span class="text-lg">Có voucher tốt hơn</span></div>`,
    html: html,
    showCancelButton: true,
    showDenyButton: false,
    showCloseButton: true,
    confirmButtonText: 'Dùng voucher mới (tiết kiệm hơn)',
    cancelButtonText: oldCouponCode ? 'Giữ voucher cũ' : 'Không dùng voucher',
    customClass: {
      popup: 'rounded-xl p-5 shadow-2xl border-0 w-full max-w-[480px]',
      title: 'text-xl font-bold text-slate-800 m-0 text-left border-0 w-full flex items-center',
      htmlContainer: 'm-0 mt-5',
      confirmButton: `bg-[#cf1018] text-white px-4 py-2 rounded text-[15px] font-medium hover:bg-[#a90d14] transition order-2`,
      cancelButton: 'bg-white text-slate-600 border border-slate-200 px-4 py-2 rounded text-[15px] font-medium hover:bg-slate-50 transition order-1',
      actions: 'gap-3 mt-6 justify-end w-full flex-nowrap',
      closeButton: 'focus:outline-none mt-2 mr-2 bg-slate-100 hover:bg-slate-200 rounded text-slate-500 hover:text-slate-700 transition'
    },
    target: document.getElementById('pos-tablet-screen') || 'body',
    buttonsStyling: false
  });

  if (result.isConfirmed) return 'use_new';
  if (result.isDismissed && result.dismiss === Swal.DismissReason.cancel) return 'use_old';
  return 'cancel';
}
