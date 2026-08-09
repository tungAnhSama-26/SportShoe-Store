<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import ONhapMatKhau from "../../../components/common/ONhapMatKhau.vue";
import ThanhDoManhMatKhau from "../../../components/common/ThanhDoManhMatKhau.vue";
import {
  Camera,
  Save,
  ScanLine,
  X,
  User,
  Mail,
  Phone,
  Calendar,
  MapPin,
  Lock,
  ArrowLeft,
} from "lucide-vue-next";
import {
  capNhatHoSoNhanVien,
  doiMatKhauHoSoNhanVien,
  layHoSoNhanVien,
  uploadFile,
} from "../../../services/nhan-vien";
import { getCurrentAdminUser } from "../../../services/auth";
import {
  getDisplayErrorMessage,
  getFieldErrors,
} from "../../../utils/error-message";
import { showSuccess } from "../../../utils/alert";
import { useAdminSession } from "../../../composable/useAdminSession";
import { layPhuongXaHaiCap, layTinhThanhHaiCap } from "../../../services/dia-chi";
import { chuanHoaDiaChi, doiChieuDiaChiHaiCap, taoPayloadDiaChi } from "../../../utils/dia-chi";

const router = useRouter();
const { refreshAdminSession } = useAdminSession();

const user = getCurrentAdminUser();
const id = user?.id;

const dangTai = ref(false);
const dangLuu = ref(false);
const dangUpload = ref(false);
const loiTrang = ref("");


const fileInputAvatar = ref(null);

const showDoiMatKhau = ref(false);
const matKhauMoi = ref("");

const loiForm = ref({
  hoTen: "",
  email: "",
  sdt: "",
});

const form = ref({
  hoTen: "",
  tenDangNhap: "",
  email: "",
  sdt: "",
  diaChiCuThe: "",
  tinhThanhCode: "",
  phuongXaCode: "",
  phuongXa: "",
  hinhAnh: "",
  vaiTro: 2,
  gioiTinh: "Nam",
  ngaySinh: "",
  tinhThanh: "",
});

const dsTinhThanh = ref([]);
const dsXaPhuong = ref([]);

// QR Scanner
const dangQuet = ref(false);
const loiCamera = ref("");
const videoRef = ref(null);
const thongBaoQrOk = ref("");
let zxingReader = null;
let BrowserMultiFormatReaderCtor = null;
let daXuLyQr = false;

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
    thongBaoQrOk.value = "Đã điền thông tin từ CCCD";
    setTimeout(() => {
      thongBaoQrOk.value = "";
    }, 4000);
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

async function taiChiTiet() {
  if (!id) return;
  dangTai.value = true;
  try {
    const data = await layHoSoNhanVien();
    const diaChi = chuanHoaDiaChi(data.diaChi);
    form.value = {
      hoTen: data.hoTen ?? "",
      tenDangNhap: data.tenDangNhap ?? "",
      email: data.email ?? "",
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
      "Không thể tải thông tin cá nhân",
    );
  } finally {
    dangTai.value = false;
  }
}

async function luu() {
  if (!id) return;
  loiForm.value = { hoTen: "", email: "", sdt: "" };
  let hasError = false;

  if (!form.value.hoTen.trim()) {
    loiForm.value.hoTen = "Vui lòng nhập họ và tên.";
    hasError = true;
  }
  if (!form.value.email.trim()) {
    loiForm.value.email = "Vui lòng nhập email.";
    hasError = true;
  }
  if (hasError) return;

  dangLuu.value = true;
  const payload = {
    hoTen: form.value.hoTen.trim(),
    email: form.value.email.trim(),
    sdt: form.value.sdt.trim() || undefined,
    gioiTinh: form.value.gioiTinh,
    ngaySinh: form.value.ngaySinh || undefined,
    diaChi: taoPayloadDiaChi(form.value),
    hinhAnh: form.value.hinhAnh || undefined,
    vaiTro: form.value.vaiTro,
    tenDangNhap: form.value.tenDangNhap,
  };

  try {
    const updated = await capNhatHoSoNhanVien(payload);
    // Cập nhật lại session storage
    const storageKeys = ["adminUser", "sport-shoe-admin-session"];
    storageKeys.forEach((key) => {
      const raw = localStorage.getItem(key);
      if (raw) {
        const current = JSON.parse(raw);
        if (current.id === id) {
          localStorage.setItem(key, JSON.stringify({ ...current, ...updated }));
        }
      }
    });
    refreshAdminSession();
    showSuccess("Cập nhật thông tin thành công.", "Thành công");
  } catch (error) {
    Object.assign(loiForm.value, getFieldErrors(error));
    loiTrang.value = getDisplayErrorMessage(
      error,
      "Không thể cập nhật thông tin",
    );
  } finally {
    dangLuu.value = false;
  }
}

async function doiMatKhau() {
  if (!id) return;
  if (!matKhauMoi.value.trim() || matKhauMoi.value.trim().length < 6) {
    loiTrang.value = "Mật khẩu mới phải có ít nhất 6 ký tự.";
    return;
  }
  dangLuu.value = true;
  try {
    await doiMatKhauHoSoNhanVien(matKhauMoi.value.trim());
    showSuccess("Đổi mật khẩu thành công.", "Thành công");
    matKhauMoi.value = "";
    showDoiMatKhau.value = false;
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể đổi mật khẩu");
  } finally {
    dangLuu.value = false;
  }
}

async function xuLyUploadAnh(event) {
  const target = event.target;
  if (!target.files?.length) return;
  dangUpload.value = true;
  try {
    const url = await uploadFile(target.files[0]);
    form.value.hinhAnh = url;
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải ảnh lên");
  } finally {
    dangUpload.value = false;
  }
}

async function chonTinhProfile() {
  const tinh = dsTinhThanh.value.find(
    (item) => String(item.value) === String(form.value.tinhThanhCode),
  );
  form.value.tinhThanh = tinh?.label ?? "";
  form.value.phuongXaCode = "";
  form.value.phuongXa = "";
  try {
    dsXaPhuong.value = form.value.tinhThanhCode
      ? (await layPhuongXaHaiCap(form.value.tinhThanhCode)).map((item) => ({
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
  }
}

function chonPhuongXaProfile() {
  form.value.phuongXa = dsXaPhuong.value.find(
    (item) => item.value === form.value.phuongXaCode,
  )?.label ?? "";
}

onMounted(async () => {
  await taiChiTiet();
  try {
    const danhSachTinh = await layTinhThanhHaiCap();
    dsTinhThanh.value = danhSachTinh.map((item) => ({
      value: String(item.code),
      label: item.ten,
    }));
    const ketQua = await doiChieuDiaChiHaiCap(
      form.value,
      danhSachTinh,
      layPhuongXaHaiCap,
    );
    Object.assign(form.value, ketQua.diaChi);
    dsXaPhuong.value = ketQua.danhSachPhuongXa.map((item) => ({
      value: String(item.code),
      label: item.ten,
    }));
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(
      error,
      "Không thể tải danh mục địa chỉ",
    );
  }
});

</script>

<template>
  <div class="invoice-flat mx-auto max-w-5xl space-y-6 pb-12">
    <!-- Header -->
    <section class="flex items-center justify-between border-b border-slate-100 pb-4">
      <div class="flex items-center gap-4">
        <button
          type="button"
          @click="router.back()"
          class="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-slate-600 shadow-sm transition hover:bg-slate-50 lg:hidden"
        >
          <ArrowLeft class="h-5 w-5" />
        </button>
      </div>
      <button
        @click="luu"
        :disabled="dangLuu"
        class="admin-btn-primary h-11 rounded-2xl px-6 shadow-md shadow-rose-200"
      >
        <Save class="mr-2 h-4 w-4" />
        {{ dangLuu ? "Đang lưu..." : "Lưu thay đổi" }}
      </button>
    </section>

    <div
      v-if="loiTrang"
      class="rounded-2xl border border-rose-100 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-700"
    >
      {{ loiTrang }}
    </div>

    <div class="grid gap-6 lg:grid-cols-3">
      <!-- Left: Avatar & Identity -->
      <section class="space-y-6 lg:col-span-1">
        <div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
          <div class="flex flex-col items-center">
            <div class="group relative">
              <div
                class="h-40 w-40 overflow-hidden rounded-full border-4 border-slate-50 ring-1 ring-slate-200"
              >
                <img
                  :src="
                    form.hinhAnh ||
                    'https://ui-avatars.com/api/?name=' +
                      encodeURIComponent(form.hoTen) +
                      '&background=f1f5f9&color=475569&size=256'
                  "
                  alt="Avatar"
                  class="h-full w-full object-cover transition duration-300 group-hover:scale-110"
                />
              </div>
              <button
                type="button"
                @click="fileInputAvatar?.click()"
                class="absolute bottom-1 right-1 flex h-10 w-10 items-center justify-center rounded-full bg-rose-500 text-white shadow-lg transition hover:bg-rose-600 active:scale-95"
              >
                <Camera class="h-5 w-5" />
              </button>
              <input
                ref="fileInputAvatar"
                type="file"
                accept="image/*"
                class="hidden"
                @change="xuLyUploadAnh"
              />
            </div>
            <h2 class="mt-5 text-xl font-bold text-slate-800">
              {{ form.hoTen }}
            </h2>
            <p class="text-sm font-medium text-slate-500">
              {{ form.tenDangNhap }}
            </p>
          </div>

          <div class="mt-8 space-y-4">
            <div class="flex items-center gap-3 rounded-2xl bg-slate-50 p-4">
              <User class="h-5 w-5 text-slate-400" />
              <div class="flex-1">
                <p
                  class="text-[11px] font-bold uppercase tracking-wider text-slate-400"
                >
                  Tên đăng nhập
                </p>
                <p class="text-sm font-semibold text-slate-700">
                  {{ form.tenDangNhap }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Password Change -->
        <div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
          <h3
            class="flex items-center gap-2 text-base font-bold text-slate-800"
          >
            <Lock class="h-4 w-4 text-slate-400" />
            Bảo mật
          </h3>
          <div class="mt-6">
            <template v-if="showDoiMatKhau">
              <div class="space-y-3">
                <div>
                  <ONhapMatKhau
                    v-model="matKhauMoi"
                    placeholder="Mật khẩu mới (>= 6 ký tự)"
                    autocomplete="new-password"
                    input-class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
                  />
                  <ThanhDoManhMatKhau :mat-khau="matKhauMoi" />
                </div>
                <div class="flex gap-2">
                  <button
                    @click="doiMatKhau"
                    :disabled="dangLuu"
                    class="admin-btn-primary flex-1 h-10 rounded-xl"
                  >
                    Lưu
                  </button>
                  <button
                    @click="showDoiMatKhau = false"
                    class="admin-btn-soft h-10 rounded-xl px-4"
                  >
                    Hủy
                  </button>
                </div>
              </div>
            </template>
            <button
              v-else
              @click="showDoiMatKhau = true"
              class="w-full rounded-2xl border border-slate-200 py-3 text-sm font-bold text-slate-600 transition hover:bg-slate-50"
            >
              Đổi mật khẩu
            </button>
          </div>
        </div>
      </section>

      <!-- Right: Main Form -->
      <section class="space-y-6 lg:col-span-2">
        <div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
          <h3 class="text-lg font-bold text-slate-800">Thông tin cơ bản</h3>
          <div class="mt-6 grid gap-6 sm:grid-cols-2">
            <!-- Full Name -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600"
                >Họ và tên <span class="text-rose-500">*</span></label
              >
              <div class="relative">
                <User
                  class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                />
                <input
                  v-model="form.hoTen"
                  type="text"
                  placeholder="Họ và tên"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
                />
              </div>
              <p v-if="loiForm.hoTen" class="text-xs text-rose-500">
                {{ loiForm.hoTen }}
              </p>
            </div>

            <!-- Email -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600"
                >Email <span class="text-rose-500">*</span></label
              >
              <div class="relative">
                <Mail
                  class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                />
                <input
                  v-model="form.email"
                  type="email"
                  placeholder="example@gmail.com"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
                />
              </div>
              <p v-if="loiForm.email" class="text-xs text-rose-500">
                {{ loiForm.email }}
              </p>
            </div>

            <!-- Phone -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600"
                >Số điện thoại</label
              >
              <div class="relative">
                <Phone
                  class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                />
                <input
                  v-model="form.sdt"
                  type="tel"
                  placeholder="0123456789"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
                />
              </div>
            </div>

            <!-- Birthday -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600"
                >Ngày sinh</label
              >
              <div class="relative">
                <Calendar
                  class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                />
                <input
                  v-model="form.ngaySinh"
                  type="date"
                  class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none focus:border-rose-300 focus:bg-white"
                />
              </div>
            </div>

            <!-- Gender -->
            <div class="space-y-1.5">
              <label class="text-[13px] font-bold text-slate-600"
                >Giới tính</label
              >
              <div
                class="flex h-11 items-center gap-8 rounded-2xl border border-slate-200 bg-slate-50 px-6"
              >
                <label class="flex items-center gap-2 cursor-pointer">
                  <input
                    v-model="form.gioiTinh"
                    type="radio"
                    value="Nam"
                    class="h-4 w-4 accent-rose-500"
                  />
                  <span class="text-sm font-medium text-slate-700">Nam</span>
                </label>
                <label class="flex items-center gap-2 cursor-pointer">
                  <input
                    v-model="form.gioiTinh"
                    type="radio"
                    value="Nữ"
                    class="h-4 w-4 accent-rose-500"
                  />
                  <span class="text-sm font-medium text-slate-700">Nữ</span>
                </label>
              </div>
            </div>

          </div>

          <div class="mt-8 h-px bg-slate-100"></div>

          <div class="mt-8 space-y-1.5">
            <label class="text-[13px] font-bold text-slate-600">Địa chỉ</label>
            <div class="relative">
              <MapPin class="absolute left-4 top-[4.75rem] h-4 w-4 text-slate-400" />
              <div class="mb-3 grid gap-3 md:grid-cols-2">
                <select
                  v-model="form.tinhThanhCode"
                  class="h-11 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none"
                  @change="chonTinhProfile"
                >
                  <option value="">-- Chọn tỉnh/thành --</option>
                  <option v-for="item in dsTinhThanh" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select>
                <select
                  v-model="form.phuongXaCode"
                  :disabled="!form.tinhThanhCode"
                  class="h-11 rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none disabled:opacity-50"
                  @change="chonPhuongXaProfile"
                >
                  <option value="">-- Chọn phường/xã --</option>
                  <option v-for="item in dsXaPhuong" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select>
              </div>
              <textarea
                v-model="form.diaChiCuThe"
                rows="3"
                placeholder="Nhập địa chỉ của bạn..."
                class="w-full rounded-2xl border border-slate-200 bg-slate-50 pb-4 pl-11 pr-4 pt-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              ></textarea>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.invoice-flat :deep([class*="rounded-"]:not(.rounded-full)) {
  border-radius: 6px !important;
}
</style>
