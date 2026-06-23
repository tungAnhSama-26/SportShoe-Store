import { computed, ref, watch, onMounted, onUnmounted } from "vue";
import { timSanPhamTaiQuay } from "../../services/ban-hang-tai-quay";
import { KHACH_VANG_LAI } from "./HangSo";
import { trichXuatTuKhoaSanPhamTuQr } from "./SanPhamQR";
import { showError, showSuccess, showWarning } from "../../utils/alert";

export function LogicSanPham({
  soLuongConLai,
  themSanPham,
  xoaPhanHoi,
  thongBaoLoi,
  thongBaoThanhCong
}) {
  const tuKhoaSanPham = ref("");
  const ketQuaBienTheSanPham = ref([]);
  const chiTietSanPhamDaChon = ref(null);
  const mauSacDaChon = ref("");
  const kichCoDaChon = ref("");
  const soLuongDaChon = ref(1);
  const dangTaiSanPham = ref(false);
  const hienThiDanhSachSanPham = ref(false);
  
  const trangHienTai = ref(1);
  const kichThuocTrang = ref(5);

  let boDemThoiGianSanPham;

  const nhanTimKiemSanPham = computed(
    () => tuKhoaSanPham.value.trim() ? "Kết quả tìm kiếm sản phẩm" : "Sản phẩm tại quầy"
  );
  const boLocThuongHieuDaChon = ref("");
  const boLocDanhMucDaChon = ref("");
  const giaThapNhatDaChon = ref(0);
  const giaCaoNhatDaChon = ref(10000000);

  const giaCaoNhatCoSan = computed(() => {
    let max = 0;
    ketQuaBienTheSanPham.value.forEach(p => {
      const price = Number(p.giaBan || 0);
      if (price > max) max = price;
    });
    // Làm tròn lên hàng trăm nghìn hoặc triệu, hoặc giữ nguyên
    return max > 0 ? max : 10000000;
  });

  watch(giaCaoNhatCoSan, (newMax) => {
    if (giaCaoNhatDaChon.value > newMax || giaCaoNhatDaChon.value === 10000000) {
      giaCaoNhatDaChon.value = newMax;
    }
  });

  const thuongHieuCoSan = computed(() => {
    const brands = new Set();
    ketQuaBienTheSanPham.value.forEach(p => {
      if (p.thuongHieu) brands.add(p.thuongHieu);
    });
    return Array.from(brands).sort();
  });

  const danhMucCoSan = computed(() => {
    const categories = new Set();
    ketQuaBienTheSanPham.value.forEach(p => {
      if (p.loaiGiay) categories.add(p.loaiGiay);
    });
    return Array.from(categories).sort();
  });

  const boLocMauSacDaChon = ref("");
  const boLocKichCoDaChon = ref("");

  const mauSacCoSan = computed(() => {
    const colors = new Set();
    ketQuaBienTheSanPham.value.forEach(p => {
      if (p.mauSac) colors.add(p.mauSac);
      else if (p.maBienThe) colors.add(p.maBienThe);
    });
    return Array.from(colors).sort();
  });

  const kichCoCoSan = computed(() => {
    const sizes = new Set();
    ketQuaBienTheSanPham.value.forEach(p => {
      if (p.kichCo) sizes.add(p.kichCo);
    });
    return Array.from(sizes).sort();
  });

  const ketQuaSanPham = computed(() => {
    let filtered = ketQuaBienTheSanPham.value;
    
    if (boLocThuongHieuDaChon.value) {
      filtered = filtered.filter(p => p.thuongHieu === boLocThuongHieuDaChon.value);
    }
    
    if (boLocDanhMucDaChon.value) {
      filtered = filtered.filter(p => p.loaiGiay === boLocDanhMucDaChon.value);
    }

    if (boLocMauSacDaChon.value) {
      filtered = filtered.filter(p => (p.mauSac || p.maBienThe) === boLocMauSacDaChon.value);
    }

    if (boLocKichCoDaChon.value) {
      filtered = filtered.filter(p => p.kichCo === boLocKichCoDaChon.value);
    }

    if (giaThapNhatDaChon.value > 0 || giaCaoNhatDaChon.value < giaCaoNhatCoSan.value) {
      filtered = filtered.filter(p => {
        const price = Number(p.giaBan || 0);
        return price >= giaThapNhatDaChon.value && price <= giaCaoNhatDaChon.value;
      });
    }
    
    return filtered.map(p => ({
      ...p,
      minPrice: Number(p.giaBan || 0),
      maxPrice: Number(p.giaBan || 0),
      coGiamGia: Number(p.giaBan || 0) < Number(p.giaGoc || 0)
    }));
  });

  const tongSoMuc = computed(() => ketQuaSanPham.value.length);
  const tongSoTrang = computed(() => Math.ceil(tongSoMuc.value / kichThuocTrang.value) || 1);
  
  watch([boLocThuongHieuDaChon, boLocDanhMucDaChon, boLocMauSacDaChon, boLocKichCoDaChon, giaThapNhatDaChon, giaCaoNhatDaChon, tuKhoaSanPham], () => {
    trangHienTai.value = 1;
  });
  
  const sanPhamPhanTrang = computed(() => {
    const start = (trangHienTai.value - 1) * kichThuocTrang.value;
    return ketQuaSanPham.value.slice(start, start + kichThuocTrang.value);
  });

  const bienTheLienQuan = computed(() => {
    if (!chiTietSanPhamDaChon.value) {
      return [];
    }
    return ketQuaBienTheSanPham.value.filter(
      (product) => product.maSanPham === chiTietSanPhamDaChon.value?.maSanPham &&
        product.tenSanPham === chiTietSanPhamDaChon.value?.tenSanPham
    );
  });
  const luaChonMauSac = computed(() => {
    const grouped = new Map();
    for (const variant of bienTheLienQuan.value) {
      const key = variant.mauSac || variant.maBienThe;
      if (!grouped.has(key)) {
        grouped.set(key, variant);
      }
    }
    return Array.from(grouped.values());
  });
  const luaChonKichCo = computed(
    () => bienTheLienQuan.value.filter((variant) => {
      if (!mauSacDaChon.value) {
        return true;
      }
      return (variant.mauSac || variant.maBienThe) === mauSacDaChon.value;
    })
  );
  const bienTheDaChon = computed(() => {
    if (!chiTietSanPhamDaChon.value) {
      return null;
    }
    return bienTheLienQuan.value.find(
      (variant) => (
        mauSacDaChon.value
          ? (variant.mauSac || variant.maBienThe) === mauSacDaChon.value
          : true
      ) && (
        kichCoDaChon.value
          ? (variant.kichCo || "") === kichCoDaChon.value
          : true
      )
    ) || chiTietSanPhamDaChon.value;
  });
  const chiTietDangChon = computed(() => bienTheDaChon.value || chiTietSanPhamDaChon.value);
  const hinhAnhDangChon = computed(
    () => chiTietDangChon.value?.hinhAnh || chiTietSanPhamDaChon.value?.hinhAnh || ""
  );
  const soLuongTonKhaDungChiTiet = computed(() => {
    if (!chiTietDangChon.value) {
      return 0;
    }
    return soLuongConLai(chiTietDangChon.value.chiTietId, chiTietDangChon.value.soLuongTon);
  });

  const soLuongTonSauKhiChon = computed(
    () => Math.max(soLuongTonKhaDungChiTiet.value - soLuongDaChon.value, 0)
  );


  async function taiSanPham(keyword, silent = false) {
    const searchKeyword = keyword !== undefined ? keyword : tuKhoaSanPham.value;
    if (!silent) {
      dangTaiSanPham.value = true;
    }
    try {
      ketQuaBienTheSanPham.value = await timSanPhamTaiQuay(searchKeyword || "");
    } catch (error) {
      if (!silent) {
        showError(error instanceof Error ? error.message : "Không thể tìm sản phẩm");
      }
    } finally {
      if (!silent) {
        dangTaiSanPham.value = false;
      }
    }
  }

  let chuKyTaiLaiTuDong = null;

  onMounted(() => {
    // Poll every 5 seconds to keep products realtime
    chuKyTaiLaiTuDong = setInterval(() => {
      // Only poll if we are not actively searching to avoid race conditions
      if (!dangTaiSanPham.value) {
        void taiSanPham(undefined, true);
      }
    }, 5000);
  });

  onUnmounted(() => {
    if (chuKyTaiLaiTuDong) {
      clearInterval(chuKyTaiLaiTuDong);
    }
  });

  function nhomBienTheSanPham(products) {
    const grouped = new Map();

    for (const product of products) {
      const key = `${product.maSanPham}::${product.tenSanPham}`;
      const soLuongKhaDung = soLuongConLai(product.chiTietId, product.soLuongTon);

      if (!grouped.has(key)) {
        grouped.set(key, {
          ...product,
          soLuongTon: 0,
          tongBienThe: 0,
          coGiamGia: false,
          minPrice: Number(product.giaBan || 0),
          maxPrice: Number(product.giaBan || 0)
        });
      }

      const groupedProduct = grouped.get(key);
      const currentGiaBan = Number(product.giaBan || 0);
      if (currentGiaBan < groupedProduct.minPrice) groupedProduct.minPrice = currentGiaBan;
      if (currentGiaBan > groupedProduct.maxPrice) groupedProduct.maxPrice = currentGiaBan;
      
      groupedProduct.soLuongTon += soLuongKhaDung;
      groupedProduct.tongBienThe += 1;
      groupedProduct.coGiamGia =
        groupedProduct.coGiamGia ||
        Number(product.giaBan || 0) < Number(product.giaGoc || 0);
      if (!groupedProduct.hinhAnh && product.hinhAnh) {
        groupedProduct.hinhAnh = product.hinhAnh;
      }
    }

    return Array.from(grouped.values());
  }

  function timBienTheChinhXacTheoMa(products, keyword) {
    const normalizedKeyword = String(keyword ?? "").trim().toLowerCase();
    if (!normalizedKeyword) {
      return null;
    }

    return products.find((product) =>
      [product.maBienThe, product.sku, product.chiTietId]
        .map((value) => String(value ?? "").trim().toLowerCase())
        .some((value) => value && value === normalizedKeyword)
    ) ?? null;
  }

  function laySoLuongTonHienTai(chiTietId, fallback) {
    return ketQuaBienTheSanPham.value.find((product) => product.chiTietId === chiTietId)?.soLuongTon ?? fallback;
  }

  function moChiTietSanPham(product) {
    chiTietSanPhamDaChon.value = product;
    mauSacDaChon.value = product.mauSac || product.maBienThe;
    kichCoDaChon.value = product.kichCo || "";
    soLuongDaChon.value = 1;
  }

  function lamMoiBoLoc() {
    boLocThuongHieuDaChon.value = "";
    boLocDanhMucDaChon.value = "";
    boLocMauSacDaChon.value = "";
    boLocKichCoDaChon.value = "";
    giaThapNhatDaChon.value = 0;
    giaCaoNhatDaChon.value = giaCaoNhatCoSan.value;
    tuKhoaSanPham.value = "";
  }

  function dongChiTietSanPham() {
    chiTietSanPhamDaChon.value = null;
    mauSacDaChon.value = "";
    kichCoDaChon.value = "";
    soLuongDaChon.value = 1;
    lamMoiBoLoc();
    hienThiDanhSachSanPham.value = false;
  }

  async function xuLyQuetQrSanPham(rawValue) {
    if (!rawValue) {
      return;
    }

    try {
      const keyword = trichXuatTuKhoaSanPhamTuQr(rawValue);
      tuKhoaSanPham.value = keyword;
      
      if (typeof xoaPhanHoi === 'function') xoaPhanHoi();
      dangTaiSanPham.value = true;
      
      const products = await timSanPhamTaiQuay(keyword);
      ketQuaBienTheSanPham.value = products;
      console.log("Tìm kiếm QR trả về:", products.length, "sản phẩm");

      if (!products || products.length === 0) {
        showError(`Không tìm thấy sản phẩm hoặc sản phẩm đã hết hàng với mã: ${keyword}`);
        return;
      }

      // 1. Cố gắng tìm biến thể chính xác
      const exactVariant = timBienTheChinhXacTheoMa(products, keyword);
      console.log("Biến thể chính xác:", exactVariant);
      
      // Theo yêu cầu: trả về giỏ hàng luôn (tự động chọn biến thể đầu tiên nếu không khớp chính xác)
      let variantToAdd = exactVariant;
      
      if (!variantToAdd && products.length > 0) {
        variantToAdd = products[0];
        console.log("Tự động chọn biến thể đầu tiên để trả về giỏ hàng:", variantToAdd);
      }
      
      if (variantToAdd) {
        hienThiDanhSachSanPham.value = false;
        lamMoiBoLoc();
        const result = themSanPham(variantToAdd, 1, {
          preserveProductSearch: true,
          scannedKeyword: keyword,
          scannedProducts: products,
        });
        console.log("Kết quả themSanPham:", result);
        
        if (!result) {
          showError(`Không thể thêm sản phẩm ${keyword} vào giỏ hàng dù đã tìm thấy.`);
          return;
        }

        if (result.status === "added") {
          showSuccess(`Đã thêm "${result.tenSanPham}" vào giỏ hàng.`, "Thành công!");
        } else if (result.status === "incremented") {
          showSuccess(`Sản phẩm "${result.tenSanPham}" đã có trong giỏ hàng, đã tăng số lượng.`, "Thành công!");
        } else if (result.status === "price_updated") {
          // Format prices for the toast
          const formatPrice = (price) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
          const oldFormatted = formatPrice(result.oldPrice);
          const newFormatted = formatPrice(result.newPrice);
          
          showWarning(`Giá sản phẩm "${result.tenSanPham}" đã được cập nhật từ ${oldFormatted} lên ${newFormatted}.`, "Thông báo đổi giá");
        }
        return;
      }
    } catch (error) {
      const errMsg = error instanceof Error ? error.message : "Có lỗi khi xử lý mã quét";
      showError(errMsg);
    } finally {
      dangTaiSanPham.value = false;
    }
  }

  function chonMauSac(value) {
    mauSacDaChon.value = value;
    kichCoDaChon.value = luaChonKichCo.value[0]?.kichCo || "";
    soLuongDaChon.value = 1;
  }

  function chonKichCo(value) {
    kichCoDaChon.value = value;
    soLuongDaChon.value = 1;
  }

  function chonBienThe(variant) {
    if (!variant) return;
    mauSacDaChon.value = variant.mauSac || variant.maBienThe;
    kichCoDaChon.value = variant.kichCo || "";
    soLuongDaChon.value = 1;
  }

  function giamSoLuongChiTiet() {
    soLuongDaChon.value = Math.max(soLuongDaChon.value - 1, 1);
  }

  function tangSoLuongChiTiet() {
    if (!bienTheDaChon.value) {
      return;
    }
    const soLuongToiDa = soLuongConLai(bienTheDaChon.value.chiTietId, bienTheDaChon.value.soLuongTon);
    soLuongDaChon.value = Math.min(soLuongDaChon.value + 1, Math.max(soLuongToiDa, 1));
  }

  function capNhatSoLuongChiTiet(newQuantity) {
    if (!bienTheDaChon.value) return;

    let newQuantityNum = parseInt(newQuantity, 10);
    if (isNaN(newQuantityNum) || newQuantityNum <= 0) {
      newQuantityNum = 1;
    }

    const soLuongToiDa = soLuongConLai(bienTheDaChon.value.chiTietId, bienTheDaChon.value.soLuongTon);
    if (newQuantityNum > soLuongToiDa) {
      soLuongDaChon.value = Math.max(soLuongToiDa, 1);
      showWarning(`Sản phẩm đã đạt giới hạn tồn kho (tối đa ${soLuongToiDa})`);
    } else {
      soLuongDaChon.value = newQuantityNum;
    }
  }

  function moDanhSachSanPham() {
    hienThiDanhSachSanPham.value = true;
    return taiSanPham(tuKhoaSanPham.value);
  }

  function dongDanhSachSanPham() {
    window.setTimeout(() => {
      hienThiDanhSachSanPham.value = false;
    }, 150);
  }

  function xoaBoDemThoiGianSanPham() {
    if (boDemThoiGianSanPham) {
      window.clearTimeout(boDemThoiGianSanPham);
    }
  }

  watch(tuKhoaSanPham, (value) => {
    xoaBoDemThoiGianSanPham();
    trangHienTai.value = 1;
    hienThiDanhSachSanPham.value = value.trim().length > 0;
    boDemThoiGianSanPham = window.setTimeout(() => {
      void taiSanPham(value);
    }, 250);
  });

  function themTrucTiepBienThe(product) {
    if (!product) return;
    const result = themSanPham(product, 1, {
      preserveProductSearch: true,
      scannedKeyword: product.maBienThe,
      scannedProducts: [product]
    });
    
    hienThiDanhSachSanPham.value = false;
    lamMoiBoLoc();
    if (!result) {
      showError(`Không thể thêm sản phẩm vào giỏ hàng.`);
      return;
    }
    if (result.status === "added") {
      showSuccess(`Đã thêm "${result.tenSanPham}" vào giỏ hàng.`, "Thành công!");
    } else if (result.status === "incremented") {
      showSuccess(`Sản phẩm "${result.tenSanPham}" đã có trong giỏ hàng, đã tăng số lượng.`, "Thành công!");
    } else if (result.status === "price_updated") {
      const formatPrice = (price) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
      const oldFormatted = formatPrice(result.oldPrice);
      const newFormatted = formatPrice(result.newPrice);
      showWarning(`Giá sản phẩm "${result.tenSanPham}" đã được cập nhật từ ${oldFormatted} lên ${newFormatted}.`, "Thông báo đổi giá");
    }
  }

  return {
    tuKhoaSanPham,
    ketQuaBienTheSanPham,
    chiTietSanPhamDaChon,
    mauSacDaChon,
    kichCoDaChon,
    soLuongDaChon,
    dangTaiSanPham,
    hienThiDanhSachSanPham,
    nhanTimKiemSanPham,
    ketQuaSanPham,
    sanPhamPhanTrang,
    trangHienTai,
    kichThuocTrang,
    tongSoMuc,
    tongSoTrang,
    boLocThuongHieuDaChon,
    boLocDanhMucDaChon,
    boLocMauSacDaChon,
    boLocKichCoDaChon,
    giaThapNhatDaChon,
    giaCaoNhatDaChon,
    giaCaoNhatCoSan,
    thuongHieuCoSan,
    danhMucCoSan,
    mauSacCoSan,
    kichCoCoSan,
    bienTheLienQuan,
    luaChonMauSac,
    luaChonKichCo,
    bienTheDaChon,
    chiTietDangChon,
    hinhAnhDangChon,
    soLuongTonKhaDungChiTiet,
    soLuongTonSauKhiChon,
    taiSanPham,
    nhomBienTheSanPham,
    timBienTheChinhXacTheoMa,
    laySoLuongTonHienTai,
    moChiTietSanPham,
    dongChiTietSanPham,
    themTrucTiepBienThe,
    xuLyQuetQrSanPham,
    chonMauSac,
    chonKichCo,
    chonBienThe,
    giamSoLuongChiTiet,
    tangSoLuongChiTiet,
    capNhatSoLuongChiTiet,
    moDanhSachSanPham,
    dongDanhSachSanPham,
    xoaBoDemThoiGianSanPham
  };
}
