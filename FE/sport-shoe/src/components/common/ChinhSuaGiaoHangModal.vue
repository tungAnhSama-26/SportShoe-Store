<script setup>
import { Info, LoaderCircle, MapPin, UserRound, X } from "lucide-vue-next";
import { computed, ref, watch } from "vue";
import { layTinhThanhHaiCap, layPhuongXaHaiCap } from "../../services/dia-chi";
import { chuanHoaDiaChi, dinhDangDiaChi, doiChieuDiaChiHaiCap, layMaDonViDiaChi, taoPayloadDiaChi } from "../../utils/dia-chi";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: "Chỉnh sửa thông tin nhận hàng" },
  initialData: { type: Object, default: () => ({}) },
  savedAddresses: { type: Array, default: () => [] },
  saving: { type: Boolean, default: false },
});
const emit = defineEmits(["update:modelValue", "save"]);

const dsTinh = ref([]);
const dsPhuongXa = ref([]);
const dangTai = ref(false);
const errors = ref({});
const form = ref(taoForm());
const diaChiDaDoiChieu = ref(null);

function taoForm() {
  return {
    tenNguoiNhan: "", sdtNguoiNhan: "", email: "", ghiChu: "", diaChiId: "new",
    tinhThanhCode: "", tinhThanh: "", phuongXaCode: "", phuongXa: "", diaChiCuThe: "",
  };
}

const diaChiDaLuu = computed(() => props.savedAddresses.find((item) => item.id === form.value.diaChiId));
const diaChiHienTai = computed(() => diaChiDaLuu.value ? (diaChiDaDoiChieu.value || chuanHoaDiaChi(diaChiDaLuu.value)) : chuanHoaDiaChi(form.value));

async function chonTinh(code) {
  const tinh = dsTinh.value.find((item) => layMaDonViDiaChi(item) === String(code));
  form.value.tinhThanhCode = tinh ? layMaDonViDiaChi(tinh) : "";
  form.value.tinhThanh = tinh?.ten || "";
  form.value.phuongXaCode = "";
  form.value.phuongXa = "";
  dsPhuongXa.value = tinh ? await layPhuongXaHaiCap(layMaDonViDiaChi(tinh)) : [];
}

function chonPhuongXa(code) {
  const phuongXa = dsPhuongXa.value.find((item) => String(item.code) === String(code));
  form.value.phuongXaCode = phuongXa?.code || "";
  form.value.phuongXa = phuongXa?.ten || "";
}

async function napDiaChi(address) {
  const ketQua = await doiChieuDiaChiHaiCap(address, dsTinh.value, layPhuongXaHaiCap);
  Object.assign(form.value, ketQua.diaChi);
  dsPhuongXa.value = ketQua.danhSachPhuongXa;
  return ketQua.diaChi;
}

async function khoiTao() {
  dangTai.value = true;
  errors.value = {};
  diaChiDaDoiChieu.value = null;
  form.value = {
    ...taoForm(),
    tenNguoiNhan: props.initialData?.tenNguoiNhan || "",
    sdtNguoiNhan: props.initialData?.soDienThoaiNguoiNhan || props.initialData?.sdtNguoiNhan || "",
    email: props.initialData?.email || "",
    ghiChu: props.initialData?.ghiChu || "",
  };
  try {
    dsTinh.value = await layTinhThanhHaiCap();
    const initialAddress = chuanHoaDiaChi(props.initialData?.diaChiGiaoHang);
    const matched = props.savedAddresses.find((item) => dinhDangDiaChi(item) === dinhDangDiaChi(initialAddress));
    const selected = matched || (!dinhDangDiaChi(initialAddress) && (props.savedAddresses.find((item) => item.laMacDinh) || props.savedAddresses[0]));
    if (selected) {
      form.value.diaChiId = selected.id;
      form.value.tenNguoiNhan ||= selected.hoTen || "";
      form.value.sdtNguoiNhan ||= selected.sdt || "";
      diaChiDaDoiChieu.value = await napDiaChi(selected);
    } else {
      diaChiDaDoiChieu.value = null;
      await napDiaChi(initialAddress);
    }
  } catch (error) {
    errors.value.diaPhuong = error?.message || "Không thể tải danh mục địa chỉ";
  } finally {
    dangTai.value = false;
  }
}

function validate() {
  const next = {};
  if (!form.value.tenNguoiNhan.trim()) next.tenNguoiNhan = "Vui lòng nhập tên người nhận.";
  if (!/^0[35789]\d{8}$/.test(form.value.sdtNguoiNhan.trim())) next.sdtNguoiNhan = "Số điện thoại không hợp lệ.";
  if (form.value.diaChiId === "new") {
    if (!form.value.tinhThanh) next.tinhThanh = "Vui lòng chọn tỉnh/thành.";
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
    diaChiGiaoHang: taoPayloadDiaChi(diaChiHienTai.value),
    ghiChu: form.value.ghiChu.trim(),
  });
}

watch(() => props.modelValue, (visible) => { if (visible) khoiTao(); }, { immediate: true });
watch(() => form.value.diaChiId, async (id) => {
  const selected = props.savedAddresses.find((item) => item.id === id);
  if (!selected) {
    diaChiDaDoiChieu.value = null;
    return;
  }
  form.value.tenNguoiNhan = selected.hoTen || form.value.tenNguoiNhan;
  form.value.sdtNguoiNhan = selected.sdt || form.value.sdtNguoiNhan;
  diaChiDaDoiChieu.value = await napDiaChi(selected);
});
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="fixed inset-0 z-[80] flex items-center justify-center bg-slate-950/60 p-3 backdrop-blur-sm sm:p-6">
      <div class="flex max-h-[92vh] w-full max-w-2xl flex-col overflow-hidden rounded-3xl bg-white shadow-[0_30px_90px_rgba(15,23,42,0.35)]">
        <header class="flex shrink-0 items-center justify-between border-b border-slate-100 bg-gradient-to-r from-rose-50 via-white to-white px-5 py-4 sm:px-7">
          <div class="flex min-w-0 items-center gap-3">
            <div class="flex size-10 shrink-0 items-center justify-center rounded-2xl bg-rose-100 text-[#B82220]">
              <Info class="size-5" />
            </div>
            <div class="min-w-0">
              <h2 class="truncate text-lg font-bold text-slate-900">{{ title }}</h2>
              <p class="mt-0.5 text-xs text-slate-500">Cập nhật người nhận và địa chỉ giao hàng</p>
            </div>
          </div>
          <button type="button" aria-label="Đóng" class="flex size-9 shrink-0 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-700" @click="emit('update:modelValue', false)">
            <X class="size-5" />
          </button>
        </header>

        <div class="min-h-0 flex-1 space-y-5 overflow-y-auto bg-slate-50/60 px-5 py-5 sm:px-7">
          <section class="rounded-2xl border border-slate-200/80 bg-white p-4 shadow-sm sm:p-5">
            <div class="mb-4 flex items-center gap-2.5">
              <div class="flex size-8 items-center justify-center rounded-xl bg-sky-50 text-sky-600"><UserRound class="size-4" /></div>
              <div>
                <h3 class="text-sm font-bold text-slate-800">Thông tin liên hệ</h3>
                <p class="text-xs text-slate-400">Thông tin dùng khi giao và liên hệ nhận hàng</p>
              </div>
            </div>
            <div class="grid gap-4 sm:grid-cols-2">
              <label class="space-y-1.5">
                <span class="text-sm font-semibold text-slate-600">Tên người nhận <b class="text-rose-500">*</b></span>
                <input v-model="form.tenNguoiNhan" class="h-11 w-full rounded-xl border border-slate-200 bg-slate-50 px-3.5 text-sm text-slate-800 outline-none transition focus:border-rose-300 focus:bg-white focus:ring-4 focus:ring-rose-100" />
                <small v-if="errors.tenNguoiNhan" class="text-xs font-medium text-rose-500">{{ errors.tenNguoiNhan }}</small>
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-semibold text-slate-600">Số điện thoại <b class="text-rose-500">*</b></span>
                <input v-model="form.sdtNguoiNhan" class="h-11 w-full rounded-xl border border-slate-200 bg-slate-50 px-3.5 text-sm text-slate-800 outline-none transition focus:border-rose-300 focus:bg-white focus:ring-4 focus:ring-rose-100" />
                <small v-if="errors.sdtNguoiNhan" class="text-xs font-medium text-rose-500">{{ errors.sdtNguoiNhan }}</small>
              </label>
            </div>
            <label class="mt-4 block space-y-1.5">
              <span class="text-sm font-semibold text-slate-600">Email</span>
              <input v-model="form.email" type="email" class="h-11 w-full rounded-xl border border-slate-200 bg-slate-50 px-3.5 text-sm text-slate-800 outline-none transition focus:border-rose-300 focus:bg-white focus:ring-4 focus:ring-rose-100" />
            </label>
          </section>

          <section class="rounded-2xl border border-slate-200/80 bg-white p-4 shadow-sm sm:p-5">
            <div class="mb-4 flex items-center gap-2.5">
              <div class="flex size-8 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600"><MapPin class="size-4" /></div>
              <div>
                <h3 class="text-sm font-bold text-slate-800">Địa chỉ giao hàng</h3>
                <p class="text-xs text-slate-400">Chọn địa chỉ đã lưu hoặc nhập địa chỉ khác</p>
              </div>
            </div>

            <div v-if="savedAddresses.length" class="space-y-2.5">
              <label
                v-for="item in savedAddresses"
                :key="item.id"
                class="flex cursor-pointer gap-3 rounded-2xl border p-3.5 transition"
                :class="form.diaChiId === item.id ? 'border-rose-300 bg-rose-50/70 ring-2 ring-rose-100' : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50'"
              >
                <input v-model="form.diaChiId" type="radio" :value="item.id" class="mt-1 size-4 shrink-0 accent-[#B82220]" />
                <span class="min-w-0 flex-1">
                  <span class="flex flex-wrap items-center gap-2">
                    <b class="text-sm text-slate-800">{{ item.hoTen || form.tenNguoiNhan || 'Người nhận' }}</b>
                    <span v-if="item.laMacDinh" class="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-700">Mặc định</span>
                    <span v-if="item.sdt" class="text-xs text-slate-500">{{ item.sdt }}</span>
                  </span>
                  <span class="mt-1 block text-sm leading-5 text-slate-600">{{ dinhDangDiaChi(item) }}</span>
                </span>
              </label>
              <label
                class="flex cursor-pointer items-center gap-3 rounded-2xl border p-3.5 transition"
                :class="form.diaChiId === 'new' ? 'border-rose-300 bg-rose-50/70 ring-2 ring-rose-100' : 'border-dashed border-slate-300 hover:bg-slate-50'"
              >
                <input v-model="form.diaChiId" type="radio" value="new" class="size-4 accent-[#B82220]" />
                <span class="text-sm font-semibold text-slate-700">Sử dụng địa chỉ khác</span>
              </label>
            </div>

            <div v-if="form.diaChiId === 'new'" class="mt-4 grid gap-4 rounded-2xl bg-slate-50 p-4 sm:grid-cols-2">
              <label class="space-y-1.5">
                <span class="text-sm font-semibold text-slate-600">Tỉnh/Thành phố <b class="text-rose-500">*</b></span>
                <select :value="form.tinhThanhCode" :disabled="dangTai" class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3.5 text-sm outline-none transition focus:border-rose-300 focus:ring-4 focus:ring-rose-100 disabled:cursor-wait disabled:opacity-60" @change="chonTinh($event.target.value)"><option value="">Chọn tỉnh/thành</option><option v-for="item in dsTinh" :key="layMaDonViDiaChi(item)" :value="layMaDonViDiaChi(item)">{{ item.ten }}</option></select>
                <small v-if="errors.tinhThanh" class="text-xs font-medium text-rose-500">{{ errors.tinhThanh }}</small>
              </label>
              <label class="space-y-1.5">
                <span class="text-sm font-semibold text-slate-600">Phường/Xã <b class="text-rose-500">*</b></span>
                <select :value="form.phuongXaCode" :disabled="!form.tinhThanhCode || dangTai" class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3.5 text-sm outline-none transition focus:border-rose-300 focus:ring-4 focus:ring-rose-100 disabled:cursor-not-allowed disabled:opacity-60" @change="chonPhuongXa($event.target.value)"><option value="">Chọn phường/xã</option><option v-for="item in dsPhuongXa" :key="item.code" :value="item.code">{{ item.ten }}</option></select>
                <small v-if="errors.phuongXa" class="text-xs font-medium text-rose-500">{{ errors.phuongXa }}</small>
              </label>
              <label class="space-y-1.5 sm:col-span-2">
                <span class="text-sm font-semibold text-slate-600">Địa chỉ cụ thể <b class="text-rose-500">*</b></span>
                <input v-model="form.diaChiCuThe" placeholder="Số nhà, tên đường..." class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3.5 text-sm outline-none transition focus:border-rose-300 focus:ring-4 focus:ring-rose-100" />
                <small v-if="errors.diaChiCuThe" class="text-xs font-medium text-rose-500">{{ errors.diaChiCuThe }}</small>
              </label>
            </div>

            <p v-if="errors.diaPhuong" class="mt-3 rounded-xl bg-amber-50 px-3 py-2 text-sm font-medium text-amber-700">{{ errors.diaPhuong }}</p>
          </section>

          <label class="block space-y-1.5 rounded-2xl border border-slate-200/80 bg-white p-4 shadow-sm sm:p-5">
            <span class="text-sm font-semibold text-slate-600">Ghi chú</span>
            <textarea v-model="form.ghiChu" rows="3" placeholder="Ghi chú giao hàng (nếu có)" class="w-full resize-none rounded-xl border border-slate-200 bg-slate-50 p-3.5 text-sm outline-none transition focus:border-rose-300 focus:bg-white focus:ring-4 focus:ring-rose-100" />
          </label>

          <div v-if="dangTai" class="flex items-center justify-center gap-2 py-2 text-sm font-medium text-slate-500">
            <LoaderCircle class="size-4 animate-spin" /> Đang tải địa chỉ...
          </div>
        </div>

        <footer class="flex shrink-0 items-center justify-end gap-3 border-t border-slate-100 bg-white px-5 py-4 sm:px-7">
          <button type="button" class="inline-flex h-11 min-w-24 items-center justify-center rounded-xl border border-slate-200 bg-white px-5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 hover:text-slate-900" @click="emit('update:modelValue', false)">Hủy</button>
          <button type="button" :disabled="saving || dangTai" class="inline-flex h-11 min-w-28 items-center justify-center gap-2 rounded-xl bg-[#B82220] px-5 text-sm font-semibold text-white shadow-lg shadow-rose-200 transition hover:bg-[#9f1d1b] disabled:cursor-not-allowed disabled:opacity-60" @click="luu">
            <LoaderCircle v-if="saving" class="size-4 animate-spin" />
            {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
          </button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>
