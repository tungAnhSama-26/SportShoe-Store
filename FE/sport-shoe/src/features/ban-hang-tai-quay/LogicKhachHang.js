import { computed, ref, watch } from "vue";
import { showError } from "../../utils/alert";
import { timKhachHangTheoSoDienThoai } from "../../services/ban-hang-tai-quay";
import {KHACH_VANG_LAI,AN_THONG_TIN,CHUA_CHON_KHACH,CHON_KHACH_HOAC_VANG_LAI} from "./HangSo";

export function LogicKhachHang({
  hoaDonChoDaChon,
  tenNguoiNhanGiaoHang,
  sdtNguoiNhanGiaoHang,
  diaChiGiaoHang,
  danhDauCanApDungLaiPhieu,
  xoaPhanHoi
}) {
  const tuKhoaKhachHang = ref("");
  const ketQuaTimKiemKhachHang = ref([]);
  const khachHangDuocChon = ref(null);
  const dangTaiKhachHang = ref(false);
  const hienThiDanhSachKhachHang = ref(false);

  let boDemThoiGianKhachHang;

  const laKhachVangLai = computed(
    () => tuKhoaKhachHang.value.trim().toLowerCase() === KHACH_VANG_LAI.toLowerCase()
  );
  const tenKhachHangHienThi = computed(() => {
    if (khachHangDuocChon.value) {
      return khachHangDuocChon.value.hoTen;
    }
    if (laKhachVangLai.value) {
      return KHACH_VANG_LAI;
    }
    return hoaDonChoDaChon.value?.tenKhachHang || CHUA_CHON_KHACH;
  });
  const soDienThoaiKhachHangHienThi = computed(() => {
    if (khachHangDuocChon.value) {
      return khachHangDuocChon.value.sdt;
    }
    if (laKhachVangLai.value) {
      return AN_THONG_TIN;
    }
    return hoaDonChoDaChon.value?.soDienThoai || CHON_KHACH_HOAC_VANG_LAI;
  });

  async function timKiemKhachHang(keyword) {
    if (!keyword.trim() || keyword.trim().toLowerCase() === KHACH_VANG_LAI.toLowerCase()) {
      ketQuaTimKiemKhachHang.value = [];
      return;
    }
    dangTaiKhachHang.value = true;
    try {
      ketQuaTimKiemKhachHang.value = await timKhachHangTheoSoDienThoai(keyword);
    } catch (error) {
      showError(error instanceof Error ? error.message : "Không thể tìm khách hàng");
    } finally {
      dangTaiKhachHang.value = false;
    }
  }

  function chonKhachHang(customer) {
    khachHangDuocChon.value = customer;
    tuKhoaKhachHang.value = customer.hoTen;
    if (!tenNguoiNhanGiaoHang.value.trim()) {
      tenNguoiNhanGiaoHang.value = customer.hoTen || "";
    }
    if (!sdtNguoiNhanGiaoHang.value.trim()) {
      sdtNguoiNhanGiaoHang.value = customer.sdt || "";
    }
    if (diaChiGiaoHang && (!diaChiGiaoHang.value || (typeof diaChiGiaoHang.value === 'string' && !diaChiGiaoHang.value.trim())) && customer.diaChiMacDinh) {
      diaChiGiaoHang.value = customer.diaChiMacDinh;
    }
    ketQuaTimKiemKhachHang.value = [];
    hienThiDanhSachKhachHang.value = false;
    danhDauCanApDungLaiPhieu();
    xoaPhanHoi();
  }

  function boChonKhachHang() {
    khachHangDuocChon.value = null;
    tuKhoaKhachHang.value = "";
    if (tenNguoiNhanGiaoHang) tenNguoiNhanGiaoHang.value = "";
    if (sdtNguoiNhanGiaoHang) sdtNguoiNhanGiaoHang.value = "";
    if (diaChiGiaoHang) diaChiGiaoHang.value = "";
    ketQuaTimKiemKhachHang.value = [];
    hienThiDanhSachKhachHang.value = false;
    danhDauCanApDungLaiPhieu();
    xoaPhanHoi();
  }

  function chonKhachVangLai() {
    khachHangDuocChon.value = null;
    tuKhoaKhachHang.value = KHACH_VANG_LAI;
    if (!hoaDonChoDaChon.value) {
      tenNguoiNhanGiaoHang.value = "";
      sdtNguoiNhanGiaoHang.value = "";
    }
    ketQuaTimKiemKhachHang.value = [];
    hienThiDanhSachKhachHang.value = false;
    danhDauCanApDungLaiPhieu();
    xoaPhanHoi();
  }

  async function moDanhSachKhachHang() {
    const keyword = tuKhoaKhachHang.value.trim();
    if (keyword && keyword.toLowerCase() !== KHACH_VANG_LAI.toLowerCase()) {
      hienThiDanhSachKhachHang.value = true;
      await timKiemKhachHang(tuKhoaKhachHang.value);
      return;
    }
    hienThiDanhSachKhachHang.value = false;
  }

  function dongDanhSachKhachHang() {
    window.setTimeout(() => {
      hienThiDanhSachKhachHang.value = false;
    }, 250);
  }

  function xoaBoDemThoiGianKhachHang() {
    if (boDemThoiGianKhachHang) {
      window.clearTimeout(boDemThoiGianKhachHang);
    }
  }

  watch(tuKhoaKhachHang, (value) => {
    xoaBoDemThoiGianKhachHang();
    const keyword = value.trim().toLowerCase();
    if (khachHangDuocChon.value) {
      const tenKhachDangChon = khachHangDuocChon.value.hoTen?.trim().toLowerCase() ?? "";
      const soDienThoaiDangChon = khachHangDuocChon.value.sdt?.trim().toLowerCase() ?? "";
      if (keyword !== tenKhachDangChon && keyword !== soDienThoaiDangChon) {
        khachHangDuocChon.value = null;
        danhDauCanApDungLaiPhieu();
      }
    }
    hienThiDanhSachKhachHang.value = value.trim().length > 0 && keyword !== KHACH_VANG_LAI.toLowerCase();
    boDemThoiGianKhachHang = window.setTimeout(() => {
      void timKiemKhachHang(value);
    }, 250);
  });

  return {
    tuKhoaKhachHang,
    ketQuaTimKiemKhachHang,
    khachHangDuocChon,
    dangTaiKhachHang,
    hienThiDanhSachKhachHang,
    laKhachVangLai,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi,
    timKiemKhachHang,
    chonKhachHang,
    boChonKhachHang,
    chonKhachVangLai,
    moDanhSachKhachHang,
    dongDanhSachKhachHang,
    xoaBoDemThoiGianKhachHang
  };
}
