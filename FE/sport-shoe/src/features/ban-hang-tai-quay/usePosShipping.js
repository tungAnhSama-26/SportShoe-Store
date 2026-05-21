import { computed } from "vue";
import { tinhPhiVanChuyenTaiQuay } from "../../services/ban-hang-tai-quay";

export function usePosShipping({
  deliveryEnabled,
  deliveryRecipientName,
  deliveryRecipientPhone,
  deliveryAddress,
  deliveryCarrier,
  deliveryFee,
  deliveryResolvedAddress,
  deliveryCalculated,
  calculatingDeliveryFee,
  deliveryConfig,
  selectedCustomer,
  activePendingInvoice,
  cartItems,
  pageError
}) {
  const tenNguoiNhanGiaoHangHienThi = computed(() => {
    if (deliveryRecipientName.value.trim()) {
      return deliveryRecipientName.value.trim();
    }
    if (selectedCustomer.value?.hoTen) {
      return selectedCustomer.value.hoTen;
    }
    return activePendingInvoice.value?.thongTinGiaoHang?.tenNguoiNhan || "";
  });
  const soDienThoaiNguoiNhanGiaoHangHienThi = computed(() => {
    if (deliveryRecipientPhone.value.trim()) {
      return deliveryRecipientPhone.value.trim();
    }
    if (selectedCustomer.value?.sdt) {
      return selectedCustomer.value.sdt;
    }
    return activePendingInvoice.value?.thongTinGiaoHang?.soDienThoaiNguoiNhan || "";
  });
  const phiVanChuyenHienThi = computed(() => deliveryEnabled.value ? deliveryFee.value : 0);
  const coTheTinhPhiVanChuyen = computed(
    () => deliveryEnabled.value &&
      cartItems.value.length > 0 &&
      Boolean(deliveryAddress.value.trim()) &&
      !calculatingDeliveryFee.value
  );
  const coThongTinGiaoHangHopLe = computed(
    () => !deliveryEnabled.value ||
      (
        Boolean(tenNguoiNhanGiaoHangHienThi.value) &&
        Boolean(soDienThoaiNguoiNhanGiaoHangHienThi.value) &&
        Boolean(deliveryAddress.value.trim()) &&
        deliveryCalculated.value
      )
  );
  const shippingInfo = computed(() => ({
    giaoHang: deliveryEnabled.value,
    tenNguoiNhan: deliveryRecipientName.value,
    soDienThoaiNguoiNhan: deliveryRecipientPhone.value,
    diaChiGiaoHang: deliveryAddress.value,
    donViVanChuyen: deliveryCarrier.value,
    phiVanChuyen: deliveryFee.value,
    diaChiDaDo: deliveryResolvedAddress.value,
    daTinhPhi: deliveryCalculated.value,
    dangTinhPhi: calculatingDeliveryFee.value,
    coTheTinhPhi: coTheTinhPhiVanChuyen.value,
    serviceTypeId: deliveryConfig.value.serviceTypeId,
    length: deliveryConfig.value.length,
    width: deliveryConfig.value.width,
    height: deliveryConfig.value.height,
    weight: deliveryConfig.value.weight
  }));

  function markShippingFeeDirty() {
    if (!deliveryEnabled.value) {
      return;
    }
    deliveryFee.value = 0;
    deliveryResolvedAddress.value = "";
    deliveryCalculated.value = false;
  }

  function buildShippingPayload() {
    if (!deliveryEnabled.value) {
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
      diaChiGiaoHang: deliveryAddress.value.trim(),
      phiVanChuyen: deliveryFee.value,
      donViVanChuyen: deliveryCarrier.value || "GHN"
    };
  }

  function updateShippingInfo(patch) {
    const canTinhLai = [
      "diaChiGiaoHang",
      "serviceTypeId",
      "length",
      "width",
      "height",
      "weight"
    ].some((key) => Object.prototype.hasOwnProperty.call(patch, key));

    if (Object.prototype.hasOwnProperty.call(patch, "giaoHang")) {
      deliveryEnabled.value = Boolean(patch.giaoHang);
    }
    if (Object.prototype.hasOwnProperty.call(patch, "tenNguoiNhan")) {
      deliveryRecipientName.value = patch.tenNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "soDienThoaiNguoiNhan")) {
      deliveryRecipientPhone.value = patch.soDienThoaiNguoiNhan ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "diaChiGiaoHang")) {
      deliveryAddress.value = patch.diaChiGiaoHang ?? "";
    }
    if (Object.prototype.hasOwnProperty.call(patch, "serviceTypeId")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        serviceTypeId: Number(patch.serviceTypeId) || 2
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "length")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        length: Number(patch.length) || 30
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "width")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        width: Number(patch.width) || 20
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "height")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        height: Number(patch.height) || 12
      };
    }
    if (Object.prototype.hasOwnProperty.call(patch, "weight")) {
      deliveryConfig.value = {
        ...deliveryConfig.value,
        weight: Number(patch.weight) || 500
      };
    }

    if (!deliveryEnabled.value) {
      deliveryFee.value = 0;
      deliveryResolvedAddress.value = "";
      deliveryCalculated.value = false;
      return;
    }

    if (canTinhLai) {
      markShippingFeeDirty();
    }
  }

  async function handleCalculateShippingFee() {
    if (!coTheTinhPhiVanChuyen.value) {
      return;
    }
    calculatingDeliveryFee.value = true;
    pageError.value = "";
    try {
      const result = await tinhPhiVanChuyenTaiQuay({
        diaChiGiaoHang: deliveryAddress.value,
        serviceTypeId: deliveryConfig.value.serviceTypeId,
        length: deliveryConfig.value.length,
        width: deliveryConfig.value.width,
        height: deliveryConfig.value.height,
        weight: deliveryConfig.value.weight
      });
      deliveryFee.value = result.phiVanChuyen;
      deliveryResolvedAddress.value = result.diaChiDaDo || "";
      deliveryCalculated.value = true;
    } catch (error) {
      deliveryFee.value = 0;
      deliveryResolvedAddress.value = "";
      deliveryCalculated.value = false;
      pageError.value = error instanceof Error ? error.message : "Không thể tính phí vận chuyển";
    } finally {
      calculatingDeliveryFee.value = false;
    }
  }

  return {
    tenNguoiNhanGiaoHangHienThi,
    soDienThoaiNguoiNhanGiaoHangHienThi,
    phiVanChuyenHienThi,
    coTheTinhPhiVanChuyen,
    coThongTinGiaoHangHopLe,
    shippingInfo,
    markShippingFeeDirty,
    buildShippingPayload,
    updateShippingInfo,
    handleCalculateShippingFee
  };
}
