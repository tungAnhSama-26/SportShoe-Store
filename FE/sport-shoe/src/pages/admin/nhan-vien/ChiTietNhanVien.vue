<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from "vue";
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
import { getDisplayErrorMessage, getFieldErrors } from "../../../utils/error-message";

// QR Scanner - dùng @zxing/browser
const dangQuet = ref(false);
const loiCamera = ref('');
const videoRef = ref<HTMLVideoElement | null>(null);
const dangQuetFile = ref(false);
const thongBaoQrOk = ref('');
let zxingReader: BrowserMultiFormatReader | null = null;
let daXuLyQr = false;

async function batDauQuet() {
  daXuLyQr = false;
  loiCamera.value = '';
  dungQuet();
  dangQuet.value = true;
  await nextTick();
  try {
    if (!videoRef.value) throw new Error('Không tìm thấy video element');
    zxingReader = new BrowserMultiFormatReader();

    const constraints: MediaStreamConstraints = {
      video: {
        facingMode: { ideal: 'environment' },
        width: { ideal: 1280 },
        height: { ideal: 720 },
      }
    };

    await zxingReader.decodeFromConstraints(
      constraints,
      videoRef.value,
      (result, err) => {
        if (result) {
          xuLyKetQuaQr(result.getText());
        }
        if (err && err.name !== 'NotFoundException') {
          console.warn('[ZXing scan error]', err);
        }
      }
    );
  } catch (e: any) {
    console.error('[batDauQuet]', e);
    const msg = String(e?.message ?? '');
    if (msg.toLowerCase().includes('permission') || msg.toLowerCase().includes('notallowed')) {
      loiCamera.value = 'Vui lòng cho phép truy cập camera và thử lại.';
    } else {
      loiCamera.value = 'Không thể mở camera. Hãy kiểm tra quyền truy cập và thử lại.';
    }
    zxingReader = null;
  }
}

function xuLyKetQuaQr(raw: string) {
  if (daXuLyQr) return;
  daXuLyQr = true;
  dungQuet();
  const resolvedRaw = raw.trim();
  loiForm.value.cccd = "";
  loiCamera.value = "";
  try {
    if (isVneIdSecureQr(resolvedRaw)) {
      loiForm.value.cccd = "QR trên ứng dụng VNeID là mã bảo mật, không chứa trực tiếp số CCCD. Vui lòng quét QR trên thẻ CCCD bản cứng hoặc nhập tay 12 số CCCD.";
      return;
    }

    // Format CCCD QR: số_cccd|số_cmnd_cũ|họ_tên|ngày_sinh|giới_tính|địa_chỉ|ngày_cấp|nơi_cấp
    const parts = resolvedRaw.split('|');
    if (parts.length >= 3) {
      const scannedCccd = parts[0]?.trim() ?? "";
      if (!/^\d{12}$/.test(scannedCccd)) {
        loiForm.value.cccd = "QR không có số CCCD hợp lệ. Vui lòng quét thẻ CCCD bản cứng hoặc nhập tay 12 số CCCD.";
        return;
      }
      form.value.cccd = scannedCccd;
      if (parts[2]) form.value.hoTen = parts[2].trim();
      if (parts[3]) form.value.ngaySinh = formatNgaySinh(parts[3].trim());
      if (parts[4]) {
        const gt = parts[4].trim().toLowerCase();
        form.value.gioiTinh = (gt === 'nam' || gt === '0') ? 'Nam' : 'Nữ';
      }
      if (parts[5]) form.value.diaChiCuThe = parts[5].trim();
    } else if (/^\d{12}$/.test(resolvedRaw)) {
      form.value.cccd = resolvedRaw;
    } else {
      loiForm.value.cccd = "Mã QR không đúng định dạng CCCD. Vui lòng quét thẻ CCCD bản cứng hoặc nhập tay 12 số CCCD.";
      return;
    }
    thongBaoQrOk.value = 'Đã điền thông tin từ CCCD';
    setTimeout(() => { thongBaoQrOk.value = ''; }, 4000);
  } catch {
    loiForm.value.cccd = "Không thể đọc dữ liệu CCCD từ mã QR này.";
  }
}

function isVneIdSecureQr(raw: string) {
  return /^eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(raw)
    || raw.length > 100;
}

function formatNgaySinh(ddmmyyyy: string) {
  if (!ddmmyyyy || ddmmyyyy.length !== 8) return '';
  return `${ddmmyyyy.slice(4,8)}-${ddmmyyyy.slice(2,4)}-${ddmmyyyy.slice(0,2)}`;
}

function syncCurrentAdminCccd(updated: any) {
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
    videoRef.value.srcObject.getTracks().forEach(track => track.stop());
    videoRef.value.srcObject = null;
  }
  try {
    zxingReader?.reset();
  } catch { /* ignore */ }
  zxingReader = null;
}

const route = useRoute();
const router = useRouter();

const id = route.params.id as string | undefined;
const laMoi = !id;

const dangTai = ref(false);
const dangLuu = ref(false);
const dangUpload = ref(false);
const loiTrang = ref("");
const thongBao = ref("");
const nhanVien = ref<any>(null);
const fileInputAvatar = ref<HTMLInputElement | null>(null);
const matKhauMoi = ref("");
const showDoiMatKhau = ref(false);

const loiForm = ref({
  hoTen: "",
  email: "",
  sdt: "",
  cccd: "",
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
  { value: 2, label: "Bán hàng" },
  { value: 3, label: "Kho" },
];



const dsQuanHuyenTheoTinh: Record<string, Array<{ value: string; label: string }>> = {
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
  { value: "96", label: "Tỉnh Cà Mau" }
];
const dsXaPhuongTheoQuan: Record<string, Array<{ value: string; label: string }>> = {
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
const dsQuanHuyen = ref<any[]>([]);
const dsXaPhuong = ref<any[]>([]);

// Theo dõi thay đổi Tỉnh/Thành để load Quận/Huyện
watch(() => form.value.tinhThanh, async (newVal) => {
  form.value.quanHuyen = "";
  form.value.xaPhuong = "";
  dsXaPhuong.value = [];
  
  if (!newVal) {
    dsQuanHuyen.value = [];
    return;
  }
  
  try {
    // Đây là URL API lấy quận huyện theo mã tỉnh (newVal là mã số như '01', '79')
    const res = await fetch(`https://provinces.open-api.vn/api/p/${newVal}?depth=2`);
    const data = await res.json();
    dsQuanHuyen.value = data.districts.map((d: any) => ({
      value: d.code.toString(),
      label: d.name
    }));
  } catch (e) {
    console.error("Lỗi tải quận huyện", e);
  }
});

// Theo dõi thay đổi Quận/Huyện để load Xã/Phường
watch(() => form.value.quanHuyen, async (newVal) => {
  form.value.xaPhuong = "";
  if (!newVal) {
    dsXaPhuong.value = [];
    return;
  }
  
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/d/${newVal}?depth=2`);
    const data = await res.json();
    dsXaPhuong.value = data.wards.map((w: any) => ({
      value: w.code.toString(),
      label: w.name
    }));
  } catch (e) {
    console.error("Lỗi tải xã phường", e);
  }
});
function layLabel(options: any[], value: string) {
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

async function apDungMaDiaChiDaQuet(duLieuQr: Record<string, any>) {
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
    const responseTinh = await fetch(`https://provinces.open-api.vn/api/p/${maTinhThanh}?depth=2`);
    const dataTinh = await responseTinh.json();
    dsQuanHuyen.value = Array.isArray(dataTinh.districts)
      ? dataTinh.districts.map((district: any) => ({
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
    const responseHuyen = await fetch(`https://provinces.open-api.vn/api/d/${maQuanHuyen}?depth=2`);
    const dataHuyen = await responseHuyen.json();
    dsXaPhuong.value = Array.isArray(dataHuyen.wards)
      ? dataHuyen.wards.map((ward: any) => ({
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
    const data = await layChiTietNhanVien(id!);
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
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải thông tin nhân viên");
  } finally {
    dangTai.value = false;
  }
}

async function luu() {
  loiForm.value = { hoTen: "", email: "", sdt: "", cccd: "" };
  let hasError = false;

  if (!form.value.hoTen.trim()) {
    loiForm.value.hoTen = "Vui lòng nhập họ và tên nhân viên.";
    hasError = true;
  }

  if (!form.value.email.trim()) {
    loiForm.value.email = "Vui lòng nhập email nhân viên.";
    hasError = true;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    loiForm.value.email = "Email nhân viên chưa đúng định dạng.";
    hasError = true;
  }

  if (!/^\d{10}$/.test(form.value.sdt.trim())) {
    loiForm.value.sdt = "Số điện thoại phải gồm đúng 10 chữ số.";
    hasError = true;
  }

  if (hasError) return;

  dangLuu.value = true;
  loiTrang.value = "";
  thongBao.value = "";

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
      await taoNhanVien(payload);
      router.push({ name: "admin-nhan-vien" });
      return;
    }

    const updated = await capNhatNhanVien(id!, payload);
    nhanVien.value = updated;
    syncCurrentAdminCccd(updated);
    if (route.query.requireCccd === "1" && /^\d{12}$/.test(String(updated.cccd ?? ""))) {
      const redirectPath = typeof route.query.redirect === "string" ? route.query.redirect : "/admin";
      router.push(redirectPath.startsWith("/admin") ? redirectPath : "/admin");
      return;
    }
    thongBao.value = "Đã lưu thay đổi thành công.";
    setTimeout(() => {
      thongBao.value = "";
    }, 3000);
  } catch (error) {
    Object.assign(loiForm.value, getFieldErrors(error));
    loiTrang.value = getDisplayErrorMessage(
      error,
      laMoi ? "Không thể tạo nhân viên" : "Không thể cập nhật nhân viên",
    );
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
    await doiMatKhauNhanVien(id!, matKhauMoi.value.trim());
    thongBao.value = "Đã đổi mật khẩu thành công.";
    matKhauMoi.value = "";
    showDoiMatKhau.value = false;
    setTimeout(() => {
      thongBao.value = "";
    }, 3000);
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể đổi mật khẩu nhân viên");
  } finally {
    dangLuu.value = false;
  }
}

async function doiTrangThai(trangThai: number) {
  try {
    const updated = await doiTrangThaiNhanVien(id!, trangThai);
    nhanVien.value = updated;
    thongBao.value = trangThai === 1 ? "Đã kích hoạt tài khoản." : "Đã khóa tài khoản.";
    setTimeout(() => {
      thongBao.value = "";
    }, 3000);
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể cập nhật trạng thái nhân viên");
  }
}

async function xoaNhanVienHienTai() {
  if (!window.confirm("Bạn có chắc chắn muốn xóa nhân viên này không?")) return;
  try {
    await xoaNhanVien(id!);
    router.push({ name: "admin-nhan-vien" });
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể xóa nhân viên");
  }
}

async function xuLyUploadAnh(event: Event) {
  const target = event.target as HTMLInputElement;
  if (!target.files?.length) return;

  dangUpload.value = true;
  loiTrang.value = "";
  try {
    const url = await uploadFile(target.files[0]);
    form.value.hinhAnh = url;
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải ảnh nhân viên");
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
</script>

<template>
  <div class="space-y-6">
    <section class="flex items-center gap-4">
      <div class="flex items-center gap-4">
        <button
          type="button"
          @click="router.push({ name: 'admin-nhan-vien' })"
          class="flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-slate-700 transition hover:bg-slate-200"
        >
          <ArrowLeft class="h-5 w-5" />
        </button>
        <h1 class="text-[28px] font-bold tracking-tight text-slate-900">
          {{ laMoi ? "Thêm nhân viên mới" : "Chi tiết nhân viên" }}
        </h1>
      </div>

    </section>

    <div
      v-if="dangTai"
      class="rounded-[28px] border border-slate-200 bg-white px-6 py-16 text-center text-sm text-slate-400 shadow-sm"
    >
      Đang tải thông tin nhân viên...
    </div>

    <template v-else>
      <!-- Toast quét QR thành công - hiển thị cố định trên cùng -->
      <Teleport to="body">
        <Transition enter-active-class="transition duration-300 ease-out" enter-from-class="opacity-0 -translate-y-3" enter-to-class="opacity-100 translate-y-0" leave-active-class="transition duration-200 ease-in" leave-from-class="opacity-100 translate-y-0" leave-to-class="opacity-0 -translate-y-3">
          <div v-if="thongBaoQrOk" class="fixed top-5 left-1/2 z-[9999] -translate-x-1/2 flex items-center gap-2.5 rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-3 text-[13px] font-medium text-emerald-700 shadow-lg">
            {{ thongBaoQrOk }}
          </div>
        </Transition>
      </Teleport>
      <div
        v-if="thongBao"
        class="rounded-[20px] border border-emerald-100 bg-emerald-50 px-5 py-3 text-sm font-medium text-emerald-700"
      >
        {{ thongBao }}
      </div>
      <div
        v-if="loiTrang"
        class="rounded-[20px] border border-rose-100 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-700"
      >
        {{ loiTrang }}
      </div>

      <div class="grid gap-6 xl:grid-cols-[380px_minmax(0,1fr)]">
        <section class="rounded-[28px] border border-slate-200 bg-white px-8 py-9 shadow-sm">
          <div class="flex items-center justify-between">
            <h2 class="text-base font-bold text-slate-800">Thông tin nhân viên</h2>
          </div>
          <div class="mt-7 h-px bg-slate-200"></div>

          <div class="pt-10">
            <div class="flex justify-center">
              <button
                type="button"
                @click="fileInputAvatar?.click()"
                class="relative flex h-[194px] w-[194px] items-center justify-center overflow-hidden rounded-full border-2 border-dashed border-slate-200 bg-slate-50 text-center transition hover:border-slate-300 hover:bg-slate-100"
              >
                <img
                  v-if="form.hinhAnh"
                  :src="form.hinhAnh"
                  alt="Ảnh nhân viên"
                  class="h-full w-full object-cover"
                />
                <span v-else class="text-[18px] font-medium text-slate-400">Chọn ảnh</span>
                <span
                  v-if="dangUpload"
                  class="absolute inset-0 flex items-center justify-center bg-white/80 text-sm font-semibold text-slate-600"
                >
                  Đang tải...
                </span>
              </button>
              <input
                ref="fileInputAvatar"
                type="file"
                accept="image/*,.jpg,.jpeg,.png,.gif,.webp"
                class="hidden"
                @change="xuLyUploadAnh"
              />
            </div>

            <label class="mt-8 block space-y-1.5">
              <span class="text-[13px] font-semibold text-slate-500">Họ và tên <span class="text-rose-500">*</span></span>
              <input
                v-model="form.hoTen"
                type="text"
                placeholder="Nhập họ và tên"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:bg-white',
                  loiForm.hoTen ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-rose-300',
                ]"
              />
              <p v-if="loiForm.hoTen" class="text-xs text-rose-500">{{ loiForm.hoTen }}</p>
            </label>
          </div>
        </section>

        <section class="rounded-[28px] border border-slate-200 bg-white px-8 py-9 shadow-sm">
          <h2 class="text-base font-bold text-slate-800">Thông tin chi tiết</h2>
          <div class="mt-7 h-px bg-slate-200"></div>

          <!-- QR Scanner Modal -->
          <Teleport to="body">
            <div v-show="dangQuet" class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm">
              <div class="relative w-full max-w-lg rounded-[28px] bg-white p-6 shadow-2xl mx-4">

                <!-- Header -->
                <div class="mb-4 flex items-center justify-between">
                  <div>
                    <h3 class="text-[18px] font-bold text-slate-900">Quét QR</h3>
                  </div>
                  <button type="button" @click="dungQuet"
                    class="flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
                    <X class="h-4 w-4" />
                  </button>
                </div>

                <!-- Camera -->
                <div>
                  <div class="relative overflow-hidden rounded-[18px] bg-black" style="aspect-ratio:4/3">
                    <video ref="videoRef" class="h-full w-full object-cover" autoplay playsinline muted></video>
                    <!-- Khung quét overlay -->
                    <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                      <div class="relative h-48 w-64">
                        <span class="absolute top-0 left-0 h-8 w-8 border-t-[3px] border-l-[3px] border-white rounded-tl-md"></span>
                        <span class="absolute top-0 right-0 h-8 w-8 border-t-[3px] border-r-[3px] border-white rounded-tr-md"></span>
                        <span class="absolute bottom-0 left-0 h-8 w-8 border-b-[3px] border-l-[3px] border-white rounded-bl-md"></span>
                        <span class="absolute bottom-0 right-0 h-8 w-8 border-b-[3px] border-r-[3px] border-white rounded-br-md"></span>
                        <div class="scan-line"></div>
                      </div>
                    </div>
                  </div>
                  <p v-if="!loiCamera" class="mt-3 text-center text-[13px] text-slate-500">🔍 Đang tìm mã QR... Giữ camera ổn định</p>
                </div>

                <!-- Lỗi chung -->
                <div v-if="loiCamera" class="mt-4 rounded-[14px] bg-rose-50 px-4 py-3 text-[13px] font-medium text-rose-600">
                  ⚠️ {{ loiCamera }}
                </div>
              </div>
            </div>
          </Teleport>

          <!-- CCCD Field -->
          <div class="mt-6 space-y-1.5">
            <span class="text-[13px] font-semibold text-slate-500">Số CCCD</span>
            <div class="flex items-center gap-3 flex-wrap">
              <button
                type="button"
                @click="batDauQuet"
                :class="[
                  'flex h-11 items-center gap-2 rounded-2xl border px-4 text-sm font-semibold transition whitespace-nowrap',
                  loiForm.cccd ? 'border-rose-400 bg-rose-50 text-rose-600 hover:bg-rose-100' : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                ]"
              >
                <ScanLine class="h-4 w-4" />
                {{ form.cccd ? 'Quét lại CCCD' : 'Quét mã CCCD' }}
              </button>
            </div>
            <p v-if="loiForm.cccd" class="text-xs text-rose-500">{{ loiForm.cccd }}</p>
          </div>

          <div class="mt-5 grid gap-x-5 gap-y-4 xl:grid-cols-12">
            <div class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Giới tính <span class="text-rose-500">*</span></span>
              <div class="flex h-11 items-center gap-6 px-1 text-sm text-slate-700">
                <label class="inline-flex items-center gap-2">
                  <input v-model="form.gioiTinh" type="radio" value="Nam" class="h-4 w-4 accent-rose-500" />
                  <span>Nam</span>
                </label>
                <label class="inline-flex items-center gap-2">
                  <input v-model="form.gioiTinh" type="radio" value="Nữ" class="h-4 w-4 accent-rose-500" />
                  <span>Nữ</span>
                </label>
              </div>
            </div>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Ngày sinh <span class="text-rose-500">*</span></span>
              <input
                v-model="form.ngaySinh"
                type="date"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
              />
            </label>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Email <span class="text-rose-500">*</span></span>
              <input
                v-model="form.email"
                type="email"
                placeholder="Nhập email"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:bg-white',
                  loiForm.email ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-rose-300',
                ]"
              />
              <p v-if="loiForm.email" class="text-xs text-rose-500">{{ loiForm.email }}</p>
            </label>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
              <select
                v-model="form.tinhThanh"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
              >
                <option value="">Chọn tỉnh thành</option>
                <option v-for="item in dsTinhThanh" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Quận/Huyện <span class="text-rose-500">*</span></span>
              <select
                v-model="form.quanHuyen"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
              >
                <option value="">Chọn quận huyện</option>
                <option v-for="item in dsQuanHuyen" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-1.5 xl:col-span-4">
              <span class="text-[13px] font-semibold text-slate-500">Xã/Phường/Thị trấn <span class="text-rose-500">*</span></span>
              <select
                v-model="form.xaPhuong"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
              >
                <option value="">Chọn xã phường</option>
                <option v-for="item in dsXaPhuong" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-1.5 xl:col-span-6">
              <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
              <input
                v-model="form.sdt"
                type="tel"
                placeholder="Nhập số điện thoại"
                :class="[
                  'h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:bg-white',
                  loiForm.sdt ? 'border-rose-400 focus:border-rose-400' : 'border-slate-200 focus:border-rose-300',
                ]"
              />
              <p v-if="loiForm.sdt" class="text-xs text-rose-500">{{ loiForm.sdt }}</p>
            </label>

            <label class="space-y-1.5 xl:col-span-6">
              <span class="text-[13px] font-semibold text-slate-500">Vai trò <span class="text-rose-500">*</span></span>
              <select
                v-model="form.vaiTro"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition focus:border-rose-300 focus:bg-white"
              >
                <option v-for="item in dsVaiTro" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-1.5 xl:col-span-12">
              <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
              <input
                v-model="form.diaChiCuThe"
                type="text"
                placeholder="Nhập địa chỉ cụ thể"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
              />
            </label>
          </div>
        </section>
      </div>

      <!-- Buttons cuối trang -->
      <div class="flex flex-wrap items-center justify-end gap-3 rounded-[28px] border border-slate-200 bg-white px-8 py-5 shadow-sm">
        <button
          type="button"
          @click="router.push({ name: 'admin-nhan-vien' })"
          class="admin-btn-soft h-12 rounded-[18px] px-8 text-[15px] font-semibold"
        >
          Hủy
        </button>
        <button
          @click="luu"
          :disabled="dangLuu"
          class="admin-btn-primary h-12 rounded-[18px] px-8 text-[15px] font-semibold"
        >
          <Save class="h-4 w-4" />
          {{ dangLuu ? "Đang lưu..." : laMoi ? "Tạo nhân viên" : "Lưu thay đổi" }}
        </button>
      </div>

      <section v-if="!laMoi" class="grid gap-6 xl:grid-cols-2">
        <div class="rounded-[28px] border border-slate-200 bg-white px-8 py-7 shadow-sm">
          <h3 class="text-[18px] font-bold text-slate-900">Đổi mật khẩu</h3>
          <div class="mt-6 space-y-4">
            <template v-if="showDoiMatKhau">
              <input
                v-model="matKhauMoi"
                type="password"
                placeholder="Mật khẩu mới tối thiểu 6 ký tự"
                class="h-12 w-full rounded-[18px] border border-slate-200 bg-white px-5 text-[16px] text-slate-700 outline-none focus:border-slate-300"
              />
              <div class="flex gap-3">
                <button @click="doiMatKhau" :disabled="dangLuu" class="admin-btn-primary h-11 rounded-[16px] px-5">
                  Xác nhận
                </button>
                <button
                  type="button"
                  @click="showDoiMatKhau = false; matKhauMoi = ''"
                  class="admin-btn-soft h-11 rounded-[16px] px-5"
                >
                  Hủy
                </button>
              </div>
            </template>
            <button
              v-else
              type="button"
              @click="showDoiMatKhau = true"
              class="admin-btn-soft h-11 rounded-[16px] px-5"
            >
              Đổi mật khẩu
            </button>
          </div>
        </div>

        <div class="rounded-[28px] border border-slate-200 bg-white px-8 py-7 shadow-sm">
          <h3 class="text-[18px] font-bold text-slate-900">Trạng thái tài khoản</h3>
          <div class="mt-6 flex flex-wrap gap-3">
            <button
              v-if="nhanVien?.trangThai === 1"
              type="button"
              @click="doiTrangThai(0)"
              class="h-11 rounded-[16px] bg-rose-50 px-5 text-sm font-semibold text-rose-600 transition hover:bg-rose-100"
            >
              Khóa tài khoản
            </button>
            <button
              v-else
              type="button"
              @click="doiTrangThai(1)"
              class="h-11 rounded-[16px] bg-emerald-50 px-5 text-sm font-semibold text-emerald-600 transition hover:bg-emerald-100"
            >
              Kích hoạt tài khoản
            </button>
            <button
              type="button"
              @click="xoaNhanVienHienTai"
              class="h-11 rounded-[16px] border border-rose-200 bg-white px-5 text-sm font-semibold text-rose-600 transition hover:bg-rose-50"
            >
              Xóa nhân viên
            </button>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #38bdf8, #0ea5e9, #38bdf8, transparent);
  border-radius: 2px;
  animation: scanMove 2s linear infinite;
  box-shadow: 0 0 8px 2px rgba(56, 189, 248, 0.6);
}

@keyframes scanMove {
  0%   { top: 0%; opacity: 1; }
  48%  { top: 100%; opacity: 1; }
  50%  { top: 100%; opacity: 0; }
  52%  { top: 0%;   opacity: 0; }
  54%  { top: 0%;   opacity: 1; }
  100% { top: 100%; opacity: 1; }
}
</style>
