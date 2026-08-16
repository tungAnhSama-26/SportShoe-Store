import { computed, ref, watch } from "vue";
import { tinhPhiVanChuyenTaiQuay } from "../../services/ban-hang-tai-quay";
import { showError } from "../../utils/alert";
import { chuanHoaDiaChi, diaChiHopLe, dinhDangDiaChi } from "../../utils/dia-chi";

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
  const nguonTinhPhi = ref("");
  const moTaPhi = ref("");
  const tenNguoiNhanGiaoHangHienThi = computed(() => tenNguoiNhanGiaoHang.value ?? "");
  const soDienThoaiNguoiNhanGiaoHangHienThi = computed(() => sdtNguoiNhanGiaoHang.value ?? "");
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
    phiVanChuyen.value = 0;
    diaChiDaXacNhan.value = "";
    daTinhPhiVanChuyen.value = false;
    nguonTinhPhi.value = "";
    moTaPhi.value = "";
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
      diaChiGiaoHang: chuanHoaDiaChi(diaChiGiaoHangHienThi.value),
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
      if (choPhepGiaoHang.value) {
        if (!diaChiHopLe(diaChiGiaoHang.value) && khachHangDuocChon.value?.diaChiMacDinh) {
          diaChiGiaoHang.value = khachHangDuocChon.value.diaChiMacDinh;
        }
        // Tự điền tên và SDT người nhận từ khách hàng đã chọn nếu đang trống
        if (!tenNguoiNhanGiaoHang.value.trim() && khachHangDuocChon.value?.hoTen) {
          tenNguoiNhanGiaoHang.value = khachHangDuocChon.value.hoTen;
        }
        if (!sdtNguoiNhanGiaoHang.value.trim() && khachHangDuocChon.value?.sdt) {
          sdtNguoiNhanGiaoHang.value = khachHangDuocChon.value.sdt;
        }
      }
    }
    if (Object.prototype.hasOwnProperty.call(patch, "tenNguoiNhan")) {
      tenNguoiNhanGiaoHang.value = patch.tenNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "soDienThoaiNguoiNhan")) {
      sdtNguoiNhanGiaoHang.value = patch.soDienThoaiNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "diaChiGiaoHang")) {
      diaChiGiaoHang.value = chuanHoaDiaChi(patch.diaChiGiaoHang);
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
      nguonTinhPhi.value = "";
      moTaPhi.value = "";
      return;
    }

    if (canTinhLai) {
      danhDauCanTinhLaiPhiVanChuyen();
    }
  }

  async function xuLyTinhPhiVanChuyen() {
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
      if (phiVanChuyenTimeout) clearTimeout(phiVanChuyenTimeout);
      phiVanChuyenTimeout = setTimeout(() => {
        xuLyTinhPhiVanChuyen().catch(() => {});
      }, 800);
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
