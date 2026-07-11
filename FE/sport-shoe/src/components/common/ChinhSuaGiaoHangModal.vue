<script setup>
import { Info, X } from "lucide-vue-next";
import { computed, ref, watch } from "vue";
import { layTinhGhn, layHuyenGhn, layXaGhn } from "../../services/gio-hang";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  initialData: { type: Object, default: () => ({}) },
  savedAddresses: { type: Array, default: () => [] },
  saving: { type: Boolean, default: false },
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
    email: "",
    diaChiId: "new",
    tinhThanh: "",
    quanHuyen: "",
    phuongXa: "",
    diaChiCuThe: "",
    ghiChu: "",
  };
}

const diaChiDayDu = computed(() => {
  if (form.value.diaChiId !== "new") {
    const selected = props.savedAddresses.find(a => a.id === form.value.diaChiId);
    if (selected) {
      return [
        selected.diaChiCuThe,
        selected.phuongXa,
        selected.quanHuyen,
        selected.tinhThanh
      ].filter(Boolean).join(", ");
    }
  }
  
  const phuongXaRaw = form.value.phuongXa;
  const quanHuyenRaw = dsHuyen.value.find((h) => String(h.id) === String(maHuyenChon.value))?.ten || "";
  const tinhThanhRaw = dsTinh.value.find((t) => String(t.id) === String(maTinhChon.value))?.ten || "";

  return [
    form.value.diaChiCuThe,
    phuongXaRaw,
    quanHuyenRaw,
    tinhThanhRaw,
  ].filter(Boolean).join(", ");
});

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
function chuanHoaTen(s) {
  return String(s || "")
    .normalize("NFD").replace(/[̀-ͯ]/g, "")
    .replace(/đ/gi, "d")
    .toLowerCase()
    .replace(/\b(tinh|thanh pho|tp|quan|huyen|thi xa|phuong|xa|thi tran)\b/g, " ")
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

function timTheoTen(danhSach, ten) {
  const muc = chuanHoaTen(ten);
  if (!muc) return undefined;
  return danhSach.find((o) => chuanHoaTen(o.ten) === muc)
    || danhSach.find((o) => {
      const t = chuanHoaTen(o.ten);
      return t && (t.includes(muc) || muc.includes(t));
    });
}

async function taiDanhSachTinh() {
  if (dsTinh.value.length) return;
  dsTinh.value = await layTinhGhn();
}

async function taiHuyenTheoTinh(code) {
  if (!code) {
    dsHuyen.value = [];
    return;
  }
  dsHuyen.value = await layHuyenGhn(code);
}

async function taiXaTheoHuyen(code) {
  if (!code) {
    dsXa.value = [];
    return;
  }
  dsXa.value = await layXaGhn(code);
}

async function dienDuLieuDiaPhuong() {
  dangTaiDiaPhuong.value = true;
  try {
    await taiDanhSachTinh();
    const tinh = timTheoTen(dsTinh.value, form.value.tinhThanh);
    maTinhChon.value = tinh?.id ? String(tinh.id) : "";
    if (!tinh) return;

    await taiHuyenTheoTinh(tinh.id);
    const huyen = timTheoTen(dsHuyen.value, form.value.quanHuyen);
    maHuyenChon.value = huyen?.id ? String(huyen.id) : "";
    if (!huyen) return;
    await taiXaTheoHuyen(huyen.id);
    const xa = timTheoTen(dsXa.value, form.value.phuongXa);
    if (xa) {
      form.value.phuongXa = xa.ten;
    } else if (form.value.phuongXa) {
      form.value.phuongXa = "";
    }
  } finally {
    dangTaiDiaPhuong.value = false;
  }
}

async function khoiTaoForm() {
  const diaChi = tachDiaChiDayDu(props.initialData?.diaChiGiaoHang);
  let defaultDiaChiId = "new";
  
  let defaultAddr = null;
  if (props.savedAddresses?.length > 0) {
    defaultAddr = props.savedAddresses.find(a => a.laMacDinh) || props.savedAddresses[0];
    defaultDiaChiId = defaultAddr.id;
  }

  form.value = {
    tenNguoiNhan: defaultAddr?.hoTen || props.initialData?.tenNguoiNhan || "",
    sdtNguoiNhan: defaultAddr?.sdt || props.initialData?.soDienThoaiNguoiNhan || props.initialData?.sdtNguoiNhan || "",
    email: defaultAddr?.email || props.initialData?.email || "",
    diaChiId: defaultDiaChiId,
    ghiChu: props.initialData?.ghiChu || "",
    ...diaChi,
  };
  
  errors.value = {};
  maTinhChon.value = "";
  maHuyenChon.value = "";
  dsHuyen.value = [];
  dsXa.value = [];
  
  try {
    await dienDuLieuDiaPhuong();
  } catch (err) {
    errors.value.diaPhuong = "Lỗi: " + (err.message || "Không xác định") + " (Vui lòng chụp màn hình lỗi này)";
  }
}

async function luuThongTin() {
  if (!validate()) return;
  
  emit("save", {
    tenNguoiNhan: form.value.tenNguoiNhan.trim(),
    sdtNguoiNhan: form.value.sdtNguoiNhan.trim(),
    email: form.value.email.trim(),
    diaChiGiaoHang: diaChiDayDu.value,
    ghiChu: form.value.ghiChu.trim()
  });
}

async function chonTinh(event) {
  const code = event.target.value;
  maTinhChon.value = code;
  maHuyenChon.value = "";
  dsXa.value = [];
  form.value.tinhThanh = dsTinh.value.find((item) => String(item.id) === code)?.ten || "";
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
  form.value.quanHuyen = dsHuyen.value.find((item) => String(item.id) === code)?.ten || "";
  form.value.phuongXa = "";
  try {
    await taiXaTheoHuyen(code);
  } catch {
    errors.value.diaPhuong = "Không thể tải danh sách phường/xã.";
  }
}

function validate() {
  const next = {};
  if (!form.value.tenNguoiNhan.trim()) next.tenNguoiNhan = "Vui lòng nhập tên người nhận.";
  if (!/^(0|\+84)[35789]\d{8}$/.test(form.value.sdtNguoiNhan.trim())) {
    next.sdtNguoiNhan = "Số điện thoại không đúng định dạng.";
  }
  
  if (form.value.diaChiId === "new") {
    if (!form.value.tinhThanh) next.tinhThanh = "Vui lòng chọn tỉnh/thành phố.";
    if (!form.value.quanHuyen) next.quanHuyen = "Vui lòng chọn quận/huyện.";
    if (!form.value.phuongXa) next.phuongXa = "Vui lòng chọn phường/xã.";
    if (!form.value.diaChiCuThe.trim()) next.diaChiCuThe = "Vui lòng nhập địa chỉ cụ thể.";
  }
  
  errors.value = next;
  return Object.keys(next).length === 0;
}

function luu() {
  if (!validate()) return;
  emit("save", {
    tenNguoiNhan: form.value.tenNguoiNhan.trim(),
    sdtNguoiNhan: form.value.sdtNguoiNhan.trim(),
    email: form.value.email.trim(),
    diaChiGiaoHang: diaChiDayDu.value,
    ghiChu: form.value.ghiChu.trim()
  });
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) khoiTaoForm();
  },
);

watch(
  () => form.value.diaChiId,
  (newId) => {
    if (newId !== "new") {
      const selected = props.savedAddresses.find(a => a.id === newId);
      if (selected) {
        if (selected.hoTen) form.value.tenNguoiNhan = selected.hoTen;
        if (selected.sdt) form.value.sdtNguoiNhan = selected.sdt;
        if (selected.email) form.value.email = selected.email;
      }
    }
  }
);
</script>

<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-[80] flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
    >
      <div class="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-md bg-white shadow-2xl">
        <header class="sticky top-0 z-10 flex items-center justify-between bg-white px-6 py-4">
          <div class="flex items-center gap-3">
            <span class="flex size-7 items-center justify-center rounded-full bg-[#B82220] text-white">
              <Info class="size-4" />
            </span>
            <h2 class="text-lg font-bold text-slate-800">Thông tin người nhận</h2>
          </div>
          <button type="button" class="rounded-full p-2 text-slate-400 hover:bg-slate-100 transition" @click="dongModal">
            <X class="size-5" />
          </button>
        </header>

        <div class="space-y-5 px-6 pb-2 mt-4">
          <!-- User info -->
          <div class="grid grid-cols-2 gap-4">
            <label class="space-y-1.5">
              <span class="text-[13px] text-slate-600">Tên người nhận <b class="text-red-500">*</b></span>
              <input v-model="form.tenNguoiNhan" class="h-[42px] w-full rounded border border-slate-200 px-3 text-[14px] text-slate-800 outline-none focus:border-[#B82220]" />
              <p v-if="errors.tenNguoiNhan" class="text-xs text-red-500">{{ errors.tenNguoiNhan }}</p>
            </label>
            <label class="space-y-1.5">
              <span class="text-[13px] text-slate-600">Số điện thoại <b class="text-red-500">*</b></span>
              <input v-model="form.sdtNguoiNhan" type="tel" class="h-[42px] w-full rounded border border-slate-200 px-3 text-[14px] text-slate-800 outline-none focus:border-[#B82220]" />
              <p v-if="errors.sdtNguoiNhan" class="text-xs text-red-500">{{ errors.sdtNguoiNhan }}</p>
            </label>
          </div>

          <label class="block space-y-1.5">
            <span class="text-[13px] text-slate-600">Email</span>
            <input v-model="form.email" type="email" placeholder="example@gmail.com" class="h-[42px] w-full rounded border border-slate-200 px-3 text-[14px] text-slate-800 outline-none focus:border-[#B82220]" />
          </label>

          <!-- Address selection box -->
          <div class="rounded-md border border-slate-100 bg-slate-50/50 p-5">
            <h3 class="mb-4 text-[14px] font-bold text-slate-800">Giao đến địa chỉ:</h3>
            <div class="space-y-4">
              <label 
                v-for="diaChi in savedAddresses" 
                :key="diaChi.id" 
                class="flex items-start gap-3 cursor-pointer"
              >
                <input 
                  type="radio" 
                  v-model="form.diaChiId" 
                  :value="diaChi.id" 
                  class="mt-1 size-4 accent-[#B82220]" 
                />
                <div>
                  <p class="text-[14px] font-bold text-slate-800">{{ diaChi.diaChiCuThe }}</p>
                  <p class="text-[13px] text-slate-500 mt-0.5">Phường/Xã: {{ diaChi.phuongXa }}, Tỉnh/TP: {{ diaChi.tinhThanh }}</p>
                  <span v-if="diaChi.laMacDinh" class="mt-1.5 inline-block rounded bg-red-100/80 px-2 py-0.5 text-[11px] font-medium text-red-700">
                    Mặc định
                  </span>
                </div>
              </label>

              <div :class="{'pt-4 mt-2 border-t border-slate-200/60': savedAddresses.length}">
                <label class="flex items-center gap-3 cursor-pointer">
                  <input 
                    type="radio" 
                    v-model="form.diaChiId" 
                    value="new" 
                    class="size-4 accent-[#B82220]" 
                  />
                  <span class="text-[14px] font-semibold text-[#B82220]">+ Giao đến một địa chỉ khác</span>
                </label>
              </div>
            </div>

            <!-- New address form -->
            <div v-if="form.diaChiId === 'new'" class="mt-5 grid gap-4 border-t border-slate-200/60 pt-5 md:grid-cols-2">
              <label class="space-y-1.5">
                <span class="text-[13px] text-slate-600">Tỉnh/Thành phố <b class="text-red-500">*</b></span>
                <select :value="maTinhChon" :disabled="dangTaiDiaPhuong" class="h-[42px] w-full rounded border border-slate-200 bg-white px-3 text-[14px] text-slate-800 outline-none focus:border-[#B82220]" @change="chonTinh">
                  <option value="">-- Chọn tỉnh/thành --</option>
                  <option v-for="tinh in dsTinh" :key="tinh.id" :value="tinh.id">{{ tinh.ten }}</option>
                </select>
                <p v-if="errors.tinhThanh" class="text-xs text-red-500">{{ errors.tinhThanh }}</p>
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] text-slate-600">Quận/Huyện <b class="text-red-500">*</b></span>
                <select :value="maHuyenChon" :disabled="!maTinhChon" class="h-[42px] w-full rounded border border-slate-200 bg-white px-3 text-[14px] text-slate-800 outline-none focus:border-[#B82220] disabled:bg-slate-50 disabled:opacity-70" @change="chonHuyen">
                  <option value="">-- Chọn quận/huyện --</option>
                  <option v-for="huyen in dsHuyen" :key="huyen.id" :value="huyen.id">{{ huyen.ten }}</option>
                </select>
                <p v-if="errors.quanHuyen" class="text-xs text-red-500">{{ errors.quanHuyen }}</p>
              </label>
              <label class="space-y-1.5">
                <span class="text-[13px] text-slate-600">Phường/Xã <b class="text-red-500">*</b></span>
                <select v-model="form.phuongXa" :disabled="!maHuyenChon" class="h-[42px] w-full rounded border border-slate-200 bg-white px-3 text-[14px] text-slate-800 outline-none focus:border-[#B82220] disabled:bg-slate-50 disabled:opacity-70">
                  <option value="">-- Chọn phường/xã --</option>
                  <option v-for="xa in dsXa" :key="xa.code" :value="xa.ten">{{ xa.ten }}</option>
                </select>
                <p v-if="errors.phuongXa" class="text-xs text-red-500">{{ errors.phuongXa }}</p>
              </label>
              <label class="space-y-1.5 md:col-span-2">
                <span class="text-[13px] text-slate-600">Địa chỉ cụ thể <b class="text-red-500">*</b></span>
                <input v-model="form.diaChiCuThe" placeholder="Số nhà, tên đường..." class="h-[42px] w-full rounded border border-slate-200 bg-white px-3 text-[14px] text-slate-800 outline-none focus:border-[#B82220]" />
                <p v-if="errors.diaChiCuThe" class="text-xs text-red-500">{{ errors.diaChiCuThe }}</p>
              </label>
              <p v-if="errors.diaPhuong" class="text-xs text-amber-600 md:col-span-2">{{ errors.diaPhuong }}</p>
            </div>
          </div>

          <label class="block space-y-1.5">
            <span class="text-[13px] text-slate-600">Ghi chú đơn hàng</span>
            <textarea v-model="form.ghiChu" rows="2" placeholder="Nhập ghi chú giao hàng (nếu có)" class="w-full rounded border border-slate-200 px-3 py-2 text-[14px] text-slate-800 outline-none focus:border-[#B82220] custom-scrollbar"></textarea>
          </label>
        </div>

        <footer class="sticky bottom-0 flex justify-end gap-3 bg-white px-6 py-5">
          <button type="button" class="h-[42px] rounded border border-slate-200 px-6 text-[14px] font-semibold text-slate-600 transition hover:bg-slate-50" @click="dongModal">Hủy</button>
          <button type="button" :disabled="saving" class="h-[42px] rounded bg-[#B82220] px-6 text-[14px] font-semibold text-white transition hover:bg-[#9b1c1c] disabled:opacity-50" @click="luu">
            {{ saving ? "Đang lưu..." : "Lưu thông tin" }}
          </button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>
