import { nextTick, onMounted, onUnmounted, ref, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Camera, Save, ScanLine, X } from "lucide-vue-next";
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
  doiChieuDiaChiCuCccd,
  layPhuongXaHaiCap,
  layTinhThanhHaiCap,
} from "../../../services/dia-chi";
import { chuanHoaDiaChi, taoPayloadDiaChi, timDonViDiaChi } from "../../../utils/dia-chi";
import {
  isValidEmail,
  isValidVnPhone,
  validateAddress,
  validateFullName,
} from "../../../utils/validation";

export function useChiTietNhanVien() {
  // QR Scanner - dùng @zxing/browser
  const dangQuet = ref(false);
  const loiCamera = ref("");
  const videoRef = ref(null);
  const dangQuetFile = ref(false);
  const thongBaoQrOk = ref("");
  const dangDoiChieuDiaChi = ref(false);
  const thongBaoAnhXaDiaChi = ref("");
  const anhXaDiaChiThanhCong = ref(false);
  let zxingReader = null;
  let BrowserMultiFormatReaderCtor = null;
  let daXuLyQr = false;
  let lanDoiChieuDiaChi = 0;

  async function layBrowserMultiFormatReader() {
    if (!BrowserMultiFormatReaderCtor) {
      const zxingBrowser = await import("@zxing/browser");
      BrowserMultiFormatReaderCtor = zxingBrowser.BrowserMultiFormatReader;
    }
    return BrowserMultiFormatReaderCtor;
  }

  async function batDauQuet() {
    daXuLyQr = false;
    loiCamera.value = "";
    dungQuet();
    dangQuet.value = true;
    await nextTick();
    try {
      if (!videoRef.value) throw new Error("Không tìm thấy video element");
      const BrowserMultiFormatReader = await layBrowserMultiFormatReader();
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
          if (err) {
            const isIgnored = err.name === "NotFoundException" || (err.message && err.message.includes("No MultiFormat Readers"));
            if (!isIgnored) {
              console.warn("[ZXing scan error]", err);
            }
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

  async function xuLyKetQuaQr(raw) {
    if (daXuLyQr) return;
    daXuLyQr = true;
    dungQuet();
    const resolvedRaw = raw.trim();
    loiCamera.value = "";
    try {
      if (isVneIdSecureQr(resolvedRaw)) {
        showError("QR trên ứng dụng VNeID là mã bảo mật, không chứa trực tiếp số CCCD. Vui lòng quét QR trên thẻ CCCD bản cứng.");
        return;
      }

      // Format CCCD QR: số_cccd|số_cmnd_cũ|họ_tên|ngày_sinh|giới_tính|địa_chỉ|ngày_cấp|nơi_cấp
      const parts = resolvedRaw.split("|");
      if (parts.length >= 6 && /^\d{12}$/.test(parts[0]?.trim() ?? "")) {
        if (parts[2]) form.value.hoTen = parts[2].trim();
        if (parts[3]) form.value.ngaySinh = formatNgaySinh(parts[3].trim());
        if (parts[4]) {
          const gt = parts[4].trim().toLowerCase();
          form.value.gioiTinh = gt === "nam" || gt === "0" ? "Nam" : "Nữ";
        }
        if (parts[5]) {
          await doiChieuDiaChiSauKhiQuet(parts[5].trim());
        }
        showSuccess(
          anhXaDiaChiThanhCong.value
            ? "Đã điền thông tin và chuyển địa chỉ CCCD sang 2 cấp"
            : "Đã điền thông tin từ CCCD",
          "Thành công",
        );
      } else {
        showError("Mã QR không đúng định dạng thẻ CCCD bản cứng.");
      }
    } catch {
      showError("Không thể đọc dữ liệu từ mã QR này.");
    }
  }

  function isVneIdSecureQr(raw) {
    const parts = String(raw ?? "").split("|");
    const laQrCccdVatLy = parts.length >= 6 && /^\d{12}$/.test(parts[0]?.trim() ?? "");
    return (
      !laQrCccdVatLy && (
        /^eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(raw) ||
        raw.length > 100
      )
    );
  }

  function formatNgaySinh(ddmmyyyy) {
    if (!ddmmyyyy || ddmmyyyy.length !== 8) return "";
    return `${ddmmyyyy.slice(4, 8)}-${ddmmyyyy.slice(2, 4)}-${ddmmyyyy.slice(0, 2)}`;
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
    diaChiCuThe: "",
    ngaySinh: "",
  });

  const form = ref({
    hoTen: "",
    tenDangNhap: "",
    email: "",
    matKhau: "",
    sdt: "",
    diaChiCuThe: "",
    tinhThanhCode: "",
    hinhAnh: "",
    vaiTro: 2,
    gioiTinh: "Nam",
    ngaySinh: "",
    tinhThanh: "",
    phuongXaCode: "",
    phuongXa: "",
    faceDescriptor: "",
  });

  const dsVaiTro = [
    { value: 1, label: "Admin" },
    { value: 2, label: "Nhân viên" },
  ];


  const dsTinhThanh = ref([]);

  const dsXaPhuong = ref([]);

  function gopDiaChi() {
    return taoPayloadDiaChi(form.value);
  }

  async function doiChieuDiaChiSauKhiQuet(diaChiCu) {
    const requestId = ++lanDoiChieuDiaChi;
    const diaChiTruocKhiQuet = {
      tinhThanhCode: form.value.tinhThanhCode,
      tinhThanh: form.value.tinhThanh,
      phuongXaCode: form.value.phuongXaCode,
      phuongXa: form.value.phuongXa,
      diaChiCuThe: form.value.diaChiCuThe,
    };
    dangDoiChieuDiaChi.value = true;
    anhXaDiaChiThanhCong.value = false;
    thongBaoAnhXaDiaChi.value = "Đang chuyển đổi địa chỉ CCCD sang địa chỉ 2 cấp...";

    try {
      const ketQua = await doiChieuDiaChiCuCccd(diaChiCu);
      if (requestId !== lanDoiChieuDiaChi) return;
      if (!ketQua?.daAnhXa) {
        if (!diaChiTruocKhiQuet.diaChiCuThe && ketQua?.diaChiCuThe) {
          form.value.diaChiCuThe = ketQua.diaChiCuThe;
        }
        thongBaoAnhXaDiaChi.value = ketQua?.thongBao
          || "Không xác định chắc chắn được địa chỉ mới. Vui lòng chọn tỉnh và phường/xã thủ công.";
        return;
      }

      if (!dsTinhThanh.value.length) {
        const danhSachTinh = await layTinhThanhHaiCap();
        if (requestId !== lanDoiChieuDiaChi) return;
        dsTinhThanh.value = danhSachTinh.map((item) => ({
          value: String(item.code),
          label: item.ten,
        }));
      }
      await chonTinhThanhNhanVien(ketQua.tinhThanhCode, false, true);
      if (requestId !== lanDoiChieuDiaChi) return;
      const phuongXa = dsXaPhuong.value.find(
        (item) => item.value === String(ketQua.phuongXaCode ?? ""),
      );
      if (!phuongXa) {
        throw new Error("Phường/xã được ánh xạ không thuộc tỉnh/thành hiện tại");
      }

      form.value.tinhThanhCode = String(ketQua.tinhThanhCode ?? "");
      form.value.tinhThanh = ketQua.tinhThanh ?? "";
      chonPhuongXaNhanVien(ketQua.phuongXaCode, true);
      form.value.diaChiCuThe = ketQua.diaChiCuThe ?? "";
      anhXaDiaChiThanhCong.value = true;
      thongBaoAnhXaDiaChi.value = `${ketQua.thongBao}: ${ketQua.phuongXa}, ${ketQua.tinhThanh}`;
    } catch (error) {
      if (requestId !== lanDoiChieuDiaChi) return;
      form.value.tinhThanhCode = diaChiTruocKhiQuet.tinhThanhCode;
      form.value.tinhThanh = diaChiTruocKhiQuet.tinhThanh;
      form.value.phuongXaCode = diaChiTruocKhiQuet.phuongXaCode;
      form.value.phuongXa = diaChiTruocKhiQuet.phuongXa;
      form.value.diaChiCuThe = diaChiTruocKhiQuet.diaChiCuThe || diaChiCu;
      thongBaoAnhXaDiaChi.value = getDisplayErrorMessage(
        error,
        "Không thể tự chuyển đổi địa chỉ. Vui lòng chọn tỉnh và phường/xã thủ công.",
      );
    } finally {
      if (requestId === lanDoiChieuDiaChi) {
        dangDoiChieuDiaChi.value = false;
      }
    }
  }

  async function chonTinhThanhNhanVien(tinhId, giuPhuongXa = false, tuDongAnhXa = false) {
    if (!tuDongAnhXa) huyAnhXaDangCho();
    const maXaHienTai = giuPhuongXa ? form.value.phuongXaCode : "";
    const tenTinhHienTai = giuPhuongXa ? form.value.tinhThanh : "";
    const tenXaHienTai = giuPhuongXa ? form.value.phuongXa : "";
    const tinh = timDonViDiaChi(dsTinhThanh.value, tinhId, tenTinhHienTai);
    form.value.tinhThanhCode = tinh?.value ?? "";
    form.value.tinhThanh = tinh?.label ?? (giuPhuongXa ? tenTinhHienTai : "");
    form.value.phuongXaCode = "";
    form.value.phuongXa = giuPhuongXa ? tenXaHienTai : "";
    try {
      dsXaPhuong.value = tinh
        ? (await layPhuongXaHaiCap(tinh.value)).map((item) => ({
            value: String(item.code),
            label: item.ten,
          }))
        : [];
    } catch (error) {
      dsXaPhuong.value = [];
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể tải danh sách xã/phường",
      );
      return;
    }
    if (giuPhuongXa) {
      const xa = timDonViDiaChi(dsXaPhuong.value, maXaHienTai, tenXaHienTai);
      form.value.phuongXaCode = xa?.value ?? "";
      form.value.phuongXa = xa?.label ?? tenXaHienTai;
    }
  }

  function chonPhuongXaNhanVien(code, tuDongAnhXa = false) {
    if (!tuDongAnhXa) huyAnhXaDangCho();
    const xa = dsXaPhuong.value.find((item) => item.value === String(code ?? ""));
    form.value.phuongXaCode = xa?.value ?? "";
    form.value.phuongXa = xa?.label ?? "";
  }

  async function apDungMaDiaChiDaQuet(duLieuQr) {
    const maTinh = String(duLieuQr.tinhThanh ?? "").trim();
    const maXa = String(duLieuQr.xaPhuong ?? "").trim();
    form.value.tinhThanhCode = maTinh;
    form.value.phuongXaCode = maXa;
    await chonTinhThanhNhanVien(maTinh, true);
  }

  function huyAnhXaDangCho() {
    if (dangDoiChieuDiaChi.value) {
      lanDoiChieuDiaChi += 1;
      dangDoiChieuDiaChi.value = false;
    }
    thongBaoAnhXaDiaChi.value = "";
    anhXaDiaChiThanhCong.value = false;
  }


  async function taiChiTiet() {
    if (laMoi) return;
    dangTai.value = true;
    try {
      const data = await layChiTietNhanVien(id);
      nhanVien.value = data;
      const diaChi = chuanHoaDiaChi(data.diaChi);
      form.value = {
        hoTen: data.hoTen ?? "",
        tenDangNhap: data.tenDangNhap ?? "",
        email: data.email ?? "",
        matKhau: "",
        sdt: data.sdt ?? "",
        gioiTinh: data.gioiTinh ?? "Nam",
        ngaySinh: data.ngaySinh ?? "",
        ...diaChi,
        hinhAnh: data.hinhAnh ?? "",
        vaiTro: data.vaiTro ?? 2,
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
    loiForm.value = { hoTen: "", email: "", sdt: "", diaChiCuThe: "", ngaySinh: "" };
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
        "Số điện thoại phải bắt đầu bằng 03, 05, 07, 08 hoặc 09 và có đúng 10 chữ số.";
      hasError = true;
    }

    const loiDiaChi = validateAddress(form.value.diaChiCuThe, "Địa chỉ");
    if (loiDiaChi) {
      loiForm.value.diaChiCuThe = loiDiaChi;
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

    const confirmed = await showConfirm(
      laMoi
        ? "Bạn có chắc chắn muốn thêm nhân viên mới này không?"
        : "Bạn có chắc chắn muốn lưu các thay đổi cho nhân viên này không?",
      laMoi ? "Xác nhận thêm nhân viên" : "Xác nhận lưu thay đổi",
      laMoi ? "Thêm mới" : "Lưu thay đổi",
      "Hủy",
    );
    if (!confirmed) return;

    dangLuu.value = true;
    loiTrang.value = "";

    const payload = {
      hoTen: form.value.hoTen.trim(),
      email: form.value.email.trim(),
      sdt: form.value.sdt.trim() || undefined,
      gioiTinh: form.value.gioiTinh || undefined,
      ngaySinh: form.value.ngaySinh || undefined,
      diaChi: gopDiaChi(),
      hinhAnh: form.value.hinhAnh || undefined,
      vaiTro: form.value.vaiTro,
      faceDescriptor: form.value.faceDescriptor || undefined,
    };
    if (!laMoi) {
      payload.tenDangNhap = form.value.tenDangNhap.trim();
    }

    try {
      if (laMoi) {
        await taoNhanVien(payload);
        if (typeof window !== "undefined") {
          window.sessionStorage.setItem(
            EMPLOYEE_CREATE_TOAST_KEY,
            JSON.stringify({
              loai: "success",
              noiDung: "Tạo tài khoản nhân viên thành công",
            }),
          );
        }
        router.push({ name: "admin-nhan-vien" });
        return;
      }

      const updated = await capNhatNhanVien(id, payload);
      nhanVien.value = updated;
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

    const message = trangThai === 1
      ? `Bạn có chắc muốn kích hoạt lại nhân viên "${nhanVien.value?.hoTen}"?`
      : `Bạn có chắc muốn khóa tài khoản nhân viên "${nhanVien.value?.hoTen}"?`;

    if (!(await showConfirm(message, "Xác nhận", "Đồng ý", "Hủy"))) {
      return;
    }

    try {
      const updated = await doiTrangThaiNhanVien(id, trangThai);
      nhanVien.value = updated;
      showSuccess(
        trangThai === 1
          ? `Đã kích hoạt nhân viên "${nhanVien.value?.hoTen || ""}" thành công.`
          : `Đã chuyển nhân viên "${nhanVien.value?.hoTen || ""}" sang trạng thái nghỉ làm thành công.`,
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
    try {
      const danhSachTinh = await layTinhThanhHaiCap();
      dsTinhThanh.value = danhSachTinh.map((item) => ({
        value: String(item.code),
        label: item.ten,
      }));
    } catch (error) {
      loiTrang.value = getDisplayErrorMessage(
        error,
        "Không thể tải danh sách tỉnh/thành",
      );
    }
    if (!laMoi) {
      await taiChiTiet();
      if (dsTinhThanh.value.length) {
        await chonTinhThanhNhanVien(form.value.tinhThanhCode, true);
      }
    }
  });

  onUnmounted(() => {
    lanDoiChieuDiaChi += 1;
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
    dangDoiChieuDiaChi,
    thongBaoAnhXaDiaChi,
    anhXaDiaChiThanhCong,
    zxingReader,
    daXuLyQr,
    batDauQuet,
    xuLyKetQuaQr,
    isVneIdSecureQr,
    formatNgaySinh,
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
    dsTinhThanh,
    dsXaPhuong,
    gopDiaChi,
    chonTinhThanhNhanVien,
    chonPhuongXaNhanVien,
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
