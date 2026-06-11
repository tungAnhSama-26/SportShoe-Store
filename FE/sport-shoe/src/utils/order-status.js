export const CAC_BUOC_DON_HANG = Object.freeze([
  {
    ten: 'Chờ xác nhận',
    icon: 'M12 8v4l3 3 M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z',
  },
  {
    ten: 'Đã xác nhận',
    icon: 'M9 11l3 3L22 4 M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11',
  },
  {
    ten: 'Chờ lấy hàng',
    icon: 'M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.3 7 12 12l8.7-5 M12 22V12',
  },
  {
    ten: 'Đang giao hàng',
    icon: 'M1 3h15v13H1z M16 8h4l3 3v5h-7V8z M5.5 18.5a2.5 2.5 0 1 0 0 .01 M18.5 18.5a2.5 2.5 0 1 0 0 .01',
  },
  {
    ten: 'Đã giao hàng',
    icon: 'M20 6 9 17l-5-5',
  },
  {
    ten: 'Hoàn thành',
    icon: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M8 12l3 3 5-6',
  },
]);

const VI_TRI_TIEN_TRINH = Object.freeze({
  1: 1,
  9: 2,
  2: 3,
  3: 4,
  4: 5,
  5: 6,
});

const TRANG_THAI_DAC_BIET = Object.freeze({
  6: {
    tieuDe: 'Đơn hàng đã hủy',
    moTa: 'Đơn hàng này đã được hủy.',
    lopMau: 'bg-rose-50 text-rose-600',
  },
  7: {
    tieuDe: 'Yêu cầu hủy đang chờ xử lý',
    moTa: 'Cửa hàng đang xem xét yêu cầu hủy đơn của bạn.',
    lopMau: 'bg-amber-50 text-amber-700',
  },
  10: {
    tieuDe: 'Giao hàng thất bại',
    moTa: 'Đơn hàng chưa thể giao thành công. Cửa hàng sẽ liên hệ để hỗ trợ.',
    lopMau: 'bg-rose-50 text-rose-600',
  },
});

export function layViTriTienTrinhDonHang(trangThai) {
  return VI_TRI_TIEN_TRINH[Number(trangThai)] ?? 0;
}

export function layCauHinhTrangThaiDonHang(trangThai) {
  const maTrangThai = Number(trangThai);
  const dacBiet = TRANG_THAI_DAC_BIET[maTrangThai];
  if (dacBiet) {
    return { ...dacBiet, hienStepper: false };
  }
  if (layViTriTienTrinhDonHang(maTrangThai) > 0) {
    return {
      hienStepper: true,
      tieuDe: '',
      moTa: '',
      lopMau: '',
    };
  }
  return {
    hienStepper: false,
    tieuDe: 'Trạng thái đơn hàng chưa xác định',
    moTa: 'Vui lòng tải lại trang hoặc liên hệ cửa hàng để được hỗ trợ.',
    lopMau: 'bg-slate-100 text-slate-600',
  };
}
