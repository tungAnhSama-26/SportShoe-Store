import { computed, onActivated, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  CheckCircle2,
  Eye,
  FileSpreadsheet,
  Filter,
  Home,
  MapPin,
  Package,
  Plus,
  RotateCcw,
  Search,
  ShoppingBag,
  Trash2,
  Users,
  X,
} from "lucide-vue-next";
import {
  doiTrangThaiKhachHang,
  layDanhSachKhachHang,
  layDanhSachDiaChi,
  themDiaChi,
  capNhatDiaChi,
  xoaDiaChi,
  datMacDinhDiaChi,
  layHoaDonTheoKhachHang,
} from "../../../services/khach-hang";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import { exportRowsToExcel } from "../../../utils/export-excel";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import { showSuccess, showError, showConfirm } from "../../../utils/alert";
import { isValidVnPhone } from "../../../utils/validation";
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";
import Input from "../../../components/ui/Input.vue";
import Badge from "../../../components/ui/Badge.vue";
import Table from "../../../components/ui/Table.vue";

export function useQuanLyKhachHang() {
  const router = useRouter();
  const CUSTOMER_CREATE_TOAST_KEY = "admin-khach-hang-toast";

  const danhSach = ref([]);
  const dangTai = ref(false);
  const loiTrang = ref("");
  const boLoc = ref({ keyword: "", trangThai: "" });

  const dsTrangThai = [
    { label: "Tất cả", value: "" },
    { label: "Hoạt động", value: "1" },
    { label: "Khóa", value: "0" },
  ];

  function hienThiThongBao(loai, tieuDe, noiDung = "", iconColor) {
    if (loai === "success") {
      showSuccess(
        noiDung || tieuDe,
        noiDung ? tieuDe : "Thành công",
        iconColor,
      );
    } else {
      showError(noiDung || tieuDe, noiDung ? tieuDe : "Thất bại");
    }
  }

  function taiThongBaoDieuHuong() {
    if (typeof window === "undefined") return;
    const raw = window.sessionStorage.getItem(CUSTOMER_CREATE_TOAST_KEY);
    if (!raw) return;
    window.sessionStorage.removeItem(CUSTOMER_CREATE_TOAST_KEY);

    try {
      const payload = JSON.parse(raw);
      const noiDung =
        typeof payload?.noiDung === "string" ? payload.noiDung : "";
      const iconColor =
        typeof payload?.iconColor === "string" ? payload.iconColor : undefined;
      hienThiThongBao(
        payload?.loai === "error" ? "error" : "success",
        payload?.tieuDe || "Thao tác thành công",
        noiDung,
        iconColor,
      );
    } catch {
      hienThiThongBao("success", "Đã tạo khách hàng mới");
    }
  }

  function mauTrangThai(trangThai) {
    return trangThai === 1
      ? "bg-emerald-50 text-emerald-600"
      : "bg-rose-50 text-rose-600";
  }

  function dinhDangNgay(ngay) {
    if (!ngay) return "—";
    return new Intl.DateTimeFormat("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(new Date(ngay));
  }

  function dinhDangTien(n) {
    if (n == null) return "—";
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(n);
  }

  function mauTrangThaiDon(trangThai) {
    const map = {
      "Hoàn thành": "bg-emerald-50 text-emerald-600",
      "Đang giao hàng": "bg-sky-50 text-sky-600",
      "Chờ xác nhận": "bg-amber-50 text-amber-600",
      "Đã xác nhận": "bg-orange-50 text-orange-600",
      "Chờ lấy hàng": "bg-violet-50 text-violet-600",
      "Đã giao hàng": "bg-teal-50 text-teal-600",
      Hủy: "bg-rose-50 text-rose-600",
      "Yêu cầu hủy": "bg-orange-50 text-orange-600",
    };
    return map[trangThai] ?? "bg-slate-50 text-slate-600";
  }

  function badgeTrangThaiDon(trangThai) {
    const map = {
      "Hoàn thành": "success",
      "Đang giao hàng": "info",
      "Chờ xác nhận": "warning",
      "Đã xác nhận": "primary",
      "Chờ lấy hàng": "info",
      "Đã giao hàng": "success",
      Hủy: "danger",
      "Yêu cầu hủy": "warning",
    };
    return map[trangThai] ?? "default";
  }

  // Phân trang
  const soPhanTuMotTrang = ref(5);
  const trangHienTai = ref(1);
  const pageSizeOptions = [5, 10, 20];
  const tongSoTrang = computed(
    () =>
      Math.ceil((danhSach.value || []).length / soPhanTuMotTrang.value) || 1,
  );
  const danhSachPhanTrang = computed(() => {
    const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
    return (danhSach.value || []).slice(start, start + soPhanTuMotTrang.value);
  });

  watch(danhSach, () => {
    trangHienTai.value = 1;
  });
  watch(soPhanTuMotTrang, () => {
    trangHienTai.value = 1;
  });

  async function taiDanhSach() {
    dangTai.value = true;
    loiTrang.value = "";
    try {
      const data = await layDanhSachKhachHang({
        keyword: boLoc.value.keyword || undefined,
        trangThai:
          boLoc.value.trangThai !== ""
            ? Number(boLoc.value.trangThai)
            : undefined,
      });
      danhSach.value = Array.isArray(data) ? data : [];
    } catch (e) {
      danhSach.value = [];
      loiTrang.value = getDisplayErrorMessage(
        e,
        "Không thể tải danh sách khách hàng",
      );
    } finally {
      dangTai.value = false;
    }
  }

  function lamMoiBoLoc() {
    boLoc.value = { keyword: "", trangThai: "" };
  }

  function xemChiTiet(id) {
    router.push({ name: "admin-khach-hang-chi-tiet", params: { id } });
  }

  const dangDoiTrangThai = ref(null);

  async function toggleTrangThai(kh) {
    if (dangDoiTrangThai.value) return;
    const trangThaiMoi = kh.trangThai === 1 ? 0 : 1;
    const hanhDong = trangThaiMoi === 1 ? "kích hoạt" : "khóa";
    const confirmed = await showConfirm(
      `Bạn có chắc muốn ${hanhDong} khách hàng này không?`,
      "Xác nhận thay đổi",
      "Xác nhận",
      "Hủy",
    );
    if (!confirmed) return;
    dangDoiTrangThai.value = kh.id;
    try {
      await doiTrangThaiKhachHang(kh.id, trangThaiMoi);
      kh.trangThai = trangThaiMoi;
      kh.tenTrangThai = trangThaiMoi === 1 ? "Hoạt động" : "Khóa";
    } catch (e) {
      loiTrang.value = getDisplayErrorMessage(
        e,
        "Không thể cập nhật trạng thái",
      );
      setTimeout(() => (loiTrang.value = ""), 3000);
    } finally {
      dangDoiTrangThai.value = null;
    }
  }

  function themMoi() {
    router.push({ name: "admin-khach-hang-them" });
  }

  function xuatExcel() {
    if (!(danhSach.value || []).length) {
      showError("Không có dữ liệu để xuất Excel.");
      return;
    }
    exportRowsToExcel({
      filename: "quan-ly-khach-hang",
      sheetName: "KhachHang",
      columns: [
        { label: "STT", value: (_, index) => index + 1 },
        { label: "Tên đăng nhập", key: "tenDangNhap" },
        { label: "Họ tên", key: "hoTen" },
        { label: "Email", value: (row) => row.email || "—" },
        {
          label: "Số điện thoại",
          value: (row) => row.sdtMacDinh || row.sdt || "—",
        },
        { label: "Giới tính", value: (row) => row.tenGioiTinh || "—" },
        { label: "Địa chỉ mặc định", value: (row) => row.diaChiMacDinh || "—" },
        { label: "Trạng thái", value: (row) => row.tenTrangThai || "—" },
      ],
      rows: danhSach.value || [],
    });
  }

  let timer;
  watch(
    () => boLoc.value,
    () => {
      clearTimeout(timer);
      timer = setTimeout(taiDanhSach, 300);
    },
    { deep: true },
  );

  // ========================
  // MODAL ĐỊA CHỈ
  // ========================
  const khModalDiaChi = ref(null);
  const dsDiaChiModal = ref([]);
  const dangTaiDiaChi = ref(false);
  const loiDiaChi = ref("");
  const hienFormDiaChi = ref(false);
  const diaChiDangSua = ref(null);
  const dangLuuDiaChi = ref(false);
  const formDiaChi = ref({
    hoTen: "",
    sdt: "",
    tinhThanh: "",
    quanHuyen: "",
    phuongXa: "",
    diaChiCuThe: "",
    laMacDinh: false,
  });

  // Province cascade
  const dsTinh = ref([]);
  const dsHuyen = ref([]);
  const dsXa = ref([]);
  const maTinhChon = ref(null);
  const maHuyenChon = ref(null);
  const dangTaiDiaPhuong = ref(false);

  async function taiDsTinh() {
    if (dsTinh.value.length) return;
    dangTaiDiaPhuong.value = true;
    try {
      const res = await fetch("https://provinces.open-api.vn/api/p/");
      dsTinh.value = await res.json();
    } catch {
      dsTinh.value = [];
    } finally {
      dangTaiDiaPhuong.value = false;
    }
  }

  async function onTinhChange(code) {
    maTinhChon.value = code;
    dsHuyen.value = [];
    dsXa.value = [];
    maHuyenChon.value = null;
    formDiaChi.value.tinhThanh =
      dsTinh.value.find((t) => t.code === code)?.name ?? "";
    formDiaChi.value.quanHuyen = "";
    formDiaChi.value.phuongXa = "";
    if (!code) return;
    try {
      const res = await fetch(
        `https://provinces.open-api.vn/api/p/${code}?depth=2`,
      );
      const data = await res.json();
      dsHuyen.value = data.districts ?? [];
    } catch {
      dsHuyen.value = [];
    }
  }

  async function onHuyenChange(code) {
    maHuyenChon.value = code;
    dsXa.value = [];
    formDiaChi.value.quanHuyen =
      dsHuyen.value.find((h) => h.code === code)?.name ?? "";
    formDiaChi.value.phuongXa = "";
    if (!code) return;
    try {
      const res = await fetch(
        `https://provinces.open-api.vn/api/d/${code}?depth=2`,
      );
      const data = await res.json();
      dsXa.value = data.wards ?? [];
    } catch {
      dsXa.value = [];
    }
  }

  function onXaChange(tenXa) {
    formDiaChi.value.phuongXa = tenXa;
  }

  async function preFillCascadeForEdit(dc) {
    await taiDsTinh();
    const tinh = dsTinh.value.find((t) => t.name === dc.tinhThanh);
    if (!tinh) return;
    maTinhChon.value = tinh.code;
    try {
      const res = await fetch(
        `https://provinces.open-api.vn/api/p/${tinh.code}?depth=2`,
      );
      const data = await res.json();
      dsHuyen.value = data.districts ?? [];
      const huyen = dsHuyen.value.find((h) => h.name === dc.quanHuyen);
      if (!huyen) return;
      maHuyenChon.value = huyen.code;
      const res2 = await fetch(
        `https://provinces.open-api.vn/api/d/${huyen.code}?depth=2`,
      );
      const data2 = await res2.json();
      dsXa.value = data2.wards ?? [];
    } catch {
      /* ignore */
    }
  }

  async function moModalDiaChi(kh) {
    khModalDiaChi.value = kh;
    hienFormDiaChi.value = false;
    diaChiDangSua.value = null;
    loiDiaChi.value = "";
    await taiDsModalDiaChi(kh.id);
    await taiDsTinh();
  }

  async function taiDsModalDiaChi(khId) {
    dangTaiDiaChi.value = true;
    try {
      dsDiaChiModal.value = await layDanhSachDiaChi(khId);
    } catch {
      dsDiaChiModal.value = [];
    } finally {
      dangTaiDiaChi.value = false;
    }
  }

  function dongModalDiaChi() {
    khModalDiaChi.value = null;
    dsDiaChiModal.value = [];
    hienFormDiaChi.value = false;
    diaChiDangSua.value = null;
    maTinhChon.value = null;
    maHuyenChon.value = null;
    dsHuyen.value = [];
    dsXa.value = [];
  }

  function moThemDiaChiModal() {
    diaChiDangSua.value = null;
    formDiaChi.value = {
      hoTen: khModalDiaChi.value?.hoTen ?? "",
      sdt: khModalDiaChi.value?.sdt ?? "",
      tinhThanh: "",
      quanHuyen: "",
      phuongXa: "",
      diaChiCuThe: "",
      laMacDinh: dsDiaChiModal.value.length === 0,
    };
    maTinhChon.value = null;
    maHuyenChon.value = null;
    dsHuyen.value = [];
    dsXa.value = [];
    hienFormDiaChi.value = true;
  }

  async function moSuaDiaChiModal(dc) {
    diaChiDangSua.value = dc;
    formDiaChi.value = {
      hoTen: dc.hoTen,
      sdt: dc.sdt,
      tinhThanh: dc.tinhThanh,
      quanHuyen: dc.quanHuyen,
      phuongXa: dc.phuongXa,
      diaChiCuThe: dc.diaChiCuThe,
      laMacDinh: dc.laMacDinh,
    };
    maTinhChon.value = null;
    maHuyenChon.value = null;
    dsHuyen.value = [];
    dsXa.value = [];
    await preFillCascadeForEdit(dc);
    hienFormDiaChi.value = true;
  }

  async function luuDiaChiModal() {
    const f = formDiaChi.value;
    const dangSua = Boolean(diaChiDangSua.value);
    if (
      !f.hoTen ||
      !f.sdt ||
      !f.tinhThanh ||
      !f.quanHuyen ||
      !f.phuongXa ||
      !f.diaChiCuThe
    ) {
      loiDiaChi.value = "Vui lòng điền đầy đủ thông tin địa chỉ.";
      return;
    }
    if (!isValidVnPhone(f.sdt)) {
      loiDiaChi.value = "Số điện thoại không đúng định dạng (VD: 0901234567).";
      return;
    }
    loiDiaChi.value = "";
    dangLuuDiaChi.value = true;
    try {
      if (diaChiDangSua.value) {
        await capNhatDiaChi(diaChiDangSua.value.id, f);
      } else {
        await themDiaChi(khModalDiaChi.value.id, f);
      }
      await taiDsModalDiaChi(khModalDiaChi.value.id);
      // Cập nhật diaChiMacDinh trong bảng nếu có thay đổi mặc định
      if (f.laMacDinh) capNhatDiaChiMacDinhTrongBang(khModalDiaChi.value.id);
      hienFormDiaChi.value = false;
      diaChiDangSua.value = null;
      hienThiThongBao(
        "success",
        f.laMacDinh
          ? "Đã lưu địa chỉ mặc định"
          : dangSua
            ? "Đã cập nhật địa chỉ"
            : "Đã thêm địa chỉ mới",
      );
    } catch (e) {
      loiDiaChi.value = getDisplayErrorMessage(e, "Không thể lưu địa chỉ.");
    } finally {
      dangLuuDiaChi.value = false;
    }
  }

  async function xoaDiaChiModal(diaChiId) {
    const confirmed = await showConfirm(
      "Bạn có chắc chắn muốn xóa địa chỉ này?",
      "Xác nhận xóa",
      "Xóa",
      "Hủy",
    );
    if (!confirmed) return;
    try {
      await xoaDiaChi(diaChiId);
      await taiDsModalDiaChi(khModalDiaChi.value.id);
      capNhatDiaChiMacDinhTrongBang(khModalDiaChi.value.id);
      hienThiThongBao("success", "Đã xóa địa chỉ");
    } catch {
      loiDiaChi.value = "Không thể xóa địa chỉ.";
    }
  }

  async function datMacDinhModal(diaChiId) {
    try {
      await datMacDinhDiaChi(diaChiId);
      await taiDsModalDiaChi(khModalDiaChi.value.id);
      capNhatDiaChiMacDinhTrongBang(khModalDiaChi.value.id);
      hienThiThongBao("success", "Đã cập nhật địa chỉ mặc định");
    } catch {
      loiDiaChi.value = "Không thể đặt địa chỉ mặc định.";
    }
  }

  function capNhatDiaChiMacDinhTrongBang(khId) {
    const macDinh = dsDiaChiModal.value.find((dc) => dc.laMacDinh);
    const kh = (danhSach.value || []).find((k) => k.id === khId);
    if (kh) {
      kh.diaChiMacDinh = macDinh
        ? `${macDinh.diaChiCuThe}, ${macDinh.phuongXa}, ${macDinh.quanHuyen}, ${macDinh.tinhThanh}`
        : null;
      kh.sdtMacDinh = macDinh ? macDinh.sdt : null;
    }
  }

  // ========================
  // ĐƠN HÀNG — CHUYỂN TRANG
  // ========================
  function moModalDonHang(kh) {
    router.push({ name: "admin-khach-hang-don-hang", params: { id: kh.id } });
  }

  onMounted(() => {
    taiDanhSach();
    taiThongBaoDieuHuong();
  });

  onActivated(() => {
    taiThongBaoDieuHuong();
  });

  return {
    computed,
    onActivated,
    onMounted,
    ref,
    watch,
    useRouter,
    CheckCircle2,
    Eye,
    FileSpreadsheet,
    Filter,
    Home,
    MapPin,
    Package,
    Plus,
    RotateCcw,
    Search,
    ShoppingBag,
    Trash2,
    Users,
    X,
    doiTrangThaiKhachHang,
    layDanhSachKhachHang,
    layDanhSachDiaChi,
    themDiaChi,
    capNhatDiaChi,
    xoaDiaChi,
    datMacDinhDiaChi,
    layHoaDonTheoKhachHang,
    AdminTableFooter,
    AdminQuickStatusAction,
    exportRowsToExcel,
    getDisplayErrorMessage,
    Card,
    Button,
    Input,
    Badge,
    Table,
    router,
    CUSTOMER_CREATE_TOAST_KEY,
    danhSach,
    dangTai,
    loiTrang,
    boLoc,
    dsTrangThai,
    hienThiThongBao,
    taiThongBaoDieuHuong,
    mauTrangThai,
    dinhDangNgay,
    dinhDangTien,
    mauTrangThaiDon,
    badgeTrangThaiDon,
    soPhanTuMotTrang,
    trangHienTai,
    pageSizeOptions,
    tongSoTrang,
    danhSachPhanTrang,
    taiDanhSach,
    lamMoiBoLoc,
    xemChiTiet,
    dangDoiTrangThai,
    toggleTrangThai,
    themMoi,
    xuatExcel,
    timer,
    khModalDiaChi,
    dsDiaChiModal,
    dangTaiDiaChi,
    loiDiaChi,
    hienFormDiaChi,
    diaChiDangSua,
    dangLuuDiaChi,
    formDiaChi,
    dsTinh,
    dsHuyen,
    dsXa,
    maTinhChon,
    maHuyenChon,
    dangTaiDiaPhuong,
    taiDsTinh,
    onTinhChange,
    onHuyenChange,
    onXaChange,
    preFillCascadeForEdit,
    moModalDiaChi,
    taiDsModalDiaChi,
    dongModalDiaChi,
    moThemDiaChiModal,
    moSuaDiaChiModal,
    luuDiaChiModal,
    xoaDiaChiModal,
    datMacDinhModal,
    capNhatDiaChiMacDinhTrongBang,
    moModalDonHang,
  };
}
