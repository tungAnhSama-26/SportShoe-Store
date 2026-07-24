import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  huyHoaDonCho,
  layChiTietHoaDonCho,
  layDanhSachHoaDonCho,
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
    tuDongApDungVaDeXuatHangMucTiepTheo,
    phieuGiamGiaHangMucTiepTheo: phieuGiamGiaMucTiepTheo,
    soTienThieuDeDatHangMuc: soTienThieuChoMucTiepTheo,
    soSanPhamThieuDeDatHangMuc: soSanPhamThieuChoMucTiepTheo,
    soTienGiamCuaHangMucTiepTheo: soTienGiamMucTiepTheo,
    phieuTotHonDeXuat: loiNhomPhieuGiamGiaTotHon,
    tuChoiPhieuTotHon: tuChoiPhieuGiamGiaTotHon,
    chapNhanPhieuTotHon: chapNhanPhieuGiamGiaTotHon,
    kiemTraPhieuTotHonTruocThanhToan
  } = LogicPhieuGiamGia({
    cartItems,
    tongTien,
    hoaDonChoDaChon,
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

  const {
    xuLyInHoaDonTaiQuay
  } = LogicInHoaDon();

  const {
    chiTietIdThemMoi,
    ketQuaBienTheSanPham
  } = LogicSanPham({ thongBaoLoi });

  const { subscribeTopic, publishMessage } = useRealtime();

  const sessionId = Math.random().toString(36).substring(2, 15);
  let isSyncingUI = false;
  let lastLocalSaveTime = 0;

  subscribeTopic('/topic/admin/pos-sync', async (rawMsg) => {
    const msg = rawMsg?.payload ?? rawMsg;
    if (msg.sender === sessionId) return;

    if (rawMsg?.type === 'POS_INVOICE_CHANGED' || ['CREATED', 'UPDATED', 'CANCELLED', 'PAID'].includes(msg.action)) {
      if (Date.now() - lastLocalSaveTime < 2500 && (msg.action === 'UPDATED' || msg.action === 'CREATED')) {
        return;
      }
      try {
        await taiDanhSachHoaDonCho();
        if (msg.action === 'PAID' || msg.action === 'CANCELLED') {
          if (hoaDonChoDaChon.value?.id === msg.invoiceId) {
            xoaBanNhap();
          }
          return;
        }
        const invoice = danhSachHoaDonCho.value.find((hd) => hd.id === msg.invoiceId);
        if (invoice) {
          if (hoaDonChoDaChon.value?.id === msg.invoiceId) {
            await chonHoaDonCho(invoice);
          } else if (!hoaDonChoDaChon.value && msg.action === 'CREATED') {
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
        await taiDanhSachHoaDonCho();
        const invoice = danhSachHoaDonCho.value.find(hd => hd.id === msg.invoiceId);
        if (invoice) {
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

      lastReceivedSyncState = msg.state;

      choPhepGiaoHang.value = msg.state.choPhepGiaoHang;
      tenNguoiNhanGiaoHang.value = msg.state.tenNguoiNhanGiaoHang;
      sdtNguoiNhanGiaoHang.value = msg.state.sdtNguoiNhanGiaoHang;
      diaChiGiaoHang.value = msg.state.diaChiGiaoHang;
      tienKhachDua.value = msg.state.tienKhachDua;
      phuongThucThanhToan.value = msg.state.phuongThucThanhToan;
      ghiChuThanhToan.value = msg.state.ghiChuThanhToan;
      tuKhoaKhachHang.value = msg.state.tuKhoaKhachHang;
      khachHangDuocChon.value = msg.state.khachHangDuocChon;

      setTimeout(() => {
        isSyncingUI = false;
        dangLuuNoiBo = false;
      }, 50);
    }
  });

  subscribeTopic('/topic/admin/san-pham', async (message) => {
    if (message.type === 'PRODUCT_CHANGED') {
      if (dangLuuNoiBo || dangLuuHoaDonCho.value || dangThanhToan.value) return;
      if (Date.now() - lastLocalSaveTime < 2500) return;

      dangLuuNoiBo = true;
      try {
        await taiDanhSachHoaDonCho();
        if (hoaDonChoDaChon.value) {
          const stillExists = danhSachHoaDonCho.value.find(hd => hd.id === hoaDonChoDaChon.value.id);
          if (stillExists) {
            const detail = await layChiTietHoaDonCho(hoaDonChoDaChon.value.id);
            chuyenHoaDonThanhBanNhap(detail);
          } else {
            // Hóa đơn đã bị xóa hoặc thanh toán bởi người khác
            hoaDonChoDaChon.value = danhSachHoaDonCho.value.length > 0 ? danhSachHoaDonCho.value[0] : null;
            if (hoaDonChoDaChon.value) {
              const detail = await layChiTietHoaDonCho(hoaDonChoDaChon.value.id);
              chuyenHoaDonThanhBanNhap(detail);
            } else {
              // Xóa trắng giỏ hàng và thông tin khách
              cartItems.value = [];
              khachHangDuocChon.value = null;
              tuKhoaKhachHang.value = "";
              choPhepGiaoHang.value = false;
              phieuGiamGiaDaApDung.value = null;
            }
          }
        } else if (danhSachHoaDonCho.value.length > 0) {
          // Tự động chọn hóa đơn mới tạo nếu chưa có hóa đơn nào được chọn
          hoaDonChoDaChon.value = danhSachHoaDonCho.value[0];
          const detail = await layChiTietHoaDonCho(hoaDonChoDaChon.value.id);
          chuyenHoaDonThanhBanNhap(detail);
        }
      } catch (e) {
        console.error("Lỗi khi tải lại dữ liệu realtime:", e);
      } finally {
        setTimeout(() => { dangLuuNoiBo = false; }, 50);
      }
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
    diaChiGiaoHang.value = "";
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

  const itemDangDoiBienThe = ref(null);

  async function xuLyMoDoiBienTheInCart(item) {
    itemDangDoiBienThe.value = item;
    const products = await timSanPhamTaiQuay(item.maSanPham);
    if (products && products.length > 0) {
      moChiTietSanPham(products[0]);
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
          iconColor: '#f59e0b',
          target: document.getElementById('pos-tablet-screen') || 'body'
        });
      } else {
        showToastSuccess(`Đã thêm ${soLuongDaChon.value} sản phẩm vào hóa đơn`);
      }
    }
  }

  function chuyenHoaDonThanhBanNhap(invoice) {
    isSyncingUI = true;
    skipNextAutosave = true;
    const thongTinTheoChiTietId = new Map(
      ketQuaBienTheSanPham.value.map((product) => [product.chiTietId, product])
    );
    const thongTinGiaoHang = invoice.thongTinGiaoHang || null;

    tuKhoaKhachHang.value = invoice.tenKhachHang || invoice.soDienThoai || "";
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
    dangLuuNoiBo = true;
    cartItems.value = invoice.items.map((item) => {
      const thongTinSanPham = thongTinTheoChiTietId.get(item.chiTietId);
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
    setTimeout(() => { 
      dangLuuNoiBo = false; 
      isSyncingUI = false;
    }, 50);
    choPhepGiaoHang.value = Boolean(thongTinGiaoHang?.giaoHang);
    tenNguoiNhanGiaoHang.value = thongTinGiaoHang?.tenNguoiNhan || "";
    sdtNguoiNhanGiaoHang.value = thongTinGiaoHang?.soDienThoaiNguoiNhan || "";
    if (thongTinGiaoHang?.giaoHang) {
      diaChiGiaoHang.value = thongTinGiaoHang.diaChiGiaoHang || "";
    } else if (!diaChiGiaoHang.value && khachHangDuocChon.value?.diaChiMacDinh) {
      diaChiGiaoHang.value = khachHangDuocChon.value.diaChiMacDinh;
    } else if (!thongTinGiaoHang?.giaoHang && !khachHangDuocChon.value) {
      diaChiGiaoHang.value = "";
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
  }

  async function luuHoaDonHienTai(force = false) {
    if (!hoaDonChoDaChon.value) return;
    if (dangThanhToan.value && !force) return;
    lastLocalSaveTime = Date.now();
    try {
      const payload = {
        tenKhachHang: khachHangDuocChon.value?.hoTen || tenNguoiNhanGiaoHang.value || (laKhachVangLai.value ? KHACH_VANG_LAI : ""),
        soDienThoai: khachHangDuocChon.value?.sdt || sdtNguoiNhanGiaoHang.value || "",
        ghiChu: "",
        khachHangId: khachHangDuocChon.value?.id || null,
        maPhieuGiamGia: phieuGiamGiaDaApDung.value?.ma || null,
        thongTinGiaoHang: (choPhepGiaoHang.value && coThongTinGiaoHangHopLe.value) ? taoPayloadGiaoHang() : null,
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
          
          if (msg.includes("Số lượng tồn kho không đủ")) {
            let hasAdjusted = false;
            const currentCartItems = [...cartItems.value];
            
            detail.items = detail.items.map(dbItem => {
              const matchedCartItem = currentCartItems.find(c => c.chiTietId === dbItem.chiTietId);
              let desiredQty = matchedCartItem ? matchedCartItem.soLuong : dbItem.soLuong;
              
              if (desiredQty > dbItem.soLuongTon) {
                 desiredQty = dbItem.soLuongTon > 0 ? 1 : 0;
                 hasAdjusted = true;
              }
              return { ...dbItem, soLuong: desiredQty };
            }).filter(item => item.soLuong > 0);
            
            chuyenHoaDonThanhBanNhap(detail);
            
            if (hasAdjusted) {
              thongBaoLoi.value = "Kho không đủ! Đã tự động điều chỉnh số lượng trong giỏ hàng về 1.";
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
    }
  }
  
  let dangLuuNoiBo = false;
  let skipNextAutosave = false;
  let boDemTuDongLuu = null;
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
          tongSanPham: cartItems.value.reduce((total, item) => total + item.soLuong, 0)
        };
      }
    }

    if (dangLuuNoiBo || dangThanhToan.value) return;
    if (boDemTuDongLuu) clearTimeout(boDemTuDongLuu);
    boDemTuDongLuu = setTimeout(() => {
      luuHoaDonHienTai().catch(() => {});
    }, 300);
  }, { deep: true });

  let lastReceivedSyncState = null;

  watch(() => [
    choPhepGiaoHang.value,
    tenNguoiNhanGiaoHang.value,
    sdtNguoiNhanGiaoHang.value,
    diaChiGiaoHang.value,
    tienKhachDua.value,
    phuongThucThanhToan.value,
    ghiChuThanhToan.value,
    tuKhoaKhachHang.value
  ], () => {
    if (isSyncingUI || dangTaiChiTietHoaDon.value) return;
    if (hoaDonChoDaChon.value) {
      const payloadState = {
        choPhepGiaoHang: choPhepGiaoHang.value,
        tenNguoiNhanGiaoHang: tenNguoiNhanGiaoHang.value,
        sdtNguoiNhanGiaoHang: sdtNguoiNhanGiaoHang.value,
        diaChiGiaoHang: diaChiGiaoHang.value,
        tienKhachDua: tienKhachDua.value,
        phuongThucThanhToan: phuongThucThanhToan.value,
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
    lastLocalSaveTime = Date.now();
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
        await xuLyApDungPhieu(true);
      } else if (choice === 'cancel') {
        return;
      }
    } else {
      const isConfirmed = await showConfirm('Bạn có chắc chắn muốn thanh toán đơn hàng này không?');
      if (!isConfirmed) {
        return;
      }
    }

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
        iconColor: '#cf1018',
        target: document.getElementById('pos-tablet-screen') || 'body'
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
        iconColor: '#cf1018',
        target: document.getElementById('pos-tablet-screen') || 'body'
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
        }
      } catch (err) {
        console.error("Lỗi khi khôi phục sản phẩm mua lại:", err);
      }
    }
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
    boLocMauSacDaChon,
    boLocKichCoDaChon,
    thuongHieuCoSan,
    danhMucCoSan,
    mauSacCoSan,
    kichCoCoSan,
    giaToiThieuDaChon: giaThapNhatDaChon,
    giaToiDaDaChon: giaCaoNhatDaChon,
    giaToiDaCoSan: giaCaoNhatCoSan,
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
