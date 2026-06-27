import { computed, watch } from "vue";
import { tinhPhiVanChuyenTaiQuay } from "../../services/ban-hang-tai-quay";
import { showError } from "../../utils/alert";

export function LogicGiaoHang({
  choPhepGiaoHang,
  tenNguoiNhanGiaoHang,
  sdtNguoiNhanGiaoHang,
  diaChiGiaoHang,
  donViVanChuyen,
  phiVanChuyen,
  diaChiDaXacNhan,
  daTinhPhiVanChuyen,
  dangTinhPhiVanChuyen,
  cauHinhGiaoHang,
  khachHangDuocChon,
  hoaDonChoDaChon,
  cartItems
}) {
  const tenNguoiNhanGiaoHangHienThi = computed(() => tenNguoiNhanGiaoHang.value ?? "");
  const soDienThoaiNguoiNhanGiaoHangHienThi = computed(() => sdtNguoiNhanGiaoHang.value ?? "");
  const phiVanChuyenHienThi = computed(() => choPhepGiaoHang.value ? phiVanChuyen.value : 0);
  const diaChiGiaoHangHienThi = computed(() => diaChiGiaoHang.value ?? "");
  const coTheTinhPhiVanChuyen = computed(
    () => choPhepGiaoHang.value &&
      cartItems.value.length > 0 &&
      Boolean(diaChiGiaoHangHienThi.value.trim()) &&
      !dangTinhPhiVanChuyen.value
  );
  const coThongTinGiaoHangHopLe = computed(
    () => !choPhepGiaoHang.value ||
      (
        Boolean(tenNguoiNhanGiaoHangHienThi.value) &&
        Boolean(soDienThoaiNguoiNhanGiaoHangHienThi.value) &&
        Boolean(diaChiGiaoHangHienThi.value.trim()) &&
        daTinhPhiVanChuyen.value
      )
  );
  const thongTinGiaoHang = computed(() => ({
    giaoHang: choPhepGiaoHang.value,
    tenNguoiNhan: tenNguoiNhanGiaoHangHienThi.value,
    soDienThoaiNguoiNhan: soDienThoaiNguoiNhanGiaoHangHienThi.value,
    diaChiGiaoHang: diaChiGiaoHangHienThi.value,
    donViVanChuyen: donViVanChuyen.value,
    phiVanChuyen: phiVanChuyen.value,
    diaChiDaDo: diaChiDaXacNhan.value,
    daTinhPhi: daTinhPhiVanChuyen.value,
    dangTinhPhi: dangTinhPhiVanChuyen.value,
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
    phiVanChuyen.value = 0;
    diaChiDaXacNhan.value = "";
    daTinhPhiVanChuyen.value = false;
  }

  function taoPayloadGiaoHang() {
    if (!choPhepGiaoHang.value) {
      return {
        giaoHang: false,
        tenNguoiNhan: null,
        soDienThoaiNguoiNhan: null,
        diaChiGiaoHang: null,
        phiVanChuyen: 0,
        donViVanChuyen: null
      };
    }

    return {
      giaoHang: true,
      tenNguoiNhan: tenNguoiNhanGiaoHangHienThi.value,
      soDienThoaiNguoiNhan: soDienThoaiNguoiNhanGiaoHangHienThi.value,
      diaChiGiaoHang: diaChiGiaoHangHienThi.value.trim(),
      phiVanChuyen: phiVanChuyen.value,
      donViVanChuyen: donViVanChuyen.value || "GHN"
    };
  }

  function capNhatThongTinGiaoHang(patch) {
    const canTinhLai = [
      "diaChiGiaoHang",
      "serviceTypeId",
      "length",
      "width",
      "height",
      "weight"
    ].some((key) => Object.prototype.hasOwnProperty.call(patch, key));

    if (Object.prototype.hasOwnProperty.call(patch, "giaoHang")) {
      choPhepGiaoHang.value = Boolean(patch.giaoHang);
    }
    if (Object.prototype.hasOwnProperty.call(patch, "tenNguoiNhan")) {
      tenNguoiNhanGiaoHang.value = patch.tenNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "soDienThoaiNguoiNhan")) {
      sdtNguoiNhanGiaoHang.value = patch.soDienThoaiNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "diaChiGiaoHang")) {
      diaChiGiaoHang.value = patch.diaChiGiaoHang ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "serviceTypeId")) {
      cauHinhGiaoHang.value = {
        ...cauHinhGiaoHang.value,
        serviceTypeId: Number(patch.serviceTypeId) || 2
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "length")) {
      cauHinhGiaoHang.value = {
        ...cauHinhGiaoHang.value,
        length: Number(patch.length) || 30
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "width")) {
      cauHinhGiaoHang.value = {
        ...cauHinhGiaoHang.value,
        width: Number(patch.width) || 20
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "height")) {
      cauHinhGiaoHang.value = {
        ...cauHinhGiaoHang.value,
        height: Number(patch.height) || 12
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "weight")) {
      cauHinhGiaoHang.value = {
        ...cauHinhGiaoHang.value,
        weight: Number(patch.weight) || 500
      };
    }

    if (!choPhepGiaoHang.value) {
      phiVanChuyen.value = 0;
      diaChiDaXacNhan.value = "";
      daTinhPhiVanChuyen.value = false;
      return;
    }

    if (canTinhLai) {
      danhDauCanTinhLaiPhiVanChuyen();
    }
  }

  async function xuLyTinhPhiVanChuyen() {
    if (!coTheTinhPhiVanChuyen.value) {
      if (!choPhepGiaoHang.value || !diaChiGiaoHangHienThi.value.trim()) {
        phiVanChuyen.value = 0;
        donViVanChuyen.value = "";
        daTinhPhiVanChuyen.value = true;
        dangTinhPhiVanChuyen.value = false;
        return;
      }
      return;
    }
    dangTinhPhiVanChuyen.value = true;
    try {
      const items = cartItems.value.map(item => ({
        chiTietId: item.chiTietId,
        soLuong: item.soLuong
      }));
      const result = await tinhPhiVanChuyenTaiQuay({
        toAddress: diaChiGiaoHangHienThi.value,
        serviceTypeId: cauHinhGiaoHang.value.serviceTypeId,
        length: cauHinhGiaoHang.value.length,
        width: cauHinhGiaoHang.value.width,
        height: cauHinhGiaoHang.value.height,
        weight: cauHinhGiaoHang.value.weight,
        items: items
      });
      phiVanChuyen.value = result.phiVanChuyen;
      diaChiDaXacNhan.value = result.diaChiDaDo || "";
      daTinhPhiVanChuyen.value = true;
    } catch (error) {
      phiVanChuyen.value = 0;
      diaChiDaXacNhan.value = "";
      daTinhPhiVanChuyen.value = false;
      showError(error instanceof Error ? error.message : "Không thể tính phí vận chuyển");
    } finally {
      dangTinhPhiVanChuyen.value = false;
    }
  }

  let phiVanChuyenTimeout = null;
  watch(
    () => [
      diaChiGiaoHangHienThi.value,
      cartItems.value,
      choPhepGiaoHang.value,
      cauHinhGiaoHang.value
    ],
    () => {
      if (coTheTinhPhiVanChuyen.value) {
        if (phiVanChuyenTimeout) clearTimeout(phiVanChuyenTimeout);
        phiVanChuyenTimeout = setTimeout(() => {
          xuLyTinhPhiVanChuyen().catch(() => {});
        }, 800);
      }
    },
    { deep: true }
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
