import { computed, ref } from "vue";
import { showError, showWarning } from "../../utils/alert";
import { validateGioHang as doValidateGioHang } from "./ValidateGioHang";

export function LogicGioHang({
  danhDauCanTinhLaiPhiVanChuyen,
  capNhatTienKhachThanhToan,
  danhDauCanApDungLaiPhieu,
  dongBoSanPhamSauKhiThemVaoGio,
  xoaPhanHoi
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

  function validateGioHang(isPayment = false) {
    return doValidateGioHang(sanPhamValidationMessage.value, isPayment, cartItems.value.length);
  }


  function taoDanhSachSanPhamThanhToan() {
    return cartItems.value.map((item) => ({
      chiTietId: item.chiTietId,
      soLuong: item.soLuong,
      giaBan: item.giaBan
    }));
  }

  function soLuongDaChon(chiTietId) {
    return cartItems.value.filter((item) => Number(item.chiTietId) === Number(chiTietId)).reduce((sum, item) => sum + item.soLuong, 0);
  }

  function soLuongDaLuu(chiTietId) {
    return cartItems.value.filter((item) => Number(item.chiTietId) === Number(chiTietId)).reduce((sum, item) => sum + (item.soLuongBanDau || 0), 0);
  }

  function soLuongConLai(chiTietId, soLuongTon) {
    return Math.max(Number(soLuongTon) - soLuongDaChon(chiTietId), 0);
  }

  function themSanPham(product, quantity = 1, options = {}) {
    const soLuongCoTheThem = soLuongConLai(product.chiTietId, product.soLuongTon);
    if (quantity > soLuongCoTheThem) {
      showWarning(`Sản phẩm ${product.tenSanPham} đã vượt giới hạn tồn kho`);
      return false;
    }

    const exactItem = cartItems.value.find(
      (item) => Number(item.chiTietId) === Number(product.chiTietId) && Number(item.giaBan) === Number(product.giaBan)
    );
    const anyExistingItem = cartItems.value.find(
      (item) => Number(item.chiTietId) === Number(product.chiTietId)
    );
    
    let resultStatus = "added";
    let oldPrice = null;

    if (exactItem) {
      resultStatus = "incremented";
      cartItems.value = cartItems.value.map(item => 
        item.cartItemId === exactItem.cartItemId
          ? { ...item, soLuong: item.soLuong + quantity }
          : item
      );
    } else {
      if (anyExistingItem) {
        resultStatus = "price_updated";
        oldPrice = anyExistingItem.giaBan;
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
          soLuongBanDau: 0,
          giaBan: product.giaBan,
          soLuongTon: product.soLuongTon
        }
      ];
    }
    
    dongBoSanPhamSauKhiThemVaoGio(options);
    danhDauCanApDungLaiPhieu();
    danhDauCanTinhLaiPhiVanChuyen();
    capNhatTienKhachThanhToan();
    xoaPhanHoi();
    
    return {
      status: resultStatus,
      oldPrice: oldPrice,
      newPrice: product.giaBan,
      tenSanPham: product.tenSanPham
    };
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
      showWarning(`Sản phẩm ${reachedLimit} đã vượt giới hạn tồn kho`);
      return;
    }
    danhDauCanApDungLaiPhieu();
    danhDauCanTinhLaiPhiVanChuyen();
    capNhatTienKhachThanhToan();
  }

  function giamSoLuong(cartItemId) {
    cartItems.value = cartItems.value
      .map((item) => item.cartItemId === cartItemId ? { ...item, soLuong: item.soLuong - 1 } : item)
      .filter((item) => item.soLuong > 0);
    danhDauCanApDungLaiPhieu();
    danhDauCanTinhLaiPhiVanChuyen();
    capNhatTienKhachThanhToan();
  }

  function xoaSanPham(cartItemId) {
    cartItems.value = cartItems.value.filter((item) => item.cartItemId !== cartItemId);
    danhDauCanApDungLaiPhieu();
    danhDauCanTinhLaiPhiVanChuyen();
    capNhatTienKhachThanhToan();
  }

  function capNhatSoLuong(cartItemId, newQuantity) {
    let newQuantityNum = parseInt(newQuantity, 10);
    if (isNaN(newQuantityNum) || newQuantityNum <= 0) {
      newQuantityNum = 1;
    }

    let reachedLimit = "";
    cartItems.value = cartItems.value.map((item) => {
      if (item.cartItemId !== cartItemId) {
        return item;
      }
      const conLaiThem = soLuongConLai(item.chiTietId, item.soLuongTon);
      const maxAllowed = item.soLuong + conLaiThem;

      if (newQuantityNum > maxAllowed) {
        reachedLimit = item.tenSanPham;
        return { ...item, soLuong: maxAllowed };
      }
      return { ...item, soLuong: newQuantityNum };
    });

    if (reachedLimit) {
      showWarning(`Sản phẩm ${reachedLimit} đã vượt giới hạn tồn kho`);
    }

    danhDauCanApDungLaiPhieu();
    danhDauCanTinhLaiPhiVanChuyen();
    capNhatTienKhachThanhToan();
  }

  return {
    cartItems,
    tongSoLuong,
    tongTien,
    sanPhamValidationMessage,
    validateGioHang,
    taoDanhSachSanPhamThanhToan,
    soLuongConLai,
    themSanPham,
    tangSoLuong,
    giamSoLuong,
    xoaSanPham,
    capNhatSoLuong,
  };
}
