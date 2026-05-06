<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Save } from "lucide-vue-next";
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
});

const form = ref({
  hoTen: "",
  email: "",
  matKhau: "",
  sdt: "",
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
      email: data.email ?? "",
      sdt: data.sdt ?? "",
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
  loiForm.value = { hoTen: "", email: "", sdt: "" };
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
    gioiTinh: form.value.gioiTinh || undefined,
    ngaySinh: form.value.ngaySinh || undefined,
    diaChi: gopDiaChi() || form.value.diaChiCuThe.trim() || undefined,
    hinhAnh: form.value.hinhAnh || undefined,
    vaiTro: form.value.vaiTro,
  };

  try {
    if (laMoi) {
      await taoNhanVien(payload);
      router.push({ name: "admin-nhan-vien" });
      return;
    }

    const updated = await capNhatNhanVien(id!, payload);
    nhanVien.value = updated;
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
            <h2 class="text-[18px] font-bold text-slate-900">Thông tin nhân viên</h2>
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

            <label class="mt-10 block space-y-2">
              <span class="text-[18px] tracking-[0.06em] text-black">
                Họ và tên <span class="text-rose-500">*</span>
              </span>
              <input
                v-model="form.hoTen"
                type="text"
                placeholder="Nhập họ và tên"
                :class="[
                  'h-14 w-full rounded-[18px] border bg-white px-5 text-[17px] text-slate-700 outline-none transition placeholder:text-slate-400',
                  loiForm.hoTen ? 'border-rose-400 ring-2 ring-rose-100' : 'border-slate-200 focus:border-rose-300',
                ]"
              />
              <p v-if="loiForm.hoTen" class="text-xs text-rose-500">{{ loiForm.hoTen }}</p>
            </label>
          </div>
        </section>

        <section class="rounded-[28px] border border-slate-200 bg-white px-8 py-9 shadow-sm">
          <h2 class="text-[18px] font-bold text-slate-900 text-black">Thông tin chi tiết</h2>
          <div class="mt-7 h-px bg-slate-200"></div>

          <div class="mt-10 grid gap-x-6 gap-y-7 xl:grid-cols-12" style="color: black;">
            <div class="space-y-2 xl:col-span-6">
              <span class="text-[18px] tracking-[0.06em] text-black">
                Giới tính <span class="text-rose-500">*</span>
              </span>
              <div class="flex h-14 items-center gap-8 px-1 text-[17px] text-slate-700">
                <label class="inline-flex items-center gap-3">
                  <input v-model="form.gioiTinh" type="radio" value="Nam" class="h-4 w-4 accent-cyan-600" />
                  <span>Nam</span>
                </label>
                <label class="inline-flex items-center gap-3">
                  <input v-model="form.gioiTinh" type="radio" value="Nữ" class="h-4 w-4 accent-cyan-600" />
                  <span>Nữ</span>
                </label>
              </div>
            </div>

            <label class="space-y-2 xl:col-span-6">
              <span class="text-[18px]  tracking-[0.06em] text-black">
                Ngày sinh <span class="text-rose-500">*</span>
              </span>
              <input
                v-model="form.ngaySinh"
                type="date"
                class="h-14 w-full rounded-[18px] border border-slate-200 bg-white px-5 text-[17px] text-slate-700 outline-none transition focus:border-slate-300"
              />
            </label>

            <label class="space-y-2 xl:col-span-6">
              <span class="text-[18px] tracking-[0.06em] text-black">
                Email <span class="text-rose-500">*</span>
              </span>
              <input
                v-model="form.email"
                type="email"
                placeholder="Nhập email"
                :class="[
                  'h-14 w-full rounded-[18px] border bg-white px-5 text-[17px] text-slate-700 outline-none transition placeholder:text-slate-400',
                  loiForm.email ? 'border-rose-400 ring-2 ring-rose-100' : 'border-slate-200 focus:border-slate-300',
                ]"
              />
              <p v-if="loiForm.email" class="text-xs text-rose-500">{{ loiForm.email }}</p>
            </label>

            <label class="space-y-2 xl:col-span-4">
              <span class="text-[18px] tracking-[0.06em] text-black">
                Tỉnh/Thành phố <span class="text-rose-500">*</span>
              </span>
              <select
                v-model="form.tinhThanh"
                class="h-14 w-full rounded-[18px] border border-slate-200 bg-white px-5 text-[17px] text-slate-700 outline-none transition focus:border-slate-300"
              >
                <option value="">Chọn tỉnh thành</option>
                <option v-for="item in dsTinhThanh" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-2 xl:col-span-4">
              <span class="text-[18px]  tracking-[0.06em] text-black">
                Quận/Huyện <span class="text-rose-500">*</span>
              </span>
              <select
                v-model="form.quanHuyen"
                class="h-14 w-full rounded-[18px] border border-slate-200 bg-white px-5 text-[17px] text-slate-700 outline-none transition focus:border-slate-300"
              >
                <option value="">Chọn quận huyện</option>
                <option v-for="item in dsQuanHuyen" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-2 xl:col-span-4">
              <span class="text-[18px]  tracking-[0.06em] text-black">
                Xã/Phường/Thị trấn <span class="text-rose-500">*</span>
              </span>
              <select
                v-model="form.xaPhuong"
                class="h-14 w-full rounded-[18px] border border-slate-200 bg-white px-5 text-[17px] text-slate-700 outline-none transition focus:border-slate-300"
              >
                <option value="">Chọn xã phường</option>
                <option v-for="item in dsXaPhuong" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-2 xl:col-span-6">
              <span class="text-[18px]  tracking-[0.06em] text-black">
                Số điện thoại <span class="text-rose-500">*</span>
              </span>
              <input
                v-model="form.sdt"
                type="tel"
                placeholder="Nhập số điện thoại"
                :class="[
                  'h-14 w-full rounded-[18px] border bg-white px-5 text-[17px] text-slate-700 outline-none transition placeholder:text-slate-400',
                  loiForm.sdt ? 'border-rose-400 ring-2 ring-rose-100' : 'border-slate-200 focus:border-slate-300',
                ]"
              />
              <p v-if="loiForm.sdt" class="text-xs text-rose-500">{{ loiForm.sdt }}</p>
            </label>

            <label class="space-y-2 xl:col-span-6">
              <span class="text-[18px] tracking-[0.06em] text-black">
                Vai trò <span class="text-rose-500">*</span>
              </span>
              <select
                v-model="form.vaiTro"
                class="h-14 w-full rounded-[18px] border border-slate-200 bg-white px-5 text-[17px] text-slate-700 outline-none transition focus:border-slate-300"
              >
                <option v-for="item in dsVaiTro" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </label>

            <label class="space-y-2 xl:col-span-12">
              <span class="text-[18px] tracking-[0.06em] text-black">
                Địa chỉ cụ thể <span class="text-rose-500">*</span>
              </span>
              <input
                v-model="form.diaChiCuThe"
                type="text"
                placeholder="Nhập địa chỉ cụ thể"
                class="h-14 w-full rounded-[18px] border border-slate-200 bg-white px-5 text-[17px] text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-slate-300"
              />
            </label>


          </div>

          <div class="mt-10 flex flex-wrap items-center gap-3 border-t border-slate-100 pt-6">
            <button
              @click="luu"
              :disabled="dangLuu"
              class="admin-btn-primary h-12 rounded-[18px] px-6 text-[15px] font-semibold"
            >
              <Save class="h-4 w-4" />
              {{ dangLuu ? "Đang lưu..." : laMoi ? "Tạo nhân viên" : "Lưu thay đổi" }}
            </button>
            <button
              type="button"
              @click="router.push({ name: 'admin-nhan-vien' })"
              class="admin-btn-soft h-12 rounded-[18px] px-6 text-[15px] font-semibold"
            >
              Hủy
            </button>
          </div>
        </section>
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
