<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  ClipboardCheck,
  Eye,
  Filter,
  PackageCheck,
  RotateCcw,
  Search,
} from "lucide-vue-next";
import AdminTableFooter from "../../../components/common/AdminTableFooter.vue";
import Badge from "../../../components/ui/Badge.vue";
import Button from "../../../components/ui/Button.vue";
import Card from "../../../components/ui/Card.vue";
import Table from "../../../components/ui/Table.vue";
import { layDanhSachTraHang } from "../../../services/tra-hang";
import { getDisplayErrorMessage } from "../../../utils/error-message";

const router = useRouter();
const danhSach = ref([]);
const dangTai = ref(false);
const loiTrang = ref("");
const trangHienTai = ref(1);
const soPhanTuMotTrang = ref(5);
const boLoc = ref({
  keyword: "",
  trangThai: "",
});

const trangThaiOptions = [
  { value: "", label: "Tất cả trạng thái" },
  { value: 1, label: "Chờ duyệt" },
  { value: 2, label: "Chờ khách gửi hàng" },
  { value: 3, label: "Đang hoàn hàng" },
  { value: 4, label: "Đã nhận hàng" },
  { value: 5, label: "Đang kiểm tra" },
  { value: 6, label: "Chờ hoàn tiền" },
  { value: 7, label: "Hoàn tất" },
  { value: 8, label: "Từ chối" },
  { value: 9, label: "Đã hủy" },
  { value: 10, label: "Hoàn hàng thất bại" },
];

const thongKe = computed(() => [
  {
    label: "Tổng phiếu",
    value: danhSach.value.length,
    className: "bg-slate-50 text-slate-700",
  },
  {
    label: "Cần xử lý",
    value: danhSach.value.filter((item) => [1, 4, 5].includes(item.trangThai)).length,
    className: "bg-amber-50 text-amber-700",
  },
  {
    label: "Chờ hoàn tiền",
    value: danhSach.value.filter((item) => item.trangThai === 6).length,
    className: "bg-rose-50 text-rose-700",
  },
  {
    label: "Hoàn tất",
    value: danhSach.value.filter((item) => item.trangThai === 7).length,
    className: "bg-emerald-50 text-emerald-700",
  },
]);

const danhSachHienThi = computed(() => {
  const keyword = boLoc.value.keyword.trim().toLowerCase();
  const trangThai = Number(boLoc.value.trangThai || 0);
  return danhSach.value.filter((item) => {
    const dungTuKhoa =
      !keyword
      || [item.ma, item.maHoaDon, item.tenKhachHang, item.soDienThoaiKhachHang]
        .some((value) => String(value || "").toLowerCase().includes(keyword));
    const dungTrangThai = !trangThai || item.trangThai === trangThai;
    return dungTuKhoa && dungTrangThai;
  });
});

const tongSoTrang = computed(() =>
  Math.max(1, Math.ceil(danhSachHienThi.value.length / soPhanTuMotTrang.value)),
);

const danhSachPhanTrang = computed(() => {
  const start = (trangHienTai.value - 1) * soPhanTuMotTrang.value;
  return danhSachHienThi.value.slice(start, start + soPhanTuMotTrang.value);
});

watch([danhSachHienThi, soPhanTuMotTrang], () => {
  trangHienTai.value = 1;
});

async function taiDanhSach() {
  dangTai.value = true;
  loiTrang.value = "";
  try {
    danhSach.value = await layDanhSachTraHang();
  } catch (error) {
    loiTrang.value = getDisplayErrorMessage(
      error,
      "Không thể tải danh sách phiếu trả hàng",
    );
  } finally {
    dangTai.value = false;
  }
}

function datLaiBoLoc() {
  boLoc.value = { keyword: "", trangThai: "" };
}

function xemChiTiet(id) {
  router.push({ name: "admin-tra-hang-chi-tiet", params: { id } });
}

function dinhDangTien(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(Number(value || 0));
}

function dinhDangNgay(value) {
  if (!value) return "Chưa cập nhật";
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(value));
}

function badgeVariant(trangThai) {
  if ([7].includes(trangThai)) return "success";
  if ([1, 2, 3, 4, 5, 6].includes(trangThai)) return "warning";
  if ([8, 9, 10].includes(trangThai)) return "danger";
  return "default";
}

onMounted(taiDanhSach);
</script>

<template>
  <div class="space-y-5 pb-8">
    <section class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
      <div
        v-for="item in thongKe"
        :key="item.label"
        class="flex min-h-24 items-center justify-between rounded-2xl border border-slate-100 bg-white px-5 py-4 shadow-sm"
      >
        <div>
          <p class="text-sm font-medium text-slate-500">{{ item.label }}</p>
          <p class="mt-1 text-2xl font-bold text-slate-800">{{ item.value }}</p>
        </div>
        <div :class="['rounded-2xl p-3', item.className]">
          <PackageCheck class="h-5 w-5" />
        </div>
      </div>
    </section>

    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-rose-50 text-primary">
            <Filter class="h-5 w-5" />
          </div>
          <div>
            <h2 class="font-semibold text-slate-800">Bộ lọc phiếu trả hàng</h2>
            <p class="mt-0.5 text-xs text-slate-400">Tìm theo mã phiếu, hóa đơn hoặc khách hàng</p>
          </div>
        </div>
      </template>

      <div class="flex flex-col gap-3 lg:flex-row lg:items-end">
        <label class="flex-1 space-y-2">
          <span class="text-xs font-semibold text-slate-500">Tìm kiếm</span>
          <div class="relative">
            <Search class="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              v-model="boLoc.keyword"
              class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-11 pr-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
              placeholder="Mã phiếu / mã hóa đơn / khách hàng..."
            />
          </div>
        </label>

        <label class="w-full space-y-2 lg:w-72">
          <span class="text-xs font-semibold text-slate-500">Trạng thái</span>
          <select
            v-model="boLoc.trangThai"
            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-rose-300 focus:bg-white"
          >
            <option
              v-for="option in trangThaiOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </label>

        <Button variant="soft" @click="datLaiBoLoc">
          <template #prefix><RotateCcw class="h-4 w-4" /></template>
          Đặt lại
        </Button>
      </div>
    </Card>

    <Card>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-rose-50 text-primary">
            <ClipboardCheck class="h-5 w-5" />
          </div>
          <div>
            <h2 class="font-semibold text-slate-800">Danh sách phiếu trả hàng</h2>
            <p class="mt-0.5 text-xs text-slate-400">
              {{ danhSachHienThi.length }} phiếu phù hợp
            </p>
          </div>
        </div>
      </template>

      <div
        v-if="dangTai"
        class="flex min-h-56 items-center justify-center text-sm text-slate-400"
      >
        Đang tải danh sách trả hàng...
      </div>

      <div
        v-else-if="loiTrang"
        class="flex min-h-56 flex-col items-center justify-center gap-4 text-center"
      >
        <p class="text-sm font-medium text-rose-600">{{ loiTrang }}</p>
        <Button variant="soft" @click="taiDanhSach">Thử lại</Button>
      </div>

      <template v-else>
        <Table>
          <template #header>
            <th class="px-4 py-3 text-center">STT</th>
            <th class="px-4 py-3">Mã phiếu</th>
            <th class="px-4 py-3">Mã hóa đơn</th>
            <th class="px-4 py-3">Khách hàng</th>
            <th class="px-4 py-3 text-right">Tiền dự kiến</th>
            <th class="px-4 py-3">Ngày tạo</th>
            <th class="px-4 py-3 text-center">Trạng thái</th>
            <th class="px-4 py-3 text-center">Hành động</th>
          </template>

          <template #body>
            <tr
              v-for="(item, index) in danhSachPhanTrang"
              :key="item.id"
              class="transition hover:bg-slate-50/80"
            >
              <td class="px-4 py-4 text-center text-slate-500">
                {{ (trangHienTai - 1) * soPhanTuMotTrang + index + 1 }}
              </td>
              <td class="px-4 py-4 font-semibold text-slate-800">{{ item.ma }}</td>
              <td class="px-4 py-4 text-slate-600">{{ item.maHoaDon }}</td>
              <td class="px-4 py-4">
                <p class="font-medium text-slate-700">{{ item.tenKhachHang || "Khách vãng lai" }}</p>
                <p class="mt-1 text-xs text-slate-400">{{ item.soDienThoaiKhachHang || "Không có SĐT" }}</p>
              </td>
              <td class="px-4 py-4 text-right font-semibold text-primary">
                {{ dinhDangTien(item.tongTienDuKien) }}
              </td>
              <td class="px-4 py-4 text-slate-500">{{ dinhDangNgay(item.ngayTao) }}</td>
              <td class="px-4 py-4 text-center">
                <Badge :variant="badgeVariant(item.trangThai)">{{ item.tenTrangThai }}</Badge>
              </td>
              <td class="px-4 py-4 text-center">
                <button
                  type="button"
                  class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-slate-500 transition hover:bg-rose-50 hover:text-primary"
                  title="Xem chi tiết"
                  @click="xemChiTiet(item.id)"
                >
                  <Eye class="h-4 w-4" />
                </button>
              </td>
            </tr>
            <tr v-if="!danhSachPhanTrang.length">
              <td colspan="8" class="px-4 py-16 text-center text-sm text-slate-400">
                Không có phiếu trả hàng phù hợp.
              </td>
            </tr>
          </template>
        </Table>

        <AdminTableFooter
          v-model:current-page="trangHienTai"
          v-model:page-size="soPhanTuMotTrang"
          :total-items="danhSachHienThi.length"
          :total-pages="tongSoTrang"
          :page-size-options="[5, 10, 20]"
          compact
          show-refresh
          @refresh="taiDanhSach"
        />
      </template>
    </Card>
  </div>
</template>
