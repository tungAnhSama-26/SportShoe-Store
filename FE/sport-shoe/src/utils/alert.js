import Swal from 'sweetalert2';

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
      background-color: #ffffff !important;
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
  `;
  document.head.appendChild(style);
}

const toastSwal = Swal.mixin({
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
  ensureAlertStyles();

  return Swal.fire({
    icon: 'success',
    title: normalizeSuccessTitle(title),
    text: text || undefined,
    toast: false,
    position: 'center',
    showConfirmButton: false,
    timer: 1400,
    timerProgressBar: true,
    background: '#ffffff',
    color: '#334155',
    iconColor: BRAND_RED,
    customClass: {
      popup: 'sportshoe-success-popup',
      title: 'sportshoe-success-title',
      htmlContainer: 'sportshoe-success-text',
      timerProgressBar: 'sportshoe-success-progress',
    },
  });
}

export function showError(text = '', title = 'Thất bại!') {
  return toastSwal.fire({
    icon: 'error',
    title,
    text: text || undefined,
    timer: 3500,
    iconColor: BRAND_RED,
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
  });

  return result.isConfirmed;
}
