import { useState, useMemo, useEffect, useCallback, useRef } from 'react';
import { timSanPhamTaiQuay } from '../../api/dichVuBanHang';
import { KHACH_VANG_LAI } from './HangSo';
import { trichXuatTuKhoaSanPhamTuQr } from './SanPhamQR';
import { showError, showToastSuccess as showSuccess, showWarning } from '../../utils/alert';

export function useLogicSanPham({
  soLuongConLai,
  themSanPham,
  xoaPhanHoi,
  thongBaoLoi,
  thongBaoThanhCong
}) {
  const [tuKhoaSanPham, setTuKhoaSanPham] = useState("");
  const [ketQuaBienTheSanPham, setKetQuaBienTheSanPham] = useState([]);
  const [chiTietSanPhamDaChon, setChiTietSanPhamDaChon] = useState(null);
  const [mauSacDaChon, setMauSacDaChon] = useState("");
  const [kichCoDaChon, setKichCoDaChon] = useState("");
  const [soLuongDaChon, setSoLuongDaChon] = useState(1);
  const [dangTaiSanPham, setDangTaiSanPham] = useState(false);
  const [hienThiDanhSachSanPham, setHienThiDanhSachSanPham] = useState(false);
  
  const [trangHienTai, setTrangHienTai] = useState(1);
  const [kichThuocTrang, setKichThuocTrang] = useState(5);

  const boDemThoiGianSanPham = useRef(null);

  const nhanTimKiemSanPham = useMemo(() => tuKhoaSanPham.trim() ? "Kết quả tìm kiếm sản phẩm" : "Sản phẩm tại quầy", [tuKhoaSanPham]);
  
  const [boLocThuongHieuDaChon, setBoLocThuongHieuDaChon] = useState("");
  const [boLocDanhMucDaChon, setBoLocDanhMucDaChon] = useState("");
  const [giaThapNhatDaChon, setGiaThapNhatDaChon] = useState(0);
  const [giaCaoNhatDaChon, setGiaCaoNhatDaChon] = useState(10000000);

  const giaCaoNhatCoSan = useMemo(() => {
    let max = 0;
    ketQuaBienTheSanPham.forEach(p => {
      const price = Number(p.giaBan || 0);
      if (price > max) max = price;
    });
    return max > 0 ? max : 10000000;
  }, [ketQuaBienTheSanPham]);

  useEffect(() => {
    if (giaCaoNhatDaChon > giaCaoNhatCoSan || giaCaoNhatDaChon === 10000000) {
      setGiaCaoNhatDaChon(giaCaoNhatCoSan);
    }
  }, [giaCaoNhatCoSan, giaCaoNhatDaChon]);

  const thuongHieuCoSan = useMemo(() => {
    const brands = new Set();
    ketQuaBienTheSanPham.forEach(p => {
      if (p.thuongHieu) brands.add(p.thuongHieu);
    });
    return Array.from(brands).sort();
  }, [ketQuaBienTheSanPham]);

  const danhMucCoSan = useMemo(() => {
    const categories = new Set();
    ketQuaBienTheSanPham.forEach(p => {
      if (p.loaiGiay) categories.add(p.loaiGiay);
    });
    return Array.from(categories).sort();
  }, [ketQuaBienTheSanPham]);

  const [boLocMauSacDaChon, setBoLocMauSacDaChon] = useState("");
  const [boLocKichCoDaChon, setBoLocKichCoDaChon] = useState("");

  const mauSacCoSan = useMemo(() => {
    const colors = new Set();
    ketQuaBienTheSanPham.forEach(p => {
      if (p.mauSac) colors.add(p.mauSac);
      else if (p.maBienThe) colors.add(p.maBienThe);
    });
    return Array.from(colors).sort();
  }, [ketQuaBienTheSanPham]);

  const kichCoCoSan = useMemo(() => {
    const sizes = new Set();
    ketQuaBienTheSanPham.forEach(p => {
      if (p.kichCo) sizes.add(p.kichCo);
    });
    return Array.from(sizes).sort();
  }, [ketQuaBienTheSanPham]);

  const ketQuaSanPham = useMemo(() => {
    let filtered = ketQuaBienTheSanPham;
    
    if (boLocThuongHieuDaChon) {
      filtered = filtered.filter(p => p.thuongHieu === boLocThuongHieuDaChon);
    }
    
    if (boLocDanhMucDaChon) {
      filtered = filtered.filter(p => p.loaiGiay === boLocDanhMucDaChon);
    }

    if (boLocMauSacDaChon) {
      filtered = filtered.filter(p => (p.mauSac || p.maBienThe) === boLocMauSacDaChon);
    }

    if (boLocKichCoDaChon) {
      filtered = filtered.filter(p => p.kichCo === boLocKichCoDaChon);
    }

    if (giaThapNhatDaChon > 0 || giaCaoNhatDaChon < giaCaoNhatCoSan) {
      filtered = filtered.filter(p => {
        const price = Number(p.giaBan || 0);
        return price >= giaThapNhatDaChon && price <= giaCaoNhatDaChon;
      });
    }
    
    return filtered.map(p => ({
      ...p,
      minPrice: Number(p.giaBan || 0),
      maxPrice: Number(p.giaBan || 0),
      coGiamGia: Number(p.giaBan || 0) < Number(p.giaGoc || 0)
    }));
  }, [ketQuaBienTheSanPham, boLocThuongHieuDaChon, boLocDanhMucDaChon, boLocMauSacDaChon, boLocKichCoDaChon, giaThapNhatDaChon, giaCaoNhatDaChon, giaCaoNhatCoSan]);

  const tongSoMuc = useMemo(() => ketQuaSanPham.length, [ketQuaSanPham]);
  const tongSoTrang = useMemo(() => Math.ceil(tongSoMuc / kichThuocTrang) || 1, [tongSoMuc, kichThuocTrang]);
  
  useEffect(() => {
    setTrangHienTai(1);
  }, [boLocThuongHieuDaChon, boLocDanhMucDaChon, boLocMauSacDaChon, boLocKichCoDaChon, giaThapNhatDaChon, giaCaoNhatDaChon, tuKhoaSanPham]);
  
  const sanPhamPhanTrang = useMemo(() => {
    const start = (trangHienTai - 1) * kichThuocTrang;
    return ketQuaSanPham.slice(start, start + kichThuocTrang);
  }, [ketQuaSanPham, trangHienTai, kichThuocTrang]);

  const bienTheLienQuan = useMemo(() => {
    if (!chiTietSanPhamDaChon) {
      return [];
    }
    return ketQuaBienTheSanPham.filter(
      (product) => product.maSanPham === chiTietSanPhamDaChon?.maSanPham &&
        product.tenSanPham === chiTietSanPhamDaChon?.tenSanPham
    );
  }, [chiTietSanPhamDaChon, ketQuaBienTheSanPham]);

  const luaChonMauSac = useMemo(() => {
    const grouped = new Map();
    for (const variant of bienTheLienQuan) {
      const key = variant.mauSac || variant.maBienThe;
      if (!grouped.has(key)) {
        grouped.set(key, variant);
      }
    }
    return Array.from(grouped.values());
  }, [bienTheLienQuan]);

  const luaChonKichCo = useMemo(() => {
    return bienTheLienQuan.filter((variant) => {
      if (!mauSacDaChon) {
        return true;
      }
      return (variant.mauSac || variant.maBienThe) === mauSacDaChon;
    });
  }, [bienTheLienQuan, mauSacDaChon]);

  const bienTheDaChon = useMemo(() => {
    if (!chiTietSanPhamDaChon) {
      return null;
    }
    return bienTheLienQuan.find(
      (variant) => (
        mauSacDaChon
          ? (variant.mauSac || variant.maBienThe) === mauSacDaChon
          : true
      ) && (
        kichCoDaChon
          ? (variant.kichCo || "") === kichCoDaChon
          : true
      )
    ) || chiTietSanPhamDaChon;
  }, [chiTietSanPhamDaChon, bienTheLienQuan, mauSacDaChon, kichCoDaChon]);

  const chiTietDangChon = useMemo(() => bienTheDaChon || chiTietSanPhamDaChon, [bienTheDaChon, chiTietSanPhamDaChon]);
  const hinhAnhDangChon = useMemo(() => chiTietDangChon?.hinhAnh || chiTietSanPhamDaChon?.hinhAnh || "", [chiTietDangChon, chiTietSanPhamDaChon]);
  
  const soLuongTonKhaDungChiTiet = useMemo(() => {
    if (!chiTietDangChon) {
      return 0;
    }
    return soLuongConLai(chiTietDangChon.chiTietId, chiTietDangChon.soLuongTon);
  }, [chiTietDangChon, soLuongConLai]);

  const soLuongTonSauKhiChon = useMemo(
    () => Math.max(soLuongTonKhaDungChiTiet - soLuongDaChon, 0),
    [soLuongTonKhaDungChiTiet, soLuongDaChon]
  );

  const taiSanPham = useCallback(async (keyword, silent = false) => {
    const searchKeyword = keyword !== undefined ? keyword : tuKhoaSanPham;
    if (!silent) {
      setDangTaiSanPham(true);
    }
    try {
      const response = await timSanPhamTaiQuay(searchKeyword || "");
      const data = response?.data || response;
      setKetQuaBienTheSanPham(Array.isArray(data) ? data : []);
    } catch (error) {
      if (!silent) {
        showError(error instanceof Error ? error.message : "Không thể tìm sản phẩm");
      }
    } finally {
      if (!silent) {
        setDangTaiSanPham(false);
      }
    }
  }, [tuKhoaSanPham]);

  useEffect(() => {
    let chuKyTaiLaiTuDong = setInterval(() => {
      // Avoid referencing stale dangTaiSanPham state
      setDangTaiSanPham(prev => {
        if (!prev) {
          taiSanPham(undefined, true).catch(() => {});
        }
        return prev;
      });
    }, 5000);

    return () => clearInterval(chuKyTaiLaiTuDong);
  }, [taiSanPham]);

  const nhomBienTheSanPham = useCallback((products) => {
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
  }, [soLuongConLai]);

  const timBienTheChinhXacTheoMa = useCallback((products, keyword) => {
    const normalizedKeyword = String(keyword ?? "").trim().toLowerCase();
    if (!normalizedKeyword) {
      return null;
    }

    return products.find((product) =>
      [product.maBienThe, product.sku, product.chiTietId]
        .map((value) => String(value ?? "").trim().toLowerCase())
        .some((value) => value && value === normalizedKeyword)
    ) ?? null;
  }, []);

  const laySoLuongTonHienTai = useCallback((chiTietId, fallback) => {
    return ketQuaBienTheSanPham.find((product) => product.chiTietId === chiTietId)?.soLuongTon ?? fallback;
  }, [ketQuaBienTheSanPham]);

  const moChiTietSanPham = useCallback((product) => {
    setChiTietSanPhamDaChon(product);
    setMauSacDaChon(product.mauSac || product.maBienThe);
    setKichCoDaChon(product.kichCo || "");
    setSoLuongDaChon(1);
  }, []);

  const lamMoiBoLoc = useCallback(() => {
    setBoLocThuongHieuDaChon("");
    setBoLocDanhMucDaChon("");
    setBoLocMauSacDaChon("");
    setBoLocKichCoDaChon("");
    setGiaThapNhatDaChon(0);
    setGiaCaoNhatDaChon(giaCaoNhatCoSan);
    setTuKhoaSanPham("");
  }, [giaCaoNhatCoSan]);

  const dongChiTietSanPham = useCallback(() => {
    setChiTietSanPhamDaChon(null);
    setMauSacDaChon("");
    setKichCoDaChon("");
    setSoLuongDaChon(1);
    lamMoiBoLoc();
    setHienThiDanhSachSanPham(false);
  }, [lamMoiBoLoc]);

  const xuLyQuetQrSanPham = useCallback(async (rawValue) => {
    if (!rawValue) {
      return;
    }

    try {
      const keyword = trichXuatTuKhoaSanPhamTuQr(rawValue);
      setTuKhoaSanPham(keyword);
      
      if (typeof xoaPhanHoi === 'function') xoaPhanHoi();
      setDangTaiSanPham(true);
      
      const response = await timSanPhamTaiQuay(keyword);
      const products = Array.isArray(response?.data) ? response.data : (Array.isArray(response) ? response : []);
      setKetQuaBienTheSanPham(products);

      if (!products || products.length === 0) {
        showError(`Không tìm thấy sản phẩm hoặc sản phẩm đã hết hàng với mã: ${keyword}`);
        return;
      }

      const exactVariant = timBienTheChinhXacTheoMa(products, keyword);
      let variantToAdd = exactVariant;
      
      if (!variantToAdd && products.length > 0) {
        variantToAdd = products[0];
      }
      
      if (variantToAdd) {
        const result = themSanPham(variantToAdd, 1, {
          preserveProductSearch: true,
          scannedKeyword: keyword,
          scannedProducts: products,
        });
        
        if (!result) {
          setHienThiDanhSachSanPham(true);
          return;
        }

        setHienThiDanhSachSanPham(false);
        lamMoiBoLoc();

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
        return;
      }
    } catch (error) {
      const errMsg = error instanceof Error ? error.message : "Có lỗi khi xử lý mã quét";
      showError(errMsg);
    } finally {
      setDangTaiSanPham(false);
    }
  }, [xoaPhanHoi, themSanPham, timBienTheChinhXacTheoMa, lamMoiBoLoc]);

  const chonMauSac = useCallback((value) => {
    setMauSacDaChon(value);
    setKichCoDaChon(luaChonKichCo[0]?.kichCo || "");
    setSoLuongDaChon(1);
  }, [luaChonKichCo]);

  const chonKichCo = useCallback((value) => {
    setKichCoDaChon(value);
    setSoLuongDaChon(1);
  }, []);

  const chonBienThe = useCallback((variant) => {
    if (!variant) return;
    setMauSacDaChon(variant.mauSac || variant.maBienThe);
    setKichCoDaChon(variant.kichCo || "");
    setSoLuongDaChon(1);
  }, []);

  const giamSoLuongChiTiet = useCallback(() => {
    setSoLuongDaChon(prev => Math.max(prev - 1, 1));
  }, []);

  const tangSoLuongChiTiet = useCallback(() => {
    if (!bienTheDaChon) {
      return;
    }
    const soLuongToiDa = soLuongConLai(bienTheDaChon.chiTietId, bienTheDaChon.soLuongTon);
    setSoLuongDaChon(prev => Math.min(prev + 1, Math.max(soLuongToiDa, 1)));
  }, [bienTheDaChon, soLuongConLai]);

  const capNhatSoLuongChiTiet = useCallback((newQuantity) => {
    if (!bienTheDaChon) return;

    let newQuantityNum = parseInt(newQuantity, 10);
    if (isNaN(newQuantityNum) || newQuantityNum <= 0) {
      newQuantityNum = 1;
    }

    const soLuongToiDa = soLuongConLai(bienTheDaChon.chiTietId, bienTheDaChon.soLuongTon);
    if (newQuantityNum > soLuongToiDa) {
      setSoLuongDaChon(Math.max(soLuongToiDa, 1));
      showWarning(`Sản phẩm đã đạt giới hạn tồn kho (tối đa ${soLuongToiDa})`);
    } else {
      setSoLuongDaChon(newQuantityNum);
    }
  }, [bienTheDaChon, soLuongConLai]);

  const moDanhSachSanPham = useCallback(() => {
    setHienThiDanhSachSanPham(true);
    taiSanPham(tuKhoaSanPham);
  }, [taiSanPham, tuKhoaSanPham]);

  const dongDanhSachSanPham = useCallback(() => {
    window.setTimeout(() => {
      setHienThiDanhSachSanPham(false);
    }, 150);
  }, []);

  const xoaBoDemThoiGianSanPham = useCallback(() => {
    if (boDemThoiGianSanPham.current) {
      window.clearTimeout(boDemThoiGianSanPham.current);
    }
  }, []);

  useEffect(() => {
    xoaBoDemThoiGianSanPham();
    setTrangHienTai(1);
    setHienThiDanhSachSanPham(tuKhoaSanPham.trim().length > 0);
    boDemThoiGianSanPham.current = window.setTimeout(() => {
      taiSanPham(tuKhoaSanPham);
    }, 250);
  }, [tuKhoaSanPham, xoaBoDemThoiGianSanPham, taiSanPham]);

  const themTrucTiepBienThe = useCallback((product) => {
    if (!product) return;
    const result = themSanPham(product, 1, {
      preserveProductSearch: true,
      scannedKeyword: product.maBienThe,
      scannedProducts: [product]
    });
    
    setHienThiDanhSachSanPham(false);
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
  }, [themSanPham, lamMoiBoLoc]);

  return {
    tuKhoaSanPham, setTuKhoaSanPham,
    ketQuaBienTheSanPham,
    chiTietSanPhamDaChon, setChiTietSanPhamDaChon,
    mauSacDaChon, setMauSacDaChon,
    kichCoDaChon, setKichCoDaChon,
    soLuongDaChon, setSoLuongDaChon,
    dangTaiSanPham, setDangTaiSanPham,
    hienThiDanhSachSanPham, setHienThiDanhSachSanPham,
    nhanTimKiemSanPham,
    ketQuaSanPham,
    sanPhamPhanTrang,
    trangHienTai, setTrangHienTai,
    kichThuocTrang, setKichThuocTrang,
    tongSoMuc,
    tongSoTrang,
    boLocThuongHieuDaChon, setBoLocThuongHieuDaChon,
    boLocDanhMucDaChon, setBoLocDanhMucDaChon,
    boLocMauSacDaChon, setBoLocMauSacDaChon,
    boLocKichCoDaChon, setBoLocKichCoDaChon,
    giaThapNhatDaChon, setGiaThapNhatDaChon,
    giaCaoNhatDaChon, setGiaCaoNhatDaChon,
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
