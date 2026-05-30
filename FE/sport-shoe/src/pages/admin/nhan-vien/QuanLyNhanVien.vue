<script setup lang="ts">
import { ref, computed, watch, onActivated, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  doiTrangThaiNhanVien,
  layDanhSachNhanVien,
} from "../../../services/nhan-vien";
import { useAdminSession } from "../../../composable/useAdminSession";
import { exportRowsToExcel } from "../../../utils/export-excel";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import AdminQuickStatusAction from "../../../components/common/AdminQuickStatusAction.vue";
import Card from "../../../components/ui/Card.vue";
import Table from "../../../components/ui/Table.vue";
import Button from "../../../components/ui/Button.vue";
import Badge from "../../../components/ui/Badge.vue";
import { getDisplayErrorMessage } from "../../../utils/error-message";

import {
  CheckCircle2,
  CalendarDays,
  Eye,
  FileSpreadsheet,
  Filter,
  Plus,
  RotateCcw,
  Search,
  TriangleAlert,
  Users,
  X,
} from "lucide-vue-next";

const router = useRouter();
const { adminSession } = useAdminSession();
const EMPLOYEE_CREATE_TOAST_KEY = "admin-nhan-vien-toast";

const danhSach = ref([]);
const dangTai = ref(false);
const loiTrang = ref("");
const boLoc = ref({ keyword: "", vaiTro: "", trangThai: "" });
const toastTimer = null;


const dsVaiTro = [
  { label: "Tất cả vai trò", value: "" },
  { label: "Admin", value: "1" },
  { label: "Bán hàng", value: "2" },

];

const dsTrangThai = [
  { label: "Tất cả", value: "" },
  { label: "Hoạt động", value: "1", color: "bg-emerald-50 text-emerald-600" },
  { label: "Khóa", value: "0", color: "bg-rose-50 text-rose-600" },
];

function mauTrangThai(trangThai: number) {
  return trangThai === 1
    ? "bg-emerald-50 text-emerald-600"
    : "bg-rose-50 text-rose-600";
}

function dinhDangNgay(ngay: string) {
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(ngay));
}

import { showSuccess, showError, showConfirm } from "../../../utils/alert";



function taiThongBaoDieuHuong() {
  if (typeof window === "undefined") return;
  const raw = window.sessionStorage.getItem(EMPLOYEE_CREATE_TOAST_KEY);
  if (!raw) return;
  window.sessionStorage.removeItem(EMPLOYEE_CREATE_TOAST_KEY);

  try {
    const payload = JSON.parse(raw);
    if (payload?.loai === "error") {
      showError(typeof payload?.noiDung === "string" ? payload.noiDung : "", payload?.tieuDe || "Lỗi");
    } else {
      showSuccess(typeof payload?.noiDung === "string" ? payload.noiDung : "", payload?.tieuDe || "Thao tác thành công");
    }
  } catch {
    showSuccess("Đã tạo nhân viên mới");
  }
}

function chuanHoaChuoi(value: unknown) {
  return String(value ?? "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d");
}

function hienThiVaiTro(nv: any) {
  const normalizedRole = chuanHoaChuoi(nv?.tenVaiTro);
  if (normalizedRole.includes("admin") || normalizedRole.includes("quan tri")) {
    return "Admin";
  }
  if (normalizedRole.includes("ban hang") || Number(nv?.vaiTro) === 2) {
    return "Bán hàng";
  }
  if (normalizedRole.includes("kho") || Number(nv?.vaiTro) === 3) {
    return "Kho";
  }
  return nv?.tenVaiTro || "—";
}

function hienThiTrangThai(nv: any) {
  const normalizedStatus = chuanHoaChuoi(nv?.tenTrangThai);
  if (normalizedStatus.includes("hoat dong") || Number(nv?.trangThai) === 1) {
    return "Hoạt động";
  }
  if (normalizedStatus.includes("khoa") || Number(nv?.trangThai) === 0) {
    return "Khóa";
  }
  return nv?.tenTrangThai || "—";
}

// Phân trang
const soPhanTuMotTrang = ref(5);
const trangHienTai = ref(1);
const pageSizeOptions = [5, 10, 20, 50];
const tongSoTrang = computed(
  () => Math.ceil(danhSach.value.length / soPhanTuMotTrang.value) || 1,
);
const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSach.value.slice(start, start + soPhanTuMotTrang.value);
});

watch(danhSach, () => {
  trangHienTai.value = 1;
});
watch(soPhanTuMotTrang, () => {
  trangHienTai.value = 1;
});

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    danhSach.value = await layDanhSachNhanVien({
      keyword: boLoc.value.keyword || undefined,
      vaiTro: boLoc.value.vaiTro ? Number(boLoc.value.vaiTro) : undefined,
      trangThai:
        boLoc.value.trangThai !== ""
          ? Number(boLoc.value.trangThai)
          : undefined,
    });
  } catch (e) {
    loiTrang.value = getDisplayErrorMessage(e, "Không thể tải danh sách nhân viên");
  } finally {
    dangTai.value = false;
  }
}

function lamMoiBoLoc() {
  boLoc.value = { keyword: "", vaiTro: "", trangThai: "" };
}

function xemChiTiet(id: string) {
  router.push({ name: "admin-nhan-vien-chi-tiet", params: { id } });
}

function themMoi() {
  router.push({ name: "admin-nhan-vien-them" });
}

function quanLyLichLam(id: string) {
  router.push({ name: "admin-nhan-vien-lich-lam", params: { id } });
}

function xuatExcel() {
  if (!danhSach.value.length) {
    showError("Không có dữ liệu để xuất Excel.");
    return;
  }

  exportRowsToExcel({
    filename: "quan-ly-nhan-vien",
    sheetName: "NhanVien",
    columns: [
      { label: "STT", value: (_, index) => index + 1 },
      { label: "Mã NV", key: "ma" },
      { label: "Họ tên", key: "hoTen" },
      { label: "Tài khoản", key: "tenDangNhap" },
      { label: "Email", key: "email" },
      { label: "Giới tính", value: (row) => row.gioiTinh || "—" },
      { label: "Số điện thoại", value: (row) => row.sdt || "—" },
      { label: "Địa chỉ", value: (row) => row.diaChi || "—" },
      { label: "Vai trò", value: (row) => hienThiVaiTro(row) },
      { label: "Trạng thái", value: (row) => hienThiTrangThai(row) },
    ],
    rows: danhSach.value,
  });
}

// Debounce search
let timer: ReturnType<typeof setTimeout>;
watch(
  () => boLoc.value,
  () => {
    clearTimeout(timer);
    timer = setTimeout(taiDanhSach, 300);
  },
  { deep: true },
);

const dangDoiTrangThai = ref<string | null>(null);

async function capNhatTrangThai(nv: any) {
  // Kiểm tra quyền: Chỉ Admin mới được đổi trạng thái
  if (adminSession.value.vaiTro !== "Quản trị viên") {
    showError("Chỉ có Quản trị viên mới có quyền thực hiện hành động này.");
    return;
  }

  // Không cho phép khóa tài khoản Admin khác
  if (nv.tenVaiTro === "Admin") {
    showError("Không thể thay đổi trạng thái của tài khoản Quản trị viên.");
    return;
  }

  const message =
    nv.trangThai === 1
      ? `Bạn có chắc muốn cho nhân viên "${nv.hoTen}" nghỉ làm?`
      : `Bạn có chắc muốn kích hoạt lại nhân viên "${nv.hoTen}"?`;

  if (!(await showConfirm(message))) return;

  dangDoiTrangThai.value = nv.id;
  try {
    await doiTrangThaiNhanVien(nv.id, nv.trangThai === 1 ? 0 : 1);
    await taiDanhSach();
  } catch (e) {
    showError(getDisplayErrorMessage(e, "Không thể cập nhật trạng thái nhân viên"));
  } finally {
    dangDoiTrangThai.value = null;
  }
}


onMounted(() => {
  taiThongBaoDieuHuong();
  taiDanhSach();
});

onActivated(() => {
  taiThongBaoDieuHuong();
});
</script>

<template>
  <div class="space-y-5">


    <!-- Header -->
    <section>
    </section>

    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-600">
            <Filter class="h-5 w-5" />
          </div>
          <h2 class="admin-section-title">Bộ lọc</h2>
        </div>
      </template>

  <div class="flex flex-col gap-5">
    <!-- HÀNG 1: CHỨA TẤT CẢ CÁC Ô NHẬP LIỆU (Search + Vai Trò + Trạng Thái) -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-12 items-end">
      <!-- Ô Tìm kiếm (Chiếm 6 cột trên màn hình lớn) -->
      <div class="lg:col-span-6">
        <div class="relative">
          <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            v-model="boLoc.keyword"
            type="text"
            placeholder="Tìm theo mã, họ tên, tài khoản, SĐT..."
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
          />
        </div>
      </div>

      <!-- Ô Vai trò (Chiếm 3 cột) -->
      <div class="lg:col-span-3">
        <select
          v-model="boLoc.vaiTro"
          class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
        >
          <option v-for="vt in dsVaiTro" :key="vt.value" :value="vt.value">
            {{ vt.label }}
          </option>
        </select>
      </div>

      <!-- Ô Trạng thái (Chiếm 3 cột) -->
      <div class="lg:col-span-3">
        <select
          v-model="boLoc.trangThai"
          class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition duration-200 focus:border-primary/50 focus:bg-white focus:ring-4 focus:ring-primary/10"
        >
          <option v-for="tt in dsTrangThai" :key="tt.value" :value="tt.value">
            {{ tt.label }}
          </option>
        </select>
      </div>
    </div>

    <!-- HÀNG 2: CHỨA CÁC NÚT BẤM - CĂN PHẢI -->
    <div class="flex flex-wrap items-center justify-end gap-3">
      <Button variant="soft" @click="lamMoiBoLoc">
        <template #prefix><RotateCcw class="h-4 w-4" /></template>
        Đặt lại bộ lọc
      </Button>
      <Button variant="soft" @click="xuatExcel">
        <template #prefix><FileSpreadsheet class="h-4 w-4" /></template>
        Xuất Excel
      </Button>
      <Button variant="primary" @click="themMoi">
        <template #prefix><Plus class="h-4 w-4" /></template>
        Thêm nhân viên
      </Button>
    </div>
  </div>
    </Card>

    <!-- Danh sách -->
    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div
            class="flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/5 text-primary"
          >
            <Users class="h-5 w-5" />
          </div>
          <div>
            <h2 class="admin-section-title">
              Danh sách nhân viên
            </h2>
          </div>
        </div>
      </template>

      <div
        v-if="loiTrang"
        class="mb-4 rounded-2xl bg-rose-50 px-4 py-3 text-sm font-medium text-rose-600"
      >
        {{ loiTrang }}
      </div>

      <div class="admin-table-scroll">
        <Table>
          <template #header>
              <th class="px-3 py-3 whitespace-nowrap">STT</th>
              <th class="px-3 py-3 whitespace-nowrap">Ảnh</th>
              <th class="px-3 py-3 whitespace-nowrap">Mã NV</th>
              <th class="px-3 py-3 whitespace-nowrap">Họ tên</th>
              <th class="px-3 py-3 whitespace-nowrap">Tài khoản</th>
              <th class="px-3 py-3 whitespace-nowrap">Giới tính</th>
              <th class="px-3 py-3 whitespace-nowrap">Số điện thoại</th>
              <th class="px-3 py-3 whitespace-nowrap">Địa chỉ</th>
              <th class="px-3 py-3 whitespace-nowrap">Vai trò</th>
              <th class="px-3 py-3 whitespace-nowrap">Trạng thái</th>
              <th class="px-3 py-3 text-center whitespace-nowrap">Hành động</th>
          </template>
          <template #body>
            <tr v-if="dangTai">
              <td colspan="11" class="py-10 text-center text-sm text-slate-400">
                Đang tải dữ liệu nhân viên...
              </td>
            </tr>
            <tr v-else-if="!danhSachPhanTrang.length">
              <td colspan="11" class="py-10 text-center text-sm text-slate-400">
                Không có nhân viên phù hợp.
              </td>
            </tr>
            <tr
              v-for="(nv, index) in danhSachPhanTrang"
              :key="nv.id"
              class="bg-white text-slate-700 shadow-sm ring-1 ring-slate-100"
            >
              <td class="rounded-l-2xl px-3 py-3 font-semibold">
                {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
              </td>
              <td class="px-3 py-3">
                <img
                  :src="
                    nv.hinhAnh ||
                    'https://ui-avatars.com/api/?name=' +
                      encodeURIComponent(nv.hoTen) +
                      '&background=f1f5f9&color=475569&size=64'
                  "
                  :alt="nv.hoTen"
                  class="h-10 w-10 rounded-full object-cover ring-2 ring-slate-100"
                />
              </td>
              <td class="px-3 py-3 font-semibold text-slate-800">
                <div class="truncate" :title="nv.ma">
                  {{ nv.ma }}
                </div>
              </td>
              <td class="px-3 py-3 font-semibold text-slate-800">
                <div class="truncate" :title="nv.hoTen">
                  {{ nv.hoTen }}
                </div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                <div class="text-sm">
                  
                  <span class="font-semibold text-slate-800" :title="nv.tenDangNhap">{{ nv.tenDangNhap }}</span>
                </div>
                <div class="text-xs mt-0.5">
                 
                  <span class="text-slate-500 select-all" :title="nv.email">{{ nv.email }}</span>
                </div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                <div class="truncate" :title="nv.gioiTinh">
                  {{ nv.gioiTinh || "—" }}
                </div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                <div class="truncate" :title="nv.sdt || '—'">
                  {{ nv.sdt || "—" }}
                </div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                <div class="min-w-[180px] max-w-[320px] break-words whitespace-normal leading-normal text-sm" :title="nv.diaChi || '—'">
                  {{ nv.diaChi || "—" }}
                </div>
              </td>
              <td class="px-3 py-3 text-slate-600">
                <div class="truncate" :title="hienThiVaiTro(nv)">
                  {{ hienThiVaiTro(nv) }}
                </div>
              </td>
              <td class="px-3 py-3">
                <Badge :variant="nv.trangThai === 1 ? 'success' : 'danger'">
                  {{ hienThiTrangThai(nv) }}
                </Badge>
              </td>
              <td class="rounded-r-2xl px-3 py-3 align-top text-center">
                <div class="flex items-center justify-center gap-0.5">
                  <AdminQuickStatusAction
                    v-if="adminSession.vaiTro === 'Quản trị viên'"
                    :loading="dangDoiTrangThai === nv.id"
                    :disabled="nv.tenVaiTro === 'Admin'"
                    :disabled-title="'Không thể đổi trạng thái Admin'"
                    :action-label="nv.trangThai === 1 ? 'Cho nghỉ làm' : 'Kích hoạt nhân viên'"
                    :intent="nv.trangThai === 1 ? 'deactivate' : 'activate'"
                    @toggle="capNhatTrangThai(nv)"
                  />
                  <!-- <button
                    type="button"
                    @click="quanLyLichLam(nv.id)"
                    class="admin-table-action text-violet-500 hover:text-violet-700"
                    title="Quản lý lịch làm"
                  >
                    <CalendarDays :size="14" />
                  </button> -->
                  <button
                    type="button"
                    @click="xemChiTiet(nv.id)"
                    class="admin-table-action text-slate-600 hover:text-slate-900"
                    title="Xem chi tiết"
                  >
                    <Eye :size="14" />
                  </button>
                </div>
              </td>
            </tr>
          </template>
        </Table>
      </div>

      <template #footer>
        <AdminTableFooter
          :current-page="trangHienTai"
          :page-size="soPhanTuMotTrang"
          :page-size-options="pageSizeOptions"
          :total-items="danhSach.length"
          :total-pages="tongSoTrang"
          compact
          show-refresh
          @refresh="taiDanhSach"
          @update:current-page="trangHienTai = $event"
          @update:page-size="soPhanTuMotTrang = $event"
        />
      </template>
    </Card>
  </div>
</template>
