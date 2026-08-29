import { computed, ref, watch } from "vue";
import {
  apDungPhieuGiamGiaTaiQuay,
  timPhieuGiamGiaTaiQuay
} from "../../services/ban-hang-tai-quay";
import { showError, showWarning, showSuccess, showPaymentConfirmWithCoupon } from "../../utils/alert";
import { dinhDangTien } from "./TienTe";

export function LogicPhieuGiamGia({
  cartItems,
  tongTien,
  hoaDonChoDaChon,
  dangTaiChiTietHoaDon,
  khachHangDuocChon,
  layIdKhachHangHienTai,
  taoDanhSachSanPhamThanhToan,
  capNhatTienKhachThanhToan,
  xoaPhanHoi,
  luuHoaDonHienTai
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
          if (coupon.giamToiDa && giamToiDaNum > 0 && calculated > giamToiDaNum) {
              calculated = giamToiDaNum;
          }
          return Math.min(calculated, amountNum);
      }
      return Math.min(giaTriNum, amountNum);
  }

  const ketQuaTimKiemPhieuDaSapXep = computed(() => {
    return [...ketQuaTimKiemPhieu.value].sort((a, b) => {
      const validA = tongTien.value >= (a.giaTriToiThieu || 0);
      const validB = tongTien.value >= (b.giaTriToiThieu || 0);
      if (validA !== validB) return validA ? -1 : 1;
      
      const getDiscount = (c) => tinhToanGiamGia(c, tongTien.value);
      return getDiscount(b) - getDiscount(a);
    });
  });

  let boDemThoiGianPhieu;
  let boDemThoiGianDanhSachPhieu;

  const tienGiam = computed(() => {
    if (phieuGiamGiaDaApDung.value) {
      return tinhToanGiamGia(phieuGiamGiaDaApDung.value, tongTien.value);
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
        phieuGiamGiaDaApDung.value?.ma?.toLowerCase() !== maPhieuGiamGia.value.trim().toLowerCase()
      )
  );

  function danhDauCanApDungLaiPhieu() {
    if (!maPhieuGiamGia.value.trim() && !phieuGiamGiaDaApDung.value) {
      phieuGiamGiaDaApDung.value = null;
      return;
    }
    void kiemTraLaiPhieuDangApDung(false);
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

  async function xuLyApDungPhieu(isAutoRefetch = false, manualCouponCode = null) {
    const maPhieuDeApDung = (manualCouponCode || maPhieuGiamGia.value).trim();
    if (!maPhieuDeApDung || cartItems.value.length === 0 || dangApDungPhieu.value) {
      if (maPhieuDeApDung && cartItems.value.length === 0 && !isAutoRefetch) {
        showError("Vui lòng thêm sản phẩm vào hóa đơn trước khi áp dụng mã");
      }
      return;
    }

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
      if (!isAutoRefetch) {
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
         const currentList = tatCaPhieuKhaDung.value || [];
         const isSame = currentList.length === (ketQua || []).length &&
           currentList.every((item, idx) => item.id === ketQua[idx].id && item.ma === ketQua[idx].ma && item.giaTri === ketQua[idx].giaTri);
         if (!isSame) {
           tatCaPhieuKhaDung.value = ketQua || [];
         }
     } catch (e) {
         console.error("taiTatCaPhieuKhaDung error:", e);
     }
  }

  const phieuTotHonDeXuat = ref(null);
  const danhSachPhieuTotHonDaTuChoi = ref(new Set());

  function tuChoiPhieuTotHon(code) {
    const maTuChoi = code || phieuTotHonDeXuat.value?.ma;
    if (maTuChoi) {
      danhSachPhieuTotHonDaTuChoi.value.add(maTuChoi);
    }
    phieuTotHonDeXuat.value = null;
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
      
      // Popup CHỈ xuất hiện khi đơn hàng ĐÃ CÓ voucher áp dụng và có voucher khác giảm nhiều hơn
      if (phieuGiamGiaDaApDung.value && phieuGiamGiaDaApDung.value?.ma !== currentBest?.ma && currentBestDiscount > currentDiscount) {
        if (!danhSachPhieuTotHonDaTuChoi.value.has(currentBest.ma)) {
          return {
            coupon: currentBest,
            newDiscount: currentBestDiscount,
            oldDiscount: currentDiscount,
            hasOldCoupon: true
          };
        }
      }
    }
    
    return null;
  }

  async function tuDongApDungVaDeXuatHangMucTiepTheo(thongBaoKhiGo = false) {
      if (!tatCaPhieuKhaDung.value.length || !tongTien.value) {
          if (phieuGiamGiaHangMucTiepTheo.value !== null) {
              phieuGiamGiaHangMucTiepTheo.value = null;
          }
          if (soTienThieuDeDatHangMuc.value !== 0) {
              soTienThieuDeDatHangMuc.value = 0;
          }
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
      
      if (phieuGiamGiaHangMucTiepTheo.value?.ma !== foundNext?.ma) {
          phieuGiamGiaHangMucTiepTheo.value = foundNext;
      }
      if (soTienGiamCuaHangMucTiepTheo.value !== foundNextDiscount) {
          soTienGiamCuaHangMucTiepTheo.value = foundNextDiscount;
      }
      const missing = foundNext ? Math.max(0, (Number(foundNext.giaTriToiThieu) || 0) - Number(tongTien.value)) : 0;
      if (soTienThieuDeDatHangMuc.value !== missing) {
          soTienThieuDeDatHangMuc.value = missing;
      }
      
      if (missing > 0 && cartItems.value && cartItems.value.length > 0) {
          const cheapestItemPrice = Math.min(...cartItems.value.map(i => i.giaDonVi || i.giaBan || 0));
          const neededCount = cheapestItemPrice > 0 ? Math.ceil(missing / cheapestItemPrice) : 1;
          if (soSanPhamThieuDeDatHangMuc.value !== neededCount) {
              soSanPhamThieuDeDatHangMuc.value = neededCount;
          }
      } else {
          if (soSanPhamThieuDeDatHangMuc.value !== 0) {
              soSanPhamThieuDeDatHangMuc.value = 0;
          }
      }
      
      if (currentBest) {
          const currentDiscount = phieuGiamGiaDaApDung.value ? tinhToanGiamGia(phieuGiamGiaDaApDung.value, tongTien.value) : 0;
          const bestDiscount = tinhToanGiamGia(currentBest, tongTien.value);

          if (!phieuGiamGiaDaApDung.value) {
              if (!danhSachPhieuTotHonDaTuChoi.value.has(currentBest.ma)) {
                  maPhieuGiamGia.value = currentBest.ma;
                  phieuGiamGiaDaApDung.value = {
                      ...currentBest,
                      soTienGiam: bestDiscount
                  };
                  capNhatTienKhachThanhToan();
              }
          } else {
              // Giữ nguyên voucher đã áp dụng, chỉ cập nhật lại số tiền giảm theo giá trị đơn hàng hiện tại
              if (Number(phieuGiamGiaDaApDung.value.soTienGiam) !== Number(currentDiscount)) {
                  phieuGiamGiaDaApDung.value = {
                      ...phieuGiamGiaDaApDung.value,
                      soTienGiam: currentDiscount
                  };
                  capNhatTienKhachThanhToan();
              }
          }
      } else {
         if (phieuGiamGiaDaApDung.value && !dangApDungPhieu.value) {
             const maBiGo = phieuGiamGiaDaApDung.value?.ma;
             maPhieuGiamGia.value = "";
             xuLyGoPhieu();
             if (thongBaoKhiGo && maBiGo) {
                 showWarning(`Phiếu giảm giá "${maBiGo}" đã ngừng hoạt động hoặc không còn đủ điều kiện áp dụng, hệ thống đã tự động gỡ bỏ.`);
                 if (hoaDonChoDaChon.value && typeof luuHoaDonHienTai === 'function') {
                     setTimeout(() => {
                         void luuHoaDonHienTai(true);
                     }, 50);
                 }
             }
         }
      }
  }

  function xuLyGoPhieu() {
    if (phieuGiamGiaDaApDung.value?.ma) {
      danhSachPhieuTotHonDaTuChoi.value.add(phieuGiamGiaDaApDung.value.ma);
    }
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
      if (hienThiDanhSachPhieu.value) {
        boDemThoiGianPhieu = window.setTimeout(() => {
          void timKiemPhieu("");
        }, 250);
      }
      return;
    }
    if (phieuGiamGiaDaApDung.value && phieuGiamGiaDaApDung.value?.ma?.toLowerCase() !== trimmed.toLowerCase()) {
      phieuGiamGiaDaApDung.value = null;
    }
    boDemThoiGianPhieu = window.setTimeout(() => {
      void timKiemPhieu(value);
    }, 250);
  });

  watch(hoaDonChoDaChon, (newInv, oldInv) => {
    if (newInv?.id !== oldInv?.id) {
      danhSachPhieuTotHonDaTuChoi.value.clear();
    }
  });

  let dangKiemTraPhieu = false;
  async function kiemTraLaiPhieuDangApDung(thongBaoKhiDoi = true) {
    if (dangKiemTraPhieu) return;
    dangKiemTraPhieu = true;
    try {
      const ma = phieuGiamGiaDaApDung.value?.ma || maPhieuGiamGia.value?.trim();
      let daGoPhieu = false;

      if (ma && cartItems.value.length > 0 && tongTien.value > 0) {
        const cu = phieuGiamGiaDaApDung.value ? { ...phieuGiamGiaDaApDung.value } : null;
        const ketQua = await timPhieuGiamGiaTaiQuay({
          keyword: ma,
          hoaDonId: hoaDonChoDaChon.value?.id ?? null,
          khachHangId: layIdKhachHangHienTai(),
          tongTienHang: tongTien.value
        });
        const moi = (ketQua || []).find(c => String(c.ma).toUpperCase() === String(ma).toUpperCase());
        
        if (!moi) {
          // Phiếu đã bị ngừng hoạt động, hết hạn, hoặc sửa thông tin khiến không còn đủ điều kiện áp dụng
          phieuGiamGiaDaApDung.value = null;
          maPhieuGiamGia.value = "";
          daGoPhieu = true;
          capNhatTienKhachThanhToan();
          if (thongBaoKhiDoi) {
            showWarning(`Phiếu giảm giá "${ma}" đã ngừng hoạt động hoặc không còn đủ điều kiện áp dụng, hệ thống đã tự động gỡ bỏ.`);
          }
          if (hoaDonChoDaChon.value && typeof luuHoaDonHienTai === 'function') {
            setTimeout(() => {
              void luuHoaDonHienTai(true);
            }, 50);
          }
        } else {
          // Phiếu vẫn đủ điều kiện -> kiểm tra xem có thay đổi thông tin / giá trị giảm không
          const cuGiam = cu ? Number(cu.soTienGiam != null ? cu.soTienGiam : tinhToanGiamGia(cu, tongTien.value)) : 0;
          const moiGiam = Number(moi.soTienGiam != null ? moi.soTienGiam : tinhToanGiamGia(moi, tongTien.value));
          const cuGiaTri = Number(cu?.giaTri || 0);
          const moiGiaTri = Number(moi.giaTri || 0);
          const cuLoai = cu?.loai;
          const moiLoai = moi.loai;
          const cuGiamToiDa = Number(cu?.giamToiDa || 0);
          const moiGiamToiDa = Number(moi?.giamToiDa || 0);

          const isConfigChanged = cu && (
            cuGiaTri !== moiGiaTri ||
            cuLoai !== moiLoai ||
            cuGiamToiDa !== moiGiamToiDa ||
            cu.ma !== moi.ma
          );

          const isDiscountAmountChanged = Math.abs(cuGiam - moiGiam) >= 1;

          if (isConfigChanged || isDiscountAmountChanged) {
            phieuGiamGiaDaApDung.value = {
              ...moi,
              soTienGiam: moiGiam
            };
            maPhieuGiamGia.value = moi.ma;
            capNhatTienKhachThanhToan();

            if (thongBaoKhiDoi) {
              if (isDiscountAmountChanged) {
                showWarning(`Thông tin phiếu giảm giá "${moi.ma}" đã thay đổi. Số tiền giảm đã được cập nhật từ ${dinhDangTien(cuGiam)} thành ${dinhDangTien(moiGiam)}.`);
              } else if (isConfigChanged) {
                const moTaCu = cuLoai === 1 ? `${cuGiaTri}%` : dinhDangTien(cuGiaTri);
                const moTaMoi = moiLoai === 1 ? `${moiGiaTri}%` : dinhDangTien(moiGiaTri);
                showWarning(`Thông tin phiếu giảm giá "${moi.ma}" đã được cập nhật (${moTaCu} ➔ ${moTaMoi}).`);
              }
            }

            if (hoaDonChoDaChon.value && typeof luuHoaDonHienTai === 'function') {
              setTimeout(() => {
                void luuHoaDonHienTai(true);
              }, 50);
            }
          }
        }
      }

      await taiTatCaPhieuKhaDung();
      await tuDongApDungVaDeXuatHangMucTiepTheo(thongBaoKhiDoi && !daGoPhieu);
    } catch (e) {
      console.error("Lỗi kiểm tra lại phiếu giảm giá realtime:", e);
    } finally {
      dangKiemTraPhieu = false;
    }
  }

  watch([coTheTimPhieu, tongTien, khachHangDuocChon, () => hoaDonChoDaChon.value?.id], async ([coTheTim]) => {
    if (dangTaiChiTietHoaDon?.value) return;

    if (!coTheTim) {
      ketQuaTimKiemPhieu.value = [];
      hienThiDanhSachPhieu.value = false;
      if (phieuGiamGiaDaApDung.value) {
        const ma = phieuGiamGiaDaApDung.value?.ma;
        phieuGiamGiaDaApDung.value = null;
        maPhieuGiamGia.value = "";
        if (cartItems.value.length > 0 && ma) {
          showWarning(`Đơn hàng không đủ điều kiện áp dụng phiếu giảm giá "${ma}" nữa.`);
        }
        capNhatTienKhachThanhToan();
        if (hoaDonChoDaChon.value && typeof luuHoaDonHienTai === 'function') {
          setTimeout(() => {
            void luuHoaDonHienTai(true);
          }, 50);
        }
      }
      
      await taiTatCaPhieuKhaDung();
      await tuDongApDungVaDeXuatHangMucTiepTheo();
      return;
    }
    
    // Auto re-eval coupons when cart total changes
    await kiemTraLaiPhieuDangApDung(false);
    
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
      kiemTraPhieuTotHonTruocThanhToan,
      kiemTraLaiPhieuDangApDung
    };
}
