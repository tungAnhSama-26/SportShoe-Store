import { computed, onBeforeUnmount, onMounted, ref, watch, markRaw } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  Banknote,
  CheckCircle2,
  CircleCheck,
  CircleX,
  ClipboardList,
  ClipboardCheck,
  Flag,
  History,
  Hourglass,
  MapPin,
  Package,
  Pencil,
  Printer,
  Search,
  Trash2,
  TriangleAlert,
  Truck,
  User,
  X,
} from "lucide-vue-next";
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";
import {
  capNhatSanPhamHoaDon,
  capNhatThongTinGiaoHang,
  capNhatTrangThaiHoaDon,
  layChiTietHoaDon,
  tinhPhiVanChuyenGhn,
  xacNhanHoanTien,
  xacNhanThanhToanCod,
} from "../../../services/hoa-don";
import { layDanhSachDiaChi } from "../../../services/khach-hang";
import { timSanPhamTaiQuay } from "../../../services/ban-hang-tai-quay";
import { printInvoiceToPdf } from "../../../utils/invoice-pdf";
import { getDisplayErrorMessage } from "../../../utils/error-message";
import { layDanhSachTaiKhoanNganHang } from "../../../services/client-profile";
import { showSuccess, showError, showConfirm } from "../../../utils/alert";
import logoGhn from "../../../constants/logoGhn";
import { ketNoiHoaDonRealtime } from "../../../services/hoa-don-realtime";

export function useChiTietHoaDon() {
  const route = useRoute();
  const router = useRouter();
  const hoaDon = ref(null);
  const dangTai = ref(false);
  const loiTrang = ref("");
  const dangCapNhat = ref(false);

  const hienModalXacNhan = ref(false);
  const hienModalLichSu = ref(false);
  const hienModalSanPham = ref(false);
  const hienModalXacNhanHuy = ref(false);
  const hienModalThanhToanCod = ref(false);
  const dangXacNhanThanhToanCod = ref(false);
  const formThanhToanCod = ref({
    hinhThucThanhToan: 2,
    tienKhachDua: "",
    ghiChu: "",
  });
  const hienModalHoanTien = ref(false);
  const dangXacNhanHoanTien = ref(false);
  const formHoanTien = ref({
    hinhThucHoanTien: 2,
    soTienHoan: "",
    maGiaoDichHoan: "",
    ghiChu: "",
  });

  const hienModalThongTin = ref(false);
  const hienModalGiaoHang = ref(false);
  const dangLuuGiaoHang = ref(false);
  const diaChiDaLuu = ref([]);
  const tabHienTai = ref("donHang");
  const formThongTin = ref({
    trangThai: "",
    tenKhachHang: "",
    soDienThoai: "",
    email: "",
    diaChi: "",
    loaiDon: "",
    ghiChu: "",
  });
  const formGhn = ref({
    serviceTypeId: 2,
    length: 30,
    width: 20,
    height: 12,
    weight: 500,
  });
  const dangTinhPhiGhn = ref(false);
  const diaChiGhnDaDo = ref("");
  let lanCapNhatCucBoGanNhat = 0;

  const trangThaiMoiXacNhan = ref(null);
  const ghiChuXacNhan = ref("");

  const tuKhoaSanPham = ref("");
  const ketQuaTimKiem = ref([]);
  const dangTimKiem = ref(false);
  const giaTuSanPham = ref("");
  const giaDenSanPham = ref("");
  const tuKhoaLocSanPham = ref("");
  const loaiSanPhamDangLoc = ref("");
  const sapXepSanPham = ref("macDinh");
  const trangSanPhamHienTai = ref(1);
  const soSanPhamMoiTrang = 5;
  const danhSachSanPhamUpdate = ref([]);

  const cacBuocCoDinh = [
    {
      id: 1,
      key: "Chờ xác nhận",
      ten: "Chờ Xác Nhận",
      icon: markRaw(Hourglass),
    },
    {
      id: 2,
      key: "Đã xác nhận",
      ten: "Đã Xác Nhận",
      icon: markRaw(ClipboardCheck),
    },
    { id: 3, key: "Chờ lấy hàng", ten: "Chờ Lấy Hàng", icon: markRaw(Package) },
    {
      id: 4,
      key: "Đang giao hàng",
      ten: "Đang Giao Hàng",
      icon: markRaw(Truck),
    },
    {
      id: 5,
      key: "Đã giao hàng",
      ten: "Đã Giao Hàng",
      icon: markRaw(CircleCheck),
    },
    { id: 6, key: "Hoàn thành", ten: "Hoàn Thành", icon: markRaw(Flag) },
  ];

  const cacBuocGiaoThatBai = [
    {
      id: 1,
      key: "Chờ xác nhận",
      ten: "Chờ Xác Nhận",
      icon: markRaw(Hourglass),
    },
    {
      id: 2,
      key: "Đã xác nhận",
      ten: "Đã Xác Nhận",
      icon: markRaw(ClipboardCheck),
    },
    { id: 3, key: "Chờ lấy hàng", ten: "Chờ Lấy Hàng", icon: markRaw(Package) },
    {
      id: 4,
      key: "Đang giao hàng",
      ten: "Đang Giao Hàng",
      icon: markRaw(Truck),
    },
    {
      id: 8,
      key: "Giao hàng thất bại",
      ten: "Giao Hàng Thất Bại",
      icon: markRaw(TriangleAlert),
    },
  ];

  const cacBuocYeuCauHuy = [
    {
      id: 1,
      key: "Chờ xác nhận",
      ten: "Chờ Xác Nhận",
      icon: markRaw(Hourglass),
    },
    {
      id: 7,
      key: "Yêu cầu hủy",
      ten: "Yêu Cầu Hủy",
      icon: markRaw(TriangleAlert),
    },
  ];

  const cacBuocDaHuy = [
    {
      id: 1,
      key: "Chờ xác nhận",
      ten: "Chờ Xác Nhận",
      icon: markRaw(Hourglass),
    },
    { id: 6, key: "Hủy", ten: "Đã Hủy", icon: markRaw(CircleX) },
  ];

  const donGiaoThatBai = computed(() => {
    const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
    return stt === "giao hàng thất bại" || stt === "giao_hang_that_bai";
  });

  const laDonTaiQuay = computed(() => {
    const diaChi = (hoaDon.value?.diaChi || "").trim().toLowerCase();
    const coDiaChiGiao =
      diaChi &&
      diaChi !== "mua tại quầy" &&
      diaChi !== "không có" &&
      diaChi !== "—";
    if (coDiaChiGiao) {
      return false;
    }
    return (
      hoaDon.value?.loaiDon === "Cửa hàng" ||
      hoaDon.value?.loaiDon === "Offline" ||
      hoaDon.value?.loaiDon === "Tại cửa hàng" ||
      hoaDon.value?.kenhBan === 1
    );
  });

  const cacBuoc = computed(() => {
    if (donGiaoThatBai.value) {
      return cacBuocGiaoThatBai;
    }
    if (laDonTaiQuay.value) {
      return [
        {
          id: 1,
          key: "Chờ xác nhận",
          ten: "Chờ Xác Nhận",
          icon: markRaw(Hourglass),
        },
        { id: 6, key: "Hoàn thành", ten: "Hoàn Thành", icon: markRaw(Flag) },
      ];
    }
    return cacBuocCoDinh;
  });

  function dinhDangTien(value) {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      maximumFractionDigits: 0,
    }).format(value || 0);
  }

  function laTextCoGiaTri(value) {
    const text = String(value ?? "")
      .trim()
      .toLowerCase();
    return (
      Boolean(text) &&
      text !== "-" &&
      text !== "không áp dụng" &&
      text !== "không có" &&
      text !== "chưa cập nhật"
    );
  }

  function dinhDangPhanTram(value) {
    return new Intl.NumberFormat("vi-VN", {
      maximumFractionDigits: 2,
    }).format(value || 0);
  }

  function dinhDangNgay(ngay) {
    if (!ngay) return "—";
    return new Intl.DateTimeFormat("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(new Date(ngay));
  }

  function dinhDangGio(ngay) {
    if (!ngay) return "--:--:--";
    return new Intl.DateTimeFormat("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    }).format(new Date(ngay));
  }

  function vietHoaChuCaiDau(text) {
    if (!text) return "";
    return text
      .toLowerCase()
      .split(" ")
      .filter(Boolean)
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(" ");
  }

  const buocHienTai = computed(() => {
    if (!hoaDon.value) return 0;
    const stt = (hoaDon.value.trangThai || "").toLowerCase().trim();
    if (stt === "chờ xác nhận" || stt === "cho_xac_nhan") return 1;
    if (stt === "đã xác nhận" || stt === "da_xac_nhan") return 2;
    if (
      stt === "chờ lấy hàng" ||
      stt === "cho_lay_hang" ||
      stt === "cho_giao_hang"
    )
      return 3;
    if (
      stt === "đang giao hàng" ||
      stt === "chờ giao hàng" ||
      stt === "đang vận chuyển" ||
      stt === "chờ vận chuyển" ||
      stt === "dang_van_chuyen"
    )
      return 4;
    if (
      stt === "đã giao hàng" ||
      stt === "vận chuyển" ||
      stt === "da_giao_hang"
    )
      return 5;
    if (stt === "giao hàng thất bại" || stt === "giao_hang_that_bai") return 8;
    if (stt === "hoàn thành" || stt === "đã hoàn thành" || stt === "hoan_thanh")
      return 6;
    return 0;
  });

  const chuanHoaTrangThaiText = (value) =>
    String(value ?? "")
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .trim();

  const laTrangThaiCanHoanTien = (value) => {
    const stt = chuanHoaTrangThaiText(value);
    return stt === "can hoan tien" || stt === "can_hoan_tien";
  };

  const laGiaTriHeThong = (value) => {
    const text = chuanHoaTrangThaiText(value);
    return !text || text === "he thong" || text === "chua cap nhat";
  };

  const laLogHoanTien = (log) => {
    const trangThai = chuanHoaTrangThaiText(log?.trangThai);
    const ghiChu = chuanHoaTrangThaiText(log?.ghiChu);
    return trangThai.includes("xac nhan hoan tien") || ghiChu.includes("hoan tien");
  };

  const laLogKhachHuyDon = (log) => {
    const ghiChu = chuanHoaTrangThaiText(log?.ghiChu);
    return ghiChu.includes("khach hang huy don") || ghiChu.includes("khach huy don");
  };

  const donDaHoanThanh = computed(() => {
    const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
    return (
      stt === "hoàn thành" || stt === "đã hoàn thành" || stt === "hoan_thanh"
    );
  });

  const donDaXacNhan = computed(() => {
    const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
    return (
      stt === "đã xác nhận" || stt === "da_xac_nhan"
    );
  });

  const donYeuCauHuy = computed(() => {
    const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
    return stt === "yêu cầu hủy" || stt === "yeu_cau_huy";
  });

  const donDaHuy = computed(() => {
    const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
    return (
      stt === "hủy" || stt === "huy" || stt === "đã hủy" || stt === "da_huy"
    );
  });

  const donDangChoHoanTien = computed(() => {
    const coThanhToanCanHoan = (hoaDon.value?.lichSuThanhToan ?? []).some(
      (item) => laTrangThaiCanHoanTien(item.trangThaiThanhToan),
    );
    return laTrangThaiCanHoanTien(hoaDon.value?.trangThai) || coThanhToanCanHoan;
  });

  const donDaKetThuc = computed(
    () => donDaHoanThanh.value || donDaHuy.value || donDangChoHoanTien.value,
  );
  const coTheSuaThongTinGiaoHang = computed(() =>
    [1, 2, 3].includes(buocHienTai.value),
  );

  function hienThiThongBao(loai, tieuDe, noiDung = "") {
    if (loai === "success") {
      showSuccess(noiDung || tieuDe, tieuDe);
    } else {
      showError(noiDung || tieuDe, tieuDe);
    }
  }

  function ganHoaDonSauThaoTac(data) {
    hoaDon.value = data;
    lanCapNhatCucBoGanNhat = Date.now();
  }

  function thongBaoDonDaHoanThanh() {
    hienThiThongBao(
      "warning",
      "Đơn Hàng Đã Kết Thúc",
      "Đơn hàng đã hoàn thành hoặc bị hủy, không thể thực hiện thao tác này.",
    );
  }

  function moModalThongTin() {
    if (donDaKetThuc.value) {
      thongBaoDonDaHoanThanh();
      return;
    }
    hienModalThongTin.value = true;
  }

  async function moModalSuaDiaChi() {
    if (!coTheSuaThongTinGiaoHang.value) {
      showError(
        "Chỉ có thể sửa thông tin giao hàng trước khi đơn bắt đầu giao.",
      );
      return;
    }

    diaChiDaLuu.value = [];
    if (hoaDon.value?.khachHangId) {
      try {
        const data = await layDanhSachDiaChi(hoaDon.value.khachHangId);
        diaChiDaLuu.value = Array.isArray(data) ? data : [];
      } catch {
        diaChiDaLuu.value = [];
      }
    }
    hienModalGiaoHang.value = true;
  }

  async function handleLuuGiaoHang(payload) {
    if (!hoaDon.value || dangLuuGiaoHang.value) return;
    dangLuuGiaoHang.value = true;
    try {
      ganHoaDonSauThaoTac(
        await capNhatThongTinGiaoHang(hoaDon.value.id, payload),
      );
      formThongTin.value.tenKhachHang = hoaDon.value.tenKhachHang || "";
      formThongTin.value.soDienThoai = hoaDon.value.soDienThoai || "";
      formThongTin.value.diaChi = hoaDon.value.diaChi || "";
      hienModalGiaoHang.value = false;
      showSuccess("Thông tin nhận hàng đã được cập nhật.");
    } catch (error) {
      showError(
        getDisplayErrorMessage(error, "Không thể cập nhật thông tin nhận hàng"),
      );
    } finally {
      dangLuuGiaoHang.value = false;
    }
  }

  const tongTienHang = computed(
    () =>
      hoaDon.value?.sanPham?.reduce((tong, sp) => tong + sp.thanhTien, 0) ?? 0,
  );
  const tongKhachCanTra = computed(() =>
    hoaDon.value
      ? tongTienHang.value +
        (hoaDon.value.phiVanChuyen || 0) -
        (hoaDon.value.giamGia || 0)
      : 0,
  );
  const coPhieuGiamGia = computed(
    () =>
      laTextCoGiaTri(hoaDon.value?.voucher) &&
      Number(hoaDon.value?.giamGia || 0) > 0,
  );
  const moTaGiaTriPhieuGiamGia = computed(() => {
    if (!coPhieuGiamGia.value) return "";

    const loai = Number(hoaDon.value?.loaiGiamGia);
    const giaTriGoc = Number(hoaDon.value?.giaTriGiamGia || 0);
    const soTienDaGiam = Number(hoaDon.value?.giamGia || 0);

    if (loai === 1 && giaTriGoc > 0) {
      return `${dinhDangPhanTram(giaTriGoc)}% (- ${dinhDangTien(soTienDaGiam)})`;
    }

    return `- ${dinhDangTien(soTienDaGiam || giaTriGoc)}`;
  });
  const thanhToanGanNhat = computed(
    () => hoaDon.value?.lichSuThanhToan?.[0] ?? null,
  );
  const lichSuRutGon = computed(() => hoaDon.value?.lichSuHoaDon ?? []);
  const thanhToanCodDangCho = computed(
    () =>
      (hoaDon.value?.lichSuThanhToan ?? []).find(
        (item) =>
          item.phuongThucThanhToan === "COD" &&
          item.trangThaiThanhToan === "Chờ thanh toán",
      ) ?? null,
  );
  const coTheThanhToanCod = computed(() => {
    const trangThai = (hoaDon.value?.trangThai || "").toLowerCase().trim();
    return trangThai === "đã giao hàng" && Boolean(thanhToanCodDangCho.value);
  });
  const thanhToanCanHoanTien = computed(
    () =>
      (hoaDon.value?.lichSuThanhToan ?? []).find(
        (item) => laTrangThaiCanHoanTien(item.trangThaiThanhToan),
      ) ?? null,
  );
  const coTheHoanTien = computed(
    () => Boolean(thanhToanCanHoanTien.value) || donDangChoHoanTien.value,
  );
  const tongTienHoan = computed(
    () =>
      thanhToanCanHoanTien.value?.tongTien ??
      hoaDon.value?.tongTien ??
      hoaDon.value?.tongThanhToan ??
      0,
  );
  const tongTienThanhToanCod = computed(
    () =>
      thanhToanCodDangCho.value?.tongTien ??
      hoaDon.value?.tongTien ??
      tongKhachCanTra.value,
  );
  const noiDungChuyenKhoanCod = computed(() =>
    `COD ${hoaDon.value?.maHoaDon || ""}`.trim(),
  );
  const qrThanhToanCodUrl = computed(() => {
    const amount = Math.max(Number(tongTienThanhToanCod.value) || 0, 0);
    const description = encodeURIComponent(noiDungChuyenKhoanCod.value);
    return `https://img.vietqr.io/image/VCB-0965852782-compact2.png?amount=${amount}&addInfo=${description}&accountName=SPORTSHOE%20STORE`;
  });
  const tienThieuThanhToanCod = computed(() => {
    if (formThanhToanCod.value.hinhThucThanhToan !== 1) {
      return 0;
    }
    const tienKhachDua = Number(formThanhToanCod.value.tienKhachDua) || 0;
    return Math.max(tongTienThanhToanCod.value - tienKhachDua, 0);
  });
  const danhSachSanPhamHoaDon = computed(() => hoaDon.value?.sanPham ?? []);
  const chuanHoaChuoiLocSanPham = (value) =>
    String(value ?? "")
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .trim();
  const danhSachLoaiSanPham = computed(() =>
    Array.from(
      new Set(
        danhSachSanPhamHoaDon.value
          .map((item) => String(item.phanLoai || "").trim())
          .filter(Boolean),
      ),
    ).sort((a, b) => a.localeCompare(b, "vi")),
  );
  const giaLonNhatSanPham = computed(() =>
    danhSachSanPhamHoaDon.value.reduce(
      (max, item) => Math.max(max, Number(item.donGia) || 0),
      0,
    ),
  );
  const giaTuSanPhamSo = computed({
    get: () =>
      Math.min(Number(giaTuSanPham.value) || 0, giaLonNhatSanPham.value),
    set: (value) => {
      const giaMax = giaLonNhatSanPham.value;
      const giaDen =
        giaDenSanPham.value === "" ? giaMax : Number(giaDenSanPham.value) || 0;
      giaTuSanPham.value = String(
        Math.max(0, Math.min(Number(value) || 0, giaDen || giaMax)),
      );
    },
  });
  const giaDenSanPhamSo = computed({
    get: () => {
      const giaMax = giaLonNhatSanPham.value;
      if (giaDenSanPham.value === "") return giaMax;
      return Math.max(
        giaTuSanPhamSo.value,
        Math.min(Number(giaDenSanPham.value) || 0, giaMax),
      );
    },
    set: (value) => {
      const giaMax = giaLonNhatSanPham.value;
      giaDenSanPham.value = String(
        Math.max(giaTuSanPhamSo.value, Math.min(Number(value) || 0, giaMax)),
      );
    },
  });
  const dinhDangTienKhongDonVi = (value) =>
    new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 0 }).format(
      value || 0,
    );
  const nhanKhoangGiaSanPham = computed(
    () =>
      `${dinhDangTienKhongDonVi(giaTuSanPhamSo.value)} - ${dinhDangTienKhongDonVi(giaDenSanPhamSo.value)} (Max: ${dinhDangTienKhongDonVi(giaLonNhatSanPham.value)})`,
  );
  const styleKhoangGiaSanPham = computed(() => {
    const giaMax = giaLonNhatSanPham.value || 1;
    const tu = (giaTuSanPhamSo.value / giaMax) * 100;
    const den = (giaDenSanPhamSo.value / giaMax) * 100;
    return {
      background: `linear-gradient(to right, #e5e7eb 0%, #e5e7eb ${tu}%, #B82220 ${tu}%, #B82220 ${den}%, #e5e7eb ${den}%, #e5e7eb 100%)`,
    };
  });
  const danhSachSanPhamDaLoc = computed(() => {
    const giaTu = Number(giaTuSanPham.value) || 0;
    const giaDen = Number(giaDenSanPham.value) || 0;
    const tuKhoa = chuanHoaChuoiLocSanPham(tuKhoaLocSanPham.value);
    const loaiSanPham = loaiSanPhamDangLoc.value;

    const danhSachDaLoc = danhSachSanPhamHoaDon.value.filter((item) => {
      const donGia = Number(item.donGia) || 0;
      const dungKhoangGia =
        (!giaTu || donGia >= giaTu) && (!giaDen || donGia <= giaDen);
      const dungLoaiSanPham = !loaiSanPham || item.phanLoai === loaiSanPham;
      const noiDungTimKiem = chuanHoaChuoiLocSanPham(
        [item.tenSanPham, item.phanLoai, item.mauSac, item.kichCo]
          .filter(Boolean)
          .join(" "),
      );
      const dungTuKhoa = !tuKhoa || noiDungTimKiem.includes(tuKhoa);
      return dungKhoangGia && dungLoaiSanPham && dungTuKhoa;
    });

    return [...danhSachDaLoc].sort((a, b) => {
      if (sapXepSanPham.value === "giaTang")
        return (Number(a.donGia) || 0) - (Number(b.donGia) || 0);
      if (sapXepSanPham.value === "giaGiam")
        return (Number(b.donGia) || 0) - (Number(a.donGia) || 0);
      if (sapXepSanPham.value === "soLuongGiam")
        return (Number(b.soLuong) || 0) - (Number(a.soLuong) || 0);
      if (sapXepSanPham.value === "tongTienGiam")
        return (Number(b.thanhTien) || 0) - (Number(a.thanhTien) || 0);
      return 0;
    });
  });
  const tongTrangSanPham = computed(
    () => Math.ceil(danhSachSanPhamDaLoc.value.length / soSanPhamMoiTrang) || 1,
  );
  const hienPhanTrangSanPham = computed(
    () => danhSachSanPhamDaLoc.value.length > soSanPhamMoiTrang,
  );
  const danhSachSanPhamPhanTrang = computed(() => {
    const start = (trangSanPhamHienTai.value - 1) * soSanPhamMoiTrang;
    return danhSachSanPhamDaLoc.value.slice(start, start + soSanPhamMoiTrang);
  });
  const thongTinBuoc = computed(() => {
    const lichSu = hoaDon.value?.lichSuHoaDon ?? [];
    const nguonBuoc = donDaHuy.value
      ? cacBuocDaHuy
      : donYeuCauHuy.value
        ? cacBuocYeuCauHuy
        : cacBuoc.value;
    return nguonBuoc.map((buoc) => {
      const logHoanTien =
        buoc.id === 6 ? lichSu.find((item) => laLogHoanTien(item)) : null;
      const banGhi =
        logHoanTien ??
        lichSu.find(
          (item) =>
            (item.trangThai || "").toLowerCase() === buoc.key.toLowerCase() ||
            (item.trangThai || "").toLowerCase() === buoc.ten.toLowerCase(),
        ) ?? null;
      let time = banGhi?.ngayTao;
      let staff = banGhi?.maNhanVien;
      if (buoc.id === 6 && laGiaTriHeThong(staff) && laLogKhachHuyDon(banGhi)) {
        staff = "Khách hàng";
      }
      if (!banGhi && buoc.id === 1) {
        time = hoaDon.value?.ngayTao;
        staff = hoaDon.value?.maNhanVien ?? "Hệ thống";
      }

      return {
        ...buoc,
        thoiGian: time,
        nhanVien: staff,
      };
    });
  });

  const cacBuocHienThi = computed(() => {
    if (donDaHuy.value) {
      return thongTinBuoc.value;
    }
    if (donYeuCauHuy.value) {
      return thongTinBuoc.value;
    }
    if (laDonTaiQuay.value) {
      return thongTinBuoc.value;
    }
    return thongTinBuoc.value.filter((b) => b.id <= buocHienTai.value + 1);
  });

  function lopVongTrangThai(buoc) {
    if (donDaHuy.value) {
      if (buoc.id === 1) {
        return "border-[#B82220] bg-[#B82220] text-white shadow-[0_10px_22px_rgba(184,34,32,0.18)]";
      }
      return "border-rose-500 bg-rose-500 text-white shadow-[0_10px_22px_rgba(244,63,94,0.2)]";
    }

    if (donYeuCauHuy.value) {
      if (buoc.id === 1) {
        return "border-[#B82220] bg-[#B82220] text-white shadow-[0_10px_22px_rgba(184,34,32,0.18)]";
      }
      return "border-amber-400 bg-white text-slate-500 shadow-[0_10px_20px_rgba(245,158,11,0.12)]";
    }

    return buoc.id <= buocHienTai.value
      ? "border-[#B82220] bg-[#B82220] text-white shadow-[0_10px_22px_rgba(184,34,32,0.18)]"
      : "border-[#B82220] bg-white text-[#B82220]";
  }

  function lopTenTrangThai(buoc) {
    if (donDaHuy.value && buoc.id === 6) {
      return "text-rose-500";
    }
    if (donYeuCauHuy.value && buoc.id === 7) {
      return "text-slate-500";
    }
    return "text-[#B82220]";
  }

  async function taiChiTiet(amThang = false) {
    if (!amThang) dangTai.value = true;
    loiTrang.value = "";
    try {
      hoaDon.value = await layChiTietHoaDon(Number(route.params.id));
    } catch (error) {
      if (!amThang) {
        loiTrang.value = getDisplayErrorMessage(
          error,
          "Không thể tải chi tiết đơn hàng",
        );
      }
    } finally {
      if (!amThang) dangTai.value = false;
    }
  }

  function openModalXacNhan(trangThai) {
    if (donDaHoanThanh.value) {
      thongBaoDonDaHoanThanh();
      return;
    }
    trangThaiMoiXacNhan.value = trangThai;
    ghiChuXacNhan.value = "";
    hienModalXacNhan.value = true;
  }

  async function handleXacNhanTrangThai() {
    if (!hoaDon.value || !trangThaiMoiXacNhan.value || dangCapNhat.value)
      return;
    if (donDaHoanThanh.value) {
      thongBaoDonDaHoanThanh();
      return;
    }
    dangCapNhat.value = true;
    try {
      ganHoaDonSauThaoTac(
        await capNhatTrangThaiHoaDon(hoaDon.value.id, {
          trangThai: trangThaiMoiXacNhan.value,
          ghiChu: ghiChuXacNhan.value,
        }),
      );
      hienThiThongBao(
        "success",
        "Cập Nhật Thành Công",
        `Đơn hàng đã chuyển sang ${trangThaiMoiXacNhan.value}.`,
      );
      hienModalXacNhan.value = false;
    } catch (error) {
      hienThiThongBao(
        "error",
        "Lỗi Cập Nhật Trạng Thái",
        getDisplayErrorMessage(
          error,
          "Không thể cập nhật trạng thái đơn hàng.",
        ),
      );
    } finally {
      dangCapNhat.value = false;
    }
  }

  async function handleXuLyYeuCauHuy(trangThai) {
    if (!hoaDon.value || dangCapNhat.value) return;
    dangCapNhat.value = true;
    try {
      ganHoaDonSauThaoTac(
        await capNhatTrangThaiHoaDon(hoaDon.value.id, {
          trangThai,
          ghiChu:
            trangThai === "Hủy"
              ? "Xác nhận yêu cầu hủy của khách hàng"
              : "Từ chối yêu cầu hủy của khách hàng",
        }),
      );
      hienThiThongBao(
        "success",
        trangThai === "Hủy" ? "Đã Xác Nhận Hủy" : "Đã Từ Chối Hủy",
        trangThai === "Hủy"
          ? "Đơn hàng đã được chuyển sang trạng thái hủy."
          : "Đơn hàng đã quay lại trạng thái trước khi khách yêu cầu hủy.",
      );
    } catch (error) {
      hienThiThongBao(
        "error",
        "Lỗi Xử Lý Yêu Cầu Hủy",
        getDisplayErrorMessage(error, "Không thể xử lý yêu cầu hủy đơn hàng."),
      );
    } finally {
      dangCapNhat.value = false;
    }
  }

  function moModalXacNhanHuy() {
    if (dangCapNhat.value) return;
    hienModalXacNhanHuy.value = true;
  }

  async function handleXacNhanHuyDon() {
    await handleXuLyYeuCauHuy("Hủy");
    if (!dangCapNhat.value) {
      hienModalXacNhanHuy.value = false;
    }
  }

  watch(hienModalSanPham, (val) => {
    if (!val || !hoaDon.value) return;
    if (donDaHoanThanh.value) {
      hienModalSanPham.value = false;
      thongBaoDonDaHoanThanh();
      return;
    }
    danhSachSanPhamUpdate.value = (hoaDon.value.sanPham || []).map((sp) => ({
      chiTietId: sp.giayChiTietId,
      tenSanPham: sp.tenSanPham,
      soLuong: sp.soLuong,
      giaBan: sp.donGia,
      maBienThe: sp.phanLoai || "",
    }));

  });

  async function timKiemSanPham() {
    if (!tuKhoaSanPham.value.trim()) {
      ketQuaTimKiem.value = [];
      return;
    }
    dangTimKiem.value = true;
    try {
      ketQuaTimKiem.value = await timSanPhamTaiQuay(tuKhoaSanPham.value);
    } finally {
      dangTimKiem.value = false;
    }
  }

  function themSanPham(sp) {
    const existing = danhSachSanPhamUpdate.value.find(
      (i) => i.chiTietId === sp.chiTietId,
    );
    if (existing) existing.soLuong++;
    else {
      danhSachSanPhamUpdate.value.push({
        chiTietId: sp.chiTietId,
        tenSanPham: sp.tenSanPham,
        soLuong: 1,
        giaBan: sp.giaBan,
        maBienThe: `${sp.mauSac} - ${sp.kichCo}`,
      });
    }
  }

  function removeSanPham(id) {
    danhSachSanPhamUpdate.value = danhSachSanPhamUpdate.value.filter(
      (i) => i.chiTietId !== id,
    );
  }

  async function handleSaveSanPham() {
    if (!hoaDon.value || dangCapNhat.value) return;
    if (donDaHoanThanh.value) {
      thongBaoDonDaHoanThanh();
      return;
    }
    dangCapNhat.value = true;
    try {
      ganHoaDonSauThaoTac(
        await capNhatSanPhamHoaDon(hoaDon.value.id, {
          items: danhSachSanPhamUpdate.value.map((i) => ({
            chiTietId: i.chiTietId,
            soLuong: i.soLuong,
          })),
        }),
      );
      hienThiThongBao(
        "success",
        "Cập Nhật Thành Công",
        "Sản phẩm hóa đơn đã được cập nhật.",
      );
      hienModalSanPham.value = false;
    } catch (error) {
      showError(
        getDisplayErrorMessage(
          error,
          "Không thể cập nhật sản phẩm trong hóa đơn",
        ),
      );
    } finally {
      dangCapNhat.value = false;
    }
  }

  const danhSachTrangThaiHienThi = [
    { key: "Chờ xác nhận", label: "Chờ xác nhận" },
    { key: "Đã xác nhận", label: "Đã xác nhận" },
    { key: "Chờ lấy hàng", label: "Chờ lấy hàng" },
    { key: "Đang giao hàng", label: "Đang giao hàng" },
    { key: "Đã giao hàng", label: "Đã giao hàng" },
    { key: "Giao hàng thất bại", label: "Giao hàng thất bại" },
    { key: "Hoàn thành", label: "Hoàn thành" },
    { key: "Hủy", label: "Hủy" },
    { key: "Yêu cầu hủy", label: "Yêu cầu hủy" },
  ];

  const indexTrangThaiHienTai = computed(() => {
    if (!hoaDon.value) return -1;
    const currentStt = (hoaDon.value.trangThai || "").toLowerCase().trim();
    let normalizedStt = hoaDon.value.trangThai;
    if (currentStt === "chờ xác nhận" || currentStt === "cho_xac_nhan")
      normalizedStt = "Chờ xác nhận";
    else if (currentStt === "đã xác nhận" || currentStt === "da_xac_nhan")
      normalizedStt = "Đã xác nhận";
    else if (
      currentStt === "chờ lấy hàng" ||
      currentStt === "cho_lay_hang" ||
      currentStt === "cho_giao_hang"
    )
      normalizedStt = "Chờ lấy hàng";
    else if (
      currentStt === "đang giao hàng" ||
      currentStt === "chờ giao hàng" ||
      currentStt === "đang vận chuyển" ||
      currentStt === "chờ vận chuyển" ||
      currentStt === "dang_van_chuyen"
    )
      normalizedStt = "Đang giao hàng";
    else if (
      currentStt === "đã giao hàng" ||
      currentStt === "vận chuyển" ||
      currentStt === "da_giao_hang"
    )
      normalizedStt = "Đã giao hàng";
    else if (
      currentStt === "giao hàng thất bại" ||
      currentStt === "giao_hang_that_bai"
    )
      normalizedStt = "Giao hàng thất bại";
    else if (
      currentStt === "hoàn thành" ||
      currentStt === "đã hoàn thành" ||
      currentStt === "hoan_thanh"
    )
      normalizedStt = "Hoàn thành";
    else if (currentStt === "yêu cầu hủy" || currentStt === "yeu_cau_huy")
      normalizedStt = "Yêu cầu hủy";
    else if (currentStt === "cần hoàn tiền" || currentStt === "can_hoan_tien")
      normalizedStt = "Cần hoàn tiền";
    else if (currentStt === "hủy" || currentStt === "huy")
      normalizedStt = "Hủy";

    return danhSachTrangThaiHienThi.findIndex((x) => x.key === normalizedStt);
  });

  function isOptionDisabled(stKey) {
    if (!hoaDon.value) return false;
    const targetIndex = danhSachTrangThaiHienThi.findIndex(
      (x) => x.key === stKey,
    );
    if (indexTrangThaiHienTai.value < 0 || targetIndex < 0) return false;
    // Disable if it's the current one (no change) or already passed
    // Actually, we want them to be able to keep current or pick next.
    // User says "update forward", so picking current is just "staying".
    return targetIndex < indexTrangThaiHienTai.value;
  }

  function hienThiOptionTrangThai(index, key) {
    if (donDaHoanThanh.value) {
      return key === "Hoàn thành";
    }
    if (donGiaoThatBai.value) {
      return key === "Giao hàng thất bại" || key === "Hủy";
    }
    const stt = (hoaDon.value?.trangThai || "").toLowerCase().trim();
    if (
      (stt === "đang giao hàng" || stt === "chờ giao hàng") &&
      key === "Giao hàng thất bại"
    ) {
      return true;
    }
    if (stt === "đã giao hàng" && key === "Hoàn thành") {
      return true;
    }
    return laDonTaiQuay.value
      ? index === 0 || key === "Hoàn thành"
      : key !== "Giao hàng thất bại" &&
          (index === indexTrangThaiHienTai.value ||
            index === indexTrangThaiHienTai.value + 1);
  }

  watch(hienModalThongTin, (val) => {
    if (!val || !hoaDon.value) return;
    if (donDaHoanThanh.value) {
      hienModalThongTin.value = false;
      thongBaoDonDaHoanThanh();
      return;
    }
    const stt = (hoaDon.value.trangThai || "").toLowerCase().trim();
    let defaultStt = hoaDon.value.trangThai;
    if (stt === "chờ xác nhận" || stt === "cho_xac_nhan")
      defaultStt = "Chờ xác nhận";
    else if (stt === "đã xác nhận" || stt === "da_xac_nhan")
      defaultStt = "Đã xác nhận";
    else if (
      stt === "chờ lấy hàng" ||
      stt === "cho_lay_hang" ||
      stt === "cho_giao_hang"
    )
      defaultStt = "Chờ lấy hàng";
    else if (
      stt === "đang giao hàng" ||
      stt === "chờ giao hàng" ||
      stt === "đang vận chuyển" ||
      stt === "chờ vận chuyển" ||
      stt === "dang_van_chuyen"
    )
      defaultStt = "Đang giao hàng";
    else if (
      stt === "đã giao hàng" ||
      stt === "vận chuyển" ||
      stt === "da_giao_hang"
    )
      defaultStt = "Đã giao hàng";
    else if (stt === "giao hàng thất bại" || stt === "giao_hang_that_bai")
      defaultStt = "Giao hàng thất bại";
    else if (
      stt === "hoàn thành" ||
      stt === "đã hoàn thành" ||
      stt === "hoan_thanh"
    )
      defaultStt = "Hoàn thành";
    else if (stt === "yêu cầu hủy" || stt === "yeu_cau_huy")
      defaultStt = "Yêu cầu hủy";
    else if (stt === "cần hoàn tiền" || stt === "can_hoan_tien")
      defaultStt = "Cần hoàn tiền";
    else if (stt === "hủy" || stt === "huy") defaultStt = "Hủy";

    formThongTin.value = {
      trangThai: defaultStt,
      tenKhachHang: hoaDon.value.tenKhachHang || "",
      soDienThoai: hoaDon.value.soDienThoai || "",
      email: hoaDon.value.email || "",
      diaChi: hoaDon.value.diaChi || "",
      loaiDon: hoaDon.value.loaiDon || "",
      ghiChu: "",
    };
    tabHienTai.value = "donHang";
  });

  watch(donDaHoanThanh, (locked) => {
    if (!locked) return;
    hienModalXacNhan.value = false;
    hienModalSanPham.value = false;
    hienModalThongTin.value = false;
  });

  watch(
    [
      giaTuSanPham,
      giaDenSanPham,
      tuKhoaLocSanPham,
      loaiSanPhamDangLoc,
      sapXepSanPham,
      () => hoaDon.value?.sanPham?.length,
    ],
    () => {
      trangSanPhamHienTai.value = 1;
    },
  );

  watch(tongTrangSanPham, (total) => {
    if (trangSanPhamHienTai.value > total) {
      trangSanPhamHienTai.value = total;
    }
  });

  async function handleLuuThongTin() {
    if (!hoaDon.value || dangCapNhat.value) return;
    if (donDaHoanThanh.value) {
      thongBaoDonDaHoanThanh();
      return;
    }
    dangCapNhat.value = true;
    try {
      const thongTinGiaoHangThayDoi =
        formThongTin.value.tenKhachHang.trim() !==
          String(hoaDon.value.tenKhachHang || "").trim() ||
        formThongTin.value.soDienThoai.trim() !==
          String(hoaDon.value.soDienThoai || "").trim() ||
        formThongTin.value.diaChi.trim() !==
          String(hoaDon.value.diaChi || "").trim();

      if (thongTinGiaoHangThayDoi) {
        if (!coTheSuaThongTinGiaoHang.value) {
          throw new Error(
            "Chỉ có thể sửa thông tin giao hàng trước khi đơn bắt đầu giao.",
          );
        }
        ganHoaDonSauThaoTac(
          await capNhatThongTinGiaoHang(hoaDon.value.id, {
            tenNguoiNhan: formThongTin.value.tenKhachHang.trim(),
            sdtNguoiNhan: formThongTin.value.soDienThoai.trim(),
            diaChiGiaoHang: formThongTin.value.diaChi.trim(),
          }),
        );
      }

      if (formThongTin.value.trangThai !== hoaDon.value.trangThai) {
        ganHoaDonSauThaoTac(
          await capNhatTrangThaiHoaDon(hoaDon.value.id, {
            trangThai: formThongTin.value.trangThai,
            ghiChu: formThongTin.value.ghiChu || "",
          }),
        );
      }
      hienThiThongBao(
        "success",
        "Cập Nhật Thành Công",
        "Thông tin hóa đơn đã được cập nhật.",
      );
      hienModalThongTin.value = false;
    } catch (error) {
      hienThiThongBao(
        "error",
        "Lỗi Cập Nhật Thông Tin",
        getDisplayErrorMessage(error, "Không thể cập nhật thông tin hóa đơn."),
      );
    } finally {
      dangCapNhat.value = false;
    }
  }

  async function handleTinhPhiGhn() {
    if (!hoaDon.value || dangTinhPhiGhn.value) return;
    if (donDaHoanThanh.value) {
      thongBaoDonDaHoanThanh();
      return;
    }
    if (!formThongTin.value.diaChi.trim()) {
      showError("Vui lòng nhập địa chỉ người nhận để tính phí GHN.");
      return;
    }

    dangTinhPhiGhn.value = true;
    try {
      const ketQua = await tinhPhiVanChuyenGhn(hoaDon.value.id, {
        toAddress: formThongTin.value.diaChi.trim(),
        serviceTypeId: Number(formGhn.value.serviceTypeId) || 2,
        length: Number(formGhn.value.length) || 30,
        width: Number(formGhn.value.width) || 20,
        height: Number(formGhn.value.height) || 12,
        weight: Number(formGhn.value.weight) || 500,
        insuranceValue: Math.min(Number(tongTienHang.value) || 0, 5000000),
      });
      diaChiGhnDaDo.value = [
        ketQua.matchedWardName,
        ketQua.matchedDistrictName,
        ketQua.matchedProvinceName,
      ]
        .filter(Boolean)
        .join(", ");
      ganHoaDonSauThaoTac(await layChiTietHoaDon(hoaDon.value.id));
      hienThiThongBao(
        "success",
        "Đã Tính Phí GHN",
        `Phí vận chuyển mới: ${dinhDangTien(ketQua.phiVanChuyen || ketQua.total || 0)}.`,
      );
    } catch (error) {
      showError(
        getDisplayErrorMessage(error, "Không thể tính phí vận chuyển GHN"),
      );
    } finally {
      dangTinhPhiGhn.value = false;
    }
  }

  function handlePrint() {
    if (!hoaDon.value) return;
    printInvoiceToPdf({
      invoice: hoaDon.value,
      filename: `hoa-don-${hoaDon.value.maHoaDon}`,
      formatCurrency: dinhDangTien,
      formatDate: dinhDangNgay,
    });
  }

  function moModalThanhToanCod() {
    if (!coTheThanhToanCod.value) return;
    formThanhToanCod.value = {
      hinhThucThanhToan: 2,
      tienKhachDua: String(tongTienThanhToanCod.value || ""),
      ghiChu: "Xác nhận thanh toán COD khi giao hàng",
    };
    hienModalThanhToanCod.value = true;
  }

  async function handleXacNhanThanhToanCod() {
    if (!hoaDon.value || dangXacNhanThanhToanCod.value) return;
    if (
      formThanhToanCod.value.hinhThucThanhToan === 1 &&
      tienThieuThanhToanCod.value > 0
    ) {
      hienThiThongBao(
        "warning",
        "Thiếu tiền thanh toán",
        "Tiền khách đưa chưa đủ để xác nhận thanh toán COD.",
      );
      return;
    }

    dangXacNhanThanhToanCod.value = true;
    try {
      ganHoaDonSauThaoTac(
        await xacNhanThanhToanCod(hoaDon.value.id, {
          hinhThucThanhToan: formThanhToanCod.value.hinhThucThanhToan,
          tienKhachDua:
            Number(formThanhToanCod.value.tienKhachDua) ||
            tongTienThanhToanCod.value,
          ghiChu: formThanhToanCod.value.ghiChu,
        }),
      );
      hienThiThongBao(
        "success",
        "Đã xác nhận thanh toán",
        "Thanh toán COD đã được ghi nhận.",
      );
      hienModalThanhToanCod.value = false;
    } catch (error) {
      hienThiThongBao(
        "error",
        "Lỗi xác nhận thanh toán",
        getDisplayErrorMessage(error, "Không thể xác nhận thanh toán COD."),
      );
    } finally {
      dangXacNhanThanhToanCod.value = false;
    }
  }

  const dsTaiKhoanNganHangKhach = ref([]);
  const dangTaiNganHangKhach = ref(false);
  const taiKhoanNganHangChon = ref(null);

  async function taiTaiKhoanNganHangKhach() {
    if (!hoaDon.value?.khachHangId) {
      dsTaiKhoanNganHangKhach.value = [];
      taiKhoanNganHangChon.value = null;
      return;
    }
    dangTaiNganHangKhach.value = true;
    try {
      const accounts = await layDanhSachTaiKhoanNganHang(
        hoaDon.value.khachHangId,
      );
      dsTaiKhoanNganHangKhach.value = accounts;
      const macDinh = accounts.find((a) => a.laMacDinh);
      taiKhoanNganHangChon.value =
        macDinh || (accounts.length > 0 ? accounts[0] : null);
    } catch (e) {
      console.error("Không thể tải danh sách tài khoản ngân hàng của khách", e);
    } finally {
      dangTaiNganHangKhach.value = false;
    }
  }

  const qrHoanTienUrl = computed(() => {
    if (!taiKhoanNganHangChon.value) return "";
    const bank = taiKhoanNganHangChon.value.tenNganHang;
    const account = taiKhoanNganHangChon.value.soTaiKhoan;
    const name = encodeURIComponent(taiKhoanNganHangChon.value.tenChuTaiKhoan);
    const amount = Number(formHoanTien.value.soTienHoan) || 0;
    const desc = encodeURIComponent(
      `HOAN TIEN DON ${hoaDon.value?.maHoaDon || hoaDon.value?.ma || ""}`,
    );
    return `https://img.vietqr.io/image/${bank}-${account}-compact2.png?amount=${amount}&addInfo=${desc}&accountName=${name}`;
  });

  function moModalHoanTien() {
    if (!coTheHoanTien.value) return;
    formHoanTien.value = {
      hinhThucHoanTien: 2,
      soTienHoan: String(tongTienHoan.value || ""),
      maGiaoDichHoan: "",
      ghiChu: "Đã hoàn tiền cho khách hàng",
    };
    taiTaiKhoanNganHangKhach();
    hienModalHoanTien.value = true;
  }

  async function handleXacNhanHoanTien() {
    if (
      !hoaDon.value ||
      dangXacNhanHoanTien.value ||
      !coTheHoanTien.value
    )
      return;
    const soTienHoan = Number(formHoanTien.value.soTienHoan) || 0;
    if (soTienHoan <= 0) {
      hienThiThongBao(
        "warning",
        "Số tiền không hợp lệ",
        "Số tiền hoàn phải lớn hơn 0.",
      );
      return;
    }
    if (soTienHoan !== Number(tongTienHoan.value || 0)) {
      hienThiThongBao(
        "warning",
        "Số tiền hoàn chưa đúng",
        "Hiện tại chỉ hỗ trợ hoàn toàn bộ số tiền cần hoàn.",
      );
      return;
    }

    if (
      Number(formHoanTien.value.hinhThucHoanTien) === 2 &&
      !taiKhoanNganHangChon.value?.id
    ) {
      hienThiThongBao(
        "warning",
        "Chưa chọn tài khoản nhận tiền",
        "Vui lòng chọn tài khoản ngân hàng của khách trước khi xác nhận hoàn tiền.",
      );
      return;
    }

    dangXacNhanHoanTien.value = true;
    try {
      ganHoaDonSauThaoTac(
        await xacNhanHoanTien(hoaDon.value.id, {
          hinhThucHoanTien: formHoanTien.value.hinhThucHoanTien,
          soTienHoan,
          maGiaoDichHoan: formHoanTien.value.maGiaoDichHoan,
          ghiChu: formHoanTien.value.ghiChu,
          taiKhoanNganHangId:
            Number(formHoanTien.value.hinhThucHoanTien) === 2
              ? taiKhoanNganHangChon.value?.id
              : null,
        }),
      );
      hienThiThongBao(
        "success",
        "Đã xác nhận hoàn tiền",
        "Giao dịch hoàn tiền đã được ghi nhận.",
      );
      hienModalHoanTien.value = false;
    } catch (error) {
      hienThiThongBao(
        "error",
        "Lỗi xác nhận hoàn tiền",
        getDisplayErrorMessage(error, "Không thể xác nhận hoàn tiền."),
      );
    } finally {
      dangXacNhanHoanTien.value = false;
    }
  }

  async function handleHuyDonTuModal(lyDoHuy) {
    if (!hoaDon.value || dangCapNhat.value) return;
    if (!lyDoHuy || !lyDoHuy.trim()) return;

    dangCapNhat.value = true;
    try {
      ganHoaDonSauThaoTac(
        await capNhatTrangThaiHoaDon(hoaDon.value.id, {
          trangThai: "Hủy",
          ghiChu: lyDoHuy.trim(),
        }),
      );
      hienThiThongBao(
        "success",
        "Hủy đơn hàng thành công",
        "Đơn hàng đã được chuyển sang trạng thái Hủy."
      );
      hienModalThongTin.value = false;
    } catch (error) {
      hienThiThongBao(
        "error",
        "Lỗi hủy đơn hàng",
        getDisplayErrorMessage(error, "Không thể hủy đơn hàng.")
      );
    } finally {
      dangCapNhat.value = false;
    }
  }

  let ngatKetNoiRealtime = null;
  let realtimeRefreshTimeout = null;

  onMounted(() => {
    taiChiTiet();
    ngatKetNoiRealtime = ketNoiHoaDonRealtime({
      authScope: "admin",
      onHoaDonThayDoi: (event) => {
        if (Number(event?.hoaDonId) !== Number(route.params.id)) return;
        if (Date.now() - lanCapNhatCucBoGanNhat < 2000) return;
        if (realtimeRefreshTimeout) clearTimeout(realtimeRefreshTimeout);
        realtimeRefreshTimeout = setTimeout(() => taiChiTiet(true), 150);
      },
    });
  });

  onBeforeUnmount(() => {
    ngatKetNoiRealtime?.();
    if (realtimeRefreshTimeout) clearTimeout(realtimeRefreshTimeout);
  });
  return {
    computed,
    onMounted,
    ref,
    watch,
    markRaw,
    useRoute,
    useRouter,
    ArrowLeft,
    Banknote,
    CheckCircle2,
    CircleCheck,
    CircleX,
    ClipboardList,
    ClipboardCheck,
    Flag,
    History,
    Hourglass,
    MapPin,
    Package,
    Pencil,
    Printer,
    Search,
    Trash2,
    TriangleAlert,
    Truck,
    User,
    X,
    Card,
    Button,
    capNhatSanPhamHoaDon,
    capNhatTrangThaiHoaDon,
    layChiTietHoaDon,
    tinhPhiVanChuyenGhn,
    xacNhanHoanTien,
    xacNhanThanhToanCod,
    timSanPhamTaiQuay,
    printInvoiceToPdf,
    getDisplayErrorMessage,
    logoGhn,
    route,
    router,
    hoaDon,
    dangTai,
    loiTrang,
    dangCapNhat,
    hienModalXacNhan,
    hienModalLichSu,
    hienModalSanPham,
    hienModalXacNhanHuy,
    hienModalThanhToanCod,
    dangXacNhanThanhToanCod,
    formThanhToanCod,
    hienModalHoanTien,
    dangXacNhanHoanTien,
    formHoanTien,
    hienModalThongTin,
    hienModalGiaoHang,
    dangLuuGiaoHang,
    diaChiDaLuu,
    tabHienTai,
    formThongTin,
    formGhn,
    dangTinhPhiGhn,
    diaChiGhnDaDo,
    trangThaiMoiXacNhan,
    ghiChuXacNhan,
    tuKhoaSanPham,
    ketQuaTimKiem,
    dangTimKiem,
    giaTuSanPham,
    giaDenSanPham,
    tuKhoaLocSanPham,
    loaiSanPhamDangLoc,
    sapXepSanPham,
    danhSachLoaiSanPham,
    giaTuSanPhamSo,
    giaDenSanPhamSo,
    giaLonNhatSanPham,
    nhanKhoangGiaSanPham,
    styleKhoangGiaSanPham,
    trangSanPhamHienTai,
    soSanPhamMoiTrang,
    danhSachSanPhamDaLoc,
    danhSachSanPhamPhanTrang,
    tongTrangSanPham,
    hienPhanTrangSanPham,
    danhSachSanPhamUpdate,
    cacBuocCoDinh,
    cacBuocGiaoThatBai,
    cacBuocYeuCauHuy,
    cacBuocDaHuy,
    laDonTaiQuay,
    cacBuoc,
    dinhDangTien,
    dinhDangNgay,
    dinhDangGio,
    vietHoaChuCaiDau,
    buocHienTai,
    donDaHoanThanh,
    donDaXacNhan,
    donYeuCauHuy,
    donGiaoThatBai,
    donDaHuy,
    donDaKetThuc,
    coTheSuaThongTinGiaoHang,
    hienThiThongBao,
    thongBaoDonDaHoanThanh,
    moModalThongTin,
    moModalSuaDiaChi,
    handleLuuGiaoHang,
    tongTienHang,
    tongKhachCanTra,
    coPhieuGiamGia,
    moTaGiaTriPhieuGiamGia,
    thanhToanGanNhat,
    thanhToanCodDangCho,
    coTheThanhToanCod,
    thanhToanCanHoanTien,
    coTheHoanTien,
    tongTienHoan,
    tongTienThanhToanCod,
    noiDungChuyenKhoanCod,
    qrThanhToanCodUrl,
    tienThieuThanhToanCod,
    lichSuRutGon,
    thongTinBuoc,
    cacBuocHienThi,
    lopVongTrangThai,
    lopTenTrangThai,
    taiChiTiet,
    openModalXacNhan,
    handleXacNhanTrangThai,
    handleXuLyYeuCauHuy,
    moModalXacNhanHuy,
    handleXacNhanHuyDon,
    timKiemSanPham,
    themSanPham,
    removeSanPham,
    handleSaveSanPham,
    danhSachTrangThaiHienThi,
    indexTrangThaiHienTai,
    isOptionDisabled,
    hienThiOptionTrangThai,
    handleLuuThongTin,
    handleTinhPhiGhn,
    handlePrint,
    moModalThanhToanCod,
    handleXacNhanThanhToanCod,
    moModalHoanTien,
    handleXacNhanHoanTien,
    handleHuyDonTuModal,
    dsTaiKhoanNganHangKhach,
    dangTaiNganHangKhach,
    taiKhoanNganHangChon,
    qrHoanTienUrl,
  };
}
