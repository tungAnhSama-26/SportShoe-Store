<script setup>
import { CheckCircle2, MapPin, Save, X } from "lucide-vue-next";
import { computed, ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  initialData: { type: Object, default: () => ({}) },
  savedAddresses: { type: Array, default: () => [] },
  saving: { type: Boolean, default: false },
  title: { type: String, default: "Chỉnh sửa thông tin giao hàng" },
});

const emit = defineEmits(["update:modelValue", "save"]);

const dsTinh = ref([]);
const dsHuyen = ref([]);
const dsXa = ref([]);
const maTinhChon = ref("");
const maHuyenChon = ref("");
const dangTaiDiaPhuong = ref(false);
const form = ref(taoFormRong());
const errors = ref({});

function taoFormRong() {
  return {
    tenNguoiNhan: "",
    sdtNguoiNhan: "",
    tinhThanh: "",
    quanHuyen: "",
    phuongXa: "",
    diaChiCuThe: "",
  };
}

const diaChiDayDu = computed(() => [
  form.value.diaChiCuThe,
  form.value.phuongXa,
  form.value.quanHuyen,
  form.value.tinhThanh,
].filter(Boolean).join(", "));

function dongModal() {
  emit("update:modelValue", false);
}

function tachDiaChiDayDu(value) {
  const parts = String(value || "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
  if (parts.length < 4) {
    return {
      diaChiCuThe: parts.join(", "),
      phuongXa: "",
      quanHuyen: "",
      tinhThanh: "",
    };
  }
  return {
    diaChiCuThe: parts.slice(0, -3).join(", "),
    phuongXa: parts.at(-3),
    quanHuyen: parts.at(-2),
    tinhThanh: parts.at(-1),
  };
}

function timTheoTen(danhSach, ten) {
  const normalized = String(ten || "").trim().toLocaleLowerCase("vi");
  return danhSach.find((item) => String(item.name || "").trim().toLocaleLowerCase("vi") === normalized);
}

async function taiDanhSachTinh() {
  if (dsTinh.value.length) return;
  const response = await fetch("https://provinces.open-api.vn/api/p/");
  if (!response.ok) throw new Error("Không thể tải danh sách tỉnh/thành phố");
  dsTinh.value = await response.json();
}

async function taiHuyenTheoTinh(code) {
  if (!code) {
    dsHuyen.value = [];
    return;
  }
  const response = await fetch(`https://provinces.open-api.vn/api/p/${code}?depth=2`);
  if (!response.ok) throw new Error("Không thể tải danh sách quận/huyện");
  const data = await response.json();
  dsHuyen.value = data.districts || [];
}

async function taiXaTheoHuyen(code) {
  if (!code) {
    dsXa.value = [];
    return;
  }
  const response = await fetch(`https://provinces.open-api.vn/api/d/${code}?depth=2`);
  if (!response.ok) throw new Error("Không thể tải danh sách phường/xã");
  const data = await response.json();
  dsXa.value = data.wards || [];
}

async function dienDuLieuDiaPhuong() {
  dangTaiDiaPhuong.value = true;
  try {
    await taiDanhSachTinh();
    const tinh = timTheoTen(dsTinh.value, form.value.tinhThanh);
    maTinhChon.value = tinh?.code ? String(tinh.code) : "";
    if (!tinh) return;

    await taiHuyenTheoTinh(tinh.code);
    const huyen = timTheoTen(dsHuyen.value, form.value.quanHuyen);
    maHuyenChon.value = huyen?.code ? String(huyen.code) : "";
    if (!huyen) return;
    await taiXaTheoHuyen(huyen.code);
  } finally {
    dangTaiDiaPhuong.value = false;
  }
}

async function khoiTaoForm() {
  const diaChi = tachDiaChiDayDu(props.initialData?.diaChiGiaoHang);
  form.value = {
    tenNguoiNhan: props.initialData?.tenNguoiNhan || "",
    sdtNguoiNhan: props.initialData?.sdtNguoiNhan || "",
    ...diaChi,
  };
  errors.value = {};
  maTinhChon.value = "";
  maHuyenChon.value = "";
  dsHuyen.value = [];
  dsXa.value = [];
  try {
    await dienDuLieuDiaPhuong();
  } catch {
    errors.value.diaPhuong = "Không thể tải dữ liệu địa phương. Bạn có thể thử mở lại form.";
  }
}

async function chonTinh(event) {
  const code = event.target.value;
  maTinhChon.value = code;
  maHuyenChon.value = "";
  dsXa.value = [];
  form.value.tinhThanh = dsTinh.value.find((item) => String(item.code) === code)?.name || "";
  form.value.quanHuyen = "";
  form.value.phuongXa = "";
  try {
    await taiHuyenTheoTinh(code);
  } catch {
    errors.value.diaPhuong = "Không thể tải danh sách quận/huyện.";
  }
}

async function chonHuyen(event) {
  const code = event.target.value;
  maHuyenChon.value = code;
  form.value.quanHuyen = dsHuyen.value.find((item) => String(item.code) === code)?.name || "";
  form.value.phuongXa = "";
  try {
    await taiXaTheoHuyen(code);
  } catch {
    errors.value.diaPhuong = "Không thể tải danh sách phường/xã.";
  }
}

async function dungDiaChiDaLuu(diaChi) {
  form.value = {
    tenNguoiNhan: diaChi.hoTen || "",
    sdtNguoiNhan: diaChi.sdt || "",
    tinhThanh: diaChi.tinhThanh || "",
    quanHuyen: diaChi.quanHuyen || "",
    phuongXa: diaChi.phuongXa || "",
    diaChiCuThe: diaChi.diaChiCuThe || "",
  };
  errors.value = {};
  try {
    await dienDuLieuDiaPhuong();
  } catch {
    errors.value.diaPhuong = "Đã chọn địa chỉ, nhưng không thể tải lại danh sách địa phương.";
  }
}

function validate() {
  const next = {};
  if (!form.value.tenNguoiNhan.trim()) next.tenNguoiNhan = "Vui lòng nhập tên người nhận.";
  if (!/^(0|\+84)[35789]\d{8}$/.test(form.value.sdtNguoiNhan.trim())) {
    next.sdtNguoiNhan = "Số điện thoại không đúng định dạng.";
  }
  if (!form.value.tinhThanh) next.tinhThanh = "Vui lòng chọn tỉnh/thành phố.";
  if (!form.value.quanHuyen) next.quanHuyen = "Vui lòng chọn quận/huyện.";
  if (!form.value.phuongXa) next.phuongXa = "Vui lòng chọn phường/xã.";
  if (!form.value.diaChiCuThe.trim()) next.diaChiCuThe = "Vui lòng nhập địa chỉ cụ thể.";
  errors.value = next;
  return Object.keys(next).length === 0;
}

function luu() {
  if (!validate()) return;
  emit("save", {
    tenNguoiNhan: form.value.tenNguoiNhan.trim(),
    sdtNguoiNhan: form.value.sdtNguoiNhan.trim(),
    diaChiGiaoHang: diaChiDayDu.value,
  });
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) khoiTaoForm();
  },
);
</script>

<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-[80] flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
      @click.self="dongModal"
    >
      <div class="max-h-[92vh] w-full max-w-3xl overflow-y-auto rounded-lg bg-white shadow-2xl">
        <header class="sticky top-0 z-10 flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <div class="flex items-center gap-3">
            <span class="flex size-10 items-center justify-center rounded-lg bg-rose-50 text-[#B82220]">
              <MapPin class="size-5" />
            </span>
            <div>
              <h2 class="text-lg font-bold text-slate-900">{{ title }}</h2>
              <p class="text-xs text-slate-500">Có thể chỉnh sửa đến khi đơn chuyển sang giao hàng.</p>
            </div>
          </div>
          <button type="button" class="rounded-full p-2 text-slate-400 hover:bg-slate-100" @click="dongModal">
            <X class="size-5" />
          </button>
        </header>

        <div class="space-y-6 p-6">
          <section v-if="savedAddresses.length" class="space-y-3">
            <h3 class="text-sm font-bold text-slate-700">Địa chỉ đã lưu</h3>
            <div class="grid gap-3 md:grid-cols-2">
              <button
                v-for="diaChi in savedAddresses"
                :key="diaChi.id"
                type="button"
                class="relative min-h-28 rounded-lg border border-slate-200 p-4 text-left transition hover:border-rose-300 hover:bg-rose-50/40"
                @click="dungDiaChiDaLuu(diaChi)"
              >
                <span v-if="diaChi.laMacDinh" class="absolute right-3 top-3 inline-flex items-center gap-1 text-[11px] font-bold text-emerald-600">
                  <CheckCircle2 class="size-3.5" /> Mặc định
                </span>
                <p class="pr-20 text-sm font-bold text-slate-800">{{ diaChi.hoTen }}</p>
                <p class="mt-1 text-xs text-slate-500">{{ diaChi.sdt }}</p>
                <p class="mt-2 text-xs leading-5 text-slate-600">
                  {{ diaChi.diaChiCuThe }}, {{ diaChi.phuongXa }}, {{ diaChi.quanHuyen }}, {{ diaChi.tinhThanh }}
                </p>
              </button>
            </div>
          </section>

          <section class="grid gap-4 border-t border-slate-100 pt-5 md:grid-cols-2">
            <label class="space-y-1.5">
              <span class="text-sm font-semibold text-slate-700">Tên người nhận <b class="text-rose-500">*</b></span>
              <input v-model="form.tenNguoiNhan" class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
              <p v-if="errors.tenNguoiNhan" class="text-xs text-rose-500">{{ errors.tenNguoiNhan }}</p>
            </label>
            <label class="space-y-1.5">
              <span class="text-sm font-semibold text-slate-700">Số điện thoại <b class="text-rose-500">*</b></span>
              <input v-model="form.sdtNguoiNhan" type="tel" class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
              <p v-if="errors.sdtNguoiNhan" class="text-xs text-rose-500">{{ errors.sdtNguoiNhan }}</p>
            </label>
            <label class="space-y-1.5">
              <span class="text-sm font-semibold text-slate-700">Tỉnh/Thành phố <b class="text-rose-500">*</b></span>
              <select :value="maTinhChon" :disabled="dangTaiDiaPhuong" class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300" @change="chonTinh">
                <option value="">-- Chọn tỉnh/thành --</option>
                <option v-for="tinh in dsTinh" :key="tinh.code" :value="tinh.code">{{ tinh.name }}</option>
              </select>
              <p v-if="errors.tinhThanh" class="text-xs text-rose-500">{{ errors.tinhThanh }}</p>
            </label>
            <label class="space-y-1.5">
              <span class="text-sm font-semibold text-slate-700">Quận/Huyện <b class="text-rose-500">*</b></span>
              <select :value="maHuyenChon" :disabled="!maTinhChon" class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 disabled:opacity-50" @change="chonHuyen">
                <option value="">-- Chọn quận/huyện --</option>
                <option v-for="huyen in dsHuyen" :key="huyen.code" :value="huyen.code">{{ huyen.name }}</option>
              </select>
              <p v-if="errors.quanHuyen" class="text-xs text-rose-500">{{ errors.quanHuyen }}</p>
            </label>
            <label class="space-y-1.5">
              <span class="text-sm font-semibold text-slate-700">Phường/Xã <b class="text-rose-500">*</b></span>
              <select v-model="form.phuongXa" :disabled="!maHuyenChon" class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 disabled:opacity-50">
                <option value="">-- Chọn phường/xã --</option>
                <option v-for="xa in dsXa" :key="xa.code" :value="xa.name">{{ xa.name }}</option>
              </select>
              <p v-if="errors.phuongXa" class="text-xs text-rose-500">{{ errors.phuongXa }}</p>
            </label>
            <label class="space-y-1.5 md:col-span-2">
              <span class="text-sm font-semibold text-slate-700">Địa chỉ cụ thể <b class="text-rose-500">*</b></span>
              <input v-model="form.diaChiCuThe" placeholder="Số nhà, tên đường..." class="h-11 w-full rounded-lg border border-slate-200 bg-slate-50 px-4 text-sm outline-none focus:border-rose-300 focus:bg-white" />
              <p v-if="errors.diaChiCuThe" class="text-xs text-rose-500">{{ errors.diaChiCuThe }}</p>
            </label>
            <div v-if="diaChiDayDu" class="rounded-lg bg-slate-50 px-4 py-3 text-sm text-slate-600 md:col-span-2">
              Giao đến: <span class="font-semibold text-slate-800">{{ diaChiDayDu }}</span>
            </div>
            <p v-if="errors.diaPhuong" class="text-xs text-amber-600 md:col-span-2">{{ errors.diaPhuong }}</p>
          </section>
        </div>

        <footer class="sticky bottom-0 flex justify-end gap-3 border-t border-slate-100 bg-white px-6 py-4">
          <button type="button" class="h-10 rounded-lg border border-slate-200 px-5 text-sm font-bold text-slate-600 hover:bg-slate-50" @click="dongModal">Hủy</button>
          <button type="button" :disabled="saving" class="inline-flex h-10 items-center gap-2 rounded-lg bg-[#B82220] px-5 text-sm font-bold text-white hover:bg-[#a51d1b] disabled:opacity-50" @click="luu">
            <Save class="size-4" />
            {{ saving ? "Đang lưu..." : "Lưu thông tin" }}
          </button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>
