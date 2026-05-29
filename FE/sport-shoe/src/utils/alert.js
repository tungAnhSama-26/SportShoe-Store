import Swal from 'sweetalert2';

// Cấu hình class chung cho các loại popup (Toast/Confirm)
const customSwal = Swal.mixin({
  customClass: {
    popup: 'rounded-[32px] p-4 shadow-xl border-0',
    title: 'text-xl font-bold text-slate-800',
    htmlContainer: 'text-sm text-slate-500 font-medium',
    confirmButton: 'bg-[#009262] text-white px-8 py-2.5 rounded-full font-semibold hover:bg-emerald-700 transition focus:ring-4 focus:ring-emerald-100',
    cancelButton: 'bg-slate-100 text-slate-700 px-8 py-2.5 rounded-full font-semibold hover:bg-slate-200 transition focus:ring-4 focus:ring-slate-200',
    actions: 'gap-4 mt-6',
    icon: 'border-0'
  },
  buttonsStyling: false,
});

/**
 * Hiển thị thông báo thành công
 */
export function showSuccess(text, title = 'Thành công!') {
  return customSwal.fire({
    title,
    text,
    icon: 'success',
    showConfirmButton: false,
    timer: 2000,
    timerProgressBar: false
  });
}

/**
 * Hiển thị thông báo lỗi
 */
export function showError(text, title = 'Thất bại!') {
  return customSwal.fire({
    title,
    text,
    icon: 'error',
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: false
  });
}

/**
 * Hiển thị popup xác nhận
 * @returns Promise<boolean> - trả về true nếu chọn Đồng ý, ngược lại false
 */
export async function showConfirm(text, title = 'Xác nhận', confirmText = 'Đồng ý', cancelText = 'Hủy') {
  const result = await customSwal.fire({
    title,
    html: text,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: confirmText,
    cancelButtonText: cancelText,
    reverseButtons: true, // Hủy bên trái, Đồng ý bên phải
    focusConfirm: false
  });
  
  return result.isConfirmed;
}
