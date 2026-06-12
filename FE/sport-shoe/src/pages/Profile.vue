<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Save, User, Mail, Phone, Calendar, Lock, Pencil, Plus, Trash2, ShieldCheck, Landmark, MapPin, CheckCircle2, X } from "lucide-vue-next";
import { layKhachId } from "../services/gio-hang";
import {
  layProfileKhachHang,
  capNhatProfileKhachHang,
  doiMatKhauProfileKhachHang,
  layDanhSachDiaChiProfile,
  themDiaChiProfile,
  capNhatDiaChiProfile,
  xoaDiaChiProfile,
  datMacDinhDiaChiProfile,
  layDanhSachTaiKhoanNganHang,
  themTaiKhoanNganHang,
  capNhatTaiKhoanNganHang,
  xoaTaiKhoanNganHang,
  datMacDinhTaiKhoanNganHang
} from "../services/client-profile";
import { getDisplayErrorMessage, getFieldErrors } from "../utils/error-message";
import { showConfirm, showSuccess, showError } from "../utils/alert";

const router = useRouter();
const khachHangId = layKhachId();

if (!khachHangId) {
  showError("Vui lòng đăng nhập để xem thông tin cá nhân.");
  router.push("/login");
}

const dangTai = ref(false);
const dangLuu = ref(false);
const dangLuuNganHang = ref(false);
const loiTrang = ref("");
const tabHienTai = ref("thongTin"); // 'thongTin' | 'nganHang'

// Profile Form State
const form = ref({
  hoTen: "",
  tenDangNhap: "",
  email: "",
  sdt: "",
  gioiTinh: 1, // 0 = Nữ, 1 = Nam, 2 = Khác
  ngaySinh: ""
});

const loiForm = ref({
  hoTen: "",
  email: "",
  sdt: ""
});

// Address book state
const dsDiaChi = ref<any[]>([]);
const dangTaiDiaChi = ref(false);
const dangLuuDiaChi = ref(false);
const hienFormDiaChi = ref(false);
const diaChiDangSua = ref<any>(null);
const dsTinh = ref<any[]>([]);
const dsHuyen = ref<any[]>([]);
const dsXa = ref<any[]>([]);
const maTinhChon = ref<number | null>(null);
const maHuyenChon = ref<number | null>(null);
const formDiaChi = ref({
  hoTen: "",
  sdt: "",
  tinhThanh: "",
  quanHuyen: "",
  phuongXa: "",
  diaChiCuThe: "",
  laMacDinh: false
});
const loiDiaChi = ref({
  hoTen: "",
  sdt: "",
  tinhThanh: "",
  quanHuyen: "",
  phuongXa: "",
  diaChiCuThe: ""
});

// Password State
const showDoiMatKhau = ref(false);
const matKhauCu = ref("");
const matKhauMoi = ref("");
const loiMatKhau = ref("");

// Bank Accounts State
const dsTaiKhoan = ref<any[]>([]);
const dsNganHang = ref<any[]>([]);
const loadingNganHang = ref(false);

// Add Bank Modal State
const showModalNganHang = ref(false);
const editBankId = ref<number | null>(null);
const searchNganHangText = ref("");
const dropdownNganHangMo = ref(false);
const bankForm = ref({
  tenNganHang: "",
  soTaiKhoan: "",
  tenChuTaiKhoan: "",
  chiNhanh: "",
  laMacDinh: false
});

const bankFormErrors = ref({
  tenNganHang: "",
  soTaiKhoan: "",
  tenChuTaiKhoan: ""
});

const filteredBanks = computed(() => {
  const q = searchNganHangText.value.toLowerCase().trim();
  if (!q) return dsNganHang.value;
  return dsNganHang.value.filter(bank => 
    bank.shortName.toLowerCase().includes(q) || 
    bank.name.toLowerCase().includes(q) ||
    bank.code.toLowerCase().includes(q)
  );
});

// Functions
async function taiProfile() {
  if (!khachHangId) return;
  dangTai.value = true;
  try {
    const data = await layProfileKhachHang(khachHangId);
    form.value = {
      hoTen: data.hoTen ?? "",
      tenDangNhap: data.tenDangNhap ?? "",
      email: data.email ?? "",
      sdt: data.sdt ?? "",
      gioiTinh: data.gioiTinh ?? 1,
      ngaySinh: data.ngaySinh ?? ""
    };
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải thông tin cá nhân");
  } finally {
    dangTai.value = false;
  }
}

async function taiDanhSachDiaChi() {
  if (!khachHangId) return;
  dangTaiDiaChi.value = true;
  try {
    dsDiaChi.value = await layDanhSachDiaChiProfile(khachHangId);
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể tải danh sách địa chỉ"));
    dsDiaChi.value = [];
  } finally {
    dangTaiDiaChi.value = false;
  }
}

async function taiDanhSachTinh() {
  if (dsTinh.value.length) return;
  try {
    const response = await fetch("https://provinces.open-api.vn/api/p/");
    dsTinh.value = await response.json();
  } catch {
    dsTinh.value = [];
    showError("Không thể tải danh sách tỉnh/thành phố. Vui lòng thử lại.");
  }
}

async function chonTinh(code: number | null) {
  maTinhChon.value = code;
  maHuyenChon.value = null;
  dsHuyen.value = [];
  dsXa.value = [];
  formDiaChi.value.tinhThanh = dsTinh.value.find((item) => item.code === code)?.name ?? "";
  formDiaChi.value.quanHuyen = "";
  formDiaChi.value.phuongXa = "";
  if (!code) return;

  try {
    const response = await fetch(`https://provinces.open-api.vn/api/p/${code}?depth=2`);
    const data = await response.json();
    dsHuyen.value = data.districts ?? [];
  } catch {
    dsHuyen.value = [];
    showError("Không thể tải danh sách quận/huyện. Vui lòng thử lại.");
  }
}

async function chonHuyen(code: number | null) {
  maHuyenChon.value = code;
  dsXa.value = [];
  formDiaChi.value.quanHuyen = dsHuyen.value.find((item) => item.code === code)?.name ?? "";
  formDiaChi.value.phuongXa = "";
  if (!code) return;

  try {
    const response = await fetch(`https://provinces.open-api.vn/api/d/${code}?depth=2`);
    const data = await response.json();
    dsXa.value = data.wards ?? [];
  } catch {
    dsXa.value = [];
    showError("Không thể tải danh sách phường/xã. Vui lòng thử lại.");
  }
}

async function dienDanhSachDiaPhuong(diaChi: any) {
  await taiDanhSachTinh();
  const tinh = dsTinh.value.find((item) => item.name === diaChi.tinhThanh);
  if (!tinh) return;

  maTinhChon.value = tinh.code;
  const tinhResponse = await fetch(`https://provinces.open-api.vn/api/p/${tinh.code}?depth=2`);
  const tinhData = await tinhResponse.json();
  dsHuyen.value = tinhData.districts ?? [];

  const huyen = dsHuyen.value.find((item) => item.name === diaChi.quanHuyen);
  if (!huyen) return;

  maHuyenChon.value = huyen.code;
  const huyenResponse = await fetch(`https://provinces.open-api.vn/api/d/${huyen.code}?depth=2`);
  const huyenData = await huyenResponse.json();
  dsXa.value = huyenData.wards ?? [];
}

function resetLoiDiaChi() {
  loiDiaChi.value = {
    hoTen: "",
    sdt: "",
    tinhThanh: "",
    quanHuyen: "",
    phuongXa: "",
    diaChiCuThe: ""
  };
}

async function moThemDiaChi() {
  diaChiDangSua.value = null;
  formDiaChi.value = {
    hoTen: form.value.hoTen || "",
    sdt: form.value.sdt || "",
    tinhThanh: "",
    quanHuyen: "",
    phuongXa: "",
    diaChiCuThe: "",
    laMacDinh: dsDiaChi.value.length === 0
  };
  resetLoiDiaChi();
  maTinhChon.value = null;
  maHuyenChon.value = null;
  dsHuyen.value = [];
  dsXa.value = [];
  hienFormDiaChi.value = true;
  await taiDanhSachTinh();
}

async function moSuaDiaChi(diaChi: any) {
  diaChiDangSua.value = diaChi;
  formDiaChi.value = {
    hoTen: diaChi.hoTen ?? "",
    sdt: diaChi.sdt ?? "",
    tinhThanh: diaChi.tinhThanh ?? "",
    quanHuyen: diaChi.quanHuyen ?? "",
    phuongXa: diaChi.phuongXa ?? "",
    diaChiCuThe: diaChi.diaChiCuThe ?? "",
    laMacDinh: Boolean(diaChi.laMacDinh)
  };
  resetLoiDiaChi();
  hienFormDiaChi.value = true;
  try {
    await dienDanhSachDiaPhuong(diaChi);
  } catch {
    showError("Không thể tải dữ liệu địa phương của địa chỉ này.");
  }
}

function huyFormDiaChi() {
  hienFormDiaChi.value = false;
  diaChiDangSua.value = null;
  resetLoiDiaChi();
}

function validateDiaChi() {
  resetLoiDiaChi();
  let hopLe = true;
  const diaChi = formDiaChi.value;

  if (!diaChi.hoTen.trim()) {
    loiDiaChi.value.hoTen = "Vui lòng nhập họ tên người nhận.";
    hopLe = false;
  }
  if (!/^(0|\+84)[35789]\d{8}$/.test(diaChi.sdt.trim())) {
    loiDiaChi.value.sdt = "Số điện thoại không đúng định dạng.";
    hopLe = false;
  }
  if (!diaChi.tinhThanh) {
    loiDiaChi.value.tinhThanh = "Vui lòng chọn tỉnh/thành phố.";
    hopLe = false;
  }
  if (!diaChi.quanHuyen) {
    loiDiaChi.value.quanHuyen = "Vui lòng chọn quận/huyện.";
    hopLe = false;
  }
  if (!diaChi.phuongXa) {
    loiDiaChi.value.phuongXa = "Vui lòng chọn phường/xã.";
    hopLe = false;
  }
  if (!diaChi.diaChiCuThe.trim()) {
    loiDiaChi.value.diaChiCuThe = "Vui lòng nhập địa chỉ cụ thể.";
    hopLe = false;
  }
  return hopLe;
}

function taoPayloadDiaChi() {
  return {
    hoTen: formDiaChi.value.hoTen.trim(),
    sdt: formDiaChi.value.sdt.trim(),
    tinhThanh: formDiaChi.value.tinhThanh,
    quanHuyen: formDiaChi.value.quanHuyen,
    phuongXa: formDiaChi.value.phuongXa,
    diaChiCuThe: formDiaChi.value.diaChiCuThe.trim(),
    laMacDinh: Boolean(formDiaChi.value.laMacDinh)
  };
}

async function luuDiaChi() {
  if (!khachHangId || !validateDiaChi()) return;
  dangLuuDiaChi.value = true;
  try {
    if (diaChiDangSua.value) {
      await capNhatDiaChiProfile(khachHangId, diaChiDangSua.value.id, taoPayloadDiaChi());
      showSuccess("Cập nhật địa chỉ thành công.", "Thành công");
    } else {
      await themDiaChiProfile(khachHangId, taoPayloadDiaChi());
      showSuccess("Thêm địa chỉ thành công.", "Thành công");
    }
    await taiDanhSachDiaChi();
    huyFormDiaChi();
  } catch (error) {
    Object.assign(loiDiaChi.value, getFieldErrors(error));
    showError(getDisplayErrorMessage(error, "Không thể lưu địa chỉ"));
  } finally {
    dangLuuDiaChi.value = false;
  }
}

async function xoaDiaChi(diaChiId: number) {
  if (!khachHangId) return;
  const confirmed = await showConfirm(
    "Bạn có chắc chắn muốn xóa địa chỉ này?",
    "Xóa địa chỉ",
    "Xóa"
  );
  if (!confirmed) return;

  try {
    await xoaDiaChiProfile(khachHangId, diaChiId);
    await taiDanhSachDiaChi();
    showSuccess("Xóa địa chỉ thành công.", "Thành công");
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể xóa địa chỉ"));
  }
}

async function datDiaChiMacDinh(diaChiId: number) {
  if (!khachHangId) return;
  try {
    await datMacDinhDiaChiProfile(khachHangId, diaChiId);
    await taiDanhSachDiaChi();
    showSuccess("Đã đặt làm địa chỉ mặc định.", "Thành công");
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể đặt địa chỉ mặc định"));
  }
}

async function taiDanhSachNganHang() {
  if (!khachHangId) return;
  try {
    dsTaiKhoan.value = await layDanhSachTaiKhoanNganHang(khachHangId);
  } catch (error) {
    console.error("Lỗi khi tải danh sách tài khoản ngân hàng", error);
  }
}

async function taiVietQrBanks() {
  loadingNganHang.value = true;
  try {
    const res = await fetch("https://api.vietqr.io/v2/banks");
    const json = await res.json();
    if (json.code === "00") {
      dsNganHang.value = json.data;
    }
  } catch (e) {
    console.error("Không thể tải danh sách ngân hàng từ VietQR", e);
  } finally {
    loadingNganHang.value = false;
  }
}

async function luuProfile() {
  if (!khachHangId) return;
  loiForm.value = { hoTen: "", email: "", sdt: "" };
  let coLoi = false;

  if (!form.value.hoTen.trim()) {
    loiForm.value.hoTen = "Họ và tên không được để trống";
    coLoi = true;
  }
  if (!form.value.email.trim()) {
    loiForm.value.email = "Email không được để trống";
    coLoi = true;
  }
  if (coLoi) return;

  dangLuu.value = true;
  try {
    const updated = await capNhatProfileKhachHang(khachHangId, {
      hoTen: form.value.hoTen.trim(),
      email: form.value.email.trim(),
      sdt: form.value.sdt.trim() || undefined,
      gioiTinh: Number(form.value.gioiTinh),
      ngaySinh: form.value.ngaySinh || undefined
    });

    // Cập nhật lại session storage cho khách hàng
    const userRaw = localStorage.getItem("user");
    if (userRaw) {
      const user = JSON.parse(userRaw);
      localStorage.setItem("user", JSON.stringify({ ...user, ...updated }));
    }

    showSuccess("Cập nhật thông tin thành công.", "Thành công");
  } catch (error) {
    Object.assign(loiForm.value, getFieldErrors(error));
    showError(getDisplayErrorMessage(error, "Không thể cập nhật thông tin cá nhân"));
  } finally {
    dangLuu.value = false;
  }
}

async function handleDoiMatKhau() {
  if (!khachHangId) return;
  loiMatKhau.value = "";

  if (!matKhauCu.value) {
    loiMatKhau.value = "Vui lòng nhập mật khẩu cũ.";
    return;
  }
  if (!matKhauMoi.value || matKhauMoi.value.length < 6) {
    loiMatKhau.value = "Mật khẩu mới phải có ít nhất 6 ký tự.";
    return;
  }

  dangLuu.value = true;
  try {
    await doiMatKhauProfileKhachHang(khachHangId, {
      matKhauCu: matKhauCu.value,
      matKhauMoi: matKhauMoi.value
    });
    showSuccess("Đổi mật khẩu thành công.", "Thành công");
    matKhauCu.value = "";
    matKhauMoi.value = "";
    showDoiMatKhau.value = false;
  } catch (error) {
    loiMatKhau.value = getDisplayErrorMessage(error, "Không thể đổi mật khẩu");
  } finally {
    dangLuu.value = false;
  }
}

// Bank account actions
function moModalThemNganHang() {
  editBankId.value = null;
  searchNganHangText.value = "";
  bankForm.value = {
    tenNganHang: "",
    soTaiKhoan: "",
    tenChuTaiKhoan: form.value.hoTen.toUpperCase(),
    chiNhanh: "",
    laMacDinh: dsTaiKhoan.value.length === 0
  };
  bankFormErrors.value = {
    tenNganHang: "",
    soTaiKhoan: "",
    tenChuTaiKhoan: ""
  };
  showModalNganHang.value = true;
}

function moModalSuaNganHang(taiKhoan: any) {
  editBankId.value = taiKhoan.id;
  searchNganHangText.value = taiKhoan.tenNganHang;
  bankForm.value = {
    tenNganHang: taiKhoan.tenNganHang,
    soTaiKhoan: taiKhoan.soTaiKhoan,
    tenChuTaiKhoan: taiKhoan.tenChuTaiKhoan,
    chiNhanh: taiKhoan.chiNhanh || "",
    laMacDinh: Boolean(taiKhoan.laMacDinh),
  };
  bankFormErrors.value = {
    tenNganHang: "",
    soTaiKhoan: "",
    tenChuTaiKhoan: "",
  };
  showModalNganHang.value = true;
}

function selectBank(bank: any) {
  bankForm.value.tenNganHang = bank.shortName;
  searchNganHangText.value = `${bank.shortName} - ${bank.name}`;
  dropdownNganHangMo.value = false;
}

async function luuNganHang() {
  if (!khachHangId) return;
  bankFormErrors.value = { tenNganHang: "", soTaiKhoan: "", tenChuTaiKhoan: "" };
  let coLoi = false;

  if (!bankForm.value.tenNganHang) {
    bankFormErrors.value.tenNganHang = "Vui lòng chọn ngân hàng.";
    coLoi = true;
  }
  if (!bankForm.value.soTaiKhoan.trim()) {
    bankFormErrors.value.soTaiKhoan = "Vui lòng nhập số tài khoản.";
    coLoi = true;
  }
  if (!bankForm.value.tenChuTaiKhoan.trim()) {
    bankFormErrors.value.tenChuTaiKhoan = "Vui lòng nhập tên chủ tài khoản.";
    coLoi = true;
  }

  if (coLoi) return;

  dangLuuNganHang.value = true;
  try {
    const payload = {
      tenNganHang: bankForm.value.tenNganHang,
      soTaiKhoan: bankForm.value.soTaiKhoan.trim(),
      tenChuTaiKhoan: bankForm.value.tenChuTaiKhoan.trim(),
      chiNhanh: bankForm.value.chiNhanh.trim() || undefined,
      laMacDinh: bankForm.value.laMacDinh
    };

    if (editBankId.value) {
      await capNhatTaiKhoanNganHang(khachHangId, editBankId.value, payload);
      showSuccess("Cập nhật tài khoản ngân hàng thành công.", "Thành công");
    } else {
      await themTaiKhoanNganHang(khachHangId, payload);
      showSuccess("Thêm tài khoản ngân hàng thành công.", "Thành công");
    }
    showModalNganHang.value = false;
    await taiDanhSachNganHang();
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể lưu tài khoản ngân hàng"));
  } finally {
    dangLuuNganHang.value = false;
  }
}

async function handleXoaNganHang(id: number) {
  if (!khachHangId) return;
  const confirmed = await showConfirm(
    "Bạn có chắc chắn muốn xóa liên kết tài khoản ngân hàng này?",
    "Xóa tài khoản ngân hàng",
    "Xóa tài khoản",
  );
  if (!confirmed) return;
  try {
    await xoaTaiKhoanNganHang(khachHangId, id);
    showSuccess("Đã xóa tài khoản ngân hàng thành công.", "Thành công");
    await taiDanhSachNganHang();
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể xóa tài khoản ngân hàng"));
  }
}

async function handleDatMacDinhNganHang(id: number) {
  if (!khachHangId) return;
  try {
    await datMacDinhTaiKhoanNganHang(khachHangId, id);
    showSuccess("Đã đặt tài khoản mặc định.", "Thành công");
    await taiDanhSachNganHang();
  } catch (error) {
    showError(getDisplayErrorMessage(error, "Không thể thiết lập mặc định"));
  }
}

function layLogoNganHang(tenNganHang: string) {
  if (!tenNganHang || dsNganHang.value.length === 0) return null;
  const bank = dsNganHang.value.find(b => 
    b.shortName.toLowerCase() === tenNganHang.toLowerCase() ||
    b.code.toLowerCase() === tenNganHang.toLowerCase()
  );
  return bank ? bank.logo : null;
}

onMounted(() => {
  taiProfile();
  taiDanhSachDiaChi();
  taiDanhSachNganHang();
  taiVietQrBanks();
});
</script>

<template>
  <div class="mx-auto max-w-6xl px-4 py-8 space-y-6 pb-20">
    <!-- Breadcrumb -->
    <div class="flex items-center gap-2 text-xs font-semibold text-slate-500">
      <router-link to="/" class="hover:text-primary transition">Trang chủ</router-link>
      <span>/</span>
      <span class="text-slate-800">Tài khoản</span>
    </div>

    <!-- Layout: Header -->
    <section class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-slate-100 pb-6">
      <div>
        <h1 class="text-3xl font-extrabold tracking-tight text-slate-900">
          Thông tin cá nhân
        </h1>
        <p class="text-sm text-slate-500 mt-1">Cập nhật thông tin cá nhân, bảo mật và tài khoản ngân hàng liên kết thụ hưởng.</p>
      </div>
      <button
        v-if="tabHienTai === 'thongTin'"
        @click="luuProfile"
        :disabled="dangLuu"
        class="inline-flex items-center justify-center gap-2 h-11 px-6 rounded-2xl bg-[#B82220] hover:bg-[#a11a19] text-white text-sm font-bold shadow-lg shadow-rose-100 transition duration-300 disabled:opacity-50 disabled:scale-100 active:scale-95"
      >
        <Save class="h-4 w-4" />
        {{ dangLuu ? "Đang lưu..." : "Lưu thay đổi" }}
      </button>
    </section>

    <div v-if="loiTrang" class="rounded-2xl border border-rose-100 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-700">
      {{ loiTrang }}
    </div>

    <!-- Main Content Grid -->
    <div class="grid gap-8 lg:grid-cols-4">
      <!-- Left side: Navigation / User info card -->
      <div class="lg:col-span-1 space-y-6">
        <!-- Avatar card -->
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm flex flex-col items-center">
          <div class="h-28 w-28 overflow-hidden rounded-full border-4 border-slate-50 ring-1 ring-slate-200 bg-slate-100 flex items-center justify-center">
            <img
              :src="'https://ui-avatars.com/api/?name=' + encodeURIComponent(form.hoTen) + '&background=B82220&color=ffffff&size=256'"
              alt="Avatar"
              class="h-full w-full object-cover"
            />
          </div>
          <h2 class="mt-4 text-lg font-bold text-slate-800 text-center">{{ form.hoTen }}</h2>
          <p class="text-xs font-semibold text-slate-400">@{{ form.tenDangNhap }}</p>

          <div class="w-full mt-6 space-y-2 border-t border-slate-100 pt-4">
            <button
              @click="tabHienTai = 'thongTin'"
              class="w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-semibold transition"
              :class="tabHienTai === 'thongTin' ? 'bg-rose-50 text-[#B82220]' : 'text-slate-600 hover:bg-slate-50'"
            >
              <User class="h-4 w-4" />
              Thông tin cá nhân
            </button>
            <button
              @click="tabHienTai = 'nganHang'"
              class="w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-semibold transition"
              :class="tabHienTai === 'nganHang' ? 'bg-rose-50 text-[#B82220]' : 'text-slate-600 hover:bg-slate-50'"
            >
              <Landmark class="h-4 w-4" />
              Tài khoản ngân hàng
            </button>
          </div>
        </div>

        <!-- Security password change card -->
        <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm space-y-4">
          <h3 class="flex items-center gap-2 text-sm font-bold text-slate-800">
            <Lock class="h-4 w-4 text-slate-400" />
            Bảo mật & Mật khẩu
          </h3>
          
          <div v-if="showDoiMatKhau" class="space-y-3 pt-2">
            <div class="space-y-1">
              <span class="text-[11px] font-bold text-slate-500 uppercase">Mật khẩu cũ</span>
              <input
                v-model="matKhauCu"
                type="password"
                placeholder="Nhập mật khẩu cũ"
                class="h-10 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
              />
            </div>
            <div class="space-y-1">
              <span class="text-[11px] font-bold text-slate-500 uppercase">Mật khẩu mới</span>
              <input
                v-model="matKhauMoi"
                type="password"
                placeholder="Mật khẩu mới (>= 6 ký tự)"
                class="h-10 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
              />
            </div>
            <p v-if="loiMatKhau" class="text-xs text-rose-500 leading-tight">{{ loiMatKhau }}</p>
            <div class="flex gap-2 pt-1">
              <button @click="handleDoiMatKhau" :disabled="dangLuu" class="inline-flex items-center justify-center flex-1 h-9 rounded-xl bg-[#B82220] hover:bg-[#a11a19] text-white text-xs font-bold transition">Đổi mật khẩu</button>
              <button @click="showDoiMatKhau = false" class="inline-flex items-center justify-center h-9 rounded-xl border border-slate-200 px-3 text-xs font-bold text-slate-500 hover:bg-slate-50 transition">Hủy</button>
            </div>
          </div>
          <button
            v-else
            @click="showDoiMatKhau = true"
            class="w-full flex items-center justify-center h-10 rounded-xl border border-slate-200 text-sm font-bold text-slate-600 hover:bg-slate-50 transition"
          >
            Thay đổi mật khẩu
          </button>
        </div>
      </div>

      <!-- Right side: Content details -->
      <div class="lg:col-span-3 space-y-6">
        <!-- Tab 1: Profile Information form -->
        <div v-if="tabHienTai === 'thongTin'" class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm space-y-6">
          <div>
            <h3 class="text-lg font-bold text-slate-800">Thông tin cơ bản</h3>
            <p class="text-xs text-slate-400 mt-0.5">Những thông tin này được dùng khi thực hiện giao dịch và đặt hàng tại shop.</p>
          </div>

          <div class="grid gap-6 md:grid-cols-2">
            <!-- Fullname -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600">Họ và tên <span class="text-rose-500">*</span></label>
              <div class="relative">
                <User class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  v-model="form.hoTen"
                  type="text"
                  placeholder="Họ và tên"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
                />
              </div>
              <p v-if="loiForm.hoTen" class="text-xs text-rose-500">{{ loiForm.hoTen }}</p>
            </div>

            <!-- Email -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600">Email <span class="text-rose-500">*</span></label>
              <div class="relative">
                <Mail class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  v-model="form.email"
                  type="email"
                  placeholder="name@example.com"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
                />
              </div>
              <p v-if="loiForm.email" class="text-xs text-rose-500">{{ loiForm.email }}</p>
            </div>

            <!-- Phone -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600">Số điện thoại</label>
              <div class="relative">
                <Phone class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  v-model="form.sdt"
                  type="tel"
                  placeholder="Nhập số điện thoại"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
                />
              </div>
              <p v-if="loiForm.sdt" class="text-xs text-rose-500">{{ loiForm.sdt }}</p>
            </div>

            <!-- Birthday -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600">Ngày sinh</label>
              <div class="relative">
                <Calendar class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  v-model="form.ngaySinh"
                  type="date"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
                />
              </div>
            </div>

            <!-- Gender -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600">Giới tính</label>
              <div class="flex gap-4 h-11 items-center">
                <label class="flex items-center gap-2 cursor-pointer text-sm font-semibold text-slate-600">
                  <input v-model="form.gioiTinh" type="radio" :value="1" class="text-rose-500 focus:ring-rose-500" />
                  Nam
                </label>
                <label class="flex items-center gap-2 cursor-pointer text-sm font-semibold text-slate-600">
                  <input v-model="form.gioiTinh" type="radio" :value="0" class="text-rose-500 focus:ring-rose-500" />
                  Nữ
                </label>
                <label class="flex items-center gap-2 cursor-pointer text-sm font-semibold text-slate-600">
                  <input v-model="form.gioiTinh" type="radio" :value="2" class="text-rose-500 focus:ring-rose-500" />
                  Khác
                </label>
              </div>
            </div>
          </div>
        </div>

        <div v-if="tabHienTai === 'thongTin'" class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm space-y-6">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div class="flex items-center gap-3">
              <div class="flex size-11 items-center justify-center rounded-2xl bg-sky-50 text-sky-500">
                <MapPin class="size-5" />
              </div>
              <div>
                <h3 class="text-lg font-bold text-slate-800 text-balance">Sổ địa chỉ</h3>
                <p class="mt-0.5 text-xs text-slate-400 text-pretty">Quản lý địa chỉ nhận hàng dùng khi đặt mua sản phẩm.</p>
              </div>
            </div>
            <button
              v-if="!hienFormDiaChi"
              type="button"
              @click="moThemDiaChi"
              class="inline-flex h-10 items-center justify-center gap-2 rounded-xl bg-[#B82220] px-4 text-xs font-bold text-white transition hover:bg-[#a11a19]"
            >
              <Plus class="size-4" />
              Thêm địa chỉ
            </button>
          </div>

          <div v-if="hienFormDiaChi" class="space-y-5 border-t border-slate-100 pt-5">
            <div class="flex items-center justify-between">
              <h4 class="text-sm font-bold text-slate-700">
                {{ diaChiDangSua ? "Cập nhật địa chỉ" : "Thêm địa chỉ mới" }}
              </h4>
              <button
                type="button"
                aria-label="Đóng form địa chỉ"
                @click="huyFormDiaChi"
                class="inline-flex size-8 items-center justify-center rounded-full text-slate-500 transition hover:bg-slate-100"
              >
                <X class="size-4" />
              </button>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <label class="space-y-1.5">
                <span class="text-[13px] font-bold text-slate-600">Họ tên người nhận <span class="text-rose-500">*</span></span>
                <input
                  v-model="formDiaChi.hoTen"
                  type="text"
                  placeholder="Nhập họ tên người nhận"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.hoTen ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                />
                <p v-if="loiDiaChi.hoTen" class="text-xs text-rose-500">{{ loiDiaChi.hoTen }}</p>
              </label>

              <label class="space-y-1.5">
                <span class="text-[13px] font-bold text-slate-600">Số điện thoại <span class="text-rose-500">*</span></span>
                <input
                  v-model="formDiaChi.sdt"
                  type="tel"
                  placeholder="VD: 0901234567"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.sdt ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                />
                <p v-if="loiDiaChi.sdt" class="text-xs text-rose-500">{{ loiDiaChi.sdt }}</p>
              </label>

              <label class="space-y-1.5">
                <span class="text-[13px] font-bold text-slate-600">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
                <select
                  :value="maTinhChon ?? ''"
                  @change="chonTinh(Number(($event.target as HTMLSelectElement).value) || null)"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.tinhThanh ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn tỉnh/thành --</option>
                  <option v-for="tinh in dsTinh" :key="tinh.code" :value="tinh.code">{{ tinh.name }}</option>
                </select>
                <p v-if="loiDiaChi.tinhThanh" class="text-xs text-rose-500">{{ loiDiaChi.tinhThanh }}</p>
              </label>

              <label class="space-y-1.5">
                <span class="text-[13px] font-bold text-slate-600">Quận/Huyện <span class="text-rose-500">*</span></span>
                <select
                  :value="maHuyenChon ?? ''"
                  :disabled="!maTinhChon"
                  @change="chonHuyen(Number(($event.target as HTMLSelectElement).value) || null)"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white disabled:cursor-not-allowed disabled:opacity-50', loiDiaChi.quanHuyen ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn quận/huyện --</option>
                  <option v-for="huyen in dsHuyen" :key="huyen.code" :value="huyen.code">{{ huyen.name }}</option>
                </select>
                <p v-if="loiDiaChi.quanHuyen" class="text-xs text-rose-500">{{ loiDiaChi.quanHuyen }}</p>
              </label>

              <label class="space-y-1.5">
                <span class="text-[13px] font-bold text-slate-600">Phường/Xã <span class="text-rose-500">*</span></span>
                <select
                  v-model="formDiaChi.phuongXa"
                  :disabled="!maHuyenChon"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white disabled:cursor-not-allowed disabled:opacity-50', loiDiaChi.phuongXa ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn phường/xã --</option>
                  <option v-for="xa in dsXa" :key="xa.code" :value="xa.name">{{ xa.name }}</option>
                </select>
                <p v-if="loiDiaChi.phuongXa" class="text-xs text-rose-500">{{ loiDiaChi.phuongXa }}</p>
              </label>

              <label class="space-y-1.5 md:col-span-2">
                <span class="text-[13px] font-bold text-slate-600">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
                <input
                  v-model="formDiaChi.diaChiCuThe"
                  type="text"
                  placeholder="Số nhà, tên đường..."
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.diaChiCuThe ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                />
                <p v-if="loiDiaChi.diaChiCuThe" class="text-xs text-rose-500">{{ loiDiaChi.diaChiCuThe }}</p>
              </label>

              <label class="flex cursor-pointer items-center gap-2 md:col-span-2">
                <input v-model="formDiaChi.laMacDinh" type="checkbox" class="size-4 rounded border-slate-300 text-[#B82220] focus:ring-[#B82220]" />
                <span class="text-sm font-semibold text-slate-600">Đặt làm địa chỉ mặc định</span>
              </label>
            </div>

            <div class="flex flex-wrap gap-3 border-t border-slate-100 pt-4">
              <button
                type="button"
                :disabled="dangLuuDiaChi"
                @click="luuDiaChi"
                class="inline-flex h-10 items-center justify-center gap-2 rounded-xl bg-[#B82220] px-5 text-sm font-bold text-white transition hover:bg-[#a11a19] disabled:opacity-50"
              >
                <Save class="size-4" />
                {{ dangLuuDiaChi ? "Đang lưu..." : "Lưu địa chỉ" }}
              </button>
              <button
                type="button"
                @click="huyFormDiaChi"
                class="inline-flex h-10 items-center justify-center rounded-xl border border-slate-200 px-5 text-sm font-bold text-slate-600 transition hover:bg-slate-50"
              >
                Hủy
              </button>
            </div>
          </div>

          <div v-else class="border-t border-slate-100 pt-5">
            <div v-if="dangTaiDiaChi" class="py-8 text-center text-sm text-slate-400">Đang tải địa chỉ...</div>
            <div v-else-if="!dsDiaChi.length" class="flex flex-col items-center justify-center gap-3 py-10 text-center">
              <div class="flex size-12 items-center justify-center rounded-full bg-slate-100 text-slate-400">
                <MapPin class="size-5" />
              </div>
              <div>
                <p class="text-sm font-bold text-slate-700">Bạn chưa có địa chỉ nhận hàng</p>
                <p class="mt-1 text-xs text-slate-400">Thêm địa chỉ để đặt hàng nhanh hơn.</p>
              </div>
              <button type="button" @click="moThemDiaChi" class="text-sm font-bold text-[#B82220] hover:underline">
                Thêm địa chỉ đầu tiên
              </button>
            </div>

            <div v-else class="grid gap-4 md:grid-cols-2">
              <article
                v-for="diaChi in dsDiaChi"
                :key="diaChi.id"
                class="flex min-h-48 flex-col justify-between rounded-2xl border border-slate-200 p-5"
              >
                <div>
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <p class="font-bold text-slate-800">{{ diaChi.hoTen }}</p>
                      <p class="mt-1 text-sm tabular-nums text-slate-500">{{ diaChi.sdt }}</p>
                    </div>
                    <span
                      v-if="diaChi.laMacDinh"
                      class="inline-flex items-center gap-1 whitespace-nowrap rounded-full bg-emerald-50 px-2.5 py-1 text-[11px] font-bold text-emerald-600"
                    >
                      <CheckCircle2 class="size-3" />
                      Mặc định
                    </span>
                  </div>
                  <p class="mt-4 text-sm leading-6 text-slate-600">
                    {{ diaChi.diaChiCuThe }}, {{ diaChi.phuongXa }}, {{ diaChi.quanHuyen }}, {{ diaChi.tinhThanh }}
                  </p>
                </div>

                <div class="mt-5 flex items-center justify-between gap-3 border-t border-slate-100 pt-4">
                  <button
                    v-if="!diaChi.laMacDinh"
                    type="button"
                    @click="datDiaChiMacDinh(diaChi.id)"
                    class="text-xs font-bold text-slate-500 transition hover:text-emerald-600"
                  >
                    Đặt làm mặc định
                  </button>
                  <span v-else class="text-xs font-semibold text-emerald-600">Địa chỉ đang sử dụng</span>

                  <div class="flex items-center gap-2">
                    <button
                      type="button"
                      aria-label="Sửa địa chỉ"
                      @click="moSuaDiaChi(diaChi)"
                      class="inline-flex size-8 items-center justify-center rounded-full bg-slate-100 text-slate-500 transition hover:bg-rose-50 hover:text-[#B82220]"
                    >
                      <Pencil class="size-4" />
                    </button>
                    <button
                      type="button"
                      aria-label="Xóa địa chỉ"
                      @click="xoaDiaChi(diaChi.id)"
                      class="inline-flex size-8 items-center justify-center rounded-full bg-slate-100 text-slate-500 transition hover:bg-rose-50 hover:text-rose-600"
                    >
                      <Trash2 class="size-4" />
                    </button>
                  </div>
                </div>
              </article>
            </div>
          </div>
        </div>

        <!-- Tab 2: Bank accounts list -->
        <div v-else class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm space-y-6">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg font-bold text-slate-800">Tài khoản ngân hàng liên kết</h3>
              <p class="text-xs text-slate-400 mt-0.5">Lưu danh sách tài khoản ngân hàng để nhận tiền hoàn trả khi hủy đơn / hoàn hàng nhanh chóng.</p>
            </div>
            <button
              @click="moModalThemNganHang"
              class="inline-flex items-center justify-center gap-1.5 h-10 px-4 rounded-xl border border-[#B82220] hover:bg-rose-50 text-[#B82220] text-xs font-bold transition duration-300 active:scale-95"
            >
              <Plus class="h-4 w-4" />
              Thêm tài khoản
            </button>
          </div>

          <!-- Accounts Cards Grid -->
          <div v-if="dsTaiKhoan.length > 0" class="grid gap-6 sm:grid-cols-2">
            <div
              v-for="tk in dsTaiKhoan"
              :key="tk.id"
              class="relative rounded-2xl border border-slate-100 bg-slate-50 p-6 flex flex-col justify-between hover:shadow-md hover:border-rose-100 transition duration-300 group"
            >
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <img 
                      v-if="layLogoNganHang(tk.tenNganHang)" 
                      :src="layLogoNganHang(tk.tenNganHang)" 
                      :alt="tk.tenNganHang" 
                      class="h-8 w-auto max-h-8 object-contain bg-white px-2 py-0.5 rounded-lg border border-slate-200" 
                    />
                    <span v-else class="flex h-8 w-8 items-center justify-center rounded-lg bg-rose-500 text-white font-bold text-xs uppercase">
                      {{ tk.tenNganHang.slice(0, 2) }}
                    </span>
                    <h4 class="font-bold text-slate-800">{{ tk.tenNganHang }}</h4>
                  </div>
                  <span v-if="tk.laMacDinh" class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-[10px] font-bold text-emerald-600 border border-emerald-100">
                    <ShieldCheck class="h-3 w-3" />
                    Mặc định
                  </span>
                </div>

                <div class="space-y-1">
                  <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Số tài khoản</p>
                  <p class="text-base font-extrabold text-slate-800 tracking-wide">{{ tk.soTaiKhoan }}</p>
                </div>

                <div class="space-y-1">
                  <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Chủ tài khoản</p>
                  <p class="text-xs font-bold text-slate-700 uppercase">{{ tk.tenChuTaiKhoan }}</p>
                </div>

                <div v-if="tk.chiNhanh" class="space-y-1">
                  <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Chi nhánh</p>
                  <p class="text-xs font-medium text-slate-500">{{ tk.chiNhanh }}</p>
                </div>
              </div>

              <!-- Actions -->
              <div class="mt-6 pt-4 border-t border-slate-200/60 flex items-center justify-between">
                <button
                  v-if="!tk.laMacDinh"
                  @click="handleDatMacDinhNganHang(tk.id)"
                  class="text-xs font-bold text-slate-500 hover:text-emerald-600 transition"
                >
                  Đặt làm mặc định
                </button>
                <div v-else class="text-xs font-bold text-emerald-600">Đang chọn nhận tiền</div>
                
                <div class="flex items-center gap-3">
                  <button
                    @click="moModalSuaNganHang(tk)"
                    class="inline-flex items-center gap-1 text-xs font-bold text-slate-500 transition hover:text-[#B82220]"
                  >
                    <Pencil class="h-3.5 w-3.5" />
                    Sửa
                  </button>
                  <button
                    @click="handleXoaNganHang(tk.id)"
                    class="text-xs font-bold text-rose-500 hover:text-rose-700 transition flex items-center gap-1"
                  >
                    <Trash2 class="h-3.5 w-3.5" />
                    Xóa
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="flex flex-col items-center justify-center py-12 rounded-2xl border-2 border-dashed border-slate-200 bg-slate-50">
            <Landmark class="h-10 w-10 text-slate-400 mb-3" />
            <p class="text-sm font-bold text-slate-700">Chưa có tài khoản ngân hàng nào</p>
            <p class="text-xs text-slate-400 mt-1">Vui lòng liên kết tài khoản ngân hàng của bạn để nhận tiền hoàn trả.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Add Bank Account -->
    <div v-if="showModalNganHang" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
      <div class="w-full max-w-lg overflow-hidden rounded-3xl border border-slate-100 bg-white p-8 shadow-2xl relative space-y-6">
        <button
          @click="showModalNganHang = false"
          class="absolute right-6 top-6 flex h-8 w-8 items-center justify-center rounded-full bg-slate-50 text-slate-400 hover:bg-slate-100 transition"
        >
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12"/></svg>
        </button>

        <div>
          <h3 class="text-lg font-bold text-slate-800">
            {{ editBankId ? "Cập nhật tài khoản ngân hàng" : "Thêm tài khoản ngân hàng" }}
          </h3>
          <p class="text-xs text-slate-400 mt-0.5">Vui lòng điền đúng thông tin số tài khoản và ngân hàng của bạn.</p>
        </div>

        <div class="space-y-4">
          <!-- Searchable Bank Select -->
          <div class="space-y-1.5 relative">
            <label class="text-[13px] font-bold text-slate-600">Ngân hàng <span class="text-rose-500">*</span></label>
            <input
              v-model="searchNganHangText"
              type="text"
              placeholder="Gõ tìm kiếm ngân hàng (Ví dụ: VCB, MBBank...)"
              @focus="dropdownNganHangMo = true"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
            />
            <p v-if="bankFormErrors.tenNganHang" class="text-xs text-rose-500 mt-0.5">{{ bankFormErrors.tenNganHang }}</p>

            <!-- Search Dropdown List -->
            <div v-if="dropdownNganHangMo" class="absolute left-0 right-0 z-50 mt-2 max-h-56 overflow-y-auto rounded-2xl border border-slate-100 bg-white shadow-xl py-2">
              <div v-if="filteredBanks.length === 0" class="px-4 py-3 text-xs text-slate-400 text-center">Không tìm thấy ngân hàng hợp lệ</div>
              <button
                v-for="bank in filteredBanks"
                :key="bank.id"
                @click="selectBank(bank)"
                class="flex w-full items-center gap-3 px-4 py-2.5 hover:bg-slate-50 transition text-left"
              >
                <img :src="bank.logo" :alt="bank.shortName" class="h-5 w-auto object-contain shrink-0" />
                <div class="flex-1">
                  <span class="text-sm font-bold text-slate-800">{{ bank.shortName }}</span>
                  <span class="text-xs text-slate-400 block truncate">{{ bank.name }}</span>
                </div>
              </button>
            </div>
            <div v-if="dropdownNganHangMo" @click="dropdownNganHangMo = false" class="fixed inset-0 z-30"></div>
          </div>

          <!-- Account Number -->
          <div class="space-y-1.5">
            <label class="text-[13px] font-bold text-slate-600">Số tài khoản <span class="text-rose-500">*</span></label>
            <input
              v-model="bankForm.soTaiKhoan"
              type="text"
              placeholder="Nhập số tài khoản ngân hàng"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
            />
            <p v-if="bankFormErrors.soTaiKhoan" class="text-xs text-rose-500 mt-0.5">{{ bankFormErrors.soTaiKhoan }}</p>
          </div>

          <!-- Account Holder Name -->
          <div class="space-y-1.5">
            <label class="text-[13px] font-bold text-slate-600">Tên chủ tài khoản <span class="text-rose-500">*</span></label>
            <input
              v-model="bankForm.tenChuTaiKhoan"
              type="text"
              placeholder="Ví dụ: NGUYEN QUOC HUY"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white uppercase transition"
            />
            <p v-if="bankFormErrors.tenChuTaiKhoan" class="text-xs text-rose-500 mt-0.5">{{ bankFormErrors.tenChuTaiKhoan }}</p>
          </div>

          <!-- Branch -->
          <div class="space-y-1.5">
            <label class="text-[13px] font-bold text-slate-600">Chi nhánh (Không bắt buộc)</label>
            <input
              v-model="bankForm.chiNhanh"
              type="text"
              placeholder="Ví dụ: Chi nhánh Hà Nội"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition"
            />
          </div>

          <!-- Set Default Checkbox -->
          <label class="flex items-center gap-2 cursor-pointer py-1">
            <input v-model="bankForm.laMacDinh" type="checkbox" class="text-rose-500 focus:ring-rose-500 rounded" />
            <span class="text-sm font-semibold text-slate-600">Đặt làm tài khoản mặc định để nhận tiền</span>
          </label>
        </div>

        <div class="flex gap-4 border-t border-slate-100 pt-6">
          <button
            @click="luuNganHang"
            :disabled="dangLuuNganHang"
            class="flex-1 inline-flex items-center justify-center h-11 rounded-2xl bg-[#B82220] hover:bg-[#a11a19] text-white text-sm font-bold shadow-lg shadow-rose-100 transition duration-300"
          >
            {{ dangLuuNganHang ? "Đang lưu..." : (editBankId ? "Lưu thay đổi" : "Thêm tài khoản") }}
          </button>
          <button
            @click="showModalNganHang = false"
            class="inline-flex items-center justify-center h-11 border border-slate-200 px-6 rounded-2xl text-sm font-bold text-slate-500 hover:bg-slate-50 transition"
          >
            Hủy
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Optional custom transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
