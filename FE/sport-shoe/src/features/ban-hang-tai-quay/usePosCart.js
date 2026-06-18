import { computed, ref } from "vue";

export function usePosCart({
  markShippingFeeDirty,
  capNhatTienKhachThanhToan,
  danhDauCanApDungLaiPhieu,
  syncProductAfterCartAdd,
  pageError,
  clearFeedback
}) {
  const cartItems = ref([]);

  const tongSoLuong = computed(
    () => cartItems.value.reduce((total, item) => total + item.soLuong, 0)
  );
  const tongTien = computed(
    () => cartItems.value.reduce((total, item) => total + item.soLuong * item.giaBan, 0)
  );
  const sanPhamKhongHopLe = computed(
    () => cartItems.value.find((item) => !Number.isInteger(Number(item.soLuong)) || Number(item.soLuong) <= 0) ?? null
  );
  const sanPhamValidationMessage = computed(() => {
    if (sanPhamKhongHopLe.value) {
      return `Số lượng của sản phẩm ${sanPhamKhongHopLe.value.tenSanPham} phải lớn hơn 0.`;
    }
    return "";
  });

  function validateCartItems(isPayment = false) {
    if (sanPhamValidationMessage.value) {
      pageError.value = sanPhamValidationMessage.value;
      return false;
    }
    if (isPayment && !cartItems.value.length) {
      pageError.value = "Vui lòng thêm ít nhất 1 sản phẩm vào hóa đơn để thanh toán.";
      return false;
    }
    return true;
  }

  function taoDanhSachSanPhamThanhToan() {
    return cartItems.value.map((item) => ({
      chiTietId: item.chiTietId,
      soLuong: item.soLuong,
      giaBan: item.giaBan
    }));
  }

  function soLuongDaChon(chiTietId) {
    return cartItems.value.filter((item) => item.chiTietId === chiTietId).reduce((sum, item) => sum + item.soLuong, 0);
  }

  function soLuongConLai(chiTietId, soLuongTon) {
    return Math.max(soLuongTon - soLuongDaChon(chiTietId), 0);
  }

  function themSanPham(product, quantity = 1, options = {}) {
    const soLuongCoTheThem = soLuongConLai(product.chiTietId, product.soLuongTon);
    const existingSamePrice = cartItems.value.find((item) => item.chiTietId === product.chiTietId && item.giaBan === product.giaBan);
    const existingDifferentPrice = cartItems.value.find((item) => item.chiTietId === product.chiTietId && item.giaBan !== product.giaBan);
    
    if (existingSamePrice) {
      if (quantity > soLuongCoTheThem) {
        pageError.value = `Sản phẩm ${existingSamePrice.tenSanPham} đã đạt giới hạn tồn kho`;
        return false;
      }
      existingSamePrice.soLuong += quantity;
    } else {
      if (quantity > soLuongCoTheThem) {
        pageError.value = `Sản phẩm ${product.tenSanPham} đã vượt giới hạn tồn kho`;
        return false;
      }
      cartItems.value = [
        ...cartItems.value,
        {
          cartItemId: Date.now().toString() + Math.random().toString(),
          chiTietId: product.chiTietId,
          maSanPham: product.maSanPham,
          tenSanPham: product.tenSanPham,
          sku: product.sku,
          mauSac: product.mauSac,
          kichCo: product.kichCo,
          hinhAnh: product.hinhAnh || "",
          soLuong: quantity,
          giaBan: product.giaBan,
          soLuongTon: product.soLuongTon,
          isPriceChanged: !!existingDifferentPrice // Flag indicating it has a different price than another item in cart
        }
      ];
    }
    syncProductAfterCartAdd(options);
    danhDauCanApDungLaiPhieu();
    markShippingFeeDirty();
    capNhatTienKhachThanhToan();
    clearFeedback();
    return true;
  }

  function tangSoLuong(cartItemId) {
    let reachedLimit = "";
    cartItems.value = cartItems.value.map((item) => {
      if (item.cartItemId !== cartItemId) {
        return item;
      }
      const conLai = soLuongConLai(item.chiTietId, item.soLuongTon);
      if (conLai <= 0) {
        reachedLimit = item.tenSanPham;
        return item;
      }
      return { ...item, soLuong: item.soLuong + 1 };
    });
    if (reachedLimit) {
      pageError.value = `Sản phẩm ${reachedLimit} đã vượt giới hạn tồn kho`;
      return;
    }
    danhDauCanApDungLaiPhieu();
    markShippingFeeDirty();
    capNhatTienKhachThanhToan();
  }

  function giamSoLuong(cartItemId) {
    cartItems.value = cartItems.value
      .map((item) => item.cartItemId === cartItemId ? { ...item, soLuong: item.soLuong - 1 } : item)
      .filter((item) => item.soLuong > 0);
    danhDauCanApDungLaiPhieu();
    markShippingFeeDirty();
    capNhatTienKhachThanhToan();
  }

  return {
    cartItems,
    tongSoLuong,
    tongTien,
    sanPhamValidationMessage,
    validateCartItems,
    taoDanhSachSanPhamThanhToan,
    soLuongConLai,
    themSanPham,
    tangSoLuong,
    giamSoLuong
  };
}
