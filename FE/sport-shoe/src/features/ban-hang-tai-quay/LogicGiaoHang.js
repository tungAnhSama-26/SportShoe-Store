import { computed, ref, watch } from "vue";
import { tinhPhiVanChuyenTaiQuay } from "../../services/ban-hang-tai-quay";
import { showError } from "../../utils/alert";
import { chuanHoaDiaChi, diaChiHopLe, dinhDangDiaChi } from "../../utils/dia-chi";

function layDiaChiDungDeTinhPhi(value) {
  const diaChi = chuanHoaDiaChi(value);
  return {
    tinhThanh: diaChi.tinhThanh,
    phuongXa: diaChi.phuongXa,
    diaChiCuThe: diaChi.diaChiCuThe
  };
}

export function LogicGiaoHang({
  choPhepGiaoHang,
  tenNguoiNhanGiaoHang,
  sdtNguoiNhanGiaoHang,
  emailNguoiNhanGiaoHang = ref(""),
  diaChiGiaoHang,
  donViVanChuyen,
  phiVanChuyen,
  diaChiDaXacNhan,
  daTinhPhiVanChuyen,
  dangTinhPhiVanChuyen,
  cauHinhGiaoHang,
  khachHangDuocChon,
  tuKhoaKhachHang,
  hoaDonChoDaChon,
  cartItems
}) {
  const nguonTinhPhi = ref("");
  const moTaPhi = ref("");
  const tenNguoiNhanGiaoHangHienThi = computed(() => tenNguoiNhanGiaoHang.value ?? "");
  const soDienThoaiNguoiNhanGiaoHangHienThi = computed(() => sdtNguoiNhanGiaoHang.value ?? "");
  const emailNguoiNhanGiaoHangHienThi = computed(() => emailNguoiNhanGiaoHang.value || khachHangDuocChon.value?.email || "");
  const phiVanChuyenHienThi = computed(() => choPhepGiaoHang.value ? phiVanChuyen.value : 0);
  const diaChiGiaoHangHienThi = computed(() => {
    if (diaChiHopLe(diaChiGiaoHang.value)) {
      return chuanHoaDiaChi(diaChiGiaoHang.value);
    }
    if (khachHangDuocChon.value?.diaChiMacDinh) {
      return khachHangDuocChon.value.diaChiMacDinh;
    }
    return null;
  });
  const coTheTinhPhiVanChuyen = computed(
    () => choPhepGiaoHang.value &&
      cartItems.value.length > 0 &&
      diaChiHopLe(diaChiGiaoHangHienThi.value) &&
      !dangTinhPhiVanChuyen.value
  );
  const coThongTinGiaoHangHopLe = computed(
    () => !choPhepGiaoHang.value ||
      (
        Boolean(tenNguoiNhanGiaoHangHienThi.value) &&
        Boolean(soDienThoaiNguoiNhanGiaoHangHienThi.value) &&
        diaChiHopLe(diaChiGiaoHangHienThi.value) &&
        daTinhPhiVanChuyen.value
      )
  );
  const thongTinGiaoHang = computed(() => ({
    giaoHang: choPhepGiaoHang.value,
    tenNguoiNhan: tenNguoiNhanGiaoHangHienThi.value,
    soDienThoaiNguoiNhan: soDienThoaiNguoiNhanGiaoHangHienThi.value,
    email: emailNguoiNhanGiaoHangHienThi.value,
    diaChiGiaoHang: diaChiGiaoHangHienThi.value,
    donViVanChuyen: donViVanChuyen.value,
    phiVanChuyen: phiVanChuyen.value,
    diaChiDaDo: diaChiDaXacNhan.value,
    daTinhPhi: daTinhPhiVanChuyen.value,
    dangTinhPhi: dangTinhPhiVanChuyen.value,
    nguonTinhPhi: nguonTinhPhi.value,
    moTaPhi: moTaPhi.value,
    coTheTinhPhi: coTheTinhPhiVanChuyen.value,
    serviceTypeId: cauHinhGiaoHang.value.serviceTypeId,
    length: cauHinhGiaoHang.value.length,
    width: cauHinhGiaoHang.value.width,
    height: cauHinhGiaoHang.value.height,
    weight: cauHinhGiaoHang.value.weight
  }));

  function danhDauCanTinhLaiPhiVanChuyen() {
    if (!choPhepGiaoHang.value) {
      return;
    }
    daTinhPhiVanChuyen.value = false;
    phiVanChuyen.value = 0;
    diaChiDaXacNhan.value = "";
    nguonTinhPhi.value = "";
    moTaPhi.value = "";
  }

  function taoPayloadGiaoHang() {
    if (!choPhepGiaoHang.value) {
      return {
        giaoHang: false,
        tenNguoiNhan: null,
        soDienThoaiNguoiNhan: null,
        email: null,
        diaChiGiaoHang: null,
        phiVanChuyen: 0,
        donViVanChuyen: null
      };
    }

    return {
      giaoHang: true,
      tenNguoiNhan: tenNguoiNhanGiaoHangHienThi.value || null,
      soDienThoaiNguoiNhan: soDienThoaiNguoiNhanGiaoHangHienThi.value || null,
      email: emailNguoiNhanGiaoHangHienThi.value || null,
      diaChiGiaoHang: diaChiHopLe(diaChiGiaoHangHienThi.value) ? chuanHoaDiaChi(diaChiGiaoHangHienThi.value) : null,
      phiVanChuyen: phiVanChuyen.value,
      donViVanChuyen: donViVanChuyen.value || "GHN"
    };
  }

  function capNhatThongTinGiaoHang(patch) {
    const khoaTinhPhiTruocKhiCapNhat = taoKhoaTinhPhiVanChuyen();

    if (Object.prototype.hasOwnProperty.call(patch, "giaoHang")) {
      choPhepGiaoHang.value = Boolean(patch.giaoHang);
      if (choPhepGiaoHang.value) {
        if (!diaChiHopLe(diaChiGiaoHang.value) && khachHangDuocChon.value?.diaChiMacDinh) {
          diaChiGiaoHang.value = khachHangDuocChon.value.diaChiMacDinh;
        }
        // Tự điền tên và SDT người nhận từ khách hàng đã chọn nếu đang trống
        if (!tenNguoiNhanGiaoHang.value?.trim()) {
          if (khachHangDuocChon.value?.hoTen) {
            tenNguoiNhanGiaoHang.value = khachHangDuocChon.value.hoTen;
          } else if (tuKhoaKhachHang?.value?.trim() && tuKhoaKhachHang.value.trim() !== "Khách lẻ" && tuKhoaKhachHang.value.trim() !== "Khách vãng lai") {
            const raw = tuKhoaKhachHang.value.trim();
            if (!/^[0-9+ ]{8,15}$/.test(raw)) {
              tenNguoiNhanGiaoHang.value = raw;
            }
          }
        }
        if (!sdtNguoiNhanGiaoHang.value?.trim()) {
          if (khachHangDuocChon.value?.sdt) {
            sdtNguoiNhanGiaoHang.value = khachHangDuocChon.value.sdt;
          } else if (tuKhoaKhachHang?.value?.trim()) {
            const raw = tuKhoaKhachHang.value.trim();
            if (/^[0-9+ ]{8,15}$/.test(raw)) {
              sdtNguoiNhanGiaoHang.value = raw;
            }
          }
        }
        if (!emailNguoiNhanGiaoHang.value?.trim() && khachHangDuocChon.value?.email) {
          emailNguoiNhanGiaoHang.value = khachHangDuocChon.value.email;
        }
      }
    }
    if (Object.prototype.hasOwnProperty.call(patch, "tenNguoiNhan")) {
      tenNguoiNhanGiaoHang.value = patch.tenNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "soDienThoaiNguoiNhan")) {
      sdtNguoiNhanGiaoHang.value = patch.soDienThoaiNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "email")) {
      emailNguoiNhanGiaoHang.value = patch.email ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "diaChiGiaoHang")) {
      const newDiaChi = chuanHoaDiaChi(patch.diaChiGiaoHang);
      if (JSON.stringify(diaChiGiaoHang.value) !== JSON.stringify(newDiaChi)) {
        diaChiGiaoHang.value = newDiaChi;
      }
    }
    if (Object.prototype.hasOwnProperty.call(patch, "serviceTypeId")) {
      const newVal = Number(patch.serviceTypeId) || 2;
      if (cauHinhGiaoHang.value.serviceTypeId !== newVal) {
        cauHinhGiaoHang.value = { ...cauHinhGiaoHang.value, serviceTypeId: newVal };
      }
    }
    if (Object.prototype.hasOwnProperty.call(patch, "length")) {
      const newVal = Number(patch.length) || 30;
      if (cauHinhGiaoHang.value.length !== newVal) {
        cauHinhGiaoHang.value = { ...cauHinhGiaoHang.value, length: newVal };
      }
    }
    if (Object.prototype.hasOwnProperty.call(patch, "width")) {
      const newVal = Number(patch.width) || 20;
      if (cauHinhGiaoHang.value.width !== newVal) {
        cauHinhGiaoHang.value = { ...cauHinhGiaoHang.value, width: newVal };
      }
    }
    if (Object.prototype.hasOwnProperty.call(patch, "height")) {
      const newVal = Number(patch.height) || 12;
      if (cauHinhGiaoHang.value.height !== newVal) {
        cauHinhGiaoHang.value = { ...cauHinhGiaoHang.value, height: newVal };
      }
    }
    if (Object.prototype.hasOwnProperty.call(patch, "weight")) {
      const newVal = Number(patch.weight) || 500;
      if (cauHinhGiaoHang.value.weight !== newVal) {
        cauHinhGiaoHang.value = { ...cauHinhGiaoHang.value, weight: newVal };
      }
    }
    if (Object.prototype.hasOwnProperty.call(patch, "phiVanChuyen")) {
      phiVanChuyen.value = Number(patch.phiVanChuyen) || 0;
      daTinhPhiVanChuyen.value = true;
      nguonTinhPhi.value = "MANUAL";
      moTaPhi.value = "";
    }

    if (!choPhepGiaoHang.value) {
      phiVanChuyen.value = 0;
      diaChiDaXacNhan.value = "";
      daTinhPhiVanChuyen.value = false;
      nguonTinhPhi.value = "";
      moTaPhi.value = "";
      return;
    }

    if (khoaTinhPhiTruocKhiCapNhat !== taoKhoaTinhPhiVanChuyen()) {
      danhDauCanTinhLaiPhiVanChuyen();
    }
  }

  let khoaYeuCauPhiDangChay = "";
  let khoaYeuCauPhiGanNhat = "";

  function taoKhoaTinhPhiVanChuyen() {
    return JSON.stringify({
      diaChi: layDiaChiDungDeTinhPhi(diaChiGiaoHangHienThi.value),
      items: cartItems.value
        .map(item => ({ id: String(item.chiTietId), sl: Number(item.soLuong) }))
        .sort((a, b) => a.id.localeCompare(b.id)),
      giaoHang: Boolean(choPhepGiaoHang.value),
      cauHinh: {
        serviceTypeId: Number(cauHinhGiaoHang.value.serviceTypeId),
        length: Number(cauHinhGiaoHang.value.length),
        width: Number(cauHinhGiaoHang.value.width),
        height: Number(cauHinhGiaoHang.value.height),
        weight: Number(cauHinhGiaoHang.value.weight)
      }
    });
  }

  async function xuLyTinhPhiVanChuyen(options = {}) {
    const force = Boolean(options?.force);
    const khoaYeuCau = taoKhoaTinhPhiVanChuyen();
    if (khoaYeuCauPhiDangChay || (!force && khoaYeuCau === khoaYeuCauPhiGanNhat)) {
      return;
    }
    if (!coTheTinhPhiVanChuyen.value) {
      if (!choPhepGiaoHang.value || !diaChiHopLe(diaChiGiaoHangHienThi.value)) {
        phiVanChuyen.value = 0;
        donViVanChuyen.value = "";
        daTinhPhiVanChuyen.value = true;
        dangTinhPhiVanChuyen.value = false;
        nguonTinhPhi.value = "";
        moTaPhi.value = "";
        return;
      }
      return;
    }
    khoaYeuCauPhiDangChay = khoaYeuCau;
    khoaYeuCauPhiGanNhat = khoaYeuCau;
    dangTinhPhiVanChuyen.value = true;
    try {
      const items = cartItems.value.map(item => ({
        chiTietId: item.chiTietId,
        soLuong: item.soLuong
      }));
      const result = await tinhPhiVanChuyenTaiQuay({
        diaChiGiaoHang: chuanHoaDiaChi(diaChiGiaoHangHienThi.value),
        serviceTypeId: cauHinhGiaoHang.value.serviceTypeId,
        length: cauHinhGiaoHang.value.length,
        width: cauHinhGiaoHang.value.width,
        height: cauHinhGiaoHang.value.height,
        weight: cauHinhGiaoHang.value.weight,
        items: items
      });
      phiVanChuyen.value = result.phiVanChuyen;
      diaChiDaXacNhan.value = dinhDangDiaChi(result.diaChiDaDoiSoat);
      nguonTinhPhi.value = result.nguonTinhPhi || "GHN_LIVE";
      moTaPhi.value = result.nguonTinhPhi === "GHN_CACHE"
        ? (result.giaCu ? "Phí GHN từ cache cũ (ước tính)" : "Phí GHN đã lưu gần nhất (ước tính)")
        : result.nguonTinhPhi === "GHN_PUBLIC_TARIFF"
          ? "Phí offline ước tính theo bảng giá công khai GHN"
          : (result.uocTinh ? "Phí GHN ước tính theo các tuyến cũ" : "Phí GHN");
      daTinhPhiVanChuyen.value = true;
    } catch (error) {
      phiVanChuyen.value = 0;
      diaChiDaXacNhan.value = "";
      daTinhPhiVanChuyen.value = false;
      nguonTinhPhi.value = "";
      moTaPhi.value = "";
      showError(error instanceof Error ? error.message : "Không thể tính phí vận chuyển");
    } finally {
      khoaYeuCauPhiDangChay = "";
      dangTinhPhiVanChuyen.value = false;
    }
  }

  const dependenciesTinhPhi = computed(taoKhoaTinhPhiVanChuyen);

  let phiVanChuyenTimeout = null;
  watch(
    dependenciesTinhPhi,
    (newVal, oldVal) => {
      if (newVal === oldVal) return;
      if (!choPhepGiaoHang.value || !cartItems.value.length || !diaChiHopLe(diaChiGiaoHangHienThi.value)) {
        khoaYeuCauPhiGanNhat = "";
        if (phiVanChuyenTimeout) clearTimeout(phiVanChuyenTimeout);
        phiVanChuyenTimeout = null;
        return;
      }
      if (daTinhPhiVanChuyen.value) {
        if (phiVanChuyenTimeout) clearTimeout(phiVanChuyenTimeout);
        phiVanChuyenTimeout = null;
        return;
      }
      if (newVal === khoaYeuCauPhiGanNhat || newVal === khoaYeuCauPhiDangChay) return;
      if (phiVanChuyenTimeout) clearTimeout(phiVanChuyenTimeout);
      phiVanChuyenTimeout = setTimeout(() => {
        phiVanChuyenTimeout = null;
        if (newVal !== dependenciesTinhPhi.value) return;
        xuLyTinhPhiVanChuyen().catch(() => {});
      }, 800);
    }
  );

  return {
    tenNguoiNhanGiaoHangHienThi,
    soDienThoaiNguoiNhanGiaoHangHienThi,
    phiVanChuyenHienThi,
    coTheTinhPhiVanChuyen,
    coThongTinGiaoHangHopLe,
    thongTinGiaoHang,
    danhDauCanTinhLaiPhiVanChuyen,
    taoPayloadGiaoHang,
    capNhatThongTinGiaoHang,
    xuLyTinhPhiVanChuyen
  };
}
