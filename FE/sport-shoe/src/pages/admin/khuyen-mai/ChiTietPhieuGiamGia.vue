<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, CheckCircle2, CheckSquare, CircleX, RefreshCcw, Save, Search, Ticket, Users, X } from "lucide-vue-next";
import {
  createPhieuGiamGia,
  createPhieuGiamGiaKhachHang,
  deletePhieuGiamGiaKhachHang,
  getPhieuGiamGiaDetail,
  getPhieuGiamGiaKhachHangList,
  updatePhieuGiamGia
} from "../../../services/khuyen-mai";
import { layDanhSachKhachHang } from "../../../services/khach-hang";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const route = useRoute();
const router = useRouter();

const id = route.params.id;
const laMoi = !id;

const dangTai = ref(false);
const saving = ref(false);
const loiTrang = ref("");
const toast = ref({
  hienThi: false,
  loai: "success",
  tieuDe: "",
  noiDung: "",
});
let toastTimer = null;

const toastClass = computed(() => {
  if (toast.value.loai === "success") return "border-emerald-100 bg-emerald-50 text-emerald-700";
  if (toast.value.loai === "warning") return "border-amber-100 bg-amber-50 text-amber-700";
  return "border-rose-100 bg-rose-50 text-rose-700";
});

const toastIconClass = computed(() => {
  if (toast.value.loai === "success") return "bg-emerald-100 text-emerald-600";
  if (toast.value.loai === "warning") return "bg-amber-100 text-amber-600";
  return "bg-rose-100 text-rose-600";
});

const toastAccentClass = computed(() => {
  if (toast.value.loai === "success") return "bg-emerald-500";
  if (toast.value.loai === "warning") return "bg-amber-500";
  return "bg-rose-500";
});

const ToastIcon = computed(() => {
  if (toast.value.loai === "success") return CheckCircle2;
  return CircleX;
});

function hienThiThongBao(loai, tieuDe, noiDung = "") {
  if (toastTimer) clearTimeout(toastTimer);
  toast.value = { hienThi: true, loai, tieuDe, noiDung };
  toastTimer = setTimeout(() => {
    toast.value.hienThi = false;
  }, 3200);
}

const formErrors = reactive({});

const form = reactive({
  id: null,
  ma: "",
  ten: "",
  loai: "1",
  loaiPhieu: "1",
  giaTri: "",
  giaTriToiThieu: "0",
  giamToiDa: "0",
  ngayBatDau: "",
  ngayKetThuc: "",
  soLuong: "",
  trangThai: "1"
});

const isReadOnly = computed(() => !laMoi && (Number(form.trangThai) === 0 || Number(form.trangThai) === 2));

const searchKh = ref("");
const danhSachKh = ref([]);
const dsEmailChon = ref([]);
const dangTaiKh = ref(false);
const lienKetKhachHangHienTai = ref([]);
const soLuongPhieuCongKhai = ref("");

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function parseVndNumber(value) {
  const rawValue = String(value ?? "").replace(/[^\d]/g, "");
  return rawValue ? Number(rawValue) : 0;
}

function formatVndNumber(value) {
  const numberValue = parseVndNumber(value);
  return numberValue ? numberValue.toLocaleString("vi-VN") : "0";
}

function handleVndInput(field, event) {
  form[field] = formatVndNumber(event.target.value);
}

function resetErrors() {
  Object.keys(formErrors).forEach((key) => delete formErrors[key]);
}

function taoMaNgauNhien() {
  form.ma = `VCH${Math.random().toString(36).substring(2, 8).toUpperCase()}`;
}

function dongBoSoLuongPhieuCaNhan() {
  if (form.loaiPhieu === "2") {
    form.soLuong = String(Math.max(1, dsEmailChon.value.length));
  }
}

async function taiDanhSachKh() {
  dangTaiKh.value = true;
  try {
    const res = await layDanhSachKhachHang({ keyword: searchKh.value, page: 0, size: 50 });
    danhSachKh.value = Array.isArray(res) ? res : (res?.content || []);
  } catch (error) {
    console.error("Lỗi tải khách hàng:", error);
  } finally {
    dangTaiKh.value = false;
  }
}

async function taiLienKetKhachHangTheoPhieu(maPhieu) {
  if (!maPhieu) {
    lienKetKhachHangHienTai.value = [];
    dsEmailChon.value = [];
    return;
  }

  const data = await getPhieuGiamGiaKhachHangList({
    keyword: maPhieu,
    pageNo: 0,
    pageSize: 1000
  });

  lienKetKhachHangHienTai.value = (data?.content || []).filter(
    (item) => item?.maPhieuGiamGia === maPhieu && item?.email
  );
  dsEmailChon.value = lienKetKhachHangHienTai.value.map((item) => item.email);
  dongBoSoLuongPhieuCaNhan();
}

function toggleEmail(email) {
  if (!email) return;

  const index = dsEmailChon.value.indexOf(email);
  if (index === -1) {
    dsEmailChon.value.push(email);
    return;
  }

  dsEmailChon.value.splice(index, 1);
}

function chonTatCa() {
  const emailsTrang = danhSachKh.value.map((kh) => kh.email).filter(Boolean);
  const daChonHet = emailsTrang.length > 0 && emailsTrang.every((email) => dsEmailChon.value.includes(email));

  if (daChonHet) {
    dsEmailChon.value = dsEmailChon.value.filter((email) => !emailsTrang.includes(email));
    return;
  }

  dsEmailChon.value = Array.from(new Set([...dsEmailChon.value, ...emailsTrang]));
}

let searchTimer;
function handleSearch() {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(taiDanhSachKh, 400);
}

watch(() => form.loaiPhieu, (loaiPhieuMoi, loaiPhieuCu) => {
  if (loaiPhieuMoi === "2") {
    soLuongPhieuCongKhai.value = form.soLuong;
    dongBoSoLuongPhieuCaNhan();
    return;
  }

  delete formErrors.email;
  if (loaiPhieuCu === "2") {
    form.soLuong = soLuongPhieuCongKhai.value;
  }
});

watch(dsEmailChon, () => {
  if (form.loaiPhieu === "2") {
    dongBoSoLuongPhieuCaNhan();
  }
}, { deep: true });

watch(() => form.giaTri, (newVal) => {
  if (newVal === "" || newVal === null || newVal === undefined) {
    delete formErrors.giaTri;
    return;
  }
  if (Number(form.loai) === 1) {
    const val = Number(newVal);
    if (val <= 0) {
      formErrors.giaTri = "Giá trị giảm phải lớn hơn 0%";
    } else if (val > 100) {
      formErrors.giaTri = "Phần trăm giảm không được vượt quá 100%";
    } else {
      delete formErrors.giaTri;
    }
  } else {
    const val = parseVndNumber(newVal);
    if (val <= 0) {
      formErrors.giaTri = "Giá trị giảm phải lớn hơn 0";
    } else {
      delete formErrors.giaTri;
    }
  }
});

watch(() => form.loai, () => {
  delete formErrors.giaTri;
  form.giaTri = "";
});

async function taiChiTiet() {
  await taiDanhSachKh();

  if (laMoi) {
    if (!form.ma) {
      taoMaNgauNhien();
    }
    form.ngayBatDau = getToday();
    return;
  }

  dangTai.value = true;
  try {
    const detail = await getPhieuGiamGiaDetail(id);
    const loai = String(detail.loai ?? 1);
    const loaiPhieu = String(detail.loaiPhieu ?? 1);
    const soLuong = String(detail.soLuong ?? "");

    Object.assign(form, {
      id: detail.id,
      ma: detail.ma ?? "",
      ten: detail.ten ?? "",
      loai,
      loaiPhieu,
      giaTri: loai === "2" ? formatVndNumber(detail.giaTri ?? 0) : String(detail.giaTri ?? ""),
      giaTriToiThieu: formatVndNumber(detail.giaTriToiThieu ?? 0),
      giamToiDa: formatVndNumber(detail.giamToiDa ?? 0),
      ngayBatDau: detail.ngayBatDau ?? "",
      ngayKetThuc: detail.ngayKetThuc ?? "",
      soLuong,
      trangThai: String(detail.trangThai ?? 1)
    });

    if (loaiPhieu === "2") {
      await taiLienKetKhachHangTheoPhieu(detail.ma);
    } else {
      soLuongPhieuCongKhai.value = soLuong;
      lienKetKhachHangHienTai.value = [];
      dsEmailChon.value = [];
    }
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể tải chi tiết phiếu giảm giá");
  } finally {
    dangTai.value = false;
  }
}

async function dongBoKhachHangPhieuCaNhan(phieuId, maPhieu) {
  const emailsDaChon = new Set(dsEmailChon.value.filter(Boolean));
  const lienKetHienTai = lienKetKhachHangHienTai.value.filter((item) => item?.id && item?.email);
  const emailHienTai = new Set(lienKetHienTai.map((item) => item.email));

  const emailsCanThem = [...emailsDaChon].filter((email) => !emailHienTai.has(email));
  const lienKetCanXoa = lienKetHienTai.filter((item) => !emailsDaChon.has(item.email));

  await Promise.all(
    emailsCanThem.map((email) => createPhieuGiamGiaKhachHang({
      phieuGiamGiaId: phieuId,
      email,
      trangThai: 1
    }))
  );

  await Promise.all(lienKetCanXoa.map((item) => deletePhieuGiamGiaKhachHang(item.id)));

  await taiLienKetKhachHangTheoPhieu(maPhieu);
}

async function submitForm() {
  resetErrors();
  let isValid = true;

  if (!form.ma.trim()) {
    formErrors.ma = "Vui lòng nhập mã phiếu giảm giá";
    isValid = false;
  }
  if (!form.ten.trim()) {
    formErrors.ten = "Vui lòng nhập tên phiếu giảm giá";
    isValid = false;
  }
  if (Number(form.loai) === 1) {
    const val = Number(form.giaTri);
    if (!form.giaTri || val <= 0) {
      formErrors.giaTri = "Giá trị giảm phải lớn hơn 0";
      isValid = false;
    } else if (val > 100) {
      formErrors.giaTri = "Phần trăm giảm không được vượt quá 100%";
      isValid = false;
    }
  } else {
    const val = parseVndNumber(form.giaTri);
    if (!form.giaTri || val <= 0) {
      formErrors.giaTri = "Giá trị giảm phải lớn hơn 0";
      isValid = false;
    }
  }
  if (form.giaTriToiThieu && parseVndNumber(form.giaTriToiThieu) < 1) {
    formErrors.giaTriToiThieu = "Giá trị đơn tối thiểu phải lớn hơn 0";
    isValid = false;
  }
  if (!form.soLuong || Number(form.soLuong) <= 0) {
    formErrors.soLuong = "Số lượng phiếu phải lớn hơn 0";
    isValid = false;
  }
  if (!form.ngayBatDau) {
    formErrors.ngayBatDau = "Vui lòng chọn ngày bắt đầu áp dụng";
    isValid = false;
  } else if (laMoi && form.ngayBatDau < getToday()) {
    formErrors.ngayBatDau = "Ngày bắt đầu không được chọn trong quá khứ";
    isValid = false;
  }

  if (!form.ngayKetThuc) {
    formErrors.ngayKetThuc = "Vui lòng chọn ngày kết thúc áp dụng";
    isValid = false;
  } else if (form.ngayKetThuc < getToday()) {
    formErrors.ngayKetThuc = "Ngày kết thúc không được chọn trong quá khứ";
    isValid = false;
  }
  if (form.ngayBatDau && form.ngayKetThuc && form.ngayBatDau > form.ngayKetThuc) {
    formErrors.ngayKetThuc = "Ngày kết thúc phải sau ngày bắt đầu";
    isValid = false;
  }
  if (form.loaiPhieu === "2" && dsEmailChon.value.length === 0) {
    formErrors.email = "Phải chọn ít nhất 1 khách hàng cho phiếu cá nhân";
    hienThiThongBao("warning", "Chưa chọn khách hàng", "Vui lòng chọn ít nhất một khách hàng cho phiếu cá nhân");
    isValid = false;
  }

  if (!isValid) return;

  saving.value = true;
  loiTrang.value = "";

  try {
    const payload = {
      ma: form.ma.trim(),
      ten: form.ten.trim(),
      loai: Number(form.loai),
      loaiPhieu: Number(form.loaiPhieu),
      giaTri: parseVndNumber(form.giaTri),
      giaTriToiThieu: parseVndNumber(form.giaTriToiThieu),
      giamToiDa: parseVndNumber(form.giamToiDa),
      ngayBatDau: form.ngayBatDau,
      ngayKetThuc: form.ngayKetThuc,
      soLuong: Number(form.soLuong),
      trangThai: laMoi ? undefined : form.trangThai,
      ngayTao: laMoi ? getToday() : undefined,
      ngayCapNhat: !laMoi ? getToday() : undefined
    };

    let phieuId = id;

    if (laMoi) {
      const res = await createPhieuGiamGia(payload);
      phieuId = res?.id;
      hienThiThongBao("success", "Tạo phiếu giảm giá thành công");
    } else {
      await updatePhieuGiamGia(id, payload);
      hienThiThongBao("success", "Cập nhật phiếu giảm giá thành công");
    }

    if (Number(form.loaiPhieu) === 2) {
      await dongBoKhachHangPhieuCaNhan(phieuId, payload.ma);
    } else if (lienKetKhachHangHienTai.value.length) {
      await Promise.all(lienKetKhachHangHienTai.value.map((item) => deletePhieuGiamGiaKhachHang(item.id)));
      lienKetKhachHangHienTai.value = [];
      dsEmailChon.value = [];
    }

    setTimeout(() => {
      router.push({ name: "admin-phieu-giam-gia" });
    }, 900);
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(error, "Không thể lưu phiếu giảm giá");
  } finally {
    saving.value = false;
  }
}

onMounted(taiChiTiet);
</script>

<template>
  <div class="space-y-5 pb-10">
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="translate-y-3 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-3 opacity-0"
    >
      <div
        v-if="toast.hienThi"
        class="fixed right-5 top-5 z-[70] w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-2xl border bg-white shadow-[0_18px_60px_rgba(15,23,42,0.18)]"
        :class="toastClass"
      >
        <div class="flex gap-3 p-4">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full" :class="toastIconClass">
            <component :is="ToastIcon" class="h-5 w-5" />
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-bold text-slate-800">{{ toast.tieuDe }}</p>
            <p v-if="toast.noiDung" class="mt-1 text-sm leading-5 text-slate-600">{{ toast.noiDung }}</p>
          </div>
          <button type="button" class="rounded-full p-1 text-slate-400 transition hover:bg-white/70 hover:text-slate-600" @click="toast.hienThi = false">
            <X class="h-4 w-4" />
          </button>
        </div>
        <div class="h-1.5 w-full" :class="toastAccentClass"></div>
      </div>
    </Transition>

    <section class="flex items-center gap-4">
      <button
        @click="router.push({ name: 'admin-phieu-giam-gia' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <h1 class="text-[26px] font-bold tracking-tight text-slate-800">
          {{ laMoi ? "Thêm phiếu giảm giá mới" : "Chi tiết phiếu giảm giá" }}
        </h1>
      </div>
    </section>

    <div v-if="loiTrang" class="rounded-2xl border border-rose-100 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-600">{{ loiTrang }}</div>
    
    <div v-if="isReadOnly" class="rounded-2xl border border-amber-100 bg-amber-50 px-5 py-3 text-sm font-medium text-amber-700">
      Phiếu giảm giá này đã hết hạn hoặc ngừng hoạt động nên chỉ có thể xem chi tiết.
    </div>

    <section class="space-y-6 rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
      <fieldset :disabled="isReadOnly" class="space-y-6">
        <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-rose-50 text-rose-500">
          <Ticket class="h-5 w-5" />
        </div>
        <div>
          <h2 class="text-base font-bold text-slate-800">Thông tin phiếu</h2>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <div class="min-w-0 space-y-2">
          <label class="block whitespace-nowrap text-[13px] font-semibold text-slate-500">Mã phiếu <span class="text-rose-500">*</span></label>
          <div class="relative">
            <input v-model="form.ma" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-4 pr-11 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white disabled:opacity-70 disabled:bg-slate-100" placeholder="Ví dụ: VOUCHER2024" />
            <button v-if="!isReadOnly" @click="taoMaNgauNhien" type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 transition-colors hover:text-rose-500">
              <RefreshCcw class="h-4 w-4" />
            </button>
          </div>
          <p v-if="formErrors.ma" class="mt-1 text-xs text-rose-500">{{ formErrors.ma }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block whitespace-nowrap text-[13px] font-semibold text-slate-500">Tên phiếu <span class="text-rose-500">*</span></label>
          <input v-model="form.ten" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" placeholder="Ví dụ: Giảm giá hè 2024" />
          <p v-if="formErrors.ten" class="mt-1 text-xs text-rose-500">{{ formErrors.ten }}</p>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Hình thức phiếu <span class="text-rose-500">*</span></label>
          <div class="flex gap-6 pt-2">
            <label class="group flex cursor-pointer items-center gap-2">
              <input type="radio" v-model="form.loaiPhieu" value="1" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-normal text-slate-600">Công khai</span>
            </label>
            <label class="group flex cursor-pointer items-center gap-2">
              <input type="radio" v-model="form.loaiPhieu" value="2" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-normal text-slate-600">Cá nhân</span>
            </label>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-[13px] font-semibold text-slate-500">Loại giảm <span class="text-rose-500">*</span></label>
          <div class="flex gap-6 pt-2">
            <label class="group flex cursor-pointer items-center gap-2">
              <input type="radio" v-model="form.loai" value="1" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-normal text-slate-600">Phần trăm (%)</span>
            </label>
            <label class="group flex cursor-pointer items-center gap-2">
              <input type="radio" v-model="form.loai" value="2" class="h-5 w-5 border-slate-300 text-rose-500 focus:ring-rose-500" />
              <span class="text-sm font-normal text-slate-600">Tiền mặt (VNĐ)</span>
            </label>
          </div>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block whitespace-nowrap text-[13px] font-semibold text-slate-500">Giá trị giảm ({{ form.loai === '1' ? '%' : 'VNĐ' }}) <span class="text-rose-500">*</span></label>
          <div class="relative">
            <input :value="form.giaTri" :type="form.loai === '1' ? 'number' : 'text'" :min="form.loai === '1' ? '1' : undefined" :max="form.loai === '1' ? '100' : undefined" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" :placeholder="form.loai === '1' ? '0' : '0'" @input="form.loai === '2' ? handleVndInput('giaTri', $event) : form.giaTri = $event.target.value" />
            <span class="absolute right-4 top-1/2 -translate-y-1/2 text-[11px] font-bold text-slate-400">{{ form.loai === '1' ? '%' : 'VNĐ' }}</span>
          </div>
          <p v-if="formErrors.giaTri" class="mt-1 text-xs text-rose-500">{{ formErrors.giaTri }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block whitespace-nowrap text-[13px] font-semibold text-slate-500">Giá trị đơn tối thiểu (VNĐ)</label>
          <input :value="form.giaTriToiThieu" type="text" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" @input="handleVndInput('giaTriToiThieu', $event)" />
          <p v-if="formErrors.giaTriToiThieu" class="mt-1 text-xs text-rose-500">{{ formErrors.giaTriToiThieu }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block whitespace-nowrap text-[13px] font-semibold text-slate-500">Giảm tối đa (VNĐ)</label>
          <input :value="form.giamToiDa" type="text" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white" @input="handleVndInput('giamToiDa', $event)" />
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block whitespace-nowrap text-[13px] font-semibold text-slate-500">Số lượng <span class="text-rose-500">*</span></label>
          <input
            v-model="form.soLuong"
            type="number"
            min="1"
            :readonly="form.loaiPhieu === '2'"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300 focus:bg-white"
            :class="form.loaiPhieu === '2' ? 'cursor-not-allowed bg-slate-100 text-slate-500 focus:border-slate-200 focus:bg-slate-100' : ''"
          />
          <p v-if="formErrors.soLuong" class="mt-1 text-xs text-rose-500">{{ formErrors.soLuong }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block whitespace-nowrap text-[13px] font-semibold text-slate-500">Ngày bắt đầu <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayBatDau" type="date" readonly class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-100 px-4 text-sm font-normal text-slate-500 outline-none cursor-not-allowed" />
          <p v-if="formErrors.ngayBatDau" class="mt-1 text-xs text-rose-500">{{ formErrors.ngayBatDau }}</p>
        </div>

        <div class="min-w-0 space-y-2">
          <label class="block whitespace-nowrap text-[13px] font-semibold text-slate-500">Ngày kết thúc <span class="text-rose-500">*</span></label>
          <input v-model="form.ngayKetThuc" type="date" :min="form.ngayBatDau || getToday()" class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm font-normal text-slate-950 outline-none transition focus:border-rose-300 focus:bg-white" />
          <p v-if="formErrors.ngayKetThuc" class="mt-1 text-xs text-rose-500">{{ formErrors.ngayKetThuc }}</p>
        </div>
      </div>

      <div v-if="form.loaiPhieu === '2'" class="mt-6 space-y-4 rounded-3xl border border-slate-100 bg-slate-50/30 p-5">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3 text-slate-800">
            <Users class="h-5 w-5 text-rose-500" />
            <span class="text-sm font-bold">Chọn khách hàng mục tiêu</span>
          </div>
          <button v-if="!isReadOnly" type="button" @click="chonTatCa" class="text-xs font-semibold text-rose-500 transition-colors hover:text-rose-600">
            {{ danhSachKh.length > 0 && danhSachKh.every((kh) => dsEmailChon.includes(kh.email)) ? "Bỏ chọn tất cả" : "Chọn tất cả trang này" }}
          </button>
        </div>

        <div class="relative">
          <Search class="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input v-model="searchKh" type="text" placeholder="Tìm theo tên hoặc số điện thoại..." @input="handleSearch" class="h-11 w-full rounded-2xl border border-slate-200 bg-white pl-11 pr-4 text-sm font-normal text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-rose-300" />
        </div>

        <div class="custom-scrollbar max-h-[300px] overflow-y-auto rounded-2xl border border-slate-100 bg-white shadow-sm">
          <table class="w-full border-collapse text-left text-sm">
            <thead class="sticky top-0 z-10 bg-slate-50 text-[12px] font-semibold text-slate-500">
              <tr>
                <th class="w-12 px-4 py-3 text-center">#</th>
                <th class="px-4 py-3">Khách hàng</th>
                <th class="px-4 py-3">SĐT / Email</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="dangTaiKh">
                <td colspan="3" class="px-4 py-6 text-center text-sm text-slate-400">Đang tải danh sách khách hàng...</td>
              </tr>
              <tr v-else-if="!danhSachKh.length">
                <td colspan="3" class="px-4 py-6 text-center text-sm text-slate-400">Không có khách hàng phù hợp.</td>
              </tr>
              <tr v-for="kh in danhSachKh" v-else :key="kh.id" @click="toggleEmail(kh.email)" class="cursor-pointer transition-colors hover:bg-rose-50/50" :class="dsEmailChon.includes(kh.email) ? 'bg-rose-50/30' : ''">
                <td class="px-4 py-3 text-center">
                  <CheckSquare v-if="dsEmailChon.includes(kh.email)" class="mx-auto h-5 w-5 text-rose-500" />
                  <div v-else class="mx-auto h-5 w-5 rounded border border-slate-300 bg-white"></div>
                </td>
                <td class="px-4 py-3 font-semibold text-slate-800">{{ kh.hoTen }}</td>
                <td class="px-4 py-3 text-slate-500">{{ kh.sdt }}<br>{{ kh.email }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="text-xs font-medium text-slate-400">
          Đã chọn: <span class="font-bold text-slate-700">{{ dsEmailChon.length }}</span> khách hàng
        </div>
        <p v-if="formErrors.email" class="text-xs text-rose-500">{{ formErrors.email }}</p>
      </div>
      </fieldset>

      <div class="flex items-center gap-3 border-t border-slate-100 pt-6">
        <button v-if="!isReadOnly" @click="submitForm" :disabled="saving" class="inline-flex items-center gap-2 rounded-2xl bg-rose-500 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-rose-600 disabled:opacity-60">
          <Save class="h-4 w-4" />
          {{ saving ? "Đang lưu..." : (laMoi ? "Tạo phiếu giảm giá" : "Lưu thay đổi") }}
        </button>
        <button @click="router.push({ name: 'admin-phieu-giam-gia' })" class="whitespace-nowrap rounded-2xl border border-slate-200 bg-slate-50 px-6 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100">{{ isReadOnly ? "Quay lại" : "Hủy" }}</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
</style>
