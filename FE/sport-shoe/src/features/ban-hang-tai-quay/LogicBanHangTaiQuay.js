import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import {
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  thanhToanTaiQuay,
  taoHoaDonCho,
  capNhatHoaDonCho,
  timSanPhamTaiQuay
} from "../../services/ban-hang-tai-quay";
import {
  KHACH_VANG_LAI,
  TOI_DA_HOA_DON_CHO,
} from "./HangSo";
import { dinhDangTien } from "./TienTe";
import { LogicGioHang } from "./LogicGioHang";
import { LogicPhieuGiamGia } from "./LogicPhieuGiamGia";
import { LogicKhachHang } from "./LogicKhachHang";
import { LogicInHoaDon } from "./LogicInHoaDon";
import { LogicThanhToan } from "./LogicThanhToan";
import { LogicSanPham } from "./LogicSanPham";
import { LogicGiaoHang } from "./LogicGiaoHang";
import { showConfirm, showToastSuccess, showError, toastSwal } from "../../utils/alert";



function LogicBanHangTaiQuay() {
  const danhSachHoaDonCho = ref([]);
  const hoaDonChoDaChon = ref(null);
  const dangTaiHoaDonCho = ref(false);
  const dangLuuHoaDonCho = ref(false);
  const dangHuyHoaDonCho = ref(false);
  const dangThanhToan = ref(false);
  const dangTaiChiTietHoaDon = ref(false);
  const thongBaoLoi = ref("");
  const thongBaoThanhCong = ref("");
  const choPhepGiaoHang = ref(false);
  const tenNguoiNhanGiaoHang = ref("");
  const sdtNguoiNhanGiaoHang = ref("");
  const diaChiGiaoHang = ref("");
  const donViVanChuyen = ref("GHN");
  const phiVanChuyen = ref(0);
  const diaChiDaXacNhan = ref("");
  const daTinhPhiVanChuyen = ref(false);
  const dangTinhPhiVanChuyen = ref(false);
  const cauHinhGiaoHang = ref({
    serviceTypeId: 2,
    length: 30,
    width: 20,
    height: 12,
    weight: 500
  });

  const daDatGioiHanHoaDonCho = computed(
    () => danhSachHoaDonCho.value.length >= TOI_DA_HOA_DON_CHO
  );
  const coTheTaoHoaDonCho = computed(
    () => !dangLuuHoaDonCho.value &&
      !maPhieuChuaApDung.value &&
      !daDatGioiHanHoaDonCho.value &&
      coThongTinGiaoHangHopLe.value &&
      !sanPhamValidationMessage.value
  );
  const coTheThanhToan = computed(() => {
    if (!cartItems?.value?.length || sanPhamValidationMessage?.value || dangThanhToan?.value || maPhieuChuaApDung?.value || !coThongTinGiaoHangHopLe?.value) {
      return false;
    }
    if (phuongThucThanhToan?.value === 1) {
      return !thongBaoLoiThanhToan?.value;
    }
    return true;
  });
  const {
    tuKhoaKhachHang,
    ketQuaTimKiemKhachHang,
    khachHangDuocChon,
    dangTaiKhachHang,
    hienThiDanhSachKhachHang,
    laKhachVangLai,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi,
    timKiemKhachHang,
    chonKhachHang,
    boChonKhachHang,
    chonKhachVangLai,
    moDanhSachKhachHang,
    dongDanhSachKhachHang,
    xoaBoDemThoiGianKhachHang
  } = LogicKhachHang({
    hoaDonChoDaChon,
    tenNguoiNhanGiaoHang,
    sdtNguoiNhanGiaoHang,
    diaChiGiaoHang,
    danhDauCanApDungLaiPhieu: danhDauCanApDungLaiPhieu,
    xoaPhanHoi,
    thongBaoLoi
  });

  const daChonKhach = computed(() => {
    if (khachHangDuocChon.value) return true;
    if (laKhachVangLai.value) return true;
    if (hoaDonChoDaChon.value) {
      if (hoaDonChoDaChon.value.khachHangId) return true;
      if (hoaDonChoDaChon.value.tenKhachHang === KHACH_VANG_LAI) return true;
    }
    // Nếu keyword trống, mặc định coi như khách lẻ -> đã chọn
    if (!tuKhoaKhachHang.value.trim()) return true;
    return false;
  });

  const {
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
    capNhatSoLuong
  } = LogicGioHang({
    danhDauCanTinhLaiPhiVanChuyen,
    capNhatTienKhachThanhToan: capNhatTienKhachThanhToan,
    danhDauCanApDungLaiPhieu: danhDauCanApDungLaiPhieu,
    dongBoSanPhamSauKhiThemVaoGio: dongBoSanPhamSauKhiThemVaoGio,
    xoaPhanHoi: xoaPhanHoi
  });

  const {
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
  } = LogicGiaoHang({
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
    cartItems,
    thongBaoLoi
  });

  const {
    maPhieuGiamGia,
    phieuGiamGiaDaApDung,
    dangApDungPhieu,
    ketQuaTimKiemPhieu,
    dangTaiPhieu,
    hienThiDanhSachPhieu,
    tienGiam,
    tongTienSauGiamHienThi,
    maPhieuChuaApDung,
    coTheTimPhieu,
    coTheApDungPhieu,
    danhDauCanApDungLaiPhieu,
    xuLyKhiFocusPhieu,
    xuLyKhiBlurPhieu,
    chonPhieuGiamGia,
    xuLyApDungPhieu,
    xuLyGoPhieu,
    xoaCacBoDemThoiGianPhieu,
    tuDongApDungVaDeXuatHangMucTiepTheo
  } = LogicPhieuGiamGia({
    cartItems,
    tongTien,
    hoaDonChoDaChon,
    khachHangDuocChon,
    layIdKhachHangHienTai,
    taoDanhSachSanPhamThanhToan,
    capNhatTienKhachThanhToan: capNhatTienKhachThanhToan,
    thongBaoLoi,
    thongBaoThanhCong,
    xoaPhanHoi
  });

  const khachCanTra = computed(() => tongTienSauGiamHienThi.value + phiVanChuyenHienThi.value);

  const {
    phuongThucThanhToan,
    tienKhachDua,
    ghiChuThanhToan,
    tienKhachThanhToan,
    tienThua,
    thongBaoLoiThanhToan,
    capNhatTienKhachThanhToan,
    kiemTraLoiThanhToan,
    xuLyTienKhachDuaInput
  } = LogicThanhToan({
    cartItems,
    khachCanTra,
    thongBaoLoi,
    hoaDonChoDaChon
  });

  const {
    xuLyInHoaDonTaiQuay
  } = LogicInHoaDon();

  const {
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
    thuongHieuCoSan,
    danhMucCoSan,
    luaChonMauSac,
    luaChonKichCo,
    bienTheDaChon,
    chiTietDangChon,
    hinhAnhDangChon,
    soLuongTonKhaDungChiTiet,
    soLuongTonSauKhiChon,
    taiSanPham,
    laySoLuongTonHienTai,
    moChiTietSanPham,
    dongChiTietSanPham,
    xuLyQuetQrSanPham,
    chonMauSac,
    chonKichCo,
    giamSoLuongChiTiet,
    tangSoLuongChiTiet,
    moDanhSachSanPham,
    dongDanhSachSanPham,
    xoaBoDemThoiGianSanPham
  } = LogicSanPham({
    daChonKhach,
    soLuongConLai,
    themSanPham,
    xoaPhanHoi,
    thongBaoLoi,
    thongBaoThanhCong
  });

  function xoaPhanHoi() {
    thongBaoLoi.value = "";
    thongBaoThanhCong.value = "";
  }

  function layIdKhachHangHienTai() {
    if (khachHangDuocChon.value) {
      return khachHangDuocChon.value.id;
    }
    if (laKhachVangLai.value) {
      return null;
    }
    return hoaDonChoDaChon.value?.khachHangId ?? null;
  }


  function dongBoSanPhamSauKhiThemVaoGio({
    preserveProductSearch = false,
    scannedKeyword = "",
    scannedProducts = [],
  } = {}) {
    if (preserveProductSearch) {
      tuKhoaSanPham.value = scannedKeyword;
      ketQuaBienTheSanPham.value = scannedProducts;
    }
    chiTietSanPhamDaChon.value = null;
    mauSacDaChon.value = "";
    kichCoDaChon.value = "";
    soLuongDaChon.value = 1;
    hienThiDanhSachSanPham.value = false;
  }

  function xoaBanNhap() {
    khachHangDuocChon.value = null;
    tuKhoaKhachHang.value = "";
    tuKhoaSanPham.value = "";
    maPhieuGiamGia.value = "";
    ketQuaTimKiemKhachHang.value = [];
    ketQuaBienTheSanPham.value = [];
    ketQuaTimKiemPhieu.value = [];
    chiTietSanPhamDaChon.value = null;
    mauSacDaChon.value = "";
    kichCoDaChon.value = "";
    soLuongDaChon.value = 1;
    cartItems.value = [];
    hoaDonChoDaChon.value = null;
    phieuGiamGiaDaApDung.value = null;
    phuongThucThanhToan.value = 1;
    tienKhachDua.value = "";
    ghiChuThanhToan.value = "";
    choPhepGiaoHang.value = false;
    tenNguoiNhanGiaoHang.value = "";
    sdtNguoiNhanGiaoHang.value = "";
    diaChiGiaoHang.value = "";
    donViVanChuyen.value = "GHN";
    phiVanChuyen.value = 0;
    diaChiDaXacNhan.value = "";
    daTinhPhiVanChuyen.value = false;
    dangTinhPhiVanChuyen.value = false;
    cauHinhGiaoHang.value = {
      serviceTypeId: 2,
      length: 30,
      width: 20,
      height: 12,
      weight: 500
    };
    hienThiDanhSachKhachHang.value = false;
    hienThiDanhSachSanPham.value = false;
    hienThiDanhSachPhieu.value = false;
    xoaPhanHoi();
    void taiSanPham("");
  }

  async function taiDanhSachHoaDonCho() {
    dangTaiHoaDonCho.value = true;
    try {
      danhSachHoaDonCho.value = await layDanhSachHoaDonCho();
    } catch (error) {
      thongBaoLoi.value = error instanceof Error
        ? error.message
        : "Không thể tải danh sách hóa đơn chờ";
    } finally {
      dangTaiHoaDonCho.value = false;
    }
  }

  watch(thongBaoLoi, (message) => {
    if (!message) {
      return;
    }
    showError(message);
    thongBaoLoi.value = "";
  });

  watch(thongBaoThanhCong, (message) => {
    if (!message) {
      return;
    }
    showToastSuccess(message);
    thongBaoThanhCong.value = "";
  });

  async function themBienTheDangChon() {
    if (!bienTheDaChon.value) {
      if (typeof thongBaoLoi !== 'undefined') thongBaoLoi.value = "Vui lòng chọn màu sắc và kích cỡ phù hợp";
      return;
    }
    
    dangTaiSanPham.value = true;
    let productToAdd = { ...bienTheDaChon.value };
    
    try {
      // Fetch latest data to get the new price
      const products = await timSanPhamTaiQuay(chiTietSanPhamDaChon.value.maSanPham);
      const latestVariant = products.find(p => p.chiTietId === bienTheDaChon.value.chiTietId);
      
      if (latestVariant) {
        productToAdd = {
          ...latestVariant,
          soLuongTon: Math.max((latestVariant.soLuongTon || 0) - (soLuongDaChon.value || 1), 0)
        };
      }
    } catch (e) {
      console.warn("Could not fetch latest price, using cached data", e);
    } finally {
      dangTaiSanPham.value = false;
    }

    const result = themSanPham(productToAdd, soLuongDaChon.value);
    if (result) {
      dongChiTietSanPham();
      if (result.status === "price_updated") {
        const formatPrice = (price) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
        toastSwal.fire({
          icon: 'warning',
          title: 'Cập nhật giá',
          text: `Sản phẩm ${result.tenSanPham} có giá thay đổi từ ${formatPrice(result.oldPrice)} đến ${formatPrice(result.newPrice)}`,
          timer: 3000,
          iconColor: '#f59e0b'
        });
      } else {
        showToastSuccess(`Đã thêm ${soLuongDaChon.value} sản phẩm vào hóa đơn`);
      }
    }
  }

  function chuyenHoaDonThanhBanNhap(invoice) {
    const thongTinTheoChiTietId = new Map(
      ketQuaBienTheSanPham.value.map((product) => [product.chiTietId, product])
    );
    const thongTinGiaoHang = invoice.thongTinGiaoHang || null;

    tuKhoaKhachHang.value = invoice.tenKhachHang || invoice.soDienThoai || "";
    khachHangDuocChon.value = invoice.khachHangId
      ? {
        id: invoice.khachHangId,
        hoTen: invoice.tenKhachHang,
        sdt: invoice.soDienThoai,
        email: null
      }
      : null;
    dangLuuNoiBo = true;
    cartItems.value = invoice.items.map((item) => {
      const thongTinSanPham = thongTinTheoChiTietId.get(item.chiTietId);
      return {
        cartItemId: Date.now().toString() + Math.random().toString(),
        chiTietId: item.chiTietId,
        maSanPham: item.maSanPham,
        tenSanPham: item.tenSanPham,
        sku: item.sku || thongTinSanPham?.sku || "",
        mauSac: item.mauSac || thongTinSanPham?.mauSac || "",
        kichCo: item.kichCo || thongTinSanPham?.kichCo || "",
        hinhAnh: item.hinhAnh || thongTinSanPham?.hinhAnh || "",
        soLuong: item.soLuong,
        soLuongBanDau: item.soLuong,
        giaBan: item.giaBan,
        soLuongTon: laySoLuongTonHienTai(item.chiTietId, item.soLuong)
      };
    });
    setTimeout(() => { dangLuuNoiBo = false; }, 50);
    choPhepGiaoHang.value = Boolean(thongTinGiaoHang?.giaoHang);
    tenNguoiNhanGiaoHang.value = thongTinGiaoHang?.tenNguoiNhan || "";
    sdtNguoiNhanGiaoHang.value = thongTinGiaoHang?.soDienThoaiNguoiNhan || "";
    diaChiGiaoHang.value = thongTinGiaoHang?.diaChiGiaoHang || "";
    donViVanChuyen.value = thongTinGiaoHang?.donViVanChuyen || "GHN";
    phiVanChuyen.value = Number(thongTinGiaoHang?.phiVanChuyen || 0);
    diaChiDaXacNhan.value = "";
    daTinhPhiVanChuyen.value = choPhepGiaoHang.value;
    cauHinhGiaoHang.value = {
      serviceTypeId: 2,
      length: 30,
      width: 20,
      height: 12,
      weight: 500
    };
    maPhieuGiamGia.value = invoice.phieuGiamGia?.ma ?? "";
    phieuGiamGiaDaApDung.value = invoice.phieuGiamGia
      ? {
        id: 0,
        ma: invoice.phieuGiamGia.ma,
        ten: invoice.phieuGiamGia.ten,
        loai: 0,
        giaTri: 0,
        giaTriToiThieu: null,
        giamToiDa: null,
        soTienGiam: invoice.tienGiam || invoice.phieuGiamGia.soTienGiam,
        tongTienHang: invoice.tongTienHang || 0,
        tongTienSauGiam: Math.max((invoice.tongTienHang || 0) - (invoice.tienGiam || 0), 0)
      }
      : null;
    ketQuaTimKiemPhieu.value = [];
    hienThiDanhSachPhieu.value = false;
    capNhatTienKhachThanhToan(true);
  }

  async function luuHoaDonHienTai() {
    if (!hoaDonChoDaChon.value) return;
    try {
      const payload = {
        tenKhachHang: khachHangDuocChon.value?.hoTen || tenNguoiNhanGiaoHang.value || (laKhachVangLai.value ? KHACH_VANG_LAI : ""),
        soDienThoai: khachHangDuocChon.value?.sdt || sdtNguoiNhanGiaoHang.value || "",
        ghiChu: "",
        khachHangId: khachHangDuocChon.value?.id || null,
        maPhieuGiamGia: phieuGiamGiaDaApDung.value?.ma || null,
        thongTinGiaoHang: choPhepGiaoHang.value ? taoPayloadGiaoHang() : null,
        items: cartItems.value.map(item => ({
          chiTietId: item.chiTietId,
          soLuong: item.soLuong,
          giaBan: item.giaBan
        })),
      };
      const response = await capNhatHoaDonCho(hoaDonChoDaChon.value.id, payload);
      // Cập nhật lại soLuongBanDau vì backend đã trừ tồn kho
      dangLuuNoiBo = true;
      cartItems.value = cartItems.value.map(item => ({
        ...item,
        soLuongBanDau: item.soLuong
      }));
      setTimeout(() => { dangLuuNoiBo = false; }, 50);
    } catch (error) {
      console.error("Lỗi khi lưu hóa đơn chờ:", error);
      const msg = error instanceof Error ? error.message : "Cập nhật hóa đơn chờ thất bại";
      thongBaoLoi.value = msg;
      
      // Nếu lỗi do phiếu giảm giá, gỡ bỏ phiếu giảm giá trên frontend để tránh lỗi liên tục
      if (msg.toLowerCase().includes("phiếu giảm giá")) {
        const maLoi = phieuGiamGiaDaApDung.value?.ma || maPhieuGiamGia.value;
        phieuGiamGiaDaApDung.value = null;
        maPhieuGiamGia.value = "";
        
        // Gọi tuDongApDungVaDeXuatHangMucTiepTheo để tự động tìm phiếu khác
        if (maLoi) {
          thongBaoLoi.value = `Phiếu giảm giá ${maLoi} không còn hợp lệ. Hệ thống đang tự động tìm phiếu giảm giá thay thế...`;
          if (tuDongApDungVaDeXuatHangMucTiepTheo) {
            tuDongApDungVaDeXuatHangMucTiepTheo();
          }
        }
      }
      throw error;
    }
  }
  
  let dangLuuNoiBo = false;
  let boDemTuDongLuu = null;
  watch(() => cartItems.value, () => {
    if (dangLuuNoiBo) return;
    if (boDemTuDongLuu) clearTimeout(boDemTuDongLuu);
    boDemTuDongLuu = setTimeout(() => {
      luuHoaDonHienTai().catch(() => {});
    }, 1000);
  }, { deep: true });

  async function chonHoaDonCho(invoice) {
    if (hoaDonChoDaChon.value && hoaDonChoDaChon.value.id !== invoice.id) {
      try {
        await luuHoaDonHienTai();
      } catch (e) {
        // ignore error when switching tabs
      }
    }

    dangTaiChiTietHoaDon.value = true;
    thongBaoLoi.value = "";
    try {
      await taiSanPham("");
      const detail = await layChiTietHoaDonCho(invoice.id);
      hoaDonChoDaChon.value = invoice;
      chuyenHoaDonThanhBanNhap(detail);
    } catch (error) {
      thongBaoLoi.value = error instanceof Error ? error.message : "Không thể tải hóa đơn chờ";
    } finally {
      dangTaiChiTietHoaDon.value = false;
    }
  }

  async function xuLyTaoHoaDonCho() {
    if (!validateGioHang(false)) {
      return;
    }
    if (daDatGioiHanHoaDonCho.value) {
      thongBaoLoi.value = `Chỉ được tạo tối đa ${TOI_DA_HOA_DON_CHO} hóa đơn chờ.`;
      return;
    }
    if (!coTheTaoHoaDonCho.value) {
      return;
    }
    dangLuuHoaDonCho.value = true;
    thongBaoLoi.value = "";
    thongBaoThanhCong.value = "";
    try {
      const createdInvoice = await taoHoaDonCho({
        khachHangId: layIdKhachHangHienTai(),
        tenKhachHang: khachHangDuocChon.value?.hoTen || (laKhachVangLai.value ? KHACH_VANG_LAI : ""),
        soDienThoai: khachHangDuocChon.value?.sdt || hoaDonChoDaChon.value?.soDienThoai || "",
        maPhieuGiamGia: phieuGiamGiaDaApDung.value?.ma ?? null,
        thongTinGiaoHang: taoPayloadGiaoHang(),
        items: taoDanhSachSanPhamThanhToan()
      });
      thongBaoThanhCong.value = `Đã tạo hóa đơn chờ ${createdInvoice.ma}`;
      await taiDanhSachHoaDonCho();
      const matchedInvoice = danhSachHoaDonCho.value.find((invoice) => invoice.id === createdInvoice.id) ?? null;
      hoaDonChoDaChon.value = matchedInvoice;
      chuyenHoaDonThanhBanNhap(createdInvoice);
    } catch (error) {
      thongBaoLoi.value = error instanceof Error ? error.message : "Không thể tạo hóa đơn chờ";
    } finally {
      dangLuuHoaDonCho.value = false;
    }
  }

  async function xuLyThanhToanNgay() {
    if (!daChonKhach.value) {
      thongBaoLoi.value = "Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thanh toán.";
      return;
    }
    if (!validateGioHang(true) || !kiemTraLoiThanhToan()) {
      return;
    }
    if (!coTheThanhToan.value) {
      return;
    }

    const isConfirmed = await showConfirm('Bạn có chắc chắn muốn thanh toán đơn hàng này không?');
    if (!isConfirmed) {
      return;
    }

    dangThanhToan.value = true;
    thongBaoLoi.value = "";
    thongBaoThanhCong.value = "";
    try {
      if (hoaDonChoDaChon.value) {
        await luuHoaDonHienTai();
      }

      const response = await thanhToanTaiQuay({
        hoaDonId: hoaDonChoDaChon.value?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        tenKhachHang: khachHangDuocChon.value?.hoTen || (laKhachVangLai.value ? KHACH_VANG_LAI : ""),
        soDienThoai: khachHangDuocChon.value?.sdt || hoaDonChoDaChon.value?.soDienThoai || "",
        maPhieuGiamGia: phieuGiamGiaDaApDung.value?.ma ?? null,
        thongTinGiaoHang: taoPayloadGiaoHang(),
        hinhThucThanhToan: phuongThucThanhToan.value,
        tienKhachDua: phuongThucThanhToan.value === 1 ? tienKhachThanhToan.value : khachCanTra.value,
        ghiChu: ghiChuThanhToan.value,
        items: taoDanhSachSanPhamThanhToan()
      });
      thongBaoThanhCong.value = `Đã thanh toán ${response.maHoaDon}`;
      await taiDanhSachHoaDonCho();
      xoaBanNhap();
    } catch (error) {
      const msg = error instanceof Error ? error.message : "Không thể thanh toán trực tiếp";
      thongBaoLoi.value = msg;
      
      if (msg.toLowerCase().includes("phiếu giảm giá")) {
        const maLoi = phieuGiamGiaDaApDung.value?.ma || maPhieuGiamGia.value;
        phieuGiamGiaDaApDung.value = null;
        maPhieuGiamGia.value = "";
        
        if (maLoi) {
          thongBaoLoi.value = `Phiếu giảm giá ${maLoi} không còn hợp lệ. Hệ thống đang tự động tìm phiếu giảm giá thay thế...`;
          if (tuDongApDungVaDeXuatHangMucTiepTheo) {
            tuDongApDungVaDeXuatHangMucTiepTheo();
          }
        }
      }
    } finally {
      dangThanhToan.value = false;
    }
  }

  async function xuLyHuyHoaDonCho() {
    if (dangHuyHoaDonCho.value) {
      return;
    }
    
    if (!hoaDonChoDaChon.value) {
      toastSwal.fire({
        icon: 'warning',
        title: 'Thông báo',
        text: 'Vui lòng chọn hóa đơn cần hủy',
        timer: 3000,
        iconColor: '#cf1018'
      });
      return;
    }

    const isConfirmed = await showConfirm(`Bạn có chắc chắn muốn hủy hóa đơn ${hoaDonChoDaChon.value.ma} không?`);
    if (!isConfirmed) {
      return;
    }

    dangHuyHoaDonCho.value = true;
    thongBaoLoi.value = "";
    try {
      await huyHoaDonCho(hoaDonChoDaChon.value.id);
      
      toastSwal.fire({
        icon: 'success',
        title: 'Thành công!',
        text: `Đã hủy hóa đơn chờ ${hoaDonChoDaChon.value.ma}`,
        timer: 3000,
        iconColor: '#cf1018'
      });

      await taiDanhSachHoaDonCho();
      xoaBanNhap();
    } catch (error) {
      thongBaoLoi.value = error instanceof Error ? error.message : "Không thể hủy hóa đơn chờ";
    } finally {
      dangHuyHoaDonCho.value = false;
    }
  }

  async function xuLyTaoHoaDonChoMoi() {
    if (daDatGioiHanHoaDonCho.value) {
      thongBaoLoi.value = `Chỉ được tạo tối đa ${TOI_DA_HOA_DON_CHO} hóa đơn chờ.`;
      return;
    }
    dangLuuHoaDonCho.value = true;
    thongBaoLoi.value = "";
    thongBaoThanhCong.value = "";
    try {
      xoaBanNhap();
      const createdInvoice = await taoHoaDonCho({
        khachHangId: null,
        tenKhachHang: "",
        soDienThoai: "",
        maPhieuGiamGia: null,
        thongTinGiaoHang: {
          giaoHang: false,
          tenNguoiNhan: null,
          soDienThoaiNguoiNhan: null,
          diaChiGiaoHang: null,
          phiVanChuyen: 0,
          donViVanChuyen: null
        },
        items: []
      });
      thongBaoThanhCong.value = `Đã tạo hóa đơn chờ ${createdInvoice.ma}`;
      await taiDanhSachHoaDonCho();
      const matchedInvoice = danhSachHoaDonCho.value.find((invoice) => invoice.id === createdInvoice.id) ?? null;
      hoaDonChoDaChon.value = matchedInvoice;
    } catch (error) {
      thongBaoLoi.value = error instanceof Error ? error.message : "Không thể tạo hóa đơn chờ";
    } finally {
      dangLuuHoaDonCho.value = false;
    }
  }

  function xoaCacBoDem() {
    xoaBoDemThoiGianKhachHang();
    xoaBoDemThoiGianSanPham();
    xoaCacBoDemThoiGianPhieu();
  }

  const xuLyInHoaDon = () => {
    xuLyInHoaDonTaiQuay({
      hoaDonChoDaChon: hoaDonChoDaChon.value,
      cartItems: cartItems.value,
      phiVanChuyen: phiVanChuyen.value,
      tienGiam: tienGiam.value,
      tongTien: tongTien.value,
      khachCanTra: khachCanTra.value,
      tenKhachHangHienThi: tenKhachHangHienThi.value,
      soDienThoaiKhachHangHienThi: soDienThoaiKhachHangHienThi.value,
      thongBaoThanhCong,
      thongBaoLoi
    });
  };

  onMounted(async () => {
    await taiSanPham("");
    await taiDanhSachHoaDonCho();
  });

  onBeforeUnmount(() => {
    xoaCacBoDem();
  });

  return {
    TOI_DA_HOA_DON_CHO,
    danhSachHoaDonCho,
    dangTaiHoaDonCho,
    daDatGioiHanHoaDonCho,
    hoaDonChoDaChon,
    tuKhoaKhachHang,
    dangTaiKhachHang,
    hienThiDanhSachKhachHang,
    ketQuaTimKiemKhachHang,
    tenKhachHangHienThi,
    soDienThoaiKhachHangHienThi,
    khachHangDuocChon,
    laKhachVangLai,
    tuKhoaSanPham,
    dangTaiSanPham,
    hienThiDanhSachSanPham,
    ketQuaSanPham,
    sanPhamPhanTrang,
    trangHienTai,
    kichThuocTrang,
    tongSoMuc,
    tongSoTrang,
    boLocThuongHieuDaChon,
    boLocDanhMucDaChon,
    thuongHieuCoSan,
    danhMucCoSan,
    nhanTimKiemSanPham,
    cartItems,
    chiTietSanPhamDaChon: chiTietSanPhamDaChon,
    chiTietDangChon,
    hinhAnhDangChon,
    soLuongTonSauKhiChon,
    luaChonMauSac,
    luaChonKichCo,
    mauSacDaChon: mauSacDaChon,
    kichCoDaChon: kichCoDaChon,
    soLuongDaChon: soLuongDaChon,
    soLuongTonKhaDungChiTiet,
    dangTaiChiTietHoaDon,
    tongSoLuong,
    tongTienSauGiamHienThi,
    tienGiam,
    tongTien,
    sanPhamValidationMessage,
    maPhieuGiamGia: maPhieuGiamGia,
    coTheApDungPhieu,
    dangApDungPhieu: dangApDungPhieu,
    hienThiDanhSachPhieu: hienThiDanhSachPhieu,
    coTheTimPhieu,
    dangTaiPhieu: dangTaiPhieu,
    ketQuaTimKiemPhieu: ketQuaTimKiemPhieu,
    phieuGiamGiaDaApDung: phieuGiamGiaDaApDung,
    maPhieuChuaApDung,
    khachCanTra,
    thongTinGiaoHang,
    phuongThucThanhToan: phuongThucThanhToan,
    tienKhachDua: tienKhachDua,
    thongBaoLoiThanhToan: thongBaoLoiThanhToan,
    tienThua,
    ghiChuThanhToan: ghiChuThanhToan,
    coTheTaoHoaDonCho,
    dangLuuHoaDonCho,
    coTheThanhToan,
    dangThanhToan,
    dangHuyHoaDonCho,
    dinhDangTien,
    soLuongConLai,
    xoaBanNhap,
    chonHoaDonCho,
    moDanhSachKhachHang,
    dongDanhSachKhachHang,
    chonKhachHang,
    chonKhachVangLai,
    boChonKhachHang,
    moDanhSachSanPham,
    dongDanhSachSanPham,
    moChiTietSanPham,
    tangSoLuong,
    giamSoLuong,
    xoaSanPham,
    capNhatSoLuong,
    dongChiTietSanPham,
    chonMauSac,
    chonKichCo,
    giamSoLuongChiTiet,
    tangSoLuongChiTiet,
    themBienTheDangChon,
    xuLyQuetQrSanPham,
    xuLyKhiFocusPhieu: xuLyKhiFocusPhieu,
    xuLyKhiBlurPhieu: xuLyKhiBlurPhieu,
    xuLyApDungPhieu: xuLyApDungPhieu,
    chonPhieuGiamGia,
    xuLyGoPhieu: xuLyGoPhieu,
    capNhatThongTinGiaoHang,
    xuLyTinhPhiVanChuyen,
    xuLyTienKhachDuaInput: xuLyTienKhachDuaInput,
    xuLyTaoHoaDonCho,
    xuLyTaoHoaDonChoMoi,
    xuLyThanhToanNgay,
    xuLyHuyHoaDonCho,
    xuLyInHoaDon
  };
}

export {
  LogicBanHangTaiQuay
};
