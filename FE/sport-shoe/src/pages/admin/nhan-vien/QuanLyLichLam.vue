<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { layDanhSachNhanVien } from "../../../services/nhan-vien.js";
import {
  layLichLamViec,
  phanCa,
  xepCaTuDong,
} from "../../../services/lich-lam.js";
import { getDisplayErrorMessage } from "../../../utils/error-message.js";
import { showSuccess, showError, showConfirm } from "../../../utils/alert.js";
import Card from "../../../components/ui/Card.vue";
import Button from "../../../components/ui/Button.vue";
import {
  ArrowLeft,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Clock,
  RefreshCw,
  Sparkles,
  Trash2,
  UserPlus,
  Users,
  X,
} from "lucide-vue-next";

// ─── Types ────────────────────────────────────────────────────────────────────

interface LichLamRecord {
  id: string;
  nhanVienId: string;
  ngay: string; // "YYYY-MM-DD"
  ca: CaKey; // "sang" | "chieu" | "toi"
}

interface NhanVien {
  id: string;
  ma: string;
  hoTen: string;
  hinhAnh?: string;
}

type CaKey = "sang" | "chieu" | "toi";

// ─── Constants ────────────────────────────────────────────────────────────────

const MAX_PER_SHIFT = 3;

const CA_LIST: {
  key: CaKey;
  label: string;
  time: string;
  color: string;
  ring: string;
}[] = [
    {
      key: "sang",
      label: "Ca Sáng",
      time: "08:00 – 12:00",
      color: "bg-amber-50 border-amber-200 text-amber-700",
      ring: "ring-amber-300",
    },
    {
      key: "chieu",
      label: "Ca Chiều",
      time: "13:00 – 17:00",
      color: "bg-sky-50 border-sky-200 text-sky-700",
      ring: "ring-sky-300",
    },
    {
      key: "toi",
      label: "Ca Tối",
      time: "18:00 – 22:00",
      color: "bg-violet-50 border-violet-200 text-violet-700",
      ring: "ring-violet-300",
    },
  ];

const DAY_LABELS = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

// ─── Route ────────────────────────────────────────────────────────────────────

const route = useRoute();
const router = useRouter();

const employeeId = computed(() => route.params.id as string | undefined);

// ─── State ────────────────────────────────────────────────────────────────────

const dangTai = ref(false);
const loiTrang = ref("");
const dangXepTuDong = ref(false);
const dangXoa = ref<string | null>(null); // id của record đang xóa

const danhSachNV = ref<NhanVien[]>([]);
const lichLam = ref<LichLamRecord[]>([]);
const tuanOffset = ref(0);

// ─── Computed: khoảng tuần (T2–CN) ───────────────────────────────────────────

const tuNgay = computed(() => {
  const now = new Date();
  const dow = now.getDay(); // 0=CN
  const monday = new Date(now);
  monday.setDate(
    now.getDate() - (dow === 0 ? 6 : dow - 1) + tuanOffset.value * 7,
  );
  monday.setHours(0, 0, 0, 0);
  return monday;
});

const denNgay = computed(() => {
  const end = new Date(tuNgay.value);
  end.setDate(tuNgay.value.getDate() + 6);
  return end;
});

const toanBoNgay = computed(() =>
  Array.from({ length: 7 }, (_, i) => {
    const d = new Date(tuNgay.value);
    d.setDate(tuNgay.value.getDate() + i);
    return d;
  }),
);

// ─── Helpers ──────────────────────────────────────────────────────────────────

function fmtIso(d: Date): string {
  return d.toISOString().slice(0, 10);
}

function fmtDisplay(d: Date): string {
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}`;
}

function isToday(d: Date): boolean {
  return fmtIso(d) === fmtIso(new Date());
}

const tieuDeNgay = computed(() => {
  return `${fmtDisplay(tuNgay.value)} – ${fmtDisplay(denNgay.value)}/${denNgay.value.getFullYear()}`;
});

function nhanVienTrongCa(ngay: Date, ca: CaKey): NhanVien[] {
  const ngayStr = fmtIso(ngay);
  const records = lichLam.value.filter(
    (r) => r.ngay === ngayStr && r.ca === ca,
  );

  if (employeeId.value) {
    return records.some((r) => r.nhanVienId === employeeId.value)
      ? danhSachNV.value.filter((nv) => nv.id === employeeId.value)
      : [];
  }

  const ids = new Set(records.map((r) => r.nhanVienId));
  return danhSachNV.value.filter((nv) => ids.has(nv.id));
}

function soNhanVienTrongCa(ngay: Date, ca: CaKey): number {
  const ngayStr = fmtIso(ngay);
  return lichLam.value.filter((r) => r.ngay === ngayStr && r.ca === ca).length;
}

function recordCuaNVTrongCa(
  nhanVienId: string,
  ngay: Date,
  ca: CaKey,
): LichLamRecord | undefined {
  const ngayStr = fmtIso(ngay);
  return lichLam.value.find(
    (r) => r.nhanVienId === nhanVienId && r.ngay === ngayStr && r.ca === ca,
  );
}

function avatarUrl(nv: NhanVien): string {
  return (
    nv.hinhAnh ||
    `https://ui-avatars.com/api/?name=${encodeURIComponent(nv.hoTen)}&background=f1f5f9&color=475569&size=64`
  );
}

// ─── Modal thêm nhân viên vào ca ─────────────────────────────────────────────

const modal = ref({
  open: false,
  ngay: null as Date | null,
  ca: "sang" as CaKey,
  nhanVienId: "",
  dang: false,
});

function moModal(ngay: Date, ca: CaKey) {
  modal.value = {
    open: true,
    ngay,
    ca,
    nhanVienId: employeeId.value ?? "",
    dang: false,
  };
}

function dongModal() {
  modal.value.open = false;
  modal.value.nhanVienId = "";
}

/** NV có thể thêm: đang hoạt động, chưa có trong ca này */
const nhanVienCoTheChon = computed(() => {
  if (!modal.value.ngay) return [];
  const ngayStr = fmtIso(modal.value.ngay);
  const daCoIds = new Set(
    lichLam.value
      .filter((r) => r.ngay === ngayStr && r.ca === modal.value.ca)
      .map((r) => r.nhanVienId),
  );
  return danhSachNV.value.filter((nv) => !daCoIds.has(nv.id));
});

async function xacNhanThem() {
  if (!modal.value.nhanVienId) {
    showError("Vui lòng chọn nhân viên.");
    return;
  }
  if (!modal.value.ngay) return;

  // Client-side guard (backend cũng enforce)
  const soHienTai = soNhanVienTrongCa(modal.value.ngay, modal.value.ca);
  if (soHienTai >= MAX_PER_SHIFT) {
    showError(`Ca này đã đủ ${MAX_PER_SHIFT} nhân viên. Không thể thêm.`);
    return;
  }

  modal.value.dang = true;
  try {
    await phanCa({
      nhanVienId: modal.value.nhanVienId,
      ngay: fmtIso(modal.value.ngay),
      ca: modal.value.ca,
    });
    showSuccess("Thêm nhân viên vào ca thành công.");
    dongModal();
    await taiLichLam();
  } catch (e) {
    showError(
      getDisplayErrorMessage(e, "Không thể phân ca. Vui lòng thử lại."),
    );
  } finally {
    modal.value.dang = false;
  }
}

// ─── Xóa nhân viên khỏi ca ───────────────────────────────────────────────────

async function xoaKhoiCa(nv: NhanVien, ngay: Date, ca: CaKey) {
  const ok = await showConfirm(
    `Xóa "${nv.hoTen}" khỏi ${CA_LIST.find((c) => c.key === ca)?.label} ngày ${fmtDisplay(ngay)}?`,
  );
  if (!ok) return;

  const record = recordCuaNVTrongCa(nv.id, ngay, ca);
  if (!record) return;

  dangXoa.value = record.id;
  try {
    // Gửi ca = null để backend xóa
    await phanCa({ nhanVienId: nv.id, ngay: fmtIso(ngay), ca: null as any });
    showSuccess("Đã xóa nhân viên khỏi ca.");
    await taiLichLam();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể xóa khỏi ca."));
  } finally {
    dangXoa.value = null;
  }
}

// ─── Xếp ca tự động ──────────────────────────────────────────────────────────

async function xepTuDong() {
  const ok = await showConfirm(
    `Tự động xếp lịch tuần ${tieuDeNgay.value}?\n` +
    `Lịch cũ trong tuần sẽ bị xóa và hệ thống sẽ phân công lại (tối đa ${MAX_PER_SHIFT} NV/ca).`,
  );
  if (!ok) return;

  dangXepTuDong.value = true;
  try {
    await xepCaTuDong(fmtIso(tuNgay.value), fmtIso(denNgay.value));
    showSuccess("Xếp lịch tự động thành công.");
    await taiLichLam();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể xếp lịch tự động."));
  } finally {
    dangXepTuDong.value = false;
  }
}

// ─── API ─────────────────────────────────────────────────────────────────────

async function taiDanhSachNV() {
  try {
    const ds = await layDanhSachNhanVien({ trangThai: 1 });
    danhSachNV.value = Array.isArray(ds) ? ds : [];
  } catch {
    danhSachNV.value = [];
  }
}

async function taiLichLam() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    const data = await layLichLamViec(
      fmtIso(tuNgay.value),
      fmtIso(denNgay.value),
    );
    lichLam.value = Array.isArray(data) ? data : [];
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải lịch làm việc.");
  } finally {
    dangTai.value = false;
  }
}

function quayLai() {
  router.push(
    employeeId.value
      ? { name: "admin-nhan-vien-chi-tiet", params: { id: employeeId.value } }
      : { name: "admin-nhan-vien" },
  );
}

// ─── Watcher & Lifecycle ──────────────────────────────────────────────────────

watch(tuanOffset, () => taiLichLam());

onMounted(async () => {
  await taiDanhSachNV();
  await taiLichLam();
});
</script>

<template>
  <div class="schedule-page space-y-5">
    <!-- ───── HEADER ───── -->
    <section class="flex flex-wrap items-center gap-3">
      <button @click="router.push({ name: 'admin-nhan-vien' })"
        class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-600 transition hover:bg-slate-200">
        <ArrowLeft class="h-5 w-5" />
      </button>

      <div class="flex-1">
        <h1 class="text-[26px] font-bold tracking-tight text-slate-900">
          Quản lý lịch làm việc
        </h1>
        <p class="text-sm text-slate-400">
          Phân ca và theo dõi giờ làm cho nhân viên
        </p>
      </div>

      <!-- Tuần hiển thị -->
      <div
        class="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm">
        <CalendarDays class="h-4 w-4 text-slate-400" />
        {{ tieuDeNgay }}
      </div>

      <button @click="xepTuDong" class="admin-btn-soft gap-2">
        <Sparkles class="h-4 w-4" /> Xếp ca tự động
      </button>
      <button @click="moModal(toanBoNgay[0], 'sang')" class="admin-btn-primary gap-2">
        <UserPlus class="h-4 w-4" /> Thêm ca mới
      </button>
    </section>

    <!-- <div class="grid gap-5">
     
      <section class="schedule-board rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
  
        <div class="mb-5 flex flex-wrap items-center gap-3">
          <h2 class="flex-1 text-base font-bold text-slate-800">
            Bảng lịch làm việc theo tuần
          </h2>

          <button @click="() => tuanOffset--"
            class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
            <ChevronLeft class="h-4 w-4" />
          </button>
          <button @click="() => tuanOffset = 0"
            class="rounded-xl bg-primary px-4 py-1.5 text-sm font-semibold text-white transition hover:bg-primary-hover shadow-sm">
            Hôm nay
          </button>
          <button @click="() => tuanOffset++"
            class="flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600 hover:bg-slate-200 transition">
            <ChevronRight class="h-4 w-4" />
          </button>
        </div>

        <div v-if="loiTrang" class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">
          {{ loiTrang }}
        </div>

        
      </section>
    </div> -->

    <!-- ───── MODAL THÊM / SỬA CA ───── -->
    <!-- ── Header ── -->
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div class="flex items-center gap-3">
        <button type="button"
          class="flex h-9 w-9 shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 shadow-sm transition hover:bg-slate-50 hover:text-slate-800"
          @click="quayLai">
          <ArrowLeft class="h-4 w-4" />
        </button>
        <div>
          <h1 class="text-lg font-bold text-slate-800">
            {{
              employeeId ? "Lịch làm việc nhân viên" : "Quản lý lịch làm việc"
            }}
          </h1>
          <p class="text-sm text-slate-400">
            Tuần: {{ tieuDeNgay }} &nbsp;·&nbsp; Tối đa {{ MAX_PER_SHIFT }} nhân
            viên / ca
          </p>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <Button variant="soft" :loading="dangXepTuDong" @click="xepTuDong">
          <template #prefix>
            <Sparkles class="h-4 w-4" />
          </template>
          Xếp tự động
        </Button>
        <Button variant="soft" :loading="dangTai" @click="taiLichLam">
          <template #prefix>
            <RefreshCw class="h-4 w-4" />
          </template>
          Làm mới
        </Button>
      </div>
    </div>

    <!-- ── Điều hướng tuần ── -->
    <Card>
      <div class="flex items-center justify-between py-1">
        <button type="button"
          class="flex h-9 w-9 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 shadow-sm transition hover:bg-slate-50 hover:text-slate-800"
          @click="tuanOffset--">
          <ChevronLeft class="h-4 w-4" />
        </button>

        <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
          <CalendarDays class="h-4 w-4 text-violet-500" />
          Tuần {{ tieuDeNgay }}
        </div>

        <button type="button"
          class="flex h-9 w-9 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 shadow-sm transition hover:bg-slate-50 hover:text-slate-800"
          @click="tuanOffset++">
          <ChevronRight class="h-4 w-4" />
        </button>
      </div>
    </Card>

    <!-- ── Lỗi ── -->
    <div v-if="loiTrang" class="rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600">
      {{ loiTrang }}
    </div>

    <!-- ── Skeleton loading ── -->
    <div v-if="dangTai" class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-7">
      <div v-for="i in 7" :key="i" class="h-72 animate-pulse rounded-3xl bg-slate-100"></div>
    </div>

    <!-- ── Lưới lịch 7 cột (T2–CN) ── -->
    <div v-else class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-7">
      <div v-for="(ngay, dayIdx) in toanBoNgay" :key="dayIdx"
        class="rounded-2xl border bg-white p-2 shadow-sm transition" :class="isToday(ngay)
            ? 'border-violet-300 ring-2 ring-violet-100'
            : 'border-slate-100'
          ">
        <!-- Header ngày -->
        <div class="mb-3 flex items-center justify-between">
          <span class="text-xs font-bold uppercase tracking-wider text-slate-400">
            {{ DAY_LABELS[dayIdx] }}
          </span>
          <span class="flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold" :class="isToday(ngay)
              ? 'bg-violet-500 text-white'
              : 'bg-slate-100 text-slate-600'
            ">
            {{ ngay.getDate() }}
          </span>
        </div>

        <!-- 3 ca -->
        <div class="space-y-2">
          <div v-for="ca in CA_LIST" :key="ca.key" class="rounded-2xl border p-2" :class="ca.color">
            <!-- Header ca -->
            <div class="mb-1.5 flex items-center justify-between gap-1">
              <div class="min-w-0">
                <p class="text-[11px] font-bold leading-tight">
                  {{ ca.label }}
                </p>
                <p class="text-[10px] opacity-60">{{ ca.time }}</p>
              </div>
              <!-- Badge số lượng -->
              <span class="shrink-0 rounded-full px-1.5 py-0.5 text-[10px] font-bold" :class="soNhanVienTrongCa(ngay, ca.key) >= MAX_PER_SHIFT
                  ? 'bg-current/20 opacity-90'
                  : 'bg-current/10 opacity-70'
                ">
                {{ soNhanVienTrongCa(ngay, ca.key) }}/{{ MAX_PER_SHIFT }}
              </span>
            </div>

            <!-- Danh sách NV trong ca -->
            <div class="space-y-1">
              <div v-for="nv in nhanVienTrongCa(ngay, ca.key)" :key="nv.id"
                class="group flex items-center gap-1.5 rounded-xl bg-white/70 px-2 py-1 transition hover:bg-white">
                <img :src="avatarUrl(nv)" :alt="nv.hoTen"
                  class="h-5 w-5 shrink-0 rounded-full object-cover ring-1 ring-white" />
                <span class="min-w-0 flex-1 truncate text-[11px] font-medium">
                  {{ nv.hoTen }}
                </span>
                <!-- Nút xóa -->
                <button type="button" :disabled="dangXoa === recordCuaNVTrongCa(nv.id, ngay, ca.key)?.id
                  "
                  class="ml-auto shrink-0 rounded-md p-0.5 opacity-0 transition hover:bg-rose-100 hover:text-rose-600 group-hover:opacity-100 disabled:opacity-30"
                  :title="`Xóa ${nv.hoTen} khỏi ca`" @click.stop="xoaKhoiCa(nv, ngay, ca.key)">
                  <X class="h-3 w-3" />
                </button>
              </div>

              <!-- Empty state -->
              <div v-if="nhanVienTrongCa(ngay, ca.key).length === 0"
                class="rounded-xl border border-dashed border-current/20 py-1 text-center text-[10px] opacity-40">
                Chưa có NV
              </div>
            </div>

            <!-- Nút thêm NV -->
            <button v-if="
              !employeeId && soNhanVienTrongCa(ngay, ca.key) < MAX_PER_SHIFT
            " type="button"
              class="mt-1.5 flex w-full items-center justify-center gap-1 rounded-xl border border-dashed border-current/30 py-1 text-[11px] font-semibold opacity-50 transition hover:opacity-100"
              @click="moModal(ngay, ca.key)">
              <UserPlus class="h-3 w-3" />
              Thêm
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Modal thêm NV vào ca ── -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="modal.open"
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4 backdrop-blur-sm"
          @click.self="dongModal">
          <div class="w-full max-w-md rounded-3xl bg-white p-6 shadow-2xl">
            <!-- Header modal -->
            <div class="mb-5 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-violet-50 text-violet-600">
                  <Users class="h-5 w-5" />
                </div>
                <div>
                  <h3 class="font-bold text-slate-800">
                    Thêm nhân viên vào ca
                  </h3>
                  <p class="text-sm text-slate-400">
                    {{CA_LIST.find((c) => c.key === modal.ca)?.label}}
                    – {{ modal.ngay ? fmtDisplay(modal.ngay) : "" }}
                  </p>
                </div>
              </div>
              <button type="button"
                class="flex h-8 w-8 items-center justify-center rounded-xl border border-slate-200 text-slate-400 hover:bg-slate-50 hover:text-slate-600"
                @click="dongModal">
                <X class="h-4 w-4" />
              </button>
            </div>

            <!-- Thông tin ca -->
            <div class="mb-4 flex items-center gap-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600">
              <Clock class="h-4 w-4 shrink-0 text-slate-400" />
              <span>{{CA_LIST.find((c) => c.key === modal.ca)?.time}}</span>
              <span class="ml-auto shrink-0 text-xs font-semibold text-slate-500">
                {{
                  modal.ngay ? soNhanVienTrongCa(modal.ngay, modal.ca) : 0
                }}/{{ MAX_PER_SHIFT }} nhân viên
              </span>
            </div>

            <!-- Chọn NV -->
            <div class="mb-5">
              <label class="mb-1.5 block text-sm font-semibold text-slate-700">
                Chọn nhân viên
                <span class="ml-1 text-xs font-normal text-slate-400">
                  ({{ nhanVienCoTheChon.length }} có thể chọn)
                </span>
              </label>
              <select v-model="modal.nhanVienId"
                class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-violet-400 focus:bg-white focus:ring-4 focus:ring-violet-100">
                <option value="">-- Chọn nhân viên --</option>
                <option v-for="nv in nhanVienCoTheChon" :key="nv.id" :value="nv.id">
                  {{ nv.hoTen }} &nbsp;({{ nv.ma }})
                </option>
              </select>

              <div v-if="nhanVienCoTheChon.length === 0"
                class="mt-2 flex items-center gap-2 rounded-xl bg-rose-50 px-3 py-2 text-xs text-rose-600">
                <X class="h-3.5 w-3.5 shrink-0" />
                Tất cả nhân viên đã được phân vào ca này hoặc ca đã đầy.
              </div>
            </div>

            <!-- Actions -->
            <div class="flex justify-end gap-3">
              <Button variant="soft" @click="dongModal">Hủy</Button>
              <Button variant="primary" :loading="modal.dang"
                :disabled="!modal.nhanVienId || nhanVienCoTheChon.length === 0" @click="xacNhanThem">
                <template #prefix>
                  <UserPlus class="h-4 w-4" />
                </template>
                Thêm vào ca
              </Button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
