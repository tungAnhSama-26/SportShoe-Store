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
    const totalStock = Number(soLuongTon) + soLuongDaLuu(chiTietId);
    return Math.max(totalStock - soLuongDaChon(chiTietId), 0);
  }

  function themSanPham(product, quantity = 1, options = {}) {
    if (product.kichHoat === 0 || product.trangThai === 0) {
      showWarning(`Sản phẩm "${product.tenSanPham}" đã ngừng hoạt động, vui lòng chọn sản phẩm khác`);
      return false;
    }

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
        // Đánh dấu các dòng sản phẩm mang mức giá cũ trước đó
        cartItems.value = cartItems.value.map(it =>
          Number(it.chiTietId) === Number(product.chiTietId) && Number(it.giaBan) !== Number(product.giaBan)
            ? { ...it, isOutdatedPrice: true }
            : it
        );
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
          giaGoc: product.giaGoc,
          oldPrice: oldPrice,
          isOutdatedPrice: false,
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

  function isOutdatedPrice(item) {
    if (!item) return false;
    if (item.isOutdatedPrice) return true;
    const sameVariantItems = cartItems.value.filter(
      (it) => Number(it.chiTietId) === Number(item.chiTietId)
    );
    if (sameVariantItems.length > 1) {
      const latestItem = sameVariantItems[sameVariantItems.length - 1];
      if (latestItem && Number(latestItem.giaBan) !== Number(item.giaBan)) {
        return true;
      }
    }
    return false;
  }

  function tangSoLuong(cartItemId) {
    const targetItem = cartItems.value.find((item) => item.cartItemId === cartItemId || item.chiTietId === cartItemId);
    if (targetItem && isOutdatedPrice(targetItem)) {
      showWarning(
        `Sản phẩm "${targetItem.tenSanPham}" đã thay đổi giá trong hệ thống. Không thể tăng số lượng ở mức giá cũ. Vui lòng thêm sản phẩm với giá mới.`,
        "Không thể tăng số lượng"
      );
      return;
    }

    let reachedLimit = "";
    cartItems.value = cartItems.value.map((item) => {
      if (item.cartItemId !== cartItemId && item.chiTietId !== cartItemId) {
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

    const targetItem = cartItems.value.find((item) => item.cartItemId === cartItemId || item.chiTietId === cartItemId);
    if (targetItem && isOutdatedPrice(targetItem)) {
      if (newQuantityNum > targetItem.soLuong) {
        showWarning(
          `Sản phẩm "${targetItem.tenSanPham}" đã thay đổi giá trong hệ thống. Không thể tăng số lượng ở mức giá cũ.`,
          "Không thể tăng số lượng"
        );
        cartItems.value = cartItems.value.map((item) => 
          (item.cartItemId === cartItemId || item.chiTietId === cartItemId) ? { ...item, soLuong: item.soLuong } : item
        );
        return;
      }
    }

    let reachedLimit = "";
    cartItems.value = cartItems.value.map((item) => {
      if (item.cartItemId !== cartItemId && item.chiTietId !== cartItemId) {
        return item;
      }
      const conLaiThem = soLuongConLai(item.chiTietId, item.soLuongTon);
      const maxAllowed = item.soLuong + conLaiThem;

      if (newQuantityNum > maxAllowed) {
        reachedLimit = item.tenSanPham;
        return { ...item, soLuong: maxAllowed > 0 ? maxAllowed : 1 };
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
    isOutdatedPrice,
    themSanPham,
    tangSoLuong,
    giamSoLuong,
    xoaSanPham,
    capNhatSoLuong,
  };
}
