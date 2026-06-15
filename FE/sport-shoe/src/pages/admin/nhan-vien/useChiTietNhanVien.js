import { nextTick, onMounted, onUnmounted, ref, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Camera, Save, ScanLine, X } from "lucide-vue-next";
import { BrowserMultiFormatReader } from "@zxing/browser";
import {
  capNhatNhanVien,
  doiMatKhauNhanVien,
  doiTrangThaiNhanVien,
  layChiTietNhanVien,
  taoNhanVien,
  uploadFile,
  xoaNhanVien,
} from "../../../services/nhan-vien";
import { getCurrentAdminUser } from "../../../services/auth";
import {
  getDisplayErrorMessage,
  getFieldErrors,
} from "../../../utils/error-message";
import { showSuccess, showError, showConfirm } from "../../../utils/alert";
import {
  isValidEmail,
  isValidVnPhone,
  validateFullName,
} from "../../../utils/validation";

export function useChiTietNhanVien() {
  // QR Scanner - dùng @zxing/browser
  const dangQuet = ref(false);
  const loiCamera = ref("");
  const videoRef = ref(null);
  const dangQuetFile = ref(false);
  const thongBaoQrOk = ref("");
  let zxingReader = null;
  let daXuLyQr = false;

  async function batDauQuet() {
    daXuLyQr = false;
    loiCamera.value = "";
    dungQuet();
    dangQuet.value = true;
    await nextTick();
    try {
      if (!videoRef.value) throw new Error("Không tìm thấy video element");
      zxingReader = new BrowserMultiFormatReader();

      const constraints = {
        video: {
          facingMode: { ideal: "environment" },
          width: { ideal: 1280 },
          height: { ideal: 720 },
        },
      };

      await zxingReader.decodeFromConstraints(
        constraints,
        videoRef.value,
        (result, err) => {
          if (result) {
            xuLyKetQuaQr(result.getText());
          }
          if (err && err.name !== "NotFoundException") {
            console.warn("[ZXing scan error]", err);
          }
        },
      );
    } catch (e) {
      console.error("[batDauQuet]", e);
      const msg = String(e?.message ?? "");
      if (
        msg.toLowerCase().includes("permission") ||
        msg.toLowerCase().includes("notallowed")
      ) {
        loiCamera.value = "Vui lòng cho phép truy cập camera và thử lại.";
      } else {
        loiCamera.value =
          "Không thể mở camera. Hãy kiểm tra quyền truy cập và thử lại.";
      }
      zxingReader = null;
    }
  }

  function xuLyKetQuaQr(raw) {
    if (daXuLyQr) return;
    daXuLyQr = true;
    dungQuet();
    const resolvedRaw = raw.trim();
    loiForm.value.cccd = "";
    loiCamera.value = "";
    try {
      if (isVneIdSecureQr(resolvedRaw)) {
        loiForm.value.cccd =
          "QR trên ứng dụng VNeID là mã bảo mật, không chứa trực tiếp số CCCD. Vui lòng quét QR trên thẻ CCCD bản cứng hoặc nhập tay 12 số CCCD.";
        return;
      }

      // Format CCCD QR: số_cccd|số_cmnd_cũ|họ_tên|ngày_sinh|giới_tính|địa_chỉ|ngày_cấp|nơi_cấp
      const parts = resolvedRaw.split("|");
      if (parts.length >= 3) {
        const scannedCccd = parts[0]?.trim() ?? "";
        if (!/^\d{12}$/.test(scannedCccd)) {
          loiForm.value.cccd =
            "QR không có số CCCD hợp lệ. Vui lòng quét thẻ CCCD bản cứng hoặc nhập tay 12 số CCCD.";
          return;
        }
        form.value.cccd = scannedCccd;
        if (parts[2]) form.value.hoTen = parts[2].trim();
        if (parts[3]) form.value.ngaySinh = formatNgaySinh(parts[3].trim());
        if (parts[4]) {
          const gt = parts[4].trim().toLowerCase();
          form.value.gioiTinh = gt === "nam" || gt === "0" ? "Nam" : "Nữ";
        }
        if (parts[5]) form.value.diaChiCuThe = parts[5].trim();
      } else if (/^\d{12}$/.test(resolvedRaw)) {
        form.value.cccd = resolvedRaw;
      } else {
        loiForm.value.cccd =
          "Mã QR không đúng định dạng CCCD. Vui lòng quét thẻ CCCD bản cứng hoặc nhập tay 12 số CCCD.";
        return;
      }
      showSuccess("Đã điền thông tin từ CCCD", "Thành công");
    } catch {
      loiForm.value.cccd = "Không thể đọc dữ liệu CCCD từ mã QR này.";
    }
  }

  function isVneIdSecureQr(raw) {
    return (
      /^eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(raw) ||
      raw.length > 100
    );
  }

  function formatNgaySinh(ddmmyyyy) {
    if (!ddmmyyyy || ddmmyyyy.length !== 8) return "";
    return `${ddmmyyyy.slice(4, 8)}-${ddmmyyyy.slice(2, 4)}-${ddmmyyyy.slice(0, 2)}`;
  }

  function syncCurrentAdminCccd(updated) {
    if (typeof window === "undefined" || !updated?.id) return;

    const storageKeys = ["adminUser", "sport-shoe-admin-session"];
    for (const key of storageKeys) {
      const raw = window.localStorage.getItem(key);
      if (!raw) continue;
      try {
        const current = JSON.parse(raw);
        if (String(current?.id) === String(updated.id)) {
          const next = { ...current, cccd: updated.cccd ?? "" };
          window.localStorage.setItem(key, JSON.stringify(next));
        }
      } catch {
        // Ignore stale localStorage values.
      }
    }
  }

  function dungQuet() {
    dangQuet.value = false;
    // Explicitly stop all camera tracks
    if (videoRef.value && videoRef.value.srcObject instanceof MediaStream) {
      videoRef.value.srcObject.getTracks().forEach((track) => track.stop());
      videoRef.value.srcObject = null;
    }
    try {
      zxingReader?.reset();
    } catch {
      /* ignore */
    }
    zxingReader = null;
  }

  const route = useRoute();
  const router = useRouter();

  const id = route.params.id;
  const laMoi = !id;
  const EMPLOYEE_CREATE_TOAST_KEY = "admin-nhan-vien-toast";

  const dangTai = ref(false);
  const dangLuu = ref(false);
  const dangUpload = ref(false);
  const loiTrang = ref("");
  const nhanVien = ref(null);
  const adminHienTai = getCurrentAdminUser();
  const laChinhMinh = computed(() => {
    return (
      nhanVien.value &&
      adminHienTai &&
      String(nhanVien.value.id) === String(adminHienTai.id)
    );
  });
  const formatDateInputValue = (date) => {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const dd = String(date.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
  };
  const ngaySinhToiDa = computed(() => {
    const today = new Date();
    today.setFullYear(today.getFullYear() - 18);
    return formatDateInputValue(today);
  });
  const ngaySinhToiThieu = computed(() => {
    const today = new Date();
    today.setFullYear(today.getFullYear() - 80);
    return formatDateInputValue(today);
  });
  const fileInputAvatar = ref(null);
  const matKhauMoi = ref("");
  const showDoiMatKhau = ref(false);

  const loiForm = ref({
    hoTen: "",
    email: "",
    sdt: "",
    cccd: "",
    ngaySinh: "",
  });

  const form = ref({
    hoTen: "",
    tenDangNhap: "",
    email: "",
    matKhau: "",
    sdt: "",
    cccd: "",
    diaChiCuThe: "",
    hinhAnh: "",
    vaiTro: 2,
    gioiTinh: "Nam",
    ngaySinh: "",
    tinhThanh: "",
    quanHuyen: "",
    xaPhuong: "",
  });

  const dsVaiTro = [
    { value: 1, label: "Admin" },
    { value: 2, label: "Nhân viên" },
  ];

  const dsQuanHuyenTheoTinh = {
    "ha-noi": [
      { value: "cau-giay", label: "Cầu Giấy" },
      { value: "dong-da", label: "Đống Đa" },
      { value: "nam-tu-liem", label: "Nam Từ Liêm" },
    ],
    "ho-chi-minh": [
      { value: "quan-1", label: "Quận 1" },
      { value: "quan-7", label: "Quận 7" },
      { value: "thu-duc", label: "TP Thủ Đức" },
    ],
    "da-nang": [
      { value: "hai-chau", label: "Hải Châu" },
      { value: "thanh-khe", label: "Thanh Khê" },
      { value: "son-tra", label: "Sơn Trà" },
    ],
  };
  const dsTinhThanh = [
    { value: "01", label: "Thành phố Hà Nội" },
    { value: "79", label: "Thành phố Hồ Chí Minh" },
    { value: "31", label: "Thành phố Hải Phòng" },
    { value: "48", label: "Thành phố Đà Nẵng" },
    { value: "92", label: "Thành phố Cần Thơ" },
    { value: "02", label: "Tỉnh Hà Giang" },
    { value: "04", label: "Tỉnh Cao Bằng" },
    { value: "06", label: "Tỉnh Bắc Kạn" },
    { value: "08", label: "Tỉnh Tuyên Quang" },
    { value: "10", label: "Tỉnh Lào Cai" },
    { value: "11", label: "Tỉnh Điện Biên" },
    { value: "12", label: "Tỉnh Lai Châu" },
    { value: "14", label: "Tỉnh Sơn La" },
    { value: "15", label: "Tỉnh Yên Bái" },
    { value: "17", label: "Tỉnh Hoà Bình" },
    { value: "19", label: "Tỉnh Thái Nguyên" },
    { value: "20", label: "Tỉnh Lạng Sơn" },
    { value: "22", label: "Tỉnh Quảng Ninh" },
    { value: "24", label: "Tỉnh Bắc Giang" },
    { value: "25", label: "Tỉnh Phú Thọ" },
    { value: "26", label: "Tỉnh Vĩnh Phúc" },
    { value: "27", label: "Tỉnh Bắc Ninh" },
    { value: "30", label: "Tỉnh Hải Dương" },
    { value: "33", label: "Tỉnh Hưng Yên" },
    { value: "34", label: "Tỉnh Thái Bình" },
    { value: "35", label: "Tỉnh Hà Nam" },
    { value: "36", label: "Tỉnh Nam Định" },
    { value: "37", label: "Tỉnh Ninh Bình" },
    { value: "38", label: "Tỉnh Thanh Hóa" },
    { value: "40", label: "Tỉnh Nghệ An" },
    { value: "42", label: "Tỉnh Hà Tĩnh" },
    { value: "44", label: "Tỉnh Quảng Bình" },
    { value: "45", label: "Tỉnh Quảng Trị" },
    { value: "46", label: "Tỉnh Thừa Thiên Huế" },
    { value: "49", label: "Tỉnh Quảng Nam" },
    { value: "51", label: "Tỉnh Quảng Ngãi" },
    { value: "52", label: "Tỉnh Bình Định" },
    { value: "54", label: "Tỉnh Phú Yên" },
    { value: "56", label: "Tỉnh Khánh Hòa" },
    { value: "58", label: "Tỉnh Ninh Thuận" },
    { value: "60", label: "Tỉnh Bình Thuận" },
    { value: "62", label: "Tỉnh Kon Tum" },
    { value: "64", label: "Tỉnh Gia Lai" },
    { value: "66", label: "Tỉnh Đắk Lắk" },
    { value: "67", label: "Tỉnh Đắk Nông" },
    { value: "68", label: "Tỉnh Lâm Đồng" },
    { value: "70", label: "Tỉnh Bình Phước" },
    { value: "72", label: "Tỉnh Tây Ninh" },
    { value: "74", label: "Tỉnh Bình Dương" },
    { value: "75", label: "Tỉnh Đồng Nai" },
    { value: "77", label: "Tỉnh Bà Rịa - Vũng Tàu" },
    { value: "80", label: "Tỉnh Long An" },
    { value: "82", label: "Tỉnh Tiền Giang" },
    { value: "83", label: "Tỉnh Bến Tre" },
    { value: "84", label: "Tỉnh Trà Vinh" },
    { value: "86", label: "Tỉnh Vĩnh Long" },
    { value: "87", label: "Tỉnh Đồng Tháp" },
    { value: "89", label: "Tỉnh An Giang" },
    { value: "91", label: "Tỉnh Kiên Giang" },
    { value: "93", label: "Tỉnh Hậu Giang" },
    { value: "94", label: "Tỉnh Sóc Trăng" },
    { value: "95", label: "Tỉnh Bạc Liêu" },
    { value: "96", label: "Tỉnh Cà Mau" },
  ];
  const dsXaPhuongTheoQuan = {
    "cau-giay": [
      { value: "dich-vong", label: "Dịch Vọng" },
      { value: "mai-dich", label: "Mai Dịch" },
      { value: "nghia-tan", label: "Nghĩa Tân" },
    ],
    "dong-da": [
      { value: "cat-linh", label: "Cát Linh" },
      { value: "lang-thuong", label: "Láng Thượng" },
      { value: "quoc-tu-giam", label: "Quốc Tử Giám" },
    ],
    "nam-tu-liem": [
      { value: "my-dinh-1", label: "Mỹ Đình 1" },
      { value: "my-dinh-2", label: "Mỹ Đình 2" },
      { value: "trung-van", label: "Trung Văn" },
    ],
    "quan-1": [
      { value: "ben-nghe", label: "Bến Nghé" },
      { value: "ben-thanh", label: "Bến Thành" },
      { value: "da-kao", label: "Đa Kao" },
    ],
    "quan-7": [
      { value: "tan-phong", label: "Tân Phong" },
      { value: "tan-quy", label: "Tân Quy" },
      { value: "phu-my", label: "Phú Mỹ" },
    ],
    "thu-duc": [
      { value: "an-khanh", label: "An Khánh" },
      { value: "hiep-binh", label: "Hiệp Bình" },
      { value: "linh-trung", label: "Linh Trung" },
    ],
    "hai-chau": [
      { value: "hai-chau-1", label: "Hải Châu 1" },
      { value: "hai-chau-2", label: "Hải Châu 2" },
      { value: "thach-thang", label: "Thạch Thang" },
    ],
    "thanh-khe": [
      { value: "tam-thuan", label: "Tam Thuận" },
      { value: "thanh-khe-dong", label: "Thanh Khê Đông" },
      { value: "xuan-ha", label: "Xuân Hà" },
    ],
    "son-tra": [
      { value: "an-hai-bac", label: "An Hải Bắc" },
      { value: "an-hai-dong", label: "An Hải Đông" },
      { value: "man-thai", label: "Mân Thái" },
    ],
  };
  // 1. Thêm hàm fetch dữ liệu từ API (Ví dụ dùng pro-vinces.open-api.vn)
  const dsQuanHuyen = ref([]);
  const dsXaPhuong = ref([]);

  // Theo dõi thay đổi Tỉnh/Thành để load Quận/Huyện
  watch(
    () => form.value.tinhThanh,
    async (newVal) => {
      form.value.quanHuyen = "";
      form.value.xaPhuong = "";
      dsXaPhuong.value = [];

      if (!newVal) {
        dsQuanHuyen.value = [];
        return;
      }

      try {
        // Đây là URL API lấy quận huyện theo mã tỉnh (newVal là mã số như '01', '79')
        const res = await fetch(
          `https://provinces.open-api.vn/api/p/${newVal}?depth=2`,
        );
        const data = await res.json();
        dsQuanHuyen.value = data.districts.map((d) => ({
          value: d.code.toString(),
          label: d.name,
        }));
      } catch (e) {
        console.error("Lỗi tải quận huyện", e);
      }
    },
  );

  // Theo dõi thay đổi Quận/Huyện để load Xã/Phường
  watch(
    () => form.value.quanHuyen,
    async (newVal) => {
      form.value.xaPhuong = "";
      if (!newVal) {
        dsXaPhuong.value = [];
        return;
      }

      try {
        const res = await fetch(
          `https://provinces.open-api.vn/api/d/${newVal}?depth=2`,
        );
        const data = await res.json();
        dsXaPhuong.value = data.wards.map((w) => ({
          value: w.code.toString(),
          label: w.name,
        }));
      } catch (e) {
        console.error("Lỗi tải xã phường", e);
      }
    },
  );
  function layLabel(options, value) {
    return options.find((item) => item.value == value)?.label ?? "";
  }

  function gopDiaChi() {
    const diaChiFull = [
      form.value.diaChiCuThe.trim(),
      layLabel(dsXaPhuong.value, form.value.xaPhuong),
      layLabel(dsQuanHuyen.value, form.value.quanHuyen),
      layLabel(dsTinhThanh, form.value.tinhThanh),
    ]
      .filter(Boolean)
      .join(", ");
    return diaChiFull;
  }

  async function apDungMaDiaChiDaQuet(duLieuQr) {
    const maTinhThanh = String(duLieuQr.tinhThanh ?? "").trim();
    const maQuanHuyen = String(duLieuQr.quanHuyen ?? "").trim();
    const maXaPhuong = String(duLieuQr.xaPhuong ?? "").trim();

    form.value.tinhThanh = maTinhThanh;

    if (!maTinhThanh) {
      form.value.quanHuyen = "";
      form.value.xaPhuong = "";
      dsQuanHuyen.value = [];
      dsXaPhuong.value = [];
      return;
    }

    try {
      const responseTinh = await fetch(
        `https://provinces.open-api.vn/api/p/${maTinhThanh}?depth=2`,
      );
      const dataTinh = await responseTinh.json();
      dsQuanHuyen.value = Array.isArray(dataTinh.districts)
        ? dataTinh.districts.map((district) => ({
            value: district.code.toString(),
            label: district.name,
          }))
        : [];
    } catch (error) {
      console.error("Không thể tải quận huyện từ dữ liệu QR", error);
      dsQuanHuyen.value = [];
    }

    form.value.quanHuyen = maQuanHuyen;

    if (!maQuanHuyen) {
      form.value.xaPhuong = "";
      dsXaPhuong.value = [];
      return;
    }

    try {
      const responseHuyen = await fetch(
        `https://provinces.open-api.vn/api/d/${maQuanHuyen}?depth=2`,
      );
      const dataHuyen = await responseHuyen.json();
      dsXaPhuong.value = Array.isArray(dataHuyen.wards)
        ? dataHuyen.wards.map((ward) => ({
            value: ward.code.toString(),
            label: ward.name,
          }))
        : [];
    } catch (error) {
      console.error("Không thể tải xã phường từ dữ liệu QR", error);
      dsXaPhuong.value = [];
    }

    form.value.xaPhuong = maXaPhuong;
  }

  async function taiChiTiet() {
    if (laMoi) return;
    dangTai.value = true;
    try {
      const data = await layChiTietNhanVien(id);
      nhanVien.value = data;
      form.value = {
        hoTen: data.hoTen ?? "",
        tenDangNhap: data.tenDangNhap ?? "",
        email: data.email ?? "",
        matKhau: "",
        sdt: data.sdt ?? "",
        cccd: data.cccd ?? "",
        gioiTinh: data.gioiTinh ?? "Nam",
        ngaySinh: data.ngaySinh ?? "",
        diaChiCuThe: data.diaChi ?? "",
        hinhAnh: data.hinhAnh ?? "",
        vaiTro: data.vaiTro ?? 2,
        tinhThanh: "",
        quanHuyen: "",
        xaPhuong: "",
      };
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể tải thông tin nhân viên",
      );
    } finally {
      dangTai.value = false;
    }
  }

  async function luu() {
    loiForm.value = { hoTen: "", email: "", sdt: "", cccd: "", ngaySinh: "" };
    let hasError = false;

    const loiHoTen = validateFullName(form.value.hoTen, "Họ và tên nhân viên");
    if (loiHoTen) {
      loiForm.value.hoTen = loiHoTen;
      hasError = true;
    }

    const email = form.value.email.trim();
    if (!email) {
      loiForm.value.email = "Vui lòng nhập email nhân viên.";
      hasError = true;
    } else if (form.value.email !== email) {
      loiForm.value.email = "Email không được có khoảng trắng ở đầu hoặc cuối.";
      hasError = true;
    } else if (email.length > 100) {
      loiForm.value.email = "Email không quá 100 ký tự.";
      hasError = true;
    } else if (!isValidEmail(email)) {
      loiForm.value.email = "Email nhân viên chưa đúng định dạng.";
      hasError = true;
    }

    if (!isValidVnPhone(form.value.sdt)) {
      loiForm.value.sdt =
        "Số điện thoại không đúng định dạng (VD: 0901234567).";
      hasError = true;
    }

    if (form.value.ngaySinh) {
      const selectedDate = new Date(form.value.ngaySinh);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (selectedDate > today) {
        loiForm.value.ngaySinh =
          "Ngày sinh không được là ngày trong tương lai.";
        hasError = true;
      } else {
        // Tuổi phải > 17 (tức từ đủ 18 tuổi)
        const birth18 = new Date(today);
        birth18.setFullYear(birth18.getFullYear() - 18);

        if (selectedDate > birth18) {
          loiForm.value.ngaySinh = "Người dùng phải từ 18 tuổi trở lên.";
          hasError = true;
        }

        // Tuổi không được lớn hơn 80
        const birth80 = new Date(today);
        birth80.setFullYear(birth80.getFullYear() - 80);

        if (selectedDate < birth80) {
          loiForm.value.ngaySinh = "Tuổi không được lớn hơn 80.";
          hasError = true;
        }
      }
    }

    if (hasError) return;

    if (laMoi) {
      const confirmed = await showConfirm(
        "Bạn có chắc chắn muốn thêm nhân viên mới này không?",
        "Xác nhận thêm nhân viên",
        "Thêm mới",
        "Hủy",
      );
      if (!confirmed) return;
    }

    dangLuu.value = true;
    loiTrang.value = "";

    const payload = {
      hoTen: form.value.hoTen.trim(),
      email: form.value.email.trim(),
      sdt: form.value.sdt.trim() || undefined,
      cccd: form.value.cccd.trim() || undefined,
      gioiTinh: form.value.gioiTinh || undefined,
      ngaySinh: form.value.ngaySinh || undefined,
      diaChi: gopDiaChi() || form.value.diaChiCuThe.trim() || undefined,
      hinhAnh: form.value.hinhAnh || undefined,
      vaiTro: form.value.vaiTro,
    };
    if (!laMoi) {
      payload.tenDangNhap = form.value.tenDangNhap.trim();
    }

    try {
      if (laMoi) {
        const created = await taoNhanVien(payload);
        if (typeof window !== "undefined") {
          const emailGuiThanhCong = created.emailDaGuiThanhCong !== false;
          const chiTietDangNhap = [
            created.email
              ? emailGuiThanhCong
                ? `Đã gửi thông tin đăng nhập tới email: ${created.email}`
                : `Email tài khoản: ${created.email}`
              : "",
            !emailGuiThanhCong && created.canhBaoEmail
              ? created.canhBaoEmail
              : "",
            created.tenDangNhap ? `Tên đăng nhập: ${created.tenDangNhap}` : "",
            created.matKhauTamThoi
              ? `Mật khẩu tạm thời: ${created.matKhauTamThoi}`
              : "",
          ]
            .filter(Boolean)
            .join(" | ");

          window.sessionStorage.setItem(
            EMPLOYEE_CREATE_TOAST_KEY,
            JSON.stringify({
              loai: emailGuiThanhCong ? "success" : "error",
              noiDung: emailGuiThanhCong
                ? "Thêm nhân viên thành công"
                : `Thêm nhân viên thành công, nhưng chưa gửi được email đăng nhập.${chiTietDangNhap ? ` ${chiTietDangNhap}` : ""}`,
            }),
          );
        }
        router.push({ name: "admin-nhan-vien" });
        return;
      }

      const updated = await capNhatNhanVien(id, payload);
      nhanVien.value = updated;
      syncCurrentAdminCccd(updated);
      if (
        route.query.requireCccd === "1" &&
        /^\d{12}$/.test(String(updated.cccd ?? ""))
      ) {
        const redirectPath =
          typeof route.query.redirect === "string"
            ? route.query.redirect
            : "/admin";
        router.push(
          redirectPath.startsWith("/admin") ? redirectPath : "/admin",
        );
        return;
      }
      await showSuccess("Đã lưu thay đổi thành công.", "Thành công");
      router.push({ name: "admin-nhan-vien" });
    } catch (error) {
      Object.assign(loiForm.value, getFieldErrors(error));
      const errorMessage = getDisplayErrorMessage(
        error,
        laMoi ? "Không thể tạo nhân viên" : "Không thể cập nhật nhân viên",
      );
      loiTrang.value = errorMessage;
      showError(errorMessage);
    } finally {
      dangLuu.value = false;
    }
  }

  async function doiMatKhau() {
    if (!matKhauMoi.value.trim() || matKhauMoi.value.trim().length < 6) {
      loiTrang.value = "Mật khẩu mới phải có ít nhất 6 ký tự.";
      return;
    }

    dangLuu.value = true;
    loiTrang.value = "";
    try {
      await doiMatKhauNhanVien(id, matKhauMoi.value.trim());
      showSuccess("Đã đổi mật khẩu thành công.", "Thành công");
      matKhauMoi.value = "";
      showDoiMatKhau.value = false;
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể đổi mật khẩu nhân viên",
      );
    } finally {
      dangLuu.value = false;
    }
  }

  async function doiTrangThai(trangThai) {
    if (trangThai === 0 && laChinhMinh.value) {
      showError("Bạn không thể tự khóa tài khoản của chính mình.");
      return;
    }
    try {
      const updated = await doiTrangThaiNhanVien(id, trangThai);
      nhanVien.value = updated;
      showSuccess(
        trangThai === 1 ? "Đã kích hoạt tài khoản." : "Đã khóa tài khoản.",
        "Thành công",
      );
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể cập nhật trạng thái nhân viên",
      );
    }
  }

  async function xoaNhanVienHienTai() {
    if (laChinhMinh.value) {
      showError("Bạn không thể tự xóa tài khoản của chính mình.");
      return;
    }
    const confirmed = await showConfirm(
      "Bạn có chắc chắn muốn xóa nhân viên này không?",
      "Xác nhận xóa",
      "Xóa",
      "Hủy",
    );
    if (!confirmed) return;
    try {
      await xoaNhanVien(id);
      router.push({ name: "admin-nhan-vien" });
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(error, "Không thể xóa nhân viên");
    }
  }

  async function xuLyUploadAnh(event) {
    const target = event.target;
    if (!target.files?.length) return;

    dangUpload.value = true;
    loiTrang.value = "";
    try {
      const url = await uploadFile(target.files[0]);
      form.value.hinhAnh = url;
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể tải ảnh nhân viên",
      );
    } finally {
      dangUpload.value = false;
    }
  }

  onMounted(async () => {
    if (!laMoi) {
      await taiChiTiet();
    }
  });

  onUnmounted(() => {
    dungQuet();
  });

  return {
    nextTick,
    onMounted,
    onUnmounted,
    ref,
    watch,
    useRoute,
    useRouter,
    ArrowLeft,
    Camera,
    Save,
    ScanLine,
    X,
    dangQuet,
    loiCamera,
    videoRef,
    dangQuetFile,
    thongBaoQrOk,
    zxingReader,
    daXuLyQr,
    batDauQuet,
    xuLyKetQuaQr,
    isVneIdSecureQr,
    formatNgaySinh,
    syncCurrentAdminCccd,
    dungQuet,
    route,
    router,
    id,
    laMoi,
    dangTai,
    dangLuu,
    dangUpload,
    loiTrang,
    nhanVien,
    fileInputAvatar,
    matKhauMoi,
    showDoiMatKhau,
    loiForm,
    form,
    dsVaiTro,
    dsQuanHuyenTheoTinh,
    dsTinhThanh,
    dsXaPhuongTheoQuan,
    dsQuanHuyen,
    dsXaPhuong,
    layLabel,
    gopDiaChi,
    apDungMaDiaChiDaQuet,
    taiChiTiet,
    luu,
    doiMatKhau,
    doiTrangThai,
    xoaNhanVienHienTai,
    xuLyUploadAnh,
    laChinhMinh,
    ngaySinhToiDa,
    ngaySinhToiThieu,
  };
}
