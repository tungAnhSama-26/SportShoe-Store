import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { chuanHoaDiaChi, DIA_CHI_RONG } from "../../utils/dia-chi";
import { useRouter } from "vue-router";
import {
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
  layTrangThaiChuyenKhoan,
  taoQrChuyenKhoan,
  thanhToanTaiQuay,
  taoHoaDonCho,
  capNhatHoaDonCho,
  timSanPhamTaiQuay,
  doiBienTheHoaDonChiTiet
} from "../../services/ban-hang-tai-quay";
import {
  KHACH_VANG_LAI,
  TOI_DA_HOA_DON_CHO,
} from "./HangSo";
import { PHUONG_THUC_THANH_TOAN } from "./Enum";
import { dinhDangTien } from "./TienTe";
import { LogicGioHang } from "./LogicGioHang";
import { LogicPhieuGiamGia } from "./LogicPhieuGiamGia";
import { LogicKhachHang } from "./LogicKhachHang";
import { LogicInHoaDon } from "./LogicInHoaDon";
import { LogicThanhToan } from "./LogicThanhToan";
import { LogicSanPham } from "./LogicSanPham";
import { LogicGiaoHang } from "./LogicGiaoHang";
import { showConfirm, showToastSuccess, showError, showWarning, toastSwal, showPaymentConfirmWithCoupon } from "../../utils/alert";
import { useRealtime } from "../../composables/useRealtime";



function LogicBanHangTaiQuay() {
  const danhSachHoaDonCho = ref([]);
  const hoaDonChoDaChon = ref(null);
  const dangTaiHoaDonCho = ref(false);
  const dangLuuHoaDonCho = ref(false);
  const dangHuyHoaDonCho = ref(false);
  const dangThanhToan = ref(false);
  // Mã QR chuyển khoản đang hiện cho khách quét (null = không hiện).
  const qrChuyenKhoan = ref(null);
  const dangTaoQrChuyenKhoan = ref(false);
  const soGiayQrConLai = ref(0);
  const dangTaiChiTietHoaDon = ref(false);
  const thongBaoLoi = ref("");
  const thongBaoThanhCong = ref("");
  const choPhepGiaoHang = ref(false);
  const tenNguoiNhanGiaoHang = ref("");
  const sdtNguoiNhanGiaoHang = ref("");
  const emailNguoiNhanGiaoHang = ref("");
  const diaChiGiaoHang = ref({ ...DIA_CHI_RONG });
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
    danhDauCanApDungLaiPhieu: () => { if (typeof danhDauCanApDungLaiPhieu === 'function') danhDauCanApDungLaiPhieu() },
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
    isOutdatedPrice,
    themSanPham,
    tangSoLuong,
    giamSoLuong,
    xoaSanPham,
    capNhatSoLuong
  } = LogicGioHang({
    danhDauCanTinhLaiPhiVanChuyen: () => { if (typeof danhDauCanTinhLaiPhiVanChuyen === 'function') danhDauCanTinhLaiPhiVanChuyen() },
    capNhatTienKhachThanhToan: (v) => { if (typeof capNhatTienKhachThanhToan === 'function') capNhatTienKhachThanhToan(v) },
    danhDauCanApDungLaiPhieu: () => { if (typeof danhDauCanApDungLaiPhieu === 'function') danhDauCanApDungLaiPhieu() },
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
    emailNguoiNhanGiaoHang,
    diaChiGiaoHang,
    donViVanChuyen,
    phiVanChuyen,
    diaChiDaXacNhan,
    daTinhPhiVanChuyen,
    dangTinhPhiVanChuyen,
    cauHinhGiaoHang,
    khachHangDuocChon,
    tuKhoaKhachHang,
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
    tuDongApDungVaDeXuatHangMucTiepTheo,
    phieuGiamGiaHangMucTiepTheo: phieuGiamGiaMucTiepTheo,
    soTienThieuDeDatHangMuc: soTienThieuChoMucTiepTheo,
    soSanPhamThieuDeDatHangMuc: soSanPhamThieuChoMucTiepTheo,
    soTienGiamCuaHangMucTiepTheo: soTienGiamMucTiepTheo,
    phieuTotHonDeXuat: loiNhomPhieuGiamGiaTotHon,
    tuChoiPhieuTotHon: tuChoiPhieuGiamGiaTotHon,
    chapNhanPhieuTotHon: chapNhanPhieuGiamGiaTotHon,
    kiemTraPhieuTotHonTruocThanhToan,
    kiemTraLaiPhieuDangApDung
  } = LogicPhieuGiamGia({
    cartItems,
    tongTien,
    hoaDonChoDaChon,
    dangTaiChiTietHoaDon,
    khachHangDuocChon,
    layIdKhachHangHienTai,
    taoDanhSachSanPhamThanhToan,
    capNhatTienKhachThanhToan: (v) => { if (typeof capNhatTienKhachThanhToan === 'function') capNhatTienKhachThanhToan(v) },
    thongBaoLoi,
    thongBaoThanhCong,
    xoaPhanHoi
  });

  const router = useRouter();
  const khachCanTra = computed(() => tongTienSauGiamHienThi.value + phiVanChuyenHienThi.value);

  const {
    phuongThucThanhToan,
    tienKhachDua,
    tienMatKetHop,
    tienChuyenKhoanKetHop,
    ghiChuThanhToan,
    tienKhachThanhToan,
    tienThua,
    thongBaoLoiThanhToan,
    hienThiMaQrLon,
    capNhatTienKhachThanhToan,
    kiemTraLoiThanhToan,
    xuLyTienKhachDuaInput,
    xuLyTienMatKetHopInput,
    xuLyTienChuyenKhoanKetHopInput
  } = LogicThanhToan({
    cartItems,
    khachCanTra,
    thongBaoLoi,
    hoaDonChoDaChon
  });

  const coTheTaoHoaDonCho = computed(
    () => !dangLuuHoaDonCho.value &&
      !maPhieuChuaApDung.value &&
      !daDatGioiHanHoaDonCho.value &&
      coThongTinGiaoHangHopLe.value &&
      !sanPhamValidationMessage.value
  );

  const coTheThanhToan = computed(() => {
    if (!cartItems.value || cartItems.value.length === 0) return false;
    if (sanPhamValidationMessage.value) return false;
    if (dangThanhToan.value) return false;
    if (choPhepGiaoHang.value && !coThongTinGiaoHangHopLe.value) return false;
    if (phuongThucThanhToan.value === 1 && thongBaoLoiThanhToan.value) return false;
    if (phuongThucThanhToan.value === 5 && thongBaoLoiThanhToan.value) return false;
    return true;
  });

  const {
    xuLyInHoaDonTaiQuay
  } = LogicInHoaDon();

  const { isConnected, subscribeTopic, publishMessage } = useRealtime();

  const sessionId = Math.random().toString(36).substring(2, 15);
  let isSyncingUI = false;

  subscribeTopic('/topic/admin/pos-sync', async (rawMsg) => {
    const msg = rawMsg?.payload ?? rawMsg;
    if (msg.sender === sessionId) return;

    if (rawMsg?.type === 'POS_INVOICE_CHANGED' || ['CREATED', 'UPDATED', 'CANCELLED', 'PAID'].includes(msg.action)) {
      try {
        // Đang hiện mã QR cho chính hóa đơn này -> tiền đã về, đóng mã và mở hóa đơn luôn.
        if (msg.action === 'PAID' && qrChuyenKhoan.value?.hoaDonId === msg.invoiceId) {
          await hoanTatChuyenKhoan(msg.invoiceId, msg.maHoaDon || qrChuyenKhoan.value?.maHoaDon);
          return;
        }
        if (msg.action === 'PAID' || msg.action === 'CANCELLED') {
          if (hoaDonChoDaChon.value?.id === msg.invoiceId) {
            if (msg.action === 'PAID') {
              showToastSuccess(msg.message || `Hóa đơn ${msg.maHoaDon || ''} đã thanh toán thành công!`);
            }
            xoaBanNhap();
          } else if (msg.action === 'PAID') {
            showToastSuccess(msg.message || `Hóa đơn ${msg.maHoaDon || ''} đã thanh toán thành công!`);
          }
          await taiDanhSachHoaDonCho(true);
          return;
        }

        // Cập nhật danh sách hóa đơn chờ một cách ngầm (silent) để không chớp nháy thanh tab
        await taiDanhSachHoaDonCho(true);

        if (hoaDonChoDaChon.value?.id === msg.invoiceId && !dangLuuNoiBo && !dangLuuAPI) {
          try {
            const detail = await layChiTietHoaDonCho(msg.invoiceId);
            chuyenHoaDonThanhBanNhap(detail);
          } catch (err) {
            console.error("Lỗi cập nhật chi tiết hóa đơn realtime:", err);
          }
        }

        // Chỉ tự động chọn nếu hiện tại chưa chọn hóa đơn nào và có hóa đơn mới được tạo
        if (!hoaDonChoDaChon.value && msg.action === 'CREATED') {
          const invoice = danhSachHoaDonCho.value.find((hd) => hd.id === msg.invoiceId);
          if (invoice) {
            await chonHoaDonCho(invoice);
          }
        }
      } catch (e) {
        console.error("Lỗi khi đồng bộ realtime POS:", e);
      }
      return;
    }

    if (msg.action === 'CHON_HOA_DON') {
      isSyncingUI = true;
      try {
        if (msg.invoiceId == null) {
          xoaBanNhap();
          return;
        }
        await taiDanhSachHoaDonCho(true);
        const invoice = danhSachHoaDonCho.value.find(hd => hd.id === msg.invoiceId);
        if (invoice && hoaDonChoDaChon.value?.id !== msg.invoiceId) {
          await chonHoaDonCho(invoice);
        }
      } catch (e) {
        console.error("Lỗi khi đồng bộ chọn hóa đơn:", e);
      } finally {
        isSyncingUI = false;
      }
    } else if (msg.action === 'SYNC_STATE') {
      if (hoaDonChoDaChon.value?.id !== msg.invoiceId) return;
      isSyncingUI = true;
      dangLuuNoiBo = true;
      skipNextAutosave = true;

      lastReceivedSyncState = {
        ...msg.state,
        diaChiGiaoHang: chuanHoaDiaChi(msg.state.diaChiGiaoHang)
      };

      choPhepGiaoHang.value = msg.state.choPhepGiaoHang;
      tenNguoiNhanGiaoHang.value = msg.state.tenNguoiNhanGiaoHang;
      sdtNguoiNhanGiaoHang.value = msg.state.sdtNguoiNhanGiaoHang;
      const newDiaChi = chuanHoaDiaChi(msg.state.diaChiGiaoHang);
      if (JSON.stringify(diaChiGiaoHang.value) !== JSON.stringify(newDiaChi)) {
        diaChiGiaoHang.value = newDiaChi;
      }
      tienKhachDua.value = msg.state.tienKhachDua || "";
      tienMatKetHop.value = msg.state.tienMatKetHop || "";
      tienChuyenKhoanKetHop.value = msg.state.tienChuyenKhoanKetHop || "";
      phuongThucThanhToan.value = msg.state.phuongThucThanhToan;
      hienThiMaQrLon.value = !!msg.state.hienThiMaQrLon;
      ghiChuThanhToan.value = msg.state.ghiChuThanhToan || "";
      tuKhoaKhachHang.value = msg.state.tuKhoaKhachHang || "";
      khachHangDuocChon.value = msg.state.khachHangDuocChon || null;

      setTimeout(() => {
        isSyncingUI = false;
        dangLuuNoiBo = false;
      }, 50);
    }
  });

  watch(isConnected, async (connected) => {
    if (!connected) return;

    try {
      const currentInvoiceId = hoaDonChoDaChon.value?.id;
      await taiDanhSachHoaDonCho(true);
      if (!currentInvoiceId) return;

      const currentInvoice = danhSachHoaDonCho.value.find((invoice) => invoice.id === currentInvoiceId);
      if (!currentInvoice) {
        xoaBanNhap();
      }
    } catch (e) {
      console.error("Lỗi tải lại POS sau khi kết nối realtime:", e);
    }
  });

  const {
    tuKhoaSanPham,
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
    thuongHieuCoSan,
    danhMucCoSan,
    mauSacCoSan,
    kichCoCoSan,
    giaThapNhatDaChon,
    giaCaoNhatDaChon,
    giaCaoNhatCoSan,
    bienTheLienQuan,
    luaChonMauSac,
    luaChonKichCo,
    bienTheDaChon,
    chiTietDangChon,
    hinhAnhDangChon,
    ketQuaBienTheSanPham,
    soLuongTonKhaDungChiTiet,
    soLuongTonSauKhiChon,
    taiSanPham,
    laySoLuongTonHienTai,
    moChiTietSanPham,
    dongChiTietSanPham,
    xuLyQuetQrSanPham,
    chonMauSac,
    chonKichCo,
    chonBienThe,
    giamSoLuongChiTiet,
    tangSoLuongChiTiet,
    capNhatSoLuongChiTiet,
    themTrucTiepBienThe,
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

  const CAC_SU_KIEN_CAN_DONG_BO_GIA = new Set([
    'PRODUCT_CHANGED',
    'PRODUCT_UPDATED',
    'DOI_TRANG_THAI_BIEN_THE',
    'DOI_TRANG_THAI_GIAY',
    'XOA_BIEN_THE',
    'CAP_NHAT_BIEN_THE',
    'DOT_GIAM_GIA',
    'PHIEU_GIAM_GIA'
  ]);
  let dongBoGiaRealtimeDangChay = false;
  let dongBoGiaRealtimeDangCho = false;
  let boDemDongBoGiaRealtime = null;

  function posDangBanKhongTheDongBoGia() {
    return dangLuuNoiBo || dangLuuHoaDonCho.value || dangThanhToan.value;
  }

  function lenLichDongBoGiaRealtime(delay = 80) {
    dongBoGiaRealtimeDangCho = true;
    if (boDemDongBoGiaRealtime) clearTimeout(boDemDongBoGiaRealtime);
    boDemDongBoGiaRealtime = setTimeout(() => {
      boDemDongBoGiaRealtime = null;
      void thucHienDongBoGiaRealtime();
    }, delay);
  }

  async function dongBoGiaGioHangTheoCatalog() {
    // Luôn tải toàn bộ biến thể để đối chiếu được cả sản phẩm không nằm trong từ khóa tìm kiếm hiện tại.
    await taiSanPham("", true);

    if (cartItems.value.length === 0) return;

    const sanPhamTheoChiTietId = new Map(
      (ketQuaBienTheSanPham.value || []).map((product) => [Number(product.chiTietId), product])
    );
    const removedItems = [];
    let hasChanges = false;
    const remainingItems = [];

    for (const item of cartItems.value) {
      const sanPhamMoi = sanPhamTheoChiTietId.get(Number(item.chiTietId));
      if (!sanPhamMoi) {
        remainingItems.push(item);
        continue;
      }
      const isInactive = sanPhamMoi.kichHoat === 0
        || sanPhamMoi.trangThai === 0
        || sanPhamMoi.trangThaiSanPham === 0;
      if (isInactive) {
        removedItems.push(item);
        hasChanges = true;
        continue;
      }
      // Kiểm tra giá sản phẩm mới trong catalog so với giá hiện tại trong giỏ hàng
      const newStock = sanPhamMoi.soLuongTon;
      const newPriceNum = Number(sanPhamMoi.giaBan);
      const currentPriceNum = Number(item.giaBan);
      const isPriceChanged = newPriceNum !== currentPriceNum;
      const isOutdated = isPriceChanged ? true : Boolean(item.isOutdatedPrice);

      if (
        item.soLuongTon !== newStock ||
        Boolean(item.isOutdatedPrice) !== isOutdated ||
        item.currentCatalogPrice !== newPriceNum
      ) {
        hasChanges = true;
      }

      remainingItems.push({
        ...item,
        soLuongTon: newStock,
        isOutdatedPrice: isOutdated,
        currentCatalogPrice: newPriceNum,
        oldPrice: isPriceChanged ? (item.oldPrice || item.giaBan) : item.oldPrice
      });
    }

    if (hasChanges) {
      dangLuuNoiBo = true;
      cartItems.value = remainingItems;
      setTimeout(() => {
        dangLuuNoiBo = false;
      }, 50);
    }

    if (removedItems.length > 0) {
      const names = [...new Set(removedItems.map(it => `"${it.tenSanPham}"`))].join(', ');
      showWarning(`Sản phẩm ${names} đã ngừng hoạt động, vui lòng chọn sản phẩm khác.`);
      
      if (hoaDonChoDaChon.value) {
        setTimeout(() => {
          void luuHoaDonHienTai(true);
        }, 50);
      }
    }
  }

  async function thucHienDongBoGiaRealtime() {
    if (!dongBoGiaRealtimeDangCho || dongBoGiaRealtimeDangChay) return;
    if (posDangBanKhongTheDongBoGia()) {
      lenLichDongBoGiaRealtime(150);
      return;
    }

    dongBoGiaRealtimeDangCho = false;
    dongBoGiaRealtimeDangChay = true;
    try {
      await dongBoGiaGioHangTheoCatalog();
    } catch (error) {
      dongBoGiaRealtimeDangCho = true;
      console.error("Lỗi khi đồng bộ giá realtime POS:", error);
    } finally {
      dongBoGiaRealtimeDangChay = false;
      if (dongBoGiaRealtimeDangCho) lenLichDongBoGiaRealtime(150);
    }
  }

  subscribeTopic('/topic/admin/san-pham', () => {
    lenLichDongBoGiaRealtime(50);
    void kiemTraLaiPhieuDangApDung(true);
  });

  subscribeTopic('/topic/admin/thuoc-tinh', () => {
    lenLichDongBoGiaRealtime(50);
    void kiemTraLaiPhieuDangApDung(true);
  });

  subscribeTopic('/topic/admin/dot-giam-gia', () => {
    lenLichDongBoGiaRealtime(50);
    void kiemTraLaiPhieuDangApDung(true);
  });

  subscribeTopic('/topic/admin/phieu-giam-gia', () => {
    lenLichDongBoGiaRealtime(50);
    void kiemTraLaiPhieuDangApDung(true);
  });

  function dongBoGiaKhiQuayLaiTab() {
    if (document.visibilityState === 'visible') {
      lenLichDongBoGiaRealtime(0);
      void kiemTraLaiPhieuDangApDung(false);
    }
  }

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
    // Không đóng hienThiDanhSachSanPham.value = false; để người dùng có thể tiếp tục chọn
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
    diaChiGiaoHang.value = { ...DIA_CHI_RONG };
    donViVanChuyen.value = "GHN";
    phiVanChuyen.value = 0;
    daTinhPhiVanChuyen.value = false;
    dangTinhPhiVanChuyen.value = false;

    if (!isSyncingUI) {
      publishMessage('/topic/admin/pos-sync', {
        sender: sessionId,
        action: 'CHON_HOA_DON',
        invoiceId: null
      });
    }
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

  async function taiDanhSachHoaDonCho(silent = false) {
    if (!silent) {
      dangTaiHoaDonCho.value = true;
    }
    try {
      danhSachHoaDonCho.value = await layDanhSachHoaDonCho();
    } catch (error) {
      if (!silent) {
        thongBaoLoi.value = error instanceof Error
          ? error.message
          : "Không thể tải danh sách hóa đơn chờ";
      }
    } finally {
      if (!silent) {
        dangTaiHoaDonCho.value = false;
      }
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

  const itemDangDoiBienThe = ref(null);

  async function xuLyMoDoiBienTheInCart(item) {
    itemDangDoiBienThe.value = item;
    dangTaiSanPham.value = true;
    try {
      const keyword = item.maSanPham || item.tenSanPham || "";
      const products = await timSanPhamTaiQuay(keyword);
      if (products && products.length > 0) {
        ketQuaBienTheSanPham.value = products;
        const currentVariant = products.find(p => Number(p.chiTietId) === Number(item.chiTietId)) || products[0];
        moChiTietSanPham(currentVariant);
      } else {
        showError("Không tìm thấy thông tin biến thể của sản phẩm này");
      }
    } catch (err) {
      console.error("Lỗi khi tải biến thể:", err);
      showError("Không thể tải danh sách biến thể");
    } finally {
      dangTaiSanPham.value = false;
    }
  }

  async function xuLyDoiBienTheInCart(item, bienTheMoi) {
    if (!bienTheMoi || !item) return;

    const isDuplicate = cartItems.value.some(
      (i) => Number(i.chiTietId) === Number(bienTheMoi.chiTietId)
    );
    if (isDuplicate) {
      showError("Sản phẩm này đã có trong giỏ hàng", "Thông báo");
      return;
    }

    const tenMoTa = `${bienTheMoi.tenSanPham || ''} (${bienTheMoi.mauSac || ''} - Size ${bienTheMoi.kichCo || ''})`;
    const isConfirmed = await showConfirm(`Bạn có chắc chắn muốn đổi sản phẩm sang "${tenMoTa}" không?`);
    if (!isConfirmed) {
      return;
    }

    try {
      if (hoaDonChoDaChon.value?.id) {
        const targetId = item.hoaDonChiTietId || item.id || item.chiTietId;
        await doiBienTheHoaDonChiTiet(targetId, {
          giayChiTietMoiId: bienTheMoi.chiTietId,
          soLuong: item.soLuong
        });
        await taiDanhSachHoaDonCho();
        const updatedDetail = await layChiTietHoaDonCho(hoaDonChoDaChon.value.id);
        chuyenHoaDonThanhBanNhap(updatedDetail);
      } else {
        const index = cartItems.value.findIndex(i => (i.cartItemId || i.chiTietId) === (item.cartItemId || item.chiTietId));
        if (index !== -1) {
          cartItems.value[index] = {
            ...cartItems.value[index],
            chiTietId: bienTheMoi.chiTietId,
            mauSac: bienTheMoi.mauSac || bienTheMoi.maBienThe,
            kichCo: bienTheMoi.kichCo,
            giaBan: bienTheMoi.giaBan,
            hinhAnh: bienTheMoi.hinhAnh || cartItems.value[index].hinhAnh
          };
        }
      }
      showToastSuccess(`Đã đổi sản phẩm thành công!`);
    } catch (e) {
      showError(e instanceof Error ? e.message : "Không thể đổi biến thể");
    } finally {
      itemDangDoiBienThe.value = null;
      dongChiTietSanPham();
    }
  }

  async function themBienTheDangChon() {
    if (!bienTheDaChon.value) {
      if (typeof thongBaoLoi !== 'undefined') thongBaoLoi.value = "Vui lòng chọn màu sắc và kích cỡ phù hợp";
      return;
    }
    
    if (itemDangDoiBienThe.value) {
      await xuLyDoiBienTheInCart(itemDangDoiBienThe.value, bienTheDaChon.value);
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
          ...latestVariant
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
          iconColor: '#f59e0b',
          target: document.getElementById('pos-tablet-screen') || 'body'
        });
      } else {
        showToastSuccess(`Đã thêm ${soLuongDaChon.value} sản phẩm vào hóa đơn`);
      }
    }
  }

  function chuyenHoaDonThanhBanNhap(invoice) {
    if (boDemTuDongLuu) {
      clearTimeout(boDemTuDongLuu);
      boDemTuDongLuu = null;
    }
    isSyncingUI = true;
    skipNextAutosave = true;
    dangLuuNoiBo = true;

    const thongTinTheoChiTietId = new Map(
      ketQuaBienTheSanPham.value.map((product) => [Number(product.chiTietId), product])
    );
    const thongTinGiaoHang = invoice.thongTinGiaoHang || null;

    if (hoaDonChoDaChon.value?.id === invoice.id && !invoice.khachHangId && !invoice.tenKhachHang) {
      // Giữ nguyên tuKhoaKhachHang để không bị mất chữ khi người dùng đang gõ
    } else {
      tuKhoaKhachHang.value = invoice.tenKhachHang || invoice.soDienThoai || "";
    }
    khachHangDuocChon.value = invoice.khachHangId
      ? (khachHangDuocChon.value?.id === invoice.khachHangId
          ? { ...khachHangDuocChon.value, hoTen: invoice.tenKhachHang, sdt: invoice.soDienThoai }
          : {
            id: invoice.khachHangId,
            hoTen: invoice.tenKhachHang,
            sdt: invoice.soDienThoai,
            email: null
          })
      : null;

    let hasRemovedInactive = false;
    cartItems.value = (invoice.items || [])
      .filter((item) => {
        const thongTin = thongTinTheoChiTietId.get(Number(item.chiTietId));
        const isInactive = item.trangThaiSanPham === 0
          || item.trangThai === 0
          || item.kichHoat === 0
          || (thongTin && (thongTin.kichHoat === 0 || thongTin.trangThai === 0));

        if (isInactive) {
          showWarning(`Sản phẩm "${item.tenSanPham}" đã ngừng hoạt động, vui lòng chọn sản phẩm khác.`);
          hasRemovedInactive = true;
          return false;
        }
        return true;
      })
      .map((item) => {
        const thongTinSanPham = thongTinTheoChiTietId.get(Number(item.chiTietId));
        if (thongTinSanPham && typeof item.soLuongTon !== 'undefined') {
          thongTinSanPham.soLuongTon = item.soLuongTon;
        }
        return {
          cartItemId: Date.now().toString() + Math.random().toString(),
          hoaDonChiTietId: item.hoaDonChiTietId || item.id || null,
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
          giaGoc: item.giaGoc || thongTinSanPham?.giaGoc || null,
          soLuongTon: item.soLuongTon
        };
      });

    choPhepGiaoHang.value = Boolean(thongTinGiaoHang?.giaoHang);
    tenNguoiNhanGiaoHang.value = thongTinGiaoHang?.tenNguoiNhan || "";
    sdtNguoiNhanGiaoHang.value = thongTinGiaoHang?.soDienThoaiNguoiNhan || "";
    emailNguoiNhanGiaoHang.value = thongTinGiaoHang?.email || khachHangDuocChon.value?.email || "";
    if (thongTinGiaoHang?.giaoHang) {
      diaChiGiaoHang.value = chuanHoaDiaChi(thongTinGiaoHang.diaChiGiaoHang);
    } else if (!diaChiGiaoHang.value && khachHangDuocChon.value?.diaChiMacDinh) {
      diaChiGiaoHang.value = chuanHoaDiaChi(khachHangDuocChon.value.diaChiMacDinh);
    } else if (!thongTinGiaoHang?.giaoHang && !khachHangDuocChon.value) {
      diaChiGiaoHang.value = { ...DIA_CHI_RONG };
    }
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
    capNhatTienKhachThanhToan(false);

    setTimeout(() => { 
      dangLuuNoiBo = false; 
      isSyncingUI = false;
      if (hasRemovedInactive) {
        luuHoaDonHienTai(true);
      }
    }, 300);

    return false;
  }

  let dangLuuAPI = false;
  let pendingSave = false;

  async function luuHoaDonHienTai(force = false) {
    if (!hoaDonChoDaChon.value) return;
    if (dangThanhToan.value && !force) return;
    
    if (dangLuuAPI) {
      pendingSave = true;
      return;
    }
    
    dangLuuAPI = true;
    pendingSave = false;
    
    try {
      while (true) {
        const currentInvoiceId = hoaDonChoDaChon.value.id;
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
        const response = await capNhatHoaDonCho(currentInvoiceId, payload);
        
        dangLuuNoiBo = true;
        if (response && Array.isArray(response.items)) {
          const respMap = new Map(response.items.map(i => [Number(i.chiTietId), i]));
          cartItems.value = cartItems.value.map(item => {
            const serverItem = respMap.get(Number(item.chiTietId));
            return {
              ...item,
              soLuongBanDau: serverItem ? serverItem.soLuong : item.soLuong,
              soLuongTon: serverItem && typeof serverItem.soLuongTon !== 'undefined' ? serverItem.soLuongTon : item.soLuongTon
            };
          });
        } else {
          cartItems.value = cartItems.value.map(item => ({
            ...item,
            soLuongBanDau: item.soLuong
          }));
        }
        setTimeout(() => { dangLuuNoiBo = false; }, 50);

        if (currentInvoiceId && !isSyncingUI) {
          publishMessage('/topic/admin/pos-sync', {
            sender: sessionId,
            action: 'UPDATED',
            invoiceId: currentInvoiceId
          });
        }

        if (pendingSave) {
          pendingSave = false;
          // loop again to save the latest state
        } else {
          break;
        }
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message : "Cập nhật hóa đơn chờ thất bại";
      if (msg.includes("Chỉ được cập nhật") || msg.includes("trạng thái chờ")) {
        return;
      }
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

      // Rollback giỏ hàng nếu lỗi liên quan đến sản phẩm (vd: ngừng kinh doanh, hết hàng)
      if (hoaDonChoDaChon.value) {
        try {
          const detail = await layChiTietHoaDonCho(hoaDonChoDaChon.value.id);
          
          if (msg.includes("Số lượng không đủ") || msg.includes("Số lượng tồn kho không đủ")) {
            let hasAdjusted = false;
            const currentCartItems = [...cartItems.value];
            
            detail.items = detail.items.map(dbItem => {
              const matchedCartItem = currentCartItems.find(c => Number(c.chiTietId) === Number(dbItem.chiTietId));
              let desiredQty = matchedCartItem ? matchedCartItem.soLuong : dbItem.soLuong;
              const totalStockAllowed = (Number(dbItem.soLuongTon) || 0) + (Number(dbItem.soLuong) || 0);
              
              if (desiredQty > totalStockAllowed) {
                 desiredQty = totalStockAllowed > 0 ? totalStockAllowed : 1;
                 hasAdjusted = true;
              }
              return { ...dbItem, soLuong: desiredQty };
            }).filter(item => item.soLuong > 0);
            
            chuyenHoaDonThanhBanNhap(detail);
            
            if (hasAdjusted) {
              thongBaoLoi.value = "Số lượng không đủ! Đã tự động điều chỉnh số lượng trong giỏ hàng về mức tối đa khả dụng.";
              setTimeout(() => {
                luuHoaDonHienTai(true).catch(() => {});
              }, 500);
            }
          } else {
            chuyenHoaDonThanhBanNhap(detail);
          }
        } catch (e) {
          console.error("Không thể tải lại hóa đơn để rollback:", e);
        }
      }

      throw error;
    } finally {
      dangLuuAPI = false;
    }
  }
  
  let dangLuuNoiBo = false;
  let skipNextAutosave = false;
  let boDemTuDongLuu = null;
  let boDemPollChuyenKhoan = null;
  let boDemDongHoQr = null;
  watch(() => [
    cartItems.value,
    choPhepGiaoHang.value,
    tenNguoiNhanGiaoHang.value,
    sdtNguoiNhanGiaoHang.value,
    diaChiGiaoHang.value,
    phiVanChuyen.value,
    khachHangDuocChon.value,
    phieuGiamGiaDaApDung.value
  ], () => {
    if (skipNextAutosave) {
      skipNextAutosave = false;
      return;
    }

    // Cập nhật số lượng sản phẩm trên hóa đơn chờ ngay lập tức để giao diện không bị giật
    if (hoaDonChoDaChon.value) {
      const index = danhSachHoaDonCho.value.findIndex(hd => hd.id === hoaDonChoDaChon.value.id);
      if (index !== -1) {
        danhSachHoaDonCho.value[index] = {
          ...danhSachHoaDonCho.value[index],
          tongSanPham: cartItems.value.reduce((total, item) => total + (item.soLuong || 0), 0)
        };
        danhSachHoaDonCho.value = [...danhSachHoaDonCho.value];
      }
    }

    if (dangLuuNoiBo || isSyncingUI || dangTaiChiTietHoaDon.value || dangThanhToan.value) return;
    if (boDemTuDongLuu) clearTimeout(boDemTuDongLuu);
    const delay = cartItems.value.length === 0 ? 100 : 400;
    boDemTuDongLuu = setTimeout(() => {
      luuHoaDonHienTai().catch(() => {});
    }, delay);
  }, { deep: true });

  let lastReceivedSyncState = null;

  watch(() => [
    choPhepGiaoHang.value,
    tenNguoiNhanGiaoHang.value,
    sdtNguoiNhanGiaoHang.value,
    diaChiGiaoHang.value,
    tienKhachDua.value,
    tienMatKetHop.value,
    tienChuyenKhoanKetHop.value,
    phuongThucThanhToan.value,
    hienThiMaQrLon.value,
    ghiChuThanhToan.value,
    tuKhoaKhachHang.value,
    khachHangDuocChon.value
  ], () => {
    if (dangLuuNoiBo || isSyncingUI || dangTaiChiTietHoaDon.value) return;
    if (hoaDonChoDaChon.value) {
      const payloadState = {
        choPhepGiaoHang: choPhepGiaoHang.value,
        tenNguoiNhanGiaoHang: tenNguoiNhanGiaoHang.value,
        sdtNguoiNhanGiaoHang: sdtNguoiNhanGiaoHang.value,
        diaChiGiaoHang: diaChiGiaoHang.value,
        tienKhachDua: tienKhachDua.value,
        tienMatKetHop: tienMatKetHop.value,
        tienChuyenKhoanKetHop: tienChuyenKhoanKetHop.value,
        phuongThucThanhToan: phuongThucThanhToan.value,
        hienThiMaQrLon: hienThiMaQrLon.value,
        ghiChuThanhToan: ghiChuThanhToan.value,
        tuKhoaKhachHang: tuKhoaKhachHang.value,
        khachHangDuocChon: khachHangDuocChon.value
      };

      if (JSON.stringify(lastReceivedSyncState) === JSON.stringify(payloadState)) {
        return;
      }

      publishMessage('/topic/admin/pos-sync', {
        sender: sessionId,
        action: 'SYNC_STATE',
        invoiceId: hoaDonChoDaChon.value.id,
        state: payloadState
      });
    }
  }, { deep: true });

  async function chonHoaDonCho(invoice) {
    if (!invoice) return;

    if (hoaDonChoDaChon.value?.id === invoice.id && !dangTaiChiTietHoaDon.value) {
      return;
    }

    if (boDemTuDongLuu) {
      clearTimeout(boDemTuDongLuu);
      boDemTuDongLuu = null;
    }

    if (!isSyncingUI) {
      publishMessage('/topic/admin/pos-sync', {
        sender: sessionId,
        action: 'CHON_HOA_DON',
        invoiceId: invoice.id
      });
    }

    if (hoaDonChoDaChon.value && hoaDonChoDaChon.value.id !== invoice.id) {
      try {
        await luuHoaDonHienTai();
      } catch (e) {
        // ignore error when switching tabs
      }
    }

    hoaDonChoDaChon.value = invoice;
    dangTaiChiTietHoaDon.value = true;
    thongBaoLoi.value = "";
    try {
      await taiSanPham("", true);
      const detail = await layChiTietHoaDonCho(invoice.id);
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
      showError(thongBaoLoi.value);
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
      
      // Báo cho các thiết bị khác chuyển sang hóa đơn mới tạo
      if (!isSyncingUI) {
        publishMessage('/topic/admin/pos-sync', {
          sender: sessionId,
          action: 'CHON_HOA_DON',
          invoiceId: createdInvoice.id
        });
      }
      
    } catch (error) {
      thongBaoLoi.value = error instanceof Error ? error.message : "Không thể tạo hóa đơn chờ";
      showError(thongBaoLoi.value);
    } finally {
      dangLuuHoaDonCho.value = false;
    }
  }

  /** Dừng vòng poll trạng thái chuyển khoản (nếu đang chạy). */
  function dungPollChuyenKhoan() {
    if (boDemPollChuyenKhoan) {
      clearInterval(boDemPollChuyenKhoan);
      boDemPollChuyenKhoan = null;
    }
  }

  function dungDongHoQr() {
    if (boDemDongHoQr) {
      clearInterval(boDemDongHoQr);
      boDemDongHoQr = null;
    }
  }

  // Mã QR chỉ sống 5 phút (BE trả hetHanLuc) — hết giờ thì ngừng chờ và mời thu ngân sinh mã mới.
  function batDauDemNguocQr(hetHanLuc) {
    dungDongHoQr();
    const moc = hetHanLuc ? new Date(hetHanLuc).getTime() : 0;
    const capNhat = () => {
      soGiayQrConLai.value = moc ? Math.max(0, Math.ceil((moc - Date.now()) / 1000)) : 0;
      if (moc && soGiayQrConLai.value <= 0) {
        dungDongHoQr();
        dungPollChuyenKhoan();
        if (qrChuyenKhoan.value) {
          qrChuyenKhoan.value = { ...qrChuyenKhoan.value, hetHan: true };
        }
      }
    };
    capNhat();
    boDemDongHoQr = setInterval(capNhat, 1000);
  }

  /** Đóng mã QR chuyển khoản; hóa đơn vẫn nằm nguyên ở trạng thái chờ. */
  function dongQrChuyenKhoan() {
    dungPollChuyenKhoan();
    dungDongHoQr();
    soGiayQrConLai.value = 0;
    qrChuyenKhoan.value = null;
  }

  // Tiền về -> webhook SePay đã chuyển trạng thái hóa đơn, POS chỉ việc dọn màn và mở hóa đơn.
  async function hoanTatChuyenKhoan(hoaDonId, maHoaDon) {
    if (!qrChuyenKhoan.value) {
      return;
    }
    dongQrChuyenKhoan();
    thongBaoThanhCong.value = `Đã thanh toán ${maHoaDon ?? ""}`.trim();
    showToastSuccess(`Hóa đơn ${maHoaDon ?? ""} đã thanh toán chuyển khoản thành công!`);
    await taiDanhSachHoaDonCho();
    xoaBanNhap();
    if (hoaDonId && router) {
      router.push(`/admin/hoa-don/${hoaDonId}`);
    }
  }

  // Websocket có thể rớt nên vẫn poll trạng thái hóa đơn trong lúc mã QR đang hiện.
  function batDauPollChuyenKhoan(hoaDonId) {
    dungPollChuyenKhoan();
    boDemPollChuyenKhoan = setInterval(async () => {
      if (!qrChuyenKhoan.value) {
        dungPollChuyenKhoan();
        return;
      }
      try {
        const trangThai = await layTrangThaiChuyenKhoan(hoaDonId);
        if (trangThai?.daThanhToan) {
          await hoanTatChuyenKhoan(hoaDonId, trangThai.maHoaDon);
        } else if (trangThai?.trangThai === 6) {
          dongQrChuyenKhoan();
          showError("Hóa đơn đã bị hủy nên không thể thanh toán chuyển khoản.");
        }
      } catch (error) {
        // Lỗi mạng tạm thời -> để lần poll sau thử lại.
      }
    }, 2500);
  }

  // Webhook SePay đối chiếu theo mã hóa đơn nên phải có hóa đơn chờ trước khi hiện mã QR.
  async function chuanBiHoaDonChoTruocKhiQuet() {
    if (hoaDonChoDaChon.value) {
      await luuHoaDonHienTai(true);
      return hoaDonChoDaChon.value?.id ?? null;
    }
    await xuLyTaoHoaDonCho();
    return hoaDonChoDaChon.value?.id ?? null;
  }

  /**
   * Hiện mã QR chuyển khoản cho khách quét.
   * @param tuDong true (chuyển khoản toàn phần): chờ webhook SePay tự chuyển trạng thái hóa đơn.
   *               false (kết hợp tiền mặt): khách chỉ chuyển một phần nên thu ngân vẫn xác nhận tay.
   */
  async function moQrChuyenKhoan({ tuDong = true, soTien = null } = {}) {
    if (dangTaoQrChuyenKhoan.value) {
      return;
    }
    dangTaoQrChuyenKhoan.value = true;
    xoaPhanHoi();
    try {
      // Tạo hóa đơn chờ mới sẽ kéo hình thức thanh toán về tiền mặt -> giữ lại lựa chọn của thu ngân.
      const phuongThucDangChon = phuongThucThanhToan.value;
      const hoaDonId = await chuanBiHoaDonChoTruocKhiQuet();
      phuongThucThanhToan.value = phuongThucDangChon;
      if (!hoaDonId) {
        if (!thongBaoLoi.value) {
          thongBaoLoi.value = "Chưa tạo được hóa đơn chờ để hiện mã QR chuyển khoản.";
        }
        showError(thongBaoLoi.value);
        return;
      }
      const qr = await taoQrChuyenKhoan(hoaDonId, soTien);
      qrChuyenKhoan.value = { ...qr, tuDong, soTienYeuCau: soTien, hetHan: false };
      batDauDemNguocQr(qr?.hetHanLuc);
      if (tuDong) {
        batDauPollChuyenKhoan(hoaDonId);
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message : "Không thể tạo mã QR chuyển khoản";
      thongBaoLoi.value = msg;
      showError(msg);
    } finally {
      dangTaoQrChuyenKhoan.value = false;
    }
  }

  // Hình thức kết hợp: thu ngân bấm nút QR cạnh ô "Chuyển khoản" để khách quét phần còn thiếu.
  async function xuLyMoQrKetHop(soTien) {
    await moQrChuyenKhoan({ tuDong: false, soTien: soTien ?? null });
  }

  /** Mã hết hạn -> sinh mã mới cho đúng hóa đơn và đúng số tiền đang chờ. */
  async function xuLyTaoLaiQr() {
    const qr = qrChuyenKhoan.value;
    if (!qr) {
      return;
    }
    await moQrChuyenKhoan({ tuDong: qr.tuDong, soTien: qr.soTienYeuCau ?? null });
  }

  async function xuLyThanhToanNgay() {
    if (!daChonKhach.value) {
      thongBaoLoi.value = "Vui lòng chọn khách hàng hoặc Khách vãng lai trước khi thanh toán.";
      showError(thongBaoLoi.value);
      return;
    }
    if (!validateGioHang(true) || !kiemTraLoiThanhToan()) {
      return;
    }
    if (!coTheThanhToan.value) {
      return;
    }

    const betterCouponInfo = await kiemTraPhieuTotHonTruocThanhToan();
    if (betterCouponInfo) {
      const choice = await showPaymentConfirmWithCoupon({
        oldCouponCode: phieuGiamGiaDaApDung.value?.ma || null,
        newCouponCode: betterCouponInfo.coupon.ma,
        oldDiscount: betterCouponInfo.oldDiscount,
        newDiscount: betterCouponInfo.newDiscount,
        tongTienHang: tongTien.value
      });

      if (choice === 'use_new') {
        maPhieuGiamGia.value = betterCouponInfo.coupon.ma;
        await xuLyApDungPhieu(true, betterCouponInfo.coupon.ma);
      } else if (choice === 'use_old') {
        tuChoiPhieuGiamGiaTotHon(betterCouponInfo.coupon.ma);
      } else if (choice === 'cancel') {
        return;
      }
    } else {
      const isConfirmed = await showConfirm('Bạn có chắc chắn muốn thanh toán đơn hàng này không?');
      if (!isConfirmed) {
        return;
      }
    }

    // Chuyển khoản toàn phần: hiện mã QR cho khách quét, webhook SePay sẽ tự hoàn tất hóa đơn.
    if (phuongThucThanhToan.value === PHUONG_THUC_THANH_TOAN.CHUYEN_KHOAN) {
      await moQrChuyenKhoan({ tuDong: true });
      return;
    }

    dongQrChuyenKhoan();
    await thucThiThanhToan();
  }

  /**
   * Gọi API thanh toán cho hóa đơn đang dựng (đã qua bước xác nhận/validate).
   * @returns true nếu hóa đơn đã thanh toán xong.
   */
  async function thucThiThanhToan() {
    dangThanhToan.value = true;
    thongBaoLoi.value = "";
    thongBaoThanhCong.value = "";
    if (boDemTuDongLuu) {
      clearTimeout(boDemTuDongLuu);
      boDemTuDongLuu = null;
    }
    try {
      if (hoaDonChoDaChon.value) {
        await luuHoaDonHienTai(true);
      }

      const response = await thanhToanTaiQuay({
        hoaDonId: hoaDonChoDaChon.value?.id ?? null,
        khachHangId: layIdKhachHangHienTai(),
        tenKhachHang: khachHangDuocChon.value?.hoTen || (laKhachVangLai.value ? KHACH_VANG_LAI : ""),
        soDienThoai: khachHangDuocChon.value?.sdt || hoaDonChoDaChon.value?.soDienThoai || "",
        maPhieuGiamGia: phieuGiamGiaDaApDung.value?.ma ?? null,
        thongTinGiaoHang: taoPayloadGiaoHang(),
        hinhThucThanhToan: phuongThucThanhToan.value,
        tienKhachDua: phuongThucThanhToan.value === 1 ? tienKhachThanhToan.value : (phuongThucThanhToan.value === 5 ? tienKhachThanhToan.value : khachCanTra.value),
        tienMat: phuongThucThanhToan.value === 5 ? (Number(tienMatKetHop.value.replace(/\D/g, '')) || 0) : null,
        tienChuyenKhoan: phuongThucThanhToan.value === 5 ? (Number(tienChuyenKhoanKetHop.value.replace(/\D/g, '')) || 0) : null,
        ghiChu: ghiChuThanhToan.value,
        items: taoDanhSachSanPhamThanhToan()
      });
      thongBaoThanhCong.value = `Đã thanh toán ${response.maHoaDon}`;
      await taiDanhSachHoaDonCho();
      const paidInvoiceId = response.hoaDonId || response.id;
      xoaBanNhap();
      if (paidInvoiceId && router) {
        router.push(`/admin/hoa-don/${paidInvoiceId}`);
      }
      return true;
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
      // thongBaoLoi không hiển thị trên màn nên phải báo thẳng cho thu ngân biết vì sao không thanh toán được.
      showError(thongBaoLoi.value);
      return false;
    } finally {
      dangThanhToan.value = false;
    }
  }

  /**
   * Nút "Đã thanh toán" ngay trên mã QR: dùng khi tiền đã về tài khoản nhưng webhook SePay
   * không báo về (SePay lỗi, mạng rớt) nên hóa đơn còn nằm ở trạng thái chờ.
   */
  async function xuLyXacNhanDaChuyenKhoan() {
    const qr = qrChuyenKhoan.value;
    if (!qr || dangThanhToan.value) {
      return;
    }
    const hoaDonId = qr.hoaDonId;

    // Webhook vừa kịp chạy -> hóa đơn đã rời trạng thái chờ, chỉ cần dọn màn, không ghi nhận lần hai.
    try {
      const trangThai = await layTrangThaiChuyenKhoan(hoaDonId);
      if (trangThai?.daThanhToan) {
        await hoanTatChuyenKhoan(hoaDonId, trangThai.maHoaDon);
        return;
      }
    } catch (error) {
      // Không hỏi được trạng thái thì vẫn để thu ngân xác nhận tay.
    }

    // Realtime có thể đổi hóa đơn đang chọn -> không thanh toán nhầm hóa đơn khác.
    if (hoaDonChoDaChon.value?.id !== hoaDonId) {
      showError("Hóa đơn của mã QR này không còn được chọn trên màn. Vui lòng đóng mã và thao tác lại.");
      return;
    }

    const isConfirmed = await showConfirm(
      `Xác nhận đã nhận ${dinhDangTien(qr.soTien)} chuyển khoản cho hóa đơn ${qr.maHoaDon}?`
    );
    if (!isConfirmed) {
      return;
    }

    dungPollChuyenKhoan();
    dungDongHoQr();
    const daXong = await thucThiThanhToan();
    if (daXong) {
      qrChuyenKhoan.value = null;
      soGiayQrConLai.value = 0;
    } else if (qrChuyenKhoan.value?.tuDong) {
      // Thanh toán tay hỏng -> mở lại vòng chờ để webhook vẫn còn cơ hội tự hoàn tất.
      batDauPollChuyenKhoan(hoaDonId);
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
        iconColor: '#cf1018',
        target: document.getElementById('pos-tablet-screen') || 'body'
      });
      return;
    }

    const currentInvoice = hoaDonChoDaChon.value;
    const maHoaDon = currentInvoice?.ma || "";
    const hoaDonId = currentInvoice?.id;

    const isConfirmed = await showConfirm(`Bạn có chắc chắn muốn hủy hóa đơn ${maHoaDon} không?`);
    if (!isConfirmed) {
      return;
    }

    dangHuyHoaDonCho.value = true;
    thongBaoLoi.value = "";
    try {
      await huyHoaDonCho(hoaDonId);
      
      toastSwal.fire({
        icon: 'success',
        title: 'Thành công!',
        text: `Đã hủy hóa đơn chờ ${maHoaDon}`,
        timer: 3000,
        iconColor: '#cf1018',
        target: document.getElementById('pos-tablet-screen') || 'body'
      });

      await taiDanhSachHoaDonCho();
      xoaBanNhap();
    } catch (error) {
      thongBaoLoi.value = error instanceof Error ? error.message : "Không thể hủy hóa đơn chờ";
      showError(thongBaoLoi.value);
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
    dungPollChuyenKhoan();
    dungDongHoQr();
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
    document.addEventListener('visibilitychange', dongBoGiaKhiQuayLaiTab);
    window.addEventListener('focus', dongBoGiaKhiQuayLaiTab);
    await taiSanPham("");
    await taiDanhSachHoaDonCho();

    const reorderStr = localStorage.getItem("reorder_items");
    if (reorderStr) {
      try {
        const reorderItems = JSON.parse(reorderStr);
        localStorage.removeItem("reorder_items");

        if (!hoaDonChoDaChon.value) {
          if (danhSachHoaDonCho.value.length > 0) {
            await chonHoaDonCho(danhSachHoaDonCho.value[0]);
          } else {
            await xuLyTaoHoaDonChoMoi();
          }
        }

        if (hoaDonChoDaChon.value) {
          const mappedItems = reorderItems.map(item => ({
            cartItemId: Date.now().toString() + Math.random().toString(),
            chiTietId: item.giayChiTietId,
            maSanPham: item.maSanPham,
            tenSanPham: item.tenSanPham,
            sku: item.sku || "",
            mauSac: item.mauSac || "",
            kichCo: item.kichCo || "",
            hinhAnh: item.hinhAnh || "",
            soLuong: item.soLuong,
            soLuongBanDau: 0,
            giaBan: item.giaBan,
            giaGoc: item.giaBan,
            soLuongTon: item.soLuongTon != null ? item.soLuongTon : 9999
          }));

          cartItems.value = mappedItems;
          await luuHoaDonHienTai(true);
          publishMessage('/topic/admin/pos-sync', {
            sender: sessionId,
            action: 'UPDATED',
            invoiceId: hoaDonChoDaChon.value.id
          });
        }
      } catch (err) {
        console.error("Lỗi khi khôi phục sản phẩm mua lại:", err);
      }
    }
  });

  onBeforeUnmount(() => {
    document.removeEventListener('visibilitychange', dongBoGiaKhiQuayLaiTab);
    window.removeEventListener('focus', dongBoGiaKhiQuayLaiTab);
    if (boDemDongBoGiaRealtime) clearTimeout(boDemDongBoGiaRealtime);
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
    boLocMauSacDaChon,
    boLocKichCoDaChon,
    thuongHieuCoSan,
    danhMucCoSan,
    mauSacCoSan,
    kichCoCoSan,
    giaThapNhatDaChon,
    giaCaoNhatDaChon,
    giaCaoNhatCoSan,
    nhanTimKiemSanPham,
    cartItems,
    chiTietSanPhamDaChon,
    chiTietDangChon,
    hinhAnhDangChon,
    soLuongTonSauKhiChon,
    bienTheLienQuan,
    luaChonMauSac,
    luaChonKichCo,
    mauSacDaChon,
    kichCoDaChon,
    soLuongDaChon,
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
    phieuGiamGiaMucTiepTheo,
    soTienThieuChoMucTiepTheo,
    soSanPhamThieuChoMucTiepTheo,
    soTienGiamMucTiepTheo,
    loiNhomPhieuGiamGiaTotHon,
    tuChoiPhieuGiamGiaTotHon,
    chapNhanPhieuGiamGiaTotHon,
    khachCanTra,
    thongTinGiaoHang,
    phuongThucThanhToan: phuongThucThanhToan,
    tienKhachDua: tienKhachDua,
    tienMatKetHop,
    tienChuyenKhoanKetHop,
    thongBaoLoiThanhToan: thongBaoLoiThanhToan,
    tienThua,
    ghiChuThanhToan: ghiChuThanhToan,
    coTheTaoHoaDonCho,
    dangLuuHoaDonCho,
    coTheThanhToan,
    dangThanhToan,
    qrChuyenKhoan,
    dangTaoQrChuyenKhoan,
    soGiayQrConLai,
    dongQrChuyenKhoan,
    xuLyMoQrKetHop,
    xuLyTaoLaiQr,
    xuLyXacNhanDaChuyenKhoan,
    dangHuyHoaDonCho,
    dinhDangTien,
    soLuongConLai,
    isOutdatedPrice,
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
    dongChiTietSanPham: () => {
      itemDangDoiBienThe.value = null;
      dongChiTietSanPham();
    },
    chonMauSac,
    chonKichCo,
    chonBienThe,
    giamSoLuongChiTiet,
    tangSoLuongChiTiet,
    capNhatSoLuongChiTiet,
    themBienTheDangChon,
    themTrucTiepBienThe,
    itemDangDoiBienThe,
    xuLyMoDoiBienTheInCart,
    xuLyDoiBienTheInCart,
    taiSanPham,
    xuLyQuetQrSanPham,
    xuLyKhiFocusPhieu: xuLyKhiFocusPhieu,
    xuLyKhiBlurPhieu: xuLyKhiBlurPhieu,
    xuLyApDungPhieu: xuLyApDungPhieu,
    chonPhieuGiamGia,
    xuLyGoPhieu: xuLyGoPhieu,
    capNhatThongTinGiaoHang,
    xuLyTinhPhiVanChuyen,
    xuLyTienKhachDuaInput: xuLyTienKhachDuaInput,
    xuLyTienMatKetHopInput,
    xuLyTienChuyenKhoanKetHopInput,
    hienThiMaQrLon,
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
