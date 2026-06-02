import Swal from 'sweetalert2';

// Toast nhỏ ở góc phải - dùng cho success/error
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

// Popup xác nhận chính giữa - dùng cho confirm dialog
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
  iconColor: '#cf1018',
  buttonsStyling: false,
});

/**
 * Hiển thị toast thông báo thành công (góc phải trên)
 * @param {string} text - Nội dung thông báo
 * @param {string} title - Tiêu đề (mặc định: 'Thành công!')
 */
export function showSuccess(text = '', title = 'Thành công!') {
  return toastSwal.fire({
    icon: 'success',
    title: title,
    text: text || undefined,
    timer: 2500,
    iconColor: '#22c55e',
  });
}

/**
 * Hiển thị toast thông báo lỗi (góc phải trên)
 * @param {string} text - Nội dung thông báo
 * @param {string} title - Tiêu đề (mặc định: 'Thất bại!')
 */
export function showError(text = '', title = 'Thất bại!') {
  return toastSwal.fire({
    icon: 'error',
    title: title,
    text: text || undefined,
    timer: 3500,
    iconColor: '#cf1018',
  });
}

/**
 * Hiển thị popup xác nhận (modal chính giữa)
 * @param {string} text - Nội dung câu hỏi xác nhận
 * @param {string} title - Tiêu đề (mặc định: 'Xác nhận')
 * @param {string} confirmText - Nhãn nút đồng ý
 * @param {string} cancelText - Nhãn nút hủy
 * @returns {Promise<boolean>} - true nếu người dùng chọn Đồng ý
 */
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
