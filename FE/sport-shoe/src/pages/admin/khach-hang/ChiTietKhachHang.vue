<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, CheckCircle2, MapPin, Pencil, Plus, Save, Trash2, User, X } from "lucide-vue-next";
import {
  capNhatDiaChi,
  capNhatKhachHang,
  datMacDinhDiaChi,
  doiMatKhauKhachHang,
  doiTrangThaiKhachHang,
  layChiTietKhachHang,
  layDanhSachDiaChi,
  taoKhachHang,
  themDiaChi,
  uploadFile,
  xoaDiaChi
} from "../../../services/khach-hang";
import { getDisplayErrorMessage, getFieldErrors } from "../../../utils/error-message";
import { showConfirm, showSuccess } from "../../../utils/alert";
import { isValidEmail, isValidVnPhone, validateNgaySinh } from "../../../utils/validation";
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";
import Table from "../../../components/ui/Table.vue";

const route = useRoute();
const router = useRouter();

const id = route.params.id as string | undefined;
const laMoi = !id;
const CUSTOMER_CREATE_TOAST_KEY = "admin-khach-hang-toast";
const PHONE_HINT = "Số điện thoại không đúng định dạng (VD: 0901234567).";

const dangTai = ref(false);
const dangLuu = ref(false);
const dangUpload = ref(false);
const loiTrang = ref("");
const loiForm = ref({ tenDangNhap: "", hoTen: "", email: "", sdt: "", ngaySinh: "", matKhau: "" });

function taoMatKhauNgauNhien(): string {
  const chars = 'ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$!';
  let result = '';
  for (let i = 0; i < 10; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

function normalizeText(value: unknown): string {
  return String(value ?? "").trim();
}

function createUsernameSegment(value: string): string {
  return normalizeText(value)
    .normalize("NFD")
    .replace(/[đĐ]/g, "d")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9._-]+/g, "")
    .slice(0, 40);
}

function taoTenDangNhapKhachHang(): string {
  const email = normalizeText(form.value.email).toLowerCase();
  const emailBase = email.includes("@") ? email.split("@")[0] : "";
  const phoneDigits = normalizeText(form.value.sdt).replace(/\D/g, "");
  const nameBase = createUsernameSegment(form.value.hoTen);
  const generated =
    createUsernameSegment(emailBase) ||
    (phoneDigits ? `kh${phoneDigits}` : "") ||
    (nameBase ? `kh${nameBase}${Date.now().toString(36).slice(-6)}` : "");

  return generated.slice(0, 50);
}

function buildDiaChiPayload() {
  const f = formDiaChi.value;
  return {
    hoTen: normalizeText(f.hoTen),
    sdt: normalizeText(f.sdt),
    tinhThanh: normalizeText(f.tinhThanh),
    quanHuyen: normalizeText(f.quanHuyen),
    phuongXa: normalizeText(f.phuongXa),
    diaChiCuThe: normalizeText(f.diaChiCuThe),
    laMacDinh: Boolean(f.laMacDinh),
  };
}
const khachHang = ref<any>(null);

const form = ref({
  tenDangNhap: "",
  hoTen: "",
  email: "",
  sdt: "",
  gioiTinh: "",
  ngaySinh: "",
  hinhAnh: "",
  matKhau: "",
});

// Form địa chỉ: dùng cho địa chỉ mặc định khi thêm mới
// và cho thao tác thêm/sửa địa chỉ trong sổ địa chỉ khi chỉnh sửa.
const formDiaChi = ref({
  hoTen: "",
  sdt: "",
  tinhThanh: "",
  quanHuyen: "",
  phuongXa: "",
  diaChiCuThe: "",
  laMacDinh: true,
});
const loiDiaChi = ref({ hoTen: "", sdt: "", tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "" });

// Province cascade
const dsTinh = ref<any[]>([]);
const dsHuyen = ref<any[]>([]);
const dsXa = ref<any[]>([]);
const maTinhChon = ref<number | null>(null);
const maHuyenChon = ref<number | null>(null);

// Sổ địa chỉ (chế độ chỉnh sửa)
const dsDiaChi = ref<any[]>([]);
const dangTaiDsDiaChi = ref(false);
const hienFormDiaChi = ref(false);
const diaChiDangSua = ref<any>(null);
const dangLuuDiaChi = ref(false);

async function taiDsTinh() {
  if (dsTinh.value.length) return;
  try {
    const res = await fetch("https://provinces.open-api.vn/api/p/");
    dsTinh.value = await res.json();
  } catch { dsTinh.value = []; }
}

async function onTinhChange(code: number | null) {
  maTinhChon.value = code;
  dsHuyen.value = [];
  dsXa.value = [];
  maHuyenChon.value = null;
  formDiaChi.value.tinhThanh = dsTinh.value.find(t => t.code === code)?.name ?? "";
  formDiaChi.value.quanHuyen = "";
  formDiaChi.value.phuongXa = "";
  if (!code) return;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/p/${code}?depth=2`);
    const data = await res.json();
    dsHuyen.value = data.districts ?? [];
  } catch { dsHuyen.value = []; }
}

async function onHuyenChange(code: number | null) {
  maHuyenChon.value = code;
  dsXa.value = [];
  formDiaChi.value.quanHuyen = dsHuyen.value.find(h => h.code === code)?.name ?? "";
  formDiaChi.value.phuongXa = "";
  if (!code) return;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/d/${code}?depth=2`);
    const data = await res.json();
    dsXa.value = data.wards ?? [];
  } catch { dsXa.value = []; }
}

// Đổ sẵn tỉnh/huyện/xã khi sửa một địa chỉ có sẵn
async function preFillCascadeForEdit(dc: any) {
  await taiDsTinh();
  const tinh = dsTinh.value.find(t => t.name === dc.tinhThanh);
  if (!tinh) return;
  maTinhChon.value = tinh.code;
  try {
    const res = await fetch(`https://provinces.open-api.vn/api/p/${tinh.code}?depth=2`);
    const data = await res.json();
    dsHuyen.value = data.districts ?? [];
    const huyen = dsHuyen.value.find(h => h.name === dc.quanHuyen);
    if (!huyen) return;
    maHuyenChon.value = huyen.code;
    const res2 = await fetch(`https://provinces.open-api.vn/api/d/${huyen.code}?depth=2`);
    const data2 = await res2.json();
    dsXa.value = data2.wards ?? [];
  } catch { /* ignore */ }
}

// Luôn đồng bộ họ tên & SĐT xuống form địa chỉ khi ở chế độ thêm mới
watch(() => form.value.hoTen, (v) => { if (laMoi) formDiaChi.value.hoTen = v; });
watch(() => form.value.sdt, (v) => { if (laMoi) formDiaChi.value.sdt = v; });

// Tự động tạo tên đăng nhập cho khách hàng mới
watch([() => form.value.email, () => form.value.sdt, () => form.value.hoTen], () => {
  if (!laMoi) return;
  form.value.tenDangNhap = taoTenDangNhapKhachHang();
});

const matKhauMoi = ref("");
const showDoiMatKhau = ref(false);

async function taiChiTiet() {
  if (laMoi) {
    form.value.matKhau = taoMatKhauNgauNhien();
    form.value.tenDangNhap = taoTenDangNhapKhachHang();
    await taiDsTinh();
    return;
  }
  dangTai.value = true;
  try {
    const data = await layChiTietKhachHang(id!);
    khachHang.value = data;
    form.value = {
      tenDangNhap: data.tenDangNhap,
      hoTen: data.hoTen,
      email: data.email ?? "",
      sdt: data.sdt ?? "",
      gioiTinh: data.gioiTinh != null ? String(data.gioiTinh) : "",
      ngaySinh: data.ngaySinh ?? "",
      hinhAnh: data.hinhAnh ?? "",
      matKhau: "",
    };
    await taiDsDiaChi();
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải thông tin khách hàng");
  } finally {
    dangTai.value = false;
  }
}

function validateThongTin(): boolean {
  loiForm.value = { tenDangNhap: "", hoTen: "", email: "", sdt: "", ngaySinh: "", matKhau: "" };
  let hasError = false;
  const hoTen = normalizeText(form.value.hoTen);
  const email = normalizeText(form.value.email);
  const sdt = normalizeText(form.value.sdt);
  const ngaySinh = normalizeText(form.value.ngaySinh);

  if (!hoTen) {
    loiForm.value.hoTen = "Vui lòng nhập họ tên khách hàng.";
    hasError = true;
  } else if (hoTen.length > 100) {
    loiForm.value.hoTen = "Họ tên không quá 100 ký tự.";
    hasError = true;
  }

  if (email) {
    if (!isValidEmail(email)) {
      loiForm.value.email = "Email khách hàng chưa đúng định dạng.";
      hasError = true;
    } else if (email.length > 100) {
      loiForm.value.email = "Email không quá 100 ký tự.";
      hasError = true;
    }
  }

  if (sdt && !isValidVnPhone(sdt)) {
    loiForm.value.sdt = PHONE_HINT;
    hasError = true;
  }

  const loiNgaySinh = validateNgaySinh(ngaySinh);
  if (loiNgaySinh) {
    loiForm.value.ngaySinh = loiNgaySinh;
    hasError = true;
  }

  if (laMoi) {
    form.value.tenDangNhap = taoTenDangNhapKhachHang();
    if (!form.value.tenDangNhap) {
      loiForm.value.tenDangNhap = "Không thể tạo tên đăng nhập cho khách hàng.";
      hasError = true;
    }
    if (!normalizeText(form.value.matKhau)) {
      form.value.matKhau = taoMatKhauNgauNhien();
    }
    if (normalizeText(form.value.matKhau).length < 6) {
      loiForm.value.matKhau = "Mật khẩu phải có ít nhất 6 ký tự.";
      hasError = true;
    }
    if (!validateDiaChi()) hasError = true;
  }

  return !hasError;
}

function validateDiaChi(): boolean {
  const f = formDiaChi.value;
  const err = { hoTen: "", sdt: "", tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "" };
  let ok = true;
  if (!f.hoTen.trim()) { err.hoTen = "Vui lòng nhập họ tên người nhận."; ok = false; }
  else if (f.hoTen.trim().length > 100) { err.hoTen = "Họ tên người nhận không quá 100 ký tự."; ok = false; }
  if (!f.sdt.trim()) { err.sdt = "Vui lòng nhập số điện thoại."; ok = false; }
  else if (!isValidVnPhone(f.sdt)) { err.sdt = PHONE_HINT; ok = false; }
  if (!f.tinhThanh) { err.tinhThanh = "Vui lòng chọn tỉnh/thành phố."; ok = false; }
  if (!f.quanHuyen) { err.quanHuyen = "Vui lòng chọn quận/huyện."; ok = false; }
  if (!f.phuongXa) { err.phuongXa = "Vui lòng chọn phường/xã."; ok = false; }
  if (!f.diaChiCuThe.trim()) { err.diaChiCuThe = "Vui lòng nhập địa chỉ cụ thể."; ok = false; }
  loiDiaChi.value = err;
  return ok;
}

async function luu() {
  if (!validateThongTin()) return;

  if (laMoi) {
    const isConfirmed = await showConfirm("Xác nhận thêm mới khách hàng này?");
    if (!isConfirmed) return;
  }

  const hoTen = normalizeText(form.value.hoTen);
  const email = normalizeText(form.value.email);
  const sdt = normalizeText(form.value.sdt);
  const ngaySinh = normalizeText(form.value.ngaySinh);
  const hinhAnh = normalizeText(form.value.hinhAnh);

  dangLuu.value = true;
  loiTrang.value = "";

  try {
    if (laMoi) {
      const result = await taoKhachHang({
        tenDangNhap: normalizeText(form.value.tenDangNhap),
        hoTen,
        email: email || undefined,
        sdt: sdt || undefined,
        gioiTinh: form.value.gioiTinh !== "" ? Number(form.value.gioiTinh) : undefined,
        ngaySinh: ngaySinh || undefined,
        hinhAnh: hinhAnh || undefined,
        matKhau: normalizeText(form.value.matKhau),
      });
      await themDiaChi(result.id, buildDiaChiPayload());
      if (typeof window !== "undefined") {
        window.sessionStorage.setItem(
          CUSTOMER_CREATE_TOAST_KEY,
          JSON.stringify({ loai: "success", tieuDe: "Đã tạo khách hàng mới", noiDung: "", iconColor: "#ef4444" }),
        );
      }
      await router.push({ name: "admin-khach-hang" });
      await nextTick();
    } else {
      const updated = await capNhatKhachHang(id!, {
        hoTen,
        email: email || undefined,
        sdt: sdt || undefined,
        gioiTinh: form.value.gioiTinh !== "" ? Number(form.value.gioiTinh) : undefined,
        ngaySinh: ngaySinh || undefined,
        hinhAnh: hinhAnh || undefined,
      });
      khachHang.value = updated;
      showSuccess("Đã lưu thay đổi thành công!", "Thành công");
    }
  } catch (e) {
    Object.assign(loiForm.value, getFieldErrors(e));
    loiTrang.value = getDisplayErrorMessage(e, laMoi ? "Không thể tạo khách hàng" : "Không thể lưu thay đổi khách hàng");
  } finally {
    dangLuu.value = false;
  }
}

// ===== Sổ địa chỉ (chế độ chỉnh sửa) =====
async function taiDsDiaChi() {
  if (laMoi) return;
  dangTaiDsDiaChi.value = true;
  try {
    dsDiaChi.value = await layDanhSachDiaChi(id!);
  } catch {
    dsDiaChi.value = [];
  } finally {
    dangTaiDsDiaChi.value = false;
  }
}

function resetFormDiaChi() {
  formDiaChi.value = {
    hoTen: form.value.hoTen || "",
    sdt: form.value.sdt || "",
    tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "",
    laMacDinh: dsDiaChi.value.length === 0,
  };
  loiDiaChi.value = { hoTen: "", sdt: "", tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "" };
  maTinhChon.value = null;
  maHuyenChon.value = null;
  dsHuyen.value = [];
  dsXa.value = [];
}

async function moThemDiaChi() {
  diaChiDangSua.value = null;
  resetFormDiaChi();
  hienFormDiaChi.value = true;
  await taiDsTinh();
}

async function moSuaDiaChi(dc: any) {
  diaChiDangSua.value = dc;
  formDiaChi.value = {
    hoTen: dc.hoTen, sdt: dc.sdt,
    tinhThanh: dc.tinhThanh, quanHuyen: dc.quanHuyen, phuongXa: dc.phuongXa,
    diaChiCuThe: dc.diaChiCuThe, laMacDinh: dc.laMacDinh,
  };
  loiDiaChi.value = { hoTen: "", sdt: "", tinhThanh: "", quanHuyen: "", phuongXa: "", diaChiCuThe: "" };
  maTinhChon.value = null;
  maHuyenChon.value = null;
  dsHuyen.value = [];
  dsXa.value = [];
  hienFormDiaChi.value = true;
  await preFillCascadeForEdit(dc);
}

function huyFormDiaChi() {
  hienFormDiaChi.value = false;
  diaChiDangSua.value = null;
}

async function luuDiaChi() {
  if (!validateDiaChi()) return;
  const dangSua = Boolean(diaChiDangSua.value);
  dangLuuDiaChi.value = true;
  loiTrang.value = "";
  try {
    if (dangSua) {
      await capNhatDiaChi(diaChiDangSua.value.id, buildDiaChiPayload());
    } else {
      await themDiaChi(id!, buildDiaChiPayload());
    }
    await taiDsDiaChi();
    hienFormDiaChi.value = false;
    diaChiDangSua.value = null;
    showSuccess(dangSua ? "Đã cập nhật địa chỉ" : "Đã thêm địa chỉ mới", "Thành công");
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể lưu địa chỉ");
  } finally {
    dangLuuDiaChi.value = false;
  }
}

async function xoaDiaChiKhachHang(diaChiId: number) {
  const ok = await showConfirm("Bạn có chắc chắn muốn xóa địa chỉ này?", "Xác nhận xóa", "Xóa", "Hủy");
  if (!ok) return;
  try {
    await xoaDiaChi(diaChiId);
    await taiDsDiaChi();
    showSuccess("Đã xóa địa chỉ", "Thành công");
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể xóa địa chỉ");
  }
}

async function datLamMacDinh(diaChiId: number) {
  try {
    await datMacDinhDiaChi(diaChiId);
    await taiDsDiaChi();
    showSuccess("Đã cập nhật địa chỉ mặc định", "Thành công");
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể đặt địa chỉ mặc định");
  }
}

async function doiMatKhau() {
  if (!matKhauMoi.value.trim() || matKhauMoi.value.length < 6) {
    loiTrang.value = "Mật khẩu mới phải có ít nhất 6 ký tự.";
    return;
  }
  dangLuu.value = true;
  loiTrang.value = "";
  try {
    await doiMatKhauKhachHang(id!, matKhauMoi.value);
    matKhauMoi.value = "";
    showDoiMatKhau.value = false;
    showSuccess("Đã đổi mật khẩu thành công!", "Thành công");
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể đổi mật khẩu khách hàng");
  } finally {
    dangLuu.value = false;
  }
}

async function doiTrangThai(trangThai: number) {
  const hanhDong = trangThai === 1 ? "kích hoạt" : "khóa";
  const tenKhachHang = form.value.hoTen || form.value.tenDangNhap || "khách hàng này";
  const isConfirmed = await showConfirm(`Bạn có chắc muốn ${hanhDong} ${tenKhachHang} không?`);
  if (!isConfirmed) return;
  try {
    const updated = await doiTrangThaiKhachHang(id!, trangThai);
    khachHang.value = updated;
    showSuccess(trangThai === 1 ? "Đã kích hoạt khách hàng!" : "Đã khóa khách hàng!", "Thành công");
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể cập nhật trạng thái khách hàng");
  }
}

async function xuLyUploadAnh(event: Event) {
  const target = event.target as HTMLInputElement;
  if (!target.files?.length) return;
  dangUpload.value = true;
  try {
    const url = await uploadFile(target.files[0]);
    form.value.hinhAnh = url;
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải ảnh khách hàng");
  } finally {
    dangUpload.value = false;
  }
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-5">
    <!-- Header -->
    <section class="flex items-center gap-4">
      <Button
        variant="ghost"
        size="icon"
        class="rounded-full bg-slate-100 hover:bg-slate-200"
        @click="router.push({ name: 'admin-khach-hang' })"
      >
        <ArrowLeft class="h-5 w-5" />
      </Button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm khách hàng mới" : "Chi tiết khách hàng" }}
        </h1>
        <p class="text-sm text-slate-400">{{ laMoi ? "Điền thông tin khách hàng mới vào form bên dưới." : `Tên đăng nhập: ${khachHang?.tenDangNhap ?? '...'}` }}</p>
      </div>
    </section>

    <div v-if="dangTai" class="rounded-[24px] border bg-white p-10 text-center text-slate-400 text-sm">Đang tải...</div>

    <template v-else>

      <div v-if="loiTrang" class="rounded-2xl bg-rose-50 border border-rose-100 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>

      <div class="flex flex-col-reverse xl:grid gap-5 xl:grid-cols-[320px_1fr]">

        <!-- Sidebar -->
        <div class="space-y-4">
          <!-- Avatar -->
          <Card class="text-center">
            <div class="relative inline-block cursor-pointer" @click="($refs.fileInputAvatar as HTMLInputElement)?.click()" title="Bấm để thay đổi hình ảnh">
              <img
                :src="form.hinhAnh || khachHang?.hinhAnh || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(form.hoTen || khachHang?.hoTen || 'KH') + '&background=f1f5f9&color=475569&size=128'"
                class="mx-auto h-32 w-32 rounded-full object-cover ring-4 ring-slate-100 hover:opacity-80 transition"
              />
              <input type="file" ref="fileInputAvatar" @change="xuLyUploadAnh" accept="image/*,.jpg,.jpeg,.png,.gif,.webp" class="hidden" />
              <div v-if="dangUpload" class="absolute inset-0 flex items-center justify-center rounded-full bg-white/70">
                <span class="text-xs font-bold text-slate-600">Đang tải...</span>
              </div>
            </div>
            <p class="mt-4 text-base font-bold text-slate-800">{{ form.hoTen || khachHang?.hoTen || "Khách hàng mới" }}</p>
            <p class="text-sm text-slate-400">{{ form.email || khachHang?.email || "Chưa cập nhật email" }}</p>
            <div v-if="!laMoi" class="mt-2">
              <span class="inline-flex rounded-full px-3 py-1 text-xs font-semibold" :class="khachHang?.trangThai === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'">
                {{ khachHang?.tenTrangThai }}
              </span>
            </div>
            <p v-else class="mt-2 text-xs text-slate-400">(Bấm vào ảnh để chọn avatar)</p>
          </Card>

          <!-- Extra functions for edit mode -->
          <template v-if="!laMoi">
            <Card>
              <h3 class="mb-3 text-sm font-bold text-slate-800">Đổi mật khẩu</h3>
              <div v-if="!showDoiMatKhau">
                <Button variant="outline" class="w-full justify-center" @click="showDoiMatKhau = true">Đổi mật khẩu</Button>
              </div>
              <div v-else class="space-y-3">
                <input v-model="matKhauMoi" type="password" placeholder="Mật khẩu mới (tối thiểu 6 ký tự)" class="h-10 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" />
                <div class="flex gap-2">
                  <Button variant="primary" class="flex-1 justify-center" @click="doiMatKhau" :disabled="dangLuu">Xác nhận</Button>
                  <Button variant="soft" class="flex-1 justify-center" @click="showDoiMatKhau = false; matKhauMoi = ''">Hủy</Button>
                </div>
              </div>
            </Card>

            <Card class="space-y-2">
              <h3 class="mb-3 text-sm font-bold text-slate-800">Trạng thái tài khoản</h3>
              <Button v-if="khachHang?.trangThai === 1" variant="soft" class="w-full justify-center text-rose-600 bg-rose-50 hover:bg-rose-100" @click="doiTrangThai(0)">🔒 Khóa tài khoản</Button>
              <Button v-else variant="soft" class="w-full justify-center text-emerald-600 bg-emerald-50 hover:bg-emerald-100" @click="doiTrangThai(1)">✓ Kích hoạt tài khoản</Button>
            </Card>
          </template>
        </div>

        <!-- Main content -->
        <div class="space-y-5">
          <!-- Thông tin cơ bản -->
          <Card class="space-y-5">
            <div class="flex items-center gap-3 mb-2">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-violet-50 text-violet-500">
                <User class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Thông tin cơ bản</h2>
                <p class="text-sm text-slate-400">Họ tên, email, liên hệ và tài khoản.</p>
              </div>
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Họ và tên <span class="text-rose-500">*</span></span>
                <input v-model="form.hoTen" type="text" placeholder="Nhập họ tên" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.hoTen ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.hoTen" class="text-xs text-rose-500">{{ loiForm.hoTen }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Email</span>
                <input v-model="form.email" type="email" placeholder="Nhập email" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.email ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.email" class="text-xs text-rose-500">{{ loiForm.email }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Số điện thoại</span>
                <input v-model="form.sdt" type="tel" placeholder="VD: 0901234567" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.sdt ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.sdt" class="text-xs text-rose-500">{{ loiForm.sdt }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Giới tính</span>
                <select v-model="form.gioiTinh" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white transition">
                  <option value="">-- Chọn giới tính --</option>
                  <option value="1">Nam</option>
                  <option value="0">Nữ</option>
                  <option value="2">Khác</option>
                </select>
              </label>

              <label class="space-y-2 sm:col-span-2">
                <span class="text-[13px] font-semibold text-slate-500">Ngày sinh</span>
                <input v-model="form.ngaySinh" type="date" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiForm.ngaySinh ? 'border-rose-500 focus:border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiForm.ngaySinh" class="text-xs text-rose-500">{{ loiForm.ngaySinh }}</p>
              </label>
            </div>


            <!-- Nút Lưu thay đổi chỉ hiện ở đây khi chỉnh sửa -->
            <div v-if="!laMoi" class="flex items-center gap-3 pt-4 border-t border-slate-100">
              <Button variant="primary" class="px-6" @click="luu" :disabled="dangLuu">
                <Save class="h-4 w-4 mr-2" />
                {{ dangLuu ? "Đang lưu..." : "Lưu thay đổi" }}
              </Button>
            </div>
          </Card>

          <!-- Địa chỉ giao hàng (chỉ khi thêm mới) -->
          <Card v-if="laMoi" class="space-y-5">
            <div class="flex items-center gap-3 mb-2">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-sky-50 text-sky-500">
                <MapPin class="h-5 w-5" />
              </div>
              <div>
                <h2 class="text-base font-bold text-slate-800">Địa chỉ giao hàng <span class="text-rose-500">*</span></h2>
                <p class="text-sm text-slate-400">Địa chỉ mặc định của khách hàng mới.</p>
              </div>
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Họ tên người nhận <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.hoTen" type="text" placeholder="Họ tên người nhận" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.hoTen ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiDiaChi.hoTen" class="text-xs text-rose-500">{{ loiDiaChi.hoTen }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.sdt" type="tel" placeholder="VD: 0901234567" :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.sdt ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiDiaChi.sdt" class="text-xs text-rose-500">{{ loiDiaChi.sdt }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
                <select
                  :value="maTinhChon"
                  @change="onTinhChange(Number(($event.target as HTMLSelectElement).value) || null)"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.tinhThanh ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn tỉnh/thành --</option>
                  <option v-for="t in dsTinh" :key="t.code" :value="t.code">{{ t.name }}</option>
                </select>
                <p v-if="loiDiaChi.tinhThanh" class="text-xs text-rose-500">{{ loiDiaChi.tinhThanh }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Quận/Huyện <span class="text-rose-500">*</span></span>
                <select
                  :value="maHuyenChon"
                  @change="onHuyenChange(Number(($event.target as HTMLSelectElement).value) || null)"
                  :disabled="!maTinhChon"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white disabled:opacity-50', loiDiaChi.quanHuyen ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn quận/huyện --</option>
                  <option v-for="h in dsHuyen" :key="h.code" :value="h.code">{{ h.name }}</option>
                </select>
                <p v-if="loiDiaChi.quanHuyen" class="text-xs text-rose-500">{{ loiDiaChi.quanHuyen }}</p>
              </label>

              <label class="space-y-2">
                <span class="text-[13px] font-semibold text-slate-500">Phường/Xã <span class="text-rose-500">*</span></span>
                <select
                  :value="formDiaChi.phuongXa"
                  @change="formDiaChi.phuongXa = ($event.target as HTMLSelectElement).value"
                  :disabled="!maHuyenChon"
                  :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white disabled:opacity-50', loiDiaChi.phuongXa ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']"
                >
                  <option value="">-- Chọn phường/xã --</option>
                  <option v-for="x in dsXa" :key="x.code" :value="x.name">{{ x.name }}</option>
                </select>
                <p v-if="loiDiaChi.phuongXa" class="text-xs text-rose-500">{{ loiDiaChi.phuongXa }}</p>
              </label>

              <label class="space-y-2 sm:col-span-2">
                <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
                <input v-model="formDiaChi.diaChiCuThe" type="text" placeholder="Số nhà, tên đường..." :class="['h-11 w-full rounded-2xl border bg-slate-50 px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.diaChiCuThe ? 'border-rose-500' : 'border-slate-200 focus:border-rose-300']" />
                <p v-if="loiDiaChi.diaChiCuThe" class="text-xs text-rose-500">{{ loiDiaChi.diaChiCuThe }}</p>
              </label>
            </div>

            <!-- Nút Tạo khách hàng nằm dưới cùng form địa chỉ -->
            <div class="flex items-center gap-3 pt-4 border-t border-slate-100">
              <Button variant="primary" class="px-6" @click="luu" :disabled="dangLuu">
                <Save class="h-4 w-4 mr-2" />
                {{ dangLuu ? "Đang tạo..." : "Tạo khách hàng" }}
              </Button>
            </div>
          </Card>

          <!-- Sổ địa chỉ (chỉ khi chỉnh sửa) -->
          <Card v-else class="space-y-5">
            <div class="flex items-center justify-between gap-3">
              <div class="flex items-center gap-3">
                <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-sky-50 text-sky-500">
                  <MapPin class="h-5 w-5" />
                </div>
                <div>
                  <h2 class="text-base font-bold text-slate-800">Sổ địa chỉ</h2>
                  <p class="text-sm text-slate-400">Quản lý các địa chỉ giao hàng của khách.</p>
                </div>
              </div>
              <Button v-if="!hienFormDiaChi" variant="primary" size="sm" @click="moThemDiaChi">
                <Plus class="h-4 w-4 mr-1" /> Thêm địa chỉ
              </Button>
            </div>

            <!-- Form thêm/sửa địa chỉ -->
            <div v-if="hienFormDiaChi" class="space-y-4 rounded-2xl border border-slate-100 bg-slate-50/60 p-5">
              <div class="flex items-center justify-between">
                <h3 class="text-sm font-bold text-slate-700">{{ diaChiDangSua ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
                <Button variant="ghost" size="icon" class="h-7 w-7 rounded-full bg-slate-100 text-slate-500 hover:bg-slate-200" @click="huyFormDiaChi">
                  <X class="h-4 w-4" />
                </Button>
              </div>
              <div class="grid gap-4 sm:grid-cols-2">
                <label class="space-y-1.5">
                  <span class="text-[13px] font-semibold text-slate-500">Họ tên người nhận <span class="text-rose-500">*</span></span>
                  <input v-model="formDiaChi.hoTen" type="text" placeholder="Họ tên người nhận" :class="['h-10 w-full rounded-2xl border bg-white px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.hoTen ? 'border-rose-500' : 'border-slate-200 focus:border-primary/50']" />
                  <p v-if="loiDiaChi.hoTen" class="text-xs text-rose-500">{{ loiDiaChi.hoTen }}</p>
                </label>

                <label class="space-y-1.5">
                  <span class="text-[13px] font-semibold text-slate-500">Số điện thoại <span class="text-rose-500">*</span></span>
                  <input v-model="formDiaChi.sdt" type="tel" placeholder="VD: 0901234567" :class="['h-10 w-full rounded-2xl border bg-white px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.sdt ? 'border-rose-500' : 'border-slate-200 focus:border-primary/50']" />
                  <p v-if="loiDiaChi.sdt" class="text-xs text-rose-500">{{ loiDiaChi.sdt }}</p>
                </label>

                <label class="space-y-1.5">
                  <span class="text-[13px] font-semibold text-slate-500">Tỉnh/Thành phố <span class="text-rose-500">*</span></span>
                  <select
                    :value="maTinhChon"
                    @change="onTinhChange(Number(($event.target as HTMLSelectElement).value) || null)"
                    :class="['h-10 w-full rounded-2xl border bg-white px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.tinhThanh ? 'border-rose-500' : 'border-slate-200 focus:border-primary/50']"
                  >
                    <option value="">-- Chọn tỉnh/thành --</option>
                    <option v-for="t in dsTinh" :key="t.code" :value="t.code">{{ t.name }}</option>
                  </select>
                  <p v-if="loiDiaChi.tinhThanh" class="text-xs text-rose-500">{{ loiDiaChi.tinhThanh }}</p>
                </label>

                <label class="space-y-1.5">
                  <span class="text-[13px] font-semibold text-slate-500">Quận/Huyện <span class="text-rose-500">*</span></span>
                  <select
                    :value="maHuyenChon"
                    @change="onHuyenChange(Number(($event.target as HTMLSelectElement).value) || null)"
                    :disabled="!maTinhChon"
                    :class="['h-10 w-full rounded-2xl border bg-white px-4 text-sm outline-none transition focus:bg-white disabled:opacity-50', loiDiaChi.quanHuyen ? 'border-rose-500' : 'border-slate-200 focus:border-primary/50']"
                  >
                    <option value="">-- Chọn quận/huyện --</option>
                    <option v-for="h in dsHuyen" :key="h.code" :value="h.code">{{ h.name }}</option>
                  </select>
                  <p v-if="loiDiaChi.quanHuyen" class="text-xs text-rose-500">{{ loiDiaChi.quanHuyen }}</p>
                </label>

                <label class="space-y-1.5">
                  <span class="text-[13px] font-semibold text-slate-500">Phường/Xã <span class="text-rose-500">*</span></span>
                  <select
                    :value="formDiaChi.phuongXa"
                    @change="formDiaChi.phuongXa = ($event.target as HTMLSelectElement).value"
                    :disabled="!maHuyenChon"
                    :class="['h-10 w-full rounded-2xl border bg-white px-4 text-sm outline-none transition focus:bg-white disabled:opacity-50', loiDiaChi.phuongXa ? 'border-rose-500' : 'border-slate-200 focus:border-primary/50']"
                  >
                    <option value="">-- Chọn phường/xã --</option>
                    <option v-for="x in dsXa" :key="x.code" :value="x.name">{{ x.name }}</option>
                  </select>
                  <p v-if="loiDiaChi.phuongXa" class="text-xs text-rose-500">{{ loiDiaChi.phuongXa }}</p>
                </label>

                <label class="space-y-1.5 sm:col-span-2">
                  <span class="text-[13px] font-semibold text-slate-500">Địa chỉ cụ thể <span class="text-rose-500">*</span></span>
                  <input v-model="formDiaChi.diaChiCuThe" type="text" placeholder="Số nhà, tên đường..." :class="['h-10 w-full rounded-2xl border bg-white px-4 text-sm outline-none transition focus:bg-white', loiDiaChi.diaChiCuThe ? 'border-rose-500' : 'border-slate-200 focus:border-primary/50']" />
                  <p v-if="loiDiaChi.diaChiCuThe" class="text-xs text-rose-500">{{ loiDiaChi.diaChiCuThe }}</p>
                </label>

                <div class="sm:col-span-2 flex items-center gap-2">
                  <input v-model="formDiaChi.laMacDinh" type="checkbox" id="laMacDinhEdit" class="h-4 w-4 rounded" />
                  <label for="laMacDinhEdit" class="text-sm font-semibold text-slate-600 cursor-pointer">Đặt làm địa chỉ mặc định</label>
                </div>
              </div>
              <div class="flex gap-3 pt-1">
                <Button variant="primary" class="flex-1 justify-center" :disabled="dangLuuDiaChi" @click="luuDiaChi">
                  {{ dangLuuDiaChi ? "Đang lưu..." : "Lưu địa chỉ" }}
                </Button>
                <Button variant="soft" class="flex-1 justify-center" @click="huyFormDiaChi">Hủy</Button>
              </div>
            </div>

            <!-- Bảng danh sách địa chỉ -->
            <div v-else>
              <div v-if="dangTaiDsDiaChi" class="py-8 text-center text-sm text-slate-400">Đang tải địa chỉ...</div>
              <div v-else-if="!dsDiaChi.length" class="py-10 text-center border-2 border-dashed border-slate-100 rounded-3xl text-slate-400 text-sm">
                Khách hàng chưa có địa chỉ nào.
              </div>
              <div v-else class="admin-table-scroll">
                <Table>
                  <template #header>
                    <th class="px-3 py-3 whitespace-nowrap">Người nhận</th>
                    <th class="px-3 py-3 whitespace-nowrap">Số điện thoại</th>
                    <th class="px-3 py-3">Địa chỉ</th>
                    <th class="px-3 py-3 text-center whitespace-nowrap">Mặc định</th>
                    <th class="px-3 py-3 text-center whitespace-nowrap">Hành động</th>
                  </template>
                  <template #body>
                    <tr
                      v-for="dc in dsDiaChi"
                      :key="dc.id"
                      class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
                      :class="dc.laMacDinh ? 'ring-emerald-200' : ''"
                    >
                      <td class="rounded-l-2xl px-3 py-3 font-semibold text-slate-800 whitespace-nowrap">{{ dc.hoTen }}</td>
                      <td class="px-3 py-3 text-slate-600 whitespace-nowrap">{{ dc.sdt }}</td>
                      <td class="px-3 py-3 text-slate-600">
                        <div class="min-w-[200px] leading-relaxed">{{ dc.diaChiCuThe }}, {{ dc.phuongXa }}, {{ dc.quanHuyen }}, {{ dc.tinhThanh }}</div>
                      </td>
                      <td class="px-3 py-3 text-center">
                        <span v-if="dc.laMacDinh" class="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2 py-0.5 text-[11px] font-bold text-emerald-700">
                          <CheckCircle2 class="h-3 w-3" /> Mặc định
                        </span>
                        <button
                          v-else
                          @click="datLamMacDinh(dc.id)"
                          class="rounded-full bg-slate-100 px-2.5 py-0.5 text-[11px] font-semibold text-slate-500 hover:bg-emerald-50 hover:text-emerald-600 transition"
                        >
                          Đặt mặc định
                        </button>
                      </td>
                      <td class="rounded-r-2xl px-3 py-3 text-center">
                        <div class="flex items-center justify-center gap-1">
                          <button @click="moSuaDiaChi(dc)" class="admin-table-action text-sky-500 hover:text-sky-700" title="Sửa địa chỉ">
                            <Pencil :size="14" />
                          </button>
                          <button
                            v-if="!dc.laMacDinh"
                            @click="xoaDiaChiKhachHang(dc.id)"
                            class="admin-table-action text-rose-500 hover:text-rose-700"
                            title="Xóa địa chỉ"
                          >
                            <Trash2 :size="14" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  </template>
                </Table>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </template>
  </div>
</template>
