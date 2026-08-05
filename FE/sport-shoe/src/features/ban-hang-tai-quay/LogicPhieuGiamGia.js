import { computed, ref, watch } from "vue";
import {
  apDungPhieuGiamGiaTaiQuay,
  timPhieuGiamGiaTaiQuay
} from "../../services/ban-hang-tai-quay";
import { showError, showWarning, showSuccess } from "../../utils/alert";

export function LogicPhieuGiamGia({
  cartItems,
  tongTien,
  hoaDonChoDaChon,
  khachHangDuocChon,
  layIdKhachHangHienTai,
  taoDanhSachSanPhamThanhToan,
  capNhatTienKhachThanhToan,
  xoaPhanHoi
}) {
  const maPhieuGiamGia = ref("");
  const phieuGiamGiaDaApDung = ref(null);
  const dangApDungPhieu = ref(false);

  const ketQuaTimKiemPhieu = ref([]);
  const dangTaiPhieu = ref(false);
  const hienThiDanhSachPhieu = ref(false);

  const tatCaPhieuKhaDung = ref([]);
  const phieuGiamGiaHangMucTiepTheo = ref(null);
  const soTienThieuDeDatHangMuc = ref(0);
  const soSanPhamThieuDeDatHangMuc = ref(0);
  const soTienGiamCuaHangMucTiepTheo = ref(0);

  function tinhToanGiamGia(coupon, amount) {
      if (!coupon) return 0;
      const amountNum = Number(amount) || 0;
      const giaTriNum = Number(coupon.giaTri) || 0;
      if (coupon.loai === 1) { 
          let calculated = (amountNum * giaTriNum) / 100;
          const giamToiDaNum = Number(coupon.giamToiDa) || 0;
          if (coupon.giamToiDa && calculated > giamToiDaNum) {
              calculated = giamToiDaNum;
          }
          return calculated;
      }
      return giaTriNum;
  }

  const ketQuaTimKiemPhieuDaSapXep = computed(() => {
    return [...ketQuaTimKiemPhieu.value].sort((a, b) => {
      const validA = tongTien.value >= (a.giaTriToiThieu || 0);
      const validB = tongTien.value >= (b.giaTriToiThieu || 0);
      if (validA !== validB) return validA ? -1 : 1;
      
      const getDiscount = (c) => {
         if (c.soTienGiam != null) return c.soTienGiam;
         return tinhToanGiamGia(c, tongTien.value);
      };
      return getDiscount(b) - getDiscount(a);
    });
  });

  let boDemThoiGianPhieu;
  let boDemThoiGianDanhSachPhieu;

  const tienGiam = computed(() => {
    if (phieuGiamGiaDaApDung.value?.soTienGiam != null) {
      return phieuGiamGiaDaApDung.value.soTienGiam;
    }
    // If we loaded a pending invoice with a coupon, but haven't re-applied it yet, use its saved discount
    if (maPhieuGiamGia.value && hoaDonChoDaChon.value?.tienGiam != null) {
      return hoaDonChoDaChon.value.tienGiam;
    }
    return 0;
  });
  const tongTienSauGiamHienThi = computed(() => Math.max(tongTien.value - tienGiam.value, 0));
  const maPhieuChuaApDung = computed(
    () => Boolean(maPhieuGiamGia.value.trim()) && !phieuGiamGiaDaApDung.value
  );
  const coTheTimPhieu = computed(() => cartItems.value.length > 0 && tongTien.value > 0);
  const phieuGiamGiaHopLeDangNhap = computed(() => {
    const keyword = maPhieuGiamGia.value.trim().toLowerCase();
    if (!keyword) {
      return null;
    }

    const trungKhopChinhXac = ketQuaTimKiemPhieu.value.find((coupon) => {
      const ma = coupon.ma?.trim().toLowerCase() ?? "";
      const ten = coupon.ten?.trim().toLowerCase() ?? "";
      return ma === keyword || ten === keyword;
    });

    if (trungKhopChinhXac) {
      return trungKhopChinhXac;
    }

    if (ketQuaTimKiemPhieu.value.length === 1) {
      return ketQuaTimKiemPhieu.value[0];
    }

    return null;
  });
  const coTheApDungPhieu = computed(
    () => Boolean(maPhieuGiamGia.value.trim()) &&
      cartItems.value.length > 0 &&
      !dangApDungPhieu.value &&
      (
        !phieuGiamGiaDaApDung.value ||
        phieuGiamGiaDaApDung.value.ma.toLowerCase() !== maPhieuGiamGia.value.trim().toLowerCase()
      )
  );

  function danhDauCanApDungLaiPhieu() {
    if (!maPhieuGiamGia.value.trim()) {
      phieuGiamGiaDaApDung.value = null;
      return;
    }
    phieuGiamGiaDaApDung.value = null;
  }

  async function timKiemPhieu(keyword) {
    if (!coTheTimPhieu.value) {
      ketQuaTimKiemPhieu.value = [];
      return;
    }
    dangTaiPhieu.value = true;
    try {
      ketQuaTimKiemPhieu.value = await timPhieuGiamGiaTaiQuay({
        keyword,
        hoaDonId: hoaDonChoDaChon.value?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        tongTienHang: tongTien.value
      });
    } catch (error) {
      ketQuaTimKiemPhieu.value = [];
      showError(error instanceof Error ? error.message : "Không thể tìm phiếu giảm giá");
    } finally {
      dangTaiPhieu.value = false;
    }
  }

  function xuLyKhiFocusPhieu() {
    if (boDemThoiGianDanhSachPhieu) {
      window.clearTimeout(boDemThoiGianDanhSachPhieu);
    }
    hienThiDanhSachPhieu.value = true;
    void timKiemPhieu(maPhieuGiamGia.value);
  }

  function xuLyKhiBlurPhieu() {
    if (boDemThoiGianDanhSachPhieu) {
      window.clearTimeout(boDemThoiGianDanhSachPhieu);
    }
    boDemThoiGianDanhSachPhieu = window.setTimeout(() => {
      hienThiDanhSachPhieu.value = false;
    }, 150);
  }

  function chonPhieuGiamGia(coupon) {
    if (boDemThoiGianDanhSachPhieu) {
      window.clearTimeout(boDemThoiGianDanhSachPhieu);
    }
    maPhieuGiamGia.value = coupon.ma;
    hienThiDanhSachPhieu.value = false;
    xoaPhanHoi();
  }

  async function timPhieuPhuHopDeApDung() {
    const keyword = maPhieuGiamGia.value.trim();
    if (!keyword || !coTheTimPhieu.value) {
      return null;
    }

    const keywordDaChuanHoa = keyword.toLowerCase();
    let ketQua = ketQuaTimKiemPhieu.value;

    if (!ketQua.length) {
      try {
        ketQua = await timPhieuGiamGiaTaiQuay({
          keyword,
          hoaDonId: hoaDonChoDaChon.value?.id ?? null,
          khachHangId: layIdKhachHangHienTai(),
          tongTienHang: tongTien.value
        });
        ketQuaTimKiemPhieu.value = ketQua;
      } catch (error) {
        showError(error instanceof Error ? error.message : "Khong the tim phieu giam gia");
        return null;
      }
    }

    const trungKhopChinhXac = ketQua.find((coupon) => {
      const ma = coupon.ma?.trim().toLowerCase() ?? "";
      const ten = coupon.ten?.trim().toLowerCase() ?? "";
      return ma === keywordDaChuanHoa || ten === keywordDaChuanHoa;
    });

    if (trungKhopChinhXac) {
      return trungKhopChinhXac;
    }

    if (ketQua.length === 1) {
      return ketQua[0];
    }

    return null;
  }

  async function xuLyApDungPhieu(isAutoRefetch = false) {
    if (!coTheApDungPhieu.value) {
      if (maPhieuGiamGia.value.trim() && cartItems.value.length === 0) {
        showError("Vui lòng thêm sản phẩm vào hóa đơn trước khi áp dụng mã");
      }
      return;
    }

    const maPhieuDeApDung = maPhieuGiamGia.value.trim();

    dangApDungPhieu.value = true;
    try {
      const coupon = await apDungPhieuGiamGiaTaiQuay({
        hoaDonId: hoaDonChoDaChon.value?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        maPhieuGiamGia: maPhieuDeApDung,
        items: taoDanhSachSanPhamThanhToan()
      });
      phieuGiamGiaDaApDung.value = coupon;
      if (!isAutoRefetch) {
        showSuccess(`Áp dụng mã ${coupon.ma} thành công`);
      }
      maPhieuGiamGia.value = coupon.ma;
      ketQuaTimKiemPhieu.value = [];
      hienThiDanhSachPhieu.value = false;
      capNhatTienKhachThanhToan();
    } catch (error) {
      if (dangApDungPhieu.value) { // only show error if initiated manually
        phieuGiamGiaDaApDung.value = null;
        showError(error instanceof Error ? error.message : "Không thể áp dụng phiếu giảm giá");
      }
    } finally {
      dangApDungPhieu.value = false;
    }
  }

  async function taiTatCaPhieuKhaDung() {
     try {
         const ketQua = await timPhieuGiamGiaTaiQuay({
            keyword: "",
            hoaDonId: hoaDonChoDaChon.value?.id ?? null,
            khachHangId: layIdKhachHangHienTai(),
            tongTienHang: 999999999 // Get all
         });
         console.log("taiTatCaPhieuKhaDung result:", ketQua);
         tatCaPhieuKhaDung.value = ketQua;
     } catch (e) {
         console.error("taiTatCaPhieuKhaDung error:", e);
         tatCaPhieuKhaDung.value = [];
     }
  }

  const phieuTotHonDeXuat = ref(null);
  const danhSachPhieuTotHonDaTuChoi = ref(new Set());

  function tuChoiPhieuTotHon() {
    if (phieuTotHonDeXuat.value) {
      danhSachPhieuTotHonDaTuChoi.value.add(phieuTotHonDeXuat.value.ma);
      phieuTotHonDeXuat.value = null;
    }
  }

  function chapNhanPhieuTotHon() {
    if (phieuTotHonDeXuat.value) {
      maPhieuGiamGia.value = phieuTotHonDeXuat.value.ma;
      phieuTotHonDeXuat.value = null;
      if (!dangApDungPhieu.value) {
         void xuLyApDungPhieu();
      }
    }
  }

  async function kiemTraPhieuTotHonTruocThanhToan() {
    // Fetch latest coupons just to be sure
    await taiTatCaPhieuKhaDung();
    
    if (!tatCaPhieuKhaDung.value.length || !tongTien.value) {
      return null;
    }
    
    const eligible = tatCaPhieuKhaDung.value.filter(c => tongTien.value >= (c.giaTriToiThieu || 0));
    let currentBest = null;
    
    if (eligible.length > 0) {
      eligible.sort((a, b) => tinhToanGiamGia(b, tongTien.value) - tinhToanGiamGia(a, tongTien.value));
      currentBest = eligible[0];
    }
    
    if (currentBest) {
      const currentBestDiscount = tinhToanGiamGia(currentBest, tongTien.value);
      const currentDiscount = phieuGiamGiaDaApDung.value ? (Number(tienGiam.value) || 0) : 0;
      
      if (currentBestDiscount > currentDiscount && (!phieuGiamGiaDaApDung.value || phieuGiamGiaDaApDung.value.ma !== currentBest.ma)) {
        if (!danhSachPhieuTotHonDaTuChoi.value.has(currentBest.ma)) {
          return {
            coupon: currentBest,
            newDiscount: currentBestDiscount,
            oldDiscount: currentDiscount
          };
        }
      }
    }
    
    return null;
  }

  function tuDongApDungVaDeXuatHangMucTiepTheo() {
      console.log("=== autoApplyAndSuggestNextTier CALLED ===");
      console.log("tongTien:", tongTien.value);

      if (!tatCaPhieuKhaDung.value.length || !tongTien.value) {
          phieuGiamGiaHangMucTiepTheo.value = null;
          soTienThieuDeDatHangMuc.value = 0;
          return;
      }
      
      const eligible = tatCaPhieuKhaDung.value.filter(c => tongTien.value >= (c.giaTriToiThieu || 0));
      let currentBest = null;
      let currentBestDiscount = 0;
      
      if (eligible.length > 0) {
          eligible.sort((a, b) => tinhToanGiamGia(b, tongTien.value) - tinhToanGiamGia(a, tongTien.value));
          currentBest = eligible[0];
          currentBestDiscount = tinhToanGiamGia(currentBest, tongTien.value);
      }
      
      currentBestDiscount = Math.max(currentBestDiscount, Number(tienGiam.value) || 0);
      
      const higher = tatCaPhieuKhaDung.value.filter(c => (Number(c.giaTriToiThieu) || 0) > Number(tongTien.value));
      higher.sort((a, b) => (Number(a.giaTriToiThieu) || 0) - (Number(b.giaTriToiThieu) || 0));
      
      let foundNext = null;
      let foundNextDiscount = 0;
      for (const c of higher) {
          const potentialDiscount = tinhToanGiamGia(c, Number(c.giaTriToiThieu) || 0);
          if (potentialDiscount > currentBestDiscount) {
              foundNext = c;
              foundNextDiscount = potentialDiscount;
              break;
          }
      }
      if (!foundNext && higher.length > 0) {
          foundNext = higher[0];
          foundNextDiscount = tinhToanGiamGia(foundNext, Number(foundNext.giaTriToiThieu) || 0);
      }
      
      phieuGiamGiaHangMucTiepTheo.value = foundNext;
      soTienGiamCuaHangMucTiepTheo.value = foundNextDiscount;
      soTienThieuDeDatHangMuc.value = foundNext ? Math.max(0, (Number(foundNext.giaTriToiThieu) || 0) - Number(tongTien.value)) : 0;
      
      if (soTienThieuDeDatHangMuc.value > 0 && cartItems.value && cartItems.value.length > 0) {
          const cheapestItemPrice = Math.min(...cartItems.value.map(i => i.giaDonVi || 0));
          soSanPhamThieuDeDatHangMuc.value = cheapestItemPrice > 0 
              ? Math.ceil(soTienThieuDeDatHangMuc.value / cheapestItemPrice) 
              : 1;
      } else {
          soSanPhamThieuDeDatHangMuc.value = 0;
      }
      
      if (currentBest) {
          if (!phieuGiamGiaDaApDung.value) {
              if (!danhSachPhieuTotHonDaTuChoi.value.has(currentBest.ma)) {
                  maPhieuGiamGia.value = currentBest.ma;
                  if (!dangApDungPhieu.value) {
                      void xuLyApDungPhieu();
                  }
              }
          } else if (phieuGiamGiaDaApDung.value.ma !== currentBest.ma) {
              const currentDiscount = tinhToanGiamGia(phieuGiamGiaDaApDung.value, tongTien.value);
              const newDiscount = tinhToanGiamGia(currentBest, tongTien.value);
              
              if (newDiscount > currentDiscount && !danhSachPhieuTotHonDaTuChoi.value.has(currentBest.ma)) {
                  phieuTotHonDeXuat.value = currentBest;
              }
          }
      } else {
         if (phieuGiamGiaDaApDung.value && !dangApDungPhieu.value) {
             maPhieuGiamGia.value = "";
             xuLyGoPhieu();
         }
      }
  }

  function xuLyGoPhieu() {
    maPhieuGiamGia.value = "";
    phieuGiamGiaDaApDung.value = null;
    ketQuaTimKiemPhieu.value = [];
    capNhatTienKhachThanhToan();
    xoaPhanHoi();
  }

  function xoaCacBoDemThoiGianPhieu() {
    if (boDemThoiGianPhieu) {
      window.clearTimeout(boDemThoiGianPhieu);
    }
    if (boDemThoiGianDanhSachPhieu) {
      window.clearTimeout(boDemThoiGianDanhSachPhieu);
    }
  }

  watch(maPhieuGiamGia, (value) => {
    if (boDemThoiGianPhieu) {
      window.clearTimeout(boDemThoiGianPhieu);
    }
    const trimmed = value.trim();
    if (!trimmed) {
      phieuGiamGiaDaApDung.value = null;
      if (hienThiDanhSachPhieu.value) {
        boDemThoiGianPhieu = window.setTimeout(() => {
          void timKiemPhieu("");
        }, 250);
      }
      return;
    }
    if (phieuGiamGiaDaApDung.value && phieuGiamGiaDaApDung.value.ma.toLowerCase() !== trimmed.toLowerCase()) {
      phieuGiamGiaDaApDung.value = null;
    }
    boDemThoiGianPhieu = window.setTimeout(() => {
      void timKiemPhieu(value);
    }, 250);
  });

  watch([coTheTimPhieu, tongTien, khachHangDuocChon, hoaDonChoDaChon, phieuGiamGiaDaApDung], async ([coTheTim]) => {
    if (!coTheTim) {
      ketQuaTimKiemPhieu.value = [];
      hienThiDanhSachPhieu.value = false;
      if (phieuGiamGiaDaApDung.value) {
        const ma = phieuGiamGiaDaApDung.value.ma;
        phieuGiamGiaDaApDung.value = null;
        maPhieuGiamGia.value = "";
        showWarning(`Đơn hàng không đủ điều kiện áp dụng phiếu giảm giá ${ma} nữa.`);
        capNhatTienKhachThanhToan();
      }
      
      await taiTatCaPhieuKhaDung();
      tuDongApDungVaDeXuatHangMucTiepTheo();
      return;
    }
    
    // Auto re-eval coupons when cart total changes
    await taiTatCaPhieuKhaDung();
    tuDongApDungVaDeXuatHangMucTiepTheo();
    
    // Validate if the currently applied coupon is still valid
    if (phieuGiamGiaDaApDung.value) {
      try {
        const ketQua = await timPhieuGiamGiaTaiQuay({
          keyword: phieuGiamGiaDaApDung.value.ma,
          hoaDonId: hoaDonChoDaChon.value?.id ?? null,
          khachHangId: layIdKhachHangHienTai(),
          tongTienHang: tongTien.value
        });
        const isValid = ketQua.some(c => c.ma === phieuGiamGiaDaApDung.value.ma);
        if (!isValid) {
           const ma = phieuGiamGiaDaApDung.value.ma;
           phieuGiamGiaDaApDung.value = null;
           maPhieuGiamGia.value = "";
           showWarning(`Phiếu giảm giá ${ma} không còn hợp lệ. Hệ thống đã tự động gỡ bỏ phiếu.`);
           capNhatTienKhachThanhToan();
        }
      } catch (e) {
        // ignore
      }
    }
    
    if (!maPhieuGiamGia.value.trim() && !hienThiDanhSachPhieu.value) {
      return;
    }
    void timKiemPhieu(maPhieuGiamGia.value);
  });

    return {
      maPhieuGiamGia,
      phieuGiamGiaDaApDung,
      dangApDungPhieu,
      ketQuaTimKiemPhieu: ketQuaTimKiemPhieuDaSapXep,
      dangTaiPhieu,
      hienThiDanhSachPhieu,
      tienGiam,
      tongTienSauGiamHienThi,
      maPhieuChuaApDung,
      coTheTimPhieu,
      coTheApDungPhieu,
      danhDauCanApDungLaiPhieu,
      timKiemPhieu,
      xuLyKhiFocusPhieu,
      xuLyKhiBlurPhieu,
      chonPhieuGiamGia,
      xuLyApDungPhieu,
      xuLyGoPhieu,
      xoaCacBoDemThoiGianPhieu,
      phieuGiamGiaHangMucTiepTheo,
      soTienThieuDeDatHangMuc,
      soSanPhamThieuDeDatHangMuc,
      soTienGiamCuaHangMucTiepTheo,
      phieuTotHonDeXuat,
      tuChoiPhieuTotHon,
      chapNhanPhieuTotHon,
      kiemTraPhieuTotHonTruocThanhToan
    };
}
